package com.sscctv.seeeyesonvif.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Environment;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sscctv.seeeyesonvif.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileDialog {

    public static final String FILE_OPEN = "FileOpen";
    public static final String FILE_SAVE = "FileSave";
    public static final String FOLDER_CHOOSE = "FolderChoose";
    private final int FileOpen = 0;
    private final int FileSave = 1;
    private final int FolderChoose = 2;
    private final int selectType;
    private String mSdcardDirectory = "";
    private final Context mContext;
    private TextView mTitleVIew;
    public String defaultFileName = "log.txt";
    private String selectedFileName = defaultFileName;
    private EditText inputText;

    private String mDir = "";
    private List<String> stringList = null;
    private final FileDialogListener fileDialogListener;
    private ArrayAdapter<String> mListAdapter = null;
    private RadioButton[] rBtn;

    public interface FileDialogListener {
        void onChosenDir(boolean chosenList, String chosenDir);
    }

    public FileDialog(Context context, String file_select_type, FileDialogListener SimpleFileDialogListener) {
        switch (file_select_type) {
            case FILE_SAVE:
                selectType = FileSave;
                break;
            case FOLDER_CHOOSE:
                selectType = FolderChoose;
                break;
            default:
                selectType = FileOpen;
                break;
        }

        mContext = context;
        mSdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileDialogListener = SimpleFileDialogListener;

        try {
            mSdcardDirectory = new File(mSdcardDirectory).getCanonicalPath();
        } catch (IOException ignored) {
        }
    }

    public void chooseFileOrDir() {
        // Initial directory is sdcard directory
        if (mDir.equals("")) {
            chooseFileOrDir(mSdcardDirectory);
        } else {
            chooseFileOrDir(mDir);
        }
    }

    public void chooseFileOrDir(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            dir = mSdcardDirectory;
        }

        try {
            dir = new File(dir).getCanonicalPath();
        } catch (IOException ioe) {
            return;
        }

        mDir = dir;
        stringList = getDirectories(dir);

        class FileDialogOnClickListener implements DialogInterface.OnClickListener {
            public void onClick(DialogInterface dialog, int item) {
                String mDirOld = mDir;
                String sel = "" + ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
                if (sel.charAt(sel.length() - 1) == '/') sel = sel.substring(0, sel.length() - 1);

                // Navigate into the sub-directory
                if (sel.equals("..")) {
                    mDir = mDir.substring(0, mDir.lastIndexOf("/"));
                } else {
                    mDir += "/" + sel;
                }
                selectedFileName = defaultFileName;

                if ((new File(mDir).isFile())) // If the selection is a regular file
                {
                    mDir = mDirOld;
                    selectedFileName = sel;
                }

                updateDirectory();
            }
        }

        AlertDialog.Builder dialogBuilder = createDirectoryChooserDialog(dir, stringList,
                new FileDialogOnClickListener());

        dialogBuilder.setPositiveButton("저장", (dialog, which) -> {
            if (fileDialogListener != null) {
                {
                    if (selectType == FileOpen || selectType == FileSave) {
                        selectedFileName = inputText.getText() + "";
                        fileDialogListener.onChosenDir(rBtn[0].isChecked(),mDir + "/" + selectedFileName);
                    } else {
                        fileDialogListener.onChosenDir(rBtn[0].isChecked(),mDir);
                    }
                }
            }
        }).setNegativeButton("닫기", null);

        final AlertDialog dirsDialog = dialogBuilder.create();

        dirsDialog.show();
    }

    private boolean createSubDir(String newDir) {
        File newDirFile = new File(newDir);
        if (!newDirFile.exists()) return newDirFile.mkdir();
        else return false;
    }

    private List<String> getDirectories(String dir) {
        List<String> dirs = new ArrayList<String>();
        try {
            File dirFile = new File(dir);

            if (!mDir.equals(mSdcardDirectory)) {
                dirs.add("..");
            }

            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }

            for (File file : dirFile.listFiles()) {
                if (file.isDirectory()) {
                    dirs.add(file.getName() + "/");
                } else if (selectType == FileSave || selectType == FileOpen) {
                    dirs.add(file.getName());
                }
            }
        } catch (Exception ignored) {
        }

        Collections.sort(dirs, String::compareTo);
        return dirs;
    }

    private AlertDialog.Builder createDirectoryChooserDialog(String title, List<String> listItems,
                                                             DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        TextView mTextView = new TextView(mContext);
        mTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mTextView.setTextSize(18);
        mTextView.setTypeface(Typeface.DEFAULT_BOLD);
        mTextView.setPadding(12, 12, 12, 12);

        if (selectType == FileOpen) mTextView.setText("Open:");
        if (selectType == FileSave) mTextView.setText("저장할 폴더를 선택하세요.");
        if (selectType == FolderChoose) mTextView.setText("Folder Select:");

        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        mTextView.setBackgroundColor(mContext.getResources().getColor(R.color.DarkSlateGray)); // dark gray 	-12303292
        mTextView.setTextColor(mContext.getResources().getColor(android.R.color.white));

        LinearLayout titleLayout1 = new LinearLayout(mContext);
        titleLayout1.setBackgroundColor(mContext.getResources().getColor(R.color.Gainsboro)); // dark gray 	-12303292

        titleLayout1.setOrientation(LinearLayout.VERTICAL);
        titleLayout1.addView(mTextView);


        if (selectType == FolderChoose || selectType == FileSave) {
            Button newDirButton = new Button(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 8, 8, 8);

            newDirButton.setLayoutParams(params);
            newDirButton.setBackgroundResource(R.drawable.button_deepgray_ra10);
            newDirButton.setText("새 폴더 생성");
            newDirButton.setTextSize(16);
            newDirButton.setTextColor(mContext.getResources().getColor(android.R.color.white));

            newDirButton.setOnClickListener(v -> {
                        final EditText input = new EditText(mContext);

                        new AlertDialog.Builder(mContext).
                                setTitle("새 폴더 이름").
                                setView(input).setPositiveButton("예", (dialog, whichButton) -> {
                            Editable newDir = input.getText();
                            String newDirName = newDir.toString();
                            if (createSubDir(mDir + "/" + newDirName)) {
                                mDir += "/" + newDirName;
                                updateDirectory();
                            } else {
                                Toast.makeText(mContext, "Failed to create '"
                                        + newDirName + "' folder", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("닫기", null).show();
                    }
            );
            titleLayout1.addView(newDirButton);
        }

        rBtn = new RadioButton[2];
        RadioGroup rg = new RadioGroup(mContext); //create the RadioGroup

        rg.setOrientation(RadioGroup.VERTICAL);//or RadioGroup.VERTICAL
        rg.setPadding(12, 12, 12, 12);
        for (int i = 0; i < 2; i++) {
            rBtn[i] = new RadioButton(mContext);
            rBtn[i].setPadding(4, 8, 4, 8);

            rBtn[i].setTextSize(16);
            rBtn[i].setTypeface(Typeface.DEFAULT_BOLD);


            if (i == 0) {
                rBtn[i].setText("비상벨 호출 목록 저장");
            } else {
                rBtn[i].setText("일반 호출 목록 저장");
            }
            rBtn[0].setChecked(true);
            rBtn[i].setId(i + 100);
            rg.addView(rBtn[i]);
        }
        titleLayout1.addView(rg);

        LinearLayout titleLayout = new LinearLayout(mContext);

        titleLayout.setOrientation(LinearLayout.VERTICAL);

        mTitleVIew = new TextView(mContext);
        mTitleVIew.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mTitleVIew.setBackgroundColor(mContext.getResources().getColor(R.color.DeepGray));
        mTitleVIew.setTextSize(16);
        mTitleVIew.setPadding(12, 12, 12, 12);
        mTitleVIew.setTextColor(mContext.getResources().getColor(android.R.color.white));
        mTitleVIew.setGravity(Gravity.CENTER_VERTICAL);
        mTitleVIew.setText(title);

        titleLayout.addView(mTitleVIew);

        if (selectType == FileOpen || selectType == FileSave) {
            inputText = new EditText(mContext);
            inputText.setPadding(8, 8, 8, 8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            inputText.setLayoutParams(params);

            inputText.setSingleLine();
            inputText.setHint(defaultFileName);
            inputText.setHintTextColor(mContext.getResources().getColor(R.color.black));
            titleLayout.addView(inputText);
        }
        ///////////////////////////////////////
        // Set Views and Finish Dialog builder  //
        ///////////////////////////////////////
        dialogBuilder.setView(titleLayout);
        dialogBuilder.setCustomTitle(titleLayout1);
        mListAdapter = createListAdapter(listItems);
        dialogBuilder.setSingleChoiceItems(mListAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(false);
        return dialogBuilder;
    }

    private void updateDirectory() {
        stringList.clear();
        stringList.addAll(getDirectories(mDir));
        mTitleVIew.setText(mDir);
        mListAdapter.notifyDataSetChanged();
        if (selectType == FileSave || selectType == FileOpen) {
            inputText.setText(selectedFileName);
        }
    }

    private ArrayAdapter<String> createListAdapter(List<String> items) {
        return new ArrayAdapter<String>(mContext, R.layout.file_select_item, R.id.fileSelectItemText, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    tv.setEllipsize(null);
                }
                return v;
            }
        };
    }
}
