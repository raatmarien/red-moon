/**
 * Copyright (c) 2020  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.libreshift.preferences

import android.app.TimePickerDialog
import android.content.Context
import android.content.res.TypedArray
import android.text.format.DateFormat
import android.util.AttributeSet
import androidx.preference.DialogPreference

import java.util.Calendar

open class TimePreference(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int,
    defStyleRes: Int
) : DialogPreference(context, attrs, defStyleAttr, defStyleRes)
{
    interface OnNeutralButtonPressListener {
        fun onNeutralButtonPress(dialog: TimePickerDialog)

        // Hack around lack of SAM for kotlin interfaces, see link:
        // https://youtrack.jetbrains.com/issue/KT-7770#focus=streamItem-27-3290802.0-0
        companion object {
            inline operator fun invoke(crossinline op: (dialog: TimePickerDialog) -> Unit) =
                object : OnNeutralButtonPressListener {
                    override fun onNeutralButtonPress(dialog: TimePickerDialog) = op(dialog)
                }

        }
    }

    val is24HourView: Boolean
    var showNeutralButton: Boolean
    val neutralButtonText: String

    var neutralButtonListener: OnNeutralButtonPressListener? = null

    init {
        val a: TypedArray = context.obtainStyledAttributes(
            attrs, R.styleable.TimePreference, defStyleAttr, defStyleAttr
        )

        val default24H = DateFormat.is24HourFormat(context)
        is24HourView = a.getBoolean(R.styleable.TimePreference_is24HourView, default24H)

        showNeutralButton = a.getBoolean(R.styleable.TimePreference_showNeutralButton, false)

        val nbText = a.getString(R.styleable.TimePreference_neutralButtonText)
        neutralButtonText = nbText ?: context.getString(R.string.btn_neutral_default)

        if (a.getBoolean(R.styleable.TimePreference_useSimpleSummary, true)) {
            summaryProvider = defaultSummaryProvider
        }

        neutralButtonListener = OnNeutralButtonPressListener { dialog ->
            dialog.updateTime(time.hour, time.minute)
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

    /** To the Android system, we represent time as a string */
    override fun onGetDefaultValue(a: TypedArray, index: Int): String? {
        return a.getString(index)
    }

    /** Internally, though, we store the time as a Time */
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
                // Can't format to local time, so just use the raw HH:mm
                return this.toString()
            }
            val calendar = Calendar.getInstance().also {
                it.set(Calendar.HOUR_OF_DAY, hour)
                it.set(Calendar.MINUTE, minute)
            }
            return DateFormat.getTimeFormat(context).format(calendar.time)
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
