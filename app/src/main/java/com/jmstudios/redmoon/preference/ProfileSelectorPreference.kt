/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
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
package com.jmstudios.redmoon.preference

import android.app.AlertDialog
import android.content.Context
import android.preference.Preference
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.helper.Profile
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.model.profiles.*
import com.jmstudios.redmoon.util.*

import org.greenrobot.eventbus.Subscribe

class ProfileSelectorPreference(ctx: Context, attrs: AttributeSet) : Preference(ctx, attrs),
                                                                     OnItemSelectedListener {
    lateinit private var mProfileSpinner: Spinner
    lateinit private var mProfileActionButton: Button
    lateinit internal var mArrayAdapter: ArrayAdapter<CharSequence>
    private var customShown: Boolean = false

    init {
        layoutResource = R.layout.preference_profile_selector
    }

    override fun onBindView(view: View) {
        Log.i("onBindView")
        super.onBindView(view)

        mProfileSpinner = view.findViewById(R.id.profile_spinner) as Spinner
        mProfileActionButton = view.findViewById(R.id.profile_action_button) as Button

        initLayout()
    }

    private fun initLayout() {
        Log.i("Starting initLayout")
        customShown = false
        mArrayAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item)
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        Profiles.forEach { mArrayAdapter.add(it.name) }
        updateLayout()
    }

    private fun updateLayout() {
        activeProfile.let {
            Log.i("Updating spinner. Active: $it; Custom: ${Config.custom}")
            if (it.isSaved) {
                Log.i("Setting remove button")
                mProfileActionButton.text = getString(R.string.button_remove_filter)
                mProfileActionButton.setOnClickListener { openRemoveProfileDialog() }
            } else {
                Log.i("Setting add button")
                mProfileActionButton.text = getString(R.string.button_add_filter)
                mProfileActionButton.setOnClickListener { openAddNewProfileDialog() }
            }
            showCustom(!Config.custom.isSaved)
            mProfileSpinner.adapter = mArrayAdapter
            mProfileSpinner.setSelection(mArrayAdapter.getPosition(it.name))
            mProfileSpinner.onItemSelectedListener = this
        }
    }

    private fun showCustom(show: Boolean) {
        if (show != customShown) mArrayAdapter.run {
            val custom = getString(R.string.filter_name_custom)
            if (show) insert(custom, 0) else remove(custom)
            customShown = show
        }
    }

    private fun openRemoveProfileDialog() {
        val builder = AlertDialog.Builder(context).apply {
            setTitle(getString(R.string.remove_profile_dialog_title))

            val okString = getString(R.string.button_remove_filter)
            val cancelString = getString(R.string.dialog_button_cancel)

            setNegativeButton(cancelString) { dialog, _ -> dialog.cancel() }
            setPositiveButton(okString) { _, _ ->
                activeProfile.delete()
            }
        }
        builder.show()
    }

    private fun openAddNewProfileDialog() {
        val builder = AlertDialog.Builder(context).apply {
            setTitle(getString(R.string.add_new_profile_dialog_title))

            val nameInput = EditText(context).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                hint = getString(R.string.add_new_profile_edit_hint)
            }
            setView(nameInput)

            val okString = getString(R.string.dialog_button_ok)
            val cancelString = getString(R.string.dialog_button_cancel)

            setNegativeButton(cancelString) { dialog, _ -> dialog.cancel() }
            setPositiveButton(okString) { dialog, _ ->
                nameInput.text.toString().run {
                    // TODO: Also fail if the name is not unique
                    if (trim { it <= ' ' } != "") {
                        activeProfile.saveAs(this)
                    } else {
                        // TODO: Toast, "Please enter a name"
                        dialog.cancel()
                    }
                }
            }
        }
        builder.show()
    }

    //region onItemSelectedListener
    override fun onItemSelected(parent: AdapterView<*>, view: View?,
                                pos: Int, id: Long) {
        Log.i("Item $pos selected")
        if (customShown) {
            if (pos == 0) {
                activeProfile = Config.custom
            } else {
                activeProfile = Profiles[pos - 1]
            }
        } else {
            activeProfile = Profiles[pos]
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) { }
    //endregion

    @Subscribe fun onProfileChanged(profile: Profile) {
        updateLayout()
    }

    @Subscribe fun onProfilesChanged(event: profilesUpdated) {
        initLayout()
    }

    companion object : Logger()
}
