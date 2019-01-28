/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (c) 2015 Chris Nguyen
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
package com.jmstudios.redmoon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.jmstudios.redmoon.filter.Command
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.filter.overlay.BrightnessManager
import com.jmstudios.redmoon.schedule.ScheduleReceiver
import com.jmstudios.redmoon.util.KLog
import com.jmstudios.redmoon.util.Logger

import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Boot broadcast received!")

        // If the filter was on when the device was powered down and the
        // automatic brightness setting is on, then it still uses the
        // dimmed brightness and we need to restore the saved brightness.
        BrightnessManager(context).brightnessLowered = false

        ScheduleReceiver.scheduleNextOnCommand()
        ScheduleReceiver.scheduleNextOffCommand()

        Command.toggle(filterIsOnPrediction(Log))
    }

    companion object : Logger()
}

class TimeZoneChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("System time zone change broadcast received!")

        ScheduleReceiver.rescheduleOnCommand()
        ScheduleReceiver.rescheduleOffCommand()

        Command.toggle(filterIsOnPrediction(Log))
    }

    companion object : Logger()
}

// Originally nested in BootReceiver, taken out to be shared with TimeZoneChangeReceiver
// Will be moved into a common lib, if there's any other place wants to use this
fun filterIsOnPrediction(Log: KLog): Boolean = if (Config.scheduleOn) {
    val now = Calendar.getInstance()

    val onTime = Config.scheduledStartTime
    val onHour = onTime.substringBefore(':').toInt()
    val onMinute = onTime.substringAfter(':').toInt()
    val on = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, onHour)
        set(Calendar.MINUTE, onMinute)
        if (after(now)) {
            add(Calendar.DATE, -1)
        }
    }

    val offTime = Config.scheduledStopTime
    val offHour = offTime.substringBefore(':').toInt()
    val offMinute = offTime.substringAfter(':').toInt()
    val off = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, offHour)
        set(Calendar.MINUTE, offMinute)
        while (before(on)) {
            add(Calendar.DATE, 1)
        }
    }

    Log.d("Start: $onTime, stop: $offTime")
    Log.d("On DAY_OF_MONTH: ${on.get(Calendar.DAY_OF_MONTH)}")
    Log.d("Off DAY_OF_MONTH: ${off.get(Calendar.DAY_OF_MONTH)}")

    now.after(on) && now.before(off)
} else {
    // Here we want the config value and not the actual state of the filter
    // since we're interested in what it was before reboot
    Config.filterIsOn
}
