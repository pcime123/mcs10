package com.sscctv.seeeyesonvif.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Items.IPDevice;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.Items.ItemDoorDevice;
import com.sscctv.seeeyesonvif.Items.ItemEmCallLog;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Server.SendDoorPacket;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.ModelList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.__Lib_JIGU.JIGU_Library;
import com.sscctv.seeeyesonvif.__Lib_JIGU.JIGU_Protocol;
import com.sscctv.seeeyesonvif.__Lib_RTSP.RTSP_Control;
import com.sscctv.seeeyesonvif.__Not_Used.Rtsp.Codec.acodec.AudioCodec;
import com.sscctv.seeeyesonvif.__Not_Used.Rtsp.Codec.acodec.CodecFactory;
import com.sscctv.seeeyesonvif.__Not_Used.Rtsp.RtspClient;
import com.sscctv.seeeyesonvif.databinding.FragmentVideoCallBinding;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_RING;
import static android.media.AudioManager.STREAM_VOICE_CALL;

public class FragmentCallView extends Fragment implements IVLCVout.Callback, IVLCVout.OnNewVideoLayoutListener {

    private static final String TAG = FragmentCallView.class.getSimpleName();
    private FragmentVideoCallBinding mBinding;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private String ipAddress, name, password;
    private int port;
    private RtspClient viewClient;
    private static int screenWidth;
    private static int screenHeight;
    private SurfaceHolder surfaceHolder;

    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;
    private RTSP_Control rtspControl;
    private Socket mSocket;
    private BufferedReader mBufferreader;
    private OutputStream mOutputStream;
    private String callUri;
    private String callLoc;
    private String callModel;
    private Handler mHandler;
    private String sendData, receiveData;

    private String callMode;
    private String getCallStatus;
    private String callMac;
    private String getDoorMac;
    private boolean isOn;
    private boolean isRecording;
    private AudioRecord record;
    private AudioTrack player;
    private AudioManager mAudioManager;
    private int recordState, playerState;
    private int minBuffer;

    //Audio Settings
    private static final int headSetSource = MediaRecorder.AudioSource.MIC;
    private static final int speakerSource = MediaRecorder.AudioSource.CAMCORDER;
    private static final int channel_in = AudioFormat.CHANNEL_IN_MONO;
    private static final int channel_out = AudioFormat.CHANNEL_OUT_MONO;
    private static final int format = AudioFormat.ENCODING_PCM_16BIT;
    private AudioCodec codec;
    private final Context context = ActivityMain.getAppContext();
    private boolean isSpeakMode;
    private boolean isMicMode = true;
    private android.media.MediaPlayer mRingTonePlay;
    private IPDevice getIpDevice;
    private int read;
    private int callPort;
    private String logDate, logTime;

    public FragmentCallView() {
    }


    public static FragmentCallView newInstance() {
        FragmentCallView fragment = new FragmentCallView();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView()");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_call, container, false);
        mAudioManager = ((AudioManager) Objects.requireNonNull(getContext()).getSystemService(Context.AUDIO_SERVICE));
        mHandler = new Handler();

        logDate = AppUtils.currentDate();
        logTime = AppUtils.currentTime();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            getIpDevice = bundle.getParcelable("ipDevice");
            if (getIpDevice != null) {
                ipAddress = getIpDevice.getIpAddress();
                callLoc = getIpDevice.getLoc();
                callModel = getIpDevice.getName();
                callMac = getIpDevice.getMac();
                callPort = getIpDevice.getPort();
                callUri = getIpDevice.getRtspUri();
                callMode = getIpDevice.getMode();

                mBinding.txtCallModel.setText(callModel);
                mBinding.txtCallLoc.setText(callLoc);

                if (callMode.equals(AppUtils.CALL_MODE_OUTGOING)) {
                    tinyDB.putString(KeyList.CALL_CURRENT_DEVICE, callMac);
                    setCallListUpdate(AppUtils.CALL_STATUS_SUCCESS);
                } else {
                    Log.d(TAG, "Current Call: " + tinyDB.getString(KeyList.CALL_CURRENT_DEVICE));
                }
            }
        }

        mBinding.btnCallEnd.setOnClickListener(view -> {
            int size = mBinding.waitList.getChildCount();
            if (size > 0) {
                ArrayList<ItemDevice> devList = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);

                View subView;
                subView = mBinding.waitList.getChildAt(size - 1);
                TextView mac = subView.findViewById(R.id.hiddenMac);

                for (int i = 0; i < devList.size(); i++) {
                    ItemDevice items = devList.get(i);
                    if (mac.getText().equals(devList.get(i).getMac())) {
                        ipAddress = items.getIp();
                        callLoc = items.getLoc();
                        callModel = items.getModel();
                        callMac = items.getMac();

                        callPort = Integer.parseInt(items.getHttp());
                        callUri = items.getRtspUri();
                        callMode = AppUtils.CALL_MODE_INCOMING;

                        mBinding.txtCallModel.setText(callModel);
                        mBinding.txtCallLoc.setText(callLoc);

                        tinyDB.putString(KeyList.CALL_CURRENT_DEVICE, callMac);
                        mBinding.waitList.removeView(subView);

                        releasePlayer();
                        mBinding.endCallTxt.setText("종료중 입니다..");
                        mBinding.endCallLayout.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(() -> {
                            setVideoView();
                            TaskRTSP(new RTSPTask());
                            new Thread(this::startConnection).start();
                            mBinding.endCallLayout.setVisibility(View.GONE);

                        }, 1500);
                        break;
                    }
                }
            } else {
                releasePlayer();
                mBinding.endCallTxt.setText("종료중 입니다..");
                mBinding.endCallLayout.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> {
                    if (callMode.equals(AppUtils.CALL_MODE_INCOMING)) {
                        ((ActivityMain) ActivityMain.context).setIncomingFragment();
                    } else {
                        ((ActivityMain) ActivityMain.context).prevFragment();
                    }
                    mBinding.endCallLayout.setVisibility(View.GONE);
                }, 1500);

            }
        });

//        ViewTreeObserver vto = mBinding.svFrame.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                mBinding.svFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                int width = mBinding.svFrame.getMeasuredWidth();
//                int height = mBinding.svFrame.getMeasuredHeight();
//                Log.d(TAG, "Frame: " + width + " , " + height);
//
//            }
//        });

        mBinding.btnCallSpeaker.setOnClickListener(view -> {
            isSpeakMode = !mAudioManager.isSpeakerphoneOn();
            changeAudio();
            setAudioOutSet(!isSpeakMode);
            new Thread(this::startAudio).start();
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
            if (isMicMode) {
                isMicMode = false;
                mBinding.btnMicOff.setImageResource(R.drawable.ic_baseline_mic_off);
                changeAudio();
            } else {
                isMicMode = true;
                mBinding.btnMicOff.setImageResource(R.drawable.ic_baseline_mic_on);
                setAudioOutSet(!isSpeakMode);
                new Thread(this::startAudio).start();
            }
        });

        mBinding.btnDoorOpen.setOnClickListener(view -> {
            if (getDoorMac != null) {
                TaskDoorEmLock(new DoorEmLock(getDoorMac, "1"));
            }
        });
        setVideoView();
        return mBinding.getRoot();
    }

    private void setVideoView() {
        surfaceHolder = mBinding.svVideo.getHolder();

        ArrayList<String> options = new ArrayList<String>();
        options.add("--audio-time-stretch"); // time stretching
        options.add("--no-sub-autodetect-file");
        options.add("--swscale-mode=0");
        options.add("--network-caching=400");
        options.add("--no-drop-late-frames");
        options.add("--no-skip-frames");
        options.add("--avcodec-skip-frame");
        options.add("--avcodec-hw=any");


        libvlc = new LibVLC(Objects.requireNonNull(getActivity()), options);
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.setFixedSize(1280, 750);
        mMediaPlayer = new MediaPlayer(libvlc);

        switch (callModel) {
            case ModelList.DCS01C:
            case ModelList.DCS01E:
            case ModelList.DCS01N:
            case ModelList.ECS30CMDCS:
            case ModelList.ECS30NMDCS:
                mBinding.btnDoorOpen.setVisibility(View.VISIBLE);
            case ModelList.ECS30CW:
            case ModelList.ECS30CWP:
                mBinding.btnDoorOpen.setVisibility(View.GONE);
        }

        if(!isMicMode){
            isMicMode = true;
            mBinding.btnMicOff.setImageResource(R.drawable.ic_baseline_mic_on);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        getConnectDoorMac();

        isSpeakMode = mAudioManager.isSpeakerphoneOn();
        setAudioOutSet(tinyDB.getBoolean(KeyList.CALL_HOOK));

        TaskRTSP(new RTSPTask());
        new Thread(this::startConnection).start();

        mBinding.timerCall.start();
        ((ActivityMain) ActivityMain.context).setMainBar(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
        tinyDB.remove(KeyList.CALL_CURRENT_DEVICE);

        releasePlayer();
        mBinding.timerCall.stop();
        ((ActivityMain) ActivityMain.context).setMainBar(true);
    }

    private void getConnectDoorMac() {
        if (callModel.equals(ModelList.ECS30CMDCS) || callModel.equals(ModelList.ECS30NMDCS)) {
            ArrayList<ItemDoorDevice> listDoorDevice = AppUtils.getItemDoorDeviceList(tinyDB, KeyList.LIST_DOOR_DEVICE);
            for (int i = 0; i < listDoorDevice.size(); i++) {
                if (listDoorDevice.get(i).getcMac().equals(callMac)) {
                    getDoorMac = listDoorDevice.get(i).getdMac();
                    break;
                }
            }
        }
    }

    private void TaskDoorEmLock(DoorEmLock asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class DoorEmLock extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(getContext());
        private DatagramSocket emSocket;
        private String doorMac, mode;
        private ItemDoorDevice itemDoorDevice;
        private boolean isEms = false;

        public DoorEmLock(String doorMac, String mode) {
            this.doorMac = doorMac;
            this.mode = mode;

        }

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("변경 중...");
            progressDialog.show();

            isEms = true;
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            new Thread(new SendDoorPacket(AppUtils.EDAL_MODE_EMLOCK, new ItemDoorDevice(doorMac, mode, "", "", "", "", "", "", "", "", ""))).start();


            try {
                emSocket = new DatagramSocket(JIGU_Protocol.RECEIVE_PORTNUM);
                emSocket.setSoTimeout(1000);
                while (isEms) {
                    byte[] receiveBuffer = new byte[512];

                    byte[] JiGuKey = new byte[4];
                    JiGuKey[0] = 0;

                    DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    emSocket.receive(packet);
                    int data_length = packet.getLength();
                    byte[] result_buffer = new byte[data_length];

                    packet.setData(receiveBuffer, 0, data_length);
                    byte error = JIGU_Protocol.JiGu_Decode(receiveBuffer, data_length, result_buffer, JiGuKey);

                    System.out.println(JIGU_Library.byteArrayToHexString(receiveBuffer, 0, data_length));
//                    String DMP = String.valueOf(result_buffer[1]);
//                    String Mac = DeviceManagement_Library.byteArrayToMacAddress(result_buffer, 2, 7).trim();
//
//                    Log.d(TAG, "DMP: " + DMP + " Mac: " + Mac);
//                    switch (error) {
//                        case DeviceManagement_Protocol.JiGu_Error_OK:
//                            if (DMP.equals(String.valueOf(DeviceManagement_Protocol.SET_IP_RESPONE)) && Mac.equals(itemDoorDevice.getdMac())) {
//                                new Thread(new SendDoorPacket(AppUtils.EDAL_MODE_RESET, itemDoorDevice)).start();
//                            } else if (DMP.equals(String.valueOf(DeviceManagement_Protocol.SET_RESET_RESPONE))) {
//                                progressDialog.setMessage("장비 부팅 중...");
//                                while (true) {
//                                    if (pingTest(itemDoorDevice.getdIp())) {
//                                        break;
//                                    }
//                                }
//                            }
//                            break;
//                        case DeviceManagement_Protocol.JiGu_Error_Header:
//                            System.out.println("Error Header");
//                            break;
//                        case DeviceManagement_Protocol.JiGu_Error_CheckSum1:
//                            System.out.println("Error_CheckSum1");
//                            break;
//                        case DeviceManagement_Protocol.JiGu_Error_CheckSum2:
//                            System.out.println("Error_CheckSum2");
//                            break;
//                    }
                }
            } catch (Exception e) {
//                e.printStackTrace();
                Log.w(TAG, "TimeException!");
            } finally {
                if (emSocket != null) {
                    try {
                        Log.w(TAG, "Socket Close!");
                        isEms = false;
                        emSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }

    }

    private void setCallListUpdate(String val) {
        ArrayList<ItemEmCallLog> callLists = AppUtils.getCallLog(tinyDB, KeyList.LOG_CALL_EM);
        callLists.add(new ItemEmCallLog(SystemClock.elapsedRealtime(), callModel, ipAddress, callMac, AppUtils.getConfigListLocation(callMac),
                logDate, logTime, AppUtils.CALL_MODE_OUTGOING, val, false));
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

    private void startConnection() {
        if (rtspControl == null) {
            rtspControl = new RTSP_Control(callUri);
            rtspControl.openRTPSocket();
        } else {
            rtspControl.stopOptionTimer();
            rtspControl.setRTSPURL(callUri);
            rtspControl.closeRTPSocket();
        }
        sendData = rtspControl.sendDescribe();
        if (sendData != null) {
            if (rtspControl.sendRTSPBuffer(sendData)) {
                receiveData = rtspControl.receiveBuffer();
                if (receiveData == null || rtspControl == null) {
                    return;
                } else {
                    rtspControl.parseDescribe(receiveData);
                }
            } else {
                return;
            }

            if (rtspControl.trackList.size() == 0) {
                return;
            }

            sendData = rtspControl.sendSETUP(2);
            if (sendData != null) {
                if (rtspControl.sendRTSPBuffer(sendData)) {
                    receiveData = rtspControl.receiveBuffer();

                    if (receiveData != null) {
                        if (rtspControl.trackList == null) {
                            return;
                        }
                        rtspControl.parseSetup(receiveData, rtspControl.trackList.get(2));
                        rtspControl.trackList.get(2).Send = true;
                    }
                }
            }

            sendData = rtspControl.sendPLAY(true);

            if (rtspControl.sendRTSPBuffer(sendData)) {
                receiveData = rtspControl.receiveBuffer();
                if (receiveData != null) {
                    if (isRecording) {
                        rtspControl.parsePLAY(receiveData);
                    }
                }
            }
            startAudio();
        }
    }

    private void setAudioOutSet(boolean mode) {
        isOn = false;
        isRecording = false;
        mAudioFocused = false;

        setAudioManagerInCallMode();
        outSpeakerMode(!mode);
        setVolumeBar(callMode);
        requestAudioFocus();

        int sampleRate = getSampleRate();
        minBuffer = AudioRecord.getMinBufferSize(sampleRate, channel_in, format);
        playerState = 0;

        if (mode) {
            record = new AudioRecord(headSetSource, sampleRate, channel_in, format, minBuffer);
            player = new AudioTrack(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
                    new AudioFormat.Builder().setEncoding(format).setSampleRate(sampleRate).setChannelMask(channel_out).build(),
                    minBuffer,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE);
        } else {
            record = new AudioRecord(speakerSource, sampleRate, channel_in, format, minBuffer);
            player = new AudioTrack(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
                    new AudioFormat.Builder().setEncoding(format).setSampleRate(sampleRate).setChannelMask(channel_out).build(),
                    minBuffer,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE);
        }

        recordState = record.getState();
        playerState = player.getState();

        int id = record.getAudioSessionId();
        Log.d(TAG, "Audio Session: " + id);
        try {
            codec = CodecFactory.getCodec("g711u");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    private boolean mAudioFocused;

    private void requestAudioFocus() {
        if (!mAudioFocused) {
            if (mAudioManager.requestAudioFocus(null, STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                mAudioFocused = true;
        }
    }


    public int getSampleRate() {
        //Find a sample rate that works with the device
        for (int rate : new int[]{8000, 11025, 16000, 22050, 44100, 48000}) {
            int buffer = AudioRecord.getMinBufferSize(rate, channel_in, format);
            if (buffer > 0)
                return rate;
        }
        return -1;
    }

    private void startAudio() {
        int read = 0, write = 0;
//        Log.d(TAG, "---> startAudio...");

        if (recordState == AudioRecord.STATE_INITIALIZED && playerState == AudioTrack.STATE_INITIALIZED) {
            record.startRecording();
            player.play();
            isRecording = true;
        }
        while (isRecording) {
            byte[] audioData = new byte[minBuffer];

            if (record != null) {
                read = record.read(audioData, 0, minBuffer);
                try {
                    rtspControl.Send_Voice_Data(codec.fromPCM(audioData));
                } catch (NullPointerException ex) {
                    return;
                }
            } else {
                break;
            }
//            Log.d("Record", "Read: " + read);
//            if(player != null){
//                player.write(audioData, 0, read);
//            } else{
//                break;
//            }
//            Log.d("Record", "Write: " + write);
        }
    }

    private void changeAudio() {
        if (record != null) {
            if (record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
                record.stop();
            isRecording = false;
            Log.d("Record", "Stopping...");
        }
        if (player != null) {
            if (player.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
                player.stop();
            isRecording = false;
            Log.d("Player", "Stopping...");
        }
    }

    public void endAudio() {
        if (record != null) {
            if (record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
                record.stop();
            isRecording = false;
            Log.d("Record", "Stopping...");
        }
        if (player != null) {
            if (player.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
                player.stop();
            isRecording = false;
            Log.d("Player", "Stopping...");
        }
        rtspControl.closeRTPSocket();

//        Objects.requireNonNull(mAudioManager).setMode(AudioManager.MODE_NORMAL);
//        mAudioManager.setSpeakerphoneOn(true);
    }


    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    public void releasePlayer() {
        Log.i(TAG, "releasePlayer");
        if (libvlc == null)
            return;
        if (rtspControl != null) {
            new Thread(() -> {
                sendData = rtspControl.sendTearDown(0);
                if (rtspControl.sendRTSPBuffer(sendData)) {
                    receiveData = rtspControl.receiveBuffer();
                }
            }).start();
            endAudio();
        } else {
            return;
        }

        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();

        surfaceHolder = null;
        libvlc.release();
        libvlc = null;
        mMediaPlayer.release();
        mMediaPlayer = null;
        isRecording = false;
        rtspControl = null;
    }

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
    }


    private void TaskRTSP(RTSPTask asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class RTSPTask extends AsyncTask<Void, String, String> implements IVLCVout.Callback {
        private ArrayList<ItemDevice> devList;
        private RequestQueue requestQueue;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            final IVLCVout vlcVout = mMediaPlayer.getVLCVout();
            vlcVout.setVideoView(mBinding.svVideo);
            vlcVout.setWindowSize(1280, 750);
            vlcVout.addCallback(this);
            vlcVout.attachViews();
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (!AppUtils.netPingChk(ipAddress)) {
                mHandler.post(() -> {
                    AppUtils.printShort(context, "Network Connection Error: Ping does not reach.");
                    ((ActivityMain) context).prevFragment();
                });
                return null;
            } else {
                Media media = new Media(libvlc, Uri.parse(callUri));
                media.setHWDecoderEnabled(true, true);
                media.addOption(":network-caching=350");
                media.addOption(":clock-jitter=0");
                media.addOption(":clock-synchro=0");
                media.addOption(":codec=all");

                mMediaPlayer.setMedia(media);
                mMediaPlayer.play();
            }
            return null;
        }

        @Override
        public void onSurfacesCreated(IVLCVout vlcVout) {
        }

        @Override
        public void onSurfacesDestroyed(IVLCVout vlcVout) {
        }
    }

    private void setVolumeBar(String mode) {
        Log.w(TAG, "isSpeker@!?:" + isSpeakMode + " Hook: " + tinyDB.getBoolean(KeyList.CALL_HOOK));
        if (isSpeakMode) {
            mBinding.txtVolume.setText("스피커 음량");
            mBinding.barRingVolume.setVisibility(View.GONE);
            mBinding.barHeadsetVolume.setVisibility(View.GONE);
            mBinding.barSpeakerVolume.setVisibility(View.VISIBLE);
            Log.d(TAG, "Set KEY_SPEAKER_VOLUME Volume: " + tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME));
            mAudioManager.setStreamVolume(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME), 0);
            mBinding.btnCallSpeaker.setImageResource(R.drawable.ic_baseline_volume_up_24);
        } else {
            mBinding.txtVolume.setText("수화기 음량");
            mBinding.barRingVolume.setVisibility(View.GONE);
            mBinding.barHeadsetVolume.setVisibility(View.VISIBLE);
            mBinding.barSpeakerVolume.setVisibility(View.GONE);
            Log.d(TAG, "Set KEY_HEADSET_VOLUME Volume: " + tinyDB.getInt(KeyList.KEY_HEADSET_VOLUME));
            mAudioManager.setStreamVolume(STREAM_VOICE_CALL, tinyDB.getInt(KeyList.KEY_HEADSET_VOLUME), 0);
            mBinding.btnCallSpeaker.setImageResource(R.drawable.ic_baseline_call_32);
        }
//        }

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
                mAudioManager.setStreamVolume(STREAM_MUSIC, tinyDB.getInt(KeyList.KEY_SPEAKER_VOLUME), 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBinding.barHeadsetVolume.setMax(mAudioManager.getStreamMaxVolume(STREAM_VOICE_CALL));
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
                mAudioManager.setStreamVolume(STREAM_VOICE_CALL, tinyDB.getInt(KeyList.KEY_HEADSET_VOLUME), 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                changeAudio();
                setAudioOutSet(!isSpeakMode);

                new Thread(() -> {
                    if (recordState == AudioRecord.STATE_INITIALIZED && playerState == AudioTrack.STATE_INITIALIZED) {
                        record.startRecording();
                        player.play();
                        isRecording = true;
                    }
                    while (isRecording) {
                        byte[] audioData = new byte[minBuffer];

                        if (record != null) {
                            read = record.read(audioData, 0, minBuffer);
                            rtspControl.Send_Voice_Data(codec.fromPCM(audioData));
                        } else {
                            break;
                        }
                    }
                }).start();

            }
        });


    }

    public void displayPausedCall(IPDevice ipDevice) {
//        Log.d(TAG, "Get IpDevice: " + ipDevice.getLoc() + "\nCurrent: " + getIpDevice.getLoc());
        LinearLayout callView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.call_inactive_row, null, false);
        TextView contactName = callView.findViewById(R.id.contact_name);
        contactName.setText(ipDevice.getLoc());
        TextView hiddenMac = callView.findViewById(R.id.hiddenMac);
        hiddenMac.setText(ipDevice.getMac());
        Chronometer timer = callView.findViewById(R.id.call_timer);
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();

        ImageView resumeCall = callView.findViewById(R.id.call_pause);
        resumeCall.setOnClickListener(view -> {
            callMac = ipDevice.getMac();
            callUri = ipDevice.getRtspUri();

            mBinding.txtCallLoc.setText(ipDevice.getLoc());
            mBinding.txtCallModel.setText(ipDevice.getName());


            releasePlayer();
            mBinding.endCallLayout.setVisibility(View.VISIBLE);
            mBinding.endCallTxt.setText("연결중 입니다..");
            new Handler().postDelayed(() -> {
                setVideoView();
                TaskRTSP(new RTSPTask());
                new Thread(this::startConnection).start();
                mBinding.endCallLayout.setVisibility(View.GONE);
            }, 1500);


            View v;
            for (int i = 0; i < mBinding.waitList.getChildCount(); i++) {
                v = mBinding.waitList.getChildAt(i);
                TextView mac = v.findViewById(R.id.hiddenMac);
                if (mac.getText().equals(callMac)) {
                    mBinding.waitList.removeView(v);
                    break;
                }
            }

            displayPausedCall(getIpDevice);
            getIpDevice = ipDevice;
        });
        mBinding.waitList.addView(callView);

    }

    public void goNewCall(IPDevice ipDevice) {
        startRingTone();
        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_new_call);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        String curTime = tinyDB.getString(KeyList.CALL_TIMEOUT_DURATION);
        final CountDownTimer countDownTimer = new CountDownTimer(Long.parseLong(curTime) * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
//                Log.i(TAG, "onTick: " + millisUntilFinished);
            }

            public void onFinish() {
//                Log.d(TAG, "Finish");
                setNewCallListUpdate(ipDevice, AppUtils.CALL_STATUS_MISSED);
                releaseRingtone();
                dialog.dismiss();
            }
        };
        countDownTimer.start();

        final TextView title = dialog.findViewById(R.id.dg_txt_nc_name);
        title.setText(ipDevice.getLoc());

        final ImageButton btnOn = dialog.findViewById(R.id.btnCallStart);
        btnOn.setOnClickListener(view -> {
            setNewCallListUpdate(ipDevice, AppUtils.CALL_STATUS_SUCCESS);

            ipAddress = ipDevice.getIpAddress();
            callLoc = ipDevice.getLoc();
            callModel = ipDevice.getName();
            callMac = ipDevice.getMac();
            callPort = ipDevice.getPort();
            callUri = ipDevice.getRtspUri();
            callMode = ipDevice.getMode();

            mBinding.txtCallModel.setText(callModel);
            mBinding.txtCallLoc.setText(callLoc);

            if (callMode.equals(AppUtils.CALL_MODE_OUTGOING)) {
                tinyDB.putString(KeyList.CALL_CURRENT_DEVICE, callMac);
                setCallListUpdate(AppUtils.CALL_STATUS_SUCCESS);
            } else {
                Log.d(TAG, "Current Call: " + tinyDB.getString(KeyList.CALL_CURRENT_DEVICE));
            }
            releaseRingtone();
            countDownTimer.cancel();
            releasePlayer();
            mBinding.endCallLayout.setVisibility(View.VISIBLE);
            mBinding.endCallTxt.setText("연결중 입니다..");
            new Handler().postDelayed(() -> {
                setVideoView();
                TaskRTSP(new RTSPTask());
                new Thread(this::startConnection).start();
                mBinding.endCallLayout.setVisibility(View.GONE);
            }, 1500);


            dialog.dismiss();
        });

        final ImageButton btnPause = dialog.findViewById(R.id.btnCallPause);
        btnPause.setOnClickListener(view -> {
            releaseRingtone();
            countDownTimer.cancel();

            displayPausedCall(ipDevice);
            dialog.dismiss();
        });

        final ImageButton btnOff = dialog.findViewById(R.id.btnCallEnd);
        btnOff.setOnClickListener(view -> {
            setNewCallListUpdate(ipDevice, AppUtils.CALL_STATUS_DECLINED);
            releaseRingtone();
            countDownTimer.cancel();
            dialog.dismiss();
        });

        dialog.show();
    }


    private void startRingTone() {
//        mAudioManager.setSpeakerphoneOn(true);
//        mAudioManager.setMode(MODE_NORMAL);
        Log.d(TAG, "Start RingRing");
        try {
            AssetFileDescriptor afd;
            afd = getContext().getAssets().openFd("basic_ring.mp3");
            if (mRingTonePlay == null) {
                mRingTonePlay = new android.media.MediaPlayer();
                mRingTonePlay.reset();
                mRingTonePlay.setAudioStreamType(STREAM_RING);
                mRingTonePlay.setLooping(true);
                try {
                    mRingTonePlay.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mRingTonePlay.setOnPreparedListener(android.media.MediaPlayer::start);
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
