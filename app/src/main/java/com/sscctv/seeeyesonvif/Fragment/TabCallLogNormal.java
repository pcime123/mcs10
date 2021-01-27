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


import com.sscctv.seeeyesonvif.Items.ItemEmCallLog;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.databinding.TabListSipBinding;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;

public class TabCallLogNormal extends Fragment {

    private static final String TAG = TabCallLogNormal.class.getSimpleName();
    private TinyDB tinyDB;
    private TabListSipBinding mBinding;
    private ArrayList<ItemEmCallLog> callLogItems;
    private int listSize;

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");

//        callLogItems = NurseCallUtils.getCallLog(tinyDB, KeyList.CALL_LOG_NORMAL);
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.tab_list_sip, container, false);
        tinyDB = new TinyDB(getContext());

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
            logTime = Calendar.getInstance();
            dateFormat = new SimpleDateFormat("yyyy-MM-dd a hh:mm:ss", Locale.getDefault());
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            int delSize;






            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String secondsToDisplayableString(int secs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, 0, 0, secs);
        return dateFormat.format(cal.getTime());
    }

    class Descending implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }

    }

}
