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
         //  row.setBackgroundColor(Color.argb(255, position * 50, position * 10, position * 50));

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
	}
	
}
