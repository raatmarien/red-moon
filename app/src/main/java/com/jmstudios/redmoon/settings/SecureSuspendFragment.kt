/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.jmstudios.redmoon.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import androidx.preference.PreferenceFragmentCompat

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.securesuspend.CurrentAppChecker
import com.jmstudios.redmoon.util.*

class SecureSuspendFragment : PreferenceFragmentCompat() {

    private val mSwitchBarPreference: SwitchPreference
        get() = pref(R.string.pref_key_secure_suspend) as SwitchPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.secure_suspend_preferences, rootKey)
        setSwitchBarTitle(mSwitchBarPreference.isChecked)
        mSwitchBarPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val on = newValue as Boolean
                // TODO: Make this readable
                if (!on) {
                    setSwitchBarTitle(on)
                    true
                } else {
                    val appChecker = CurrentAppChecker(appContext)
                    if (!appChecker.isWorking) createEnableUsageStatsDialog()
                    val working = appChecker.isWorking
                    setSwitchBarTitle(working && on)
                    working
                }
            }
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.activity_secure_suspend)
    }

    private fun setSwitchBarTitle(on: Boolean) {
        mSwitchBarPreference.setTitle(
                if (on) R.string.text_switch_on
                else R.string.text_switch_off
        )
    }

    private fun createEnableUsageStatsDialog() {
        if (atLeastAPI(21)) {
            AlertDialog.Builder(activity).apply {
                setMessage(R.string.dialog_message_permission_usage_stats)
                setTitle(R.string.dialog_title_permission_usage_stats)
                setPositiveButton(R.string.dialog_button_ok) { _, _ ->
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    startActivityForResult(intent, RESULT_USAGE_ACCESS)
                }
            }.show()
        } else {
            Log.e("Usage stats unavailable on API<21, this should not happen")
        }
    }

    companion object : Logger() {
        const val RESULT_USAGE_ACCESS = 1
    }
}
