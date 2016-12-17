package com.unclesunny.datetimepreferencetest;

import java.util.Calendar;

import com.unclesunny.datetimepreference.DateTimePreference;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class DateTimePreferenceActivity extends PreferenceActivity {
	private Bundle savedInstanceState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.savedInstanceState = savedInstanceState;
		addPreferencesFromResource(R.xml.preferences);
	}

	public Bundle getBundle() {
		return savedInstanceState;
	}

	public Calendar getCalendarofStart() {
		return DateTimePreference.getDateTimeFor(preferences(), "start_time");
	}

	public Calendar getCalendarofEnd() {
		return DateTimePreference.getDateTimeFor(preferences(), "end_time");
	}

	private SharedPreferences preferences() {
		return PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		getPreference("start_time").setDateTime(prefs.getString("start_time", DateTimePreference.defaultCalendarString()));
	}

	@Override
	protected void onPause() {
		super.onPause();
		Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
		editor.putString("start_time", DateTimePreference.formatter().format(getCalendarofStart().getTime()));
	}
	
	public void clearSharedPreferences(){
		Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
	}

	private DateTimePreference getPreference(String key) {
		return (DateTimePreference) getPreferenceManager().findPreference(key);
	}
}
