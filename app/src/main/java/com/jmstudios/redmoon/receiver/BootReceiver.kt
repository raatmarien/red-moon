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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.manager.BrightnessManager
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.service.ScreenFilterService

import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Boot broadcast received!")

        // If the filter was on when the device was powered down and the
        // automatic brightness setting is on, then it still uses the
        // dimmed brightness and we need to restore the saved brightness.
        BrightnessManager(context).restore()

        TimeToggleChangeReceiver.scheduleNextOnCommand()
        TimeToggleChangeReceiver.scheduleNextOffCommand()

        ScreenFilterService.toggle(filterIsOnPrediction)
    }

    private val filterIsOnPrediction: Boolean = if (Config.timeToggle) {
        val now = Calendar.getInstance()

        val onTime = Config.automaticTurnOnTime
        val onHour = Integer.parseInt(onTime.split(":".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()[0])
        val onMinute = Integer.parseInt(onTime.split(":".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()[1])
        val on = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, onHour)
            set(Calendar.MINUTE, onMinute)
            if (after(now)) {
                add(Calendar.DATE, -1)
            }
        }

        val offTime = Config.automaticTurnOffTime
        val offHour = Integer.parseInt(offTime.split(":".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()[0])
        val offMinute = Integer.parseInt(offTime.split(":".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()[1])
        val off = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, offHour)
            set(Calendar.MINUTE, offMinute)
            while (before(on)) {
                add(Calendar.DATE, 1)
            }
        }

        Log.d("On: $onTime, off: $offTime")
        Log.d("On DAY_OF_MONTH: " + Integer.toString(on.get(Calendar.DAY_OF_MONTH)))
        Log.d("Off DAY_OF_MONTH: " + Integer.toString(off.get(Calendar.DAY_OF_MONTH)))

        now.after(on) && now.before(off)
    } else {
        // Here we want the config value and not the actual state of the filter
        // since we're interested in what it was before reboot
        Config.filterIsOn
    }

    companion object : Logger()
}
