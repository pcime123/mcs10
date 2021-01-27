package com.sscctv.seeeyesonvif.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Interfaces.EditGroupItemTouchCallback;
import com.sscctv.seeeyesonvif.Items.IPDevice;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.Map.utils.AdapterMapSort;
import com.sscctv.seeeyesonvif.Map.utils.ItemMapConfig;
import com.sscctv.seeeyesonvif.Map.utils.ItemMapList;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.JToastShow;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.ModelList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;
import com.sscctv.seeeyesonvif.databinding.FragmentEmapBinding;

import java.util.ArrayList;
import java.util.Objects;

public class FragmentMapMain extends Fragment implements AdapterMapSort.OnStartDragListener {

    private static final String TAG = FragmentMapMain.class.getSimpleName();
    private FragmentEmapBinding mBinding;
    private ArrayList<ItemMapList> resultList;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private LinearLayout mapLayout;
    private JToastShow jToastShow;

    private AdapterMapSort adapterMapSort;
    private ItemTouchHelper itemTouchHelper;

    public static FragmentMapMain newInstance() {
        FragmentMapMain fragment = new FragmentMapMain();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        getMapList();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
        saveMapConfig();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_emap, container, false);
        jToastShow = new JToastShow();
//        Bundle bundle = this.getArguments();
//        if (bundle != null) {
//
//        }

        mBinding.layoutMap.setVisibility(View.VISIBLE);
        mBinding.btnMapView.setOnClickListener(view -> {
            ((ActivityMain) ActivityMain.context).setGoFragment(new FragmentMapList());

        });

        mBinding.btnCallList.setOnClickListener(view -> {
            ((ActivityMain) ActivityMain.context).setGoFragment(new FragmentCallLog());
        });

        mBinding.btnMapListSort.setOnClickListener(view -> {
            if (resultList.size() != 0) {
                editGroupDialog(getContext());
            } else {
                jToastShow.createToast(Objects.requireNonNull(getContext()), "에러", "등록된 맵 정보가 없습니다.", jToastShow.TOAST_WARNING, 80, jToastShow.LONG_DURATION, null);
            }
        });
        return mBinding.getRoot();
    }


    private void getMapList() {
        resultList = AppUtils.getMapConfigList(tinyDB, KeyList.LIST_MAP_CONFIG);
        adapterMapSort = new AdapterMapSort(getContext(), resultList, this);

        int resultSize = resultList.size();
        for (int i = 0; i < resultSize; i++) {
            createMapList(resultList.get(i), i);
            if (tinyDB.getInt(KeyList.MAP_LAST_PAGE) == i) {
                mapLayout.performClick();
            }
        }
    }

    private void saveMapConfig() {
        AppUtils.putMapConfigList(tinyDB, KeyList.LIST_MAP_CONFIG, resultList);
    }

    private void createMapList(ItemMapList mapConfigs, int page) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.item_map_main, null, false);
        TextView mapName = layout.findViewById(R.id.itemMapName);
        mapName.setText(mapConfigs.getName());
        TextView macLoc = layout.findViewById(R.id.itemMapLocation);
        macLoc.setText(mapConfigs.getLocation());

        mapLayout = layout.findViewById(R.id.itemMapTitleLayout);
        mapLayout.setTag(page);

        mapLayout.setOnClickListener(view -> {
            tinyDB.putInt(KeyList.MAP_LAST_PAGE, (int) view.getTag());

            mBinding.testView.removeAllViews();
            final boolean[] tVal = new boolean[1];
            int numIcon = 0;
            if (mapConfigs.getImgPath() != null) {
                Glide.with(Objects.requireNonNull(getContext()))
                        .load(mapConfigs.getImgPath())
                        .apply(new RequestOptions().centerCrop())
                        .into(mBinding.mainMapImg);
            }

            for (int i = 0; i < mapConfigs.getDevList().size(); i++) {

                final int num = i;

                if (mapConfigs.getDevList().get(i).isTagged()) {

                    final LayoutInflater[] inflate = {(LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(Context.LAYOUT_INFLATER_SERVICE)};
                    View iconView = Objects.requireNonNull(inflate[0]).inflate(R.layout.item_map_icon, null);
                    ImageView mapIcon = iconView.findViewById(R.id.itemMapIcon);
                    mapIcon.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_state_normal));

                    String model = mapConfigs.getDevList().get(i).getModel();
                    switch (model) {
                        case ModelList.ECS30CW:
                            mapIcon.setImageResource(R.drawable.icon_ecs30cw);
                            break;
                        case ModelList.ECS30CWP:
                            mapIcon.setImageResource(R.drawable.icon_ecs30cwp);
                            break;
                        default:
                            mapIcon.setImageResource(R.drawable.icon_ecs30cmdcs);
                            break;
                    }

                    if (mapConfigs.getDevList().get(num).getxPos() == 0) {
                        numIcon++;
                        int xPos = 10 + (100 * num);
                        int yPos;

                        if (numIcon < 10) {
                            yPos = 10;
                        } else {
                            yPos = 150;
                        }

                        mapConfigs.getDevList().get(num).setxPos(xPos);
                        mapConfigs.getDevList().get(num).setyPos(yPos);
                    }


                    iconView.setX(mapConfigs.getDevList().get(num).getxPos());
                    iconView.setY(mapConfigs.getDevList().get(num).getyPos());
                    iconView.setOnClickListener(view1 -> {
                        goDevInfoDialog(mapConfigs.getDevList().get(num));
                    });

                    iconView.setOnLongClickListener(view12 -> {
                        mapIcon.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_state_move));

                        tVal[0] = true;
                        return false;
                    });
                    iconView.setOnTouchListener(new View.OnTouchListener() {
                        float x, y;

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    mapIcon.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.button_state_pressed));

                                    x = v.getX() - event.getRawX();
                                    y = v.getY() - event.getRawY();
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (tVal[0]) {
                                        float moveX = event.getRawX() + x;
                                        float moveY = event.getRawY() + y;
                                        if (moveX > 930) {
                                            moveX = 930;
                                        } else if (moveX < 0) {
                                            moveX = 0;
                                        }
                                        if (moveY > 630) {
                                            moveY = 630;
                                        } else if (moveY < 0) {
                                            moveY = 0;
                                        }
                                        v.animate().x(moveX).y(moveY).setDuration(0).start();
                                    }
                                    break;
                                case MotionEvent.ACTION_UP:
                                    mapIcon.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.button_state_normal));

                                    if (tVal[0]) {
                                        mapConfigs.getDevList().get(num).setxPos(v.getX());
                                        mapConfigs.getDevList().get(num).setyPos(v.getY());

                                        tVal[0] = false;
                                        return true;
                                    }
                                    break;

                            }

                            return false;
                        }
                    });

                    mBinding.testView.addView(iconView);
                }
            }

        });
        mBinding.mapMainList.addView(layout);
    }

    private void goDevInfoDialog(ItemMapConfig mapConfig) {
        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_dev_info);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        final ImageView icon = dialog.findViewById(R.id.eInfoIcon);
        final TextView model = dialog.findViewById(R.id.eInfoModel);
        final TextView ip = dialog.findViewById(R.id.eInfoIp);
        final TextView mac = dialog.findViewById(R.id.eInfoMac);
        final TextView group = dialog.findViewById(R.id.eInfoGroup);
        final TextView location = dialog.findViewById(R.id.eInfoLoc);

        String getModel = mapConfig.getModel();
        model.setText(getModel);

        icon.setImageResource(AppUtils.modelIconImage(getModel));
        ip.setText(mapConfig.getIp());
        mac.setText(mapConfig.getMac());
        group.setText(mapConfig.getGroup());
        location.setText(mapConfig.getLoc());

        final Button call = dialog.findViewById(R.id.eInfoBtnCall);
        call.setOnClickListener(view -> {
            ArrayList<ItemDevice> temp = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);
            String callMac = mapConfig.getMac();
            ItemDevice callDevice = null;
            for (int i = 0; i < temp.size(); i++) {
                if (callMac.equals(temp.get(i).getMac())) {
                    callDevice = temp.get(i);
                    break;
                }
            }

            if (callDevice == null) {
                jToastShow.createToast(getContext(), "에러", "장비 목록에 해당 장비가 없습니다. 해당 장비는 사용할 수 없으니 삭제해주세요.", jToastShow.TOAST_WARNING, 80, jToastShow.LONG_DURATION, null);
                return;
            }

            if (!AppUtils.netPingChk(callDevice.getIp())) {
                jToastShow.createToast(getContext(), "에러", "네트워크 연결에 문제가 있습니다. 해당 장비 또는 네트워크를 확인해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
                return;
            }

            IPDevice ipDevice = new IPDevice();
            ipDevice.setId(tinyDB.getString(KeyList.KEY_MASTER_ID));
            ipDevice.setPassword(tinyDB.getString(KeyList.KEY_MASTER_PASS));
            ipDevice.setIpAddress(callDevice.getIp());
            ipDevice.setName(callDevice.getModel());
            ipDevice.setPort(Integer.parseInt(callDevice.getHttp()));
            ipDevice.setRtspUri(callDevice.getRtspUri());
            ipDevice.setLoc(callDevice.getLoc());
            ipDevice.setMac(callDevice.getMac());
            ipDevice.setMode(AppUtils.CALL_MODE_OUTGOING);
            Bundle videoBundle = new Bundle(1);
            videoBundle.putParcelable("ipDevice", ipDevice);

            tinyDB.putString(KeyList.CALL_CURRENT_DEVICE, callDevice.getMac());

            FragmentCallView fragment = new FragmentCallView();
            fragment.setArguments(videoBundle);
            ((ActivityMain) ActivityMain.context).setGoFragment(fragment);
            dialog.dismiss();
        });


        final Button close = dialog.findViewById(R.id.eInfoBtnClose);
        close.setOnClickListener(view -> {
            dialog.dismiss();
        });


        dialog.show();

    }

    private void editGroupDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_map_sort);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        RecyclerView listDevice = dialog.findViewById(R.id.dgMapSortList);
        listDevice.setHasFixedSize(true);
        listDevice.setLayoutManager(new LinearLayoutManager(context));

        EditGroupItemTouchCallback callback = new EditGroupItemTouchCallback(adapterMapSort);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(listDevice);

        listDevice.setAdapter(adapterMapSort);

        final Button close = dialog.findViewById(R.id.dgEditGroupClose);
        close.setOnClickListener(view -> {
            AppUtils.putMapConfigList(tinyDB, KeyList.LIST_MAP_CONFIG, resultList);
            mBinding.mapMainList.removeAllViews();
            for (int i = 0; i < resultList.size(); i++) {
                createMapList(resultList.get(i), i);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onStartDrag(AdapterMapSort.ViewHolder holder) {
        itemTouchHelper.startDrag(holder);

    }
}
