package com.cs2110Project.guide;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	public static final String SETTINGS_NAME = "settingsFile";
	private SharedPreferences prefManager;
	private ListPreference listPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.activitypreferences);
		prefManager = PreferenceManager.getDefaultSharedPreferences(this);
		prefManager.registerOnSharedPreferenceChangeListener(this);

		listPref = new ListPreference(this);
		CharSequence[] entries = { "Easy", "Medium", "Hard" };
		CharSequence[] entryValues = { "1", "2", "3" };
		listPref.setEntryValues(entryValues);
		listPref.setEntries(entries);
		listPref.setTitle("Difficulty");
		listPref.setDefaultValue("2");
		listPref.setKey("PREF_ZOMBIE_SPEED");
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
		if (pref.getBoolean("PREF_ZOMBIE_MODE", false)) {
			((PreferenceScreen) findPreference("PREFMAIN"))
					.addPreference(listPref);
		} else {
			((PreferenceScreen) findPreference("PREFMAIN"))
					.removePreference(listPref);

		}

	}
}
