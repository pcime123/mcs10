package com.sscctv.seeeyesonvif.Map.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.ModelList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.List;

public class AdapterMapDeivce extends RecyclerView.Adapter<AdapterMapDeivce.ViewHolder> {
    private static final String TAG = AdapterMapDeivce.class.getSimpleName();
    private final List<ItemMapConfig> items;
    private final Context context;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private Toast toast = null;

    public AdapterMapDeivce(Context context, List<ItemMapConfig> items) {
        super();
        this.context = context;
        this.items = items;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_map_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int pos = position;

        ItemMapConfig item = items.get(position);
        holder.mapModel.setText(item.getModel());
        holder.mapIcon.setImageResource(AppUtils.modelIconImage(item.getModel()));
//
//        switch (item.getModel()) {
//            case ModelList.ECS30CWP:
//                holder.mapIcon.setImageResource(R.drawable.icon_ecs30cwp);
//                break;
//            case ModelList.ECS30CMDCS:
//            case ModelList.DCS01C:
//                holder.mapIcon.setImageResource(R.drawable.icon_ecs30cmdcs);
//                break;
//            case ModelList.ECS30NMDCS:
//            case ModelList.DCS01E:
//            case ModelList.DCS01N:
//                holder.mapIcon.setImageResource(R.drawable.icon_ecs30nmdcs);
//                break;
//
//            default:
//                holder.mapIcon.setImageResource(R.drawable.icon_ecs30cw);
//                break;
//        }

        holder.mapMac.setText(item.getMac());
        holder.mapIp.setText(item.getIp());
        holder.mapGroup.setText(item.getGroup());
        holder.mapLoc.setText(item.getLoc());

        holder.chkBox.setChecked(item.isSelected());
        holder.chkBox.setTag(items.get(position));
        holder.chkBox.setOnClickListener(view -> {
            CheckBox cb = (CheckBox) view;
            ItemMapConfig extItem = (ItemMapConfig) cb.getTag();
            extItem.setSelected(cb.isChecked());
            items.get(position).setSelected(cb.isChecked());
            notifyDataSetChanged();
        });

        holder.itemView.setOnClickListener(view -> {
            holder.chkBox.performClick();
        });

    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox chkBox;
        private ImageView mapIcon;
        private TextView mapModel;
        private TextView mapMac;
        private TextView mapIp;
        private TextView mapGroup;
        private TextView mapLoc;


        ViewHolder(View view) {
            super(view);
            chkBox = view.findViewById(R.id.itemChkBox);
            mapIcon = view.findViewById(R.id.itemMapIcon);
            mapModel = view.findViewById(R.id.itemMapModel);
            mapMac = view.findViewById(R.id.itemMapMac);
            mapIp = view.findViewById(R.id.itemMapIp);
            mapGroup = view.findViewById(R.id.itemMapGroup);
            mapLoc = view.findViewById(R.id.itemMapLoc);
        }


    }



}
