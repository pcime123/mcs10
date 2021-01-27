package com.sscctv.seeeyesonvif.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Items.ItemDoorDevice;
import com.sscctv.seeeyesonvif.Items.ItemEmCallLog;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.Items.ItemGroup;
import com.sscctv.seeeyesonvif.Items.ItemMainDevice;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Service.CallDevice;
import com.sscctv.seeeyesonvif.Map.utils.ItemMapList;

import java.io.IOException;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppUtils {
    private static final String TAG = AppUtils.class.getSimpleName();
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());

    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    public static final int GROUP_HEADER = 0;
    public static final int GROUP_CHILD = 1;

    public static final String NONE = "none";
    public static final String ETH_STATIC = "STATIC";
    public static final String ETH_DHCP = "DHCP";

    public static final String CALL_MODE_INCOMING = "Incoming";
    public static final String CALL_MODE_OUTGOING = "Outgoing";
    public static final String CALL_MODE_NORMAL = "Normal";

    public static final String CALL_STATUS_SUCCESS = "Success";
    public static final String CALL_STATUS_ABORTED = "Aborted";
    public static final String CALL_STATUS_MISSED = "Missed";
    public static final String CALL_STATUS_DECLINED = "Declined";
    public static final String CALL_STATUS_END = "End";

    public static final String ECHO_SPEAKER_GAIN = "02A0";
    public static final String ECHO_MIC_GAIN = "02B2";
    public static final String ECHO_RESET = "0006";

    public static final String EDAL_MODE_SEARCH = "edal_search";
    public static final String EDAL_MODE_INFO = "edal_info";
    public static final String EDAL_MODE_CHANGE = "edal_change";
    public static final String EDAL_MODE_RESET = "edal_reset";
    public static final String EDAL_MODE_DEFAULT = "edal_default";
    public static final String EDAL_MODE_EMLOCK = "edal_emlock";


    public static void printShort(Context context, String str) {

        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static void dispatchOnUIThread(Runnable r) {
        sHandler.post(r);
    }


    public static void putItemDevConfigList(TinyDB tinyDB, String key, ArrayList<ItemDevice> items) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        for (ItemDevice item : items) {
            objString.add(gson.toJson(item));
        }
        tinyDB.putListString(key, objString);
    }

    public static ArrayList<ItemDevice> getItemDevConfigList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<ItemDevice> list = new ArrayList<>();
        for (String jObjString : objStrings) {
            ItemDevice item = gson.fromJson(jObjString, ItemDevice.class);
            list.add(item);
        }
        return list;
    }

    public static void putItemDoorDeviceList(TinyDB tinyDB, String key, ArrayList<ItemDoorDevice> items) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        for (ItemDoorDevice item : items) {
            objString.add(gson.toJson(item));
        }
        tinyDB.putListString(key, objString);
    }

    public static ArrayList<ItemDoorDevice> getItemDoorDeviceList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<ItemDoorDevice> list = new ArrayList<>();
        for (String jObjString : objStrings) {
            ItemDoorDevice item = gson.fromJson(jObjString, ItemDoorDevice.class);
            list.add(item);
        }
        return list;
    }

    public static void putGroupList(TinyDB tinyDB, String key, ArrayList<ItemGroup> items) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        for (ItemGroup item : items) {
            objString.add(gson.toJson(item));
        }
        tinyDB.putListString(key, objString);
    }

    public static ArrayList<ItemGroup> getGroupList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<ItemGroup> list = new ArrayList<>();
        for (String jObjString : objStrings) {
            ItemGroup item = gson.fromJson(jObjString, ItemGroup.class);
            list.add(item);
        }
        return list;
    }

    public static void putMapConfigList(TinyDB tinyDB, String key, ArrayList<ItemMapList> items) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        for (ItemMapList item : items) {
            objString.add(gson.toJson(item));
        }
        tinyDB.putListString(key, objString);
    }

    public static ArrayList<ItemMapList> getMapConfigList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<ItemMapList> list = new ArrayList<>();
        for (String jObjString : objStrings) {
            ItemMapList item = gson.fromJson(jObjString, ItemMapList.class);
            list.add(item);
        }
        return list;
    }


    public static void putCallDeviceList(TinyDB tinyDB, String key, ArrayList<CallDevice> items) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        for (CallDevice item : items) {
            objString.add(gson.toJson(item));
        }
        tinyDB.putListString(key, objString);
    }

    public static ArrayList<CallDevice> getCallDeviceList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<CallDevice> list = new ArrayList<>();
        for (String jObjString : objStrings) {
            CallDevice item = gson.fromJson(jObjString, CallDevice.class);
            list.add(item);
        }
        return list;
    }


    public static void putCallLog(TinyDB tinyDB, String key, ArrayList<ItemEmCallLog> items) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        for (ItemEmCallLog item : items) {
            objString.add(gson.toJson(item));
        }
        tinyDB.putListString(key, objString);
    }

    public static ArrayList<ItemEmCallLog> getCallLog(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<ItemEmCallLog> list = new ArrayList<>();
        for (String jObjString : objStrings) {
            ItemEmCallLog item = gson.fromJson(jObjString, ItemEmCallLog.class);
            list.add(item);
        }
        return list;
    }

    public static void putMainDeviceList(TinyDB tinyDB, String key, ArrayList<ItemMainDevice> items) {
        Gson gson = new Gson();
        ArrayList<String> objString = new ArrayList<>();
        for (ItemMainDevice item : items) {
            objString.add(gson.toJson(item));
        }
        tinyDB.putListString(key, objString);
    }

    public static ArrayList<ItemMainDevice> getMainDeviceList(TinyDB tinyDB, String key) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = tinyDB.getListString(key);
        ArrayList<ItemMainDevice> list = new ArrayList<>();
        for (String jObjString : objStrings) {
            ItemMainDevice item = gson.fromJson(jObjString, ItemMainDevice.class);
            list.add(item);
        }
        return list;
    }



    public static boolean isIpAddress(String ipAddress) {
//        Log.d(TAG, "isIpAddress: " + ipAddress);
        boolean returnValue = false;

        String regex = "^([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3}).([0-9]{1,3})$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(ipAddress);

        if (m.matches()) {
            returnValue = true;
        }
        return returnValue;
    }


    public static boolean netPingChk(String ip) {
        Runtime runtime = Runtime.getRuntime();

        String cmd = "ping -c 1 -W 2 " + ip;

        Process process = null;

        try {
            process = runtime.exec(cmd);
        } catch (IOException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        try {
            assert process != null;
            process.waitFor();
        } catch (InterruptedException e) {
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        int result = process.exitValue();

        return result == 0;
    }

    public static String sendRequest(Context context, RequestQueue requestQueue, String url) {
        final String[] result = {null};
        StringRequest request = new StringRequest(
                Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "응답: " + response);
                    result[0] = response;

                },
                error -> Log.e(TAG, "에러: " + error.getLocalizedMessage())
        );
        request.setShouldCache(false);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        requestQueue.add(request);

        return result[0];
    }

    public static void startNewIntent(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static String currentDate() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd a", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String currentTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getConfigListLocation(String mac) {
        TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
        ArrayList<ItemDevice> temp = getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);
        String location = "None";
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).getMac().equals(mac)) {
                location = temp.get(i).getLoc();
                break;
            }
        }

        return location;
    }

    public static Bundle setBundle(String tag, String msg) {
        Bundle bundle = new Bundle();
        bundle.putString(tag, msg);
        return bundle;
    }

    public static int modelIconImage(String model) {

        switch (model) {
            case ModelList.MCS10:
                return R.drawable.icon_mcs10;
            case ModelList.DCS01C:
            case ModelList.DCS01E:
            case ModelList.ECS30CMDCS:
                return R.drawable.icon_ecs30cmdcs;
            case ModelList.DCS01N:
            case ModelList.ECS30NMDCS:
                return R.drawable.icon_ecs30nmdcs;
            case ModelList.ECS30CW:
                return R.drawable.icon_ecs30cw;
            case ModelList.ECS30CWP:
                return R.drawable.icon_ecs30cwp;
            default:
                return R.drawable.null_device;
        }
    }


    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("eth0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ignored) {
        }
        return "00:00:00:00:00:00";
    }


}
