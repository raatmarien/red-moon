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
package com.jmstudios.redmoon.model

import com.jmstudios.redmoon.BuildConfig
import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.helper.Profile
import com.jmstudios.redmoon.helper.EventBus
import com.jmstudios.redmoon.helper.KLogging.logger
import com.jmstudios.redmoon.receiver.SwitchAppWidgetProvider
import com.jmstudios.redmoon.receiver.TimeToggleChangeReceiver
import com.jmstudios.redmoon.util.*

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator

import java.util.Calendar
import java.util.TimeZone

import me.smichel.android.KPreferences.Preferences

private const val BROADCAST_ACTION = "com.jmstudios.redmoon.RED_MOON_TOGGLED"
private const val BROADCAST_FIELD  = "jmstudios.bundle.key.FILTER_IS_ON"

/**
 * This singleton provides allows easy access to the shared preferences
 */
object Config : Preferences(appContext) {
    val Log = logger("Config")

    //region preferences
    var filterIsOn by BooleanPreference(R.string.pref_key_filter_is_on, false) {
        Log.i("Sending update broadcasts")
        //Broadcast to keep appwidgets in sync
        context.sendBroadcast(intent(SwitchAppWidgetProvider::class).apply {
            action = SwitchAppWidgetProvider.ACTION_UPDATE
            putExtra(SwitchAppWidgetProvider.EXTRA_POWER, it)
        })

        // If an app like Tasker wants to do something each time
        // Red Moon is toggled, it can listen for this event
        context.sendBroadcast(intent().apply {
            action = BROADCAST_ACTION
            putExtra(BROADCAST_FIELD, it)
        })
        EventBus.post(filterIsOnChanged())
    }
    
    var color by IntPreference(R.string.pref_key_color, 10) {
        activeProfile.run { if (it != color) activateProfile(copy(color = it)) }
    }

    var intensity by IntPreference(R.string.pref_key_intensity, 30) {
        activeProfile.run { if (it != intensity) activateProfile(copy(intensity = it)) }
    }

    var dimLevel by IntPreference(R.string.pref_key_dim, 40) {
        activeProfile.run { if (it != dimLevel) activateProfile(copy(dimLevel = it)) }
    }

    var lowerBrightness by BooleanPreference(R.string.pref_key_lower_brightness, false) {
        activeProfile.run { if (it != lowerBrightness) activateProfile(copy(lowerBrightness = it)) }
    }

    private fun activateProfile(profile: Profile) {
        Log.i("Activating profile: $profile")
        custom = profile
        activeProfile = profile
    }

    private var _custom by StringOrNullPreference(R.string.pref_key_custom_profile)
    var custom: Profile
        get() = _custom?.let { Profile.parse(it) } ?: activeProfile
        set(value) {
            Log.i("custom set to $value")
            _custom = value.toString()
        }

    val secureSuspend by BooleanPreference(R.string.pref_key_secure_suspend, false) {
        EventBus.post(secureSuspendChanged())
    }

    val buttonBacklightFlag by StringPreference(R.string.pref_key_button_backlight, "off") {
        EventBus.post(buttonBacklightChanged())
    }
    
    var darkThemeFlag by BooleanPreference(R.string.pref_key_dark_theme, false)

    var timeToggle by BooleanPreference(R.string.pref_key_time_toggle, true) {
        if (it) {
            Log.i("Timer turned on")
            TimeToggleChangeReceiver.rescheduleOnCommand()
            TimeToggleChangeReceiver.rescheduleOffCommand()
        } else {
            Log.i("Timer turned on")
            TimeToggleChangeReceiver.cancelAlarms()
        }
        EventBus.post(timeToggleChanged())
    }

    val customTurnOnTime by StringPreference(R.string.pref_key_custom_turn_on_time, "22:00") {
        TimeToggleChangeReceiver.rescheduleOnCommand()
        EventBus.post(customTurnOnTimeChanged())
    }

    val customTurnOffTime by StringPreference(R.string.pref_key_custom_turn_off_time, "06:00") {
        TimeToggleChangeReceiver.rescheduleOffCommand()
        EventBus.post(customTurnOffTimeChanged())
    }

    var useLocation by BooleanPreference(R.string.pref_key_use_location, false) {
        EventBus.post(useLocationChanged())
    }
    //endregion

    //region state
    val activeTheme: Int
        get() = if (darkThemeFlag) R.style.AppThemeDark else R.style.AppTheme

    val buttonBacklightLevel: Float
        get() = when (buttonBacklightFlag) {
                    "system" -> -1.toFloat()
                    "dim" -> 1 - (dimLevel.toFloat() / 100)
                    else -> 0.toFloat()
                }

    val automaticTurnOnTime: String
        get() = if (useLocation) sunsetTime else customTurnOnTime

    val automaticTurnOffTime: String
        get() = if (useLocation) sunriseTime else customTurnOffTime
    
    private var _location by StringPreference(R.string.pref_key_location, "0,0") {
        TimeToggleChangeReceiver.rescheduleOffCommand()
        TimeToggleChangeReceiver.rescheduleOnCommand()
        EventBus.post(locationChanged())
    }

    const val NOT_SET: Long = -1
    private var _locationTimestamp by LongPreference(R.string.pref_key_location_timestamp, NOT_SET)

    var location: Triple<String, String, Long?>
        get() = with (_location) {
            val latitude  = substringBefore(',')
            val longitude = substringAfter(',')
            val timestamp = _locationTimestamp.let { if (it == NOT_SET) null else it }
            return Triple(latitude, longitude, timestamp)
        }
        set(l) {
            _locationTimestamp = l.third ?: NOT_SET
            _location = l.first + "," + l.second
        }

    const val DEFAULT_SUNSET = "19:30"
    val sunsetTime: String
        get() {
            val (latitude, longitude, time) = location
            return if (time == null) {
                DEFAULT_SUNSET
            } else {
                val sunLocation = com.luckycatlabs.sunrisesunset.dto.Location(latitude, longitude)
                val calculator  = SunriseSunsetCalculator(sunLocation, TimeZone.getDefault())
                calculator.getOfficialSunsetForDate(Calendar.getInstance())
            }
        }

    const val DEFAULT_SUNRISE = "06:30"
    val sunriseTime: String
        get() {
            val (latitude, longitude, time) = location
            return if (time == null) {
                DEFAULT_SUNRISE
            } else {
                val sunLocation = com.luckycatlabs.sunrisesunset.dto.Location(latitude, longitude)
                val calculator  = SunriseSunsetCalculator(sunLocation, TimeZone.getDefault())
                calculator.getOfficialSunriseForDate(Calendar.getInstance())
            }
        }

    var introShown by BooleanPreference(R.string.pref_key_intro_shown, false)

    var brightness by IntPreference(R.string.pref_key_brightness, 0)
    
    var automaticBrightness by BooleanPreference(R.string.pref_key_automatic_brightness, true)

    var brightnessLowered by BooleanPreference(R.string.pref_key_brightness_lowered, false)
    //endregion

    //region application
    var fromVersionCode by IntPreference(R.string.pref_key_from_version_code, -1)
    //endregion
}
