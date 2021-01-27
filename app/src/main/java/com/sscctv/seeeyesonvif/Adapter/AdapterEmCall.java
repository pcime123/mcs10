package com.sscctv.seeeyesonvif.Adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Fragment.FragmentCallView;
import com.sscctv.seeeyesonvif.Items.IPDevice;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.Items.ItemEmCallLog;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.JToastShow;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AdapterEmCall extends RecyclerView.Adapter<AdapterEmCall.ViewHolder> {
    private static final String TAG = AdapterEmCall.class.getSimpleName();
    private final SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);
    private final ArrayList<ItemEmCallLog> mLogs;
    public final Context context = ActivityMain.getAppContext();
    private ArrayList<ItemEmCallLog> emLogItems;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private int pos;
    private JToastShow jToastShow = new JToastShow();
    public AdapterEmCall(ArrayList<ItemEmCallLog> logs) {
        super();
        mLogs = logs;
    }

    public Object getItem(int position) {
        return mLogs.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemEmCallLog log = mLogs.get(position);
        pos = position;
        holder.date.setText(log.getCallDate());
        holder.time.setText(log.getCallTime());

        String mDevice = log.getCallModel();
        holder.mIcon.setImageResource(AppUtils.modelIconImage(mDevice));
//
//        switch (mDevice) {
//            case ModelList.ECS30CW:
//                holder.mIcon.setImageResource(R.drawable.icon_ecs30cw);
//                break;
//            case ModelList.ECS30CWP:
//                holder.mIcon.setImageResource(R.drawable.icon_ecs30cwp);
//                break;
//            case ModelList.DCS01C:
//            case ModelList.DCS01E:
//            case ModelList.DCS01N:
//            case ModelList.ECS30CMDCS:
//            case ModelList.ECS30NMDCS:
//                holder.mIcon.setImageResource(R.drawable.icon_ecs30cmdcs);
//                break;
//            default:
//                holder.mIcon.setImageResource(R.drawable.null_device);
//                break;
//        }

        String mCall = log.getCallType();
        String mStatus = log.getCallStatus();
        holder.status.setText(mStatus);

        if (mStatus.equals(AppUtils.CALL_STATUS_MISSED)) {
            holder.icon2.setImageResource(R.drawable.call_status_miss);
            holder.status.setTextColor(context.getResources().getColor(R.color.red));

        } else {
            switch (mCall) {
                case AppUtils.CALL_MODE_OUTGOING:
                    holder.icon2.setImageResource(R.drawable.call_status_out);
                    holder.status.setTextColor(context.getResources().getColor(R.color.blue));
                    break;
                case AppUtils.CALL_MODE_INCOMING:
                    holder.icon2.setImageResource(R.drawable.call_status_in);
                    holder.status.setTextColor(context.getResources().getColor(R.color.DarkOrange));
                    break;
            }
        }
        holder.device.setText(log.getCallModel());
        holder.loc.setText(log.getCallLoc());
        holder.type.setText(log.getCallType());

        if (log.isCallCheck()) {
            holder.mCheckIcon.setVisibility(View.VISIBLE);
            holder.layout.setBackground(ContextCompat.getDrawable(context, R.drawable.back_tired));
        } else {
            holder.mCheckIcon.setVisibility(View.INVISIBLE);
            holder.layout.setBackground(ContextCompat.getDrawable(context, R.drawable.back_cloudy_knoxvile));
        }

        holder.mCheckIcon.setOnClickListener(view -> {
            if (log.isCallCheck()) {
                log.setCallCheck(false);
                notifyDataSetChanged();
                AppUtils.putCallLog(tinyDB, KeyList.LOG_CALL_EM, mLogs);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.mLogs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView type;
        final TextView status;
        final TextView device;
        final TextView loc;
        final TextView time;
        final TextView date;
        final ImageView mIcon;
        final ImageView icon2;
        final CardView card;
        final LinearLayout layout;
        final ImageView mCheckIcon;

        ViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.logLayoutColor);
            card = view.findViewById(R.id.logCard);
            type = view.findViewById(R.id.logCallType);
            status = view.findViewById(R.id.logCallStatus);
            device = view.findViewById(R.id.logCallDevice);
            time = view.findViewById(R.id.logCallTime);
            date = view.findViewById(R.id.logCallDate);
            mIcon = view.findViewById(R.id.logModelIcon);
            mCheckIcon = view.findViewById(R.id.logCallCheck);
            icon2 = view.findViewById(R.id.logCallIcon);
            loc = view.findViewById(R.id.logCallLoc);


            card.setOnClickListener(view1 -> {
                Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_dev_info);

                dialog.setContentView(R.layout.dialog_dev_info);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                final ImageView icon = dialog.findViewById(R.id.eInfoIcon);
                final TextView model = dialog.findViewById(R.id.eInfoModel);
                final TextView ip = dialog.findViewById(R.id.eInfoIp);
                final TextView mac = dialog.findViewById(R.id.eInfoMac);

                final LinearLayout timeLayout = dialog.findViewById(R.id.eInfoLayoutTime);
                timeLayout.setVisibility(View.VISIBLE);
                final TextView time = dialog.findViewById(R.id.eInfoTime);

                final TextView group = dialog.findViewById(R.id.eInfoGroup);
                final TextView titleGroup = dialog.findViewById(R.id.eInfoTitleGroup);
                titleGroup.setText("Date");
                final TextView location = dialog.findViewById(R.id.eInfoLoc);

                ItemEmCallLog getLog = mLogs.get(pos);
                String getModel = getLog.getCallModel();
                model.setText(getModel);

                icon.setImageResource(AppUtils.modelIconImage(getModel));
                ip.setText(getLog.getCallIp());
                mac.setText(getLog.getCallMac());
                group.setText(getLog.getCallDate());
                time.setText(getLog.getCallTime());
                location.setText(getLog.getCallLoc());

                final Button call = dialog.findViewById(R.id.eInfoBtnCall);
                call.setOnClickListener(view2 -> {
                    ArrayList<ItemDevice> temp = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);
                    String callMac = getLog.getCallMac();
                    ItemDevice callDevice = null;
                    for (int i = 0; i < temp.size(); i++) {
                        if (callMac.equals(temp.get(i).getMac())) {
                            callDevice = temp.get(i);
                            break;
                        }
                    }

                    if (callDevice == null) {
                        jToastShow.createToast(context, "에러", "장비 목록에 해당 장비가 없습니다. 해당 장비는 사용할 수 없으니 삭제해주세요.", jToastShow.TOAST_WARNING, 80, jToastShow.LONG_DURATION, null);
                        return;
                    }

                    if (!AppUtils.netPingChk(callDevice.getIp())) {
                        jToastShow.createToast(context, "에러", "네트워크 연결에 문제가 있습니다. 해당 장비 또는 네트워크를 확인해주세요.", jToastShow.TOAST_ERROR, 80, jToastShow.LONG_DURATION, null);
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

                close.setOnClickListener(view2 -> {
                    dialog.dismiss();
                });


                dialog.show();
            });


        }

    }


    @SuppressLint("SimpleDateFormat")
    private String secondsToDisplayableString(int secs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("a HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.set(0, 0, 0, 0, 0, secs);
        return dateFormat.format(cal.getTime());
    }

    public static String _timestampToHumanDate(Context context, long timestamp, String format) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp * 1000);

            SimpleDateFormat dateFormat;
            dateFormat = new SimpleDateFormat(format, Locale.getDefault());

            return dateFormat.format(cal.getTime());
        } catch (NumberFormatException nfe) {
            return String.valueOf(timestamp);
        }
    }

//    public List<AllExtItem> getSelectedItem() {
//        List<AllExtItem> sel = new ArrayList<>();
//
//        for (int i = 0; i < items.size(); i++) {
//            AllExtItem item = items.get(i);
//            if(item.isSelected()) {
//                sel.add(new AllExtItem(item.getNum(), item.getName(), true));
//            }
//        }
//        return sel;
//    }
//
//    private void toggleItemSelected(int position) {
//        Log.d(TAG, "Select: " + mSelectedItems.get(position));
//        if (mSelectedItems.get(position)) {
////            mSelectedItems.delete(position);
//            notifyItemChanged(position);
//        } else {
//            mSelectedItems.put(position, true);
//            notifyItemChanged(position);
//        }
//    }
//
//    private boolean isItemSelected(int position) {
//        return mSelectedItems.get(position, false);
//    }
//
//    public void clearSelectedItem() {
//        int position;
//
//        for (int i = 0; i < mSelectedItems.size(); i++) {
//            position = mSelectedItems.keyAt(i);
//            mSelectedItems.put(position, false);
//            notifyItemChanged(position);
//        }
//
//        mSelectedItems.clear();
//    }
//
//
//    public void clear() {
//        int size = items.size();
//        if (size > 0) {
//            items.subList(0, size).clear();
//            notifyItemRangeRemoved(0, size);
//        }
//    }


}
