/*
 *  Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.android.settings.cmremix.animations;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.ViewConfiguration;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.util.Log;
import android.text.TextUtils;

import com.android.settings.cmremix.utils.SeekBarPreferenceCham;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

public class ScrollAnimationInterfaceSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "ScrollAnimationInterfaceSettings";

    private static final String ANIMATION_FLING_VELOCITY = "animation_fling_velocity";
    private static final String ANIMATION_SCROLL_FRICTION = "animation_scroll_friction";
    private static final String ANIMATION_OVERSCROLL_DISTANCE = "animation_overscroll_distance";
    private static final String ANIMATION_OVERFLING_DISTANCE = "animation_overfling_distance";
    private static final float MULTIPLIER_SCROLL_FRICTION = 10000f;
    private static final String ANIMATION_NO_SCROLL = "animation_no_scroll";

    private static final int MENU_RESET = Menu.FIRST;

    private ContentResolver mResolver;
    private Context mContext;

    private SeekBarPreferenceCham mAnimationFling;
    private SeekBarPreferenceCham mAnimationScroll;
    private SeekBarPreferenceCham mAnimationOverScroll;
    private SeekBarPreferenceCham mAnimationOverFling;
    private SwitchPreference mAnimNoScroll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.scroll_animation_interface_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity().getApplicationContext();
        mResolver = mContext.getContentResolver();

        mAnimNoScroll = (SwitchPreference) prefSet.findPreference(ANIMATION_NO_SCROLL);
        mAnimNoScroll.setChecked(Settings.CMREMIX.getInt(mResolver,
                Settings.CMREMIX.ANIMATION_CONTROLS_NO_SCROLL, 0) == 1);
        mAnimNoScroll.setOnPreferenceChangeListener(this);

        float defaultScroll = Settings.CMREMIX.getFloat(mResolver,
                Settings.CMREMIX.CUSTOM_SCROLL_FRICTION, ViewConfiguration.DEFAULT_SCROLL_FRICTION);
        mAnimationScroll = (SeekBarPreferenceCham) prefSet.findPreference(ANIMATION_SCROLL_FRICTION);
        mAnimationScroll.setValue((int) (defaultScroll * MULTIPLIER_SCROLL_FRICTION));
        mAnimationScroll.setOnPreferenceChangeListener(this);

        int defaultFling = Settings.CMREMIX.getInt(mResolver,
                Settings.CMREMIX.CUSTOM_FLING_VELOCITY, ViewConfiguration.DEFAULT_MAXIMUM_FLING_VELOCITY);
        mAnimationFling = (SeekBarPreferenceCham) prefSet.findPreference(ANIMATION_FLING_VELOCITY);
        mAnimationFling.setValue(defaultFling);
        mAnimationFling.setOnPreferenceChangeListener(this);

        int defaultOverScroll = Settings.CMREMIX.getInt(mResolver,
                Settings.CMREMIX.CUSTOM_OVERSCROLL_DISTANCE, ViewConfiguration.DEFAULT_OVERSCROLL_DISTANCE);
        mAnimationOverScroll = (SeekBarPreferenceCham) prefSet.findPreference(ANIMATION_OVERSCROLL_DISTANCE);
        mAnimationOverScroll.setValue(defaultOverScroll);
        mAnimationOverScroll.setOnPreferenceChangeListener(this);

        int defaultOverFling = Settings.CMREMIX.getInt(mResolver,
                Settings.CMREMIX.CUSTOM_OVERFLING_DISTANCE, ViewConfiguration.DEFAULT_OVERFLING_DISTANCE);
        mAnimationOverFling = (SeekBarPreferenceCham) prefSet.findPreference(ANIMATION_OVERFLING_DISTANCE);
        mAnimationOverFling.setValue(defaultOverFling);
        mAnimationOverFling.setOnPreferenceChangeListener(this);

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_backup) // use the backup icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog  = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.reset);
        alertDialog.setMessage(R.string.animation_settings_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetAllValues();
                resetAllSettings();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetAllValues() {
        mAnimationFling.setValue(ViewConfiguration.DEFAULT_MAXIMUM_FLING_VELOCITY);
        mAnimationScroll.setValue((int) (ViewConfiguration.DEFAULT_SCROLL_FRICTION * MULTIPLIER_SCROLL_FRICTION));
        mAnimationOverScroll.setValue(ViewConfiguration.DEFAULT_OVERSCROLL_DISTANCE);
        mAnimationOverFling.setValue(ViewConfiguration.DEFAULT_OVERFLING_DISTANCE);
        mAnimNoScroll.setChecked(false);
    }

    private void resetAllSettings() {
        setProperVal(mAnimationFling, ViewConfiguration.DEFAULT_MAXIMUM_FLING_VELOCITY);
        Settings.CMREMIX.putFloat(mResolver,
                   Settings.CMREMIX.CUSTOM_SCROLL_FRICTION, ViewConfiguration.DEFAULT_SCROLL_FRICTION);
        setProperVal(mAnimationOverScroll, ViewConfiguration.DEFAULT_OVERSCROLL_DISTANCE);
        setProperVal(mAnimationOverFling, ViewConfiguration.DEFAULT_OVERFLING_DISTANCE);
        setProperVal(mAnimNoScroll, 0);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mAnimNoScroll) {
            boolean value = (Boolean) objValue;
            Settings.CMREMIX.putInt(mResolver, Settings.CMREMIX.ANIMATION_CONTROLS_NO_SCROLL, value ? 1 : 0);
        } else if (preference == mAnimationScroll) {
            int val = ((Integer)objValue).intValue();
            Settings.CMREMIX.putFloat(mResolver,
                   Settings.CMREMIX.CUSTOM_SCROLL_FRICTION,
                   ((float) (val / MULTIPLIER_SCROLL_FRICTION)));
        } else if (preference == mAnimationFling) {
            int val = ((Integer)objValue).intValue();
            Settings.CMREMIX.putInt(mResolver,
                    Settings.CMREMIX.CUSTOM_FLING_VELOCITY,
                    val);
        } else if (preference == mAnimationOverScroll) {
            int val = ((Integer)objValue).intValue();
            Settings.CMREMIX.putInt(mResolver,
                    Settings.CMREMIX.CUSTOM_OVERSCROLL_DISTANCE,
                    val);
        } else if (preference == mAnimationOverFling) {
            int val = ((Integer)objValue).intValue();
            Settings.CMREMIX.putInt(mResolver,
                    Settings.CMREMIX.CUSTOM_OVERFLING_DISTANCE,
                    val);
        } else {
            return false;
        }
        return true;
    }

    private void setProperVal(Preference preference, int val) {
        String mString = "";
        if (preference == mAnimNoScroll) {
            mString = Settings.CMREMIX.ANIMATION_CONTROLS_NO_SCROLL;
        } else if (preference == mAnimationFling) {
            mString = Settings.CMREMIX.CUSTOM_FLING_VELOCITY;
        } else if (preference == mAnimationOverScroll) {
            mString = Settings.CMREMIX.CUSTOM_OVERSCROLL_DISTANCE;
        } else if (preference == mAnimationOverFling) {
            mString = Settings.CMREMIX.CUSTOM_OVERFLING_DISTANCE;
        }

        Settings.CMREMIX.putInt(mResolver, mString, val);
    }

}
