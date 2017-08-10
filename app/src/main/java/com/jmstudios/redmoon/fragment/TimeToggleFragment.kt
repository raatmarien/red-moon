/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.fragment

import android.os.Bundle
import android.preference.SwitchPreference
import android.support.design.widget.Snackbar
import android.view.ViewGroup
import android.widget.TextView

import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.helper.Permission
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.preference.TimePickerPreference
import com.jmstudios.redmoon.service.LocationUpdateService
import com.jmstudios.redmoon.util.*

import org.greenrobot.eventbus.Subscribe


class TimeToggleFragment : EventPreferenceFragment() {

    // Preferences
    private val timeTogglePref: SwitchPreference
        get() = (preferenceScreen.findPreference
                (getString(R.string.pref_key_time_toggle)) as SwitchPreference)

    private val automaticTurnOnPref: TimePickerPreference
        get() = (preferenceScreen.findPreference
                (getString(R.string.pref_key_custom_turn_on_time)) as TimePickerPreference)

    private val automaticTurnOffPref: TimePickerPreference
        get() = (preferenceScreen.findPreference
                (getString(R.string.pref_key_custom_turn_off_time)) as TimePickerPreference)

    private val useLocationPref: SwitchPreference
        get() = (preferenceScreen.findPreference
                (getString(R.string.pref_key_use_location)) as SwitchPreference)

    private var mSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.time_toggle_preferences)
    }

    override fun onStart() {
        super.onStart()
        LocationUpdateService.update()
    }

    override fun onResume() {
        updatePrefs()
        super.onResume()
    }

    private fun updatePrefs() {
        updateSwitchBarTitle()
        updateTimePrefs()
        updateLocationPref()
    }

    private fun updateSwitchBarTitle() {
        timeTogglePref.setTitle(
                if (Config.timeToggle) R.string.text_switch_on
                else R.string.text_switch_off
        )
    }

    private fun updateLocationPref() {
        val (latitude, longitude, time) = Config.location
        useLocationPref.summaryOn = if (time == null) {
            getString(R.string.location_not_set)
        } else {
            val lat  = getString(R.string.latitude_short)
            val long = getString(R.string.longitude_short)

            "$lat: ${latitude.round()}, $long: ${longitude.round()}"
        }
    }

    private fun String.round(digitsAfterDecimal: Int = 3): String {
        val digits = this.indexOf(".") + digitsAfterDecimal
        return this.padEnd(digits+1).substring(0..digits).trimEnd()
    }

    private fun updateTimePrefs() {
        val enabled = Config.timeToggle && !Config.useLocation
        automaticTurnOnPref.isEnabled  = enabled
        automaticTurnOffPref.isEnabled = enabled
        automaticTurnOnPref.summary  = Config.automaticTurnOnTime
        automaticTurnOffPref.summary = Config.automaticTurnOffTime
    }

    private fun showSnackbar(resId: Int, duration: Int = Snackbar.LENGTH_INDEFINITE) {
        mSnackbar = Snackbar.make(view, getString(resId), duration).apply {
            if (Config.darkThemeFlag) {
                val group = this.view as ViewGroup
                group.setBackgroundColor(getColor(R.color.snackbar_color_dark_theme))

                val snackbarTextId = android.support.design.R.id.snackbar_text
                val textView = group.findViewById(snackbarTextId) as TextView
                textView.setTextColor(getColor(R.color.text_color_dark_theme))
            }
        }
        mSnackbar?.show()
    }

    private fun dismissSnackBar() {
        if (mSnackbar?.duration == Snackbar.LENGTH_INDEFINITE) {
            mSnackbar?.dismiss()
        }
    }

    //region presenter
    @Subscribe
    fun onTimeToggleChanged(event: timeToggleChanged) {
        LocationUpdateService.update()
        updatePrefs()
    }

    @Subscribe
    fun onUseLocationChanged(event: useLocationChanged) {
        LocationUpdateService.update()
        updateTimePrefs()
    }

    @Subscribe
    fun onLocationServiceEvent(service: locationService) {
        Log.i("onLocationEvent: ${service.isSearching}")
        if (!service.isRunning) {
            dismissSnackBar()
        } else if (service.isSearching) {
            showSnackbar(R.string.snackbar_searching_location)
        } else {
            showSnackbar(R.string.snackbar_warning_no_location)
        }
    }

    @Subscribe fun onLocationChanged(event: locationChanged) {
        showSnackbar(R.string.snackbar_location_updated, Snackbar.LENGTH_SHORT)
        updatePrefs()
    }

    @Subscribe
    fun onLocationAccessDenied(event: locationAccessDenied) {
        if (Config.timeToggle && Config.useLocation) {
            Permission.Location.request(activity)
        }
    }

    @Subscribe
    fun onLocationPermissionDialogClosed(event: Permission.Location) {
        if (!Permission.Location.isGranted) {
            useLocationPref.isChecked = false
        }
        LocationUpdateService.update()
    }
    //endregion

    companion object : Logger()
}
