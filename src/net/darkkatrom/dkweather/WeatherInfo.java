/*
 * Copyright (C) 2012 The AOKP Project
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

package net.darkkatrom.dkweather;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import net.darkkatrom.dkweather.utils.Config;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.text.DecimalFormat;

public class WeatherInfo {
    private static final DecimalFormat NO_DIGITS_FORMAT  = new DecimalFormat("0");
    private static final DecimalFormat ONE_DIGITS_FORMAT = new DecimalFormat("##0.#");
    private static final DecimalFormat TWO_DIGITS_FORMAT = new DecimalFormat("##0.##");

    public static final int ICON_MONOCHROME = 0;
    public static final int ICON_COLORED    = 1;

    private static final float PRECIPITATION_ITEM_THRESHOLD = 0.005f;

    public static final String NO_VALUE    = "-";

    private final Context mContext;
    private final String mPressureUnit;
    private final String mPrecipitationUnit1h;
    private final String mPrecipitationUnit3h;

    private final String mId;
    private final String mCity;
    private final String mCondition;
    private final int mConditionCode;
    private final float mTemperature;
    private final String mTempUnit;
    private final float mHumidity;
    private final float mWind;
    private final int mWindDirection;
    private final String mSpeedUnit;
    private final float mPressure;
    private final float mRain1H, mRain3H, mSnow1H, mSnow3H;
    private ArrayList<DayForecast> mForecasts;
    private ArrayList<HourForecast> mHourForecasts;
    private final long mTimestamp;
    private final long mSunrise;
    private final long mSunset;

    public WeatherInfo(Context context, String id, String city, String condition,
            int conditionCode, float temp, String tempUnit, float humidity, float wind,
            int windDir, String speedUnit, float pressure, float rain1H, float rain3H,
            float snow1H, float snow3H, ArrayList<DayForecast> forecasts,
            ArrayList<HourForecast> hourForecasts, long timestamp, long sunrise, long sunset) {
        mContext = context.getApplicationContext();
        mPressureUnit = mContext.getResources().getString(R.string.pressure_unit_title);
        mPrecipitationUnit1h = mContext.getResources().getString(R.string.precipitation_unit_1h_title);
        mPrecipitationUnit3h = mContext.getResources().getString(R.string.precipitation_unit_3h_title);

        mId = id;
        mCity = city;
        mCondition = condition;
        mConditionCode = conditionCode;
        mTemperature = temp;
        mTempUnit = tempUnit;
        mHumidity = humidity;
        mWind = wind;
        mWindDirection = windDir;
        mSpeedUnit = speedUnit;
        mPressure = pressure;
        mRain1H = rain1H;
        mRain3H = rain3H;
        mSnow1H = snow1H;
        mSnow3H = snow3H;
        mForecasts = forecasts;
        mHourForecasts = hourForecasts;
        mTimestamp = timestamp;
        mSunrise = sunrise;
        mSunset = sunset;
    }

    public static class WeatherLocation {
        public String id;
        public String city;
        public String postal;
        public String countryId;
        public String country;
    }
    
    public static class DayForecast {
        public final Context context;
        public final String condition;
        public final int conditionCode;
        public final float low;
        public final float morning;
        public final float day;
        public final float evening;
        public final float night;
        public final float high;
        public final String tempUnit;

        public DayForecast(Context context, String condition, int conditionCode, float low,
                float morning, float day, float evening, float night, float high, String tempUnit) {
            this.context = context.getApplicationContext();
            this.condition = condition;
            this.conditionCode = conditionCode;
            this.low = low;
            this.morning = morning;
            this.day = day;
            this.evening = evening;
            this.night = night;
            this.high = high;
            this.tempUnit = tempUnit;
        }

        public String getCondition() {
            return WeatherInfo.getCondition(context, conditionCode, condition);
        }

        public int getConditionCode() {
            return conditionCode;
        }

        public String getLow() {
            return getTemperature(low);
        }

        public String getHigh() {
            return getTemperature(high);
        }

        public String getFormattedLow() {
            return getFormattedTemperature(low, tempUnit);
        }

        public String getFormattedMorning() {
            return getFormattedTemperature(morning, tempUnit);
        }

        public String getFormattedDay() {
            return getFormattedTemperature(day, tempUnit);
        }

        public String getFormattedEvening() {
            return getFormattedTemperature(evening, tempUnit);
        }

        public String getFormattedNight() {
            return getFormattedTemperature(night, tempUnit);
        }

        public String getFormattedHigh() {
            return getFormattedTemperature(high, tempUnit);
        }
    }

    public static class HourForecast {
        public final Context context;
        public final String condition;
        public final int conditionCode;
        public final float temperature;
        public final String tempUnit;
        public final float humidity;
        public final float wind;
        public final int windDirection;
        public final String speedUnit;
        public final float pressure;
        public final String pressureUnit;
        public final float rain, snow;
        public final long timestamp;
        private final String precipitationUnit;

        public HourForecast(Context context, String condition, int conditionCode,
                float temp, String tempUnit, float humidity, float wind, int windDir,
                String speedUnit, float pressure, float rain, float snow, long timestamp) {
            this.context = context.getApplicationContext();
            this.condition = condition;
            this.conditionCode = conditionCode;
            this.temperature = temp;
            this.tempUnit = tempUnit;
            this.humidity = humidity;
            this.wind = wind;
            this.windDirection = windDir;
            this.speedUnit = speedUnit;
            this.pressure = pressure;
            this.pressureUnit = context.getResources().getString(R.string.pressure_unit_title);
            this.rain = rain;
            this.snow = snow;
            this.timestamp = timestamp;
            this.precipitationUnit = context.getResources().getString(R.string.precipitation_unit_1h_title);
        }

        public String getCondition() {
            return WeatherInfo.getCondition(context, conditionCode, condition);
        }

        public int getConditionCode() {
            return conditionCode;
        }

        public String getFormattedTemperature() {
            return WeatherInfo.getFormattedTemperature(temperature, tempUnit);
        }

        public String getFormattedHumidity() {
            return WeatherInfo.getFormattedHumidity(humidity);
        }

        public String getFormattedWind() {
            return WeatherInfo.getFormattedWind(context, wind, windDirection, speedUnit);
        }

        public String getFormattedPressure() {
            return WeatherInfo.getFormattedPressure(pressure, pressureUnit);
        }

        public String getFormattedRain() {
            return getFormattedPrecipitation(rain, precipitationUnit);
        }

        public String getFormattedSnow() {
            return getFormattedPrecipitation(snow, precipitationUnit);
        }

        public Date getDate() {
            return new Date(timestamp * 1000);
        }

        public String getFormattedDate() {
            return WeatherInfo.getFormattedDate(getDate(), true);
        }

        public String getDay() {
            Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            calendar.setTime(getDate());
            String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT,
                    Locale.getDefault());
            return day;
        }

        public String getTime() {
            return WeatherInfo.getTime(getDate(), true);
        }
    }

    public String getId() {
        return mId;
    }

    public String getCity() {
        return mCity;
    }

    public String getCondition() {
        return getCondition(mContext, mConditionCode, mCondition);
    }

    public int getConditionCode() {
        return mConditionCode;
    }

    public String getTemperature() {
        return getTemperature(mTemperature);
    }

    public String getFormattedTemperature() {
        return getFormattedTemperature(mTemperature, mTempUnit);
    }

    public String getLow() {
        return getTemperature(mForecasts.get(0).low);
    }

    public String getHigh() {
        return getTemperature(mForecasts.get(0).high);
    }

    public String getFormattedLow() {
        return getFormattedTemperature(mForecasts.get(0).low, mTempUnit);
    }

    public String getFormattedMorning() {
        return getFormattedTemperature(mForecasts.get(0).morning, mTempUnit);
    }

    public String getFormattedDay() {
        return getFormattedTemperature(mForecasts.get(0).day, mTempUnit);
    }

    public String getFormattedEvening() {
        return getFormattedTemperature(mForecasts.get(0).evening, mTempUnit);
    }

    public String getFormattedNight() {
        return getFormattedTemperature(mForecasts.get(0).night, mTempUnit);
    }

    public String getFormattedHigh() {
        return getFormattedTemperature(mForecasts.get(0).high, mTempUnit);
    }

    public String getFormattedHumidity() {
        return getFormattedHumidity(mHumidity);
    }

    public String getFormattedWind() {
        return getFormattedWind(mContext, mWind, mWindDirection, mSpeedUnit);
    }

    public String getFormattedPressure() {
        return getFormattedPressure(mPressure, mPressureUnit);
    }

    public String getFormattedRain1H() {
        return getFormattedPrecipitation(mRain1H, mPrecipitationUnit1h);
    }

    public String getFormattedRain3H() {
        return getFormattedPrecipitation(mRain3H, mPrecipitationUnit3h);
    }

    public String getFormattedSnow1H() {
        return getFormattedPrecipitation(mSnow1H, mPrecipitationUnit1h);
    }

    public String getFormattedSnow3H() {
        return getFormattedPrecipitation(mSnow3H, mPrecipitationUnit3h);
    }

    public ArrayList<DayForecast> getForecasts() {
        return mForecasts;
    }

    public ArrayList<HourForecast> getHourForecasts() {
        return mHourForecasts;
    }

    public ArrayList<HourForecast> getHourForecastsDay(String d) {
        ArrayList<HourForecast> day = new ArrayList<HourForecast>();
        for (int i = 0; i < mHourForecasts.size(); i++) {
            String s = mHourForecasts.get(i).getDay();
            if (d.equals(s)) {
                day.add(mHourForecasts.get(i));
            }
        }
        return day;
    }

    public ArrayList<String> getHourForecastDays() {
        ArrayList<String> days = new ArrayList<String>();
        String day = mHourForecasts.get(0).getDay();
        days.add(day);
        for (int i = 0; i < mHourForecasts.size(); i++) {
            String s = mHourForecasts.get(i).getDay();
            if (!day.equals(s)) {
                day = s;
                days.add(day);
            }
        }
        return days;
    }

    public Date getDate() {
        return new Date(mTimestamp);
    }

    public String getTime() {
        return getTime(getDate(), false);
    }

    public String getFormattedDate() {
        return getFormattedDate(getDate(), false);
    }

    public String getSunrise() {
        return getTime(new Date(mSunrise * 1000), false);
    }

    public String getSunset() {
        return getTime(new Date(mSunset * 1000) , false);
    }

    private static String getCondition(Context context, int conditionCode, String condition) {
        final Resources res = context.getResources();
        final int resId = res.getIdentifier("weather_" + conditionCode, "string", context.getPackageName());
        if (resId != 0) {
            return res.getString(resId);
        }
        return condition;
    }

    private static String getFormattedValue(float value, String unit) {
        if (Float.isNaN(value)) {
            return NO_VALUE;
        }
        String formatted = NO_DIGITS_FORMAT.format(value);
        if (formatted.equals("-0")) {
            formatted = "0";
        }
        return formatted + unit;
    }

    private static String getTemperature(float temp) {
        if (Float.isNaN(temp)) {
            return NO_VALUE;
        }
        String formatted = NO_DIGITS_FORMAT.format(temp);
        if (formatted.equals("-0")) {
            formatted = "0";
        }
        return formatted + "\u00b0";
    }

    private static String getFormattedTemperature(float temp, String unit) {
        if (Float.isNaN(temp)) {
            return NO_VALUE;
        }
        String formatted = NO_DIGITS_FORMAT.format(temp);
        if (formatted.equals("-0")) {
            formatted = "0";
        }
        return formatted + "\u00b0" + unit;
    }

    private static String getFormattedHumidity(float humidity) {
        if (Float.isNaN(humidity)) {
            return NO_VALUE;
        }
        String formatted = NO_DIGITS_FORMAT.format(humidity);
        if (formatted.equals("-0")) {
            formatted = "0";
        }
        if (formatted.equals("-1")) {
            return NO_VALUE;
        }
        return formatted + "%";
    }

    private static String getFormattedWind(Context context, float speed, int direction, String unit) {
        if (Float.isNaN(speed)) {
            return NO_VALUE;
        }
        if (NO_DIGITS_FORMAT.format(speed).equals("-0") || NO_DIGITS_FORMAT.format(speed).equals("0")) {
            return NO_VALUE;
        }
        if (Config.getProviderId(context).equals("OpenWeatherMap") && Config.isMetric(context)) {
            speed *= 3.6f;
        }
        String FormattedUnitAndDirection = unit + " - " + getWindDirection(context, direction);

        return ONE_DIGITS_FORMAT.format(speed) + FormattedUnitAndDirection;
    }

    private static String getWindDirection(Context context, int direction) {
        int resId;

        if (direction < 0) resId = R.string.unknown;
        else if (direction < 23) resId = R.string.weather_N;
        else if (direction < 68) resId = R.string.weather_NE;
        else if (direction < 113) resId = R.string.weather_E;
        else if (direction < 158) resId = R.string.weather_SE;
        else if (direction < 203) resId = R.string.weather_S;
        else if (direction < 248) resId = R.string.weather_SW;
        else if (direction < 293) resId = R.string.weather_W;
        else if (direction < 338) resId = R.string.weather_NW;
        else resId = R.string.weather_N;

        return context.getString(resId);
    }

    private static String getFormattedPressure(float pressure, String unit) {
        if (Float.isNaN(pressure)) {
            return NO_VALUE;
        }
        String formatted = ONE_DIGITS_FORMAT.format(pressure);
        if (formatted.equals("-0")) {
            formatted = "0";
        }
        if (formatted.equals("0")) {
            return NO_VALUE;
        }
        return formatted + unit;
    }

    private static String getFormattedPrecipitation(float precipitation, String unit) {
        if (Float.isNaN(precipitation)) {
            return NO_VALUE;
        }
        if (precipitation >= PRECIPITATION_ITEM_THRESHOLD) {
            return TWO_DIGITS_FORMAT.format(precipitation) + " " + unit;
        } else {
            return NO_VALUE;
        }
    }

    private static String getTime(Date date, boolean isUTC) {
        SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.US);
        if (isUTC) {
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        return sdf.format(date);
    }

    public static String getFormattedDate(Date date, boolean isUTC) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd. MMMM yyyy", Locale.getDefault());
        if (isUTC) {
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        return sdf.format(date);
    }

    public Drawable getConditionIcon(int iconNameValue, int conditionCode) {
        String iconName;

        if (iconNameValue == ICON_MONOCHROME) {
            iconName = "weather_";
        } else if (iconNameValue == ICON_COLORED) {
            iconName = "weather_color_";
        } else {
            iconName = "weather_vclouds_";
        }

        final int resId = mContext.getResources().getIdentifier(
                iconName + conditionCode, "drawable", mContext.getPackageName());
        if (resId != 0) {
            return mContext.getResources().getDrawable(resId);
        }

        // Use the default color set unknown icon
        return mContext.getResources().getDrawable(R.drawable.weather_color_na);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WeatherInfo for ");
        builder.append(getCity());
        builder.append(" (");
        builder.append(getId());
        builder.append(") @ ");
        builder.append(getDate());
        builder.append(": ");
        builder.append(getCondition());
        builder.append("(");
        builder.append(getConditionCode());
        builder.append("), temperature ");
        builder.append(getFormattedTemperature());
        builder.append(", low ");
        builder.append(getFormattedLow());
        builder.append(", high ");
        builder.append(getFormattedHigh());
        builder.append(", humidity ");
        builder.append(getFormattedHumidity());
        builder.append(", wind ");
        builder.append(getFormattedWind());
        builder.append(", pressure ");
        builder.append(getFormattedPressure());
        builder.append(", rain1H ");
        builder.append(getFormattedRain1H());
        builder.append(", rain3H ");
        builder.append(getFormattedRain3H());
        builder.append(", snow1H ");
        builder.append(getFormattedSnow1H());
        builder.append(", snow3H ");
        builder.append(getFormattedSnow3H());
        builder.append(", sunrise ");
        builder.append(getSunrise());
        builder.append(", sunset ");
        builder.append(getSunset());
        if (mForecasts.size() > 0) {
            builder.append(", forecasts:");
        }
        for (int i = 0; i < mForecasts.size(); i++) {
            DayForecast d = mForecasts.get(i);
            if (i != 0) {
                builder.append(";");
            }
            builder.append(" day ").append(i + 1).append(": ");
            builder.append(d.getCondition());
            builder.append("(").append(d.conditionCode).append(")");
            builder.append(", low ").append(d.getFormattedLow());
            builder.append(", morning ").append(d.getFormattedMorning());
            builder.append(", day ").append(d.getFormattedDay());
            builder.append(", evening ").append(d.getFormattedEvening());
            builder.append(", night ").append(d.getFormattedNight());
            builder.append(", high ").append(d.getFormattedHigh());
            builder.append(", tempunit ").append(d.tempUnit);
        }
        if (mHourForecasts.size() > 0) {
            builder.append(", hourForecasts:");
        }
        for (int i = 0; i < mHourForecasts.size(); i++) {
            HourForecast h = mHourForecasts.get(i);
            if (i != 0) {
                builder.append(";");
            }
            builder.append(" time ").append(h.getTime()).append(": ");
            builder.append(h.getCondition());
            builder.append("(").append(h.conditionCode).append(")");
            builder.append(", temp ").append(h.getFormattedTemperature());
            builder.append(", humidity ").append(h.getFormattedHumidity());
            builder.append(", wind ").append(h.getFormattedWind());
            builder.append(", pressure ").append(h.getFormattedPressure());
            builder.append(", rain ").append(h.getFormattedRain());
            builder.append(", snow ").append(h.getFormattedSnow());
            builder.append(", timestemp ").append(h.getFormattedSnow());
        }
        return builder.toString();
    }

    public String toSerializedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(mId).append('|');
        builder.append(mCity).append('|');
        builder.append(mCondition).append('|');
        builder.append(mConditionCode).append('|');
        builder.append(mTemperature).append('|');
        builder.append(mTempUnit).append('|');
        builder.append(mHumidity).append('|');
        builder.append(mWind).append('|');
        builder.append(mWindDirection).append('|');
        builder.append(mSpeedUnit).append('|');
        builder.append(mPressure).append('|');
        builder.append(mRain1H).append('|');
        builder.append(mRain3H).append('|');
        builder.append(mSnow1H).append('|');
        builder.append(mSnow3H).append('|');
        builder.append(mTimestamp).append('|');
        builder.append(mSunrise).append('|');
        builder.append(mSunset).append('|');
        serializeForecasts(builder);
        return builder.toString();
    }

    private void serializeForecasts(StringBuilder builder) {
        builder.append(mForecasts.size());
        for (DayForecast d : mForecasts) {
            builder.append(';');
            builder.append(d.condition).append(';');
            builder.append(d.conditionCode).append(';');
            builder.append(d.low).append(';');
            builder.append(d.morning).append(';');
            builder.append(d.day).append(';');
            builder.append(d.evening).append(';');
            builder.append(d.night).append(';');
            builder.append(d.high).append(';');
            builder.append(d.tempUnit);
        }
        builder.append('|');
        serializeHourForecasts(builder);
    }

    private void serializeHourForecasts(StringBuilder builder) {
        builder.append(mHourForecasts.size());
        for (HourForecast h : mHourForecasts) {
            builder.append(';');
            builder.append(h.condition).append(';');
            builder.append(h.conditionCode).append(';');
            builder.append(h.temperature).append(';');
            builder.append(h.tempUnit).append(';');
            builder.append(h.humidity).append(';');
            builder.append(h.wind).append(';');
            builder.append(h.windDirection).append(';');
            builder.append(h.speedUnit).append(';');
            builder.append(h.pressure).append(';');
            builder.append(h.rain).append(';');
            builder.append(h.snow).append(';');
            builder.append(h.timestamp);
        }
    }

    public static WeatherInfo fromSerializedString(Context context, String input) {
        if (input == null) {
            return null;
        }

        String[] parts = input.split("\\|");
        if (parts == null || parts.length != 20) {
            return null;
        }

        int conditionCode, windDirection;
        long timestamp;
        long sunrise;
        long sunset;
        float temperature, humidity, wind, pressure, rain1H, rain3H, snow1H, snow3H;
        String[] forecastParts = parts[18].split(";");
        String[] hourForecastParts = parts[19].split(";");
        int forecastItems;
        int hourForecastItems;
        ArrayList<DayForecast> forecasts = new ArrayList<DayForecast>();
        ArrayList<HourForecast> hourForecasts = new ArrayList<HourForecast>();

        // Parse the core data
        try {
            conditionCode = Integer.parseInt(parts[3]);
            temperature = Float.parseFloat(parts[4]);
            humidity = Float.parseFloat(parts[6]);
            wind = Float.parseFloat(parts[7]);
            windDirection = Integer.parseInt(parts[8]);
            pressure = Float.parseFloat(parts[10]);
            rain1H = Float.parseFloat(parts[11]);
            rain3H = Float.parseFloat(parts[12]);
            snow1H = Float.parseFloat(parts[13]);
            snow3H = Float.parseFloat(parts[14]);
            timestamp = Long.parseLong(parts[15]);
            sunrise = Long.parseLong(parts[16]);
            sunset = Long.parseLong(parts[17]);
            forecastItems = forecastParts == null ? 0 : Integer.parseInt(forecastParts[0]);
            hourForecastItems = hourForecastParts == null ? 0 : Integer.parseInt(hourForecastParts[0]);
        } catch (NumberFormatException e) {
            return null;
        }

        if (forecastItems == 0 || forecastParts.length != 9 * forecastItems + 1) {
            return null;
        }

        // Parse the forecast data
        try {
            for (int item = 0; item < forecastItems; item ++) {
                int offset = item * 9 + 1;
                DayForecast day = new DayForecast(context,
                        /* condition */ forecastParts[offset],
                        /* conditionCode */ Integer.parseInt(forecastParts[offset + 1]),
                        /* low */ Float.parseFloat(forecastParts[offset + 2]),
                        /* morning */ Float.parseFloat(forecastParts[offset + 3]),
                        /* day */ Float.parseFloat(forecastParts[offset + 4]),
                        /* evening */ Float.parseFloat(forecastParts[offset + 5]),
                        /* night */ Float.parseFloat(forecastParts[offset + 6]),
                        /* high */ Float.parseFloat(forecastParts[offset + 7]),
                        /* tempUnit */ forecastParts[offset + 8]);

                if (!Float.isNaN(day.low) && !Float.isNaN(day.high) && day.conditionCode >= 0) {
                    forecasts.add(day);
                }
            }
        } catch (NumberFormatException ignored) {
        }

        if (hourForecastItems == 0 || hourForecastParts.length != 12 * hourForecastItems + 1) {
            return null;
        }

        // Parse the hourForecast data
        try {
            for (int itemH = 0; itemH < hourForecastItems; itemH ++) {
                int offsetH = itemH * 12 + 1;
                HourForecast h = new HourForecast(context,
                        /* condition */ hourForecastParts[offsetH],
                        /* conditionCode */ Integer.parseInt(hourForecastParts[offsetH + 1]),
                        /* temperature */ Float.parseFloat(hourForecastParts[offsetH + 2]),
                        /* tempUnit */ hourForecastParts[offsetH + 3],
                        /* humidity */ Float.parseFloat(hourForecastParts[offsetH + 4]),
                        /* wind */ Float.parseFloat(hourForecastParts[offsetH + 5]),
                        /* windDirection */ Integer.parseInt(hourForecastParts[offsetH + 6]),
                        /* speedUnit */ hourForecastParts[offsetH + 7],
                        /* pressure */ Float.parseFloat(hourForecastParts[offsetH + 8]),
                        /* rain */ Float.parseFloat(hourForecastParts[offsetH + 9]),
                        /* snow */ Float.parseFloat(hourForecastParts[offsetH + 10]),
                        /* timestamp */ Long.parseLong(hourForecastParts[offsetH + 11]));

                if (!Float.isNaN(h.temperature) && h.conditionCode >= 0) {
                    hourForecasts.add(h);
                }
            }
        } catch (NumberFormatException ignored) {
        }

        if (hourForecasts.isEmpty()) {
            return null;
        }

        return new WeatherInfo(context,
                /* id */ parts[0], /* city */ parts[1], /* condition */ parts[2],
                conditionCode, temperature, /* tempUnit */ parts[5],
                humidity, wind, windDirection, /* speedUnit */ parts[9],
                pressure, rain1H, rain3H, snow1H, snow3H, forecasts, hourForecasts, timestamp,
                sunrise, sunset);
    }
}
