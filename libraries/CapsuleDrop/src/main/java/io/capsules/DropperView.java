package io.capsules;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

/**
 * Created by wil on 1/14/14.
 */
public class DropperView extends RelativeLayout {

    private static final String TAG_LIST = "listView";
    private View mDragView;
    private int mXDelta;

    private final ViewDragHelper mDragHelper;

    private int mDisplayWidth;

    private int mDragRange;

    private float mInitialMotionX;
    private float mInitialMotionY;


    private ListView mListView;
    private ListAdapter mListAdapter;






    public DropperView(Context context) {
        this(context, null);
    }
    public DropperView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }
    public DropperView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);

        mDragHelper = ViewDragHelper.create(this, 1.0f,new DragHelperCallback());
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT);

        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        mDisplayWidth = dm.widthPixels;
    }



        @Override
    protected void onFinishInflate() {

        mDragView = findViewWithTag("dragView");
        mListView = (ListView)findViewWithTag(TAG_LIST);

            // RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)mDragView.getLayoutParams();


       // lParams.leftMargin = mDisplayWidth - 20;

      //  Log.d(getClass().getName(), "Left margin=" + lParams.leftMargin );
     //   mDragView.setLayoutParams(lParams);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {



        //relative to parent

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        final int childCount = getChildCount();


        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            if (child.equals(mDragView)){
                mDragView.layout(getWidth(), t,  r + mDragView.getMeasuredWidth(), t + mDragView.getMeasuredHeight());

            } else {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final int childHeight = child.getMeasuredHeight();

                final int childTop =  paddingTop;
                final int childBottom = childTop + childHeight;
                final int childLeft = paddingLeft;
                final int childRight = childLeft + child.getMeasuredWidth();

                child.layout(childLeft, childTop, childRight, childBottom);
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setDragView(View view){

        mDragView = view;



        /*
        final View that = this;

        mDragView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int X = (int) motionEvent.getRawX();
                final int Y = (int) motionEvent.getRawY();

                View parent = (View)view.getTag();

                int relativeLeft = (int) motionEvent.getX() - view.getLeft();

                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        mXDelta = X - lParams.leftMargin;


                        mDragHelper.captureChildView(mDragView, 1);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(getClass().getName(), "Up, release slide to " + mDisplayWidth + " " + mDragView.getTop());

                        if (mDragHelper.smoothSlideViewTo(mDragView, mDisplayWidth, mDragView.getTop())){
                            ViewCompat.postInvalidateOnAnimation(that);

                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:

                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        layoutParams.leftMargin = X - mXDelta;
                        view.setLayoutParams(layoutParams);
                        break;
                }

                ((View) view.getParent()).invalidate();
                return true;
            }
        });
        */

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);


        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel();
            return false;
        }



        final float x = ev.getX();
        final float y = ev.getY();
        boolean interceptTap = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = x;
                mInitialMotionY = y;
                interceptTap = mDragHelper.isViewUnder(mDragView, (int) x, (int) y);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final float adx = Math.abs(x - mInitialMotionX);
                final float ady = Math.abs(y - mInitialMotionY);
                final int slop = mDragHelper.getTouchSlop();
                //check out the slop, if change too great cancel.
                // if the vertical change is greater than the horizontal change also cancel
                if (adx > slop && ady > adx) {
                    mDragHelper.cancel();
                    return false;
                }
            }
        }

        //return true to intercept
        return mDragHelper.shouldInterceptTouchEvent(ev);// || interceptTap;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        Log.d(TAG, "On touch x=" + ev.getX() + " y=" + ev.getY());


        mDragHelper.processTouchEvent(ev);

        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();

        boolean isViewUnder = mDragHelper.isViewUnder(mDragView, (int) x, (int) y);


        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = x;
                mInitialMotionY = y;
                break;
            }

            case MotionEvent.ACTION_UP: {
                //We wamt to close when up
                final float dx = x - mInitialMotionX;
                final float dy = y - mInitialMotionY;
                final int slop = mDragHelper.getTouchSlop();

                Log.d(getClass().getName(), "On touch under? " + isViewUnder + " dx " + dx + " dy " + dy + " slop " + slop);

             //   if (dx * dx + dy * dy < slop * slop && isViewUnder) {
                  //  if (isViewUnder){
                    Log.d(getClass().getName(), "On touch, slide to ");

                    smoothSlideTo(1f);

                //}
                break;
            }
        }

        //true if event handled
        return true;//isViewUnder && isViewHit(mDragView, (int) x, (int) y) ;


    }
    boolean smoothSlideTo(float slideOffset) {
        final int leftBound = getWidth();

        int x = (int) (leftBound + slideOffset * mDragRange);

        if (mDragHelper.smoothSlideViewTo(mDragView, leftBound, mDragView.getTop())) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    private boolean isViewHit(View view, int x, int y) {
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.getWidth() &&
                screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
    }

    /*
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = MotionEventCompat.getActionMasked(ev);


        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            return false;
        }

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {


                break;
            }

            case MotionEvent.ACTION_MOVE: {

                boolean hit = isDragViewHit((int) x, (int) y);
                if (hit){
                    return true;
                }
                break;
            }

        }

        //Return true to steal motion events from the children and have them dispatched to this ViewGroup through onTouchEvent()
        //Will not slide if super is called
        return false;
    }
*/
    private boolean isDragViewHit(int x, int y) {
        if (mDragView == null) return false;
        int[] viewLocation = new int[2];
        mDragView.getLocationOnScreen(viewLocation);

        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;

        int [] windowLocation = new int [2];
        this.getLocationInWindow(windowLocation);



        Log.d(getClass().getSimpleName(), "view x= " + viewLocation[0] + " parent x=" + parentLocation[0] + " x= " + x);
        return viewLocation[0] <= parentLocation[0];
        // return screenX >= viewLocation[0] && screenX < viewLocation[0] + v.getWidth();// &&
        //screenY >= viewLocation[1] && screenY < viewLocation[1] + v.getHeight();
    }


    private class DragHelperCallback extends ViewDragHelper.Callback {



        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            Log.d(getClass().getName(), "onViewCaptured");

        }
        @Override
        public int getViewHorizontalDragRange(View child){
            Log.d(getClass().getName(),"w = " + mDragView.getWidth());
            return mDragView.getWidth();
        }
        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            Log.d(getClass().getName(),"Edget touched");

            super.onEdgeTouched(edgeFlags, pointerId);
        }

        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            Log.d(getClass().getName(),"Edge drag start");

            mDragHelper.captureChildView(mDragView, pointerId);
        }


        /**
         *
         * @param releasedChild
         * @param xvel
         * @param yvel
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {


            int top = getPaddingTop();

            Log.d(getClass().getName(), "onViewRelease  l=" + getWidth() + " t=" + top);

            mDragHelper.settleCapturedViewAt(getWidth(), top);

        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            //Without invalidate the view leaves a black trail
            invalidate();
        }

        @Override
        public boolean tryCaptureView(View view, int i) {
            Log.d(getClass().getName(), "tryCaptureView");

            return view == mDragView;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            Log.d(getClass().getName(), "clampViewPositionHorizontal " + left + "," + dx);

            final int leftBound = getWidth()-mDragView.getWidth();//getPaddingLeft();


            final int rightBound = getWidth();// - mDragView.getWidth();

            final int newLeft = Math.min(Math.max(left, leftBound), rightBound);

            return newLeft;

        }


    }


    public void setAdapter(ListAdapter adapter){
        mListAdapter = adapter;
    }


    }
