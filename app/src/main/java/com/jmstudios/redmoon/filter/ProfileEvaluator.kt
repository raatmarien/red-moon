/*
 * Copyright (c) 2017 Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.jmstudios.redmoon.filter

import android.animation.ArgbEvaluator
import android.animation.IntEvaluator
import android.animation.TypeEvaluator

import com.jmstudios.redmoon.model.Profile

class ProfileEvaluator : TypeEvaluator<Profile> {
    private val intEval  = IntEvaluator()
    private val argbEval = ArgbEvaluator()

    override fun evaluate(fraction: Float, start: Profile, end: Profile) = end.copy(
        color     = argbEval.evaluate(fraction, start.color, end.color) as Int,
        intensity = intEval.evaluate(fraction, start.intensity, end.intensity),
        dimLevel  = intEval.evaluate(fraction, start.dimLevel,  end.dimLevel ))
}
