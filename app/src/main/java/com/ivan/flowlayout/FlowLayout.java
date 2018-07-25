package com.ivan.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/*
 * @author liuwei
 * @email 13040839537@163.com
 * create at 2018/7/24
 * description:流式布局
 */
public class FlowLayout extends ViewGroup {
    //每一行的view集合
    private List<List<View>> mViewLinesList = new ArrayList<>();
    //每一列的高度集合
    private List<Integer> mLineHeights = new ArrayList<>();


    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    //重写LayoutParams,方便获取margin
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //清理集合，进行初始化
        mViewLinesList.clear();
        mLineHeights.clear();

        int mWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int mHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        int mWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int mHeightSize = MeasureSpec.getSize(heightMeasureSpec);

        int measuredWidth = 0;
        int measuredHeight = 0;

        int mCurrentWidth = 0;
        int mCurrentHeight = 0;

        //如果宽高为精确的测量模式
        if (mWidthMode == MeasureSpec.EXACTLY && mHeightMode == MeasureSpec.EXACTLY) {
            measuredWidth = mWidthSize;
            measuredHeight = mHeightSize;
        //如果宽高非精确的测量模式
        } else {
            int iChildWidth;
            int iChildHeight;

            int count = getChildCount();
            //遍历view集合前，初始化一行的集合
            List<View> viewList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                //对view进行测量，让view赋值测量的宽高
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) child.getLayoutParams();
                iChildWidth = child.getMeasuredWidth() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
                iChildHeight = child.getMeasuredHeight() + marginLayoutParams.topMargin +
                        marginLayoutParams.bottomMargin;
                //如果view的宽度加上当前宽度大于屏幕宽度，则换行
                if (iChildWidth + mCurrentWidth > mWidthSize) {
                    //换行，记录最大测量宽以及测量高度
                    measuredWidth = Math.max(measuredWidth, mCurrentWidth);
                    measuredHeight += mCurrentHeight;

                    //将一行的viewList添加进集合中
                    mViewLinesList.add(viewList);
                    //将当前行的高度添加集合
                    mLineHeights.add(mCurrentHeight);

                    //赋值下一行的第一个view的宽高。为当前宽高
                    mCurrentWidth = iChildWidth;
                    mCurrentHeight = iChildHeight;

                    //初始化一个viewList，用于存储新的一行的所有view
                    viewList = new ArrayList<>();
                    //将新的view加入到新的一行中
                    viewList.add(child);
                //如果没有超出一行
                } else {
                    //叠加当前宽高，当前高
                    mCurrentWidth += iChildWidth;
                    mCurrentHeight = Math.max(mCurrentHeight, iChildHeight);
                    viewList.add(child);
                }
                //通过最后一个view，来判断最后的测量宽度和高度
                if (i == count - 1) {
                    measuredWidth = Math.max(measuredWidth, mCurrentWidth);
                    measuredHeight += mCurrentHeight;

                    mViewLinesList.add(viewList);
                    mLineHeights.add(mCurrentHeight);
                }
            }
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean b, int n, int i1, int i2, int i3) {
        int left, top, right, bottom;
        int curTop = 0;
        int curLeft = 0;

        int lineCount = mViewLinesList.size();

        //循环遍历View
        for (int i = 0; i < lineCount; i++) {
            List<View> viewList = mViewLinesList.get(i);
            int lineViewSize = viewList.size();
            for (int j = 0; j < lineViewSize; j++) {
                View childView = viewList.get(j);
                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) childView.getLayoutParams();

                left = curLeft + marginLayoutParams.leftMargin;
                top = curTop + marginLayoutParams.topMargin;
                right = left + childView.getMeasuredWidth();
                bottom = top + childView.getMeasuredHeight();

                //获取每个view的left,top,right,bottom
                childView.layout(left, top, right, bottom);

                //行内，对left值进行偏移
                curLeft += childView.getMeasuredWidth() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
            }
            //换行，对left值清零;top值进行叠加
            curLeft = 0;
            curTop += mLineHeights.get(i);
        }

    }

}
