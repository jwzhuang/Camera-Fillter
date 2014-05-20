package com.kingnez.watermark.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by kingnez on 5/20/14.
 */
public class WatermarkView extends WebView {

    private int mScaleInPercent = -1;

    public WatermarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(0x00000000);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mScaleInPercent == -1) {
            mScaleInPercent = (int) (100.0 * getMeasuredWidth() / 640);
            setInitialScale(mScaleInPercent);
        }
    }
}
