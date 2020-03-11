/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
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
package com.jmstudios.redmoon

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.filter.Command
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.model.ProfilesModel
import com.jmstudios.redmoon.settings.SettingsActivity
import com.jmstudios.redmoon.util.*

import org.greenrobot.eventbus.Subscribe

class MainActivity : ThemedAppCompatActivity() {

    data class UI(val isOpen: Boolean) : EventBus.Event

    companion object : Logger() {
        const val EXTRA_FROM_SHORTCUT_BOOL = "com.jmstudios.redmoon.activity.MainActivity.EXTRA_FROM_SHORTCUT_BOOL"
    }

    override val fragment = FilterFragment()
    override val tag = "jmstudios.fragment.tag.FILTER"

    private val fab: FloatingActionButton get() = findViewById(R.id.fab_toggle)

    override fun onCreate(savedInstanceState: Bundle?) {
        val fromShortcut = intent.getBooleanExtra(EXTRA_FROM_SHORTCUT_BOOL, false)
        Log.i("Got intent")
        if (fromShortcut) { toggleAndFinish() }

        super.onCreate(savedInstanceState)

        if (!Config.introShown) { startActivity(intent(Intro::class)) }
        showChangelogAuto(this)

        fab.setOnClickListener { _ -> Command.toggle() }
        fab.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    private fun setFabIcon(on: Boolean = filterIsOn) {
        fab.setImageResource(if (on) R.drawable.fab_pause else R.drawable.fab_start)
    }

    override fun onStart() {
        super.onStart()
        EventBus.postSticky(UI(isOpen = true))
    }

    override fun onResume() {
        Log.i("onResume")
        super.onResume()
        setFabIcon()
        EventBus.register(this)
    }

    override fun onPause() {
        EventBus.unregister(this)
        super.onPause()
    }

    override fun onStop() {
        EventBus.postSticky(UI(isOpen = false))
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        Log.i("onNewIntent")
        super.onNewIntent(intent)
        val fromShortcut = intent.getBooleanExtra(EXTRA_FROM_SHORTCUT_BOOL, false)
        if (fromShortcut) { toggleAndFinish() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.itemId) {
            R.id.menu_show_intro -> {
                startActivity(intent(Intro::class))
            }
            R.id.menu_about -> {
                startActivity(intent(AboutActivity::class))
            }
            R.id.menu_settings -> {
                startActivity(intent(SettingsActivity::class))
            }
            R.id.menu_restore_default_filters -> {
                ProfilesModel.restoreDefaultProfiles()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun toggleAndFinish() {
        Command.toggle(!filterIsOn)
        finish()
    }

    @Subscribe fun onFilterIsOnChanged(event: filterIsOnChanged) {
        Log.i("FilterIsOnChanged")
        setFabIcon()
    }

    @Subscribe fun onOverlayPermissionDenied(event: overlayPermissionDenied) {
        setFabIcon(false)
        Permission.Overlay.request(this)
    }
}
