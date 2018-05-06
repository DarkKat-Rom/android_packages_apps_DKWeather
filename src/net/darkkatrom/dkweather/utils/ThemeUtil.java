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
import android.util.TypedValue;

import net.darkkatrom.dkweather.WeatherInfo;

public class ThemeUtil {

    public static int getConditionIconColor(Context context, int conditionIconType) {
        int conditionIconColor = 0;
        if (conditionIconType == WeatherInfo.ICON_MONOCHROME) {
            TypedValue tv = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.colorControlNormal, tv, true);
            if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                conditionIconColor = tv.data;
            } else {
                conditionIconColor = context.getColor(tv.resourceId);
            }
        }
        return conditionIconColor;
    }
}
