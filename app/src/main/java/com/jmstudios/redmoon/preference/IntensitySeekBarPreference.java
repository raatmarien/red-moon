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
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jmstudios.redmoon.helper.FilterCommandFactory;
import com.jmstudios.redmoon.helper.FilterCommandSender;
import com.jmstudios.redmoon.service.ScreenFilterService;
import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.view.ScreenFilterView;
import com.jmstudios.redmoon.activity.ShadesActivity;

public class IntensitySeekBarPreference extends Preference {
    public static final int DEFAULT_VALUE = 50;
    private static final String TAG = "IntensitySeekBarPreference";

    public SeekBar mIntensityLevelSeekBar;
    private int mIntensityLevel;
    private View mView;
    private FilterCommandSender mCommandSender;
    private FilterCommandFactory mCommandFactory;

    public IntensitySeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutResource(R.layout.preference_intensity_seekbar);
    }

    public void setProgress(int progress) {
        if (mIntensityLevelSeekBar != null) {
            mIntensityLevelSeekBar.setProgress(progress);
        } else {
            mIntensityLevel = progress;
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mIntensityLevel = getPersistedInt(DEFAULT_VALUE);
        } else {
            mIntensityLevel = (Integer) defaultValue;
            persistInt(mIntensityLevel);
        }
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        mView = view;

        mIntensityLevelSeekBar = (SeekBar) view.findViewById(R.id.intensity_level_seekbar);
        initLayout();
    }

    private void initLayout() {
        mCommandSender = new FilterCommandSender(mView.getContext());
        mCommandFactory = new FilterCommandFactory(mView.getContext());
        mIntensityLevelSeekBar.setProgress(mIntensityLevel);

        mIntensityLevelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mIntensityLevel = progress;
                persistInt(mIntensityLevel);

                updateMoonIconColor();
                updateProgressText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "Touch down on a seek bar");

                Intent showPreviewCommand = mCommandFactory.createCommand
                    (ScreenFilterService.COMMAND_SHOW_PREVIEW);
                mCommandSender.send(showPreviewCommand);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "Released a seek bar");

                Intent hidePreviewCommand = mCommandFactory.createCommand
                    (ScreenFilterService.COMMAND_HIDE_PREVIEW);
                mCommandSender.send(hidePreviewCommand);
            }
        });

        updateMoonIconColor();
        updateProgressText();
    }

    public void updateMoonIconColor() {
        if (!isEnabled()) return;

        int colorTempProgress = ((ShadesActivity) getContext()).getColorTempProgress();

        int color = ScreenFilterView.getIntensityColor(mIntensityLevel, colorTempProgress);

        ImageView moonIcon = (ImageView) mView.findViewById(R.id.moon_icon_intensity);

        PorterDuffColorFilter colorFilter
            = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);

        moonIcon.setColorFilter(colorFilter);
    }

    private void updateProgressText() {
        String progress = Integer.toString(mIntensityLevel);
        String suffix = "%";

        TextView progressText = (TextView) mView.findViewById(R.id.current_intensity_level);
        progressText.setText(progress + suffix);
    }
}
