package com.sscctv.seeeyesonvif.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Items.ItemDevice;
import com.sscctv.seeeyesonvif.Items.ItemGroup;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.AppUtils;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.ArrayList;
import java.util.Objects;

public class AdapterModifyList extends RecyclerView.Adapter<AdapterModifyList.ViewHolder> {
    private static final String TAG = AdapterModifyList.class.getSimpleName();
    private static final String KEY = KeyList.LIST_DEVICE_CONFIG;

    private final Context context;
    private final ArrayList<ItemDevice> items;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());

    public AdapterModifyList(Context context, ArrayList<ItemDevice> items) {
        super();
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dev_modify, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemDevice item = items.get(position);
        holder.model.setText(item.getModel());
        holder.ip.setText(item.getIp());
        holder.mac.setText(item.getMac());
        holder.loc.setText(item.getLoc());
        holder.group.setText(item.getGroup());
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView model;
        private final TextView ip;
        private final TextView mac;
        private final TextView loc;
        private final TextView group;

        ViewHolder(View view) {
            super(view);
            model = view.findViewById(R.id.mModel);
            ip = view.findViewById(R.id.mIp);
            mac = view.findViewById(R.id.mMac);
            loc = view.findViewById(R.id.mLocation);
            group = view.findViewById(R.id.mGroup);
            ImageButton btnLoc = view.findViewById(R.id.mBtnLocation);

            btnLoc.setOnClickListener(view1 -> {
                int pos = getAdapterPosition();
                Log.d(TAG, "Position: " + pos);
                Dialog dialog = new Dialog(Objects.requireNonNull(context));
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_edit_list);

                final EditText editLoc = dialog.findViewById(R.id.dg_edit_loc);
                final Button apply = dialog.findViewById(R.id.dg_btn_loc);
                final AppCompatSpinner spinner = dialog.findViewById(R.id.dgListGroup);

//                editLoc.setHint("설치 장소 (최대 14자)");
                editLoc.setText(items.get(pos).getLoc());
                editLoc.setSelection(editLoc.getText().length());

                int setGroup = 0;
                ArrayList<ItemGroup> itemGroups = AppUtils.getGroupList(tinyDB, KeyList.LIST_GROUP_HEADER);
                ArrayList<String> groupList = new ArrayList<>();
                for (int i = 0; i < itemGroups.size(); i++) {
                    groupList.add(itemGroups.get(i).getGroup());
                    if (itemGroups.get(i).getGroup().equals(items.get(pos).getGroup())) {
                        setGroup = i;
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.spinner_normal, R.id.spinnerText, groupList);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown);
                spinner.setAdapter(adapter);
                spinner.setSelection(setGroup);

                apply.setOnClickListener(view2 -> {
                    items.get(pos).setGroup(spinner.getSelectedItem().toString());
                    items.get(pos).setLoc(editLoc.getText().toString());
                    notifyDataSetChanged();
                    AppUtils.putItemDevConfigList(tinyDB, KeyList.LIST_DEVICE_CONFIG, items);
                    dialog.dismiss();
                });

                dialog.show();
            });

        }
    }

}
