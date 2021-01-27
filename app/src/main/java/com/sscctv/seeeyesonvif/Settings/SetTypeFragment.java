package com.sscctv.seeeyesonvif.Settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.databinding.FragmentSettingsTypeBinding;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Server.SendMultiCast;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.Objects;


public class SetTypeFragment extends Fragment {

    private static final String TAG = "MainActivity";
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private InputMethodManager imm;
    private FragmentSettingsTypeBinding layout;

    static SetTypeFragment newInstance() {
        return new SetTypeFragment();
    }

    private ActivityMain activity;

    @Override
    public void onAttach(@NonNull Context context) {
        activity = (ActivityMain) getActivity();
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        getSetupValue();
        super.onResume();
    }

    @SuppressLint({"WrongConstant", "ClickableViewAccessibility"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_settings_type, container, false);
        imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);

        layout.getRoot().setOnTouchListener((view, motionEvent) -> {
            if (activity.getCurrentFocus() != null) {
                assert imm != null;
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
            return true;
        });


        layout.btnSetupApply.setOnClickListener(view -> {
            tinyDB.putBoolean(KeyList.DEVICE_TYPE, layout.mMainType.isChecked());
            tinyDB.putString(KeyList.MULTI_CAST_IP, layout.eTypeIp.getText().toString());
            tinyDB.putString(KeyList.MULTI_CAST_PORT, layout.eTypePort.getText().toString());

            AppUtils.printShort(getContext(), "설정 완료되었습니다.");
        });

        layout.btnDefault.setOnClickListener(view -> {
            String defaultIp = "239.21.218.198";
            int defaultPort = 1235;

            layout.eTypeIp.setText(defaultIp);
            layout.eTypePort.setText(String.valueOf(defaultPort));
            layout.mSubType.setChecked(true);

        });

        layout.btnIpPing.setOnClickListener(view -> {
            new Thread(new SendMultiCast("none")).start();

        });

        return layout.getRoot();
    }

    private void getSetupValue() {
        boolean mode = tinyDB.getBoolean(KeyList.DEVICE_TYPE);
        String ip = tinyDB.getString(KeyList.MULTI_CAST_IP);
        String port = tinyDB.getString(KeyList.MULTI_CAST_PORT);

        if (mode) {
            layout.mMainType.setChecked(true);
        } else {
            layout.mSubType.setChecked(true);
        }

        Log.d(TAG, "Get IP: " + ip);
        layout.eTypeIp.setText(ip);
        layout.eTypePort.setText(port);
    }

}
