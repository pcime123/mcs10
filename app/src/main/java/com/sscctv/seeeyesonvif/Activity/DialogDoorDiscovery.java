package com.sscctv.seeeyesonvif.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.InputFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sscctv.seeeyesonvif.Adapter.AdapterDialogDoorDiscovery;
import com.sscctv.seeeyesonvif.Fragment.FragmentDeviceView;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.Items.ItemDoorDevice;
import com.sscctv.seeeyesonvif.Items.ItemGroup;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Server.SendDoorPacket;
import com.sscctv.seeeyesonvif.Server.SendEmPacket;
import com.sscctv.seeeyesonvif.Server.SendEMTest;
import com.sscctv.seeeyesonvif.__Not_Used.ONVIFRequest;
import com.sscctv.seeeyesonvif.Service.PersistentService;
import com.sscctv.seeeyesonvif.__Lib_JIGU.Device_DM_Protocol;
import com.sscctv.seeeyesonvif.__Lib_JIGU.Device_EDAL;
import com.sscctv.seeeyesonvif.__Lib_JIGU.JIGU_Library;
import com.sscctv.seeeyesonvif.__Lib_JIGU.JIGU_Protocol;
import com.sscctv.seeeyesonvif.Interfaces.CallFragOption;
import com.sscctv.seeeyesonvif.Interfaces.OnSelectDoorDevice;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.IPUtilsCommand;
import com.sscctv.seeeyesonvif.Utils.JToastShow;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.ModelList;
import com.sscctv.seeeyesonvif.Utils.StringValidator;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class DialogDoorDiscovery extends Dialog {

    private static final String TAG = DialogDoorDiscovery.class.getSimpleName();
    public static final String ERROR_TIMEOUT = "error_timeout";
    public static final String ERROR_IO_EXCEPTION = "error_io_exception";
    public static final String ERROR_SOAP_AUTH = "error_soap_auth";
    public static final String ERROR_SOAP_FAULT = "error_soap_fault";
    private final Context context;
    private TextView txtModel, txtMac, txtVersion, txtHttp, txtHttps;
    private RecyclerView listDoor;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private final CallFragOption option;
    private boolean isRunning = false;
    private InputMethodManager imm;
    private JToastShow jToastShow;
    private ItemDoorDevice selectConfig;
    private Dialog dialogChange, dialogRegister, dialogCamIpChange;
    private RequestQueue requestQueue;
    private Button btnSearch, btnClose, btnDoorChange, btnActivation, btnDefault, sortModel, sortIp, sortMac, sortLoc, btnDoorCamAct, btnDoorCamIpChange, btnDoorDefault;
    private ArrayList<ItemDoorDevice> list;
    private AdapterDialogDoorDiscovery adapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean sModel, sIp, sMac, sLoc;
    private String edalCamIp;
    private PersistentService mService;

    public DialogDoorDiscovery(@NonNull Context context, CallFragOption option) {
        super(context);
        this.context = context;
        this.option = option;
        Log.w(TAG, "DialogDiscoveryDialogDiscoveryDialogDiscovery");
        Intent service = new Intent(getContext(), PersistentService.class);
        // get service.
        // callback registration
        ServiceConnection mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                PersistentService.BindServiceBinder binder = (PersistentService.BindServiceBinder) service;
                mService = binder.getService(); // get service.
                mService.registerCallback(mCallback); // callback registration
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }
        };
        getContext().bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "onCreate");


        // Dialog View Settings
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        if (getWindow() != null) {
            getWindow().setAttributes(layoutParams);
            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        // Get Layout
        setContentView(R.layout.dialog_door_search);

        // Set InputMethod Manger
        imm = (InputMethodManager) Objects.requireNonNull(context).getSystemService(Context.INPUT_METHOD_SERVICE);

        // Set RequestQueue
        if (requestQueue != null) {
            requestQueue = Volley.newRequestQueue(context);
        }

        // Set JToastShow
        jToastShow = new JToastShow();
        dialogChange = new Dialog(context);
        selectConfig = null;

        initFindViewById();
        initSortBtn(0);
        initBtnListener();
//        initIpFormat();

        @SuppressLint("HandlerLeak") Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mService.stopBroadCastTask(IPUtilsCommand.MODE_DISCOVERY);
            }
        };
        handler.sendEmptyMessageDelayed(0, 500);


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        requestQueue = null;
        isRunning = false;
    }

    private void initFindViewById() {
        btnClose = findViewById(R.id.btnDoorClose);
        btnSearch = findViewById(R.id.btnDoorSearch);
        btnDoorChange = findViewById(R.id.btnDoorChange);
        btnDoorDefault = findViewById(R.id.btnDoorDefault);
        btnDoorCamIpChange = findViewById(R.id.btnDoorCamChange);
        btnDoorCamAct = findViewById(R.id.btnDoorCamAct);
        sortModel = findViewById(R.id.btnDoorSortModel);
        sortIp = findViewById(R.id.btnDoorSortIp);
        sortMac = findViewById(R.id.btnDoorSortMac);
        sortLoc = findViewById(R.id.btnDoorSortLoc);

        listDoor = findViewById(R.id.listDoor);
        listDoor.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        listDoor.setLayoutManager(layoutManager);
    }

    private void initBtnListener() {
        btnSearch.setOnClickListener(view -> {
            refreshChangeData();
//        mService.stopBroadCastTask(IPUtilsCommand.MODE_DISCOVERY);
        });

        btnDoorChange.setOnClickListener(view -> {
            if (selectConfig == null) {
                jToastShow.createToast(context, "실패", "선택된 장비가 없습니다. 검색 후 장비를 선택하세요.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                return;
            }

            doorIpChangeDialog(selectConfig);

//            if (checkModelName(txtModel.getText().toString())) {
//                TaskEDALIpChange(new EDALIpChangeTask(txtMac.getText().toString(), editIp.getText().toString(), editGateway.getText().toString(), editMask.getText().toString()));
//            } else {
//                if (listCheck()) {
//                    for (int i = 0; i < list.size(); i++) {
//                        if (list.get(i).getDoorIp().equals(Objects.requireNonNull(editIp.getText()).toString())) {
//                            checkCompareChange(selectConfig);
//                            break;
//                        } else {
//                            if (i == list.size() - 1) {
//                                changeDialog(selectConfig);
//                            }
//                        }
//
//                    }
//                }
//            }

        });

        btnDoorCamAct.setOnClickListener(view -> {
            if (selectConfig == null) {
                jToastShow.createToast(context, "실패", "선택된 장비가 없습니다. 검색 후 장비를 선택하세요.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                return;
            }
            if (!selectConfig.getcMac().equals("192.168.100.100")) {
                jToastShow.createToast(context, "실패", "선택한 장비는 초기 장비가 아닙니다. 192.168.100.100 인 장비만 활성화 시킬 수 있습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
                return;
            }

            activationDialog(selectConfig);
        });

        btnDoorCamIpChange.setOnClickListener(view -> {
            if (selectConfig == null) {
                jToastShow.createToast(context, "실패", "선택된 장비가 없습니다. 검색 후 장비를 선택하세요.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                return;
            }
            camIpChangeDialog(selectConfig);
        });

        btnDoorDefault.setOnClickListener(view ->

        {
            if (selectConfig == null) {
                jToastShow.createToast(context, "실패", "선택된 장비가 없습니다. 검색 후 장비를 선택하세요.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                return;
            }
//            if (listCheck()) {
            //TODO Factory Default
            doorResetDialog(selectConfig);
//            }
        });

        btnClose.setOnClickListener(view ->
        {
            mService.startBroadCastTask();
            ((ActivityMain) ActivityMain.context).setGoFragment(new FragmentDeviceView());
            dismiss();
        });

        sortModel.setOnClickListener(view ->

        {
            if (listCheck()) {
                if (!sModel) {
                    sModel = true;
                    sortModel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);
                    Collections.sort(list, (a1, p1) -> p1.getdModel().compareTo(a1.getdModel()));
                } else {
                    sModel = false;
                    sortModel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                    Collections.sort(list, (a1, p1) -> a1.getdModel().compareTo(p1.getdModel()));
                }
                adapter.notifyDataSetChanged();
                initSortBtn(1);
            }
        });


        sortIp.setOnClickListener(view ->

        {
            if (listCheck()) {
                if (!sIp) {
                    sIp = true;
                    sortIp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);
                    Collections.sort(list, (a1, p1) -> p1.getdIp().compareTo(a1.getdIp()));
                } else {
                    sIp = false;
                    sortIp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                    Collections.sort(list, (a1, p1) -> a1.getdIp().compareTo(p1.getdIp()));
                }
                adapter.notifyDataSetChanged();
                initSortBtn(3);
            }
        });

        sortMac.setOnClickListener(view ->

        {
            if (listCheck()) {
                if (!sMac) {
                    sMac = true;
                    sortMac.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);
                    Collections.sort(list, (a1, p1) -> p1.getdMac().compareTo(a1.getdMac()));
                } else {
                    sMac = false;
                    sortMac.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                    Collections.sort(list, (a1, p1) -> a1.getdMac().compareTo(p1.getdMac()));
                }
                adapter.notifyDataSetChanged();
                initSortBtn(4);
            }
        });
        sortLoc.setOnClickListener(view ->

        {
            if (listCheck()) {
                if (!sLoc) {
                    sLoc = true;
                    sortLoc.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_24dp, 0);
                    Collections.sort(list, (a1, p1) -> p1.getLoc().compareTo(a1.getLoc()));
                } else {
                    sLoc = false;
                    sortLoc.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                    Collections.sort(list, (a1, p1) -> a1.getLoc().compareTo(p1.getLoc()));
                }
                adapter.notifyDataSetChanged();
                initSortBtn(5);
            }
        });
    }

    private boolean listCheck() {
        if (list != null && list.size() != 0) {
            return true;
        }
        jToastShow.createToast(context, "실패", "검색된 장비가 없습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
        return false;
    }

    private Bundle setBundle(String tag, String msg) {
        Bundle bundle = new Bundle();
        bundle.putString(tag, msg);
        return bundle;
    }

    private void initSortBtn(int mode) {
        switch (mode) {
            case 0:
                sModel = false;
                sIp = false;
                sMac = false;
                sLoc = false;
                break;
            case 1:
                sIp = false;
                sMac = false;
                sLoc = false;
                sortIp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                sortMac.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                sortLoc.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
            case 3:
                sModel = false;
                sMac = false;
                sLoc = false;
                sortModel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                sortMac.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                sortLoc.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
            case 4:
                sModel = false;
                sIp = false;
                sLoc = false;
                sortModel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                sortIp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                sortLoc.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
            case 5:
                sModel = false;
                sIp = false;
                sMac = false;
                sortModel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                sortIp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                sortMac.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_24dp, 0);
                break;
        }
    }


    private void refreshChangeData() {
        if (dialogChange.isShowing()) {
            dialogChange.dismiss();
        }

        isRunning = true;

        try {
            TaskRunDiscovery(new ReceivedBroadcast());
            SystemClock.sleep(100);
            new Thread(new SendEMTest(AppUtils.EDAL_MODE_SEARCH)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private final PersistentService.ICallback mCallback = () -> {
        refreshChangeData();
    };

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            assert imm != null;
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void TaskRunDiscovery(ReceivedBroadcast asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class ReceivedBroadcast extends AsyncTask<Void, Void, Void> implements OnSelectDoorDevice {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        String duplicateLocation;
        DatagramSocket emSocket;

        private boolean isEms = false;

        @Override
        protected void onPreExecute() {
            list = new ArrayList<>();
            adapter = new AdapterDialogDoorDiscovery(getContext(), list, this);
            listDoor.setAdapter(adapter);

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("검색 중...");
            progressDialog.show();

            isEms = true;
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                emSocket = new DatagramSocket(JIGU_Protocol.RECEIVE_PORTNUM);
                emSocket.setSoTimeout(3000);
                while (isEms) {
                    byte[] receiveBuffer = new byte[512];

                    byte[] JiGuKey = new byte[4];
                    JiGuKey[0] = 0;

                    DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    emSocket.receive(packet);
                    Log.d(TAG, "GetPacket: " + Arrays.toString(packet.getData()));
                    int data_length = packet.getLength();
                    byte[] result_buffer = new byte[data_length];

                    packet.setData(receiveBuffer, 0, data_length);
//                    Log.v(TAG, DeviceManagement_Library.byteArrayToHexString(receiveBuffer, 0, data_length));

                    byte error = JIGU_Protocol.JiGu_Decode(receiveBuffer, data_length, result_buffer, JiGuKey);
                    switch (error) {
                        case JIGU_Protocol.JiGu_Error_OK:
                            process_Data(result_buffer, packet.getAddress().toString());
                            publishProgress();
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
                e.printStackTrace();
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
            adapter.notifyDataSetChanged();
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            selectConfig = null;
            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }


        @Override
        public void onSelectDoorList(ItemDoorDevice itemDevConfig) {
//            if (!modelDCSSeries(itemDevConfig.getModel())){
            selectDevice(itemDevConfig);
//            } else {
//
//            }
        }


        @Override
        public void onRegisterDoorList(ItemDoorDevice itemDevConfig) {
            Log.d(TAG, "List Select: " + itemDevConfig.getdMac());
            if (checkDuplicate(list, itemDevConfig)) {
                edalCamIp = null;
                registerDialog(itemDevConfig);
            }
        }

        private boolean listCompare(String macAddress) {
            boolean val = false;
            ArrayList<ItemDevice> temp = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);

            for (int i = 0; i < temp.size(); i++) {
                if (temp.get(i).getMac().equals(macAddress)) {
                    duplicateLocation = temp.get(i).getLoc();
                    val = true;
                    break;
                }
            }
            return val;
        }

        private boolean modelCompare(String modelName) {
            return modelName.equals(ModelList.ECS30CW) || modelName.equals(ModelList.ECS30CWP) || modelName.equals(ModelList.DCS01C)
                    || modelName.equals(ModelList.DCS01E) || modelName.equals(ModelList.DCS01N);
        }

        private boolean modelDCSSeries(String modelName) {
            return modelName.equals(ModelList.DCS01C)
                    || modelName.equals(ModelList.DCS01E) || modelName.equals(ModelList.DCS01N);
        }

        private void process_Data(byte[] Data, String IP) {
            int Mode = JIGU_Library.HexByteToInteger(Data, 1, 1);
            try {
                switch (Mode) {
                    case JIGU_Protocol.GET_NET_INFO_RESPONSE_CAMERA:
                        Process_NetInfo(Data);
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        private void Process_NetInfo(byte[] Data) {

            String dMac = JIGU_Library.byteArrayToMacAddress(Data, 2, 7).trim();
            String dIp = JIGU_Library.byteArrayToIPAddress(Data, 8, 11);
            String dGateWay = JIGU_Library.byteArrayToIPAddress(Data, 12, 15);
            String dNetMask = JIGU_Library.byteArrayToIPAddress(Data, 16, 19);
            String dModel = JIGU_Library.byteArrayToDeviceModel(Data, 20, 33).trim();
            String dVersion = JIGU_Library.byteArrayToHexString1(Data, 33, 34).trim();
            byte[] bMode = Arrays.copyOfRange(Data, 34, 35);
            String cIp = JIGU_Library.byteArrayToIPAddress(Data, 35, 38).trim();
            String cNetMask = JIGU_Library.byteArrayToIPAddress(Data, 39, 42).trim();
            String cGateWay = JIGU_Library.byteArrayToIPAddress(Data, 43, 46).trim();
            String cMac = JIGU_Library.byteArrayToMacAddress(Data, 47, 52).trim();
            String loc = "입력 안됨";

            ArrayList<ItemDevice> temp = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);

            for (int i = 0; i < temp.size(); i++) {
                Log.d(TAG, "CMAC: " + cMac + " listMac: " + temp.get(i).getMac());

                if (cMac.equals(temp.get(i).getMac())) {
                    loc = temp.get(i).getLoc();
                    break;
                }
            }
            if (modelCompare(dModel)) {
                list.add(new ItemDoorDevice(dMac, dIp, dNetMask, dGateWay, dModel, cMac, cIp, cNetMask, cGateWay, loc, ""));
            }
//            Log.d(TAG, "MAC: " + strMac + " IP: " + strIpAddress + " Name: " + strModelName + " Gateway: " + strGateWay + " NetMask: " + strNetMask
//                    + " Version:" + strVersion + " CamIp: " + strCamIp + " CamNenMask: " + strCamNetMask + " CamGateWay: " + strCamGateWay + " CamMac: " + strCamMac);
        }

        private StringBuilder getVersion(byte[] val) {
            StringBuilder device;
            device = new StringBuilder();
            String str = Arrays.toString(val);
            str.substring(0, 1);
            return device.append(str.substring(1, 2)).append(".").append(str.substring(2, 3)).append(".").append(str.substring(3, 4));


        }
    }


    private void TaskCamIpChange(camIpChangeTask asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void doorIpChangeDialog(ItemDoorDevice itemDoorDevice) {
        dialogChange = new Dialog(context);
        dialogChange.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogChange.setContentView(R.layout.dialog_door_change);
        dialogChange.setCancelable(false);
        dialogChange.setCanceledOnTouchOutside(false);

        final JToastShow jToastShow = new JToastShow();
        final EditText doorIp = dialogChange.findViewById(R.id.dg_door_ip);
        final EditText doorNetMask = dialogChange.findViewById(R.id.dg_door_netmask);
        final EditText doorGateWay = dialogChange.findViewById(R.id.dg_door_gateway);
        final Button btnChange = dialogChange.findViewById(R.id.dgBtnAdd);
        final Button btnClose = dialogChange.findViewById(R.id.dgBtnClose);

        InputFilter[] filters = new InputFilter[1];
        filters[0] = (source, start, end, dest, dstart, dend) -> {
            if (end > start) {
                String destTxt = dest.toString();
                String resultingTxt = destTxt.substring(0, dstart) +
                        source.subSequence(start, end) + destTxt.substring(dend);
                if (!resultingTxt.matches("^\\d{1,3}(\\." + "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                    return "";
                } else {
                    String[] splits = resultingTxt.split("\\.");
                    for (String split : splits) {
                        if (Integer.parseInt(split) > 255) {
                            return "";
                        }
                    }
                }
            }
            return null;
        };
        doorIp.setFilters(filters);
        doorNetMask.setFilters(filters);
        doorGateWay.setFilters(filters);

        doorIp.setText(itemDoorDevice.getdIp());
        doorNetMask.setText(itemDoorDevice.getdNetMask());
        doorGateWay.setText(itemDoorDevice.getdGateWay());

        btnChange.setOnClickListener(view -> {
            hideSoftKeyboard();

            try {

//                String newVal = editIp.getText().toString() + "_@#@_" +
//                        editMask.getText().toString() + "_@#@_" +
//                        editGateway.getText().toString();
//
//                isRunning = true;
//                new Thread(new SendEMTest(AppUtils.EDAL_MODE_CHANGE)).start();
//                list.add(new ItemDoorDevice(dMac, dIp, dNetMask, dGateWay, dModel, cMac, cIp, cNetMask, cGateWay, "입력 안됨", ""));

                Log.d(TAG, "Door IP: " + doorIp.getText());
                TaskDoorIpChange(new DoorIpChange(new ItemDoorDevice(itemDoorDevice.getdMac(),
                        doorIp.getText().toString(), doorNetMask.getText().toString(), doorGateWay.getText().toString(), itemDoorDevice.getdModel(), itemDoorDevice.getcMac()
                        , itemDoorDevice.getcIp(), itemDoorDevice.getcNetMask(), itemDoorDevice.getcGateWay(), itemDoorDevice.getLoc(), itemDoorDevice.getGroup())));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnClose.setOnClickListener(view -> {
            dialogChange.dismiss();
        });
        dialogChange.show();
    }


    private void TaskDoorIpChange(DoorIpChange asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class DoorIpChange extends AsyncTask<Void, Void, Void> implements OnSelectDoorDevice {
        private ProgressDialog progressDialog = new ProgressDialog(getContext());
        private String duplicateLocation;
        private DatagramSocket emSocket;
        private String strMac, strIp, strGw, strNm;
        private ItemDoorDevice itemDoorDevice;
        private boolean isEms = false;

        public DoorIpChange(ItemDoorDevice itemDoorDevice) {
            this.itemDoorDevice = itemDoorDevice;

        }

        @Override
        protected void onPreExecute() {
            list = new ArrayList<>();
            adapter = new AdapterDialogDoorDiscovery(getContext(), list, this);
            listDoor.setAdapter(adapter);

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
            Log.d(TAG, "!! Door IP: " + itemDoorDevice.getdIp());

            new Thread(new SendDoorPacket(AppUtils.EDAL_MODE_CHANGE, itemDoorDevice)).start();


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

//                    System.out.println(DeviceManagement_Library.byteArrayToHexString(receiveBuffer, 0, data_length));
                    String DMP = String.valueOf(result_buffer[1]);
                    String Mac = JIGU_Library.byteArrayToMacAddress(result_buffer, 2, 7).trim();

                    Log.d(TAG, "DMP: " + DMP + " Mac: " + Mac);
                    switch (error) {
                        case JIGU_Protocol.JiGu_Error_OK:
                            if (DMP.equals(String.valueOf(JIGU_Protocol.SET_IP_RESPONE)) && Mac.equals(itemDoorDevice.getdMac())) {
                                new Thread(new SendDoorPacket(AppUtils.EDAL_MODE_RESET, itemDoorDevice)).start();
                            } else if (DMP.equals(String.valueOf(JIGU_Protocol.SET_RESET_RESPONE))) {
                                progressDialog.setMessage("장비 부팅 중...");
                                while (true) {
                                    if (pingTest(itemDoorDevice.getdIp())) {
                                        break;
                                    }
                                }
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
            if (dialogChange.isShowing()) {
                dialogChange.dismiss();
            }
            try {
                TaskRunDiscovery(new ReceivedBroadcast());
                SystemClock.sleep(100);
                new Thread(new SendEMTest(AppUtils.EDAL_MODE_SEARCH)).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(aVoid);
        }

        private boolean pingTest(String ip) {
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
            Log.d(TAG, "PingTest: " + result);
            return result == 0;
        }


        @Override
        public void onSelectDoorList(ItemDoorDevice itemDevConfig) {
        }


        @Override
        public void onRegisterDoorList(ItemDoorDevice itemDevConfig) {
        }

    }

    private void activationDialog(ItemDoorDevice itemDoorDevice) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_door_cam_activation);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final JToastShow jToastShow = new JToastShow();
        final TextView title = dialog.findViewById(R.id.dgTxtDoorCamActTitle);
        final TextInputEditText editId = dialog.findViewById(R.id.dgEditDoorCamId);
        final TextInputEditText editPass = dialog.findViewById(R.id.dgEditDoorCamPass);
        final TextInputLayout layoutPass = dialog.findViewById(R.id.dgLayActPass);
        final Button btnActChk = dialog.findViewById(R.id.dgBtnActChk);
        final Button btnActivation = dialog.findViewById(R.id.dgBtnDoorCamActApply);
        final Button btnClose = dialog.findViewById(R.id.dgBtnDoorCamActClose);


//        title.setText("Activation Device");
//        btnActivation.setText("Activation");

        editId.setText("root");
        editId.setEnabled(false);
        layoutPass.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

        String defaultIpAddress = "192.168.100.100";
        String defaultPort = "80";

        btnActChk.setOnClickListener(view -> {
            try {
                if (AppUtils.netPingChk(defaultIpAddress)) {
                    StringRequest request = new StringRequest(
                            Request.Method.GET, IPUtilsCommand.cgiCheckActivation(defaultIpAddress, defaultPort),
                            response -> {
                                if (response.contains("SUCCESS") || response.contains("The user_id already exists.")) {
                                    jToastShow.createToast(context,
                                            "장비 비활성화 상태",
                                            "선택된 장비는 비활성화 상태 입니다. 활성화를 진행해주세요.", jToastShow.TOAST_WARNING, 80, jToastShow.SHORT_DURATION, null);
                                } else {
                                    if (response.contains("Authentication Fail")) {
                                        jToastShow.createToast(context, "장비 활성화 상태", "선택된 장비는 활성화 상태 입니다. ", jToastShow.TOAST_SUCCESS, 80, jToastShow.SHORT_DURATION, null);
                                    }
                                }
                                Log.d(TAG, "OK Res: " + response);
                            },
                            error -> {
                                Log.e(TAG, "Error Res: " + error.getLocalizedMessage());
//                                jToastShow.createToast(context,
//                                        "문제가 발생했습니다.",
//                                        error.getLocalizedMessage(), jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                            }
                    );

                    request.setShouldCache(false);
                    if (requestQueue == null) {
                        requestQueue = Volley.newRequestQueue(context);
                    }
                    requestQueue.add(request);
                } else {
                    jToastShow.createToast(context,
                            "문제가 발생했습니다.", "네트워크 주소를 확인해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnActivation.setOnClickListener(view -> {
            hideSoftKeyboard();

            if (Objects.requireNonNull(editId.getText()).toString().isEmpty()) {
                jToastShow.createToast(context, "입력 오류", "아이디가 입력되지 않았습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                return;
            }

            if (Objects.requireNonNull(editPass.getText()).toString().isEmpty()) {
                jToastShow.createToast(context, "입력 오류", "비밀번호가 입력되지 않았습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                return;
            }

            if (StringValidator.isPasswordCharacter(editPass.getText().toString())) {
                if (StringValidator.continuousPwd(editPass.getText().toString())) {
                    try {
                        StringRequest request = new StringRequest(
                                Request.Method.GET, IPUtilsCommand.cgiActivation("192.168.100.100", "80", editId.getText().toString(), editPass.getText().toString()),
                                response -> {
                                    if (response.contains("SUCCESS") || response.contains("The user_id already exists.")) {
                                        jToastShow.createToast(context,
                                                "성공",
                                                "장비가 정상적으로 활성화 되었습니다.", jToastShow.TOAST_SUCCESS, 80, jToastShow.SHORT_DURATION, null);
                                        dialog.dismiss();
                                        refreshChangeData();
                                        option.refreshFragment();
                                    } else {
                                        if (response.contains("Authentication Fail")) {
                                            jToastShow.createToast(context, "장비 활성화를 실패 했습니다", "암호를 다시 확인해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                                        }
                                    }
                                    Log.d(TAG, "OK Res: " + response);
                                },
                                error -> {
                                    Log.e(TAG, "Error Res: " + error.getLocalizedMessage());
                                    jToastShow.createToast(context,
                                            "장비 활성화를 실패 했습니다",
                                            error.getLocalizedMessage(), jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                                }
                        );

                        request.setShouldCache(false);
                        if (requestQueue == null) {
                            requestQueue = Volley.newRequestQueue(context);
                        }
                        requestQueue.add(request);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    jToastShow.createToast(context, "4개 이상의 연속 문자는 허용되지 않습니다.", "4개 이상의 반복 문자는 허용되지 않습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                }
            } else {
                jToastShow.createToast(context, "비밀번호 길이는 8~16자, 영문자와 숫자 특수문자를 포함해야합니다.", "~'!$^*()_-{}[].;, 특수문자만 사용가능 합니다.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
            }

        });

        btnClose.setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void registerDialog(ItemDoorDevice itemDevConfig) {
        dialogRegister = new Dialog(context);
        dialogRegister.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogRegister.setContentView(R.layout.dialog_door_add);
        dialogRegister.setCancelable(false);
        dialogRegister.setCanceledOnTouchOutside(false);

        final JToastShow jToastShow = new JToastShow();
        final TextView txtDoorIp = dialogRegister.findViewById(R.id.dgTxtDoorIp);
        final TextView txtCamIp = dialogRegister.findViewById(R.id.dgTxtDoorCamIp);
        final TextInputEditText editId = dialogRegister.findViewById(R.id.dgEditDoorCamId);
        final TextInputEditText editPass = dialogRegister.findViewById(R.id.dgEditDoorCamPass);
        final TextInputEditText editLoc = dialogRegister.findViewById(R.id.dgEditDoorCamLoc);
        final TextInputLayout layoutPass = dialogRegister.findViewById(R.id.dgLayDoorPass);
        final TextInputLayout layoutLoc = dialogRegister.findViewById(R.id.dgLayDoorLoc);
        final Button btnAdd = dialogRegister.findViewById(R.id.dgBtnDoorAdd);
        final Button btnClose = dialogRegister.findViewById(R.id.dgBtnDoorClose);
        final AppCompatSpinner spinner = dialogRegister.findViewById(R.id.dgListDoorGroup);
        final EditText editHttp = dialogRegister.findViewById(R.id.dgEditDoorCamHttp);
        final EditText editHttps = dialogRegister.findViewById(R.id.dgEditDoorCamHttps);

        txtDoorIp.setText(itemDevConfig.getdIp());
        txtCamIp.setText(itemDevConfig.getcIp());
        editId.setText("root");
        editHttp.setText("80");
        editHttps.setText("443");

        layoutPass.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        layoutLoc.setCounterEnabled(true);
        layoutLoc.setCounterMaxLength(20);

        ArrayList<ItemGroup> itemGroups = AppUtils.getGroupList(tinyDB, KeyList.LIST_GROUP_HEADER);
        ArrayList<String> groupList = new ArrayList<>();
        for (int i = 0; i < itemGroups.size(); i++) {
            groupList.add(itemGroups.get(i).getGroup());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.spinner_normal, R.id.spinnerText, groupList);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                hideSoftKeyboard();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnAdd.setOnClickListener(view -> {
            hideSoftKeyboard();
//            String ip = itemDevConfig.getIp();
//            String http = itemDevConfig.getHttp();
//            String mac = itemDevConfig.getMac();
            String id = Objects.requireNonNull(editId.getText()).toString();
            String pass = Objects.requireNonNull(editPass.getText()).toString();
            String loc = Objects.requireNonNull(editLoc.getText()).toString();
            String group = spinner.getSelectedItem().toString();

            TaskRegister(new RegisterTask(itemDevConfig, id, pass, loc, group, editHttp.getText().toString(), editHttps.getText().toString(), option));
        });

        btnClose.setOnClickListener(view -> {
            Log.d(TAG, "getCamIp: " + edalCamIp);
            dialogRegister.dismiss();
        });


        dialogRegister.show();
    }

    private void TaskRegister(RegisterTask asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class RegisterTask extends AsyncTask<String, Integer, Integer> {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        private String regIp, regMac, regModel;
        private final String regId, regPass, regGroup, regLoc, regHttp, regHttps;
        private final ItemDoorDevice itemDoorDevice;
        private ArrayList<ItemDevice> itemDevices;
        private ArrayList<ItemDoorDevice> listDoorDevice;
        private final CallFragOption option;

        public RegisterTask(ItemDoorDevice itemDoorDevice, String regId, String regPass, String regLoc, String regGroup, String regHttp, String regHttps, CallFragOption option) {
            this.itemDoorDevice = itemDoorDevice;
            this.regId = regId;
            this.regLoc = regLoc;
            this.regPass = regPass;
            this.regGroup = regGroup;
            this.regHttp = regHttp;
            this.regHttps = regHttps;
            this.option = option;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            itemDevices = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);
            listDoorDevice = AppUtils.getItemDoorDeviceList(tinyDB, KeyList.LIST_DOOR_DEVICE);

            regIp = itemDoorDevice.getcIp();
            regMac = itemDoorDevice.getdMac();

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("등록 중...");
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            for (int i = 0; i < itemDevices.size(); i++) {
                if (itemDevices.get(i).getMac().equals(regMac)) {
                    return 0;
                }
            }
            if (regId.isEmpty()) {
                return 6;
            }
            if (regPass.isEmpty()) {
                return 7;
            }
            if (Objects.requireNonNull(regLoc.equals(""))) {
                return 1;
            }
            if (!AppUtils.netPingChk(regIp)) {
                return 2;
            }

            Log.v(TAG, "IP: " + regIp + ", " + regHttp + " , " + regId + " , " + regPass);
            String strUri = getRtspUri(regIp, regHttp, regId, regPass);

            switch (strUri) {
                case "Error":
                    return 3;
                case ERROR_TIMEOUT:
                    return 4;
                case ERROR_IO_EXCEPTION:
                    return 5;
            }

            switch (itemDoorDevice.getdModel()) {
                case ModelList.DCS01C:
                    regModel = ModelList.ECS30CMDCS;
                    break;
                case ModelList.DCS01E:
                case ModelList.DCS01N:
                    regModel = ModelList.ECS30NMDCS;
                    break;
            }

            try {
                StringRequest request = new StringRequest(
                        Request.Method.GET, IPUtilsCommand.cgiCreateAccount(context, regIp, transPort(regHttp), regId, regPass),
                        response -> {
                            if (response.contains("SUCCESS") || response.contains("The user_id already exists.")) {
                                itemDevices.add(new ItemDevice(regModel, itemDoorDevice.getcMac(), "STATIC", itemDoorDevice.getcIp()
                                        , itemDoorDevice.getcNetMask(), itemDoorDevice.getcGateWay(), "None"
                                        , regHttp, regHttps, regLoc, regGroup, strUri, true));
                                StringRequest setupRequest = new StringRequest(
                                        Request.Method.GET, IPUtilsCommand.cgiMcsModeSetup(regIp, transPort(regHttp), "mcs"),
                                        response1 -> {
                                            Log.d(TAG, "cgiMcsModeSetup: " + response1);
                                        },
                                        error -> {
                                            publishProgress(2);
                                            Log.e(TAG, "cgiMcsModeSetup Error Res: " + error.getLocalizedMessage());
                                        }
                                );

                                setupRequest.setShouldCache(false);
                                if (requestQueue == null) {
                                    requestQueue = Volley.newRequestQueue(context);
                                }
                                requestQueue.add(setupRequest);

                                AppUtils.putItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG, itemDevices);

                                listDoorDevice.add(itemDoorDevice);
                                AppUtils.putItemDoorDeviceList(tinyDB, KeyList.LIST_DOOR_DEVICE, listDoorDevice);
                                publishProgress(0);

                            } else {
                                if (response.contains("Authentication Fail")) {
                                    publishProgress(1);
                                }
                            }
                            Log.d(TAG, "OK Res: " + response);
                        },
                        error -> {
                            publishProgress(2);
                            Log.e(TAG, "Error Res: " + error.getLocalizedMessage());
                        }
                );

                request.setShouldCache(false);
                if (requestQueue == null) {
                    requestQueue = Volley.newRequestQueue(context);
                }
                requestQueue.add(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values[0] == 0) {
                jToastShow.createToast(context,
                        "성공",
                        "장비가 정상 등록되었습니다. ", jToastShow.TOAST_SUCCESS, 80, jToastShow.SHORT_DURATION, null);
                refreshChangeData();
                option.refreshFragment();
                dialogRegister.dismiss();
                progressDialog.dismiss();
            } else if (values[0] == 1) {
                jToastShow.createToast(context, "장비 등록을 실패 했습니다", "아이디 또는 암호를 다시 확인해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
            } else if (values[0] == 2) {
                jToastShow.createToast(context, "장비 등록을 실패 했습니다", "네트워크 상태를 확인해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
            }


        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer != null) {
                switch (integer) {
                    case 0:
                        jToastShow.createToast(context, "등록 오류", "이미 등록되어 있는 장비 입니다.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
                        break;
                    case 1:
                        progressDialog.dismiss();
                        jToastShow.createToast(context, "입력 오류", "설치 장소가 입력되지 않았습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
                        return;
                    case 2:
                        jToastShow.createToast(context, "네트워크 오류", "설정하려는 장비와 네트워크 통신이 되지 않습니다.\nIP 주소를 확인해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
                        break;
                    case 3:
                        progressDialog.dismiss();
                        jToastShow.createToast(context, "장비 등록을 실패 했습니다", "아이디 또는 암호를 다시 확인해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
                        return;
                    case 4:
                        jToastShow.createToast(context, "네트워크 문제로 장비 등록을 실패 했습니다", "본 장비의 IP 주소 및 케이블 연결 상태를 확인해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
                        break;
                    case 5:
                        jToastShow.createToast(context, "장비 등록을 실패 했습니다", "본 장비의 IP 주소 및 케이블 연결 상태를 확인해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
                        break;
                    case 6:
                        progressDialog.dismiss();
                        jToastShow.createToast(context, "입력 오류", "아이디가 입력되지 않았습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
                        return;
                    case 7:
                        progressDialog.dismiss();
                        jToastShow.createToast(context, "입력 오류", "패스워드가 입력되지 않았습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
                        return;
                }
                progressDialog.dismiss();
                dialogRegister.dismiss();
            }
        }

        //        @Override
//        protected void onPostExecute(Void aVoid) {
//            progressDialog.dismiss();
//            hideSoftKeyboard();
//            super.onPostExecute(aVoid);
//        }


    }

    private String getRtspUri(String ip, String port, String id, String pass) {
        ONVIFRequest request = new ONVIFRequest(ip, Integer.parseInt(port), id, pass);

        int deviceManagementAuthHeader = request.createDeviceManagementAuthHeader();
        if (deviceManagementAuthHeader == ONVIFRequest.ERROR_SOCKET_TIMEOUT) {
            return ERROR_TIMEOUT;
        } else if (deviceManagementAuthHeader == ONVIFRequest.ERROR_IOEXCEPTION) {
            return ERROR_IO_EXCEPTION;
        }

        int getCapabilities = request.getCapabilities();
        if (getCapabilities == ONVIFRequest.SUCCESS) {
            request.createMediaManagementAuthHeader();
            request.getProfiles();
            request.getAllStreamUri();
            Log.v(TAG, "TAG!!!====");
            Log.v(TAG, "request.mGetStreamUriResponses.get(0).mMediaUri.mUri: " + request.mGetStreamUriResponses.get(0).mMediaUri.mUri);
            return request.mGetStreamUriResponses.get(0).mMediaUri.mUri;
        } else {
            return "Error";
        }

    }

    private boolean checkCompareChange(ItemDoorDevice itemDevice) {
        final boolean[] val = new boolean[1];
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_yes_or_no);

        final TextView title = dialog.findViewById(R.id.dg_yn_title);
        final TextView content = dialog.findViewById(R.id.dg_yn_content);
        final Button yes = dialog.findViewById(R.id.dg_yn_yes);
        final Button no = dialog.findViewById(R.id.dg_yn_no);

        title.setText("IP 중복 설정 확인");
        content.setText("네트워크에 동일한 IP 주소를 가진 장비가 존재합니다.\n변경하시겠습니까?");

        yes.setOnClickListener(view -> {
            val[0] = true;
            doorIpChangeDialog(itemDevice);
            dialog.dismiss();
        });

        no.setOnClickListener(view -> {
            val[0] = false;
            dialog.dismiss();
            jToastShow.createToast(context, "확인", "네트워크 정보 변경을 취소했습니다.", jToastShow.TOAST_WARNING, 80, jToastShow.SHORT_DURATION, null);
        });

        dialog.show();
        return val[0];
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (getCurrentFocus() != null) {
            assert imm != null;
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return true;
    }


    private void selectDevice(ItemDoorDevice itemDevConfig) {
        if(itemDevConfig != null) {
            selectConfig = itemDevConfig;
        } else {
            selectConfig = null;
        }
//        txtModel.setText(itemDevConfig.getDoorModel());
//        if (!checkModelName(itemDevConfig.getModel())) {
//            txtHttp.setText(String.valueOf(Integer.parseInt(itemDevConfig.getHttp().substring(2, 4) + itemDevConfig.getHttp().substring(0, 2), 16)));
//            txtHttps.setText(String.valueOf(Integer.parseInt(itemDevConfig.getHttps().substring(2, 4) + itemDevConfig.getHttps().substring(0, 2), 16)));
//
//        } else {
//
//            txtHttp.setText(itemDevConfig.getHttp());
//            txtHttps.setText(itemDevConfig.getHttps());
//        }
//        txtMac.setText(itemDevConfig.getMac());
//        txtVersion.setText(itemDevConfig.getVersion());
//        editIp.setText(itemDevConfig.getIp());
//        editMask.setText(itemDevConfig.getMask());
//        editGateway.setText(itemDevConfig.getGate());

    }

    private boolean checkModelName(String model) {
        return model.equals(ModelList.DCS01C) || model.equals(ModelList.DCS01E) || model.equals(ModelList.DCS01N);
    }


    private boolean checkDuplicate(ArrayList<ItemDoorDevice> list, ItemDoorDevice itemDevConfig) {
        String selectDeviceIp = itemDevConfig.getdIp();
        String selectDeviceMac = itemDevConfig.getdMac();

        for (int i = 0; i < list.size(); i++) {
            Log.d(TAG, list.get(i).getdModel() + " , " + list.get(i).getdIp());
            if (!selectDeviceMac.equals(list.get(i).getdMac())) {
                if (selectDeviceIp.equals(list.get(i).getdIp())) {
                    errorDuplicate();
                    return false;
                }
            }
        }

        return true;
    }

    private void errorDuplicate() {
        jToastShow.createToast(context,
                "경고",
                "검색된 장비 목록에 중복된 IP 주소가 존재합니다. IP 주소를 변경해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);

    }

    private String useFulString(String str) {
        return str.replaceAll("\\p{Cntrl}", "");
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

//    private void initIpFormat() {
//        InputFilter[] filters = new InputFilter[1];
//        filters[0] = (source, start, end, dest, dstart, dend) -> {
//            if (end > start) {
//                String destTxt = dest.toString();
//                String resultingTxt = destTxt.substring(0, dstart) +
//                        source.subSequence(start, end) + destTxt.substring(dend);
//                if (!resultingTxt.matches("^\\d{1,3}(\\." + "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
//                    return "";
//                } else {
//                    String[] splits = resultingTxt.split("\\.");
//                    for (String split : splits) {
//                        if (Integer.parseInt(split) > 255) {
//                            return "";
//                        }
//                    }
//                }
//            }
//            return null;
//        };
//        editIp.setFilters(filters);
//        editMask.setFilters(filters);
//        editGateway.setFilters(filters);
//
//    }

    private String transPort(String val) {
        if (val.length() != 3) {
            return val;
        }
        String str1 = val.substring(0, 2);
        String str2 = val.substring(2, 4);
        return String.valueOf(Integer.parseInt((str2 + str1), 16));
    }

    private void TaskEDALIpChange(EDALIpChangeTask asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class EDALIpChangeTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        private String mac, newIp, newSubMask, newGateWay;
        private int result;
        private DatagramSocket socket;
        private boolean isEdal = false;
        InetAddress serverAddr;

        public EDALIpChangeTask(String mac, String newIp, String newGateWay, String newSubMask) {
            this.mac = mac;
            this.newIp = newIp;
            this.newGateWay = newGateWay;
            this.newSubMask = newSubMask;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("IP 주소 변경 중...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            isEdal = true;

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... values) {
            try {
                socket = new DatagramSocket();
                serverAddr = InetAddress.getByName(IPUtilsCommand.IP);
                socket.setBroadcast(true);

                byte[] JiGuKey = new byte[4];
                JiGuKey[0] = 0;
                byte[] buffer = JIGU_Protocol.Make_SET_IP_Packet(mac, newIp, newGateWay, newSubMask);
                byte[] send_buf = JIGU_Protocol.JiGu_Encode(buffer, buffer.length, JiGuKey);
                DatagramPacket packet = new DatagramPacket(send_buf, send_buf.length, serverAddr, JIGU_Protocol.SEND_PORTNUM);
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }

            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket(JIGU_Protocol.RECEIVE_PORTNUM);

                while (isEdal) {
                    byte[] receiveBuffer = new byte[512];
                    byte[] JiGuKey = new byte[4];
                    JiGuKey[0] = 0;

                    DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    datagramSocket.receive(packet);
                    int data_length = packet.getLength();
                    byte[] result_buffer = new byte[data_length];

                    packet.setData(receiveBuffer, 0, data_length);
                    byte error = JIGU_Protocol.JiGu_Decode(receiveBuffer, data_length, result_buffer, JiGuKey);
                    String checkMode = String.format("%02x", result_buffer[1]);
                    Log.d(TAG, "CheckMode: " + checkMode + " Error: " + error);
                    switch (error) {
                        case JIGU_Protocol.JiGu_Error_OK:
                            if (checkMode.equals("63")) {

                                try {
                                    byte[] buffer = JIGU_Protocol.Make_Set_Reset(mac);
                                    byte[] send_buf = JIGU_Protocol.JiGu_Encode(buffer, buffer.length, JiGuKey);
                                    DatagramPacket resetPacket = new DatagramPacket(send_buf, send_buf.length, serverAddr, JIGU_Protocol.SEND_PORTNUM);
                                    socket.send(resetPacket);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                publishProgress();
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                isEdal = false;

                                break;
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
                if (datagramSocket != null) {
                    try {
                        Log.w(TAG, "Socket Close!");
                        isEdal = false;
                        datagramSocket.close();
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
            progressDialog.setMessage("재부팅 중입니다...");


        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressDialog.dismiss();
            hideSoftKeyboard();
            refreshChangeData();
        }


    }

    private void TaskEDALGetInfo(EDALGetInfoTask asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class EDALGetInfoTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        private String mac, newIp, newSubMask, newGateWay;
        private int result;
        private DatagramSocket socket;
        private boolean isEdal = false;
        InetAddress serverAddr;

        public EDALGetInfoTask(String mac) {
            this.mac = mac;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("정보 가져오는 중...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            isEdal = true;

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... values) {
            new Thread(new SendEMTest(AppUtils.EDAL_MODE_INFO)).start();

            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket(JIGU_Protocol.RECEIVE_PORTNUM);

                while (isEdal) {
                    byte[] receiveBuffer = new byte[512];
                    byte[] JiGuKey = new byte[4];
                    JiGuKey[0] = 0;

                    DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    datagramSocket.receive(packet);
                    int data_length = packet.getLength();
                    byte[] result_buffer = new byte[data_length];

                    packet.setData(receiveBuffer, 0, data_length);
                    byte error = JIGU_Protocol.JiGu_Decode(receiveBuffer, data_length, result_buffer, JiGuKey);
                    String checkMode = String.format("%02x", result_buffer[1]);
                    String checkMac = JIGU_Library.byteArrayToMacAddress(result_buffer, 2, 7).trim();
                    Log.d(TAG, "CheckMode: " + checkMode + " checkMac: " + checkMac);
                    switch (error) {
                        case JIGU_Protocol.JiGu_Error_OK:
                            if (checkMode.equals("44") && mac.equals(checkMac)) {
                                Device_DM_Protocol protocol = new Device_DM_Protocol();
                                Device_EDAL edal = new Device_EDAL();
                                getEDALCamIp(protocol.process_EDAL_Data(result_buffer, edal));

                                publishProgress();
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e(TAG, "Error --- Version: invalid Version");
                            }
                            isEdal = false;
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
                if (datagramSocket != null) {
                    try {
                        Log.w(TAG, "Socket Close!");
                        isEdal = false;
                        datagramSocket.close();
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
            progressDialog.setMessage("잠시만 기다려주세요...");

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }


    }

    private void getEDALCamIp(String ip) {
        this.edalCamIp = ip;
    }

    private boolean isEDALModel(String model) {
        return ModelList.DCS01C.equals(model) || ModelList.DCS01E.equals(model) || ModelList.DCS01N.equals(model);
    }

    private void doorResetDialog(ItemDoorDevice itemDoorDevice) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_door_reset);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final TextView doorIpAddress = dialog.findViewById(R.id.dgTxtDoorIp);
        final TextView doorCamAddress = dialog.findViewById(R.id.dgTxtDoorCamIp);

        final Button btnDoorRstApply = dialog.findViewById(R.id.dgBtnDoorRstApply);
        final Button btnDoorRstClose = dialog.findViewById(R.id.dgBtnDoorRstClose);

        doorIpAddress.setText(itemDoorDevice.getdIp());
        doorCamAddress.setText(itemDoorDevice.getcIp());

        btnDoorRstApply.setOnClickListener(view -> {
            TaskDoorDefault(new DoorDefaultTask(itemDoorDevice));
//            new Thread(new SendDoorPacket(AppUtils.EDAL_MODE_DEFAULT, itemDoorDevice)).start();
            dialog.dismiss();

        });

        btnDoorRstClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private void TaskDoorDefault(DoorDefaultTask asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class DoorDefaultTask extends AsyncTask<Void, Boolean, Void> implements OnSelectDoorDevice {
        private ProgressDialog progressDialog = new ProgressDialog(getContext());
        private String duplicateLocation;
        private DatagramSocket emSocket;
        private String strMac, strIp, strGw, strNm;
        private ItemDoorDevice itemDoorDevice;
        private boolean isEms = false;

        public DoorDefaultTask(ItemDoorDevice itemDoorDevice) {
            this.itemDoorDevice = itemDoorDevice;

        }

        @Override
        protected void onPreExecute() {
            list = new ArrayList<>();
            adapter = new AdapterDialogDoorDiscovery(getContext(), list, this);
            listDoor.setAdapter(adapter);

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            isEms = true;
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            new Thread(new SendDoorPacket(AppUtils.EDAL_MODE_DEFAULT, itemDoorDevice)).start();

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

//                    System.out.println(DeviceManagement_Library.byteArrayToHexString(receiveBuffer, 0, data_length));
                    String DMP = String.valueOf(result_buffer[1]);
                    String Mac = JIGU_Library.byteArrayToMacAddress(result_buffer, 2, 7).trim();

                    Log.d(TAG, "DMP: " + DMP + " Mac: " + Mac);
                    switch (error) {
                        case JIGU_Protocol.JiGu_Error_OK:
                            if (DMP.equals(String.valueOf(JIGU_Protocol.SET_FACTORY_RESET_RESPONE)) && Mac.equals(itemDoorDevice.getdMac())) {
                                publishProgress(true);
                            } else if (DMP.equals(String.valueOf(JIGU_Protocol.SET_RESET_RESPONE))) {
                                publishProgress(false);
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
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);
            if (values[0]) {
                jToastShow.createToast(context, "성공", "공장 초기화를 진행중입니다. 잠시 후 검색 버튼을 눌러주세요.", jToastShow.TOAST_SUCCESS, 80, jToastShow.LONG_DURATION, null);
            } else {
                jToastShow.createToast(context, "실패", "공장 초기화를 실패했습니다. 네트워크 상태를 확인해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            progressDialog.dismiss();
            if (dialogChange.isShowing()) {
                dialogChange.dismiss();
            }
            super.onPostExecute(aVoid);
        }



        @Override
        public void onSelectDoorList(ItemDoorDevice itemDevConfig) {
        }


        @Override
        public void onRegisterDoorList(ItemDoorDevice itemDevConfig) {
        }

    }

    private void camIpChangeDialog(ItemDoorDevice itemDoorDevice) {
        dialogCamIpChange = new Dialog(context);
        dialogCamIpChange.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogCamIpChange.setContentView(R.layout.dialog_door_cam_change);
        dialogCamIpChange.setCancelable(false);
        dialogCamIpChange.setCanceledOnTouchOutside(false);

        final JToastShow jToastShow = new JToastShow();
//        final TextView title = dialogCamIpChange.findViewById(R.id.dialog_title);
        final TextInputEditText editId = dialogCamIpChange.findViewById(R.id.dgEditDoorCamId);
        final TextInputEditText editPass = dialogCamIpChange.findViewById(R.id.dgEditDoorCamPass);
        final TextInputLayout layoutPass = dialogCamIpChange.findViewById(R.id.dgLayIpPass);
        final Button btnChange = dialogCamIpChange.findViewById(R.id.dgDoorCamChange);
        final Button btnClose = dialogCamIpChange.findViewById(R.id.dgDoorCamClose);
        final EditText editIp = dialogCamIpChange.findViewById(R.id.dgEditDoorCamIp);
        final EditText editNetMask = dialogCamIpChange.findViewById(R.id.dgEditDoorCamNetMask);
        final EditText editGateWay = dialogCamIpChange.findViewById(R.id.dgEditDoorCamGateWay);
        final EditText editHttp = dialogCamIpChange.findViewById(R.id.dgEditDoorCamHttp);
        final EditText editHttps = dialogCamIpChange.findViewById(R.id.dgEditDoorCamHttps);

        InputFilter[] filters = new InputFilter[1];
        filters[0] = (source, start, end, dest, dstart, dend) -> {
            if (end > start) {
                String destTxt = dest.toString();
                String resultingTxt = destTxt.substring(0, dstart) +
                        source.subSequence(start, end) + destTxt.substring(dend);
                if (!resultingTxt.matches("^\\d{1,3}(\\." + "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                    return "";
                } else {
                    String[] splits = resultingTxt.split("\\.");
                    for (String split : splits) {
                        if (Integer.parseInt(split) > 255) {
                            return "";
                        }
                    }
                }
            }
            return null;
        };
        editIp.setFilters(filters);
        editNetMask.setFilters(filters);
        editGateWay.setFilters(filters);

        editId.setText("root");

        editIp.setText(itemDoorDevice.getcIp());
        editNetMask.setText(itemDoorDevice.getcNetMask());
        editGateWay.setText(itemDoorDevice.getcGateWay());

        editHttp.setText("80");
        editHttps.setText("443");

        layoutPass.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

        btnChange.setOnClickListener(view -> {
            hideSoftKeyboard();

            if (Objects.requireNonNull(editId.getText()).toString().isEmpty()) {
                jToastShow.createToast(context, "입력 오류", "아이디가 입력되지 않았습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                return;
            }

            if (Objects.requireNonNull(editPass.getText()).toString().isEmpty()) {
                jToastShow.createToast(context, "입력 오류", "비밀번호가 입력되지 않았습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                return;
            }

            try {
                String str = IPUtilsCommand.changeNetwork(false,
                        Objects.requireNonNull(editId.getText()).toString(),
                        Objects.requireNonNull(editPass.getText()).toString(),
                        itemDoorDevice.getcMac(), "00",
                        Objects.requireNonNull(editIp.getText()).toString(),
                        Objects.requireNonNull(editNetMask.getText()).toString(),
                        Objects.requireNonNull(editGateWay.getText()).toString(),
                        editHttp.getText().toString(), editHttps.getText().toString());

                Log.d(TAG, "Packet: " + str);

                String newVal = editIp.getText().toString() + "_@#@_" +
                        editNetMask.getText().toString() + "_@#@_" +
                        editGateWay.getText().toString();

                isRunning = true;
                TaskCamIpChange(new camIpChangeTask(str, newVal, itemDoorDevice.getcMac()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnClose.setOnClickListener(view -> {
            dialogCamIpChange.dismiss();
        });
        dialogCamIpChange.show();
    }


    private class camIpChangeTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        private final byte[] byteStr;
        private final String newVal;
        private String newIp, newSubMask, newGateWay;
        private int result;
        private DatagramSocket socket;
        private final String cMac;

        public camIpChangeTask(String str, String newVal, String cMac) {
            this.newVal = newVal;
            this.byteStr = IPUtilsCommand.hexStringToByteArray(str);
            this.cMac = cMac;
        }

        @Override
        protected void onPreExecute() {
            String[] strVal = newVal.split("_@#@_");
            newIp = strVal[0];
            newSubMask = strVal[1];
            newGateWay = strVal[2];

            list = new ArrayList<>();
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("변경 중...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            result = 2;
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... values) {

            new Thread(new SendEmPacket(byteStr)).start();


            try {
                socket = new DatagramSocket(IPUtilsCommand.PORT);
                socket.setBroadcast(true);
                socket.setSoTimeout(3000);
                InetAddress address = InetAddress.getByName(IPUtilsCommand.IP);

                while (isRunning) {
                    DatagramPacket packet = new DatagramPacket(byteStr, byteStr.length, address, IPUtilsCommand.PORT);
                    socket.receive(packet);
                    if (!packet.getAddress().toString().equals("/" + tinyDB.getString(KeyList.DEVICE_CUR_IP))) {
                        byte[] compareByte = Arrays.copyOfRange(packet.getData(), 0, 80);
                        String allData = new String(compareByte);
                        Log.d(TAG, "All Data: " + allData);
                        Log.d(TAG, "selectConfig.getMac(): " + cMac);
                        if (allData.contains(cMac)) {
                            if (allData.contains(newIp) && allData.contains(newGateWay)) {
                                result = 1;
                            } else {
                                result = 2;
                            }
                        } else {
                            result = 3;
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "TimeException!");
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
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            hideSoftKeyboard();

            switch (result) {
                case 1:
                    dialogCamIpChange.dismiss();
                    jToastShow.createToast(context,
                            "네트워크 정보 변경을 완료했습니다.",
                            "변경 시 네트워크 상황에 따라 일정 시간이 소요되니 다시 검색해주세요.", jToastShow.TOAST_SUCCESS, 80, jToastShow.SHORT_DURATION, null);
                    break;
                case 2:
                    jToastShow.createToast(context,
                            "네트워크 정보 변경 실패",
                            "아이디와 비밀번호를 다시 확인해주세요. ", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                    break;
                case 3:
                    jToastShow.createToast(context,
                            "MAC 주소 불일치",
                            "아이디와 비밀번호를 다시 확인해주세요. ", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                    break;
            }
            super.onPostExecute(aVoid);
        }


    }
}
