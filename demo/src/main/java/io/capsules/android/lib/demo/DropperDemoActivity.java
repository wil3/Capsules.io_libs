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

import io.capsules.DropCandidate;
import io.capsules.DropperView;

public class DropperDemoActivity extends FragmentActivity implements DropperView.Callback {
    List<ColoredBoxDropCandidate> mItems = new ArrayList<ColoredBoxDropCandidate>();
    DropArrayAdapter adapter;
    DropperView dropperView;
    ListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropper_demo);

        dropperView = (DropperView)findViewById(R.id.dropper);
        dropperView.setCallbackListener(this);
        dropperView.setCandidateResourceId(R.layout.draggable_view);

        mListView = (ListView)findViewById(R.id.mylist);


        dropperView.setList(mListView);

        buildItemList();
        //Drop view
        adapter = new DropArrayAdapter(this,R.layout.listitem_colored_box, mItems);

        mListView.setAdapter(adapter);
       // dropperView.setAdapter(adapter);



        int i=0;
    }



    @Override
    public void onCandidateDropped(int index,View view) {

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
    public View buildView(int index) {

        ColoredBoxDropCandidate obj = adapter.getItem(index);
        LayoutInflater inflater;
        inflater = LayoutInflater.from(this);

        View view = inflater.inflate(R.layout.draggable_view, null);

        View slideContainer = view.findViewById(R.id.obj);
        TextView textLabel = (TextView)view.findViewById(R.id.text_label);

        textLabel.setText("(" + obj.getLabel() + ")");
        int [] rgb = obj.getRgb();
        slideContainer.setBackgroundColor(Color.argb(255, rgb[0], rgb[1], rgb[2]));


        return view;
    }

    private void buildItemList(){
        Log.i(getClass().getName(),"Building data list");
        for (int i=0; i< 10;i++){
            ColoredBoxDropCandidate dc = new ColoredBoxDropCandidate();
            dc.setLabel("" + i);
            int [] rgb = {50 * i, 10 * i, 50 * i};
            dc.setRgb(rgb);
            mItems.add(dc);
        }
    }


}
