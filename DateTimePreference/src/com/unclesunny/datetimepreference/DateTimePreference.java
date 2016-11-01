package com.unclesunny.datetimepreference;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

/**
 * 
 * @author UncleSunny
 *
 */
public class DateTimePreference extends DialogPreference
		implements DatePicker.OnDateChangedListener, TimePicker.OnTimeChangedListener {

	private String dateTimeString;
	private String changedValueCanBeNull;
	private DatePicker datePicker;
	private TimePicker timePicker;

	public DateTimePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public DateTimePreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.dialogPreferenceStyle);
	}

	public DateTimePreference(Context context) {
		this(context, null);
	}

	/**
	 * Produces a DateTimePicker set to the time produced by
	 * {@link #getDateTime()}. When overriding be sure to call the super.
	 * 
	 * @return a DateTimePicker with the date set
	 */
	@Override
	protected View onCreateDialogView() {
		setDialogLayoutResource(R.layout.date_time_preference);
		return super.onCreateDialogView();
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		this.datePicker = (DatePicker) view.findViewById(R.id.datepicker);
		this.timePicker = (TimePicker) view.findViewById(R.id.timepicker);
		this.timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(getContext()));
		Calendar calendar = getDateTime();
		this.datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), this);
		this.timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		this.timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
		this.timePicker.setOnTimeChangedListener(this);
	}

	/**
	 * Produces the time used for the time picker. If the user has not selected
	 * a time, produces the default from the XML's android:defaultValue. If the
	 * default is not set in the XML or if the XML's default is invalid it uses
	 * the value produced by {@link #defaultCalendar()}.
	 * 
	 * @return the Calendar for the time picker
	 */
	public Calendar getDateTime() {
		try {
			Date date = formatter().parse(defaultValue());
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			return cal;
		} catch (java.text.ParseException e) {
			return defaultCalendar();
		}
	}

	/**
	 * Set the selected date and time to the specified string.
	 * 
	 * @param dateTimeString
	 *            The date, represented as a string, in the format specified by
	 *            {@link #formatter()}.
	 */
	public void setDateTime(String dateTimeString) {
		this.dateTimeString = dateTimeString;
	}

	/**
	 * Produces the date formatter used for times in the XML. The default is
	 * yyyy.MM.dd HH:mm. Override this to change that.
	 * 
	 * @return the SimpleDateFormat used for XML times
	 */
	public static DateFormat formatter() {
		return new SimpleDateFormat("yyyy.MM.dd HH:mm");
	}

	/**
	 * Produces the date formatter used for showing the time in the summary.
	 * Override this to change it.
	 * 
	 * @return the SimpleDateFormat used for summary dates
	 */
	public static DateFormat summaryFormatter(Context context) {
		return new SimpleDateFormat("MMMM dd, yyyy    HH:mm");
	}

	public static DateFormat summaryDateFormatter(Context context) {
		return android.text.format.DateFormat.getMediumDateFormat(context);
	}

	public static DateFormat summaryTimeFormatter(Context context) {
		return android.text.format.DateFormat.getTimeFormat(context);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	/**
	 * Called when the time picker is shown or restored. If it's a restore it
	 * gets the persisted value, otherwise it persists the value.
	 */
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object def) {
		if (restoreValue) {
			this.dateTimeString = getPersistedString(defaultValue());
			setTheDateTime(this.dateTimeString);
		} else {
			boolean wasNull = this.dateTimeString == null;
			setDateTime((String) def);
			if (!wasNull)
				persistDateTime(this.dateTimeString);
		}
	}

	/**
	 * Called when Android pauses the activity.
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
		if (isPersistent())
			return super.onSaveInstanceState();
		else
			return new SavedState(super.onSaveInstanceState());
	}

	/**
	 * Called when Android restores the activity.
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState s = (SavedState) state;
		super.onRestoreInstanceState(s.getSuperState());
		setTheDateTime(s.dateValue);
	}

	/**
	 * Called when the user changes the date.
	 */
	public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		onTimeChanged(null, 0, 0);
	}

	/**
	 * Called when the user changes the time.
	 */
	public void onTimeChanged(TimePicker view, int hour, int minute) {
		Calendar selected = new GregorianCalendar(this.datePicker.getYear(), this.datePicker.getMonth(),
				this.datePicker.getDayOfMonth(), this.timePicker.getCurrentHour(), this.timePicker.getCurrentMinute());
		this.changedValueCanBeNull = formatter().format(selected.getTime());
		getDialog().setTitle(summaryDateFormatter(getContext()).format(selected.getTime()) + "    "
				+ summaryTimeFormatter(getContext()).format(selected.getTime()));
	}

	/**
	 * Called when the dialog is closed. If the close was by preOssing
	 * DialogInterface.BUTTON_POSITIVE it saves the value.
	 */
	@Override
	protected void onDialogClosed(boolean shouldSave) {
		if (shouldSave) {
			if (this.changedValueCanBeNull != null) {
				setTheDateTime(this.changedValueCanBeNull);
				this.changedValueCanBeNull = null;
			} else {
				Calendar selected = new GregorianCalendar(this.datePicker.getYear(), this.datePicker.getMonth(),
						this.datePicker.getDayOfMonth(), this.timePicker.getCurrentHour(),
						this.timePicker.getCurrentMinute());
				setTheDateTime(formatter().format(selected.getTime()));
			}
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (getDialog().getCurrentFocus() != null)
			getDialog().getCurrentFocus().clearFocus();
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		onTimeChanged(null, 0, 0);
	}

	private void setTheDateTime(String s) {
		setDateTime(s);
		persistDateTime(s);
	}

	private void persistDateTime(String s) {
		persistString(s);
		setSummary(summaryDateFormatter(getContext()).format(getDateTime().getTime()) + "    "
				+ summaryTimeFormatter(getContext()).format(getDateTime().getTime()));
	}

	/**
	 * The default time to use when the XML does not set it or the XML has an
	 * error.
	 * 
	 * @return the Calendar set to the default date
	 */
	public static Calendar defaultCalendar() {
		// return new GregorianCalendar(1970, 0, 1, 0, 0);
		return new GregorianCalendar();
	}

	/**
	 * The defaultCalendar() as a string using the {@link #formatter()}.
	 * 
	 * @return a String representation of the default time
	 */
	public static String defaultCalendarString() {
		return formatter().format(defaultCalendar().getTime());
	}

	private String defaultValue() {
		if (this.dateTimeString == null)
			setDateTime(defaultCalendarString());
		return this.dateTimeString;
	}

	/**
	 * Produces the date the user has selected for the given preference, as a
	 * calendar.
	 * 
	 * @param preferences
	 *            the SharedPreferences to get the date from
	 * @param field
	 *            the name of the preference to get the date from
	 * @return a Calendar that the user has selected
	 */
	public static Calendar getDateTimeFor(SharedPreferences preferences, String field) {
		Date date = stringToDate(preferences.getString(field, defaultCalendarString()));
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	private static Date stringToDate(String dateTimeString) {
		try {
			return formatter().parse(dateTimeString);
		} catch (ParseException e) {
			return defaultCalendar().getTime();
		}
	}

	private static class SavedState extends BaseSavedState {
		String dateValue;

		public SavedState(Parcel p) {
			super(p);
			dateValue = p.readString();
		}

		public SavedState(Parcelable p) {
			super(p);
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeString(dateValue);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

}