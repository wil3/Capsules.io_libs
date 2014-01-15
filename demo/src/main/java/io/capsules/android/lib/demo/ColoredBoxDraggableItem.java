package io.capsules.android.lib.demo;

import io.capsules.DraggableItem;

/**
 * Created by wil on 1/15/14.
 */
public class ColoredBoxDraggableItem  implements DraggableItem{
    String mLabel;
    @Override
    public String getLabel() {
        return mLabel;
    }

    @Override
    public void setLabel(String label) {
        mLabel = label;
    }
}
