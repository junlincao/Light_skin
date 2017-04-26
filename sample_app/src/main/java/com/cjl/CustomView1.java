package com.cjl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自定义控件实现换肤方案1 直接在AttrManager中添加换肤属性。
 * 适合所有没有实现ISkinable接口的第三方控件
 * <p>
 * <code>
 * AttrManager.addSkinAttr(new AttrManager.ColorAttr() {
 * public String getAttrName() { return "paintTextSize"; }
 * public void apply(View view, Integer obj) { if(view instanceof CustomView1){((CustomView1) view).setTextColor(obj); }}});
 * </code>
 *
 * @author CJL
 * @since 2017-04-26
 */

public class CustomView1 extends View {

    public CustomView1(Context context) {
        this(context, null);
    }

    public CustomView1(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomView1(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = context.getResources().getDisplayMetrics().density;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomView1);
        mTextColor = a.getColor(R.styleable.CustomView1_paintTextColor, ContextCompat.getColor(context, R.color.text_color));
        float textSize = a.getDimension(R.styleable.CustomView1_paintTextSize, density * 16);
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
        canvas.drawText("CustomView change skin solutions 1", getWidth() / 2, getHeight() / 2, mPaint);
    }

    @Override
    public int getMinimumHeight() {
        return (int) (getResources().getDisplayMetrics().density * 30);
    }
}
