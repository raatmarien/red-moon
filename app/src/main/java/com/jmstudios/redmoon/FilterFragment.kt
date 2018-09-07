/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (c) 2015 Chris Nguyen
 *
 *     Permission to use, copy, modify, and/or distribute this software
 *     for any purpose with or without fee is hereby granted, provided
 *     that the above copyright notice and this permission notice appear
 *     in all copies.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *     WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *     WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *     AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 *     CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 *     OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *     NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 *     CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package com.jmstudios.redmoon

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.TwoStatePreference
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.model.Profile
import com.jmstudios.redmoon.preference.SeekBarPreference
import com.jmstudios.redmoon.util.*
import org.greenrobot.eventbus.Subscribe

class FilterFragment : PreferenceFragment() {
    //private var hasShownWarningToast = false
    companion object : Logger()

    // Preferences
    private val profileSelectorPref: Preference
        get() = pref(R.string.pref_key_profile_spinner)

    private val colorPref: SeekBarPreference
        get() = pref(R.string.pref_key_color) as SeekBarPreference

    private val intensityPref: SeekBarPreference
        get() = pref(R.string.pref_key_intensity) as SeekBarPreference

    private val dimLevelPref: SeekBarPreference
        get()= pref(R.string.pref_key_dim) as SeekBarPreference

    private val lowerBrightnessPref: TwoStatePreference
        get() = pref(R.string.pref_key_lower_brightness) as TwoStatePreference

    private val schedulePref: Preference
        get() = pref(R.string.pref_key_schedule_header)

    private val secureSuspendPref: Preference
        get() = pref(R.string.pref_key_secure_suspend_header)

    private val buttonBacklightPref: Preference
        get() = pref(R.string.pref_key_button_backlight)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.filter_preferences)

        updateSecureSuspendSummary()
        updateScheduleSummary()
        updateBacklightPrefSummary()

        if (!Permission.WriteSettings.isGranted) {
            lowerBrightnessPref.isChecked = false
        }

        lowerBrightnessPref.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    val checked = newValue as Boolean
                    if (checked) Permission.WriteSettings.request(activity) else true
                }

        schedulePref.intent = intent(ScheduleActivity::class)
        secureSuspendPref.intent = intent(SecureSuspendActivity::class)
    }

    override fun onStart() {
        Log.i("onStart")
        super.onStart()
        EventBus.register(profileSelectorPref)
        EventBus.register(this)
        updateSecureSuspendSummary()
        updateScheduleSummary()
    }

    override fun onStop() {
        EventBus.unregister(this)
        EventBus.unregister(profileSelectorPref)
        super.onStop()
    }

    private fun updateScheduleSummary() {
        schedulePref.summary = when {
            !Config.scheduleOn -> getString(R.string.pref_summary_schedule_none)
            Config.useLocation -> insertTimes(R.string.pref_summary_schedule_sun)
            else -> insertTimes(R.string.pref_summary_schedule_custom)
        }
    }

    private fun insertTimes(resId: Int): String {
        return getString(resId)
                .replace("%on", Config.scheduledStartTime)
                .replace("%off", Config.scheduledStopTime)
    }

    private fun updateSecureSuspendSummary() {
        secureSuspendPref.setSummary(if (Config.secureSuspend) {
            R.string.text_switch_on
        } else {
            R.string.text_switch_off
        })
    }

    private fun updateBacklightPrefSummary() {
        buttonBacklightPref.setSummary(when(Config.buttonBacklightFlag) {
            "system" -> R.string.pref_entry_button_backlight_system
            "dim"    -> R.string.pref_entry_button_backlight_filter_dim_level
            else     -> R.string.pref_entry_button_backlight_turn_off
        })
    }

    //region presenter
    @Subscribe fun onProfileChanged(profile: Profile) {
        profile.run {
            colorPref.setProgress(color)
            intensityPref.setProgress(intensity)
            dimLevelPref.setProgress(dimLevel)
            lowerBrightnessPref.isChecked = lowerBrightness
        }
    }

    @Subscribe
    fun onButtonBacklightChanged(event: ButtonBacklightChanged) {
        updateBacklightPrefSummary()
    }
    //endregion
}
