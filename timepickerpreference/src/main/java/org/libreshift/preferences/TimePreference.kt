/**
 * Copyright (c) 2020  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.libreshift.preferences

import android.content.Context
import android.content.res.TypedArray
import android.text.format.DateFormat
import android.util.AttributeSet
import androidx.preference.DialogPreference

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

open class TimePreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs)
{
    init {
        // TODO: Get xml preferences and respond to them
        // TODO: Make this configurable
        setSummaryProvider(defaultSummaryProvider)
    }

    private var timeWasSet: Boolean = false
    var time: Time? = null
        set(value) {
            val changed = field != value
            if (changed || !timeWasSet) {
                timeWasSet = true
                persistString(value.toString())
                field = value
            }
            if (changed) {
                notifyChanged()
            }
        }

    val minute: Int get() = time?.minute ?: 0
    val hour: Int get() = time?.hour ?: 0

    override fun onGetDefaultValue(a: TypedArray, index: Int): String? {
        return a.getString(index)
        // TODO: Experiment with returning a Time? here
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val default: String = defaultValue as? String ?: Time().toString()
        time = Time(getPersistedString(default))
    }

    data class Time(val hour: Int = 0, val minute: Int = 0) {

        init {
            if (hour !in 0..23 || minute !in 0..59) {
                // TODO: Throw a more specific exception
                throw Exception("Invalid hour ($hour) or minute($minute)")
            }
        }

        constructor(str: String) : this (
            hour = Integer.parseInt(str.substring(0..1)),
            minute = Integer.parseInt(str.substring(3..4))
        )

        override fun toString(): String {
            return String.format("%02d:%02d", hour, minute)
        }
    }

    class SimpleSummaryProvider : SummaryProvider<TimePreference> {
        override fun provideSummary(preference: TimePreference): String {
            val time = Calendar.getInstance().run {
                set(Calendar.HOUR_OF_DAY, preference.hour)
                set(Calendar.MINUTE, preference.minute)
                getTime()
            }
            val formatter = DateFormat.getTimeFormat(preference.context)
            return formatter.format(time)
        }
    }

    companion object {
        val defaultSummaryProvider = SimpleSummaryProvider()
    }
}
