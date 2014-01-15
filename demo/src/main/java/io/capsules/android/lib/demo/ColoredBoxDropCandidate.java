package io.capsules.android.lib.demo;

import io.capsules.DropCandidate;

/**
 * Created by wil on 1/15/14.
 */
public class ColoredBoxDropCandidate implements DropCandidate {
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
