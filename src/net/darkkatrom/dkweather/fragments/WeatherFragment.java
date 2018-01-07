/*
 * Copyright (C) 2017 DarkKat
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

import android.app.Fragment;
import android.os.Bundle;

import net.darkkatrom.dkweather.WeatherInfo;
import net.darkkatrom.dkweather.activities.MainActivity;

public class WeatherFragment extends Fragment {

    protected String mForecastDay;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mForecastDay = savedInstanceState.getString(MainActivity.KEY_DAY_INDEX);
        }
    }

    public void setForecastDay(String forecastDay) {
        mForecastDay = forecastDay;
    }

    public void setDayForecastIndex(int index) {
    }

    public void updateContent(WeatherInfo weather) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(MainActivity.KEY_DAY_INDEX, mForecastDay);
        super.onSaveInstanceState(outState);
    }
}
