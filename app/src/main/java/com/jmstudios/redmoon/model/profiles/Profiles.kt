/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017 Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.model.profiles

import android.content.Context
import android.content.SharedPreferences

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.event.profilesUpdated
import com.jmstudios.redmoon.helper.EventBus
import com.jmstudios.redmoon.helper.KLogging
import com.jmstudios.redmoon.helper.Profile
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.util.*

import me.smichel.android.KPreferences.Preferences

private const val PREFERENCE_NAME = "com.jmstudios.redmoon.PROFILES_PREFERENCE"
private const val MODE = Context.MODE_PRIVATE

private val prefs: SharedPreferences
    get() = appContext.getSharedPreferences(PREFERENCE_NAME, MODE)

private var modelOutdated: Boolean = true

private val _ProfilesModel: Map<Profile, String>
    get() = prefs.all.mapKeys { (k, _) -> Profile.parse(k) }.mapValues { (_, v) -> v as String }

private var ProfilesModel: Map<Profile, String> = _ProfilesModel
    get() {
        if (modelOutdated) {
            field = _ProfilesModel
            modelOutdated = false
        }
        return field
    }

val Profiles: List<Profile>
    get() = ProfilesModel.keys.sorted()

val Profile.name
    get() = ProfilesModel[this] ?: getString(R.string.filter_name_custom)

val Profile.isSaved: Boolean
    get() = ProfilesModel.containsKey(this)

val Profile.next: Profile
    get() = Profiles.indexOf(this).let {
        when {
            it < Profiles.lastIndex -> Profiles[it + 1]
            Config.custom.isSaved -> Profiles[0]
            else -> Config.custom
        }
    }

fun Profile.saveAs(name: String) {
    if (!ProfilesModel.let { it.containsKey(this) || it.containsValue(name) }) {
        prefs.edit().putString(this.toString(), name).apply()
        modelOutdated = true
        EventBus.post(profilesUpdated())
    }
}

fun Profile.delete() {
    if (isSaved) {
        Config.custom = this
        prefs.edit().remove(this.toString()).apply()
        modelOutdated = true
        EventBus.post(profilesUpdated())
    }
}

private val defaultProfiles: Map<Profile, String> = mapOf(
        Profile(10, 30, 40, false) to getString(R.string.filter_name_default),
        Profile(20, 60, 78, false) to getString(R.string.filter_name_bed_reading),
        Profile(0, 0, 60, false) to getString(R.string.filter_name_dim_only))

fun restoreDefaultProfiles() {
    val editor = prefs.edit()
    defaultProfiles.forEach { profile, name ->
        editor.putString(profile.toString(), name)
    }
    editor.apply()
    modelOutdated = true
    EventBus.post(profilesUpdated())
}

private val Log = KLogging.logger("ProfilesModel", true)
