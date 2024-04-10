package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
class EventPreferencesCall extends EventPreferences {

    int _callEvent;
    String _contacts; // contactId#phoneId|...
    String _contactGroups; // groupId|...
    int _contactListType;
    boolean _runAfterCallEndPermanentRun;
    int _runAfterCallEndDuration;
    int _forSIMCard;
    boolean _stopRinging;
    boolean _sendSMS;
    String _smsText;
    int _ringingDuration;

    long _runAfterCallEndTime;
    int _runAfterCallEndFromSIMSlot;
    long _ringingTime;
    int _ringingFromSIMSlot;

    static final String PREF_EVENT_CALL_ENABLED = "eventCallEnabled";
    private static final String PREF_EVENT_CALL_EVENT = "eventCallEvent";
    static final String PREF_EVENT_CALL_CONTACTS = "eventCallContacts";
    static final String PREF_EVENT_CALL_CONTACT_GROUPS = "eventCallContactGroups";
    private static final String PREF_EVENT_CALL_CONTACT_LIST_TYPE = "eventCallContactListType";
    private static final String PREF_EVENT_CALL_RUN_AFTER_CALL_END_PERMANENT_RUN = "eventCallPermanentRun";
    private static final String PREF_EVENT_CALL_RUN_AFTER_CALL_END_DURATION = "eventCallDuration";
    static final String PREF_EVENT_CALL_EXTENDER = "eventCallExtender";
    //static final String PREF_EVENT_CALL_INSTALL_EXTENDER = "eventCallInstallExtender";
    //static final String PREF_EVENT_CALL_ACCESSIBILITY_SETTINGS = "eventCallAccessibilitySettings";
    //static final String PREF_EVENT_CALL_LAUNCH_EXTENDER = "eventCallLaunchExtender";
    private static final String PREF_EVENT_CALL_FOR_SIM_CARD = "eventCallForSimCard";
    private static final String PREF_EVENT_CALL_STOP_RINGING = "eventCallStopRinging";
    private static final String PREF_EVENT_CALL_SEND_SMS = "eventCallSendSMS";
    private static final String PREF_EVENT_CALL_SMS_TEXT = "eventCallSMSText";
    private static final String PREF_EVENT_CALL_RINGING_DURATION = "eventCallRingingDuration";
    private static final String PREF_EVENT_CALL_STOP_RINGING_INFO = "eventCallStopRingingInfo";

    static final String PREF_EVENT_CALL_ENABLED_NO_CHECK_SIM = "eventCallEnabledEnabledNoCheckSim";

    static final String PREF_EVENT_CALL_CATEGORY = "eventCallCategoryRoot";

    static final int CALL_EVENT_RINGING = 0;
    private static final int CALL_EVENT_INCOMING_CALL_ANSWERED = 1;
    private static final int CALL_EVENT_OUTGOING_CALL_STARTED = 2;
    static final int CALL_EVENT_MISSED_CALL = 3;
    static final int CALL_EVENT_INCOMING_CALL_ENDED = 4;
    static final int CALL_EVENT_OUTGOING_CALL_ENDED = 5;

    static final int CONTACT_LIST_TYPE_WHITE_LIST = 0;
    static final int CONTACT_LIST_TYPE_BLACK_LIST = 1;
    static final int CONTACT_LIST_TYPE_NOT_USE = 2;

    static final int PHONE_CALL_EVENT_UNDEFINED = 0;
    private static final int PHONE_CALL_EVENT_INCOMING_CALL_RINGING = 1;
    //static final int PHONE_CALL_EVENT_OUTGOING_CALL_STARTED = 2;
    private static final int PHONE_CALL_EVENT_INCOMING_CALL_ANSWERED = 3;
    private static final int PHONE_CALL_EVENT_OUTGOING_CALL_ANSWERED = 4;
    private static final int PHONE_CALL_EVENT_INCOMING_CALL_ENDED = 5;
    private static final int PHONE_CALL_EVENT_OUTGOING_CALL_ENDED = 6;
    private static final int PHONE_CALL_EVENT_MISSED_CALL = 7;
    private static final int PHONE_CALL_EVENT_SERVICE_UNBIND = 8;

    private static final String PREF_EVENT_CALL_EVENT_TYPE = "eventCallEventType";
    private static final String PREF_EVENT_CALL_PHONE_NUMBER = "eventCallPhoneNumber";
    private static final String PREF_EVENT_CALL_RUN_AFTER_CALL_END_TIME = "eventCallEventTime";
    private static final String PREF_EVENT_CALL_RUN_AFTER_CALL_END_FROM_SIM_SLOT = "eventCallSIMSlot";
    private static final String PREF_EVENT_CALL_RINGING_TIME = "eventCallRingingTime";
    private static final String PREF_EVENT_CALL_RINGING_FROM_SIM_SLOT = "eventCallRingingSIMSlot";

    EventPreferencesCall(Event event,
                         boolean enabled,
                         int callEvent,
                         String contacts,
                         String contactGroups,
                         int contactListType,
                         boolean runAfterCallEndPermanentRun,
                         int runAfterCallEndDuration,
                         int forSIMCard,
                         boolean stopRinging,
                         boolean sendSMS,
                         String smsText,
                         int ringingDuration) {
        super(event, enabled);

        this._callEvent = callEvent;
        this._contacts = contacts;
        this._contactGroups = contactGroups;
        this._contactListType = contactListType;
        this._runAfterCallEndPermanentRun = runAfterCallEndPermanentRun;
        this._runAfterCallEndDuration = runAfterCallEndDuration;
        this._forSIMCard = forSIMCard;
        this._stopRinging = stopRinging;
        this._sendSMS = sendSMS;
        this._smsText = smsText;
        this._ringingDuration = ringingDuration;

        this._runAfterCallEndTime = 0;
        this._runAfterCallEndFromSIMSlot = 0;
    }

    void copyPreferences(Event fromEvent) {
        this._enabled = fromEvent._eventPreferencesCall._enabled;
        this._callEvent = fromEvent._eventPreferencesCall._callEvent;
        this._contacts = fromEvent._eventPreferencesCall._contacts;
        this._contactGroups = fromEvent._eventPreferencesCall._contactGroups;
        this._contactListType = fromEvent._eventPreferencesCall._contactListType;
        this._runAfterCallEndPermanentRun = fromEvent._eventPreferencesCall._runAfterCallEndPermanentRun;
        this._runAfterCallEndDuration = fromEvent._eventPreferencesCall._runAfterCallEndDuration;
        this._forSIMCard = fromEvent._eventPreferencesCall._forSIMCard;
        this._stopRinging = fromEvent._eventPreferencesCall._stopRinging;
        this._sendSMS = fromEvent._eventPreferencesCall._sendSMS;
        this._smsText = fromEvent._eventPreferencesCall._smsText;
        this._ringingDuration = fromEvent._eventPreferencesCall._ringingDuration;
        this.setSensorPassed(fromEvent._eventPreferencesCall.getSensorPassed());

        this._runAfterCallEndTime = 0;
        this._runAfterCallEndFromSIMSlot = 0;
    }

    void loadSharedPreferences(SharedPreferences preferences) {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_CALL_ENABLED, _enabled);
        editor.putString(PREF_EVENT_CALL_EVENT, String.valueOf(this._callEvent));
        editor.putString(PREF_EVENT_CALL_CONTACTS, this._contacts);
        editor.putString(PREF_EVENT_CALL_CONTACT_GROUPS, this._contactGroups);
        editor.putString(PREF_EVENT_CALL_CONTACT_LIST_TYPE, String.valueOf(this._contactListType));
        editor.putBoolean(PREF_EVENT_CALL_RUN_AFTER_CALL_END_PERMANENT_RUN, this._runAfterCallEndPermanentRun);
        editor.putString(PREF_EVENT_CALL_RUN_AFTER_CALL_END_DURATION, String.valueOf(this._runAfterCallEndDuration));
        editor.putString(PREF_EVENT_CALL_FOR_SIM_CARD, String.valueOf(this._forSIMCard));
        editor.putBoolean(PREF_EVENT_CALL_STOP_RINGING, this._stopRinging);
        editor.putBoolean(PREF_EVENT_CALL_SEND_SMS, this._sendSMS);
        editor.putString(PREF_EVENT_CALL_SMS_TEXT, this._smsText);
        editor.putString(PREF_EVENT_CALL_RINGING_DURATION, String.valueOf(this._ringingDuration));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences) {
        this._enabled = preferences.getBoolean(PREF_EVENT_CALL_ENABLED, false);
        this._callEvent = Integer.parseInt(preferences.getString(PREF_EVENT_CALL_EVENT, "0"));
        this._contacts = preferences.getString(PREF_EVENT_CALL_CONTACTS, "");
        this._contactGroups = preferences.getString(PREF_EVENT_CALL_CONTACT_GROUPS, "");
        this._contactListType = Integer.parseInt(preferences.getString(PREF_EVENT_CALL_CONTACT_LIST_TYPE, "0"));
        this._runAfterCallEndPermanentRun = preferences.getBoolean(PREF_EVENT_CALL_RUN_AFTER_CALL_END_PERMANENT_RUN, false);
        this._runAfterCallEndDuration = Integer.parseInt(preferences.getString(PREF_EVENT_CALL_RUN_AFTER_CALL_END_DURATION, "5"));
        this._forSIMCard = Integer.parseInt(preferences.getString(PREF_EVENT_CALL_FOR_SIM_CARD, "0"));
        this._stopRinging = preferences.getBoolean(PREF_EVENT_CALL_STOP_RINGING, false);
        this._sendSMS = preferences.getBoolean(PREF_EVENT_CALL_SEND_SMS, false);
        this._smsText = preferences.getString(PREF_EVENT_CALL_SMS_TEXT, "");
        this._ringingDuration = Integer.parseInt(preferences.getString(PREF_EVENT_CALL_RINGING_DURATION, "5"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        StringBuilder _value = new StringBuilder();

        if (!this._enabled) {
            if (!addBullet)
                _value.append(context.getString(R.string.event_preference_sensor_call_summary));
        } else {
            if (addBullet) {
                _value.append(StringConstants.TAG_BOLD_START_HTML);
                _value.append(getPassStatusString(context.getString(R.string.event_type_call), addPassStatus, DatabaseHandler.ETYPE_CALL, context));
                _value.append(StringConstants.TAG_BOLD_END_WITH_SPACE_HTML);
            }

            PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_CALL_ENABLED, context);
            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext());
                if (extenderVersion == 0) {
                    _value.append(context.getString(R.string.profile_preferences_device_not_allowed))
                            .append(StringConstants.STR_COLON_WITH_SPACE).append(context.getString(R.string.preference_not_allowed_reason_not_extender_installed));
                } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_8_1_3) {
                    _value.append(context.getString(R.string.profile_preferences_device_not_allowed))
                            .append(StringConstants.STR_COLON_WITH_SPACE).append(context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded));
                } else if (!PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context.getApplicationContext(), false, true
                        /*, "EventPreferencesCall.getPreferencesDescription"*/)) {
                    _value.append(context.getString(R.string.profile_preferences_device_not_allowed))
                            .append(StringConstants.STR_COLON_WITH_SPACE).append(context.getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender));
                } else if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                    _value.append(context.getString(R.string.profile_preferences_device_not_allowed))
                            .append(StringConstants.STR_COLON_WITH_SPACE).append(context.getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined));
                } else {
                    _value.append(context.getString(R.string.pref_event_call_event));
                    String[] callEvents = context.getResources().getStringArray(R.array.eventCallEventsArray);
                    _value.append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(callEvents[this._callEvent], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                    _value.append(context.getString(R.string.event_preferences_call_contact_groups)).append(StringConstants.STR_COLON_WITH_SPACE);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(ContactGroupsMultiSelectDialogPreference.getSummary(_contactGroups, context), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                    _value.append(context.getString(R.string.event_preferences_call_contacts)).append(StringConstants.STR_COLON_WITH_SPACE);
                    _value.append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(ContactsMultiSelectDialogPreference.getSummary(_contacts, false, context), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML).append(StringConstants.STR_BULLET);

                    _value.append(context.getString(R.string.event_preferences_contactListType));
                    String[] contactListTypes = context.getResources().getStringArray(R.array.eventCallContactListTypeArray);
                    _value.append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(contactListTypes[this._contactListType], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);

                        boolean hasSIMCard = false;
                        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (telephonyManager != null) {
                            int phoneCount = telephonyManager.getPhoneCount();
                            if (phoneCount > 1) {
                                boolean simExists;
//                                Log.e("EventPreferencesCall.getPreferencesDescription", "called hasSIMCard");
                                HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                                boolean sim1Exists = hasSIMCardData.hasSIM1;
                                boolean sim2Exists = hasSIMCardData.hasSIM2;

                                simExists = sim1Exists;
                                simExists = simExists && sim2Exists;
                                hasSIMCard = simExists;
                            }
                        }
                        if (hasSIMCard) {
                            _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_call_forSimCard));
                            String[] forSimCard = context.getResources().getStringArray(R.array.eventCallForSimCardArray);
                            _value.append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(forSimCard[this._forSIMCard], disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        }

                    if ((this._callEvent == CALL_EVENT_MISSED_CALL) ||
                            (this._callEvent == CALL_EVENT_INCOMING_CALL_ENDED) ||
                            (this._callEvent == CALL_EVENT_OUTGOING_CALL_ENDED)) {
                        if (this._runAfterCallEndPermanentRun)
                            _value.append(StringConstants.STR_BULLET).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.pref_event_permanentRun), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                        else
                            _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.pref_event_duration)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(this._runAfterCallEndDuration), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                    }

                    if (this._callEvent == CALL_EVENT_RINGING) {
                        if (this._stopRinging) {
                            _value.append(StringConstants.STR_BULLET).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.event_preferences_call_stop_ringing), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                            _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_call_ringing_duration)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(this._ringingDuration), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                            if (this._sendSMS && (!this._smsText.isEmpty())) {
                                _value.append(StringConstants.STR_BULLET).append(StringConstants.TAG_BOLD_START_HTML).append(getColorForChangedPreferenceValue(context.getString(R.string.event_preferences_call_send_sms), disabled, context)).append(StringConstants.TAG_BOLD_END_HTML);
                                _value.append(StringConstants.STR_BULLET).append(context.getString(R.string.event_preferences_call_sms_text)).append(StringConstants.STR_COLON_WITH_SPACE).append(StringConstants.TAG_BOLD_START_HTML).append(this._smsText).append(StringConstants.TAG_BOLD_END_HTML);
                            }
                        }
                    }
                }
            }
            else {
                _value.append(context.getString(R.string.profile_preferences_device_not_allowed))
                        .append(StringConstants.STR_COLON_WITH_SPACE).append(preferenceAllowed.getNotAllowedPreferenceReasonString(context));
            }
        }

        return _value.toString();
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_CALL_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }

        if (key.equals(PREF_EVENT_CALL_EVENT) ||
                key.equals(PREF_EVENT_CALL_CONTACT_LIST_TYPE)) {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_CALL_EVENT)) {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                boolean isEndCall = value.equals(String.valueOf(CALL_EVENT_MISSED_CALL)) ||
                        value.equals(String.valueOf(CALL_EVENT_INCOMING_CALL_ENDED)) ||
                        value.equals(String.valueOf(CALL_EVENT_OUTGOING_CALL_ENDED));
                boolean isRingingCall = value.equals(String.valueOf(CALL_EVENT_RINGING));
                Preference preferenceDuration = prefMng.findPreference(PREF_EVENT_CALL_RUN_AFTER_CALL_END_DURATION);
                Preference preferencePermanentRun = prefMng.findPreference(PREF_EVENT_CALL_RUN_AFTER_CALL_END_PERMANENT_RUN);
                if (preferenceDuration != null) {
                    boolean enabled = isEndCall;
                    enabled = enabled && !preferences.getBoolean(PREF_EVENT_CALL_RUN_AFTER_CALL_END_PERMANENT_RUN, false);
                    preferenceDuration.setEnabled(enabled);
                }
                if (preferencePermanentRun != null)
                    preferencePermanentRun.setEnabled(isEndCall);

                Preference preferenceStopRinging = prefMng.findPreference(PREF_EVENT_CALL_STOP_RINGING);
                Preference preferenceStopRingingInfo = prefMng.findPreference(PREF_EVENT_CALL_STOP_RINGING_INFO);
                Preference preferenceRingingDuration = prefMng.findPreference(PREF_EVENT_CALL_RINGING_DURATION);
                Preference preferenceSendSMS = prefMng.findPreference(PREF_EVENT_CALL_SEND_SMS);
                Preference preferenceSMSText = prefMng.findPreference(PREF_EVENT_CALL_SMS_TEXT);
                if (preferenceStopRinging != null)
                    preferenceStopRinging.setEnabled(isRingingCall);
                if (preferenceStopRingingInfo != null)
                    preferenceStopRingingInfo.setEnabled(isRingingCall);
                boolean stopRinging = preferences.getBoolean(PREF_EVENT_CALL_STOP_RINGING, false);
                if (preferenceRingingDuration != null)
                    preferenceRingingDuration.setEnabled(isRingingCall && stopRinging);
                if (preferenceSendSMS != null)
                    preferenceSendSMS.setEnabled(isRingingCall && stopRinging);
                if (preferenceSMSText != null)
                    preferenceSMSText.setEnabled(isRingingCall && stopRinging);
            }
        }
        if (key.equals(PREF_EVENT_CALL_RUN_AFTER_CALL_END_PERMANENT_RUN)) {
            SwitchPreferenceCompat permanentRunPreference = prefMng.findPreference(key);
            if (permanentRunPreference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(permanentRunPreference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
            String callEvent = preferences.getString(PREF_EVENT_CALL_EVENT, "-1");
            if (!callEvent.equals(String.valueOf(CALL_EVENT_MISSED_CALL)) &&
                    !callEvent.equals(String.valueOf(CALL_EVENT_INCOMING_CALL_ENDED)) &&
                    !callEvent.equals(String.valueOf(CALL_EVENT_OUTGOING_CALL_ENDED))) {
                Preference preference = prefMng.findPreference(PREF_EVENT_CALL_RUN_AFTER_CALL_END_DURATION);
                if (preference != null) {
                    preference.setEnabled(false);
                }
            } else {
                Preference preference = prefMng.findPreference(PREF_EVENT_CALL_RUN_AFTER_CALL_END_DURATION);
                if (preference != null) {
                    preference.setEnabled(value.equals(StringConstants.FALSE_STRING));
                }
            }
        }
        if (key.equals(PREF_EVENT_CALL_RUN_AFTER_CALL_END_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 5;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 5, false, false, false, false);
        }

        if (key.equals(PREF_EVENT_CALL_STOP_RINGING)) {
            SwitchPreferenceCompat stopRingingPreference = prefMng.findPreference(key);
            if (stopRingingPreference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(stopRingingPreference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
            String callEvent = preferences.getString(PREF_EVENT_CALL_EVENT, "-1");
            boolean isRingingCall = callEvent.equals(String.valueOf(CALL_EVENT_RINGING));
            boolean stopRinging = preferences.getBoolean(PREF_EVENT_CALL_STOP_RINGING, false);
            Preference preference = prefMng.findPreference(PREF_EVENT_CALL_RINGING_DURATION);
            if (preference != null)
                preference.setEnabled(isRingingCall && stopRinging);
            preference = prefMng.findPreference(PREF_EVENT_CALL_SEND_SMS);
            if (preference != null)
                preference.setEnabled(isRingingCall && stopRinging);
            preference = prefMng.findPreference(PREF_EVENT_CALL_SMS_TEXT);
            if (preference != null)
                preference.setEnabled(isRingingCall && stopRinging);
        }
        if (key.equals(PREF_EVENT_CALL_RINGING_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 5;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 5, false, false, false, false);
        }
        if (key.equals(PREF_EVENT_CALL_SEND_SMS)) {
            SwitchPreferenceCompat stopRingingPreference = prefMng.findPreference(key);
            if (stopRingingPreference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(stopRingingPreference, true, preferences.getBoolean(key, false), false, false, false, false);
            }
        }
        if (key.equals(PREF_EVENT_CALL_SMS_TEXT))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !value.isEmpty(), false, false, false, false);
            }
        }

        boolean hasFeature = false;
        boolean hasSIMCard = false;
        if (key.equals(PREF_EVENT_CALL_FOR_SIM_CARD)) {
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int phoneCount = telephonyManager.getPhoneCount();
                if (phoneCount > 1) {
                    hasFeature = true;
                    boolean simExists;
//                    Log.e("EventPreferencesCall.setSummary", "called hasSIMCard");
                    HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                    boolean sim1Exists = hasSIMCardData.hasSIM1;
                    boolean sim2Exists = hasSIMCardData.hasSIM2;

                    simExists = sim1Exists;
                    simExists = simExists && sim2Exists;
                    hasSIMCard = simExists;
                    PPListPreference listPreference = prefMng.findPreference(key);
                    if (listPreference != null) {
                        int index = listPreference.findIndexOfValue(value);
                        CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                        listPreference.setSummary(summary);
                    }
                }
            }
            if (!hasFeature) {
                Preference preference = prefMng.findPreference(PREF_EVENT_CALL_FOR_SIM_CARD);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
            }
            else if (!hasSIMCard) {
                Preference preference = prefMng.findPreference(PREF_EVENT_CALL_FOR_SIM_CARD);
                if (preference != null) {
                    PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                    preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS;
                    preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                            StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
            }
        }

        /*
        if (key.equals(PREF_EVENT_CALL_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0) {
                    String summary = context.getString(R.string.profile_preferences_PPPExtender_not_installed_summary);// +
                            //"\n\n" + context.getString(R.string.event_preferences_call_PPPExtender_install_summary);
                    preference.setSummary(summary);
                }
                else {
                    String extenderVersionName = PPExtenderBroadcastReceiver.getExtenderVersionName(context);
                    String summary = context.getString(R.string.profile_preferences_PPPExtender_installed_summary) +
                            " " + extenderVersionName + " (" + extenderVersion + ")\n\n";
                    if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)
                        summary = summary + context.getString(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                    else
                        summary = summary + context.getString(R.string.pppextender_pref_dialog_PPPExtender_upgrade_summary);
                    preference.setSummary(summary);
                }
            }
        }
        */

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesCall.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesCall.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_CALL_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_CALL_CONTACT_GROUPS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_CALL_CONTACT_GROUPS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_CALL_CONTACTS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_CALL_CONTACTS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable, false);
        }
        preference = prefMng.findPreference(PREF_EVENT_CALL_CONTACT_LIST_TYPE);
        if (preference != null)
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !isRunnable, false);
        preference = prefMng.findPreference(PREF_EVENT_CALL_FOR_SIM_CARD);
        if (preference != null)
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, false, !isRunnable, false);

        int _isAccessibilityEnabled = event._eventPreferencesCall.isAccessibilityServiceEnabled(context, false);
        boolean isAccessibilityEnabled = _isAccessibilityEnabled == 1;

        ExtenderDialogPreference extenderPreference = prefMng.findPreference(PREF_EVENT_CALL_EXTENDER);
        if (extenderPreference != null) {
            extenderPreference.setSummaryEDP();
            GlobalGUIRoutines.setPreferenceTitleStyleX(extenderPreference, enabled, false, false, true,
                    !(isAccessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1)), true);
        }
        /*
        preference = prefMng.findPreference(PREF_EVENT_CALL_ACCESSIBILITY_SETTINGS);
        if (preference != null) {

            String summary;
            if (isAccessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1))
                summary = context.getString(R.string.accessibility_service_enabled);
            else {
                if (_isAccessibilityEnabled == -1) {
                    summary = context.getString(R.string.accessibility_service_not_used);
                    summary = summary + "\n\n" + context.getString(R.string.preference_not_used_extender_reason) + " " +
                            context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else {
                    summary = context.getString(R.string.accessibility_service_disabled);
                    summary = summary + "\n\n" + context.getString(R.string.event_preferences_call_AccessibilitySettingsForExtender_summary);
                }
            }
            preference.setSummary(summary);

            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true,
                    !(isAccessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1)), true);
        }
        */
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context) {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_CALL_ENABLED) ||
                key.equals(PREF_EVENT_CALL_RUN_AFTER_CALL_END_PERMANENT_RUN) ||
                key.equals(PREF_EVENT_CALL_STOP_RINGING) ||
                key.equals(PREF_EVENT_CALL_SEND_SMS)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? StringConstants.TRUE_STRING : StringConstants.FALSE_STRING, context);
        }
        if (key.equals(PREF_EVENT_CALL_EVENT) ||
                key.equals(PREF_EVENT_CALL_CONTACT_LIST_TYPE) ||
                key.equals(PREF_EVENT_CALL_CONTACTS) ||
                key.equals(PREF_EVENT_CALL_CONTACT_GROUPS) ||
                key.equals(PREF_EVENT_CALL_RUN_AFTER_CALL_END_DURATION) ||
                key.equals(PREF_EVENT_CALL_EXTENDER) ||
                //key.equals(PREF_EVENT_CALL_INSTALL_EXTENDER) ||
                key.equals(PREF_EVENT_CALL_FOR_SIM_CARD) ||
                key.equals(PREF_EVENT_CALL_SMS_TEXT) ||
                key.equals(PREF_EVENT_CALL_RINGING_DURATION) ||
                key.equals(PREF_EVENT_CALL_STOP_RINGING_INFO)) {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context) {
        setSummary(prefMng, PREF_EVENT_CALL_ENABLED, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_EVENT, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_CONTACT_LIST_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_CONTACTS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_CONTACT_GROUPS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_RUN_AFTER_CALL_END_PERMANENT_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_RUN_AFTER_CALL_END_DURATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_EXTENDER, preferences, context);
        //setSummary(prefMng, PREF_EVENT_CALL_INSTALL_EXTENDER, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_FOR_SIM_CARD, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_STOP_RINGING, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_STOP_RINGING_INFO, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SEND_SMS, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_SMS_TEXT, preferences, context);
        setSummary(prefMng, PREF_EVENT_CALL_RINGING_DURATION, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = EventStatic.isEventPreferenceAllowed(PREF_EVENT_CALL_ENABLED_NO_CHECK_SIM, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesCall tmp = new EventPreferencesCall(this._event, this._enabled, this._callEvent, this._contacts, this._contactGroups,
                    this._contactListType, this._runAfterCallEndPermanentRun, this._runAfterCallEndDuration, this._forSIMCard,
                    this._stopRinging, this._sendSMS, this._smsText, this._ringingDuration);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_CALL_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_CALL_ENABLED, false);
                boolean runnable = tmp.isRunnable(context) &&
                        (tmp.isAccessibilityServiceEnabled(context, false) == 1) &&
                        (PPApplication.accessibilityServiceForPPPExtenderConnected == 1);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_PHONE_CALL).isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(runnable && permissionGranted), true);
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false,  false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        } else {
            Preference preference = prefMng.findPreference(PREF_EVENT_CALL_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                        StringConstants.STR_COLON_WITH_SPACE + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context) {

        boolean runnable = super.isRunnable(context);

        runnable = runnable && ((_contactListType == CONTACT_LIST_TYPE_NOT_USE) ||
                (!(_contacts.isEmpty() && _contactGroups.isEmpty())));

        return runnable;
    }

    @Override
    int isAccessibilityServiceEnabled(Context context, boolean againCheckInDelay)
    {
        int extenderVersion = PPExtenderBroadcastReceiver.isExtenderInstalled(context);
        if (extenderVersion == 0)
            return -2;
        if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_8_1_3)
            return -1;
        if ((_event.getStatus() != Event.ESTATUS_STOP) && this._enabled && isRunnable(context)) {
            if (PPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, againCheckInDelay, true
                            /*, "EventPreferencesCall.isAccessibilityServiceEnabled"*/))
                return 1;
        } else
            return 1;
        return 0;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        super.checkPreferences(prefMng, onlyCategory, context);
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_CALL_ENABLED) != null)
            {
                final boolean accessibilityEnabled =
                        PPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_8_1_3, true, false
                                /*, "EventPreferencesCall.checkPreferences"*/);

                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_CALL_ENABLED, false);
                Preference preference = prefMng.findPreference(PREF_EVENT_CALL_EXTENDER);
                if (preference != null)
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !accessibilityEnabled, true);

//                preference = prefMng.findPreference(PREF_EVENT_CALL_ACCESSIBILITY_SETTINGS);
//                if (preference != null)
//                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !accessibilityEnabled, true);

                boolean showPreferences = false;
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
//                        Log.e("EventPreferencesCall.checkPreferences", "called hasSIMCard");
                        HasSIMCardData hasSIMCardData = GlobalUtils.hasSIMCard(context);
                        boolean sim1Exists = hasSIMCardData.hasSIM1;
                        boolean sim2Exists = hasSIMCardData.hasSIM2;

                        showPreferences = true;
                        //preference = prefMng.findPreference("eventCallDualSIMInfo");
                        //if (preference != null)
                        //    preference.setEnabled(enabled && sim1Exists && sim2Exists);
                        preference = prefMng.findPreference(PREF_EVENT_CALL_FOR_SIM_CARD);
                        if (preference != null)
                            preference.setEnabled(enabled && sim1Exists && sim2Exists);
                    } else {
                        //preference = prefMng.findPreference("eventCallDualSIMInfo");
                        //if (preference != null)
                        //    preference.setEnabled(false);
                        preference = prefMng.findPreference(PREF_EVENT_CALL_FOR_SIM_CARD);
                        if (preference != null)
                            preference.setEnabled(false);
                    }
                }
                if (!showPreferences) {
                    //preference = prefMng.findPreference("eventCallDualSIMInfo");
                    //if (preference != null)
                    //    preference.setVisible(false);
                    preference = prefMng.findPreference(PREF_EVENT_CALL_FOR_SIM_CARD);
                    if (preference != null)
                        preference.setVisible(false);
                }

                setSummary(prefMng, PREF_EVENT_CALL_ENABLED, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    private long computeRunAfterCallEndAlarm() {
        Calendar callEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        callEndTime.setTimeInMillis((_runAfterCallEndTime - gmtOffset) + (_runAfterCallEndDuration * 1000L));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = callEndTime.getTimeInMillis();

        return alarmTime;
    }

    private long computeStopRingingAlarm() {
        Calendar callStartTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        callStartTime.setTimeInMillis((_ringingTime - gmtOffset) + (_ringingDuration * 1000L));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = callStartTime.getTimeInMillis();

        return alarmTime;
    }

    @Override
    void setSystemEventForStart(Context context) {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that will change state into RUNNING;
        // from broadcast will by called EventsHandler

        removeRunAfterCallEndAlarm(context);
        removeStopRingingAlarm(context);
    }

    @Override
    void setSystemEventForPause(Context context) {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that will change state into PAUSE;
        // from broadcast will by called EventsHandler

        removeRunAfterCallEndAlarm(context);
        removeStopRingingAlarm(context);

        if (!(isRunnable(context) && _enabled))
            return;

        if ((_callEvent == CALL_EVENT_MISSED_CALL) ||
                (_callEvent == CALL_EVENT_INCOMING_CALL_ENDED) ||
                (_callEvent == CALL_EVENT_OUTGOING_CALL_ENDED))
            setRunAfterCallEndAlarm(computeRunAfterCallEndAlarm(), context);
        if (_callEvent == CALL_EVENT_RINGING)
            setStopRingingAlarm(computeStopRingingAlarm(), context);
    }

    @Override
    void removeSystemEvent(Context context) {
        removeRunAfterCallEndAlarm(context);
        removeStopRingingAlarm(context);
    }

    void removeRunAfterCallEndAlarm(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, MissedCallEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_MISSED_CALL_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, MissedCallEventEndBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_CALL_SENSOR_TAG_WORK+"_" + (int) _event._id);
    }

    private void setRunAfterCallEndAlarm(long alarmTime, Context context) {
        if (!_runAfterCallEndPermanentRun) {
            if (_runAfterCallEndTime > 0) {
                //Intent intent = new Intent(context, MissedCallEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_MISSED_CALL_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, MissedCallEventEndBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                    else {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    }
                }
            }
        }
    }

    void removeStopRingingAlarm(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, StopRingingBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_STOP_RINGING_BROADCAST_RECEIVER);
                //intent.setClass(context, StopRingingBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_CALL_SENSOR_TAG_WORK+"_" + (int) _event._id);
    }

    private void setStopRingingAlarm(long alarmTime, Context context) {
        if (!_runAfterCallEndPermanentRun) {
            if (_ringingTime > 0) {
                //Intent intent = new Intent(context, StopRingingBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_STOP_RINGING_BROADCAST_RECEIVER);
                //intent.setClass(context, StopRingingBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                    else {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    }
                }
            }
        }
    }

    boolean isPhoneNumberConfigured(List<Contact> contactList, String phoneNumber/*, DataWrapper dataWrapper*/) {
        boolean phoneNumberFound = false;

        if (this._contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) {
            /*ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
            if (contactsCache == null)
                return false;
            List<Contact> contactList;
//            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.isPhoneNumberConfigured", "(1) PPApplication.contactsCacheMutex");
            synchronized (PPApplication.contactsCacheMutex) {
                contactList = contactsCache.getList(); //false
            }*/

            // find phone number in groups
            String[] splits = this._contactGroups.split(StringConstants.STR_SPLIT_REGEX);
            for (String split : splits) {
                if (!split.isEmpty()) {
//                    PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.isPhoneNumberConfigured", "(2) PPApplication.contactsCacheMutex");
                    synchronized (PPApplication.contactsCacheMutex) {
                        if (contactList != null) {
                            for (Contact contact : contactList) {
                                if (contact.groups != null) {
                                    long groupId = contact.groups.indexOf(Long.valueOf(split));
                                    if (groupId != -1) {
                                        // group found in contact
                                        if (contact.phoneId != 0) {
                                            String _phoneNumber = contact.phoneNumber;
                                            if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                                phoneNumberFound = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (phoneNumberFound)
                    break;
            }

            if (!phoneNumberFound) {
                // find phone number in contacts
                // contactId#phoneId|...
                splits = this._contacts.split(StringConstants.STR_SPLIT_REGEX);
                for (String split : splits) {
                    String[] splits2 = split.split(StringConstants.STR_SPLIT_CONTACTS_REGEX);

                    if ((!split.isEmpty()) &&
                            (splits2.length == 3) &&
                            (!splits2[0].isEmpty()) &&
                            (!splits2[1].isEmpty()) &&
                            (!splits2[2].isEmpty())) {
                        String contactPhoneNumber = splits2[1];
                        if (PhoneNumberUtils.compare(contactPhoneNumber, phoneNumber)) {
                            // phone number is in sensor configured
                            phoneNumberFound = true;
                            break;
                        }
                    }
                }
            }

            if (this._contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_BLACK_LIST)
                phoneNumberFound = !phoneNumberFound;
        } else
            phoneNumberFound = true;

        return phoneNumberFound;
    }

    void saveRunAfterCallEndTime(List<Contact> contactList, DataWrapper dataWrapper) {
        if (this._runAfterCallEndTime == 0) {
            // alarm for end is not set
            if (Permissions.checkContacts(dataWrapper.context)) {
                int callEventType = ApplicationPreferences.prefEventCallEventType;
                long callTime = ApplicationPreferences.prefEventCallRunAfterCallEndTime;
                String phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;
                int simSlot = ApplicationPreferences.prefEventCallRunAfterCallEndFromSIMSlot;

                if (((_callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL) && (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_MISSED_CALL)) ||
                    ((_callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED) && (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ENDED)) ||
                    ((_callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED) && (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ENDED))) {

                    boolean phoneNumberFound = isPhoneNumberConfigured(contactList, phoneNumber/*, dataWrapper*/);

                    if (phoneNumberFound) {
                        this._runAfterCallEndTime = callTime; // + (10 * 1000);
                        this._runAfterCallEndFromSIMSlot = simSlot;
                    }
                    else {
                        this._runAfterCallEndTime = 0;
                        this._runAfterCallEndFromSIMSlot = 0;
                    }

                    DatabaseHandler.getInstance(dataWrapper.context).updateCallRunAfterCallEndTime(_event);

                    if (phoneNumberFound) {
                        //if (_event.getStatus() == Event.ESTATUS_RUNNING)
                            setSystemEventForPause(dataWrapper.context);
                    }
                }// else {
                //    _startTime = 0;
                //    DatabaseHandler.getInstance(dataWrapper.context).updateCallStartTime(_event);
                //}
            } else {
                _runAfterCallEndTime = 0;
                DatabaseHandler.getInstance(dataWrapper.context).updateCallRunAfterCallEndTime(_event);
            }
        }
    }

    void saveRingingTime(List<Contact> contactList, DataWrapper dataWrapper) {
        if (this._ringingTime == 0) {
            // alarm for end is not set
            if (Permissions.checkContacts(dataWrapper.context)) {
                int callEventType = ApplicationPreferences.prefEventCallEventType;
                long callTime = ApplicationPreferences.prefEventCallRingingTime;
                String phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;
                int simSlot = ApplicationPreferences.prefEventCallRingingFromSIMSlot;

                if ((_callEvent == EventPreferencesCall.CALL_EVENT_RINGING) && (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_RINGING)) {

                    boolean phoneNumberFound = isPhoneNumberConfigured(contactList, phoneNumber/*, dataWrapper*/);

                    if (phoneNumberFound) {
                        this._ringingTime = callTime; // + (10 * 1000);
                        this._ringingFromSIMSlot = simSlot;
                    }
                    else {
                        this._ringingTime = 0;
                        this._ringingFromSIMSlot = 0;
                    }

                    DatabaseHandler.getInstance(dataWrapper.context).updateCallRingingTime(_event);

                    if (phoneNumberFound) {
                        //if (_event.getStatus() == Event.ESTATUS_RUNNING)
                        setSystemEventForPause(dataWrapper.context);
                    }
                }// else {
                //    _startTime = 0;
                //    DatabaseHandler.getInstance(dataWrapper.context).updateCallStartTime(_event);
                //}
            } else {
                _ringingTime = 0;
                DatabaseHandler.getInstance(dataWrapper.context).updateCallRingingTime(_event);
            }
        }
    }

    static void getEventCallEventType(Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.getEventCallEventType", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            ApplicationPreferences.prefEventCallEventType = ApplicationPreferences.
                    getSharedPreferences(context).getInt(EventPreferencesCall.PREF_EVENT_CALL_EVENT_TYPE, EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED);
            //return ApplicationPreferences.prefEventCallEventType;
        }
    }
    static void setEventCallEventType(Context context, int eventType) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCallsgetEventCallEventType", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(EventPreferencesCall.PREF_EVENT_CALL_EVENT_TYPE, eventType);
            editor.apply();
            ApplicationPreferences.prefEventCallEventType = eventType;
        }
    }

    /** @noinspection SameParameterValue*/
    static void getEventCallEventTime(Context context, int eventType) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.getEventCallEventTime", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            if ((eventType == EventPreferencesCall.PHONE_CALL_EVENT_MISSED_CALL) ||
                    (eventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ENDED) ||
                    (eventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ENDED) ||
                    (eventType == PHONE_CALL_EVENT_UNDEFINED))
                ApplicationPreferences.prefEventCallRunAfterCallEndTime = ApplicationPreferences.
                        getSharedPreferences(context).getLong(EventPreferencesCall.PREF_EVENT_CALL_RUN_AFTER_CALL_END_TIME, 0);
            if ((eventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_RINGING) ||
                    (eventType == PHONE_CALL_EVENT_UNDEFINED))
                ApplicationPreferences.prefEventCallRingingTime = ApplicationPreferences.
                        getSharedPreferences(context).getLong(EventPreferencesCall.PREF_EVENT_CALL_RINGING_TIME, 0);

            //return ApplicationPreferences.prefEventCallEventTime;
        }
    }
    static void setEventCallEventTime(Context context, long time, int eventType) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.setEventCallEventTime", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            if ((eventType == EventPreferencesCall.PHONE_CALL_EVENT_MISSED_CALL) ||
                    (eventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ENDED) ||
                    (eventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ENDED) ||
                    (eventType == PHONE_CALL_EVENT_UNDEFINED)) {
                editor.putLong(EventPreferencesCall.PREF_EVENT_CALL_RUN_AFTER_CALL_END_TIME, time);
                ApplicationPreferences.prefEventCallRunAfterCallEndTime = time;
            }
            if ((eventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_RINGING) ||
                    (eventType == PHONE_CALL_EVENT_UNDEFINED)) {
                editor.putLong(EventPreferencesCall.PREF_EVENT_CALL_RINGING_TIME, time);
                ApplicationPreferences.prefEventCallRingingTime = time;
            }
            editor.apply();
        }
    }

    static void getEventCallPhoneNumber(Context context) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.getEventCallPhoneNumber", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            ApplicationPreferences.prefEventCallPhoneNumber = ApplicationPreferences.
                    getSharedPreferences(context).getString(EventPreferencesCall.PREF_EVENT_CALL_PHONE_NUMBER, "");
            //return ApplicationPreferences.prefEventCallPhoneNumber;
        }
    }
    static void setEventCallPhoneNumber(Context context, String phoneNumber) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.setEventCallPhoneNumber", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(EventPreferencesCall.PREF_EVENT_CALL_PHONE_NUMBER, phoneNumber);
            editor.apply();
            ApplicationPreferences.prefEventCallPhoneNumber = phoneNumber;
        }
    }
    /** @noinspection SameParameterValue*/
    static void getEventCallFromSIMSlot(Context context, int eventType) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.getEventCallSIMSlot", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            if ((eventType == EventPreferencesCall.PHONE_CALL_EVENT_MISSED_CALL) ||
                    (eventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ENDED) ||
                    (eventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ENDED) ||
                    (eventType == PHONE_CALL_EVENT_UNDEFINED))
                ApplicationPreferences.prefEventCallRunAfterCallEndFromSIMSlot = ApplicationPreferences.
                    getSharedPreferences(context).getInt(EventPreferencesCall.PREF_EVENT_CALL_RUN_AFTER_CALL_END_FROM_SIM_SLOT, 0);
            if ((eventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_RINGING) ||
                    (eventType == PHONE_CALL_EVENT_UNDEFINED))
                ApplicationPreferences.prefEventCallRingingFromSIMSlot = ApplicationPreferences.
                        getSharedPreferences(context).getInt(EventPreferencesCall.PREF_EVENT_CALL_RINGING_FROM_SIM_SLOT, 0);

            //return ApplicationPreferences.prefEventCallPhoneNumber;
        }
    }
    static void setEventCallFromSIMSlot(Context context, int simSlot, int eventType) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.setEventCallSIMSlot", "PPApplication.eventCallSensorMutex");
        synchronized (PPApplication.eventCallSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            if ((eventType == EventPreferencesCall.PHONE_CALL_EVENT_MISSED_CALL) ||
                    (eventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ENDED) ||
                    (eventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ENDED) ||
                    (eventType == PHONE_CALL_EVENT_UNDEFINED)) {
                editor.putInt(EventPreferencesCall.PREF_EVENT_CALL_RUN_AFTER_CALL_END_FROM_SIM_SLOT, simSlot);
                ApplicationPreferences.prefEventCallRunAfterCallEndFromSIMSlot = simSlot;
            }
            if ((eventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_RINGING) ||
                    (eventType == PHONE_CALL_EVENT_UNDEFINED)) {
                editor.putInt(EventPreferencesCall.PREF_EVENT_CALL_RINGING_FROM_SIM_SLOT, simSlot);
                ApplicationPreferences.prefEventCallRingingFromSIMSlot = simSlot;
            }
            editor.apply();
        }
    }

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((EventStatic.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventCallContacts(context, event, null)*//* &&
                  this is not required, is only for simulating ringing -> Permissions.checkEventPhoneBroadcast(context, event, null)*/) {
                int callEventType = ApplicationPreferences.prefEventCallEventType;
                String phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;
                int simSlot = ApplicationPreferences.prefEventCallRunAfterCallEndFromSIMSlot;
                //Log.e("EventPreferencesCall.doHandleEvent", "callEventType="+callEventType);
                //Log.e("EventPreferencesCall.doHandleEvent", "phoneNumber="+phoneNumber);
                //Log.e("EventPreferencesCall.doHandleEvent", "simSlot="+simSlot);

                boolean phoneNumberFound = false;

                if (callEventType != EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED) {
                    if (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_SERVICE_UNBIND)
                        eventsHandler.callPassed = false;
                    else {
                        ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                        if (contactsCache != null) {
                            List<Contact> contactList;
//                            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.doHandleEvent", "PPApplication.contactsCacheMutex");
                            synchronized (PPApplication.contactsCacheMutex) {
                                contactList = contactsCache.getList(/*false*/);
                            }
                            phoneNumberFound = isPhoneNumberConfigured(contactList, phoneNumber/*, this*/);
                            if (contactList != null)
                                contactList.clear();
                        }
                    }

                    if (phoneNumberFound) {
                        _runAfterCallEndFromSIMSlot = simSlot;

                        if ((_forSIMCard == 0) || (_forSIMCard == _runAfterCallEndFromSIMSlot)) {
                            if (_callEvent == EventPreferencesCall.CALL_EVENT_RINGING) {
                                //noinspection StatementWithEmptyBody
                                if ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_RINGING) ||
                                        ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ANSWERED)))
                                    ;//eventStart = eventStart && true;
                                else
                                    eventsHandler.callPassed = false;
                            } else if (_callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ANSWERED) {
                                //noinspection StatementWithEmptyBody
                                if (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ANSWERED)
                                    ;//eventStart = eventStart && true;
                                else
                                    eventsHandler.callPassed = false;
                            } else if (_callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_STARTED) {
                                //noinspection StatementWithEmptyBody
                                if (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ANSWERED)
                                    ;//eventStart = eventStart && true;
                                else
                                    eventsHandler.callPassed = false;
                            } else if ((_callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL) ||
                                    (_callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED) ||
                                    (_callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED)) {
//                                Log.e("EventPreferencesCall.doHandleEvent", "_startTime="+_startTime);
                                if (_runAfterCallEndTime > 0) {
                                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                                    long startTime = _runAfterCallEndTime - gmtOffset;


                                    // compute end datetime
                                    long endAlarmTime = computeRunAfterCallEndAlarm();

                                    Calendar now = Calendar.getInstance();
                                    long nowAlarmTime = now.getTimeInMillis();

                                    if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_PHONE_CALL)) {
//                                        Log.e("EventPreferencesCall.doHandleEvent", "SENSOR_TYPE_PHONE_CALL");
                                        //noinspection StatementWithEmptyBody
                                        if (((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_MISSED_CALL) && (_callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL)) ||
                                                ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ENDED) && (_callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED)) ||
                                                ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ENDED) && (_callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED)))
                                            ;//eventStart = eventStart && true;
                                        else
                                            eventsHandler.callPassed = false;
                                    } else if (!_runAfterCallEndPermanentRun) {
                                        if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_PHONE_CALL_EVENT_END))
                                            eventsHandler.callPassed = false;
                                        else
                                            eventsHandler.callPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                                    } else {
                                        eventsHandler.callPassed = nowAlarmTime >= startTime;
                                    }
                                } else
                                    eventsHandler.callPassed = false;
                            }
                        }
                        else
                            eventsHandler.callPassed = false;

                        //if ((callEventType == PhoneCallsListener.CALL_EVENT_INCOMING_CALL_ENDED) ||
                        //        (callEventType == PhoneCallsListener.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                        //    //callPassed = true;
                        //    //eventStart = eventStart && false;
                        //    callPassed = false;
                        //}
                    } else
                        eventsHandler.callPassed = false;

                    if (!eventsHandler.callPassed) {
                        _runAfterCallEndTime = 0;
                        _runAfterCallEndFromSIMSlot = 0;
                        DatabaseHandler.getInstance(eventsHandler.context).updateCallRunAfterCallEndTime(_event);
                    }
                } else {
                    if ((_callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL) ||
                            (_callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED) ||
                            (_callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                        if (_runAfterCallEndTime > 0) {
                            int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                            long startTime = _runAfterCallEndTime - gmtOffset;

                            // compute end datetime
                            long endAlarmTime = computeRunAfterCallEndAlarm();

                            Calendar now = Calendar.getInstance();
                            long nowAlarmTime = now.getTimeInMillis();

                            if (!_runAfterCallEndPermanentRun) {
                                if (Arrays.stream(eventsHandler.sensorType).anyMatch(i -> i == EventsHandler.SENSOR_TYPE_PHONE_CALL_EVENT_END))
                                    eventsHandler.callPassed = false;
                                else
                                    eventsHandler.callPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                            } else {
                                eventsHandler.callPassed = nowAlarmTime >= startTime;
                            }
                        }
                        else
                            eventsHandler.callPassed = false;

                        if (!eventsHandler.callPassed) {
                            _runAfterCallEndTime = 0;
                            _runAfterCallEndFromSIMSlot = 0;
                            DatabaseHandler.getInstance(eventsHandler.context).updateCallRunAfterCallEndTime(_event);
                        }
                    }
                    else
                        eventsHandler.notAllowedCall = true;
                }

                if (!eventsHandler.notAllowedCall) {
                    if (eventsHandler.callPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            }
            else
                eventsHandler.notAllowedCall = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_CALL);
            }
        }
    }

}
