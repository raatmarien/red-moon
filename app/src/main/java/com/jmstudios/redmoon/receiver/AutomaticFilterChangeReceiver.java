/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jmstudios.redmoon.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.app.AlarmManager;
import android.net.Uri;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Handler;

import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.TimeZone;

import com.jmstudios.redmoon.helper.FilterCommandFactory;
import com.jmstudios.redmoon.helper.FilterCommandSender;
import com.jmstudios.redmoon.helper.DismissNotificationRunnable;
import com.jmstudios.redmoon.model.SettingsModel;
import com.jmstudios.redmoon.service.ScreenFilterService;
import com.jmstudios.redmoon.presenter.ScreenFilterPresenter;

public class AutomaticFilterChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "AutomaticFilterChange";
    private static final boolean DEBUG = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) Log.i(TAG, "Alarm received");
        FilterCommandSender commandSender = new FilterCommandSender(context);
        FilterCommandFactory commandFactory = new FilterCommandFactory(context);
        Intent onCommand = commandFactory.createCommand(ScreenFilterService.COMMAND_ON);
        Intent pauseCommand = commandFactory.createCommand(ScreenFilterService.COMMAND_PAUSE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SettingsModel settingsModel = new SettingsModel(context.getResources(), sharedPreferences);

        boolean turnOn = intent.getData().toString().equals("turnOnIntent");

        if (turnOn) {
            commandSender.send(onCommand);
            cancelTurnOnAlarm(context);
            scheduleNextOnCommand(context);
        } else {
            commandSender.send(pauseCommand);
            cancelPauseAlarm(context);
            scheduleNextPauseCommand(context);

            // We want to dismiss the notification if the filter is paused
            // automatically.
            // However, the filter fades out and the notification is only
            // refreshed when this animation has been completed.  To make sure
            // that the new notification is removed we create a new runnable to
            // be excecuted 100 ms after the filter has faded out.
            Handler handler = new Handler();

            DismissNotificationRunnable runnable = new DismissNotificationRunnable(context);
            handler.postDelayed(runnable, ScreenFilterPresenter.FADE_DURATION_MS + 100);
        }

        // Update times for the next time (fails silently)
        LocationManager locationManager = (LocationManager)
            context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            LocationListener listener = new LocationUpdateListener(context);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                                   0, 0, listener);
        }
    }

    public static void scheduleNextOnCommand(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SettingsModel settingsModel = new SettingsModel(context.getResources(), sharedPreferences);

        if (!settingsModel.getAutomaticFilterMode().equals("never")) {
            String time;
            time = settingsModel.getAutomaticTurnOnTime();

            Intent turnOnIntent = new Intent(context, AutomaticFilterChangeReceiver.class);
            turnOnIntent.setData(Uri.parse("turnOnIntent"));
            turnOnIntent.putExtra("turn_on", true);

            scheduleNextAlarm(context, time, turnOnIntent, false);
        }
    }

    public static void scheduleNextPauseCommand(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SettingsModel settingsModel = new SettingsModel(context.getResources(), sharedPreferences);

        if (!settingsModel.getAutomaticFilterMode().equals("never")) {
            String time = settingsModel.getAutomaticTurnOffTime();

            Intent pauseIntent = new Intent(context, AutomaticFilterChangeReceiver.class);
            pauseIntent.putExtra("turn_on", false);
            pauseIntent.setData(Uri.parse("pauseIntent"));

            scheduleNextAlarm(context, time, pauseIntent, false);
        }
    }

    public static void scheduleNextAlarm(Context context, String time, Intent operation, boolean timeInUtc) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1]));

        GregorianCalendar now = new GregorianCalendar();
        now.add(Calendar.SECOND, 1);
        if (calendar.before(now)) {
            calendar.add(Calendar.DATE, 1);
        }
        if (!timeInUtc)
            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (DEBUG) Log.i(TAG, "Scheduling alarm for " + calendar.toString());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, operation, 0);

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static void cancelAlarms(Context context) {
        cancelPauseAlarm(context);
        cancelTurnOnAlarm(context);
    }

    public static void cancelPauseAlarm(Context context) {
        Intent commands = new Intent(context, AutomaticFilterChangeReceiver.class);
        commands.setData(Uri.parse("pauseIntent"));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, commands, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public static void cancelTurnOnAlarm(Context context) {
        Intent commands = new Intent(context, AutomaticFilterChangeReceiver.class);
        commands.setData(Uri.parse("turnOnIntent"));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, commands, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
