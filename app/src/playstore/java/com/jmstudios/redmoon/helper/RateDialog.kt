/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017 Stephen Michel <s@smichel.me>
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
package com.jmstudios.redmoon.helper

import android.app.Activity
import hotchemi.android.rate.AppRate

fun showRateDialog(activity: Activity) {
    AppRate.with(activity)
        .setInstallDays(7) // default 10, 0 means install day.
        .setLaunchTimes(5) // default 10
        .setRemindInterval(3) // default 1
        .setShowLaterButton(true) // default true
        .monitor()

    AppRate.showRateDialogIfMeetsConditions(activity)
}
