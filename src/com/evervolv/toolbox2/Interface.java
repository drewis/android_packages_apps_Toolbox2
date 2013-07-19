package com.evervolv.toolbox2;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

public class Interface extends TabFragment implements OnPreferenceChangeListener {

    private static final String TAG = "EVToolbox";

    private static final String BUTTONS_CATEGORY = "pref_interface_category_buttons";
    private static final String TABLETDPI_CATEGORY = "pref_interface_category_tabletdpi";

    private static final String ROTATION_PREF = "pref_interface_rotation";
    private static final String POWER_MENU_PREF = "pref_interface_power_menu";
    private static final String TRACKBALL_WAKE_TOGGLE = "pref_trackball_wake_toggle";
    private static final String VOLUME_WAKE_TOGGLE = "pref_volume_wake_toggle";
    private static final String LOCKSCREEN_MUSIC_CTRL_VOLBTN = "pref_lockscreen_music_controls_volbtn";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;
    private PreferenceScreen mRotation;
    private PreferenceScreen mPowerMenu;
    private PreferenceCategory mButtons;
    private PreferenceCategory mTabletDpi;
    private CheckBoxPreference mTrackballWake;
    private CheckBoxPreference mVolumeWake;
    private CheckBoxPreference mMusicCtrlVolBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.interface_settings);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();

        //categories
        mButtons = (PreferenceCategory) mPrefSet.findPreference(BUTTONS_CATEGORY);
        mTabletDpi = (PreferenceCategory) mPrefSet.findPreference(TABLETDPI_CATEGORY);

        //preferences
        mRotation = (PreferenceScreen) mPrefSet.findPreference(ROTATION_PREF);
        mPowerMenu = (PreferenceScreen) mPrefSet.findPreference(POWER_MENU_PREF);

        /* Trackball wake pref */
        mTrackballWake = (CheckBoxPreference) mPrefSet.findPreference(TRACKBALL_WAKE_TOGGLE);
        mTrackballWake.setChecked(Settings.System.getInt(mCr,
                Settings.System.TRACKBALL_WAKE_SCREEN, 1) == 1);
        /* Remove mTrackballWake on devices without trackballs */
        if (!getResources().getBoolean(R.bool.has_trackball)) {
            mButtons.removePreference(mTrackballWake);
        }


        /* Volume wake pref */
        mVolumeWake = (CheckBoxPreference) mPrefSet.findPreference(VOLUME_WAKE_TOGGLE);
        mVolumeWake.setChecked(Settings.System.getInt(mCr,
                Settings.System.VOLUME_WAKE_SCREEN, 0) == 1);

        /* Volume button music controls */
        mMusicCtrlVolBtn = (CheckBoxPreference) mPrefSet.findPreference(LOCKSCREEN_MUSIC_CTRL_VOLBTN);
        mMusicCtrlVolBtn.setChecked(Settings.System.getInt(mCr,
                Settings.System.LOCKSCREEN_MUSIC_CONTROLS_VOLBTN, 1) == 1);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

}
