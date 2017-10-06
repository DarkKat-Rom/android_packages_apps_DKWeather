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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.res.ColorStateList;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.util.darkkat.DetailedWeatherColorHelper;
import com.android.internal.util.darkkat.ThemeHelper;
import com.android.internal.util.darkkat.WeatherHelper;

import net.darkkatrom.dkweather.R;
import net.darkkatrom.dkweather.WeatherInfo;
import net.darkkatrom.dkweather.WeatherInfo.HourForecast;
import net.darkkatrom.dkweather.activities.MainActivity;
import net.darkkatrom.dkweather.utils.Config;

import java.util.ArrayList;

public class ForecastWeatherFragment extends Fragment {
    private WeatherInfo mWeatherInfo;
    private LayoutInflater mInflater;

    private LinearLayout mCardsLayout;
    private View mCard;

    private TextView mDayTemps;
    private TextView[] mDayTempsValues;
    private TextView mMinValue;
    private TextView mMaxValue;
    private TextView mProviderLink;

    private String mForecastDay;
    private int mDayForecastIndex = 1;

    private ArrayList<ViewHolder> mHolders = new ArrayList<ViewHolder>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflateAndSetupView(inflater, container, savedInstanceState);
    }

    private View inflateAndSetupView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mWeatherInfo = Config.getWeatherData(getActivity());
        mInflater = inflater;

        View layout = mInflater.inflate(R.layout.detailed_weather_forecast, container, false);
        mCardsLayout = (LinearLayout) layout.findViewById(R.id.forecast_cards_layout);
        mCard = layout.findViewById(R.id.forecast_daytemps_card);

        mDayTemps = (TextView) layout.findViewById(R.id.forecast_daytemps);
        TextView[] dayTempsTitles = {
            (TextView) layout.findViewById(R.id.forecast_weather_temp_morning_title),
            (TextView) layout.findViewById(R.id.forecast_weather_temp_day_title),
            (TextView) layout.findViewById(R.id.forecast_weather_temp_evening_title),
            (TextView) layout.findViewById(R.id.forecast_weather_temp_night_title)
        };
        mDayTempsValues = new TextView[] {
                (TextView) layout.findViewById(R.id.forecast_weather_temp_morning_value),
                (TextView) layout.findViewById(R.id.forecast_weather_temp_day_value),
                (TextView) layout.findViewById(R.id.forecast_weather_temp_evening_value),
                (TextView) layout.findViewById(R.id.forecast_weather_temp_night_value)
        };
        TextView minTitle = (TextView) layout.findViewById(R.id.forecast_weather_min_title);
        mMinValue = (TextView) layout.findViewById(R.id.forecast_weather_min_value);
        TextView maxTitle = (TextView) layout.findViewById(R.id.forecast_weather_max_title);
        mMaxValue = (TextView) layout.findViewById(R.id.forecast_weather_max_value);
        mProviderLink = 
                (TextView) layout.findViewById(R.id.forecast_daytemps_provider_link);

        mProviderLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWeatherInfo != null) {
                    String cityId = mWeatherInfo.getId();
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://openweathermap.org/city/" + cityId));
                    startActivity(intent);
                }
            }
        });

        final boolean customizeColors = !ThemeHelper.detailedWeatherUseThemeColors(getActivity());
        if (customizeColors) {
            final int accentColor = DetailedWeatherColorHelper.getWeatherAccentColor(getActivity());
            final int backgroundColor = DetailedWeatherColorHelper.getContentBgColor(getActivity());
            final int cardBackgroundColor = DetailedWeatherColorHelper.getCardBgColor(getActivity());
            final int textColorPrimary = DetailedWeatherColorHelper.getCardPrimaryTextColor(getActivity());
            final int textColorSecondary = DetailedWeatherColorHelper.getCardSecondaryTextColor(getActivity());
            final int rippleColor = DetailedWeatherColorHelper.getCardRippleColor(getActivity());
            layout.setBackgroundColor(backgroundColor);
            mCard.setBackgroundTintList(ColorStateList.valueOf(cardBackgroundColor));
            mDayTemps.setTextColor(textColorPrimary);
            for (int i = 0; i < mDayTempsValues.length; i++) {
                dayTempsTitles[i].setTextColor(textColorPrimary);
                mDayTempsValues[i].setTextColor(textColorSecondary);
            }
            mProviderLink.setTextColor(accentColor);
            minTitle.setTextColor(textColorPrimary);
            mMinValue.setTextColor(textColorSecondary);
            maxTitle.setTextColor(textColorPrimary);
            mMaxValue.setTextColor(textColorSecondary);
            ((RippleDrawable) mProviderLink.getBackground())
                    .setColor(ColorStateList.valueOf(rippleColor));
        }

        if (savedInstanceState != null) {
            mForecastDay = savedInstanceState.getString(MainActivity.KEY_DAY_INDEX);
        }

        if (mWeatherInfo != null && mCardsLayout != null && mForecastDay != null) {
            ArrayList<HourForecast> hourForecasts = mWeatherInfo.getHourForecastsDay(mForecastDay);
            if (hourForecasts.size() != 0) {
                final String[] tempValues = {
                    mWeatherInfo.getForecasts().get(mDayForecastIndex).getFormattedMorning(),
                    mWeatherInfo.getForecasts().get(mDayForecastIndex).getFormattedDay(),
                    mWeatherInfo.getForecasts().get(mDayForecastIndex).getFormattedEvening(),
                    mWeatherInfo.getForecasts().get(mDayForecastIndex).getFormattedNight()
                };
                for (int i = 0; i < mDayTempsValues.length; i++) {
                    mDayTempsValues[i].setText(tempValues[i]);
                }
                final String min =
                        mWeatherInfo.getForecasts().get(mDayForecastIndex).getFormattedLow();
                final String max =
                        mWeatherInfo.getForecasts().get(mDayForecastIndex).getFormattedHigh();
                mMinValue.setText(min);
                mMaxValue.setText(max);
                for (int i = 0; i < hourForecasts.size(); i++) {
                    HourForecast h = hourForecasts.get(i);
                    ViewHolder holder = new ViewHolder(mInflater);
                    holder.setColors(customizeColors);
                    holder.updateWeather(h);
                    mHolders.add(holder);
                    mCardsLayout.addView(holder.getForecastCard());
                }
            }
        }
        return layout;
    }

    public void setForecastDay(String forecastDay) {
        mForecastDay = forecastDay;
    }

    public void setDayForecastIndex(int index) {
        mDayForecastIndex = index;
    }

    public void updateWeather(WeatherInfo weather) {
        if (weather == null) {
            return;
        }
        mWeatherInfo = weather;

        if (mCardsLayout != null && mForecastDay != null) {
            ArrayList<HourForecast> hourForecasts = mWeatherInfo.getHourForecastsDay(mForecastDay);
            if (hourForecasts.size() != 0) {
                final String[] tempValues = {
                    mWeatherInfo.getForecasts().get(mDayForecastIndex).getFormattedMorning(),
                    mWeatherInfo.getForecasts().get(mDayForecastIndex).getFormattedDay(),
                    mWeatherInfo.getForecasts().get(mDayForecastIndex).getFormattedEvening(),
                    mWeatherInfo.getForecasts().get(mDayForecastIndex).getFormattedNight()
                };
                for (int i = 0; i < mDayTempsValues.length; i++) {
                    mDayTempsValues[i].setText(tempValues[i]);
                }
                final String min =
                        mWeatherInfo.getForecasts().get(mDayForecastIndex).getFormattedLow();
                final String max =
                        mWeatherInfo.getForecasts().get(mDayForecastIndex).getFormattedHigh();
                mMinValue.setText(min);
                mMaxValue.setText(max);
                if (mHolders.size() != hourForecasts.size()) {
                    final boolean customizeColors =
                            !ThemeHelper.detailedWeatherUseThemeColors(getActivity());
                    if (mCardsLayout.getChildCount() > 1) {
                        mCardsLayout.removeViews(1, mCardsLayout.getChildCount() - 1);
                    }
                    for (int i = 0; i < hourForecasts.size(); i++) {
                        HourForecast h = hourForecasts.get(i);
                        ViewHolder holder = new ViewHolder(mInflater);
                        holder.setColors(customizeColors);
                        holder.updateWeather(h);
                        mHolders.add(holder);
                        mCardsLayout.addView(holder.getForecastCard());
                    }
                } else {
                    for (int i = 0; i < hourForecasts.size(); i++) {
                        HourForecast h = hourForecasts.get(i);
                        mHolders.get(i).updateWeather(h);
                    }
                }
            }
        }
    }

    private class ViewHolder {
        public View cardLayout;
        public View card;
        public View expandedContent;
        public TextView forecast;
        public TextView timeValue;
        public ImageView image;
        public View imageDivider;
        public TextView tempValue;
        public View tempConditionDivider;
        public TextView conditionValue;
        public TextView precipitationTitle;
        public TextView precipitationValue;
        public TextView windTitle;
        public TextView windValue;
        public TextView humidityTitle;
        public TextView humidityValue;
        public TextView pressureTitle;
        public TextView pressureValue;
        public View expandCollapseButtonDivider;
        public LinearLayout expandCollapseButton;
        public TextView expandCollapseButtonText;
        public ImageView expandCollapseButtonIcon;
        public ValueAnimator animator;
        public int expandedContentHeight = 0;
        public boolean animateExpansion = true;

        public ViewHolder(LayoutInflater inflater) {
            cardLayout = inflater.inflate(R.layout.forecast_weather_card, null);
            card = cardLayout.findViewById(R.id.forecast_card);
            expandedContent = cardLayout.findViewById(R.id.forecast_expanded_content_layout);

            forecast = (TextView) cardLayout.findViewById(R.id.forecast_weather);
            timeValue = (TextView) cardLayout.findViewById(R.id.forecast_time);
            image = (ImageView) cardLayout.findViewById(R.id.forecast_condition_image);
            imageDivider = cardLayout.findViewById(R.id.forecast_image_divider);
            tempValue = (TextView) cardLayout.findViewById(R.id.forecast_temp_value);
            tempConditionDivider = cardLayout.findViewById(R.id.forecast_temp_condition_divider);
            conditionValue = (TextView) cardLayout.findViewById(R.id.forecast_condition_value);
            precipitationTitle = 
                    (TextView) cardLayout.findViewById(R.id.forecast_precipitation_title);
            precipitationValue = 
                    (TextView) cardLayout.findViewById(R.id.forecast_precipitation_value);
            windTitle = (TextView) cardLayout.findViewById(R.id.forecast_wind_title);
            windValue = (TextView) cardLayout.findViewById(R.id.forecast_wind_value);
            humidityTitle = (TextView) cardLayout.findViewById(R.id.forecast_humidity_title);
            humidityValue = (TextView) cardLayout.findViewById(R.id.forecast_humidity_value);
            pressureTitle = (TextView) cardLayout.findViewById(R.id.forecast_pressure_title);
            pressureValue = (TextView) cardLayout.findViewById(R.id.forecast_pressure_value);
            expandCollapseButtonDivider = 
                    cardLayout.findViewById(R.id.forecast_expand_collapse_button_divider);
            expandCollapseButton = 
                    (LinearLayout) cardLayout.findViewById(R.id.forecast_expand_collapse_button);
            expandCollapseButtonText = 
                    (TextView) cardLayout.findViewById(R.id.forecast_expand_collapse_button_text);
            expandCollapseButtonIcon = 
                    (ImageView) cardLayout.findViewById(R.id.forecast_expand_collapse_button_icon);

            expandedContent.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    expandedContentHeight = expandedContent.getHeight();
                    expandedContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    expandedContent.setVisibility(View.GONE);
                }
            });

            expandCollapseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (animator != null) {
                        animator.start();
                    }
                }
            });

            animator = createForecastAnimator();
        }

        public void setColors(boolean customizeColors) {
            final int conditionImageColor = 
                    DetailedWeatherColorHelper.getConditionImageColor(getActivity());
            if (customizeColors) {
                final int cardBackgroundColor = DetailedWeatherColorHelper.getCardBgColor(getActivity());
                final int textColorPrimary = 
                        DetailedWeatherColorHelper.getCardPrimaryTextColor(getActivity());
                final int textColorSecondary = 
                        DetailedWeatherColorHelper.getCardSecondaryTextColor(getActivity());
                final int iconColor = DetailedWeatherColorHelper.getCardIconColor(getActivity());
                final int dividerAlpha = DetailedWeatherColorHelper.getDividerAlpha(getActivity());
                final int dividerColor = (dividerAlpha << 24) | (iconColor & 0x00ffffff);
                final int rippleColor = DetailedWeatherColorHelper.getCardRippleColor(getActivity());

                card.setBackgroundTintList(ColorStateList.valueOf(cardBackgroundColor));
                forecast.setTextColor(textColorPrimary);
                timeValue.setTextColor(textColorSecondary);

                if (conditionImageColor != 0) {
                    image.setImageTintList(ColorStateList.valueOf(conditionImageColor));
                } else {
                    image.setImageTintList(null);
                }

                imageDivider.setBackgroundColor(dividerColor);
                tempValue.setTextColor(textColorPrimary);
                tempConditionDivider.setBackgroundColor(dividerColor);
                conditionValue.setTextColor(textColorPrimary);
                precipitationTitle.setTextColor(textColorPrimary);
                precipitationValue.setTextColor(textColorSecondary);
                windTitle.setTextColor(textColorPrimary);
                windValue.setTextColor(textColorSecondary);
                humidityTitle.setTextColor(textColorPrimary);
                humidityValue.setTextColor(textColorSecondary);
                pressureTitle.setTextColor(textColorPrimary);
                pressureValue.setTextColor(textColorSecondary);
                expandCollapseButtonDivider.setBackgroundColor(dividerColor);
                ((RippleDrawable) expandCollapseButton.getBackground())
                        .setColor(ColorStateList.valueOf(rippleColor));
                expandCollapseButtonText.setTextColor(textColorPrimary);
                expandCollapseButtonIcon.setImageTintList(ColorStateList.valueOf(iconColor));
            } else if (conditionImageColor == 0) {
                image.setImageTintList(null);
            }
        }

        public View getForecastCard() {
            return cardLayout;
        }

        private  ValueAnimator createForecastAnimator() {
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.setDuration(300);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = animation.getAnimatedFraction();
                    float height;
                    float alpha = value;
                    if (animateExpansion) {
                        height = expandedContentHeight * value;
                    } else {
                        height = expandedContentHeight * (1 - value);
                        alpha = 1 - value;
                    }
                    expandedContent.getLayoutParams().height = Math.round(height);
                    expandCollapseButtonDivider.setAlpha(alpha);
                    expandedContent.requestLayout();
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (animateExpansion) {
                        expandedContent.setVisibility(View.VISIBLE);
                        expandCollapseButtonDivider.setVisibility(View.VISIBLE);
                    }
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (animateExpansion) {
                        expandCollapseButtonIcon.setImageResource(R.drawable.ic_expand_less);
                        expandCollapseButtonText.setText(R.string.collapse_card);
                    } else {
                        expandCollapseButtonIcon.setImageResource(R.drawable.ic_expand_more);
                        expandCollapseButtonText.setText(R.string.expand_card);
                        expandedContent.setVisibility(View.GONE);
                        expandCollapseButtonDivider.setVisibility(View.GONE);
                    }
                    animateExpansion = !animateExpansion;
                }
            });
            return animator;
        }

        public void updateWeather(HourForecast h) {
            if (getActivity() == null || mWeatherInfo == null) {
                return;
            }
            final Drawable icon = mWeatherInfo.getConditionIcon(
                    WeatherHelper.getDetailedWeatherConditionIconType(getActivity()),
                    h.getConditionCode());
            final String rain = h.getFormattedRain();
            final String snow = h.getFormattedSnow();
            final String noPrecipitationValue = getActivity().getResources().getString(
                    R.string.no_precipitation_value);

            timeValue.setText(h.getTime());
            image.setImageDrawable(icon);
            tempValue.setText(h.getFormattedTemperature());
            conditionValue.setText(h.getCondition());
            if (!snow.equals(WeatherInfo.NO_VALUE)) {
                precipitationValue.setText(snow);
            } else if (!rain.equals(WeatherInfo.NO_VALUE)) {
                precipitationValue.setText(rain);
            } else {
                precipitationValue.setText(noPrecipitationValue);
            }
            windValue.setText(h.getFormattedWind());
            humidityValue.setText(h.getFormattedHumidity());
            pressureValue.setText(h.getFormattedPressure());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(MainActivity.KEY_DAY_INDEX, mForecastDay);
        super.onSaveInstanceState(outState);
    }
}
