/* Copyright (c) 2017-12-01  cj
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */

package com.jmstudios.redmoon.receiver

import android.annotation.TargetApi
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

import com.jmstudios.redmoon.event.filterIsOnChanged
import com.jmstudios.redmoon.helper.EventBus
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.service.ScreenFilterService
import com.jmstudios.redmoon.util.*

import org.greenrobot.eventbus.Subscribe

@TargetApi(24)
class TileReceiver : TileService() {

    override fun onStartListening() {
        EventBus.register(this)
        updateState()
    }

    override fun onClick() {
        super.onClick()
        ScreenFilterService.toggle()
    }

    override fun onStopListening() {
        EventBus.unregister(this)
    }

    @Subscribe fun onFilterIsOnChanged(event: filterIsOnChanged) {
        updateState()
    }

    private fun updateState() = qsTile.run {
        state = if (filterIsOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        updateTile()
    }
}
