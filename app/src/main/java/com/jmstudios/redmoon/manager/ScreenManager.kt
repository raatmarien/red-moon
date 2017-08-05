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
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (c) 2015 Chris Nguyen
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
package com.jmstudios.redmoon.manager

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager

import com.jmstudios.redmoon.helper.Logger

private const val DEFAULT_NAV_BAR_HEIGHT_DP = 48
private const val DEFAULT_STATUS_BAR_HEIGHT_DP = 25

class ScreenManager(context: Context, private val mWindowManager: WindowManager) {

    private val mResources: Resources = context.resources
    private var mStatusBarHeight = -1
    private var mNavigationBarHeight = -1

    val layoutParams: WindowManager.LayoutParams
        get() = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                screenHeight,
                0,
                -statusBarHeightPx,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

    val screenHeight: Int
        get() {
            val display = mWindowManager.defaultDisplay
            val dm = DisplayMetrics()
            display.getRealMetrics(dm)
            return if (inPortrait) {
                dm.heightPixels + statusBarHeightPx + navigationBarHeightPx
            } else {
                dm.heightPixels + statusBarHeightPx
            }
        }

    val statusBarHeightPx: Int
        get() {
            if (mStatusBarHeight == -1) {
                val statusBarHeightId = mResources.getIdentifier("status_bar_height", "dimen", "android")

                if (statusBarHeightId > 0) {
                    mStatusBarHeight = mResources.getDimensionPixelSize(statusBarHeightId)
                    Log.i("Found Status Bar Height: " + mStatusBarHeight)
                } else {
                    mStatusBarHeight = dpToPx(DEFAULT_STATUS_BAR_HEIGHT_DP)
                    Log.i("Using default Status Bar Height: " + mStatusBarHeight)
                }
            }

            return mStatusBarHeight
        }

    val navigationBarHeightPx: Int
        get() {
            if (mNavigationBarHeight == -1) {
                val navBarHeightId = mResources.getIdentifier("navigation_bar_height", "dimen", "android")

                if (navBarHeightId > 0) {
                    mNavigationBarHeight = mResources.getDimensionPixelSize(navBarHeightId)
                    Log.i("Found Navigation Bar Height: " + mNavigationBarHeight)
                } else {
                    mNavigationBarHeight = dpToPx(DEFAULT_NAV_BAR_HEIGHT_DP)
                    Log.i("Using default Navigation Bar Height: " + mNavigationBarHeight)
                }
            }

            return mNavigationBarHeight
        }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                         dp.toFloat(), mResources.displayMetrics).toInt()
    }
    
    private val inPortrait: Boolean
        get() = mResources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    companion object : Logger()
}
