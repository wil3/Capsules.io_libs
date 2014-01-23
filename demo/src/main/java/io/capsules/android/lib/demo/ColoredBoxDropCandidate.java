package io.capsules.android.lib.demo;

import io.capsules.DropCandidate;

/**
 * Created by wil on 1/15/14.
 */
public class ColoredBoxDropCandidate implements DropCandidate {
    String mLabel;

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    String mDescription;
    public int[] getRgb() {
        return rgb;
    }

    public void setRgb(int[] rgb) {
        this.rgb = rgb;
    }

    int[] rgb;
    @Override
    public String getLabel() {
        return mLabel;
    }

    @Override
    public void setLabel(String label) {
        mLabel = label;
    }
}
