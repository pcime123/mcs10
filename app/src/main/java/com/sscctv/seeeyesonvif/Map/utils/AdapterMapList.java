package com.sscctv.seeeyesonvif.Map.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.List;

public class AdapterMapList extends RecyclerView.Adapter<AdapterMapList.ViewHolder> {
    private static final String TAG = AdapterMapList.class.getSimpleName();
    private final List<ItemMapList> items;
    private final Context context;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private MapClickListener listener;
    public AdapterMapList(Context context, List<ItemMapList> items, MapClickListener listener) {
        super();
        this.context = context;
        this.items = items;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cell = inflater.inflate(R.layout.item_map_list, parent, false);
        return new ViewHolder(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ItemMapList item = items.get(position);

        Glide.with(context)
                .load(item.getImgPath())
                .apply(new RequestOptions().centerCrop())
                .into(holder.mapImg);

        holder.mapName.setText(item.getName());
        holder.mapLocation.setText(item.getLocation());

        holder.mapImg.setOnClickListener(view -> {
            Log.d(TAG, "Click Img");
            listener.ClickMapConfig(items.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mapImg;
        private CheckBox mapChk;
        private TextView mapName;
        private TextView mapLocation;

        ViewHolder(View view) {
            super(view);
            mapImg = view.findViewById(R.id.itemMapImg);
            mapChk = view.findViewById(R.id.itemMapChk);
            mapName = view.findViewById(R.id.itemMapName);
            mapLocation = view.findViewById(R.id.itemMapLocation);
        }


    }



}
