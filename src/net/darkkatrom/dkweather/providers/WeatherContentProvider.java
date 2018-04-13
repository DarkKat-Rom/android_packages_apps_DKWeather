/*
 * Copyright (C) 2013 The CyanogenMod Project
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
package net.darkkatrom.dkweather.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import net.darkkatrom.dkweather.WeatherInfo;
import net.darkkatrom.dkweather.WeatherInfo.DayForecast;
import net.darkkatrom.dkweather.WeatherInfo.HourForecast;
import net.darkkatrom.dkweather.utils.Config;

public class WeatherContentProvider extends ContentProvider {
    private static final String TAG = "DKWeather:WeatherContentProvider";
    private static final boolean DEBUG = false;

    static WeatherInfo sCachedWeatherInfo;

    private static final int URI_TYPE_WEATHER = 1;
    private static final int URI_TYPE_SETTINGS = 2;

    private static final int TYPE_CURRENT_WEATHER = 1;
    private static final int TYPE_DAYFORECAST = 2;
    private static final int TYPE_HOURFORECAST = 3;

    private static final String COLUMN_TYPE =
            "type";
    private static final String COLUMN_CURRENT_CITY_ID =
            "city_id";
    private static final String COLUMN_CURRENT_CITY =
            "city";
    private static final String COLUMN_CURRENT_CONDITION =
            "condition";
    private static final String COLUMN_CURRENT_CONDITION_CODE =
            "condition_code";
    private static final String COLUMN_CURRENT_FORMATTED_TEMPERATURE =
            "formatted_temperature";
    private static final String COLUMN_CURRENT_TEMPERATURE_LOW =
            "temperature_low";
    private static final String COLUMN_CURRENT_TEMPERATURE_HIGHT =
            "temperature_hight";
    private static final String COLUMN_CURRENT_FORMATTED_TEMPERATURE_LOW =
            "formatted_temperature_low";
    private static final String COLUMN_CURRENT_FORMATTED_TEMPERATURE_HIGHT =
            "formatted_temperature_hight";
    private static final String COLUMN_CURRENT_FORMATTED_HUMIDITY =
            "formatted_humidity";
    private static final String COLUMN_CURRENT_FORMATTED_WIND =
            "formatted_wind";
    private static final String COLUMN_CURRENT_FORMATTED_PRESSURE =
            "formatted_pressure";
    private static final String COLUMN_CURRENT_FORMATTED_RAIN1H =
            "formatted_rain1h";
    private static final String COLUMN_CURRENT_FORMATTED_RAIN3H =
            "formatted_rain3h";
    private static final String COLUMN_CURRENT_FORMATTED_SNOW1H =
            "formatted_snow1h";
    private static final String COLUMN_CURRENT_FORMATTED_SNOW3H =
            "formatted_snow3h";
    private static final String COLUMN_CURRENT_TIME_STAMP =
            "time_stamp";
    private static final String COLUMN_CURRENT_SUNRISE =
            "sunrise";
    private static final String COLUMN_CURRENT_SUNSET =
            "sunset";

    private static final String COLUMN_DAYFORECAST_CONDITION =
            "dayforecast_condition";
    private static final String COLUMN_DAYFORECAST_CONDITION_CODE =
            "dayforecast_condition_code";
    private static final String COLUMN_DAYFORECAST_TEMPERATURE_LOW =
            "dayforecast_temperature_low";
    private static final String COLUMN_DAYFORECAST_TEMPERATURE_HIGH =
            "dayforecast_temperature_high";
    private static final String COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_LOW =
            "dayforecast_formatted_temperature_low";
    private static final String COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_HIGH =
            "dayforecast_formatted_temperature_high";
    private static final String COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_MORNING =
            "dayforecast_formatted_temperature_morning";
    private static final String COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_DAY =
            "dayforecast_formatted_temperature_day";
    private static final String COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_EVENING =
            "dayforecast_formatted_temperature_evening";
    private static final String COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_NIGHT =
            "dayforecast_formatted_temperature_night";

    private static final String COLUMN_HOURFORECAST_CONDITION =
            "hourforecast_condition";
    private static final String COLUMN_HOURFORECAST_CONDITION_CODE =
            "hourforecast_condition_code";
    private static final String COLUMN_HOURFORECAST_FORMATTED_TEMPERATURE =
            "hourforecast_formatted_temperature";
    private static final String COLUMN_HOURFORECAST_FORMATTED_HUMIDITY =
            "hourforecast_formatted_humidity";
    private static final String COLUMN_HOURFORECAST_FORMATTED_WIND =
            "hourforecast_formatted_wind";
    private static final String COLUMN_HOURFORECAST_FORMATTED_PRESSURE =
            "hourforecast_formatted_pressure";
    private static final String COLUMN_HOURFORECAST_FORMATTED_RAIN =
            "hourforecast_formatted_rain";
    private static final String COLUMN_HOURFORECAST_FORMATTED_SNOW =
            "hourforecast_formatted_snow";
    private static final String COLUMN_HOURFORECAST_TIME =
            "hourforecast_time";
    private static final String COLUMN_HOURFORECAST_DAY =
            "hourforecast_day";

    private static final String COLUMN_ENABLED = "enabled";
    private static final String COLUMN_PROVIDER = "provider";
    private static final String COLUMN_INTERVAL = "interval";
    private static final String COLUMN_UNITS = "units";
    private static final String COLUMN_LOCATION = "location";

    private static final String[] PROJECTION_DEFAULT_WEATHER = new String[] {
            COLUMN_TYPE,
            COLUMN_CURRENT_CITY_ID,
            COLUMN_CURRENT_CITY,
            COLUMN_CURRENT_CONDITION,
            COLUMN_CURRENT_CONDITION_CODE,
            COLUMN_CURRENT_FORMATTED_TEMPERATURE,
            COLUMN_CURRENT_TEMPERATURE_LOW,
            COLUMN_CURRENT_TEMPERATURE_HIGHT,
            COLUMN_CURRENT_FORMATTED_TEMPERATURE_LOW,
            COLUMN_CURRENT_FORMATTED_TEMPERATURE_HIGHT,
            COLUMN_CURRENT_FORMATTED_HUMIDITY,
            COLUMN_CURRENT_FORMATTED_WIND,
            COLUMN_CURRENT_FORMATTED_PRESSURE,
            COLUMN_CURRENT_FORMATTED_RAIN1H,
            COLUMN_CURRENT_FORMATTED_RAIN3H,
            COLUMN_CURRENT_FORMATTED_SNOW1H,
            COLUMN_CURRENT_FORMATTED_SNOW3H,
            COLUMN_CURRENT_TIME_STAMP,
            COLUMN_CURRENT_SUNRISE,
            COLUMN_CURRENT_SUNSET,
            COLUMN_DAYFORECAST_CONDITION,
            COLUMN_DAYFORECAST_CONDITION_CODE,
            COLUMN_DAYFORECAST_TEMPERATURE_LOW,
            COLUMN_DAYFORECAST_TEMPERATURE_HIGH,
            COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_LOW,
            COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_HIGH,
            COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_MORNING,
            COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_DAY,
            COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_EVENING,
            COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_NIGHT,
            COLUMN_HOURFORECAST_CONDITION,
            COLUMN_HOURFORECAST_CONDITION_CODE,
            COLUMN_HOURFORECAST_FORMATTED_TEMPERATURE,
            COLUMN_HOURFORECAST_FORMATTED_HUMIDITY,
            COLUMN_HOURFORECAST_FORMATTED_WIND,
            COLUMN_HOURFORECAST_FORMATTED_PRESSURE,
            COLUMN_HOURFORECAST_FORMATTED_RAIN,
            COLUMN_HOURFORECAST_FORMATTED_SNOW,
            COLUMN_HOURFORECAST_TIME,
            COLUMN_HOURFORECAST_DAY
    };

    private static final String[] PROJECTION_DEFAULT_SETTINGS = new String[] {
            COLUMN_ENABLED,
            COLUMN_PROVIDER,
            COLUMN_INTERVAL,
            COLUMN_UNITS,
            COLUMN_LOCATION
    };

    public static final String AUTHORITY = "net.darkkatrom.dkweather.provider";

    private static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(URI_TYPE_WEATHER);
        sUriMatcher.addURI(AUTHORITY, "weather", URI_TYPE_WEATHER);
        sUriMatcher.addURI(AUTHORITY, "settings", URI_TYPE_SETTINGS);
    }

    private Context mContext;

    @Override
    public boolean onCreate() {
        mContext = getContext();
        sCachedWeatherInfo = Config.getWeatherData(mContext);
        return true;
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        final int projectionType = sUriMatcher.match(uri);
        final MatrixCursor result = new MatrixCursor(resolveProjection(projection, projectionType));

        if (DEBUG) Log.d(TAG, "query: " + uri.toString());

        if (projectionType == URI_TYPE_SETTINGS) {
            result.newRow()
                    .add(COLUMN_ENABLED, Config.isEnabled(mContext) ? 1 : 0)
                    .add(COLUMN_PROVIDER, Config.getProviderId(mContext))
                    .add(COLUMN_INTERVAL, Config.getUpdateInterval(mContext))
                    .add(COLUMN_UNITS, Config.isMetric(mContext) ? 0 : 1)
                    .add(COLUMN_LOCATION, Config.isCustomLocation(mContext) ? Config.getLocationName(mContext) : "");

            return result;
        } else if (projectionType == URI_TYPE_WEATHER) {
            WeatherInfo weather = sCachedWeatherInfo;
            if (weather != null) {
                // current
                result.newRow()
                        .add(COLUMN_TYPE, TYPE_CURRENT_WEATHER)
                        .add(COLUMN_CURRENT_CITY_ID, weather.getId())
                        .add(COLUMN_CURRENT_CITY, weather.getCity())
                        .add(COLUMN_CURRENT_CONDITION, weather.getCondition())
                        .add(COLUMN_CURRENT_CONDITION_CODE, weather.getConditionCode())
                        .add(COLUMN_CURRENT_FORMATTED_TEMPERATURE, weather.getFormattedTemperature())
                        .add(COLUMN_CURRENT_TEMPERATURE_LOW, weather.getLow())
                        .add(COLUMN_CURRENT_TEMPERATURE_HIGHT, weather.getHigh())
                        .add(COLUMN_CURRENT_FORMATTED_TEMPERATURE_LOW, weather.getFormattedLow())
                        .add(COLUMN_CURRENT_FORMATTED_TEMPERATURE_HIGHT, weather.getFormattedHigh())
                        .add(COLUMN_CURRENT_FORMATTED_HUMIDITY, weather.getFormattedHumidity())
                        .add(COLUMN_CURRENT_FORMATTED_WIND, weather.getFormattedWind())
                        .add(COLUMN_CURRENT_FORMATTED_PRESSURE, weather.getFormattedPressure())
                        .add(COLUMN_CURRENT_FORMATTED_RAIN1H, weather.getFormattedRain1H())
                        .add(COLUMN_CURRENT_FORMATTED_RAIN3H, weather.getFormattedRain3H())
                        .add(COLUMN_CURRENT_FORMATTED_SNOW1H, weather.getFormattedSnow1H())
                        .add(COLUMN_CURRENT_FORMATTED_SNOW3H, weather.getFormattedSnow3H())
                        .add(COLUMN_CURRENT_TIME_STAMP, weather.getDate().toString())
                        .add(COLUMN_CURRENT_SUNRISE, weather.getSunrise())
                        .add(COLUMN_CURRENT_SUNSET, weather.getSunset());

                // dayforecast
                for (DayForecast day : weather.getForecasts()) {
                    result.newRow()
                            .add(COLUMN_TYPE, TYPE_DAYFORECAST)
                            .add(COLUMN_DAYFORECAST_CONDITION, day.getCondition())
                            .add(COLUMN_DAYFORECAST_CONDITION_CODE, day.getConditionCode())
                            .add(COLUMN_DAYFORECAST_TEMPERATURE_LOW, day.getLow())
                            .add(COLUMN_DAYFORECAST_TEMPERATURE_HIGH, day.getHigh())
                            .add(COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_LOW, day.getFormattedLow())
                            .add(COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_HIGH, day.getFormattedHigh())
                            .add(COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_MORNING, day.getFormattedMorning())
                            .add(COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_DAY, day.getFormattedDay())
                            .add(COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_EVENING, day.getFormattedEvening())
                            .add(COLUMN_DAYFORECAST_FORMATTED_TEMPERATURE_NIGHT, day.getFormattedNight());
                }

                // hourforecast
                for (HourForecast hour : weather.getHourForecasts()) {
                    result.newRow()
                            .add(COLUMN_TYPE, TYPE_HOURFORECAST)
                            .add(COLUMN_HOURFORECAST_CONDITION, hour.getCondition())
                            .add(COLUMN_HOURFORECAST_CONDITION_CODE, hour.getConditionCode())
                            .add(COLUMN_HOURFORECAST_FORMATTED_TEMPERATURE, hour.getFormattedTemperature())
                            .add(COLUMN_HOURFORECAST_FORMATTED_HUMIDITY, hour.getFormattedHumidity())
                            .add(COLUMN_HOURFORECAST_FORMATTED_WIND, hour.getFormattedWind())
                            .add(COLUMN_HOURFORECAST_FORMATTED_PRESSURE, hour.getFormattedPressure())
                            .add(COLUMN_HOURFORECAST_FORMATTED_RAIN, hour.getFormattedRain())
                            .add(COLUMN_HOURFORECAST_FORMATTED_SNOW, hour.getFormattedSnow())
                            .add(COLUMN_HOURFORECAST_DAY, hour.getDay())
                            .add(COLUMN_HOURFORECAST_TIME, hour.getTime());
                }
                return result;
            }
        }
        return null;
    }

    private String[] resolveProjection(String[] projection, int uriType) {
        if (projection != null)
            return projection;
        switch (uriType) {
            default:
            case URI_TYPE_WEATHER:
                return PROJECTION_DEFAULT_WEATHER;

            case URI_TYPE_SETTINGS:
                return PROJECTION_DEFAULT_SETTINGS;
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public static void updateCachedWeatherInfo(Context context) {
        if (DEBUG) Log.d(TAG, "updateCachedWeatherInfo()");
        sCachedWeatherInfo = Config.getWeatherData(context);
        context.getContentResolver().notifyChange(
                Uri.parse("content://" + WeatherContentProvider.AUTHORITY + "/weather"), null);
    }
}
