/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.preference

import android.content.Context
import android.content.res.TypedArray
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.helper.Profile
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.service.ScreenFilterService

abstract class SeekBarPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs), SeekBar.OnSeekBarChangeListener {

    companion object : Logger()

    lateinit var mSeekBar: SeekBar
    protected var mProgress: Int = 0
    lateinit protected var mView: View

    open val DEFAULT_VALUE: Int = 20
    abstract val color: Int
    abstract val progress: Int
    abstract val suffix: String

    val colorFilter: PorterDuffColorFilter
        get() = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)

    init {
        layoutResource = R.layout.preference_seekbar
    }

    fun setProgress(progress: Int) { mSeekBar.progress = progress }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInteger(index, DEFAULT_VALUE)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            mProgress = getPersistedInt(DEFAULT_VALUE)
        } else {
            mProgress = (defaultValue as Int?) ?: DEFAULT_VALUE
            persistInt(mProgress)
        }
    }

    override fun onBindView(view: View) {
        Log.i("onBindView")
        super.onBindView(view)
        mView = view
        mSeekBar = view.findViewById(R.id.seekbar) as SeekBar
        setProgress(mProgress)
        mSeekBar.setOnSeekBarChangeListener(this)
        updateView()
    }

    //region OnSeekBarChangedListener
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        Log.i("onSeekbarProgressChanged, $title to $progress")
        mProgress = progress
        persistInt(mProgress)
        updateView()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        Log.d("Touch down on a seek bar")
        ScreenFilterService.preview(true)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        Log.d("Released a seek bar")
        ScreenFilterService.preview(false)
    }
    //end region

    private fun updateView() {
        if (isEnabled) {
            val moonIcon = mView.findViewById(R.id.moon_icon) as ImageView
            moonIcon.colorFilter = colorFilter
        }

        val progressView = mView.findViewById(R.id.seekbar_value) as TextView
        progressView.text = "$progress$suffix"
    }
}

class ColorSeekBarPreference(context: Context, attrs: AttributeSet) : SeekBarPreference(context, attrs) {

    override val suffix = "K"
        
    override val color: Int
        get() = Profile.rgbFromColor(mProgress)

    override val progress: Int
        get() = Profile.getColorTemperature(mProgress)
}

class IntensitySeekBarPreference(context: Context, attrs: AttributeSet) : SeekBarPreference(context, attrs) {

    override val suffix = "%"

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

    override val color: Int
        get() {
            val lightness = 102 + ((100 - mProgress).toFloat() * (2.55f * 0.6f)).toInt()
            return Color.rgb(lightness, lightness, lightness)
        }

    override val progress: Int
        get() = (mProgress.toFloat() * Profile.DIM_MAX_ALPHA).toInt()
}
