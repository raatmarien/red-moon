/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon

import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.util.*

abstract class ThemedAppCompatActivity : AppCompatActivity() {

    protected abstract val fragment: PreferenceFragment
    protected abstract val tag: String

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Config.activeTheme)
        setContentView(R.layout.activity_main)

        // Only create and attach a new fragment on the first Activity creation.
        if (savedInstanceState == null) {
            Log.i("onCreate - First creation")
            fragmentManager.beginTransaction()
                           .replace(R.id.fragment_container, fragment, tag)
                           .commit()
        }

        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        Permission.onRequestResult(requestCode)
    }
    companion object : Logger()
}
