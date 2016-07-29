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

import android.app.Activity;
import android.util.Log;
import android.os.Bundle;
import android.content.Intent;

import com.jmstudios.redmoon.R;

public class ShortcutCreationActivity extends Activity {
    public static final boolean DEBUG = true;
    public static final String TAG = "ShortcutCreation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.i(TAG, "Create ShortcutCreationActivity");
        super.onCreate(savedInstanceState);

        Intent shortcutIntent = new Intent(this, ShortcutToggleActivity.class);
        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // See: http://www.kind-kristiansen.no/2010/android-adding-desktop-shortcut-support-to-your-app/
        Intent.ShortcutIconResource iconResource =
            Intent.ShortcutIconResource.fromContext(this, R.drawable.toggle_icon);
         
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                        getResources().getString(R.string.toggle_shortcut_title));
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        setResult(RESULT_OK, intent);

        finish();
    }
}
