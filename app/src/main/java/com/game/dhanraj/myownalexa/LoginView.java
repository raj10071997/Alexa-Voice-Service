package com.game.dhanraj.myownalexa;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by dhanraj on 28/6/17.
 */

public class LoginView extends View {

    Paint backgroundPaint;
    Paint progressPaint;
    Drawable loginBtn;

    public LoginView(Context context) {
        super(context);
        init();
    }

    public LoginView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoginView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LoginView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
    }
}
