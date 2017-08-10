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

class ScreenStateReceiver(private val mListener: ScreenStateReceiver.ScreenStateListener?) : BroadcastReceiver() {

    companion object : Logger()

    interface ScreenStateListener {
        fun onScreenTurnedOn()
        fun onScreenTurnedOff()
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Intent received")

        if (Intent.ACTION_SCREEN_ON == intent.action) {
            Log.i("Screen turned on")

            mListener?.onScreenTurnedOn()
        } else if (Intent.ACTION_SCREEN_OFF == intent.action) {
            Log.i("Screen turned off")

            mListener?.onScreenTurnedOff()
        }
    }
}
