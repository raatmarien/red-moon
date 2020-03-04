/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.jmstudios.redmoon.securesuspend

data class App(val packageName: String, val className: String) {
    // TODO: check activity names
    val isWhitelisted: Boolean
        get() = Whitelist.contains(this) ?: when (packageName) {
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "me.phh.superuser",
            "com.google.android.packageinstaller",
            "com.owncloud.android" -> true
            "com.android.packageinstaller" ->
                className == "com.android.packageinstaller.PackageInstallerActivity"
            else -> false
        }
}
