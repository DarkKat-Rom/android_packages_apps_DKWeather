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
import android.app.UiModeManager;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.android.internal.util.darkkat.ThemeHelper;

import net.darkkatrom.dkweather.R;
import net.darkkatrom.dkweather.WeatherInfo;
import net.darkkatrom.dkweather.fragments.WeatherFragment;
import net.darkkatrom.dkweather.fragments.CurrentWeatherFragment;
import net.darkkatrom.dkweather.fragments.ForecastWeatherFragment;
import net.darkkatrom.dkweather.fragments.NoWeatherDataFragment;
import net.darkkatrom.dkweather.fragments.SettingsFragment;
import net.darkkatrom.dkweather.utils.Config;
import net.darkkatrom.dkweather.utils.JobUtil;
import net.darkkatrom.dkweather.utils.NotificationUtil;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends BaseActivity implements
        OnClickListener, OnLongClickListener {

    private static final String TAG = "DKWeather:MainActivity";

    private static final Uri WEATHER_URI =
            Uri.parse("content://net.darkkatrom.dkweather.provider/weather");

    public static final String KEY_VISIBLE_SCREEN = "visible_screen";
    public static final String KEY_DAY_INDEX      = "day_index";

    public static final int TODAY     = 0;
    public static final int TOMORROW  = 1;
    public static final int LAST_DAY  = 4;
    public static final int SETTINGS  = 5;

    private static final int TOAST_SPACE_TOP = 24;

    private Handler mHandler;
    private ContentResolver mResolver;
    private WeatherObserver mWeatherObserver;

    private WeatherInfo mWeatherInfo;

    private CharSequence[] mActionBarSubTitles;

    private ImageView mUpdateButton;

    private View mNavigationButtonPreviousDay;
    private View mNavigationButtonSettings;
    private View mNavigationButtonNextDay;

    private int mVisibleScreen = TODAY;
    private int mDayIndex = mVisibleScreen;

    private boolean mUpdateRequested = false;

    private boolean mUseOptionalLightStatusBar;
    private boolean mUseOptionalLightNavigationBar;

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
            mWeatherInfo = getWeather();
            if (mWeatherInfo == null) {
                Log.e(TAG, "Error retrieving weather data");
                if (mUpdateRequested) {
                    mUpdateRequested = false;
                }
            } else {
                if (mUpdateRequested) {
                    showToast(R.string.weather_updated);
                    mUpdateRequested = false;
                }
            }
            updateContent();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        updateTheme();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mHandler = new Handler();
        mResolver = getContentResolver();
        mWeatherObserver = new WeatherObserver(mHandler);
        mWeatherInfo = getWeather();

        NotificationUtil notificationUtil = new NotificationUtil(this);
        notificationUtil.setNotificationChannels();

        createOrRestoreState(savedInstanceState == null ? getIntent().getExtras() : savedInstanceState);
        updateActionBar();
        setupBottomNavigation();

        if (savedInstanceState == null) {
            replaceFragment();
        }
    }

    private void updateTheme() {
        mUseOptionalLightStatusBar = ThemeHelper.themeSupportsOptionalĹightSB(this)
                && ThemeHelper.useLightStatusBar(this);
        mUseOptionalLightNavigationBar = ThemeHelper.themeSupportsOptionalĹightNB(this)
                && ThemeHelper.useLightNavigationBar(this);
        int themeResId = 0;

        if (mUseOptionalLightStatusBar && mUseOptionalLightNavigationBar) {
            themeResId = R.style.ThemeOverlay_LightStatusBar_LightNavigationBar;
        } else if (mUseOptionalLightStatusBar) {
            themeResId = R.style.ThemeOverlay_LightStatusBar;
        } else if (mUseOptionalLightNavigationBar) {
            themeResId = R.style.ThemeOverlay_LightNavigationBar;
        } else {
            themeResId = R.style.AppTheme;
        }
        setTheme(themeResId);

        int oldFlags = getWindow().getDecorView().getSystemUiVisibility();
        int newFlags = oldFlags;
        if (!mUseOptionalLightStatusBar) {
            // Possibly we are using the Whiteout theme
            boolean isWhiteoutTheme =
                    ThemeHelper.getTheme(this) == UiModeManager.MODE_NIGHT_NO_WHITEOUT;
            boolean isLightStatusBar = (newFlags & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                    == View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            // Check if light status bar flag was set,
            // and we are not using the Whiteout theme,
            // (Whiteout theme should always use a light status bar).
            if (isLightStatusBar && !isWhiteoutTheme) {
                // Remove flag
                newFlags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        }
        if (!mUseOptionalLightNavigationBar) {
            // Check if light navigation bar flag was set
            boolean isLightNavigationBar = (newFlags & View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
                    == View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            if (isLightNavigationBar) {
                // Remove flag
                newFlags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        }
        if (oldFlags != newFlags) {
            getWindow().getDecorView().setSystemUiVisibility(newFlags);
        }
    }

    private void createOrRestoreState(Bundle b) {
        if (b == null) {
            mVisibleScreen = TODAY;
            mDayIndex = TODAY;
        } else {
            mVisibleScreen = b.getInt(KEY_VISIBLE_SCREEN);
            mDayIndex = b.getInt(KEY_DAY_INDEX);
        }
    }

    private void updateActionBar() {
        TimeZone myTimezone = TimeZone.getDefault();
        Calendar calendar = new GregorianCalendar(myTimezone);
        if (mActionBarSubTitles == null) {
            mActionBarSubTitles = new String[6];
        }
        for (int i = 0; i <mActionBarSubTitles.length; i++) {
            if (i == 0) {
                mActionBarSubTitles[i] = getResources().getString(R.string.today_title);
            } else if (i == 5) {
                mActionBarSubTitles[i] = getResources().getString(R.string.settings_title);
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                mActionBarSubTitles[i] = WeatherInfo.getFormattedDate(calendar.getTime(), false);
            }
        }
        String noWeatherDataPart = mWeatherInfo == null
                ? getResources().getString(R.string.action_bar_no_weather_data_part) : "";

        getActionBar().setSubtitle(mActionBarSubTitles[mVisibleScreen] + noWeatherDataPart);
    }

    private void setupBottomNavigation() {
        mNavigationButtonPreviousDay = findViewById(R.id.bottom_navigation_item_previous_day);
        mNavigationButtonSettings = findViewById(R.id.bottom_navigation_item_settings);
        mNavigationButtonNextDay = findViewById(R.id.bottom_navigation_item_next_day);

        ImageView iv = (ImageView) mNavigationButtonPreviousDay.findViewById(R.id.bottom_navigation_item_icon);
        TextView tv = (TextView) mNavigationButtonPreviousDay.findViewById(R.id.bottom_navigation_item_text);
        iv.setImageResource(R.drawable.ic_action_previous_day);
        tv.setText(R.string.action_previous_day_title);

        iv = (ImageView) mNavigationButtonSettings.findViewById(R.id.bottom_navigation_item_icon);
        tv = (TextView) mNavigationButtonSettings.findViewById(R.id.bottom_navigation_item_text);
        iv.setImageResource(R.drawable.ic_action_settings);
        iv.setImageTintList(getColorStateList(R.color.bottom_navigation_selectable_item_text_icon_color));
        tv.setText(R.string.settings_title);
        tv.setTextColor(getColorStateList(R.color.bottom_navigation_selectable_item_text_icon_color));

        iv = (ImageView) mNavigationButtonNextDay.findViewById(R.id.bottom_navigation_item_icon);
        tv = (TextView) mNavigationButtonNextDay.findViewById(R.id.bottom_navigation_item_text);
        iv.setImageResource(R.drawable.ic_action_next_day);
        tv.setText(R.string.action_next_day_title);
        updateBottomNavigationItemState();

    }

    private void updateBottomNavigationItemState() {
        mNavigationButtonPreviousDay.setEnabled(mVisibleScreen > TODAY
                && mVisibleScreen != SETTINGS && mWeatherInfo != null);
        mNavigationButtonSettings.setSelected(mVisibleScreen == SETTINGS);
        mNavigationButtonSettings.setEnabled(mVisibleScreen != SETTINGS);
        mNavigationButtonNextDay.setEnabled(mVisibleScreen < LAST_DAY && mWeatherInfo != null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean useOptionalLightStatusBar = ThemeHelper.themeSupportsOptionalĹightSB(this)
                && ThemeHelper.useLightStatusBar(this);
        boolean useOptionalLightNavigationBar = ThemeHelper.themeSupportsOptionalĹightNB(this)
                && ThemeHelper.useLightNavigationBar(this);
        if (mUseOptionalLightStatusBar != useOptionalLightStatusBar
                || mUseOptionalLightNavigationBar != useOptionalLightNavigationBar) {
            recreate();
        } else {
            mWeatherObserver.observe();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWeatherObserver.unobserve();
    }

    public void onFragmentResume(WeatherFragment f) {
        f.updateContent(mWeatherInfo);
    }

    public WeatherInfo getWeather() {
        return Config.getWeatherData(this);
    }

    private String getForecastDay() {
        return mWeatherInfo.getHourForecastDays().get(mVisibleScreen);
    }

    private Fragment getFragmentForVisibleScreen() {
        if (mVisibleScreen == SETTINGS) {
            return getSettingsFragment();
        } else {
            if (mWeatherInfo == null) { 
                return new NoWeatherDataFragment();
            } else {
                if (mVisibleScreen == TODAY) {
                    return getCurrentWeatherFragment();
                } else {
                    return getForecastWeatherFragment();
                }
            }
        }
    }

    private void replaceFragment() {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_content, getFragmentForVisibleScreen())
                .commit();
    }

    private Fragment getCurrentWeatherFragment() {
        WeatherFragment wf = new CurrentWeatherFragment();
        wf.setForecastDay(getForecastDay());
        return wf;
    }

    private Fragment getForecastWeatherFragment() {
        WeatherFragment wf = new ForecastWeatherFragment();
        wf.setForecastDay(getForecastDay());
        return wf;
    }

    private Fragment getSettingsFragment() {
        return new SettingsFragment();
    }

    private void updateContent() {
        updateActionBar();
        replaceFragment();
        updateBottomNavigationItemState();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update, menu);
        MenuItem itemUpdate = menu.findItem(R.id.item_update);
        LinearLayout updateButtonLayout = (LinearLayout) itemUpdate.getActionView();
        mUpdateButton = (ImageView) updateButtonLayout.findViewById(R.id.update_button);

        updateButtonLayout.setOnClickListener(this);
        updateButtonLayout.setOnLongClickListener(this);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.item_update).setVisible(mVisibleScreen != SETTINGS);
        return super.onPrepareOptionsMenu(menu);
    }

    public void onBottomNavigationItemClick(View v) {
        if (v == mNavigationButtonPreviousDay) {
            mDayIndex--;
            mVisibleScreen--;
        } else if (v == mNavigationButtonSettings) {
            mVisibleScreen = SETTINGS;
        } else {
            mVisibleScreen++;
            mDayIndex++;
        }
        updateContent();
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
            JobUtil.startUpdate(this);
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
        int actionBarHeight = getActionBar().getHeight();
        int spaceTopDP = TOAST_SPACE_TOP * Math.round(density);

        Toast toast = Toast.makeText(this, resId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, actionBarHeight + spaceTopDP);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        if (mVisibleScreen > TODAY) {
            if (mVisibleScreen == SETTINGS) {
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    super.onBackPressed();
                } else {
                    mVisibleScreen = mDayIndex;
                }
            } else {
                mVisibleScreen = TODAY;
                mDayIndex = mVisibleScreen;
            }
            if (mVisibleScreen != SETTINGS) {
                mWeatherInfo = getWeather();
                updateContent();
            }
        } else {
            finish();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_VISIBLE_SCREEN, mVisibleScreen);
        outState.putInt(KEY_DAY_INDEX, mDayIndex);
        super.onSaveInstanceState(outState);
    }
}
