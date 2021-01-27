package com.sscctv.seeeyesonvif.Fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sscctv.seeeyesonvif.Adapter.AdapterEmCall;
import com.sscctv.seeeyesonvif.Items.ItemEmCallLog;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.databinding.TabListEmBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class TabCallLogEmergency extends Fragment {

    private static final String TAG = TabCallLogEmergency.class.getSimpleName();
    private TinyDB tinyDB;
    private ArrayList<ItemEmCallLog> emLogItems;
    private AdapterEmCall adapterEmCall;
    private TabListEmBinding mBinding;
    boolean getMode = false;
    private Bundle bundle;

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");


        TaskGetList(new getListTask());

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.tab_list_em, container, false);
        tinyDB = new TinyDB(getContext());

        bundle = this.getArguments();
        if (bundle != null) {
            getMode = bundle.getBoolean("missed");
        }

        mBinding.tabListAll.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mBinding.tabListAll.setLayoutManager(manager);

        return mBinding.getRoot();
    }

    private void TaskGetList(AsyncTask<Void, Void, Void> asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @SuppressLint("StaticFieldLeak")
    private class getListTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        Calendar logTime;
        SimpleDateFormat dateFormat;

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "getListTask onPreExecute");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getResources().getString(R.string.please_wait));
            progressDialog.show();
            emLogItems = AppUtils.getCallLog(tinyDB, KeyList.LOG_CALL_EM);
            Collections.reverse(emLogItems);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            int delSize;
            Log.d(TAG, "Item Size: " + emLogItems.size() + " Del Size: " + tinyDB.getInt(KeyList.CALL_LOG_MAX));

            if (emLogItems.size() != 0) {
                Collections.sort(emLogItems, (t1, t2) -> {
                    String in1 = String.valueOf(t1.getCallNum());
                    String in2 = String.valueOf(t2.getCallNum());
                    return in2.compareTo(in1);
                });

                if (getMode) {
                    Collections.sort(emLogItems, (t1, t2) -> {
                        String in1 = String.valueOf(t1.isCallCheck());
                        String in2 = String.valueOf(t2.isCallCheck());
                        return in2.compareTo(in1);
                    });
                }
//                else {
//                    Collections.sort(emLogItems, (t1, t2) -> {
//                        String in1 = String.valueOf(t1.getCallNum());
//                        String in2 = String.valueOf(t2.getCallNum());
//                        return in2.compareTo(in1);
//                    });
//                }


                if (emLogItems.size() > tinyDB.getInt(KeyList.CALL_LOG_MAX)) {
                    delSize = emLogItems.size() - tinyDB.getInt(KeyList.CALL_LOG_MAX);

                    for (int i = 0; i < delSize; i++) {
                        emLogItems.remove((emLogItems.size() - 1) - i);
                        delSize--;
                        i--;
                    }
                }

//                NurseCallUtils.putEmLog(tinyDB, KeyList.CALL_LOG_EMERGENCY, emLogItems);
                AppUtils.putCallLog(tinyDB, KeyList.LOG_CALL_EM, emLogItems);
                adapterEmCall = new AdapterEmCall(emLogItems);
                mBinding.tabListAll.setAdapter(adapterEmCall);
            }

            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }


}
