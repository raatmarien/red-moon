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
package com.jmstudios.redmoon.preference;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff.Mode;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.ImageView;
import android.widget.TextView;

import com.jmstudios.redmoon.helper.FilterCommandFactory;
import com.jmstudios.redmoon.helper.FilterCommandSender;
import com.jmstudios.redmoon.service.ScreenFilterService;
import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.view.ScreenFilterView;

public class DimSeekBarPreference extends Preference {
    public static final int DEFAULT_VALUE = 50;
    private static final String TAG = "DimSeekBarPreference";

    public SeekBar mDimLevelSeekBar;
    private int mDimLevel;
    private View mView;
    private FilterCommandSender mCommandSender;
    private FilterCommandFactory mCommandFactory;

    public DimSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_dim_seekbar);
    }

    public void setProgress(int progress) {
        if (mDimLevelSeekBar != null) {
            mDimLevelSeekBar.setProgress(progress);
        } else {
            mDimLevel = progress;
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mDimLevel = getPersistedInt(DEFAULT_VALUE);
        } else {
            mDimLevel = (Integer) defaultValue;
            persistInt(mDimLevel);
        }
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        mView = view;

        mDimLevelSeekBar = (SeekBar) view.findViewById(R.id.dim_level_seekbar);
        initLayout();
    }

    private void initLayout() {
        mCommandSender = new FilterCommandSender(mView.getContext());
        mCommandFactory = new FilterCommandFactory(mView.getContext());
        mDimLevelSeekBar.setProgress(mDimLevel);

        mDimLevelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDimLevel = progress;
                persistInt(mDimLevel);

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

    private void updateMoonIconColor() {
        if (!isEnabled()) return;

        int lightness = 102 + (int) (((float) (100 - mDimLevel)) * (2.55f * 0.6f));
        int color = Color.rgb(lightness, lightness, lightness);

        ImageView moonIcon = (ImageView) mView.findViewById(R.id.moon_icon_dim);

        PorterDuffColorFilter colorFilter
            = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);

        moonIcon.setColorFilter(colorFilter);
    }

    private void updateProgressText() {
        String progress = Integer.toString((int) (((float) mDimLevel) * ScreenFilterView.DIM_MAX_ALPHA));
        String suffix = "%";

        TextView progressText = (TextView) mView.findViewById(R.id.current_dim_level);
        progressText.setText(progress + suffix);
    }
}
