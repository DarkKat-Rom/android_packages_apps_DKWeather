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
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import net.darkkatrom.dkweather.R;
import net.darkkatrom.dkweather.utils.Config;
import net.darkkatrom.dkweather.utils.NotificationUtil;

public class NotificationSettings extends PreferenceFragment implements
        OnSharedPreferenceChangeListener {

    private SwitchPreference mShow;
    private SwitchPreference mShowOngoing;
    private SwitchPreference mShowLocation;
    private SwitchPreference mShowDKIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notification_settings);
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);

        mShow = (SwitchPreference) findPreference(Config.PREF_KEY_SHOW_NOTIF);

        mShowOngoing = (SwitchPreference) findPreference(Config.PREF_KEY_SHOW_NOTIF_ONGOING);

        mShowLocation =
                (SwitchPreference) findPreference(Config.PREF_KEY_NOTIF_SHOW_LOCATION);

        mShowDKIcon =
                (SwitchPreference) findPreference(Config.PREF_KEY_NOTIF_SHOW_DK_ICON);

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setSubtitle(R.string.action_bar_subtitle_settings_notification);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == mShow.getKey()) {
            if (mShow.isChecked()) {
                sendNotification();
            } else {
                NotificationUtil.removeNotification(getActivity());
            }
        } else if (key == mShowOngoing.getKey()
                    || key == mShowLocation.getKey()
                    || key == mShowDKIcon.getKey()) {
            sendNotification();
        }
    }

    private void sendNotification() {
        NotificationUtil notificationUtil = new NotificationUtil(getActivity());
        notificationUtil.sendNotification();
    }
}
