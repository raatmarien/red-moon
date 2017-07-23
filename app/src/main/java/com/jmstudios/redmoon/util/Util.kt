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
package com.jmstudios.redmoon.util

import android.content.Intent
import android.support.v4.content.ContextCompat

import com.jmstudios.redmoon.application.RedMoonApplication
import com.jmstudios.redmoon.helper.EventBus
import com.jmstudios.redmoon.helper.Profile
import com.jmstudios.redmoon.helper.KLogging
import com.jmstudios.redmoon.model.Config

import kotlin.reflect.KClass

private val uLog = KLogging.logger("Util", true)

val appContext = RedMoonApplication.app

var activeProfile: Profile
    get() = EventBus.getSticky(Profile::class) ?: with (Config) {
                Profile(color, intensity, dimLevel, lowerBrightness)
            }
    set(value) = value.let {
        if (it != EventBus.getSticky(Profile::class)) with (Config) {
            uLog.i("activeProfile set to $it")
            EventBus.postSticky(it)
            color = it.color
            intensity = it.intensity
            dimLevel = it.dimLevel
            lowerBrightness = it.lowerBrightness
        }
    }

var filterIsOn: Boolean = false
    set(value) {
        field = value
        Config.filterIsOn = value
    }

fun getString(resId: Int): String = appContext.getString(resId)
fun getColor (resId: Int): Int = ContextCompat.getColor(appContext, resId)

fun atLeastAPI(api: Int): Boolean = android.os.Build.VERSION.SDK_INT >= api
fun belowAPI  (api: Int): Boolean = android.os.Build.VERSION.SDK_INT <  api

fun intent() = Intent()
fun <T: Any>intent(kc: KClass<T>) = Intent(appContext, kc.java)
