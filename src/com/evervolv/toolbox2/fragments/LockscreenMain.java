package com.evervolv.toolbox2.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.evervolv.toolbox2.R;

public class LockscreenMain extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.lockscreen_main);
    }
}
