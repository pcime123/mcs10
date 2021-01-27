package com.sscctv.seeeyesonvif.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.databinding.FragmentMapListBinding;
import com.sscctv.seeeyesonvif.Map.utils.AdapterMapList;
import com.sscctv.seeeyesonvif.Map.utils.ItemMapList;
import com.sscctv.seeeyesonvif.Map.utils.MapClickListener;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.ArrayList;

public class FragmentMapList extends Fragment implements MapClickListener {

    private static final String TAG = FragmentMapList.class.getSimpleName();
    private FragmentMapListBinding mBinding;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private ArrayList<ItemMapList> resultList;
    private AdapterMapList adapter;

    public static FragmentMapList newInstance() {
        FragmentMapList fragment = new FragmentMapList();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        getMapConfig();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_map_list, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {

        }

        mBinding.btnMapConfigAdd.setOnClickListener(view -> {
            ((ActivityMain) ActivityMain.context).setGoFragment(new FragmentMapConfig());
        });

        mBinding.btnImageEdit.setOnClickListener(view -> {
            ((ActivityMain) ActivityMain.context).setGoFragment(new FragmentMapAdd());

        });
        mBinding.btnMapConfigClose.setOnClickListener(view -> {
            ((ActivityMain) ActivityMain.context).prevFragment();
        });

        resultList = AppUtils.getMapConfigList(tinyDB, KeyList.LIST_MAP_CONFIG);

        return mBinding.getRoot();
    }

    private void getMapConfig() {

        GridLayoutManager manager = new GridLayoutManager(getContext(), 4);
        mBinding.gListView.setLayoutManager(manager);
        mBinding.gListView.hasFixedSize();

        adapter = new AdapterMapList(getContext(), resultList, this);
        mBinding.gListView.setAdapter(adapter);

    }

    @Override
    public void ClickMapConfig(ItemMapList holder) {
        ItemMapList itemMapList = new ItemMapList();
        itemMapList.setName(holder.getName());
        itemMapList.setLocation(holder.getLocation());
        itemMapList.setImgPath(holder.getImgPath());
        itemMapList.setDevList(holder.getDevList());

        Bundle bundle = new Bundle();
        bundle.putParcelable("mapConfig", itemMapList);

        FragmentMapConfig fragmentMapConfig = new FragmentMapConfig();
        fragmentMapConfig.setArguments(bundle);

        ((ActivityMain) ActivityMain.context).setGoFragment(fragmentMapConfig);

    }

    private Bundle setBundle(String tag, String msg) {
        Bundle bundle = new Bundle();
        bundle.putString(tag, msg);
        return bundle;
    }
}
