/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jmstudios.redmoon.preference

import android.content.Context
import android.content.res.TypedArray
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

import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.service.ScreenFilterService
import com.jmstudios.redmoon.util.Logger
import com.jmstudios.redmoon.view.ScreenFilterView

abstract class SeekBarPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs), SeekBar.OnSeekBarChangeListener {

    companion object : Logger()

    lateinit var mSeekBar: SeekBar
    protected var mProgress: Int = 0
    lateinit protected var mView: View

    // Changes to DEFAULT_VALUE should be reflected in preferences.xml
    abstract val DEFAULT_VALUE: Int
    abstract val colorFilter: PorterDuffColorFilter
    abstract val progress: Int
    abstract val suffix: String

    init {
        layoutResource = R.layout.preference_seekbar
    }

    fun setProgress(progress: Int) {
        mSeekBar.progress = progress
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInteger(index, DEFAULT_VALUE)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            mProgress = getPersistedInt(DEFAULT_VALUE)
        } else {
            mProgress = (defaultValue as Int?) ?: 0
            persistInt(mProgress)
        }
    }

    override fun onBindView(view: View) {
        super.onBindView(view)
        mView = view
        mSeekBar = view.findViewById(R.id.seekbar) as SeekBar
        mSeekBar.progress = mProgress
        mSeekBar.setOnSeekBarChangeListener(this)
        updateMoonIcon()
        updateProgressText()
    }

    //region OnSeekBarChangedListener
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        mProgress = progress
        persistInt(mProgress)
        updateMoonIcon()
        updateProgressText()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        Log.d("Touch down on a seek bar")
        ScreenFilterService.moveToState(ScreenFilterService.Command.SHOW_PREVIEW)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        Log.d("Released a seek bar")
        ScreenFilterService.moveToState(ScreenFilterService.Command.HIDE_PREVIEW)
    }
    //end region

    fun updateMoonIcon() {
        if (isEnabled) {
            val moonIcon = mView.findViewById(R.id.moon_icon) as ImageView
            moonIcon.colorFilter = colorFilter
        }
    }

    fun updateProgressText() {
        val progressView = mView.findViewById(R.id.seekbar_value) as TextView
        progressView.text = String.format("%d%s", progress, suffix)
    }
}

class ColorSeekBarPreference(context: Context, attrs: AttributeSet) : SeekBarPreference(context, attrs) {

    // TODO: Get the default value from the XML and handle it in the parent class
    companion object : Logger() {
        const val DEFAULT_VALUE = 10
    }

    // Changes to DEFAULT_VALUE should be reflected in preferences.xml
    override val DEFAULT_VALUE = 10
    override val suffix = "K"
        
    override val colorFilter: PorterDuffColorFilter
        get() {
            val color = ScreenFilterView.rgbFromColorProgress(mProgress)
            return PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }

    override val progress: Int
        get() = ScreenFilterView.getColorTempFromProgress(mProgress)
}

class IntensitySeekBarPreference(context: Context, attrs: AttributeSet) : SeekBarPreference(context, attrs) {


    // TODO: Get the default value from the XML and handle it in the parent class
    companion object : Logger() {
        const val DEFAULT_VALUE = 50
    }

    // Changes to DEFAULT_VALUE should be reflected in preferences.xml
    override val DEFAULT_VALUE = 50
    override val suffix = "%"

    override val colorFilter: PorterDuffColorFilter
        get() {
            val color = ScreenFilterView.getIntensityColor(mProgress, Config.color)
            return PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }

    override val progress: Int
        get() = mProgress
}


class DimSeekBarPreference(context: Context, attrs: AttributeSet) : SeekBarPreference(context, attrs) {

    // TODO: Get the default value from the XML and handle it in the parent class
    companion object : Logger() {
        const val DEFAULT_VALUE = 50
    }

    // Changes to DEFAULT_VALUE should be reflected in preferences.xml
    override val DEFAULT_VALUE = 50
    override val suffix = "%"

    override val colorFilter: PorterDuffColorFilter
        get() {
            val lightness = 102 + ((100 - mProgress).toFloat() * (2.55f * 0.6f)).toInt()
            val color = Color.rgb(lightness, lightness, lightness)
            return PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }

    override val progress: Int
        get() = (mProgress.toFloat() * ScreenFilterView.DIM_MAX_ALPHA).toInt()
}
