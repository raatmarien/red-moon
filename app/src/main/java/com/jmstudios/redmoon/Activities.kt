/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.jmstudios.redmoon

class SecureSuspendActivity : ThemedAppCompatActivity() {
    override val fragment = SecureSuspendFragment()
    override val tag = "jmstudios.fragment.tag.SECURE_SUSPEND"
}

class ScheduleActivity : ThemedAppCompatActivity() {
    override val fragment = ScheduleFragment()
    override val tag = "jmstudios.fragment.tag.SCHEDULE"
}

class AboutActivity : ThemedAppCompatActivity() {
    override val fragment = AboutFragment()
    override val tag = "jmstudios.fragment.tag.ABOUT"
}
