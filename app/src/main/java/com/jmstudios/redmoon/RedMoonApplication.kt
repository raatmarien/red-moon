/*
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */

package com.jmstudios.redmoon

import android.app.Application
import com.jmstudios.redmoon.util.Logger

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.model.Profile
import com.jmstudios.redmoon.model.ProfilesModel
import com.jmstudios.redmoon.schedule.ScheduleReceiver

import org.json.JSONObject

class RedMoonApplication: Application() {

    override fun onCreate() {
        Log.i("onCreate -- Initializing appContext")
        app = this
        super.onCreate()
        upgradeFrom(Config.fromVersionCode)
        //EventBus.builder().addIndex(eventBusIndex()).installDefaultEventBus()
    }

    private tailrec fun upgradeFrom(version: Int): Unit = when (version) {
        BuildConfig.VERSION_CODE -> {
            Config.fromVersionCode = version
        } -1 -> { // fresh install
            ProfilesModel.restoreDefaultProfiles()
            upgradeFrom(BuildConfig.VERSION_CODE)
        } in 0..25 -> {
            upgradeToggleModePreferences()
            upgradeFrom(26)
        } in 26..27 -> {
            upgradeFrom(28)
        } in 28..29 -> {
            upgradeProfilesFrom(version)
            upgradeFrom(30)
        } in 30..33 -> {
            ScheduleReceiver.rescheduleOnCommand()
            ScheduleReceiver.rescheduleOffCommand()
            upgradeFrom(34)
        } 34 -> {
            upgradeFrom(35)
        } else -> {
            Log.e("Didn't catch upgrades from version $version")
            upgradeFrom(version+1)
        }
    }

    private fun upgradeProfilesFrom(version: Int) {
        Log.i("upgradeProfilesFrom($version)")
        val PREFERENCE_NAME = "com.jmstudios.redmoon.PROFILES_PREFERENCE"
        val MODE = Context.MODE_PRIVATE
        val prefs: SharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE)

        val profiles = if (version in 0..28) {
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

        prefs.edit().apply {
            clear()
            profiles.forEach { (profile, name) ->
                Log.i("Storing profile $profile as $name")
                putString(profile.toString(), name)
            }
        }.apply()
        ProfilesModel.restoreDefaultProfiles()
    }

    private fun upgradeToggleModePreferences() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val scheduleKey = getString(R.string.pref_key_schedule)
        val currentToggleMode: String = sharedPrefs.getString(scheduleKey, "manual")
        sharedPrefs.edit().remove(scheduleKey).apply()
        Config.scheduleOn = currentToggleMode != "manual"
        Config.useLocation = currentToggleMode == "sun"
    }

    companion object : Logger() {
        lateinit var app: RedMoonApplication
            private set
    }
}
