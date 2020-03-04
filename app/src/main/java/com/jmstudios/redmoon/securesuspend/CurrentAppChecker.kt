/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.jmstudios.redmoon.securesuspend

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.ContextWrapper

import com.jmstudios.redmoon.util.*

class CurrentAppChecker(private val context: Context) {

    val isWorking: Boolean = getCurrentApp(App("", "")) != App("", "")

    fun getCurrentApp(default: App): App {
        // http://stackoverflow.com/q/33581311
        return try {
            if (atLeastAPI(21)) {
                getCurrentAppFromUsageStats(default)
            } else {
                currentAppFromActivityManager ?: default
            }
        } catch (e: Exception) {
            default
        }
    }

    @SuppressLint("WrongConstant")
    @TargetApi(21)
    private fun getCurrentAppFromUsageStats(default: App): App {
        // Although the UsageStatsManager was added in API 21, the
        // constant to specify it wasn't added until API 22.
        // So we use the value of that constant on API 21.
        val uss = if (belowAPI(22)) "usagestats" else @TargetApi(22) {
            Context.USAGE_STATS_SERVICE
        }
        val usm = context.getSystemService(uss) as UsageStatsManager
        val time = System.currentTimeMillis()
        // Only look at events in the past 10 seconds
        val events = usm.queryEvents(time - 1000 * 10, time)
        val event = UsageEvents.Event()

        tailrec fun findLastApp(app: App): App {
            return if (!events.hasNextEvent()) app else {
                events.getNextEvent(event)
                //https://developer.android.com/reference/android/app/usage
                //                       /UsageEvents.Event.html#getClassName()
                // although it's not documented in the api reference above,
                // one of the times the class name is null is when the
                // notification shade is shown and this activity is not
                // actually the one which will be restored.
                if (event.className == null) {
                    findLastApp(app)
                } else {
                    findLastApp(App(event.packageName, event.className))
                }
            }
        }

        return findLastApp(default)
    }

    private val currentAppFromActivityManager: App?
        @Suppress("DEPRECATION") get() {
            val bc = ContextWrapper(context).baseContext
            val am = bc.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val ta = am.getRunningTasks(1)[0].topActivity ?: return null
            return App(ta.packageName, ta.className)
        }
}
