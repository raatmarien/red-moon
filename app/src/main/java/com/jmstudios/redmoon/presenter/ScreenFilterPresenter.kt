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

import android.animation.Animator
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.WindowManager

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.activity.MainActivity
import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.helper.*
import com.jmstudios.redmoon.manager.ScreenManager
import com.jmstudios.redmoon.manager.WindowViewManager
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.model.ProfilesModel
import com.jmstudios.redmoon.receiver.NextProfileCommandReceiver
import com.jmstudios.redmoon.receiver.OrientationChangeReceiver
import com.jmstudios.redmoon.receiver.ScreenStateReceiver
import com.jmstudios.redmoon.receiver.SwitchAppWidgetProvider
import com.jmstudios.redmoon.service.ScreenFilterService
import com.jmstudios.redmoon.service.ServiceLifeCycleController
import com.jmstudios.redmoon.thread.CurrentAppMonitoringThread
import com.jmstudios.redmoon.util.*
import com.jmstudios.redmoon.view.ScreenFilterView

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ScreenFilterPresenter(private val mServiceController: ServiceLifeCycleController,
                            private val mContext: Context,
                            private val mWindowViewManager: WindowViewManager,
                            private val mScreenManager: ScreenManager):
                        OrientationChangeReceiver.OnOrientationChangeListener,
                        ScreenStateReceiver.ScreenStateListener {
    private var mView: ScreenFilterView = mWindowViewManager.mView
    private var mCamThread: CurrentAppMonitoringThread? = null
    private val mScreenStateReceiver = ScreenStateReceiver(this)
    private var screenOff: Boolean = false

    private val mOnState = OnState()
    private val mOffState = OffState()
    private val mPreviewState = PreviewState()
    private val mSuspendState = SuspendState()

    private var mCurrentState: State = InitState()

    // Screen brightness state
    private var oldBrightness: Int = -1
    private var oldAutomaticBrightness: Boolean = false

    // Just a convenience
    private val filterIsOn: Boolean
        get() = mCurrentState.filterIsOn

    init {
        // Always initialize to off
        onScreenFilterCommand(ScreenFilterService.Command.OFF)
    }

    fun updateWidgets() {
        //Broadcast to keep appwidgets in sync
        Log.i("Sending update broadcast")
        val updateAppWidgetIntent = Intent(mContext, SwitchAppWidgetProvider::class.java)
        updateAppWidgetIntent.action = SwitchAppWidgetProvider.ACTION_UPDATE
        updateAppWidgetIntent.putExtra(SwitchAppWidgetProvider.EXTRA_POWER, filterIsOn)
        mContext.sendBroadcast(updateAppWidgetIntent)
    }

    fun onScreenFilterCommand(command: ScreenFilterService.Command) {
        Log.i("Handling command ${command.name} in state: $this")
        mCurrentState.onScreenFilterCommand(command)
    }

    @Subscribe
    fun onPowerStateChanged(event: filterIsOnChanged) {
        updateWidgets()
        // If an app like Tasker wants to do something each time
        // Red Moon is toggled, it can listen for this event
        val intent = Intent()
        intent.action = BROADCAST_ACTION
        intent.putExtra(BROADCAST_FIELD, filterIsOn)
        mContext.sendBroadcast(intent)
    }

    @Subscribe
    fun onProfileChanged(event: profileChanged) {
        refreshForegroundNotification()
    }

    @Subscribe
    fun onColorChanged(event: colorChanged) {
        mCurrentState.onColorChanged()
    }

    @Subscribe
    fun onIntensityChanged(event: intensityChanged) {
        mCurrentState.onIntensityChanged()
    }

    @Subscribe
    fun onDimChanged(event: dimChanged) {
        mCurrentState.onDimChanged()
    }

    @Subscribe
    fun onLowerBrightnessChanged(event: lowerBrightnessChanged) {
        if (hasWriteSettingsPermission) {
            mCurrentState.onLowerBrightnessChanged()
        } else {
            EventBus.getDefault().post(changeBrightnessDenied())
        }
    }

    @Subscribe
    fun onSecureSuspendChanged(event: secureSuspendChanged) {
        mCurrentState.onSecureSuspendChanged()
    }

    @Subscribe
    fun onButtonBacklightChanged(event: buttonBacklightChanged) {
        mWindowViewManager.reLayoutWindow(filterLayoutParams)
    }

    override fun onScreenTurnedOn() {
        Log.i("Screen turn on received")
        screenOff = false
        startCamThread()
    }

    override fun onScreenTurnedOff() {
        Log.i("Screen turn off received")
        screenOff = true
        stopCamThread()
    }

    override fun onPortraitOrientation() {
        mWindowViewManager.reLayoutWindow(filterLayoutParams)
    }

    override fun onLandscapeOrientation() {
        mWindowViewManager.reLayoutWindow(filterLayoutParams)
    }

    private fun refreshForegroundNotification() {
        Log.d("Creating notification while in $mCurrentState")

        val nb = NotificationCompat.Builder(mContext).apply {
            val context = mView.context
            val profilesModel = ProfilesModel(context)

            // Set notification appearance
            setSmallIcon(R.drawable.notification_icon_half_moon)
            setContentTitle(context.getString(R.string.app_name))
            setContentText(ProfilesHelper.getProfileName(profilesModel, Config.profile, context))
            setColor(ContextCompat.getColor(context, R.color.color_primary))
            setPriority(Notification.PRIORITY_MIN)

            // Open Red Moon when tapping notification body
            val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val mainActivityPI = PendingIntent.getActivity(context, REQUEST_CODE_ACTION_SETTINGS,
                                           mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            setContentIntent(mainActivityPI)

            // Add toggle action
            val toggleIconResId = if (filterIsOn) { R.drawable.ic_stop }
                                  else { R.drawable.ic_play }
            val toggleActionText = if (filterIsOn) { context.getString(R.string.action_off) }
                                   else { context.getString(R.string.action_on) }
            val toggleCommand = with (ScreenFilterService) {
                if (filterIsOn) { command(ScreenFilterService.Command.OFF) }
                else { command(ScreenFilterService.Command.ON) }
            }
            val togglePI = PendingIntent.getService(context, REQUEST_CODE_ACTION_TOGGLE,
                                         toggleCommand, PendingIntent.FLAG_UPDATE_CURRENT)
            addAction(toggleIconResId, toggleActionText, togglePI)

            // Add profile switch action
            val nextProfileText   = context.getString(R.string.action_next_filter)
            val nextProfileIntent = Intent(context, NextProfileCommandReceiver::class.java)
            val nextProfilePI     = PendingIntent.getBroadcast(context, REQUEST_CODE_NEXT_PROFILE,
                                                               nextProfileIntent, 0)
            addAction(R.drawable.ic_next_profile, nextProfileText, nextProfilePI)
        }

        if (filterIsOn) {
            Log.d("Creating a persistent notification")
            mServiceController.startForeground(NOTIFICATION_ID, nb.build())
        } else {
            mServiceController.stopForeground(false)
            val nm = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIFICATION_ID, nb.build())
        }
    }

    private val filterLayoutParams: WindowManager.LayoutParams
        get() = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    mScreenManager.screenHeight,
                    0,
                    -mScreenManager.statusBarHeightPx,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT
                ).apply{
                    gravity = Gravity.TOP or Gravity.START
                    buttonBrightness = Config.buttonBacklightLevel
                }

    private fun startCamThread() {
        if (mCamThread == null && !screenOff) {
            mCamThread = CurrentAppMonitoringThread(mContext)
            mCamThread!!.start()
        }
    }

    private fun stopCamThread() {
        if (mCamThread != null) {
            if (!mCamThread!!.isInterrupted) { mCamThread!!.interrupt() }
            mCamThread = null
        }
    }

    private fun startAppMonitoring() {
        Log.i("Starting app monitoring")
        val powerManager = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        screenOff = if (atLeastAPI(20)) { !powerManager.isInteractive }
                    else @Suppress("DEPRECATION") { !powerManager.isScreenOn }

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        mContext.registerReceiver(mScreenStateReceiver, filter)
        startCamThread()
    }

    private fun stopAppMonitoring() {
        Log.i("Stopping app monitoring")
        try {
            mContext.unregisterReceiver(mScreenStateReceiver)
        } catch (e: IllegalArgumentException) {
            // Catch errors when receiver is unregistered more than
            // once, it is not a problem, so we just ignore it.
        }

    }

    private fun saveBrightness() {
        if (Config.lowerBrightness) {
            try {
                val resolver = mContext.contentResolver
                oldBrightness = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS)
                oldAutomaticBrightness = 1 == Settings.System.getInt(resolver, "screen_brightness_mode")
            } catch (e: SettingNotFoundException) {
                Log.i("Error reading brightness state $e")
                oldAutomaticBrightness = false
            }
        } else {
            oldBrightness = -1
        }
        Config.automaticBrightness = oldAutomaticBrightness
        Config.brightness = oldBrightness
    }

    private fun restoreBrightness() {
        setBrightness(Config.brightness, Config.automaticBrightness, mContext)
    }

    private abstract inner class State {
        abstract val filterIsOn: Boolean

        open internal fun onActivation(prevState: State) {
            Log.i("super($this).onActivation($prevState)")
            Config.filterIsOn = filterIsOn
            refreshForegroundNotification()
        }

        open internal val nextState: (ScreenFilterService.Command) -> State = {
            when (it) {
                ScreenFilterService.Command.OFF           -> mOffState
                ScreenFilterService.Command.ON            -> mOnState
                ScreenFilterService.Command.SHOW_PREVIEW  -> mPreviewState
                ScreenFilterService.Command.START_SUSPEND -> mSuspendState
                else -> this
            }
        }

        open fun onScreenFilterCommand(command: ScreenFilterService.Command) {
            moveToState(nextState(command))
        }

        protected fun moveToState(newState: State) {
            if (!hasOverlayPermission) {
                Log.i("No overlay permission.")
                EventBus.getDefault().post(overlayPermissionDenied())
            } else if (newState !== this) {
                Log.i("Transitioning from $this to $newState")
                mCurrentState = newState
                mCurrentState.onActivation(this)
            }
        }

        open fun onColorChanged() {}
        open fun onIntensityChanged() {}
        open fun onDimChanged() {}
        open fun onLowerBrightnessChanged() {}
        open fun onSecureSuspendChanged() {}

        internal fun openScreenFilter() {
            mWindowViewManager.openWindow(filterLayoutParams)
        }

        open internal fun closeScreenFilter() {
            mWindowViewManager.closeWindow()
        }

        override fun toString(): String {
            return javaClass.simpleName
        }
    }
    
    private inner class InitState : State() {
        override val filterIsOn = false
    }

    private inner class OnState : State() {
        override val filterIsOn = true

        override fun onActivation(prevState: State) {
            openScreenFilter()
            super.onActivation(prevState)
            mView.animateDimLevel(Config.dim, null)
            mView.animateIntensityLevel(Config.intensity, null)

            if (Config.lowerBrightness) {
                saveBrightness()
                setBrightness(0, false, mContext)
            }
            if (Config.secureSuspend) startAppMonitoring()
        }

        override val nextState: (ScreenFilterService.Command) -> State = {
            if (it == ScreenFilterService.Command.TOGGLE) { mOffState }
            else { super.nextState(it) }
        }

        override fun onColorChanged() {
            mView.colorTempProgress = Config.color
        }

        override fun onIntensityChanged() {
            val intensity= Config.intensity
            mView.cancelIntensityAnimator()
            mView.filterIntensityLevel = intensity
        }

        override fun onDimChanged() {
            val dim = Config.dim
            mView.cancelDimAnimator()
            mView.filterDimLevel = dim
            if (Config.buttonBacklightFlag == "dim") {
                mWindowViewManager.reLayoutWindow(filterLayoutParams)
            }
        }

        override fun onLowerBrightnessChanged() {
            val lowerBrightness = Config.lowerBrightness
            Log.i("Lower brightness flag changed to: $lowerBrightness")
            if (lowerBrightness) {
                saveBrightness()
                setBrightness(0, false, mContext)
            } else {
                restoreBrightness()
            }
        }

        override fun onSecureSuspendChanged() {
            if (Config.secureSuspend) startAppMonitoring()
            else stopAppMonitoring()
        }

        override fun closeScreenFilter() {
            Log.i("Filter is turning on again; don't close it.")
        }
    }

    private inner class OffState : State() {
        override val filterIsOn = false

        override fun onActivation(prevState: State) {
            super.onActivation(prevState)

            if (prevState !== mPreviewState) {
                mView.animateIntensityLevel(ScreenFilterView.MIN_INTENSITY, null)
                val listener = object: AbstractAnimatorListener() {
                    override fun onAnimationCancel(animator: Animator) { mCurrentState.closeScreenFilter() }
                    override fun onAnimationEnd(animator: Animator) { mCurrentState.closeScreenFilter() }
                }
                mView.animateDimLevel(ScreenFilterView.MIN_DIM, listener)
            } else {
                closeScreenFilter()
            }

            if (Config.lowerBrightness) restoreBrightness()
            if (Config.secureSuspend) stopAppMonitoring()
        }

        override val nextState: (ScreenFilterService.Command) -> State = {
            if (it == ScreenFilterService.Command.TOGGLE) mOnState
            else super.nextState(it)
        }
    }

    /* This State is used to present the filter to the user when (s)he
     * is holding one of the seekbars to adjust the filter. It turns
     * on the filter and saves what state it should be when it will be
     * turned off.
     */
    private inner class PreviewState : State() {
        lateinit var stateToReturnTo: State
        var pressesActive: Int = 0

        override val filterIsOn: Boolean
            get() = stateToReturnTo.filterIsOn

        override fun onActivation(prevState: State) {
            stateToReturnTo = prevState
            pressesActive = 1
            super.onActivation(prevState)
            // If the animators are not canceled, preview does not work when filter is turning off
            mView.cancelDimAnimator()
            mView.cancelIntensityAnimator()
            openScreenFilter()

            mView.filterDimLevel = Config.dim
            mView.filterIntensityLevel = Config.intensity
            mView.colorTempProgress = Config.color
        }

        override val nextState: (ScreenFilterService.Command) -> State = {
            command -> stateToReturnTo.nextState(command)
        }

        override fun onScreenFilterCommand(command: ScreenFilterService.Command) {
            Log.d("Preview, got command: " + command.name)
            when (command) {
                ScreenFilterService.Command.SHOW_PREVIEW -> {
                    pressesActive++
                    Log.d(String.format("%d presses active", pressesActive))
                }
                ScreenFilterService.Command.HIDE_PREVIEW -> {
                    pressesActive--
                    Log.d(String.format("%d presses active", pressesActive))
                    if (pressesActive <= 0) {
                        Log.d("Moving back to state: $stateToReturnTo")
                        moveToState(stateToReturnTo)
                    }
                }
                else -> stateToReturnTo = nextState(command)
            }
        }

        override fun onColorChanged() {
            mView.colorTempProgress = Config.color
        }

        override fun onIntensityChanged() {
            val intensity = Config.intensity
            mView.cancelDimAnimator()
            mView.filterIntensityLevel = intensity
        }

        override fun onDimChanged() {
            val dim = Config.dim
            mView.cancelDimAnimator()
            mView.filterDimLevel = dim
            if (Config.buttonBacklightFlag == "dim") {
                mWindowViewManager.reLayoutWindow(filterLayoutParams)
            }
        }
    }

    /* This state is used when the filter is suspended temporarily,
     * because the user is in an excluded app (for example the package
     * installer). It stops the filter like in the OffState, but
     * doesn't change the UI, switch or brightness state just like the
     * PreviewState. Like the PreviewState, it logs changes to the
     * state and applies them when the suspend state is deactivated.
     */
    private inner class SuspendState : State() {
        lateinit var stateToReturnTo: State
        
        override val filterIsOn: Boolean
            get() = stateToReturnTo.filterIsOn

        override fun onActivation(prevState: State) {
            stateToReturnTo = prevState
            closeScreenFilter()
            super.onActivation(prevState)
        }

        override val nextState: (ScreenFilterService.Command) -> State = {
            command -> stateToReturnTo.nextState(command)
        }

        override fun onScreenFilterCommand(command: ScreenFilterService.Command) {
            Log.d("In Suspend, got command: " + command.name)
            when (command) {
                ScreenFilterService.Command.STOP_SUSPEND -> moveToState(stateToReturnTo)
                ScreenFilterService.Command.START_SUSPEND -> {}
                else -> stateToReturnTo = nextState(command)
            }
        }
    }

    companion object : Logger() {
        const val NOTIFICATION_ID = 1
        private const val REQUEST_CODE_ACTION_SETTINGS = 1000
        private const val REQUEST_CODE_ACTION_TOGGLE = 3000
        private const val REQUEST_CODE_NEXT_PROFILE = 4000

        const val FADE_DURATION_MS = 1000

        const val BROADCAST_ACTION = "com.jmstudios.redmoon.RED_MOON_TOGGLED"
        const val BROADCAST_FIELD = "jmstudios.bundle.key.FILTER_IS_ON"

        // Statically used by BootReceiver
        fun setBrightness(brightness: Int, automatic: Boolean, context: Context) {
            Log.i("Setting brightness to: $brightness, automatic: $automatic")
            if (atLeastAPI(23) && !Settings.System.canWrite(context)) return
            if (brightness >= 0) {
                val resolver = context.contentResolver
                Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
                Settings.System.putInt(resolver, "screen_brightness_mode", if (automatic) 1 else 0)
            }
        }
    }
}
