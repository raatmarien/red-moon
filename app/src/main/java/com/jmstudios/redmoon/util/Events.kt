/*
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.jmstudios.redmoon.util

import com.jmstudios.redmoon.util.EventBus.Event

class filterIsOnChanged        : Event
//class themeChanged             : Event
class profilesUpdated          : Event
class scheduleChanged          : Event
class useLocationChanged       : Event
class locationChanged          : Event
class secureSuspendChanged     : Event
class buttonBacklightChanged   : Event

class overlayPermissionDenied  : Event
class locationAccessDenied     : Event
class changeBrightnessDenied   : Event

data class locationService(val isSearching: Boolean, val isRunning: Boolean = true) : Event
