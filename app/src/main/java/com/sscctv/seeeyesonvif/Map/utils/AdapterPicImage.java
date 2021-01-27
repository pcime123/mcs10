package com.sscctv.seeeyesonvif.Map.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.util.ArrayList;

import static androidx.core.view.ViewCompat.setTransitionName;

/**
 * Author CodeBoy722
 * <p>
 * A RecyclerView Adapter class that's populates a RecyclerView with images from
 * a folder on the device external storage
 */
public class AdapterPicImage extends RecyclerView.Adapter<PicHolder> {

    private ArrayList<ItemPictureList> pictureList;
    private Context pictureContx;
    private final ItemClickListener picListerner;
    private TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());

    /**
     * @param pictureList  ArrayList of pictureFacer objects
     * @param pictureContx The Activities Context
     * @param picListerner An interface for listening to clicks on the RecyclerView's items
     */
    public AdapterPicImage(ArrayList<ItemPictureList> pictureList, Context pictureContx, ItemClickListener picListerner) {
        this.pictureList = pictureList;
        this.pictureContx = pictureContx;
        this.picListerner = picListerner;
    }

    @NonNull
    @Override
    public PicHolder onCreateViewHolder(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View cell = inflater.inflate(R.layout.item_gallery_img, container, false);
        return new PicHolder(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull final PicHolder holder, final int position) {

        final ItemPictureList image = pictureList.get(position);
        if (!tinyDB.getBoolean(KeyList.KEY_REMOVE_GALLERY)) {
            holder.imageChk.setVisibility(View.GONE);
        } else {
            holder.imageChk.setVisibility(View.VISIBLE);
        }

        holder.imageChk.setChecked(image.isRemoveSel());
        holder.imageChk.setTag(pictureList.get(position));
        holder.imageChk.setOnClickListener(view -> {
            Log.d("jinseop", "Click: " + holder.imageChk);
            CheckBox cb = (CheckBox) view;
            ItemPictureList extItem = (ItemPictureList) cb.getTag();
            extItem.setRemoveSel(cb.isChecked());
            pictureList.get(position).setRemoveSel(cb.isChecked());
            notifyDataSetChanged();
        });


        Glide.with(pictureContx)
                .load(image.getPicturePath())
                .apply(new RequestOptions().centerCrop())
                .into(holder.picture);

        setTransitionName(holder.picture, String.valueOf(position) + "_image");

//        if (holder.imageChk.getVisibility() == View.GONE) {
        holder.picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picListerner.onPicClicked(holder, position, pictureList);
            }
        });
        String text = "" + image.getPictureName();
        holder.folderName.setText(text);
        Log.d("Jinseop", "Uri: " + pictureList.get(position).getImageUri());
//        }

    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }

}
