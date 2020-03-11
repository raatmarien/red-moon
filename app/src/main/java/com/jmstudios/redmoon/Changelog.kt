/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.jmstudios.redmoon

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AlertDialog

import com.jmstudios.redmoon.model.Config

import java.io.BufferedReader
import java.io.InputStreamReader

fun showChangelogAuto(activity: Activity): Dialog? {
    val lastShown = Config.lastChangelogShown
    Config.lastChangelogShown = BuildConfig.VERSION_CODE
    return when (lastShown) {
        0, BuildConfig.VERSION_CODE -> null // First run or already shown
        else -> showChangelog(activity)
    }
}

fun showChangelog(activity: Activity): Dialog? {
    val changelog = StringBuilder()
    val input = activity.assets.open("changelog.md")
    val reader = BufferedReader(InputStreamReader(input))
    reader.forEachLine {
        changelog.append(it).append("\n")
    }
    reader.close()

    val dialog = AlertDialog.Builder(activity).run {
        setMessage(changelog.toString())
        setCancelable(true)
        setPositiveButton("OK", null)
        create()
    }
    dialog.show()
    return dialog
}
