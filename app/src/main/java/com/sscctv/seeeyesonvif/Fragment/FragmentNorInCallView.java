package com.sscctv.seeeyesonvif.Fragment;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Items.IPDevice;
import com.sscctv.seeeyesonvif.Items.ItemEmCallLog;
import com.sscctv.seeeyesonvif.Items.ItemMainDevice;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Service.MainCallService;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.databinding.FragmentNormalInCallBinding;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static android.media.AudioManager.MODE_NORMAL;
import static android.media.AudioManager.STREAM_RING;

public class FragmentNorInCallView extends Fragment {

    private static final String TAG = FragmentNorInCallView.class.getSimpleName();
    private FragmentNormalInCallBinding mBinding;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private String ipAddress, name, password;
    private AudioManager mAudioManager;
    private String callLoc;
    private String callModel;
    private String callMac;
    private String callUri;
    private String callMode;
    private int callPort;
    private Core mCore;
    private Call mCall;
    private CoreListenerStub mCoreListener;

    private String curTime;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private CountDownTimer countDownTimer;
    private MediaPlayer mRingTonePlay;
    private SoundPool soundPool;
    private int sound;
    private String logDate, logTime;
    private String getCallMode;

    public static FragmentNorInCallView newInstance() {
        FragmentNorInCallView fragment = new FragmentNorInCallView();
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_normal_in_call, container, false);
        mAudioManager = ((AudioManager) Objects.requireNonNull(getContext()).getSystemService(Context.AUDIO_SERVICE));
        mCore = MainCallService.getCore();
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                Log.w(TAG, "Normal Call State Change: " + state);

                switch (state) {
                    case IncomingReceived:
                        break;
                    case Connected:
                        break;
                    case Error:
                    case End:
                        mBinding.txtStat.setText("호출 종료 됨..");

                        new Handler().postDelayed(() -> {
                            ((ActivityMain) ActivityMain.context).prevFragment();
                        }, 1500);
                        break;
                    case Released:
                        break;
                    case OutgoingRinging:
                        break;
                    case OutgoingInit:
                        break;
                    case StreamsRunning:
                        break;
                }
            }
        };
        getCallMode = AppUtils.CALL_MODE_INCOMING;

        mBinding.btnCallEnd.setOnClickListener(view -> {
//            setCallListUpdate(AppUtils.CALL_STATUS_DECLINED);
            callDecline();

            new Handler().postDelayed(() -> {
                ((ActivityMain) ActivityMain.context).prevFragment();
            }, 1500);
        });

        mBinding.btnCallStart.setOnClickListener(view -> {
            acceptCall(mCall);
        });


        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        outSpeakerMode(true);
        mCore.addListener(mCoreListener);

        ((ActivityMain) ActivityMain.context).setMainBar(false);

        logDate = AppUtils.currentDate();
        logTime = AppUtils.currentTime();

        mBinding.timerCall.start();
        mCall = null;
        for (Call call : mCore.getCalls()) {
            if (Call.State.IncomingReceived == call.getState() || Call.State.IncomingEarlyMedia == call.getState()) {
                mCall = call;
                Log.d(TAG, "1: " + mCall.getRemoteAddress().getDomain());
                getCallDeviceInfo(mCall.getRemoteAddress().getDomain());
                mBinding.txtCallModel.setText(callModel);
                mBinding.txtCallLoc.setText(callLoc);
                break;
            }
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
        mCore.removeListener(mCoreListener);

//        stopSoundPoolRingTone();
        tinyDB.remove(KeyList.CALL_CURRENT_DEVICE);
        mBinding.timerCall.stop();
        ((ActivityMain) ActivityMain.context).setMainBar(true);
    }

    private void getCallDeviceInfo(String ip) {
        ArrayList<ItemMainDevice> devList = AppUtils.getMainDeviceList(tinyDB, KeyList.LIST_MAIN_DEVICE);
        Log.d(TAG, "DevListSize: " + devList.size());
        for (int i = 0; i < devList.size(); i++) {
            if (devList.get(i).getmIp().equals(ip)) {
                Log.d(TAG, "Name: " + devList.get(i).getmName());
                Log.d(TAG, "Name: " + devList.get(i).getmIp());
                Log.d(TAG, "Name: " + devList.get(i).getmMac());
                Log.d(TAG, "Name: " + devList.get(i).getmModel());
                callLoc = devList.get(i).getmName();
                callModel = devList.get(i).getmModel();
                tinyDB.putString(KeyList.CALL_CURRENT_DEVICE, devList.get(i).getmMac());
                break;
            }
        }

        if (callLoc == null) {
            callLoc = "등록되지 않은 장비";
            callModel = "장비를 등록해주세요.";
        }

    }

    private void acceptCall(Call call) {
        Log.d(TAG, "Call: " + call.getRemoteAddress());

        if (call == null) {
            return;
        }

        CallParams params = mCore.createCallParams(call);
        call.acceptWithParams(params);

        FragmentNormalCallView fragment = new FragmentNormalCallView();
        fragment.setArguments(AppUtils.setBundle("call", AppUtils.CALL_MODE_INCOMING));
        ((ActivityMain) ActivityMain.context).setGoFragment(fragment);
    }

    public void callDecline() {
//        tinyDB.putBoolean(KeyList.CALL_CONNECTED, isConnect);

        int size = mCore.getCalls().length;
        for (int i = 0; i < size; i++) {
            mCore.getCalls()[i].terminate();
        }

        Call call = mCore.getCurrentCall();
        if (call != null) {
            call.terminate();
        }

        mBinding.timerCall.setVisibility(View.GONE);
        mBinding.txtStat.setText("호출 종료 중..");

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(150); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        mBinding.txtStat.startAnimation(anim);

        new Handler().postDelayed(() -> {
            ((ActivityMain) ActivityMain.context).prevFragment();
        }, 1500);
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


    private void startSoundPoolRingTone() {
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


}
