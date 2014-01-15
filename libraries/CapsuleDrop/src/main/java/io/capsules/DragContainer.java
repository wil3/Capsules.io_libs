package io.capsules;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * This is the container for where objects can be dragged
 *
 * Created by wil on 1/14/14.
 */
public class DragContainer extends RelativeLayout {
    private float mInitialMotionX;
    private float mInitialMotionY;

    private boolean mChildViewAdded = false;

    public DragContainer(Context context) {
        super(context);
    }
    public DragContainer(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public DragContainer(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
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
                mInitialMotionX = x;
                mInitialMotionY = y;

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final float adx = Math.abs(x - mInitialMotionX);
                final float ady = Math.abs(y - mInitialMotionY);

              //  Log.d(getClass().getName(), "X=" + ev.getX() + " Y=" + ev.getY());
                break;
            }

        }

        //Return true to steal motion events from the children and have them dispatched to this ViewGroup through onTouchEvent()
        //Will not slide if super is called
        return false;
    }
 */
    View mLastChildAdded;
    @Override
    public void addView(View child){

        mLastChildAdded = child;
        mChildViewAdded = true;
        super.addView(child);
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent ev){

        if( mLastChildAdded == null ) {
            return super.dispatchTouchEvent(ev);
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {

                break;
            }

            case MotionEvent.ACTION_MOVE: {

                int [] windowLocation = new int [2];
                this.getLocationInWindow(windowLocation);

                Log.d(getClass().getName(), "X=" + ev.getX() + " Y=" + ev.getY() + " window Y= " + windowLocation[1]);
                mLastChildAdded.dispatchTouchEvent(ev);
                break;
            }

            case MotionEvent.ACTION_UP: {
                mLastChildAdded = null;
                break;
            }

        }


        return super.dispatchTouchEvent(ev);
    }


}
