/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (c) 2015 Chris Nguyen
 *     Copyright (c) 2016 Zoraver <https://github.com/Zoraver>
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
package com.jmstudios.redmoon.manager

import android.content.Context
import android.provider.Settings
import com.jmstudios.redmoon.event.changeBrightnessDenied
import com.jmstudios.redmoon.helper.EventBus
import com.jmstudios.redmoon.helper.Profile
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.helper.Permission
import com.jmstudios.redmoon.util.*

import org.greenrobot.eventbus.Subscribe

class BrightnessManager(private val mContext: Context) {
    companion object: Logger()

    val hasPermission
        get() = Permission.WriteSettings.isGranted

    @Subscribe fun onProfileChanged(profile: Profile) {
        Log.i("Recieved profile change: $profile")
        if (profile.lowerBrightness) lower() else restore()
    }

    fun lower() =  when {
        !filterIsOn -> Log.w("Can't lower brightness; filter is off!")
        Config.brightnessLowered -> Log.w("Brightness is already lowered!")
        !activeProfile.lowerBrightness -> Log.w("Lower brightness not enabled!")
        !hasPermission -> {
            EventBus.post(changeBrightnessDenied())
            Log.i("Permission not granted!")
        }
        else -> try {
            val resolver = mContext.contentResolver
            val oldLevel = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS)
            val oldAuto = 1 == Settings.System.getInt(resolver, "screen_brightness_mode")
            Config.automaticBrightness = oldAuto
            Config.brightness = oldLevel

            Log.i("Lowering brightness from: $oldLevel, auto: $oldAuto")
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 0)
            Settings.System.putInt(resolver, "screen_brightness_mode", 0)
            Config.brightnessLowered = true
        } catch (e: Settings.SettingNotFoundException) {
            Log.e("Error reading brightness state $e")
        }
    }

    fun restore() = when {
        !Config.brightnessLowered -> Log.w("Can't restore brightness; it's not lowered!")
        !hasPermission -> Log.w("Permission not granted!")
        else -> {
            val resolver = mContext.contentResolver
            val automatic = if (Config.automaticBrightness) 1 else 0

            Log.i("Restoring brightness to: ${Config.brightness}, automatic: $automatic")
            Settings.System.putInt(resolver, "screen_brightness_mode", automatic)
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, Config.brightness)
            Config.brightnessLowered = false
        }
    }
}
