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
import android.widget.SeekBar;

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
import com.sscctv.seeeyesonvif.databinding.FragmentNormalCallBinding;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static android.media.AudioManager.MODE_NORMAL;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_VOICE_CALL;

public class FragmentNormalCallView extends Fragment {

    private static final String TAG = FragmentNormalCallView.class.getSimpleName();
    private FragmentNormalCallBinding mBinding;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private String ipAddress, name, password;
    private AudioManager mAudioManager;
    private String callLoc;
    private String callModel;
    private String callMac;
    private String callUri;
    private int callPort;
    private Core mCore;
    private Call mCall;
    private boolean isCall;
    private boolean isConnect = false;


    private String curTime;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private CountDownTimer countDownTimer;
    private MediaPlayer mRingTonePlay;
    private SoundPool soundPool;
    private int sound;
    private String logDate, logTime;
    private String getCallMode;
    private CoreListenerStub mCoreListener;
    private boolean isSpeakMode;
    private boolean pushEnd = false;
    private boolean mIsMicMuted;

    public static FragmentNormalCallView newInstance() {
        FragmentNormalCallView fragment = new FragmentNormalCallView();
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_normal_call, container, false);
        mAudioManager = ((AudioManager) Objects.requireNonNull(getContext()).getSystemService(Context.AUDIO_SERVICE));

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            getCallMode = bundle.getString("call");
        } else {
            getCallMode = AppUtils.CALL_MODE_OUTGOING;
        }

        mCore = MainCallService.getCore();

        mCoreListener = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                Log.w(TAG, "Normal Call State Change: " + state);

                switch (state) {
                    case IncomingReceived:
                        break;
                    case Connected:
                        stopSoundPoolRingTone();
                        setVolumeBar(!tinyDB.getBoolean(KeyList.CALL_HOOK));
                        outSpeakerMode(!tinyDB.getBoolean(KeyList.CALL_HOOK));

                        mBinding.txtStat.setText("전화 중..");
                        break;
                    case Error:
                    case End:
                        stopSoundPoolRingTone();
                        if (!pushEnd) callDecline();
                        break;
                    case Released:
                        break;
                    case OutgoingRinging:
                        getCallMode = AppUtils.CALL_MODE_OUTGOING;
                        startSoundPoolRingTone();
                        setRingVolumeBar();
                        mBinding.txtStat.setText("전화 거는 중..");
                        break;
                    case OutgoingInit:
                        break;
                    case StreamsRunning:
                        break;
                }
            }
        };

        mBinding.btnCallSpeaker.setOnClickListener(view -> {
            isSpeakMode = !mAudioManager.isSpeakerphoneOn();
            Log.d(TAG, "isSpeaker: " + isSpeakMode);

            setVolumeBar(isSpeakMode);
            outSpeakerMode(isSpeakMode);

        });

        mBinding.btnVolumeSet.setOnClickListener(view -> {
            if (mBinding.layoutVolume.getVisibility() == View.GONE) {
                mBinding.layoutVolume.setVisibility(View.VISIBLE);
                mBinding.btnVolumeSet.setImageResource(R.drawable.icon_volume_on);
            } else {
                mBinding.layoutVolume.setVisibility(View.GONE);
                mBinding.btnVolumeSet.setImageResource(R.drawable.icon_volume_off);
            }
        });

        mBinding.btnMicOff.setOnClickListener(view -> {
            mIsMicMuted = mCore.micEnabled();
            mBinding.btnMicOff.setSelected(mIsMicMuted);
            mCore.enableMic(!mIsMicMuted);
            if (mIsMicMuted) {
                mBinding.btnMicOff.setImageResource(R.drawable.ic_baseline_mic_off);
            } else {
                mBinding.btnMicOff.setImageResource(R.drawable.ic_baseline_mic_on);
            }
        });

        mBinding.btnCallEnd.setOnClickListener(view -> {
            stopSoundPoolRingTone();
            pushEnd = true;
//            setCallListUpdate(AppUtils.CALL_STATUS_DECLINED);
            callDecline();
        });

        return mBinding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
//        setCallListUpdate(AppUtils.CALL_STATUS_SUCCESS);
        mCore.addListener(mCoreListener);

        logDate = AppUtils.currentDate();
        logTime = AppUtils.currentTime();

        mBinding.timerCall.start();

        mCall = null;
        Log.d(TAG, "GetCallMode: " + getCallMode);

        if (getCallMode.equals(AppUtils.CALL_MODE_OUTGOING)) {
            if (mCore != null) {
                for (Call call : mCore.getCalls()) {
                    Call.State callState = call.getState();
                    if (Call.State.OutgoingInit == callState
                            || Call.State.OutgoingProgress == callState
                            || Call.State.OutgoingRinging == callState
                            || Call.State.OutgoingEarlyMedia == callState) {
                        mCall = call;
                        getCallDeviceInfo(mCall.getRemoteAddress().getDomain());
                        mBinding.txtCallModel.setText(callModel);
                        mBinding.txtCallLoc.setText(callLoc);
                        break;
                    }
                }
            }
            Log.d(TAG, "get Call: " + mCall);
        } else if (getCallMode.contains(AppUtils.CALL_MODE_INCOMING)) {
            if (!getCallMode.contains("+")) {
                for (Call call : mCore.getCalls()) {
                    if (Call.State.IncomingReceived == call.getState()
                            || Call.State.IncomingEarlyMedia == call.getState()) {
                        mCall = call;
                        break;
                    }
                }

                if (mCall != null) {
                    Log.d(TAG, "1: " + mCall.getRemoteAddress().getDomain());
                    getCallDeviceInfo(mCall.getRemoteAddress().getDomain());
                    mBinding.txtCallModel.setText(callModel);
                    mBinding.txtCallLoc.setText(callLoc);
                }
            }
        } else if (getCallMode.contains(AppUtils.CALL_MODE_NORMAL)) {
            isConnect = true;
            isCall = true;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
        stopSoundPoolRingTone();
        mCore.removeListener(mCoreListener);

        tinyDB.remove(KeyList.CALL_CURRENT_DEVICE);
        mBinding.timerCall.stop();
        ((ActivityMain) ActivityMain.context).setMainBar(true);
    }

    private void getCallDeviceInfo(String ip) {
        Log.d(TAG, "MAC !!!: " + ip);
        ArrayList<ItemMainDevice> devList = AppUtils.getMainDeviceList(tinyDB, KeyList.LIST_MAIN_DEVICE);

        for (int i = 0; i < devList.size(); i++) {
            if (devList.get(i).getmIp().equals(ip)) {
                callLoc = devList.get(i).getmName();
                callModel = devList.get(i).getmModel();
                break;
            }
        }

        if (callLoc.isEmpty()) {
            callLoc = "등록되지 않은 장비";
            callModel = "장비를 등록해주세요.";
        }

        mBinding.txtCallModel.setText(callModel);
        mBinding.txtCallLoc.setText(callLoc);

    }

    private void acceptCall(Call call) {
        Log.d(TAG, "Call: " + call.getRemoteAddress());

        if (call == null) {
            return;
        }


        isConnect = true;
        isCall = true;
        changeLayout(getCallMode);

        CallParams params = mCore.createCallParams(call);
        call.acceptWithParams(params);
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

        mBinding.timerCall.stop();
        mBinding.timerCall.setVisibility(View.GONE);
        mBinding.barButton.setVisibility(View.GONE);
        mBinding.layoutVolume.setVisibility(View.GONE);
        mBinding.txtStat.setText("전화 종료 중..");

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(150); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        mBinding.txtStat.startAnimation(anim);

        new Handler().postDelayed(() -> {
            if (getCallMode.equals(AppUtils.CALL_MODE_INCOMING)) {
                ((ActivityMain) ActivityMain.context).setIncomingFragment();
            } else {
                ((ActivityMain) ActivityMain.context).prevFragment();
            }
        }, 1500);

    }

    private void inCallDecline() {

        int size = mCore.getCalls().length;
        for (int i = 0; i < size; i++) {
            mCore.getCalls()[i].terminate();
        }

        Call call = mCore.getCurrentCall();
        if (call != null) {
            call.terminate();
        }

        mBinding.timerCall.stop();
        mBinding.timerCall.setVisibility(View.GONE);
        mBinding.barButton.setVisibility(View.GONE);
        mBinding.layoutVolume.setVisibility(View.GONE);
        mBinding.txtStat.setText("전화 종료 중..");

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(150); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        mBinding.txtStat.startAnimation(anim);

        new Handler().postDelayed(() -> {
            if (getCallMode.equals(AppUtils.CALL_MODE_INCOMING)) {
                ((ActivityMain) ActivityMain.context).setIncomingFragment();
            } else {
                ((ActivityMain) ActivityMain.context).prevFragment();
            }
        }, 1500);
    }


    private void changeLayout(String mode) {
        ((ActivityMain) ActivityMain.context).setMainBar(false);
        mBinding.txtCallModel.setText(callModel);
        mBinding.txtCallLoc.setText(callLoc);
        switch (mode) {
            case AppUtils.CALL_MODE_OUTGOING:
                mBinding.txtStat.setText("전화 거는 중..");
                break;
            case AppUtils.CALL_MODE_NORMAL:
                mBinding.txtStat.setText("전화 중..");
                mBinding.barButton.setVisibility(View.VISIBLE);
                break;
            case AppUtils.CALL_MODE_INCOMING:
                mBinding.txtStat.setText("전화 거는 중..");
                mBinding.barButton.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void outSpeakerMode(boolean mode) {
        Log.d(TAG, "Mode: " + mode + " Get: " + mAudioManager.isSpeakerphoneOn());
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

            soundPool = new SoundPool(1, STREAM_RING, 0);
            sound = soundPool.load(getContext(), R.raw.freq_440hz_1sec, 1);

            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    soundPool.play(sound, 0.2f, 0.2f, 0, -1, 1f);
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

    private void setRingVolumeBar() {
        mBinding.txtVolume.setText("벨소리 음량");
        mBinding.barRingVolume.setVisibility(View.VISIBLE);
        mBinding.barHeadsetVolume.setVisibility(View.GONE);
        mBinding.barSpeakerVolume.setVisibility(View.GONE);
        mBinding.btnMicOff.setVisibility(View.INVISIBLE);
        mAudioManager.setStreamVolume(STREAM_RING, tinyDB.getInt(KeyList.KEY_RING_VOLUME), 0);
        mBinding.btnCallSpeaker.setImageResource(R.drawable.ic_baseline_volume_up_24);

        mBinding.barRingVolume.setMax(mAudioManager.getStreamMaxVolume(STREAM_RING));
        mBinding.barRingVolume.setProgress(tinyDB.getInt(KeyList.KEY_RING_VOLUME));
        mBinding.barRingVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tinyDB.putInt(KeyList.KEY_RING_VOLUME, i);
                mAudioManager.setStreamVolume(STREAM_RING, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
    private void setVolumeBar(boolean mode) {
        Log.w(TAG, "isSpeker@!?:" + mode + " Hook: " + tinyDB.getBoolean(KeyList.CALL_HOOK));
        if (mode) {
            mBinding.txtVolume.setText("스피커 음량");
            mBinding.barRingVolume.setVisibility(View.GONE);
            mBinding.barHeadsetVolume.setVisibility(View.GONE);
            mBinding.barSpeakerVolume.setVisibility(View.VISIBLE);
            mBinding.btnMicOff.setVisibility(View.VISIBLE);
            mAudioManager.setStreamVolume(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME), 0);
            mBinding.btnCallSpeaker.setImageResource(R.drawable.ic_baseline_volume_up_24);
        } else {
            mBinding.txtVolume.setText("수화기 음량");
            mBinding.barRingVolume.setVisibility(View.GONE);
            mBinding.barHeadsetVolume.setVisibility(View.VISIBLE);
            mBinding.barSpeakerVolume.setVisibility(View.GONE);
            mBinding.btnMicOff.setVisibility(View.VISIBLE);
            mAudioManager.setStreamVolume(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_HEADSET_VOLUME), 0);
            mBinding.btnCallSpeaker.setImageResource(R.drawable.ic_baseline_call_32);
        }


        mBinding.barSpeakerVolume.setMax(30);
        mBinding.barSpeakerVolume.setProgress(tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME));
        mBinding.barSpeakerVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == 0) {
                    seekBar.setProgress(1);
                    tinyDB.putInt(KeyList.KEY_SPEAKER_VOLUME, 1);
                } else {
                    tinyDB.putInt(KeyList.KEY_SPEAKER_VOLUME, i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mAudioManager.setStreamVolume(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME), 0);

            }
        });

        mBinding.barHeadsetVolume.setMax(15);
        mBinding.barHeadsetVolume.setProgress(tinyDB.getInt(KeyList.KEY_HEADSET_VOLUME));
        mBinding.barHeadsetVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == 0) {
                    seekBar.setProgress(1);
                    tinyDB.putInt(KeyList.KEY_HEADSET_VOLUME, 1);
                } else {
                    tinyDB.putInt(KeyList.KEY_HEADSET_VOLUME, i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mAudioManager.setStreamVolume(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_HEADSET_VOLUME), 0);

            }
        });


    }
}
