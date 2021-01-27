package com.sscctv.seeeyesonvif.Settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
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
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.databinding.FragmentSettingsCallBinding;

import java.util.Objects;


public class SetCallFragment extends Fragment {

    private static final String TAG = "SetCallFragment";
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private InputMethodManager imm;
    private FragmentSettingsCallBinding layout;

    static SetCallFragment newInstance() {
        return new SetCallFragment();
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
        super.onResume();
    }

    @SuppressLint({"WrongConstant", "ClickableViewAccessibility"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_settings_call, container, false);
        imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);

        layout.getRoot().setOnTouchListener((view, motionEvent) -> {
            if (activity.getCurrentFocus() != null) {
                assert imm != null;
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
            return true;
        });


        layout.btnSetupApply.setOnClickListener(view -> {
            tinyDB.putString(KeyList.CALL_TIMEOUT_DURATION,layout.editTimeOut.getText().toString() );
            tinyDB.putString(KeyList.CALL_MAX_WAIT_DEVICE, layout.editWaitDevice.getText().toString());

            AppUtils.printShort(getContext(), "설정 완료되었습니다.");
        });

        layout.btnDefault.setOnClickListener(view -> {

            layout.editTimeOut.setText("120");
            layout.editWaitDevice.setText("5");

        });

        layout.editTimeOut.setText(tinyDB.getString(KeyList.CALL_TIMEOUT_DURATION));
        layout.editWaitDevice.setText(tinyDB.getString(KeyList.CALL_MAX_WAIT_DEVICE));
        return layout.getRoot();
    }


}
