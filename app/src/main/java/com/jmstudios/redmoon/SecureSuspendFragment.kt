/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.provider.Settings
import com.jmstudios.redmoon.securesuspend.CurrentAppChecker
import com.jmstudios.redmoon.util.Logger
import com.jmstudios.redmoon.util.appContext
import com.jmstudios.redmoon.util.pref

class SecureSuspendFragment : PreferenceFragment() {

    private val mSwitchBarPreference: SwitchPreference
        get() = pref(R.string.pref_key_secure_suspend) as SwitchPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate()")
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.secure_suspend_preferences)
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
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                } else {
                    TODO("VERSION.SDK_INT < LOLLIPOP")
                }
                startActivityForResult(intent, RESULT_USAGE_ACCESS)
            }
        }.show()
    }

    companion object : Logger() {
        const val RESULT_USAGE_ACCESS = 1
    }
}
