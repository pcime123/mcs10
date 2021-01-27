package com.sscctv.seeeyesonvif.Map.utils;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sscctv.seeeyesonvif.R;


/**
 *Author CodeBoy722
 *
 * picture_Adapter's ViewHolder
 */

public class PicHolder extends RecyclerView.ViewHolder{

    public ImageView picture;
    TextView folderName;
    TextView folderSize;
    CheckBox imageChk;

    PicHolder(@NonNull View itemView) {
        super(itemView);

        picture = itemView.findViewById(R.id.imagePic);
        folderName = itemView.findViewById(R.id.folderName);
        folderSize=itemView.findViewById(R.id.folderSize);
        imageChk = itemView.findViewById(R.id.imageChk);
    }
}
