/*
 * Copyright (C) 2016 DarkKat
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

package net.darkkatrom.dkweather.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;

import net.darkkatrom.dkweather.R;
import net.darkkatrom.dkweather.WeatherLocationTask;
import net.darkkatrom.dkweather.WeatherService;
import net.darkkatrom.dkweather.WeatherInfo;
import net.darkkatrom.dkweather.preferences.CustomLocationPreference;
import net.darkkatrom.dkweather.utils.Config;

public class SettingsFragment extends PreferenceFragment implements
        OnPreferenceChangeListener, WeatherLocationTask.Callback  {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    private SharedPreferences mPrefs;

    private SwitchPreference mEnable;
    private ListPreference mUpdateInterval;
    private EditTextPreference mOWMApiKey;
    private ListPreference mUnits;
    private SwitchPreference mCustomLocation;
    private CustomLocationPreference mLocation;
//    private SwitchPreference mShowSettingsShortcut;

    private boolean mTriggerUpdate;
    private boolean mTriggerPermissionCheck;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        int intvalue;

        mEnable = (SwitchPreference) findPreference(Config.PREF_KEY_ENABLE);
        mEnable.setOnPreferenceChangeListener(this);

        mUpdateInterval = (ListPreference) findPreference(Config.PREF_KEY_UPDATE_INTERVAL);
        intvalue = Config.getUpdateInterval(getActivity());
        mUpdateInterval.setValue(String.valueOf(intvalue));
        mUpdateInterval.setSummary(mUpdateInterval.getEntry());
        mUpdateInterval.setOnPreferenceChangeListener(this);

        mOWMApiKey = (EditTextPreference) findPreference(Config.PREF_KEY_OWM_API_KEY);
        mOWMApiKey.getEditText().setHint(getResources().getString(
                R.string.default_api_key_title));
        final int summaryResId = Config.getAPIKey(getActivity()).equals(Config.DEFAULT_OWM_API_KEY)
                ? R.string.default_api_key_title : R.string.custom_api_key_summary;
        mOWMApiKey.setSummary(getResources().getString(summaryResId));
        mOWMApiKey.setOnPreferenceChangeListener(this);

        mUnits = (ListPreference) findPreference(Config.PREF_KEY_UNITS);
        intvalue = Config.getUnit(getActivity());
        mUnits.setValue(String.valueOf(intvalue));
        mUnits.setSummary(mUnits.getEntry());
        mUnits.setOnPreferenceChangeListener(this);

        mCustomLocation = (SwitchPreference) findPreference(Config.PREF_KEY_CUSTOM_LOCATION);
        mCustomLocation.setOnPreferenceChangeListener(this);

        mLocation = (CustomLocationPreference) findPreference(Config.PREF_KEY_CUSTOM_LOCATION_CITY);
        if (mPrefs.getBoolean(Config.PREF_KEY_ENABLE, false)
                && !mPrefs.getBoolean(Config.PREF_KEY_CUSTOM_LOCATION, false)) {
            mTriggerUpdate = false;
            checkLocationEnabled();
        }

//        mShowSettingsShortcut = (SwitchPreference) findPreference(Config.PREF_KEY_SHOW_SETTINGS_SHORTCUT);
//        mShowSettingsShortcut.setChecked(Config.getShowSettingsShortcut(getActivity()));
//        mShowSettingsShortcut.setOnPreferenceChangeListener(this);

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTriggerPermissionCheck) {
            checkLocationPermissions();
            mTriggerPermissionCheck = false;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean value;
        int intValue;
        int index;

        if (preference == mEnable) {
            value = (Boolean) newValue;
            if (value) {
                if (!mCustomLocation.isChecked()) {
                    mTriggerUpdate = true;
                    checkLocationEnabled();
                } else {
                    WeatherService.scheduleUpdate(getActivity());
                }
            } else {
                disableService();
            }
            return true;
        } else if (preference == mUpdateInterval) {
            intValue = Integer.valueOf((String) newValue);
            index = mUpdateInterval.findIndexOfValue((String) newValue);
            preference.setSummary(mUpdateInterval.getEntries()[index]);
            WeatherService.scheduleUpdate(getActivity());
            return true;
        } else if (preference == mOWMApiKey) {
            String stringValue = (String) newValue;
            boolean isDefaultkey = stringValue == null || stringValue.isEmpty()
                    || stringValue.equals(Config.DEFAULT_OWM_API_KEY);
            String summary = getResources().getString(isDefaultkey
                    ? R.string.default_api_key_title : R.string.custom_api_key_summary);
            preference.setSummary(summary);
            return true;
        } else if (preference == mUnits) {
            intValue = Integer.valueOf((String) newValue);
            index = mUnits.findIndexOfValue((String) newValue);
            preference.setSummary(mUnits.getEntries()[index]);
            WeatherService.startUpdate(getActivity(), true);
            return true;
        } else if (preference == mCustomLocation) {
            value = (Boolean) newValue;
            if (!value) {
                mTriggerUpdate = true;
                checkLocationEnabled();
            } else {
                if (Config.getLocationName(getActivity()) != null) {
                    // city ids are provider specific - so we need to recheck
                    // cause provider migth be changed while unchecked
                    new WeatherLocationTask(getActivity(), Config.getLocationName(getActivity()),
                            this).execute();
                } else {
                    disableService();
                }
            }
            return true;
//        } else if (preference == mShowSettingsShortcut) {
//            value = (Boolean) newValue;
//            mShowSettingsShortcut.setChecked(value);
//            Config.setShowSettingsShortcut(getActivity(), value);
        }
        return false;
    }
    
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Dialog dialog;

        // Build and show the dialog
        builder.setTitle(R.string.weather_retrieve_location_dialog_title);
        builder.setMessage(R.string.weather_retrieve_location_dialog_message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.weather_retrieve_location_dialog_enable_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mTriggerPermissionCheck = true;
                        mTriggerUpdate = true;
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, null);
        dialog = builder.create();
        dialog.show();
    }

    private void checkLocationPermissions() {
        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            if (mTriggerUpdate) {
                WeatherService.scheduleUpdate(getActivity());
                mTriggerUpdate = false;
            }
        }
    }

    private void checkLocationEnabled() {
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            showDialog();
        } else {
            checkLocationPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mTriggerUpdate) {
                        WeatherService.scheduleUpdate(getActivity());
                        mTriggerUpdate = false;
                    }
                }
                break;
            }
        }
    }

    private void disableService() {
        // stop any pending
        WeatherService.cancelUpdate(getActivity());
        WeatherService.removeNotification(getActivity());
    }

    @Override
    public void applyLocation(WeatherInfo.WeatherLocation result) {
        Config.setLocationId(getActivity(), result.id);
        Config.setLocationName(getActivity(), result.city);
        mLocation.setText(result.city);
        mLocation.setSummary(result.city);
        WeatherService.startUpdate(getActivity(), true);
    }
}
