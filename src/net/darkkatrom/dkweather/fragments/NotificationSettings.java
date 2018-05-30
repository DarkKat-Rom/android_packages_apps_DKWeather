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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import net.darkkatrom.dkweather.R;
import net.darkkatrom.dkweather.utils.Config;
import net.darkkatrom.dkweather.utils.NotificationUtil;

public class NotificationSettings extends PreferenceFragment implements
        OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updatePreferenceScreen();
    }

    public void updatePreferenceScreen() {
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }
        addPreferencesFromResource(R.xml.notification_settings);

        if (!Config.getShowNotification(getActivity())) {
            removePreference(Config.PREF_KEY_SHOW_NOTIF_ONGOING);
            removePreference(Config.PREF_KEY_NOTIF_SHOW_LOCATION);
            removePreference(Config.PREF_KEY_NOTIF_SHOW_DK_ICON);
        }

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setSubtitle(R.string.action_bar_subtitle_settings_notification);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Config.PREF_KEY_SHOW_NOTIF)) {
            if (Config.getShowNotification(getActivity())) {
                sendNotification();
            } else {
                NotificationUtil.removeNotification(getActivity());
            }
            updatePreferenceScreen();
        } else if (key.equals(Config.PREF_KEY_SHOW_NOTIF_ONGOING)
                    || key.equals(Config.PREF_KEY_NOTIF_SHOW_LOCATION)
                    || key.equals(Config.PREF_KEY_NOTIF_SHOW_DK_ICON)) {
            sendNotification();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void sendNotification() {
        NotificationUtil notificationUtil = new NotificationUtil(getActivity());
        notificationUtil.sendNotification();
    }

    protected void removePreference(String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }
}
