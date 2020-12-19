/**
 * Copyright (c) 2020  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Based in part on Android's PreferenceDialogFragmentCompat, which is
 * Copyright 2018 The Android Open Source Project
 * Used under the Apache License, Version 2.0
 */
package org.libreshift.preferences

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.preference.DialogPreference.TargetFragment
import androidx.preference.PreferenceFragmentCompat


open class TimePreferenceDialogFragmentCompat : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    /**
     * The preference that requested this dialog. Available after [.onCreate] has
     * been called on the [PreferenceFragmentCompat] which launched this dialog.
     */
    var preference: TimePreference? = null
        get() {
            if (field == null) {
                val key = requireArguments().getString(ARG_KEY)!!
                val fragment: TargetFragment? = targetFragment as? TargetFragment
                field = fragment?.findPreference(key)
            }
            return field
        }
        private set

    private var neutralButtonText: CharSequence? = null
    private var showNeutralButton: Boolean = false
    private var is24HourView: Boolean = false
    private var initialHour: Int = 0
    private var initialMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragment: TargetFragment
        targetFragment.let {
            check(it is TargetFragment) {
                "Target fragment must implement TargetFragment interface"
            }
            fragment = it
        }
        if (savedInstanceState == null) {
            val key = requireArguments().getString(ARG_KEY)!!
            preference = fragment.findPreference(key)
            neutralButtonText = preference?.neutralButtonText
            showNeutralButton = preference?.showNeutralButton ?: showNeutralButton
            is24HourView = preference?.is24HourView ?: DateFormat.is24HourFormat(context)
            initialHour = preference?.time?.hour ?: initialHour
            initialMinute = preference?.time?.minute ?: initialMinute
        } else {
            neutralButtonText = savedInstanceState.getCharSequence(SAVE_STATE_NEUTRAL_TEXT)
            showNeutralButton = savedInstanceState.getBoolean(SAVE_STATE_SHOW_NEUTRAL)
            is24HourView = savedInstanceState.getBoolean(SAVE_STATE_24_HOUR_VIEW)
            initialHour = savedInstanceState.getInt(SAVE_STATE_INITIAL_HOUR)
            initialMinute = savedInstanceState.getInt(SAVE_STATE_INITIAL_MINUTE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(SAVE_STATE_NEUTRAL_TEXT, neutralButtonText)
        outState.putBoolean(SAVE_STATE_SHOW_NEUTRAL, showNeutralButton)
        outState.putBoolean(SAVE_STATE_24_HOUR_VIEW, is24HourView)
        outState.putInt(SAVE_STATE_INITIAL_HOUR, initialHour)
        outState.putInt(SAVE_STATE_INITIAL_MINUTE, initialMinute)
    }

    /**
     * We'd have more control if we instantiated the dialog and populated the
     * view directly, but putting a time picker inside a dialog is a black art.
     * TimePickerDialog needs to subclass AlertDialog and use package-private
     * methods to do it (specifically for layout in landscape orientation).
     *
     * So we'll just call TimePickerDialog and hack its output. This is also
     * why we subclass DialogFragment instead of PreferenceDialogFragmentCompat.
     * This means we only have access to the dialog, not its builder, but the
     * only *real* downside is that we can't access the dialog view (ie, the
     * time picker) directly, to offer onBindDialogView().
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context: Context = requireActivity()

        val dialog = TimePickerDialog(context, this, initialHour, initialMinute, is24HourView)

        if (showNeutralButton) {
            // I would pass null for the last argument, but kotlin's type checker yells at me
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, neutralButtonText) { _, _ -> }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.create()
            // If we had a higher minApi, we could attach the listener here
            // Instead, we need to wait until dialog.show() has been called
        }
        return dialog
    }

    /**
     * DialogFragment.onStart() is where dialog.show() is called, which means
     * the view has been instantiated so now we can get a reference to the
     * neutral button and override the onClick listener, to prevent the dialog
     * from closing if we don't want it to.
     *
     * This is a fragile implementation. It would be better to create our own
     * TimePickerDialog subclass and override its show() and/or onClick methods.
     * This would also allow us to persist values in the neutral button case.
     * See: https://android.googlesource.com/platform/frameworks/base/+/master
     *                          /core/java/android/app/TimePickerDialog.java#149
     *
     * Maybe later, I've already spent a lot of time on this.
     */
    override fun onStart() {
        super.onStart()
        val tpd = (dialog as TimePickerDialog)
        tpd.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
            preference?.neutralButtonListener?.onNeutralButtonPress(tpd)
        }
    }

    /** Called when the user presses OK, after the dialog has closed. */
    override fun onTimeSet(picker: TimePicker?, hour: Int, minute: Int) {
        Log.i(TAG, "onTimeSet: $hour:$minute")
        preference?.let {
            val shouldSave = it.callChangeListener(TimePreference.Time(hour, minute))
            if (shouldSave) {
                it.time = TimePreference.Time(hour, minute)
            }
        }
    }

    /** Called when dialog closes, regardless of which button was pressed */
    // override fun onDismiss(dialog: DialogInterface) { }

    companion object {
        private const val TAG = "TimePrefDialogFragment"
        // For persisting state
        private const val ARG_KEY = "key"
        private const val SAVE_STATE_NEUTRAL_TEXT = "TimePreferenceDialogFragment.neutralText"
        private const val SAVE_STATE_SHOW_NEUTRAL = "TimePreferenceDialogFragment.showNeutral"
        private const val SAVE_STATE_24_HOUR_VIEW = "TimePreferenceDialogFragment.is24HourView"
        private const val SAVE_STATE_INITIAL_HOUR = "TimePreferenceDialogFragment.initialHour"
        private const val SAVE_STATE_INITIAL_MINUTE = "TimePreferenceDialogFragment.initialMinute"

        fun newInstance(key: String): TimePreferenceDialogFragmentCompat {
            return TimePreferenceDialogFragmentCompat().apply {
                arguments = Bundle(1).apply { putString(ARG_KEY, key) }
            }
        }
    }
}
