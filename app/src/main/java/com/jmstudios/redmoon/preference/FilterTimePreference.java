/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
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
 */
package com.jmstudios.redmoon.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.content.SharedPreferences;

import java.util.TimeZone;
import java.util.Calendar;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

public class FilterTimePreference extends TimePickerPreference {
    public static final String TAG = "FilterTimePreference";
    public static final boolean DEBUG = true;

    private boolean mIsCustom = true;

    public FilterTimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setToSunTime(String time) {
        if (mIsCustom) {
            // Backup custom times
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putString(getKey() + "_custom", mTime);
            editor.commit();
        }
        mTime = time;
        persistString(mTime);
        setSummary(mTime);

        mIsCustom = false;
    }

    public void setToCustomTime() {
        mIsCustom = true;

        mTime = getSharedPreferences().getString(getKey() + "_custom", DEFAULT_VALUE);
        persistString(mTime);
        setSummary(mTime);
    }

    public static String getSunTimeFromLocation(android.location.Location location,
                                                boolean sunset) {
        com.luckycatlabs.sunrisesunset.dto.Location sunriseSunsetLocation =
            new com.luckycatlabs.sunrisesunset.dto.Location(location.getLatitude(),
                                                            location.getLongitude());
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator
            (sunriseSunsetLocation, TimeZone.getDefault());
        if (sunset) {
            return calculator.getOfficialSunsetForDate(Calendar.getInstance());
        } else {
            return calculator.getOfficialSunriseForDate(Calendar.getInstance());
        }
    }
}
