package com.sscctv.seeeyesonvif.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Interfaces.OnSelectEmDevice;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.ModelList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.List;

public class AdapterDeviceList extends RecyclerView.Adapter<AdapterDeviceList.ViewHolder> {
        private static final String TAG = AdapterDeviceList.class.getSimpleName();
    private final List<ItemDevice> items;
    private final Context context;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private final OnSelectEmDevice listener;
    private String rate;

    public AdapterDeviceList(Context context, List<ItemDevice> items, OnSelectEmDevice listener) {
        super();
        this.context = context;
        this.items = items;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tile_deivce, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int pos = position;

        ItemDevice item = items.get(position);
        if (!tinyDB.getBoolean(KeyList.KEY_REMOVE_LIST)) {
            holder.checkBox.setVisibility(View.GONE);
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
        }

//        Log.d(TAG, "Adapter Rate: " + item.getRate());
        if (item.getRate() != null) {
            rate = item.getRate();

            holder.icon.setImageResource(AppUtils.modelIconImage(item.getModel()));
//            if (ModelList.ECS30CW.equals(item.getModel())) {
//                holder.icon.setImageResource(R.drawable.icon_ecs30cw);
//            } else if (ModelList.ECS30CWP.equals(item.getModel())) {
//                holder.icon.setImageResource(R.drawable.icon_ecs30cwp);
//            } else if (ModelList.ECS30CMDCS.equals(item.getModel()) || ModelList.DCS01C.equals(item.getModel())) {
//                holder.icon.setImageResource(R.drawable.icon_ecs30cmdcs);
//            } else if (ModelList.ECS30NMDCS.equals(item.getModel()) || ModelList.DCS01E.equals(item.getModel()) || ModelList.DCS01N.equals(item.getModel())) {
//                holder.icon.setImageResource(R.drawable.icon_ecs30nmdcs);
//            } else {
//                holder.icon.setImageResource(R.drawable.icon_none);
//            }

            if (rate.equals("Error")) {
                holder.rate.setTextColor(ContextCompat.getColor(context, R.color.red));
                holder.layout.setBackground(ContextCompat.getDrawable(context, R.drawable.rectangle_stroke_red));
            } else {
                Log.d(TAG, " Test: " + holder.layout.getBackground());
                holder.rate.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.layout.setBackground(ContextCompat.getDrawable(context, R.drawable.rectangle_stroke_blue));
            }

            holder.rate.setText(item.getRate());
        } else {
            holder.rate.setText(R.string.wait);
            holder.layout.setBackground(ContextCompat.getDrawable(context, R.drawable.rectangle_stroke_gray));
        }

        holder.model.setText(item.getModel());
        holder.ip.setText(item.getIp());
        holder.loc.setText(item.getLoc());

        holder.checkBox.setChecked(item.isSelected());
        holder.checkBox.setTag(items.get(position));
        holder.checkBox.setOnClickListener(view -> {
            CheckBox cb = (CheckBox) view;
            ItemDevice extItem = (ItemDevice) cb.getTag();
            extItem.setSelected(cb.isChecked());
            items.get(pos).setSelected(cb.isChecked());
            notifyDataSetChanged();
        });

//        holder.itemView.setOnClickListener(view -> {
//            if (tinyDB.getBoolean(KeyList.KEY_REMOVE_LIST)) {
//                holder.checkBox.performClick();
//            } else {
//                listener.onSelectList(item);
//            }
//        });

        holder.itemView.setOnTouchListener((view, motionEvent) -> {
            Log.d("HI", "Rate: "  + holder.rate.getText() + " Action: " + motionEvent.getAction());
            if (!holder.rate.getText().equals("Error")) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (tinyDB.getBoolean(KeyList.KEY_REMOVE_LIST)) {
                        holder.checkBox.performClick();
                    } else {
                        holder.layout.setBackground(ContextCompat.getDrawable(context, R.drawable.rectangle_stroke_darkblue));
                        listener.onSelectList(item);
                    }
                } else {
                    if (rate.equals("Error")) {
                        holder.layout.setBackground(ContextCompat.getDrawable(context, R.drawable.rectangle_stroke_red));
                    } else {
                        holder.layout.setBackground(ContextCompat.getDrawable(context, R.drawable.rectangle_stroke_blue));
                    }
                }
            } else {

            }

            return false;
        });
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox checkBox;
        private final ImageView icon;
        private final TextView model;
        private final TextView rate;
        private final TextView ip;
        private final TextView loc;
        private final LinearLayout layout;

        ViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.dev_layout);
            checkBox = view.findViewById(R.id.dev_chk);
            model = view.findViewById(R.id.dev_model);
            icon = view.findViewById(R.id.dev_icon);
            rate = view.findViewById(R.id.dev_rate);
            ip = view.findViewById(R.id.dev_ip);
            loc = view.findViewById(R.id.dev_loc);

        }


//    public void clear() {
//        int size = items.size();
//        if (size > 0) {
//            items.subList(0, size).clear();
//            notifyItemRangeRemoved(0, size);
//            AppUtils.putItemDevConfigList(tinyDB, KEY, (ArrayList<ItemDevPing>) items);
//            notifyDataSetChanged();
//        }
//    }


        public void delete(int position) {

            try {
                items.remove(position);
                notifyItemRemoved(position);
            } catch (IndexOutOfBoundsException ex) {

                ex.printStackTrace();

            }

        }


    }
}
