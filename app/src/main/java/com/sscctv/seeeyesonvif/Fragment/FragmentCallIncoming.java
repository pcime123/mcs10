package com.sscctv.seeeyesonvif.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Items.IPDevice;
import com.sscctv.seeeyesonvif.Items.ItemEmCallLog;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.databinding.FragmentIncomingCallBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static android.media.AudioManager.MODE_NORMAL;
import static android.media.AudioManager.STREAM_RING;

public class FragmentCallIncoming extends Fragment {

    private static final String TAG = FragmentCallIncoming.class.getSimpleName();
    private FragmentIncomingCallBinding mBinding;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private String ipAddress, name, password;
    private AudioManager mAudioManager;
    private String callLoc;
    private String callModel;
    private String callMac;
    private String callUri;
    private String callMode;
    private int callPort;
    private String curTime;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private CountDownTimer countDownTimer;
    private MediaPlayer mRingTonePlay;
    private SoundPool soundPool;
    private int sound;
    private String logDate, logTime;

    public static FragmentCallIncoming newInstance() {
        FragmentCallIncoming fragment = new FragmentCallIncoming();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.v(TAG, "onAttach()");


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView()");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_incoming_call, container, false);
        mAudioManager = ((AudioManager) Objects.requireNonNull(getContext()).getSystemService(Context.AUDIO_SERVICE));

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            IPDevice ipDevice = bundle.getParcelable("ipDevice");
            if (ipDevice != null) {
                ipAddress = ipDevice.getIpAddress();
                callLoc = ipDevice.getLoc();
                callModel = ipDevice.getName();
                callMac = ipDevice.getMac();
                callPort = ipDevice.getPort();
                callUri = ipDevice.getRtspUri();
                callMode = ipDevice.getMode();
                mBinding.txtCallModel.setText(callModel);
                mBinding.txtCallLoc.setText(callLoc);

            }
        }

        mBinding.btnCallEnd.setOnClickListener(view -> {
            ((ActivityMain) ActivityMain.context).prevFragment();
            tinyDB.remove(KeyList.CALL_CURRENT_DEVICE);
            setCallListUpdate(AppUtils.CALL_STATUS_DECLINED);

        });


        mBinding.btnCallStart.setOnClickListener(view -> {

            IPDevice ipDevice = new IPDevice();
            ipDevice.setId(tinyDB.getString(KeyList.KEY_MASTER_ID));
            ipDevice.setPassword(tinyDB.getString(KeyList.KEY_MASTER_PASS));
            ipDevice.setIpAddress(ipAddress);
            ipDevice.setName(callModel);
            ipDevice.setPort(callPort);
            ipDevice.setRtspUri(callUri);
            ipDevice.setLoc(callLoc);
            ipDevice.setMac(callMac);
            ipDevice.setMode(callMode);
            Bundle videoBundle = new Bundle(1);
            videoBundle.putParcelable("ipDevice", ipDevice);

            setCallListUpdate(AppUtils.CALL_STATUS_SUCCESS);
            tinyDB.putString(KeyList.CALL_CURRENT_DEVICE, callMac);

            FragmentCallView fragment = new FragmentCallView();
            fragment.setArguments(videoBundle);
            ((ActivityMain) ActivityMain.context).setGoFragment(fragment);
        });
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        startSoundPoolRingTone();
//        setCallListUpdate(AppUtils.CALL_STATUS_SUCCESS);

        logDate = AppUtils.currentDate();
        logTime = AppUtils.currentTime();

        mBinding.timerCall.start();
        startTimer();
        ((ActivityMain) ActivityMain.context).setMainBar(false);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
        stopSoundPoolRingTone();
        callMode = null;
        mBinding.timerCall.stop();
        stopTimer();
        ((ActivityMain) ActivityMain.context).setMainBar(true);
    }

    private void outSpeakerMode(boolean mode) {
        if (mode == mAudioManager.isSpeakerphoneOn()) {
            return;
        }
        mAudioManager.setSpeakerphoneOn(mode);
    }

    private void setAudioManagerInCallMode() {
        if (mAudioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            return;
        }
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }


    private boolean mAudioFocused;

    private void requestAudioFocus(int stream) {

        if (!mAudioFocused) {
            if (mAudioManager.requestAudioFocus(null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                mAudioFocused = true;
        }
    }

    private void requestAudioFocus1() {
        if (!mAudioFocused) {
            if (mAudioManager.requestAudioFocus(null, STREAM_RING, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                mAudioFocused = true;
        }
    }


    private void setCallListUpdate(String val) {
        boolean stat = false;

        if (val.equals(AppUtils.CALL_STATUS_MISSED)) {
            stat = true;
        }

        ArrayList<ItemEmCallLog> callLists = AppUtils.getCallLog(tinyDB, KeyList.LOG_CALL_EM);
        callLists.add(new ItemEmCallLog(SystemClock.elapsedRealtime(), callModel, ipAddress, callMac, AppUtils.getConfigListLocation(callMac),
                logDate, logTime, AppUtils.CALL_MODE_INCOMING, val, stat));
        AppUtils.putCallLog(tinyDB, KeyList.LOG_CALL_EM, callLists);
    }

    private void setNewCallListUpdate(IPDevice ipDevice, String val) {
        boolean stat = false;

        if (val.equals(AppUtils.CALL_STATUS_MISSED)) {
            stat = true;
        }

        ArrayList<ItemEmCallLog> callLists = AppUtils.getCallLog(tinyDB, KeyList.LOG_CALL_EM);
        callLists.add(new ItemEmCallLog(SystemClock.elapsedRealtime(), ipDevice.getName(), ipDevice.getIpAddress(), ipDevice.getMac(), AppUtils.getConfigListLocation(ipDevice.getMac()),
                logDate, logTime, AppUtils.CALL_MODE_INCOMING, val, stat));
        AppUtils.putCallLog(tinyDB, KeyList.LOG_CALL_EM, callLists);
    }

    private void startTimer() {
        curTime = tinyDB.getString(KeyList.CALL_TIMEOUT_DURATION);
        countDownTimer();
        countDownTimer.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }


    public void countDownTimer() {
        countDownTimer = new CountDownTimer(Long.parseLong(curTime) * 1000, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
//                Log.i("PersistentService", "onTick: " + millisUntilFinished);
            }

            public void onFinish() {
//                Log.d(TAG, "Finish");

                ((ActivityMain) ActivityMain.context).prevFragment();
                tinyDB.remove(KeyList.CALL_CURRENT_DEVICE);
                setCallListUpdate(AppUtils.CALL_STATUS_MISSED);
            }
        };
    }

    private void startSoundPoolRingTone() {
//        Log.d(TAG, "setSpeakerphoneOn: " + mAudioManager.isSpeakerphoneOn());
//        Log.d(TAG, "setMode: " + mAudioManager.getMode());
//        Log.d(TAG, "setWiredHeadsetOn: " + mAudioManager.isWiredHeadsetOn());
        mAudioManager.setWiredHeadsetOn(false);
        mAudioManager.setSpeakerphoneOn(true);
        mAudioManager.setMode(MODE_NORMAL);

        if (soundPool == null) {

            soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
            sound = soundPool.load(getContext(), R.raw.basic_ring, 1);

            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    soundPool.play(sound, 1f, 1f, 0, -1, 1f);
                }
            });
        }
    }

    private void stopSoundPoolRingTone() {
        if (soundPool != null) {
            soundPool.stop(sound);
            soundPool = null;
        }
    }


    private void startRingTone() {
        mAudioManager.setSpeakerphoneOn(true);
        mAudioManager.setMode(MODE_NORMAL);
        Log.d(TAG, "Start RingRing");
        requestAudioFocus(STREAM_RING);
        try {
            AssetFileDescriptor afd;
            afd = getContext().getAssets().openFd("basic_ring.mp3");
            if (mRingTonePlay == null) {
                mRingTonePlay = new MediaPlayer();
                mRingTonePlay.reset();
                mRingTonePlay.setAudioStreamType(STREAM_RING);
                mRingTonePlay.setLooping(true);
                try {
                    mRingTonePlay.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mRingTonePlay.setOnPreparedListener(MediaPlayer::start);
                mRingTonePlay.prepareAsync();

                mRingTonePlay.setOnCompletionListener(mediaPlayer -> {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseRingtone() {
        Log.w(TAG, "releaseRingtone!!");
        if (mRingTonePlay != null) {
//            Log.w(TAG, "stopMedia " + mHookPlayer);
            mRingTonePlay.stop();
            mRingTonePlay.reset();
            mRingTonePlay.release();
            mRingTonePlay = null;
        }
    }

    public void goNewCall(IPDevice ipDevice) {
        startSoundPoolRingTone();
        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_new_call);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        String curTime = tinyDB.getString(KeyList.CALL_TIMEOUT_DURATION);
        final CountDownTimer countDownTimer = new CountDownTimer(Long.parseLong(curTime) * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (((ActivityMain) ActivityMain.context).getCurFragment() instanceof FragmentCallIncoming){
                    //TODO Nothing
                } else {
                    stopSoundPoolRingTone();
                }
                dialog.dismiss();
            }
        };
        countDownTimer.start();

        final TextView title = dialog.findViewById(R.id.dg_txt_nc_name);
        title.setText(ipDevice.getLoc());

        final ImageButton btnOn = dialog.findViewById(R.id.btnCallStart);
        btnOn.setOnClickListener(view -> {
            if (((ActivityMain) ActivityMain.context).getCurFragment() instanceof FragmentCallIncoming){
                setCallListUpdate(AppUtils.CALL_STATUS_DECLINED);
            } else {
                stopSoundPoolRingTone();
            }
            countDownTimer.cancel();

            Bundle videoBundle = new Bundle(1);
            videoBundle.putParcelable("ipDevice", ipDevice);

            setNewCallListUpdate(ipDevice, AppUtils.CALL_STATUS_SUCCESS);
            tinyDB.putString(KeyList.CALL_CURRENT_DEVICE, callMac);

            FragmentCallView fragment = new FragmentCallView();
            fragment.setArguments(videoBundle);
            ((ActivityMain) ActivityMain.context).setGoFragment(fragment);
            dialog.dismiss();
        });

        final LinearLayout layoutPause = dialog.findViewById(R.id.layoutBtnCallPause);
        layoutPause.setVisibility(View.GONE);

        final ImageButton btnOff = dialog.findViewById(R.id.btnCallEnd);
        btnOff.setOnClickListener(view -> {
            setNewCallListUpdate(ipDevice, AppUtils.CALL_STATUS_DECLINED);
            if (((ActivityMain) ActivityMain.context).getCurFragment() instanceof FragmentCallIncoming){
                //TODO Nothing
            } else {
                stopSoundPoolRingTone();
            }
            countDownTimer.cancel();
            dialog.dismiss();
        });

        dialog.show();
    }


}
