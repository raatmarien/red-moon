/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon

import android.os.Bundle
import androidx.preference.SwitchPreference
import com.google.android.material.snackbar.Snackbar
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceFragmentCompat

import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.schedule.*
import com.jmstudios.redmoon.util.*

import org.greenrobot.eventbus.Subscribe

class ScheduleFragment : PreferenceFragmentCompat() {
    private val schedulePref: SwitchPreference
        get() = pref(R.string.pref_key_schedule) as SwitchPreference

    private val automaticTurnOnPref: TimePickerPreference
        get() = pref(R.string.pref_key_start_time) as TimePickerPreference

    private val automaticTurnOffPref: TimePickerPreference
        get() = pref(R.string.pref_key_stop_time) as TimePickerPreference

    private val useLocationPref: SwitchPreference
        get() = pref(R.string.pref_key_use_location) as SwitchPreference

    private var mSnackbar: Snackbar? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreate(savedInstanceState)
        setPreferencesFromResource(R.xml.schedule_preferences, rootKey)
    }

    override fun onStart() {
        super.onStart()
        updatePrefs()
        EventBus.register(this)
        LocationUpdateService.update()
    }

    override fun onStop() {
        EventBus.unregister(this)
        super.onStop()
    }

    private fun updatePrefs() {
        updateSwitchBarTitle()
        updateTimePrefs()
        updateLocationPref()
    }

    private fun updateSwitchBarTitle() {
        schedulePref.setTitle(
                if (Config.scheduleOn) R.string.text_switch_on
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
        val enabled = Config.scheduleOn && !Config.useLocation
        automaticTurnOnPref.isEnabled  = enabled
        automaticTurnOffPref.isEnabled = enabled
        automaticTurnOnPref.summary  = Config.scheduledStartTime
        automaticTurnOffPref.summary = Config.scheduledStopTime
    }

    private fun showSnackbar(resId: Int, duration: Int = Snackbar.LENGTH_INDEFINITE) {
        mSnackbar = Snackbar.make(requireView(), getString(resId), duration).apply {
            if (Config.darkThemeFlag) {
                val group = this.view as ViewGroup
                group.setBackgroundColor(getColor(R.color.snackbar_color_dark_theme))
                group.findViewById<TextView>(R.id.snackbar_text)
                        .setTextColor(getColor(R.color.text_color_dark_theme))
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
    fun onScheduleChanged(event: scheduleChanged) {
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
        if (Config.scheduleOn && Config.useLocation) {
            Permission.Location.request(requireActivity())
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