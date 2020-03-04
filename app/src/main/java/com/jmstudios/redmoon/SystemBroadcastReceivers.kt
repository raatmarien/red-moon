/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
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
import com.jmstudios.redmoon.util.inActivePeriod
import com.jmstudios.redmoon.util.KLog
import com.jmstudios.redmoon.util.Logger

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Boot broadcast received!")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // If the filter was on when the device was powered down and the
            // automatic brightness setting is on, then it still uses the
            // dimmed brightness and we need to restore the saved brightness.
            BrightnessManager(context).brightnessLowered = false

            ScheduleReceiver.scheduleNextOnCommand()
            ScheduleReceiver.scheduleNextOffCommand()

            Command.toggle(filterIsOnPrediction(Log))
        }
    }

    companion object : Logger()
}

class TimeZoneChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("System time zone change broadcast received!")

        if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            ScheduleReceiver.rescheduleOnCommand()
            ScheduleReceiver.rescheduleOffCommand()
            Command.toggle(filterIsOnPrediction(Log))
        }
    }

    companion object : Logger()
}

fun filterIsOnPrediction(Log: KLog): Boolean {
    // If schedule is not enabled, restore the previous state before shutdown
    return if (Config.scheduleOn) inActivePeriod(Log) else Config.filterIsOn
}
