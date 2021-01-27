package com.sscctv.seeeyesonvif.Settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.JToastShow;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.databinding.FragmentSettingsInfoBinding;


public class SetInfoFragment extends Fragment {

    private static final String TAG = SetInfoFragment.class.getSimpleName();
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private FragmentSettingsInfoBinding layout;
    private JToastShow jToastShow;
    public static final String DEFAULT_PAGE_HOME = "DEFAULT_HOME";
    public static final String DEFAULT_PAGE_EM = "DEFAULT_EM";
    public static final String DEFAULT_PAGE_EMAP = "DEFAULT_EMAP";

    public static final String ACTIVATION_ENABLE = "activation_enable";
    public static final String ACTIVATION_DISABLE = "activation_disable";

    static SetInfoFragment newInstance() {
        return new SetInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_settings_info, container, false);
        jToastShow = new JToastShow();
        String devName = tinyDB.getString(KeyList.DEVICE_CUR_NAME);
        if(devName.equals("")) {
            devName = "입력 안됨";
        }
        layout.setInfoName.setText(devName);

        String devDefault = tinyDB.getString(KeyList.DEVICE_CUR_START);
        Log.d(TAG, "dev:" + devDefault);
        switch (devDefault) {
            case DEFAULT_PAGE_EM:
                layout.setInfoBtnEm.setChecked(true);
                break;
            case DEFAULT_PAGE_EMAP:
                layout.setInfoBtnEmap.setChecked(true);
                break;
            default:
                layout.setInfoBtnHome.setChecked(true);
                break;
        }

        String devActivation = tinyDB.getString(KeyList.DEVICE_CUR_ACTIVATION);
        switch (devActivation) {
            case ACTIVATION_ENABLE:
                layout.setInfoBtnYAct.setChecked(true);
                break;
            case ACTIVATION_DISABLE:
                layout.setInfoBtnNAct.setChecked(true);
                break;
        }

        String patrolActivation = tinyDB.getString(KeyList.DEVICE_CUR_PATROL);
        switch (patrolActivation) {
            case ACTIVATION_ENABLE:
                layout.setInfoPatrolOn.setChecked(true);
                break;
            case ACTIVATION_DISABLE:
                layout.setInfoPatrolOff.setChecked(true);
                break;
        }


        layout.setInfoBtnApply.setOnClickListener(view -> {
            if(!layout.setInfoName.getText().toString().isEmpty()) {
                tinyDB.putString(KeyList.DEVICE_CUR_NAME, layout.setInfoName.getText().toString());
                jToastShow.createToast(getContext(), "성공", "설정이 완료되었습니다.", jToastShow.TOAST_SUCCESS, 80, jToastShow.SHORT_DURATION, null);
            } else {
                jToastShow.createToast(getContext(), "실패", "장비 이름이 비어있습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                return;
            }

            if(layout.setInfoBtnHome.isChecked()) {
                tinyDB.putString(KeyList.DEVICE_CUR_START, DEFAULT_PAGE_HOME);
            } else if(layout.setInfoBtnEm.isChecked()) {
                tinyDB.putString(KeyList.DEVICE_CUR_START, DEFAULT_PAGE_EM);
            } else if(layout.setInfoBtnEmap.isChecked()) {
                tinyDB.putString(KeyList.DEVICE_CUR_START, DEFAULT_PAGE_EMAP);
            }

            if(layout.setInfoBtnYAct.isChecked()) {
                tinyDB.putString(KeyList.DEVICE_CUR_ACTIVATION, ACTIVATION_ENABLE);
            } else {
                tinyDB.putString(KeyList.DEVICE_CUR_ACTIVATION, ACTIVATION_DISABLE);
            }

            if(layout.setInfoPatrolOn.isChecked()) {
                tinyDB.putString(KeyList.DEVICE_CUR_PATROL, ACTIVATION_ENABLE);
            } else {
                tinyDB.putString(KeyList.DEVICE_CUR_PATROL, ACTIVATION_DISABLE);
            }


        });


        return layout.getRoot();
    }



}