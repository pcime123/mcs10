package com.sscctv.seeeyesonvif.Utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.sscctv.seeeyesonvif.R;

public class JToastShow {

    public JToastShow() {
        resetToastColor();
    }

    public long LONG_DURATION = 5000L;
    public long SHORT_DURATION = 2000L;

    public String TOAST_SUCCESS = "SUCCESS";
    public String TOAST_ERROR = "FAILED";
    public String TOAST_WARNING = "WARNING";
    public String TOAST_INFO = "INFO";
    public String TOAST_DELETE = "DELETE";
    public String TOAST_NO_INTERNET = "NO INTERNET";

    public int successToast, errorToast, waringToast, infoToast, deleteToast;
    public int backSuccess, backError, backWarning, backInfo, backDelete;

    public void resetToastColor() {
        successToast = R.color.success_color;
        errorToast = R.color.error_color;
        waringToast = R.color.warning_color;
        infoToast = R.color.info_color;
        deleteToast = R.color.delete_color;

        backSuccess = R.color.success_bg_color;
        backError = R.color.error_bg_color;
        backWarning = R.color.warning_bg_color;
        backInfo = R.color.info_bg_color;
        backDelete = R.color.delete_bg_color;
    }


    public void setSuccessToast(int color) {
        successToast = color;
    }

    public void setBackSuccess(int color) {
        backSuccess = color;
    }

    public void createToast(Context context, String title, String msg, String style, int position, long duration, Typeface font) {
        View toastLayout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.motion_toast, null);
        ImageView toastIcon = toastLayout.findViewById(R.id.custom_toast_image);
        TextView toastText = toastLayout.findViewById(R.id.custom_toast_text);
        TextView toastContent = toastLayout.findViewById(R.id.custom_toast_description);

        if (style.equals(TOAST_SUCCESS)) {
            toastIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_green));
            toastIcon.setColorFilter(ContextCompat.getColor(context, successToast));
            setBackgroundAndFilter(R.drawable.toast_round_background, backSuccess, toastLayout, context);
//            toastText.setTextColor(ContextCompat.getColor(context, successToast));
        } else if(style.equals(TOAST_ERROR)) {
            toastIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_error_));
            toastIcon.setColorFilter(ContextCompat.getColor(context, errorToast));
            setBackgroundAndFilter(R.drawable.toast_round_background, backError, toastLayout, context);
        } else if(style.equals(TOAST_WARNING)) {
            toastIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_warning_yellow));
            toastIcon.setColorFilter(ContextCompat.getColor(context, waringToast));
            setBackgroundAndFilter(R.drawable.toast_round_background, backWarning, toastLayout, context);
        } else if(style.equals(TOAST_INFO)) {
            toastIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_info_blue));
            toastIcon.setColorFilter(ContextCompat.getColor(context, infoToast));
            setBackgroundAndFilter(R.drawable.toast_round_background, backInfo, toastLayout, context);
        } else if(style.equals(TOAST_DELETE)) {
            toastIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_delete_));
            toastIcon.setColorFilter(ContextCompat.getColor(context, deleteToast));
            setBackgroundAndFilter(R.drawable.toast_round_background, backDelete, toastLayout, context);
        }

        startPulseAnimation(context, toastIcon);

        if (!title.isEmpty()) {
            toastText.setText(title);
        } else {
            toastText.setText("Title Empty");
        }

        if (!msg.isEmpty()) {
            toastContent.setText(msg);
        } else {
            toastContent.setText("Content Empty");
        }

        toastText.setTextColor(ContextCompat.getColor(context, R.color.white));
        toastContent.setTextColor(ContextCompat.getColor(context, R.color.WhiteSmoke));

        Toast toast = new Toast(context);
        startTimer(duration, toast);
        setGravity(position, toast);
        toast.setView(toastLayout);
        toast.show();
    }

    private void startTimer(long duration, Toast toast) {
        CountDownTimer timer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long l) {
                // do noghint
            }

            @Override
            public void onFinish() {
                toast.cancel();
            }
        }.start();
    }

    private void setGravity(int pos, Toast toast) {
        if (pos == Gravity.BOTTOM) {
            toast.setGravity(pos, 0, 100);
        } else {
            toast.setGravity(pos, 0, 0);
        }
    }

    private void startPulseAnimation(Context context, View layout) {
        Animation pulse = AnimationUtils.loadAnimation(context, R.anim.animation);
        layout.startAnimation(pulse);
    }

    private void setBackgroundAndFilter(@DrawableRes int background, @ColorRes int colorFilter, View layout, Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, background);
        PorterDuffColorFilter colorFilter1 = new PorterDuffColorFilter(ContextCompat.getColor(context, colorFilter), PorterDuff.Mode.MULTIPLY);
        drawable.setColorFilter(colorFilter1);
        layout.setBackground(drawable);
    }
}
