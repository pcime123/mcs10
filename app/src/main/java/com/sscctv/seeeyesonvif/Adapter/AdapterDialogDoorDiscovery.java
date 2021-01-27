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

import com.sscctv.seeeyesonvif.Items.ItemDoorDevice;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.ModelList;
import com.sscctv.seeeyesonvif.Interfaces.OnSelectDoorDevice;

import java.util.List;

public class AdapterDialogDoorDiscovery extends RecyclerView.Adapter<AdapterDialogDoorDiscovery.ViewHolder> {
    private static final String TAG = AdapterDialogDoorDiscovery.class.getSimpleName();
    private final SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);
    private final Context context;
    private final OnSelectDoorDevice mCallback;
    private final List<ItemDoorDevice> items;

    public AdapterDialogDoorDiscovery(Context context, List<ItemDoorDevice> items, OnSelectDoorDevice mCallback) {
        this.context = context;
        this.items = items;
        this.mCallback = mCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_door_config, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ItemDoorDevice item = items.get(position);
        holder.doorModel.setText(item.getdModel());

        if(item.getdModel().equals(ModelList.DCS01C)) {
            holder.camModel.setText(ModelList.ECS30CMDCS);
        } else {
            holder.camModel.setText(ModelList.ECS30NMDCS);
        }

        holder.doorIp.setText(item.getdIp());
        holder.doorMac.setText(item.getdMac());
        holder.camIp.setText(item.getcIp());
        holder.camMac.setText(item.getcMac());
        holder.location.setText(item.getLoc());

        if (isItemSelected(position)) {
            holder.layout.setBackgroundColor(context.getResources().getColor(R.color.light_dark_sky));
            mCallback.onSelectDoorList(item);
        } else {
            holder.layout.setBackgroundColor(context.getResources().getColor(R.color.White_1));
            mCallback.onSelectDoorList(null);
        }

        if (item.isRegistered()) {
            holder.btnReg.setVisibility(View.GONE);
            holder.reg.setVisibility(View.VISIBLE);
            holder.reg.setText(R.string.reg_added);
        } else {
            holder.btnReg.setVisibility(View.VISIBLE);
            holder.btnReg.setText(R.string.reg_add);
            holder.reg.setVisibility(View.GONE);
        }

        holder.btnReg.setOnClickListener(view -> mCallback.onRegisterDoorList(item));

        holder.layout.setSelected(isItemSelected(position));
    }


    @Override
    public int getItemCount() {
        return this.items.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView doorModel, doorIp, doorMac, location, reg;
        final TextView camModel, camIp, camMac;
        final Button btnReg;
        final LinearLayout layout;

        ViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.doorLayout);
            doorModel = view.findViewById(R.id.doorModel);
            camModel = view.findViewById(R.id.camModel);
            doorIp = view.findViewById(R.id.doorIp);
            camIp = view.findViewById(R.id.camIp);
            doorMac = view.findViewById(R.id.doorMac);
            camMac = view.findViewById(R.id.camMac);
            location = view.findViewById(R.id.doorLocation);
            reg = view.findViewById(R.id.doorTxtReg);
            btnReg = view.findViewById(R.id.doorBtnReg);

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
