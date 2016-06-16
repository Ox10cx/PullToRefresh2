package com.gx.pulltorefresh2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2016/3/26.
 */

/**
 * 自定义View
 */
public class RefreshThirdStepView extends View {
    private Bitmap endBitmap;
    public RefreshThirdStepView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshThirdStepView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RefreshThirdStepView(Context context) {
        super(context);
        init();
    }
    private void init() {
        //通过图片的ID获得Bitmap图像
        endBitmap = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bb));
    }

    /**
     * View在屏幕上显示出来要先经过measure的计算和Layout布局
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //通过此方法告诉父控件，需要多大地方放置子控件
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureWidth(widthMeasureSpec) * endBitmap.getHeight() / endBitmap.getWidth());
    }

    private int measureWidth(int widthMeasureSpec){
        int result = 0;
        int size = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        //依据specMode的值，（MeasureSpec有3种模式分别是UNSPECIFIED, EXACTLY和AT_MOST）
        //如果是AT_MOST，specSize 代表的是最大可获得的空间；
        //如果是EXACTLY，specSize 代表的是精确的尺寸；
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        }else {
            result = endBitmap.getWidth();
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }
}
