package com.sscctv.seeeyesonvif.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.sscctv.seeeyesonvif.Adapter.AdapterCallLogTitle;
import com.sscctv.seeeyesonvif.Items.ItemEmCallLog;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.FileDialog;
import com.sscctv.seeeyesonvif.Utils.JToastShow;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.databinding.FragmentCalllogBinding;

import org.linphone.core.Core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentCallLog extends Fragment {
    private static final String TAG = "CallListFragment";
    public static final String NULL = "   ";
    private TinyDB tinyDB;
    private AdapterCallLogTitle adapterCallLogTitle;
    private Core core;
    private FragmentCalllogBinding mBinding;
    private ArrayList<ItemEmCallLog> callLogItems = new ArrayList<>();
    private int listSize;
    private List<Fragment> listFragments;
    boolean isMissed = false;
    private JToastShow jToastShow;
    boolean isBtnClick = false;
    int tabPosition = 0;
    String mChosen;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        Log.v(TAG, "onAttach()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        Log.v(TAG, "onDetach()");
    }

    @Override
    public void onResume() {
        super.onResume();
        initFragment();
//        Log.v(TAG, "onResume()");

    }

    @Override
    public void onPause() {
        super.onPause();
//        Log.v(TAG, "onPause()");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_calllog, container, false);

        tinyDB = new TinyDB(getContext());
        jToastShow = new JToastShow();

        mBinding.callLayout.addTab((mBinding.callLayout.newTab().setText("비상벨 호출 목록")));
        mBinding.callLayout.addTab((mBinding.callLayout.newTab().setText("일반 호출 목록")));
        mBinding.callLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        assert getFragmentManager() != null;


        mBinding.btnRemoveList.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            Log.d(TAG, "Tab Postion: " + tabPosition);
            if (tabPosition == 0) {

                builder.setTitle("확인").setMessage("비상벨 호출 목록을 삭제하시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tinyDB.remove(KeyList.LOG_CALL_EM);
                        initFragment();
                    }
                });

            } else {
                builder.setTitle("확인").setMessage("일반 호출 목록을 삭제하시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        tinyDB.remove(KeyList.LOG_CALL_EM);
//                        initFragment();
                    }
                });

            }
            builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });


            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        });

        mBinding.btnMissedSort.setOnClickListener(view -> {
            if (!isBtnClick) {
                ArrayList<ItemEmCallLog> temp = AppUtils.getCallLog(tinyDB, KeyList.LOG_CALL_EM);
                for (int i = 0; i < temp.size(); i++) {
                    if (temp.get(i).isCallCheck()) {
                        isMissed = true;
                        break;
                    }
                }
                if (isMissed) {
                    mBinding.btnMissedSort.setText("Reset Sort");
                    isBtnClick = true;
                    initFragment();
                } else {
                    jToastShow.createToast(Objects.requireNonNull(getContext()), "알림", "부재인 호출이 목록에 없습니다.", jToastShow.TOAST_INFO, 80, jToastShow.SHORT_DURATION, null);
                }
            } else {
                mBinding.btnMissedSort.setText("Missed Sort");
                isMissed = false;
                isBtnClick = false;
                initFragment();
            }
        });

        mBinding.btnSaveList.setOnClickListener(view -> {
            FileDialog FileOpenDialog = new FileDialog(getContext(), FileDialog.FILE_SAVE,
                    this::setTxtLogFile);

            FileOpenDialog.defaultFileName = "";
            FileOpenDialog.chooseFileOrDir();
        });
        return mBinding.getRoot();
    }

    private void setTxtLogFile(boolean list, String path) {

        File saveFile = new File(path + ".txt");

        ArrayList<ItemEmCallLog> log = AppUtils.getCallLog(tinyDB, KeyList.LOG_CALL_EM);
        String callLog = " ";
        try {
            FileOutputStream fos = new FileOutputStream(saveFile);
            for(int i = 0; i<log.size(); i++) {
                ItemEmCallLog items = log.get(i);
                callLog = items.getCallDate() + NULL + items.getCallTime() + NULL + items.getCallModel() + NULL + items.getCallStatus() + NULL + items.getCallType() + NULL +
                        items.getCallLoc() + "\n";
                fos.write(callLog.getBytes());
            }
            fos.close();
        } catch (IOException ignored) {}

        if(saveFile.isFile()) {
            Log.d(TAG, "Good");
        } else {
            Log.e(TAG, "Fault");
        }
    }

    private Bundle missedSort(boolean val) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("missed", val);
        return bundle;
    }

    private void initFragment() {


        listFragments = new ArrayList<>();
        TabCallLogEmergency tabCallLogEmergency = new TabCallLogEmergency();
        tabCallLogEmergency.setArguments(missedSort(isMissed));

        listFragments.add(tabCallLogEmergency);
        listFragments.add(new TabCallLogNormal());

        adapterCallLogTitle = new AdapterCallLogTitle(getChildFragmentManager(), listFragments);
        mBinding.viewPager.setAdapter(adapterCallLogTitle);
        mBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.callLayout));
        mBinding.callLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mBinding.viewPager.setCurrentItem(tab.getPosition());
                tabPosition = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
