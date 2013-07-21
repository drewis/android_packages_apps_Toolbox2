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

package com.evervolv.toolbox2;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Window;

import com.evervolv.toolbox2.R;
import com.evervolv.toolbox2.tabs.InterfaceTab;
import com.evervolv.toolbox2.tabs.LockscreenTab;
import com.evervolv.toolbox2.tabs.PerformanceTab;
import com.evervolv.toolbox2.tabs.StatusbarTab;
import com.evervolv.toolbox2.tabs.UpdatesTab;

import java.util.ArrayList;

public class Toolbox extends FragmentActivity {

    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); 
        setContentView(R.layout.toolbox);
        
        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setTitle(R.string.app_name);
        // TODO: Fix this so home will be Settings 
        // bar.setDisplayHomeAsUpEnabled(true);

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_title_lockscreen),
                LockscreenTab.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_title_interface),
                InterfaceTab.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_title_statusbar),
                StatusbarTab.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_title_performance),
                PerformanceTab.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_title_updates),
                UpdatesTab.class, null);
    }

    static class TabsAdapter extends FragmentPagerAdapter
            implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
        
            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        public TabsAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());
            mContext = activity;
            mActionBar = activity.getActionBar();
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }

        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
            for (int i=0; i<mTabs.size(); i++) {
                if (mTabs.get(i) == tag) {
                    mViewPager.setCurrentItem(i);
                }
            }
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
        public void onPageScrollStateChanged(int state) { }
        public void onTabUnselected(Tab tab, FragmentTransaction ft) { }
        public void onTabReselected(Tab tab, FragmentTransaction ft) { }
    }

}
