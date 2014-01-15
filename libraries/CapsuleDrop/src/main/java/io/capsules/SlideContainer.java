package io.capsules;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Created by wil on 1/14/14.
 */
public class SlideContainer extends RelativeLayout {
    private float mInitialMotionX;
    private float mInitialMotionY;
    private boolean mDragViewHit;
    private View mDragView;


    public SlideContainer(Context context) {
        super(context);
    }
    public SlideContainer(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    public SlideContainer(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
    }


    public void setDragView(View view){
        mDragView = view;
    }
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
                mDragViewHit = isDragViewHit((int) x, (int) y);
                if (mDragViewHit){
                    dispatchCapsuleSelected(ev);
                    return true;
                }
                break;
            }

        }

        //Return true to steal motion events from the children and have them dispatched to this ViewGroup through onTouchEvent()
        //Will not slide if super is called
        return false;
    }



    private void dispatchCapsuleSelected(MotionEvent ev) {
        Log.d(getClass().getName(), "Dispatching capsule selected ");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CapsuleDropperActivity.DROPPING_CAPSULE);
        broadcastIntent.putExtra(CapsuleDropperActivity.EXTRA_CAPSULE, (Integer) getTag());// includes the position in the array of capsules
        broadcastIntent.putExtra(CapsuleDropperActivity.EXTRA_X, mLastDragX);
        broadcastIntent.putExtra(CapsuleDropperActivity.EXTRA_Y, mLastDragY);

        broadcastIntent.putExtra(CapsuleDropperActivity.EXTRA_MOTION_EVENT, ev);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(broadcastIntent);

    }

    private int mLastDragX;
    private int mLastDragY;
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


        mLastDragX = viewLocation[0];
        mLastDragY = viewLocation[1];

        Log.d(getClass().getSimpleName(), "view x= " + viewLocation[0] + " parent x=" + parentLocation[0] + " x= " + x);
        return viewLocation[0] <= parentLocation[0];
        // return screenX >= viewLocation[0] && screenX < viewLocation[0] + v.getWidth();// &&
        //screenY >= viewLocation[1] && screenY < viewLocation[1] + v.getHeight();
    }
}
