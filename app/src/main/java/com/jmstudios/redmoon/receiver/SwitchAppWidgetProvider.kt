/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
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

package com.jmstudios.redmoon.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.service.ScreenFilterService
import com.jmstudios.redmoon.util.*

class SwitchAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.i("Updating!")

        for (i in appWidgetIds.indices) {
            val appWidgetId = appWidgetIds[i]

            val toggleIntent = Intent(context, SwitchAppWidgetProvider::class.java)
            toggleIntent.action = SwitchAppWidgetProvider.ACTION_TOGGLE
            val togglePendingIntent = PendingIntent.getBroadcast(context, 0, toggleIntent, 0)

            val views = RemoteViews(context.packageName, R.layout.appwidget_switch)
            views.setOnClickPendingIntent(R.id.widget_pause_play_button, togglePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            updateImage(context, filterIsOn)
        }
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == SwitchAppWidgetProvider.ACTION_TOGGLE) {
            ScreenFilterService.toggle()
        } else if (intent.action == SwitchAppWidgetProvider.ACTION_UPDATE) {
            updateImage(ctx, !intent.getBooleanExtra(SwitchAppWidgetProvider.EXTRA_POWER, false))
        } else {
            super.onReceive(ctx, intent)
        }
    }

    internal fun updateImage(context: Context, filterIsOn: Boolean) {
        Log.i("Updating image!")
        val views = RemoteViews(context.packageName, R.layout.appwidget_switch)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetComponent = ComponentName(context, SwitchAppWidgetProvider::class.java.name)
        val drawable = if (filterIsOn) R.drawable.ic_play
                       else R.drawable.ic_stop

        views.setInt(R.id.widget_pause_play_button, "setImageResource", drawable)
        appWidgetManager.updateAppWidget(appWidgetComponent, views)
    }

    companion object : Logger() {
        const val ACTION_TOGGLE = "com.jmstudios.redmoon.action.APPWIDGET_TOGGLE"
        const val ACTION_UPDATE = "com.jmstudios.redmoon.action.APPWIDGET_UPDATE"
        const val EXTRA_POWER = "com.jmstudios.redmoon.action.APPWIDGET_EXTRA_POWER"
    }
}
