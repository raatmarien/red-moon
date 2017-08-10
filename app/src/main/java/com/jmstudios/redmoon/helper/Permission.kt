/*
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.helper

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog

import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.util.*

private const val REQ_CODE_OVERLAY  = 1111
private const val REQ_CODE_LOCATION = 2222
private const val REQ_CODE_SETTINGS = 3333

abstract class PermissionHelper : EventBus.Event {
    abstract val isGranted: Boolean
    abstract protected val requestCode: Int
    abstract protected fun send(activity: Activity)
    fun request(activity: Activity): Boolean {
        if (!isGranted) send(activity)
        return isGranted
    }
}

object Permission {
    fun onRequestResult(requestCode: Int) {
        EventBus.post(when (requestCode) {
                          REQ_CODE_OVERLAY -> Overlay
                          REQ_CODE_LOCATION -> Location
                          REQ_CODE_SETTINGS -> WriteSettings
                          else -> return@onRequestResult
                      })
    }

    object Location : PermissionHelper() {
        override val requestCode: Int = REQ_CODE_LOCATION

        override val isGranted: Boolean
            get() {
                val lp = Manifest.permission.ACCESS_FINE_LOCATION
                val granted = PackageManager.PERMISSION_GRANTED
                return ContextCompat.checkSelfPermission(appContext, lp) == granted
            }

        override fun send(activity: Activity) {
            val permission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(activity, permission, requestCode)
        }
    }

    abstract class ElevatedPermission : PermissionHelper() {
        abstract val granted: Boolean
        override val isGranted: Boolean
            get() = if (atLeastAPI(23)) granted else true
    }

    object Overlay : ElevatedPermission() {
        override val requestCode: Int = REQ_CODE_OVERLAY
        override val granted: Boolean
            get() = Settings.canDrawOverlays(appContext)

        override @TargetApi(23) fun send(activity: Activity) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + activity.packageName))
            AlertDialog.Builder(activity).run {
                setMessage(R.string.dialog_message_permission_overlay)
                setTitle(R.string.dialog_title_permission_overlay)
                setPositiveButton(R.string.dialog_button_ok) { _, _ ->
                    activity.startActivityForResult(intent, requestCode)
                }
                show()
            }
        }
    }

    object WriteSettings : ElevatedPermission() {
        override val requestCode: Int = REQ_CODE_SETTINGS

        override val granted: Boolean
            get() = if (atLeastAPI(23)) Settings.System.canWrite(appContext) else true

        override @TargetApi(23) fun send(activity: Activity) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                Uri.parse("package:" + activity.packageName))
            AlertDialog.Builder(activity).run {
                setMessage(R.string.dialog_message_permission_write_settings)
                setTitle(R.string.dialog_title_permission_write_settings)
                setPositiveButton(R.string.dialog_button_ok) { _, _ ->
                    activity.startActivityForResult(intent, requestCode)
                }
                show()
            }
        }
    }
}
