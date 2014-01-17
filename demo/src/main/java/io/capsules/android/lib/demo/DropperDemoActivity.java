package io.capsules.android.lib.demo;

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

import java.util.ArrayList;
import java.util.List;

import io.capsules.DropCandidate;
import io.capsules.DropperView;

public class DropperDemoActivity extends FragmentActivity implements DropperView.Callback {
    List<DropCandidate> mItems = new ArrayList<DropCandidate>();
    DropArrayAdapter adapter;
    DropperView dropperView;
    ListView mListView;
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

        int i=0;
    }



    @Override
    public void onCandidateDropped(int index) {

        Log.i(getClass().getName(), "Remove index " + index);


        mItems.remove(index);
        adapter.notifyDataSetChanged();

       // mListView.invalidate();

    }

    private void buildItemList(){
        Log.i(getClass().getName(),"Building data list");
        for (int i=0; i< 10;i++){
            DropCandidate dc = new ColoredBoxDropCandidate();
            dc.setLabel("" + i);
            mItems.add(dc);
        }
    }


}
