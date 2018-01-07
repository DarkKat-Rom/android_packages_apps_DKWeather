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
package net.darkkatrom.dkweather.utils;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;

import net.darkkatrom.dkweather.R;
import net.darkkatrom.dkweather.activities.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class ShortcutUtil {
    private static final String FORMAT_WEEKDAY   = "EEEE";
    private static final String SHORTCUT_ID_BASE = "shortcut_weather_forecast_";

    private static final int NUM_DYNAMIC_SHORTCUTS      = 4;
    private static final int MAX_DYNAMIC_SHORTCUTS_RANK = 3;

    private final Context mContext;
    private final Resources mResources;

    public ShortcutUtil(Context context) {
        mContext = context;
        mResources = context.getResources();
    }

    public void addOrUpdateShortcuts() {
        ShortcutManager shortcutManager = mContext.getSystemService(ShortcutManager.class);
        List<ShortcutInfo> shortcuts = shortcutManager.getDynamicShortcuts();

        if (shortcuts.isEmpty()) {
            shortcutManager.addDynamicShortcuts(createShortcuts());
        } else {
            shortcutManager.updateShortcuts(createShortcuts());
        }
    }

    private List<ShortcutInfo> createShortcuts() {
        ArrayList<ShortcutInfo> shortcuts = new ArrayList<ShortcutInfo>();
        TimeZone myTimezone = TimeZone.getDefault();
        Calendar calendar = new GregorianCalendar(myTimezone);
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_WEEKDAY, Locale.getDefault());

        for (int i = 0; i < NUM_DYNAMIC_SHORTCUTS; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);

            Bundle b = new Bundle();
            b.putInt(MainActivity.KEY_VISIBLE_SCREEN, i + 1);
            b.putInt(MainActivity.KEY_DAY_INDEX, i + 1);
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.putExtras(b);

            ShortcutInfo shortcut = new ShortcutInfo.Builder(mContext, SHORTCUT_ID_BASE + (i + 1))
                .setShortLabel(sdf.format(calendar.getTime()))
                .setLongLabel(sdf.format(calendar.getTime()))
                .setIcon(Icon.createWithResource(mContext, R.drawable.ic_shortcut_weather))
                .setRank(MAX_DYNAMIC_SHORTCUTS_RANK - i)
                .setIntent(intent)
                .build();

            shortcuts.add(shortcut);
        }
        return shortcuts;
    }
}
