/*
 * Copyright (C) 2013 The Evervolv Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evervolv.toolbox2.tabs;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.evervolv.toolbox2.fragments.PerformanceMain;

public class PerformanceTab extends PreferenceFragment {
    
    private static final String TAG = "EVToolbox";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // TODO: This is temporary until we split it up into tabs
        // Processor / Memory Management?
        FrameLayout view = new FrameLayout(getActivity().getApplicationContext());
        view.setId(10101010);
        PreferenceFragment frag = new PerformanceMain();
        FragmentTransaction ft = getFragmentManager().beginTransaction(); 
        ft.add(view.getId(), frag);
        ft.commit();
        return view;
    }

}
