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
import android.view.View
import com.jmstudios.redmoon.helper.Logger

import com.jmstudios.redmoon.helper.Profile

class ScreenFilterView(context: Context) : View(context) {
    companion object: Logger()

    var profile: Profile = Profile(100, 0, 0, false)
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) = canvas.drawColor(profile.filterColor)
}
