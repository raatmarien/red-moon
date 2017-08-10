/*
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
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
