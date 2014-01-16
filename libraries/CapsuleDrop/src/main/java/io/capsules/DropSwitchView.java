package io.capsules;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

/**
 * Created by William Koch on 1/16/14.
 */
public class DropSwitchView extends RelativeLayout{

    private enum SlideDirection {
        LEFT,
        RIGHT
    }
    private final ViewDragHelper mDragHelper;
    private View mDragView;
    public DropSwitchView(Context context) {
        this(context, null);
    }
    public DropSwitchView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }
    public DropSwitchView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);

        mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        //mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT);


    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();

        final int childCount = getChildCount();

        if (childCount == 0 || childCount > 1){
            super.onLayout(changed,l,t,r,b);
        }  else { // one child

            View child  = getChildAt(0);

            //Align the child to the right
            final int childLeft = getWidth() - child.getMeasuredWidth() - paddingLeft;
            final int childTop = paddingTop;
            final int childRight = childLeft + child.getMeasuredWidth() - paddingRight;
            final int childBottom = childTop + child.getMeasuredHeight() - getPaddingBottom();
            child.layout(childLeft, childTop, childRight, childBottom);

            mDragView = child;
        }

    }


    public void switchOn (){
       slideTo(SlideDirection.LEFT);

    }
    public void switchOff(){
        slideTo(SlideDirection.RIGHT);
    }

    private void slideTo(SlideDirection direction){
        //final int left = (direction == SlideDirection.LEFT) ? getPaddingLeft() : getWidth() - getPaddingRight();
       // return mDragHelper.smoothSlideViewTo(mDragView, left, 0);

        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.switch_on);

        mDragView.startAnimation(anim);
    }
    private class DragHelperCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View view, int i) {
            return view.equals(mDragView);
        }
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            //Log.d(getClass().getName(), "clampViewPositionHorizontal " + left + "," + dx);

                final int leftBound = getWidth()-mDragView.getWidth();//getPaddingLeft();

                final int rightBound = getWidth();// - mDragView.getWidth();

                final int newLeft = Math.min(Math.max(left, leftBound), rightBound);


            return newLeft;

        }

    }

}
