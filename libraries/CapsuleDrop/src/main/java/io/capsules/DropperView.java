
/*
 *
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


package io.capsules;

import android.content.Context;
import android.content.res.TypedArray;
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
 *
 *
 * A candidate view is one that can potientially be removed from the list
 *
 * A released view is one that has been released from the list
 *
 *
 *
 * Created by Wil Koch on 1/14/14.
 */
public class DropperView extends RelativeLayout {

    private static final String TAG = "DroppedView";
    
    private enum ScrollingState {
        STATE_SCROLLING_IDLE,
        STATE_SCROLLING_DOWN,
        STATE_SCROLLING_UP

    }

    private enum DrawerState {
        START,
        STOP
    }



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

    public interface Callback {
        public void onCandidateDropped(int index,View view);
        public void onCandidateAdded(int index,View view);

        public View createReleasedView(int index);
        public void onCandidateEnabled(ViewGroup listitem);
        public void onCandidateEnabledStart(ViewGroup listitem);

        public void onCandidateDisable(ViewGroup listitem);
        public int getListItemCount();

        public void onReset();
    }

    public static int SLIDE_DURATION = 500;

    /**
     * When scrolling occurs what is the delay before moving to the next list item
     */
    public static int SCROLL_DELAY = 100;

    public static int LONG_FOCUS_TIME = 750;//milli

    public static int HOVER_SLOP = 40;

    public static int SCROLL_ACTIVATION_HEIGHT = 50;

    private CandidateState mCandidateState = CandidateState.NOT_STARTED;

    /**
     * Callback for certain events
     */
    private Callback mCallback;

    private ScrollingState mScrollingState = ScrollingState.STATE_SCROLLING_IDLE;

    /**
     * Last known view that is in the enabled position, that is,
     * it is slid to the left
     */
    private View mLastEnabledCandidate = null;

    private DrawerState mDrawerState = DrawerState.STOP;
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


    /**
     * The list view used to display all viable candidates to be released
     */
    private ListView mListView;
   // private ArrayAdapter mListAdapter;

    private int mLastLeftPosition;


   // private View mScrolldownView;


    /**
     * Callback for UI events
     */
    private Handler mHandler = new Handler();

    /**
     * Keep track of all the released views
     */
    private List<View> mDroppedViews = new ArrayList<View>();


    private int mScrollIndex =0;


    /**
     * The index used to access the item in the adapter
     */
    private int mFocusedListItemIndex = -1;

    /**
     * The last list item index that was expanded
     */
    private int mLastExpandedViewIndex = -1;

    /**
     * The currently focused list item
     */
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

    private View mLastExpandedView;



    private MotionEvent mLastKnownMotionEvent;


    private boolean mTimerStarted = false;

    private int mListItemHeight;

    /**
     * Timer used to determine long touches
     */
    Timer mLongFocusTimer = new Timer();



    private int mDrawerResourceId;
    private int mReleasableResourceId;

    private int mCurrentY;

    public DropperView(Context context) {
        this(context, null);
    }
    public DropperView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }
    public DropperView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);

        init(attrs);

        mDragHelper = ViewDragHelper.create(this, 1.0f,new DragHelperCallback());
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT);

        Log.d(TAG, "Velocity= " + mDragHelper.getMinVelocity());

        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        mDisplayWidth = dm.widthPixels;


    }
    private void init( AttributeSet attrs){

        TypedArray a=getContext().obtainStyledAttributes(
                attrs,R.styleable.DropperView);

        mReleasableResourceId = a.getResourceId(R.styleable.DropperView_releasableView, -1);


        if (mReleasableResourceId == -1){
            throw new IllegalArgumentException("You must define a releasable view");
        }

        mDrawerResourceId = a.getResourceId(R.styleable.DropperView_drawerView, -1);
        if (mDrawerResourceId == -1){
            throw new IllegalArgumentException("You must define a drawer view");
        }

        mListItemHeight = a.getDimensionPixelOffset(R.styleable.DropperView_releasableViewHeight, -1);
        if (mListItemHeight == -1){
            throw new IllegalArgumentException("You must define the release view height");
        }

        if (a != null){
            a.recycle();
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b){

        final int count = getChildCount();

        // These are the far left and right edges in which we are performing layout.
        int leftPos = getPaddingLeft();
        int rightPos = r - l - getPaddingRight();


        // These are the top and bottom edges in which we are performing layout.
        final int parentTop = getPaddingTop();
        final int parentBottom = b - t - getPaddingBottom();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft = 0;
                int childRight= 0;
                int childTop= parentTop;
                int childBottom= 0;
                if (child.equals(mDragView)){
                    if (mDrawerState == DrawerState.STOP){
                        childLeft = getWidth();

                    } else {
                        childLeft = getWidth() - width;

                    }
                    childRight = childLeft + width;

                    childBottom = childTop + height;

                } else {
                    Log.v(TAG, "onLayout margins left= " + lp.leftMargin + " top= " + lp.topMargin );
                    Log.v(TAG, "onLayout left= " + child.getLeft() + " top= " + child.getTop());
                    Object tag = child.getTag(R.id.key_beforeLayout);
                    if (tag != null){
                        boolean beforeLayout = (Boolean) tag;
                        //This view is not in a layout yet so use the margins
                        if (beforeLayout) {

                            childLeft = lp.leftMargin;
                            childTop = lp.topMargin;


                            child.setTag(R.id.key_beforeLayout, false);
                        } else {

                           //Already in layout so only the left, right because it is changing by the drag
                            childLeft = child.getLeft();
                            childTop = child.getTop();

                        }
                    }
                    childRight = childLeft + width;
                    childBottom = childTop + height;

                }

                // Place the child.
                child.layout(childLeft, childTop,
                        childRight,childBottom);
            }
        }

    }

    @Override
    protected void onFinishInflate() {

        mDragView = (RelativeLayout)findViewById(mDrawerResourceId);

        mLastLeftPosition = getWidth() - mDragView.getWidth();

        super.onFinishInflate();

        //Start with drawer closed
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                closeDrawer();
            }
        },1500);

    }



    public void setCallbackListener(Callback callback){
        mCallback = callback;
    }



    /**
     *
     * @param view
     * @return
     */
    //TODO Add this logic to the view and skip the complexity of having to forward the events in the dispatcher
    private View addEventListenersToReleasedView(final View view){//}, int left, int top){

       // Log.v(TAG, "Setting listeners id=" + view.getId() + " l= " + left + " t= " + top) ;


     //   view.setLayoutParams(layoutParams);

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

                Log.v(TAG, "Released view onTouch action x=" + X + " y=" + Y ) ;
                RelativeLayout.LayoutParams layoutParams = ( RelativeLayout.LayoutParams)view.getLayoutParams();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "Released view onTouch down");

                        mInitialMotionX = X;
                        mInitialMotionY = Y;

                        //view.setTag(R.id.key_beforeLayout, true);
                        break;
                    case MotionEvent.ACTION_UP:

                        layoutParams.leftMargin = X - (view.getWidth()/2);
                        layoutParams.topMargin = Y - (view.getHeight()/2);

                        Log.d(TAG, "****Released view onTouch UP  "+  layoutParams.leftMargin + " t=" +  layoutParams.topMargin );

////////                        view.setLayoutParams(layoutParams);

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

                        consumed = true;
                        break;
                }

                //Causes black streak is not present
                DropperView.this.invalidate();
                return consumed;

            }
        });


        return view;
    }

    public void setList(ListView list){
        mListView = list;


    }



    /**
     * Pass the touch screen motion event down to the target view, or this view if it is the target.
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent (MotionEvent ev){

        final int action = MotionEventCompat.getActionMasked(ev);


        //Response from the
        boolean handled = super.dispatchTouchEvent(ev);
        Log.v(TAG, "Dispatch touch event handled? " + handled + " action= " + action);



            //If the event wasnt handled upstream pass it down

            if (mFocusedDroppedView != null && !handled){

                Log.v(TAG, "Dispatch " + action + " touch event to the skelton ");
                // ev.setAction(MotionEvent.ACTION_MOVE);

                final MotionEvent newEv = MotionEvent.obtainNoHistory(ev);


                mFocusedDroppedView.dispatchTouchEvent(newEv);
                return true;

            }


        //True if the event was handled by the view, false otherwise.
        return handled;

    }


    /**
     * Intercept if:
     * (1) The drawer is out and a touch is on the drawer
     * (2) A released view is touched
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

                  interceptTap = (mDrawerState == DrawerState.START) ? true : false;

                //See if there is a dropped view at this location
                final View view;
                if ((view = getDroppedView((int)x,(int)y)) != null){

                    mFocusedDroppedView = view;
                    Log.d(TAG, "On touch Down Intercept, dropped view hit");
                    interceptTap = true;
                } else {
                    interceptTap = mDragHelper.isViewUnder(mDragView, (int) x, (int) y);
                }
                break;
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

                //If we are done and there is an expanded view then we want to add this view in there
                //Perform animation to move the view to where it should be in the list and then
                //actually make it real by adding a new item to the listview
                if (mFocusedDroppedView != null && mLastExpandedView != null && (getWidth() - mDragView.getWidth() == mDragView.getLeft())) { //isDrawerOpen()){

                  //  updateRealPosition(mFocusedDroppedView);

                        addViewToList();


                    return true;
                } else {
                    Log.v(TAG, "Action up, resting");
                    reset();

                }

                break;
            }
            case MotionEvent.ACTION_MOVE: {


                mLastKnownMotionEvent = ev;
                mCurrentY = (int) y;


                Log.v(TAG, "expanded index=" + mLastExpandedViewIndex );


                int firstVisiblePosition = mListView.getFirstVisiblePosition();
                int lastVisiblePosition = mListView.getLastVisiblePosition();
                ScrollingState scrollDirection = determineScrollDirection((int)x, (int)y);

                //Are we already scrolling?
                if (scrollDirection == ScrollingState.STATE_SCROLLING_UP && firstVisiblePosition > 0){
                    if (mScrollingState != ScrollingState.STATE_SCROLLING_UP){
                        mScrollIndex = firstVisiblePosition;
                        mScrollingState = ScrollingState.STATE_SCROLLING_UP;
                        scrollUp();
                    }

                } else if (scrollDirection == ScrollingState.STATE_SCROLLING_DOWN && lastVisiblePosition < mCallback.getListItemCount()){
                    if (mScrollingState != ScrollingState.STATE_SCROLLING_DOWN) {
                        Log.d(TAG, " Last visible position=" + lastVisiblePosition);
                        mScrollIndex = lastVisiblePosition;
                        mScrollingState = ScrollingState.STATE_SCROLLING_DOWN;
                        scrollDown();
                    }

                } else {
                    mScrollingState = ScrollingState.STATE_SCROLLING_IDLE;
                }


                if (isHoveredVerticalEdge(ViewDragHelper.EDGE_RIGHT, (int) x) && !isDrawerOpen()){
                    openDrawer();
                }







//If one of the dropped views is moving
                //If were ditached just forward move events to the dropped view
                if (isADroppedViewFocused()){
                    //mDragHelper.cancel();

                    //If we arent over the drawer then have it closed
                    //TODO only close if fully open?
                    if (!mDragHelper.isViewUnder(mDragView, (int)x, (int)y) && (getWidth() - mDragView.getWidth() == mDragView.getLeft())) {//isDrawerOpen()){
                        closeDrawer();
                    }

                   attemptExpand((int)x,(int)y);

                    //Let the touch event be forwarded to the actual focused view
                    return false;

                } else {

                    Log.v(TAG, "Touch, move not by focused released view");
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



                    ViewGroup focusedListItem = (ViewGroup)getFocusedListItem((int) x, (int) y);

                //We are on a new focused item in the list then do animation to enable new
                    //view and disable last view
                    if (focusedListItem != null && !focusedListItem.equals(mFocusedListItem)){




                            //If there was a previous enabled one disable it
                            if (mLastEnabledCandidate != null) disableCandidate(mLastEnabledCandidate);

                            View newCandidate = getDraggableViewInListRow(focusedListItem);
                            enableCandidate(newCandidate);
                            mLastEnabledCandidate = newCandidate;



                        mFocusedListItem = focusedListItem;
                    }
                }
                break;
            }
        }

        //true if event handled
        boolean handled = mCandidateState != CandidateState.NOT_STARTED || (mDrawerState == DrawerState.START);
        Log.v(TAG, "Returning " + handled + " from touch event");


        return handled;//;//isViewUnder && isViewHit(mDragView, (int) x, (int) y) ;


    }

    private ScrollingState determineScrollDirection(int x, int y){

        //If touching top

       //

        int[] xy = new int[2];
        mDragView.getLocationOnScreen(xy);

        int left = xy[0];
        int right = getWidth();
        int top = xy[1] - getScreenOffset()[0]; //Subtract screen because x,y is only for the view, not screen
        int bottom = top + mDragView.getHeight();

        ScrollingState state = ScrollingState.STATE_SCROLLING_IDLE;
        if (left <= x && x <= right){

            if (top <= y && y <= top + SCROLL_ACTIVATION_HEIGHT){
                state = ScrollingState.STATE_SCROLLING_UP;
            } else if (bottom - SCROLL_ACTIVATION_HEIGHT <= y && y <= bottom){
                state = ScrollingState.STATE_SCROLLING_DOWN;
            }
        }

        return state;
    }

    private void collapseListItemView(View view){
        view.setPadding(0,0,0,0);
    }

    /**
     * Collapse with out animation
     * @param view
     */
    private void quickCollapseListItemView(View view){
        view.setPadding(0,0,0,0);
    }

    /**
     * Move the view to the specified location
     * @param view
     * @param toLeft
     * @param toTop
     */
    private void moveFreedViewInList(View view, int toLeft, int toTop){

        float dx =  toLeft - view.getLeft();
        float dy =  toTop - view.getTop();
        TranslateAnimation anim = new TranslateAnimation(
                Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, dx ,
                Animation.ABSOLUTE, 0,
                Animation.ABSOLUTE, dy);
        anim.setDuration(SLIDE_DURATION);
        anim.setFillEnabled(true);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {


              continueWithAddingReleasedViewToList();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(anim);
        int i=0;
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

    private boolean isDrawerExtended(){
        return mLastLeftPosition == getWidth()-mDragView.getWidth();
    }


    private boolean isADroppedViewFocused(){
        return mFocusedDroppedView != null;
    }

    /**
     * Reset the state of the view
     */
    private void reset(){

        mDrawerState = DrawerState.STOP;

        setCandidateState(CandidateState.NOT_STARTED);

        closeDrawer();

        //Clean up
        listReset();
        mFocusedDroppedView = null;

        mCallback.onReset();
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

        Log.i(TAG, "is Drawer Open? " + mDragView.getLeft() + " ?= " + (getWidth() - mDragView.getWidth()));

        //TODO fix me, this should be true to the actual state, in this return it could be in progress
        //return mDragView.getLeft() == getWidth() - mDragView.getWidth();
        return mDrawerState != DrawerState.STOP;
    }
    private boolean openDrawer(){
        final int leftBound = getWidth() - mDragView.getWidth();

        mDrawerState = DrawerState.START;

        return slideTo(leftBound);

    }
    private boolean closeDrawer(){

        Log.v(TAG, "Closing drawer, state=" + mDragHelper.getViewDragState());

        final int leftBound = getWidth();

        mDrawerState = DrawerState.STOP;
        return slideTo(leftBound);

    }


    private boolean slideTo(int finalX){
        if (mDragHelper.smoothSlideViewTo(mDragView, finalX, mDragView.getTop())) {

            ViewCompat.postInvalidateOnAnimation(this); //Invalidate on the next animation frame
            return true;
        }
        return false;
    }
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
       int pos = mListView.pointToPosition(x,y);

        ViewGroup listitemView = (ViewGroup)getListItemAtPosition(xTest, y);

        updateFocusedListItemIndex(xTest,y);

        //mListView.setSelection(mFocusedListItemIndex);
        return listitemView;
    }


    private void attemptExpand(int x, int y){

        final int xTest = getWidth() - (mDragView.getWidth()/2);
        ViewGroup listitemView = (ViewGroup)getListItemAtPosition(xTest, y);
        if (listitemView == null) return;
        int wantedPosition =  mListView.getPositionForView(listitemView);//getListIndexByView(listitemView);

        //If child not found no view to expand
        if (wantedPosition == -1){
            Log.w(TAG, " Index could not be found for view " + listitemView);
            return;
        }
        if (listitemView == null){
            Log.w(TAG, "List item could not be found at position x=" + xTest + " y=" + y);
            return;
        }

        if (mLastExpandedView != null) collapseListItemView(mLastExpandedView);
Log.d(TAG, " attempting to expand x=" + x + " y=" + y);
        //TOP HALF,padding on the top
        if (listitemView.getTop() <= y && listitemView.getTop() + listitemView.getHeight()/2 >= y){
            listitemView.setPadding(0,mListItemHeight,0,0);

        } else { //Bottom half, padding on the bottom

            listitemView.setPadding(0,0,0,mListItemHeight);

        }


        mLastExpandedView = listitemView;
        mLastExpandedViewIndex = wantedPosition;



            //  handled = true;

    }

    /**
     * Determine which view in the list will be used to expand when we are carrying a view to try and
     * place it back in the list
     * @param x
     * @param y
     * @return
     */
    private View getViewToExpand(int x, int y){
        final int xTest = getWidth() - (mDragView.getWidth()/2);
        ViewGroup listitemView = (ViewGroup)getListItemAtPosition(xTest, y);
      //  updateFocusedListItemIndex(xTest, y);

        int wantedPosition =  mListView.getPositionForView(listitemView);//getListIndexByView(listitemView);

        //If child not found no view to expand
        if (wantedPosition == -1){
            Log.w(TAG, " Index could not be found for view " + listitemView);
            return null;
        }
        if (listitemView == null){
            Log.w(TAG, "List item could not be found at position x=" + xTest + " y=" + y);
            return null;
        }

        if (wantedPosition == 0){ //its the first item so we need to expand from the top
            //TOP HALF,
            if (listitemView.getTop() <= y && listitemView.getTop() + listitemView.getHeight()/2 >= y){

                return listitemView;
            }
        } else {
            //TOP HALF,
            if (listitemView.getTop() <= y && listitemView.getTop() + listitemView.getHeight()/2 >= y){


            } else { //Bottom half, expand next
                if (wantedPosition <  mCallback.getListItemCount()-1){
                    wantedPosition += 1;
                    return (ViewGroup)getViewByPosition(wantedPosition);

                } else {

                }

            }
        }

        return null;
    }


    private View getViewByPosition(int wantedPosition){
        int firstPosition = mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount(); // This is the same as child #0
        int wantedChild = wantedPosition - firstPosition;
// Say, first visible position is 8, you want position 10, wantedChild will now be 2
// So that means your view is child #2 in the ViewGroup:
        if (wantedChild < 0 || wantedChild >= mListView.getChildCount()) {
            Log.w(TAG, "Unable to get view for desired position, because it's not being displayed on screen.");
            return null;
        }
// Could also check if wantedPosition is between listView.getFirstVisiblePosition() and listView.getLastVisiblePosition() instead.
        View wantedView = mListView.getChildAt(wantedChild);
        return wantedView;
    }

    /**
     * Expand the list item so a released view can be added to it
     * @param view
     */
    private void expandListItemView(final View view ){

        RelativeLayout.LayoutParams layoutParams = ( RelativeLayout.LayoutParams)mDragView.getLayoutParams();
        Log.v(TAG, "Expand list view dragview l=" + layoutParams.leftMargin + " r=" + layoutParams.rightMargin);

        layoutParams.rightMargin = 0;
//////        mDragView.setLayoutParams(layoutParams);



/*
        final int newLeftMargin = 50;

        Animation a = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
               int padding = (int)(newLeftMargin * interpolatedTime);
                view.setPadding(0,0,0,padding);

            }
        };

        view.startAnimation(a);
*/


        view.setPadding(0,mListItemHeight,0,0);

        mLastExpandedView = view;
        mLastExpandedViewIndex = mListView.getPositionForView(view);

    }
    private int getListIndexByView(View view){
        int foundIndex = -1;
        final int childCount = mCallback.getListItemCount();
        for (int i = 0; i < childCount; i++) {
            final View child = mListView.getChildAt(i);
            if (child.equals(view)){
                foundIndex = i;
                break;

            }
        }

        return foundIndex;
    }

    /**
     * Get the view that is able to slide into enabled position to be removed from the list
     * @param listRow
     * @return
     */
    private View getDraggableViewInListRow(ViewGroup listRow){
        //Can and only should have 1 child view
       // if (listRow.getChildCount() != 1) return null;


       // View child = listRow.getChildAt(0);

        return listRow.findViewById(mReleasableResourceId);

    }


    private void updateFocusedListItemIndex(int x, int y){
        if (mListView == null) return;
        int focusedListItem;

        final int xTest = getWidth() - (mDragView.getWidth()/2);
        ViewGroup listitemView = (ViewGroup)getListItemAtPosition(xTest, y);
        if (listitemView == null) return;
        int wantedPosition =  mListView.getPositionForView(listitemView);//getListIndexByView(listitemView);

        if (wantedPosition != -1){
            mFocusedListItemIndex = wantedPosition;
        } else {
            Log.w(TAG, "No list item at x=" + x + " y=" + y);
        }
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
     * Add the focused dropped view to the list
     */
    private void addViewToList(){
        int toLeft = getWidth()  - mFocusedDroppedView.getWidth();
        //Because the view is expanded with padding from next view
        int toTop = (mLastExpandedView.getPaddingBottom() == 0) ? mLastExpandedView.getTop() :
                mLastExpandedView.getTop() + mListItemHeight; //relative to list view
        Log.v(TAG, "Action up, snapping view back l=" + toLeft + " t=" + toTop + " t padding=" + mLastExpandedView.getPaddingTop() + " b padding=" + mLastExpandedView.getPaddingBottom());

        //Move the view into position
        moveFreedViewInList(mFocusedDroppedView, toLeft, toTop);

    }
    /**
     * Called after animation has finished
     */
    private void continueWithAddingReleasedViewToList(){
        //Now actually add it to the list and determine where it should go in the list
        //Look at the padding to see which side it is on, if padding on the bottom then add after the expanded row
        int listPosition = (mLastExpandedView.getPaddingTop() == 0) ? mLastExpandedViewIndex + 1 : mLastExpandedViewIndex;
        mCallback.onCandidateAdded(listPosition, mFocusedDroppedView);

        //The view is added to the list so collapse
        quickCollapseListItemView(mLastExpandedView);
        //Remove the view
        removeView(mFocusedDroppedView);

        mFocusedDroppedView= null;

        mLastEnabledCandidate = null;
        //onReset();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                closeDrawer();
            }
        }, 1000);
    }


    /**
     * Release the view from the list
     */
    private void releaseViewFromList(){
        Log.d(TAG, "Release View from List");
       //mListView.removeView(mLastEnabledCandidate);
       //addView(mLastEnabledCandidate);
    if (mFocusedListItem == null) return;
     //   if (mLastEnabledCandidate != null)
     disableCandidate(mLastEnabledCandidate, 0, new Animation.AnimationListener() {
         @Override
         public void onAnimationStart(Animation animation) {
         }

         @Override
         public void onAnimationEnd(Animation animation) {
                continueWithReleasingViewFromList();
         }

         @Override
         public void onAnimationRepeat(Animation animation) {
         }
     });



    }

    private void continueWithReleasingViewFromList(){
        //   mFocusedListItem.setVisibility(INVISIBLE);
        //mFocusedListItem.invalidate();

        //Expand previous list item to look like its the space where we were
        Log.d(TAG,"Expanding view before releasing view index=" + mFocusedListItemIndex);
        int i = (mFocusedListItemIndex == 0) ? mFocusedListItemIndex + 1: mFocusedListItemIndex;
        View listItemToExpand =getViewByPosition(mFocusedListItemIndex);
        expandListItemView(listItemToExpand);
        //mLastExpandedView = listItemToExpand;


        View releasedView = mCallback.createReleasedView(mFocusedListItemIndex);

mHandler.postDelayed(new Runnable() {
    @Override
    public void run() {
        closeDrawer();

    }
},1000);
        //     mFocusedListItem.setVisibility(VISIBLE);

        //This will rebuild the list
        mCallback.onCandidateDropped(mFocusedListItemIndex,null);

        int leftMargin = getWidth() - (mLastEnabledCandidate.getWidth()*2);
        int[] lt = new int[2];
        mFocusedListItem.getLocationOnScreen(lt);
        final int topMargin = lt[1] - getScreenOffset()[0];
        addEventListenersToReleasedView(releasedView);//, leftMargin, topMargin);


        Log.v(TAG, "Release view l= " + leftMargin + " t= " + topMargin) ;


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mLastEnabledCandidate.getWidth(), mLastEnabledCandidate.getHeight());
        //      75,75);
        //Init off screen
        layoutParams.leftMargin = leftMargin;
        layoutParams.topMargin = topMargin;
        // releasedView.setLeft(left);
        //  releasedView.setTop(top);

        //Now add it
        releasedView.setTag(R.id.key_beforeLayout, true);
        addView(releasedView, layoutParams);
        //  mListAdapter.notifyDataSetChanged();
        mDroppedViews.add(releasedView);
        mFocusedDroppedView = releasedView;

        mLastEnabledCandidate = null;
    }
    /**
     * When the view is slide to the enabled position, start the mLongFocusTimer to determine if
     * the user is over this view for a specified time. If so then release it from the
     * list
     */
    private void onCandidateEnabled(View view){
        Log.v(TAG, "On candidate enable");

        setCandidateState(CandidateState.IN_ENABLED_POSITION);

        final int xTest = getWidth() - (mDragView.getWidth()/2);
        updateFocusedListItemIndex(xTest, mCurrentY);


        mCallback.onCandidateEnabled(mFocusedListItem);
    }

    /**
     * Callback when the candidate is in the disabled position
     * @param view
     */
    private void onCandidateDisabled(View view){
        Log.v(TAG, "On candidate disable");

        // mLongFocusTimer.cancel();
        //mLongFocusTimer.purge();
        //setCandidateState(CandidateState.IN_DISABLED_POSITION);
        mCallback.onCandidateDisable(null);
    }

    private void quickEnableCandidate(View view){

    }

    /**
     * Move view into the ready state
     * @param view
     */
    private void enableCandidate(final View view){
if (mFocusedDroppedView != null) return;
        Log.v(TAG, "Enable candidate");
        //Slide the view to the left
        final float start = 0f;
        final float end = view.getWidth() * -1f;



        Animation.AnimationListener listener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

                mCallback.onCandidateEnabledStart((ViewGroup)view);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onCandidateEnabled(view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        };

        //This is relative to where the view currently is
        horizontalSlideAnimate(view,SLIDE_DURATION,start, end, listener);
    }

   // private void quickDisableCandidate(View view){
   //     disableCandidate(view, 0);
   // }
    private void disableCandidate(final View view){

        Animation.AnimationListener listener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onCandidateDisabled(view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        };


        disableCandidate(view, SLIDE_DURATION, listener);
    }
    private void disableCandidate(final View view, int duration, Animation.AnimationListener listener){


        if (view == null) return;
        Log.v(TAG, "Disable candidate");

        //Start where we left off...
        final float start =  view.getWidth() * -1f;
        //End at our resting place
        final float end = 0f;

        horizontalSlideAnimate(view,duration, start, end, listener);

    }
        private void horizontalSlideAnimate(final View view, int duration, final float fromX, final float toX, Animation.AnimationListener listener){
        TranslateAnimation anim = new TranslateAnimation(
                Animation.ABSOLUTE, fromX,
                Animation.ABSOLUTE, toX ,
                Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0 );
        anim.setDuration(duration);
        anim.setFillEnabled(true);
        anim.setFillAfter(true);

        //The animation does not actually change the position
        //of the view, it only changes the screen rendering
            if (listener != null){
        anim.setAnimationListener(listener);
            }
        view.startAnimation(anim);
    }

    /**
     *
     */
    private void scrollDown(){

        Log.v(TAG, "Scroll down state = " + mScrollingState + " index= "  + mScrollIndex);
        //Recursively scroll until the we no longer want to scroll or we reach  the end
        if (mScrollingState == ScrollingState.STATE_SCROLLING_DOWN && mScrollIndex <= mCallback.getListItemCount()){

            mListView.smoothScrollToPosition(mScrollIndex++);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollDown();
                }
            }, SCROLL_DELAY);

        }

    }
    /**
     *
     */
    private void scrollUp(){

        //Recursively scroll until the we no longer want to scroll or we reach  the end
        if (mScrollingState == ScrollingState.STATE_SCROLLING_UP && mScrollIndex >= 0){

            mListView.smoothScrollToPosition(mScrollIndex--);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollUp();
                }
            }, SCROLL_DELAY);

        }

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


    /**
     * Only can get the list item if it is being displayed, if scrolled and items arent displayed they will not be analyzed
     * @param x
     * @param y
     * @return
     */
    //TODO this is going to cause poor performance, redo
    private View getListItemAtPosition(int x, int y){

        int topOffset = getScreenOffset()[0];
        final int childCount = mListView.getChildCount();
        View foundView = null;
        for (int i = 0; i<childCount; i++) {
            final View child = mListView.getChildAt(i);


            int [] location  = new int[2];
          //  child.getLocationInWindow(location);
            child.getLocationOnScreen(location);
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

            mDrawerState = DrawerState.START;

            //When the edge is touched lets slide out half way
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

        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {


            Log.d(TAG, "onViewPositionChanged  l=" + left);

            //Without invalidate the view leaves a black trail
            invalidate();
        }

        @Override
        public boolean tryCaptureView(View view, int i) {
          //  Log.v(TAG, "tryCaptureView");

            return view.equals(mDragView);
        }


        /**
         * Handles the actual moving of the drawer
         *
         * @param child
         * @param left
         * @param dx
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            int newLeft;
            if (dx <= 0){ //only move to the left

                final int leftBound = getWidth()-mDragView.getWidth();//getPaddingLeft();

                final int rightBound = getWidth();// - mDragView.getWidth();

                newLeft = Math.min(Math.max(left, leftBound), rightBound);

                mLastLeftPosition = newLeft;

            } else {
                newLeft = mLastLeftPosition;
            }

            Log.v(TAG, "clampViewPositionHorizontal newleft " + newLeft + "," + dx);

            return newLeft;

        }


    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
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
