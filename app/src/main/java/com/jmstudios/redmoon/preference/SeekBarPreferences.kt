/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.jmstudios.redmoon.preference

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet

import com.jmstudios.redmoon.model.Profile
import com.jmstudios.redmoon.model.Config

class ColorSeekBarPreference(context: Context, attrs: AttributeSet) : SeekBarPreference(context, attrs) {

    override val suffix = "K"

    override val max =  100
        
    override val color: Int
        get() = Profile.rgbFromColor(mProgress)

    override val progress: Int
        get() = Profile.getColorTemperature(mProgress)
}

class IntensitySeekBarPreference(context: Context, attrs: AttributeSet) : SeekBarPreference(context, attrs) {

    override val suffix = "%"

    override val max =  100

    override val color: Int
        get() = getIntensityColor(mProgress, Config.color)

    override val progress: Int
        get() = mProgress

    private fun getIntensityColor(intensityLevel: Int, color: Int): Int {
        val argb = Profile.rgbFromColor(color)
        val red   = Color.red  (argb).toFloat()
        val green = Color.green(argb).toFloat()
        val blue  = Color.blue (argb).toFloat()
        val intensity = 1.0f - intensityLevel.toFloat() / 100.0f

        return Color.argb(255,
                          (red +   (255.0f - red  ) * intensity).toInt(),
                          (green + (255.0f - green) * intensity).toInt(),
                          (blue +  (255.0f - blue ) * intensity).toInt())
    }
}

class DimSeekBarPreference(context: Context, attrs: AttributeSet) : SeekBarPreference(context, attrs) {
    override val suffix = "%"

    override val max = 90

    override val color: Int
        get() {
            val lightness = 102 + ((100 - mProgress).toFloat() * (2.55f * 0.6f)).toInt()
            return Color.rgb(lightness, lightness, lightness)
        }

    override val progress: Int
        get() = mProgress
}
