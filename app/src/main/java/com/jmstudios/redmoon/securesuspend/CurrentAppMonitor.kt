/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.jmstudios.redmoon.securesuspend

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager

import com.jmstudios.redmoon.filter.Command
import com.jmstudios.redmoon.filter.ScreenStateReceiver
import com.jmstudios.redmoon.util.*

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

import kotlin.properties.Delegates

class CurrentAppMonitor(
        private val mContext: Context,
        private val mExecutor: ScheduledExecutorService)
    : ScreenStateReceiver.ScreenStateListener {

    private val screenStateReceiver = ScreenStateReceiver(this)
    private var mAppChecker = CurrentAppChecker(mContext)
    private var mFuture: ScheduledFuture<*>? = null

    var monitoring : Boolean by Delegates.observable(false) {
        _, isOn, turnOn -> when {
            isOn == turnOn -> Log.i("Monitoring is already started/stopped")
            turnOn -> start()
            else -> stop()
        }
    }

    private val handleCurrentApp = Runnable {
        val newApp = mAppChecker.getCurrentApp(currentApp)
        Log.v("Current app is: $newApp")
        Log.v("Last app was: $currentApp")
        when {
            newApp == currentApp -> {} // state not changed
            newApp.isWhitelisted -> Command.PAUSE.send()
            else -> Command.RESUME.send()
        }
        currentApp = newApp
    }

    private var active: Boolean by Delegates.observable(false) {
        _, isOn, turnOn -> when {
            isOn == turnOn -> {} // state not changed
            turnOn -> {
                mFuture = mExecutor.scheduleWithFixedDelay(handleCurrentApp, 0, 1, TimeUnit.SECONDS)
            }
            else -> mFuture?.cancel(true)
        }
    }

    private fun start() {
        Log.v("Starting app monitoring")
        val pm = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenOn = pm.run {
            if (atLeastAPI(20)) isInteractive else @Suppress("DEPRECATION") isScreenOn
        }
        active = screenOn

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON )
        }
        mContext.registerReceiver(screenStateReceiver, filter)
    }

    private fun stop() {
        Log.v("Stopping app monitoring")
        mContext.unregisterReceiver(screenStateReceiver)
        active = false
    }

    override fun onScreenTurnedOn() {
        Log.v("Screen turn on received")
        active = true
    }

    override fun onScreenTurnedOff() {
        Log.v("Screen turn off received")
        active = false
    }

    companion object : Logger() {
        var currentApp: App = App("", "")
            private set
    }
}
