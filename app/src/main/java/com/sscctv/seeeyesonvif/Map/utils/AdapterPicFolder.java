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
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.ArrayList;

/**
 * Author CodeBoy722
 * <p>
 * An adapter for populating RecyclerView with items representing folders that contain images
 */
public class AdapterPicFolder extends RecyclerView.Adapter<AdapterPicFolder.FolderHolder> {

    private ArrayList<ImageFolder> folders;
    private Context folderContx;
    private ItemClickListener listenToClick;
    private TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());

    /**
     * @param folders     An ArrayList of String that represents paths to folders on the external storage that contain pictures
     * @param folderContx The Activity or fragment Context
     * @param listen      interFace for communication between adapter and fragment or activity
     */
    public AdapterPicFolder(ArrayList<ImageFolder> folders, Context folderContx, ItemClickListener listen) {
        this.folders = folders;
        this.folderContx = folderContx;
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
        final ImageFolder folder = folders.get(position);

        Glide.with(folderContx)
                .load(folder.getFirstPic())
                .apply(new RequestOptions().centerCrop())
                .into(holder.folderPic);

        //setting the number of images
        String text = "" + folder.getFolderName();
        String folderSizeString = "" + folder.getNumberOfPics() + " Media";
        holder.folderSize.setText(folderSizeString);
        holder.folderName.setText(text);

        holder.folderPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenToClick.onPicClicked(folder.getPath(), folder.getFolderName());
            }
        });

        if (text.equals("MAP List")) {
            holder.folderLayout.setBackgroundColor(ContextCompat.getColor(folderContx, R.color.gallery_map));
            holder.folderIcon.setImageDrawable(ContextCompat.getDrawable(folderContx, R.drawable.icon_map));
        } else {
            holder.folderLayout.setBackgroundColor(ContextCompat.getColor(folderContx, R.color.gallery_normal));
            holder.folderIcon.setImageDrawable(ContextCompat.getDrawable(folderContx, R.drawable.icon_folder));
        }

        holder.folderChk.setChecked(folder.isSelected());
        holder.folderChk.setTag(folders.get(position));
        holder.folderChk.setOnClickListener(view -> {
            CheckBox cb = (CheckBox) view;
            ImageFolder extItem = (ImageFolder) cb.getTag();
            extItem.setSelected(cb.isChecked());
            folders.get(position).setSelected(cb.isChecked());
            notifyDataSetChanged();
        });

        if (!tinyDB.getBoolean(KeyList.KEY_REMOVE_GALLERY)) {
            holder.folderChk.setVisibility(View.GONE);
        } else {
            holder.folderChk.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return folders.size();
    }


    public class FolderHolder extends RecyclerView.ViewHolder {
        ImageView folderPic;
        TextView folderName;
        //set textview for foldersize
        TextView folderSize;
        ImageView folderIcon;
        CheckBox folderChk;
        LinearLayout folderLayout;

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
