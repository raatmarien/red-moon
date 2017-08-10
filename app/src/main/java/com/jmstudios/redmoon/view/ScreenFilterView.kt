/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.view

import android.content.Context
import android.graphics.Canvas
import android.view.View
import com.jmstudios.redmoon.helper.Logger

import com.jmstudios.redmoon.helper.Profile

class ScreenFilterView(context: Context) : View(context) {
    companion object: Logger()

    var profile: Profile = Profile(100, 0, 0, false)
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) = canvas.drawColor(profile.filterColor)
}
