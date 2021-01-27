package com.sscctv.seeeyesonvif.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.sscctv.seeeyesonvif.Interfaces.ServiceWaitThreadListener;
import com.sscctv.seeeyesonvif.Items.ItemGroup;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Service.MainCallService;
import com.sscctv.seeeyesonvif.Service.ServiceWaitThread;
import com.sscctv.seeeyesonvif.Settings.SetInfoFragment;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_RING;
import static com.sscctv.seeeyesonvif.Settings.SetInfoFragment.ACTIVATION_ENABLE;

public class ActivityIntro extends AppCompatActivity {
    private static final String TAG = "IntroActivity";
    private TinyDB tinyDB;
    private InitTask initTask;

    private DataOutputStream opt;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tinyDB = new TinyDB(getApplicationContext());
        mAudioManager = ((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
        setContentView(R.layout.activity_intro);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GPIOPortSet();
        setReadMemoryBuffer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setEchoCanceller();
        initTask = new InitTask();
        initTask.execute();

    }

    @Override
    protected void onPause() {
        super.onPause();
        initTask.cancel(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @SuppressLint("StaticFieldLeak")
    private class InitTask extends AsyncTask<Void, Void, Void> implements ServiceWaitThreadListener {
        protected Void doInBackground(Void... params) {
            publishProgress();

            if (!tinyDB.getBoolean(KeyList.FIRST_KEY)) {
                defaultSettings();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (MainCallService.isReady()) {
                onServiceReady();
            } else {
                startService(new Intent().setClass(getApplicationContext(), MainCallService.class));
                new ServiceWaitThread(this).start();
            }
        }

        @Override
        public void onServiceReady() {
            Log.w(TAG, "onServiceReady");
            tinyDB.remove(KeyList.CALL_CURRENT_DEVICE);
            tinyDB.remove(KeyList.CALL_WAIT_DEVICE);

            Intent intent = new Intent();
            intent.setClass(ActivityIntro.this, ActivityMain.class);
            if (getIntent() != null && getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            intent.setAction(Objects.requireNonNull(getIntent()).getAction());
            intent.setType(getIntent().getType());
            startActivity(intent);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);

            MainCallService.getInstance().changeStatusToOnline();
        }
    }

    private void defaultSettings() {

//        tinyDB.putBoolean(KeyList.CALL_SPEAKER_MODE, getResources().getBoolean(R.bool.first_call_mode));
//        tinyDB.putInt(KeyList.SCREEN_CHANGE_TIME, 0);
//        tinyDB.putInt(KeyList.SCREEN_CHANGE_POS, 0);
//        tinyDB.putBoolean(KeyList.MISSED_CALL, getResources().getBoolean(R.bool.missed_call_mode));
//        tinyDB.putBoolean(KeyList.FIRST_BED_FRAG, getResources().getBoolean(R.bool.first_bed_frag));
//        tinyDB.putBoolean(KeyList.FIRST_SIP_SETTINGS, getResources().getBoolean(R.bool.first_sip_settings));
//        tinyDB.putString(KeyList.KEY_MULTICAST_IP, getResources().getString(R.string.first_multicast_ip));
//        tinyDB.putString(KeyList.KEY_MULTICAST_PORT, getResources().getString(R.string.first_multicast_port));
//        tinyDB.putString(KeyList.KEY_TCP_PORT, getResources().getString(R.string.first_tcp_port));
//        tinyDB.putInt(KeyList.CALL_LOG_MAX, getResources().getInteger(R.integer.default_log_size));

        tinyDB.putInt(KeyList.KEY_SPEAKER_VOLUME, getResources().getInteger(R.integer.default_speaker_volume));
        tinyDB.putInt(KeyList.KEY_HEADSET_VOLUME, getResources().getInteger(R.integer.default_headset_volume));
        tinyDB.putInt(KeyList.KEY_RING_VOLUME, getResources().getInteger(R.integer.default_ring_volume));
        tinyDB.putString(KeyList.KEY_MASTER_ID, getResources().getString(R.string.default_login_id));
        tinyDB.putString(KeyList.KEY_MASTER_PASS, getResources().getString(R.string.default_login_pass));
        tinyDB.putInt(KeyList.LIST_SPAN_COUNT, getResources().getInteger(R.integer.default_span_count));
        tinyDB.putString(KeyList.CALL_TIMEOUT_DURATION, getResources().getString(R.string.default_call_timeout));
        tinyDB.putInt(KeyList.CALL_LOG_MAX, getResources().getInteger(R.integer.default_log_size));
        tinyDB.putString(KeyList.DEVICE_CUR_START, SetInfoFragment.DEFAULT_PAGE_HOME);
        tinyDB.putString(KeyList.DEVICE_CUR_NAME, getResources().getString(R.string.default_dev_name));
        tinyDB.putString(KeyList.DEVICE_CUR_ACTIVATION, ACTIVATION_ENABLE);
        tinyDB.putString(KeyList.DEVICE_CUR_PATROL, ACTIVATION_ENABLE);

        ArrayList<ItemGroup> defaultGroup = new ArrayList<>();
        defaultGroup.add(new ItemGroup(AppUtils.GROUP_HEADER, "Group 1", "", "", "", ""));
        AppUtils.putGroupList(tinyDB, KeyList.LIST_GROUP_HEADER, defaultGroup);
//        getSaveFolder();
//        createLogoFile();
//        setDefaultVolume();
        setDefaultVolume();

    }

    private void setReadMemoryBuffer() {
        Log.i(TAG, "setReadMemoryBuffer: 655360");
        // 163840 Default
        // 262142 Set
        // 655360 Force Setup
        try {
            DataOutputStream opt = new DataOutputStream(Runtime.getRuntime().exec("su").getOutputStream());
            opt.writeBytes("echo 655360 > /proc/sys/net/core/rmem_default\n");
        } catch (Exception e) {
            e.printStackTrace();
            throw new SecurityException();
        }
    }

    private void GPIOPortSet() {
        try {
            Runtime command = Runtime.getRuntime();
            Process proc;

            proc = command.exec("su");
            opt = new DataOutputStream(proc.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            throw new SecurityException();
        }
    }

    private void setEchoCanceller() {
        setEchoBypass();
        setSpeakerGain(tinyDB.getString(KeyList.ECHO_FRONT_SPEAKER));
        setMicGain(tinyDB.getString(KeyList.ECHO_FRONT_MIC));
        resetEcho();

    }

    private void setEchoBypass() {
        if (tinyDB.getBoolean(KeyList.ECHO_FRONT_BYPASS)) {
            try {
                opt.writeBytes("echo 0300 c00a > /proc/hbi/dev_145/write_reg\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            tinyDB.putBoolean(KeyList.ECHO_FRONT_BYPASS, true);
        } else {
            try {
                opt.writeBytes("echo 0300 c008 > /proc/hbi/dev_145/write_reg\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            tinyDB.putBoolean(KeyList.ECHO_FRONT_BYPASS, false);
        }
    }

    private void setSpeakerGain(String val) {

        try {
            opt.writeBytes("echo " + AppUtils.ECHO_SPEAKER_GAIN + " " + val + " > /proc/hbi/dev_145/write_reg\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setMicGain(String val) {

        try {
            Log.d(TAG, "echo " + AppUtils.ECHO_MIC_GAIN + " " + val + " > /proc/hbi/dev_145/write_reg\n");
            opt.writeBytes("echo " + AppUtils.ECHO_MIC_GAIN + " " + val + " > /proc/hbi/dev_145/write_reg\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void resetEcho() {
        try {
            opt.writeBytes("echo " + AppUtils.ECHO_RESET + " 0002 > /proc/hbi/dev_145/write_reg\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDefaultVolume() {

        mAudioManager.setStreamVolume(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME), 0);
        mAudioManager.setStreamVolume(STREAM_RING, tinyDB.getInt(KeyList.KEY_RING_VOLUME), 0);

    }
}
