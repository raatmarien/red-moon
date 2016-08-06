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
package com.jmstudios.redmoon.activity;

import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import com.jmstudios.redmoon.R;

public class Intro extends AppIntro {
    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(AppIntroFragment.newInstance
                 (getString(R.string.intro_slide_1_title),
                  getString(R.string.intro_slide_1_text),
                  R.drawable.intro_slide_1,
                  0xFFD32F2F));

        addSlide(AppIntroFragment.newInstance
                 (getString(R.string.intro_slide_2_title),
                  getString(R.string.intro_slide_2_text),
                  R.drawable.intro_slide_2,
                  0xFF2196F3));

        addSlide(AppIntroFragment.newInstance
                 (getString(R.string.intro_slide_3_title),
                  getString(R.string.intro_slide_3_text),
                  R.drawable.intro_slide_3,
                  0xFF388E3C));

        addSlide(AppIntroFragment.newInstance
                (getString(R.string.intro_slide_5_title),
                        getString(R.string.intro_slide_5_text),
                        R.drawable.intro_slide_4, //TODO, make icon
                        0xFFAAB3D5));

        addSlide(AppIntroFragment.newInstance
                 (getString(R.string.intro_slide_4_title),
                  getString(R.string.intro_slide_4_text),
                  R.drawable.intro_slide_4,
                  0xFFFFB300));
    }

    @Override
    public void onSkipPressed() {
        // Do something when users tap on Skip button.
        finish();
    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        finish();
    }

    public void onSlideChanged() {
        // Do something when the slide changes.
    }

    @Override
    public void onNextPressed() {
        // Do something when users tap on Next button.
    }
}
