/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017 Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.helper

import android.content.Context
import android.preference.PreferenceManager

import com.jmstudios.redmoon.BuildConfig
import com.jmstudios.redmoon.helper.KLogging
import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.model.profiles.restoreDefaultProfiles
import com.jmstudios.redmoon.util.appContext

import org.json.JSONObject


fun upgrade() {
    val Log = KLogging.logger("Upgrade", true)

    fun upgradeProfiles(fromVersion: Int) {
        Log.i("Upgrading profiles from $fromVersion")
        val PREFERENCE_NAME = "com.jmstudios.redmoon.PROFILES_PREFERENCE"
        val MODE = Context.MODE_PRIVATE
        val prefs  = appContext.getSharedPreferences(PREFERENCE_NAME, MODE)

        val profiles = if (fromVersion in 0..28) {
            prefs.all.map { (key, values) ->
                val v = values as String

                val name      = key.substringBefore('_')
                val color     = v.substringBefore(',').toInt()
                val intensity = v.substringAfter(',').substringBefore(',').toInt()
                val dimLevel  = v.substringAfterLast(',').toInt()
                val profile = Profile(color, intensity, dimLevel, false)

                Pair(profile, name)
            }
        } else prefs.all.map { (_, value) ->
            JSONObject(value as String).run {
                val name      = optString("name")
                val color     = optInt("color")
                val intensity = optInt("intensity")
                val dimLevel  = optInt("dim")
                val lowerBrightness = optBoolean("lower-brightness")
                val profile = Profile(color, intensity, dimLevel, lowerBrightness)

                Pair(profile, name)
            }
        }

        prefs.edit().run {
            clear()
            profiles.forEach { (profile, name) ->
                Log.i("Storing profile $profile as $name")
                putString(profile.toString(), name)
            }
            apply()
        }
        restoreDefaultProfiles()
    }

    fun upgradeToggleModePreferences() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        val timerKey = appContext.getString(R.string.pref_key_time_toggle)
        val currentToggleMode: String = sharedPrefs.getString(timerKey, "manual")
        sharedPrefs.edit().remove(timerKey).apply()
        Config.timeToggle  = currentToggleMode != "manual"
        Config.useLocation = currentToggleMode == "sun"
    }

    tailrec fun upgradeFrom(version: Int): Unit = when (version) {
        BuildConfig.VERSION_CODE -> {
            Config.fromVersionCode = version
        } -1 -> { // fresh install
            restoreDefaultProfiles()
            upgradeFrom(BuildConfig.VERSION_CODE)
        } in 0..25 -> {
            upgradeToggleModePreferences()
            upgradeFrom(26)
        } in 26..27 -> {
            upgradeFrom(28)
        } in 28..29 -> {
            upgradeProfiles(version)
            upgradeFrom(30)
        } else -> {
            Log.e("Didn't catch upgrades from version $version")
            upgradeFrom(version+1)
        }
    }

    upgradeFrom(Config.fromVersionCode)
}
