package com.sscctv.seeeyesonvif.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sscctv.seeeyesonvif.Map.utils.CropDemoPreset;
import com.sscctv.seeeyesonvif.Map.utils.ViewPagerImageIndicator;
import com.sscctv.seeeyesonvif.Map.utils.ImageIndicatorListener;
import com.sscctv.seeeyesonvif.Map.utils.ItemPictureList;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

import static androidx.core.view.ViewCompat.setTransitionName;


/**
 * Author: CodeBoy722
 * <p>
 * this fragment handles the browsing of all images in an ArrayList of pictureFacer passed in the constructor
 * the images are loaded in a ViewPager an a RecyclerView is used as a pager indicator for
 * each image in the ViewPager
 */
public class FragmentPicture extends Fragment implements ImageIndicatorListener, CropImageView.OnSetImageUriCompleteListener, CropImageView.OnCropImageCompleteListener {
    private static final String TAG = FragmentPicture.class.getSimpleName();
    private ArrayList<ItemPictureList> allImages = new ArrayList<>();
    private int position;
    private Context animeContx;
    private ImageView image;
    private ViewPager imagePager;
    private RecyclerView indicatorRecycler;
    private int viewVisibilityController;
    private int viewVisibilityLooper;
    private ImagesPagerAdapter pagingImages;
    private int previousSelected = -1;

    private CropDemoPreset mDemoPreset;
    private CropImageView mCropImageView;

    private Uri imageUri;
    private TextView txtName;
    private TextView txtScale, txtShape, txtGuideLine, txtRatio, txtAutoZoom, txtMaxZoom, txtInitRect, txtResetRect, txtMultiTouch, txtOverlay, txtProgress;
    private Button btnRotate, btnCrop, btnHFlip, btnVFlip, btnClose;
    private boolean modeCrop;

    public FragmentPicture() {
    }

    public FragmentPicture(ArrayList<ItemPictureList> allImages, int imagePosition, Context anim) {
        this.allImages = allImages;
        this.position = imagePosition;
        this.animeContx = anim;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mCropImageView != null) {
            mCropImageView.setOnSetImageUriCompleteListener(null);
            mCropImageView.setOnCropImageCompleteListener(null);
        }
    }

    public static FragmentPicture newInstance(ArrayList<ItemPictureList> allImages, int imagePosition, Context anim) {
        FragmentPicture fragment = new FragmentPicture(allImages, imagePosition, anim);
        Log.d(TAG, "FragmentPicture newInstance");

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        Log.d(TAG, "onCreateView");
        mDemoPreset = CropDemoPreset.RECT;

        rootView = inflater.inflate(R.layout.fragment_picture_detail, container, false);
//        switch (mDemoPreset) {
//            case RECT:
//            case CUSTOM:
//                rootView = inflater.inflate(R.layout.fragment_main_rect, container, false);
//                break;
//            case CIRCULAR:
//                rootView = inflater.inflate(R.layout.fragment_main_oval, container, false);
//                break;
//            case CUSTOMIZED_OVERLAY:
//                rootView = inflater.inflate(R.layout.fragment_main_customized, container, false);
//                break;
//            case MIN_MAX_OVERRIDE:
//                rootView = inflater.inflate(R.layout.fragment_main_min_max, container, false);
//                break;
//            case SCALE_CENTER_INSIDE:
//                rootView = inflater.inflate(R.layout.fragment_main_scale_center, container, false);
//                break;
//            default:
//                throw new IllegalStateException("Unknown preset: " + mDemoPreset);
//        }
        txtName = rootView.findViewById(R.id.title_image);

        txtScale = rootView.findViewById(R.id.drawer_option_toggle_scale);
        txtShape = rootView.findViewById(R.id.drawer_option_toggle_shape);
        txtGuideLine = rootView.findViewById(R.id.drawer_option_toggle_guidelines);
        txtRatio = rootView.findViewById(R.id.drawer_option_toggle_aspect_ratio);
        txtAutoZoom = rootView.findViewById(R.id.drawer_option_toggle_auto_zoom);
        txtMaxZoom = rootView.findViewById(R.id.drawer_option_toggle_max_zoom);
        txtInitRect = rootView.findViewById(R.id.drawer_option_set_initial_crop_rect);
        txtResetRect = rootView.findViewById(R.id.drawer_option_reset_crop_rect);
        txtMultiTouch = rootView.findViewById(R.id.drawer_option_toggle_multitouch);
        txtOverlay = rootView.findViewById(R.id.drawer_option_toggle_show_overlay);
        txtProgress = rootView.findViewById(R.id.drawer_option_toggle_show_progress_bar);
        mCropImageView = rootView.findViewById(R.id.cropImageView);
        mCropImageView.setOnSetImageUriCompleteListener(this);
        mCropImageView.setOnCropImageCompleteListener(this);

        btnRotate = rootView.findViewById(R.id.btnRotate);
        btnRotate.setOnClickListener(view -> {
            mCropImageView.rotateImage(90);
        });

        btnCrop = rootView.findViewById(R.id.btnCrop);
        btnCrop.setOnClickListener(view -> {
//            mCropImageView.getCroppedImageAsync();
            if (mCropImageView.getCroppedImage() == null) {
                mCropImageView.setImageUriAsync(Uri.fromFile(new File(allImages.get(position).getPicturePath())));
                btnCrop.setText("Cancel");
            } else {
                mCropImageView.clearImage();
                btnCrop.setText("Crop");
            }
        });

        Button btnSave = rootView.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(view -> {
            if (mCropImageView.getCroppedImage() != null) {
                mCropImageView.getCroppedImageAsync();
            } else {
                ItemPictureList pic = allImages.get(position);
                Log.d(TAG, "What: " + pic.getPicturePath());
                saveDialog(getContext(), pic.getPicturePath());

            }
        });

        btnHFlip = rootView.findViewById(R.id.btnHorizonFlip);
        btnHFlip.setOnClickListener(view -> {
            mCropImageView.flipImageHorizontally();

        });

        btnVFlip = rootView.findViewById(R.id.btnVerticalFlip);
        btnVFlip.setOnClickListener(view -> {
            mCropImageView.flipImageVertically();

        });

        btnClose = rootView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(view -> {
            prevFragment();
        });

        return rootView;

    }


    private void saveDialog(Context context, String path) {

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_img_save);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final EditText name = dialog.findViewById(R.id.editBitmapName);

        final Button apply = dialog.findViewById(R.id.btnBitmapSave);
        apply.setOnClickListener(view -> {
            try {
                copy(path, name.getText().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
            prevFragment();

        });

        final Button cancel = dialog.findViewById(R.id.btnBitmapCancel);
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialog.show();

    }

    public void copy(String path, String name) throws IOException {
        String mPath = Environment.getExternalStorageDirectory().getAbsolutePath() + KeyList.ADMIN_FILE + KeyList.IMAGE_FILE;

        File dirFile = new File(mPath);
        if(!dirFile.exists()) {
            dirFile.mkdirs();
            Log.d(TAG, "파일 생성!!");
        }

        File mapFile = new File(mPath + name + ".png");

        try (InputStream in = new FileInputStream(path)) {
            try (OutputStream out = new FileOutputStream(mapFile)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
        getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mapFile)));

    }

    public void prevFragment() {
        Fragment curFragment = getFragmentManager().findFragmentById(R.id.display_container);
        Log.d(TAG, "Cur Fragment: " + curFragment);

        getFragmentManager().popBackStackImmediate();

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        viewVisibilityController = 0;
        viewVisibilityLooper = 0;

        imagePager = view.findViewById(R.id.imagePager);
        pagingImages = new ImagesPagerAdapter();
        imagePager.setAdapter(pagingImages);
        imagePager.setOffscreenPageLimit(3);
        imagePager.setCurrentItem(position);//displaying the image at the current position passed by the ImageDisplay Activity

        indicatorRecycler = view.findViewById(R.id.indicatorRecycler);
        indicatorRecycler.hasFixedSize();
        indicatorRecycler.setLayoutManager(new GridLayoutManager(getContext(), 1, RecyclerView.HORIZONTAL, false));
        ViewPagerImageIndicator indicatorAdapter = new ViewPagerImageIndicator(allImages, getContext(), this);
        indicatorRecycler.setAdapter(indicatorAdapter);

        //adjusting the recyclerView indicator to the current position of the viewPager, also highlights the image in recyclerView with respect to the
        //viewPager's position
        allImages.get(position).setSelected(true);
        previousSelected = position;
        indicatorAdapter.notifyDataSetChanged();
        indicatorRecycler.scrollToPosition(position);

        imagePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (previousSelected != -1) {
                    allImages.get(previousSelected).setSelected(false);
                }
                previousSelected = position;
                allImages.get(position).setSelected(true);
                Objects.requireNonNull(indicatorRecycler.getAdapter()).notifyDataSetChanged();
                indicatorRecycler.scrollToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        indicatorRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (viewVisibilityController == 0) {
                    indicatorRecycler.setVisibility(View.VISIBLE);
                    visibiling();
                } else {
                    viewVisibilityLooper++;
                }
                return false;
            }
        });

//        Log.w(TAG, "allImages.get(position).getPicturePath()): " + allImages.get(position).getPicturePath());
//

    }


    @Override
    public void onImageIndicatorClicked(int ImagePosition) {

        //the below lines of code highlights the currently select image in  the indicatorRecycler with respect to the viewPager position
        if (previousSelected != -1) {
            allImages.get(previousSelected).setSelected(false);
            previousSelected = ImagePosition;
            Objects.requireNonNull(indicatorRecycler.getAdapter()).notifyDataSetChanged();
        } else {
            previousSelected = ImagePosition;
        }
//        mCropImageView.setImageUriAsync(Uri.fromFile(new File(allImages.get(ImagePosition).getPicturePath())));
        imagePager.setCurrentItem(ImagePosition);
    }

    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        handleCropResult(result);
    }

    private void handleCropResult(CropImageView.CropResult result) {
        if (result.getError() == null) {
            DialogCropSave dialog = new DialogCropSave(Objects.requireNonNull(getContext()), result.getUri());
            DialogCropSave.mImage = mCropImageView.getCropShape() == CropImageView.CropShape.OVAL ? CropImage.toOvalBitmap(result.getBitmap()) : result.getBitmap();
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
//        if (error == null) {
//            Toast.makeText(getActivity(), "Image load successful", Toast.LENGTH_SHORT).show();
//        } else {
//            Log.e("AIC", "Failed to load image by URI", error);
//            Toast.makeText(getActivity(), "Image load failed: " + error.getMessage(), Toast.LENGTH_LONG)
//                    .show();
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            handleCropResult(result);
        }
    }

    /**
     * the imageViewPager's adapter
     */
    private class ImagesPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return allImages.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup containerCollection, int position) {
            LayoutInflater layoutinflater = (LayoutInflater) containerCollection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = Objects.requireNonNull(layoutinflater).inflate(R.layout.item_picture_pager, null);
            image = view.findViewById(R.id.image);

            setTransitionName(image, position + "picture");

            ItemPictureList pic = allImages.get(position);
            Glide.with(animeContx)
                    .load(pic.getPicturePath())
                    .apply(new RequestOptions().fitCenter())
                    .into(image);

            txtName.setText(allImages.get(position).getPictureName());
            image.setOnClickListener(v -> {

                if (indicatorRecycler.getVisibility() == View.GONE) {
                    indicatorRecycler.setVisibility(View.VISIBLE);
                } else {
                    indicatorRecycler.setVisibility(View.GONE);
                }
            });

            containerCollection.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup containerCollection, int position, Object view) {
            containerCollection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == ((View) object);
        }
    }

    /**
     * function for controlling the visibility of the recyclerView indicator
     */
    private void visibiling() {
        viewVisibilityController = 1;
        final int checker = viewVisibilityLooper;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (viewVisibilityLooper > checker) {
                    visibiling();
                } else {
                    indicatorRecycler.setVisibility(View.GONE);
                    viewVisibilityController = 0;

                    viewVisibilityLooper = 0;
                }
                Log.d(TAG, "checker: " + checker);

            }
        }, 4000);
    }

//    public void setCropImageViewOptions(CropImageViewOptions options) {
//        mCropImageView.setScaleType(options.scaleType);
//        mCropImageView.setCropShape(options.cropShape);
//        mCropImageView.setGuidelines(options.guidelines);
//        mCropImageView.setAspectRatio(options.aspectRatio.first, options.aspectRatio.second);
//        mCropImageView.setFixedAspectRatio(options.fixAspectRatio);
//        mCropImageView.setMultiTouchEnabled(options.multitouch);
//        mCropImageView.setShowCropOverlay(options.showCropOverlay);
//        mCropImageView.setShowProgressBar(options.showProgressBar);
//        mCropImageView.setAutoZoomEnabled(options.autoZoomEnabled);
//        mCropImageView.setMaxZoom(options.maxZoomLevel);
//        mCropImageView.setFlippedHorizontally(options.flipHorizontally);
//        mCropImageView.setFlippedVertically(options.flipVertically);
//    }
//
//    public void setInitialCropRect() {
//        mCropImageView.setCropRect(new Rect(100, 300, 500, 1200));
//    }
//
//    public void resetCropRect() {
//        mCropImageView.resetCropRect();
//    }
//
//    public void updateCurrentCropViewOptions() {
//        CropImageViewOptions options = new CropImageViewOptions();
//        options.scaleType = mCropImageView.getScaleType();
//        options.cropShape = mCropImageView.getCropShape();
//        options.guidelines = mCropImageView.getGuidelines();
//        options.aspectRatio = mCropImageView.getAspectRatio();
//        options.fixAspectRatio = mCropImageView.isFixAspectRatio();
//        options.showCropOverlay = mCropImageView.isShowCropOverlay();
//        options.showProgressBar = mCropImageView.isShowProgressBar();
//        options.autoZoomEnabled = mCropImageView.isAutoZoomEnabled();
//        options.maxZoomLevel = mCropImageView.getMaxZoom();
//        options.flipHorizontally = mCropImageView.isFlippedHorizontally();
//        options.flipVertically = mCropImageView.isFlippedVertically();
//
//        updateDrawerTogglesByOptions(options);
//    }
//
//    private void updateDrawerTogglesByOptions(CropImageViewOptions options) {
//        txtScale.setText(getResources().getString(R.string.drawer_option_toggle_scale, options.scaleType.name()));
//        txtShape.setText(getResources().getString(R.string.drawer_option_toggle_shape, options.cropShape.name()));
//        txtGuideLine.setText(getResources().getString(R.string.drawer_option_toggle_guidelines, options.guidelines.name()));
//        txtMultiTouch.setText(getResources().getString(R.string.drawer_option_toggle_multitouch, Boolean.toString(options.multitouch)));
//        txtOverlay.setText(getResources().getString(R.string.drawer_option_toggle_show_overlay, Boolean.toString(options.showCropOverlay)));
//        txtProgress.setText(getResources().getString(R.string.drawer_option_toggle_show_progress_bar, Boolean.toString(options.showProgressBar)));
//
//        String aspectRatio = "FREE";
//        if (options.fixAspectRatio) {
//            aspectRatio = options.aspectRatio.first + ":" + options.aspectRatio.second;
//        }
//        txtRatio.setText(getResources().getString(R.string.drawer_option_toggle_aspect_ratio, aspectRatio));
//        txtAutoZoom.setText(getResources().getString(R.string.drawer_option_toggle_auto_zoom, options.autoZoomEnabled ? "Enabled" : "Disabled"));
//        txtMaxZoom.setText(getResources().getString(R.string.drawer_option_toggle_max_zoom, String.valueOf(options.maxZoomLevel)));
//    }

}
