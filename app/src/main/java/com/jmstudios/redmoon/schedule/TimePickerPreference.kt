/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.schedule

import android.content.Context
import androidx.preference.DialogPreference
import android.util.AttributeSet
import android.util.Log
import android.widget.TimePicker
import com.jmstudios.redmoon.util.atLeastAPI

data class Time(val hour: Int = 0, val minute: Int = 0) {
    companion object {
        fun fromTimePicker(picker: TimePicker): Time {
            return Time(
                if (atLeastAPI(23)) picker.hour else picker.currentHour,
                if (atLeastAPI(23)) picker.minute else picker.currentMinute
            )
        }

        fun fromString(str: String): Time {
            return Time(
                Integer.parseInt(str.substring(0..1)),
                Integer.parseInt(str.substring(3..4))
            )
        }
    }

    override fun toString(): String {
        return String.format("%02d", hour) + ":" + String.format("%02d", minute)
    }
}

open class TimePickerPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {

    private var time: Time? = null

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            time = Time.fromString(getPersistedString(Time().toString()))
        } else {
            time = Time.fromString((defaultValue as String?)?: Time().toString())
            persistString(time.toString())
        }
        summary = time.toString()
    }

    override fun callChangeListener(newValue: Any?): Boolean {
        time = newValue as Time?
        persistString(time.toString())
        return super.callChangeListener(newValue)
    }
}
