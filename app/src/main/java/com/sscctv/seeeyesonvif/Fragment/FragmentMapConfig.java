package com.sscctv.seeeyesonvif.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.Items.ItemGroup;
import com.sscctv.seeeyesonvif.Map.utils.AdapterMapConfig;
import com.sscctv.seeeyesonvif.Map.utils.AdapterMapDeivce;
import com.sscctv.seeeyesonvif.Map.utils.AdapterMapFolder;
import com.sscctv.seeeyesonvif.Map.utils.ItemClickListener;
import com.sscctv.seeeyesonvif.Map.utils.ItemMapConfig;
import com.sscctv.seeeyesonvif.Map.utils.ItemMapList;
import com.sscctv.seeeyesonvif.Map.utils.ItemPictureList;
import com.sscctv.seeeyesonvif.Map.utils.PicHolder;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.JToastShow;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.databinding.FragmentMapConfigBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class FragmentMapConfig extends Fragment implements ItemClickListener {

    private static final String TAG = FragmentMapConfig.class.getSimpleName();
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private FragmentMapConfigBinding mBinding;
    private Dialog selectDialog;
    private InputMethodManager imm;
    private JToastShow jToastShow;
    private String imgPath, name, location;
    private ArrayList<ItemMapConfig> mapList;
    private ArrayList<ItemDevice> devList;
    private boolean mode = false;
    private AdapterMapConfig listAdapter;

    public static FragmentMapConfig newInstance() {
        FragmentMapConfig fragment = new FragmentMapConfig();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        getDeviceList();
        onDeleteMode();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_map_config, container, false);
        imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
        jToastShow = new JToastShow();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            ItemMapList itemMapList = bundle.getParcelable("mapConfig");
            if (itemMapList != null) {
                imgPath = itemMapList.getImgPath();
                name = itemMapList.getName();
                location = itemMapList.getLocation();
                mapList = itemMapList.getDevList();

                loadConfig();
                mode = true;
            }
        } else {
            mapList = new ArrayList<>();
        }

        mBinding.getRoot().setOnTouchListener((view, motionEvent) -> {
            if (Objects.requireNonNull(getActivity()).getCurrentFocus() != null) {
                assert imm != null;
                imm.hideSoftInputFromWindow(Objects.requireNonNull(getActivity().getCurrentFocus()).getWindowToken(), 0);
            }
            return true;
        });

        mBinding.btnMapSetup.setOnClickListener(view -> {
            dialogSetImg();
        });

        mBinding.btnMapRemove.setOnClickListener(view -> {
            dialogRemoveImg();
        });

        mBinding.layoutMapConfig.setOnClickListener(view -> {
            if (imgPath == null) {
                jToastShow.createToast(getContext(), "알림", "이미지가 없습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
            } else {
                dialogSelectMap(imgPath);
            }
        });

        mBinding.btnMapDevice.setOnClickListener(view -> {
//            mapList = new ArrayList<>();
            dialogAddDevice();

        });

        mBinding.btnMapSave.setOnClickListener(view -> {
            if (saveMapConfig()) {
                ((ActivityMain) ActivityMain.context).prevFragment();
            }
        });

        mBinding.btnMapClose.setOnClickListener(view -> {
//            for (int i = 0; i < mapList.size(); i++) {
//                Log.d(TAG, "List: " + mapList.get(i).getIp());
//            }
            ((ActivityMain) ActivityMain.context).prevFragment();
        });


        mBinding.btnMapDelete.setOnClickListener(view -> {
            deleteConfig();
        });

        return mBinding.getRoot();
    }

    private void deleteConfig() {
        ArrayList<ItemMapList> result = AppUtils.getMapConfigList(tinyDB, KeyList.LIST_MAP_CONFIG);
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).getName().equals(name) && result.get(i).getImgPath().equals(imgPath) && result.get(i).getLocation().equals(location)) {
                result.remove(i);
                break;
            }
        }
        AppUtils.putMapConfigList(tinyDB, KeyList.LIST_MAP_CONFIG, result);

        ((ActivityMain) ActivityMain.context).prevFragment();
    }

    private void onDeleteMode() {
        if (mode) {
            mBinding.btnMapDelete.setVisibility(View.VISIBLE);
        } else {
            mBinding.btnMapDelete.setVisibility(View.GONE);
        }
    }

    private void loadConfig() {
        mBinding.imageConfig.setVisibility(View.VISIBLE);
        mBinding.empty.setVisibility(View.GONE);
        mBinding.editMapName.setText(name);
        mBinding.editMapLocation.setText(location);

        if (imgPath != null) {
            Glide.with(getContext())
                    .load(imgPath)
                    .apply(new RequestOptions().centerCrop())
                    .into(mBinding.imageConfig);
        }

        getDeviceList();
    }

    private boolean saveMapConfig() {
        ArrayList<ItemMapList> result = AppUtils.getMapConfigList(tinyDB, KeyList.LIST_MAP_CONFIG);

        if (!mode) {
            String mapImage = imgPath;
            String mapName = mBinding.editMapName.getText().toString();

            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).getName().equals(mapName)) {
                    jToastShow.createToast(getContext(), "확인", "중복된 이름은 사용할 수 없습니다. 맵 이름을 변경해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                    return false;
                }
            }

            String mapLocation = mBinding.editMapLocation.getText().toString();
            result.add(new ItemMapList(mapImage, mapName, mapLocation, mapList));

        } else {
            int position = result.size() + 1;
            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).getName().equals(name)) {
                    position = i;
                    result.remove(i);
                    break;
                }
            }

            String mapImage = imgPath;
            String mapName = mBinding.editMapName.getText().toString();

            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).getName().equals(mapName)) {
                    jToastShow.createToast(getContext(), "확인", "중복된 이름은 사용할 수 없습니다. 맵 이름을 변경해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                    return false;
                }
            }
            String mapLocation = mBinding.editMapLocation.getText().toString();
            result.add(position, new ItemMapList(mapImage, mapName, mapLocation, mapList));
        }
        AppUtils.putMapConfigList(tinyDB, KeyList.LIST_MAP_CONFIG, result);
        return true;
    }

    public void getDeviceList() {
        Log.d(TAG, "getDeviceList()");

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext());
        mBinding.listMapConfig.setLayoutManager(manager);
        listAdapter = new AdapterMapConfig(getContext(), mapList);
        mBinding.listMapConfig.setAdapter(listAdapter);
    }

    private void dialogAddDevice() {

        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_map_device);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final AppCompatSpinner spinner = dialog.findViewById(R.id.mapDeviceGroupList);
        final RecyclerView mapDevList = dialog.findViewById(R.id.dialogMapDeviceList);
        final Button close = dialog.findViewById(R.id.dialogMapClose);
        final Button add = dialog.findViewById(R.id.dialogMapAdd);

        ArrayList<ItemGroup> itemGroups = AppUtils.getGroupList(tinyDB, KeyList.LIST_GROUP_HEADER);
        ArrayList<String> groupList = new ArrayList<>();
        groupList.add("All Device");
        for (int i = 0; i < itemGroups.size(); i++) {
            groupList.add(itemGroups.get(i).getGroup());
        }
        Log.d(TAG, "Group: " + groupList.size());
        for (int i = 0; i < groupList.size(); i++) {
            Log.d(TAG, "GroupItems: " + groupList.get(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_normal, R.id.spinnerText, groupList);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext());
        mapDevList.setLayoutManager(manager);

        ArrayList<ItemDevice> deviceList = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);

        ArrayList<ItemMapConfig> tempMapList = new ArrayList<>();
        AdapterMapDeivce tempAdapter = new AdapterMapDeivce(getContext(), tempMapList);

        mapDevList.setAdapter(tempAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                tempMapList.clear();
                tempAdapter.notifyDataSetChanged();

                String strCurGroup = spinner.getSelectedItem().toString();

                if (strCurGroup.equals("All Device")) {
                    for (int i = 0; i < deviceList.size(); i++) {
                        tempMapList.add(new ItemMapConfig(deviceList.get(i).getModel(), deviceList.get(i).getMac(), deviceList.get(i).getIp(), deviceList.get(i).getGroup(), deviceList.get(i).getLoc(),true));

                    }
                } else {
                    for (int i = 0; i < deviceList.size(); i++) {
                        Log.d(TAG, "strGroup: " + strCurGroup + " Dev: " + deviceList.get(i).getGroup());
                        if (deviceList.get(i).getGroup().equals(strCurGroup)) {
                            tempMapList.add(new ItemMapConfig(deviceList.get(i).getModel(), deviceList.get(i).getMac(), deviceList.get(i).getIp(), deviceList.get(i).getGroup(), deviceList.get(i).getLoc(),true));
                        }
                    }
                }


                tempAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        add.setOnClickListener(view ->

        {
            ArrayList<ItemMapConfig> tempAddList = new ArrayList<>();
            Log.d(TAG, "tempMapList: " + tempMapList.size());

            for (int i = 0; i < tempMapList.size(); i++) {
                if (tempMapList.get(i).isSelected()) {
                    Log.d(TAG, "tempMapList Item: " + tempMapList.get(i).getMac());
                    tempAddList.add(tempMapList.get(i));
                }
            }



            for(int i = 0; i < tempAddList.size(); i++) {

                for(int a = 0; a < mapList.size(); a++) {
                    if(tempAddList.get(i).getMac().equals(mapList.get(a).getMac())) {
                        jToastShow.createToast(getContext(), "중복 확인", tempAddList.get(i).getIp() + " , " + tempAddList.get(i).getMac() + " 장비는 이미 등록되어 있습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
                        return;
                    }
                }
                Log.d(TAG, "mapList Item: " + tempMapList.get(i).getMac());
                mapList.add(tempAddList.get(i));

            }


//            for (int v = 0; v < mapList.size(); v++) {
//                if (tempMac.equals(mapList.get(i).getMac())) {
//                    Log.e(TAG, "중복된 " + mapList.get(i).getMac() + " 장비는 등록 안됨");
//                    return;
//                }
//            }
            dialog.dismiss();
            Collections.sort(mapList, (a1, p1) -> p1.getIp().compareTo(a1.getIp()));
            listAdapter = new AdapterMapConfig(getContext(), mapList);
            mBinding.listMapConfig.setAdapter(listAdapter);
//            getDeviceList();
        });

        close.setOnClickListener(view ->

        {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void dialogSetImg() {
        selectDialog = new Dialog(getContext());
        selectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectDialog.setContentView(R.layout.dialog_map_image);
        selectDialog.setCancelable(false);
        selectDialog.setCanceledOnTouchOutside(false);
        final RecyclerView imgList = selectDialog.findViewById(R.id.imageList);
        final Button close = selectDialog.findViewById(R.id.dialogMapClose);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.IMAGE_FILE;
        ArrayList<ItemPictureList> imageList = getAllImagesByFolder(path);
        AdapterMapFolder mapAdapter = new AdapterMapFolder(imageList, getContext(), this);

        GridLayoutManager manager = new GridLayoutManager(getContext(), 4);
        imgList.setLayoutManager(manager);
        imgList.hasFixedSize();
        imgList.setAdapter(mapAdapter);

        close.setOnClickListener(view -> {
            selectDialog.dismiss();
        });

        selectDialog.show();

    }

    private void dialogRemoveImg() {
        mBinding.imageConfig.setVisibility(View.GONE);
        mBinding.empty.setVisibility(View.VISIBLE);
    }

    private void dialogSelectMap(String path) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_map_info);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final ImageView imgView = dialog.findViewById(R.id.dialogMapImg);
        final Button close = dialog.findViewById(R.id.dialogMapClose);


        Glide.with(getContext())
                .load(path)
                .apply(new RequestOptions().centerCrop())
                .into(imgView);

        close.setOnClickListener(view -> {
            dialog.dismiss();
        });
        dialog.show();

    }

    public ArrayList<ItemPictureList> getAllImagesByFolder(String path) {
        ArrayList<ItemPictureList> images = new ArrayList<>();
        Uri allUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE};
        Cursor cursor = getContext().getContentResolver().query(allUri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    ItemPictureList pic = new ItemPictureList();
                    pic.setPictureName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                    pic.setPicturePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                    pic.setPictureSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
                    images.add(pic);
                } while (cursor.moveToNext());
                cursor.close();
                ArrayList<ItemPictureList> reSelection = new ArrayList<>();
                for (int i = images.size() - 1; i > -1; i--) {
                    reSelection.add(images.get(i));
                }
                images = reSelection;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    @Override
    public void onPicClicked(PicHolder holder, int position, ArrayList<ItemPictureList> pics) {

    }

    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {
        imgPath = pictureFolderPath;
        Glide.with(getContext())
                .load(imgPath)
                .apply(new RequestOptions().centerCrop())
                .into(mBinding.imageConfig);

        mBinding.imageConfig.setVisibility(View.VISIBLE);
        mBinding.empty.setVisibility(View.GONE);
        selectDialog.dismiss();

    }

}
