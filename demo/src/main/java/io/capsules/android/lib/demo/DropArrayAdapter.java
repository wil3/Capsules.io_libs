package io.capsules.android.lib.demo;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.List;

import io.capsules.*;


public class DropArrayAdapter extends ArrayAdapter<ColoredBoxDropCandidate> {

	private Context mContext;
	private int mViewResourceId;
	private List<ColoredBoxDropCandidate> mObjects;
	//protected ImageLoader imageLoader = ImageLoader.getInstance();
	private TextView mBtnSize;
    private int _xDelta;
    private int _yDelta;
    private int mInitialMotionX;
    private int mInitialMotionY;
    private int targetLeft = 50;

    private View _root;

    public DropArrayAdapter(Context context, int viewResourceId, List<ColoredBoxDropCandidate> objects){
		super(context, viewResourceId, objects);
		this.mContext = context;
		this.mViewResourceId = viewResourceId;
		this.mObjects = objects;
	}


    @Override
    public ColoredBoxDropCandidate getItem(int position){
        return mObjects.get(position);
    }
	   @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
		   
		   View row = convertView;


           DropCandidateHolder holder = null;

           if (row == null){
               LayoutInflater inflater = LayoutInflater.from(mContext);

               row = inflater.inflate(mViewResourceId, parent, false);
			   
			   
			   holder = new DropCandidateHolder();
                holder.textLabel = (TextView)row.findViewById(R.id.text_label);
               holder.textDescription = (TextView)row.findViewById(R.id.description);

			   row.setTag(holder);
			   
		   } else {
			   holder = (DropCandidateHolder)row.getTag();
		   }

          holder.position = position;

           final ColoredBoxDropCandidate item = mObjects.get(position);

           holder.textLabel.setText("(" + item.getLabel() + ")");

           holder.textDescription.setText(item.getmDescription());
           int [] rgb = item.getRgb();

		   return row;

		
	}


	/**
	 * Store the UI in a holder so we can access it faster
	 * @author wil
	 *
	 */
	static class DropCandidateHolder {
		int position;
		TextView textLabel;
        TextView textDescription;

        //ImageView imageIcon;
      //  View slideView;
        View slideContainer;
	}
	
}
