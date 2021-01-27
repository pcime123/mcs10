package com.sscctv.seeeyesonvif.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Adapter.AdapterDeviceList;
import com.sscctv.seeeyesonvif.Adapter.AdapterEditGroupList;
import com.sscctv.seeeyesonvif.Adapter.AdapterGroupList;
import com.sscctv.seeeyesonvif.Adapter.AdapterModifyList;
import com.sscctv.seeeyesonvif.Activity.DialogDoorDiscovery;
import com.sscctv.seeeyesonvif.Activity.DialogEmDiscovery;
import com.sscctv.seeeyesonvif.Items.IPDevice;
import com.sscctv.seeeyesonvif.Interfaces.EditGroupItemTouchCallback;
import com.sscctv.seeeyesonvif.Interfaces.OnSelectEmDevice;
import com.sscctv.seeeyesonvif.Interfaces.OnSelectGroup;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.Items.ItemGroup;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.CommandExecutor;
import com.sscctv.seeeyesonvif.Utils.JToastShow;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.databinding.FragmentDeviceBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import static androidx.recyclerview.widget.LinearLayoutManager.VERTICAL;

public class FragmentDeviceView extends Fragment implements OnSelectEmDevice, OnSelectGroup, AdapterEditGroupList.OnStartDragListener {
    private static final String TAG = FragmentDeviceView.class.getSimpleName();
    private FragmentDeviceBinding mBinding;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private AdapterDeviceList listAdapter;
    private AdapterGroupList adapterGroupList;
    private AdapterEditGroupList adapterEditGroupList;
    private ItemTouchHelper itemTouchHelper;
    private ArrayList<ItemDevice> devList;
    private ArrayList<ItemGroup> groupList;
    private RequestQueue requestQueue;
    private boolean isRunning;
    private JToastShow jToastShow;

    public static FragmentDeviceView newInstance() {
        FragmentDeviceView fragment = new FragmentDeviceView();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        getDeviceList();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
        isRunning = false;
        AppUtils.putItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG, devList);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_device, container, false);
        if (requestQueue != null) {
            requestQueue = Volley.newRequestQueue(ActivityMain.getAppContext());
        }
        jToastShow = new JToastShow();


        Bundle bundle = this.getArguments();
        if (bundle != null) {

        }
        mBinding.btnEmSearch.setOnClickListener(view -> {
            isRunning = false;
            DialogEmDiscovery dialog = new DialogEmDiscovery(Objects.requireNonNull(getContext()), this::getDeviceList);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.show();
        });

        mBinding.btnDoorSearch.setOnClickListener(view -> {
            isRunning = false;
            DialogDoorDiscovery dialog = new DialogDoorDiscovery(Objects.requireNonNull(getContext()), this::getDeviceList);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.show();
        });

        mBinding.btnDeviceSort.setOnClickListener(view ->
        {
            if (devList.size() != 0) {
                dialogSort(getContext());
            }
        });
        mBinding.btnDeviceModify.setOnClickListener(view -> modifyDialog(getContext()));
        mBinding.btnRemoveList.setOnClickListener(view -> goNormalMode());
        mBinding.btnDeviceDone.setOnClickListener(view -> goRemoveMode(false));
        mBinding.btnDeviceRemove.setOnClickListener(view -> goRemoveMode(true));
        mBinding.listDevice.setHasFixedSize(true);
        Objects.requireNonNull(mBinding.listDevice.getItemAnimator()).setChangeDuration(0);

        mBinding.btnDeviceGroup.setOnClickListener(view -> editGroupDialog(getContext()));
        mBinding.btnAllDevice.setOnClickListener(view -> {
            adapterGroupList.clearSelectedItem();
            getDeviceList();
        });
        return mBinding.getRoot();
    }


    public void getDeviceList() {
//        Log.d(TAG, "getDeviceList()");
        GridLayoutManager manager;
        final int spanCount = tinyDB.getInt(KeyList.LIST_SPAN_COUNT);
        if (spanCount == 0) {
            manager = new GridLayoutManager(getContext(), 4);
        } else {
            manager = new GridLayoutManager(getContext(), spanCount);
        }
        mBinding.listDevice.setLayoutManager(manager);
        devList = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);
        for(int i = 0; i<devList.size(); i++) {
            Log.d(TAG, "Dev: " + devList.get(i).getRate());
        }
        getGroupList();

        listAdapter = new AdapterDeviceList(getContext(), devList, this);
        mBinding.listDevice.setAdapter(listAdapter);

        isRunning = true;
        TaskRateCheck(new RateCheckTask());
    }

    private void getGroupList() {
//        Log.d(TAG, "getGroupList()");
        mBinding.listDeviceGroup.setLayoutManager(new LinearLayoutManager(getContext(), VERTICAL, false));
        groupList = AppUtils.getGroupList(tinyDB, KeyList.LIST_GROUP_HEADER);
        if (groupList.size() == 0) {
            groupList.add(new ItemGroup(AppUtils.GROUP_HEADER, "Group 1", "", "", "", ""));
            AppUtils.putGroupList(tinyDB, KeyList.LIST_GROUP_HEADER, groupList);
        }
        adapterGroupList = new AdapterGroupList(getContext(), groupList, this);


        mBinding.titleSelect.setText("All Device");
        mBinding.txtAllCount.setText("(" + devList.size() + ")");
        mBinding.btnAllDevice.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.rectangle_stroke_green));
        mBinding.listDeviceGroup.setAdapter(adapterGroupList);
        adapterEditGroupList = new AdapterEditGroupList(getContext(), groupList, this::onStartDrag);

    }

    @Override
    public void groupSelect(int position) {
        ArrayList<ItemDevice> temp = new ArrayList<>();
        String strGroup = groupList.get(position).getGroup();
        mBinding.titleSelect.setText(strGroup);
        if (devList.size() == 0) {
            jToastShow.createToast(getContext(), "확인", "해당 그룹에 등록된 장비가 없습니다.", jToastShow.TOAST_WARNING, 80, jToastShow.SHORT_DURATION, null);
        } else {
            for (int i = 0; i < devList.size(); i++) {
                if (devList.get(i).getGroup().equals(strGroup)) {
                    temp.add(devList.get(i));
                }
            }
        }
        listAdapter = new AdapterDeviceList(getContext(), temp, this);
        mBinding.listDevice.setAdapter(listAdapter);

        mBinding.btnAllDevice.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.rectangle_stroke_gray));
    }

    private void setRegisteredNumber() {
//        mBinding.txtDeviceNum.setText(getRegisteredDevice());
    }

    private String getRegisteredDevice() {
        return String.valueOf(devList.size());
    }

    private void goNormalMode() {
        mBinding.btnDeviceGroup.setVisibility(View.GONE);
        mBinding.btnEmSearch.setVisibility(View.GONE);
        mBinding.btnDeviceRemove.setVisibility(View.VISIBLE);
        mBinding.btnDeviceDone.setVisibility(View.VISIBLE);
        mBinding.btnDeviceModify.setVisibility(View.GONE);
        mBinding.btnRemoveList.setVisibility(View.GONE);
        mBinding.btnDeviceSort.setVisibility(View.GONE);
        tinyDB.putBoolean(KeyList.KEY_REMOVE_LIST, true);
        listAdapter.notifyDataSetChanged();
        isRunning = false;
        setRegisteredNumber();
    }

    private void goRemoveMode(boolean val) {
        if (val) {
            int size = devList.size();

            for (int i = 0; i < size; i++) {
                ItemDevice item = devList.get(i);

                if (item.isSelected()) {
                    devList.remove(i);
                    size--;
                    i--;
                }
            }
            AppUtils.putItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG, devList);
            listAdapter.notifyDataSetChanged();
        }

        mBinding.btnDeviceRemove.setVisibility(View.GONE);
        mBinding.btnDeviceDone.setVisibility(View.GONE);
        mBinding.btnDeviceGroup.setVisibility(View.VISIBLE);
        mBinding.btnEmSearch.setVisibility(View.VISIBLE);
        mBinding.btnDeviceModify.setVisibility(View.VISIBLE);
        mBinding.btnRemoveList.setVisibility(View.VISIBLE);
        mBinding.btnDeviceSort.setVisibility(View.VISIBLE);
        tinyDB.putBoolean(KeyList.KEY_REMOVE_LIST, false);

        isRunning = true;
        TaskRateCheck(new RateCheckTask());
        setRegisteredNumber();
    }



    @Override
    public void onStartDrag(AdapterEditGroupList.ViewHolder holder) {
        itemTouchHelper.startDrag(holder);

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
                timeDelay = 500 / devList.size();
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
                    String res = getPingTime(devList.get(i).getIp());
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

    private void editGroupDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_group);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        RecyclerView listDevice = dialog.findViewById(R.id.dgEditGroupList);
        listDevice.setHasFixedSize(true);
        listDevice.setLayoutManager(new LinearLayoutManager(context));

        EditGroupItemTouchCallback callback = new EditGroupItemTouchCallback(adapterEditGroupList);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(listDevice);

        listDevice.setAdapter(adapterEditGroupList);
        final JToastShow jToastShow = new JToastShow();

        final Button add = dialog.findViewById(R.id.dgEditGroupAdd);
        add.setOnClickListener(view -> {
            Dialog subDialog = new Dialog(Objects.requireNonNull(context));
            subDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            subDialog.setContentView(R.layout.dialog_edit_list);

            final LinearLayout groupLayout = subDialog.findViewById(R.id.layout_group);
            groupLayout.setVisibility(View.GONE);

            final TextView title = subDialog.findViewById(R.id.dialog_title);
            title.setText("Add Group");
            final EditText addGroup = subDialog.findViewById(R.id.dg_edit_loc);
            final Button apply = subDialog.findViewById(R.id.dg_btn_loc);
            addGroup.setHint("Input Group Name");

            apply.setOnClickListener(view2 -> {
                groupList.add(new ItemGroup(AppUtils.GROUP_HEADER, Objects.requireNonNull(addGroup.getText()).toString(), "", "", "", ""));
                AppUtils.putGroupList(tinyDB, KeyList.LIST_GROUP_HEADER, groupList);
                adapterEditGroupList.notifyDataSetChanged();
                getGroupList();

                listDevice.setAdapter(adapterEditGroupList);
                subDialog.dismiss();
            });

            subDialog.show();
        });
        final Button delete = dialog.findViewById(R.id.dgEditGroupDelete);
        delete.setOnClickListener(view -> {
            int size = groupList.size();
            int selectSize = 0;
            for (int i = 0; i < size; i++) {
                ItemGroup item = groupList.get(i);
                if (item.isSelected()) {
                    selectSize++;
                }
            }

            if ((size - selectSize) == 0) {
                jToastShow.createToast(context, "삭제 실패", "한개 이상 그룹이 존재해야 합니다.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                return;
            }
            for (int i = 0; i < size; i++) {
                ItemGroup item = groupList.get(i);

                if (item.isSelected()) {
                    groupList.remove(i);
                    size--;
                    i--;
                }
            }
            AppUtils.putGroupList(tinyDB, KeyList.LIST_GROUP_HEADER, groupList);
            adapterEditGroupList.notifyDataSetChanged();
            getGroupList();
        });
        final Button close = dialog.findViewById(R.id.dgEditGroupClose);
        close.setOnClickListener(view -> {
            getDeviceList();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void modifyDialog(Context context) {
        ArrayList<ItemDevice> temp = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_modify_list);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        RecyclerView listDevice = dialog.findViewById(R.id.modifyList);
        listDevice.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        listDevice.setLayoutManager(layoutManager);

        AdapterModifyList modifyList = new AdapterModifyList(getContext(), temp);
        listDevice.setAdapter(modifyList);
        listDevice.setItemViewCacheSize(temp.size());
        final Button apply = dialog.findViewById(R.id.dg_mApply);
        apply.setOnClickListener(view -> {
            getDeviceList();
            dialog.dismiss();
        });
        dialog.show();

    }

    private void dialogSort(Context context) {

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_dev_sort);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final TextView title = dialog.findViewById(R.id.dgTxtTitle);
        final AppCompatSpinner spanSpinner = dialog.findViewById(R.id.dgSpanCount);
        final RadioButton btnAscending = dialog.findViewById(R.id.dgRaBtnAscending);
        final RadioButton btnModel = dialog.findViewById(R.id.dgRaBtnModel);
        final RadioButton btnIp = dialog.findViewById(R.id.dgRaBtnIp);
        final RadioButton btnLoc = dialog.findViewById(R.id.dgBtnRaLoc);

        final Button apply = dialog.findViewById(R.id.dgBtnSortApply);
        final Button close = dialog.findViewById(R.id.dbBtnSortClose);

        title.setText("Sort List");

        String[] countArray = getResources().getStringArray(R.array.span_count);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.spinner_normal, R.id.spinnerText, countArray);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spanSpinner.setAdapter(adapter);
        spanSpinner.setSelection(tinyDB.getInt(KeyList.LIST_SPAN_COUNT) - 1);

        apply.setOnClickListener(view -> {
            ArrayList<ItemDevice> temp = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);

            boolean isSort = btnAscending.isChecked();

            if (btnModel.isChecked()) {
                if (isSort) {
                    Collections.sort(temp, (a1, p1) -> p1.getModel().compareTo(a1.getModel()));
                } else {
                    Collections.sort(temp, (a1, p1) -> a1.getModel().compareTo(p1.getModel()));
                }
            } else if (btnIp.isChecked()) {
                if (isSort) {
                    Collections.sort(temp, (a1, p1) -> p1.getIp().compareTo(a1.getIp()));
                } else {
                    Collections.sort(temp, (a1, p1) -> a1.getIp().compareTo(p1.getIp()));
                }
            } else if (btnLoc.isChecked()) {
                if (isSort) {
                    Collections.sort(temp, (a1, p1) -> p1.getLoc().compareTo(a1.getLoc()));
                } else {
                    Collections.sort(temp, (a1, p1) -> a1.getLoc().compareTo(p1.getLoc()));
                }
            }
            AppUtils.putItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG, temp);
            tinyDB.putInt(KeyList.LIST_SPAN_COUNT, spanSpinner.getSelectedItemPosition() + 1);

            getDeviceList();

            dialog.dismiss();

        });

        close.setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialog.show();

    }

    @Override
    public void onSelectList(ItemDevice itemDevice) {
        if (pingTest(itemDevice.getIp())) {
            IPDevice ipDevice = new IPDevice();
            ipDevice.setId(tinyDB.getString(KeyList.KEY_MASTER_ID));
            ipDevice.setPassword(tinyDB.getString(KeyList.KEY_MASTER_PASS));
            ipDevice.setIpAddress(itemDevice.getIp());
            ipDevice.setName(itemDevice.getModel());
            Log.d(TAG, "==========Port: " + itemDevice.getHttp());
            ipDevice.setPort(Integer.parseInt(transPort(itemDevice.getHttp())));
            ipDevice.setRtspUri(itemDevice.getRtspUri());
            ipDevice.setLoc(itemDevice.getLoc());
            ipDevice.setMac(itemDevice.getMac());
            ipDevice.setMode(AppUtils.CALL_MODE_OUTGOING);
            Bundle videoBundle = new Bundle(1);
            videoBundle.putParcelable("ipDevice", ipDevice);

            FragmentCallView fragment = new FragmentCallView();
            fragment.setArguments(videoBundle);
            ((ActivityMain) ActivityMain.context).setGoFragment(fragment);
        } else {
            jToastShow.createToast(getContext(), "연결 실패", "네트워크 연결을 실패했습니다. 비상벨과 주수신기 연결 상태를 확인하세요.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
        }
    }

    @Override
    public void onRegisterList(ItemDevice itemDevConfig) {
        Log.d(TAG, "What: " + itemDevConfig.getLoc());
    }


    private String transPort(String val) {
        if (val.length() != 3) {
            return val;
        }
        String str1 = val.substring(0, 2);
        String str2 = val.substring(2, 4);
        return String.valueOf(Integer.parseInt((str2 + str1), 16));
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

        return result == 0;
    }
}
