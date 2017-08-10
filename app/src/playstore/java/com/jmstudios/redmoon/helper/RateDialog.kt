/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017 Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
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
