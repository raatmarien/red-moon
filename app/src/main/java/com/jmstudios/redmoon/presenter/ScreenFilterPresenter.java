/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
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

package com.jmstudios.redmoon.presenter;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.content.ContentResolver;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.Build.VERSION;
import android.os.PowerManager;
import android.app.NotificationManager;
import android.content.IntentFilter;

import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.activity.ShadesActivity;
import com.jmstudios.redmoon.helper.AbstractAnimatorListener;
import com.jmstudios.redmoon.helper.FilterCommandFactory;
import com.jmstudios.redmoon.helper.FilterCommandParser;
import com.jmstudios.redmoon.helper.ProfilesHelper;
import com.jmstudios.redmoon.manager.ScreenManager;
import com.jmstudios.redmoon.manager.WindowViewManager;
import com.jmstudios.redmoon.model.SettingsModel;
import com.jmstudios.redmoon.model.ProfilesModel;
import com.jmstudios.redmoon.receiver.OrientationChangeReceiver;
import com.jmstudios.redmoon.receiver.SwitchAppWidgetProvider;
import com.jmstudios.redmoon.receiver.NextProfileCommandReceiver;
import com.jmstudios.redmoon.service.ScreenFilterService;
import com.jmstudios.redmoon.service.ServiceLifeCycleController;
import com.jmstudios.redmoon.view.ScreenFilterView;
import com.jmstudios.redmoon.thread.CurrentAppMonitoringThread;
import com.jmstudios.redmoon.receiver.ScreenStateReceiver;

public class ScreenFilterPresenter implements OrientationChangeReceiver.OnOrientationChangeListener,
                                              SettingsModel.OnSettingsChangedListener,
                                              ScreenStateReceiver.ScreenStateListener {
    private static final String TAG = "ScreenFilterPresenter";
    private static final boolean DEBUG = false;

    public static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE_ACTION_SETTINGS = 1000;
    private static final int REQUEST_CODE_ACTION_STOP = 2000;
    private static final int REQUEST_CODE_ACTION_PAUSE_OR_RESUME = 3000;
    private static final int REQUEST_CODE_NEXT_PROFILE = 4000;

    public static final int FADE_DURATION_MS = 1000;

    private ScreenFilterView mView;
    private SettingsModel mSettingsModel;
    private ServiceLifeCycleController mServiceController;
    private Context mContext;
    private WindowViewManager mWindowViewManager;
    private ScreenManager mScreenManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private FilterCommandFactory mFilterCommandFactory;
    private FilterCommandParser mFilterCommandParser;
    private CurrentAppMonitoringThread mCamThread;
    private ScreenStateReceiver mScreenStateReceiver;
    private boolean screenOff;

    private boolean mShuttingDown = false;
    private boolean mScreenFilterOpen = false;

    private ValueAnimator mColorAnimator;
    private ValueAnimator mDimAnimator;
    private ValueAnimator mIntensityAnimator;

    private final State mOnState = new OnState();
    private final State mPauseState = new PauseState();
    private final State mPreviewState = new PreviewState();
    private final State mSuspendState = new SuspendState();
    private State mCurrentState = mPauseState;

    private final PreviewState mPreviewStateCast = (PreviewState) mPreviewState;
    private final SuspendState mSuspendStateCast = (SuspendState) mSuspendState;

    // Screen brightness state
    private int oldScreenBrightness;
    private boolean oldIsAutomaticBrightness;

    public ScreenFilterPresenter(@NonNull ScreenFilterView view,
                                 @NonNull SettingsModel model,
                                 @NonNull ServiceLifeCycleController serviceController,
                                 @NonNull Context context,
                                 @NonNull WindowViewManager windowViewManager,
                                 @NonNull ScreenManager screenManager,
                                 @NonNull NotificationCompat.Builder notificationBuilder,
                                 @NonNull FilterCommandFactory filterCommandFactory,
                                 @NonNull FilterCommandParser filterCommandParser) {
        mView = view;
        mSettingsModel = model;
        mServiceController = serviceController;
        mContext = context;
        mWindowViewManager = windowViewManager;
        mScreenManager = screenManager;
        mNotificationBuilder = notificationBuilder;
        mFilterCommandFactory = filterCommandFactory;
        mFilterCommandParser = filterCommandParser;
        mScreenStateReceiver = new ScreenStateReceiver(this);
        oldScreenBrightness = -1;

        mCurrentState.onScreenFilterCommand(ScreenFilterService.COMMAND_PAUSE);
    }

    private void refreshForegroundNotification() {
        Context context = mView.getContext();

        ProfilesModel profilesModel = new ProfilesModel(context);

        String title = context.getString(R.string.app_name);
        int color = context.getResources().getColor(R.color.color_primary);

        int smallIconResId = R.drawable.notification_icon_half_moon;
        String contentText;
        int pauseOrResumeDrawableResId;
        Intent pauseOrResumeCommand;
        String pauseOrResumeActionText;

        if (isPaused()) {
            Log.d(TAG, "Creating notification while in pause state");
            contentText = context.getString(R.string.paused);
            pauseOrResumeDrawableResId = R.drawable.ic_play;
            pauseOrResumeCommand = mFilterCommandFactory.createCommand(ScreenFilterService.COMMAND_ON);
            pauseOrResumeActionText = context.getString(R.string.resume_action);
        } else {
            Log.d(TAG, "Creating notification while NOT in pause state");
            contentText = context.getString(mSettingsModel.getAutomaticSuspend() ?
                                            R.string.running_no_warning : R.string.running);
            pauseOrResumeDrawableResId = R.drawable.ic_pause;
            pauseOrResumeCommand = mFilterCommandFactory.createCommand(ScreenFilterService.COMMAND_PAUSE);
            pauseOrResumeActionText = context.getString(R.string.pause_action);
        }

        Intent shadesActivityIntent = new Intent(context, ShadesActivity.class);
        shadesActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pauseOrResumePI = PendingIntent.getService(context, REQUEST_CODE_ACTION_PAUSE_OR_RESUME,
                pauseOrResumeCommand, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent settingsPI = PendingIntent.getActivity(context, REQUEST_CODE_ACTION_SETTINGS,
                shadesActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextProfileIntent = new Intent(context, NextProfileCommandReceiver.class);

        PendingIntent nextProfilePI = PendingIntent.getBroadcast(context, REQUEST_CODE_NEXT_PROFILE,
                                                                 nextProfileIntent, 0);

        mNotificationBuilder = new NotificationCompat.Builder(mContext);
        mNotificationBuilder.setSmallIcon(smallIconResId)
            .setContentTitle(title)
            .setContentText(contentText)
            .setColor(color)
            .setContentIntent(settingsPI)
            .addAction(pauseOrResumeDrawableResId,
                       pauseOrResumeActionText,
                       pauseOrResumePI)
            .addAction(R.drawable.ic_next_profile,
                       ProfilesHelper.getProfileName(profilesModel, mSettingsModel.getProfile(), context),
                       nextProfilePI)
            .setPriority(Notification.PRIORITY_MIN);

        if (isPaused()) {
            Log.d(TAG, "Creating a dismissible notification");
            NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
        } else {
            Log.d(TAG, "Creating a persistent notification");
            mServiceController.startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
        }
    }

    public void onScreenFilterCommand(Intent command) {
        int commandFlag = mFilterCommandParser.parseCommandFlag(command);

        if (mShuttingDown) {
            Log.i(TAG, "In the process of shutting down; ignoring command: " + commandFlag);
            return;
        }

        if (DEBUG) Log.i(TAG, String.format("Handling command: %d in current state: %s",
                commandFlag, mCurrentState));

        mCurrentState.onScreenFilterCommand(commandFlag);
    }

    //region OnSettingsChangedListener
    @Override
    public void onShadesPauseStateChanged(boolean pauseState) {
        //Broadcast to keep appwidgets in sync
        if(DEBUG) Log.i(TAG, "Sending update broadcast");
        Intent updateAppWidgetIntent = new Intent(mContext, SwitchAppWidgetProvider.class);
        updateAppWidgetIntent.setAction(SwitchAppWidgetProvider.ACTION_UPDATE);
        updateAppWidgetIntent.putExtra(SwitchAppWidgetProvider.EXTRA_POWER, !pauseState);
        mContext.sendBroadcast(updateAppWidgetIntent);
    }

    @Override
    public void onShadesDimLevelChanged(int dimLevel) {
        if (!isPaused() || isPreviewing()) {
            cancelRunningAnimator(mDimAnimator);

            mView.setFilterDimLevel(dimLevel);
        }
    }

    @Override
    public void onShadesIntensityLevelChanged(int intensityLevel) {
        if (!isPaused() || isPreviewing()) {
            cancelRunningAnimator(mIntensityAnimator);

            mView.setFilterIntensityLevel(intensityLevel);
        }
    }

    @Override
    public void onShadesColorChanged(int color) {
        if (!isPaused() || isPreviewing()) {
            mView.setColorTempProgress(color);
        }
    }

    @Override
    public void onShadesAutomaticFilterModeChanged(String automaticFilterMode) { }

    @Override
    public void onShadesAutomaticTurnOnChanged(String turnOnTime) { }

    @Override
    public void onShadesAutomaticTurnOffChanged(String turnOffTime) { }

    @Override
    public void onLowerBrightnessChanged(boolean lowerBrightness) {
        if (DEBUG) Log.i(TAG, "Lower brightness flag changed to: " + lowerBrightness);
        if (!isPaused()) {
            if (lowerBrightness) {
                saveOldBrightnessState();
                setBrightnessState(0, false, mContext);
            } else {
                restoreBrightnessState();
            }
        }
    }

    @Override
    public void onProfileChanged(int profile) {
        refreshForegroundNotification();
    }

    @Override
    public void onAutomaticSuspendChanged(boolean automaticSuspend) {
        if (mCurrentState == mOnState) {
            if (automaticSuspend) {
                startAppMonitoring();
            } else {
                stopAppMonitoring();
            }
        }
    }

    private void animateShadesColor(int toColor) {
        cancelRunningAnimator(mColorAnimator);

        int fromColor = mView.getColorTempProgress();

        mColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        mColorAnimator.setDuration(FADE_DURATION_MS);
        mColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mView.setColorTempProgress((Integer) valueAnimator.getAnimatedValue());
            }
        });

        mColorAnimator.start();
    }

    private void animateDimLevel(int toDimLevel, Animator.AnimatorListener listener) {
        cancelRunningAnimator(mDimAnimator);

        int fromDimLevel = mView.getFilterDimLevel();

        mDimAnimator = ValueAnimator.ofInt(fromDimLevel, toDimLevel);
        mDimAnimator.setDuration(FADE_DURATION_MS);
        mDimAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mView.setFilterDimLevel((Integer) valueAnimator.getAnimatedValue());
            }
        });

        if (listener != null) {
            mDimAnimator.addListener(listener);
        }

        mDimAnimator.start();
    }

    private void animateIntensityLevel(int toIntensityLevel, Animator.AnimatorListener listener) {
        cancelRunningAnimator(mIntensityAnimator);

        int fromIntensityLevel = mView.getFilterIntensityLevel();

        mIntensityAnimator = ValueAnimator.ofInt(fromIntensityLevel, toIntensityLevel);
        mIntensityAnimator.setDuration(FADE_DURATION_MS);
        mIntensityAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mView.setFilterIntensityLevel((Integer) valueAnimator.getAnimatedValue());
                }
            });

        if (listener != null) {
            mIntensityAnimator.addListener(listener);
        }

        mIntensityAnimator.start();
    }

    private boolean isPaused() {
        return mCurrentState.isPaused();
    }

    private boolean isPreviewing() {
        return mCurrentState == mPreviewState;
    }

    private void cancelRunningAnimator(Animator animator) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }
    //endregion

    //region OnOrientationChangeListener
    public void onPortraitOrientation() {
        reLayoutScreenFilter();
    }

    public void onLandscapeOrientation() {
        reLayoutScreenFilter();
    }
    //endregion

    private void saveOldBrightnessState() {
        if (mSettingsModel.getBrightnessControlFlag()) {
            ContentResolver resolver = mContext.getContentResolver();
            try {
                oldScreenBrightness = Settings.System.getInt
                    (resolver, Settings.System.SCREEN_BRIGHTNESS);
                oldIsAutomaticBrightness = 1 == Settings.System.getInt(resolver, "screen_brightness_mode");
            } catch (SettingNotFoundException e) {
                Log.e(TAG, "Error reading brightness state", e);
                oldIsAutomaticBrightness = false;
            }
        } else {
            oldScreenBrightness = -1;
        }
        mSettingsModel.setBrightnessAutomatic(oldIsAutomaticBrightness);
        mSettingsModel.setBrightnessLevel(oldScreenBrightness);
    }

    // Statically used by BootReceiver
    public static void setBrightnessState(int brightness, boolean automatic, Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23 &&
            !Settings.System.canWrite(context)) return;
        if (DEBUG) Log.i(TAG, "Setting brightness to: " + brightness + ", automatic: " + automatic);
        if (brightness >= 0) {
            ContentResolver resolver = context.getContentResolver();
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
            Settings.System.putInt(resolver, "screen_brightness_mode", (automatic ? 1 : 0));
        }
    }

    private void restoreBrightnessState() {
        setBrightnessState(mSettingsModel.getBrightnessLevel(),
                           mSettingsModel.getBrightnessAutomatic(),
                           mContext);
    }

    public void startAppMonitoring() {
        if (DEBUG) Log.i(TAG, "Starting app monitoring");
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
            screenOff = !powerManager.isInteractive();
        } else {
            screenOff = !powerManager.isScreenOn();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiver(mScreenStateReceiver, filter);

        if (mCamThread == null && !screenOff) {
            mCamThread = new CurrentAppMonitoringThread(mContext);
            mCamThread.start();
        }
    }

    public void stopAppMonitoring() {
        if (DEBUG) Log.i(TAG, "Stopping app monitoring");
        if (mCamThread != null) {
            if (!mCamThread.interrupted()) {
                mCamThread.interrupt();
            }
            mCamThread = null;
        }

        try {
            mContext.unregisterReceiver(mScreenStateReceiver);
        } catch (IllegalArgumentException e) {
            // Catch errors when receiver is unregistered more than
            // once, it is not a problem, so we just ignore it.
        }
    }

    @Override
    public void onScreenTurnedOn() {
        if (DEBUG) Log.i(TAG, "Screen turn on received");
        screenOff = false;

        if (mCamThread == null) {
            mCamThread = new CurrentAppMonitoringThread(mContext);
            mCamThread.start();
        }
    }

    @Override
    public void onScreenTurnedOff() {
        if (DEBUG) Log.i(TAG, "Screen turn off received");
        screenOff = true;

        if (mCamThread != null) {
            if (!mCamThread.interrupted()) {
                mCamThread.interrupt();
            }
            mCamThread = null;
        }
    }

    private WindowManager.LayoutParams createFilterLayoutParams() {
        WindowManager.LayoutParams wlp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                mScreenManager.getScreenHeight(),
                0,
                -mScreenManager.getStatusBarHeightPx(),
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);

        wlp.gravity = Gravity.TOP | Gravity.START;
        wlp.buttonBrightness = mSettingsModel.getDimButtonsFlag() ? 0 : -1;

        return wlp;
    }

    private void openScreenFilter() {
        if (!mScreenFilterOpen) {
            // Display the transparent filter
            mWindowViewManager.openWindow(mView, createFilterLayoutParams());
            mScreenFilterOpen = true;
        }
    }

    private void reLayoutScreenFilter() {
        if (!mScreenFilterOpen) {
            return;
        }
        mWindowViewManager.reLayoutWindow(mView, createFilterLayoutParams());
    }

    private void closeScreenFilter() {
        if (!mScreenFilterOpen || !mCurrentState.isPaused()) {
            return;
        }

        // Close the window once the fade-out animation is complete
        mWindowViewManager.closeWindow(mView);
        mScreenFilterOpen = false;
    }

    private void moveToState(int commandFlag) {
        switch (commandFlag) {
            case ScreenFilterService.COMMAND_PAUSE:
                moveToState(mPauseState);
                break;

            case ScreenFilterService.COMMAND_ON:
                moveToState(mOnState);
                break;

            case ScreenFilterService.COMMAND_SHOW_PREVIEW:
                moveToState(mPreviewState);
                break;

            case ScreenFilterService.COMMAND_START_SUSPEND:
                moveToState(mSuspendState);
                break;
        }
    }

    private void moveToState(@NonNull State newState) {
        if (DEBUG) Log.i(TAG, String.format("Transitioning state from %s to %s",
                                                      mCurrentState, newState));
        if (newState == mCurrentState) return;

        State oldState = mCurrentState;
        mCurrentState = newState;

        mCurrentState.onActivation(oldState);
        mSettingsModel.setShadesPauseState(isPaused());
    }


    private abstract class State {
        protected abstract void onActivation(State prevState);
        protected abstract boolean isPaused();

        protected void onScreenFilterCommand(int commandFlag) {
            moveToState(commandFlag);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    private class OnState extends State {
        @Override
        protected void onActivation(State prevState) {
            refreshForegroundNotification();

            openScreenFilter();

            animateDimLevel(mSettingsModel.getShadesDimLevel(), null);
            animateIntensityLevel(mSettingsModel.getShadesIntensityLevel(), null);

            if (mSettingsModel.getBrightnessControlFlag()) {
                saveOldBrightnessState();
                setBrightnessState(0, false, mContext);
            }

            if (mSettingsModel.getAutomaticSuspend()) {
                startAppMonitoring();
            }
        }

        @Override
        protected boolean isPaused() { return false; }
    }

    private class PauseState extends State {
        @Override
        protected void onActivation(State prevState) {
            mServiceController.stopForeground(false);
            refreshForegroundNotification();

            if (prevState==mPreviewState) {
                mServiceController.stopForeground(false);
                closeScreenFilter();
            } else {
                animateIntensityLevel(ScreenFilterView.MIN_INTENSITY, null);
                animateDimLevel(ScreenFilterView.MIN_DIM, new AbstractAnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator animator) {
                        closeScreenFilter();
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        closeScreenFilter();
                    }
                });
            }

            if (mSettingsModel.getBrightnessControlFlag()) {
                restoreBrightnessState();
            }

            if (mSettingsModel.getAutomaticSuspend()) {
                stopAppMonitoring();
            }
        }

        @Override
        protected boolean isPaused() { return true; }
    }

    /* This State is used to present the filter to the user when (s)he
     * is holding one of the seekbars to adjust the filter. It turns
     * on the filter and saves what state it should be when it will be
     * turned off.
     */
    private class PreviewState extends State {
        public State stateToReturnTo;
        public int pressesActive;

        @Override
        protected void onActivation(State prevState) {
            stateToReturnTo = prevState;
            pressesActive = 1;
            refreshForegroundNotification();
            openScreenFilter();

            int dim = mSettingsModel.getShadesDimLevel();
            int intensity = mSettingsModel.getShadesIntensityLevel();
            int filterColor = mSettingsModel.getShadesColor();

            mView.setFilterDimLevel(dim);
            mView.setFilterIntensityLevel(intensity);
            mView.setColorTempProgress(filterColor);
        }

        @Override
        protected void onScreenFilterCommand(int commandFlag) {
            if (DEBUG) Log.d(TAG, String.format("Preview, got command: %d", commandFlag));
            switch (commandFlag) {
                case ScreenFilterService.COMMAND_ON:
                    stateToReturnTo = mOnState;
                    break;

                case ScreenFilterService.COMMAND_PAUSE:
                    if (DEBUG) Log.d(TAG, String.format("State to return to changed to %d while in preview mode", commandFlag));
                    stateToReturnTo = mPauseState;
                    break;

                case ScreenFilterService.COMMAND_SHOW_PREVIEW:
                    pressesActive++;
                    if (DEBUG) Log.d(TAG,
                            String.format("%d presses active", pressesActive));
                    break;

                case ScreenFilterService.COMMAND_HIDE_PREVIEW:
                    pressesActive--;
                    if (DEBUG) Log.d(TAG,
                            String.format("%d presses active", pressesActive));

                    if (pressesActive <= 0) {
                        if (DEBUG)
                            Log.d(TAG, String.format("Moving back to state %d", stateToReturnTo));
                        if (isPaused()) {
                            mServiceController.stopForeground(false);
                            closeScreenFilter();
                        }
                        moveToState(stateToReturnTo);
                    }
                    break;
            }
        }

        @Override
        protected boolean isPaused() { return stateToReturnTo.isPaused(); }
    }

    /* This state is used when the filter is suspended temporarily,
     * because the user is in an excluded app (for example the package
     * installer). It stops the filter like in the PauseState, but
     * doesn't change the UI, switch or brightness state just like the
     * PreviewState. Like the PreviewState, it logs changes to the
     * state and applies them when the suspend state is deactivated.
     */
    private class SuspendState extends State {
        public State stateToReturnTo;

        @Override
        protected void onActivation(State prevState) {
            stateToReturnTo = prevState;
            mServiceController.stopForeground(false);
            closeScreenFilter();
            refreshForegroundNotification();
        }

        @Override
        public void onScreenFilterCommand(int commandFlag) {
            if (DEBUG) Log.d(TAG, String.format("Suspend, got command: %d", commandFlag));
            switch (commandFlag) {
                case ScreenFilterService.COMMAND_STOP_SUSPEND:
                    moveToState(stateToReturnTo);
                    break;
                case ScreenFilterService.COMMAND_PAUSE:
                    moveToState(commandFlag);
                    break;
                case ScreenFilterService.COMMAND_ON:
                    // Suspended is a subset of on, so there is nothing to do
                case ScreenFilterService.COMMAND_SHOW_PREVIEW:
                case ScreenFilterService.COMMAND_HIDE_PREVIEW:
                    // Preview is ignored when the filter is suspended
                    break;
            }
        }

        @Override
        protected boolean isPaused() { return stateToReturnTo.isPaused(); }
    }
}
