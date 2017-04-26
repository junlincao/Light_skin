package com.cjl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.cjl.skin.ISkinable;
import com.cjl.skin.ResourceManager;

/**
 * 自定义控件实现换肤方案2 实现ISkinable接口，自己处理所有的换肤操作。
 *
 * @author CJL
 * @since 2017-04-26
 */

public class CustomView2 extends View implements ISkinable {

    public CustomView2(Context context) {
        this(context, null);
    }

    public CustomView2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = context.getResources().getDisplayMetrics().density;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomView2);
        mTextColor = a.getColor(R.styleable.CustomView2_paintTextColor2, ContextCompat.getColor(context, R.color.text_color));
        float textSize = a.getDimension(R.styleable.CustomView2_paintTextSize2, density * 16);
        mPaint.setTextSize(textSize);
        a.recycle();
    }

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mTextColor;


    public void setTextColor(int color) {
        this.mTextColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setColor(mTextColor);
        canvas.drawText("CustomView change skin solutions 2", getWidth() / 2, getHeight() / 2, mPaint);
    }


    @Override
    public void applySkin(ResourceManager rm) {
        Integer tColor = rm.getColor("text_color");
        if (tColor != null) {
            setTextColor(tColor);
        }
    }
}
