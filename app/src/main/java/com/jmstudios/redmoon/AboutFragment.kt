/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import com.jmstudios.redmoon.util.*
import de.cketti.library.changelog.ChangeLog

class AboutFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate()")
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.about)
        pref(R.string.pref_key_version).apply{
            summary = BuildConfig.VERSION_NAME
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                ChangeLog(activity).fullLogDialog.show()
                true
            }
        }
    }
    companion object : Logger()
}
