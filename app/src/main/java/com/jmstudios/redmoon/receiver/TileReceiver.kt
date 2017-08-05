/* Copyright (c) 2017-12-01  cj
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
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
