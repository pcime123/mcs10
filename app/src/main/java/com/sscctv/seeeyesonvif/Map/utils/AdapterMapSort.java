package com.sscctv.seeeyesonvif.Map.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Interfaces.EditGroupItemTouchCallback;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.Items.ItemGroup;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;


public class AdapterMapSort extends RecyclerView.Adapter<AdapterMapSort.ViewHolder>
implements EditGroupItemTouchCallback.OnItemMoveListener {
    private static final String TAG = AdapterMapSort.class.getSimpleName();

    public interface OnStartDragListener {
        void onStartDrag(ViewHolder holder);
    }

    private ArrayList<ItemMapList> items;
    private final Context context;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private final OnStartDragListener listener;
    public AdapterMapSort(Context context, ArrayList<ItemMapList> items, OnStartDragListener listener) {
        super();
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_map_sort, parent, false);
        return new ViewHolder(view);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ItemMapList item = items.get(position);

        holder.mapName.setText(item.getName());
        holder.mapLoc.setText(item.getLocation());

        holder.btnDrag.setOnTouchListener((view, motionEvent) -> {
            if(MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) {
                listener.onStartDrag(holder);
            }
            return false;
        });

    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(items, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mapName;
        final TextView mapLoc;
        final ImageView btnDrag;

        ViewHolder(View view) {
            super(view);
            mapName = view.findViewById(R.id.iMapSortName);
            mapLoc = view.findViewById(R.id.iMapSortLoc);
            btnDrag = view.findViewById(R.id.iMapSortDrag);

        }

    }


    @Override
    public int getItemCount() {
        return this.items.size();
    }


}
