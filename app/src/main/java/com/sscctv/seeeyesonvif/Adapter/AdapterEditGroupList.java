package com.sscctv.seeeyesonvif.Adapter;

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


public class AdapterEditGroupList extends RecyclerView.Adapter<AdapterEditGroupList.ViewHolder>
implements EditGroupItemTouchCallback.OnItemMoveListener {
    private static final String TAG = AdapterEditGroupList.class.getSimpleName();
    private static final String KEY = KeyList.LIST_DEVICE_GROUP;
    public static final int HEADER = AppUtils.GROUP_HEADER;
    public static final int CHILD = AppUtils.GROUP_CHILD;

    public interface OnStartDragListener {
        void onStartDrag(ViewHolder holder);
    }

    private ArrayList<ItemGroup> items;
    private final Context context;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private final OnStartDragListener listener;
    public AdapterEditGroupList(Context context, ArrayList<ItemGroup> items, OnStartDragListener listener) {
        super();
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_group, parent, false);
        return new ViewHolder(view);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ItemGroup item = items.get(position);

        holder.name.setText(item.getGroup());
        holder.checkBox.setChecked(item.isSelected());
        holder.checkBox.setTag(items.get(position));
        holder.checkBox.setOnClickListener(view -> {
            CheckBox cb = (CheckBox) view;
            ItemGroup extItem = (ItemGroup) cb.getTag();
            extItem.setSelected(cb.isChecked());
            items.get(position).setSelected(cb.isChecked());
            notifyDataSetChanged();
        });

        holder.btnDrag.setOnTouchListener((view, motionEvent) -> {
            if(MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) {
                listener.onStartDrag(holder);
            }
            return false;
        });

        holder.itemView.setOnClickListener(view -> {
            holder.checkBox.performClick();
        });
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(items, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        AppUtils.putGroupList(tinyDB, KeyList.LIST_GROUP_HEADER, items);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final CheckBox checkBox;
        final ImageView btnEdit;
        final ImageView btnDrag;

        ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.edit_group_check);
            name = view.findViewById(R.id.edit_group_name);
            btnEdit = view.findViewById(R.id.edit_group_btn);
            btnEdit.setOnClickListener(view1 -> {
                int pos = getAdapterPosition();
                Dialog dialog = new Dialog(Objects.requireNonNull(context));
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_edit_list);

                final TextView title = dialog.findViewById(R.id.dialog_title);
                final TextView subTitle = dialog.findViewById(R.id.dg_edit_title);
                final LinearLayout groupLayout = dialog.findViewById(R.id.layout_group);
                final EditText editLoc = dialog.findViewById(R.id.dg_edit_loc);
                final Button apply = dialog.findViewById(R.id.dg_btn_loc);

                groupLayout.setVisibility(View.GONE);
                title.setText(R.string.change_group_name);
                subTitle.setText(R.string.name);
                String prevGroup = items.get(pos).getGroup();
                editLoc.setText(prevGroup);
                editLoc.setSelection(editLoc.getText().length());

                apply.setOnClickListener(view2 -> {
                    String newGroup = editLoc.getText().toString();
                    items.get(pos).setGroup(newGroup);
                    notifyDataSetChanged();
                    AppUtils.putGroupList(tinyDB, KeyList.LIST_GROUP_HEADER, items);

                    ArrayList<ItemDevice> temp = AppUtils.getItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG);
                    for(int i = 0; i<temp.size(); i++) {
                        if(temp.get(i).getGroup().equals(prevGroup)) {
                            temp.get(i).setGroup(newGroup);
                        }
                    }
                    AppUtils.putItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG, temp);
                    dialog.dismiss();
                });

                dialog.show();
            });
            btnDrag = view.findViewById(R.id.edit_group_drag);

        }

    }


    @Override
    public int getItemCount() {
        return this.items.size();
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
