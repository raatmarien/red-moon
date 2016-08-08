package com.jmstudios.redmoon.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jmstudios.redmoon.helper.FilterCommandFactory;
import com.jmstudios.redmoon.helper.FilterCommandSender;
import com.jmstudios.redmoon.model.SettingsModel;
import com.jmstudios.redmoon.service.ScreenFilterService;

public class HideScreenOverlaysReceiver extends BroadcastReceiver {
    public static final String EXTRA_HIDE_OVERLAYS = "eu.chainfire.supersu.extra.HIDE";

    @Override
    public final void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(EXTRA_HIDE_OVERLAYS)) {
            onHideOverlays(context, intent.getBooleanExtra(EXTRA_HIDE_OVERLAYS, false));
        }
    }

    public void onHideOverlays(Context context, boolean hide) {
        FilterCommandSender commandSender = new FilterCommandSender(context);
        FilterCommandFactory commandFactory = new FilterCommandFactory(context);
        Intent onCommand = commandFactory.createCommand(ScreenFilterService.COMMAND_ON);
        Intent pauseCommand = commandFactory.createCommand(ScreenFilterService.COMMAND_PAUSE);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        SettingsModel settingsModel = new SettingsModel(context.getResources(), sharedPreferences);
        boolean paused = settingsModel.getShadesPauseState();
        if (hide) {
            commandSender.send(pauseCommand);
        } else {
            if (paused) commandSender.send(onCommand);
        }
    }
}

