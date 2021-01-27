// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.sscctv.seeeyesonvif.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.KeyList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public final class DialogCropSave extends Dialog {
    private static final String TAG = DialogCropSave.class.getSimpleName();

    /**
     * The image to show in the activity.
     */
    static Bitmap mImage;

    private ImageView imageView;
    private final Context context;
    private int sampleSize;
    private Uri imageUri;

    public DialogCropSave(@NonNull Context context,  Uri imageUri) {
        super(context);
        this.context = context;
        this.imageUri = imageUri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Dialog View Settings
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        if (getWindow() != null) {
            getWindow().setAttributes(layoutParams);
            getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }


        setContentView(R.layout.dialog_crop_save);

        Log.d(TAG, "Uri: " + imageUri);
        imageView = findViewById(R.id.resultImageView);
        imageView.setBackgroundResource(R.drawable.backdrop);

        if (mImage != null) {
            imageView.setImageBitmap(mImage);
        } else {
            if (imageUri != null) {
                imageView.setImageURI(imageUri);
            }
        }

        EditText fileName = findViewById(R.id.editBitmapName);

        Button btnSave = findViewById(R.id.btnBitmapSave);
        btnSave.setOnClickListener(view -> {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.IMAGE_FILE;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }

            File mapFile = new File(path + fileName.getText() + ".png");
            OutputStream out = null;

            try {
                mapFile.createNewFile();
                out = new FileOutputStream(mapFile);
                mImage.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mapFile)));
            releaseBitmap();
            dismiss();
            ((ActivityMain) ActivityMain.context).setGoFragment(new FragmentMapAdd());

        });

        Button btnCancel = findViewById(R.id.btnBitmapCancel);
        btnCancel.setOnClickListener(view -> {
            releaseBitmap();
            dismiss();
        });
    }

    @Override
    public void onBackPressed() {
        releaseBitmap();
        super.onBackPressed();
    }

    private void releaseBitmap() {
        if (mImage != null) {
            mImage.recycle();
            mImage = null;
        }
    }
}
