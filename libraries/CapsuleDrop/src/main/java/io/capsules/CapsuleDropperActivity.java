package io.capsules;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CapsuleDropperActivity extends Activity implements View.OnTouchListener {

    public static final String DROPPING_CAPSULE = "DROPPING";
    public static final String EXTRA_CAPSULE = "EXTRA_CAPSULE";
    public static final String EXTRA_X = "EXTRA_X";
    public static final String EXTRA_Y = "EXTRA_Y";
    public static final String EXTRA_MOTION_EVENT = "EXTRA_MOTION_EVENT";

    TextView _view;
    DragContainer mDragOverlayView;
    private int _xDelta;
    private int _yDelta;
    private BroadcastReceiver mReceiver = new MyBroadcastReceiver();
    List<CapsuleHeader> capsules = new ArrayList<CapsuleHeader>();
    DropArrayAdapter mAdapter;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.capsuledropper_activity);

        mDragOverlayView = (DragContainer)findViewById(R.id.container);

        initListView();
    }

    private void addSample(){

        _view = new TextView(this);
        _view.setText("TextView!!!!!!!!");

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(150, 50);
        layoutParams.leftMargin = 50;
        layoutParams.topMargin = 50;
        layoutParams.bottomMargin = -250;
        layoutParams.rightMargin = -250;
        _view.setLayoutParams(layoutParams);

        _view.setOnTouchListener(this);
        mDragOverlayView.addView(_view);

    }

    private void initListView(){
        ListView list = (ListView)findViewById(R.id.list_carrying_capsules);

        for (int i=0; i<10;i++){
            capsules.add(new CapsuleHeader());
        }

        mAdapter = new DropArrayAdapter(this,R.layout.listitem, capsules);

        list.setAdapter(mAdapter);

    }

    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();

        Log.d(getClass().getName(), "onTouch");

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.leftMargin = X - _xDelta;
                layoutParams.topMargin = Y - _yDelta;
                layoutParams.rightMargin = -250;
                layoutParams.bottomMargin = -250;
                view.setLayoutParams(layoutParams);
                break;
        }
        mDragOverlayView.invalidate();
        return true;
    }
    /*
    @Override
    public boolean onTouchEvent (MotionEvent event) {
        Log.d(getClass().getName(), "onTouchEvent at event.x = " + event.getX() + " event.y= " + event.getY());

        return  false;
    }
    */
    private void addDraggedViewToOverlay(final MotionEvent ev,int x, int y){
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.draggable_view, null);

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int topOffset = dm.heightPixels - mDragOverlayView.getMeasuredHeight();


        y -= topOffset;//correct from action bar

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = x;
        layoutParams.topMargin = y;

        Log.d(getClass().getName(), "Drop at x=" + x + " y=" + y + " ev.x = " + ev.getX() + " ev.y= " + ev.getY());

        ev.setLocation(x, y);
        ev.setAction(MotionEvent.ACTION_DOWN);

        final MotionEvent newEv = MotionEvent.obtainNoHistory(ev);

        Log.d(getClass().getName(),"After ev.x = " + ev.getX() + " ev.y= " + ev.getY());

        // layoutParams.bottomMargin = -250;
       // layoutParams.rightMargin = -250;


        view.setLayoutParams(layoutParams);

        view.setOnTouchListener(this);

        mDragOverlayView.addView(view);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //view.dispatchTouchEvent(newEv);
            }
        }, 500);

    }



    @Override
    public void onStart(){
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(DROPPING_CAPSULE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mIntentFilter);
        super.onStart();
    }
    @Override
    public void onStop(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onStop();
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            Log.d(getClass().getSimpleName(), "Dropping capsule");
            int pos = intent.getIntExtra(EXTRA_CAPSULE, -1);
            if (pos != -1){
                capsules.remove(pos);
                mAdapter.notifyDataSetChanged();


                int x = intent.getIntExtra(EXTRA_X, -1);
                int y = intent.getIntExtra(EXTRA_Y, -1);
                MotionEvent ev = intent.getParcelableExtra(EXTRA_MOTION_EVENT);
                addDraggedViewToOverlay(ev, x,y);

            }


        }
    }



}
