/*
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */

package com.jmstudios.redmoon.application

import android.app.Application
import com.jmstudios.redmoon.helper.Logger

class RedMoonApplication: Application() {

    override fun onCreate() {
        Log.i("onCreate -- Initializing appContext")
        app = this
        super.onCreate()
        //EventBus.builder().addIndex(eventBusIndex()).installDefaultEventBus()
    }

    companion object : Logger() {
        lateinit var app: RedMoonApplication
            private set
    }
}
