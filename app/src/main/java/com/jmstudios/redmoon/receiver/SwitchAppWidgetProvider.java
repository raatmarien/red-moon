/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
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
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (c) 2016 Zoraver <https://github.com/Zoraver>
 *
 *     Permission to use, copy, modify, and/or distribute this software
 *     for any purpose with or without fee is hereby granted, provided
 *     that the above copyright notice and this permission notice appear
 *     in all copies.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *     WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *     WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *     AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 *     CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 *     OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *     NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 *     CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.jmstudios.redmoon.receiver;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.helper.FilterCommandFactory;
import com.jmstudios.redmoon.model.SettingsModel;
import com.jmstudios.redmoon.service.ScreenFilterService;

public class SwitchAppWidgetProvider extends AppWidgetProvider {
    public final static String ACTION_TOGGLE = "com.jmstudios.redmoon.action.APPWIDGET_TOGGLE";
    public final static String ACTION_UPDATE = "com.jmstudios.redmoon.action.APPWIDGET_UPDATE";
    public final static String EXTRA_POWER = "com.jmstudios.redmoon.action.APPWIDGET_EXTRA_POWER";
    private final static String TAG = "SwitchAppWidgetProvider";
    private final static boolean DEBUG = false;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if(DEBUG) Log.i(TAG, "Updating!");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SettingsModel settingsModel = new SettingsModel(context.getResources(), sharedPreferences);

        for(int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent toggleIntent = new Intent(context, SwitchAppWidgetProvider.class);
            toggleIntent.setAction(SwitchAppWidgetProvider.ACTION_TOGGLE);
            PendingIntent togglePendingIntent = PendingIntent.getBroadcast(context, 0, toggleIntent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_switch);
            views.setOnClickPendingIntent(R.id.widget_pause_play_button, togglePendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
            updateImage(context, settingsModel.getShadesPauseState());
        }
    }

    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(SwitchAppWidgetProvider.ACTION_TOGGLE)) toggle(context);
        else if(intent.getAction().equals(SwitchAppWidgetProvider.ACTION_UPDATE))
            updateImage(context, intent.getBooleanExtra(SwitchAppWidgetProvider.EXTRA_POWER, false));
        else super.onReceive(context, intent);
    }

    void toggle(Context context) {
        FilterCommandFactory commandFactory = new FilterCommandFactory(context);
        Intent onCommand = commandFactory.createCommand(ScreenFilterService.COMMAND_ON);
        Intent pauseCommand = commandFactory.createCommand(ScreenFilterService.COMMAND_PAUSE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SettingsModel settingsModel = new SettingsModel(context.getResources(), sharedPreferences);

        if(settingsModel.getShadesPauseState()) {
            context.startService(onCommand);
        } else {
            context.startService(pauseCommand);
        }
    }

    void updateImage(Context context, boolean pausedState) {
        if(DEBUG) Log.i(TAG, "Updating image!");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_switch);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName appWidgetComponent = new ComponentName(context, SwitchAppWidgetProvider.class.getName());

        int drawable;

        if(!pausedState) drawable = R.drawable.ic_play;
        else drawable = R.drawable.ic_pause;

        views.setInt(R.id.widget_pause_play_button, "setImageResource", drawable);
        appWidgetManager.updateAppWidget(appWidgetComponent, views);
    }
}
