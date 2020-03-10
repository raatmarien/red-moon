/**
 * Copyright (c) 2020  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Based in part on Android's PreferenceDialogFragmentCompat
 * Copyright 2018 The Android Open Source Project
 * Used under the Apache License, Version 2.0
 */
package org.libreshift.preferences

import androidx.annotation.RestrictTo.Scope.LIBRARY

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.DialogPreference
import androidx.preference.DialogPreference.TargetFragment
import androidx.preference.PreferenceFragmentCompat


// TimePickerDialog(context, { _, h, m ->
//     preference.callChangeListener(Time(h, m))
// }, 0, 0, false).show()

open class TimePreferenceDialogFragmentCompat() : DialogFragment(), DialogInterface.OnClickListener {
    /**
     * The preference that requested this dialog. Available after [.onCreate] has
     * been called on the [PreferenceFragmentCompat] which launched this dialog.
     *
     * @return The [TimePreference] associated with this dialog
     */
    var mPreference: TimePreference? = null
        get() {
            if (field == null) {
                val key = arguments!!.getString(ARG_KEY)!!
                val fragment: TargetFragment? = targetFragment as? TargetFragment
                field = fragment?.findPreference(key)
            }
            return field
        }
        private set

    private var mDialogTitle: CharSequence? = null
    private var mPositiveButtonText: CharSequence? = null
    private var mNegativeButtonText: CharSequence? = null
    private var mDialogMessage: CharSequence? = null

    @LayoutRes
    private var mDialogLayoutRes = 0
    private var mDialogIcon: BitmapDrawable? = null

    /** Which button was clicked.  */
    private var mWhichButtonClicked = 0
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
            val key = arguments!!.getString(ARG_KEY)!!
            mPreference = fragment.findPreference(key)
            mDialogTitle = mPreference?.dialogTitle
            mPositiveButtonText = mPreference?.positiveButtonText
            mNegativeButtonText = mPreference?.negativeButtonText
            mDialogMessage = mPreference?.dialogMessage
            mDialogLayoutRes = mPreference?.dialogLayoutResource ?: mDialogLayoutRes
            val icon: Drawable? = mPreference?.dialogIcon
            mDialogIcon = if (icon == null || icon is BitmapDrawable) {
                icon as BitmapDrawable?
            } else {
                val bitmap = Bitmap.createBitmap(icon.intrinsicWidth,
                        icon.intrinsicHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                icon.setBounds(0, 0, canvas.width, canvas.height)
                icon.draw(canvas)
                BitmapDrawable(resources, bitmap)
            }
        } else {
            mDialogTitle = savedInstanceState.getCharSequence(SAVE_STATE_TITLE)
            mPositiveButtonText = savedInstanceState.getCharSequence(SAVE_STATE_POSITIVE_TEXT)
            mNegativeButtonText = savedInstanceState.getCharSequence(SAVE_STATE_NEGATIVE_TEXT)
            mDialogMessage = savedInstanceState.getCharSequence(SAVE_STATE_MESSAGE)
            mDialogLayoutRes = savedInstanceState.getInt(SAVE_STATE_LAYOUT, 0)
            val bitmap = savedInstanceState.getParcelable<Bitmap>(SAVE_STATE_ICON)
            if (bitmap != null) {
                mDialogIcon = BitmapDrawable(resources, bitmap)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(SAVE_STATE_TITLE, mDialogTitle)
        outState.putCharSequence(SAVE_STATE_POSITIVE_TEXT, mPositiveButtonText)
        outState.putCharSequence(SAVE_STATE_NEGATIVE_TEXT, mNegativeButtonText)
        outState.putCharSequence(SAVE_STATE_MESSAGE, mDialogMessage)
        outState.putInt(SAVE_STATE_LAYOUT, mDialogLayoutRes)
        if (mDialogIcon != null) {
            outState.putParcelable(SAVE_STATE_ICON, mDialogIcon!!.bitmap)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context: Context = activity!!
        mWhichButtonClicked = DialogInterface.BUTTON_NEGATIVE
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                .setTitle(mDialogTitle)
                .setIcon(mDialogIcon)
                .setPositiveButton(mPositiveButtonText, this)
                .setNegativeButton(mNegativeButtonText, this)
        val contentView: View? = onCreateDialogView(context)
        if (contentView != null) {
            onBindDialogView(contentView)
            builder.setView(contentView)
        } else {
            builder.setMessage(mDialogMessage)
        }
        onPrepareDialogBuilder(builder)
        // Create the dialog
        val dialog: Dialog = builder.create()
        return dialog
    }


    /**
     * Prepares the dialog builder to be shown when the preference is clicked.
     * Use this to set custom properties on the dialog.
     *
     *
     * Do not [AlertDialog.Builder.create] or [AlertDialog.Builder.show].
     */
    protected fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {}

    /**
     * Creates the content view for the dialog (if a custom content view is required).
     * By default, it inflates the dialog layout resource if it is set.
     *
     * @return The content view for the dialog
     * @see DialogPreference.setLayoutResource
     */
    protected fun onCreateDialogView(context: Context?): View? {
        val resId = mDialogLayoutRes
        if (resId == 0) {
            return null
        }
        val inflater = LayoutInflater.from(context)
        return inflater.inflate(resId, null)
    }

    /**
     * Binds views in the content view of the dialog to data.
     *
     *
     * Make sure to call through to the superclass implementation.
     *
     * @param view The content view of the dialog, if it is custom
     */
    protected fun onBindDialogView(view: View) {
        val dialogMessageView: View? = view.findViewById(R.id.message)
        if (dialogMessageView != null) {
            val message = mDialogMessage
            var newVisibility: Int = View.GONE
            if (!TextUtils.isEmpty(message)) {
                if (dialogMessageView is TextView) {
                    (dialogMessageView as TextView).text = message
                }
                newVisibility = View.VISIBLE
            }
            if (dialogMessageView.visibility !== newVisibility) {
                dialogMessageView.visibility = newVisibility
            }
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        mWhichButtonClicked = which
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDialogClosed(mWhichButtonClicked == DialogInterface.BUTTON_POSITIVE)
    }

    protected fun onDialogClosed(positiveResult: Boolean) {}

    companion object {
        // For persisting state
        protected const val ARG_KEY = "key"
        private const val SAVE_STATE_TITLE = "TimePreferenceDialogFragment.title"
        private const val SAVE_STATE_POSITIVE_TEXT = "TimePreferenceDialogFragment.positiveText"
        private const val SAVE_STATE_NEGATIVE_TEXT = "TimePreferenceDialogFragment.negativeText"
        private const val SAVE_STATE_MESSAGE = "TimePreferenceDialogFragment.message"
        private const val SAVE_STATE_LAYOUT = "TimePreferenceDialogFragment.layout"
        private const val SAVE_STATE_ICON = "TimePreferenceDialogFragment.icon"

        fun newInstance(key: String): TimePreferenceDialogFragmentCompat {
            return TimePreferenceDialogFragmentCompat().apply {
                arguments = Bundle(1).apply { putString(ARG_KEY, key) }
            }
        }
    }
}
