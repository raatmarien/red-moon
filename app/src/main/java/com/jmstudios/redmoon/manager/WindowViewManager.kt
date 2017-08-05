/*
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
package com.jmstudios.redmoon.manager

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.IntEvaluator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.view.WindowManager
import com.jmstudios.redmoon.event.buttonBacklightChanged

import com.jmstudios.redmoon.helper.AbstractAnimatorListener
import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.helper.Profile
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.receiver.OrientationChangeReceiver
import com.jmstudios.redmoon.util.*
import com.jmstudios.redmoon.view.ScreenFilterView

import org.greenrobot.eventbus.Subscribe

private class ProfileEvaluator : TypeEvaluator<Profile> {
    private val intEval  = IntEvaluator()
    private val argbEval = ArgbEvaluator()

    override fun evaluate(fraction: Float, start: Profile, end: Profile) = end.copy(
        color    = argbEval.evaluate(fraction, start.color, end.color) as Int,
        intensity = intEval.evaluate(fraction, start.intensity, end.intensity),
        dimLevel  = intEval.evaluate(fraction, start.dimLevel,  end.dimLevel ))
}

class WindowViewManager(private val mView: ScreenFilterView,
                        private val mScreenManager: ScreenManager,
                        private val mWindowManager: WindowManager) :
                    OrientationChangeReceiver.OnOrientationChangeListener {

    private val mAnimator = ValueAnimator.ofObject(ProfileEvaluator(), mView.profile).apply {
        addUpdateListener { valueAnimator ->
            mView.profile = valueAnimator.animatedValue as Profile
            if (filterIsOn && Config.buttonBacklightFlag == "dim") { updateLayout() }
        }
    }

    private var mLayoutParams = mScreenManager.layoutParams
        get() = field.apply { buttonBrightness = Config.buttonBacklightLevel }

    fun open(time: Int?) {
        mAnimator.removeAllListeners()
        setProfile(time, activeProfile)
        if (filterIsOn) {
            Log.d("Screen filter is already open")
            updateLayout()
        } else {
            mWindowManager.addView(mView, mLayoutParams)
            filterIsOn = true
        }
    }

    fun close(time: Int?, onEnd: () -> Unit = {}) {
        Log.i("close(time = $time, onEnd() = $onEnd)")
        mAnimator.removeAllListeners()
        mAnimator.addListener(object : AbstractAnimatorListener {
            override fun onAnimationEnd(animator: Animator) {
                if (filterIsOn) {
                    Log.i("Closing screen filter")
                    mWindowManager.removeView(mView)
                    filterIsOn = false
                } else {
                    Log.w("Can't close Screen filter; it's already closed")
                }
                onEnd()
            }
        })
        setProfile(time, activeProfile.copy(intensity = 0, dimLevel = 0))
    }

    private fun updateLayout() = mWindowManager.updateViewLayout(mView, mLayoutParams)

    private fun setProfile(time: Int?,  profile: Profile) = setProfile(time?.toLong(), profile)
    private fun setProfile(time: Long?, profile: Profile) = mAnimator.run {
        setObjectValues(mView.profile, profile)
        duration = time ?: if (isRunning) duration - currentPlayTime else 0
        Log.i("Setting screen filter to $profile. Duration: $duration")
        start()
    }

    // TODO: @Subscribe fun reLayout(event: orientationChanged) {
    override fun onOrientationChanged() {
        mLayoutParams = mScreenManager.layoutParams
        if (filterIsOn) { updateLayout() }
    }

    @Subscribe fun onButtonBacklightChanged(event: buttonBacklightChanged) {
        if (filterIsOn) updateLayout()
    }

    companion object : Logger()
}
