/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017 Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
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
package com.jmstudios.redmoon.filter.overlay

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.WindowManager

import com.jmstudios.redmoon.filter.manager.ScreenManager
import com.jmstudios.redmoon.filter.Filter
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.util.*

import kotlin.properties.Delegates

import org.greenrobot.eventbus.Subscribe

class Overlay(context: Context) : View(context), Filter,
        OrientationChangeReceiver.OnOrientationChangeListener {

    private val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mScreenManager = ScreenManager(context, mWindowManager)
    private val mOrientationReceiver = OrientationChangeReceiver(context, this)
    private val mBrightnessManager = BrightnessManager(context)

    override fun onCreate() {
        Log.i("onCreate()")
    }

    override var profile = activeProfile.off
        set(value) {
            Log.i("profile set to: $value")
            field = value
            filtering = !value.isOff
        }

    override fun onDestroy() {
        Log.i("onDestroy()")
        filtering = false
    }

    private var filtering: Boolean by Delegates.observable(false) { _, isOn, turnOn ->
        when {
            !isOn && turnOn -> show()
            isOn && !turnOn -> hide()
            isOn && turnOn -> update()
        }
    }

    private fun show() {
        updateLayoutParams()
        mWindowManager.addView(this, mLayoutParams)
        mBrightnessManager.brightnessLowered = profile.lowerBrightness
        mOrientationReceiver.register()
        EventBus.register(this)
    }

    private fun hide() {
        mBrightnessManager.brightnessLowered = false
        mWindowManager.removeView(this)
        mOrientationReceiver.unregister()
        EventBus.unregister(this)
    }

    private fun update() {
        invalidate() // Forces call to onDraw
        if (Config.buttonBacklightFlag == "dim") {
            reLayout()
        }
        mBrightnessManager.brightnessLowered = profile.lowerBrightness
    }

    private var mLayoutParams = mScreenManager.layoutParams
        get() = field.apply {
            buttonBrightness = Config.buttonBacklightLevel
            type = if (atLeastAPI(26)) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            }
        }

    private fun updateLayoutParams() {
        mLayoutParams = mScreenManager.layoutParams
    }

    private fun reLayout() = mWindowManager.updateViewLayout(this, mLayoutParams)

    override fun onDraw(canvas: Canvas) = canvas.drawColor(profile.filterColor)

    override fun onOrientationChanged() {
        updateLayoutParams()
        reLayout()
    }

    @Subscribe fun onButtonBacklightChanged(event: buttonBacklightChanged) {
        reLayout()
    }

    companion object : Logger()
}
