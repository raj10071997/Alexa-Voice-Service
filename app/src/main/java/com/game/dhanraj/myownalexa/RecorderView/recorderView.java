package com.game.dhanraj.myownalexa.RecorderView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.game.dhanraj.myownalexa.R;

/**
 * Created by Dhanraj on 09-06-2017.
 */

public class recorderView extends View {

    Paint backgroundPaint;
    Paint wavepaint;

    int width = 0;
    int height = 0;
    int min = 0;
    int imageSize;

    private float rmsdbLevel = 0;

    Drawable microphone;

    public recorderView(Context context) {
        super(context);
        init();
    }

    public recorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public recorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public recorderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        backgroundPaint = new Paint();
        backgroundPaint.setColor(0x66000000);
        backgroundPaint.setStyle(Paint.Style.FILL);

        wavepaint = new Paint();
        wavepaint.setColor(0xff3F51B5);
        wavepaint.setAntiAlias(true);
        wavepaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(width / 2, height / 2, getRadius(), wavepaint);

        if(microphone == null){
            microphone = ContextCompat.getDrawable(getContext(), R.drawable.microphone);
            microphone.setFilterBitmap(true);
            microphone.setBounds((width - imageSize) / 2, (height - imageSize) / 2, width - ((width - imageSize) / 2), height - ((height - imageSize) / 2));
        }

        microphone.draw(canvas);
    }

 /*   @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }*/

    private float getRadius(){
        float percent = (float) (rmsdbLevel * Math.log(rmsdbLevel)) * .01f;
        percent = Math.min(Math.max(percent, 0f), 1f);
        percent = .55f  + .45f * percent;
        return percent * ((float) min) / 2f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = MeasureSpec.getSize((widthMeasureSpec));
        height = MeasureSpec.getSize(heightMeasureSpec);

        min = Math.min(width,height);

        imageSize = (int)(min*0.45);
        setRmsdbLevel(1);
    }

    public  void setRmsdbLevel(float level){
        rmsdbLevel = level;
        postInvalidate();
    }
}
