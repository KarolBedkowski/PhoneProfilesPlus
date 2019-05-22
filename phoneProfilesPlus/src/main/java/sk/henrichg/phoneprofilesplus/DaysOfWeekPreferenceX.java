package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import androidx.preference.DialogPreference;

public class DaysOfWeekPreferenceX extends DialogPreference {

    DaysOfWeekPreferenceFragmentX fragment;

    static final String allValue = "#ALL#";

    Context context;

    private String value = "";
    String defaultValue;

    final List<DayOfWeek> daysOfWeekList;

    public DaysOfWeekPreferenceX(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        daysOfWeekList = new ArrayList<>();

        //CharSequence[] newEntries = new CharSequence[8];
        //CharSequence[] newEntryValues = new CharSequence[8];

        /*
        String[] newEntries = _context.getResources().getStringArray(R.array.daysOfWeekArray);
        String[] newEntryValues = _context.getResources().getStringArray(R.array.daysOfWeekValues);
        */

        daysOfWeekList.clear();
        DayOfWeek dayOfWeek = new DayOfWeek();
        dayOfWeek.name = context.getString(R.string.array_pref_event_all);
        dayOfWeek.value = allValue;
        daysOfWeekList.add(dayOfWeek);

        String[] namesOfDay = DateFormatSymbols.getInstance().getWeekdays();

        int _dayOfWeek;
        for (int i = 1; i < 8; i++)
        {
            _dayOfWeek = EventPreferencesTime.getDayOfWeekByLocale(i-1);
            Log.e("DaysOfWeekPreferenceX.DaysOfWeekPreferenceX", "_dayOfWeek="+_dayOfWeek);
            Log.e("DaysOfWeekPreferenceX.DaysOfWeekPreferenceX", "namesOfDay[_dayOfWeek+1]="+namesOfDay[_dayOfWeek+1]);

            dayOfWeek = new DayOfWeek();
            dayOfWeek.name = namesOfDay[_dayOfWeek+1];
            dayOfWeek.value = String.valueOf(_dayOfWeek);
            daysOfWeekList.add(dayOfWeek);
        }

    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        // Get the persistent value
        value = getPersistedString((String)defaultValue);
        this.defaultValue = (String)defaultValue;
        getValueDOWMDP();
        setSummaryDOWMDP();
    }

    void getValueDOWMDP()
    {
        // change checked state by value
        if (daysOfWeekList != null)
        {
            Log.e("DaysOfWeekPreferenceX.getValueDOWMDP", "value="+value);
            String[] splits = value.split("\\|");
            boolean allIsConfigured = false;
            for (String split : splits) {
                Log.e("DaysOfWeekPreferenceX.getValueDOWMDP", "split="+split);
                if (split.equals(allValue)) {
                    Log.e("DaysOfWeekPreferenceX.getValueDOWMDP", "allIsConfigured");
                    allIsConfigured = true;
                    for (DayOfWeek dayOfWeek : daysOfWeekList) {
                        dayOfWeek.checked = !dayOfWeek.value.equals(allValue);
                    }
                    break;
                }
            }
            if (!allIsConfigured) {
                for (DayOfWeek dayOfWeek : daysOfWeekList) {
                    dayOfWeek.checked = false;
                    for (String split : splits) {
                        if (dayOfWeek.value.equals(split))
                            dayOfWeek.checked = true;
                    }
                }
            }
        }
    }

    @SuppressWarnings("StringConcatenationInLoop")
    private void setSummaryDOWMDP() {
        String[] namesOfDay = DateFormatSymbols.getInstance().getShortWeekdays();

        String summary = "";

        String[] splits = value.split("\\|");
        boolean allIsConfigured = false;
        boolean[] daySet = new boolean[7];
        for (String split : splits) {
            if (split.equals(allValue)) {
                allIsConfigured = true;
                break;
            }
            daySet[Integer.valueOf(split)] = true;
        }
        if (!allIsConfigured) {
            allIsConfigured = true;
            for (int i = 0; i < 7; i++)
                allIsConfigured = allIsConfigured && daySet[i];
        }
        Log.e("DaysOfWeekPreferenceX.setSummaryDOWMDP", "allIsConfigured");
        if (allIsConfigured)
            summary = summary + context.getString(R.string.array_pref_event_all) + " ";
        else {
            for (String split : splits) {
                for ( int i = 1; i < 8; i++ ) {
                    int _dayOfWeek = EventPreferencesTime.getDayOfWeekByLocale(i-1);
                    if (split.equals(String.valueOf(_dayOfWeek))) {
                        Log.e("DaysOfWeekPreferenceX.setSummaryDOWMDP", "_dayOfWeek="+_dayOfWeek);
                        Log.e("DaysOfWeekPreferenceX.setSummaryDOWMDP", "namesOfDay[_dayOfWeek+1]="+namesOfDay[_dayOfWeek+1]);
                        summary = summary + namesOfDay[_dayOfWeek+1] + " ";
                        break;
                    }
                }
            }
        }

        setSummary(summary);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    void getValue() {
        // fill with days of week separated with |
        value = "";
        if (daysOfWeekList != null)
        {
            for (DayOfWeek dayOfWeek : daysOfWeekList)
            {
                if (dayOfWeek.checked)
                {
                    if (!value.isEmpty())
                        value = value + "|";
                    value = value + dayOfWeek.value;
                }
            }
        }
    }

    void persistValue() {
        if (shouldPersist())
        {
            getValue();
            persistString(value);

            setSummaryDOWMDP();
        }
    }

    void resetSummary() {
        value = getPersistedString(defaultValue);
        setSummaryDOWMDP();
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final DaysOfWeekPreferenceX.SavedState myState = new DaysOfWeekPreferenceX.SavedState(superState);
        myState.value = value;
        myState.defaultValue = defaultValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        //if (dataWrapper == null)
        //    dataWrapper = new DataWrapper(prefContext, false, 0, false);

        if (!state.getClass().equals(DaysOfWeekPreferenceX.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            setSummaryDOWMDP();
            return;
        }

        // restore instance state
        DaysOfWeekPreferenceX.SavedState myState = (DaysOfWeekPreferenceX.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        getValueDOWMDP();
        setSummaryDOWMDP();
        //notifyChanged();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String value;
        String defaultValue;


        SavedState(Parcel source)
        {
            super(source);

            value = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(value);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<DaysOfWeekPreferenceX.SavedState> CREATOR =
                new Creator<DaysOfWeekPreferenceX.SavedState>() {
                    public DaysOfWeekPreferenceX.SavedState createFromParcel(Parcel in)
                    {
                        return new DaysOfWeekPreferenceX.SavedState(in);
                    }
                    public DaysOfWeekPreferenceX.SavedState[] newArray(int size)
                    {
                        return new DaysOfWeekPreferenceX.SavedState[size];
                    }

                };

    }

}
