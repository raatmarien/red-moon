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
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;

import com.jmstudios.redmoon.R;

public class TimePickerPreference extends DialogPreference {
    public static final String DEFAULT_VALUE = "00:00";

    private static final String TAG = "TimePickerPref";
    private static final boolean DEBUG = true;

    private TimePicker mTimePicker;
    protected String mTime;

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText(getContext().getResources().getString
                              (R.string.set_dialog));
        setNegativeButtonText(getContext().getResources().getString
                              (R.string.cancel_dialog));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mTime = getPersistedString(DEFAULT_VALUE);
        } else {
            mTime = (String) defaultValue;
            persistString(mTime);
        }
        setSummary(mTime);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
    }

    @Override
    protected View onCreateDialogView() {
        mTimePicker = new TimePicker(getContext());
        mTimePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
        return mTimePicker;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        int hour = Integer.parseInt(mTime.split(":")[0]);
        int minute = Integer.parseInt(mTime.split(":")[1]);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            mTimePicker.setHour(hour);
            mTimePicker.setMinute(minute);
        } else {
            mTimePicker.setCurrentHour(hour);
            mTimePicker.setCurrentMinute(minute);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            int hour = 0, minute = 0;
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                hour = mTimePicker.getHour();
                minute = mTimePicker.getMinute();
            } else {
                hour = mTimePicker.getCurrentHour();
                minute = mTimePicker.getCurrentMinute();
            }

            mTime = (hour < 10 ? "0" : "") + Integer.toString(hour) + ":" +
                (minute < 10 ? "0" : "") + Integer.toString(minute);
            persistString(mTime);
        }
        setSummary(mTime);
    }
}
