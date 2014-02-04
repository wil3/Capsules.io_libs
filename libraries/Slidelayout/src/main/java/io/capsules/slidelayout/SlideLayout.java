package io.capsules.slidelayout;


import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

public class SlideLayout extends ScrollView { //RelativeLayout {

    private static final String TAG = "SlideLayout";


    public interface ScrollOverflowListener {
        public void onStartOverflow();
        public void onStopOverflow();
        public void onTopScrollOverflow(float offset);
        public void onBottomScrollOverflow(float offset);
    }
    public enum ScrollResting {
        TOP,
        BOTTOM,
        MIDDLE

    }
    public interface ScrollViewListener {
        void onScrollChanged(View scrollView,
                             int x, int y, int oldx, int oldy);
    }

    private ScrollViewListener scrollViewListener = null;

    private ScrollResting mScrollRest = ScrollResting.TOP;
    private final ViewDragHelper mDragHelper;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private boolean mDragViewHit = false;
    private boolean mIsUsingDragViewTouchEvents = false;
    private int mScrollTouchSlop;

    private int mSlideRange;

    private boolean mShouldIntercept = false;

    private boolean isSlideInProgress = false;

    private ScrollOverflowListener mListener;

    private View mFillerView;
    private View mHeaderView;
    private View mBodyView;
    private View mCoverView;


    private float mLastY;
    private float mOffsetY;
    private float mLeftOffset;

    private SlideLayoutScrollView mMainContentView;


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
        mDragHelper = ViewDragHelper.create(this, 1.0f,new DragHelperCallback());

    }

    @Override
    protected void onFinishInflate () {
        super.onFinishInflate();
        mSlideRange = getHeight();



    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = MotionEventCompat.getActionMasked(ev);


        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            Log.v(TAG," cancelled");
            mDragHelper.cancel();
            return false;
        }

        //Log.v(TAG, "top " + mMainContentView.getTop() + "bottom " +  mMainContentView.getBottom() + " y " + mMainContentView.getScrollY());

        final float x = ev.getX();
        final float y = ev.getY();
        boolean interceptTap = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                Log.v(TAG, "intercept down");
                mShouldIntercept = false;
                mInitialMotionX = x;
                mInitialMotionY = y;

                break;
            }
            case MotionEvent.ACTION_UP: {



                    Log.v(TAG, "intercept up");
                    break;
            }

            case MotionEvent.ACTION_MOVE: {
                final float dy = y - mInitialMotionY;
                final float ady = Math.abs(dy);
                final float dx = x - mInitialMotionX;
                final float adx = Math.abs(dx);
                final int dragSlop = mDragHelper.getTouchSlop();
                // Handle any horizontal scrolling on the drag view.
                    if (adx > mScrollTouchSlop && ady < mScrollTouchSlop) {
                        Log.d(TAG, " Sending to children");
                        return super.onInterceptTouchEvent(ev);
                    }
                    // Intercept the touch if the drag view has any vertical scroll.
                    // onTouchEvent will determine if the view should drag vertically.
                    //TODO revisit this value of 50, this should make it so that the horizontal gets higher priority
                    else if (ady > 50){//mScrollTouchSlop) {
                        Log.v(TAG, "change greater than slop");

                    }


                if ((mScrollRest == ScrollResting.TOP && dy > 0 || mScrollRest == ScrollResting.BOTTOM && dy < 0) && ady > adx  ){
                   // if (!isSlideInProgress){
                   //     mListener.onStartOverflow();
                   //     isSlideInProgress = true;
                   // mShouldIntercept = true;
                        return true;

                  //  }
                }

                if (isSlideInProgress){
                    return true;
                }


               // if (ady > dragSlop && adx > ady) {
               ////     mDragHelper.cancel();
               //     return false;
               // }

              //  Log.d(TAG, "dx " + adx + " dy " + ady);

                break;
            }

        }

       // final boolean interceptForDrag = mDragViewHit && mDragHelper.shouldInterceptTouchEvent(ev);
       // Log.v(TAG, "fordrag=" + interceptForDrag + " tap=" + interceptTap + " action=" + action );

        //Return true to steal motion events from the children and have them dispatched to this ViewGroup through onTouchEvent()
        //Will not slide if super is called
        Log.d(TAG, "Not intercepting ");
       // return true;
        return super.onInterceptTouchEvent(ev);//interceptForDrag || interceptTap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
/*
        if (!mShouldIntercept){
            Log.d(TAG, "Shouldnt intercept");
            super.onTouchEvent(ev);
            return false;
        }
        */
   //     mDragHelper.processTouchEvent(ev);
        boolean handled = true;

        final int action = MotionEventCompat.getActionMasked(ev);
        final float x = ev.getX();
        final float y = ev.getY();
        switch (action){

            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = x;
                mInitialMotionY = y;
                mLastY = y;
                mOffsetY =0;
                break;
            }
            case MotionEvent.ACTION_UP: {
                Log.v(TAG, "touch up " + isSlideInProgress);

                if (isSlideInProgress){
                    isSlideInProgress = false;
                    mListener.onStopOverflow();

                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float dy = y - mInitialMotionY;
                final float ady = Math.abs(dy);
                final float dx = x - mInitialMotionX;
                 float adx = Math.abs(dx);

                float _dy = y - mLastY;
                mOffsetY += y - mLastY ;
                mLastY = y;

                //Remove the offset from the pager being opened
                adx -= mLeftOffset;


                if (
                        ((mScrollRest == ScrollResting.BOTTOM && dy < 0) ||
                                (mScrollRest == ScrollResting.TOP && dy > 0))  && ady > adx ){
                     //   ((mScrollRest == ScrollResting.BOTTOM) ||
                    //            (mScrollRest == ScrollResting.TOP))  && ady > adx ){

                    if (!isSlideInProgress){
                        mListener.onStartOverflow();
                        isSlideInProgress = true;

                    }

                }
                Log.d(TAG,  " offsetY " + mOffsetY + " ady " + ady + " adx " + adx);
                Log.d(TAG, " dy " + _dy);

                if (isSlideInProgress && ady > adx){

                 //   else {


/*
                        if (mScrollRest == ScrollResting.TOP && dy > 0) {
                            dispatchTopScrollOverflowEvent(dy);
                        } else if (mScrollRest == ScrollResting.BOTTOM && dy < 0){
                            dispatchBottomScrollOverflowEvent(dy);

                        }*/
                        if (mScrollRest == ScrollResting.TOP ) {
                            dispatchTopScrollOverflowEvent(_dy);
                        } else if (mScrollRest == ScrollResting.BOTTOM){
                            dispatchBottomScrollOverflowEvent(mOffsetY);

                        }
                   // }

                } else {
                    return super.onTouchEvent(ev);
                }
                break;
            }
        }

        return handled;
    }
    private void dispatchTopScrollOverflowEvent(float y){

        if (mListener != null){
            float offset = y /  getHeight();
            mListener.onTopScrollOverflow(offset);
        }
    }

    private void dispatchBottomScrollOverflowEvent(float y){

        if (mListener != null){
            float offset = y /  getHeight();
            mListener.onBottomScrollOverflow(offset);
        }
    }

    public void setOnScrollOverflowListner(ScrollOverflowListener listener){
        mListener = listener;
    }
    public void setLeftOffset(float leftOffset){mLeftOffset = leftOffset;}


    private boolean isDragViewHit(View v, int x, int y) {
        if (v == null) return false;
        int[] viewLocation = new int[2];
        v.getLocationOnScreen(viewLocation);
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + v.getWidth() &&
                screenY >= viewLocation[1] && screenY < viewLocation[1] + v.getHeight();
    }

    @Override
    protected void onScrollChanged( int x, int y, int oldx, int oldy){
        super.onScrollChanged(x,y,oldx, oldy);
      //  updateCoverHeight();
        // We take the last son in the scrollview
        View view = (View) getChildAt(getChildCount() - 1);
        int diff = (view.getBottom() - (getHeight() + getScrollY()));

        Log.d(TAG, "scroll y " + getScrollY());
        // if diff is zero, then the bottom has been reached
        if (diff == 0) {
            Log.d(TAG, "End of scroll");
            mScrollRest = ScrollResting.BOTTOM;
        } else if (getScrollY() == 0){
            mScrollRest = ScrollResting.TOP;
        } else {
            mScrollRest = ScrollResting.MIDDLE;
        }

        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }
    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }


    private void updateCoverHeight(){
        int[] xy = new int[2];
        mBodyView.getLocationOnScreen(xy);

        int newHeight = xy[1] - getPaddingTop();
        if (newHeight <= getHeight()){
        ViewGroup.LayoutParams params = mFillerView.getLayoutParams();
        params.height =  newHeight;

        Log.d(TAG, " cover h " + newHeight);
        mFillerView.setLayoutParams(params);
        }
    }

    public void setFillerView(int resid){
        mFillerView = findViewById(resid);



        ViewTreeObserver vto = mFillerView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mFillerView.getViewTreeObserver().isAlive()) {
                    // only need to calculate once, so remove listener
                    mFillerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                int newHeight = getHeight() - mHeaderView.getHeight();
                ViewGroup.LayoutParams params = mFillerView.getLayoutParams();
                params.height = getHeight();
                mFillerView.setLayoutParams(params);

            }
        });

    }
    public void setHeaderView(int resid){
        mHeaderView = findViewById(resid);
    }
    public void setBodyView(int resid){
        mBodyView = findViewById(resid);
    }
    public void setCoverView(int resid){
        mCoverView = findViewById(resid);
    }

    public void setMainContentView(int resid){
        /*
        View v = findViewById(resid);
        if (v instanceof SlideLayoutScrollView){
            mMainContentView  = (SlideLayoutScrollView) v;
            mMainContentView.setScrollViewListener(new SlideLayoutScrollView.ScrollViewListener() {
                @Override
                public void onScrollChanged(SlideLayoutScrollView scrollView, int x, int y, int oldx, int oldy) {
                    // We take the last son in the scrollview
                    View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

                    Log.d(TAG, "scroll y " + scrollView.getScrollY());
                    // if diff is zero, then the bottom has been reached
                    if (diff == 0) {
                        Log.d(TAG, "End of scroll");
                        mScrollRest = ScrollResting.BOTTOM;
                    } else if (scrollView.getScrollY() == 0){
                        mScrollRest = ScrollResting.TOP;
                    } else {
                        mScrollRest = ScrollResting.MIDDLE;
                    }


                    View firstView = scrollView.getChildAt(0);
                }
            });
        } else {
            throw  new RuntimeException("Content view must be of type SlideLayoutScrollView");
        }
        */

    }
    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            super.onEdgeTouched(edgeFlags, pointerId);
        }

        @Override
        public boolean onEdgeLock(int edgeFlags) {
            return super.onEdgeLock(edgeFlags);
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            super.onEdgeDragStarted(edgeFlags, pointerId);
        }

        @Override
        public int getOrderedChildIndex(int index) {
            return super.getOrderedChildIndex(index);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return super.getViewHorizontalDragRange(child);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return super.getViewVerticalDragRange(child);
        }

        @Override
        public boolean tryCaptureView(View view, int i) {
            return false;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return super.clampViewPositionHorizontal(child, left, dx);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return super.clampViewPositionVertical(child, top, dy);
        }
    }


}
