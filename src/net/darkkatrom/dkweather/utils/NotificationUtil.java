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

import android.app.Notification;
import android.app.Notification.Action;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.TextView;

import net.darkkatrom.dkweather.R;
import net.darkkatrom.dkweather.WeatherInfo;
import net.darkkatrom.dkweather.WeatherInfo.DayForecast;
import net.darkkatrom.dkweather.activities.MainActivity;
import net.darkkatrom.dkweather.utils.Config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationUtil {
    public static final int WEATHER_NOTIFICATION_ID            = 1;

    public static final String WEATHER_NOTIFICATION_CHANNEL_ID =
            "weather_notification_chanel";

    private final Context mContext;
    private final Resources mResources;

    public NotificationUtil(Context context) {
        mContext = context;
        mResources = context.getResources();
    }

    public void sendNotification() {
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(WEATHER_NOTIFICATION_ID, createWeatherNotification());
    }

    private Notification createWeatherNotification() {
        WeatherInfo info =  Config.getWeatherData(mContext);
        boolean showDKIcon =  Config.getNotificationShowDKIcon(mContext);
        int notificationColor = mContext.getColor(R.color.accent_darkkat);

        Notification.Builder builder = new Notification.Builder(mContext, WEATHER_NOTIFICATION_CHANNEL_ID)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setOngoing(true)
            .setStyle(new Notification.DecoratedCustomViewStyle())
            .setCustomContentView(getCollapsedContent(info, notificationColor))
            .setCustomBigContentView(getExpandedContent(info, notificationColor))
            .setColor(notificationColor)
            .addAction(getSettingsAction());

        if (showDKIcon) {
            builder.setSmallIcon(R.drawable.ic_dk);
        } else {
            builder.setSmallIcon(textAsSmallIcon(info.getTemperature(), info.getFormattedTemperature()));
        }

        return builder.build();
    }

    private Icon textAsSmallIcon(String textSmall, String textLarge) {
        int iconSize = mResources.getDimensionPixelSize(R.dimen.notification_small_icon_size);
        int maxTextWidth = iconSize;
        int maxTextHeight = mResources.getDimensionPixelSize(R.dimen.notification_small_icon_max_text_height);
        int iconColor = mContext.getColor(R.color.notification_small_icon_color);
        String usedText = textLarge;
        float textSize = 0f;
        float textHeight = 0f;
        float textX = iconSize * 0.5f;
        float textY = 0f;

        TextPaint paint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        Bitmap b = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        Rect bounds = new Rect();

        TextView tv = new TextView(mContext);
        paint.setTextAlign(TextPaint.Align.CENTER);

        tv.setTextAppearance(android.R.style.TextAppearance_Material_Notification_Info);
        Typeface tf = tv.getTypeface();
        paint.setTypeface(tv.getTypeface());
        paint.setColor(iconColor);

        do {
            paint.setTextSize(textSize++);
            paint.getTextBounds(usedText, 0, usedText.length(), bounds);
        } while (bounds.height() < maxTextHeight);
        paint.setTextSize(textSize);

        if (bounds.width() > maxTextWidth) {
            usedText = textSmall;
            paint.getTextBounds(usedText, 0, usedText.length(), bounds);
            if (bounds.width() > maxTextWidth) {
                do {
                    paint.setTextSize(textSize--);
                    paint.getTextBounds(usedText, 0, usedText.length(), bounds);
                } while (bounds.width() > maxTextWidth);
            }
        }
        paint.setTextSize(textSize);

        textHeight = -paint.getFontMetrics().ascent;
        textY = (iconSize + textHeight) * 0.47f;
        canvas.drawText(usedText, textX, textY, paint);

        return Icon.createWithBitmap(b);
    }

    private RemoteViews getCollapsedContent(WeatherInfo info, int notificationColor) {
        Icon icon = getConditionIcon(info.getConditionCode(), notificationColor);
        boolean showLocation =  Config.getNotificationShowLocation(mContext);
        String title = info.getFormattedTemperature() + " - " + info.getCondition();
        String text = showLocation ? info.getCity() : "";
        RemoteViews collapsedContent = new RemoteViews(mContext.getPackageName(),
                R.layout.notification_collapsed_content);

        collapsedContent.setOnClickPendingIntent(R.id.collapsed_content, getContentIntent(5, 0));
        collapsedContent.setImageViewIcon(R.id.content_image, icon);
        collapsedContent.setTextViewText(R.id.content_title, title);
        collapsedContent.setTextViewText(R.id.content_text, text);
        return collapsedContent;
    }

    private RemoteViews getExpandedContent(WeatherInfo info, int notificationColor) {
        TimeZone myTimezone = TimeZone.getDefault();
        Calendar calendar = new GregorianCalendar(myTimezone);
        ArrayList<DayForecast> forecasts = (ArrayList) info.getForecasts();
        RemoteViews expandedContent = new RemoteViews(mContext.getPackageName(),
                R.layout.notification_expanded_content);

        for (int i = 0; i < 5; i++) {
            RemoteViews dayContent = new RemoteViews(mContext.getPackageName(),
                    R.layout.notification_expanded_content_item);
            dayContent.setOnClickPendingIntent(R.id.expanded_content_item, getContentIntent(i, i));

            DayForecast d = forecasts.get(i);
            String dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT,
                    Locale.getDefault());
            Icon icon = getConditionIcon(d.getConditionCode(), notificationColor);
            String dayTemps = d.getFormattedLow() + " | " + d.getFormattedHigh();

            dayContent.setTextViewText(R.id.content_item_day, dayName);
            dayContent.setImageViewIcon(R.id.content_item_image, icon);
            dayContent.setTextViewText(R.id.content_item_temp, dayTemps);

            calendar.roll(Calendar.DAY_OF_WEEK, true);
            expandedContent.addView(R.id.expanded_content, dayContent);
        }
        return expandedContent;
    }

    private Icon getConditionIcon(int conditionCode, int notificationColor) {
        int iconResid = mResources.getIdentifier(
                "weather_" + conditionCode, "drawable", mContext.getPackageName());
        Icon icon = Icon.createWithResource(mContext, iconResid);
        icon.setTint(notificationColor);
        return icon;
    }

    private PendingIntent getContentIntent(int requestCode, int day) {
        Bundle b = new Bundle();
        b.putInt(MainActivity.KEY_VISIBLE_SCREEN, day);
        b.putInt(MainActivity.KEY_DAY_INDEX, day);
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtras(b);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private Action getSettingsAction() {
        Bundle b = new Bundle();
        b.putInt(MainActivity.KEY_VISIBLE_SCREEN, MainActivity.SETTINGS);
        b.putInt(MainActivity.KEY_DAY_INDEX, MainActivity.TODAY);
        String title = mResources.getString(R.string.settings_title);
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtras(b);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 6, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Action.Builder builder = new Action.Builder(R.drawable.ic_notification_action_settings,
                title, pendingIntent);
        return builder.build();
    }

    public void setNotificationChannels() {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = getWeatherNotificationChannel();

        channel.setSound(null, null);
        channel.enableLights(false);
        channel.enableVibration(false);

        notificationManager.createNotificationChannel(channel);
    }

    private NotificationChannel getWeatherNotificationChannel() {
        String id = WEATHER_NOTIFICATION_CHANNEL_ID;
        CharSequence name = mContext.getString(R.string.weather_notification_chanel_title);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        String description = mContext.getString(R.string.weather_notification_chanel_description);

        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);

        return channel;
    }

    public static void removeNotification(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(WEATHER_NOTIFICATION_ID);
    }
}
