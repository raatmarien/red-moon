/*
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
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
