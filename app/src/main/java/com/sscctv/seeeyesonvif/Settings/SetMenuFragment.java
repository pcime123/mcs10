package com.sscctv.seeeyesonvif.Settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.databinding.FragmentSettingsMenuBinding;

import java.util.Objects;

public class SetMenuFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SetMenuFragment.class.getSimpleName();
    private FragmentSettingsMenuBinding mBinding;
    public static SetMenuFragment newInstance() {
        SetMenuFragment fragment = new SetMenuFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        setChildFragment(SetEthFragment.newInstance());
        Log.v(TAG, "onResume()");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings_menu, container, false);

        String str;
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            str = bundle.getString("str");
            Log.d(TAG, "String: " + str);

            if(Objects.requireNonNull(str).equals("eth")) {
                setChildFragment(SetEthFragment.newInstance());
            }
        }


        mBinding.btnEthernet.setOnClickListener(this);
        mBinding.btnRange.setOnClickListener(this);

        mBinding.btnType.setOnClickListener(this);
        mBinding.btnAccount.setOnClickListener(this);
        mBinding.btnAudio.setOnClickListener(this);
        mBinding.btnDisplay.setOnClickListener(this);
        mBinding.btnDatetime.setOnClickListener(this);
        mBinding.btnAdminAccount.setOnClickListener(this);
        mBinding.btnAdminSetup.setOnClickListener(this);
        mBinding.btnInfo.setOnClickListener(this);
        mBinding.btnCall.setOnClickListener(this);
        return mBinding.getRoot();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Fragment fragment;
        switch (view.getId()) {
            case R.id.btn_ethernet:
                fragment = SetEthFragment.newInstance();
                setChildFragment(fragment);
                break;
            case R.id.btn_range:
                fragment = SetRangeFragment.newInstance();
                setChildFragment(fragment);
                break;
            case R.id.btn_type:
                fragment = SetTypeFragment.newInstance();
                setChildFragment(fragment);
                break;
            case R.id.btn_audio:
                fragment = SetAudioFragment.newInstance();
                setChildFragment(fragment);
                break;
            case R.id.btn_datetime:
                fragment = SetDatetimeFragment.newInstance();
                setChildFragment(fragment);
                break;

            case R.id.btn_info:
                fragment = SetInfoFragment.newInstance();
                setChildFragment(fragment);
                break;

            case R.id.btn_call:
                fragment = SetCallFragment.newInstance();
                setChildFragment(fragment);
                break;
//            case R.id.btn_admin_setup:
////                NurseCallUtils.startIntent(getContext(), SettingsAdminSetup.class);
//                fragment = SetDpBoardFragment.newInstance();
//                setChildFragment(fragment);
//                break;

//            case R.id.btn_admin_account:
//                fragment = SetAdminFragment.newInstance();
//                setChildFragment(fragment);
//                break;
//            case R.id.btn_account:
//                fragment = SetAccountFragment.newInstance();
//                setChildFragment(fragment);
//                break;
//            case R.id.btn_display:
//                fragment = SetDisplayFragment.newInstance();
//                setChildFragment(fragment);
//                break;

        }
    }

    private void setChildFragment(Fragment child) {
        FragmentTransaction childFt = getChildFragmentManager().beginTransaction();

        if (!child.isAdded()) {
            childFt.replace(R.id.child_settings_fragment, child);
            childFt.addToBackStack(null);
            childFt.commit();
        }
    }
}
