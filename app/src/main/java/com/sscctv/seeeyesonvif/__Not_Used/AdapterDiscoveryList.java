package com.sscctv.seeeyesonvif.__Not_Used;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Interfaces.CallBackSelect;

import java.util.List;

public class AdapterDiscoveryList extends RecyclerView.Adapter<AdapterDiscoveryList.ViewHolder> {
    private static final String TAG = AdapterDiscoveryList.class.getSimpleName();
    private static final String KEY_LIST = "room_list";
    private static final String KEY_ROOM_DB = "room$";
    private static final String KEY_SELECT = "room_select";

    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);
    private List<DeviceDiscovery> items;
    private Context context;
    private CallBackSelect mCallBack;

    public AdapterDiscoveryList(Context context, List<DeviceDiscovery> items, CallBackSelect callBack) {
        super();
        this.context = context;
        this.items = items;
        this.mCallBack = callBack;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final DeviceDiscovery item = items.get(position);
//
        holder.name.setText(item.getName());
        holder.ip.setText(item.getIp());
        holder.port.setText(String.valueOf(item.getPort()));

        holder.layout.setOnClickListener(view -> {
            DeviceDiscovery deviceDiscovery = new DeviceDiscovery();
            deviceDiscovery.setName(item.getName());
            deviceDiscovery.setIp(item.getIp());
            deviceDiscovery.setPort(item.getPort());

            mCallBack.discoverDevice(deviceDiscovery);
        });
////
//        if(isItemSelected(position)) {
////            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.JBlue));
//            holder.roomLayout.setBackground(context.getResources().getDrawable(R.drawable.back_fly_high));
//
//            tinyDB.putString(KEY_SELECT, item.getNum());
//            mCallback.roomSelect(position);
//        } else {
////            holder.cardView.setCardBackgroundColor(context.getResources().getDrawable(R.drawable.back_loon_crest));
//            holder.roomLayout.setBackground(context.getResources().getDrawable(R.drawable.back_loon_crest));
//        }
//        holder.cardView.setSelected(isItemSelected(position));
////        if (selectedItem == position) {
////            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.JBlue));
////            tinyDB.putString(KEY_SELECT, item.getNum());
////            mCallback.roomSelect();
////        } else {
////            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.gray));
////        }
////
////        holder.itemView.setTag(items.get(position));
////        holder.itemView.setOnClickListener(view -> {
////            int previousItem = selectedItem;
////            selectedItem = position;
////            notifyItemChanged(previousItem);
////            notifyItemChanged(position);
////        });
//
//        holder.cardView.getLayoutParams().height = tinyDB.getInt(KeyList.VIEW_HEIGHT) / 6;

    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name,ip,port;
        private LinearLayout layout;

        ViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.layout);
            name = view.findViewById(R.id.name);
            ip = view.findViewById(R.id.ip);
            port = view.findViewById(R.id.port);
        }

    }

    private void toggleItemSelected(int position) {
        Log.d(TAG, "Select: " + mSelectedItems.get(position));
        if (!mSelectedItems.get(position)) {
            mSelectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    private boolean isItemSelected(int position) {
        return mSelectedItems.get(position, false);
    }

    public void clearSelectedItem() {
        int position;

        for (int i = 0; i < mSelectedItems.size(); i++) {
            position = mSelectedItems.keyAt(i);
            mSelectedItems.put(position, false);
            notifyItemChanged(position);
        }

        mSelectedItems.clear();
    }


    public void clear() {
        int size = items.size();
        if (size > 0) {
            items.subList(0, size).clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    public void refresh() {

    }


}
