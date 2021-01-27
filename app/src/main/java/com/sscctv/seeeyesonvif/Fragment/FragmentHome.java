package com.sscctv.seeeyesonvif.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Items.ItemEmCallLog;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Settings.SetInfoFragment;
import com.sscctv.seeeyesonvif.Settings.SetMenuFragment;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.databinding.FragmentHomeBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentHome extends Fragment {

    private static final String TAG = FragmentHome.class.getSimpleName();
    private FragmentHomeBinding mBinding;
    private TinyDB tinyDB;
    private String deviceName, ipAddress;
    private int port;
    private InputMethodManager imm;
    private Timer timerUpdateTime;
    private Timer alarmTime;
    private boolean isDotChange = false;
    private Handler mHandler;

    public static FragmentHome newInstance() {
        FragmentHome fragment = new FragmentHome();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        checkActPatrol(tinyDB.getString(KeyList.DEVICE_CUR_PATROL));
        mBinding.homeTxtName.setText(tinyDB.getString(KeyList.DEVICE_CUR_NAME));
        initHomeTask();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        if (timerUpdateTime != null) {
            timerUpdateTime.cancel();
            timerUpdateTime = null;
        }

        if (alarmTime != null) {
            alarmTime.cancel();
            alarmTime = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        tinyDB = new TinyDB(getContext());
        imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);

        Bundle bundle = this.getArguments();

        if (bundle != null) {

        }
        mBinding.btnDevice.setOnClickListener(view -> ((ActivityMain) ActivityMain.context).setGoFragment(new FragmentDeviceView()));
        mBinding.btnCall.setOnClickListener(view -> ((ActivityMain) ActivityMain.context).setGoFragment(new FragmentNormalCallList()));
        mBinding.btnEmap.setOnClickListener(view -> ((ActivityMain) ActivityMain.context).setGoFragment(new FragmentMapMain()));
        mBinding.btnSetup.setOnClickListener(view -> ((ActivityMain) ActivityMain.context).setGoFragment(new SetMenuFragment()));
        mBinding.btnCallLog.setOnClickListener(view -> ((ActivityMain) ActivityMain.context).setGoFragment(new FragmentCallLog()));
        mBinding.btnAlarm.setOnClickListener(view -> {
            ((ActivityMain) ActivityMain.context).setGoFragment(new FragmentCallLog());
        });


        return mBinding.getRoot();
    }

    public static float dpToPx(Context context, float dp) {
        if (context == null) {
            return -1;
        }
        return dp * context.getResources().getDisplayMetrics().density;
    }

    private void checkActPatrol(String act) {
        if(act.equals(SetInfoFragment.ACTIVATION_DISABLE)) {
            if(mBinding.btnPatrol.getVisibility() == View.VISIBLE) {
                mBinding.btnPatrol.setVisibility(View.GONE);
            }
        } else {
            if(mBinding.btnPatrol.getVisibility() == View.GONE) {
                mBinding.btnPatrol.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initHomeTask() {
        mHandler = new Handler();
        TimerTask homeTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(updateTime);
                mHandler.post(() -> {
                    if (!isDotChange) {
                        mBinding.colon.setVisibility(View.INVISIBLE);
                        isDotChange = true;
                    } else {
                        mBinding.colon.setVisibility(View.VISIBLE);
                        isDotChange = false;
                    }
                });
            }
        };
        timerUpdateTime = new Timer();
        timerUpdateTime.schedule(homeTask, 0, 500);

        TimerTask alarmTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(updateDeclineCall);

            }
        };
        alarmTime = new Timer();
        alarmTime.schedule(alarmTask, 0, 2000);
    }

    private Runnable updateTime = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat fHour = new SimpleDateFormat("HH", Locale.KOREA);
            SimpleDateFormat fMinute = new SimpleDateFormat("mm", Locale.KOREA);
            String strHour = fHour.format(date);
            String strMinute = fMinute.format(date);
            mBinding.hour.setText(strHour);
            mBinding.minute.setText(strMinute);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. MM. dd. E요일", Locale.getDefault());
            String formatDate = dateFormat.format(date);
            mBinding.homeTxtDate.setText(formatDate);

        }
    };

    private Runnable updateDeclineCall = () -> visibleDecline(isDeclineVal());

    private void createDeclineCall(long num, String date, String time, String loc) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.call_decline_row, null, false);
        TextView txtHiddenMac = layout.findViewById(R.id.txtDeclineMac);
        TextView txtDate = layout.findViewById(R.id.txtDeclineDate);
        TextView txtTime = layout.findViewById(R.id.txtDeclineTime);
        TextView txtLoc = layout.findViewById(R.id.txtDeclineLoc);

        txtHiddenMac.setText(String.valueOf(num));
        txtDate.setText(date);
        txtTime.setText(time);
        txtLoc.setText(loc);

        mBinding.declineLayout.addView(layout);
        LinearLayout.LayoutParams lp;
        if (mBinding.declineLayout.getChildCount() > 5) {
            lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) dpToPx(getContext(), 170));
        } else {
            lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        mBinding.scrollLayout.setLayoutParams(lp);

    }


    public void checkDeclineCall() {
        ArrayList<ItemEmCallLog> callLog = AppUtils.getCallLog(tinyDB, KeyList.LOG_CALL_EM);
        View v;
        Log.d(TAG, "CallSize: " + callLog.size());
        for (int i = 0; i < callLog.size(); i++) {
            boolean isCheck = callLog.get(i).isCallCheck();
            Log.d(TAG, "Log " + i + " : " + isCheck);
            if (isCheck) {
                createDeclineCall(callLog.get(i).getCallNum(), callLog.get(i).getCallDate(), callLog.get(i).getCallTime(), callLog.get(i).getCallLoc());
            }
        }
    }

    private void setLayoutAnimation(final LinearLayout textView, boolean val) {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(1000);
        if (val) {
            textView.startAnimation(animation);
        } else {
            textView.clearAnimation();
        }
    }

    private boolean isDeclineVal() {
        boolean val = false;
        ArrayList<ItemEmCallLog> callLog = AppUtils.getCallLog(tinyDB, KeyList.LOG_CALL_EM);
        for (int i = 0; i < callLog.size(); i++) {
            if (callLog.get(i).isCallCheck()) {
                val = true;
                break;
            }
        }
        return val;
    }

    private void visibleDecline(boolean val) {
        setLayoutAnimation(mBinding.layoutAlarm, val);
        if (val) {
            if (mBinding.layoutAlarm.getVisibility() == View.INVISIBLE) {
                mBinding.layoutAlarm.setVisibility(View.VISIBLE);
                checkDeclineCall();
            }
        } else {
            if (mBinding.layoutAlarm.getVisibility() == View.VISIBLE) {
                mBinding.layoutAlarm.setVisibility(View.INVISIBLE);
            }
        }
    }
}
