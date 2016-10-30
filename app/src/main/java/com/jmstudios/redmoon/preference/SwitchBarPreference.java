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

import android.preference.Preference;
import android.util.AttributeSet;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.CompoundButton;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.app.Activity;
import android.net.Uri;

import android.widget.Switch;

import com.jmstudios.redmoon.thread.CurrentAppMonitoringThread;
import com.jmstudios.redmoon.activity.AutomaticSuspendPreferenceActivity;
import com.jmstudios.redmoon.R;

public class SwitchBarPreference extends Preference
    implements CompoundButton.OnCheckedChangeListener,
               View.OnClickListener {
    public static final boolean DEFAULT_VALUE = false;

    private static final String TAG = "SwitchBarPreference";
    private static final boolean DEBUG = false;

    private boolean mValue;

    private Context mContext;
    private Activity mActivity;
    private TextView mTextView;
    private Switch mSwitch;

    public SwitchBarPreference(Context context, Activity activity) {
        super(context);
        setLayoutResource(R.layout.switch_bar);

        mContext = context;
        mActivity = activity;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getBoolean(index, DEFAULT_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mValue = getPersistedBoolean(DEFAULT_VALUE);
        } else {
            mValue = (Boolean) defaultValue;
            persistBoolean(mValue);
        }
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        mTextView = (TextView) view.findViewById(R.id.switch_text);
        mTextView.setText(mValue ? R.string.text_switch_on : R.string.text_switch_off);

        FrameLayout bar = (FrameLayout) view.findViewById(R.id.frame_layout_switch_bar);
        bar.setBackgroundColor(mContext.getResources().getColor(R.color.switch_bar_background));
        bar.setOnClickListener(this);

        mSwitch = (Switch) view.findViewById(R.id.switch_bar_switch);
        mSwitch.setOnCheckedChangeListener(this);
        mSwitch.setChecked(mValue);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && !CurrentAppMonitoringThread
            .isAppMonitoringWorking(mContext)) {
            createEnableUsageStatsDialog();

            buttonView.setChecked(false);
            mValue = false;
        } else {
            mValue = isChecked;
        }

        persistBoolean(mValue);

        mTextView.setText(mValue ? R.string.text_switch_on : R.string.text_switch_off);
    }

    @Override
    public void onClick(View v) {
        mSwitch.toggle();
    }

    public void usageStatsPermissionAttempted() {
        if (CurrentAppMonitoringThread.isAppMonitoringWorking(mContext)) {
            mSwitch.setChecked(true);
        }
    }

    private void createEnableUsageStatsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setMessage(R.string.usage_stats_dialog_message)
            .setTitle(R.string.usage_stats_dialog_title)
            .setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        mActivity.startActivityForResult
                            (intent, AutomaticSuspendPreferenceActivity.RESULT_USAGE_ACCESS);
                    }
                });

        builder.show();
    }
}
