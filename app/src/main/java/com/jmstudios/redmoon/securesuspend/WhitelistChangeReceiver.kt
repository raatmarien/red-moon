/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.jmstudios.redmoon.securesuspend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.jmstudios.redmoon.filter.Command
import com.jmstudios.redmoon.util.*

class WhitelistChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra(EXTRA_ACTION)
        val app = CurrentAppMonitor.currentApp
        when (action) {
            ACTION_ADD -> {
                Whitelist.add(app)
                Command.PAUSE.send()
            }
            ACTION_REMOVE -> {
                Whitelist.remove(app)
                Command.RESUME.send()
            }
        }
    }

    companion object : Logger() {
        fun intent(add: Boolean): Intent {
            return intent(WhitelistChangeReceiver::class).apply {
                putExtra(EXTRA_ACTION, if (add) ACTION_ADD else ACTION_REMOVE)
            }
        }

        private const val EXTRA_ACTION = "jmstudios.bundle.key.wlAction"
        private const val ACTION_ADD = "add"
        private const val ACTION_REMOVE = "remove"
    }
}
