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
import android.widget.Spinner;
import android.widget.Button;
import android.view.View;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.content.Context;
import android.content.res.TypedArray;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.text.InputType;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.model.ProfilesModel;
import com.jmstudios.redmoon.model.SettingsModel;
import com.jmstudios.redmoon.activity.ShadesActivity;
import com.jmstudios.redmoon.fragment.ShadesFragment;
import com.jmstudios.redmoon.preference.ColorSeekBarPreference;
import com.jmstudios.redmoon.model.SettingsModel;
import com.jmstudios.redmoon.helper.ProfilesHelper;

public class ProfileSelectorPreference extends Preference
    implements OnItemSelectedListener {
    public static final int DEFAULT_VALUE = 1;

    private static final String TAG = "ProfileSelectorPref";
    private static final boolean DEBUG = false;

    public static final int DEFAULT_OPERATIONS_AM = 3;

    private Spinner mProfileSpinner;
    private Button mProfileActionButton;
    ArrayAdapter<CharSequence> mArrayAdapter;
    private int mProfile;
    private View mView;
    private ProfilesModel mProfilesModel;
    private Context mContext;

    private ArrayList<CharSequence> mDefaultOperations;

    private int currentColor;
    private int currentIntensity;
    private int currentDim;

    private boolean mIsListenerRegistered;

    // Settings model from the activity to save the ammount of profiles
    private SettingsModel mSettingsModel;

    public ProfileSelectorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_profile_selector);

        currentColor = currentDim = currentIntensity = 0;

        mProfilesModel = new ProfilesModel(context);

        mIsListenerRegistered = false;

        mSettingsModel = ((ShadesActivity) getContext()).getSettingsModel();

        mContext = context;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            mProfile = getPersistedInt(DEFAULT_VALUE);
        } else {
            mProfile = (Integer) defaultValue;
            persistInt(mProfile);
        }
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);

        mView = view;

        mProfileSpinner = (Spinner) view.findViewById(R.id.profile_spinner);
        mProfileActionButton = (Button) view.findViewById(R.id.profile_action_button);

        initLayout();

        updateButtonSetup();

        addSettingsChangedListener();
    }

    private void initLayout() {
        if (DEBUG) Log.i(TAG, "Starting initLayout");
        // The default operations first need to be converted to an ArrayList,
        // because the ArrayAdapter will turn it into an AbstractList otherwise,
        // which doesn't support certain actions, like adding elements.
        // See: http://stackoverflow.com/a/3200631
        mDefaultOperations = new ArrayList<CharSequence>
            (Arrays.asList(getContext().getResources().getStringArray(R.array.standard_profiles_array)));
        mArrayAdapter = new ArrayAdapter<CharSequence>
            (getContext(), android.R.layout.simple_spinner_item, mDefaultOperations);
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        readProfiles();

        mProfileSpinner.setAdapter(mArrayAdapter);
        mProfileSpinner.setSelection(mProfile);
        mProfileSpinner.setOnItemSelectedListener(this);
    }

    private void updateButtonSetup() {
        if (mProfile > (DEFAULT_OPERATIONS_AM - 1)) {
            if (DEBUG) Log.i(TAG, "Setting remove button");
            mProfileActionButton.setText(getContext().getResources().getString
                                         (R.string.button_remove_profile));
            mProfileActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openRemoveProfileDialog();
                    }
                });

        } else {
            if (DEBUG) Log.i(TAG, "Setting add button");
            mProfileActionButton.setText(getContext().getResources().getString
                                         (R.string.button_add_profile));
            mProfileActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openAddNewProfileDialog();
                    }
                });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        if (DEBUG) Log.i(TAG, "Item " + pos + " selected");
        mProfile = pos;
        persistInt(mProfile);
        updateButtonSetup();

        // Update the dependent settings
        if (mProfile != 0) {
            // We need a ProfilesModel to get the properties of the
            // profile from the index
            ProfilesModel profilesModel = new ProfilesModel(mContext);
            ProfilesModel.Profile profileObject = ProfilesHelper.getProfile
                (profilesModel, mProfile, mContext);

            mSettingsModel.setShadesDimLevel(profileObject.mDimProgress);
            mSettingsModel.setShadesIntensityLevel(profileObject.mIntensityProgress);
            mSettingsModel.setShadesColor(profileObject.mColorProgress);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private void openRemoveProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getResources().getString
                         (R.string.remove_profile_dialog_title));

        String okString = getContext().getResources().getString(R.string.button_remove_profile);
        String cancelString = getContext().getResources().getString(R.string.cancel_dialog);

        builder.setPositiveButton(okString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                        mProfilesModel.removeProfile(mProfile - DEFAULT_OPERATIONS_AM);
                        mProfile = 0;
                        initLayout();

                        updateAmmountProfiles();
                }
            });

        builder.setNegativeButton(cancelString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

        builder.show();
    }

    private void openAddNewProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getResources().getString(R.string.add_new_profile_dialog_title));

        final EditText nameInput = new EditText(getContext());
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameInput.setHint(getContext().getResources().getString(R.string.add_new_profile_edit_hint));

        builder.setView(nameInput);

        String okString = getContext().getResources().getString(R.string.ok_dialog);
        String cancelString = getContext().getResources().getString(R.string.cancel_dialog);

        builder.setPositiveButton(okString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!(nameInput.getText().toString().trim().equals(""))) {
                        ProfilesModel.Profile profile = new ProfilesModel.Profile
                            (nameInput.getText().toString(),
                             getColorTemperatureProgress(),
                             getIntensityLevelProgress(),
                             getDimLevelProgress());

                        mProfilesModel.addProfile(profile);
                        mArrayAdapter.add((CharSequence) profile.mProfileName);

                         mProfileSpinner.setSelection
                            (mProfilesModel.getProfiles().size() - 1 + DEFAULT_OPERATIONS_AM);

                         updateAmmountProfiles();
                    } else {
                        dialog.cancel();
                    }
                }
            });

        builder.setNegativeButton(cancelString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

        builder.show();
    }

    //Section: Reading and writing preference states

    private int getColorTemperatureProgress() {
        return ((ShadesActivity) getContext()).getColorTempProgress();
    }

    private int getIntensityLevelProgress() {
        return ((ShadesActivity) getContext()).getIntensityLevelProgress();
    }

    private int getDimLevelProgress() {
        return ((ShadesActivity) getContext()).getDimLevelProgress();
    }

    private void setColorTemperatureProgress(int progress) {
        currentColor = progress;
        ShadesFragment fragment = ((ShadesActivity) getContext()).getFragment();
        ColorSeekBarPreference colorPref = (ColorSeekBarPreference) fragment.findPreference
            (getContext().getResources().getString(R.string.pref_key_shades_color_temp));

        colorPref.mColorTempSeekBar.setProgress(progress);
    }

    private void setIntensityLevelProgress(int progress) {
        currentIntensity = progress;
        ShadesFragment fragment = ((ShadesActivity) getContext()).getFragment();
        IntensitySeekBarPreference intensityPref = (IntensitySeekBarPreference) fragment.findPreference
            (getContext().getResources().getString(R.string.pref_key_shades_intensity_level));

        intensityPref.mIntensityLevelSeekBar.setProgress(progress);
    }

    private void setDimLevelProgress(int progress) {
        currentDim = progress;
        ShadesFragment fragment = ((ShadesActivity) getContext()).getFragment();
        DimSeekBarPreference dimPref = (DimSeekBarPreference) fragment.findPreference
            (getContext().getResources().getString(R.string.pref_key_shades_dim_level));

        dimPref.mDimLevelSeekBar.setProgress(progress);
    }

    //Section: Reading and writing profiles

    /**
     * Reads the profiles saved in the SharedPreference in the spinner
     */
    public void readProfiles() {
        ArrayList<ProfilesModel.Profile> profiles = mProfilesModel.getProfiles();

        for (ProfilesModel.Profile profile : profiles) {
            mArrayAdapter.add((CharSequence) profile.mProfileName);
        }
    }

    /**
     * Updates the ammount of profiles in the shared preferences
     */
    private void updateAmmountProfiles() {
        int ammountProfiles = mProfilesModel.getProfiles().size() + DEFAULT_OPERATIONS_AM;
        if (DEBUG) Log.i(TAG, "There are now " + ammountProfiles + " profiles.");
        mSettingsModel.setAmmountProfiles(ammountProfiles);
    }

    //Section: onSettingsChangedListener
    private void addSettingsChangedListener() {
        if (mIsListenerRegistered) return;
        mIsListenerRegistered = true;
        SettingsModel model = ((ShadesActivity) getContext()).getSettingsModel();
        model.addOnSettingsChangedListener(new SettingsModel.OnSettingsChangedListener() {
                @Override
                public void onShadesPauseStateChanged(boolean pauseState) { }

                @Override
                public void onShadesDimLevelChanged(int dimLevel) {
                    if (dimLevel == currentDim) return;
                    mProfileSpinner.setSelection(0);
                }

                @Override
                public void onShadesIntensityLevelChanged(int intensityLevel) {
                    if (intensityLevel == currentIntensity) return;
                    mProfileSpinner.setSelection(0);
                }

                @Override
                public void onShadesColorChanged(int color) {
                    if (color == currentColor) return;
                    mProfileSpinner.setSelection(0);
                }

                @Override
                public void onShadesAutomaticFilterModeChanged(String automaticFilterMode) {}

                @Override
                public void onShadesAutomaticTurnOnChanged(String turnOnTime) {}

                @Override
                public void onShadesAutomaticTurnOffChanged(String turnOffTime) {}

                @Override
                public void onLowerBrightnessChanged(boolean lowerBrightness) { }

                @Override
                public void onProfileChanged(int profile) {
                    mProfile = profile;
                    mProfileSpinner.setSelection(mProfile);

                    if (mProfile != 0) {
                        ProfilesModel.Profile newProfile = ProfilesHelper.getProfile
                            (mProfilesModel, mProfile, mContext);

                        currentDim = newProfile.mDimProgress;
                        currentIntensity = newProfile.mIntensityProgress;
                        currentColor = newProfile.mColorProgress;
                    }
                }

                @Override
                public void onAutomaticSuspendChanged(boolean automaticSuspend) { }
            });
    }
}
