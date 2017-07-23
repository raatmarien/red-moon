/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
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
package com.jmstudios.redmoon.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.support.v7.widget.SwitchCompat

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.fragment.FilterFragment
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.model.profiles.restoreDefaultProfiles
import com.jmstudios.redmoon.service.ScreenFilterService
import com.jmstudios.redmoon.helper.EventBus
import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.helper.Permission
import com.jmstudios.redmoon.helper.upgrade
import com.jmstudios.redmoon.util.*

import de.cketti.library.changelog.ChangeLog
import org.greenrobot.eventbus.Subscribe

class MainActivity : ThemedAppCompatActivity() {

    data class UI(val isOpen: Boolean) : EventBus.Event

    companion object : Logger() {
        const val EXTRA_FROM_SHORTCUT_BOOL = "com.jmstudios.redmoon.activity.MainActivity.EXTRA_FROM_SHORTCUT_BOOL"
    }

    override val fragment = FilterFragment()
    override val tag = "jmstudios.fragment.tag.FILTER"

    private var mSwitch : SwitchCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        upgrade()
        val fromShortcut = intent.getBooleanExtra(EXTRA_FROM_SHORTCUT_BOOL, false)
        Log.i("Got intent")
        if (fromShortcut) { toggleAndFinish() }

        super.onCreate(savedInstanceState)
        if (!Config.introShown) { startActivity(intent(Intro::class)) }
        ChangeLog(this).run { if (isFirstRun) logDialog.show() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)

        menu.findItem(R.id.menu_dark_theme).isChecked = Config.darkThemeFlag
        mSwitch = (menu.findItem(R.id.screen_filter_switch).actionView as SwitchCompat).apply {
            safeSetChecked(filterIsOn) // Side effect: sets listener
        }

        return true
    }

    fun SwitchCompat.safeSetChecked(checked: Boolean) {
        setOnCheckedChangeListener { _, _ ->  }
        isChecked = checked
        setOnCheckedChangeListener { _, checked ->
            ScreenFilterService.toggle(checked)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.postSticky(UI(isOpen = true))
    }

    override fun onResume() {
        Log.i("onResume")
        super.onResume()
        mSwitch?.safeSetChecked(filterIsOn)
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
            R.id.menu_dark_theme -> {
                Config.darkThemeFlag = !Config.darkThemeFlag
                recreate()
            }
            R.id.menu_restore_default_filters -> {
                restoreDefaultProfiles()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun toggleAndFinish() {
        ScreenFilterService.toggle()
        finish()
    }

    @Subscribe fun onFilterIsOnChanged(event: filterIsOnChanged) {
        Log.i("FilterIsOnChanged")
        mSwitch?.safeSetChecked(filterIsOn)
    }

    @Subscribe fun onOverlayPermissionDenied(event: overlayPermissionDenied) {
        mSwitch?.safeSetChecked(false)
        Permission.Overlay.request(this)
    }
}
