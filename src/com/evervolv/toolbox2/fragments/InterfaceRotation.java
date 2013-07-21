package com.evervolv.toolbox2.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

import com.evervolv.toolbox2.R;

public class InterfaceRotation extends PreferenceFragment {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.interface_rotation);
    }

}
