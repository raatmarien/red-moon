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
package com.jmstudios.redmoon.model;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.preference.ColorSeekBarPreference;
import com.jmstudios.redmoon.preference.DimSeekBarPreference;
import com.jmstudios.redmoon.preference.IntensitySeekBarPreference;

/**
 * This class provides access to get and set Shades settings, and also listen to settings changes.
 *
 * <p>In order to listen to settings changes, invoke
 * {@link SettingsModel#addOnSettingsChangedListener(OnSettingsChangedListener)} and
 * {@link SettingsModel#openSettingsChangeListener()}.
 *
 * <p><b>You must call {@link SettingsModel#closeSettingsChangeListener()} when you are done
 * listening to changes.</b>
 *
 * <p>To begin listening again, invoke {@link SettingsModel#openSettingsChangeListener()}.
 */
public class SettingsModel implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsModel";
    private static final boolean DEBUG = false;

    private SharedPreferences mSharedPreferences;
    private ArrayList<OnSettingsChangedListener> mSettingsChangedListeners;

    private String mPauseStatePrefKey;
    private String mDimPrefKey;
    private String mIntensityPrefKey;
    private String mColorPrefKey;
    private String mOpenOnBootPrefKey;
    private String mKeepRunningAfterRebootPrefKey;
    private String mDarkThemePrefKey;
    private String mBrightnessControlPrefKey;
    private String mAutomaticFilterModePrefKey;
    private String mAutomaticTurnOnPrefKey;
    private String mAutomaticTurnOffPrefKey;
    private String mDimButtonsPrefKey;
    private String mBrightnessAutomaticPrefKey;
    private String mBrightnessLevelPrefKey;
    private String mProfilePrefKey;
    private String mAmmountProfilesPrefKey;
    private String mIntroShownPrefKey;
    private String mAutomaticSuspendPrefKey;

    public SettingsModel(@NonNull Resources resources, @NonNull SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
        mSettingsChangedListeners = new ArrayList<OnSettingsChangedListener>();

        mPauseStatePrefKey = resources.getString(R.string.pref_key_shades_pause_state);
        mDimPrefKey = resources.getString(R.string.pref_key_shades_dim_level);
        mIntensityPrefKey = resources.getString(R.string.pref_key_shades_intensity_level);
        mColorPrefKey = resources.getString(R.string.pref_key_shades_color_temp);
        mOpenOnBootPrefKey = resources.getString(R.string.pref_key_always_open_on_startup);
        mKeepRunningAfterRebootPrefKey = resources.getString(R.string.pref_key_keep_running_after_reboot);
        mDarkThemePrefKey = resources.getString(R.string.pref_key_dark_theme);
        mBrightnessControlPrefKey = resources.getString(R.string.pref_key_control_brightness);
        mAutomaticFilterModePrefKey = resources.getString(R.string.pref_key_automatic_filter);
        mAutomaticTurnOnPrefKey = resources.getString(R.string.pref_key_custom_start_time);
        mAutomaticTurnOffPrefKey = resources.getString(R.string.pref_key_custom_end_time);
        mDimButtonsPrefKey = resources.getString(R.string.pref_key_dim_buttons);
        mBrightnessAutomaticPrefKey = resources.getString(R.string.pref_key_brightness_automatic);
        mBrightnessLevelPrefKey = resources.getString(R.string.pref_key_brightness_level);
        mProfilePrefKey = resources.getString(R.string.pref_key_profile_spinner);
        mAmmountProfilesPrefKey = resources.getString(R.string.pref_key_ammount_profiles);
        mIntroShownPrefKey = resources.getString(R.string.pref_key_intro_shown);
        mAutomaticSuspendPrefKey = resources.getString(R.string.pref_key_automatic_suspend);
    }

    public boolean getShadesPauseState() {
        return mSharedPreferences.getBoolean(mPauseStatePrefKey, false);
    }

    public void setShadesPauseState(boolean state) {
        mSharedPreferences.edit().putBoolean(mPauseStatePrefKey, state).apply();
    }

    public void setBrightnessAutomatic(boolean automatic) {
        mSharedPreferences.edit().putBoolean(mBrightnessAutomaticPrefKey, automatic).apply();
    }

    public void setBrightnessLevel(int level) {
        mSharedPreferences.edit().putInt(mBrightnessLevelPrefKey, level).apply();
    }

    public void setProfile(int profile) {
        mSharedPreferences.edit().putInt(mProfilePrefKey, profile).apply();
    }

    public void setAmmountProfiles(int ammountProfiles) {
        mSharedPreferences.edit().putInt(mAmmountProfilesPrefKey, ammountProfiles).apply();
    }

    public void setShadesDimLevel(int dimLevel) {
        mSharedPreferences.edit().putInt(mDimPrefKey, dimLevel).apply();
    }

    public void setShadesIntensityLevel(int intensityLevel) {
        mSharedPreferences.edit().putInt(mIntensityPrefKey, intensityLevel).apply();
    }

    public void setShadesColor(int color) {
        mSharedPreferences.edit().putInt(mColorPrefKey, color).apply();
    }

    public void setIntroShown(boolean shown) {
        mSharedPreferences.edit().putBoolean(mIntroShownPrefKey, shown).apply();
    }

    public int getShadesDimLevel() {
        return mSharedPreferences.getInt(mDimPrefKey, DimSeekBarPreference.DEFAULT_VALUE);
    }

    public int getShadesIntensityLevel() {
        return mSharedPreferences.getInt(mIntensityPrefKey, IntensitySeekBarPreference.DEFAULT_VALUE);
    }

    public int getShadesColor() {
        return mSharedPreferences.getInt(mColorPrefKey, ColorSeekBarPreference.DEFAULT_VALUE);
    }

    public boolean getOpenOnBootFlag() {
        return mSharedPreferences.getBoolean(mOpenOnBootPrefKey, false);
    }

    public boolean getResumeAfterRebootFlag() {
        return mSharedPreferences.getBoolean(mKeepRunningAfterRebootPrefKey, false);
    }

    public boolean getDarkThemeFlag() {
        return mSharedPreferences.getBoolean(mDarkThemePrefKey, false);
    }

    public boolean getBrightnessControlFlag() {
        return mSharedPreferences.getBoolean(mBrightnessControlPrefKey, false);
    }

    public String getAutomaticFilterMode() {
        return mSharedPreferences.getString(mAutomaticFilterModePrefKey, "never");
    }

    public String getAutomaticTurnOnTime() {
        return mSharedPreferences.getString(mAutomaticTurnOnPrefKey, "22:00");
    }

    public String getAutomaticTurnOffTime() {
        return mSharedPreferences.getString(mAutomaticTurnOffPrefKey, "06:00");
    }

    public boolean getDimButtonsFlag() {
        return mSharedPreferences.getBoolean(mDimButtonsPrefKey, true);
    }

    public boolean getBrightnessAutomatic() {
        return mSharedPreferences.getBoolean(mBrightnessAutomaticPrefKey, true);
    }

    public int getBrightnessLevel() {
        return mSharedPreferences.getInt(mBrightnessLevelPrefKey, 0);
    }

    public int getProfile() {
        return mSharedPreferences.getInt(mProfilePrefKey, 1);
    }

    public int getAmmountProfiles() {
        return mSharedPreferences.getInt(mAmmountProfilesPrefKey, 3);
    }

    public boolean getIntroShown() {
        return mSharedPreferences.getBoolean(mIntroShownPrefKey, false);
    }

    public boolean getAutomaticSuspend() {
        return mSharedPreferences.getBoolean(mAutomaticSuspendPrefKey, false);
    }

    public void addOnSettingsChangedListener(OnSettingsChangedListener listener) {
        mSettingsChangedListeners.add(listener);
    }

    public void openSettingsChangeListener() {
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (DEBUG) Log.d(TAG, "Opened Settings change listener");
    }

    public void closeSettingsChangeListener() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        if (DEBUG) Log.d(TAG, "Closed Settings change listener");
    }

    //region OnSharedPreferenceChangeListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        for (OnSettingsChangedListener mSettingsChangedListener : mSettingsChangedListeners)
            if (mSettingsChangedListener == null) {
                mSettingsChangedListeners.remove(mSettingsChangedListeners.indexOf(mSettingsChangedListener));
            }

        if (key.equals(mPauseStatePrefKey))
        {
            boolean pauseState = getShadesPauseState();
            for (OnSettingsChangedListener mSettingsChangedListener : mSettingsChangedListeners)
                mSettingsChangedListener.onShadesPauseStateChanged(pauseState);
        }
        else if (key.equals(mDimPrefKey))
        {
            int dimLevel = getShadesDimLevel();
            for (OnSettingsChangedListener mSettingsChangedListener : mSettingsChangedListeners)
                mSettingsChangedListener.onShadesDimLevelChanged(dimLevel);
        }
        else if (key.equals(mIntensityPrefKey))
        {
            int intensityLevel = getShadesIntensityLevel();
            for (OnSettingsChangedListener mSettingsChangedListener : mSettingsChangedListeners)
                 mSettingsChangedListener.onShadesIntensityLevelChanged(intensityLevel);
        }
        else if (key.equals(mColorPrefKey))
        {
            int color = getShadesColor();
            for (OnSettingsChangedListener mSettingsChangedListener : mSettingsChangedListeners)
                mSettingsChangedListener.onShadesColorChanged(color);
        }
        else if (key.equals(mAutomaticFilterModePrefKey))
            {
            String automaticFilterMode = getAutomaticFilterMode();
            for (OnSettingsChangedListener mSettingsChangedListener : mSettingsChangedListeners) {
                mSettingsChangedListener.onShadesAutomaticFilterModeChanged(automaticFilterMode);
            }
        }
        else if (key.equals(mAutomaticTurnOnPrefKey))
            {
            String turnOnTime = getAutomaticTurnOnTime();
            for (OnSettingsChangedListener mSettingsChangedListener : mSettingsChangedListeners)
                mSettingsChangedListener.onShadesAutomaticTurnOnChanged(turnOnTime);
        }
        else if (key.equals(mAutomaticTurnOffPrefKey))
            {
            String turnOffTime = getAutomaticTurnOffTime();
            for (OnSettingsChangedListener mSettingsChangedListener : mSettingsChangedListeners)
                mSettingsChangedListener.onShadesAutomaticTurnOffChanged(turnOffTime);
        } else if (key.equals(mBrightnessControlPrefKey)) {
            boolean brightnessControlFlag = getBrightnessControlFlag();
            for (OnSettingsChangedListener mSettingsChangedListener : mSettingsChangedListeners)
                mSettingsChangedListener.onLowerBrightnessChanged(brightnessControlFlag);
        } else if (key.equals(mProfilePrefKey)) {
            int profile = getProfile();
            for (OnSettingsChangedListener mSettingsChangedListener : mSettingsChangedListeners)
                mSettingsChangedListener.onProfileChanged(profile);
        } else if (key.equals(mAutomaticSuspendPrefKey)) {
            boolean automaticSuspend = getAutomaticSuspend();
            for (OnSettingsChangedListener mSettingsChangedListener : mSettingsChangedListeners)
                mSettingsChangedListener.onAutomaticSuspendChanged(automaticSuspend);
        }
    }
    //endregion

    public interface OnSettingsChangedListener {
        void onShadesPauseStateChanged(boolean pauseState);
        void onShadesDimLevelChanged(int dimLevel);
        void onShadesIntensityLevelChanged(int intensityLevel);
        void onShadesColorChanged(int color);
        void onShadesAutomaticFilterModeChanged(String automaticFilterMode);
        void onShadesAutomaticTurnOnChanged(String turnOnTime);
        void onShadesAutomaticTurnOffChanged(String turnOffTime);
        void onLowerBrightnessChanged(boolean lowerBrightness);
        void onProfileChanged(int profile);
        void onAutomaticSuspendChanged(boolean automaticSuspend);
    }
}
