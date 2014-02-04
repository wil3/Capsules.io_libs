package io.capsules.slidelayout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by wil on 2/3/14.
 */
public class SlideLayoutScrollView extends ScrollView {


    public interface ScrollViewListener {
        void onScrollChanged(SlideLayoutScrollView scrollView,
                             int x, int y, int oldx, int oldy);
    }

    private ScrollViewListener scrollViewListener = null;

    public SlideLayoutScrollView(Context context) {
        super(context);
    }

    public SlideLayoutScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideLayoutScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }
}
