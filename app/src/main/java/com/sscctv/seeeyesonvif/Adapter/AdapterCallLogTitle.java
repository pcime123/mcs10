package com.sscctv.seeeyesonvif.Adapter;

import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class AdapterCallLogTitle extends FragmentStatePagerAdapter {
    private static final String TAG = AdapterCallLogTitle.class.getSimpleName();
    public List<Fragment> listFragment;
    int num;

    public AdapterCallLogTitle(@NonNull FragmentManager fm, List<Fragment> listFragment) {
        super(fm);
        this.listFragment = listFragment;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
//        Log.d(TAG, "Get Item: " + position );
        return listFragment.get(position);

    }


    @Override
    public int getCount() {
        return listFragment.size();
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }
}
