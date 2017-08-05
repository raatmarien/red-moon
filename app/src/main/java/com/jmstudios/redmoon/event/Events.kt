/*
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

package com.jmstudios.redmoon.event

import com.jmstudios.redmoon.helper.EventBus.Event

class filterIsOnChanged        : Event
//class themeChanged             : Event
class customTurnOnTimeChanged  : Event
class customTurnOffTimeChanged : Event
class profilesUpdated          : Event
class timeToggleChanged        : Event
class useLocationChanged       : Event
class locationChanged          : Event
class sunsetTimeChanged        : Event
class sunriseTimeChanged       : Event
class secureSuspendChanged     : Event
class buttonBacklightChanged   : Event

class overlayPermissionDenied  : Event
class locationAccessDenied     : Event
class changeBrightnessDenied   : Event

data class locationService(val isSearching: Boolean, val isRunning: Boolean = true) : Event
