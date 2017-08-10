/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri

import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.service.LocationUpdateService
import com.jmstudios.redmoon.service.ScreenFilterService
import com.jmstudios.redmoon.util.*

import java.util.Calendar
import java.util.GregorianCalendar


class TimeToggleChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Alarm received")

        val turnOn = intent.data.toString() == "turnOnIntent"

        if (Config.useLocation) {
            ScreenFilterService.fade(turnOn)
        } else {
            ScreenFilterService.toggle(turnOn)
        }
        cancelAlarm(turnOn)
        scheduleNextCommand(turnOn)

        LocationUpdateService.update(foreground = false)
    }

    companion object : Logger() {
        private val intent: Intent
            get() = Intent(appContext, TimeToggleChangeReceiver::class.java)

        private val alarmManager: AlarmManager
            get() = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Conveniences
        fun scheduleNextOnCommand()  = scheduleNextCommand(true)
        fun scheduleNextOffCommand() = scheduleNextCommand(false)
        fun rescheduleOnCommand()  = rescheduleCommand(true)
        fun rescheduleOffCommand() = rescheduleCommand(false)
        private fun rescheduleCommand(on: Boolean) {
            cancelAlarm(on)
            scheduleNextCommand(on)
        }
        fun cancelAlarms() {
            cancelAlarm(true)
            cancelAlarm(false)
        }

        private fun scheduleNextCommand(turnOn: Boolean) {
            if (Config.timeToggle) {
                Log.d("Scheduling alarm to turn filter ${if (turnOn) "on" else "off"}")
                val time = if (turnOn) { Config.automaticTurnOnTime }
                           else { Config.automaticTurnOffTime }

                val command = intent.apply {
                    data = Uri.parse(if (turnOn) "turnOnIntent" else "offIntent")
                    putExtra("turn_on", turnOn)
                }

                val calendar = GregorianCalendar().apply {
                    set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()[0]))
                    set(Calendar.MINUTE, Integer.parseInt(time.split(":".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()[1]))
                }

                val now = GregorianCalendar()
                now.add(Calendar.SECOND, 1)
                if (calendar.before(now)) { calendar.add(Calendar.DATE, 1) }

                Log.i("Scheduling alarm for " + calendar.toString())

                val pendingIntent = PendingIntent.getBroadcast(appContext, 0, command, 0)

                if (atLeastAPI(19)) {
                    alarmManager.setExact(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
                }
            } else {
                Log.i("Tried to schedule alarm, but timer is disabled.")
            }
        }

        private fun cancelAlarm(turnOn: Boolean) {
            Log.d("Canceling alarm to turn filter ${if (turnOn) "on" else "off"}")
            val command = intent.apply {
                data = Uri.parse(if (turnOn) "turnOnIntent" else "offIntent")
            }
            val pendingIntent = PendingIntent.getBroadcast(appContext, 0, command, 0)
            alarmManager.cancel(pendingIntent)
        }
    }
}
