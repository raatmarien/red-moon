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
 *     Copyright (c) 2015 Chris Nguyen
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

package com.jmstudios.redmoon.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.view.WindowManager
import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.activity.MainActivity

import com.jmstudios.redmoon.helper.EventBus
import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.manager.BrightnessManager
import com.jmstudios.redmoon.manager.CurrentAppMonitor
import com.jmstudios.redmoon.manager.ScreenManager
import com.jmstudios.redmoon.manager.WindowViewManager
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.model.profiles.name
import com.jmstudios.redmoon.presenter.ScreenFilterPresenter
import com.jmstudios.redmoon.receiver.NextProfileCommandReceiver
import com.jmstudios.redmoon.receiver.OrientationChangeReceiver
import com.jmstudios.redmoon.util.*
import com.jmstudios.redmoon.view.ScreenFilterView

private const val BUNDLE_KEY_COMMAND = "jmstudios.bundle.key.COMMAND"
private const val COMMAND_MISSING = -1
private const val NOTIFICATION_ID = 1

private const val REQUEST_CODE_ACTION_SETTINGS = 1000
private const val REQUEST_CODE_ACTION_TOGGLE   = 3000
private const val REQUEST_CODE_NEXT_PROFILE    = 4000
private const val REQUEST_CODE_ACTION_STOP     = 5000

interface ServiceController {
    fun start(time: Int? = null)
    fun pause(time: Int? = null)
    fun stop (time: Int? = null)
}

class ScreenFilterService : Service(), ServiceController {
    enum class Command {
        ON, OFF, TOGGLE, PAUSE, SUSPEND, RESUME,
        FADE_ON, FADE_OFF, SHOW_PREVIEW, HIDE_PREVIEW
    }

    val notificationManager: NotificationManager
        get() = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private lateinit var mPresenter:           ScreenFilterPresenter
    private lateinit var mOrientationReceiver: OrientationChangeReceiver

    private lateinit var mBrightnessManager: BrightnessManager
    private lateinit var mCurrentAppMonitor: CurrentAppMonitor
    private lateinit var mWindowViewManager: WindowViewManager

    override fun onCreate() {
        super.onCreate()
        Log.i("onCreate")
        filterIsOn = false

        // If we ever support a root mode, pass a different view here
        val view = ScreenFilterView(this)
        val wMan = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val sMan = ScreenManager(this, wMan)

        mCurrentAppMonitor   = CurrentAppMonitor(this)
        mBrightnessManager   = BrightnessManager(this)
        mPresenter           = ScreenFilterPresenter(this)
        mWindowViewManager   = WindowViewManager(view, sMan, wMan)
        mOrientationReceiver = OrientationChangeReceiver(mWindowViewManager)

        EventBus.register(mWindowViewManager)
        EventBus.register(mCurrentAppMonitor)
        registerReceiver(mOrientationReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_CONFIGURATION_CHANGED)
        })
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(String.format("onStartCommand(%s, %d, %d", intent, flags, startId))
        val flag = intent.getIntExtra(BUNDLE_KEY_COMMAND, COMMAND_MISSING)
        Log.i("Recieved flag: $flag")
        if (flag != COMMAND_MISSING) mPresenter.handleCommand(Command.values()[flag])

        // Do not attempt to restart if the hosting process is killed by Android
        return Service.START_NOT_STICKY
    }

    override fun start(time: Int?) {
        Log.i("start(time = $time)")
        startForeground(NOTIFICATION_ID, getNotification(true))
        mWindowViewManager.open(time)
        mBrightnessManager.lower()
        mCurrentAppMonitor.start()
    }

    override fun pause(time: Int?) {
        Log.i("pause(time = $time)")
        notificationManager.notify(NOTIFICATION_ID, getNotification(false))
        stopForeground(false)
        mWindowViewManager.close(time)
    }

    override fun stop(time: Int?) {
        Log.i("stop(time = $time)")
        mWindowViewManager.close(time) {
            mBrightnessManager.restore()
            mCurrentAppMonitor.stop()
            stopForeground(true)
            stopSelf()
        }
    }

    override fun onBind(intent: Intent): IBinder? = null // Prevent binding.

    override fun onDestroy() {
        Log.i("onDestroy")
        EventBus.unregister(mWindowViewManager)
        EventBus.unregister(mCurrentAppMonitor)
        unregisterReceiver(mOrientationReceiver)
        filterIsOn = false
        super.onDestroy()
    }

    private fun getNotification(filterIsOn: Boolean): Notification {
        fun servicePI(code: Int, intent: Intent) = PendingIntent.getService(
                 appContext, code, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        fun activityPI(code: Int, intent: Intent) = PendingIntent.getActivity(
                   appContext, code, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        fun broadcastPI(code: Int, intent: Intent) = PendingIntent.getBroadcast(
                                                     appContext, code, intent, 0)

        val nb = NotificationCompat.Builder(appContext).apply {
            // Set notification appearance
            setSmallIcon(R.drawable.notification_icon_half_moon)
            color    = ContextCompat.getColor(appContext, R.color.color_primary)
            priority = Notification.PRIORITY_MIN

            if (belowAPI(24)) { setContentTitle(getString(R.string.app_name)) }
            setContentText(if (filterIsOn) {
                activeProfile.name
            } else {
                getString(R.string.notification_status_paused)
            })

            // Open Red Moon when tapping notification body
            val mainActivityIntent = Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            setContentIntent(activityPI(REQUEST_CODE_ACTION_SETTINGS, mainActivityIntent))

            // Turn off Red Moon when the notification is manually dismissed
            setDeleteIntent(servicePI(REQUEST_CODE_ACTION_STOP, intent(Command.OFF)))

            // Add toggle action
            val (toggleIcon, toggleTextResId, togglePI) = if (filterIsOn) {
                Triple(R.drawable.ic_stop_circle_outline_white_36dp,
                       R.string.notification_action_turn_off,
                       servicePI(REQUEST_CODE_ACTION_TOGGLE, intent(Command.PAUSE)))
            } else {
                Triple(R.drawable.ic_play,
                       R.string.notification_action_turn_on,
                       servicePI(REQUEST_CODE_ACTION_TOGGLE, intent(Command.ON)))
            }

            addAction(toggleIcon, getString(toggleTextResId), togglePI)

            // Add profile switch action
            val nextProfileText   = getString(R.string.notification_action_next_filter)
            val nextProfileIntent = Intent(appContext, NextProfileCommandReceiver::class.java)
            val nextProfilePI     = broadcastPI(REQUEST_CODE_NEXT_PROFILE, nextProfileIntent)
            addAction(R.drawable.ic_skip_next_white_36dp, nextProfileText, nextProfilePI)
        }
        return nb.build()
    }

    companion object : Logger() {
        fun toggle(on: Boolean? = null) = moveToState(when (on) {
            true  -> Command.ON
            false -> Command.OFF
            null  -> Command.TOGGLE
        })

        fun pause(on: Boolean = true) = moveToState(when(on) {
            true  -> Command.PAUSE
            false -> Command.RESUME
        })

        fun preview(on: Boolean = true) = moveToState(when(on) {
            true  -> Command.SHOW_PREVIEW
            false -> Command.HIDE_PREVIEW
        })

        fun fade(on: Boolean = true) = moveToState(when(on) {
            true  -> Command.FADE_ON
            false -> Command.FADE_OFF
        })
    }
}

private fun moveToState(c: ScreenFilterService.Command): ComponentName = appContext.startService(intent(c))
private fun intent(c: ScreenFilterService.Command) = intent(ScreenFilterService::class).apply {
    putExtra(BUNDLE_KEY_COMMAND, c.ordinal)
}
