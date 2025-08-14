package com.example.weeksanddayscounterwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.RemoteViews;

import java.util.Calendar;

public class CounterWidgetProvider extends AppWidgetProvider {

    public static final String PREFS_NAME = "HelloWidgetPrefs";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
        // Schedule the next update at midnight
        scheduleNextUpdate(context);
    }

    @Override
    public void onEnabled(Context context) {
        // First widget added — start midnight updates
        scheduleNextUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Last widget removed — cancel updates
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0,
                new Intent(context, CounterWidgetProvider.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    private void scheduleNextUpdate(Context context) {
        Calendar midnight = Calendar.getInstance();
        midnight.add(Calendar.DAY_OF_YEAR, 1);
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);

        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);


        Intent intent = new Intent(context, CounterWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        // We need to pass all widget IDs so they get updated
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] widgetIds = appWidgetManager.getAppWidgetIds(
                new android.content.ComponentName(context, CounterWidgetProvider.class)
        );
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnight.getTimeInMillis(), pendingIntent);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String name = prefs.getString("name_" + widgetId, "User");
        String color = prefs.getString("color_" + widgetId, "#FFD740");
        String date = prefs.getString("date_" + widgetId, "");
        String datecolor = prefs.getString("colordate_" + widgetId, "#00EB9A");

        // Calculate weeks and days since the date
        String weeksAndDays = "";
        if (!date.isEmpty()) {
            try {
                String[] parts = date.split("/"); // "day/month/year"
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1; // Calendar month is 0-based
                int year = Integer.parseInt(parts[2]);

                Calendar startDate = Calendar.getInstance();
                startDate.set(year, month, day, 0, 0, 0);
                startDate.set(Calendar.MILLISECOND, 0);

                Calendar today = Calendar.getInstance();

                long diffMillis = today.getTimeInMillis() - startDate.getTimeInMillis();
                long diffDays = diffMillis / (24 * 60 * 60 * 1000);
                long weeks = diffDays / 7;
                long days = diffDays % 7;

                String weekModifier = (weeks == 1) ? "week" : "weeks";

                if (days == 0) {
                    weeksAndDays = weeks + " " + weekModifier;
                } else if (days == 1) {
                    weeksAndDays = weeks + " " + weekModifier + " " + days + " day";
                } else {
                    weeksAndDays = weeks + " " + weekModifier + " " + days + " days";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widget_text_name, name);
        views.setTextViewText(R.id.widget_text_counter, weeksAndDays);
        views.setTextColor(R.id.widget_text_name, Color.parseColor(color));
        views.setTextColor(R.id.widget_text_counter, Color.parseColor(datecolor));

        Intent intent = new Intent(context, WidgetConfigureActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        appWidgetManager.updateAppWidget(widgetId, views);
    }
}
