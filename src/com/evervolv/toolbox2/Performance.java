package com.evervolv.toolbox2;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

public class Performance extends TabFragment {

    private static final String MEM_MANAGEMENT = "pref_memory_management";
    private static final String PROCESSOR_PREF = "pref_processor";

    private PreferenceScreen mPrefSet;
    private PreferenceScreen mMemoryManagement;
    private PreferenceScreen mProcessor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.performance_settings);

        mPrefSet = getPreferenceScreen();

        mMemoryManagement = (PreferenceScreen) mPrefSet.findPreference(MEM_MANAGEMENT);

        mProcessor = (PreferenceScreen) mPrefSet.findPreference(PROCESSOR_PREF);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return false;
    }

}
