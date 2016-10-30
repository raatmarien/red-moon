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

import android.content.Intent;
import android.support.annotation.NonNull;
import android.content.Context;
import android.util.Log;

import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.activity.ShadesActivity;
import com.jmstudios.redmoon.fragment.ShadesFragment;
import com.jmstudios.redmoon.helper.FilterCommandFactory;
import com.jmstudios.redmoon.helper.FilterCommandSender;
import com.jmstudios.redmoon.model.SettingsModel;
import com.jmstudios.redmoon.service.ScreenFilterService;
import com.jmstudios.redmoon.receiver.AutomaticFilterChangeReceiver;

import com.jmstudios.redmoon.preference.ColorSeekBarPreference;
import com.jmstudios.redmoon.preference.IntensitySeekBarPreference;
import com.jmstudios.redmoon.preference.DimSeekBarPreference;

public class ShadesPresenter implements SettingsModel.OnSettingsChangedListener {
    private static final String TAG = "ShadesPresenter";
    private static final boolean DEBUG = false;

    private ShadesFragment mView;
    private SettingsModel mSettingsModel;
    private FilterCommandFactory mFilterCommandFactory;
    private FilterCommandSender mFilterCommandSender;
    private Context mContext;
    private ShadesActivity mActivity;

    public ShadesPresenter(@NonNull ShadesFragment view,
                           @NonNull SettingsModel settingsModel,
                           @NonNull FilterCommandFactory filterCommandFactory,
                           @NonNull FilterCommandSender filterCommandSender,
                           @NonNull Context context) {
        mView = view;
        mSettingsModel = settingsModel;
        mFilterCommandFactory = filterCommandFactory;
        mFilterCommandSender = filterCommandSender;
        mContext = context;
        mActivity = (ShadesActivity) context;
    }

    public void onStart() {
        boolean paused = mSettingsModel.getShadesPauseState();
        mActivity.setSwitch(!paused);
    }

    //region OnSettingsChangedListener
    @Override
    public void onShadesPauseStateChanged(boolean pauseState) {
        mActivity.setSwitch(!pauseState);
        if (!pauseState) {
            mActivity.displayInstallWarningToast();
        }
    }

    @Override
    public void onShadesDimLevelChanged(int dimLevel) {
        DimSeekBarPreference pref = (DimSeekBarPreference) mView.getPreferenceScreen()
            .findPreference(mContext.getString(R.string.pref_key_shades_dim_level));
        pref.setProgress(dimLevel);
    }

    @Override
    public void onShadesIntensityLevelChanged(int intensityLevel) {
        IntensitySeekBarPreference pref = (IntensitySeekBarPreference) mView.getPreferenceScreen()
            .findPreference(mContext.getString(R.string.pref_key_shades_intensity_level));
        pref.setProgress(intensityLevel);
    }

    @Override
    public void onShadesColorChanged(int color) {
        ColorSeekBarPreference pref = (ColorSeekBarPreference) mView.getPreferenceScreen()
            .findPreference(mContext.getString(R.string.pref_key_shades_color_temp));
        pref.setProgress(color);
    }

    @Override
    public void onShadesAutomaticFilterModeChanged(String automaticFilterMode) {
        if (DEBUG) Log.i(TAG, "Filter mode changed to " + automaticFilterMode);
        if (!automaticFilterMode.equals("never")) {
            AutomaticFilterChangeReceiver.cancelAlarms(mContext);
            AutomaticFilterChangeReceiver.scheduleNextOnCommand(mContext);
            AutomaticFilterChangeReceiver.scheduleNextPauseCommand(mContext);
        } else {
            AutomaticFilterChangeReceiver.cancelAlarms(mContext);
        }
    }

    @Override
    public void onShadesAutomaticTurnOnChanged(String turnOnTime) {
        AutomaticFilterChangeReceiver.cancelTurnOnAlarm(mContext);
        AutomaticFilterChangeReceiver.scheduleNextOnCommand(mContext);
    }

    @Override
    public void onShadesAutomaticTurnOffChanged(String turnOffTime) {
        AutomaticFilterChangeReceiver.cancelPauseAlarm(mContext);
        AutomaticFilterChangeReceiver.scheduleNextPauseCommand(mContext);
    }

    @Override
    public void onLowerBrightnessChanged(boolean lowerBrightness) { }

    @Override
    public void onProfileChanged(int profile) { }

    @Override
    public void onAutomaticSuspendChanged(boolean automaticSuspend) { }
    //endregion
}
