package com.sscctv.seeeyesonvif.Adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Interfaces.OnSelectGroup;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.Items.ItemGroup;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AdapterGroupList extends RecyclerView.Adapter<AdapterGroupList.ViewHolder> {
    private static final String TAG = AdapterGroupList.class.getSimpleName();
    private static final String KEY = KeyList.LIST_DEVICE_GROUP;
    public static final int HEADER = AppUtils.GROUP_HEADER;
    public static final int CHILD = AppUtils.GROUP_CHILD;
    private final SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);

    private final List<ItemGroup> items;
    private final Context context;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private final OnSelectGroup mCallBack;

    public AdapterGroupList(Context context, List<ItemGroup> items, OnSelectGroup mCallBack) {
        super();
        this.context = context;
        this.items = items;
        this.mCallBack = mCallBack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ItemGroup item = items.get(position);
        int itemCount = 0;
        for (int i = 0; i < holder.itemDevice.size(); i++) {
            if (item.getGroup().equals(holder.itemDevice.get(i).getGroup())) {
                itemCount++;
            }
        }
        holder.gName.setText(item.getGroup());
        holder.gCount.setText(String.format(Locale.getDefault(), "(%d)", itemCount));
        if (isItemSelected(position)) {
            holder.gCardView.setBackground(ContextCompat.getDrawable(context, R.drawable.rectangle_stroke_green));
            mCallBack.groupSelect(position);
        } else {
            holder.gCardView.setBackground(ContextCompat.getDrawable(context, R.drawable.rectangle_stroke_gray));
        }
        holder.gCardView.setSelected(isItemSelected(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView gName;
        private final TextView gCount;
        private final CardView gCardView;
        private final ArrayList<ItemDevice> itemDevice;

        ViewHolder(View view) {
            super(view);
            gCardView = view.findViewById(R.id.card_group);
            gName = view.findViewById(R.id.group_name);
            gCount = view.findViewById(R.id.group_count);

            gCardView.setOnClickListener(view1 -> {
                int position = getAdapterPosition();
                clearSelectedItem();
                toggleItemSelected(position);
            });
            itemDevice = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);


        }

    }


    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public boolean isItemSelected(int position) {
        return mSelectedItems.get(position, false);
    }

    public void toggleItemSelected(int position) {
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

    public void delete(int position) {

        try {
            items.remove(position);
            notifyItemRemoved(position);
        } catch (IndexOutOfBoundsException ex) {

            ex.printStackTrace();

        }

    }

}
