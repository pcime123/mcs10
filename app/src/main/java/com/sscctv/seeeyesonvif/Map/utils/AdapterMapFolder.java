package com.sscctv.seeeyesonvif.Map.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.ArrayList;

/**
 * Author CodeBoy722
 * <p>
 * An adapter for populating RecyclerView with items representing folders that contain images
 */
public class AdapterMapFolder extends RecyclerView.Adapter<AdapterMapFolder.FolderHolder> {

    private ArrayList<ItemPictureList> folders;
    private Context context;
    private ItemClickListener listenToClick;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());

    public AdapterMapFolder(ArrayList<ItemPictureList> folders, Context context, ItemClickListener listen) {
        this.folders = folders;
        this.context = context;
        this.listenToClick = listen;
    }

    @NonNull
    @Override
    public FolderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cell = inflater.inflate(R.layout.item_gallery_folder, parent, false);
        return new FolderHolder(cell);

    }

    @Override
    public void onBindViewHolder(@NonNull FolderHolder holder, int position) {
        final ItemPictureList folder = folders.get(position);

        Glide.with(context)
                .load(folder.getPicturePath())
                .apply(new RequestOptions().centerCrop())
                .into(holder.folderPic);

        String text = "" + folder.getPictureName();
        holder.folderName.setText(text);

        holder.folderPic.setOnClickListener(v -> listenToClick.onPicClicked(folder.getPicturePath(), folder.getPictureName()));

        holder.folderLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.gallery_normal));
        holder.folderIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_map));
        holder.folderChk.setVisibility(View.GONE);

    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public static class FolderHolder extends RecyclerView.ViewHolder {
        LinearLayout folderLayout;
        ImageView folderIcon;
        ImageView folderPic;
        TextView folderName;
        TextView folderSize;
        CheckBox folderChk;

        public FolderHolder(@NonNull View itemView) {
            super(itemView);
            folderLayout = itemView.findViewById(R.id.folderLayout);
            folderIcon = itemView.findViewById(R.id.folderIcon);
            folderPic = itemView.findViewById(R.id.folderPic);
            folderName = itemView.findViewById(R.id.folderName);
            folderSize = itemView.findViewById(R.id.folderSize);
            folderChk = itemView.findViewById(R.id.folderChk);
        }
    }

}
