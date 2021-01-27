package com.sscctv.seeeyesonvif.__Not_Used;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.sscctv.seeeyesonvif.Activity.ActivityMain;
import com.sscctv.seeeyesonvif.Interfaces.DialogClickListener;
import com.sscctv.seeeyesonvif.R;
import com.sscctv.seeeyesonvif.Utils.TinyDB;

public class ServiceDialogCall extends Dialog {

    private static final String TAG = ServiceDialogCall.class.getSimpleName();

    private Context context;
    private TinyDB tinyDB = new TinyDB(ActivityMain.getAppContext());
    private boolean isRunning = false;
    private ImageButton decline, accept;
    private DialogClickListener listener;

    public ServiceDialogCall(@NonNull Context context, DialogClickListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        getWindow().setAttributes(layoutParams);
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_alram_call);


        decline = findViewById(R.id.dg_call_decline);
        accept = findViewById(R.id.dg_call_accept);

        decline.setOnClickListener(view -> {
            Log.d(TAG, "click decline");
            this.listener.onNegativeClick();
            dismiss();
        });

        accept.setOnClickListener(view -> {
            this.listener.onPositiveClick();
        });



    }


    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            assert imm != null;
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return true;
    }
}



