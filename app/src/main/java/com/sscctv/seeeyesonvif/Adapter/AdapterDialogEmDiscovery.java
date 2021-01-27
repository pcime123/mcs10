package com.sscctv.seeeyesonvif.Adapter;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.sscctv.seeeyesonvif.Interfaces.OnSelectEmDevice;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.R;

import java.util.List;

public class AdapterDialogEmDiscovery extends RecyclerView.Adapter<AdapterDialogEmDiscovery.ViewHolder> {
    private static final String TAG = AdapterDialogEmDiscovery.class.getSimpleName();
    private final SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);

    private final List<ItemDevice> items;
    private final Context context;
    private final OnSelectEmDevice mCallback;
    public AdapterDialogEmDiscovery(Context context, List<ItemDevice> items, OnSelectEmDevice mCallback) {
        this.context = context;
        this.items = items;
        this.mCallback = mCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dev_config, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ItemDevice item = items.get(position);
        holder.model.setText(item.getModel());
        holder.ip.setText(item.getIp());
        holder.type.setText(item.getType());
        holder.mac.setText(item.getMac());
        holder.location.setText(item.getLoc());

        if(isItemSelected(position)) {
            holder.layout.setBackgroundColor(context.getResources().getColor(R.color.light_dark_sky));
            mCallback.onSelectList(item);
        } else {
            holder.layout.setBackgroundColor(context.getResources().getColor(R.color.White_1));
        }

        if(item.isRegistered()) {
            holder.btnReg.setVisibility(View.GONE);
            holder.reg.setVisibility(View.VISIBLE);
            holder.reg.setText(R.string.reg_added);
        } else {
            holder.btnReg.setVisibility(View.VISIBLE);
            holder.btnReg.setText(R.string.reg_add);
            holder.reg.setVisibility(View.GONE);
        }

        holder.btnReg.setOnClickListener(view -> {
            mCallback.onRegisterList(item);
        });


        holder.layout.setSelected(isItemSelected(position));
    }


    @Override
    public int getItemCount() {
        return this.items.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView model, type, ip, mac, location, reg;
        final Button btnReg;
        final LinearLayout layout;
        ViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.iLayout);
            model = view.findViewById(R.id.iModel);
            type = view.findViewById(R.id.iType);
            ip = view.findViewById(R.id.iIp);
            mac = view.findViewById(R.id.iMac);
            location = view.findViewById(R.id.iLocation);
            reg = view.findViewById(R.id.iTxtReg);
            btnReg = view.findViewById(R.id.iBtnReg);

            layout.setOnClickListener(view1 -> {
                int position = getAdapterPosition();
                clearSelectedItem();
                toggleItemSelected(position);
            });
        }
    }

    private void toggleItemSelected(int position) {
        Log.d(TAG, "Select: " + mSelectedItems.get(position));
        if (!mSelectedItems.get(position)) {
            mSelectedItems.put(position, true);
        }
        notifyItemChanged(position);
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

//    public List<ItemDevConfig> getSelectedItem() {
//        List<ItemDevConfig> sel = new ArrayList<>();
//
//        for (int i = 0; i < items.size(); i++) {
//            ItemDevConfig item = items.get(i);
//            if (item.isSelected()) {
//                sel.add(new ExtItem(item.getNum(), item.getName(), true));
//            }
//        }
//        return sel;
//    }

//    public void setAllSelected(final boolean ischked) {
//        final int tempSize = items.size();
//        Log.d(TAG, "checked: " + ischked + " Size: " + tempSize);
//
//        if (ischked) {
//            for (int i = 0; i < tempSize; i++) {
//                items.get(i).setSelected(true);
//            }
//        } else {
//            for (int i = 0; i < tempSize; i++) {
//                items.get(i).setSelected(false);
//            }
//        }
//    }

    private boolean isItemSelected(int position) {
        return mSelectedItems.get(position, false);
    }

    public void clear() {
        int size = items.size();
        if (size > 0) {
            items.subList(0, size).clear();

            notifyItemRangeRemoved(0, size);
        }
    }
}
