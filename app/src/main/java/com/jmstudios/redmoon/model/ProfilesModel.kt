/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017 Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.jmstudios.redmoon.model

import android.content.Context
import android.content.SharedPreferences

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.util.*

private const val PREFERENCE_NAME = "com.jmstudios.redmoon.PROFILES_PREFERENCE"
private const val MODE = Context.MODE_PRIVATE

private val defaultProfiles: Map<Profile, String> = mapOf(
        Profile(10, 30, 40, false) to getString(R.string.filter_name_default),
        Profile(20, 60, 78, false) to getString(R.string.filter_name_bed_reading),
        Profile(0, 0, 60, false) to getString(R.string.filter_name_dim_only))

private val prefs: SharedPreferences
    get() = appContext.getSharedPreferences(PREFERENCE_NAME, MODE)

private var modelOutdated: Boolean = true

private val _model: Map<Profile, String>
    get() = prefs.all.mapKeys { (k, _) -> Profile.parse(k) }.mapValues { (_, v) -> v as String }

private var model: Map<Profile, String> = _model
    get() {
        if (modelOutdated) {
            field = _model
            modelOutdated = false
        }
        return field
    }

object ProfilesModel : Logger() {
    // Must get from model directly to avoid cache invalidation issue
    val profiles: List<Profile>
        get() = model.keys.sorted()

    fun nameOf(p: Profile): String? = model[p]

    private fun i(profile: Profile) = profiles.indexOf(profile)

    fun profileAfter(profile: Profile): Profile = when {
        i(profile) < profiles.lastIndex -> profiles[i(profile) + 1]
        Config.custom.isSaved -> profiles[0]
        else -> Config.custom
    }

    val Profile.isSaved: Boolean
        get() = model.containsKey(this)

    fun save(profile: Profile, name: String) {
        Log.i("saving $name: $profile")
	var newName = name
        if (!profile.isSaved) {
            if(model.containsValue(name)) {
                var profileVersionNumber = 1
                while (model.containsValue("$name ($profileVersionNumber)")){
                    ++profileVersionNumber
                }
                newName = "$name ($profileVersionNumber)"
            }

            prefs.edit().putString(profile.toString(), newName).apply()
            modelOutdated = true
            EventBus.post(profilesUpdated())
        }
    }

    fun delete(profile: Profile) {
        Log.i("deleting ${profile.name}: $profile")
        profile.let {
            if (it.isSaved) {
                Config.custom = it
                prefs.edit().remove(it.toString()).apply()
                modelOutdated = true
                EventBus.post(profilesUpdated())
            }
        }
    }

    fun restoreDefaultProfiles() {
        val editor = prefs.edit()
        defaultProfiles.forEach { (profile, name) ->
            editor.putString(profile.toString(), name)
        }
        editor.apply()
        modelOutdated = true
        EventBus.post(profilesUpdated())
    }
}
