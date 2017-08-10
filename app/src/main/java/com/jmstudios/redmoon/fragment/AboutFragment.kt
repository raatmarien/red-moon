/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.fragment

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import com.jmstudios.redmoon.BuildConfig

import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.helper.Logger
import de.cketti.library.changelog.ChangeLog

class AboutFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate()")
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.about)
        preferenceScreen.getPreference(0).apply{
            summary = BuildConfig.VERSION_NAME
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                ChangeLog(activity).logDialog.show()
                true
            }
        }
    }
    companion object : Logger()
}
