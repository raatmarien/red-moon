/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
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
package com.jmstudios.redmoon.activity

import android.os.Bundle
import android.support.v4.app.Fragment

import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment

import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.model.Config

class Intro : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showStatusBar(false)
        showSkipButton(false)

        addSlide(AppIntroFragment.newInstance(getString(R.string.slide_title_welcome),
                getString(R.string.slide_text_welcome),
                R.drawable.intro_slide_1,
                0xFFD32F2F.toInt()))

        addSlide(AppIntroFragment.newInstance(getString(R.string.slide_title_protect_eyes),
                getString(R.string.slide_text_protect_eyes),
                R.drawable.intro_slide_2,
                0xFF2196F3.toInt()))

        addSlide(AppIntroFragment.newInstance(getString(R.string.slide_title_improve_sleep),
                getString(R.string.slide_text_improve_sleep),
                R.drawable.intro_slide_3,
                0xFF388E3C.toInt()))

        addSlide(AppIntroFragment.newInstance(getString(R.string.slide_title_get_started),
                getString(R.string.slide_text_get_started),
                R.drawable.intro_slide_4,
                0xFFFFB300.toInt()))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        saveAndFinish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        Config.introShown = true
        saveAndFinish()
    }

    private fun saveAndFinish() {
        Config.introShown = true
        finish()
    }
}
