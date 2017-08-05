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

package com.jmstudios.redmoon.presenter

import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.helper.EventBus
import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.helper.Permission
import com.jmstudios.redmoon.helper.Profile
import com.jmstudios.redmoon.service.ServiceController
import com.jmstudios.redmoon.service.ScreenFilterService.Command

import org.greenrobot.eventbus.Subscribe

const val DURATION_FADE    = 3600000 // One hour
const val DURATION_LONG    = 1000 // One second
const val DURATION_SHORT   = 250
const val DURATION_INSTANT = 0

class ScreenFilterPresenter(private val mController: ServiceController) {
    private val mOnState      = OnState()
    private val mOffState     = OffState()
    private val mPauseState   = PauseState()
    private val mPreviewState = PreviewState()
    private val mSuspendState = SuspendState()
    private val mFadeInState  = FadeInState()
    private val mFadeOutState = FadeOutState()

    private var mCurrentState: State = mOffState

    init {
        Log.i("Initializing")
        EventBus.register(mCurrentState)
    }

    fun handleCommand(command: Command) = mCurrentState.handleCommand(command)

    private abstract inner class State {
        @Subscribe open fun onProfileChanged(profile: Profile) {}
        abstract fun onActivation(prevState: State)

        open internal fun nextState(command: Command): State = when (command) {
            Command.OFF           -> mOffState
            Command.ON            -> mOnState
            Command.SHOW_PREVIEW  -> mPreviewState
            Command.SUSPEND       -> mSuspendState
            Command.PAUSE         -> mPauseState
            Command.FADE_ON       -> mFadeInState
            Command.FADE_OFF      -> mFadeOutState
            else                  -> this
        }

        open internal fun handleCommand(command: Command) = moveToState(nextState(command))

        protected fun moveToState(newState: State) {
            if (!Permission.Overlay.isGranted) {
                Log.i("No overlay permission.")
                EventBus.post(overlayPermissionDenied())
            } else if (newState !== this) {
                Log.i("Transitioning from $this to $newState")
                EventBus.unregister(this)
                mCurrentState = newState
                EventBus.register(mCurrentState)
                mCurrentState.onActivation(this)
            }
        }

        override fun toString(): String = javaClass.simpleName
    }

    private open inner class OnState : State() {
        override fun onActivation(prevState: State) = mController.start(DURATION_LONG)
        override fun onProfileChanged(profile: Profile) = mController.start(DURATION_SHORT)
        override fun nextState(command: Command): State {
            return if (command == Command.TOGGLE) mOffState else super.nextState(command)
        }
    }

    private inner class FadeInState : OnState() {
        override fun onActivation(prevState: State) = mController.start(DURATION_FADE)
    }

    private open inner class OffState : State() {
        override fun onActivation(prevState: State) {
            val len = if (prevState === mPreviewState) DURATION_INSTANT else DURATION_LONG
            mController.stop(len)
        }

        override fun nextState(command: Command): State {
            return if (command == Command.TOGGLE) mOnState else super.nextState(command)
        }
    }

    private inner class FadeOutState : OffState() {
        override fun onActivation(prevState: State) = mController.stop(DURATION_FADE)
    }

    private abstract inner class TempState : State() {
        lateinit var mPrevState: State

        override fun onActivation(prevState: State)  { mPrevState = prevState }
        override fun handleCommand(command: Command) { mPrevState = nextState(command) }

        override fun nextState(command: Command): State = mPrevState.nextState(command)
    }

    /* This State is used to present the filter to the user when (s)he
     * is holding one of the seekbars to adjust the filter. It turns
     * on the filter and saves what state it should be when it will be
     * turned off.
     */
    private inner class PreviewState : TempState() {
        var pressesActive: Int = 0

        override fun onActivation(prevState: State) {
            super.onActivation(prevState)
            pressesActive = 1
            mController.start(DURATION_INSTANT)
        }

        override fun handleCommand(command: Command) {
            Log.d("Preview, got command: " + command.name)
            when (command) {
                Command.SHOW_PREVIEW -> {
                    pressesActive++
                    Log.d("$pressesActive presses active")
                }
                Command.HIDE_PREVIEW -> {
                    pressesActive--
                    Log.d("$pressesActive presses active")
                    if (pressesActive <= 0) {
                        Log.d("Moving back to state: $mPrevState")
                        moveToState(mPrevState)
                    }
                }
                else -> super.handleCommand(command)
            }
        }

        override fun onProfileChanged(profile: Profile) = mController.start(DURATION_INSTANT)
    }

    /* This state is used when the filter is suspended temporarily,
     * because the user is in an excluded app (for example the package
     * installer). It stops the filter like in the OffState, but
     * doesn't change the UI, switch or brightness state just like the
     * PreviewState. Like the PreviewState, it logs changes to the
     * state and applies them when the suspend state is deactivated.
     */
    private open inner class SuspendState : TempState() {
        open val duration = DURATION_SHORT

        override fun onActivation(prevState: State) {
            super.onActivation(prevState)
            mController.pause(duration)
        }

        override fun handleCommand(command: Command) {
            Log.d("In Suspend, got command: " + command.name)
            when (command) {
                Command.SUSPEND, Command.PAUSE -> {}
                Command.RESUME -> moveToState(mPrevState)
                Command.ON     -> moveToState(mOnState)
                else -> super.handleCommand(command)
            }
        }
    }

    private inner class PauseState : SuspendState() {
        override val duration = DURATION_LONG
    }

    companion object : Logger()
}
