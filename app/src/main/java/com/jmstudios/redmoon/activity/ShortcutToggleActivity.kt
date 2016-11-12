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
package com.jmstudios.redmoon.activity

import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log

import com.jmstudios.redmoon.helper.FilterCommandFactory
import com.jmstudios.redmoon.helper.FilterCommandSender
import com.jmstudios.redmoon.model.SettingsModel
import com.jmstudios.redmoon.service.ScreenFilterService

class ShortcutToggleActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (DEBUG) Log.i(TAG, "ShortcutToggleActivity created")
        toggleAndFinish(this)
    }

    companion object {
        private val TAG = "ShortcutToggleActivity"
        private val DEBUG = false

        fun toggleAndFinish(activity: Activity) {
            if (DEBUG) Log.i(TAG, "toggleAndFinish called.")
            val commandSender = FilterCommandSender(activity)
            val commandFactory = FilterCommandFactory(activity)
            val onCommand = commandFactory.createCommand(ScreenFilterService.COMMAND_ON)
            val pauseCommand = commandFactory.createCommand(ScreenFilterService.COMMAND_PAUSE)

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val settingsModel = SettingsModel(activity.resources, sharedPreferences)
            val paused = settingsModel.pauseState

            if (paused) {
                commandSender.send(onCommand)
            } else {
                commandSender.send(pauseCommand)
            }

            activity.finish()
        }
    }
}