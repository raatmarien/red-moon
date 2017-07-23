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

package com.jmstudios.redmoon.helper

import org.greenrobot.eventbus.EventBus
import kotlin.reflect.KClass

/* EventBus uses classes as events. These are the available events to send */

object EventBus {
    interface Event

    private val bus: EventBus
        get() = EventBus.getDefault()

    fun register  (subscriber: Any) = bus.register  (subscriber)
    fun unregister(subscriber: Any) = bus.unregister(subscriber)

    fun post        (event: Event) = bus.post             (event)
    fun postSticky  (event: Event) = bus.postSticky       (event)
    fun removeSticky(event: Event) = bus.removeStickyEvent(event)

    fun <T: Event>getSticky(eventClass: KClass<T>): T? = bus.getStickyEvent(eventClass.java)
}
