/*
 *  Copyright (C) 2017 DarkKat
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.darkkatrom.dkweather.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.widget.Toolbar;

import net.darkkatrom.dkweather.R;
import net.darkkatrom.dkweather.fragments.NotificationSettings;
import net.darkkatrom.dkweather.fragments.SettingsFragment;

public class AppSettingsActivity extends Activity implements
        PreferenceFragment.OnPreferenceStartFragmentCallback {

    public static final String EXTRA_SHOW_FRAGMENT             = ":android:show_fragment";
    public static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS   = ":android:show_fragment_args";
    public static final String EXTRA_SHOW_FRAGMENT_TITLE       = ":android:show_fragment_title";
    public static final String EXTRA_SHOW_FRAGMENT_SHORT_TITLE = ":android:show_fragment_short_title";
    public static final String EXTRA_NO_HEADERS                = ":android:no_headers";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTheme().applyStyle(getSettingsThemeResId(), true);
        setContentView(R.layout.settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);

        if (savedInstanceState == null && getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT) == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_content, new SettingsFragment())
                    .commit();
        }
    }

    protected int getSettingsThemeResId() {
        return R.style.AppSettingsTheme;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        startPreferencePanel(pref.getFragment(), pref.getExtras(), pref.getTitleRes(),
                pref.getTitle(), null, 0);
        return true;
    }

    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes,
        CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        Fragment f = Fragment.instantiate(this, fragmentClass, args);
        if (resultTo != null) {
            f.setTargetFragment(resultTo, resultRequestCode);
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_content, f);
        if (titleRes != 0) {
            transaction.setBreadCrumbTitle(titleRes);
        } else if (titleText != null) {
            transaction.setBreadCrumbTitle(titleText);
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }
}
