package com.sscctv.seeeyesonvif.Fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Map.utils.AdapterPicFolder;
import com.sscctv.seeeyesonvif.Map.utils.AdapterPicImage;
import com.sscctv.seeeyesonvif.Map.utils.PicHolder;
import com.sscctv.seeeyesonvif.Map.utils.ImageFolder;
import com.sscctv.seeeyesonvif.Map.utils.ItemClickListener;
import com.sscctv.seeeyesonvif.Map.utils.ItemPictureList;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.databinding.FragmentGalleryBinding;
import com.sscctv.seeeyesonvif.Utils.JToastShow;
import com.sscctv.seeeyesonvif.Utils.KeyList;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class FragmentMapAdd extends Fragment implements ItemClickListener {
    private static final String TAG = FragmentMapAdd.class.getSimpleName();
    private FragmentGalleryBinding mBinding;
    private int pageStack;
    private AdapterPicFolder folderAdapter;
    private AdapterPicImage imageAdapter;
    private ArrayList<ImageFolder> folders;
    private ArrayList<ItemPictureList> imageList;
    private BroadcastReceiver mountReceiver;
    private final TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());

    public static FragmentMapAdd newInstance() {
        FragmentMapAdd fragment = new FragmentMapAdd();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
        getFolderList();
        setRemoveDone();
        startWatchingMount();


    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
        stopWatchingMount();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false);
        JToastShow jToastShow = new JToastShow();
        mBinding.btnPrev.setOnClickListener(view -> {
            getFolderList();
        });

        mBinding.btnClose.setOnClickListener(view -> {
            ((ActivityMain) ActivityMain.context).setGoNewFragment(new FragmentMapMain());
        });

        mBinding.btnRemove.setOnClickListener(view -> {
            if (getRemoveSelected(pageStack)) {
                goRemoveMode(pageStack);
                setRemoveDone();
            } else {
                jToastShow.createToast(getContext(), "삭제 실패", "선택된 항목이 없습니다.", jToastShow.TOAST_ERROR, 80, jToastShow.SHORT_DURATION, null);
            }
        });

        mBinding.btnRemoveList.setOnClickListener(view -> {
            setRemoveGo();
        });

        mBinding.btnDone.setOnClickListener(view -> {
            setRemoveDone();
        });

        return mBinding.getRoot();
    }

    private void setRemoveGo() {
        mBinding.layoutRemove.setVisibility(View.GONE);
        mBinding.layoutRemoveList.setVisibility(View.VISIBLE);
        tinyDB.putBoolean(KeyList.KEY_REMOVE_GALLERY, true);
        if (pageStack == 0) {
            folderAdapter.notifyDataSetChanged();
        } else {
            imageAdapter.notifyDataSetChanged();
        }
    }

    private void setRemoveDone() {
        mBinding.layoutRemove.setVisibility(View.VISIBLE);
        mBinding.layoutRemoveList.setVisibility(View.GONE);
        tinyDB.putBoolean(KeyList.KEY_REMOVE_GALLERY, false);
        if (pageStack == 0) {
            folderAdapter.notifyDataSetChanged();
        } else {
            imageAdapter.notifyDataSetChanged();
        }
    }

    private void goRemoveMode(int val) {
        if (val == 0) {
            int size = folders.size();

            for (int i = 0; i < size; i++) {
                ImageFolder item = folders.get(i);

                Log.d(TAG, "Remove: " + item.getFolderName() + " ," + item.isSelected());
                if (item.isSelected()) {
                    folders.remove(i);
                    size--;
                    i--;
                    setDirDelete(item.getPath());

                }
            }
            folderAdapter.notifyDataSetChanged();
        } else {
            int size = imageList.size();

            for (int i = 0; i < size; i++) {
                ItemPictureList item = imageList.get(i);

                if (item.isRemoveSel()) {
                    imageList.remove(i);
                    size--;
                    i--;
                    setDirDelete(item.getPicturePath());

                }
            }
            imageAdapter.notifyDataSetChanged();
        }
    }

    private boolean getRemoveSelected(int val) {
        if (val == 0) {
            for (int i = 0; i < folders.size(); i++) {
                if (folders.get(i).isSelected()) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < imageList.size(); i++) {
                if (imageList.get(i).isRemoveSel()) {
                    return true;
                }
            }
        }
        return false;

    }

    private void setDirDelete(String path) {
        File tempFile = new File(path);
        if (pageStack == 0) {
            File[] childTempFilesList = tempFile.listFiles();

            if (tempFile.exists()) {
                for (File childTempFile : Objects.requireNonNull(childTempFilesList)) {
                    if (childTempFile.isDirectory()) {
                        setDirDelete(childTempFile.getAbsolutePath());
                    } else {
                        childTempFile.delete();
                        MediaScannerConnection.scanFile(
                                getContext(),
                                new String[]{childTempFile.getAbsolutePath(), null},
                                null, null);
                    }
                }
                tempFile.delete();

            }
        } else {
            tempFile.delete();
            MediaScannerConnection.scanFile(
                    getContext(),
                    new String[]{tempFile.getAbsolutePath(), null},
                    null, null);
        }
    }

    private void getFolderList() {
        GridLayoutManager manager = new GridLayoutManager(getContext(), 4);
        mBinding.gListView.setLayoutManager(manager);
        mBinding.gListView.hasFixedSize();

        folders = getPicturePaths();
        if (folders.isEmpty()) {
            mBinding.empty.setVisibility(View.VISIBLE);
            folderAdapter = new AdapterPicFolder(new ArrayList<>(), getContext(), this);
        } else {
            mBinding.empty.setVisibility(View.GONE);
            int num = -1;
            ImageFolder imageFolder = null;
            for (int i = 0; i < folders.size(); i++) {
                if (folders.get(i).getFolderName().equals("MAP List")) {
                    if (i != 0) {
                        imageFolder = folders.get(i);
                        num = i;
                    }
                }
            }
//
            if (num != -1) {
                folders.add(0, imageFolder);
                folders.remove(num + 1);
            }

            folderAdapter = new AdapterPicFolder(folders, getContext(), this);
        }
        mBinding.gListView.setAdapter(folderAdapter);

    }


    @SuppressLint("InlinedApi")
    private ArrayList<ImageFolder> getPicturePaths() {
        mBinding.btnPrev.setVisibility(View.GONE);
        mBinding.titleGallery.setVisibility(View.VISIBLE);
        mBinding.titleGallery.setText("MAP IMAGE");
        pageStack = 0;
        ArrayList<ImageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = getContext().getContentResolver().query(allImagesUri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    ImageFolder folds = new ImageFolder();
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    String folder;
                    folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String dataPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String folderpaths = dataPath.substring(0, dataPath.lastIndexOf(folder + "/"));
                    folderpaths = folderpaths + folder + "/";
                    Log.d(TAG, "folderpaths: " + folderpaths);

                    if (!picPaths.contains(folderpaths)) {
                        picPaths.add(folderpaths);
                        folds.setPath(folderpaths);
                        folds.setFolderName(folder);
                        folds.setFirstPic(dataPath);
                        folds.addpics();
                        picFolders.add(folds);
                    } else {
                        for (int i = 0; i < picFolders.size(); i++) {
                            if (picFolders.get(i).getPath().equals(folderpaths)) {
                                picFolders.get(i).setFirstPic(dataPath);
                                picFolders.get(i).addpics();
                            }
                        }
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //reverse order ArrayList
       /* ArrayList<imageFolder> reverseFolders = new ArrayList<>();

        for(int i = picFolders.size()-1;i > reverseFolders.size()-1;i--){
            reverseFolders.add(picFolders.get(i));
        }*/

        return picFolders;
    }

    @Override
    public void onPicClicked(PicHolder holder, int position, ArrayList<ItemPictureList> pics) {
        FragmentPicture browser = FragmentPicture.newInstance(pics, position, getContext());
        browser.setEnterTransition(new Fade());
        browser.setExitTransition(new Fade());
        getFragmentManager()
                .beginTransaction()
                .addSharedElement(holder.picture, position + "picture")
                .add(R.id.display_container, browser)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {
        Log.d(TAG, "picPath: " + pictureFolderPath);
        imageList = getAllImagesByFolder(pictureFolderPath);
        imageAdapter = new AdapterPicImage(imageList, getContext(), this);
        mBinding.gListView.setAdapter(imageAdapter);
    }

    public ArrayList<ItemPictureList> getAllImagesByFolder(String path) {
        mBinding.titleGallery.setVisibility(View.GONE);
        mBinding.btnPrev.setVisibility(View.VISIBLE);
        pageStack = 1;

        ArrayList<ItemPictureList> images = new ArrayList<>();
        Uri allUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor cursor = getContext().getContentResolver().query(allUri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    ItemPictureList pic = new ItemPictureList();
                    pic.setPictureName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
                    pic.setPicturePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                    pic.setPictureSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
//                    pic.setImageUri(imageUri);
                    Log.d(TAG, "getPictureName: " + pic.getPictureName());
                    Log.d(TAG, "getPicturePath: " + pic.getPicturePath());
                    Log.d(TAG, "getPictureSize: " + pic.getPictureSize());
                    Log.d(TAG, "getPicturePath(URI): " + Uri.fromFile(new File(pic.getPicturePath())));

                    images.add(pic);
                } while (cursor.moveToNext());
                cursor.close();
                ArrayList<ItemPictureList> reSelection = new ArrayList<>();
                for (int i = images.size() - 1; i > -1; i--) {
                    reSelection.add(images.get(i));
                }
                images = reSelection;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    public void startWatchingMount() {
        if (mountReceiver == null) {
            mountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Log.v(TAG, "startWatchingViewerClose Broadcast : " + intent);
                    if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                        new Handler().postDelayed(() -> {
                            getFolderList();
                        }, 1000);
                    } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                        getFolderList();

                    }
                }
            };

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addDataScheme("file");
            getContext().registerReceiver(mountReceiver, filter);
        }
    }

    public void stopWatchingMount() {
        if (mountReceiver != null) {
            try {
                getContext().unregisterReceiver(mountReceiver);
            }catch (Exception e) {
            } finally {
            }
        }
    }
}
