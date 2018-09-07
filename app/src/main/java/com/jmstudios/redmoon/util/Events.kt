/*
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */

package com.jmstudios.redmoon.util

import com.jmstudios.redmoon.util.EventBus.Event

class FilterIsOnChanged : Event
//class ThemeChanged             : Event
class ProfilesUpdated : Event

class ScheduleChanged : Event
class UseLocationChanged : Event
class LocationChanged : Event
class SecureSuspendChanged : Event
class ButtonBacklightChanged : Event

class OverlayPermissionDenied : Event
class LocationAccessDenied : Event
class ChangeBrightnessDenied : Event

data class LocationService(val isSearching: Boolean, val isRunning: Boolean = true) : Event
