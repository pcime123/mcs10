package com.sscctv.seeeyesonvif.Settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.databinding.FragmentSettingsRangeBinding;
import com.sscctv.seeeyesonvif.Utils.JToastShow;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;


public class SetRangeFragment extends Fragment {

    private static final String TAG = "MainActivity";
    private InputMethodManager imm;
    private FragmentSettingsRangeBinding mBinding;

    private ActivityMain activity;
    private ArrayList<ItemRange> rangeList;
    private AdapterRangeList listAdapter;
    private boolean isRunning;
    private String pingTime;
    private JToastShow jToastShow;

    static SetRangeFragment newInstance() {
        return new SetRangeFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        activity = (ActivityMain) getActivity();
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

        TaskOnListUpdate(new OnListUpdate());
//        initRangeList();
    }

    @SuppressLint({"ClickableViewAccessibility", "WrongConstant"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings_range, container, false);
        imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
        jToastShow = new JToastShow();

        mBinding.getRoot().setOnTouchListener((view, motionEvent) -> {
            if (activity.getCurrentFocus() != null) {
                assert imm != null;
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
            return true;
        });

        mBinding.btnAdb.setOnClickListener(view -> {
            pingTime = mBinding.editRangeTime.getText().toString();
            if (pingTime.equals("0") || pingTime.isEmpty()) {
                jToastShow.createToast(getContext(), "실패", "대기 시간 설정 값을 확인해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                return;
            }
            TaskOnSearch(new OnSearch());
        });
        GridLayoutManager manager = new GridLayoutManager(getContext(), 15);
        mBinding.listDevice.setLayoutManager(manager);
        mBinding.listDevice.setHasFixedSize(true);

        return mBinding.getRoot();
    }

    private void initRangeList() {
        GridLayoutManager manager = new GridLayoutManager(getContext(), 15);
        mBinding.listDevice.setLayoutManager(manager);

        rangeList = new ArrayList<>();
        for (int i = 1; i < 255; i++) {
            rangeList.add(new ItemRange(i, ""));
        }
        Log.d(TAG, "RangeList Size: " + rangeList.size() + " ," + rangeList.get(0).getNum());
        listAdapter = new AdapterRangeList(getContext(), rangeList);
        mBinding.listDevice.setAdapter(listAdapter);
    }

    private void TaskOnListUpdate(OnListUpdate asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class OnListUpdate extends AsyncTask<Void, Integer, String> {
        private Dialog progressDialog;
        private Handler handler = new Handler();
        private boolean isRunning;
        private Button progressStop;

        @Override
        protected void onPreExecute() {
            isRunning = true;
            rangeList = new ArrayList<>();
            progressDialog = new Dialog(Objects.requireNonNull(getContext()), R.style.progress_dialog);
            progressDialog.setContentView(R.layout.dialog_progress);
            progressDialog.setCancelable(true);
            Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            TextView msg = progressDialog.findViewById(R.id.id_tv_ip);
            msg.setVisibility(View.GONE);
            TextView loading = progressDialog.findViewById(R.id.id_tv_loadingmsg);
            loading.setText("Loading");
            progressStop = progressDialog.findViewById(R.id.id_tv_btn);
            progressStop.setVisibility(View.GONE);
            progressStop.setOnClickListener(view -> {
                isRunning = false;
                progressDialog.dismiss();
            });
            progressDialog.show();

            super.onPreExecute();
        }

        protected String doInBackground(Void... params) {

            for (int i = 1; i < 255; i++) {
                rangeList.add(new ItemRange(i, ""));
//                Log.d(TAG, "send Value: " + i);

//                publishProgress(i);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.d(TAG, "Get Value: " + values[0]);
//            listAdapter.onUpdate(values[0]);
//            progressIp.setText(String.format(Locale.getDefault(),"%s%d", ipAddress, values[0] + 1));
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String s) {
            listAdapter = new AdapterRangeList(getContext(), rangeList);
            mBinding.listDevice.setAdapter(listAdapter);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            super.onPostExecute(s);
        }

    }

    private void TaskOnSearch(OnSearch asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @SuppressLint("StaticFieldLeak")
    private class OnSearch extends AsyncTask<Void, Integer, String> {
        private TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
        private String ipAddress;
        private Dialog progressDialog;
        private TextView progressText, progressIp;
        private Button progressStop;
        private Handler handler = new Handler();

        @Override
        protected void onPreExecute() {
            isRunning = true;

            ipAddress = tinyDB.getString(KeyList.DEVICE_CUR_IP);
            String[] splits = ipAddress.split("\\.");
            ipAddress = splits[0] + "." + splits[1] + "." + splits[2] + ".";

            progressDialog = new Dialog(Objects.requireNonNull(getContext()), R.style.progress_dialog);
            progressDialog.setContentView(R.layout.dialog_progress);
            progressDialog.setCancelable(true);
            Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
            progressText = progressDialog.findViewById(R.id.id_tv_loadingmsg);
            progressIp = progressDialog.findViewById(R.id.id_tv_ip);
            progressStop = progressDialog.findViewById(R.id.id_tv_btn);

            progressText.setText(R.string.searching);
            progressIp.setText("Wait");
            progressStop.setOnClickListener(view -> {
                isRunning = false;
                progressDialog.dismiss();
            });
            progressDialog.show();
            super.onPreExecute();
        }

        protected String doInBackground(Void... params) {
            for (int i = 1; i < rangeList.size(); i++) {
                if (!isRunning) {
                    break;
                }

                String sIP = ipAddress + i;

                if (pingTest(sIP)) {
                    listAdapter.onStatus(i - 1);
                    Log.d(TAG, "Ping Good: " + sIP);
                } else {
                    listAdapter.offStatus(i - 1);
                    Log.e(TAG, "Ping BAD: " + sIP);
                }
                publishProgress(i - 1);

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            listAdapter.onUpdate(values[0]);
            progressIp.setText(String.format(Locale.getDefault(), "%s%d", ipAddress, values[0] + 1));
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String s) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            super.onPostExecute(s);
        }

    }

    private boolean pingTest(String ip) {
        Runtime runtime = Runtime.getRuntime();

        String cmd = "ping -c 1 -W " + pingTime + " " + ip;

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

    public static long getLongFromIPv4(String sIPAddress) {
        long var1 = 0L;

        try {
            byte[] var4;
            if ((var4 = InetAddress.getByName(sIPAddress).getAddress()).length == 4) {
                ByteBuffer var5;
                (var5 = ByteBuffer.allocate(8)).put(new byte[]{0, 0, 0, 0});
                var5.put(var4);
                var5.position(0);
                var1 = var5.getLong();
            } else {
                Log.d(TAG, "Malformed IP address: " + sIPAddress);
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return var1;
    }


}
