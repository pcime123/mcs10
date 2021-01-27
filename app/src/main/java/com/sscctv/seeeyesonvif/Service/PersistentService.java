package com.sscctv.seeeyesonvif.Service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Items.ItemMainDevice;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Server.SendDeviceSearch;
import com.sscctv.seeeyesonvif.Server.SendEmSearch;
import com.sscctv.seeeyesonvif.Settings.SetInfoFragment;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.IPUtilsCommand;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.ModelList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.MODE_IN_COMMUNICATION;
import static android.media.AudioManager.MODE_NORMAL;
import static android.media.AudioManager.STREAM_MUSIC;

public class PersistentService extends Service implements SensorEventListener {
    private static final String TAG = "PersistentService";
    public static final String BROADCAST_BUFFER_SEND_CODE = "com.sscctv.seeeyesonvif.AppUtils.NurseCallUtils";
    private int MILLILITRE = 100 * 100;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private static PersistentService sInstance;
    private CountDownTimer countDownTimer;
    private TinyDB tinyDB;
    private MediaPlayer mHookPlayer;
    private AudioManager mAudioManager;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private String light;
    private String brMode;

    private boolean isRunning;
    private final IBinder mBinder = new BindServiceBinder();
    private ICallback mCallback;
    private Handler hookHandler;
    private boolean callStat, isHook;
    private String curCall;
    private DataOutputStream opt;
    private Timer hookTimer;
    private SoundPool hookSoundPool;
    private int hookSound;
    private boolean isCheckMac;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public interface ICallback {
        public void remoteCall();
    }

    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    public void startBroadCastTask() {
        isRunning = true;
        TaskCallIncoming(new CallIncomingTask());
    }

    public void stopBroadCastTask(String mode) {
        brMode = mode;
        isRunning = false;
        new Thread(new SendEmSearch(IPUtilsCommand.hexDiscovery)).start();
    }

    public class BindServiceBinder extends Binder {
        public PersistentService getService() {
            return PersistentService.this;
        }
    }

    public static boolean isReady() {
        return sInstance != null;
    }

    public static PersistentService getInstance() {
        return sInstance;
    }

    public Context getApplicationContext() {
        return this;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        tinyDB = new TinyDB(this);
        unregisterRestartAlarm();
        GPIOPortSet();
        hookHandler = new Handler();

        initData();
        registerReceiver(broadcastBufferReceiver, new IntentFilter(BROADCAST_BUFFER_SEND_CODE));
        mAudioManager = ((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));

        initSensor();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, new Notification());

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification;

        notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("")
                .setContentText("")
                .build();

        assert nm != null;
        nm.notify(startId, notification);
        nm.cancel(startId);

        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

        isRunning = true;
        TaskCallIncoming(new CallIncomingTask());


        TimerTask hookTask = new TimerTask() {
            @Override
            public void run() {
                hookHandler.post(
                        () -> {
//                            Log.d(TAG,  "isHook: " + tinyDB.getBoolean(KeyList.CALL_HOOK) + " | Call: " + callStat + " | isSpeaker: " + mAudioManager.isSpeakerphoneOn());
                            isHook = tinyDB.getBoolean(KeyList.CALL_HOOK);
                            curCall = tinyDB.getString(KeyList.CALL_CURRENT_DEVICE);
//                            Log.d(TAG, "curCall: " + curCall + " isHook: " + isHook);
                            callStat = !curCall.equals("");

                            if (!callStat) {
                                if (mAudioManager.getMode() == MODE_NORMAL) {
                                    outSpeakerMode(!isHook);
                                }

                                if (isHook) {
                                    if (mHookPlayer != null && !mHookPlayer.isPlaying()) {
                                        mHookPlayer.start();
                                    } else {
                                        startSoundPoolHook();
//                                        playMedia();
                                        ledCallBtn(true);
                                    }
                                } else {
                                    stopSoundPoolHook();
//                                    stopMedia();
                                    if (mHookPlayer != null && mHookPlayer.isPlaying()) {
                                        mHookPlayer.pause();
                                        ledCallBtn(false);
                                    }
                                }


                            } else {
                                if (isHook) {
                                    stopSoundPoolHook();

//                                    stopMedia();
                                    if (mHookPlayer != null && mHookPlayer.isPlaying()) {
                                        mHookPlayer.pause();
                                        ledCallBtn(false);
                                    }
                                }
                            }

                        });
            }
        };
        hookTimer = new Timer("NurseCall Hook");
        hookTimer.schedule(hookTask, 0, 500);
        return super.onStartCommand(intent, flags, startId);
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


    private void outSpeakerMode(boolean mode) {
        if (mode == mAudioManager.isSpeakerphoneOn()) {
            return;
        }
        mAudioManager.setSpeakerphoneOn(mode);
    }

    private void ledCallBtn(boolean mode) {

        try {
            if (mode) {
//                opt.writeBytes("echo 0 > /sys/class/gpio_sw/PG2/data\n");
                opt.writeBytes("echo 1 > /sys/class/gpio_sw/PG3/data\n");
            } else {
//                opt.writeBytes("echo 1 > /sys/class/gpio_sw/PG2/data\n");
                opt.writeBytes("echo 0 > /sys/class/gpio_sw/PG3/data\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("PersistentService", "onDestroy");
        countDownTimer.cancel();
        registerRestartAlarm();
        unregisterReceiver(broadcastBufferReceiver);
    }

    private void TaskCallIncoming(CallIncomingTask asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class CallIncomingTask extends AsyncTask<Void, String, Void> {
        private DatagramSocket socket;

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "=== CallIncomingTask onPreExecute ===");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.w(TAG, "=== CallIncomingTask doInBackground ===");

            try {
                socket = new DatagramSocket(IPUtilsCommand.PORT);
                socket.setBroadcast(true);
                Log.d(TAG, "BroadCastTask socket: " + socket);

                while (isRunning) {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    Log.d(TAG, "IP: " + packet.getAddress() + " Device IP: " + tinyDB.getString(KeyList.DEVICE_CUR_IP));
                    if (!packet.getAddress().toString().equals("/" + tinyDB.getString(KeyList.DEVICE_CUR_IP))) {
                        String allData = IPUtilsCommand.bytesToHex(packet.getData());

                        String checkHeader = allData.substring(0, 20);
                        Log.d(TAG, "InputData: " + allData.substring(0, 20));
                        Log.w(TAG, "InputData: " + IPUtilsCommand.hexResponseEmergencyCall.toUpperCase());

                        if (IPUtilsCommand.hexResponseEmergencyCall.toUpperCase().equals(checkHeader)) {
//                            Log.v(TAG, "AllData: " + allData);
                            String strMac = new String(packet.getData(), 10, 20);
                            byte[] bMode = Arrays.copyOfRange(packet.getData(), 30, 31);
                            String strIp = new String(packet.getData(), 31, 16);
                            String strMask = new String(packet.getData(), 47, 16);
                            String strGate = new String(packet.getData(), 63, 16);
                            byte[] strStatus = Arrays.copyOfRange(packet.getData(), 163, 164);
                            String ipAddress = useMatchString(strIp);
                            String netMask = useMatchString(strMask);
                            String gateWay = useMatchString(strGate);
                            String macAddress = useMatchString(strMac);

//                            ArrayList<CallDevice> tempList = AppUtils.getCallDeviceList(tinyDB, KeyList.LIST_WAIT_CALL);
//                            tempList.add(new CallDevice(ipAddress, netMask, gateWay, macAddress, IPUtilsCommand.bytesToHex(strStatus)));
//                            AppUtils.putCallDeviceList(tinyDB, KeyList.LIST_WAIT_CALL, tempList);

                            CallDevice callDevice = new CallDevice();
                            callDevice.setIpAddress(ipAddress);
                            callDevice.setNetMask(netMask);
                            callDevice.setGateway(gateWay);
                            callDevice.setMacAddress(macAddress);
                            callDevice.setStatus(IPUtilsCommand.bytesToHex(strStatus));
                            callDevice.setResponse("wait");

                            sendMessage(callDevice);
                            Log.d(TAG, "D9: " + IPUtilsCommand.bytesToHex(strStatus));
                            Log.d(TAG, "IP: " + ipAddress + " NetMask: " + netMask + " GateWay: " + gateWay + " MAC: " + macAddress);
                        }

                        String mainStr = new String(packet.getData(), 0, packet.getLength());
                        String[] data = mainStr.split(IPUtilsCommand.STRING_SPLIT);

                        Log.d(TAG, "mainStr + " + mainStr);
                        if (data[0].equals(IPUtilsCommand.sendMainDeviceDiscovery)) {
                            String head = IPUtilsCommand.resMainDeviceDiscovery;
                            String devAct = tinyDB.getString(KeyList.DEVICE_CUR_ACTIVATION);
                            String devMac = AppUtils.getMacAddr();
                            String devIp = tinyDB.getString(KeyList.DEVICE_CUR_IP);
                            String devName = tinyDB.getString(KeyList.DEVICE_CUR_NAME);
                            String devModel = ModelList.MCS10;

                            StringBuilder sb = new StringBuilder();
                            sb.append(head).append(IPUtilsCommand.STRING_SPLIT);
                            sb.append(devAct).append(IPUtilsCommand.STRING_SPLIT);
                            sb.append(devMac).append(IPUtilsCommand.STRING_SPLIT);
                            sb.append(devIp).append(IPUtilsCommand.STRING_SPLIT);
                            sb.append(devName).append(IPUtilsCommand.STRING_SPLIT);
                            sb.append(devModel);

                            Log.d(TAG, "Mac: " + sb.toString());
                            publishProgress(sb.toString());
                        } else if (data[0].equals(IPUtilsCommand.resMainDeviceDiscovery)) {
                            String mAct = data[1];
                            String mMac = data[2];
                            String mIp = data[3];
                            String mName = data[4];
                            String mModel = data[5];


                            if (mAct.equals(SetInfoFragment.ACTIVATION_ENABLE)) {
                                ArrayList<ItemMainDevice> temp = AppUtils.getMainDeviceList(tinyDB, KeyList.LIST_MAIN_DEVICE);
                                if(temp.size() == 0) {
                                    temp.add(new ItemMainDevice(mMac, mIp, mModel, mName));
                                    Log.d(TAG, "mMac: " + mMac);

                                } else {
                                    for (int i = 0; i < temp.size(); i++) {
                                        if (!mMac.equals(temp.get(i).getmMac())) {
                                            temp.add(new ItemMainDevice(mMac, mIp, mModel, mName));
                                            Log.d(TAG, "Add Mac: " + mMac + " IP: " + mIp + " Name: " + mName + " Model: " + mModel);
                                        }
                                    }
                                }
                                Log.d(TAG, "temp: " + temp.size());
                                AppUtils.putMainDeviceList(tinyDB, KeyList.LIST_MAIN_DEVICE, temp);
                            }


                        }
                    }

                }
            } catch (Exception e) {
                Log.w(TAG, "TimeException!");
//                e.printStackTrace();
                return null;
            } finally {
                if (socket != null) {
                    try {
                        Log.w(TAG, "Socket Close!");
                        isRunning = false;
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            new Thread(new SendDeviceSearch(values[0])).start();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.v(TAG, "BroadCastTask onPostExecute");
//            if (brMode.equals(IPUtilsCommand.MODE_DISCOVERY)) {
//                mCallback.remoteCall();
//            }
            super.onPostExecute(aVoid);
        }

    }

    private String useMatchString(String str) {
        StringBuilder builder = new StringBuilder(str);

        for (int i = 0; i < builder.length(); i++) {
//            Log.d(TAG, "String: " + builder.substring(i, i + 1) + " Size: " + builder.length() + " Num: " + i + " What: " + builder.substring(i, i + 1).matches("^[\\d\\w\\-]"));
            if (!builder.substring(i, i + 1).matches("^[\\d\\w\\-\\.\\:\\_]")) {
                builder.delete(i, builder.length());
                break;
            }
        }
//        Log.d(TAG, "result: " + builder.toString());
        return builder.toString();
    }


    private void initSensor() {
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = Objects.requireNonNull(sensorManager).getDefaultSensor(Sensor.TYPE_LIGHT);
    }


    public void initData() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        if (tinyDB.getInt(KeyList.SCREEN_CHANGE_TIME) != 0) {
            MILLILITRE = tinyDB.getInt(KeyList.SCREEN_CHANGE_TIME);
            countDownTimer();
            countDownTimer.start();
        }
    }

    private void startTimer() {
        if (tinyDB.getInt(KeyList.SCREEN_CHANGE_TIME) != 0) {
            MILLILITRE = tinyDB.getInt(KeyList.SCREEN_CHANGE_TIME);
            countDownTimer();
            countDownTimer.start();
        }
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }


    public void countDownTimer() {
        countDownTimer = new CountDownTimer(MILLILITRE, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                Log.i("PersistentService", "onTick: " + millisUntilFinished);
            }

            public void onFinish() {
                AppUtils.startNewIntent(PersistentService.this, ActivityMain.class);
            }
        };
    }

    private void registerRestartAlarm() {

        Log.i("000 PersistentService", "registerRestartAlarm");
        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 1 * 1000;

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 1 * 1000, sender);

    }

    /**
     * 알람 매니져에 서비스 해제
     */
    private void unregisterRestartAlarm() {

        Log.i(TAG, "unregisterRestartAlarm");
        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        assert alarmManager != null;
        alarmManager.cancel(sender);


    }

    private BroadcastReceiver broadcastBufferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent bufferIntent) {
            int val = Objects.requireNonNull(bufferIntent.getExtras()).getInt("mode");

            switch (val) {
                case 0:
                    initData();
                    break;
                case 1:
                    stopTimer();
                    break;
                case 2:
                    startTimer();
                    break;
                case 3:
//                    tinyDB.putInt(KeyList.BACK_DISPLAY_STAT, 0);
                    break;
                case 4:
//                    tinyDB.putInt(KeyList.BACK_DISPLAY_STAT, 1);
                    break;
            }
        }
    };

    private void sendMessage(CallDevice message) {
        Log.d(TAG, "SendMessage: " + message);
//        stopMedia();
        stopSoundPoolHook();
        Intent intent = new Intent("call_start");
        intent.putExtra("msg", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
            light = String.valueOf(sensorEvent.values[0]);
//            Log.d(TAG, "Light: " + light);
            tinyDB.putString(KeyList.SENSOR_LIGHT, light);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private void startSoundPoolHook() {
        Log.d(TAG, "Hook Sound: " + hookSoundPool + " Cur: " + tinyDB.getString(KeyList.CALL_CURRENT_DEVICE));
        mAudioManager.setMode(MODE_IN_COMMUNICATION);
        mAudioManager.setSpeakerphoneOn(false);
        mAudioManager.setWiredHeadsetOn(true);
        if (hookSoundPool == null) {
            hookSoundPool = new SoundPool(1, STREAM_MUSIC, 0);
            hookSound = hookSoundPool.load(getApplicationContext(), R.raw.freq_440hz_1sec, 1);

            hookSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    Log.w(TAG, "Sound LoadComplete!!");
                    soundPool.play(hookSound, 1f, 1f, 0, -1, 1f);

//                soundPool.play(hookSound, 1f, 1f, 0, -1, 1f);
                }
            });
        }
    }

    private void stopSoundPoolHook() {
        if (hookSoundPool != null) {
            hookSoundPool.stop(hookSound);
            hookSoundPool = null;
        }
    }


    private void playMedia() {
        if (mHookPlayer == null) {
//            Log.w(TAG, "PlayMedia()");
            mAudioManager.setSpeakerphoneOn(false);
            mAudioManager.setMode(STREAM_MUSIC);

            try {
                AssetFileDescriptor afd = getAssets().openFd("freq_440hz_1sec.wav");
                mHookPlayer = new MediaPlayer();
                mHookPlayer.setAudioStreamType(STREAM_MUSIC);
                mHookPlayer.setLooping(true);
                try {
                    mHookPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mHookPlayer.prepare();
                    mHookPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void stopMedia() {
        if (mHookPlayer != null) {
//            Log.w(TAG, "stopMedia " + mHookPlayer);
            mHookPlayer.stop();
            mHookPlayer.reset();
            mHookPlayer.release();
            mHookPlayer = null;
            ledCallBtn(false);
        }
    }


}

