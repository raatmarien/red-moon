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

open class TimePreference(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int,
    defStyleRes: Int
) : DialogPreference(context, attrs, defStyleAttr, defStyleRes)
{
    var showNeutralButton: Boolean
    val neutralButtonText: String

    init {
        val a: TypedArray = context.obtainStyledAttributes(
            attrs, R.styleable.TimePreference, defStyleAttr, defStyleAttr
        )

        showNeutralButton = a.getBoolean(R.styleable.TimePreference_showNeutralButton, false)

        val nbText = a.getString(R.styleable.TimePreference_neutralButtonText)
        neutralButtonText = nbText ?: context.getString(R.string.btn_neutral_default)

        if (a.getBoolean(R.styleable.TimePreference_useSimpleSummary, true)) {
            setSummaryProvider(defaultSummaryProvider)
        }

        a.recycle()
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : this(context, attrs, defStyleAttr, 0)

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : this(context, attrs, R.attr.dialogPreferenceStyle)

    private var timeWasSet: Boolean = false
    var time: Time = Time()
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

    override fun onGetDefaultValue(a: TypedArray, index: Int): String? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val default: String = defaultValue as? String ?: "$time"
        time = Time(getPersistedString(default))
    }

    data class Time(
        val hour: Int = 0,
        val minute: Int = 0
    ) {
        init {
            if (hour !in 0..23 || minute !in 0..59) {
                throw IllegalArgumentException("Hour ($hour) or minute ($minute) out of range")
            }
        }

        // Same semantics as the primary constructor: invalid input throws
        constructor(str: String) : this (
            hour = Integer.parseInt(str.substring(0..1)),
            minute = Integer.parseInt(str.substring(3..4))
        ) {
            if (str[2] != ':') {
                throw IllegalArgumentException("Time string ($str) not formatted as HH:mm")
            }
        }

        fun format(context: Context?): String {
            if (context == null) {
                return this.toString()
            }
            val timestr = Calendar.getInstance().run {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                getTime()
            }
            return DateFormat.getTimeFormat(context).format(timestr)
        }

        override fun toString(): String {
            return String.format("%02d:%02d", hour, minute)
        }
    }

    class SimpleSummaryProvider : SummaryProvider<TimePreference> {
        override fun provideSummary(preference: TimePreference): String {
            return preference.time.format(preference.context)
        }
    }

    companion object {
        val defaultSummaryProvider = SimpleSummaryProvider()
    }
}
