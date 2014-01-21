package io.capsules;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wil on 1/14/14.
 */
public class DropperView extends RelativeLayout {

    private static final String TAG = "DroppedView";
    
    private enum ScrollingState {
        STATE_SCROLLING_IDLE,
        STATE_SCROLLING_DOWN,
        STATE_SCROLLING_UP

    }

    private enum ViewState {
        START,
        STOP
    }

    public interface Callback {
        public void onCandidateDropped(int index,View view);
        public View buildView(int index);
    }

    private Callback mCallback;
    public static int SLIDE_DURATION = 1000;

    /**
     * When scrolling occurs what is the delay before moving to the next list item
     */
    public static int SCROLL_DELAY = 100;

    public static int LONG_FOCUS_TIME = 750;//milli

    public static int HOVER_SLOP = 40;

    private static final String TAG_LIST = "listView";

    private ScrollingState mScrollingState = ScrollingState.STATE_SCROLLING_IDLE;

    /**
     * Last known view that is in the enabled position, that is,
     * it is slid to the left
     */
    private View mLastEnabledCandidate = null;

    private ViewState mViewState = ViewState.STOP;
    /**
     * The draw view that contains the list
     */
    private RelativeLayout mDragView;
    private int mXDelta;

    private final ViewDragHelper mDragHelper;

    private int mDisplayWidth;

    private int mDragRange;

    private float mInitialMotionX;
    private float mInitialMotionY;


    private ListView mListView;
   // private ArrayAdapter mListAdapter;

    private int mLastLeftPosition;


   // private View mScrolldownView;


    private Handler mHandler = new Handler();

    private List<View> mDroppedViews = new ArrayList<View>();


    private int mScrollIndex =0;


    /**
     * The index used to access the item in the adapter
     */
    private int mFocusedListItemIndex = -1;

    private ViewGroup mFocusedListItem;

    /**
     * The view in the list and to be dragged should be the same
     */
    private int mCandidateResourceId;

    /**
     * Current view that is dropped moving around, this is set when dropped and then when the view is
     * no longer focused will be null
     */
    private View mFocusedDroppedView;

    /**
     * Kept ready to drop
     */
    //private View mCandidateSkeleton;


    /**
     * State of the focused item
     */
    private enum CandidateState {
        /**
         * Nothing has happened yet, view probably isnt being used
         */
        NOT_STARTED,
        IN_ENABLED_POSITION,
       // IN_DISABLED_POSITION,
        READY_TO_MOVE
       // DETACHED_FROM_LIST
    }
    private CandidateState mCandidateState = CandidateState.NOT_STARTED;


    private MotionEvent mLastKnownMotionEvent;


    private boolean mTimerStarted = false;

    Timer mLongFocusTimer = new Timer();

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


    public void setCallbackListener(Callback callback){
        mCallback = callback;
    }
    private int _xDelta;
    private int _yDelta;

    public void setCandidateResourceId(int resourceId){

        mCandidateResourceId = resourceId;

    }

    /**
     *
     * @param view
     * @param left
     * @param top
     * @return
     */
    //TODO Add this to interface so it can be customized
    private View addEventListenersToReleasedView(final View view, int left, int top){


        String txt = ((TextView)((ViewGroup)view).getChildAt(0)).getText().toString();

        Log.v(TAG, "Setting listeners id=" + view.getId()) ;


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(50,50);
          //      75,75);
        //Init off screen
        layoutParams.leftMargin = left;
        layoutParams.topMargin = top;

        view.setLayoutParams(layoutParams);

        /**
         * On a long click we want to pick the view back up to be
         * moved or added back into the list
*/
        view.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, " On Long Click");
                return false;
            }
        });



        /**
         * When events are dispatched from the drop view they are events from the parent view,
         * when the view has been dropped the event coordinates are relative to the dropped view
         */
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                boolean consumed = false;

                //Releative to view
                final int X = (int) event.getX();
                final int Y = (int) event.getY();
                final int action = MotionEventCompat.getActionMasked(event);
                String txt = ((TextView)((ViewGroup)view).getChildAt(0)).getText().toString();

                Log.v(TAG, "Released view onTouch action x=" + X + " y=" + Y + " txt=" + txt) ;

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "Released view onTouch down");

                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        _xDelta = X - lParams.leftMargin;
                        _yDelta = Y - lParams.topMargin;

                        mInitialMotionX = X;
                        mInitialMotionY = Y;
                        break;
                    case MotionEvent.ACTION_UP:

                        RelativeLayout.LayoutParams layoutParams = ( RelativeLayout.LayoutParams)view.getLayoutParams();
                        layoutParams.leftMargin = X - (view.getWidth()/2);
                        layoutParams.topMargin = Y - (view.getHeight()/2);
                         view.setLayoutParams(layoutParams);

                        //no longer focused
                        mFocusedDroppedView = null;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.v(TAG, "Skeleton onTouch move");
                        int left = X - view.getLeft() - (view.getWidth()/2);
                        int top = Y - view.getTop() - (view.getHeight()/2);
                        view.offsetLeftAndRight(left);
                        view.offsetTopAndBottom(top);
/*
                        //TODO instead to detemrine where its coming from set a custom action or source or just make the parent
                        //dispatch do the conversion
                        if (mCandidateState != CandidateState.NOT_STARTED) { //forwarded event



                        } else { //event handle  directly by view

                            int dx = X - view.getLeft();//(int)mInitialMotionX;
                            int dy = Y - view.getTop();//(int)mInitialMotionY;


 //TODO WHEN A NEW VEIW IS ADDED AND LAYOUT IS CALLED DO THE OFFSETS GET CLEARED?!?!
                            view.offsetLeftAndRight(dx);
                            view.offsetTopAndBottom(dy);
                        }
                        */
                        consumed = true;
                        break;
                }
                DropperView.this.invalidate();
                return consumed;

            }
        });


        return view;
    }

    public void setList(ListView list){
        mListView = list;
    }
        @Override
    protected void onFinishInflate() {

        mDragView = (RelativeLayout)findViewWithTag("dragView");



      //  mListView = (ListView)findViewWithTag(TAG_LIST);

            super.onFinishInflate();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    closeDrawer();
                }
            },1500);

    }



    /**
     * Pass the touch screen motion event down to the target view, or this view if it is the target.
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent (MotionEvent ev){

        final int action = MotionEventCompat.getActionMasked(ev);


        boolean handled = super.dispatchTouchEvent(ev);
        Log.v(TAG, "Dispatch touch event handled? " + handled + " action= " + action);




            if (mFocusedDroppedView != null){
                String txt = ((TextView)((ViewGroup)mFocusedDroppedView).getChildAt(0)).getText().toString();

                Log.v(TAG, "Dispatch " + action + " touch event to the skelton " + txt);
                // ev.setAction(MotionEvent.ACTION_MOVE);

                final MotionEvent newEv = MotionEvent.obtainNoHistory(ev);


                mFocusedDroppedView.dispatchTouchEvent(newEv);
                return true;

            }


        //True if the event was handled by the view, false otherwise.
        return handled;

    }
        @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }



    /**
     * Intercept if the drawer is out or if teh candidate is released and being placed
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);


        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel();
            return false;
        }
        boolean intercept = mDragHelper.shouldInterceptTouchEvent(ev);



        final float x = ev.getX();
        final float y = ev.getY();
        boolean interceptTap = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = x;
                mInitialMotionY = y;
                Log.d(TAG, "On touch Down Intercept");

                  interceptTap = (mViewState == ViewState.START) ? true : false;

                final View view;
                if ((view = getDroppedView((int)x,(int)y))!= null){
                    mFocusedDroppedView = view;
                    Log.d(TAG, "On touch Down Intercept, dropped view hit");
                    interceptTap = true;
                }
               // interceptTap = mDragHelper.isViewUnder(mDragView, (int) x, (int) y);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final float adx = Math.abs(x - mInitialMotionX);
                final float ady = Math.abs(y - mInitialMotionY);
                final int slop = mDragHelper.getTouchSlop();

                int findX = getWidth() - (mDragView.getWidth() / 2);
                View v = mDragHelper.findTopChildUnder(findX,(int)y);
                Log.d(TAG, "On touch Intercept");


             //   if (mCandidateDetached){
              //      return true;
             //   }

                //check out the slop, if change too great cancel.
                // if the vertical change is greater than the horizontal change also cancel
               /* if (adx > slop && ady > adx) {
                    Log.d(TAG,"Cancelling event");

                    mDragHelper.cancel();
                    return false;
                }*/
            }
        }

        //return true to intercept
        Log.v(TAG, "Intercept? " + intercept + " Edge touched? " + interceptTap);
        return intercept || interceptTap;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        Log.d(TAG, "On touch x=" + ev.getX() + " y=" + ev.getY());

        final int action = MotionEventCompat.getActionMasked(ev);



        mDragHelper.processTouchEvent(ev);

        final float x = ev.getX();
        final float y = ev.getY();

        boolean isViewUnder = mDragHelper.isViewUnder(mDragView, (int) x, (int) y);

        boolean isMovingHorizontally = true;

        switch (action) {

            case MotionEvent.ACTION_UP: {

                //if (mFocusedDroppedView == null){
                    reset();
               // } else {
               //     closeDrawer();
               // }

                break;
            }
            case MotionEvent.ACTION_MOVE: {


                mLastKnownMotionEvent = ev;
            //    Log.v(TAG, "On touch x=" + (int) x + " y=" + (int) y );

/*
                final float dx = x - mInitialMotionX;

                int findX = getWidth() - (mDragView.getWidth() / 2);
                View v = mDragHelper.findTopChildUnder((int)x,(int)y);
                if (v == null) break;

                Log.v(TAG, "On touch x=" + (int) x + " y=" + (int) y + " dx= " + dx );

                if (v.getTag() != null){
                    String tag = (String)v.getTag();
                    if (tag.equals("scrolldown")){
                        Log.d(TAG,"Scroll down...");

                        //If we are not already scrolling...
                        if (mScrollingState != ScrollingState.STATE_SCROLLING_DOWN){
                            mScrollingState = ScrollingState.STATE_SCROLLING_DOWN;
                            //Keep scrolling until we are out

                            mScrollIndex =0;
                            scrollDown();
                        }
                    } else {
                        mScrollingState = ScrollingState.STATE_SCROLLING_IDLE;
                    }
                } else {
                    mScrollingState = ScrollingState.STATE_SCROLLING_IDLE;

                }

*/
             //   updateFocusedListItem((int) x, (int) y);

                if (isHoveredVerticalEdge(ViewDragHelper.EDGE_RIGHT, (int) x) && !isDrawerOpen()){
                    openDrawer();
                }



                //If were ditached just forward move events to the dropped view
                if (isADroppedViewFocused()){

                    //mDragHelper.cancel();


                    if (!mDragHelper.isViewUnder(mDragView, (int)x, (int)y) && isDrawerOpen()){
                        closeDrawer();
                    }
                    return true;
                }

                ViewGroup focusedListItem = (ViewGroup)getFocusedListItem((int) x, (int) y);



                //Handle the removable of the item from the list to be dropped
                if (!isUnderEnabledCandidate((int) x, (int) y)){
                    if (mTimerStarted){
                        cancelLongTouch();
                    }
                   // setCandidateState(CandidateState.IN_ENABLED_POSITION);
                } else {
                    Log.v(TAG, "Under enable candate " );
                    //Start the long touch if its not already in progress and the view is different
                    if (!mTimerStarted && !isADroppedViewFocused()){
                        startLongTouch();
                    }
                }


                //We are on a new focused item, this should only be
                if (focusedListItem != null && !focusedListItem.equals(mFocusedListItem)){

                    //If there was a previous enabled one disable it
                    if (mLastEnabledCandidate != null) disableCandidate(mLastEnabledCandidate);

                    View newCandidate = getDraggableViewInListRow(focusedListItem);
                    enableCandidate(newCandidate);
                    mLastEnabledCandidate = newCandidate;

                    mFocusedListItem = focusedListItem;
                }

                break;
            }
        }

        //true if event handled
        boolean handled = mCandidateState != CandidateState.NOT_STARTED || (mViewState == ViewState.START);
        Log.d(TAG, "Returning " + handled + " from touch event");


        return handled;//;//isViewUnder && isViewHit(mDragView, (int) x, (int) y) ;


    }


    private boolean isHoveredVerticalEdge(int edge, int x){

        if (edge == ViewDragHelper.EDGE_RIGHT){
            return getWidth() - HOVER_SLOP <= x && x <= getWidth();
        } else if (edge == ViewDragHelper.EDGE_LEFT){
            return 0 <= x && x <= HOVER_SLOP;
        }
        return false;
    }

    private void setCandidateState(CandidateState state){
        Log.d(TAG, "State changed " + state);
        mCandidateState = state;
    }
    private void cancelLongTouch(){
        mLongFocusTimer.cancel();
        mLongFocusTimer.purge();
        mTimerStarted = false;
    }

    private void startLongTouch(){
        //Is the timer already in progress?
        //Must be over the enabled candidate to start the timer and keep
        //your finger over it
        mLongFocusTimer = new Timer();
        mLongFocusTimer.schedule(new LongFocusTask(), LONG_FOCUS_TIME);
        mTimerStarted = true;
    }
    /**
     * When the view is slide to the enabled position, start the mLongFocusTimer to determine if
     * the user is over this view for a specified time. If so then release it from the
     * list
     */
    private void onCandidateEnabled(View view){

        setCandidateState(CandidateState.IN_ENABLED_POSITION);


    }
    private void onCandidateDisabled(View view){
       // mLongFocusTimer.cancel();
        //mLongFocusTimer.purge();
        //setCandidateState(CandidateState.IN_DISABLED_POSITION);

    }
    private boolean isDrawExtended(){
        return mLastLeftPosition == getWidth()-mDragView.getWidth();
    }


    private boolean isADroppedViewFocused(){
        return mFocusedDroppedView != null;
    }

    private void reset(){

        mViewState = ViewState.STOP;

        setCandidateState(CandidateState.NOT_STARTED);

        closeDrawer();

        //Clean up
        listReset();

    }
    /**
     * Start off fresh
     */
    private void listReset(){
        disableCandidate(mLastEnabledCandidate);
        mLastEnabledCandidate = null;
        mFocusedListItem = null;
    }

    private boolean isDrawerOpen(){

        Log.i(TAG, " drag view= " + mDragView.getLeft() + " w= " + getWidth());
        return mDragView.getLeft() != getWidth();
    }
    private boolean openDrawer(){
        final int leftBound = getWidth() - mDragView.getWidth();

        return slideTo(leftBound);

    }
    private boolean closeDrawer(){
        final int leftBound = getWidth();

        return slideTo(leftBound);

    }

    private boolean slideTo(int finalX){
        if (mDragHelper.smoothSlideViewTo(mDragView, finalX, mDragView.getTop())) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }


    /*
    private void setListItemToBeDragged(int x, int y){

        y -= getScreenOffset()[0];//correct from action bar

        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCandidateSkeleton.getLayoutParams();//new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 150;
        layoutParams.topMargin = y;



              //  mCandidateSkeleton.setLayoutParams(layoutParams);
              //  invalidate();

    }
    */
    /**
     * If we are hovering over an object to drop
     * @param x
     */
    private boolean shouldDrop(int x){

        return (getWidth() - mDragView.getWidth()) <= x && x <= (getWidth() -( mDragView.getWidth()/2)) ;
    }


    /**
     * Return the row view in the list according to the x,y position
     * @param x
     * @param y
     * @return
     */
    private View getFocusedListItem(int x, int y){
        final int xTest = getWidth() - (mDragView.getWidth()/2);
        ViewGroup listitemView = (ViewGroup)getListItemAtPosition(xTest, y);

        mFocusedListItemIndex = getListItemPosition(xTest, y);
        //mListView.setSelection(mFocusedListItemIndex);
        return listitemView;
    }

    /**
     * Get the view that is able to slide into enabled position to be removed from the list
     * @param listRow
     * @return
     */
    private View getDraggableViewInListRow(ViewGroup listRow){
        //Can and only should have 1 child view
        if (listRow.getChildCount() != 1) return null;


        View child = listRow.getChildAt(0);
        return child;

    }

    private void updateFocusedListItem(int x, int y){
        //This is the top root list element that is defined when creaeting a layout for the row
        final int xTest = getWidth() - (mDragView.getWidth()/2);
        ViewGroup listitemView = (ViewGroup)getListItemAtPosition(xTest, y);
        if (listitemView == null || listitemView.equals(mFocusedListItem)) return;
        //Can and only should have 1 child view
        if (listitemView.getChildCount() != 1) return;

        mFocusedListItemIndex = getListItemPosition(xTest, y);

        //Close the previous one
        if (mLastEnabledCandidate != null) disableCandidate(mLastEnabledCandidate);


        View child = listitemView.getChildAt(0);
        enableCandidate(child);

        mLastEnabledCandidate = child;

        mFocusedListItem = listitemView;
        Log.d(TAG, "List item " + mFocusedListItemIndex);
      //  listView.


    }

    /**
     * Get the dropped view that has been released from thel ist, if it is under these coorridiates
     * @param x
     * @param y
     * @return
     */
    private View getDroppedView(int x, int y){
        View found = null;
        for (View view : mDroppedViews){
            if (mDragHelper.isViewUnder(view, x, y)){
                found = view;
                break;
            }
        }

        return found;
    }


    /**
     * Determines if we are on top an enabled candidate
     * @param x
     * @param y
     * @return
     */
    private boolean isUnderEnabledCandidate(int x, int y){
        //Since we are trying to get the position of the view that went through animation,
        //The current position we query is not actually its position on the screen
        boolean isUnder = false;
        //We know the currently focused list item
        if (mLastEnabledCandidate == null) return false;

        if (mCandidateState == CandidateState.IN_ENABLED_POSITION){
            int[] xy = new int[2];
            mLastEnabledCandidate.getLocationOnScreen(xy);

            final int left = xy[0] - mLastEnabledCandidate.getWidth(); //it has slid over
            final int right = left + mLastEnabledCandidate.getWidth();
            final int top = xy[1] - getScreenOffset()[0];
            final int bottom = top + mLastEnabledCandidate.getHeight();

            isUnder = left <= x && x <= right && top <= y && y <= bottom;

            Log.v(TAG, "isUnderEnabledCandidate view l " + mLastEnabledCandidate.getLeft());

            Log.v(TAG, "isUnderEnabledCandidate l,r " + left + " < " + x + " < " + right);
            Log.v(TAG, "isUnderEnabledCandidate t,b " + top + " < " + y + " < " + bottom);
        }

        return isUnder;
    }

    /**
     * Move view into the ready state
     * @param view
     */
    private void enableCandidate(View view){

        //Slide the view to the left
        final float start = 0f;
        final float end = view.getWidth() * -1f;

        //This is relative to where the view currently is
        horizontalSlideAnimate(view,start, end);
    }


    /**
     * Release the view from the list
     */
    private void releaseViewFromList(){
       //mListView.removeView(mLastEnabledCandidate);
       //addView(mLastEnabledCandidate);

        mFocusedListItem.setVisibility(INVISIBLE);
        //mFocusedListItem.invalidate();

        View releasedView = mCallback.buildView(mFocusedListItemIndex);

closeDrawer();

        mFocusedListItem.setVisibility(VISIBLE);
        mCallback.onCandidateDropped(mFocusedListItemIndex,mLastEnabledCandidate);


        int leftMargin = getWidth() - mDragView.getWidth();
        int[] lt = new int[2];
        mFocusedListItem.getLocationOnScreen(lt);
        final int topMargin = lt[1] - getScreenOffset()[0];
        addEventListenersToReleasedView(releasedView, leftMargin, topMargin);



        //Now add it
        addView(releasedView);
        //  mListAdapter.notifyDataSetChanged();
        mDroppedViews.add(releasedView);
        mFocusedDroppedView = releasedView;


        /*
        if (mCandidateSkeleton != null){

            int[] xy = new int[2];
            mLastEnabledCandidate.getLocationOnScreen(xy);
            final int left = xy[0] - mLastEnabledCandidate.getWidth(); //it has slid over
            final int top = xy[1] - getScreenOffset()[0];

            final MotionEvent newEv = MotionEvent.obtainNoHistory(mLastKnownMotionEvent);
            newEv.setAction(MotionEvent.ACTION_MOVE);
            newEv.setLocation(left,top);

            mCandidateSkeleton.dispatchTouchEvent(newEv);
        }
        */
    }

    private void disableCandidate(View view){
        if (view == null) return;
        //Start where we left off...
        final float start =  view.getWidth() * -1f;
        //End at our resting place
        final float end = 0f;
        horizontalSlideAnimate(view, start, end);
    }
    private void horizontalSlideAnimate(final View view,  final float fromX, final float toX){
        TranslateAnimation anim = new TranslateAnimation(
                Animation.ABSOLUTE, fromX,
                Animation.ABSOLUTE, toX ,
                Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0 );
        anim.setDuration(SLIDE_DURATION);
        anim.setFillEnabled(true);
        anim.setFillAfter(true);

        //The animation does not actually change the position
        //of the view, it only changes the screen rendering
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                   RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)view.getLayoutParams();
                    if (toX < 0){



                        onCandidateEnabled(view);
                    } else {
                        onCandidateDisabled(view);
                    }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(anim);
    }

    private void scrollDown(){

        /*
        if (mScrollingState == ScrollingState.STATE_SCROLLING_DOWN && mScrollIndex < mListAdapter.getCount()){

            mListView.smoothScrollToPosition(mScrollIndex++);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollDown();
                }
            }, SCROLL_DELAY);

        } */
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

    private int getListItemPosition(int x, int y){
        final int childCount = mListView.getChildCount();
        for (int i = 0; i<childCount; i++) {
            final View child = mListView.getChildAt(i);

            DisplayMetrics dm = new DisplayMetrics();
            ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
            int topOffset = dm.heightPixels - getMeasuredHeight();

            int [] location  = new int[2];
            child.getLocationInWindow(location);
            final int left = location[0];
            final int right = location[0] + child.getWidth();
            final int top = location[1]-topOffset;
            final int bottom = top + child.getHeight();

          //  Log.d(TAG  , "X=" + x + " Y= " + y + " l " + left + " r " + right + " t " + top + " b " + bottom);
            if (x >= left && x < right &&
                    y >= top && y < bottom) {
                return i;//((DropArrayAdapter.DropCandidateHolder)child.getTag()).position;
            }
        }
        return -1;
    }
    private View getListItemAtPosition(int x, int y){

        int topOffset = getScreenOffset()[0];
        final int childCount = mListView.getChildCount();
        View foundView = null;
        for (int i = 0; i<childCount; i++) {
            final View child = mListView.getChildAt(i);


            int [] location  = new int[2];
            child.getLocationInWindow(location);
            final int left = location[0];
            final int right = location[0] + child.getWidth();
            final int top = location[1]-topOffset;
            final int bottom = top + child.getHeight();

            //  Log.d(TAG  , "X=" + x + " Y= " + y + " l " + left + " r " + right + " t " + top + " b " + bottom);
            if (x >= left && x < right &&
                    y >= top && y < bottom) {
                return child;
            }
        }
        return foundView;
    }

    /**
     *
     * @return index 0, top offset, index 1 left offset
     */
    private int[] getScreenOffset(){
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);

        int [] offset = new int[2];
       offset[0] = dm.heightPixels - getMeasuredHeight();
        offset[1] = dm.widthPixels - getMeasuredWidth();

        return offset;
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
        public void onViewDragStateChanged(int state){
            Log.v(TAG, "onViewDragStateChanged " + state);
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            Log.v(TAG, "onViewCaptured");

        }
        @Override
        public int getViewHorizontalDragRange(View child){
           // Log.v(TAG,"w = " + mDragView.getWidth());
            return mDragView.getWidth();
        }
        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            Log.d(TAG,"Edget touched");

            mViewState = ViewState.START;
            //Makes it so it can slide out
           if ( mDragHelper.smoothSlideViewTo(mDragView, getWidth()-mDragView.getWidth()/2, 0)){
               ViewCompat.postInvalidateOnAnimation(getRootView());
           }
            //super.onEdgeTouched(edgeFlags, pointerId);
        }

        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            Log.v(TAG,"Edge drag start");

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

            Log.v(TAG, "onViewRelease  l=" + getWidth() + " t=" + top);

            mDragHelper.settleCapturedViewAt(getWidth(), top);

        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            //Without invalidate the view leaves a black trail
            invalidate();
        }

        @Override
        public boolean tryCaptureView(View view, int i) {
          //  Log.v(TAG, "tryCaptureView");

            return view.equals(mDragView);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            //Log.d(TAG, "clampViewPositionHorizontal " + left + "," + dx);
            int newLeft;
            if (dx <= 0){ //only move to the left

                final int leftBound = getWidth()-mDragView.getWidth();//getPaddingLeft();

                final int rightBound = getWidth();// - mDragView.getWidth();

                newLeft = Math.min(Math.max(left, leftBound), rightBound);

                mLastLeftPosition = newLeft;

            } else {
                newLeft = mLastLeftPosition;
            }
            return newLeft;

        }


    }

/*
    public void setAdapter(ArrayAdapter adapter){
        mListAdapter = adapter;
        mListView.setAdapter(mListAdapter);
    }
*/

    Handler mLongFocusHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            Log.d(TAG, "DROP!");
            releaseViewFromList();
           // setCandidateState(CandidateState.READY_TO_MOVE);
            cancelLongTouch();

            return false;
        }
    });
    //tells handler to send a message
    class LongFocusTask extends TimerTask {

        @Override
        public void run() {
            mLongFocusHandler.sendEmptyMessage(0);
        }
    };

    }
