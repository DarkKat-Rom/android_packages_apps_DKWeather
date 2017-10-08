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
package net.darkkatrom.dkweather.activities;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import com.android.internal.util.darkkat.DetailedWeatherColorHelper;
import com.android.internal.util.darkkat.ThemeHelper;

import net.darkkatrom.dkweather.R;
import net.darkkatrom.dkweather.WeatherInfo;
import net.darkkatrom.dkweather.WeatherService;
import net.darkkatrom.dkweather.fragments.CurrentWeatherFragment;
import net.darkkatrom.dkweather.fragments.ForecastWeatherFragment;
import net.darkkatrom.dkweather.fragments.SettingsFragment;
import net.darkkatrom.dkweather.utils.Config;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener,  OnClickListener, OnLongClickListener {

    private static final String TAG = "MainActivity";

    private static final Uri WEATHER_URI =
            Uri.parse("content://net.darkkatrom.dkweather.provider/weather");

    public static final String KEY_DAY_INDEX = "day_index";
    public static final String KEY_START_FRAGMENT = "start_fragment";

    public static final int FRAGMENT_WEATHER  = 0;
    public static final int FRAGMENT_SETTINGS = 1;

    public static final int DAY_INDEX_TODAY  = 0;

    private static final int MENU_ITEM_INDEX_TODAY    = 0;
    private static final int MENU_ITEM_INDEX_SETTINGS = 5;

    private static final int TOAST_SPACE_TOP = 24;

    private Handler mHandler;
    private ContentResolver mResolver;
    private WeatherObserver mWeatherObserver;

    private WeatherInfo mWeatherInfo;

    private CurrentWeatherFragment mCurrentWeatherFragment;
    private ForecastWeatherFragment mForecastWeatherFragment;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private ImageView mUpdateButton;

    private CharSequence[] mMenuItemTitles;

    private int mSelectedMenuItemIndex = MENU_ITEM_INDEX_TODAY;
    private int mOldSelectedMenuItemIndex = MENU_ITEM_INDEX_TODAY;

    private boolean mUpdateRequested = false;

    class WeatherObserver extends ContentObserver {
        WeatherObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            mResolver.registerContentObserver(WEATHER_URI, false, this);
        }

        void unobserve() {
            mResolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            if (getWeather() == null) {
                Log.e(TAG, "Error retrieving forecast data");
                if (mUpdateRequested) {
                    mUpdateRequested = false;
                }
            } else {
                mWeatherInfo = getWeather();
                updateWeather();
                if (mUpdateRequested) {
                    showToast(R.string.weather_updated);
                    mUpdateRequested = false;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTheme().applyStyle(R.style.AppTheme, true);

        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        mResolver = getContentResolver();
        mWeatherObserver = new WeatherObserver(mHandler);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final boolean customizeColors = !ThemeHelper.detailedWeatherUseThemeColors(this);
        if (customizeColors) {
            final int actionBarBgColor = DetailedWeatherColorHelper.getActionBarBgColor(this);
            final int textColorPrimary = DetailedWeatherColorHelper.getActionBarPrimaryTextColor(this);
            final int textColorSecondary = DetailedWeatherColorHelper.getActionBarSecondaryTextColor(this);
            toolbar.setBackgroundColor(actionBarBgColor);
            toolbar.setTitleTextColor(textColorPrimary);
            toolbar.setSubtitleTextColor(textColorSecondary);
        }

        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        setupNavigationView(savedInstanceState == null ? getIntent().getExtras() : savedInstanceState);

        TimeZone myTimezone = TimeZone.getDefault();
        Calendar calendar = new GregorianCalendar(myTimezone);
        TypedArray ta = getResources().obtainTypedArray(R.array.navigation_menu_items);
        mMenuItemTitles = new String[6];
        for (int i = 0; i <mMenuItemTitles.length; i++) {
            if (i == 0) {
                mMenuItemTitles[i] = mNavigationView.getMenu().findItem(R.id.menu_item_weather_today).getTitle();
            } else if (i == 5) {
                mMenuItemTitles[i] = mNavigationView.getMenu().findItem(R.id.menu_item_settings).getTitle();
            } else {
                int resId = ta.getResourceId(i, 0);
                MenuItem item = mNavigationView.getMenu().findItem(resId);
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                item.setTitle(WeatherInfo.getFormattedDate(calendar.getTime(), false));
                mMenuItemTitles[i] = item.getTitle();
            }
        }
        ta.recycle();

        getSupportActionBar().setSubtitle(mMenuItemTitles[mSelectedMenuItemIndex]);

        if (getWeather() != null) {
            mWeatherInfo = getWeather();

            View headerView = mNavigationView.getHeaderView(0);
            if (headerView != null) {
                ImageView headerIcon = (ImageView) headerView.findViewById(R.id.nav_view_header_icon);
                Drawable icon = mWeatherInfo.getConditionIcon(0, mWeatherInfo.getConditionCode()).mutate();
                headerIcon.setImageDrawable(icon);

                TextView title = (TextView) headerView.findViewById(R.id.nav_view_header_title);
                TextView subtitle = (TextView) headerView.findViewById(R.id.nav_view_header_subtitle);
                String titleText = mWeatherInfo.getCondition() + ", " + mWeatherInfo.getFormattedTemperature();
                String subtitleText = mWeatherInfo.getFormattedLow() + " | " + mWeatherInfo.getFormattedHigh();
                title.setText(titleText);
                subtitle.setText(subtitleText);
            }

            if (savedInstanceState == null) {
                Fragment startFragment = getStartFragment(savedInstanceState == null
                        ? getIntent().getExtras() : savedInstanceState);

                if (startFragment instanceof ForecastWeatherFragment) {
                    getForecastWeatherFragment().setForecastDay(getForecastDay());
                }

                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_content, startFragment)
                        .commit();
            }
        }
    }

    @Override
    public void onResume() {
        super.onPause();
        mWeatherObserver.observe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWeatherObserver.unobserve();
    }

    private WeatherInfo getWeather() {
        return Config.getWeatherData(this);
    }

    private String getForecastDay() {
        return mWeatherInfo.getHourForecastDays().get(mSelectedMenuItemIndex);
    }

    private void setupNavigationView(Bundle b) {
        if (b == null) {
            mNavigationView.getMenu().findItem(R.id.menu_item_weather_today).setChecked(true);
            mSelectedMenuItemIndex = MENU_ITEM_INDEX_TODAY;
        } else if (b.getInt(KEY_START_FRAGMENT) == FRAGMENT_SETTINGS) {
            mNavigationView.getMenu().findItem(R.id.menu_item_settings).setChecked(true);
            mSelectedMenuItemIndex = MENU_ITEM_INDEX_SETTINGS;
        } else {
            mSelectedMenuItemIndex = b.getInt(KEY_DAY_INDEX);
            TypedArray ta = getResources().obtainTypedArray(R.array.navigation_menu_items);
            int resId = ta.getResourceId(mSelectedMenuItemIndex, R.id.menu_item_weather_today);
            mNavigationView.getMenu().findItem(resId).setChecked(true);
            ta.recycle();
        }
        mOldSelectedMenuItemIndex = mSelectedMenuItemIndex;
    }

    private Fragment getStartFragment(Bundle b) {
        if (b == null) {
            return getCurrentWeatherFragment();
        } else if (b.getInt(KEY_START_FRAGMENT) == FRAGMENT_SETTINGS) {
            return new SettingsFragment();
        } else {
            if (b.getInt(KEY_DAY_INDEX) == DAY_INDEX_TODAY) {
                return getCurrentWeatherFragment();
            } else {
                return getForecastWeatherFragment();
            }
        }
    }

    private CurrentWeatherFragment getCurrentWeatherFragment() {
        if (mCurrentWeatherFragment == null) {
            mCurrentWeatherFragment = new CurrentWeatherFragment();
        }
        return mCurrentWeatherFragment;
    }

    private ForecastWeatherFragment getForecastWeatherFragment() {
        if (mForecastWeatherFragment == null) {
            mForecastWeatherFragment = new ForecastWeatherFragment();
        }
        return mForecastWeatherFragment;
    }

    private void updateWeather() {
        TimeZone myTimezone = TimeZone.getDefault();
        Calendar calendar = new GregorianCalendar(myTimezone);
        TypedArray ta = getResources().obtainTypedArray(R.array.navigation_menu_items);
        for (int i = 0; i < 5; i++) {
            if (i != 0) {
                int resId = ta.getResourceId(i, 0);
                MenuItem item = mNavigationView.getMenu().findItem(resId);
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                item.setTitle(WeatherInfo.getFormattedDate(calendar.getTime(), false));
                mMenuItemTitles[i] = item.getTitle();
            }
        }
        ta.recycle();

        getSupportActionBar().setSubtitle(mMenuItemTitles[mSelectedMenuItemIndex]);

        View headerView = mNavigationView.getHeaderView(0);
        if (headerView != null) {
            ImageView headerIcon = (ImageView) headerView.findViewById(R.id.nav_view_header_icon);
            Drawable icon = mWeatherInfo.getConditionIcon(0, mWeatherInfo.getConditionCode()).mutate();
            headerIcon.setImageDrawable(icon);

            TextView title = (TextView) headerView.findViewById(R.id.nav_view_header_title);
            TextView subtitle = (TextView) headerView.findViewById(R.id.nav_view_header_subtitle);
            String titleText = mWeatherInfo.getCondition() + ", " + mWeatherInfo.getFormattedTemperature();
            String subtitleText = mWeatherInfo.getFormattedLow() + " | " + mWeatherInfo.getFormattedHigh();
            title.setText(titleText);
            subtitle.setText(subtitleText);
        }

        if (mSelectedMenuItemIndex != MENU_ITEM_INDEX_SETTINGS) {
            if (mSelectedMenuItemIndex == MENU_ITEM_INDEX_TODAY) {
                getCurrentWeatherFragment().updateWeather(mWeatherInfo);
            } else {
                getForecastWeatherFragment().setForecastDay(getForecastDay());
                getForecastWeatherFragment().updateWeather(mWeatherInfo);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update, menu);
        MenuItem itemUpdate = menu.findItem(R.id.item_update);
        LinearLayout updateButtonLayout = (LinearLayout) itemUpdate.getActionView();
        mUpdateButton = (ImageView) updateButtonLayout.findViewById(R.id.update_button);
        final boolean customizeColors = !ThemeHelper.detailedWeatherUseThemeColors(this);

        updateButtonLayout.setOnClickListener(this);
        updateButtonLayout.setOnLongClickListener(this);
        if (customizeColors) {
            final int iconColor = DetailedWeatherColorHelper.getActionBarIconColor(this);
            final int rippleColor = DetailedWeatherColorHelper.getActionBarRippleColor(this);
            mUpdateButton.setImageTintList(ColorStateList.valueOf(iconColor));
            ((RippleDrawable) updateButtonLayout.getBackground())
                    .setColor(ColorStateList.valueOf(rippleColor));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.isChecked()) {
            return false;
        }
        mOldSelectedMenuItemIndex = mSelectedMenuItemIndex;
        TypedArray ta = getResources().obtainTypedArray(R.array.navigation_menu_items);
        for (int i = 0; i < 6; i++) {
            int resId = ta.getResourceId(i, 0);
            if (resId == item.getItemId()) {
                mSelectedMenuItemIndex = i;
                if (mSelectedMenuItemIndex == MENU_ITEM_INDEX_TODAY) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_content, getCurrentWeatherFragment())
                            .commit();
                } else if (mSelectedMenuItemIndex == MENU_ITEM_INDEX_SETTINGS) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_content, new SettingsFragment())
                            .commit();
                } else {
                    if (mWeatherInfo != null) {
                        getForecastWeatherFragment().setForecastDay(getForecastDay());
                    }
//                    if (mOldSelectedMenuItemIndex == MENU_ITEM_INDEX_TODAY
//                            || mOldSelectedMenuItemIndex == MENU_ITEM_INDEX_SETTINGS) {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.fragment_content, getForecastWeatherFragment())
                                .commit();
//                    } else {
                        if (mWeatherInfo != null) {
                            getForecastWeatherFragment().updateWeather(mWeatherInfo);
                        }

//                    }
                }
            }
        }
        ta.recycle();
        uncheckmenuItemIfNeeded();
        getSupportActionBar().setSubtitle(mMenuItemTitles[mSelectedMenuItemIndex]);
        mDrawerLayout.closeDrawers();
        mOldSelectedMenuItemIndex = mSelectedMenuItemIndex;
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.update_button_layout) {
            RotateAnimation anim = new RotateAnimation(0.0f, 360.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setInterpolator(new LinearInterpolator());
            anim.setDuration(700);
            anim.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mUpdateButton.setAnimation(null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mUpdateButton.startAnimation(anim);
            mUpdateRequested = true;
            WeatherService.startUpdate(this, true);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.update_button_layout) {
            showToast(R.string.update_weather);
            return true;
        }
        return false;
    }

    private void showToast(int resId) {
		float density = getResources().getDisplayMetrics().density;
        int toolbarHeight = findViewById(R.id.toolbar).getHeight();
        int spaceTopDP = TOAST_SPACE_TOP * Math.round(density);

        Toast toast = Toast.makeText(this, resId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, toolbarHeight + spaceTopDP);
        toast.show();
    }

    private void uncheckmenuItemIfNeeded() {
        TypedArray ta = getResources().obtainTypedArray(R.array.navigation_menu_items);
        int resId;
        if (mSelectedMenuItemIndex == MENU_ITEM_INDEX_TODAY
                || mSelectedMenuItemIndex == MENU_ITEM_INDEX_SETTINGS) {
            resId = ta.getResourceId(mOldSelectedMenuItemIndex, 0);
            mNavigationView.getMenu().findItem(resId).setChecked(false);
        } else {
            if (mOldSelectedMenuItemIndex == MENU_ITEM_INDEX_TODAY
                    || mOldSelectedMenuItemIndex == MENU_ITEM_INDEX_SETTINGS) {
                resId = ta.getResourceId(mOldSelectedMenuItemIndex, 0);
                mNavigationView.getMenu().findItem(resId).setChecked(false);
            }
        }
        ta.recycle();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSelectedMenuItemIndex == MENU_ITEM_INDEX_SETTINGS) {
            outState.putInt(KEY_START_FRAGMENT, FRAGMENT_SETTINGS);
        } else {
            outState.putInt(KEY_START_FRAGMENT, FRAGMENT_WEATHER);
            outState.putInt(KEY_DAY_INDEX, mSelectedMenuItemIndex);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        if (mSelectedMenuItemIndex > MENU_ITEM_INDEX_TODAY) {
            if (mSelectedMenuItemIndex == MENU_ITEM_INDEX_SETTINGS
                    && getFragmentManager().getBackStackEntryCount() > 0) {
                super.onBackPressed();
            } else {
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_content, getCurrentWeatherFragment())
                        .commit();
                mNavigationView.getMenu().findItem(R.id.menu_item_weather_today).setChecked(true);
                mSelectedMenuItemIndex = MENU_ITEM_INDEX_TODAY;
                uncheckmenuItemIfNeeded();
                getSupportActionBar().setSubtitle(mMenuItemTitles[mSelectedMenuItemIndex]);
                mOldSelectedMenuItemIndex = MENU_ITEM_INDEX_TODAY;
            }
        } else {
            finish();
        }
    }
}
