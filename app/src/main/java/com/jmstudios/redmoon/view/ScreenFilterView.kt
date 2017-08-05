/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
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
package com.jmstudios.redmoon.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Process
import android.view.View
import com.jmstudios.redmoon.service.ScreenFilterService
import com.jmstudios.redmoon.helper.Logger

import com.jmstudios.redmoon.helper.Profile
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter


class ScreenFilterView(context: Context) : View(context) {
    companion object: Logger()

    var profile: Profile = Profile(100, 0, 0, false)
        set(value) {
            field = value

            val rootMode = true
            if (rootMode) {
                //Log.i("Root mode, not invalidating")
                rootTintScreen();
            } else {
                invalidate()
            }
        }

    var suOut : DataOutputStream? = null

    override fun onDraw(canvas: Canvas) = canvas.drawColor(profile.filterColor)

    var i = 0
    fun rootTintScreen() {
        var sh = Runtime.getRuntime().exec("sh")
        var shOut = DataOutputStream(sh.outputStream)
        shOut?.writeBytes(getSurfaceMatrix() + " \n")
        shOut?.flush()
    }

    fun getSurfaceMatrix() : String {
        // See https://github.com/raatmarien/red-moon/issues/150
        val matrix = "echo 1015 i32 1 f %.9f f 0 f 0 f 0 f 0 f %.9f f 0 f 0 f 0 f 0 f %.9f f 0 f 0 f 0 f 0 f 1 > ${context.cacheDir.canonicalPath}/pipe"

        Log.i("%d %d %d".format(Color.red(profile.multFilterColor), Color.green(profile.multFilterColor), Color.blue(profile.multFilterColor)))
        return matrix.format(Color.red(profile.multFilterColor) / 255.0f,
                Color.green(profile.multFilterColor) / 255.0f,
                Color.blue(profile.multFilterColor) / 255.0f)
    }
}
