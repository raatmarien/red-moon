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
package com.jmstudios.redmoon.activity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Switch;
import android.widget.Toast;

import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.activity.Intro;
import com.jmstudios.redmoon.fragment.ShadesFragment;
import com.jmstudios.redmoon.helper.FilterCommandFactory;
import com.jmstudios.redmoon.helper.FilterCommandSender;
import com.jmstudios.redmoon.model.SettingsModel;
import com.jmstudios.redmoon.presenter.ShadesPresenter;
import com.jmstudios.redmoon.service.ScreenFilterService;

public class ShadesActivity extends AppCompatActivity {
    private static final String TAG = "ShadesActivity";
    private static final boolean DEBUG = false;
    private static final String FRAGMENT_TAG_SHADES = "jmstudios.fragment.tag.SHADES";

    public static final String EXTRA_FROM_SHORTCUT_BOOL =
        "com.jmstudios.redmoon.activity.ShadesActivity.EXTRA_FROM_SHORTCUT_BOOL";
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    private ShadesPresenter mPresenter;
    private ShadesFragment mFragment;
    private SettingsModel mSettingsModel;
    private Switch mSwitch;
    private FilterCommandFactory mFilterCommandFactory;
    private FilterCommandSender mFilterCommandSender;
    private ShadesActivity context = this;

    private boolean hasShownWarningToast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (DEBUG) Log.i(TAG, "Got intent");

        // Wire MVP classes
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSettingsModel = new SettingsModel(getResources(), sharedPreferences);
        mFilterCommandFactory = new FilterCommandFactory(this);
        mFilterCommandSender = new FilterCommandSender(this);

        boolean fromShortcut = intent.getBooleanExtra(EXTRA_FROM_SHORTCUT_BOOL, false);
        if (fromShortcut) {
            toggleAndFinish();
        }


        if (mSettingsModel.getDarkThemeFlag()) setTheme(R.style.AppThemeDark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shades);

        FragmentManager fragmentManager = getFragmentManager();

        ShadesFragment view;

        // Only create and attach a new fragment on the first Activity creation.
        // On Activity re-creation, retrieve the existing fragment stored in the FragmentManager.
        if (savedInstanceState == null) {
            if (DEBUG) Log.i(TAG, "onCreate - First creation");

            view = new ShadesFragment();

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, view, FRAGMENT_TAG_SHADES)
                    .commit();
        } else {
            if (DEBUG) Log.i(TAG, "onCreate - Re-creation");

            view = (ShadesFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG_SHADES);
        }

        mPresenter = new ShadesPresenter(view, mSettingsModel, mFilterCommandFactory,
                                         mFilterCommandSender, context);
        view.registerPresenter(mPresenter);

        // Make Presenter listen to settings changes
        mSettingsModel.addOnSettingsChangedListener(mPresenter);

        mFragment = view;

        if (!mSettingsModel.getIntroShown()) {
            startIntro();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);

        final MenuItem item = menu.findItem(R.id.screen_filter_switch);
        mSwitch = (Switch) item.getActionView();
        mSwitch.setChecked(mSettingsModel.getShadesPauseState());
        mSwitch.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasOverlayPermission()) {
                        sendCommand(mSwitch.isChecked() ?
                                            ScreenFilterService.COMMAND_ON :
                                            ScreenFilterService.COMMAND_PAUSE);
                    } else {
                        mSwitch.setChecked(false);
                    }
                }
        });

        return true;
    }

    public void setSwitch(boolean onState) {
        if (mSwitch != null) {
            mSwitch.setChecked(onState);
        }
    }

    private boolean hasOverlayPermission() {
        // http://stackoverflow.com/a/3993933
        if (android.os.Build.VERSION.SDK_INT < 23) { return true; }

        if (!Settings.canDrawOverlays(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setMessage(R.string.overlay_dialog_message)
                .setTitle(R.string.overlay_dialog_title)
                .setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                    }
                });

            builder.show();
        }
        return Settings.canDrawOverlays(context);
    }

    private void sendCommand(int command) {
        Intent iCommand = mFilterCommandFactory.createCommand(command);
        mFilterCommandSender.send(iCommand);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSettingsModel.openSettingsChangeListener();
        mPresenter.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // When the activity is not on the screen, but the user
        // updates the profile through the notification. the
        // notification spinner and the seekbars will have missed this
        // change. To update them correctly, we artificially change
        // these settings.
        int intensity = mSettingsModel.getShadesIntensityLevel();
        mSettingsModel.setShadesIntensityLevel(intensity == 0 ? 1 : 0);
        mSettingsModel.setShadesIntensityLevel(intensity);

        int dim = mSettingsModel.getShadesDimLevel();
        mSettingsModel.setShadesDimLevel(dim == 0 ? 1 : 0);
        mSettingsModel.setShadesDimLevel(dim);

        int color = mSettingsModel.getShadesColor();
        mSettingsModel.setShadesColor(color == 0 ? 1 : 0);
        mSettingsModel.setShadesColor(color);

        // The profile HAS to be updated last, otherwise the spinner
        // will switched to custom.
        int profile = mSettingsModel.getProfile();
        mSettingsModel.setProfile(profile == 0 ? 1 : 0);
        mSettingsModel.setProfile(profile);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        boolean fromShortcut = intent.getBooleanExtra(EXTRA_FROM_SHORTCUT_BOOL, false);
        if (fromShortcut) {
            toggleAndFinish();
        }
    }

    @Override
    protected void onStop() {
        mSettingsModel.closeSettingsChangeListener();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.show_intro_button:
                startIntro();
                return true;
            case R.id.view_github:
                String github = getResources().getString(R.string.project_page_url);
                Intent projectIntent = new Intent(Intent.ACTION_VIEW)
                                            .setData(Uri.parse(github));
                startActivity(projectIntent);
            case R.id.email_developer:
                String email = getResources().getString(R.string.contact_email_adress);
                Intent emailIntent = new Intent(Intent.ACTION_VIEW)
                                            .setData(Uri.parse(email));
                startActivity(emailIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void displayInstallWarningToast() {
        if (hasShownWarningToast || mSettingsModel.getAutomaticSuspend())
            return;

        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getApplicationContext(),
                                     getString(R.string.toast_warning_install),
                                     duration);
        toast.show();

        hasShownWarningToast = true;
    }

    private void startIntro() {
        Intent introIntent = new Intent(this, Intro.class);
        startActivity(introIntent);

        mSettingsModel.setIntroShown(true);
    }

    public int getColorTempProgress() {
        return mSettingsModel.getShadesColor();
    }

    public int getIntensityLevelProgress() {
        return mSettingsModel.getShadesIntensityLevel();
    }

    public int getDimLevelProgress() {
        return mSettingsModel.getShadesDimLevel();
    }

    public ShadesFragment getFragment() {
        return mFragment;
    }

    public SettingsModel getSettingsModel() {
        return mSettingsModel;
    }

    private void toggleAndFinish() {
        boolean paused = mSettingsModel.getShadesPauseState();
        sendCommand( paused ? ScreenFilterService.COMMAND_ON
                            : ScreenFilterService.COMMAND_PAUSE);
        finish();
    }
}
