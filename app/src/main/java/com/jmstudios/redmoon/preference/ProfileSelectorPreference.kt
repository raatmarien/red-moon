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
import android.content.res.TypedArray
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
import com.jmstudios.redmoon.helper.ProfilesHelper
import com.jmstudios.redmoon.model.ProfilesModel
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.util.Logger

import org.greenrobot.eventbus.Subscribe

import java.util.ArrayList
import java.util.Arrays

class ProfileSelectorPreference(private val mContext: Context, attrs: AttributeSet) : Preference(mContext, attrs), OnItemSelectedListener {

    lateinit private var mProfileSpinner: Spinner
    lateinit private var mProfileActionButton: Button
    lateinit private var mView: View

    lateinit internal var mArrayAdapter: ArrayAdapter<CharSequence>

    private var mProfile: Int = 0
    private val mProfilesModel: ProfilesModel = ProfilesModel(mContext)
    private var mDefaultOperations: ArrayList<CharSequence>? = null

    private var currentColor: Int = 0
    private var currentIntensity: Int = 0
    private var currentDim: Int = 0

    private var mIsListenerRegistered: Boolean = false

    init {
        layoutResource = R.layout.preference_profile_selector
        mIsListenerRegistered = false
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInteger(index, DEFAULT_VALUE)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            mProfile = getPersistedInt(DEFAULT_VALUE)
        } else {
            mProfile = (defaultValue as Int?)?: 0
            persistInt(mProfile)
        }
    }

    override fun onBindView(view: View) {
        super.onBindView(view)

        mView = view

        mProfileSpinner = view.findViewById(R.id.profile_spinner) as Spinner
        mProfileActionButton = view.findViewById(R.id.profile_action_button) as Button

        initLayout()

        updateButtonSetup()
    }

    private fun initLayout() {
        Log.i("Starting initLayout")
        // The default operations first need to be converted to an ArrayList,
        // because the ArrayAdapter will turn it into an AbstractList otherwise,
        // which doesn't support certain actions, like adding elements.
        // See: http://stackoverflow.com/a/3200631
        mDefaultOperations = ArrayList<CharSequence>(Arrays.asList(*context.resources.getStringArray(R.array.standard_profiles_array)))
        mArrayAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, mDefaultOperations)
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        readProfiles()

        mProfileSpinner.adapter = mArrayAdapter
        mProfileSpinner.setSelection(mProfile)
        mProfileSpinner.onItemSelectedListener = this
    }

    private fun updateButtonSetup() {
        if (mProfile > DEFAULT_OPERATIONS_AM - 1) {
            Log.i("Setting remove button")
            mProfileActionButton.text = context.resources.getString(R.string.button_remove_profile)
            mProfileActionButton.setOnClickListener { openRemoveProfileDialog() }

        } else {
            Log.i("Setting add button")
            mProfileActionButton.text = context.resources.getString(R.string.button_add_profile)
            mProfileActionButton.setOnClickListener { openAddNewProfileDialog() }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View,
                                pos: Int, id: Long) {
        Log.i("Item $pos selected")
        mProfile = pos
        persistInt(mProfile)
        updateButtonSetup()

        // Update the dependent settings
        if (mProfile != 0) {
            // We need a ProfilesModel to get the properties of the
            // profile from the index
            val profilesModel = ProfilesModel(mContext)
            val profileObject = ProfilesHelper.getProfile(profilesModel, mProfile, mContext)

            Config.dim = profileObject.mDimProgress
            Config.intensity = profileObject.mIntensityProgress
            Config.color = profileObject.mColorProgress
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) { }

    private fun openRemoveProfileDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.remove_filter_dialog_title))

        val okString = context.resources.getString(R.string.button_remove_profile)
        val cancelString = context.resources.getString(R.string.cancel_dialog)

        builder.setPositiveButton(okString) { _, _ ->
            mProfilesModel.removeProfile(mProfile - DEFAULT_OPERATIONS_AM)
            mProfile = 0
            initLayout()

            updateAmountProfiles()
        }

        builder.setNegativeButton(cancelString) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun openAddNewProfileDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.add_new_filter_dialog_title))

        val nameInput = EditText(context)
        nameInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        nameInput.hint = context.resources.getString(R.string.add_new_filter_edit_hint)

        builder.setView(nameInput)

        val okString = context.resources.getString(R.string.ok_dialog)
        val cancelString = context.resources.getString(R.string.cancel_dialog)

        builder.setPositiveButton(okString) { dialog, _ ->
            if (nameInput.text.toString().trim { it <= ' ' } != "") {
                val profile = ProfilesModel.Profile(nameInput.text.toString(),
                                                    Config.color,
                                                    Config.intensity,
                                                    Config.dim)

                mProfilesModel.addProfile(profile)
                mArrayAdapter.add(profile.mProfileName as CharSequence)

                mProfileSpinner.setSelection(mProfilesModel.profiles.size - 1 + DEFAULT_OPERATIONS_AM)

                updateAmountProfiles()
            } else {
                dialog.cancel()
            }
        }

        builder.setNegativeButton(cancelString) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    //Section: Reading and writing profiles

    /**
     * Reads the profiles saved in the SharedPreference in the spinner
     */
    fun readProfiles() {
        val profiles = mProfilesModel.profiles

        for (profile in profiles) {
            mArrayAdapter.add(profile.mProfileName as CharSequence)
        }
    }

    /**
     * Updates the amount of profiles in the shared preferences
     */
    private fun updateAmountProfiles() {
        val amountProfiles = mProfilesModel.profiles.size + DEFAULT_OPERATIONS_AM
        Log.i("There are now $amountProfiles profiles.")
        Config.amountProfiles = amountProfiles
    }

    @Subscribe
    fun onDimLevelChanged(event: dimChanged) {
        if (Config.dim != currentDim) mProfileSpinner.setSelection(0)
    }

    @Subscribe
    fun onIntensityLevelChanged(event: intensityChanged) {
        if (Config.intensity != currentIntensity) mProfileSpinner.setSelection(0)
    }

    @Subscribe
    fun onColorChanged(event: colorChanged) {
        if (Config.color != currentColor) mProfileSpinner.setSelection(0)
    }

    @Subscribe
    fun onProfileChanged(event: profileChanged) {
        mProfile = Config.profile
        mProfileSpinner.setSelection(mProfile)

        if (mProfile != 0) {
            val newProfile = ProfilesHelper.getProfile(mProfilesModel, mProfile, mContext)

            currentDim = newProfile.mDimProgress
            currentIntensity = newProfile.mIntensityProgress
            currentColor = newProfile.mColorProgress
        }
    }

    companion object : Logger() {
        const val DEFAULT_VALUE = 1
        const val DEFAULT_OPERATIONS_AM = 3
    }
}
