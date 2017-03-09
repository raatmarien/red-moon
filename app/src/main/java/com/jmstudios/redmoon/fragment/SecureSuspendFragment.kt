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
import com.jmstudios.redmoon.util.Logger

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
            setMessage(R.string.usage_stats_dialog_message)
            setTitle(R.string.usage_stats_dialog_title)
            setPositiveButton(R.string.ok_dialog) { _, _ ->
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                startActivityForResult(intent, RESULT_USAGE_ACCESS)
            }
        }.show()
    }

    companion object : Logger() {
        const val RESULT_USAGE_ACCESS = 1
    }
}
