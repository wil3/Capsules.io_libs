package io.capsules.android.lib.demo;

import android.support.v4.app.FragmentActivity;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import io.capsules.DropArrayAdapter;
import io.capsules.DropCandidate;
import io.capsules.DropperView;

public class DropperDemoActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropper_demo);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dropper_demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            DropperView dropperView = (DropperView)inflater.inflate(R.layout.fragment_dropper_demo, container, false);

            ListAdapter adapter = new DropArrayAdapter(getActivity().getApplicationContext(),R.layout.listitem_colored_box, getListItems());

            dropperView.setAdapter(adapter);

            return dropperView;
        }


        private List<DropCandidate> getListItems(){

            List<DropCandidate>  items = new ArrayList<DropCandidate>();
            for (int i=0; i< 20;i++){
                DropCandidate dc = new ColoredBoxDropCandidate();
                dc.setLabel("Label " + i);
                items.add(dc);
            }
            return items;
        }
    }

}
