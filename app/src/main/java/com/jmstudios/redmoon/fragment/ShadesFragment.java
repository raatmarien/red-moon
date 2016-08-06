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
package com.jmstudios.redmoon.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.activity.ShadesActivity;
import com.jmstudios.redmoon.model.SettingsModel;
import com.jmstudios.redmoon.preference.FilterTimePreference;
import com.jmstudios.redmoon.preference.LocationPreference;
import com.jmstudios.redmoon.presenter.ShadesPresenter;
import com.jmstudios.redmoon.service.ScreenFilterService;

import eu.chainfire.libsuperuser.Shell;

public class ShadesFragment extends PreferenceFragment {
    private static final String TAG = "ShadesFragment";
    private static final boolean DEBUG = true;

    private ShadesPresenter mPresenter;
    private FloatingActionButton mToggleFab;
    private View mView;
    private Snackbar mHelpSnackbar;

    // Preferences
    private SwitchPreference darkThemePref;
    private SwitchPreference lowerBrightnessPref;
    private SwitchPreference stopTemporarilyPref;
    private ListPreference automaticFilterPref;
    private FilterTimePreference automaticTurnOnPref;
    private FilterTimePreference automaticTurnOffPref;
    private LocationPreference locationPref;
    private Preference otherPrefCategory;

    private boolean searchingLocation;

    public ShadesFragment() {
        // Android Fragments require an explicit public default constructor for re-creation
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        String darkThemePrefKey= getString(R.string.pref_key_dark_theme);
        String lowerBrightnessPrefKey = getString(R.string.pref_key_control_brightness);
        String automaticFilterPrefKey = getString(R.string.pref_key_automatic_filter);
        String automaticTurnOnPrefKey = getString(R.string.pref_key_custom_start_time);
        String automaticTurnOffPrefKey = getString(R.string.pref_key_custom_end_time);
        String locationPrefKey = getString(R.string.pref_key_location);
        String otherCategoryPrefKey = getString(R.string.pref_key_other);
        String stopTemporarilyPrefKey = "pref_key_stop_temporarily";

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        PreferenceScreen prefScreen = getPreferenceScreen();
        darkThemePref = (SwitchPreference) prefScreen.findPreference(darkThemePrefKey);
        lowerBrightnessPref = (SwitchPreference) prefScreen.findPreference(lowerBrightnessPrefKey);
        stopTemporarilyPref = (SwitchPreference) prefScreen.findPreference(stopTemporarilyPrefKey);

        automaticFilterPref = (ListPreference) prefScreen.findPreference(automaticFilterPrefKey);
        automaticTurnOnPref = (FilterTimePreference) prefScreen.findPreference(automaticTurnOnPrefKey);
        automaticTurnOffPref = (FilterTimePreference) prefScreen.findPreference(automaticTurnOffPrefKey);
        locationPref = (LocationPreference) prefScreen.findPreference(locationPrefKey);
        otherPrefCategory = prefScreen.findPreference(otherCategoryPrefKey);

        darkThemePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getActivity().recreate();
                return true;
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
            !Settings.System.canWrite(getContext())) lowerBrightnessPref.setChecked(false);

        lowerBrightnessPref.setOnPreferenceChangeListener
            (new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = (Boolean) newValue;
                if (checked && android.os.Build.VERSION.SDK_INT >= 23 &&
                    !Settings.System.canWrite(getContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                               Uri.parse("package:" +
                                                         getContext().getPackageName()));
                    startActivityForResult(intent, -1);
                    return false;
                }

                return true;
            }
        });

        boolean custom = automaticFilterPref.getValue().toString().equals("custom");
        automaticTurnOnPref.setEnabled(custom);
        automaticTurnOffPref.setEnabled(custom);
        boolean sun = automaticFilterPref.getValue().toString().equals("sun");
        locationPref.setEnabled(sun);

        automaticFilterPref.setSummary(automaticFilterPref.getEntry());

        onAutomaticFilterPreferenceChange(automaticFilterPref,
                                          automaticFilterPref.getValue().toString());

        automaticFilterPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return onAutomaticFilterPreferenceChange(preference, newValue);
                }
            });

        locationPref.setOnLocationChangedListener(new LocationPreference.OnLocationChangedListener() {
                @Override
                public void onLocationChange() {
                    if (automaticFilterPref.getValue().equals("sun")) {
                        updateFilterTimesFromSun();
                    }
                }
            });

        stopTemporarilyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!sharedPreferences.getBoolean("screen_overlays", false)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Screen overlays")
                            .setMessage("App uses screen overlays to control screen, but Android system prevents installing APKs if such overlay is applied. Please use our Package Installer (RedMoon) which just stops RedMoon, opens Android's Package Installer and after installation, it resumes RedMoon.\n\nIf you are familiar with ADB shell, run \"adb shell pm grant com.jmstudios.redmoon android.permission.DUMP\" for even better results.\n\nAlso, Superuser apps disable allow/deny buttons when other apps request root access if screen overlay is aplied. You can grant root access for this app  in the next dialog so app can get info from Superuser apps whether to show screen overlays or not and app can handle it properly.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    sharedPreferences.edit().putBoolean("screen_overlays", true).apply();
                                    dialog.cancel();
                                    getSuPrompt();
                                    // Do nothing, we dont need root
                                    // Just to show Superuser SU reguest dialog for our app
                                }
                            })
                            .show();
                }
                int state = (boolean)newValue ?
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                ComponentName alias = new ComponentName(getActivity(), "com.jmstudios.redmoon.activity.PackageInstallerActivity" );
                getActivity().getPackageManager().setComponentEnabledSetting( alias, state, PackageManager.DONT_KILL_APP );
                return true;
            }
        });
    }

    private void getSuPrompt() {
        Shell.SU.run("pm grant com.jmstudios.redmoon android.permission.DUMP");
    }

    private boolean onAutomaticFilterPreferenceChange(Preference preference, Object newValue) {
        if (newValue.toString().equals("sun") && ContextCompat.checkSelfPermission
            (getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                {Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return false;
        }

        boolean custom = newValue.toString().equals("custom");
        automaticTurnOnPref.setEnabled(custom);
        automaticTurnOffPref.setEnabled(custom);

        boolean sun = newValue.toString().equals("sun");
        locationPref.setEnabled(sun);

        // From something to sun
        if (newValue.toString().equals("sun")) {
            // Update the FilterTimePreferences
            updateFilterTimesFromSun();

            // Attempt to get a new location
            locationPref.searchLocation(false);
        }

        // From sun to something
        String oldValue = preference.getSharedPreferences().getString
            (preference.getKey(), "never");
        if (oldValue.equals("sun") && !newValue.equals("sun")) {
            automaticTurnOnPref.setToCustomTime();
            automaticTurnOffPref.setToCustomTime();
        }

        ListPreference lp = (ListPreference) preference;
        String entry = lp.getEntries()[lp.findIndexOfValue(newValue.toString())].toString();
        lp.setSummary(entry);

        return true;
    }

    private void updateFilterTimesFromSun() {
        String location = locationPref.getLocation();
        if (location.equals("not set")) {
            automaticTurnOnPref.setToSunTime("19:30");
            automaticTurnOffPref.setToSunTime("06:30");
        } else {
            Location androidLocation = new Location(LocationManager.NETWORK_PROVIDER);
            androidLocation.setLatitude(Double.parseDouble(location.split(",")[0]));
            androidLocation.setLongitude(Double.parseDouble(location.split(",")[1]));

            String sunsetTime = FilterTimePreference.getSunTimeFromLocation
                (androidLocation, true);
            automaticTurnOnPref.setToSunTime(sunsetTime);

            String sunriseTime = FilterTimePreference.getSunTimeFromLocation
                (androidLocation, false);
            automaticTurnOffPref.setToSunTime(sunriseTime);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = super.onCreateView(inflater, container, savedInstanceState);

        mToggleFab = (FloatingActionButton) getActivity().findViewById(R.id.toggle_fab);
        mToggleFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SettingsModel settingsModel = ((ShadesActivity) getActivity()).getSettingsModel();
                    boolean poweredOn = settingsModel.getShadesPowerState();
                    boolean paused = settingsModel.getShadesPauseState();

                    if (!poweredOn || paused) {
                        mPresenter.sendCommand(ScreenFilterService.COMMAND_ON);
                    } else {
                        mPresenter.sendCommand(ScreenFilterService.COMMAND_PAUSE);
                    }
                }
            });

        mView = v;

        return v;
    }

    public void registerPresenter(@NonNull ShadesPresenter presenter) {
        mPresenter = presenter;

        if (DEBUG) Log.i(TAG, "Registered Presenter");
    }

    public void setSwitchOn(boolean powerState, boolean pauseState) {
        ShadesActivity activity = (ShadesActivity) getActivity();
        SwitchCompat filterSwitch = activity.getSwitch();
        if (filterSwitch != null) {
            activity.setIgnoreNextSwitchChange(powerState != filterSwitch.isChecked());
            filterSwitch.setChecked(powerState);
        }
        updateFabIcon();

        if (!powerState) {
            disableFilterPreferences();
            mToggleFab.hide();
            showHelpSnackbar();
        } else {
            setPreferencesEnabled();
            if (mHelpSnackbar != null)
                mHelpSnackbar.dismiss();
            mToggleFab.show();
        }

        if (powerState && !pauseState) {
            activity.displayInstallWarningToast();
        }
    }

    private void updateFabIcon() {
        SettingsModel settingsModel = ((ShadesActivity) getActivity()).getSettingsModel();
        boolean poweredOn = settingsModel.getShadesPowerState();
        boolean paused = settingsModel.getShadesPauseState();

        if (!poweredOn || paused) {
            mToggleFab.setImageResource(R.drawable.fab_start);
        } else {
            mToggleFab.setImageResource(R.drawable.fab_pause);
        }
    }

    private void disableFilterPreferences() {
        setAllPreferencesEnabled(false);
        otherPrefCategory.setEnabled(true);
    }

    private void setPreferencesEnabled() {
        setAllPreferencesEnabled(true);

        boolean custom = automaticFilterPref.getValue().toString().equals("custom");
        automaticTurnOnPref.setEnabled(custom);
        automaticTurnOffPref.setEnabled(custom);
        boolean sun = automaticFilterPref.getValue().toString().equals("sun");
        locationPref.setEnabled(sun);
    }

    private void setAllPreferencesEnabled(boolean enabled) {
        PreferenceScreen root = getPreferenceScreen();
        for (int i = 0; i < root.getPreferenceCount(); i++) {
            root.getPreference(i).setEnabled(enabled);
        }
    }

    private void showHelpSnackbar() {
        mHelpSnackbar = Snackbar.make
            (mView, getActivity().getString(R.string.help_snackbar_text),
             Snackbar.LENGTH_INDEFINITE);

        if (((ShadesActivity) getActivity()).getSettingsModel().getDarkThemeFlag()) {
            ViewGroup group = (ViewGroup) mHelpSnackbar.getView();
            group.setBackgroundColor(getActivity().getResources().getColor(R.color.snackbar_color_dark_theme));

            int snackbarTextId = android.support.design.R.id.snackbar_text;
            TextView textView = (TextView) group.findViewById(snackbarTextId);
            textView.setTextColor(getActivity().getResources().getColor(R.color.text_color_dark_theme));
        }

        mHelpSnackbar.show();
    }
}
