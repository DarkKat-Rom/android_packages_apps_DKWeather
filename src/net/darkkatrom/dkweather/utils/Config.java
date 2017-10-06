/*
 *  Copyright (C) 2015 The OmniROM Project
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
package net.darkkatrom.dkweather.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import net.darkkatrom.dkweather.providers.AbstractWeatherProvider;
import net.darkkatrom.dkweather.providers.OpenWeatherMapProvider;
import net.darkkatrom.dkweather.WeatherInfo;

public class Config {
    public static final String PREF_KEY_ENABLE                 = "enable";
    public static final String PREF_KEY_AUTO_UPDATE            = "auto_update";
    public static final String PREF_KEY_UPDATE_INTERVAL        = "update_interval";
    public static final String PREF_KEY_PROVIDER               = "provider";
    public static final String PREF_KEY_OWM_API_KEY            = "owm_api_key";
    public static final String PREF_KEY_UNITS                  = "units";
    public static final String PREF_KEY_CUSTOM_LOCATION        = "custom_location";
    public static final String PREF_KEY_CUSTOM_LOCATION_CITY   = "weather_custom_location_city";
    public static final String PREF_KEY_SHOW_SETTINGS_SHORTCUT = "show_settings_shortcut";
    public static final String PREF_KEY_LOCATION_ID            = "location_id";
    public static final String PREF_KEY_LOCATION_NAME          = "location_name";
    public static final String PREF_KEY_WEATHER_DATA           = "weather_data";
    public static final String PREF_KEY_LAST_UPDATE            = "last_update";

    public static final String PREF_KEY_SHOW_NOTIF          = "show_notification";
    public static final String PREF_KEY_NOTIF_SHOW_LOCATION = "notification_show_location";
    public static final String PREF_KEY_NOTIF_SHOW_DK_ICON  = "notification_show_dk_icon";
    public static final String PREF_KEY_NOTIF_SHOW_SECURE   = "notification_show_secure";

    public static final String DEFAULT_OWM_API_KEY = "6d2f4f034d60d9680a720c12df8c7ddd";

    public static boolean isEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getBoolean(PREF_KEY_ENABLE, false);
    }

    public static boolean setEnabled(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.edit().putBoolean(PREF_KEY_ENABLE, value).commit();
    }

    public static boolean isAutoUpdate(Context context) {
        return true;
    }

    public static int getUpdateInterval(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        String valueString = prefs.getString(PREF_KEY_UPDATE_INTERVAL, "1");
        return Integer.valueOf(valueString);
    }

    public static AbstractWeatherProvider getProvider(Context context) {
        return new OpenWeatherMapProvider(context);
    }

    public static String getProviderId(Context context) {
        return "OpenWeatherMap";
    }

    public static String getAPIKey(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        String apiKey = prefs.getString(PREF_KEY_OWM_API_KEY, DEFAULT_OWM_API_KEY);
        if (apiKey.isEmpty()) {
            apiKey = DEFAULT_OWM_API_KEY;
        }
        return apiKey;
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getString(PREF_KEY_UNITS, "0").equals("0");
    }

    public static int getUnit(Context context) {
        return isMetric(context) ? 0 : 1;
    }

    public static boolean isCustomLocation(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getBoolean(PREF_KEY_CUSTOM_LOCATION, false);
    }

    public static String getLocationId(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getString(PREF_KEY_LOCATION_ID, null);
    }

    public static void setLocationId(Context context, String id) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        prefs.edit().putString(PREF_KEY_LOCATION_ID, id).commit();
    }
    
    public static String getLocationName(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getString(PREF_KEY_LOCATION_NAME, null);
    }
    
    public static void setLocationName(Context context, String name) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        prefs.edit().putString(PREF_KEY_LOCATION_NAME, name).commit();
    }

    public static boolean getShowSettingsShortcut(Context context) {
        PackageManager pm = context.getPackageManager();
        ComponentName cn = new ComponentName(context, "net.darkkatrom.dkweather.LauncherActivity");
        int componentEnabledSetting = pm.getComponentEnabledSetting(cn);
        boolean isEnabled = componentEnabledSetting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        return isEnabled;
    }

    public static void setShowSettingsShortcut(Context context, boolean enabled) {
        if (Config.getShowSettingsShortcut(context) == enabled) {
            return;
        }
        PackageManager pm = context.getPackageManager();
        ComponentName cn = new ComponentName(context, "net.darkkatrom.dkweather.LauncherActivity");
        int componentEnabledSetting = enabled
                ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        pm.setComponentEnabledSetting(cn, componentEnabledSetting, PackageManager.DONT_KILL_APP);
    }

    public static WeatherInfo getWeatherData(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        String str = prefs.getString(PREF_KEY_WEATHER_DATA, null);
        if (str != null) {
            WeatherInfo data = WeatherInfo.fromSerializedString(context, str);
            return data;
        }
        return null;
    }
    
    public static void setWeatherData(Context context, WeatherInfo data) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        prefs.edit().putString(PREF_KEY_WEATHER_DATA, data.toSerializedString()).commit();
        prefs.edit().putLong(PREF_KEY_LAST_UPDATE, System.currentTimeMillis()).commit();
    }

    public static void clearWeatherData(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        prefs.edit().remove(PREF_KEY_WEATHER_DATA).commit();
        prefs.edit().remove(PREF_KEY_LAST_UPDATE).commit();
    }
    
    public static long getLastUpdateTime(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getLong(PREF_KEY_LAST_UPDATE, 0);
    }

    public static void clearLastUpdateTime(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        prefs.edit().putLong(PREF_KEY_LAST_UPDATE, 0).commit();
    }

    public static boolean getShowNotification(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getBoolean(PREF_KEY_SHOW_NOTIF, false);
    }

    public static boolean getNotificationShowLocation(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getBoolean(PREF_KEY_NOTIF_SHOW_LOCATION, true);
    }

    public static boolean getNotificationShowDKIcon(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getBoolean(PREF_KEY_NOTIF_SHOW_DK_ICON, true);
    }

    public static boolean getNotificationShowSecure(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getBoolean(PREF_KEY_NOTIF_SHOW_SECURE, false);
    }
}
