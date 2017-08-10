/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.fragment

import android.app.AlertDialog
import android.preference.PreferenceFragment
import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.SwitchPreference
import android.provider.Settings

import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.thread.CurrentAppMonitoringThread
import com.jmstudios.redmoon.util.appContext
import com.jmstudios.redmoon.helper.Logger

class SecureSuspendFragment : PreferenceFragment() {

    private val appMonitoringIsWorking: Boolean
        get() = CurrentAppMonitoringThread.isAppMonitoringWorking(appContext)

    private val mSwitchBarPreference: SwitchPreference
        get() = (preferenceScreen.findPreference
                (getString(R.string.pref_key_secure_suspend)) as SwitchPreference)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate()")
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.secure_suspend_preferences)
        setSwitchBarTitle(mSwitchBarPreference.isChecked)

        mSwitchBarPreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val on = newValue as Boolean
                if (!on) {
                    setSwitchBarTitle(on)
                    true
                } else {
                    if (!appMonitoringIsWorking) createEnableUsageStatsDialog()
                    val b = appMonitoringIsWorking
                    setSwitchBarTitle(b && on)
                    b
                }
            }
    }

    private fun setSwitchBarTitle(on: Boolean) {
        mSwitchBarPreference.setTitle(
                if (on) R.string.text_switch_on
                else R.string.text_switch_off
        )
    }

    // TODO: Fix on API < 21
    private fun createEnableUsageStatsDialog() {
        AlertDialog.Builder(activity).apply {
            setMessage(R.string.dialog_message_permission_usage_stats)
            setTitle(R.string.dialog_title_permission_usage_stats)
            setPositiveButton(R.string.dialog_button_ok) { _, _ ->
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                startActivityForResult(intent, RESULT_USAGE_ACCESS)
            }
        }.show()
    }

    companion object : Logger() {
        const val RESULT_USAGE_ACCESS = 1
    }
}
