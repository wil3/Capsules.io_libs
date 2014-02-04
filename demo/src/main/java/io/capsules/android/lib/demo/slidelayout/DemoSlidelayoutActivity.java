package io.capsules.android.lib.demo.slidelayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import io.capsules.android.lib.demo.R;
import io.capsules.slidelayout.SlideLayout;

public class DemoSlidelayoutActivity extends FragmentActivity {

    private static final String TAG = "DemoSlidelayoutActivity";


    SectionsPagerAdapter mSectionsPagerAdapter;

    private FixedViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_slidelayout);

        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        mViewPager = (FixedViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);




    }


    class Page1 extends Fragment implements SlideLayout.ScrollOverflowListener, SlideLayout.ScrollViewListener{

        private SlideLayout mSlideLayout;
        public static final float SCALED_SCROLL = 1f;
        private int mFakeLeftOffset;
        private View mCoverView;
        private View mBodyView;
        /**
         * The invisible view above the header used for layout reasons
         */
        private View mSpacerView;

        private boolean mIsCollapsed = true;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            final View rootView = inflater.inflate(R.layout.fragment_demo_slidelayout_page1,
                    container, false);
            mSlideLayout = (SlideLayout)rootView.findViewById(R.id.slidelayout);
            mSlideLayout.setScrollOverflowListener(this);
            mSlideLayout.setScrollViewListener(this);

            //We need to dynamicaly determine the height of the spacer so the header is at the bottom
            mSpacerView = rootView.findViewById(R.id.spacer);
            mSpacerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mIsCollapsed) {
                        mSlideLayout.smoothScrollTo(0,rootView.getHeight()/2);
                        mIsCollapsed = false;
                    } else {
                        mSlideLayout.smoothScrollTo(0,0);
                        mIsCollapsed = true;
                    }
                }
            });
            ViewTreeObserver vto = rootView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (rootView.getViewTreeObserver().isAlive()) {
                        // only need to calculate once, so remove listener
                        rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    ViewGroup.LayoutParams params = mSpacerView.getLayoutParams();
                    params.height = rootView.getHeight();
                    mSpacerView.setLayoutParams(params);

                }
            });

           mCoverView = rootView.findViewById(R.id.cover);
           mBodyView = rootView.findViewById(R.id.body);

            return rootView;
        }

        @Override
        public void onStartOverflow(SlideLayout.ScrollResting restingPosition) {

            boolean ok = mViewPager.beginFakeDrag();
            mFakeLeftOffset = 0;
            Log.d(TAG, "Start fake drag? " + ok);

        }
        @Override
        public void onStopOverflow(SlideLayout.ScrollResting restingPosition) {
            Log.v(TAG, "Ending fake drag");
            if (mViewPager.isFakeDragging()){
                mViewPager.endFakeDrag();
            }
        }
        @Override
        public void onTopScrollOverflow(float percent) {
            fakeDragBy(percent);
        }
        @Override
        public void onBottomScrollOverflow(float percent) {
            fakeDragBy(-percent);
        }
        private void fakeDragBy(float offset){
            Log.v(TAG, "Fake drag by " + offset);
            float pixels =  mViewPager.getWidth() * offset * SCALED_SCROLL;
            mFakeLeftOffset += pixels;
            mSlideLayout.setFakeLeftOffset(Math.abs(mFakeLeftOffset));
            mViewPager.fakeDragBy(-pixels);
        }

        @Override
        public void onScrollChanged(View scrollView, int x, int y, int oldX, int oldY) {

            int[] xy = new int[2];
            mBodyView.getLocationInWindow(xy);

            int newHeight = xy[1];
                ViewGroup.LayoutParams params = mCoverView.getLayoutParams();
                params.height =  newHeight;
                mCoverView.setLayoutParams(params);
        }
    }


    class Page2 extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_demo_slidelayout_page2,
                    container, false);

            return rootView;
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private static final int NUMBER_OF_PAGES = 2;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {

            if (position == 0){
                return new Page1();
            } else {
                    return new Page2();
                }
        }
        @Override
        public int getCount() {
            return NUMBER_OF_PAGES;
        }


    }




}
