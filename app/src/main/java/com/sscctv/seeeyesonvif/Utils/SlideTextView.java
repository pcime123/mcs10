package com.sscctv.seeeyesonvif.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.sscctv.seeeyesonvif.R;

@SuppressLint("DrawAllocation")
public class SlideTextView extends View {
	private String showText = " Network Emergency Solution";

	private Paint paint;
	private int cycleNum = 0;

	private int firstLineOffset = 0;

	private int extraPaddingTop = PixValue.dip.valueOf(1f);

	private int textSize = PixValue.sp.valueOf(40);
	private int textColor = Color.WHITE;

	private int textWidth, textHeight;

	private String extraText, actualText;
	private int extraWidth;
	private float rangeArea;
	private float maxXDistance;
	private int darkColor;
	private Matrix trans;
	private boolean val = true;

	public SlideTextView(Context context) {
		this(context, null);
	}

	public SlideTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlideTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(textColor);
		paint.setTextSize(textSize);
		paint.setTextAlign(Align.LEFT);

		paint.setTypeface(ResourcesCompat.getFont(context, R.font.mapodpp));
		Rect rect = new Rect();
		paint.getTextBounds("Network", 0, 1, rect);
		firstLineOffset = (int) (rect.height() - rect.bottom) + extraPaddingTop;

		textWidth = (int) paint.measureText(showText);
		textHeight = textSize;
		extraText = "            ";
		extraWidth = (int) paint.measureText(extraText);
		rangeArea = (float) textWidth / (textWidth + extraWidth * 2) / 3;
		actualText = extraText + showText + extraText;
		maxXDistance = 1 + 2 * rangeArea;
		darkColor = Color.parseColor("#454545");
		trans = new Matrix();
		trans.setRotate(-90);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		new UIThread().start();
	}

	public void setTextSize(int val) {
		textSize = PixValue.sp.valueOf(val);
	}

	public void stop() {
		val = false;
	}
	@Override
	protected void onDraw(Canvas canvas) {
		int thisNum = 1 + cycleNum % circulationNum;

		float centerX = -rangeArea + maxXDistance * thisNum / circulationNum;
		Shader shader = new LinearGradient(0, 0, 0, textWidth, new int[] {
				darkColor, darkColor, Color.WHITE, darkColor, darkColor },
				new float[] { -1f, centerX - rangeArea, centerX,
						centerX + rangeArea, 2f }, TileMode.CLAMP);
		shader.setLocalMatrix(trans);
		paint.setShader(shader);
		canvas.drawText(actualText, -extraWidth + getPaddingLeft(),
				getPaddingTop() + firstLineOffset, paint);
	}

	private int circulationNum = 30;

	class UIThread extends Thread {

		public UIThread() {
			cycleNum = 0;
		}

		@Override
		public void run() {
			try {
				while (val) {
					sleep(40);
					if (cycleNum > 0 && cycleNum % circulationNum == 0) {
						sleep(40);
					}

					Message msg = uiHandler.obtainMessage();
					cycleNum++;
					msg.sendToTarget();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
	};

	private Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			invalidate();
		};
	};

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		setMeasuredDimension(textWidth + getPaddingLeft() + getPaddingRight(),
				textHeight + getPaddingTop() + getPaddingBottom());
	}
}
