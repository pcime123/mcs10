package com.sscctv.seeeyesonvif.Activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.EthernetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sscctv.seeeyesonvif.Fragment.FragmentCallIncoming;
import com.sscctv.seeeyesonvif.Fragment.FragmentCallView;
import com.sscctv.seeeyesonvif.Fragment.FragmentDeviceView;
import com.sscctv.seeeyesonvif.Fragment.FragmentHome;
import com.sscctv.seeeyesonvif.Fragment.FragmentMapMain;
import com.sscctv.seeeyesonvif.Interfaces.OnSelectEmDevice;
import com.sscctv.seeeyesonvif.Items.IPDevice;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.Items.ItemEmCallLog;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Service.CallDevice;
import com.sscctv.seeeyesonvif.Service.MainCallService;
import com.sscctv.seeeyesonvif.Service.MainPreferences;
import com.sscctv.seeeyesonvif.Service.PersistentService;
import com.sscctv.seeeyesonvif.Service.RestartService;
import com.sscctv.seeeyesonvif.Settings.SetMenuFragment;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.CommandExecutor;
import com.sscctv.seeeyesonvif.Utils.IPUtilsCommand;
import com.sscctv.seeeyesonvif.Utils.JToastShow;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.__Lib_JIGU.Device_DM_Protocol;
import com.sscctv.seeeyesonvif.__Lib_JIGU.Device_EDAL;
import com.sscctv.seeeyesonvif.__Lib_JIGU.JIGU_Library;
import com.sscctv.seeeyesonvif.__Lib_JIGU.JIGU_Protocol;
import com.sscctv.seeeyesonvif.databinding.ActivityMainBinding;

import org.linphone.core.Core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.media.AudioManager.MODE_NORMAL;
import static com.sscctv.seeeyesonvif.Settings.SetInfoFragment.DEFAULT_PAGE_EM;
import static com.sscctv.seeeyesonvif.Settings.SetInfoFragment.DEFAULT_PAGE_EMAP;

@SuppressLint("WrongConstant")
public class ActivityMain extends AppCompatActivity implements OnSelectEmDevice {
    private static final String TAG = ActivityMain.class.getSimpleName();
    private static final String ETHERNET = "ethernet";

    private ActivityMainBinding mBinding;
    private EthernetManager mEthernetManager;
    private FragmentManager manager;
    private AudioManager mAudioManager;
    private MediaPlayer mRingerPlayer;
    private Timer ethStatusTimer, getCallTimer;
    private MultiCastServer multiCastServer;
    public static JToastShow jToastShow;

    private boolean isRunning;
    //    private boolean backChkRunning;
    private boolean modeSet;
    private boolean isAdbUse;
    private Core core;

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static Context getAppContext() {
        return ActivityMain.context;
    }

    private TinyDB tinyDB;
    private final FragmentHome homeFragment = new FragmentHome();
    MediaPlayer mHookPlayer = new MediaPlayer();
    SoundPool soundPool;
    int sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        manager = getSupportFragmentManager();
        mEthernetManager = (EthernetManager) getApplicationContext().getSystemService(ETHERNET);
        mAudioManager = ((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
        jToastShow = new JToastShow();
        tinyDB = new TinyDB(context);
        core = MainCallService.getCore();


        mBinding.btnEth0.setOnClickListener(view -> {
            SetMenuFragment fragment = new SetMenuFragment();
            fragment.setArguments(AppUtils.setBundle("str", "eth"));
            setGoFragment(fragment);
        });
        mBinding.btnAdb.setOnClickListener(view -> setAdbMode());

        mBinding.btnBack.setOnClickListener(view -> prevFragment());


        mBinding.btnHome.setOnClickListener(view -> {
//            goHomeFragment();

        });


        mBinding.btnLock.setOnClickListener(view -> {
//            jToastShow.createToast(context, "알림", "준비 중입니다..", jToastShow.TOAST_INFO, 80, jToastShow.SHORT_DURATION, null);
//            startSoundPoolRingTone();
            Log.d(TAG, AppUtils.getMacAddr());
        });

        Log.w(TAG, "isUsingRandom: " + MainPreferences.instance().isUsingRandomPort());
        Log.w(TAG, "isUseIpv6: " + MainPreferences.instance().isUsingIpv6());
    }



    private void startSoundPoolRingTone() {
        mAudioManager.setWiredHeadsetOn(false);
        mAudioManager.setSpeakerphoneOn(true);
        mAudioManager.setMode(MODE_NORMAL);
        if (soundPool == null) {
            soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
            sound = soundPool.load(this, R.raw.basic_ring, 1);

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
            soundPool.release();
            soundPool = null;
        }
    }

    public Map<String, String> getNotifications() {
        RingtoneManager manager =
                new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_RINGTONE);
        Cursor cursor = manager.getCursor();

        Map<String, String> list = new HashMap<>();
        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String notificationUri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX);

            list.put(notificationTitle, notificationUri);
        }

        return list;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case 0:
        Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI);
        RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, uri);
        Log.d(TAG, "Uri: " + uri);
//                break;
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(goCallReceiver, new IntentFilter("call_start"));

        tinyDB.putBoolean(KeyList.FIRST_KEY, true);
        isRunning = true;
        isAdbUse = false;
        modeSet = false;

        initAllTask();
        initService();
        getAdbState();

     goDefaultStart();
    }


    @Override
    protected void onPause() {
        super.onPause();

        isRunning = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(goCallReceiver);
//        if (multiCastServer != null) {
//            multiCastServer.cancel(false);
//            new Thread(new SendMultiCast(AppUtils.NONE)).start();
//        }

        if (ethStatusTimer != null) {
            ethStatusTimer.cancel();
            ethStatusTimer = null;
        }

        if (getCallTimer != null) {
            getCallTimer.cancel();
            getCallTimer = null;
        }
    }

    private void goDefaultStart() {
        String defaultStart = tinyDB.getString(KeyList.DEVICE_CUR_START);
        switch (defaultStart) {
            case DEFAULT_PAGE_EM:
                setGoFragment(new FragmentDeviceView());
                break;
            case DEFAULT_PAGE_EMAP:
                setGoFragment(new FragmentMapMain());
                break;
            default:
                setGoFragment(new FragmentHome());
                break;
        }
    }

    private void initAllTask() {
        TimerTask ethStatusTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(updateNetwork);
            }
        };

        TimerTask getCallTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(getCallStat);
            }
        };

        ethStatusTimer = new Timer();
        ethStatusTimer.schedule(ethStatusTask, 0, 3000);

        getCallTimer = new Timer();
        getCallTimer.schedule(getCallTask, 0, 100);

//        multiCastServer = new MultiCastServer();
    }

    private void initService() {
        if (PersistentService.isReady()) {
            Log.i(TAG, "PersistentService isReady()");
        } else {
            RestartService restartService = new RestartService();
            Intent intent = new Intent(this, PersistentService.class);

            IntentFilter intentFilter = new IntentFilter("com.sscctv.seeeyesonvif.Service.PersistentService");
            registerReceiver(restartService, intentFilter);
            startService(intent);

        }
    }

    private final Runnable updateNetwork = new Runnable() {
        @Override
        public void run() {
            final String ipAddress;
            if (getEthMode().equals(AppUtils.ETH_STATIC)) {
                ipAddress = getIpAddress();

                if (activationEth0(ipAddress)) {
                    modeSet = true;
                    mBinding.btnEth0.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_circle_green, 0, 0, 0);
                    mBinding.btnEth0.setTextColor(getResources().getColor(R.color.gray));
                    mBinding.btnEth0.setText(String.format("STATIC  %s", ipAddress));
                } else {
                    modeSet = false;
                    mBinding.btnEth0.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_circle_red, 0, 0, 0);
                    mBinding.btnEth0.setTextColor(getResources().getColor(R.color.tiRed));
                    mBinding.btnEth0.setText(R.string.network_connection_error);
                }

            } else {
                ipAddress = getDHCPAddress();

                if (activationEth0(ipAddress)) {
                    modeSet = true;
                    mBinding.btnEth0.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_circle_green, 0, 0, 0);
                    mBinding.btnEth0.setTextColor(getResources().getColor(R.color.gray));
                    mBinding.btnEth0.setText(String.format("DHCP  %s", ipAddress));
                } else {
                    modeSet = false;
                    mBinding.btnEth0.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_circle_red, 0, 0, 0);
                    mBinding.btnEth0.setTextColor(getResources().getColor(R.color.tiRed));
                    mBinding.btnEth0.setText(R.string.network_connection_error);
                }

            }
            setTextAnimation(mBinding.btnEth0, modeSet);

            if (!tinyDB.getString(KeyList.DEVICE_CUR_IP).equals(ipAddress)) {
                tinyDB.putString(KeyList.DEVICE_CUR_IP, ipAddress);
            }
        }
    };

    private void setTextAnimation(final TextView textView, boolean val) {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(1000);
        if (!val) {
            textView.startAnimation(animation);
        } else {
            textView.clearAnimation();
        }
    }

    private final Runnable getCallStat = new Runnable() {
        @Override
        public void run() {
//            String currentCall = tinyDB.getString(KeyList.CALL_CURRENT_INFO);
        }
    };


    private String getIpAddress() {
        String str = mEthernetManager.getConfiguration().toString();
        int value = str.indexOf("address ") + 8;
        int value2 = str.indexOf("/");
        return str.substring(value, value2);
    }

    private void getAdbState() {
        final String val = CommandExecutor.execSingleCommand("getprop service.adb.tcp.port").replaceAll("\\p{C}", "");
        if (val.equals("5555")) {
            mBinding.btnAdb.setImageResource(R.drawable.ic_baseline_adb_24_on);
            isAdbUse = true;
        } else {
            mBinding.btnAdb.setImageResource(R.drawable.ic_baseline_adb_24);
            isAdbUse = false;
        }
    }

    private void setAdbMode() {
        if (!isAdbUse) {
            CommandExecutor.execSingleCommand("setprop service.adb.tcp.port 5555");
            CommandExecutor.execSingleCommand("stop adbd");
            CommandExecutor.execSingleCommand("start adbd");
            isAdbUse = true;
            mBinding.btnAdb.setImageResource(R.drawable.ic_baseline_adb_24_on);
        } else {
            CommandExecutor.execSingleCommand("setprop service.adb.tcp.port -1");
            CommandExecutor.execSingleCommand("stop adbd");
            CommandExecutor.execSingleCommand("start adbd");
            isAdbUse = false;
            mBinding.btnAdb.setImageResource(R.drawable.ic_baseline_adb_24);

        }
    }


    public void setGoFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction;
        fragmentTransaction = manager.beginTransaction().add(fragment, fragment.toString()).addToBackStack(null);
        fragmentTransaction.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
        fragmentTransaction.replace(R.id.main_frame, fragment).commit();
    }


    public void setGoNewFragment(Fragment fragment) {
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction;
        fragmentTransaction = manager.beginTransaction().add(fragment, fragment.toString()).addToBackStack(null);
        fragmentTransaction.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
        fragmentTransaction.replace(R.id.main_frame, fragment).commit();
    }


    public void setIncomingFragment() {
        manager.popBackStack();
        manager.popBackStack();
    }


    public void prevFragment() {
        Fragment curFragment = getSupportFragmentManager().findFragmentById(R.id.main_frame);
        if (curFragment == null) {
            setGoNewFragment(new FragmentHome());
            return;
        }
        if (curFragment instanceof FragmentHome) {
            AppUtils.printShort(getApplicationContext(), "이미 홈 화면 입니다.");
        } else if (curFragment instanceof FragmentDeviceView) {
            setGoNewFragment(new FragmentHome());
        } else if (curFragment instanceof FragmentMapMain) {
            setGoNewFragment(new FragmentHome());
        } else {
            manager.popBackStack();

        }
    }

    public Fragment getCurFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.main_frame);
    }

    private void goHomeFragment() {

        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, homeFragment).commit();
    }

    public void setMainBar(boolean val) {
        mBinding.btnEth0.setEnabled(val);
        mBinding.btnBack.setEnabled(val);
        mBinding.btnHome.setEnabled(val);
        mBinding.btnLock.setEnabled(val);
    }


    private final Handler mHandler = new Handler();


    private String getEthMode() {
        String mode = mEthernetManager.getConfiguration().toString();
        int val = mode.indexOf(AppUtils.ETH_DHCP);

        String ethMode;
        if (val == -1) {
            ethMode = AppUtils.ETH_STATIC;
        } else {
            ethMode = AppUtils.ETH_DHCP;
        }
        return ethMode;
    }

    private String getDHCPAddress() {
        String sValue;

        try {
            Process p = Runtime.getRuntime().exec("getprop");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((sValue = input.readLine()) != null) {
                if (sValue.contains("[dhcp.eth0.ipaddress]:")) {
                    Pattern pDHCPIPAddress = Pattern.compile("\\[dhcp.eth0.ipaddress\\]: \\[(.+?)\\]");
                    Matcher m = pDHCPIPAddress.matcher(sValue);
                    if (m.find()) {
                        Log.d(TAG, "IP---------" + m.group(1));
                        return m.group(1);
                    }
                }
            }
            input.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return getResources().getString(R.string.dhcp_connection_error);
    }

    private boolean activationEth0(String val) {
        String eth0 = CommandExecutor.execSingleCommand("ifconfig eth0");
        if (!eth0.isEmpty()) {
            if (eth0.contains("ip ")) {
                int ipVal = eth0.indexOf("ip ") + 3;
                int maskVal = eth0.indexOf(" mask");
                String ipAddress = eth0.substring(ipVal, maskVal);
                return val.equals(ipAddress);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    private boolean mAudioFocused;

    private void requestAudioFocus() {
        if (!mAudioFocused) {
            if (mAudioManager.requestAudioFocus(null, AudioManager.STREAM_RING, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                mAudioFocused = true;
        }
    }

    private void TaskMultiCast(MultiCastServer asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class MultiCastServer extends AsyncTask<Void, String, String> {

        private int port;
        private String ipAddress;
        private MulticastSocket socket;
        private InetAddress address;
        private int test;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            test = 0;
        }

        protected String doInBackground(Void... params) {
            try {

                Log.w(TAG, "<< ...MultiCast Server running... >>");
                port = Integer.parseInt(tinyDB.getString(KeyList.MULTI_CAST_PORT));
                ipAddress = tinyDB.getString(KeyList.MULTI_CAST_IP);

                socket = new MulticastSocket(port);
                address = InetAddress.getByName(ipAddress);
                socket.joinGroup(address);

                while (isRunning && !isCancelled()) {
                    byte[] buffer = new byte[100];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    if (msg.equals("ping")) {
                        Log.d(TAG, "Get IP: " + packet.getAddress());
                        String str = "Good" + packet.getAddress().toString();
                        byte[] strBuffer = str.getBytes();

                        DatagramPacket datagramPacket = new DatagramPacket(strBuffer, strBuffer.length, address, port);
                        socket.send(datagramPacket);
                    }

                    if (msg.contains("Good")) {
                        String[] ping = msg.split("/");
                        if (ping[1].equals(tinyDB.getString(KeyList.DEVICE_CUR_IP))) {
                            if (ping[0].equals("Good")) {
                                mHandler.post(() -> {
                                    AppUtils.printShort(getApplicationContext(), "Good Connection!");
                                });
                            }
                        }
                    }
                    publishProgress(msg);
                }

                Log.w(TAG, "<< ...Server Stop... >>");

                socket.leaveGroup(address);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
//            getListUpdate(values[0]);
//            mBinding.btnAddAuto.setText(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.w(TAG, "<< ...Service End... >>");
        }
    }

    private void TaskTestCast(TestCastServer asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class TestCastServer extends AsyncTask<Void, String, String> {

        private int port;
        private String ipAddress;
        private MulticastSocket socket;
        private InetAddress address;
        private int test;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            test = 0;
        }

        protected String doInBackground(Void... params) {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(JIGU_Protocol.RECEIVE_PORTNUM);

                while (isRunning) {
                    byte[] receiveBuffer = new byte[512];

                    byte[] JiGuKey = new byte[4];
                    JiGuKey[0] = 0;

                    DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(packet);
                    Log.d(TAG, "GetPacket: " + IPUtilsCommand.bytesToHex(packet.getData()));
                    int data_length = packet.getLength();
                    byte[] result_buffer = new byte[data_length];

                    packet.setData(receiveBuffer, 0, data_length);
                    //System.out.println(DeviceManagement_Library.byteArrayToHexString(rececive_buffer, 0, data_length));

                    byte error = JIGU_Protocol.JiGu_Decode(receiveBuffer, data_length, result_buffer, JiGuKey);
                    String DMP = String.valueOf(result_buffer[1]);
                    String Mac = JIGU_Library.byteArrayToMacAddress(result_buffer, 2, 7).trim();

                    switch (error) {
                        case JIGU_Protocol.JiGu_Error_OK:
                            Log.d(TAG, "buffer: " + Arrays.toString(result_buffer) + " IP: " + packet.getAddress());
//                            process_Data(result_buffer, packet.getAddress().toString());
                            Log.d(TAG, "DMP: " + DMP + " MAC: " + Mac);
                            if (DMP.equals("68") && Mac.equals("65:6B:73:6C:73:32")) {
                                Device_DM_Protocol protocol = new Device_DM_Protocol();
                                Device_EDAL edal = new Device_EDAL();
                                protocol.process_EDAL_Data(result_buffer, edal);
                            }
                            break;
                        case JIGU_Protocol.JiGu_Error_Header:
                            System.out.println("Error Header");
                            break;
                        case JIGU_Protocol.JiGu_Error_CheckSum1:
                            System.out.println("Error_CheckSum1");
                            break;
                        case JIGU_Protocol.JiGu_Error_CheckSum2:
                            System.out.println("Error_CheckSum2");
                            break;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "TimeException!");
                e.printStackTrace();
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
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.w(TAG, "<< ...onPostExecute Service End... >>");
        }
    }

    private final BroadcastReceiver goCallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            CallDevice callDevice = intent.getParcelableExtra("msg");
            ArrayList<ItemDevice> temp = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);
            String ip = null;
            String model = null;
            String loc = null, mac = null, mode = "null", port = null;
            String uri = null;
            boolean result = false;
            if (callDevice != null) {
                for (int i = 0; i < temp.size(); i++) {
                    if (temp.get(i).getIp().equals(callDevice.getIpAddress()) && temp.get(i).getMac().equals(callDevice.getMacAddress())) {
                        ip = callDevice.getIpAddress();
                        mac = callDevice.getMacAddress();
                        mode = callDevice.getStatus();

                        model = temp.get(i).getModel();
                        loc = temp.get(i).getLoc();
                        port = temp.get(i).getHttp();
                        uri = temp.get(i).getRtspUri();
                        result = true;
                        break;
                    }
                }

                if (!result) {
                    return;
                }

                if (!AppUtils.netPingChk(ip)) {
                    jToastShow.createToast(context, "감지된 IP: " + ip, "호출이 감지되었으나 IP 주소 영역대가 달라 연결되지 않습니다.", jToastShow.TOAST_WARNING, 80, jToastShow.LONG_DURATION, null);
                    return;
                }

                String status;
                if (mode.equals("00")) {
                    status = AppUtils.CALL_STATUS_END;
                } else {
                    status = AppUtils.CALL_STATUS_SUCCESS;
                }

                if (status.equals(AppUtils.CALL_STATUS_END)) {
                    return;
                }

                ArrayList<ItemEmCallLog> emLogItems = AppUtils.getCallLog(tinyDB, KeyList.LOG_CALL_EM);
                ArrayList<ItemEmCallLog> waitItems = AppUtils.getCallLog(tinyDB, KeyList.CALL_WAIT_DEVICE);


                IPDevice ipDevice = new IPDevice();
                ipDevice.setId(tinyDB.getString(KeyList.KEY_MASTER_ID));
                ipDevice.setPassword(tinyDB.getString(KeyList.KEY_MASTER_PASS));
                ipDevice.setIpAddress(ip);
                ipDevice.setName(model);
                ipDevice.setPort(Integer.parseInt(port));
                ipDevice.setRtspUri(uri);
                ipDevice.setLoc(loc);
                ipDevice.setMac(mac);
                ipDevice.setMode(AppUtils.CALL_MODE_INCOMING);

                Bundle videoBundle = new Bundle(1);
                videoBundle.putParcelable("ipDevice", ipDevice);

                Fragment curFragment = getSupportFragmentManager().findFragmentById(R.id.main_frame);
                String currentCall = tinyDB.getString(KeyList.CALL_CURRENT_DEVICE);

                if (curFragment instanceof FragmentCallView) {
                    if (!currentCall.equals("")) {
                        FragmentCallView fragmentCallView = (FragmentCallView) getSupportFragmentManager().findFragmentById(R.id.main_frame);
                        if (fragmentCallView != null) {
                            fragmentCallView.goNewCall(ipDevice);
                        } else {
                            Log.e(TAG, "Null FragmentCallView!");
                        }
                    }
                } else if (curFragment instanceof FragmentCallIncoming) {
                    if (!currentCall.equals("")) {
                        FragmentCallIncoming fragmentCallIncoming = (FragmentCallIncoming) getSupportFragmentManager().findFragmentById(R.id.main_frame);
                        if (fragmentCallIncoming != null) {
                           fragmentCallIncoming.goNewCall(ipDevice);
                        } else {
                            Log.e(TAG, "Null FragmentCallIncoming");
                        }
                    }
                } else {
                    tinyDB.putString(KeyList.CALL_CURRENT_DEVICE, mac);
                    goIncomingCall(videoBundle);
                }

            }
        }

    };


    private void goIncomingCall(Bundle bundle) {
        FragmentCallIncoming fragment = new FragmentCallIncoming();
        fragment.setArguments(bundle);
        ((ActivityMain) ActivityMain.context).setGoFragment(fragment);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        int flag = event.getFlags();

        if (flag == 40) {
            return true;
        }
        new Thread(()-> {

        }).start();
        if (keyCode == KeyEvent.KEYCODE_F8) {
            if (action == KeyEvent.ACTION_DOWN) {
                tinyDB.putBoolean(KeyList.CALL_HOOK, true);
            } else if (action == KeyEvent.ACTION_UP) {
                tinyDB.putBoolean(KeyList.CALL_HOOK, false);
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onSelectList(ItemDevice itemDevConfig) {

    }

    @Override
    public void onRegisterList(ItemDevice itemDevConfig) {

    }


}