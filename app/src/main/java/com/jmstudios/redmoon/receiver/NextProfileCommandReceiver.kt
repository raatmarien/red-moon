/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.model.profiles.next
import com.jmstudios.redmoon.util.activeProfile

class NextProfileCommandReceiver : BroadcastReceiver() {

    companion object : Logger()

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Next profile requested")
        activeProfile = activeProfile.next
    }
}
