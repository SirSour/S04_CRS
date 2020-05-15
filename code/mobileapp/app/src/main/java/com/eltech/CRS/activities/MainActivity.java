package com.eltech.CRS.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.eltech.CRS.R;
import com.eltech.CRS.adapters.ViewPagerAdapter;
import com.eltech.CRS.fragments.BrowseImageResultFragment;
import com.eltech.CRS.fragments.BrowseNoImageFragment;
import com.eltech.CRS.fragments.CameraFragment;
import com.eltech.CRS.resultStuff.CarInfo;
import com.eltech.CRS.utils.OpenImageExternalActivity;
import com.eltech.ImageProcessor.CarRecognizer;
import com.eltech.ImageProcessor.Recognition;
import com.google.android.material.tabs.TabLayout;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements BrowseNoImageFragment.OnFragmentInteractionListener,
                   BrowseImageResultFragment.OnFragmentInteractionListener {

    public static final String LOG_TAG = "myLogs";

    private final int CONTEXT_MENU_ITEM_OPEN_ID = 0;
    private final int CONTEXT_MENU_ITEM_EXPORT_ID = 1;
    private final int CONTEXT_MENU_ITEM_ABOUT_ID = 2;

    private final int PERMISSIONS_REQUEST_CODE = 1;

    private ViewPagerAdapter viewPagerAdapter;

    private CarRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPagerAdapter = new ViewPagerAdapter(this,
                                                getSupportFragmentManager(),
                                                true);

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

        recognizer = new CarRecognizer(getAssets());
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
        }
    }

    @Override
    public void onImageChosen(Uri imageUri) {
        try {
            onImageChosen(obtainBitmap(imageUri));
        } catch (FileNotFoundException ignored) {}
    }
    public void onImageChosen(Bitmap image) {
        Bitmap mutableImage = giveMutableBitmap(image);
        recognise(mutableImage);
        switchTabToBrowse();
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

    public CarRecognizer getRecognizer() {
        return recognizer;
    }

    private void recognise(Bitmap image) {
        List<Recognition> recognitions = recognizer.recognizeImageAndVisualise(image);
        List<CarInfo> carInfoList = new ArrayList<>();
        for (Recognition recognition : recognitions) {
            carInfoList.add(new CarInfo(recognition.getImagePart(),
                                        recognition.getColor(),
                                        recognition.getColorName(),
                                        recognition.getLocation()));
        }

        viewPagerAdapter.displayResult(this, image, carInfoList);
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

    private Bitmap giveMutableBitmap(Bitmap bitmap) {
        return bitmap.isMutable() ? bitmap : bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    private void switchTabToBrowse() {
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.getTabAt(0).select();
    }
}
