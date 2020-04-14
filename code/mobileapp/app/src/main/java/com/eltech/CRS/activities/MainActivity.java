package com.eltech.CRS.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.util.Size;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.eltech.CRS.R;
import com.eltech.CRS.adapters.ViewPagerAdapter;
import com.eltech.CRS.fragments.BrowseImageResultFragment;
import com.eltech.CRS.fragments.BrowseNoImageFragment;
import com.eltech.CRS.fragments.CameraFragment;
import com.eltech.CRS.resultStuff.CarInfo;
import com.eltech.CRS.utils.CameraService;
import com.eltech.CRS.utils.OpenImageExternalActivity;
import com.eltech.ImageProcessor.CarRecognizer;
import com.eltech.ImageProcessor.Recognition;
import com.google.android.material.tabs.TabLayout;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements BrowseNoImageFragment.OnFragmentInteractionListener,
                   BrowseImageResultFragment.OnFragmentInteractionListener,
                   CameraFragment.OnFragmentInteractionListener {

    public static final String LOG_TAG = "myLogs";

    private final int CONTEXT_MENU_ITEM_OPEN_ID = 0;
    private final int CONTEXT_MENU_ITEM_EXPORT_ID = 1;
    private final int CONTEXT_MENU_ITEM_ABOUT_ID = 2;

    private final int PERMISSIONS_REQUEST_CODE = 1;

    private ViewPagerAdapter viewPagerAdapter;
    private CameraManager cameraManager;
    private CameraService backCamera;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private CarRecognizer recognizer = new CarRecognizer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPerms();
        }

        viewPagerAdapter = new ViewPagerAdapter(this,
                                                getSupportFragmentManager(),
                                                checkCameraHardware());

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        ImageButton contextMenuButton = findViewById(R.id.contextMenuButton);
        contextMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openContextMenu(v);
            }
        });
        registerForContextMenu(contextMenuButton);

        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
//                Log.i(LOG_TAG, "orientation: " + orientation);
                backCamera.setDeviceOrientation(orientation);
            }
        };
        orientationEventListener.enable();
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        if(fragment instanceof BrowseNoImageFragment) {
            ((BrowseNoImageFragment) fragment).setOnFragmentInteractionListener(this);
        }

        if(fragment instanceof BrowseImageResultFragment) {
            ((BrowseImageResultFragment) fragment).setOnFragmentInteractionListener(this);
        }

        if(fragment instanceof CameraFragment) {
            ((CameraFragment) fragment).setOnFragmentInteractionListener(this);
        }
    }

    @Override
    public void onImageChosen(Uri imageUri) {
        try {
            onImageChosen(obtainBitmap(imageUri));
        } catch (FileNotFoundException ignored) {}
    }
    public void onImageChosen(Bitmap image) {
        switchTabToBrowse();
        recognise(image);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, CONTEXT_MENU_ITEM_OPEN_ID, 0, "Open image ...");
//        menu.add(1, CONTEXT_MENU_ITEM_EXPORT_ID, 0, "Export ...");
//        menu.add(2, CONTEXT_MENU_ITEM_ABOUT_ID, 0, "About");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        super.onContextItemSelected(item);

        switch (item.getItemId()) {
            case CONTEXT_MENU_ITEM_OPEN_ID:
                OpenImageExternalActivity.start(this);
                break;
            case CONTEXT_MENU_ITEM_EXPORT_ID:
                //TODO Export action
                break;
            case CONTEXT_MENU_ITEM_ABOUT_ID:
                //TODO "About" activity
                break;
            default:
                return false;
        }

        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(OpenImageExternalActivity.isSucceed(requestCode, resultCode, data)) {
            Uri selectedFile = data.getData();
            onImageChosen(selectedFile);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startCameraActivity(); // запускаем активность с камерой (ну или фрагмент)
            }
        }
    }

    @Override
    public void onPause() {
        if(backCamera.isOpen()) {
            backCamera.closeCamera();
        }
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    public CameraService getBackCamera() {
        return backCamera;
    }

    @Override
    public void requestStartBackgroundThread() {
        startBackgroundThread();
    }
    @Override
    public void requestStopBackgroundThread() {
        stopBackgroundThread();
    }

    private void recognise(Bitmap image) {
        List<Recognition> recognitions = recognizer.recognizeImage(image, getAssets());

        List<CarInfo> carInfoList = new ArrayList<>();
        for (Recognition recognition : recognitions) {
            carInfoList.add(new CarInfo(recognition.getImagePart(),
                                        "Color",
                                        "Mark",
                                        "ABC",
                                        recognition.getLocation()));
        }

        viewPagerAdapter.displayResult(this, image, carInfoList);
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

        backCamera.setBackgroundHandler(mBackgroundHandler);
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
            backCamera.setBackgroundHandler(null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPerms() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
        }
    }

    private boolean checkCameraHardware() {
        try {
            // выводим информацию по камере
            for (String cameraID : cameraManager.getCameraIdList()) {
                Log.i(LOG_TAG, "cameraID: "+cameraID);
                int id = Integer.parseInt(cameraID);

                // Получениe характеристик камеры
                CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraID);
                // Получения списка выходного формата, который поддерживает камера
                StreamConfigurationMap configurationMap =
                        cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                //  Определение какая камера куда смотрит
                int direction = cc.get(CameraCharacteristics.LENS_FACING);
                if (direction == CameraCharacteristics.LENS_FACING_FRONT) {
                    Log.i(LOG_TAG,"Camera with ID: " + cameraID +  "  is FRONT CAMERA  ");
                }
                if (direction == CameraCharacteristics.LENS_FACING_BACK) {
                    Log.i(LOG_TAG,"Camera with: ID " + cameraID +  " is BACK CAMERA  ");
                    backCamera = new CameraService(cameraManager, cameraID, this);
                }

                // Получения списка разрешений которые поддерживаются для формата jpeg
                Size[] sizesJPEG = configurationMap.getOutputSizes(ImageFormat.JPEG);
                if (sizesJPEG != null) {
                    for (Size item:sizesJPEG) {
                        Log.i(LOG_TAG, "w:"+item.getWidth()+" h:"+item.getHeight());
                    }
                }  else {
                    Log.i(LOG_TAG, "camera don`t support JPEG");
                }
            }
        } catch(CameraAccessException e){
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }

        return backCamera != null;
    }

    private Bitmap obtainBitmap(Uri imageUri) throws FileNotFoundException {
        Bitmap selectedImage;
        try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(imageUri, "r");
            selectedImage = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor());
        } catch (FileNotFoundException e) {
            throw e;
        }

        return selectedImage;
    }

    private void switchTabToBrowse() {
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.getTabAt(0).select();
    }
}
