package com.eltech.CRS.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import com.eltech.CRS.R;
import com.eltech.CRS.activities.MainActivity;
import com.eltech.CRS.fragments.BrowseImageResultFragment;
import com.eltech.CRS.fragments.BrowseNoImageFragment;
import com.eltech.CRS.fragments.CameraFragment;
import com.eltech.CRS.resultStuff.CarInfo;

import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_browse_name, R.string.tab_camera_name};
    private final FragmentManager fragmentManager;
    private final MainActivity mainActivity;
    private Fragment browseTabFragment;
    private int tabsCount;

    public ViewPagerAdapter(Context context, FragmentManager fm, boolean doesHaveCamera) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        if(context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        } else {
            throw new RuntimeException("Wrong context");
        }

        fragmentManager = fm;
        browseTabFragment = BrowseNoImageFragment.newInstance();
        tabsCount = doesHaveCamera ? TAB_TITLES.length : TAB_TITLES.length - 1;
    }

    // getItem is called to instantiate the fragment for the given page.
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                //Browse stuff
                return browseTabFragment;
            case 1:
                //Camera stuff
                return CameraFragment.newInstance(mainActivity);
            default:
                throw new RuntimeException("Unexpected position");
        }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        if(object instanceof CameraFragment) return POSITION_UNCHANGED;

        if(object.getClass() != browseTabFragment.getClass()) {
            return POSITION_NONE;
        } else {
            return 0;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mainActivity.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return tabsCount;
    }

    public void switchBrowseTabToFragment(Fragment fragment) {
        browseTabFragment = fragment;
        notifyDataSetChanged();
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    public void displayResult(Context context, Bitmap image, List<CarInfo> carInfoList) {
        if(browseTabFragment instanceof BrowseNoImageFragment) {
            switchBrowseTabToFragment(BrowseImageResultFragment.newInstance(context, image, carInfoList));
            return;
        }

        if(browseTabFragment instanceof BrowseImageResultFragment) {
            ((BrowseImageResultFragment) browseTabFragment).setImage(image);
            ((BrowseImageResultFragment) browseTabFragment).setCarInfoList(carInfoList);
            fragmentManager.beginTransaction()
                           .detach(browseTabFragment)
                           .attach(browseTabFragment)
                           .commit();
            return;
        }
    }
}
