package com.sscctv.seeeyesonvif.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Adapter.AdapterMainDeviceList;
import com.sscctv.seeeyesonvif.Interfaces.OnSelectMainDevice;
import com.sscctv.seeeyesonvif.Items.ItemMainDevice;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Server.SendDeviceSearch;
import com.sscctv.seeeyesonvif.Service.MainCallService;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.CommandExecutor;
import com.sscctv.seeeyesonvif.Utils.IPUtilsCommand;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.databinding.FragmentNormalCallBinding;
import com.sscctv.seeeyesonvif.databinding.FragmentNormalCallListBinding;

import org.linphone.core.AccountCreator;
import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;

public class FragmentNormalCallList extends Fragment implements OnSelectMainDevice {

    private static final String TAG = FragmentNormalCallList.class.getSimpleName();
    private FragmentNormalCallListBinding mBinding;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());

    private String deviceName, ipAddress;
    private int port;
    private InputMethodManager imm;
    private Timer timerUpdateTime;
    private Timer alarmTime;
    private boolean isDotChange = false;
    private Handler mHandler;
    private Core core;
    private ArrayList<ItemMainDevice> devList;
    private AdapterMainDeviceList listAdapter;
    private boolean isRunning;

    public static FragmentNormalCallList newInstance() {
        FragmentNormalCallList fragment = new FragmentNormalCallList();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        getDeviceList();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        isRunning = false;
        AppUtils.putMainDeviceList(tinyDB, KeyList.LIST_MAIN_DEVICE, devList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_normal_call_list, container, false);
        imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);

        Bundle bundle = this.getArguments();

        if (bundle != null) {

        }
        core = MainCallService.getCore();

        ((SimpleItemAnimator) mBinding.listDevice.getItemAnimator()).setSupportsChangeAnimations(false);
        mBinding.btnMainRefresh.setOnClickListener(view -> {
            isRunning = false;

            tinyDB.remove(KeyList.LIST_MAIN_DEVICE);
            new Thread(new SendDeviceSearch(IPUtilsCommand.sendMainDeviceDiscovery)).start();

            new Handler().postDelayed(this::getDeviceList, 1000);
        });

        mBinding.btnDeviceGroup.setOnClickListener(view -> {
            Log.d(TAG, "proxy: " + core.getDefaultProxyConfig());
            Log.d(TAG, "getDomain: " + core.getDefaultProxyConfig().getDomain());
            Log.d(TAG, "getUserData: " + core.getDefaultProxyConfig().getUserData());
        });

        mBinding.btnDeviceModify.setOnClickListener(view -> {

        });
        return mBinding.getRoot();
    }

    public void getDeviceList() {
        GridLayoutManager manager;
        final int spanCount = tinyDB.getInt(KeyList.LIST_SPAN_COUNT);
        if (spanCount == 0) {
            manager = new GridLayoutManager(getContext(), 4);
        } else {
            manager = new GridLayoutManager(getContext(), spanCount);
        }

        mBinding.listDevice.setLayoutManager(manager);
        devList = AppUtils.getMainDeviceList(tinyDB, KeyList.LIST_MAIN_DEVICE);
        for (int i = 0; i < devList.size(); i++) {
            ItemMainDevice items = devList.get(i);
                Log.d(TAG, "Add Mac: " + items.getmMac() + " IP: " + items.getmIp() + " Name: " + items.getmName() + " Model: " + items.getmModel());

        }
        listAdapter = new AdapterMainDeviceList(getContext(), devList, this);
        mBinding.listDevice.setAdapter(listAdapter);
        isRunning = true;

        TaskRateCheck(new RateCheckTask());
    }


    public void TaskRateCheck(RateCheckTask asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class RateCheckTask extends AsyncTask<Void, Integer, String> {
        private Dialog progressDialog;

        private int timeDelay;
        private Button progressStop;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (devList.size() != 0) {
                timeDelay = 1000 / devList.size();
            } else {
                isRunning = false;
            }
            progressDialog = new Dialog(Objects.requireNonNull(getContext()), R.style.progress_dialog);
            progressDialog.setContentView(R.layout.dialog_progress);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            TextView msg = progressDialog.findViewById(R.id.id_tv_ip);
            msg.setVisibility(View.GONE);
            TextView loading = progressDialog.findViewById(R.id.id_tv_loadingmsg);
            loading.setText("Loading..");
            progressStop = progressDialog.findViewById(R.id.id_tv_btn);
            progressStop.setVisibility(View.GONE);
            progressDialog.show();


        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground Start");
            while (isRunning) {
                for (int i = 0; i < devList.size(); i++) {
                    String res = getPingTime(devList.get(i).getmIp());
                    if (!isRunning || devList.size() == 0) {
                        break;
                    }
                    if (!res.equals("0")) {
                        devList.get(i).setRate(res);
                    } else {
                        devList.get(i).setRate("0");
                    }
                    publishProgress(i);

                }
                SystemClock.sleep(timeDelay);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            listAdapter.notifyItemChanged(values[0]);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        private String getPingTime(String ip) {
            String result = CommandExecutor.execSingleCommand("ping -c 1 -W 2 " + ip);

            if (result.contains("time=")) {
                int indexing = result.indexOf("time=") + 5;
                return result.substring(indexing, indexing + 4) + " ms";
            } else {
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute End");
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private static void inviteAddress(Context context, Address address) {
        Core core = MainCallService.getCore();
        CallParams params = core.createCallParams(null);
        if (address != null) {
            core.inviteAddressWithParams(address, params);
        }
    }

    public static void newOutgoingCall(Context context, String to) {
        Core core = MainCallService.getCore();
        Log.d("Call", "To: " + to + " Core: " + core);
        if (to == null) return;
        Address address = core.interpretUrl(to);
//        Address address = core.interpretUrl("1004");
        inviteAddress(context, address);
    }


    @Override
    public void onSelectList(ItemMainDevice itemMainDevice) {
        String sipName = "sip:" + itemMainDevice.getmIp();
        newOutgoingCall(getContext(), sipName);
        tinyDB.putString(KeyList.CALL_CURRENT_DEVICE, itemMainDevice.getmMac());
        Log.d(TAG, "SelectDevice: " + tinyDB.getString(KeyList.CALL_CURRENT_DEVICE));
    }
}
