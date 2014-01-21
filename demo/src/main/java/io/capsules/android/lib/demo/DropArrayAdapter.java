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
			  // holder.textName = (TextView)row.findViewById(R.id.text_capsule_title);
			 //  holder.imageIcon = (ImageView)row.findViewById(R.id.icon);
              // holder.slideView = row.findViewById(R.id.slide);
             //  holder.slideContainer = row.findViewById(R.id.obj);
                holder.textLabel = (TextView)row.findViewById(R.id.text_label);

			   row.setTag(holder);
			   
		   } else {
			   holder = (DropCandidateHolder)row.getTag();
		   }

          holder.position = position;

           final ColoredBoxDropCandidate item = mObjects.get(position);

           holder.textLabel.setText("(" + item.getLabel() + ")");
           int [] rgb = item.getRgb();
           //holder.slideContainer.setBackgroundColor(Color.argb(255,rgb[0],rgb[1],rgb[2] ));

           //mDragOverlayView = row.findViewById(R.id.container);
			//holder.textName.setText(capsule.getName());

//           SlideContainer container = (SlideContainer)row.findViewById(R.id.container);
 //          container.setTag(position);
   //        container.setDragView(holder.slideView);
          // holder.slideView.setTag();

           /**
            * Listen for touch events, this view can slide horizontally

           holder.slideView.setOnTouchListener(new View.OnTouchListener() {
               @Override
               public boolean onTouch(View view, MotionEvent motionEvent) {
                   final int X = (int) motionEvent.getRawX();
                   final int Y = (int) motionEvent.getRawY();

                   Log.d(getClass().getName(), "List item touched");

                   View parent = (View)view.getTag();

                   int relativeLeft = (int) motionEvent.getX() - view.getLeft();

                   switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                       case MotionEvent.ACTION_DOWN:
                           RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                           _xDelta = X - lParams.leftMargin;
                           _yDelta = Y - lParams.topMargin;
                           mInitialMotionX = (int)motionEvent.getX();
                           mInitialMotionY = (int)motionEvent.getY();
                           Log.d(getClass().getName(), "Down");

                           break;
                       case MotionEvent.ACTION_UP:
                           Log.d(getClass().getName(), "Up");

                           break;
                       case MotionEvent.ACTION_POINTER_DOWN:
                           break;
                       case MotionEvent.ACTION_POINTER_UP:
                           break;
                       case MotionEvent.ACTION_MOVE:

                           RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                           layoutParams.leftMargin = X - _xDelta;
                           view.setLayoutParams(layoutParams);
                           break;
                   }

                   ((View) view.getParent()).invalidate();
                   return true;
               }
           });
            */
			//imageLoader.displayImage(capsule.getThumbnail(), holder.imageIcon, Constants.OPTIONS_IMAGE_LOADER);
		
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
		//ImageView imageIcon;
      //  View slideView;
        View slideContainer;
	}
	
}
