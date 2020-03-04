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
package com.jmstudios.redmoon.filter.overlay

import android.content.Context
import android.provider.Settings

import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.util.*

class BrightnessManager(private val context: Context) {
    companion object: Logger()

    private val resolver = context.contentResolver

    private var level: Int
        get() = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS)
        set(value) {
            if (belowAPI(23) || Settings.System.canWrite(context)) {
                Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, value)
            } else {
                Log.w("Write settings permission not granted; cannot set brightness")
            }
        }

    private var auto: Boolean
        get() = 1 == Settings.System.getInt(resolver, "screen_brightness_mode")
        set(value) {
            val i = if (value) 1 else 0
            Settings.System.putInt(resolver, "screen_brightness_mode", i)
        }

    var brightnessLowered: Boolean
        get() = Config.brightnessLowered && !auto && (level == 0)
        set(lower) = when {
            !Permission.WriteSettings.isGranted -> {
                Log.i("Permission not granted!")
                EventBus.post(changeBrightnessDenied())
            } lower == brightnessLowered -> {
                Log.i("Brightness already raised/lowered")
            } lower -> {
                try {
                    Log.i("Saving current brightness")
                    Config.automaticBrightness = auto
                    Config.brightness = level
                    Log.i("Lowering brightness")
                    level = 0
                    auto = false
                    Config.brightnessLowered = true
                } catch (e: Settings.SettingNotFoundException) {
                    Log.e("Error reading brightness state $e")
                }
            } else -> {
                Log.i("Restoring brightness")
                auto = Config.automaticBrightness
                level = Config.brightness
                Config.brightnessLowered = false
            }
        }
}
