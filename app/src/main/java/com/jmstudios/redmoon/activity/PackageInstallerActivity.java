package com.jmstudios.redmoon.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.helper.FilterCommandFactory;
import com.jmstudios.redmoon.helper.FilterCommandSender;
import com.jmstudios.redmoon.model.SettingsModel;
import com.jmstudios.redmoon.service.ScreenFilterService;

public class PackageInstallerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeTransparent);
        super.onCreate(savedInstanceState);

        FilterCommandSender commandSender = new FilterCommandSender(this);
        FilterCommandFactory commandFactory = new FilterCommandFactory(this);
        Intent pauseCommand = commandFactory.createCommand(ScreenFilterService.COMMAND_PAUSE);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        SettingsModel settingsModel = new SettingsModel(getResources(), sharedPreferences);
        boolean poweredOn = settingsModel.getShadesPowerState();

        if (poweredOn) {
            commandSender.send(pauseCommand);
            sharedPreferences.edit().putBoolean("package_installer_interrupt", true).apply();
        }

        Uri apk = getIntent().getData();
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData(apk);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.google.android.packageinstaller");
        try {
            startActivity(intent);
        } catch (Exception e) {
            intent.setPackage("com.android.packageinstaller");
            try {
                startActivity(intent);
            } catch (Exception ignored) {
            }
        }
        finish();

    }
}
