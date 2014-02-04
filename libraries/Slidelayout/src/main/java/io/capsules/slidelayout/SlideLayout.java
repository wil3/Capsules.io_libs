/*
 *      Copyright 2014 Capsules LLC
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */

package io.capsules.slidelayout;


import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * This view allows for notification when either bounds are eached on the scroll view
 * @author Wil Koch
 */
public class SlideLayout extends ScrollView {

    private static final String TAG = "SlideLayout";


    public interface ScrollOverflowListener {

        /**
         * Notification when the user reached one of the bounds
         * @param  restingPosition The current resting position
         */
        public void onStartOverflow(ScrollResting restingPosition);

        /**
         * The user is no longer at the scroll bounds
         * @param restingPosition
         */
        public void onStopOverflow(ScrollResting restingPosition);

        /**
         *
         * @param percent The percent in relation to the scroll height the overflow has been dragged
         */
        public void onTopScrollOverflow(float percent);

        /**
         *
         * @param percent The percent in relation to the scroll height the overflow has been dragged
         */
        public void onBottomScrollOverflow(float percent);
    }

    public interface ScrollViewListener {
        void onScrollChanged(View scrollView, int x, int y, int oldX, int oldY);
    }

    public enum ScrollResting {
        TOP,
        BOTTOM,
        OTHER
    }

    private ScrollViewListener mScrollViewListener;
    private ScrollResting mScrollRest = ScrollResting.TOP;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private int mScrollTouchSlop;
    private boolean isSlideInProgress = false;
    private ScrollOverflowListener mScrollOverflowListener;
    private float mLastY;
    private float mLeftOffset;


    public SlideLayout(Context context) {
        this(context, null);
    }

    public SlideLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        ViewConfiguration vc = ViewConfiguration.get(context);
        mScrollTouchSlop = vc.getScaledTouchSlop();

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = MotionEventCompat.getActionMasked(ev);


        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = x;
                mInitialMotionY = y;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float dy = y - mInitialMotionY;
                final float ady = Math.abs(dy);
                final float dx = x - mInitialMotionX;
                final float adx = Math.abs(dx);

                // Handle any horizontal scrolling on the drag view.
                if (adx > mScrollTouchSlop && ady < mScrollTouchSlop) {
                    Log.d(TAG, " Sending to children");
                    return super.onInterceptTouchEvent(ev);
                }

                if ((mScrollRest == ScrollResting.TOP && dy > 0 || mScrollRest == ScrollResting.BOTTOM && dy < 0) && ady > adx  ){
                    return true;
                }

                if (isSlideInProgress){
                    return true;
                }

                break;
            }

        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        boolean handled = true;

        final int action = MotionEventCompat.getActionMasked(ev);
        final float x = ev.getX();
        final float y = ev.getY();
        switch (action){

            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = x;
                mInitialMotionY = y;
                mLastY = y;
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (isSlideInProgress){
                    isSlideInProgress = false;
                    mScrollOverflowListener.onStopOverflow(mScrollRest);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float dy = y - mInitialMotionY;
                final float ady = Math.abs(dy);
                final float dx = x - mInitialMotionX;
                float adx = Math.abs(dx);

                float dy_last = y - mLastY;
                mLastY = y;

                //Remove the offset from the pager being opened
                adx -= mLeftOffset;


                if (
                        ((mScrollRest == ScrollResting.BOTTOM && dy < 0) ||
                                (mScrollRest == ScrollResting.TOP && dy > 0))  && ady > adx ){

                    if (!isSlideInProgress){
                        mScrollOverflowListener.onStartOverflow(mScrollRest);
                        isSlideInProgress = true;
                    }
                }

                if (isSlideInProgress && ady > adx){

                        if (mScrollRest == ScrollResting.TOP ) {
                            dispatchTopScrollOverflowEvent(dy_last);
                        } else if (mScrollRest == ScrollResting.BOTTOM){
                            dispatchBottomScrollOverflowEvent(dy_last);
                        }

                } else {
                    return super.onTouchEvent(ev);
                }
                break;
            }
        }

        return handled;
    }
    private void dispatchTopScrollOverflowEvent(float y){

        if (mScrollOverflowListener != null){
            float percentage = calculatePercent(y);
            mScrollOverflowListener.onTopScrollOverflow(percentage);
        }
    }

    private void dispatchBottomScrollOverflowEvent(float y){

        if (mScrollOverflowListener != null){
            float percentage = calculatePercent(y);
            mScrollOverflowListener.onBottomScrollOverflow(percentage);
        }
    }

    private float calculatePercent(float y){
        return y /  getHeight();
    }

    /**
     *
     * @param leftOffset
     */
    public void setFakeLeftOffset(float leftOffset){mLeftOffset = leftOffset;}


    @Override
    protected void onScrollChanged( int x, int y, int oldX, int oldY){
        super.onScrollChanged(x,y,oldX, oldY);
        // We take the last son in the scrollview
        View view = (View) getChildAt(getChildCount() - 1);
        int diff = (view.getBottom() - (getHeight() + getScrollY()));

        // if diff is zero, then the bottom has been reached
        if (diff == 0) {
            Log.v(TAG, "Reached end of scroll");
            mScrollRest = ScrollResting.BOTTOM;
        } else if (getScrollY() == 0){
            mScrollRest = ScrollResting.TOP;
        } else {
            mScrollRest = ScrollResting.OTHER;
        }

        if (mScrollViewListener != null) {
            mScrollViewListener.onScrollChanged(this, x, y, oldX, oldY);
        }
    }
    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        mScrollViewListener = scrollViewListener;
    }

    public void setScrollOverflowListener(ScrollOverflowListener listener){
        mScrollOverflowListener = listener;
    }



}
