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

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v13.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.evervolv.toolbox2.R;
import com.evervolv.toolbox2.fragments.UpdatesGapps;
import com.evervolv.toolbox2.fragments.UpdatesNightlies;
import com.evervolv.toolbox2.fragments.UpdatesReleases;
import com.evervolv.toolbox2.fragments.UpdatesTesting;
import com.evervolv.toolbox2.updates.UpdatesFragment;

import java.util.List;

public class UpdatesTab extends PreferenceFragment {

    private FragmentTabHost mTabHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), container.getId());

        mTabHost.addTab(mTabHost.newTabSpec("nightlies").setIndicator("N"),
                UpdatesNightlies.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("releases").setIndicator("R"),
                UpdatesReleases.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("testing").setIndicator("T"),
                UpdatesTesting.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("gapps").setIndicator("G"),
                UpdatesGapps.class, null);

        return mTabHost;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.updates_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                UpdatesFragment frag = (UpdatesFragment) getChildFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
                frag.checkForUpdates();
                return true;
        }
        return false;
    }
    
}
