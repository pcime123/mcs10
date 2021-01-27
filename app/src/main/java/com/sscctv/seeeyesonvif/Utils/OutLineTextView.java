package com.sscctv.seeeyesonvif.Utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import com.sscctv.seeeyesonvif.R;

public class OutLineTextView extends androidx.appcompat.widget.AppCompatTextView {

    private boolean stroke = false;
    private float strokeWidth = 0.0f;
    private int strokeColor;

    public OutLineTextView(Context context) {
        super(context);
    }

    public OutLineTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OutLineTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }


    private void initView(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OutLineTextView);
        stroke = a.getBoolean(R.styleable.OutLineTextView_textStroke, false);
        strokeWidth = a.getFloat(R.styleable.OutLineTextView_textStrokeWidth, 0.0f);
        strokeColor = a.getColor(R.styleable.OutLineTextView_textStrokeColor, 0xffffffff);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (stroke) {
            ColorStateList states = getTextColors();
            getPaint().setStyle(Paint.Style.STROKE);
            getPaint().setStrokeWidth(strokeWidth);
            setTextColor(strokeColor);
            super.onDraw(canvas);
            getPaint().setStyle(Paint.Style.FILL);
            setTextColor(states);
        }
        super.onDraw(canvas);
    }
}
