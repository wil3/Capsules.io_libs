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

package io.capsules.android.lib.demo;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.capsules.DropCandidate;
import io.capsules.DropperView;

public class DropperDemoActivity extends FragmentActivity implements DropperView.Callback {
    private static String TAG = "DropperDemo";
    List<ColoredBoxDropCandidate> mItems = new ArrayList<ColoredBoxDropCandidate>();
    DropArrayAdapter adapter;
    DropperView dropperView;
    ListView mListView;
    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropper_demo);

        dropperView = (DropperView)findViewById(R.id.dropper);
        dropperView.setCallbackListener(this);
       // dropperView.setCandidateResourceId(R.layout.draggable_view);

        mListView = (ListView)findViewById(R.id.mylist);


        dropperView.setList(mListView);

        buildItemList();
        //Drop view
        adapter = new DropArrayAdapter(this,R.layout.listitem_colored_box, mItems);

        mListView.setAdapter(adapter);
       // dropperView.setAdapter(adapter);


        inflater = LayoutInflater.from(this);
        int i=0;
    }



    @Override
    public void onCandidateDropped(int index,View view) {

        onReset();
        Log.i(getClass().getName(), "Remove index " + index);


   mItems.remove(index);
   adapter.notifyDataSetChanged();

        //dropperView.addView(view);

        mListView.invalidate();
        dropperView.invalidate();

        /*
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 0;
        layoutParams.topMargin = 0;
        view.setLayoutParams(layoutParams);
        dropperView.addView(view);
        */
    }

    @Override
    public void onCandidateAdded(int index, View view) {
        ColoredBoxDropCandidate dc = (ColoredBoxDropCandidate)view.getTag();
        dc.setLabel(dc.getLabel());

        Log.d(TAG, " Adding (" + dc.getLabel() + ") back to list, at index= " + index );
        mItems.add(index, dc);
        adapter.notifyDataSetChanged();

    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }


    @Override
    public View createReleasedView(int index) {

        ColoredBoxDropCandidate obj = adapter.getItem(index);


        View view = inflater.inflate(R.layout.draggable_view, dropperView, false);
        //Set the object to the tag so we can retreive it later
        view.setTag(obj);
        view.setId(generateViewId());
Log.d(getClass().getName(), "View ID=" + view.getId());
        //View slideContainer = view.findViewById(R.id.obj);
        TextView textLabel = (TextView)view.findViewById(R.id.text_label);

        textLabel.setText("[" + obj.getLabel() + "]");
        int [] rgb = obj.getRgb();

       // textLabel.setBackgroundColor(Color.argb(255, rgb[0], rgb[1], rgb[2]));
        //slideContainer.setBackgroundColor(Color.argb(255, rgb[0], rgb[1], rgb[2]));


        //final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(50,50);
       // view.setLayoutParams(layoutParams);
        return view;
    }

    View mLastEnabledListItem;
    @Override
    public void onCandidateEnabled(ViewGroup listitem) {

/*
        TextView desc = (TextView)listitem.findViewById(R.id.description);
        Log.d(TAG,"Description to display " + desc.getText());
        desc.setVisibility(View.VISIBLE);
        mLastEnabledListItem = desc;
        */
    }

    @Override
    public void onCandidateEnabledStart(ViewGroup listitem) {
      //  if (mLastEnabledListItem != null) mLastEnabledListItem.setVisibility(View.GONE);

    }

    @Override
    public void onCandidateDisable(ViewGroup listitem) {

       // mLastEnabledListItem.setVisibility(View.GONE);
    }

    @Override
    public int getListItemCount() {
        return mItems.size();
    }

    @Override
    public void onListItemFocused(ViewGroup listitem) {
        if (mLastEnabledListItem != null) mLastEnabledListItem.setVisibility(View.GONE);

        TextView desc = (TextView)listitem.findViewById(R.id.description);
        desc.setVisibility(View.VISIBLE);
        mLastEnabledListItem = desc;
    }

    @Override
    public void onReset() {
        if (mLastEnabledListItem != null) mLastEnabledListItem.setVisibility(View.GONE);

    }

    private void buildItemList(){
        Log.i(getClass().getName(),"Building data list");
        for (int i=0; i< 30;i++){
            ColoredBoxDropCandidate dc = new ColoredBoxDropCandidate();
            dc.setLabel("" + i);
            int [] rgb = {50 * i, 10 * i, 50 * i};
            dc.setRgb(rgb);
            dc.setmDescription("Description " + i);
            mItems.add(dc);
        }
    }


}
