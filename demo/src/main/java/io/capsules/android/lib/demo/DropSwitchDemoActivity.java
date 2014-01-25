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

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ToggleButton;

import io.capsules.DropSwitchView;

public class DropSwitchDemoActivity extends Activity {

    private DropSwitchView mSwitch;
    private View mSwitchSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_switch_demo);

      //  mSwitch = (DropSwitchView)findViewById(R.id.io_capsules_switch);

        mSwitchSlider = findViewById(R.id.switch_slider);
    }


    public void onToggleClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();

        if (on) {
            horizontalSlideAnimate(mSwitchSlider, 0f, -50);
            //Animation anim = AnimationUtils.loadAnimation(this, io.capsules.R.anim.switch_on);

           // mSwitchSlider.startAnimation(anim);
       //    mSwitch.switchOn();
        } else {
            horizontalSlideAnimate(mSwitchSlider,  -50f,0f);

            //     mSwitch.switchOff();
        }
    }

    private void slide(){
        TranslateAnimation anim = new TranslateAnimation( 0, 100 , 0, 0 );
        anim.setDuration(1000);
        anim.setFillAfter( true );
        mSwitchSlider.startAnimation(anim);
    }

    private void horizontalSlideAnimate(View view,  float fromX, float toX){
        TranslateAnimation anim = new TranslateAnimation(
                Animation.ABSOLUTE, fromX,
                Animation.ABSOLUTE, toX ,
                Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0 );
        anim.setDuration(1000);
        anim.setFillAfter( true );
        view.startAnimation(anim);
    }

}
