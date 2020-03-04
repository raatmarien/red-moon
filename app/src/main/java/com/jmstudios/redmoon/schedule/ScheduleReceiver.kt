/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.jmstudios.redmoon.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.AlarmManagerCompat

import com.jmstudios.redmoon.filter.Command
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.util.*

import java.util.Calendar
import java.util.GregorianCalendar

class ScheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Alarm received")

        val turnOn = intent.data.toString() == "turnOnIntent"

        Command.toggle(turnOn)
        cancelAlarm(turnOn)
        scheduleNextCommand(turnOn)

        LocationUpdateService.update(foreground = false)
    }

    companion object : Logger() {
        private val intent: Intent
            get() = Intent(appContext, ScheduleReceiver::class.java)

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
            if (Config.scheduleOn) {
                Log.d("Scheduling alarm to turn filter ${if (turnOn) "on" else "off"}")
                val time = if (turnOn) {
                    Config.scheduledStartTime
                } else {
                    Config.scheduledStopTime
                }

                val command = intent.apply {
                    data = Uri.parse(if (turnOn) "turnOnIntent" else "offIntent")
                    putExtra("turn_on", turnOn)
                }

                val calendar = GregorianCalendar().apply {
                    set(Calendar.HOUR_OF_DAY, time.substringBefore(':').toInt())
                    set(Calendar.MINUTE, time.substringAfter(':').toInt())
                }

                val now = GregorianCalendar()
                now.add(Calendar.SECOND, 1)
                if (calendar.before(now)) { calendar.add(Calendar.DATE, 1) }

                Log.i("Scheduling alarm for " + calendar.toString())

                val pendingIntent = PendingIntent.getBroadcast(appContext, 0, command, 0)

                AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC,
                                                        calendar.timeInMillis, pendingIntent)
            } else {
                Log.i("Tried to schedule alarm, but schedule is disabled.")
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
