package com.sscctv.seeeyesonvif.Map.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import static android.widget.Toast.makeText;

public class AdapterMapConfig extends RecyclerView.Adapter<AdapterMapConfig.ViewHolder> {
    private static final String TAG = AdapterMapConfig.class.getSimpleName();
    private final List<ItemMapConfig> items;
    private final Context context;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private Toast toast = null;

    public AdapterMapConfig(Context context, List<ItemMapConfig> items) {
        super();
        this.context = context;
        this.items = items;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_map_config, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int pos = position;

        ItemMapConfig item = items.get(position);
        holder.mapModel.setText(item.getModel());
        holder.mapIp.setText(item.getIp());
        holder.mapLoc.setText(item.getLoc());
        holder.mapMac.setText(item.getMac());
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

        if(item.isTagged()) {
            holder.mapTag.setImageResource(R.drawable.icon_way_on);
        } else {
            holder.mapTag.setImageResource(R.drawable.icon_way_off);
        }

        holder.mapTag.setOnClickListener(view -> {
            if(item.isTagged()) {
                holder.mapTag.setImageResource(R.drawable.icon_way_off);
                item.setTagged(false);
            } else {
                holder.mapTag.setImageResource(R.drawable.icon_way_on);
                item.setTagged(true);
            }
            notifyDataSetChanged();
        });

        holder.mapRemove.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("확인").setMessage("선택한 장비를 목록에서 삭제하시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        items.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, items.size());
//                        notifyDataSetChanged();
                    }
                });


            builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });


            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mapIcon;
        private TextView mapModel;
        private TextView mapIp;
        private TextView mapLoc;
        private TextView mapMac;
        private ImageView mapTag;
        private ImageView mapRemove;

        ViewHolder(View view) {
            super(view);
            mapIcon = view.findViewById(R.id.itemMapIcon);
            mapModel = view.findViewById(R.id.itemMapModel);
            mapMac = view.findViewById(R.id.itemMapMac);
            mapIp = view.findViewById(R.id.itemMapIp);
            mapLoc = view.findViewById(R.id.itemMapLoc);
            mapTag = view.findViewById(R.id.itemMapTag);
            mapRemove = view.findViewById(R.id.itemMapRemove);

        }


    }



}
