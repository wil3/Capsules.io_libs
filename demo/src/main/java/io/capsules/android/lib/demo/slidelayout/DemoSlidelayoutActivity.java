package io.capsules.android.lib.demo.slidelayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        SlideLayout mSlideLayout;
        private static final float SCALED_SCROLL = 1f;
        private int mTotalPixels;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_demo_slidelayout_page1,
                    container, false);
            mSlideLayout = (SlideLayout)rootView.findViewById(R.id.slidelayout);
            mSlideLayout.setOnScrollOverflowListner(this);
            mSlideLayout.setMainContentView(R.id.content);
            mSlideLayout.setHeaderView(R.id.header);
            mSlideLayout.setBodyView(R.id.body);
            mSlideLayout.setFillerView(R.id.filler);
            mSlideLayout.setCoverView(R.id.image_wrapper);
            mSlideLayout.setScrollViewListener(this);
            return rootView;
        }

        @Override
        public void onStartOverflow() {

            //if (mViewPager.isFakeDragging()) return;
            boolean ok = mViewPager.beginFakeDrag();
            mTotalPixels = 0;
            Log.d(TAG, "start fake " + ok);

        }

        @Override
        public void onStopOverflow() {
            Log.d(TAG, "end fake");
            if (mViewPager.isFakeDragging()){
                mViewPager.endFakeDrag();
            }
        }

        @Override
        public void onTopScrollOverflow(float offset) {
            float pixels =  mViewPager.getWidth() * offset * SCALED_SCROLL;
            Log.d(TAG, "Page viewer  " + pixels);

            mTotalPixels += pixels;
            mSlideLayout.setLeftOffset(mTotalPixels);
            mViewPager.fakeDragBy(-pixels);
        }

        @Override
        public void onBottomScrollOverflow(float offset) {

        }

        @Override
        public void onScrollChanged(View scrollView, int x, int y, int oldx, int oldy) {
            View cover = findViewById(R.id.image_wrapper);

            View root = findViewById(R.id.rootview);
            View body = findViewById(R.id.body);

            int[] xy = new int[2];
            body.getLocationInWindow(xy);


            int newHeight = xy[1];// +  root.getPaddingTop();
         //   if (newHeight <= root.getHeight()){
                ViewGroup.LayoutParams params = cover.getLayoutParams();
                params.height =  newHeight;

              //  Log.d(TAG, " cover h " + newHeight);
                cover.setLayoutParams(params);
         //   }
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
