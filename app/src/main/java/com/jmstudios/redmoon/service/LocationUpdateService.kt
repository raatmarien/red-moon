/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (c) 2015 Chris Nguyen
 *     Copyright (c) 2016 Zoraver <https://github.com/Zoraver>
 *
 *     Permission to use, copy, modify, and/or distribute this software
 *     for any purpose with or without fee is hereby granted, provided
 *     that the above copyright notice and this permission notice appear
 *     in all copies.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *     WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *     WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *     AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 *     CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 *     OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *     NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 *     CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.jmstudios.redmoon.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder

import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.helper.Permission
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.util.appContext

import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit

/**
 * When the service starts, we request location updates. When we get a new
 * location fix, we shut down the service. When we shut down (for any reason),
 * we update the location with the last known location. This way, even if we
 * didn't get a fix before the service was stopped, we might be able to get
 * something more recent than the last time red moon updated location.
 */
class LocationUpdateService: Service(), LocationListener {

    private inner class Provider(val provider: String) {
        private var mRequested = false

        val exists: Boolean
            get() = locationManager.allProviders.contains(provider)

        private val enabled: Boolean
            get() = locationManager.isProviderEnabled(provider)

        val searching: Boolean
            get() = mRequested && enabled

        val lastKnownLocation: Location?
            get() = locationManager.getLastKnownLocation(provider)

        fun requestUpdates(listener: LocationListener) = when {
            !exists -> {
                Log.i("$provider doesn't exist; can't request updates")
                postStatus(searching = false)
            } mRequested -> {
                Log.i("Already searching for updates using $provider")
                postStatus(searching = enabled)
            } else -> {
                Log.i("Requesting location updates using $provider")
                mRequested = true
                if (enabled) { postStatus(searching = true) }
                // If disabled, onProviderDisabled is called immediately
                locationManager.requestLocationUpdates(provider, 0, 0f, listener)
            }
        }
    }

    // Starts null so we don't send duplicate status messages on init
    private var mIsForeground: Boolean? = null

    // Convenience to avoid null checks
    private val isForeground
        get() = mIsForeground ?: false

    private var mNetworkProvider = Provider(LocationManager.NETWORK_PROVIDER)
    private var mGpsProvider     = Provider(LocationManager.GPS_PROVIDER)

    private var mIsSearching: Boolean? = null

    private val locationManager: LocationManager
        get() = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val lastKnownLocation: Location?
        get() = mGpsProvider.lastKnownLocation ?: mNetworkProvider.lastKnownLocation

    private val locationUpToDate: Boolean
        get() = lastKnownLocation?.time?.isRecent ?: Config.location.third?.isRecent ?: false

    private val Long.isRecent: Boolean
        get() = 1 > TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - this)

    override fun onCreate() {
        super.onCreate()
        Log.i("onCreate")
        when {
            locationUpToDate -> {
                Log.i("Last known location is recent enough.")
                stopSelf()
            } mNetworkProvider.exists -> {
                mNetworkProvider.requestUpdates(this)
            } mGpsProvider.exists -> {
                mGpsProvider.requestUpdates(this)
            } else -> {
                Log.i("No good providers, only: ${locationManager.allProviders}")
                stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("onStartCommand($intent, $flags, $startId)")
        if (mIsForeground != null) {
            val searching = mNetworkProvider.searching || mGpsProvider.searching
            EventBus.getDefault().post(locationService(searching))
        }
        val fg = intent.getBooleanExtra(BUNDLE_KEY_FOREGROUND, false)
        mIsForeground = fg || isForeground

        // Do not attempt to restart if the hosting process is killed by Android
        return Service.START_NOT_STICKY
    }

    // Prevent binding.
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onLocationChanged(location: Location) {
        Log.i("Location search succeeded")
        stopSelf()
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        Log.i("Provider $provider changed to status $status with extras $extras")
    }

    override fun onProviderEnabled(provider: String) {
        Log.i("onProviderEnabled: $provider")
        postStatus(searching = true)
    }

    override fun onProviderDisabled(provider: String) {
        Log.i("onProviderDisabled: $provider")
        if (!isForeground) {
            Log.i("Avoiding gps for background updates")
            stopSelf()
        } else when (provider) {
            mNetworkProvider.provider -> mGpsProvider.requestUpdates(this)
            mGpsProvider.provider -> mNetworkProvider.requestUpdates(this)
            else -> Log.w("We shouldn't be getting $provider updates")
        }
    }

    override fun onDestroy() {
        Log.i("onDestroy")
        if (Permission.Location.isGranted) {
            locationManager.removeUpdates(this)
            lastKnownLocation?.apply {
                Config.location = Triple(latitude.toString(), longitude.toString(), time)
            }
        }
        super.onDestroy()
        EventBus.getDefault().post(locationService(false, isRunning = false))
    }

    // Filters duplicates
    private fun postStatus(searching: Boolean) {
        if (mIsSearching != searching) {
            Log.i("posting: $searching")
            mIsSearching = searching
            EventBus.getDefault().post(locationService(searching))
        }
    }

    companion object : Logger() {
        private const val BUNDLE_KEY_FOREGROUND = "jmstudios.bundle.key.FOREGROUND"

        private val intent: Intent
            get() = Intent(appContext, LocationUpdateService::class.java)

        fun update(foreground: Boolean = true) {
            Log.i("Received request")
            if (!Permission.Location.isGranted) {
                EventBus.getDefault().post(locationAccessDenied())
            } else if (Config.timeToggle && Config.useLocation) {
                val i = intent.putExtra(BUNDLE_KEY_FOREGROUND, foreground)
                appContext.startService(i)
            } else {
                appContext.stopService(intent) 
            }
        }
    }
}
