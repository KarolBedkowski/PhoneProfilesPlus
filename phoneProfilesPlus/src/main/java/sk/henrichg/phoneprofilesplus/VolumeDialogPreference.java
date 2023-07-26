package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class VolumeDialogPreference extends DialogPreference {

    VolumeDialogPreferenceFragment fragment;

    private final Context _context;

    final AudioManager audioManager;

    // Custom xml attributes.
    final int forVolumesSensor;
    final String volumeType;
    int noChange;
    //int sharedProfile;
    //final int disableSharedProfile;
    int sensorOperator;

    int maximumValue = 7;
    //final int minimumValue = 0;
    int maximumMediaValue = 15;
    private int defaultValueRing = 0;
    private int defaultValueNotification = 0;
    int defaultValueMusic = 0;
    private int defaultValueAlarm = 0;
    private int defaultValueSystem = 0;
    private int defaultValueVoice = 0;
    private int defaultValueDTMF = 0;
    private int defaultValueAccessibility = 0;
    private int defaultValueBluetoothSCO = 0;
    final int stepSize = 1;
    boolean oldMediaMuted = false;

    private String sValue = "0|1";
    private String defaultValue;
    private boolean savedInstanceState;

    String[] operatorValues;

    int value = 0;

    public VolumeDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        _context = context;

        //noinspection resource
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
            R.styleable.PPVolumeDialogPreference);

        forVolumesSensor = typedArray.getInteger(
                R.styleable.PPVolumeDialogPreference_forVolumesSensor, 0);

        volumeType = typedArray.getString(
            R.styleable.PPVolumeDialogPreference_volumeType);
        if (forVolumesSensor == 0) {
            noChange = typedArray.getInteger(
                    R.styleable.PPVolumeDialogPreference_vNoChange, 1);
            /*sharedProfile = typedArray.getInteger(
                    R.styleable.VolumeDialogPreference_vSharedProfile, 0);
            disableSharedProfile = typedArray.getInteger(
                    R.styleable.VolumeDialogPreference_vDisableSharedProfile, 0);*/
        }
        else {
            sensorOperator = typedArray.getInteger(
                    R.styleable.PPVolumeDialogPreference_sensorOperator, 0);
            operatorValues = context.getResources().getStringArray(R.array.volumesSensorOperatorValues);
        }

        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager != null) {
            // get max. values from audio manager
            if (volumeType != null) {
                if (volumeType.equalsIgnoreCase("RINGTONE"))
                    maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                else if (volumeType.equalsIgnoreCase("NOTIFICATION"))
                    maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                else if (volumeType.equalsIgnoreCase("MEDIA"))
                    maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                else if (volumeType.equalsIgnoreCase("ALARM"))
                    maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                else if (volumeType.equalsIgnoreCase("SYSTEM"))
                    maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
                else if (volumeType.equalsIgnoreCase("VOICE"))
                    maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
                else if (volumeType.equalsIgnoreCase("DTMF"))
                    maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF);
                else if (/*(Build.VERSION.SDK_INT >= 26) &&*/ volumeType.equalsIgnoreCase("ACCESSIBILITY"))
                    maximumValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY);
                else if (volumeType.equalsIgnoreCase("BLUETOOTHSCO"))
                    maximumValue = audioManager.getStreamMaxVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO);
            }
            maximumMediaValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            // get actual values from audio manager
            defaultValueRing = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            defaultValueNotification = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

            oldMediaMuted = audioManager.isStreamMute(AudioManager.STREAM_MUSIC);
            if (!oldMediaMuted)
                defaultValueMusic = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            else
                defaultValueMusic = -1;
            defaultValueAlarm = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            defaultValueSystem = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
            defaultValueVoice = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            defaultValueDTMF = audioManager.getStreamVolume(AudioManager.STREAM_DTMF);
            //if (Build.VERSION.SDK_INT >= 26)
                defaultValueAccessibility = audioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY);
            defaultValueBluetoothSCO = audioManager.getStreamVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO);

        }

        typedArray.recycle();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        sValue = getPersistedString((String) defaultValue);
        this.defaultValue = (String)defaultValue;
        getValueVDP();
        setSummaryVDP();
    }

    private void getValueVDP()
    {
        String[] splits = sValue.split(StringConstants.STR_SPLIT_REGEX);
        try {
            value = Integer.parseInt(splits[0]);
            if (value == -1)
            {
                if (volumeType.equalsIgnoreCase("SYSTEM"))
                    value = defaultValueSystem;
                else
                if (volumeType.equalsIgnoreCase("RINGTONE"))
                    value = defaultValueRing;
                else
                if (volumeType.equalsIgnoreCase("NOTIFICATION"))
                    value =  defaultValueNotification;
                else
                if (volumeType.equalsIgnoreCase("MEDIA"))
                    value =  defaultValueMusic;
                else
                if (volumeType.equalsIgnoreCase("ALARM"))
                    value =  defaultValueAlarm;
                else
                if (volumeType.equalsIgnoreCase("VOICE"))
                    value =  defaultValueVoice;
                else
                if (volumeType.equalsIgnoreCase("DTMF"))
                    value =  defaultValueDTMF;
                else
                if (volumeType.equalsIgnoreCase("ACCESSIBILITY"))
                    value =  defaultValueAccessibility;
                else
                if (volumeType.equalsIgnoreCase("BLUETOOTHSCO"))
                    value =  defaultValueBluetoothSCO;
            }
        } catch (Exception e) {
            //Log.e("VolumeDialogPreference.getValueVDP", Log.getStackTraceString(e));
            PPApplicationStatic.recordException(e);
            value = 0;
        }
        //value = value - minimumValue;

        if (forVolumesSensor == 0) {
            try {
                noChange = Integer.parseInt(splits[1]);
            } catch (Exception e) {
                noChange = 1;
            }
            /*try {
                sharedProfile = Integer.parseInt(splits[2]);
            } catch (Exception e) {
                sharedProfile = 0;
            }*/
        } else {
            try {
                sensorOperator = Integer.parseInt(splits[1]);
            } catch (Exception e) {
                sensorOperator = 0;
            }
        }


        // You're never know...
        if (value < 0) {
            value = 0;
        }

    }

    private void setSummaryVDP()
    {
        String prefVolumeDataSummary;
        if (forVolumesSensor == 0) {
            if (noChange == 1)
                prefVolumeDataSummary = _context.getString(R.string.preference_profile_no_change);
            /*else
            if (sharedProfile == 1)
                prefVolumeDataSummary = _context.getString(R.string.preference_profile_default_profile);*/
            else
                prefVolumeDataSummary = value + " / " + maximumValue;
        } else {
            String[] entries = _context.getResources().getStringArray(R.array.volumesSensorOperatorArray);
            String[] entryValues = _context.getResources().getStringArray(R.array.volumesSensorOperatorValues);

            int operatorIdx = 0;
            for (String entryValue : entryValues) {
                if (entryValue.equals(String.valueOf(sensorOperator))) {
                    break;
                }
                ++operatorIdx;
            }

            prefVolumeDataSummary = entries[operatorIdx];

            if (sensorOperator != 0) {
                prefVolumeDataSummary = prefVolumeDataSummary + " " + value;
            }
        }
        setSummary(prefVolumeDataSummary);
    }

    String getSValue() {
        if (forVolumesSensor == 0) {
            //int _value = value + minimumValue;
            return value
                    + "|" + noChange
                    + "|" + "0";
        } else {
            return value
                    + "|" + sensorOperator
                    + "|" + "0";
        }
    }

    void persistValue() {
        if (shouldPersist()) {
            persistString(getSValue());
            setSummaryVDP();
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            sValue = getPersistedString(defaultValue);
            getValueVDP();
            setSummaryVDP();
        }
        savedInstanceState = false;
    }

    static boolean changeEnabled(String value) {
        String[] splits = value.split(StringConstants.STR_SPLIT_REGEX);
        if (splits.length > 1) {
            try {
                return Integer.parseInt(splits[1]) == 0;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return false;
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            return superState;
        }*/

        final VolumeDialogPreference.SavedState myState = new VolumeDialogPreference.SavedState(superState);
        myState.sValue = getSValue();
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(VolumeDialogPreference.SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            getValueVDP();
            setSummaryVDP();
            return;
        }

        // restore instance state
        VolumeDialogPreference.SavedState myState = (VolumeDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        sValue = myState.sValue;
        defaultValue = myState.defaultValue;

        getValueVDP();
        setSummaryVDP();
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        String sValue;
        String defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            sValue = source.readString();
            defaultValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeString(sValue);
            dest.writeString(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<VolumeDialogPreference.SavedState>() {
                    public VolumeDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new VolumeDialogPreference.SavedState(in);
                    }
                    public VolumeDialogPreference.SavedState[] newArray(int size)
                    {
                        return new VolumeDialogPreference.SavedState[size];
                    }

                };

    }

}
