package com.eltech.CRS.utils;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class OpenImageExternalActivity {
    private static final int REQUEST_CODE = 5;
    private static final String TITLE = "Select picture or video";
    private static final String INTENT_TYPE = "image/*";

    private static Intent intentInstance = getIntent();

    public static void start(Activity activity) {
        activity.startActivityForResult(Intent.createChooser(intentInstance, TITLE), REQUEST_CODE);
    }

    public static void start(Fragment fragment) {
        fragment.startActivityForResult(Intent.createChooser(intentInstance, TITLE), REQUEST_CODE);
    }

    public static boolean isSucceed(int requestCode, int resultCode, @Nullable Intent data) {
        return requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null;
    }

    private static Intent getIntent() {
        intentInstance = new Intent();
        intentInstance.setType(INTENT_TYPE)
                      .setAction(Intent.ACTION_GET_CONTENT);

        return intentInstance;
    }
}
