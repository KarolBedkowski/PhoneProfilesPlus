package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

public class VolumeDialogPreferenceFragmentX extends PreferenceDialogFragmentCompat
        implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener,
                   AdapterView.OnItemSelectedListener
{

    private Context context;
    private VolumeDialogPreferenceX preference;

    private AppCompatSpinner operatorSpinner = null;
    private SeekBar seekBar = null;
    private TextView valueText = null;
    //private CheckBox sharedProfileChBox = null;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        this.context = context;
        preference = (VolumeDialogPreferenceX) getPreference();
        preference.fragment = this;

        /*
        final Context _context = context;
        PPApplication.startHandlerThreadPlayTone();
        final Handler handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (preference.volumeType.equalsIgnoreCase("RINGTONE")) {
                        String ringtoneUri = Settings.System.DEFAULT_RINGTONE_URI.toString();
                        Uri _ringtoneUri = Uri.parse(ringtoneUri);
                        preference.mediaPlayer = MediaPlayer.create(_context, _ringtoneUri);
                    }
                    else if (preference.volumeType.equalsIgnoreCase("NOTIFICATION")) {
                        String ringtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI.toString();
                        Uri _ringtoneUri = Uri.parse(ringtoneUri);
                        preference.mediaPlayer = MediaPlayer.create(_context, _ringtoneUri);
                    }
                    else if (preference.volumeType.equalsIgnoreCase("MEDIA"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if (preference.volumeType.equalsIgnoreCase("ALARM")) {
                        String ringtoneUri = Settings.System.DEFAULT_ALARM_ALERT_URI.toString();
                        Uri _ringtoneUri = Uri.parse(ringtoneUri);
                        preference.mediaPlayer = MediaPlayer.create(_context, _ringtoneUri);
                    }
                    else if (preference.volumeType.equalsIgnoreCase("SYSTEM"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if (preference.volumeType.equalsIgnoreCase("VOICE"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if (preference.volumeType.equalsIgnoreCase("DTMF"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if ((Build.VERSION.SDK_INT >= 26) && preference.volumeType.equalsIgnoreCase("ACCESSIBILITY"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else if (preference.volumeType.equalsIgnoreCase("BLUETOOTHSCO"))
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);
                    else
                        preference.mediaPlayer = MediaPlayer.create(_context, R.raw.volume_change_notif);

                    if (preference.mediaPlayer != null) {
                        AudioAttributes attrs = new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build();
                        preference.mediaPlayer.setAudioAttributes(attrs);
                        //preference.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    }
                } catch (Exception ignored) {}
            }
        });
        */

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_volume_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("VolumeDialogPreferenceFragmentX.onBindDialogView", "value=" + preference.value);
            PPApplication.logE("VolumeDialogPreferenceFragmentX.onBindDialogView", "noChange=" + preference.noChange);
            //PPApplication.logE("VolumeDialogPreferenceFragmentX.onBindDialogView", "sharedProfile="+preference.sharedProfile);
        }*/

        CheckBox noChangeChBox = view.findViewById(R.id.volumePrefDialogNoChange);
        operatorSpinner = view.findViewById(R.id.volumePrefDialogVolumesSensorOperator);

        if (preference.forVolumesSensor == 1) {
            GlobalGUIRoutines.HighlightedSpinnerAdapter voiceSpinnerAdapter = new GlobalGUIRoutines.HighlightedSpinnerAdapter(
                    (EventsPrefsActivity) context,
                    R.layout.highlighted_spinner,
                    getResources().getStringArray(R.array.volumesSensorOperatorArray));
            voiceSpinnerAdapter.setDropDownViewResource(R.layout.highlighted_spinner_dropdown);
            operatorSpinner.setAdapter(voiceSpinnerAdapter);
            operatorSpinner.setPopupBackgroundResource(R.drawable.popupmenu_background);
            operatorSpinner.setBackgroundTintList(ContextCompat.getColorStateList(context/*getBaseContext()*/, R.color.highlighted_spinner_all));
        }

        seekBar = view.findViewById(R.id.volumePrefDialogSeekbar);
        valueText = view.findViewById(R.id.volumePrefDialogValueText);
        //sharedProfileChBox = view.findViewById(R.id.volumePrefDialogSharedProfile);

        seekBar.setKeyProgressIncrement(preference.stepSize);
        seekBar.setMax(preference.maximumValue/* - preference.minimumValue*/);
        seekBar.setProgress(preference.value);

        valueText.setText(String.valueOf(preference.value/* + preference.minimumValue*/));

        if (preference.forVolumesSensor == 0) {
            operatorSpinner.setVisibility(View.GONE);
            if (noChangeChBox != null) {
                noChangeChBox.setVisibility(View.VISIBLE);
                noChangeChBox.setChecked((preference.noChange == 1));
            }
        }
        else {
            if (noChangeChBox != null) {
                noChangeChBox.setVisibility(View.GONE);
            }
            operatorSpinner.setVisibility(View.VISIBLE);
            String[] entryValues = getResources().getStringArray(R.array.volumesSensorOperatorValues);
            int operatorIdx = 0;
            for (String entryValue : entryValues) {
                if (entryValue.equals(String.valueOf(preference.sensorOperator))) {
                    break;
                }
                ++operatorIdx;
            }
            operatorSpinner.setSelection(operatorIdx);
        }

        //sharedProfileChBox.setChecked((preference.sharedProfile == 1));
        //sharedProfileChBox.setEnabled(preference.disableSharedProfile == 0);

        //if (preference.noChange == 1)
        //    sharedProfileChBox.setChecked(false);
        //if (preference.sharedProfile == 1)
        //    noChangeChBox.setChecked(false);

        if (preference.forVolumesSensor == 0) {
            valueText.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
            seekBar.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
        }
        else {
            valueText.setEnabled(preference.sensorOperator != 0);
            seekBar.setEnabled(preference.sensorOperator != 0);
        }

        seekBar.setOnSeekBarChangeListener(this);
        if (preference.forVolumesSensor == 0) {
            if (noChangeChBox != null)
                noChangeChBox.setOnCheckedChangeListener(this);
        }
        else
            operatorSpinner.setOnItemSelectedListener(this);
        //sharedProfileChBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        }
        else {
            preference.resetSummary();
        }

        final Context appContext = context.getApplicationContext();
        final AudioManager audioManager = preference.audioManager;
        //PPApplication.startHandlerThreadPlayTone();
        //final Handler __handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
        //__handler.post(new StopPlayRingtoneRunnable(
        //        context.getApplicationContext(), preference.audioManager) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadPlayTone", "START run - from=VolumeDialogPreferenceFragmentX.onDialogClosed");

            //Context appContext = appContextWeakRef.get();
            //AudioManager audioManager = audioManagerWeakRef.get();

            //if ((appContext != null) && (audioManager != null)) {
                if (preference.defaultValueMusic != -1)
                    ActivateProfileHelper.setMediaVolume(appContext, audioManager, preference.defaultValueMusic);
                if (preference.oldMediaMuted) {
                    EventPreferencesVolumes.internalChange = true;
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                    PPPExecutors.scheduleDisableVolumesInternalChangeExecutor();
                }
                if (VolumeDialogPreferenceX.mediaPlayer != null) {
                    try {
                        if (VolumeDialogPreferenceX.mediaPlayer.isPlaying())
                            VolumeDialogPreferenceX.mediaPlayer.stop();
                    } catch (Exception e) {
                        //PPApplication.recordException(e);
                    }
                    try {
                        VolumeDialogPreferenceX.mediaPlayer.release();
                    } catch (Exception e) {
                        //PPApplication.recordException(e);
                    }
                }
            //}
        }; //);
        PPApplication.playToneExecutor.submit(runnable);

        preference.fragment = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (preference.forVolumesSensor == 0) {

            if (buttonView.getId() == R.id.volumePrefDialogNoChange) {
                preference.noChange = (isChecked) ? 1 : 0;

                valueText.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);
                seekBar.setEnabled((preference.noChange == 0) /*&& (preference.sharedProfile == 0)*/);

                //if (isChecked)
                //    sharedProfileChBox.setChecked(false);
            }

            /*
            if (buttonView.getId() == R.id.volumePrefDialogSharedProfile)
            {
                preference.sharedProfile = (isChecked)? 1 : 0;

                valueText.setEnabled((preference.noChange == 0) && (preference.sharedProfile == 0));
                seekBar.setEnabled((preference.noChange == 0) && (preference.sharedProfile == 0));
                if (isChecked)
                    noChangeChBox.setChecked(false);
            }
            */

            preference.callChangeListener(preference.getSValue());
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("VolumeDialogPreferenceFragmentX.onProgressChanged", "progress=" + progress);
            PPApplication.logE("VolumeDialogPreferenceFragmentX.onProgressChanged", "fromUser=" + fromUser);
        }*/

        if (fromUser) {
            // Round the value to the closest integer value.
            //noinspection ConstantConditions
            if (preference.stepSize >= 1) {
                preference.value = Math.round((float) progress / preference.stepSize) * preference.stepSize;
            } else {
                preference.value = progress;
            }

            // Set the valueText text.
            valueText.setText(String.valueOf(preference.value/* + preference.minimumValue*/));

            preference.callChangeListener(preference.getSValue());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //if (preference.mediaPlayer != null) {
            int volume;
            if ((preference.volumeType != null) && preference.volumeType.equalsIgnoreCase("MEDIA"))
                volume = preference.value;
            else {
                float percentage = (float) preference.value / preference.maximumValue * 100.0f;
                volume = Math.round(preference.maximumMediaValue / 100.0f * percentage);
            }

            if (preference.oldMediaMuted && (preference.audioManager != null)) {
                EventPreferencesVolumes.internalChange = true;
                preference.audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                PPPExecutors.scheduleDisableVolumesInternalChangeExecutor();
            }
            ActivateProfileHelper.setMediaVolume(context, preference.audioManager, volume);

            final Context appContext = context.getApplicationContext();
            //final AudioManager audioManager = preference.audioManager;
            //PPApplication.startHandlerThreadPlayTone();
            //final Handler __handler = new Handler(PPApplication.handlerThreadPlayTone.getLooper());
            //__handler.post(new PlayRingtoneRunnable(
            //        context.getApplicationContext(), preference.audioManager) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadPlayTone", "START run - from=VolumeDialogPreferenceFragmentX.onStopTrackingTouch");

                //Context appContext = appContextWeakRef.get();
                //AudioManager audioManager = audioManagerWeakRef.get();

                //if ((appContext != null) && (audioManager != null)) {

                    if (VolumeDialogPreferenceX.mediaPlayer != null) {
                        try {
                            if (VolumeDialogPreferenceX.mediaPlayer.isPlaying())
                                VolumeDialogPreferenceX.mediaPlayer.stop();
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }
                        try {
                            VolumeDialogPreferenceX.mediaPlayer.release();
                        } catch (Exception e) {
                            //PPApplication.recordException(e);
                        }
                    }

                    try {
                        if (preference.volumeType.equalsIgnoreCase("RINGTONE")) {
                            Uri _ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE);
                            if ((_ringtoneUri == null) || (_ringtoneUri.toString().equals(TonesHandler.RINGING_TONE_URI_NONE)))
                                VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                            else
                                VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, _ringtoneUri);
                        } else if (preference.volumeType.equalsIgnoreCase("NOTIFICATION")) {
                            Uri _ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_NOTIFICATION);
                            if ((_ringtoneUri == null) || (_ringtoneUri.toString().equals(TonesHandler.NOTIFICATION_TONE_URI_NONE)))
                                VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                            else
                                VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, _ringtoneUri);
                        } else if (preference.volumeType.equalsIgnoreCase("MEDIA")) {
                            Uri _ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE);
                            if ((_ringtoneUri == null) || (_ringtoneUri.toString().equals(TonesHandler.RINGING_TONE_URI_NONE)))
                                VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                            else
                                VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, _ringtoneUri);
                        } else if (preference.volumeType.equalsIgnoreCase("ALARM")) {
                            Uri _ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_ALARM);
                            if ((_ringtoneUri == null) || (_ringtoneUri.toString().equals(TonesHandler.ALARM_TONE_URI_NONE)))
                                VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                            else
                                VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, _ringtoneUri);
                        } else if (preference.volumeType.equalsIgnoreCase("SYSTEM"))
                            VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                        else if (preference.volumeType.equalsIgnoreCase("VOICE")) {
                            Uri _ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE);
                            if ((_ringtoneUri == null) || (_ringtoneUri.toString().equals(TonesHandler.RINGING_TONE_URI_NONE)))
                                VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                            else
                                VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, _ringtoneUri);
                        } else if (preference.volumeType.equalsIgnoreCase("DTMF"))
                            VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                        else if ((Build.VERSION.SDK_INT >= 26) && preference.volumeType.equalsIgnoreCase("ACCESSIBILITY"))
                            VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                        else if (preference.volumeType.equalsIgnoreCase("BLUETOOTHSCO")) {
                            Uri _ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext, RingtoneManager.TYPE_RINGTONE);
                            if ((_ringtoneUri == null) || (_ringtoneUri.toString().equals(TonesHandler.RINGING_TONE_URI_NONE)))
                                VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);
                            else
                                VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, _ringtoneUri);
                        } else
                            VolumeDialogPreferenceX.mediaPlayer = MediaPlayer.create(appContext, R.raw.volume_change_notif);

                        if (VolumeDialogPreferenceX.mediaPlayer != null) {
                            AudioAttributes attrs = new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build();
                            VolumeDialogPreferenceX.mediaPlayer.setAudioAttributes(attrs);
                            //preference.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                            //PPApplication.logE("VolumeDialogPreferenceFragmentX.onStopTrackingTouch", "start playing");
                            VolumeDialogPreferenceX.mediaPlayer.start();
                        }
                    } catch (Exception e) {
                        //Log.e("VolumeDialogPreferenceFragmentX.onStopTrackingTouch", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    }
                //}
            }; //);
            PPApplication.playToneExecutor.submit(runnable);
            /*
            try {
                preference.mediaPlayer.start();
            } catch (Exception ignored) {
            }
            */
        //}
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (preference.forVolumesSensor == 1) {
            ((GlobalGUIRoutines.HighlightedSpinnerAdapter)operatorSpinner.getAdapter()).setSelection(position);

            preference.sensorOperator = Integer.parseInt(preference.operatorValues[position]);

            valueText.setEnabled(preference.sensorOperator != 0);
            seekBar.setEnabled(preference.sensorOperator != 0);

            preference.callChangeListener(preference.getSValue());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

/*    private static abstract class PlayRingtoneRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<AudioManager> audioManagerWeakRef;

        PlayRingtoneRunnable(Context appContext,
                                    AudioManager audioManager) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.audioManagerWeakRef = new WeakReference<>(audioManager);
        }

    }*/

/*    private static abstract class StopPlayRingtoneRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<AudioManager> audioManagerWeakRef;

        StopPlayRingtoneRunnable(Context appContext,
                                        AudioManager audioManager) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.audioManagerWeakRef = new WeakReference<>(audioManager);
        }

    }*/
}
