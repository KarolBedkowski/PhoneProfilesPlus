package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseHandler extends SQLiteOpenHelper {


    // All Static variables

    // singleton fields
    private static volatile DatabaseHandler instance;
    //private SQLiteDatabase writableDb;

    final Context context;
    
    // Database Version
    private static final int DATABASE_VERSION = 2493;

    // Database Name
    private static final String DATABASE_NAME = "phoneProfilesManager";

    // Table names
    static final String TABLE_PROFILES = "profiles";
    static final String TABLE_MERGED_PROFILE = "merged_profile";
    static final String TABLE_EVENTS = "events";
    private static final String TABLE_EVENT_TIMELINE = "event_timeline";
    private static final String TABLE_ACTIVITY_LOG = "activity_log";
    private static final String TABLE_GEOFENCES = "geofences";
    private static final String TABLE_SHORTCUTS = "shortcuts";
    private static final String TABLE_MOBILE_CELLS = "mobile_cells";
    private static final String TABLE_NFC_TAGS = "nfc_tags";
    private static final String TABLE_INTENTS = "intents";

    // import/export
    static final String EXPORT_DBFILENAME = DATABASE_NAME + ".backup";
    final Lock importExportLock = new ReentrantLock();
    final Condition runningImportExportCondition  = importExportLock.newCondition();
    final Condition runningCommandCondition = importExportLock.newCondition();
    private boolean runningImportExport = false;
    private boolean runningCommand = false;
    static final int IMPORT_ERROR_BUG = 0;
    static final int IMPORT_ERROR_NEVER_VERSION = -999;
    static final int IMPORT_OK = 1;

//    // create, upgrade, downgrade
//    private final Condition runningUpgradeCondition = importExportLock.newCondition();
//    private boolean runningUpgrade = false;

    // profile type
    static final int PTYPE_CONNECT_TO_SSID = 1;
    static final int PTYPE_FORCE_STOP = 2;
    static final int PTYPE_LOCK_DEVICE = 3;

    // event type
    static final int ETYPE_ALL = -1;
    static final int ETYPE_TIME = 1;
    static final int ETYPE_BATTERY = 2;
    static final int ETYPE_CALL = 3;
    static final int ETYPE_ACCESSORY = 4;
    static final int ETYPE_CALENDAR = 5;
    static final int ETYPE_WIFI_CONNECTED = 6;
    static final int ETYPE_WIFI_NEARBY = 7;
    static final int ETYPE_SCREEN = 8;
    static final int ETYPE_BLUETOOTH_CONNECTED = 9;
    static final int ETYPE_BLUETOOTH_NEARBY = 10;
    static final int ETYPE_SMS = 11;
    static final int ETYPE_NOTIFICATION = 12;
    static final int ETYPE_APPLICATION = 13;
    static final int ETYPE_LOCATION = 14;
    static final int ETYPE_ORIENTATION = 15;
    static final int ETYPE_MOBILE_CELLS = 16;
    static final int ETYPE_NFC = 17;
    static final int ETYPE_RADIO_SWITCH = 18;
    static final int ETYPE_RADIO_SWITCH_WIFI = 19;
    static final int ETYPE_RADIO_SWITCH_BLUETOOTH = 20;
    static final int ETYPE_RADIO_SWITCH_MOBILE_DATA = 21;
    static final int ETYPE_RADIO_SWITCH_GPS = 22;
    static final int ETYPE_RADIO_SWITCH_NFC = 23;
    static final int ETYPE_RADIO_SWITCH_AIRPLANE_MODE = 24;
    static final int ETYPE_WIFI = 25;
    static final int ETYPE_BLUETOOTH = 26;
    static final int ETYPE_ALARM_CLOCK = 27;
    static final int ETYPE_TIME_TWILIGHT = 28;
    static final int ETYPE_BATTERY_WITH_LEVEL = 29;
    static final int ETYPE_ALL_SCANNER_SENSORS = 30;
    static final int ETYPE_DEVICE_BOOT = 31;
    static final int ETYPE_SOUND_PROFILE = 36;
    static final int ETYPE_PERIODIC = 37;
    static final int ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS = 38;
    static final int ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS = 39;
    static final int ETYPE_RADIO_SWITCH_SIM_ON_OFF = 40;
    static final int ETYPE_VOLUMES = 41;
    static final int ETYPE_ACTIVATED_PROFILE = 42;

    // Profiles Table Columns names
    static final String KEY_ID = "id";
    static final String KEY_NAME = "name";
    static final String KEY_ICON = "icon";
    static final String KEY_CHECKED = "checked";
    static final String KEY_PORDER = "porder";
    static final String KEY_VOLUME_RINGER_MODE = "volumeRingerMode";
    static final String KEY_VOLUME_ZEN_MODE = "volumeZenMode";
    static final String KEY_VOLUME_RINGTONE = "volumeRingtone";
    static final String KEY_VOLUME_NOTIFICATION = "volumeNotification";
    static final String KEY_VOLUME_MEDIA = "volumeMedia";
    static final String KEY_VOLUME_ALARM = "volumeAlarm";
    static final String KEY_VOLUME_SYSTEM = "volumeSystem";
    static final String KEY_VOLUME_VOICE = "volumeVoice";
    static final String KEY_SOUND_RINGTONE_CHANGE = "soundRingtoneChange";
    static final String KEY_SOUND_RINGTONE = "soundRingtone";
    static final String KEY_SOUND_NOTIFICATION_CHANGE = "soundNotificationChange";
    static final String KEY_SOUND_NOTIFICATION = "soundNotification";
    static final String KEY_SOUND_ALARM_CHANGE = "soundAlarmChange";
    static final String KEY_SOUND_ALARM = "soundAlarm";
    static final String KEY_DEVICE_AIRPLANE_MODE = "deviceAirplaneMode";
    static final String KEY_DEVICE_WIFI = "deviceWiFi";
    static final String KEY_DEVICE_BLUETOOTH = "deviceBluetooth";
    static final String KEY_DEVICE_SCREEN_TIMEOUT = "deviceScreenTimeout";
    static final String KEY_DEVICE_BRIGHTNESS = "deviceBrightness";
    static final String KEY_DEVICE_WALLPAPER_CHANGE = "deviceWallpaperChange";
    static final String KEY_DEVICE_WALLPAPER = "deviceWallpaper";
    static final String KEY_DEVICE_MOBILE_DATA = "deviceMobileData";
    static final String KEY_DEVICE_MOBILE_DATA_PREFS = "deviceMobileDataPrefs";
    static final String KEY_DEVICE_GPS = "deviceGPS";
    static final String KEY_DEVICE_RUN_APPLICATION_CHANGE = "deviceRunApplicationChange";
    static final String KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME = "deviceRunApplicationPackageName";
    static final String KEY_DEVICE_AUTOSYNC = "deviceAutosync";
    static final String KEY_SHOW_IN_ACTIVATOR = "showInActivator";
    static final String KEY_DEVICE_AUTOROTATE = "deviceAutoRotate";
    static final String KEY_DEVICE_LOCATION_SERVICE_PREFS = "deviceLocationServicePrefs";
    static final String KEY_VOLUME_SPEAKER_PHONE = "volumeSpeakerPhone";
    static final String KEY_DEVICE_NFC = "deviceNFC";
    static final String KEY_DURATION = "duration";
    static final String KEY_AFTER_DURATION_DO = "afterDurationDo";
    static final String KEY_ASK_FOR_DURATION = "askForDuration";
    static final String KEY_DURATION_NOTIFICATION_SOUND = "durationNotificationSound";
    static final String KEY_DURATION_NOTIFICATION_VIBRATE = "durationNotificationVibrate";
    static final String KEY_DEVICE_KEYGUARD = "deviceKeyguard";
    static final String KEY_VIBRATE_ON_TOUCH = "vibrateOnTouch";
    static final String KEY_DEVICE_WIFI_AP = "deviceWifiAP";
    static final String KEY_DEVICE_POWER_SAVE_MODE = "devicePowerSaveMode";
    static final String KEY_DEVICE_NETWORK_TYPE = "deviceNetworkType";
    static final String KEY_NOTIFICATION_LED = "notificationLed";
    static final String KEY_VIBRATE_WHEN_RINGING = "vibrateWhenRinging";
    static final String KEY_VIBRATE_NOTIFICATIONS = "vibrateNotifications";
    static final String KEY_DEVICE_WALLPAPER_FOR = "deviceWallpaperFor";
    static final String KEY_HIDE_STATUS_BAR_ICON = "hideStatusBarIcon";
    static final String KEY_LOCK_DEVICE = "lockDevice";
    static final String KEY_DEVICE_CONNECT_TO_SSID = "deviceConnectToSSID";
    static final String KEY_APPLICATION_DISABLE_WIFI_SCANNING = "applicationDisableWifiScanning";
    static final String KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING = "applicationDisableBluetoothScanning";
    static final String KEY_DEVICE_WIFI_AP_PREFS = "deviceWifiAPPrefs";
    static final String KEY_APPLICATION_DISABLE_LOCATION_SCANNING = "applicationDisableLocationScanning";
    static final String KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING = "applicationDisableMobileCellScanning";
    static final String KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING = "applicationDisableOrientationScanning";
    static final String KEY_HEADS_UP_NOTIFICATIONS = "headsUpNotifications";
    static final String KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE = "deviceForceStopApplicationChange";
    static final String KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME = "deviceForceStopApplicationPackageName";
    static final String KEY_ACTIVATION_BY_USER_COUNT = "activationByUserCount";
    static final String KEY_DEVICE_NETWORK_TYPE_PREFS = "deviceNetworkTypePrefs";
    static final String KEY_DEVICE_CLOSE_ALL_APPLICATIONS = "deviceCloseAllApplications";
    static final String KEY_SCREEN_DARK_MODE = "screenNightMode";
    static final String KEY_DTMF_TONE_WHEN_DIALING = "dtmfToneWhenDialing";
    static final String KEY_SOUND_ON_TOUCH = "soundOnTouch";
    static final String KEY_VOLUME_DTMF = "volumeDTMF";
    static final String KEY_VOLUME_ACCESSIBILITY = "volumeAccessibility";
    static final String KEY_VOLUME_BLUETOOTH_SCO = "volumeBluetoothSCO";
    static final String KEY_AFTER_DURATION_PROFILE = "afterDurationProfile";
    static final String KEY_ALWAYS_ON_DISPLAY = "alwaysOnDisplay";
    static final String KEY_SCREEN_ON_PERMANENT = "screenOnPermanent";
    static final String KEY_VOLUME_MUTE_SOUND = "volumeMuteSound";
    static final String KEY_DEVICE_LOCATION_MODE = "deviceLocationMode";
    static final String KEY_APPLICATION_DISABLE_NOTIFICATION_SCANNING = "applicationDisableNotificationScanning";
    static final String KEY_GENERATE_NOTIFICATION = "generateNotification";
    static final String KEY_CAMERA_FLASH = "cameraFlash";
    static final String KEY_DEVICE_NETWORK_TYPE_SIM1 = "deviceNetworkTypeSIM1";
    static final String KEY_DEVICE_NETWORK_TYPE_SIM2 = "deviceNetworkTypeSIM2";
    static final String KEY_DEVICE_MOBILE_DATA_SIM1 = "deviceMobileDataSIM1";
    static final String KEY_DEVICE_MOBILE_DATA_SIM2 = "deviceMobileDataSIM2";
    static final String KEY_DEVICE_DEFAULT_SIM_CARDS = "deviceDefaultSIMCards";
    static final String KEY_DEVICE_ONOFF_SIM1 = "deviceOnOffSIM1";
    static final String KEY_DEVICE_ONOFF_SIM2 = "deviceOnOffSIM2";
    static final String KEY_SOUND_RINGTONE_CHANGE_SIM1 = "soundRingtoneChangeSIM1";
    static final String KEY_SOUND_RINGTONE_SIM1 = "soundRingtoneSIM1";
    static final String KEY_SOUND_RINGTONE_CHANGE_SIM2 = "soundRingtoneChangeSIM2";
    static final String KEY_SOUND_RINGTONE_SIM2 = "soundRingtoneSIM2";
    static final String KEY_SOUND_NOTIFICATION_CHANGE_SIM1 = "soundNotificationChangeSIM1";
    static final String KEY_SOUND_NOTIFICATION_SIM1 = "soundNotificationSIM1";
    static final String KEY_SOUND_NOTIFICATION_CHANGE_SIM2 = "soundNotificationChangeSIM2";
    static final String KEY_SOUND_NOTIFICATION_SIM2 = "soundNotificationSIM2";
    static final String KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS = "soundSameRingtoneForBothSIMCards";
    static final String KEY_DEVICE_LIVE_WALLPAPER = "deviceLiveWallpaper";
    static final String KEY_CHANGE_WALLPAPER_TIME = "deviceChangeWallpapaerTime";
    static final String KEY_DEVICE_WALLPAPER_FOLDER = "deviceWallpaperFolder";
    static final String KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN = "applicationDisableGlobalEventsRun";
    static final String KEY_DEVICE_VPN_SETTINGS_PREFS = "deviceVPNSettingsPrefs";
    static final String KEY_END_OF_ACTIVATION_TYPE = "endOfActivationType";
    static final String KEY_END_OF_ACTIVATION_TIME = "endOfActivationTime";
    static final String KEY_APPLICATION_DISABLE_PERIODIC_SCANNING = "applicationDisablePeriodicScanning";

    // Events Table Columns names
    static final String KEY_E_ID = "id";
    static final String KEY_E_NAME = "name";
    static final String KEY_E_START_ORDER = "startOrder";
    static final String KEY_E_FK_PROFILE_START = "fkProfile";
    static final String KEY_E_STATUS = "status";
    static final String KEY_E_START_TIME = "startTime";
    static final String KEY_E_END_TIME = "endTime";
    static final String KEY_E_DAYS_OF_WEEK = "daysOfWeek";
    static final String KEY_E_USE_END_TIME = "useEndTime";
    static final String KEY_E_BATTERY_LEVEL = "batteryLevel";
    static final String KEY_E_NOTIFICATION_SOUND_START = "notificationSound";
    static final String KEY_E_BATTERY_LEVEL_LOW = "batteryLevelLow";
    static final String KEY_E_BATTERY_LEVEL_HIGHT = "batteryLevelHight";
    static final String KEY_E_BATTERY_CHARGING = "batteryCharging";
    static final String KEY_E_TIME_ENABLED = "timeEnabled";
    static final String KEY_E_BATTERY_ENABLED = "batteryEnabled";
    static final String KEY_E_CALL_ENABLED = "callEnabled";
    static final String KEY_E_CALL_EVENT = "callEvent";
    static final String KEY_E_CALL_CONTACTS = "callContacts";
    static final String KEY_E_CALL_CONTACT_LIST_TYPE = "contactListType";
    static final String KEY_E_FK_PROFILE_END = "fkProfileEnd";
    static final String KEY_E_FORCE_RUN = "forceRun";
    static final String KEY_E_BLOCKED = "blocked";
    static final String KEY_E_UNDONE_PROFILE = "undoneProfile";
    static final String KEY_E_PRIORITY = "priority";
    static final String KEY_E_ACCESSORY_ENABLED = "peripheralEnabled";
    static final String KEY_E_PERIPHERAL_TYPE = "peripheralType";
    static final String KEY_E_CALENDAR_ENABLED = "calendarEnabled";
    static final String KEY_E_CALENDAR_CALENDARS = "calendarCalendars";
    static final String KEY_E_CALENDAR_SEARCH_FIELD = "calendarSearchField";
    static final String KEY_E_CALENDAR_SEARCH_STRING = "calendarSearchString";
    static final String KEY_E_CALENDAR_EVENT_START_TIME = "calendarEventStartTime";
    static final String KEY_E_CALENDAR_EVENT_END_TIME = "calendarEventEndTime";
    static final String KEY_E_CALENDAR_EVENT_FOUND = "calendarEventFound";
    static final String KEY_E_WIFI_ENABLED = "wifiEnabled";
    static final String KEY_E_WIFI_SSID = "wifiSSID";
    static final String KEY_E_WIFI_CONNECTION_TYPE = "wifiConnectionType";
    static final String KEY_E_SCREEN_ENABLED = "screenEnabled";
    //static final String KEY_E_SCREEN_DELAY = "screenDelay";
    static final String KEY_E_SCREEN_EVENT_TYPE = "screenEventType";
    static final String KEY_E_DELAY_START = "delayStart";
    static final String KEY_E_IS_IN_DELAY_START = "isInDelay";
    static final String KEY_E_SCREEN_WHEN_UNLOCKED = "screenWhenUnlocked";
    static final String KEY_E_BLUETOOTH_ENABLED = "bluetoothEnabled";
    static final String KEY_E_BLUETOOTH_ADAPTER_NAME = "bluetoothAdapterName";
    static final String KEY_E_BLUETOOTH_CONNECTION_TYPE = "bluetoothConnectionType";
    static final String KEY_E_SMS_ENABLED = "smsEnabled";
    //static final String KEY_E_SMS_EVENT = "smsEvent";
    static final String KEY_E_SMS_CONTACTS = "smsContacts";
    static final String KEY_E_SMS_CONTACT_LIST_TYPE = "smsContactListType";
    static final String KEY_E_SMS_START_TIME = "smsStartTime";
    static final String KEY_E_CALL_CONTACT_GROUPS = "callContactGroups";
    static final String KEY_E_SMS_CONTACT_GROUPS = "smsContactGroups";
    static final String KEY_E_AT_END_DO = "atEndDo";
    static final String KEY_E_CALENDAR_AVAILABILITY = "calendarAvailability";
    static final String KEY_E_MANUAL_PROFILE_ACTIVATION = "manualProfileActivation";
    static final String KEY_E_FK_PROFILE_START_WHEN_ACTIVATED = "fkProfileStartWhenActivated";
    static final String KEY_E_SMS_DURATION = "smsDuration";
    static final String KEY_E_NOTIFICATION_ENABLED = "notificationEnabled";
    static final String KEY_E_NOTIFICATION_APPLICATIONS = "notificationApplications";
    static final String KEY_E_NOTIFICATION_DURATION = "notificationDuration";
    static final String KEY_E_NOTIFICATION_START_TIME = "notificationStartTime";
    static final String KEY_E_BATTERY_POWER_SAVE_MODE = "batteryPowerSaveMode";
    static final String KEY_E_BLUETOOTH_DEVICES_TYPE = "bluetoothDevicesType";
    static final String KEY_E_APPLICATION_ENABLED = "applicationEnabled";
    static final String KEY_E_APPLICATION_APPLICATIONS = "applicationApplications";
    static final String KEY_E_NOTIFICATION_END_WHEN_REMOVED = "notificationEndWhenRemoved";
    static final String KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS = "calendarIgnoreAllDayEvents";
    static final String KEY_E_LOCATION_ENABLED = "locationEnabled";
    static final String KEY_E_LOCATION_FK_GEOFENCE = "fklocationGeofenceId";
    static final String KEY_E_LOCATION_WHEN_OUTSIDE = "locationWhenOutside";
    static final String KEY_E_DELAY_END = "delayEnd";
    static final String KEY_E_IS_IN_DELAY_END = "isInDelayEnd";
    static final String KEY_E_START_STATUS_TIME = "startStatusTime";
    static final String KEY_E_PAUSE_STATUS_TIME = "pauseStatusTime";
    static final String KEY_E_ORIENTATION_ENABLED = "orientationEnabled";
    static final String KEY_E_ORIENTATION_SIDES = "orientationSides";
    static final String KEY_E_ORIENTATION_DISTANCE = "orientationDistance";
    static final String KEY_E_ORIENTATION_DISPLAY = "orientationDisplay";
    static final String KEY_E_ORIENTATION_IGNORE_APPLICATIONS = "orientationIgnoreApplications";
    static final String KEY_E_MOBILE_CELLS_ENABLED = "mobileCellsEnabled";
    static final String KEY_E_MOBILE_CELLS_WHEN_OUTSIDE = "mobileCellsWhenOutside";
    static final String KEY_E_MOBILE_CELLS_CELLS = "mobileCellsCells";
    static final String KEY_E_LOCATION_GEOFENCES = "fklocationGeofences";
    static final String KEY_E_NFC_ENABLED = "nfcEnabled";
    static final String KEY_E_NFC_NFC_TAGS = "nfcNfcTags";
    static final String KEY_E_NFC_START_TIME = "nfcStartTime";
    static final String KEY_E_NFC_DURATION = "nfcDuration";
    static final String KEY_E_SMS_PERMANENT_RUN = "smsPermanentRun";
    static final String KEY_E_NOTIFICATION_PERMANENT_RUN = "notificationPermanentRun";
    static final String KEY_E_NFC_PERMANENT_RUN = "nfcPermanentRun";
    static final String KEY_E_CALENDAR_START_BEFORE_EVENT = "calendarStartBeforeEvent";
    static final String KEY_E_RADIO_SWITCH_ENABLED = "radioSwitchEnabled";
    static final String KEY_E_RADIO_SWITCH_WIFI = "radioSwitchWifi";
    static final String KEY_E_RADIO_SWITCH_BLUETOOTH = "radioSwitchBluetooth";
    static final String KEY_E_RADIO_SWITCH_MOBILE_DATA = "radioSwitchMobileData";
    static final String KEY_E_RADIO_SWITCH_GPS = "radioSwitchGPS";
    static final String KEY_E_RADIO_SWITCH_NFC = "radioSwitchNFC";
    static final String KEY_E_RADIO_SWITCH_AIRPLANE_MODE = "radioSwitchAirplaneMode";
    static final String KEY_E_NOTIFICATION_VIBRATE_START = "notificationVibrate";
    static final String KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION = "eventNoPauseByManualActivation";
    static final String KEY_E_CALL_DURATION = "callDuration";
    static final String KEY_E_CALL_PERMANENT_RUN = "callPermanentRun";
    static final String KEY_E_CALL_START_TIME = "callStartTime";
    static final String KEY_E_NOTIFICATION_SOUND_REPEAT_START = "notificationSoundRepeat";
    static final String KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START = "notificationSoundRepeatInterval";
    static final String KEY_E_NOTIFICATION_IN_CALL = "notificationRingingCall";
    static final String KEY_E_NOTIFICATION_MISSED_CALL = "notificationMissedCall";
    static final String KEY_E_START_WHEN_ACTIVATED_PROFILE = "startWhenActivatedProfile";
    static final String KEY_E_BLUETOOTH_SENSOR_PASSED = "bluetoothSensorPassed";
    static final String KEY_E_LOCATION_SENSOR_PASSED = "locationSensorPassed";
    static final String KEY_E_MOBILE_CELLS_SENSOR_PASSED = "mobileCellsSensorPassed";
    static final String KEY_E_ORIENTATION_SENSOR_PASSED = "orientationSensorPassed";
    static final String KEY_E_WIFI_SENSOR_PASSED = "wifiSensorPassed";
    static final String KEY_E_APPLICATION_SENSOR_PASSED = "applicationSensorPassed";
    static final String KEY_E_BATTERY_SENSOR_PASSED = "batterySensorPassed";
    static final String KEY_E_CALENDAR_SENSOR_PASSED = "calendarSensorPassed";
    static final String KEY_E_CALL_SENSOR_PASSED = "callSensorPassed";
    static final String KEY_E_NFC_SENSOR_PASSED = "nfcSensorPassed";
    static final String KEY_E_NOTIFICATION_SENSOR_PASSED = "notificationSensorPassed";
    static final String KEY_E_ACCESSORY_SENSOR_PASSED = "peripheralSensorPassed";
    static final String KEY_E_RADIO_SWITCH_SENSOR_PASSED = "radioSwitchSensorPassed";
    static final String KEY_E_SCREEN_SENSOR_PASSED = "screenSensorPassed";
    static final String KEY_E_SMS_SENSOR_PASSED = "smsSensorPassed";
    static final String KEY_E_TIME_SENSOR_PASSED = "timeSensorPassed";
    static final String KEY_E_CALENDAR_ALL_EVENTS = "calendarAllEvents";
    static final String KEY_E_ALARM_CLOCK_ENABLED = "alarmClockEnabled";
    static final String KEY_E_ALARM_CLOCK_PERMANENT_RUN = "alarmClockPermanentRun";
    static final String KEY_E_ALARM_CLOCK_DURATION = "alarmClockDuration";
    static final String KEY_E_ALARM_CLOCK_START_TIME = "alarmClockStartTime";
    static final String KEY_E_ALARM_CLOCK_SENSOR_PASSED = "alarmClockSensorPassed";
    static final String KEY_E_NOTIFICATION_SOUND_END = "notificationSoundEnd";
    static final String KEY_E_NOTIFICATION_VIBRATE_END = "notificationVibrateEnd";
    static final String KEY_E_BATTERY_PLUGGED = "batteryPlugged";
    static final String KEY_E_TIME_TYPE = "timeType";
    static final String KEY_E_ORIENTATION_CHECK_LIGHT = "orientationCheckLight";
    static final String KEY_E_ORIENTATION_LIGHT_MIN = "orientationLightMin";
    static final String KEY_E_ORIENTATION_LIGHT_MAX = "orientationLightMax";
    static final String KEY_E_NOTIFICATION_CHECK_CONTACTS = "notificationCheckContacts";
    static final String KEY_E_NOTIFICATION_CONTACTS = "notificationContacts";
    static final String KEY_E_NOTIFICATION_CONTACT_GROUPS = "notificationContactGroups";
    static final String KEY_E_NOTIFICATION_CHECK_TEXT = "notificationCheckText";
    static final String KEY_E_NOTIFICATION_TEXT = "notificationText";
    static final String KEY_E_NOTIFICATION_CONTACT_LIST_TYPE = "notificationContactListType";
    static final String KEY_E_DEVICE_BOOT_ENABLED = "deviceBootEnabled";
    static final String KEY_E_DEVICE_BOOT_PERMANENT_RUN = "deviceBootPermanentRun";
    static final String KEY_E_DEVICE_BOOT_DURATION = "deviceBootDuration";
    static final String KEY_E_DEVICE_BOOT_START_TIME = "deviceBootStartTime";
    static final String KEY_E_DEVICE_BOOT_SENSOR_PASSED = "deviceBootSensorPassed";
    static final String KEY_E_ALARM_CLOCK_APPLICATIONS = "alarmClockApplications";
    static final String KEY_E_ALARM_CLOCK_PACKAGE_NAME = "alarmClockPackageName";
    static final String KEY_E_AT_END_HOW_UNDO = "atEndHowUndo";
    static final String KEY_E_CALENDAR_STATUS = "calendarStatus";
    static final String KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END = "manualProfileActivationAtEnd";
    static final String KEY_E_CALENDAR_EVENT_TODAY_EXISTS = "calendarEventTodayExists";
    static final String KEY_E_CALENDAR_DAY_CONTAINS_EVENT = "calendarDayContainsEvent";
    static final String KEY_E_CALENDAR_ALL_DAY_EVENTS = "calendarAllDayEvents";
    static final String KEY_E_ACCESSORY_TYPE = "accessoryType";
    static final String KEY_E_CALL_FROM_SIM_SLOT = "callFromSIMSlot";
    static final String KEY_E_CALL_FOR_SIM_CARD = "callForSIMCard";
    static final String KEY_E_SMS_FROM_SIM_SLOT = "smsFromSIMSlot";
    static final String KEY_E_SMS_FOR_SIM_CARD = "smsForSIMCard";
    static final String KEY_E_MOBILE_CELLS_FOR_SIM_CARD = "mobileCellsForSIMCard";
    static final String KEY_E_SOUND_PROFILE_ENABLED = "soundProfileEnabled";
    static final String KEY_E_SOUND_PROFILE_RINGER_MODES = "soundProfileRingerModes";
    static final String KEY_E_SOUND_PROFILE_ZEN_MODES = "soundProfileZenModes";
    static final String KEY_E_SOUND_PROFILE_SENSOR_PASSED = "soundProfileSensorPassed";
    static final String KEY_E_PERIODIC_ENABLED = "periodicEnabled";
    static final String KEY_E_PERIODIC_MULTIPLY_INTERVAL = "periodicMultiplyInterval";
    static final String KEY_E_PERIODIC_DURATION = "periodicDuration";
    static final String KEY_E_PERIODIC_START_TIME = "periodicStartTime";
    static final String KEY_E_PERIODIC_COUNTER = "periodicCounter";
    static final String KEY_E_PERIODIC_SENSOR_PASSED = "periodicSensorPassed";
    static final String KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS = "radioSwitchDefaultSIMForCalls";
    static final String KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS = "radioSwitchDefaultSIMForSMS";
    static final String KEY_E_RADIO_SWITCH_SIM_ON_OFF = "radioSwitchSIMOnOff";
    static final String KEY_E_VOLUMES_ENABLED = "volumesEnabled";
    static final String KEY_E_VOLUMES_SENSOR_PASSED = "volumesSensorPassed";
    static final String KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE = "notificationSoundStartPlayAlsoInSilentMode";
    static final String KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE = "notificationSoundEndPlayAlsoInSilentMode";
    static final String KEY_E_VOLUMES_RINGTONE = "volumesRingtone";
    static final String KEY_E_VOLUMES_NOTIFICATION = "volumesNotification";
    static final String KEY_E_VOLUMES_MEDIA = "volumesMedia";
    static final String KEY_E_VOLUMES_ALARM = "volumesAlarm";
    static final String KEY_E_VOLUMES_SYSTEM = "volumesSystem";
    static final String KEY_E_VOLUMES_VOICE = "volumesVoice";
    static final String KEY_E_VOLUMES_BLUETOOTHSCO = "volumesBluetoothSCO";
    static final String KEY_E_VOLUMES_ACCESSIBILITY = "volumesAccessibility";
    static final String KEY_E_ACTIVATED_PROFILE_ENABLED = "activatedProfileEnabled";
    static final String KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED = "activatedProfileSensorPassed";
    static final String KEY_E_ACTIVATED_PROFILE_START_PROFILE = "activatedProfileStartProfile";
    static final String KEY_E_ACTIVATED_PROFILE_END_PROFILE = "activatedProfileEndProfile";
    static final String KEY_E_ACTIVATED_PROFILE_RUNNING = "activatedProfileRunning";

    // EventTimeLine Table Columns names
    private static final String KEY_ET_ID = "id";
    private static final String KEY_ET_EORDER = "eorder";
    private static final String KEY_ET_FK_EVENT = "fkEvent";
    private static final String KEY_ET_FK_PROFILE_RETURN = "fkProfileReturn";

    // ActivityLog Columns names
    private static final String KEY_AL_ID = "_id";  // for CursorAdapter must by this name
    static final String KEY_AL_LOG_TYPE = "logType";
    static final String KEY_AL_LOG_DATE_TIME = "logDateTime";
    static final String KEY_AL_EVENT_NAME = "eventName";
    static final String KEY_AL_PROFILE_NAME = "profileName";
    private static final String KEY_AL_PROFILE_ICON = "profileIcon";
    private static final String KEY_AL_DURATION_DELAY = "durationDelay";
    static final String KEY_AL_PROFILE_EVENT_COUNT = "profileEventCount";

    // Geofences Columns names
    static final String KEY_G_ID = "_id";
    private static final String KEY_G_LATITUDE = "latitude";
    private static final String KEY_G_LONGITUDE = "longitude";
    private static final String KEY_G_RADIUS = "radius";
    static final String KEY_G_NAME = "name";
    static final String KEY_G_CHECKED = "checked";
    private static final String KEY_G_TRANSITION = "transition";

    // Shortcuts Columns names
    private static final String KEY_S_ID = "_id";
    private static final String KEY_S_INTENT = "intent";
    private static final String KEY_S_NAME = "name";

    // Mobile cells Columns names
    private static final String KEY_MC_ID = "_id";
    private static final String KEY_MC_CELL_ID = "cellId";
    private static final String KEY_MC_NAME = "name";
    private static final String KEY_MC_NEW = "new";
    private static final String KEY_MC_LAST_CONNECTED_TIME = "lastConnectedTime";
    private static final String KEY_MC_LAST_RUNNING_EVENTS = "lastRunningEvents";
    private static final String KEY_MC_LAST_PAUSED_EVENTS = "lastPausedEvents";
    private static final String KEY_MC_DO_NOT_DETECT = "doNotDetect";

    // NFC tags Columns names
    private static final String KEY_NT_ID = "_id";
    private static final String KEY_NT_NAME = "name";
    private static final String KEY_NT_UID = "uid";

    // Intents Columns names
    private static final String KEY_IN_ID = "_id";
    private static final String KEY_IN_NAME = "_name";
    private static final String KEY_IN_PACKAGE_NAME = "packageName";
    private static final String KEY_IN_CLASS_NAME = "className";
    private static final String KEY_IN_ACTION = "_action";
    private static final String KEY_IN_DATA = "data";
    private static final String KEY_IN_MIME_TYPE = "mimeType";
    private static final String KEY_IN_EXTRA_KEY_1 = "extraKey1";
    private static final String KEY_IN_EXTRA_VALUE_1 = "extraValue1";
    private static final String KEY_IN_EXTRA_TYPE_1 = "extraType1";
    private static final String KEY_IN_EXTRA_KEY_2 = "extraKey2";
    private static final String KEY_IN_EXTRA_VALUE_2 = "extraValue2";
    private static final String KEY_IN_EXTRA_TYPE_2 = "extraType2";
    private static final String KEY_IN_EXTRA_KEY_3 = "extraKey3";
    private static final String KEY_IN_EXTRA_VALUE_3 = "extraValue3";
    private static final String KEY_IN_EXTRA_TYPE_3 = "extraType3";
    private static final String KEY_IN_EXTRA_KEY_4 = "extraKey4";
    private static final String KEY_IN_EXTRA_VALUE_4 = "extraValue4";
    private static final String KEY_IN_EXTRA_TYPE_4 = "extraType4";
    private static final String KEY_IN_EXTRA_KEY_5 = "extraKey5";
    private static final String KEY_IN_EXTRA_VALUE_5 = "extraValue5";
    private static final String KEY_IN_EXTRA_TYPE_5 = "extraType5";
    private static final String KEY_IN_EXTRA_KEY_6 = "extraKey6";
    private static final String KEY_IN_EXTRA_VALUE_6 = "extraValue6";
    private static final String KEY_IN_EXTRA_TYPE_6 = "extraType6";
    private static final String KEY_IN_EXTRA_KEY_7 = "extraKey7";
    private static final String KEY_IN_EXTRA_VALUE_7 = "extraValue7";
    private static final String KEY_IN_EXTRA_TYPE_7 = "extraType7";
    private static final String KEY_IN_EXTRA_KEY_8 = "extraKey8";
    private static final String KEY_IN_EXTRA_VALUE_8 = "extraValue8";
    private static final String KEY_IN_EXTRA_TYPE_8 = "extraType8";
    private static final String KEY_IN_EXTRA_KEY_9 = "extraKey9";
    private static final String KEY_IN_EXTRA_VALUE_9 = "extraValue9";
    private static final String KEY_IN_EXTRA_TYPE_9 = "extraType9";
    private static final String KEY_IN_EXTRA_KEY_10 = "extraKey10";
    private static final String KEY_IN_EXTRA_VALUE_10 = "extraValue10";
    private static final String KEY_IN_EXTRA_TYPE_10 = "extraType10";
    private static final String KEY_IN_CATEGORIES = "categories";
    private static final String KEY_IN_FLAGS = "flags";
    //private static final String KEY_IN_USED_COUNT = "usedCount";
    private static final String KEY_IN_INTENT_TYPE = "intentType";
    private static final String KEY_IN_DO_NOT_DELETE = "doNotDelete";

    private static final String TEXT_TYPE = "TEXT";
    private static final String INTEGER_TYPE = "INTEGER";
    private static final String DATETIME_TYPE = "DATETIME";
    private static final String DOUBLE_TYPE = "DOUBLE";
    private static final String FLOAT_TYPE = "FLOAT";

    private DatabaseHandler(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    public static DatabaseHandler getInstance(Context context) {
        //Double check locking pattern
        if (instance == null) { //Check for the first time
            synchronized (DatabaseHandler.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (instance == null) instance = new DatabaseHandler(context);
            }
        }
        return instance;
    }
    
    SQLiteDatabase getMyWritableDatabase() {
        //if ((writableDb == null) || (!writableDb.isOpen())) {
        //    writableDb = this.getWritableDatabase();
        //}
        //return writableDb;
        return this.getWritableDatabase();
    }
 
//    @Override
//    public synchronized void close() {
//        super.close();
//        if (writableDb != null) {
//            writableDb.close();
//            writableDb = null;
//        }
//    }

    /*
    // be sure to call this method by: DatabaseHandler.getInstance().closeConnection()
    // when application is closed by some means most likely
    // onDestroy method of application
    synchronized void closeConnection() {
        if (instance != null)
        {
            instance.close();
            instance = null;
        }
    }
    */

    private String profileTableCreationString(String tableName) {
        String idField = KEY_ID + " " + INTEGER_TYPE + " PRIMARY KEY,";
        if (tableName.equals(TABLE_MERGED_PROFILE))
            idField = KEY_ID + " " + INTEGER_TYPE + ",";
        return "CREATE TABLE IF NOT EXISTS " + tableName + "("
                + idField
                + KEY_NAME + " " + TEXT_TYPE + ","
                + KEY_ICON + " " + TEXT_TYPE + ","
                + KEY_CHECKED + " " + INTEGER_TYPE + ","
                + KEY_PORDER + " " + INTEGER_TYPE + ","
                + KEY_VOLUME_RINGER_MODE + " " + INTEGER_TYPE + ","
                + KEY_VOLUME_RINGTONE + " " + TEXT_TYPE + ","
                + KEY_VOLUME_NOTIFICATION + " " + TEXT_TYPE + ","
                + KEY_VOLUME_MEDIA + " " + TEXT_TYPE + ","
                + KEY_VOLUME_ALARM + " " + TEXT_TYPE + ","
                + KEY_VOLUME_SYSTEM + " " + TEXT_TYPE + ","
                + KEY_VOLUME_VOICE + " " + TEXT_TYPE + ","
                + KEY_SOUND_RINGTONE_CHANGE + " " + INTEGER_TYPE + ","
                + KEY_SOUND_RINGTONE + " " + TEXT_TYPE + ","
                + KEY_SOUND_NOTIFICATION_CHANGE + " " + INTEGER_TYPE + ","
                + KEY_SOUND_NOTIFICATION + " " + TEXT_TYPE + ","
                + KEY_SOUND_ALARM_CHANGE + " " + INTEGER_TYPE + ","
                + KEY_SOUND_ALARM + " " + TEXT_TYPE + ","
                + KEY_DEVICE_AIRPLANE_MODE + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_WIFI + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_BLUETOOTH + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_SCREEN_TIMEOUT + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_BRIGHTNESS + " " + TEXT_TYPE + ","
                + KEY_DEVICE_WALLPAPER_CHANGE + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_WALLPAPER + " " + TEXT_TYPE + ","
                + KEY_DEVICE_MOBILE_DATA + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_MOBILE_DATA_PREFS + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_GPS + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_RUN_APPLICATION_CHANGE + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + " " + TEXT_TYPE + ","
                + KEY_DEVICE_AUTOSYNC + " " + INTEGER_TYPE + ","
                + KEY_SHOW_IN_ACTIVATOR + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_AUTOROTATE + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_LOCATION_SERVICE_PREFS + " " + INTEGER_TYPE + ","
                + KEY_VOLUME_SPEAKER_PHONE + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_NFC + " " + INTEGER_TYPE + ","
                + KEY_DURATION + " " + INTEGER_TYPE + ","
                + KEY_AFTER_DURATION_DO + " " + INTEGER_TYPE + ","
                + KEY_VOLUME_ZEN_MODE + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_KEYGUARD + " " + INTEGER_TYPE + ","
                + KEY_VIBRATE_ON_TOUCH + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_WIFI_AP + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_POWER_SAVE_MODE + " " + INTEGER_TYPE + ","
                + KEY_ASK_FOR_DURATION + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_NETWORK_TYPE + " " + INTEGER_TYPE + ","
                + KEY_NOTIFICATION_LED + " " + INTEGER_TYPE + ","
                + KEY_VIBRATE_WHEN_RINGING + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_WALLPAPER_FOR + " " + INTEGER_TYPE + ","
                + KEY_HIDE_STATUS_BAR_ICON + " " + INTEGER_TYPE + ","
                + KEY_LOCK_DEVICE + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_CONNECT_TO_SSID + " " + TEXT_TYPE + ","
                + KEY_APPLICATION_DISABLE_WIFI_SCANNING + " " + INTEGER_TYPE + ","
                + KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING + " " + INTEGER_TYPE + ","
                + KEY_DURATION_NOTIFICATION_SOUND + " " + TEXT_TYPE + ","
                + KEY_DURATION_NOTIFICATION_VIBRATE + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_WIFI_AP_PREFS + " " + INTEGER_TYPE + ","
                + KEY_APPLICATION_DISABLE_LOCATION_SCANNING + " " + INTEGER_TYPE + ","
                + KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING + " " + INTEGER_TYPE + ","
                + KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING + " " + INTEGER_TYPE + ","
                + KEY_HEADS_UP_NOTIFICATIONS + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + " " + TEXT_TYPE + ","
                + KEY_ACTIVATION_BY_USER_COUNT + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_NETWORK_TYPE_PREFS + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_CLOSE_ALL_APPLICATIONS + " " + INTEGER_TYPE + ","
                + KEY_SCREEN_DARK_MODE + " " + INTEGER_TYPE + ","
                + KEY_DTMF_TONE_WHEN_DIALING + " " + INTEGER_TYPE + ","
                + KEY_SOUND_ON_TOUCH + " " + INTEGER_TYPE + ","
                + KEY_VOLUME_DTMF + " " + TEXT_TYPE + ","
                + KEY_VOLUME_ACCESSIBILITY + " " + TEXT_TYPE + ","
                + KEY_VOLUME_BLUETOOTH_SCO + " " + TEXT_TYPE + ","
                + KEY_AFTER_DURATION_PROFILE + " " + INTEGER_TYPE + ","
                + KEY_ALWAYS_ON_DISPLAY + " " + INTEGER_TYPE + ","
                + KEY_SCREEN_ON_PERMANENT + " " + INTEGER_TYPE + ","
                + KEY_VOLUME_MUTE_SOUND + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_LOCATION_MODE + " " + INTEGER_TYPE + ","
                + KEY_APPLICATION_DISABLE_NOTIFICATION_SCANNING + " " + INTEGER_TYPE + ","
                + KEY_GENERATE_NOTIFICATION + " " + TEXT_TYPE + ","
                + KEY_CAMERA_FLASH + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_NETWORK_TYPE_SIM1 + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_NETWORK_TYPE_SIM2 + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_MOBILE_DATA_SIM1 + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_MOBILE_DATA_SIM2 + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_DEFAULT_SIM_CARDS + " " + TEXT_TYPE + ","
                + KEY_DEVICE_ONOFF_SIM1 + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_ONOFF_SIM2 + " " + INTEGER_TYPE + ","
                + KEY_SOUND_RINGTONE_CHANGE_SIM1 + " " + INTEGER_TYPE + ","
                + KEY_SOUND_RINGTONE_SIM1 + " " + TEXT_TYPE + ","
                + KEY_SOUND_RINGTONE_CHANGE_SIM2 + " " + INTEGER_TYPE + ","
                + KEY_SOUND_RINGTONE_SIM2 + " " + TEXT_TYPE + ","
                + KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + " " + INTEGER_TYPE + ","
                + KEY_SOUND_NOTIFICATION_SIM1 + " " + TEXT_TYPE + ","
                + KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + " " + INTEGER_TYPE + ","
                + KEY_SOUND_NOTIFICATION_SIM2 + " " + TEXT_TYPE + ","
                + KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_LIVE_WALLPAPER + " " + TEXT_TYPE + ","
                + KEY_VIBRATE_NOTIFICATIONS + " " + INTEGER_TYPE + ","
                + KEY_CHANGE_WALLPAPER_TIME + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_WALLPAPER_FOLDER + " " + TEXT_TYPE + ","
                + KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN + " " + INTEGER_TYPE + ","
                + KEY_DEVICE_VPN_SETTINGS_PREFS + " " + INTEGER_TYPE + ","
                + KEY_END_OF_ACTIVATION_TYPE + " " + INTEGER_TYPE + ","
                + KEY_END_OF_ACTIVATION_TIME + " " + INTEGER_TYPE + ","
                + KEY_APPLICATION_DISABLE_PERIODIC_SCANNING + " " + INTEGER_TYPE
                + ")";
    }

    private void createTables(SQLiteDatabase db) {
        final String CREATE_PROFILES_TABLE = profileTableCreationString(TABLE_PROFILES);
        db.execSQL(CREATE_PROFILES_TABLE);

        final String CREATE_MERGED_PROFILE_TABLE = profileTableCreationString(TABLE_MERGED_PROFILE);
        db.execSQL(CREATE_MERGED_PROFILE_TABLE);

        final String CREATE_EVENTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + "("
                + KEY_E_ID + " " + INTEGER_TYPE + " PRIMARY KEY,"
                + KEY_E_NAME + " " + TEXT_TYPE + ","
                + KEY_E_FK_PROFILE_START + " " + INTEGER_TYPE + ","
                + KEY_E_START_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_END_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_DAYS_OF_WEEK + " " + TEXT_TYPE + ","
                + KEY_E_USE_END_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_STATUS + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_SOUND_START + " " + TEXT_TYPE + ","
                + KEY_E_BATTERY_LEVEL_LOW + " " + INTEGER_TYPE + ","
                + KEY_E_BATTERY_LEVEL_HIGHT + " " + INTEGER_TYPE + ","
                + KEY_E_BATTERY_CHARGING + " " + INTEGER_TYPE + ","
                + KEY_E_TIME_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_BATTERY_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_CALL_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_CALL_EVENT + " " + INTEGER_TYPE + ","
                + KEY_E_CALL_CONTACTS + " " + TEXT_TYPE + ","
                + KEY_E_CALL_CONTACT_LIST_TYPE + " " + INTEGER_TYPE + ","
                + KEY_E_FK_PROFILE_END + " " + INTEGER_TYPE + ","
                + KEY_E_FORCE_RUN + " " + INTEGER_TYPE + ","
                + KEY_E_BLOCKED + " " + INTEGER_TYPE + ","
                //+ KEY_E_UNDONE_PROFILE + " " + INTEGER_TYPE + ","
                + KEY_E_PRIORITY + " " + INTEGER_TYPE + ","
                + KEY_E_ACCESSORY_ENABLED + " " + INTEGER_TYPE + ","
                //+ KEY_E_ACCESSORY_TYPE + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_CALENDARS + " " + TEXT_TYPE + ","
                + KEY_E_CALENDAR_SEARCH_FIELD + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_SEARCH_STRING + " " + TEXT_TYPE + ","
                + KEY_E_CALENDAR_EVENT_START_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_EVENT_END_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_EVENT_FOUND + " " + INTEGER_TYPE + ","
                + KEY_E_WIFI_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_WIFI_SSID + " " + TEXT_TYPE + ","
                + KEY_E_WIFI_CONNECTION_TYPE + " " + INTEGER_TYPE + ","
                + KEY_E_SCREEN_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_SCREEN_EVENT_TYPE + " " + INTEGER_TYPE + ","
                + KEY_E_DELAY_START + " " + INTEGER_TYPE + ","
                + KEY_E_IS_IN_DELAY_START + " " + INTEGER_TYPE + ","
                + KEY_E_SCREEN_WHEN_UNLOCKED + " " + INTEGER_TYPE + ","
                + KEY_E_BLUETOOTH_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_BLUETOOTH_ADAPTER_NAME + " " + TEXT_TYPE + ","
                + KEY_E_BLUETOOTH_CONNECTION_TYPE + " " + INTEGER_TYPE + ","
                + KEY_E_SMS_ENABLED + " " + INTEGER_TYPE + ","
                //+ KEY_E_SMS_EVENT + " " + INTEGER_TYPE + ","
                + KEY_E_SMS_CONTACTS + " " + TEXT_TYPE + ","
                + KEY_E_SMS_CONTACT_LIST_TYPE + " " + INTEGER_TYPE + ","
                + KEY_E_SMS_START_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_CALL_CONTACT_GROUPS + " " + TEXT_TYPE + ","
                + KEY_E_SMS_CONTACT_GROUPS + " " + TEXT_TYPE + ","
                + KEY_E_AT_END_DO + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_AVAILABILITY + " " + INTEGER_TYPE + ","
                + KEY_E_MANUAL_PROFILE_ACTIVATION + " " + INTEGER_TYPE + ","
                + KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + " " + INTEGER_TYPE + ","
                + KEY_E_SMS_DURATION + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_APPLICATIONS + " " + TEXT_TYPE + ","
                + KEY_E_NOTIFICATION_START_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_DURATION + " " + INTEGER_TYPE + ","
                + KEY_E_BATTERY_POWER_SAVE_MODE + " " + INTEGER_TYPE + ","
                + KEY_E_BLUETOOTH_DEVICES_TYPE + " " + INTEGER_TYPE + ","
                + KEY_E_APPLICATION_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_APPLICATION_APPLICATIONS + " " + TEXT_TYPE + ","
                + KEY_E_NOTIFICATION_END_WHEN_REMOVED + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS + " " + INTEGER_TYPE + ","
                + KEY_E_LOCATION_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_LOCATION_FK_GEOFENCE + " " + INTEGER_TYPE + ","
                + KEY_E_LOCATION_WHEN_OUTSIDE + " " + INTEGER_TYPE + ","
                + KEY_E_DELAY_END + " " + INTEGER_TYPE + ","
                + KEY_E_IS_IN_DELAY_END + " " + INTEGER_TYPE + ","
                + KEY_E_START_STATUS_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_PAUSE_STATUS_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_ORIENTATION_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_ORIENTATION_SIDES + " " + TEXT_TYPE + ","
                + KEY_E_ORIENTATION_DISTANCE + " " + INTEGER_TYPE + ","
                + KEY_E_ORIENTATION_DISPLAY + " " + TEXT_TYPE + ","
                + KEY_E_ORIENTATION_IGNORE_APPLICATIONS + " " + TEXT_TYPE + ","
                + KEY_E_MOBILE_CELLS_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + " " + INTEGER_TYPE + ","
                + KEY_E_MOBILE_CELLS_CELLS + " " + TEXT_TYPE + ","
                + KEY_E_LOCATION_GEOFENCES + " " + TEXT_TYPE + ","
                + KEY_E_START_ORDER + " " + INTEGER_TYPE + ","
                + KEY_E_NFC_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_NFC_NFC_TAGS + " " + TEXT_TYPE + ","
                + KEY_E_NFC_DURATION + " " + INTEGER_TYPE + ","
                + KEY_E_NFC_START_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_SMS_PERMANENT_RUN + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_PERMANENT_RUN + " " + INTEGER_TYPE + ","
                + KEY_E_NFC_PERMANENT_RUN + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_START_BEFORE_EVENT + " " + INTEGER_TYPE + ","
                + KEY_E_RADIO_SWITCH_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_RADIO_SWITCH_WIFI + " " + INTEGER_TYPE + ","
                + KEY_E_RADIO_SWITCH_BLUETOOTH + " " + INTEGER_TYPE + ","
                + KEY_E_RADIO_SWITCH_MOBILE_DATA + " " + INTEGER_TYPE + ","
                + KEY_E_RADIO_SWITCH_GPS + " " + INTEGER_TYPE + ","
                + KEY_E_RADIO_SWITCH_NFC + " " + INTEGER_TYPE + ","
                + KEY_E_RADIO_SWITCH_AIRPLANE_MODE + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_VIBRATE_START + " " + INTEGER_TYPE + ","
                + KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION + " " + INTEGER_TYPE + ","
                + KEY_E_CALL_DURATION + " " + INTEGER_TYPE + ","
                + KEY_E_CALL_PERMANENT_RUN + " " + INTEGER_TYPE + ","
                + KEY_E_CALL_START_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_SOUND_REPEAT_START + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_IN_CALL + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_MISSED_CALL + " " + INTEGER_TYPE + ","
                + KEY_E_START_WHEN_ACTIVATED_PROFILE + " " + TEXT_TYPE + ","
                + KEY_E_BLUETOOTH_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_LOCATION_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_MOBILE_CELLS_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_ORIENTATION_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_WIFI_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_APPLICATION_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_BATTERY_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_CALL_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_NFC_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_ACCESSORY_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_RADIO_SWITCH_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_SCREEN_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_SMS_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_TIME_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_ALL_EVENTS + " " + INTEGER_TYPE + ","
                + KEY_E_ALARM_CLOCK_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_ALARM_CLOCK_PERMANENT_RUN + " " + INTEGER_TYPE + ","
                + KEY_E_ALARM_CLOCK_DURATION + " " + INTEGER_TYPE + ","
                + KEY_E_ALARM_CLOCK_START_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_ALARM_CLOCK_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_SOUND_END + " " + TEXT_TYPE + ","
                + KEY_E_NOTIFICATION_VIBRATE_END + " " + INTEGER_TYPE + ","
                + KEY_E_BATTERY_PLUGGED + " " + TEXT_TYPE + ","
                + KEY_E_TIME_TYPE + " " + INTEGER_TYPE + ","
                + KEY_E_ORIENTATION_CHECK_LIGHT + " " + INTEGER_TYPE + ","
                + KEY_E_ORIENTATION_LIGHT_MIN + " " + INTEGER_TYPE + ","
                + KEY_E_ORIENTATION_LIGHT_MAX + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_CHECK_CONTACTS + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_CONTACTS + " " + TEXT_TYPE + ","
                + KEY_E_NOTIFICATION_CONTACT_GROUPS + " " + TEXT_TYPE + ","
                + KEY_E_NOTIFICATION_CHECK_TEXT + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_TEXT + " " + TEXT_TYPE + ","
                + KEY_E_NOTIFICATION_CONTACT_LIST_TYPE + " " + INTEGER_TYPE + ","
                + KEY_E_DEVICE_BOOT_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_DEVICE_BOOT_PERMANENT_RUN + " " + INTEGER_TYPE + ","
                + KEY_E_DEVICE_BOOT_DURATION + " " + INTEGER_TYPE + ","
                + KEY_E_DEVICE_BOOT_START_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_DEVICE_BOOT_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_ALARM_CLOCK_APPLICATIONS + " " + TEXT_TYPE + ","
                + KEY_E_ALARM_CLOCK_PACKAGE_NAME + " " + TEXT_TYPE + ","
                + KEY_E_AT_END_HOW_UNDO + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_STATUS + " " + INTEGER_TYPE + ","
                + KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_EVENT_TODAY_EXISTS + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_DAY_CONTAINS_EVENT + " " + INTEGER_TYPE + ","
                + KEY_E_CALENDAR_ALL_DAY_EVENTS + " " + INTEGER_TYPE + ","
                + KEY_E_ACCESSORY_TYPE + " " + TEXT_TYPE + ","
                + KEY_E_CALL_FROM_SIM_SLOT + " " + INTEGER_TYPE + ","
                + KEY_E_CALL_FOR_SIM_CARD + " " + INTEGER_TYPE + ","
                + KEY_E_SMS_FROM_SIM_SLOT + " " + INTEGER_TYPE + ","
                + KEY_E_SMS_FOR_SIM_CARD + " " + INTEGER_TYPE + ","
                + KEY_E_MOBILE_CELLS_FOR_SIM_CARD + " " + INTEGER_TYPE + ","
                + KEY_E_SOUND_PROFILE_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_SOUND_PROFILE_RINGER_MODES + " " + TEXT_TYPE + ","
                + KEY_E_SOUND_PROFILE_ZEN_MODES + " " + TEXT_TYPE + ","
                + KEY_E_SOUND_PROFILE_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_PERIODIC_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_PERIODIC_MULTIPLY_INTERVAL + " " + INTEGER_TYPE + ","
                + KEY_E_PERIODIC_DURATION + " " + INTEGER_TYPE + ","
                + KEY_E_PERIODIC_START_TIME + " " + INTEGER_TYPE + ","
                + KEY_E_PERIODIC_COUNTER + " " + INTEGER_TYPE + ","
                + KEY_E_PERIODIC_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS + " " + INTEGER_TYPE + ","
                + KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS + " " + INTEGER_TYPE + ","
                + KEY_E_RADIO_SWITCH_SIM_ON_OFF + " " + INTEGER_TYPE + ","
                + KEY_E_VOLUMES_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_VOLUMES_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE + " " + INTEGER_TYPE + ","
                + KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE + " " + INTEGER_TYPE + ","
                + KEY_E_VOLUMES_RINGTONE + " " + TEXT_TYPE + ","
                + KEY_E_VOLUMES_NOTIFICATION + " " + TEXT_TYPE + ","
                + KEY_E_VOLUMES_MEDIA + " " + TEXT_TYPE + ","
                + KEY_E_VOLUMES_ALARM + " " + TEXT_TYPE + ","
                + KEY_E_VOLUMES_SYSTEM + " " + TEXT_TYPE + ","
                + KEY_E_VOLUMES_VOICE + " " + TEXT_TYPE + ","
                + KEY_E_VOLUMES_BLUETOOTHSCO + " " + TEXT_TYPE + ","
                + KEY_E_VOLUMES_ACCESSIBILITY + " " + TEXT_TYPE + ","
                + KEY_E_ACTIVATED_PROFILE_ENABLED + " " + INTEGER_TYPE + ","
                + KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED + " " + INTEGER_TYPE + ","
                + KEY_E_ACTIVATED_PROFILE_START_PROFILE + " " + INTEGER_TYPE + ","
                + KEY_E_ACTIVATED_PROFILE_END_PROFILE + " " + INTEGER_TYPE + ","
                + KEY_E_ACTIVATED_PROFILE_RUNNING + " " + INTEGER_TYPE
                + ")";
        db.execSQL(CREATE_EVENTS_TABLE);

        final String CREATE_EVENTTIME_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_EVENT_TIMELINE + "("
                + KEY_ET_ID + " " + INTEGER_TYPE + " PRIMARY KEY,"
                + KEY_ET_EORDER + " " + INTEGER_TYPE + ","
                + KEY_ET_FK_EVENT + " " + INTEGER_TYPE + ","
                + KEY_ET_FK_PROFILE_RETURN + " " + INTEGER_TYPE
                + ")";
        db.execSQL(CREATE_EVENTTIME_TABLE);

        final String CREATE_ACTIVITYLOG_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ACTIVITY_LOG + "("
                + KEY_AL_ID + " " + INTEGER_TYPE + " PRIMARY KEY,"
                + KEY_AL_LOG_DATE_TIME + " " + DATETIME_TYPE + " DEFAULT CURRENT_TIMESTAMP,"
                + KEY_AL_LOG_TYPE + " " + INTEGER_TYPE + ","
                + KEY_AL_EVENT_NAME + " " + TEXT_TYPE + ","
                + KEY_AL_PROFILE_NAME + " " + TEXT_TYPE + ","
                + KEY_AL_PROFILE_ICON + " " + TEXT_TYPE + ","
                + KEY_AL_DURATION_DELAY + " " + INTEGER_TYPE + ","
                + KEY_AL_PROFILE_EVENT_COUNT + " " + TEXT_TYPE
                + ")";
        db.execSQL(CREATE_ACTIVITYLOG_TABLE);

        final String CREATE_GEOFENCES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_GEOFENCES + "("
                + KEY_G_ID + " " + INTEGER_TYPE + " PRIMARY KEY,"
                + KEY_G_LATITUDE + " " + DOUBLE_TYPE + ","
                + KEY_G_LONGITUDE + " " + DOUBLE_TYPE + ","
                + KEY_G_RADIUS + " " + FLOAT_TYPE + ","
                + KEY_G_NAME + " " + TEXT_TYPE + ","
                + KEY_G_CHECKED + " " + INTEGER_TYPE + ","
                + KEY_G_TRANSITION + " " + INTEGER_TYPE
                + ")";
        db.execSQL(CREATE_GEOFENCES_TABLE);

        final String CREATE_SHORTCUTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SHORTCUTS + "("
                + KEY_S_ID + " " + INTEGER_TYPE + " PRIMARY KEY,"
                + KEY_S_INTENT + " " + TEXT_TYPE + ","
                + KEY_S_NAME + " " + TEXT_TYPE
                + ")";
        db.execSQL(CREATE_SHORTCUTS_TABLE);

        final String CREATE_MOBILE_CELLS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MOBILE_CELLS + "("
                + KEY_MC_ID + " " + INTEGER_TYPE + " PRIMARY KEY,"
                + KEY_MC_CELL_ID + " " + INTEGER_TYPE + ","
                + KEY_MC_NAME + " " + TEXT_TYPE + ","
                + KEY_MC_NEW + " " + INTEGER_TYPE + ","
                + KEY_MC_LAST_CONNECTED_TIME + " " + INTEGER_TYPE + ","
                + KEY_MC_LAST_RUNNING_EVENTS + " " + TEXT_TYPE + ","
                + KEY_MC_LAST_PAUSED_EVENTS + " " + TEXT_TYPE + ","
                + KEY_MC_DO_NOT_DETECT + " " + INTEGER_TYPE
                + ")";
        db.execSQL(CREATE_MOBILE_CELLS_TABLE);

        final String CREATE_NFC_TAGS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NFC_TAGS + "("
                + KEY_NT_ID + " " + INTEGER_TYPE + " PRIMARY KEY,"
                + KEY_NT_NAME + " " + TEXT_TYPE + ","
                + KEY_NT_UID + " " + TEXT_TYPE
                + ")";
        db.execSQL(CREATE_NFC_TAGS_TABLE);

        final String CREATE_INTENTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_INTENTS + "("
                + KEY_IN_ID + " " + INTEGER_TYPE + " PRIMARY KEY,"
                + KEY_IN_PACKAGE_NAME + " " + TEXT_TYPE + ","
                + KEY_IN_CLASS_NAME + " " + TEXT_TYPE + ","
                + KEY_IN_ACTION + " " + TEXT_TYPE + ","
                + KEY_IN_DATA + " " + TEXT_TYPE + ","
                + KEY_IN_MIME_TYPE + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_KEY_1 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_VALUE_1 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_TYPE_1 + " " + INTEGER_TYPE + ","
                + KEY_IN_EXTRA_KEY_2 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_VALUE_2 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_TYPE_2 + " " + INTEGER_TYPE + ","
                + KEY_IN_EXTRA_KEY_3 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_VALUE_3 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_TYPE_3 + " " + INTEGER_TYPE + ","
                + KEY_IN_EXTRA_KEY_4 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_VALUE_4 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_TYPE_4 + " " + INTEGER_TYPE + ","
                + KEY_IN_EXTRA_KEY_5 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_VALUE_5 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_TYPE_5 + " " + INTEGER_TYPE + ","
                + KEY_IN_EXTRA_KEY_6 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_VALUE_6 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_TYPE_6 + " " + INTEGER_TYPE + ","
                + KEY_IN_EXTRA_KEY_7 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_VALUE_7 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_TYPE_7 + " " + INTEGER_TYPE + ","
                + KEY_IN_EXTRA_KEY_8 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_VALUE_8 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_TYPE_8 + " " + INTEGER_TYPE + ","
                + KEY_IN_EXTRA_KEY_9 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_VALUE_9 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_TYPE_9 + " " + INTEGER_TYPE + ","
                + KEY_IN_EXTRA_KEY_10 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_VALUE_10 + " " + TEXT_TYPE + ","
                + KEY_IN_EXTRA_TYPE_10 + " " + INTEGER_TYPE + ","
                + KEY_IN_CATEGORIES + " " + TEXT_TYPE + ","
                + KEY_IN_FLAGS + " " + TEXT_TYPE + ","
                + KEY_IN_NAME + " " + TEXT_TYPE + ","
                //+ KEY_IN_USED_COUNT + " " + INTEGER_TYPE + ","
                + KEY_IN_INTENT_TYPE + " " + INTEGER_TYPE + ","
                + KEY_IN_DO_NOT_DELETE + " " + INTEGER_TYPE
                + ")";
        db.execSQL(CREATE_INTENTS_TABLE);
    }

    private void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_PORDER ON " + TABLE_PROFILES + " (" + KEY_PORDER + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_SHOW_IN_ACTIVATOR ON " + TABLE_PROFILES + " (" + KEY_SHOW_IN_ACTIVATOR + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_P_NAME ON " + TABLE_PROFILES + " (" + KEY_NAME + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_FK_PROFILE ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_START + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_E_NAME ON " + TABLE_EVENTS + " (" + KEY_E_NAME + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_FK_PROFILE_END ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_END + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_PRIORITY ON " + TABLE_EVENTS + " (" + KEY_E_PRIORITY + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_START_ORDER ON " + TABLE_EVENTS + " (" + KEY_E_START_ORDER + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_ET_PORDER ON " + TABLE_EVENT_TIMELINE + " (" + KEY_ET_EORDER + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_AL_LOG_DATE_TIME ON " + TABLE_ACTIVITY_LOG + " (" + KEY_AL_LOG_DATE_TIME + ")");


        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_DEVICE_AUTOROTATE ON " + TABLE_PROFILES + " (" + KEY_DEVICE_AUTOROTATE + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_DEVICE_CONNECT_TO_SSID ON " + TABLE_PROFILES + " (" + KEY_DEVICE_CONNECT_TO_SSID + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_LOCATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_LOCATION_ENABLED + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__TIME_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_TIME_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__BATTERY_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_BATTERY_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__CALL_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_CALL_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__PERIPHERAL_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_ACCESSORY_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__CALENDAR_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_CALENDAR_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__WIFI_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_WIFI_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__SCREEN_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_SCREEN_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__BLUETOOTH_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_BLUETOOTH_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__SMS_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_SMS_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__APPLICATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_APPLICATION_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__LOCATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_LOCATION_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__ORIENTATION_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_ORIENTATION_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__MOBILE_CELLS_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_MOBILE_CELLS_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__NFC_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_NFC_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__RADIO_SWITCH_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_RADIO_SWITCH_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__ALARM_CLOCK_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_ALARM_CLOCK_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__DEVICE_BOOT_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_DEVICE_BOOT_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__SOUND_PROFILE_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_SOUND_PROFILE_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__PERIODIC_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_PERIODIC_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__VOLUMES_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_VOLUMES_ENABLED + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__ACTIVATED_PROFILE_ENABLED ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_ACTIVATED_PROFILE_ENABLED + ")");

        //db.execSQL("CREATE INDEX IF NOT EXISTS IDX_STATUS__MOBILE_CELLS_ENABLED_WHEN_OUTSIDE ON " + TABLE_EVENTS + " (" + KEY_E_STATUS + "," + KEY_E_MOBILE_CELLS_ENABLED + "," + KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_NAME ON " + TABLE_GEOFENCES + " (" + KEY_G_NAME + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_NAME ON " + TABLE_MOBILE_CELLS + " (" + KEY_MC_NAME + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_CELL_ID ON " + TABLE_MOBILE_CELLS + " (" + KEY_MC_CELL_ID + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_NAME ON " + TABLE_NFC_TAGS + " (" + KEY_NT_NAME + ")");

        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_ACTIVATION_BY_USER_COUNT ON " + TABLE_PROFILES + " (" + KEY_ACTIVATION_BY_USER_COUNT + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS IDX_FK_PROFILE_START_WHEN_ACTIVATED ON " + TABLE_EVENTS + " (" + KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + ")");
    }

    private List<String> getTableColums(SQLiteDatabase db, java.lang.String table) {
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info("+ table +")", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    columns.add(name);
                }
            }
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }
        return columns;
    }

    private void createTableColumsWhenNotExists(SQLiteDatabase db, String table) {
        List<String> columns = getTableColums(db, table);
        //PPApplication.logE("DatabaseHandler.createTableColumsWhenNotExists", "cocolumns.size()=" + columns.size());
        switch (table) {
            case TABLE_PROFILES:
            case TABLE_MERGED_PROFILE:
                createColumnWhenNotExists(db, table, KEY_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_ICON, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_CHECKED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_PORDER,  INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_RINGER_MODE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_RINGTONE, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_NOTIFICATION, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_MEDIA, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_ALARM, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_SYSTEM, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_VOICE, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_RINGTONE_CHANGE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_RINGTONE, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_NOTIFICATION_CHANGE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_NOTIFICATION, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_ALARM_CHANGE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_ALARM, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_AIRPLANE_MODE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_WIFI, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_BLUETOOTH, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_SCREEN_TIMEOUT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_BRIGHTNESS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_WALLPAPER_CHANGE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_WALLPAPER, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_MOBILE_DATA, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_MOBILE_DATA_PREFS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_GPS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_RUN_APPLICATION_CHANGE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_AUTOSYNC, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SHOW_IN_ACTIVATOR, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_AUTOROTATE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_LOCATION_SERVICE_PREFS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_SPEAKER_PHONE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_NFC, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DURATION, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_AFTER_DURATION_DO, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_ZEN_MODE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_KEYGUARD, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VIBRATE_ON_TOUCH, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_WIFI_AP, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_POWER_SAVE_MODE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_ASK_FOR_DURATION, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_NETWORK_TYPE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_NOTIFICATION_LED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VIBRATE_WHEN_RINGING, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_WALLPAPER_FOR, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_HIDE_STATUS_BAR_ICON, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_LOCK_DEVICE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_CONNECT_TO_SSID, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_APPLICATION_DISABLE_WIFI_SCANNING, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DURATION_NOTIFICATION_SOUND, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DURATION_NOTIFICATION_VIBRATE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_WIFI_AP_PREFS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_APPLICATION_DISABLE_LOCATION_SCANNING, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_HEADS_UP_NOTIFICATIONS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_ACTIVATION_BY_USER_COUNT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_NETWORK_TYPE_PREFS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_CLOSE_ALL_APPLICATIONS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SCREEN_DARK_MODE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DTMF_TONE_WHEN_DIALING, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_ON_TOUCH, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_DTMF, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_ACCESSIBILITY, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_BLUETOOTH_SCO, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_AFTER_DURATION_PROFILE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_ALWAYS_ON_DISPLAY, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SCREEN_ON_PERMANENT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VOLUME_MUTE_SOUND, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_LOCATION_MODE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_APPLICATION_DISABLE_NOTIFICATION_SCANNING, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_GENERATE_NOTIFICATION, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_CAMERA_FLASH, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_NETWORK_TYPE_SIM1, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_NETWORK_TYPE_SIM2, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_MOBILE_DATA_SIM1, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_MOBILE_DATA_SIM2, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_DEFAULT_SIM_CARDS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_ONOFF_SIM1, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_ONOFF_SIM2, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_RINGTONE_CHANGE_SIM1, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_RINGTONE_SIM1, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_RINGTONE_CHANGE_SIM2, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_RINGTONE_SIM2, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_NOTIFICATION_CHANGE_SIM1, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_NOTIFICATION_SIM1, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_NOTIFICATION_CHANGE_SIM2, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_NOTIFICATION_SIM2, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_LIVE_WALLPAPER, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_VIBRATE_NOTIFICATIONS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_CHANGE_WALLPAPER_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_WALLPAPER_FOLDER, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_DEVICE_VPN_SETTINGS_PREFS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_END_OF_ACTIVATION_TYPE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_END_OF_ACTIVATION_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_APPLICATION_DISABLE_PERIODIC_SCANNING, INTEGER_TYPE, columns);
                break;
            case TABLE_EVENTS:
                createColumnWhenNotExists(db, table, KEY_E_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_FK_PROFILE_START, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_START_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_END_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_DAYS_OF_WEEK, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_USE_END_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_STATUS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_SOUND_START, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BATTERY_LEVEL_LOW, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BATTERY_LEVEL_HIGHT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BATTERY_CHARGING, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_TIME_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BATTERY_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALL_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALL_EVENT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALL_CONTACTS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALL_CONTACT_LIST_TYPE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_FK_PROFILE_END, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_FORCE_RUN, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BLOCKED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_PRIORITY, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ACCESSORY_ENABLED, INTEGER_TYPE, columns);
                //createColumnWhenNotExists(db, table, KEY_E_ACCESSORY_TYPE, TINTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_CALENDARS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_SEARCH_FIELD, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_SEARCH_STRING, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_EVENT_START_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_EVENT_END_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_EVENT_FOUND, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_WIFI_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_WIFI_SSID, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_WIFI_CONNECTION_TYPE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SCREEN_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SCREEN_EVENT_TYPE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_DELAY_START, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_IS_IN_DELAY_START, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SCREEN_WHEN_UNLOCKED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BLUETOOTH_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BLUETOOTH_ADAPTER_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BLUETOOTH_CONNECTION_TYPE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SMS_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SMS_CONTACTS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SMS_CONTACT_LIST_TYPE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SMS_START_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALL_CONTACT_GROUPS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SMS_CONTACT_GROUPS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_AT_END_DO, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_AVAILABILITY, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_MANUAL_PROFILE_ACTIVATION, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SMS_DURATION, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_APPLICATIONS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_START_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_DURATION, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BATTERY_POWER_SAVE_MODE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BLUETOOTH_DEVICES_TYPE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_APPLICATION_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_APPLICATION_APPLICATIONS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_END_WHEN_REMOVED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_LOCATION_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_LOCATION_FK_GEOFENCE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_LOCATION_WHEN_OUTSIDE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_DELAY_END, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_IS_IN_DELAY_END, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_START_STATUS_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_PAUSE_STATUS_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ORIENTATION_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ORIENTATION_SIDES, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ORIENTATION_DISTANCE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ORIENTATION_DISPLAY, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ORIENTATION_IGNORE_APPLICATIONS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_MOBILE_CELLS_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_MOBILE_CELLS_WHEN_OUTSIDE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_MOBILE_CELLS_CELLS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_LOCATION_GEOFENCES, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_START_ORDER, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NFC_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NFC_NFC_TAGS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NFC_DURATION, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NFC_START_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SMS_PERMANENT_RUN, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_PERMANENT_RUN, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NFC_PERMANENT_RUN, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_START_BEFORE_EVENT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_RADIO_SWITCH_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_RADIO_SWITCH_WIFI, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_RADIO_SWITCH_BLUETOOTH, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_RADIO_SWITCH_MOBILE_DATA, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_RADIO_SWITCH_GPS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_RADIO_SWITCH_NFC, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_RADIO_SWITCH_AIRPLANE_MODE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_VIBRATE_START, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALL_DURATION, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALL_PERMANENT_RUN, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALL_START_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_SOUND_REPEAT_START, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_IN_CALL, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_MISSED_CALL, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_START_WHEN_ACTIVATED_PROFILE, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BLUETOOTH_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_LOCATION_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_MOBILE_CELLS_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ORIENTATION_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_WIFI_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_APPLICATION_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BATTERY_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALL_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NFC_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ACCESSORY_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_RADIO_SWITCH_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SCREEN_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SMS_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_TIME_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_ALL_EVENTS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ALARM_CLOCK_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ALARM_CLOCK_PERMANENT_RUN, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ALARM_CLOCK_DURATION, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ALARM_CLOCK_START_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ALARM_CLOCK_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_SOUND_END, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_VIBRATE_END, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_BATTERY_PLUGGED, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_TIME_TYPE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ORIENTATION_CHECK_LIGHT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ORIENTATION_LIGHT_MIN, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ORIENTATION_LIGHT_MAX, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_CHECK_CONTACTS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_CONTACTS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_CONTACT_GROUPS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_CHECK_TEXT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_TEXT, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_CONTACT_LIST_TYPE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_DEVICE_BOOT_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_DEVICE_BOOT_PERMANENT_RUN, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_DEVICE_BOOT_DURATION, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_DEVICE_BOOT_START_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_DEVICE_BOOT_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ALARM_CLOCK_APPLICATIONS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ALARM_CLOCK_PACKAGE_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_AT_END_HOW_UNDO, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_STATUS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_EVENT_TODAY_EXISTS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_DAY_CONTAINS_EVENT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALENDAR_ALL_DAY_EVENTS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ACCESSORY_TYPE, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALL_FROM_SIM_SLOT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_CALL_FOR_SIM_CARD, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SMS_FROM_SIM_SLOT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SMS_FOR_SIM_CARD, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_MOBILE_CELLS_FOR_SIM_CARD, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SOUND_PROFILE_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SOUND_PROFILE_RINGER_MODES, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SOUND_PROFILE_ZEN_MODES, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_SOUND_PROFILE_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_PERIODIC_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_PERIODIC_MULTIPLY_INTERVAL, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_PERIODIC_DURATION, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_PERIODIC_START_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_PERIODIC_COUNTER, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_PERIODIC_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_RADIO_SWITCH_SIM_ON_OFF, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_VOLUMES_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_VOLUMES_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_VOLUMES_RINGTONE, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_VOLUMES_NOTIFICATION, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_VOLUMES_MEDIA, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_VOLUMES_ALARM, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_VOLUMES_SYSTEM, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_VOLUMES_VOICE, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_VOLUMES_BLUETOOTHSCO, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_VOLUMES_ACCESSIBILITY, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ACTIVATED_PROFILE_ENABLED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ACTIVATED_PROFILE_START_PROFILE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ACTIVATED_PROFILE_END_PROFILE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_E_ACTIVATED_PROFILE_RUNNING, INTEGER_TYPE, columns);
                break;
            case TABLE_EVENT_TIMELINE:
                createColumnWhenNotExists(db, table, KEY_ET_EORDER, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_ET_FK_EVENT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_ET_FK_PROFILE_RETURN, INTEGER_TYPE, columns);
                break;
            case TABLE_ACTIVITY_LOG:
                createColumnWhenNotExists(db, table, KEY_AL_LOG_DATE_TIME, DATETIME_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_AL_LOG_TYPE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_AL_EVENT_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_AL_PROFILE_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_AL_PROFILE_ICON, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_AL_DURATION_DELAY, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_AL_PROFILE_EVENT_COUNT, TEXT_TYPE, columns);
                break;
            case TABLE_GEOFENCES:
                createColumnWhenNotExists(db, table, KEY_G_LATITUDE, DOUBLE_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_G_LONGITUDE, DOUBLE_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_G_RADIUS, FLOAT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_G_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_G_CHECKED, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_G_TRANSITION, INTEGER_TYPE, columns);
                break;
            case TABLE_SHORTCUTS:
                createColumnWhenNotExists(db, table, KEY_S_INTENT, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_S_NAME, TEXT_TYPE, columns);
                break;
            case TABLE_MOBILE_CELLS:
                createColumnWhenNotExists(db, table, KEY_MC_CELL_ID, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_MC_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_MC_NEW, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_MC_LAST_CONNECTED_TIME, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_MC_LAST_RUNNING_EVENTS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_MC_LAST_PAUSED_EVENTS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_MC_DO_NOT_DETECT, INTEGER_TYPE, columns);
                break;
            case TABLE_NFC_TAGS:
                createColumnWhenNotExists(db, table, KEY_NT_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_NT_UID, TEXT_TYPE, columns);
                break;
            case TABLE_INTENTS:
                createColumnWhenNotExists(db, table, KEY_IN_PACKAGE_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_CLASS_NAME, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_ACTION, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_DATA, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_MIME_TYPE, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_KEY_1, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_VALUE_1, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_TYPE_1, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_KEY_2, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_VALUE_2, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_TYPE_2, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_KEY_3, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_VALUE_3, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_TYPE_3, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_KEY_4, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_VALUE_4, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_TYPE_4, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_KEY_5, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_VALUE_5, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_TYPE_5, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_KEY_6, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_VALUE_6, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_TYPE_6, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_KEY_7, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_VALUE_7, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_TYPE_7, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_KEY_8, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_VALUE_8, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_TYPE_8, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_KEY_9, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_VALUE_9, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_TYPE_9, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_KEY_10, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_VALUE_10, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_EXTRA_TYPE_10, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_CATEGORIES, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_FLAGS, TEXT_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_NAME, TEXT_TYPE, columns);
                //createColumnWhenNotExists(db, table, KEY_IN_USED_COUNT, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_INTENT_TYPE, INTEGER_TYPE, columns);
                createColumnWhenNotExists(db, table, KEY_IN_DO_NOT_DELETE, INTEGER_TYPE, columns);
                break;
        }
    }

    private boolean columnExists (String column, List<String> columns) {
        boolean isExists = false;
        for (String _column : columns) {
            //Log.e("DatabaseHandler.columnExists", "oldVersion < 2446 --- "+_column);
            if (column.equalsIgnoreCase(_column)) {
                isExists = true;
                break;
            }
        }
        return isExists;
    }

    private void createColumnWhenNotExists(SQLiteDatabase db, String table, String column, String columnType, List<String> columns) {
        if (!columnExists(column, columns))
            // create column
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + columnType);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        importExportLock.lock();
//        try {
//            try {
//                startRunningUpgrade();

//                PPApplication.logE("[IN_LISTENER] DatabaseHandler.onCreate", "xxx");

                createTables(db);
                createIndexes(db);

//            } catch (Exception e) {
//                //PPApplication.recordException(e);
//            }
//        } finally {
//            stopRunningUpgrade();
//        }
    }

    /*@Override
    public void onOpen(SQLiteDatabase db) {
        db.disableWriteAheadLogging();
        super.onOpen(db);
    }*/

    @Override
    public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
//        importExportLock.lock();
//        try {
//            try {
//                startRunningUpgrade();

//                PPApplication.logE("[IN_LISTENER] DatabaseHandler.onDowngrade", "xxx");

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("DatabaseHandler.onDowngrade", "oldVersion=" + oldVersion);
                    PPApplication.logE("DatabaseHandler.onDowngrade", "newVersion=" + newVersion);
                }*/

                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MERGED_PROFILE);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_TIMELINE);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY_LOG);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_GEOFENCES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHORTCUTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOBILE_CELLS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NFC_TAGS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_INTENTS);

                createTables(db);
                createIndexes(db);

//            } catch (Exception e) {
//                //PPApplication.recordException(e);
//            }
//        } finally {
//            stopRunningUpgrade();
//        }
    }

    private void updateDb(SQLiteDatabase db, int oldVersion) {
        // check colums existence

        if (oldVersion < 16)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER_CHANGE + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER + "='-'");
        }

        if (oldVersion < 18)
        {
            String value = "=replace(" + KEY_ICON + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_ICON + value);
            value = "=replace(" + KEY_VOLUME_RINGTONE + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_RINGTONE + value);
            value = "=replace(" + KEY_VOLUME_NOTIFICATION + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_NOTIFICATION + value);
            value = "=replace(" + KEY_VOLUME_MEDIA + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_MEDIA + value);
            value = "=replace(" + KEY_VOLUME_ALARM + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_ALARM + value);
            value = "=replace(" + KEY_VOLUME_SYSTEM + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_SYSTEM + value);
            value = "=replace(" + KEY_VOLUME_VOICE + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_VOICE + value);
            value = "=replace(" + KEY_DEVICE_BRIGHTNESS + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_BRIGHTNESS + value);
            value = "=replace(" + KEY_DEVICE_WALLPAPER + ",':','|')";
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER + value);

        }

        if (oldVersion < 19)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_MOBILE_DATA + "=0");
        }

        if (oldVersion < 20)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_MOBILE_DATA_PREFS + "=0");
        }

        if (oldVersion < 21)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_GPS + "=0");
        }

        if (oldVersion < 22)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_RUN_APPLICATION_CHANGE + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + "=\"-\"");
        }

        if (oldVersion < 24)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOSYNC + "=0");
        }

        if (oldVersion < 26)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SHOW_IN_ACTIVATOR + "=1");
        }

        if (oldVersion < 29)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_END_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DAYS_OF_WEEK + "=\"#ALL#\"");
        }

        if (oldVersion < 30)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_USE_END_TIME + "=0");
        }

        if (oldVersion < 32)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_STATUS + "=0");
        }

        if (oldVersion < 1001)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=0");
        }

        if (oldVersion < 1002)
        {
            // autorotate off -> rotation 0
            // autorotate on -> autorotate
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=1 WHERE " + KEY_DEVICE_AUTOROTATE + "=1");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=1 WHERE " + KEY_DEVICE_AUTOROTATE + "=3");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=2 WHERE " + KEY_DEVICE_AUTOROTATE + "=2");
        }

        if (oldVersion < 1012)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL + "=15");
        }

        if (oldVersion < 1015)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_LOCATION_SERVICE_PREFS + "=0");
        }

        if (oldVersion < 1020)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_SPEAKER_PHONE + "=0");
        }

        if (oldVersion < 1022)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SOUND_START + "=\"\"");
        }

        if (oldVersion < 1023)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL_LOW + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_LEVEL_HIGHT + "=100");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_CHARGING + "=0");

            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_BATTERY_LEVEL +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        String batteryLevel = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_BATTERY_LEVEL));

                        db.execSQL("UPDATE " + TABLE_EVENTS +
                                " SET " + KEY_E_BATTERY_LEVEL_HIGHT + "=" + batteryLevel + " " +
                                "WHERE " + KEY_E_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1030)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_TIME_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_ENABLED + "=0");
        }

        if (oldVersion < 1035)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NFC + "=0");
        }

        if (oldVersion < 1040)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_EVENT + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_CONTACTS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_CONTACT_LIST_TYPE + "=0");
        }

        if (oldVersion < 1045)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_FK_PROFILE_END + "=" + Profile.PROFILE_NO_ACTIVATE);
        }

        if (oldVersion < 1050)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_FORCE_RUN + "=0");
        }

        if (oldVersion < 1051)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLOCKED + "=0");
        }

        if (oldVersion < 1060)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_UNDONE_PROFILE + "=0");

            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_FK_PROFILE_END +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        int fkProfileEnd = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_FK_PROFILE_END));

                        if (fkProfileEnd == Profile.PROFILE_NO_ACTIVATE)
                            db.execSQL("UPDATE " + TABLE_EVENTS +
                                    " SET " + KEY_E_UNDONE_PROFILE + "=1 " +
                                    "WHERE " + KEY_E_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1070)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PRIORITY + "=0");
        }

        if (oldVersion < 1080)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ACCESSORY_ENABLED + "=0");
            //db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ACCESSORY_TYPE + "=0");
        }

        if (oldVersion < 1081)
        {
            // conversion to GMT
            int gmtOffset = TimeZone.getDefault().getRawOffset();
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_TIME + "=" + KEY_E_START_TIME + "+" + gmtOffset);
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_END_TIME + "=" + KEY_E_END_TIME + "+" + gmtOffset);
        }

        if (oldVersion < 1090)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_CALENDARS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_SEARCH_FIELD + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_SEARCH_STRING + "=\"\"");
        }

        if (oldVersion < 1095)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_EVENT_START_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_EVENT_END_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_EVENT_FOUND + "=0");
        }

        if (oldVersion < 1100)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PRIORITY + "=4 WHERE " + KEY_E_PRIORITY + "=2");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PRIORITY + "=2 WHERE " + KEY_E_PRIORITY + "=1");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PRIORITY + "=-2 WHERE " + KEY_E_PRIORITY + "=-1");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PRIORITY + "=-4 WHERE " + KEY_E_PRIORITY + "=-2");
        }

        if (oldVersion < 1105)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_WIFI_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_WIFI_SSID + "=\"\"");
        }

        if (oldVersion < 1106)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_WIFI_CONNECTION_TYPE + "=1");
        }

        if (oldVersion < 1110)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SCREEN_ENABLED + "=0");
        }

        if (oldVersion < 1111)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SCREEN_EVENT_TYPE + "=1");
        }

        if (oldVersion < 1112)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DELAY_START + "=0");
        }

        if (oldVersion < 1113)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_IS_IN_DELAY_START + "=0");
        }

        if (oldVersion < 1120)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DURATION + "=" + Profile.AFTER_DURATION_DO_RESTART_EVENTS);
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_AFTER_DURATION_DO + "=" + Profile.AFTER_DURATION_DO_RESTART_EVENTS);
        }

        if (oldVersion < 1125)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SCREEN_WHEN_UNLOCKED + "=0");
        }

        if (oldVersion < 1130)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_ADAPTER_NAME + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=0");
        }

        if (oldVersion < 1140)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_ENABLED + "=0");
            //db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_EVENT + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_CONTACTS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_CONTACT_LIST_TYPE + "=0");
        }

        if (oldVersion < 1141)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_START_TIME + "=0");
        }

        if (oldVersion < 1150)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_ZEN_MODE + "=0");
        }

        if (oldVersion < 1156)
        {
            try {
                //if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
                //{
                final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_DEVICE_BRIGHTNESS +
                        " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        String brightness = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DEVICE_BRIGHTNESS));

                        //value|noChange|automatic|sharedProfile
                        String[] splits = brightness.split("\\|");

                        if (splits[2].equals("1")) // automatic is set
                        {
                            // hm, found brightness values without default profile :-/
                            /*
                            if (splits.length == 4)
                                brightness = adaptiveBrightnessValue+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                            else
                                brightness = adaptiveBrightnessValue+"|"+splits[1]+"|"+splits[2]+"|0";
                            */
                            if (splits.length == 4)
                                brightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + "|" + splits[1] + "|" + splits[2] + "|" + splits[3];
                            else
                                brightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + "|" + splits[1] + "|" + splits[2] + "|0";

                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                    " SET " + KEY_DEVICE_BRIGHTNESS + "=\"" + brightness + "\" " +
                                    "WHERE " + KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
                //}
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1160)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_KEYGUARD + "=0");
        }

        if (oldVersion < 1165)
        {
            try {
                final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_DEVICE_BRIGHTNESS +
                        " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        String brightness = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DEVICE_BRIGHTNESS));

                        //value|noChange|automatic|sharedProfile
                        String[] splits = brightness.split("\\|");

                        int percentage = Integer.parseInt(splits[0]);
                        percentage = (int) Profile.convertBrightnessToPercents(percentage/*, 255, 1*/);

                        // hm, found brightness values without default profile :-/
                        if (splits.length == 4)
                            brightness = percentage + "|" + splits[1] + "|" + splits[2] + "|" + splits[3];
                        else
                            brightness = percentage + "|" + splits[1] + "|" + splits[2] + "|0";

                        db.execSQL("UPDATE " + TABLE_PROFILES +
                                " SET " + KEY_DEVICE_BRIGHTNESS + "=\"" + brightness + "\" " +
                                "WHERE " + KEY_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1170)
        {
            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_DELAY_START +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ID));
                        int delayStart = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_DELAY_START)) * 60;  // conversion to seconds

                        db.execSQL("UPDATE " + TABLE_EVENTS +
                                " SET " + KEY_E_DELAY_START + "=" + delayStart + " " +
                                "WHERE " + KEY_E_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        /*
        if (oldVersion < 1175)
        {
            try {
                if (android.os.Build.VERSION.SDK_INT < 21)
                {
                    final String selectQuery = "SELECT " + KEY_ID + "," +
                                                    KEY_DEVICE_BRIGHTNESS +
                                                " FROM " + TABLE_PROFILES;

                    Cursor cursor = db.rawQuery(selectQuery, null);

                    // looping through all rows and adding to list
                    if (cursor.moveToFirst()) {
                        do {
                            long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                            String brightness = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DEVICE_BRIGHTNESS));

                            //value|noChange|automatic|sharedProfile
                            String[] splits = brightness.split("\\|");

                            if (splits[2].equals("1")) // automatic is set
                            {
                                int percentage = 50;

                                // hm, found brightness values without default profile :-/
                                if (splits.length == 4)
                                    brightness = percentage+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                                else
                                    brightness = percentage+"|"+splits[1]+"|"+splits[2]+"|0";

                                db.execSQL("UPDATE " + TABLE_PROFILES +
                                             " SET " + KEY_DEVICE_BRIGHTNESS + "=\"" + brightness +"\"" +
                                            "WHERE " + KEY_ID + "=" + id);
                            }

                        } while (cursor.moveToNext());
                    }

                    cursor.close();
                }
            } catch (Exception ignored) {}
        }
        */

        if (oldVersion < 1180)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_CONTACT_GROUPS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_CONTACT_GROUPS + "=\"\"");
        }

        if (oldVersion < 1210)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VIBRATE_ON_TOUCH + "=0");
        }

        if (oldVersion < 1220)
        {
            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_USE_END_TIME + "," +
                        KEY_E_START_TIME +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ID));
                        long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_START_TIME));

                        if (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_USE_END_TIME)) != 1)
                            db.execSQL("UPDATE " + TABLE_EVENTS +
                                    " SET " + KEY_E_END_TIME + "=" + (startTime + 5000) + ", "
                                    + KEY_E_USE_END_TIME + "=1" +
                                    " WHERE " + KEY_E_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1295)
        {
            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_UNDONE_PROFILE +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ID));
                        int atEndDo;

                        if ((cursor.getColumnIndex(KEY_E_UNDONE_PROFILE) == -1) || (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_UNDONE_PROFILE)) == 0))
                            atEndDo = Event.EATENDDO_NONE;
                        else
                            atEndDo = Event.EATENDDO_UNDONE_PROFILE;

                        db.execSQL("UPDATE " + TABLE_EVENTS +
                                " SET " + KEY_E_AT_END_DO + "=" + atEndDo +
                                " WHERE " + KEY_E_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1300)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_AVAILABILITY + "=0");
        }

        if (oldVersion < 1310)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MANUAL_PROFILE_ACTIVATION + "=0");
        }

        if (oldVersion < 1330)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WIFI_AP + "=0");
        }

        if (oldVersion < 1340)
        {
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_WIFI_AP + "=0");
        }

        if (oldVersion < 1350)
        {
            try {
                final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_DURATION +
                        " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        int delayStart = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DURATION)) * 60;  // conversion to seconds

                        db.execSQL("UPDATE " + TABLE_PROFILES +
                                " SET " + KEY_DURATION + "=" + delayStart + " " +
                                "WHERE " + KEY_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1370)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + "=-999");
        }

        if (oldVersion < 1380)
        {
            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_CALENDAR_SEARCH_STRING + "," +
                        KEY_E_WIFI_SSID + "," +
                        KEY_E_BLUETOOTH_ADAPTER_NAME +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ID));
                        String calendarSearchString = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_SEARCH_STRING)).replace("%", "\\%").replace("_", "\\_");
                        String wifiSSID = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_WIFI_SSID)).replace("%", "\\%").replace("_", "\\_");
                        String bluetoothAdapterName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_BLUETOOTH_ADAPTER_NAME)).replace("%", "\\%").replace("_", "\\_");

                        db.execSQL("UPDATE " + TABLE_EVENTS +
                                " SET " + KEY_E_CALENDAR_SEARCH_STRING + "=\"" + calendarSearchString + "\"," +
                                KEY_E_WIFI_SSID + "=\"" + wifiSSID + "\"," +
                                KEY_E_BLUETOOTH_ADAPTER_NAME + "=\"" + bluetoothAdapterName + "\"" +
                                " WHERE " + KEY_E_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1390)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_DURATION + "=5");
        }

        if (oldVersion < 1400)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_APPLICATIONS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_START_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_DURATION + "=5");
        }

        /*if (oldVersion < 1410)
        {
            try {
                final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_VOLUME_ZEN_MODE +
                        " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        int zenMode = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOLUME_ZEN_MODE));

                        if ((zenMode == 6) && (android.os.Build.VERSION.SDK_INT < 23)) // Alarms only zen mode is supported from Android 6.0
                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                    " SET " + KEY_VOLUME_ZEN_MODE + "=3" + " " +
                                    "WHERE " + KEY_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }*/

        if (oldVersion < 1420)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_POWER_SAVE_MODE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_POWER_SAVE_MODE + "=0");
        }

        if (oldVersion < 1430)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_POWER_SAVE_MODE + "=0");
        }

        if (oldVersion < 1440)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_DEVICES_TYPE + "=0");
        }

        if (oldVersion < 1450)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_APPLICATION_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_APPLICATION_APPLICATIONS + "=\"\"");
        }

        if (oldVersion < 1460)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_END_WHEN_REMOVED + "=0");
        }

        if (oldVersion < 1470)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS + "=0");
        }

        if (oldVersion < 1490)
        {
            db.execSQL("UPDATE " + TABLE_GEOFENCES + " SET " + KEY_G_CHECKED + "=0");
        }

        if (oldVersion < 1500)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_LOCATION_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_LOCATION_FK_GEOFENCE + "=0");
        }

        if (oldVersion < 1510) {
            db.execSQL("UPDATE " + TABLE_GEOFENCES + " SET " + KEY_G_TRANSITION + "=0");
        }

        if (oldVersion < 1520) {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_LOCATION_WHEN_OUTSIDE + "=0");
        }

        if (oldVersion < 1530)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DELAY_END + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_IS_IN_DELAY_END + "=0");
        }

        if (oldVersion < 1540)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_STATUS_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PAUSE_STATUS_TIME + "=0");
        }

        if (oldVersion < 1560)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_ASK_FOR_DURATION + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_ASK_FOR_DURATION + "=0");
        }

        if (oldVersion < 1570)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NETWORK_TYPE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_NETWORK_TYPE + "=0");
        }

        if (oldVersion < 1580)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_NOTIFICATION_LED + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_NOTIFICATION_LED + "=0");
        }

        if (oldVersion < 1600)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_SIDES + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_DISTANCE + "=0");
        }

        if (oldVersion < 1610)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_DISPLAY + "=\"\"");
        }

        if (oldVersion < 1620)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_IGNORE_APPLICATIONS + "=\"\"");
        }

        if (oldVersion < 1630)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VIBRATE_WHEN_RINGING + "=0");
        }

        if (oldVersion < 1640) {
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_VIBRATE_WHEN_RINGING + "=0");
        }

        if (oldVersion < 1660) {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER_FOR + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_WALLPAPER_FOR + "=0");
        }

        if (oldVersion < 1670)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MOBILE_CELLS_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + "=0");
        }

        if (oldVersion < 1680)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MOBILE_CELLS_CELLS +  "=\"\"");
        }

        if (oldVersion < 1700)
        {
            db.execSQL("UPDATE " + TABLE_MOBILE_CELLS + " SET " + KEY_MC_NEW +  "=0");
        }

        if (oldVersion < 1710)
        {
            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_LOCATION_FK_GEOFENCE +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long geofenceId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_LOCATION_FK_GEOFENCE));

                        ContentValues values = new ContentValues();

                        if (geofenceId > 0) {
                            values.put(KEY_E_LOCATION_GEOFENCES, String.valueOf(geofenceId));
                        } else {
                            values.put(KEY_E_LOCATION_GEOFENCES, "");
                        }
                        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ID))});

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1720)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_ORDER +  "=0");
        }

        if (oldVersion < 1740)
        {
            try {
                // initialize startOrder
                final String selectQuery = "SELECT " + KEY_E_ID +
                        " FROM " + TABLE_EVENTS +
                        " ORDER BY " + KEY_E_PRIORITY;

                Cursor cursor = db.rawQuery(selectQuery, null);

                int startOrder = 0;
                if (cursor.moveToFirst()) {
                    do {
                        //long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ID));
                        ContentValues values = new ContentValues();
                        values.put(KEY_E_START_ORDER, ++startOrder);
                        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ID))});
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 1750)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NFC_ENABLED + "=0");
        }

        if (oldVersion < 1770)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NFC_NFC_TAGS + "=\"\"");
        }

        if (oldVersion < 1780)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NFC_DURATION + "=5");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NFC_START_TIME + "=0");
        }

        if (oldVersion < 1790)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NFC_PERMANENT_RUN + "=1");
        }

        if (oldVersion < 1800)
        {
            db.execSQL("UPDATE " + TABLE_MOBILE_CELLS + " SET " + KEY_MC_LAST_CONNECTED_TIME +  "=0");
        }

        if (oldVersion < 1810) {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_HIDE_STATUS_BAR_ICON + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_HIDE_STATUS_BAR_ICON + "=0");
        }

        if (oldVersion < 1820)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_LOCK_DEVICE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_LOCK_DEVICE + "=0");
        }

        if (oldVersion < 1830)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_START_BEFORE_EVENT + "=0");
        }

        if (oldVersion < 1840)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_WIFI + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_BLUETOOTH + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_MOBILE_DATA + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_GPS + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_NFC + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_AIRPLANE_MODE + "=0");
        }

        /*if (oldVersion < 1850)
        {
        }*/

        if (oldVersion < 1860)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_CONNECT_TO_SSID + "=\""+Profile.CONNECTTOSSID_JUSTANY+"\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_CONNECT_TO_SSID + "=\""+Profile.CONNECTTOSSID_JUSTANY+"\"");
        }

        if (oldVersion < 1870)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_WIFI_SCANNING + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING + "=0");
        }

        if (oldVersion < 1880)
        {
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_WIFI_SCANNING + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING + "=0");
        }

        if (oldVersion < 1890) {
            changePictureFilePathToUri(db/*, false*/);
        }

        if (oldVersion < 1900)
        {
            // conversion into local time
            int gmtOffset = TimeZone.getDefault().getRawOffset();
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_TIME + "=" + KEY_E_START_TIME + "-" + gmtOffset);
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_END_TIME + "=" + KEY_E_END_TIME + "-" + gmtOffset);
        }

        if (oldVersion < 1910)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_VIBRATE_START + "=0");
        }

        if (oldVersion < 1920)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION + "=0");
        }

        if (oldVersion < 1930)
        {
            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_START_TIME + "," +
                        KEY_E_END_TIME +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        ContentValues values = new ContentValues();

                        long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_START_TIME));
                        long endTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_END_TIME));

                        Calendar calendar = Calendar.getInstance();

                        calendar.setTimeInMillis(startTime);
                        values.put(KEY_E_START_TIME, calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));
                        calendar.setTimeInMillis(endTime);
                        values.put(KEY_E_END_TIME, calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));

                        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ID))});
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }


        if (oldVersion < 1950)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DURATION_NOTIFICATION_SOUND + "=\"\"");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DURATION_NOTIFICATION_VIBRATE + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DURATION_NOTIFICATION_VIBRATE + "=\"\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DURATION_NOTIFICATION_VIBRATE + "=0");
        }

        if (oldVersion < 1960)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_DURATION + "=5");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_START_TIME + "=0");
        }

        if (oldVersion < 1970)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SOUND_REPEAT_START + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START + "=15");
        }

        if (oldVersion < 1980)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WIFI_AP_PREFS + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_WIFI_AP_PREFS + "=0");
        }

        if (oldVersion < 1990) {
            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        ContentValues values = new ContentValues();

                        int repeatInterval = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START));

                        values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, repeatInterval * 60);

                        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ID))});
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2000)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_IN_CALL + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_MISSED_CALL + "=0");
        }

        if (oldVersion < 2010)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_LOCATION_SCANNING + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_LOCATION_SCANNING + "=0");
        }

        if (oldVersion < 2020)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING + "=0");
        }

        if (oldVersion < 2030)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_HEADS_UP_NOTIFICATIONS + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_HEADS_UP_NOTIFICATIONS + "=0");
        }

        if (oldVersion < 2040)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_START_WHEN_ACTIVATED_PROFILE + "=\"\"");

            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_FK_PROFILE_START_WHEN_ACTIVATED +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        ContentValues values = new ContentValues();

                        long fkProfile = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED));

                        if (fkProfile != Profile.PROFILE_NO_ACTIVATE) {
                            values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, String.valueOf(fkProfile));
                            db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ID))});
                        }
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2050)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + "=\"-\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME + "=\"-\"");
        }

        if (oldVersion < 2060)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_ACTIVATION_BY_USER_COUNT + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_ACTIVATION_BY_USER_COUNT + "=0");
        }

        if (oldVersion < 2070)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NETWORK_TYPE_PREFS + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_NETWORK_TYPE_PREFS + "=0");
        }

        if (oldVersion < 2080)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_CLOSE_ALL_APPLICATIONS + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_CLOSE_ALL_APPLICATIONS + "=0");
        }

        if (oldVersion < 2090)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SCREEN_DARK_MODE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SCREEN_DARK_MODE + "=0");
        }

        if (oldVersion < 2100)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DTMF_TONE_WHEN_DIALING + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_ON_TOUCH + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DTMF_TONE_WHEN_DIALING + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_ON_TOUCH + "=0");
        }

        if (oldVersion < 2110)
        {
            try {
                final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_DEVICE_WIFI_AP +
                        " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        int wifiAP = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_WIFI_AP));

                        if ((wifiAP == 3) && (android.os.Build.VERSION.SDK_INT >= 26)) // Toggle is not supported for wifi AP in Android 8+
                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                    " SET " + KEY_DEVICE_WIFI_AP + "=0" + " " +
                                    "WHERE " + KEY_ID + "=" + id);

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2120) {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BLUETOOTH_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_LOCATION_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MOBILE_CELLS_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_WIFI_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2130) {
            db.execSQL("UPDATE " + TABLE_NFC_TAGS + " SET " + KEY_NT_UID + "=\"\"");
        }

        if (oldVersion < 2140) {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_APPLICATION_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NFC_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ACCESSORY_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SCREEN_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_SENSOR_PASSED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_TIME_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2150) {
            try {
                final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_LOCK_DEVICE +
                        " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        int lockDevice = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LOCK_DEVICE));

                        if (lockDevice == 3) {
                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                    " SET " + KEY_LOCK_DEVICE + "=1" + " " +
                                    "WHERE " + KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2160)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_ALL_EVENTS + "=0");
        }

        if (oldVersion < 2170)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ALARM_CLOCK_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ALARM_CLOCK_START_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ALARM_CLOCK_DURATION + "=5");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ALARM_CLOCK_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ALARM_CLOCK_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2180)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SOUND_END + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_VIBRATE_END + "=0");
        }

        if (oldVersion < 2200)
        {
            db.execSQL("UPDATE " + TABLE_INTENTS + " SET " + KEY_IN_NAME + "=\"\"");
        }

        if (oldVersion < 2230)
        {
            db.execSQL("UPDATE " + TABLE_INTENTS + " SET " + KEY_IN_ACTION + "=\"\"");
        }

        if (oldVersion < 2240)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_BATTERY_PLUGGED + "=\"\"");
        }

        if (oldVersion < 2270) {
            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_CALENDAR_SEARCH_STRING +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        String calendarSearchString = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_SEARCH_STRING));

                        String searchStringNew = "";
                        String[] searchStringSplits = calendarSearchString.split("\\|");
                        for (String split : searchStringSplits) {
                            if (!split.isEmpty()) {
                                String searchPattern = split;
                                if (searchPattern.startsWith("!")) {
                                    searchPattern = "\\" + searchPattern;
                                }
                                if (!searchStringNew.isEmpty())
                                    //noinspection StringConcatenationInLoop
                                    searchStringNew = searchStringNew + "|";
                                //noinspection StringConcatenationInLoop
                                searchStringNew = searchStringNew + searchPattern;
                            }
                        }

                        ContentValues values = new ContentValues();
                        values.put(KEY_E_CALENDAR_SEARCH_STRING, searchStringNew);

                        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ID))});
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        /*if (oldVersion < 2280)
        {
            db.execSQL("UPDATE " + TABLE_INTENTS + " SET " + KEY_IN_USED_COUNT + "=0");
        }*/

        if (oldVersion < 2290)
        {
            db.execSQL("UPDATE " + TABLE_INTENTS + " SET " + KEY_IN_INTENT_TYPE + "=0");
        }

        if (oldVersion < 2300) {
            try {
                final String selectQuery = "SELECT *" +
                        " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                //Profile sharedProfile = Profile.getProfileFromSharedPreferences(context/*, "profile_preferences_default_profile"*/);

                if (cursor.moveToFirst()) {
                    do {
                        Profile profile = new Profile(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_ICON)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CHECKED)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PORDER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOLUME_RINGER_MODE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOLUME_RINGTONE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOLUME_NOTIFICATION)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOLUME_MEDIA)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOLUME_ALARM)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOLUME_SYSTEM)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOLUME_VOICE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SOUND_RINGTONE_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_SOUND_RINGTONE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SOUND_NOTIFICATION_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_SOUND_NOTIFICATION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SOUND_ALARM_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_SOUND_ALARM)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_AIRPLANE_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_WIFI)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_BLUETOOTH)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_SCREEN_TIMEOUT)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_DEVICE_BRIGHTNESS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_WALLPAPER_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_DEVICE_WALLPAPER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_MOBILE_DATA)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_MOBILE_DATA_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_GPS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_RUN_APPLICATION_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_AUTOSYNC)),
                                (cursor.getColumnIndex(KEY_SHOW_IN_ACTIVATOR) != -1) && (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SHOW_IN_ACTIVATOR)) == 1),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_AUTOROTATE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_LOCATION_SERVICE_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOLUME_SPEAKER_PHONE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_NFC)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DURATION)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_AFTER_DURATION_DO)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOLUME_ZEN_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_KEYGUARD)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VIBRATE_ON_TOUCH)),
                                (cursor.getColumnIndex(KEY_DEVICE_WIFI_AP) != -1) ? cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_WIFI_AP)) : 0,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_POWER_SAVE_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ASK_FOR_DURATION)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_NETWORK_TYPE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTIFICATION_LED)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VIBRATE_WHEN_RINGING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_WALLPAPER_FOR)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_HIDE_STATUS_BAR_ICON)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_LOCK_DEVICE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_DEVICE_CONNECT_TO_SSID)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_APPLICATION_DISABLE_WIFI_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_DURATION_NOTIFICATION_SOUND)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DURATION_NOTIFICATION_VIBRATE)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_WIFI_AP_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_APPLICATION_DISABLE_LOCATION_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_HEADS_UP_NOTIFICATIONS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ACTIVATION_BY_USER_COUNT)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_NETWORK_TYPE_PREFS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_CLOSE_ALL_APPLICATIONS)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SCREEN_DARK_MODE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DTMF_TONE_WHEN_DIALING)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SOUND_ON_TOUCH)),
                                "-1|1|0",
                                "-1|1|0",
                                "-1|1|0",
                                Profile.PROFILE_NO_ACTIVATE,
                                0,
                                0,
                                false,
                                0,
                                0,
                                "0|0||",
                                0,
                                0,
                                0,
                                0,
                                0,
                                "0|0|0",
                                0,
                                0,
                                0,
                                "",
                                0,
                                "",
                                0,
                                "",
                                0,
                                "",
                                0,
                                "",
                                0,
                                "-",
                                0,
                                0,
                                0,
                                0,
                                0
                        );

                        // this change old, no longer used SHARED_PROFILE_VALUE to "Not used" value
                        //profile = Profile.getMappedProfile(profile, sharedProfile);
                        profile = Profile.removeSharedProfileParameters(profile);
                        if (profile != null) {
                            ContentValues values = new ContentValues();
                            values.put(KEY_NAME, profile._name);
                            values.put(KEY_ICON, profile._icon);
                            values.put(KEY_CHECKED, (profile._checked) ? 1 : 0);
                            values.put(KEY_PORDER, profile._porder);
                            values.put(KEY_VOLUME_RINGER_MODE, profile._volumeRingerMode);
                            values.put(KEY_VOLUME_ZEN_MODE, profile._volumeZenMode);
                            values.put(KEY_VOLUME_RINGTONE, profile._volumeRingtone);
                            values.put(KEY_VOLUME_NOTIFICATION, profile._volumeNotification);
                            values.put(KEY_VOLUME_MEDIA, profile._volumeMedia);
                            values.put(KEY_VOLUME_ALARM, profile._volumeAlarm);
                            values.put(KEY_VOLUME_SYSTEM, profile._volumeSystem);
                            values.put(KEY_VOLUME_VOICE, profile._volumeVoice);
                            values.put(KEY_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
                            values.put(KEY_SOUND_RINGTONE, profile._soundRingtone);
                            values.put(KEY_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
                            values.put(KEY_SOUND_NOTIFICATION, profile._soundNotification);
                            values.put(KEY_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
                            values.put(KEY_SOUND_ALARM, profile._soundAlarm);
                            values.put(KEY_DEVICE_AIRPLANE_MODE, profile._deviceAirplaneMode);
                            values.put(KEY_DEVICE_WIFI, profile._deviceWiFi);
                            values.put(KEY_DEVICE_BLUETOOTH, profile._deviceBluetooth);
                            values.put(KEY_DEVICE_SCREEN_TIMEOUT, profile._deviceScreenTimeout);
                            values.put(KEY_DEVICE_BRIGHTNESS, profile._deviceBrightness);
                            values.put(KEY_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
                            values.put(KEY_DEVICE_WALLPAPER, profile._deviceWallpaper);
                            values.put(KEY_DEVICE_MOBILE_DATA, profile._deviceMobileData);
                            values.put(KEY_DEVICE_MOBILE_DATA_PREFS, profile._deviceMobileDataPrefs);
                            values.put(KEY_DEVICE_GPS, profile._deviceGPS);
                            values.put(KEY_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange);
                            values.put(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
                            values.put(KEY_DEVICE_AUTOSYNC, profile._deviceAutoSync);
                            values.put(KEY_SHOW_IN_ACTIVATOR, (profile._showInActivator) ? 1 : 0);
                            values.put(KEY_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
                            values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs);
                            values.put(KEY_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone);
                            values.put(KEY_DEVICE_NFC, profile._deviceNFC);
                            values.put(KEY_DURATION, profile._duration);
                            values.put(KEY_AFTER_DURATION_DO, profile._afterDurationDo);
                            values.put(KEY_DURATION_NOTIFICATION_SOUND, profile._durationNotificationSound);
                            values.put(KEY_DURATION_NOTIFICATION_VIBRATE, profile._durationNotificationVibrate);
                            values.put(KEY_DEVICE_KEYGUARD, profile._deviceKeyguard);
                            values.put(KEY_VIBRATE_ON_TOUCH, profile._vibrationOnTouch);
                            values.put(KEY_DEVICE_WIFI_AP, profile._deviceWiFiAP);
                            values.put(KEY_DEVICE_POWER_SAVE_MODE, profile._devicePowerSaveMode);
                            values.put(KEY_ASK_FOR_DURATION, (profile._askForDuration) ? 1 : 0);
                            values.put(KEY_DEVICE_NETWORK_TYPE, profile._deviceNetworkType);
                            values.put(KEY_NOTIFICATION_LED, profile._notificationLed);
                            values.put(KEY_VIBRATE_WHEN_RINGING, profile._vibrateWhenRinging);
                            values.put(KEY_VIBRATE_NOTIFICATIONS, profile._vibrateNotifications);
                            values.put(KEY_DEVICE_WALLPAPER_FOR, profile._deviceWallpaperFor);
                            values.put(KEY_HIDE_STATUS_BAR_ICON, (profile._hideStatusBarIcon) ? 1 : 0);
                            values.put(KEY_LOCK_DEVICE, profile._lockDevice);
                            values.put(KEY_DEVICE_CONNECT_TO_SSID, profile._deviceConnectToSSID);
                            values.put(KEY_APPLICATION_DISABLE_WIFI_SCANNING, profile._applicationDisableWifiScanning);
                            values.put(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING, profile._applicationDisableBluetoothScanning);
                            values.put(KEY_DEVICE_WIFI_AP_PREFS, profile._deviceWiFiAPPrefs);
                            values.put(KEY_APPLICATION_DISABLE_LOCATION_SCANNING, profile._applicationDisableLocationScanning);
                            values.put(KEY_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, profile._applicationDisableMobileCellScanning);
                            values.put(KEY_APPLICATION_DISABLE_ORIENTATION_SCANNING, profile._applicationDisableOrientationScanning);
                            values.put(KEY_HEADS_UP_NOTIFICATIONS, profile._headsUpNotifications);
                            values.put(KEY_DEVICE_FORCE_STOP_APPLICATION_CHANGE, profile._deviceForceStopApplicationChange);
                            values.put(KEY_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, profile._deviceForceStopApplicationPackageName);
                            values.put(KEY_ACTIVATION_BY_USER_COUNT, profile._activationByUserCount);
                            values.put(KEY_DEVICE_NETWORK_TYPE_PREFS, profile._deviceNetworkTypePrefs);
                            values.put(KEY_DEVICE_CLOSE_ALL_APPLICATIONS, profile._deviceCloseAllApplications);
                            values.put(KEY_SCREEN_DARK_MODE, profile._screenDarkMode);
                            values.put(KEY_DTMF_TONE_WHEN_DIALING, profile._dtmfToneWhenDialing);
                            values.put(KEY_SOUND_ON_TOUCH, profile._soundOnTouch);
                            values.put(KEY_APPLICATION_DISABLE_NOTIFICATION_SCANNING, profile._applicationDisableNotificationScanning);
                            values.put(KEY_GENERATE_NOTIFICATION, profile._generateNotification);
                            values.put(KEY_CAMERA_FLASH, profile._cameraFlash);
                            values.put(KEY_DEVICE_NETWORK_TYPE_SIM1, profile._deviceNetworkTypeSIM1);
                            values.put(KEY_DEVICE_NETWORK_TYPE_SIM2, profile._deviceNetworkTypeSIM2);
                            values.put(KEY_DEVICE_MOBILE_DATA_SIM1, profile._deviceMobileDataSIM1);
                            values.put(KEY_DEVICE_MOBILE_DATA_SIM2, profile._deviceMobileDataSIM2);
                            values.put(KEY_DEVICE_DEFAULT_SIM_CARDS, profile._deviceDefaultSIMCards);
                            values.put(KEY_DEVICE_ONOFF_SIM1, profile._deviceOnOffSIM1);
                            values.put(KEY_DEVICE_ONOFF_SIM2, profile._deviceOnOffSIM2);
                            values.put(KEY_SOUND_RINGTONE_CHANGE_SIM1, profile._soundRingtoneChangeSIM1);
                            values.put(KEY_SOUND_RINGTONE_SIM1, profile._soundRingtoneSIM1);
                            values.put(KEY_SOUND_RINGTONE_CHANGE_SIM2, profile._soundRingtoneChangeSIM2);
                            values.put(KEY_SOUND_RINGTONE_SIM2, profile._soundRingtoneSIM2);
                            values.put(KEY_SOUND_NOTIFICATION_CHANGE_SIM1, profile._soundNotificationChangeSIM1);
                            values.put(KEY_SOUND_NOTIFICATION_SIM1, profile._soundNotificationSIM1);
                            values.put(KEY_SOUND_NOTIFICATION_CHANGE_SIM2, profile._soundNotificationChangeSIM2);
                            values.put(KEY_SOUND_NOTIFICATION_SIM2, profile._soundNotificationSIM2);
                            values.put(KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, profile._soundSameRingtoneForBothSIMCards);
                            values.put(KEY_DEVICE_LIVE_WALLPAPER, profile._deviceLiveWallpaper);
                            values.put(KEY_DEVICE_WALLPAPER_FOLDER, profile._deviceWallpaperFolder);
                            values.put(KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN, profile._applicationDisableGloabalEventsRun);
                            values.put(KEY_APPLICATION_DISABLE_PERIODIC_SCANNING, profile._applicationDisablePeriodicScanning);

                            // updating row
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                    new String[]{String.valueOf(profile._id)});
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2310)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_DTMF + "=\"-1|1|0\"");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_ACCESSIBILITY + "=\"-1|1|0\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_VOLUME_DTMF + "=\"-1|1|0\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_VOLUME_ACCESSIBILITY + "=\"-1|1|0\"");
        }

        if (oldVersion < 2320)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_BLUETOOTH_SCO + "=\"-1|1|0\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_VOLUME_BLUETOOTH_SCO + "=\"-1|1|0\"");
        }

        if (oldVersion < 2340)
        {
            try {
                final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_VOLUME_RINGER_MODE +
                        " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        int ringerMode = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOLUME_RINGER_MODE));

                        if (ringerMode == 2) {
                            ringerMode = 1;

                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                    " SET " + KEY_VOLUME_RINGER_MODE + "=" + ringerMode + ", " +
                                    KEY_VIBRATE_WHEN_RINGING + "=1" + " " +
                                    "WHERE " + KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2350)
        {
            db.execSQL("UPDATE " + TABLE_MOBILE_CELLS + " SET " + KEY_MC_LAST_RUNNING_EVENTS + "=\"\"");
        }

        if (oldVersion < 2360)
        {
            db.execSQL("UPDATE " + TABLE_MOBILE_CELLS + " SET " + KEY_MC_LAST_PAUSED_EVENTS + "=\"\"");
        }

        if (oldVersion < 2370)
        {
            db.execSQL("UPDATE " + TABLE_MOBILE_CELLS + " SET " + KEY_MC_DO_NOT_DETECT + "=0");
        }

        if (oldVersion < 2380)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_TIME_TYPE + "=0");
        }

        if (oldVersion < 2390)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_DURATION + "=0");
        }

        if (oldVersion < 2400)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_AFTER_DURATION_PROFILE + "=" + Profile.PROFILE_NO_ACTIVATE);
        }
        if (oldVersion < 2401)
        {
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_AFTER_DURATION_PROFILE + "=" + Profile.PROFILE_NO_ACTIVATE);
        }
        if (oldVersion < 2402)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_ALWAYS_ON_DISPLAY + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_ALWAYS_ON_DISPLAY + "=0");
        }

        if (oldVersion < 2403)
        {
            try {
                final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_VOLUME_RINGER_MODE + "," +
                        KEY_VOLUME_ZEN_MODE +
                        " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        int ringerMode = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOLUME_RINGER_MODE));
                        int zenMode = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOLUME_ZEN_MODE));

                        if ((ringerMode == 5) && (zenMode == 0)) {
                            ringerMode = 0;

                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                    " SET " + KEY_VOLUME_RINGER_MODE + "=" + ringerMode + ", " +
                                    KEY_VOLUME_ZEN_MODE + "=1" + " " +
                                    "WHERE " + KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2404)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_CHECK_LIGHT + "=0");
        }
        if (oldVersion < 2405)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_LIGHT_MIN + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ORIENTATION_LIGHT_MAX + "=0");
        }

        if (oldVersion < 2406)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_CHECK_CONTACTS + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_CONTACT_GROUPS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_CONTACTS + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_CHECK_TEXT + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_TEXT + "=\"\"");
        }
        if (oldVersion < 2407)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_CONTACT_LIST_TYPE + "=0");
        }

        if (oldVersion < 2408)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SCREEN_ON_PERMANENT + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SCREEN_ON_PERMANENT + "=0");
        }

        if (oldVersion < 2409) {
            db.execSQL("UPDATE " + TABLE_ACTIVITY_LOG + " SET " + KEY_AL_PROFILE_EVENT_COUNT + "=\"1 [0]\"");
        }

        if (oldVersion < 2410) {
            try {
                final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_DEVICE_SCREEN_TIMEOUT +
                        " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        int screenTimeout = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_SCREEN_TIMEOUT));

                        if ((screenTimeout == 6) || (screenTimeout == 8)) {
                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                    " SET " + KEY_DEVICE_SCREEN_TIMEOUT + "=0, " +
                                    KEY_SCREEN_ON_PERMANENT + "=1" + " " +
                                    "WHERE " + KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2420)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DEVICE_BOOT_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DEVICE_BOOT_START_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DEVICE_BOOT_DURATION + "=5");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DEVICE_BOOT_PERMANENT_RUN + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_DEVICE_BOOT_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2421)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ALARM_CLOCK_APPLICATIONS + "=\"\"");
        }

        if (oldVersion < 2422)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ALARM_CLOCK_PACKAGE_NAME + "=\"\"");
        }

        if (oldVersion < 2423)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_MUTE_SOUND + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_VOLUME_MUTE_SOUND + "=0");
        }

        if (oldVersion < 2424)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_LOCATION_MODE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_LOCATION_MODE + "=0");
        }

        if (oldVersion < 2425)
        {
            try {
                final String selectQuery = "SELECT " + KEY_ID + "," +
                        KEY_VOLUME_ZEN_MODE +
                        " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        int zenMode = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOLUME_ZEN_MODE));

                        if (zenMode == 0) {
                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                    " SET " + KEY_VOLUME_ZEN_MODE + "=1" + " " +
                                    "WHERE " + KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2437)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_NOTIFICATION_SCANNING + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_NOTIFICATION_SCANNING + "=0");
        }

        if (oldVersion < 2439)
        {
            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_ORIENTATION_LIGHT_MIN + "," +
                        KEY_E_ORIENTATION_LIGHT_MAX +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        int lightMin = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ORIENTATION_LIGHT_MIN));
                        int lightMax = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ORIENTATION_LIGHT_MAX));

                        PPApplication.startHandlerThreadOrientationScanner();
                        if (PPApplication.handlerThreadOrientationScanner.maxLightDistance > 1.0f) {
                            lightMin = (int) Math.round(lightMin / 10000.0 * PPApplication.handlerThreadOrientationScanner.maxLightDistance);
                            lightMax = (int) Math.round(lightMax / 10000.0 * PPApplication.handlerThreadOrientationScanner.maxLightDistance);

                            db.execSQL("UPDATE " + TABLE_EVENTS +
                                    " SET " + KEY_E_ORIENTATION_LIGHT_MIN + "=" + lightMin + "," +
                                    KEY_E_ORIENTATION_LIGHT_MAX + "=" + lightMax + " " +
                                    "WHERE " + KEY_E_ID + "=" + id);
                        }
                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2440)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_STATUS + "=0");
        }

        if (oldVersion < 2441)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_GENERATE_NOTIFICATION + "=\"0|0||\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_GENERATE_NOTIFICATION + "=\"0|0||\"");
        }

        if (oldVersion < 2442)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END + "=0");
        }

        if (oldVersion < 2443)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_EVENT_TODAY_EXISTS + "=0");
        }

        if (oldVersion < 2444)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_DAY_CONTAINS_EVENT + "=0");
        }

        if (oldVersion < 2446)
        {
//            Log.e("DatabaseHandler.updateDb", "oldVersion < 2446 --- START");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALENDAR_ALL_DAY_EVENTS + "=0");

            try {
                List<String> columns = getTableColums(db, TABLE_EVENTS);
                if (columnExists(KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS, columns)) {
//                    Log.e("DatabaseHandler.updateDb", "oldVersion < 2446 --- column exists");

                    final String selectQuery = "SELECT " + KEY_E_ID + "," +
                            KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS +
                            " FROM " + TABLE_EVENTS;

                    Cursor cursor = db.rawQuery(selectQuery, null);

                    if (cursor.moveToFirst()) {
                        do {
                            long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ID));
                            int ignoreAllDayEvents = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS));

                            if (ignoreAllDayEvents == 1) {
                                db.execSQL("UPDATE " + TABLE_EVENTS +
                                        " SET " + KEY_E_CALENDAR_ALL_DAY_EVENTS + "=1 " +
                                        "WHERE " + KEY_E_ID + "=" + id);

                            }
                        } while (cursor.moveToNext());
                    }

                    cursor.close();
                }
//                else
//                    Log.e("DatabaseHandler.updateDb", "oldVersion < 2446 --- column NOT exists");
//
//                Log.e("DatabaseHandler.updateDb", "oldVersion < 2446 --- END");
            } catch (Exception ignored) {
                //Log.e("DatabaseHandler.updateDb", Log.getStackTraceString(e));
            }
        }

        if (oldVersion < 2448)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ACCESSORY_TYPE + "=\"\"");

            try {
                List<String> columns = getTableColums(db, TABLE_EVENTS);
                if (columnExists(KEY_E_PERIPHERAL_TYPE, columns)) {
//                    Log.e("DatabaseHandler.updateDb", "oldVersion < 2446 --- column exists");

                    final String selectQuery = "SELECT " + KEY_E_ID + "," +
                            KEY_E_PERIPHERAL_TYPE +
                            " FROM " + TABLE_EVENTS;

                    Cursor cursor = db.rawQuery(selectQuery, null);

                    if (cursor.moveToFirst()) {
                        do {
                            long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ID));
                            int peripheralType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_PERIPHERAL_TYPE));

                            db.execSQL("UPDATE " + TABLE_EVENTS +
                                    " SET " + KEY_E_ACCESSORY_TYPE + "=\"" + peripheralType + "\"" +
                                    " WHERE " + KEY_E_ID + "=" + id);

                        } while (cursor.moveToNext());
                    }

                    cursor.close();
                }
//                else
//                    Log.e("DatabaseHandler.updateDb", "oldVersion < 2446 --- column NOT exists");
//
//                Log.e("DatabaseHandler.updateDb", "oldVersion < 2446 --- END");
            } catch (Exception ignored) {
                //Log.e("DatabaseHandler.updateDb", Log.getStackTraceString(e));
            }
        }

        if (oldVersion < 2449)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_CAMERA_FLASH + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_CAMERA_FLASH + "=0");
        }

        if (oldVersion < 2450)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NETWORK_TYPE_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NETWORK_TYPE_SIM2 + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_NETWORK_TYPE_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_NETWORK_TYPE_SIM2 + "=0");
        }

        if (oldVersion < 2451)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_MOBILE_DATA_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_MOBILE_DATA_SIM2 + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_MOBILE_DATA_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_MOBILE_DATA_SIM2 + "=0");
        }

        if (oldVersion < 2453)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_DEFAULT_SIM_CARDS + "=\"0|0|0\"");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_DEFAULT_SIM_CARDS + "=\"0|0|0\"");
        }

        if (oldVersion < 2454)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_ONOFF_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_ONOFF_SIM2 + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_ONOFF_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_ONOFF_SIM2 + "=0");
        }

        if (oldVersion < 2459)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_RINGTONE_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_RINGTONE_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_RINGTONE_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_RINGTONE_SIM2 + "=\"\"");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_NOTIFICATION_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_NOTIFICATION_SIM2 + "=\"\"");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_RINGTONE_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_RINGTONE_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_RINGTONE_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_RINGTONE_SIM2 + "=\"\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_NOTIFICATION_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_NOTIFICATION_SIM2 + "=\"\"");
        }

        if (oldVersion < 2460)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + "=0");
        }

        if (oldVersion < 2461)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_FROM_SIM_SLOT + "=0");
        }

        if (oldVersion < 2462)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_FOR_SIM_CARD + "=0");
        }

        if (oldVersion < 2463)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_FROM_SIM_SLOT + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_FOR_SIM_CARD + "=0");
        }

        if (oldVersion < 2464)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MOBILE_CELLS_FOR_SIM_CARD + "=0");
        }

        if (oldVersion < 2466)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SOUND_PROFILE_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SOUND_PROFILE_RINGER_MODES + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SOUND_PROFILE_ZEN_MODES + "=\"\"");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SOUND_PROFILE_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2467)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_LIVE_WALLPAPER + "=\"\"");
        }

        if (oldVersion < 2468)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VIBRATE_NOTIFICATIONS + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_LIVE_WALLPAPER + "=\"\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_VIBRATE_NOTIFICATIONS + "=0");
        }

        if (oldVersion < 2469)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PERIODIC_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PERIODIC_START_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PERIODIC_COUNTER + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PERIODIC_DURATION + "=5");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PERIODIC_MULTIPLY_INTERVAL + "=1");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_PERIODIC_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2470)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_CHANGE_WALLPAPER_TIME + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_CHANGE_WALLPAPER_TIME + "=0");
        }

        if (oldVersion < 2471)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER_FOLDER + "='-'");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_WALLPAPER_FOLDER + "='-'");
        }

        if (oldVersion < 2472) {
            try {
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_PERIODIC_DURATION + "," +
                        KEY_E_PERIODIC_MULTIPLY_INTERVAL +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        ContentValues values = new ContentValues();

                        int multipleInterval = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_PERIODIC_MULTIPLY_INTERVAL));
                        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_PERIODIC_DURATION));

                        if ((multipleInterval == 0) || (duration == 0)) {
                            if (multipleInterval == 0)
                                values.put(KEY_E_PERIODIC_MULTIPLY_INTERVAL, 1);
                            if (duration == 0)
                                values.put(KEY_E_PERIODIC_DURATION, 5);

                            db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ID))});
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            } catch (Exception ignored) {}
        }

        if (oldVersion < 2473)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN + "=0");
        }

        if (oldVersion < 2474)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_VPN_SETTINGS_PREFS + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_VPN_SETTINGS_PREFS + "=0");
        }

        if (oldVersion < 2475)
        {
            db.execSQL("UPDATE " + TABLE_INTENTS + " SET " + KEY_IN_DO_NOT_DELETE + "=0");
        }

        if (oldVersion < 2477) {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS + "=0");
        }

        if (oldVersion < 2478) {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_SIM_ON_OFF + "=0");
        }

        if (oldVersion < 2479)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_END_OF_ACTIVATION_TYPE + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_END_OF_ACTIVATION_TIME + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_END_OF_ACTIVATION_TYPE + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_END_OF_ACTIVATION_TIME + "=0");
        }

        if (oldVersion < 2487)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_VOLUMES_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_VOLUMES_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2488)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE + "=0");
        }

        if (oldVersion < 2490)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_VOLUMES_RINGTONE + "='0|0|0'");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_VOLUMES_NOTIFICATION + "='0|0|0'");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_VOLUMES_MEDIA + "='0|0|0'");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_VOLUMES_ALARM + "='0|0|0'");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_VOLUMES_SYSTEM + "='0|0|0'");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_VOLUMES_VOICE  + "='0|0|0'");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_VOLUMES_BLUETOOTHSCO + "='0|0|0'");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_VOLUMES_ACCESSIBILITY + "='0|0|0'");
        }

        if (oldVersion < 2491)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_APPLICATION_DISABLE_PERIODIC_SCANNING + "=0");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_APPLICATION_DISABLE_PERIODIC_SCANNING + "=0");
        }

        if (oldVersion < 2492)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ACTIVATED_PROFILE_ENABLED + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED + "=0");
        }

        if (oldVersion < 2493)
        {
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ACTIVATED_PROFILE_START_PROFILE + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ACTIVATED_PROFILE_END_PROFILE + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_ACTIVATED_PROFILE_RUNNING + "=0");
        }

    }

    private void afterUpdateDb(SQLiteDatabase db) {
        Cursor cursorUpdateDB = null;
        String intentName;
        boolean found;
        ContentValues values = new ContentValues();

        // update volumes by device max value
        try {
            intentName = "[OpenVPN Connect - connect URL profile]";
            cursorUpdateDB = db.rawQuery("SELECT " + KEY_IN_NAME + " FROM " + TABLE_INTENTS +
                     " WHERE " + KEY_IN_NAME + "=\"" + intentName + "\"",
                    null);
            found = false;
            if (cursorUpdateDB.moveToFirst()) {
                do {
                    String name = cursorUpdateDB.getString(cursorUpdateDB.getColumnIndexOrThrow(KEY_IN_NAME));
                    if (name.equals(intentName)) {
                        found = true;
                        break;
                    }
                } while (cursorUpdateDB.moveToNext());
            }
            //Log.e("DatabaseHandler.afterUpdateDb", "(1) found="+found);
            if (!found) {
                values.put(KEY_IN_NAME, intentName);
                values.put(KEY_IN_ACTION, "net.openvpn.openvpn.CONNECT");
                //values.put(KEY_IN_ACTION, "android.intent.action.VIEW");
                values.put(KEY_IN_PACKAGE_NAME, "net.openvpn.openvpn");
                values.put(KEY_IN_CLASS_NAME, "net.openvpn.unified.MainActivity");
                values.put(KEY_IN_EXTRA_KEY_1, "net.openvpn.openvpn.AUTOSTART_PROFILE_NAME");
                values.put(KEY_IN_EXTRA_VALUE_1, "AS {your_profile_name}");
                values.put(KEY_IN_EXTRA_TYPE_1, 0); // string
                values.put(KEY_IN_EXTRA_KEY_2, "net.openvpn.openvpn.AUTOCONNECT");
                values.put(KEY_IN_EXTRA_VALUE_2, "true");
                values.put(KEY_IN_EXTRA_TYPE_2, 0); // string
                values.put(KEY_IN_INTENT_TYPE, 0); // activity
                values.put(KEY_IN_DO_NOT_DELETE, "1");
                db.insert(TABLE_INTENTS, null, values);
            }
            cursorUpdateDB.close();

            intentName = "[OpenVPN Connect - connect file profile]";
            cursorUpdateDB = db.rawQuery("SELECT " + KEY_IN_NAME + " FROM " + TABLE_INTENTS +
                            " WHERE " + KEY_IN_NAME + "=\"" + intentName + "\"",
                    null);
            found = false;
            if (cursorUpdateDB.moveToFirst()) {
                do {
                    String name = cursorUpdateDB.getString(cursorUpdateDB.getColumnIndexOrThrow(KEY_IN_NAME));
                    if (name.equals(intentName)) {
                        found = true;
                        break;
                    }
                } while (cursorUpdateDB.moveToNext());
            }
            //Log.e("DatabaseHandler.afterUpdateDb", "(2) found="+found);
            if (!found) {
                values.clear();
                values.put(KEY_IN_NAME, intentName);
                values.put(KEY_IN_ACTION, "net.openvpn.openvpn.CONNECT");
                //values.put(KEY_IN_ACTION, "android.intent.action.VIEW");
                values.put(KEY_IN_PACKAGE_NAME, "net.openvpn.openvpn");
                values.put(KEY_IN_CLASS_NAME, "net.openvpn.unified.MainActivity");
                values.put(KEY_IN_EXTRA_KEY_1, "net.openvpn.openvpn.AUTOSTART_PROFILE_NAME");
                values.put(KEY_IN_EXTRA_VALUE_1, "PC {your_profile_name}");
                values.put(KEY_IN_EXTRA_TYPE_1, 0); // string
                values.put(KEY_IN_EXTRA_KEY_2, "net.openvpn.openvpn.AUTOCONNECT");
                values.put(KEY_IN_EXTRA_VALUE_2, "true");
                values.put(KEY_IN_EXTRA_TYPE_2, 0); // string
                values.put(KEY_IN_INTENT_TYPE, 0); // activity
                values.put(KEY_IN_DO_NOT_DELETE, "1");
                db.insert(TABLE_INTENTS, null, values);
            }
            cursorUpdateDB.close();

            intentName = "[OpenVPN Connect - disconnect]";
            cursorUpdateDB = db.rawQuery("SELECT " + KEY_IN_NAME + " FROM " + TABLE_INTENTS +
                            " WHERE " + KEY_IN_NAME + "=\"" + intentName + "\"",
                    null);
            found = false;
            if (cursorUpdateDB.moveToFirst()) {
                do {
                    String name = cursorUpdateDB.getString(cursorUpdateDB.getColumnIndexOrThrow(KEY_IN_NAME));
                    if (name.equals(intentName)) {
                        found = true;
                        break;
                    }
                } while (cursorUpdateDB.moveToNext());
            }
            //Log.e("DatabaseHandler.afterUpdateDb", "(3) found="+found);
            if (!found) {
                values.clear();
                values.put(KEY_IN_NAME, intentName);
                values.put(KEY_IN_ACTION, "net.openvpn.openvpn.DISCONNECT");
                values.put(KEY_IN_PACKAGE_NAME, "net.openvpn.openvpn");
                values.put(KEY_IN_CLASS_NAME, "net.openvpn.unified.MainActivity");
                values.put(KEY_IN_EXTRA_KEY_1, "net.openvpn.openvpn.STOP");
                values.put(KEY_IN_EXTRA_VALUE_1, "true");
                values.put(KEY_IN_EXTRA_TYPE_1, 0); // string
                values.put(KEY_IN_INTENT_TYPE, 0); // activity
                values.put(KEY_IN_DO_NOT_DELETE, "1");
                db.insert(TABLE_INTENTS, null, values);
            }
            cursorUpdateDB.close();

            intentName = "[OpenVPN for Android - connect]";
            cursorUpdateDB = db.rawQuery("SELECT " + KEY_IN_NAME + " FROM " + TABLE_INTENTS +
                            " WHERE " + KEY_IN_NAME + "=\"" + intentName + "\"",
                    null);
            found = false;
            if (cursorUpdateDB.moveToFirst()) {
                do {
                    String name = cursorUpdateDB.getString(cursorUpdateDB.getColumnIndexOrThrow(KEY_IN_NAME));
                    if (name.equals(intentName)) {
                        found = true;
                        break;
                    }
                } while (cursorUpdateDB.moveToNext());
            }
            //Log.e("DatabaseHandler.afterUpdateDb", "(4) found="+found);
            if (!found) {
                values.put(KEY_IN_NAME, intentName);
                values.put(KEY_IN_ACTION, "android.intent.action.MAIN");
                values.put(KEY_IN_PACKAGE_NAME, "de.blinkt.openvpn");
                values.put(KEY_IN_CLASS_NAME, "de.blinkt.openvpn.api.ConnectVPN");
                values.put(KEY_IN_EXTRA_KEY_1, "de.blinkt.openvpn.api.profileName");
                values.put(KEY_IN_EXTRA_VALUE_1, "{your_profile_name}");
                values.put(KEY_IN_EXTRA_TYPE_1, 0); // string
                values.put(KEY_IN_INTENT_TYPE, 0); // activity
                values.put(KEY_IN_DO_NOT_DELETE, "1");
                db.insert(TABLE_INTENTS, null, values);
            }
            cursorUpdateDB.close();

            intentName = "[OpenVPN for Android - disconnect]";
            cursorUpdateDB = db.rawQuery("SELECT " + KEY_IN_NAME + " FROM " + TABLE_INTENTS +
                            " WHERE " + KEY_IN_NAME + "=\"" + intentName + "\"",
                    null);
            found = false;
            if (cursorUpdateDB.moveToFirst()) {
                do {
                    String name = cursorUpdateDB.getString(cursorUpdateDB.getColumnIndexOrThrow(KEY_IN_NAME));
                    if (name.equals(intentName)) {
                        found = true;
                        break;
                    }
                } while (cursorUpdateDB.moveToNext());
            }
            //Log.e("DatabaseHandler.afterUpdateDb", "(5) found="+found);
            if (!found) {
                values.put(KEY_IN_NAME, intentName);
                values.put(KEY_IN_ACTION, "android.intent.action.MAIN");
                values.put(KEY_IN_PACKAGE_NAME, "de.blinkt.openvpn");
                values.put(KEY_IN_CLASS_NAME, "de.blinkt.openvpn.api.DisconnectVPN");
                values.put(KEY_IN_EXTRA_KEY_1, "de.blinkt.openvpn.api.profileName");
                values.put(KEY_IN_EXTRA_VALUE_1, "{your_profile_name}");
                values.put(KEY_IN_EXTRA_TYPE_1, 0); // string
                values.put(KEY_IN_INTENT_TYPE, 0); // activity
                values.put(KEY_IN_DO_NOT_DELETE, "1");
                db.insert(TABLE_INTENTS, null, values);
            }
            cursorUpdateDB.close();
        } finally {
            if ((cursorUpdateDB != null) && (!cursorUpdateDB.isClosed()))
                cursorUpdateDB.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        importExportLock.lock();
//        try {
//            try {
//                startRunningUpgrade();

//                PPApplication.logE("[IN_LISTENER] DatabaseHandler.onUpgrade", "xxx");

                if (PPApplication.logEnabled()) {
                    PPApplication.logE("DatabaseHandler.onUpgrade", "--------- START");
                    PPApplication.logE("DatabaseHandler.onUpgrade", "oldVersion=" + oldVersion);
                    PPApplication.logE("DatabaseHandler.onUpgrade", "newVersion=" + newVersion);
                }

                /*
                // Drop older table if existed
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);

                // Create tables again
                onCreate(db);
                */

                createTables(db);
                createTableColumsWhenNotExists(db, TABLE_PROFILES);
                createTableColumsWhenNotExists(db, TABLE_MERGED_PROFILE);
                createTableColumsWhenNotExists(db, TABLE_EVENTS);
                createTableColumsWhenNotExists(db, TABLE_EVENT_TIMELINE);
                createTableColumsWhenNotExists(db, TABLE_ACTIVITY_LOG);
                createTableColumsWhenNotExists(db, TABLE_GEOFENCES);
                createTableColumsWhenNotExists(db, TABLE_SHORTCUTS);
                createTableColumsWhenNotExists(db, TABLE_MOBILE_CELLS);
                createTableColumsWhenNotExists(db, TABLE_NFC_TAGS);
                createTableColumsWhenNotExists(db, TABLE_INTENTS);
                createIndexes(db);

                updateDb(db, oldVersion);

                afterUpdateDb(db);

                DataWrapper dataWrapper = new DataWrapper(context, false, 0, false, 0, 0, 0f);
//                PPApplication.logE("[APP_START] DatabaseHandler.onUpgrade", "xxx");
                dataWrapper.restartEventsWithRescan(true, true, true, false, false, false);

                //PPApplication.sleep(10000); // for test only

                PPApplication.logE("DatabaseHandler.onUpgrade", " --------- END");

//            } catch (Exception e) {
//                //PPApplication.recordException(e);
//            }
//        } finally {
//            stopRunningUpgrade();
//        }
    }

    void startRunningCommand() throws Exception {
//        if (PPApplication.logEnabled()) {
//            PPApplication.logE("[DB_LOCK] ----------- DatabaseHandler.startRunningCommand", "lock");
//            PPApplication.logE("[DB_LOCK] ----------- DatabaseHandler.startRunningCommand", "runningCommand=" + runningCommand);
//            PPApplication.logE("[DB_LOCK] ----------- DatabaseHandler.startRunningCommand", "runningImportExport=" + runningImportExport);
//            PPApplication.logE("[DB_LOCK] ----------- DatabaseHandler.startRunningCommand", "runningUpgrade=" + runningUpgrade);
//        }

//        if (runningUpgrade)
//            runningUpgradeCondition.await();
        if (runningImportExport)
            runningImportExportCondition.await();
        runningCommand = true;
    }

    void stopRunningCommand() {
//        if (PPApplication.logEnabled()) {
//            PPApplication.logE("[DB_LOCK] =========== DatabaseHandler.stopRunningCommand", "unlock");
//            PPApplication.logE("[DB_LOCK] =========== DatabaseHandler.stopRunningCommand", "runningCommand=" + runningCommand);
//            PPApplication.logE("[DB_LOCK] =========== DatabaseHandler.stopRunningCommand", "runningImportExport=" + runningImportExport);
//            PPApplication.logE("[DB_LOCK] =========== DatabaseHandler.stopRunningCommand", "runningUpgrade=" + runningUpgrade);
//        }

        runningCommand = false;
        runningCommandCondition.signalAll();
        importExportLock.unlock();
    }

    private void startRunningImportExport() throws Exception {
//        if (PPApplication.logEnabled()) {
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.startRunningImportExport", "lock");
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.startRunningImportExport", "runningCommand=" + runningCommand);
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.startRunningImportExport", "runningImportExport=" + runningImportExport);
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.startRunningImportExport", "runningUpgrade=" + runningUpgrade);
//        }

//        if (runningUpgrade)
//            runningUpgradeCondition.await();
        if (runningCommand)
            runningCommandCondition.await();
        //PPApplication.logE("----------- DatabaseHandler.startRunningImportExport", "continue");
        runningImportExport = true;
    }

    private void stopRunningImportExport() {
//        if (PPApplication.logEnabled()) {
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.stopRunningImportExport", "unlock");
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.stopRunningImportExport", "runningCommand=" + runningCommand);
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.stopRunningImportExport", "runningImportExport=" + runningImportExport);
//            PPApplication.logE("[DB_LOCK] *********** DatabaseHandler.stopRunningImportExport", "runningUpgrade=" + runningUpgrade);
//        }

        runningImportExport = false;
        runningImportExportCondition.signalAll();
        importExportLock.unlock();
        //PPApplication.logE("----------- DatabaseHandler.stopRunningImportExport", "unlock");
    }

/*
    private void startRunningUpgrade() throws Exception {
        if (PPApplication.logEnabled()) {
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.startRunningUpgrade", "lock");
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.startRunningUpgrade", "runningCommand=" + runningCommand);
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.startRunningUpgrade", "runningImportExport=" + runningImportExport);
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.startRunningUpgrade", "runningUpgrade=" + runningUpgrade);
        }

        if (runningImportExport)
            runningImportExportCondition.await();
        if (runningCommand)
            runningCommandCondition.await();
        runningUpgrade = true;
    }

    private void stopRunningUpgrade() {
        if (PPApplication.logEnabled()) {
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.stopRunningUpgrade", "unlock");
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.stopRunningUpgrade", "runningCommand=" + runningCommand);
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.stopRunningUpgrade", "runningImportExport=" + runningImportExport);
            PPApplication.logE("[DB_LOCK] xxxxxxxxxxx DatabaseHandler.stopRunningUpgrade", "runningUpgrade=" + runningUpgrade);
        }

        runningUpgrade = false;
        runningUpgradeCondition.signalAll();
        importExportLock.unlock();
    }
 */

// PROFILES --------------------------------------------------------------------------------

    // Adding new profile
    void addProfile(Profile profile, boolean merged) {
        DatabaseHandlerProfiles.addProfile(this, profile, merged);
    }

    // Getting single profile
    Profile getProfile(long profile_id, boolean merged) {
        return DatabaseHandlerProfiles.getProfile(this, profile_id, merged);
    }

    // Getting All Profiles
    List<Profile> getAllProfiles() {
        return DatabaseHandlerProfiles.getAllProfiles(this);
    }

    // Updating single profile
    void updateProfile(Profile profile) {
        DatabaseHandlerProfiles.updateProfile(this, profile);
    }

    // Deleting single profile
    void deleteProfile(Profile profile) {
        DatabaseHandlerProfiles.deleteProfile(this, profile);
    }

    // Deleting all profiles
    boolean deleteAllProfiles() {
        return DatabaseHandlerProfiles.deleteAllProfiles(this);
    }

    void activateProfile(Profile profile)
    {
        DatabaseHandlerProfiles.activateProfile(this, profile);
    }

    void deactivateProfile()
    {
        DatabaseHandlerProfiles.deactivateProfile(this);
    }

    Profile getActivatedProfile()
    {
        return DatabaseHandlerProfiles.getActivatedProfile(this);
    }

    long getProfileIdByName(String name)
    {
        return DatabaseHandlerProfiles.getProfileIdByName(this, name);
    }

    void setProfileOrder(List<Profile> list)
    {
        DatabaseHandlerProfiles.setProfileOrder(this, list);
    }

    void getProfileIcon(Profile profile)
    {
        DatabaseHandlerProfiles.getProfileIcon(this, profile);
    }

    void saveMergedProfile(Profile profile) {
        DatabaseHandlerProfiles.saveMergedProfile(this, profile);
    }

    // this is called only from onUpgrade and importDB
    // for this, is not needed calling importExportLock.lock();
    private void changePictureFilePathToUri(SQLiteDatabase database/*, boolean lock*/) {
        try {
            SQLiteDatabase db;
            if (database == null) {
                //SQLiteDatabase db = this.getWritableDatabase();
                db = getMyWritableDatabase();
            } else
                db = database;

            final String selectQuery = "SELECT " + KEY_ID + "," +
                    KEY_ICON + "," +
                    KEY_DEVICE_WALLPAPER_CHANGE + "," +
                    KEY_DEVICE_WALLPAPER +
                    " FROM " + TABLE_PROFILES;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (database == null)
                db.beginTransaction();
            //noinspection TryFinallyCanBeTryWithResources
            try {

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
                        String icon = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ICON));
                        int wallpaperChange = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DEVICE_WALLPAPER_CHANGE));

                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "id=" + id);
                            PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "icon=" + icon);
                            PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "wallpaperChange=" + wallpaperChange);
                        }*/

                        ContentValues values = new ContentValues();

                        try {
                            String[] splits = icon.split("\\|");
                            String isIconResourceId = splits[1];
                            //PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "isIconResourceId=" + isIconResourceId);
                            if (!isIconResourceId.equals("1")) {
                                values.put(KEY_ICON, "ic_profile_default|1|0|0");
                            }
                        } catch (Exception e) {
                            //Log.e("DatabaseHandler.changePictureFilePathToUri", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                            values.put(KEY_ICON, "ic_profile_default|1|0|0");
                        }
                        if (wallpaperChange == 1) {
                            values.put(KEY_DEVICE_WALLPAPER_CHANGE, 0);
                        }
                        values.put(KEY_DEVICE_WALLPAPER, "-");

                        //PPApplication.logE("DatabaseHandler.changePictureFilePathToUri", "values.size()=" + values.size());
                        if (values.size() > 0) {
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
                        }

                    } while (cursor.moveToNext());
                }

                if (database == null)
                    db.setTransactionSuccessful();

            } catch (Exception e) {
                //Error in between database transaction
                PPApplication.recordException(e);
                //Log.e("DatabaseHandler.changePictureFilePathToUri", Log.getStackTraceString(e));
            } finally {
                if (database == null)
                    db.endTransaction();
                cursor.close();
            }

            //db.close();
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    long getActivationByUserCount(long profileId) {
        return DatabaseHandlerProfiles.getActivationByUserCount(this, profileId);
    }

    void increaseActivationByUserCount(Profile profile) {
        DatabaseHandlerProfiles.increaseActivationByUserCount(this, profile);
    }

    List<Profile> getProfilesForDynamicShortcuts(/*boolean counted*/) {
        return DatabaseHandlerProfiles.getProfilesForDynamicShortcuts(this);
    }

    List<Profile> getProfilesInQuickTilesForDynamicShortcuts() {
        return DatabaseHandlerProfiles.getProfilesInQuickTilesForDynamicShortcuts(this);
    }

    void updateProfileShowInActivator(Profile profile) {
        DatabaseHandlerProfiles.updateProfileShowInActivator(this, profile);
    }

// EVENTS --------------------------------------------------------------------------------

    // Adding new event
    void addEvent(Event event) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                int startOrder = getMaxEventStartOrder() + 1;

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_NAME, event._name); // Event Name
                values.put(KEY_E_START_ORDER, startOrder); // start order
                values.put(KEY_E_FK_PROFILE_START, event._fkProfileStart); // profile start
                values.put(KEY_E_FK_PROFILE_END, event._fkProfileEnd); // profile end
                values.put(KEY_E_STATUS, event.getStatus()); // event status
                values.put(KEY_E_NOTIFICATION_SOUND_START, event._notificationSoundStart); // notification sound
                values.put(KEY_E_NOTIFICATION_VIBRATE_START, event._notificationVibrateStart); // notification vibrate
                values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_START, event._repeatNotificationStart); // repeat notification sound
                values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, event._repeatNotificationIntervalStart); // repeat notification sound interval
                values.put(KEY_E_NOTIFICATION_SOUND_END, event._notificationSoundEnd); // notification sound
                values.put(KEY_E_NOTIFICATION_VIBRATE_END, event._notificationVibrateEnd); // notification vibrate
                values.put(KEY_E_FORCE_RUN, event._ignoreManualActivation ? 1 : 0); // force run when manual profile activation
                values.put(KEY_E_BLOCKED, event._blocked ? 1 : 0); // temporary blocked
                values.put(KEY_E_PRIORITY, event._priority); // priority
                values.put(KEY_E_DELAY_START, event._delayStart); // delay for start
                values.put(KEY_E_IS_IN_DELAY_START, event._isInDelayStart ? 1 : 0); // event is in delay before start
                values.put(KEY_E_AT_END_DO, event._atEndDo); //at end of event do
                values.put(KEY_E_MANUAL_PROFILE_ACTIVATION, event._manualProfileActivation ? 1 : 0); // manual profile activation at start
                values.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                values.put(KEY_E_DELAY_END, event._delayEnd); // delay for end
                values.put(KEY_E_IS_IN_DELAY_END, event._isInDelayEnd ? 1 : 0); // event is in delay after pause
                values.put(KEY_E_START_STATUS_TIME, event._startStatusTime); // time for status RUNNING
                values.put(KEY_E_PAUSE_STATUS_TIME, event._pauseStatusTime); // time for change status from RUNNING to PAUSE
                values.put(KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION, event._noPauseByManualActivation ? 1 : 0); // no pause event by manual profile activation
                values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, event._startWhenActivatedProfile); // start when profile is activated
                //values.put(KEY_E_AT_END_HOW_UNDO, event._atEndHowUndo);
                values.put(KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END, event._manualProfileActivationAtEnd ? 1 : 0); // manual profile activation at end
                values.put(KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE, event._notificationSoundStartPlayAlsoInSilentMode ? 1 : 0);
                values.put(KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE, event._notificationSoundEndPlayAlsoInSilentMode ? 1 : 0);

                db.beginTransaction();

                try {
                    // Inserting Row
                    event._id = db.insert(TABLE_EVENTS, null, values);
                    updateEventPreferences(event, db);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting single event
    Event getEvent(long event_id) {
        importExportLock.lock();
        try {
            Event event = null;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{KEY_E_ID,
                                KEY_E_NAME,
                                KEY_E_START_ORDER,
                                KEY_E_FK_PROFILE_START,
                                KEY_E_FK_PROFILE_END,
                                KEY_E_STATUS,
                                KEY_E_NOTIFICATION_SOUND_START,
                                KEY_E_NOTIFICATION_VIBRATE_START,
                                KEY_E_NOTIFICATION_SOUND_REPEAT_START,
                                KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START,
                                KEY_E_NOTIFICATION_SOUND_END,
                                KEY_E_NOTIFICATION_VIBRATE_END,
                                KEY_E_FORCE_RUN,
                                KEY_E_BLOCKED,
                                KEY_E_PRIORITY,
                                KEY_E_DELAY_START,
                                KEY_E_IS_IN_DELAY_START,
                                KEY_E_AT_END_DO,
                                KEY_E_MANUAL_PROFILE_ACTIVATION,
                                KEY_E_START_WHEN_ACTIVATED_PROFILE,
                                KEY_E_DELAY_END,
                                KEY_E_IS_IN_DELAY_END,
                                KEY_E_START_STATUS_TIME,
                                KEY_E_PAUSE_STATUS_TIME,
                                KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION,
                                KEY_E_AT_END_HOW_UNDO,
                                KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END,
                                KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE,
                                KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event_id)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {

                        event = new Event(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_NAME)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_START_ORDER)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_FK_PROFILE_START)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_FK_PROFILE_END)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_STATUS)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_START)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_FORCE_RUN)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_BLOCKED)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_PRIORITY)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_DELAY_START)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_IS_IN_DELAY_START)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_AT_END_DO)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_MANUAL_PROFILE_ACTIVATION)) == 1,
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_START_WHEN_ACTIVATED_PROFILE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_DELAY_END)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_IS_IN_DELAY_END)) == 1,
                                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_START_STATUS_TIME)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_PAUSE_STATUS_TIME)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_VIBRATE_START)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_REPEAT_START)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_END)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_VIBRATE_END)) == 1,
                                //cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_AT_END_HOW_UNDO))
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE)) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE)) == 1
                        );
                    }

                    cursor.close();
                }

                if (event != null)
                    getEventPreferences(event, db);

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return event;
        } finally {
            stopRunningCommand();
        }
    }

    // Getting All Events
    List<Event> getAllEvents() {
        importExportLock.lock();
        try {
            List<Event> eventList = new ArrayList<>();
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_NAME + "," +
                        KEY_E_FK_PROFILE_START + "," +
                        KEY_E_FK_PROFILE_END + "," +
                        KEY_E_STATUS + "," +
                        KEY_E_NOTIFICATION_SOUND_START + "," +
                        KEY_E_NOTIFICATION_VIBRATE_START + "," +
                        KEY_E_NOTIFICATION_SOUND_REPEAT_START + "," +
                        KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START + "," +
                        KEY_E_NOTIFICATION_SOUND_END + "," +
                        KEY_E_NOTIFICATION_VIBRATE_END + "," +
                        KEY_E_FORCE_RUN + "," +
                        KEY_E_BLOCKED + "," +
                        KEY_E_PRIORITY + "," +
                        KEY_E_DELAY_START + "," +
                        KEY_E_IS_IN_DELAY_START + "," +
                        KEY_E_AT_END_DO + "," +
                        KEY_E_MANUAL_PROFILE_ACTIVATION + "," +
                        KEY_E_START_WHEN_ACTIVATED_PROFILE + "," +
                        KEY_E_DELAY_END + "," +
                        KEY_E_IS_IN_DELAY_END + "," +
                        KEY_E_START_STATUS_TIME + "," +
                        KEY_E_PAUSE_STATUS_TIME + "," +
                        KEY_E_START_ORDER + "," +
                        KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION + "," +
                        KEY_E_AT_END_HOW_UNDO + "," +
                        KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END + "," +
                        KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE + "," +
                        KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE +
                        " FROM " + TABLE_EVENTS +
                        " ORDER BY " + KEY_E_ID;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Event event = new Event();
                        event._id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ID));
                        event._name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_NAME));
                        event._fkProfileStart = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_FK_PROFILE_START));
                        event._fkProfileEnd = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_FK_PROFILE_END));
                        event.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_STATUS)));
                        event._notificationSoundStart = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_START));
                        event._notificationVibrateStart = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_VIBRATE_START)) == 1;
                        event._repeatNotificationStart = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_REPEAT_START)) == 1;
                        event._repeatNotificationIntervalStart = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START));
                        event._notificationSoundEnd = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_END));
                        event._notificationVibrateEnd = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_VIBRATE_END)) == 1;
                        event._ignoreManualActivation = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_FORCE_RUN)) == 1;
                        event._blocked = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_BLOCKED)) == 1;
                        event._priority = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_PRIORITY));
                        event._delayStart = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_DELAY_START));
                        event._isInDelayStart = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_IS_IN_DELAY_START)) == 1;
                        event._atEndDo = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_AT_END_DO));
                        event._manualProfileActivation = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_MANUAL_PROFILE_ACTIVATION)) == 1;
                        event._startWhenActivatedProfile = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_START_WHEN_ACTIVATED_PROFILE));
                        event._delayEnd = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_DELAY_END));
                        event._isInDelayEnd = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_IS_IN_DELAY_END)) == 1;
                        event._startStatusTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_START_STATUS_TIME));
                        event._pauseStatusTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_PAUSE_STATUS_TIME));
                        event._startOrder = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_START_ORDER));
                        event._noPauseByManualActivation = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION)) == 1;
                        //event._atEndHowUndo = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_AT_END_HOW_UNDO));
                        event._manualProfileActivationAtEnd = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END)) == 1;
                        event._notificationSoundStartPlayAlsoInSilentMode = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE)) == 1;
                        event._notificationSoundEndPlayAlsoInSilentMode = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE)) == 1;
                        event.createEventPreferences();
                        getEventPreferences(event, db);
                        eventList.add(event);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return eventList;
        } finally {
            stopRunningCommand();
        }
    }

    // Updating single event
    void updateEvent(Event event) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_NAME, event._name);
                values.put(KEY_E_START_ORDER, event._startOrder);
                values.put(KEY_E_FK_PROFILE_START, event._fkProfileStart);
                values.put(KEY_E_FK_PROFILE_END, event._fkProfileEnd);
                values.put(KEY_E_STATUS, event.getStatus());
                values.put(KEY_E_NOTIFICATION_SOUND_START, event._notificationSoundStart);
                values.put(KEY_E_NOTIFICATION_VIBRATE_START, event._notificationVibrateStart ? 1 : 0);
                values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_START, event._repeatNotificationStart ? 1 : 0);
                values.put(KEY_E_NOTIFICATION_SOUND_REPEAT_INTERVAL_START, event._repeatNotificationIntervalStart);
                values.put(KEY_E_NOTIFICATION_SOUND_END, event._notificationSoundEnd);
                values.put(KEY_E_NOTIFICATION_VIBRATE_END, event._notificationVibrateEnd ? 1 : 0);
                values.put(KEY_E_FORCE_RUN, event._ignoreManualActivation ? 1 : 0);
                values.put(KEY_E_BLOCKED, event._blocked ? 1 : 0);
                //values.put(KEY_E_UNDONE_PROFILE, 0);
                values.put(KEY_E_PRIORITY, event._priority);
                values.put(KEY_E_DELAY_START, event._delayStart);
                values.put(KEY_E_IS_IN_DELAY_START, event._isInDelayStart ? 1 : 0);
                values.put(KEY_E_AT_END_DO, event._atEndDo);
                values.put(KEY_E_MANUAL_PROFILE_ACTIVATION, event._manualProfileActivation ? 1 : 0);
                values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, event._startWhenActivatedProfile);
                values.put(KEY_E_DELAY_END, event._delayEnd);
                values.put(KEY_E_IS_IN_DELAY_END, event._isInDelayEnd ? 1 : 0);
                values.put(KEY_E_START_STATUS_TIME, event._startStatusTime);
                values.put(KEY_E_PAUSE_STATUS_TIME, event._pauseStatusTime);
                values.put(KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION, event._noPauseByManualActivation ? 1 : 0);
                //values.put(KEY_E_AT_END_HOW_UNDO, event._atEndHowUndo);
                values.put(KEY_E_MANUAL_PROFILE_ACTIVATION_AT_END, event._manualProfileActivationAtEnd ? 1 : 0);
                values.put(KEY_E_NOTIFICATION_SOUND_START_PLAY_ALSO_IN_SILENT_MODE, event._notificationSoundStartPlayAlsoInSilentMode ? 1 : 0);
                values.put(KEY_E_NOTIFICATION_SOUND_END_PLAY_ALSO_IN_SILENT_MODE, event._notificationSoundEndPlayAlsoInSilentMode ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});
                    updateEventPreferences(event, db);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateEvent", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting single event
    void deleteEvent(Event event) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();
                db.delete(TABLE_EVENTS, KEY_E_ID + " = ?",
                        new String[]{String.valueOf(event._id)});
                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting all events
    void deleteAllEvents() {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();
                db.delete(TABLE_EVENTS, null, null);
                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void unlinkEventsFromProfile(Profile profile)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {
                    ContentValues values = new ContentValues();
                    values.put(KEY_E_FK_PROFILE_START, 0);
                    db.update(TABLE_EVENTS, values, KEY_E_FK_PROFILE_START + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    ContentValues values2 = new ContentValues();
                    values2.put(KEY_E_FK_PROFILE_END, Profile.PROFILE_NO_ACTIVATE);
                    db.update(TABLE_EVENTS, values2, KEY_E_FK_PROFILE_END + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    ContentValues values3 = new ContentValues();
                    values3.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                    db.update(TABLE_EVENTS, values3, KEY_E_FK_PROFILE_START_WHEN_ACTIVATED + " = ?",
                            new String[]{String.valueOf(profile._id)});

                    final String selectQuery = "SELECT " + KEY_E_ID + "," +
                            KEY_E_START_WHEN_ACTIVATED_PROFILE +
                            " FROM " + TABLE_EVENTS;
                    Cursor cursor = db.rawQuery(selectQuery, null);
                    if (cursor.moveToFirst()) {
                        do {
                            String oldFkProfiles = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_START_WHEN_ACTIVATED_PROFILE));
                            if (!oldFkProfiles.isEmpty()) {
                                String[] splits = oldFkProfiles.split("\\|");
                                StringBuilder newFkProfiles = new StringBuilder();
                                for (String split : splits) {
                                    long fkProfile = Long.parseLong(split);
                                    if (fkProfile != profile._id) {
                                        if (newFkProfiles.length() > 0)
                                            newFkProfiles.append("|");
                                        newFkProfiles.append(split);
                                    }
                                }
                                values = new ContentValues();
                                values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, newFkProfiles.toString());
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ID))});
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void unlinkAllEvents()
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_FK_PROFILE_START, 0);
                values.put(KEY_E_FK_PROFILE_END, Profile.PROFILE_NO_ACTIVATE);
                values.put(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED, Profile.PROFILE_NO_ACTIVATE);
                values.put(KEY_E_START_WHEN_ACTIVATED_PROFILE, "");

                // updating row
                db.update(TABLE_EVENTS, values, null, null);

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting max(startOrder)
    private int getMaxEventStartOrder() {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                String countQuery = "SELECT MAX(" + KEY_E_START_ORDER + ") FROM " + TABLE_EVENTS;
                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        r = cursor.getInt(0);
                    }
                }

                cursor.close();
                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    void setEventStartOrder(List<Event> list)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();

                db.beginTransaction();
                try {

                    for (int i = 0; i < list.size(); i++) {
                        Event event = list.get(i);
                        event._startOrder = i + 1;

                        values.put(KEY_E_START_ORDER, event._startOrder);

                        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                new String[]{String.valueOf(event._id)});
                    }

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    boolean isAnyEventEnabled() {
        importExportLock.lock();
        try {
            boolean r = false;
            try {
                startRunningCommand();

                String countQuery = "SELECT count(" + KEY_E_ID + ") FROM " + TABLE_EVENTS +
                                        " WHERE " + KEY_E_STATUS + " != " + Event.ESTATUS_STOP;
                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        r = cursor.getInt(0) > 0;
                    }
                }

                cursor.close();
                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    private void getEventPreferencesTime(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_TIME_ENABLED,
                        KEY_E_DAYS_OF_WEEK,
                        KEY_E_START_TIME,
                        KEY_E_END_TIME,
                        //KEY_E_USE_END_TIME
                        KEY_E_TIME_SENSOR_PASSED,
                        KEY_E_TIME_TYPE
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);

        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesTime eventPreferences = event._eventPreferencesTime;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_TIME_ENABLED)) == 1);

                String daysOfWeek = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_DAYS_OF_WEEK));

                if (daysOfWeek != null)
                {
                    String[] splits = daysOfWeek.split("\\|");
                    if (splits[0].equals(DaysOfWeekPreferenceX.allValue))
                    {
                        eventPreferences._sunday = true;
                        eventPreferences._monday = true;
                        eventPreferences._tuesday = true;
                        eventPreferences._wednesday = true;
                        eventPreferences._thursday = true;
                        eventPreferences._friday = true;
                        eventPreferences._saturday = true;
                    }
                    else
                    {
                        eventPreferences._sunday = false;
                        eventPreferences._monday = false;
                        eventPreferences._tuesday = false;
                        eventPreferences._wednesday = false;
                        eventPreferences._thursday = false;
                        eventPreferences._friday = false;
                        eventPreferences._saturday = false;
                        for (String value : splits)
                        {
                            eventPreferences._sunday = eventPreferences._sunday || value.equals("0");
                            eventPreferences._monday = eventPreferences._monday || value.equals("1");
                            eventPreferences._tuesday = eventPreferences._tuesday || value.equals("2");
                            eventPreferences._wednesday = eventPreferences._wednesday || value.equals("3");
                            eventPreferences._thursday = eventPreferences._thursday || value.equals("4");
                            eventPreferences._friday = eventPreferences._friday || value.equals("5");
                            eventPreferences._saturday = eventPreferences._saturday || value.equals("6");
                        }
                    }
                }
                eventPreferences._startTime = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_START_TIME));
                eventPreferences._endTime = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_END_TIME));
                //eventPreferences._useEndTime = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_USE_END_TIME)) == 1) ? true : false;
                eventPreferences._timeType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_TIME_TYPE));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_TIME_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesBattery(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_BATTERY_ENABLED,
                        KEY_E_BATTERY_LEVEL_LOW,
                        KEY_E_BATTERY_LEVEL_HIGHT,
                        KEY_E_BATTERY_CHARGING,
                        KEY_E_BATTERY_POWER_SAVE_MODE,
                        KEY_E_BATTERY_SENSOR_PASSED,
                        KEY_E_BATTERY_PLUGGED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesBattery eventPreferences = event._eventPreferencesBattery;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_BATTERY_ENABLED)) == 1);
                eventPreferences._levelLow = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_BATTERY_LEVEL_LOW));
                eventPreferences._levelHight = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_BATTERY_LEVEL_HIGHT));
                eventPreferences._charging = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_BATTERY_CHARGING));
                eventPreferences._powerSaveMode = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_BATTERY_POWER_SAVE_MODE)) == 1);
                eventPreferences._plugged = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_BATTERY_PLUGGED));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_BATTERY_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesCall(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_CALL_ENABLED,
                        KEY_E_CALL_EVENT,
                        KEY_E_CALL_CONTACTS,
                        KEY_E_CALL_CONTACT_LIST_TYPE,
                        KEY_E_CALL_CONTACT_GROUPS,
                        KEY_E_CALL_DURATION,
                        KEY_E_CALL_PERMANENT_RUN,
                        KEY_E_CALL_START_TIME,
                        KEY_E_CALL_SENSOR_PASSED,
                        KEY_E_CALL_FROM_SIM_SLOT,
                        KEY_E_CALL_FOR_SIM_CARD
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesCall eventPreferences = event._eventPreferencesCall;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALL_ENABLED)) == 1);
                eventPreferences._callEvent = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALL_EVENT));
                eventPreferences._contacts = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_CALL_CONTACTS));
                eventPreferences._contactListType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALL_CONTACT_LIST_TYPE));
                eventPreferences._contactGroups = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_CALL_CONTACT_GROUPS));
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_CALL_START_TIME));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALL_DURATION));
                eventPreferences._permanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALL_PERMANENT_RUN)) == 1);
                eventPreferences._fromSIMSlot = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALL_FROM_SIM_SLOT));
                eventPreferences._forSIMCard = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALL_FOR_SIM_CARD));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALL_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesAccessory(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_ACCESSORY_ENABLED,
                        KEY_E_ACCESSORY_TYPE,
                        KEY_E_ACCESSORY_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesAccessories eventPreferences = event._eventPreferencesAccessories;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ACCESSORY_ENABLED)) == 1);
                eventPreferences._accessoryType = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ACCESSORY_TYPE));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ACCESSORY_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesCalendar(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_CALENDAR_ENABLED,
                        KEY_E_CALENDAR_CALENDARS,
                        KEY_E_CALENDAR_SEARCH_FIELD,
                        KEY_E_CALENDAR_SEARCH_STRING,
                        KEY_E_CALENDAR_EVENT_START_TIME,
                        KEY_E_CALENDAR_EVENT_END_TIME,
                        KEY_E_CALENDAR_EVENT_FOUND,
                        KEY_E_CALENDAR_AVAILABILITY,
                        KEY_E_CALENDAR_STATUS,
                        //KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS,
                        KEY_E_CALENDAR_START_BEFORE_EVENT,
                        KEY_E_CALENDAR_SENSOR_PASSED,
                        KEY_E_CALENDAR_ALL_EVENTS,
                        KEY_E_CALENDAR_EVENT_TODAY_EXISTS,
                        KEY_E_CALENDAR_DAY_CONTAINS_EVENT,
                        KEY_E_CALENDAR_ALL_DAY_EVENTS
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesCalendar eventPreferences = event._eventPreferencesCalendar;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_ENABLED)) == 1);
                eventPreferences._calendars = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_CALENDARS));
                eventPreferences._searchField = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_SEARCH_FIELD));
                eventPreferences._searchString = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_SEARCH_STRING));
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_EVENT_START_TIME));
                eventPreferences._endTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_EVENT_END_TIME));
                eventPreferences._eventFound = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_EVENT_FOUND)) == 1);
                eventPreferences._availability = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_AVAILABILITY));
                eventPreferences._status = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_STATUS));
                //eventPreferences._ignoreAllDayEvents = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS)) == 1);
                eventPreferences._startBeforeEvent = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_START_BEFORE_EVENT));
                eventPreferences._allEvents = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_ALL_EVENTS)) == 1);
                eventPreferences._eventTodayExists = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_EVENT_TODAY_EXISTS)) == 1);
                eventPreferences._dayContainsEvent = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_DAY_CONTAINS_EVENT));
                eventPreferences._allDayEvents = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_ALL_DAY_EVENTS));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesWifi(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] { KEY_E_WIFI_ENABLED,
                                                KEY_E_WIFI_SSID,
                                                KEY_E_WIFI_CONNECTION_TYPE,
                                                KEY_E_WIFI_SENSOR_PASSED
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesWifi eventPreferences = event._eventPreferencesWifi;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_WIFI_ENABLED)) == 1);
                eventPreferences._SSID = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_WIFI_SSID));
                eventPreferences._connectionType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_WIFI_CONNECTION_TYPE));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_WIFI_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesScreen(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] { KEY_E_SCREEN_ENABLED,
                                                KEY_E_SCREEN_EVENT_TYPE,
                                                KEY_E_SCREEN_WHEN_UNLOCKED,
                                                KEY_E_SCREEN_SENSOR_PASSED
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesScreen eventPreferences = event._eventPreferencesScreen;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SCREEN_ENABLED)) == 1);
                eventPreferences._eventType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SCREEN_EVENT_TYPE));
                eventPreferences._whenUnlocked = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SCREEN_WHEN_UNLOCKED)) == 1);
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SCREEN_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesBluetooth(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                                 new String[] { KEY_E_BLUETOOTH_ENABLED,
                                                KEY_E_BLUETOOTH_ADAPTER_NAME,
                                                KEY_E_BLUETOOTH_CONNECTION_TYPE,
                                                KEY_E_BLUETOOTH_DEVICES_TYPE,
                                                KEY_E_BLUETOOTH_SENSOR_PASSED
                                                },
                                 KEY_E_ID + "=?",
                                 new String[] { String.valueOf(event._id) }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesBluetooth eventPreferences = event._eventPreferencesBluetooth;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_BLUETOOTH_ENABLED)) == 1);
                eventPreferences._adapterName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_BLUETOOTH_ADAPTER_NAME));
                eventPreferences._connectionType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_BLUETOOTH_CONNECTION_TYPE));
                //eventPreferences._devicesType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_BLUETOOTH_DEVICES_TYPE));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_BLUETOOTH_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesSMS(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_SMS_ENABLED,
                        //KEY_E_SMS_EVENT,
                        KEY_E_SMS_CONTACTS,
                        KEY_E_SMS_CONTACT_LIST_TYPE,
                        KEY_E_SMS_START_TIME,
                        KEY_E_SMS_CONTACT_GROUPS,
                        KEY_E_SMS_DURATION,
                        KEY_E_SMS_PERMANENT_RUN,
                        KEY_E_SMS_SENSOR_PASSED,
                        KEY_E_SMS_FROM_SIM_SLOT,
                        KEY_E_SMS_FOR_SIM_CARD
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesSMS eventPreferences = event._eventPreferencesSMS;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SMS_ENABLED)) == 1);
                //eventPreferences._smsEvent = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SMS_EVENT));
                eventPreferences._contacts = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_SMS_CONTACTS));
                eventPreferences._contactListType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SMS_CONTACT_LIST_TYPE));
                //if ((event != null) && (event._name != null) && (event._name.equals("SMS event")))
                //    PPApplication.logE("[SMS sensor] DatabaseHandler.getEventPreferencesSMS", "startTime="+eventPreferences._startTime);
                eventPreferences._contactGroups = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_SMS_CONTACT_GROUPS));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SMS_DURATION));
                eventPreferences._permanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SMS_PERMANENT_RUN)) == 1);
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_SMS_START_TIME));
                eventPreferences._fromSIMSlot = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SMS_FROM_SIM_SLOT));
                eventPreferences._forSIMCard = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SMS_FOR_SIM_CARD));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SMS_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesNotification(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_NOTIFICATION_ENABLED,
                        KEY_E_NOTIFICATION_APPLICATIONS,
                        //KEY_E_NOTIFICATION_START_TIME,
                        KEY_E_NOTIFICATION_DURATION,
                        //KEY_E_NOTIFICATION_END_WHEN_REMOVED,
                        //KEY_E_NOTIFICATION_PERMANENT_RUN,
                        KEY_E_NOTIFICATION_IN_CALL,
                        KEY_E_NOTIFICATION_MISSED_CALL,
                        KEY_E_NOTIFICATION_SENSOR_PASSED,
                        KEY_E_NOTIFICATION_CHECK_CONTACTS,
                        KEY_E_NOTIFICATION_CONTACT_GROUPS,
                        KEY_E_NOTIFICATION_CONTACTS,
                        KEY_E_NOTIFICATION_CHECK_TEXT,
                        KEY_E_NOTIFICATION_TEXT,
                        KEY_E_NOTIFICATION_CONTACT_LIST_TYPE
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesNotification eventPreferences = event._eventPreferencesNotification;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_ENABLED)) == 1);
                eventPreferences._applications = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_APPLICATIONS));
                eventPreferences._inCall = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_IN_CALL)) == 1);
                eventPreferences._missedCall = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_MISSED_CALL)) == 1);
                //eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_START_TIME));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_DURATION));
                //eventPreferences._endWhenRemoved = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_END_WHEN_REMOVED)) == 1);
                //eventPreferences._permanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_PERMANENT_RUN))) == 1);
                eventPreferences._checkContacts = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_CHECK_CONTACTS)) == 1);
                eventPreferences._contactGroups = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_CONTACT_GROUPS));
                eventPreferences._contacts = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_CONTACTS));
                eventPreferences._checkText = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_CHECK_TEXT)) == 1);
                eventPreferences._text = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_TEXT));
                eventPreferences._contactListType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_CONTACT_LIST_TYPE));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesApplication(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_APPLICATION_ENABLED,
                        KEY_E_APPLICATION_APPLICATIONS,
                        KEY_E_APPLICATION_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesApplication eventPreferences = event._eventPreferencesApplication;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_APPLICATION_ENABLED)) == 1);
                eventPreferences._applications = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_APPLICATION_APPLICATIONS));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_APPLICATION_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesLocation(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_LOCATION_ENABLED,
                        KEY_E_LOCATION_GEOFENCES,
                        KEY_E_LOCATION_WHEN_OUTSIDE,
                        KEY_E_LOCATION_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesLocation eventPreferences = event._eventPreferencesLocation;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_LOCATION_ENABLED)) == 1);
                eventPreferences._geofences = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_LOCATION_GEOFENCES));
                eventPreferences._whenOutside = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_LOCATION_WHEN_OUTSIDE)) == 1;
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_LOCATION_SENSOR_PASSED)));

            }
            cursor.close();
        }
    }

    private void getEventPreferencesOrientation(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_ORIENTATION_ENABLED,
                        KEY_E_ORIENTATION_SIDES,
                        KEY_E_ORIENTATION_DISTANCE,
                        KEY_E_ORIENTATION_DISPLAY,
                        KEY_E_ORIENTATION_IGNORE_APPLICATIONS,
                        KEY_E_ORIENTATION_SENSOR_PASSED,
                        KEY_E_ORIENTATION_CHECK_LIGHT,
                        KEY_E_ORIENTATION_LIGHT_MIN,
                        KEY_E_ORIENTATION_LIGHT_MAX
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesOrientation eventPreferences = event._eventPreferencesOrientation;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ORIENTATION_ENABLED)) == 1);
                eventPreferences._sides = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ORIENTATION_SIDES));
                eventPreferences._distance = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ORIENTATION_DISTANCE));
                eventPreferences._display = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ORIENTATION_DISPLAY));
                eventPreferences._checkLight = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ORIENTATION_CHECK_LIGHT)) == 1;
                eventPreferences._lightMin = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ORIENTATION_LIGHT_MIN)));
                eventPreferences._lightMax = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ORIENTATION_LIGHT_MAX)));
                eventPreferences._ignoredApplications = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ORIENTATION_IGNORE_APPLICATIONS));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ORIENTATION_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesMobileCells(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_MOBILE_CELLS_ENABLED,
                        KEY_E_MOBILE_CELLS_CELLS,
                        KEY_E_MOBILE_CELLS_WHEN_OUTSIDE,
                        KEY_E_MOBILE_CELLS_SENSOR_PASSED,
                        KEY_E_MOBILE_CELLS_FOR_SIM_CARD
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesMobileCells eventPreferences = event._eventPreferencesMobileCells;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_MOBILE_CELLS_ENABLED)) == 1);
                eventPreferences._cells = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_MOBILE_CELLS_CELLS));
                eventPreferences._whenOutside = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_MOBILE_CELLS_WHEN_OUTSIDE)) == 1;
                eventPreferences._forSIMCard = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_MOBILE_CELLS_FOR_SIM_CARD));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_MOBILE_CELLS_SENSOR_PASSED)));

            }
            cursor.close();
        }
    }

    private void getEventPreferencesNFC(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_NFC_ENABLED,
                            KEY_E_NFC_NFC_TAGS,
                            KEY_E_NFC_DURATION,
                            KEY_E_NFC_START_TIME,
                            KEY_E_NFC_PERMANENT_RUN,
                            KEY_E_NFC_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesNFC eventPreferences = event._eventPreferencesNFC;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NFC_ENABLED)) == 1);
                eventPreferences._nfcTags = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_NFC_NFC_TAGS));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NFC_DURATION));
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_NFC_START_TIME));
                eventPreferences._permanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NFC_PERMANENT_RUN)) == 1);
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_NFC_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesRadioSwitch(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_RADIO_SWITCH_ENABLED,
                        KEY_E_RADIO_SWITCH_WIFI,
                        KEY_E_RADIO_SWITCH_BLUETOOTH,
                        KEY_E_RADIO_SWITCH_MOBILE_DATA,
                        KEY_E_RADIO_SWITCH_GPS,
                        KEY_E_RADIO_SWITCH_NFC,
                        KEY_E_RADIO_SWITCH_AIRPLANE_MODE,
                        KEY_E_RADIO_SWITCH_SENSOR_PASSED,
                        KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS,
                        KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS,
                        KEY_E_RADIO_SWITCH_SIM_ON_OFF
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesRadioSwitch eventPreferences = event._eventPreferencesRadioSwitch;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_RADIO_SWITCH_ENABLED)) == 1);
                eventPreferences._wifi = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_RADIO_SWITCH_WIFI));
                eventPreferences._bluetooth = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_RADIO_SWITCH_BLUETOOTH));
                eventPreferences._mobileData = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_RADIO_SWITCH_MOBILE_DATA));
                eventPreferences._gps = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_RADIO_SWITCH_GPS));
                eventPreferences._nfc = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_RADIO_SWITCH_NFC));
                eventPreferences._airplaneMode = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_RADIO_SWITCH_AIRPLANE_MODE));
                eventPreferences._defaultSIMForCalls = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS));
                eventPreferences._defaultSIMForSMS = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS));
                eventPreferences._simOnOff = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_RADIO_SWITCH_SIM_ON_OFF));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_RADIO_SWITCH_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesAlarmClock(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_ALARM_CLOCK_ENABLED,
                        KEY_E_ALARM_CLOCK_START_TIME,
                        KEY_E_ALARM_CLOCK_DURATION,
                        KEY_E_ALARM_CLOCK_PERMANENT_RUN,
                        KEY_E_ALARM_CLOCK_SENSOR_PASSED,
                        KEY_E_ALARM_CLOCK_APPLICATIONS,
                        KEY_E_ALARM_CLOCK_PACKAGE_NAME
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesAlarmClock eventPreferences = event._eventPreferencesAlarmClock;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ALARM_CLOCK_ENABLED)) == 1);
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ALARM_CLOCK_START_TIME));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ALARM_CLOCK_DURATION));
                eventPreferences._permanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ALARM_CLOCK_PERMANENT_RUN)) == 1);
                eventPreferences._applications = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ALARM_CLOCK_APPLICATIONS));
                eventPreferences._alarmPackageName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ALARM_CLOCK_PACKAGE_NAME));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ALARM_CLOCK_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesDeviceBoot(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_DEVICE_BOOT_ENABLED,
                        KEY_E_DEVICE_BOOT_START_TIME,
                        KEY_E_DEVICE_BOOT_DURATION,
                        KEY_E_DEVICE_BOOT_PERMANENT_RUN,
                        KEY_E_DEVICE_BOOT_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesDeviceBoot eventPreferences = event._eventPreferencesDeviceBoot;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_DEVICE_BOOT_ENABLED)) == 1);
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_DEVICE_BOOT_START_TIME));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_DEVICE_BOOT_DURATION));
                eventPreferences._permanentRun = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_DEVICE_BOOT_PERMANENT_RUN)) == 1);
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_DEVICE_BOOT_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesSoundProfile(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_SOUND_PROFILE_ENABLED,
                        KEY_E_SOUND_PROFILE_RINGER_MODES,
                        KEY_E_SOUND_PROFILE_ZEN_MODES,
                        KEY_E_SOUND_PROFILE_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesSoundProfile eventPreferences = event._eventPreferencesSoundProfile;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SOUND_PROFILE_ENABLED)) == 1);
                eventPreferences._ringerModes = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_SOUND_PROFILE_RINGER_MODES));
                eventPreferences._zenModes = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_SOUND_PROFILE_ZEN_MODES));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SOUND_PROFILE_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesPeriodic(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_PERIODIC_ENABLED,
                        KEY_E_PERIODIC_START_TIME,
                        KEY_E_PERIODIC_COUNTER,
                        KEY_E_PERIODIC_DURATION,
                        KEY_E_PERIODIC_MULTIPLY_INTERVAL,
                        KEY_E_PERIODIC_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesPeriodic eventPreferences = event._eventPreferencesPeriodic;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_PERIODIC_ENABLED)) == 1);
                eventPreferences._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_PERIODIC_START_TIME));
                eventPreferences._counter = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_PERIODIC_COUNTER));
                eventPreferences._multipleInterval = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_PERIODIC_MULTIPLY_INTERVAL));
                eventPreferences._duration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_PERIODIC_DURATION));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_PERIODIC_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesVolumes(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_VOLUMES_ENABLED,
                        KEY_E_VOLUMES_RINGTONE,
                        KEY_E_VOLUMES_NOTIFICATION,
                        KEY_E_VOLUMES_MEDIA,
                        KEY_E_VOLUMES_ALARM,
                        KEY_E_VOLUMES_SYSTEM,
                        KEY_E_VOLUMES_VOICE,
                        KEY_E_VOLUMES_BLUETOOTHSCO,
                        KEY_E_VOLUMES_SENSOR_PASSED
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesVolumes eventPreferences = event._eventPreferencesVolumes;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_VOLUMES_ENABLED)) == 1);
                eventPreferences._volumeRingtone = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_VOLUMES_RINGTONE));
                eventPreferences._volumeNotification = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_VOLUMES_NOTIFICATION));
                eventPreferences._volumeMedia = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_VOLUMES_MEDIA));
                eventPreferences._volumeAlarm = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_VOLUMES_ALARM));
                eventPreferences._volumeSystem = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_VOLUMES_SYSTEM));
                eventPreferences._volumeVoice = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_VOLUMES_VOICE));
                eventPreferences._volumeBluetoothSCO = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_VOLUMES_BLUETOOTHSCO));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_VOLUMES_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    private void getEventPreferencesActivatedProfile(Event event, SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{KEY_E_ACTIVATED_PROFILE_ENABLED,
                        KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED,
                        KEY_E_ACTIVATED_PROFILE_START_PROFILE,
                        KEY_E_ACTIVATED_PROFILE_END_PROFILE,
                        KEY_E_ACTIVATED_PROFILE_RUNNING
                },
                KEY_E_ID + "=?",
                new String[]{String.valueOf(event._id)}, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                EventPreferencesActivatedProfile eventPreferences = event._eventPreferencesActivatedProfile;

                eventPreferences._enabled = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ACTIVATED_PROFILE_ENABLED)) == 1);
                eventPreferences._startProfile = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ACTIVATED_PROFILE_START_PROFILE));
                eventPreferences._endProfile = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ACTIVATED_PROFILE_END_PROFILE));
                eventPreferences._running = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ACTIVATED_PROFILE_RUNNING));
                eventPreferences.setSensorPassed(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED)));
            }
            cursor.close();
        }
    }

    // this is called only from getEvent and getAllEvents
    // for this is not needed to calling importExportLock.lock();
    private void getEventPreferences(Event event, SQLiteDatabase db) {
        getEventPreferencesTime(event, db);
        getEventPreferencesBattery(event, db);
        getEventPreferencesCall(event, db);
        getEventPreferencesAccessory(event, db);
        getEventPreferencesCalendar(event, db);
        getEventPreferencesWifi(event, db);
        getEventPreferencesScreen(event, db);
        getEventPreferencesBluetooth(event, db);
        getEventPreferencesSMS(event, db);
        getEventPreferencesNotification(event, db);
        getEventPreferencesApplication(event, db);
        getEventPreferencesLocation(event, db);
        getEventPreferencesOrientation(event, db);
        getEventPreferencesMobileCells(event, db);
        getEventPreferencesNFC(event, db);
        getEventPreferencesRadioSwitch(event, db);
        getEventPreferencesAlarmClock(event, db);
        getEventPreferencesDeviceBoot(event, db);
        getEventPreferencesSoundProfile(event, db);
        getEventPreferencesPeriodic(event, db);
        getEventPreferencesVolumes(event, db);
        getEventPreferencesActivatedProfile(event, db);
    }

    private void updateEventPreferencesTime(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesTime eventPreferences = event._eventPreferencesTime;

        String daysOfWeek = "";
        if (eventPreferences._sunday) daysOfWeek = daysOfWeek + "0|";
        if (eventPreferences._monday) daysOfWeek = daysOfWeek + "1|";
        if (eventPreferences._tuesday) daysOfWeek = daysOfWeek + "2|";
        if (eventPreferences._wednesday) daysOfWeek = daysOfWeek + "3|";
        if (eventPreferences._thursday) daysOfWeek = daysOfWeek + "4|";
        if (eventPreferences._friday) daysOfWeek = daysOfWeek + "5|";
        if (eventPreferences._saturday) daysOfWeek = daysOfWeek + "6|";

        values.put(KEY_E_TIME_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_DAYS_OF_WEEK, daysOfWeek);
        values.put(KEY_E_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_END_TIME, eventPreferences._endTime);
        //values.put(KEY_E_USE_END_TIME, (eventPreferences._useEndTime) ? 1 : 0);
        values.put(KEY_E_TIME_TYPE, eventPreferences._timeType);
        values.put(KEY_E_TIME_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesBattery(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesBattery eventPreferences = event._eventPreferencesBattery;

        values.put(KEY_E_BATTERY_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_BATTERY_LEVEL_LOW, eventPreferences._levelLow);
        values.put(KEY_E_BATTERY_LEVEL_HIGHT, eventPreferences._levelHight);
        values.put(KEY_E_BATTERY_CHARGING, eventPreferences._charging);
        values.put(KEY_E_BATTERY_POWER_SAVE_MODE, eventPreferences._powerSaveMode ? 1 : 0);
        values.put(KEY_E_BATTERY_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(KEY_E_BATTERY_PLUGGED, eventPreferences._plugged);

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesCall(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesCall eventPreferences = event._eventPreferencesCall;

        values.put(KEY_E_CALL_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_CALL_EVENT, eventPreferences._callEvent);
        values.put(KEY_E_CALL_CONTACTS, eventPreferences._contacts);
        values.put(KEY_E_CALL_CONTACT_LIST_TYPE, eventPreferences._contactListType);
        values.put(KEY_E_CALL_CONTACT_GROUPS, eventPreferences._contactGroups);
        values.put(KEY_E_CALL_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_CALL_DURATION, eventPreferences._duration);
        values.put(KEY_E_CALL_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(KEY_E_CALL_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(KEY_E_CALL_FROM_SIM_SLOT, eventPreferences._fromSIMSlot);
        values.put(KEY_E_CALL_FOR_SIM_CARD, eventPreferences._forSIMCard);

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesAccessory(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesAccessories eventPreferences = event._eventPreferencesAccessories;

        values.put(KEY_E_ACCESSORY_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_ACCESSORY_TYPE, eventPreferences._accessoryType);
        values.put(KEY_E_ACCESSORY_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesCalendar(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesCalendar eventPreferences = event._eventPreferencesCalendar;

        values.put(KEY_E_CALENDAR_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_CALENDAR_CALENDARS, eventPreferences._calendars);
        values.put(KEY_E_CALENDAR_SEARCH_FIELD, eventPreferences._searchField);
        values.put(KEY_E_CALENDAR_SEARCH_STRING, eventPreferences._searchString);
        values.put(KEY_E_CALENDAR_EVENT_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_CALENDAR_EVENT_END_TIME, eventPreferences._endTime);
        values.put(KEY_E_CALENDAR_EVENT_FOUND, (eventPreferences._eventFound) ? 1 : 0);
        values.put(KEY_E_CALENDAR_AVAILABILITY, eventPreferences._availability);
        values.put(KEY_E_CALENDAR_STATUS, eventPreferences._status);
        //values.put(KEY_E_CALENDAR_IGNORE_ALL_DAY_EVENTS, (eventPreferences._ignoreAllDayEvents) ? 1 : 0);
        values.put(KEY_E_CALENDAR_START_BEFORE_EVENT, eventPreferences._startBeforeEvent);
        values.put(KEY_E_CALENDAR_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(KEY_E_CALENDAR_ALL_EVENTS, (eventPreferences._allEvents) ? 1 : 0);
        values.put(KEY_E_CALENDAR_EVENT_TODAY_EXISTS, (eventPreferences._eventTodayExists) ? 1 : 0);
        values.put(KEY_E_CALENDAR_DAY_CONTAINS_EVENT, eventPreferences._dayContainsEvent);
        values.put(KEY_E_CALENDAR_ALL_DAY_EVENTS, eventPreferences._allDayEvents);

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesWifi(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesWifi eventPreferences = event._eventPreferencesWifi;

        values.put(KEY_E_WIFI_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_WIFI_SSID, eventPreferences._SSID);
        values.put(KEY_E_WIFI_CONNECTION_TYPE, eventPreferences._connectionType);
        values.put(KEY_E_WIFI_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesScreen(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesScreen eventPreferences = event._eventPreferencesScreen;

        values.put(KEY_E_SCREEN_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_SCREEN_EVENT_TYPE, eventPreferences._eventType);
        values.put(KEY_E_SCREEN_WHEN_UNLOCKED, (eventPreferences._whenUnlocked) ? 1 : 0);
        values.put(KEY_E_SCREEN_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesBluetooth(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesBluetooth eventPreferences = event._eventPreferencesBluetooth;

        values.put(KEY_E_BLUETOOTH_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_BLUETOOTH_ADAPTER_NAME, eventPreferences._adapterName);
        values.put(KEY_E_BLUETOOTH_CONNECTION_TYPE, eventPreferences._connectionType);
        //values.put(KEY_E_BLUETOOTH_DEVICES_TYPE, eventPreferences._devicesType);
        values.put(KEY_E_BLUETOOTH_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesSMS(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesSMS eventPreferences = event._eventPreferencesSMS;

        values.put(KEY_E_SMS_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        //values.put(KEY_E_SMS_EVENT, eventPreferences._smsEvent);
        values.put(KEY_E_SMS_CONTACTS, eventPreferences._contacts);
        values.put(KEY_E_SMS_CONTACT_LIST_TYPE, eventPreferences._contactListType);
        values.put(KEY_E_SMS_CONTACT_GROUPS, eventPreferences._contactGroups);
        values.put(KEY_E_SMS_DURATION, eventPreferences._duration);
        values.put(KEY_E_SMS_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(KEY_E_SMS_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_SMS_FROM_SIM_SLOT, eventPreferences._fromSIMSlot);
        values.put(KEY_E_SMS_FOR_SIM_CARD, eventPreferences._forSIMCard);

        values.put(KEY_E_SMS_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                        new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesNotification(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesNotification eventPreferences = event._eventPreferencesNotification;

        values.put(KEY_E_NOTIFICATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_NOTIFICATION_APPLICATIONS, eventPreferences._applications);
        values.put(KEY_E_NOTIFICATION_IN_CALL, (eventPreferences._inCall) ? 1 : 0);
        values.put(KEY_E_NOTIFICATION_MISSED_CALL, (eventPreferences._missedCall) ? 1 : 0);
        //values.put(KEY_E_NOTIFICATION_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_NOTIFICATION_DURATION, eventPreferences._duration);
        //values.put(KEY_E_NOTIFICATION_END_WHEN_REMOVED, (eventPreferences._endWhenRemoved) ? 1 : 0);
        //values.put(KEY_E_NOTIFICATION_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(KEY_E_NOTIFICATION_CHECK_CONTACTS, (eventPreferences._checkContacts) ? 1 : 0);
        values.put(KEY_E_NOTIFICATION_CONTACT_GROUPS, eventPreferences._contactGroups);
        values.put(KEY_E_NOTIFICATION_CONTACTS, eventPreferences._contacts);
        values.put(KEY_E_NOTIFICATION_CHECK_TEXT, (eventPreferences._checkText) ? 1 : 0);
        values.put(KEY_E_NOTIFICATION_TEXT, eventPreferences._text);
        values.put(KEY_E_NOTIFICATION_CONTACT_LIST_TYPE, eventPreferences._contactListType);
        values.put(KEY_E_NOTIFICATION_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesApplication(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesApplication eventPreferences = event._eventPreferencesApplication;

        values.put(KEY_E_APPLICATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_APPLICATION_APPLICATIONS, eventPreferences._applications);
        //values.put(KEY_E_APPLICATION_START_TIME, eventPreferences._startTime);
        //values.put(KEY_E_APPLICATION_DURATION, eventPreferences._duration);
        values.put(KEY_E_APPLICATION_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesLocation(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesLocation eventPreferences = event._eventPreferencesLocation;

        values.put(KEY_E_LOCATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_LOCATION_GEOFENCES, eventPreferences._geofences);
        values.put(KEY_E_LOCATION_WHEN_OUTSIDE, (eventPreferences._whenOutside) ? 1 : 0);
        values.put(KEY_E_LOCATION_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesOrientation(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesOrientation eventPreferences = event._eventPreferencesOrientation;

        values.put(KEY_E_ORIENTATION_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_ORIENTATION_SIDES, eventPreferences._sides);
        values.put(KEY_E_ORIENTATION_DISTANCE, eventPreferences._distance);
        values.put(KEY_E_ORIENTATION_DISPLAY, eventPreferences._display);
        values.put(KEY_E_ORIENTATION_CHECK_LIGHT, (eventPreferences._checkLight) ? 1 : 0);
        values.put(KEY_E_ORIENTATION_LIGHT_MIN, eventPreferences._lightMin);
        values.put(KEY_E_ORIENTATION_LIGHT_MAX, eventPreferences._lightMax);
        values.put(KEY_E_ORIENTATION_IGNORE_APPLICATIONS, eventPreferences._ignoredApplications);
        values.put(KEY_E_ORIENTATION_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesMobileCells(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesMobileCells eventPreferences = event._eventPreferencesMobileCells;

        values.put(KEY_E_MOBILE_CELLS_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_MOBILE_CELLS_CELLS, eventPreferences._cells);
        values.put(KEY_E_MOBILE_CELLS_WHEN_OUTSIDE, (eventPreferences._whenOutside) ? 1 : 0);
        values.put(KEY_E_MOBILE_CELLS_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(KEY_E_MOBILE_CELLS_FOR_SIM_CARD, eventPreferences._forSIMCard);

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesNFC(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesNFC eventPreferences = event._eventPreferencesNFC;

        values.put(KEY_E_NFC_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_NFC_NFC_TAGS, eventPreferences._nfcTags);
        values.put(KEY_E_NFC_DURATION, eventPreferences._duration);
        values.put(KEY_E_NFC_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_NFC_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(KEY_E_NFC_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesRadioSwitch(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesRadioSwitch eventPreferences = event._eventPreferencesRadioSwitch;

        values.put(KEY_E_RADIO_SWITCH_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_RADIO_SWITCH_WIFI, eventPreferences._wifi);
        values.put(KEY_E_RADIO_SWITCH_BLUETOOTH, eventPreferences._bluetooth);
        values.put(KEY_E_RADIO_SWITCH_MOBILE_DATA, eventPreferences._mobileData);
        values.put(KEY_E_RADIO_SWITCH_GPS, eventPreferences._gps);
        values.put(KEY_E_RADIO_SWITCH_NFC, eventPreferences._nfc);
        values.put(KEY_E_RADIO_SWITCH_AIRPLANE_MODE, eventPreferences._airplaneMode);
        values.put(KEY_E_RADIO_SWITCH_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS, eventPreferences._defaultSIMForCalls);
        values.put(KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS, eventPreferences._defaultSIMForSMS);
        values.put(KEY_E_RADIO_SWITCH_SIM_ON_OFF, eventPreferences._simOnOff);

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesAlarmClock(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesAlarmClock eventPreferences = event._eventPreferencesAlarmClock;

        values.put(KEY_E_ALARM_CLOCK_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_ALARM_CLOCK_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_ALARM_CLOCK_DURATION, eventPreferences._duration);
        values.put(KEY_E_ALARM_CLOCK_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(KEY_E_ALARM_CLOCK_SENSOR_PASSED, eventPreferences.getSensorPassed());
        values.put(KEY_E_ALARM_CLOCK_APPLICATIONS, eventPreferences._applications);
        values.put(KEY_E_ALARM_CLOCK_PACKAGE_NAME, eventPreferences._alarmPackageName);

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesDeviceBoot(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesDeviceBoot eventPreferences = event._eventPreferencesDeviceBoot;

        values.put(KEY_E_DEVICE_BOOT_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_DEVICE_BOOT_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_DEVICE_BOOT_DURATION, eventPreferences._duration);
        values.put(KEY_E_DEVICE_BOOT_PERMANENT_RUN, (eventPreferences._permanentRun) ? 1 : 0);
        values.put(KEY_E_DEVICE_BOOT_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesSoundProfile(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesSoundProfile eventPreferences = event._eventPreferencesSoundProfile;

        values.put(KEY_E_SOUND_PROFILE_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_SOUND_PROFILE_RINGER_MODES, eventPreferences._ringerModes);
        values.put(KEY_E_SOUND_PROFILE_ZEN_MODES, eventPreferences._zenModes);
        values.put(KEY_E_SOUND_PROFILE_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesPeriodic(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesPeriodic eventPreferences = event._eventPreferencesPeriodic;

        values.put(KEY_E_PERIODIC_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_PERIODIC_START_TIME, eventPreferences._startTime);
        values.put(KEY_E_PERIODIC_COUNTER, eventPreferences._counter);
        values.put(KEY_E_PERIODIC_DURATION, eventPreferences._duration);
        values.put(KEY_E_PERIODIC_MULTIPLY_INTERVAL, eventPreferences._multipleInterval);
        values.put(KEY_E_PERIODIC_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesVolumes(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesVolumes eventPreferences = event._eventPreferencesVolumes;

        values.put(KEY_E_VOLUMES_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_VOLUMES_RINGTONE, eventPreferences._volumeRingtone);
        values.put(KEY_E_VOLUMES_NOTIFICATION, eventPreferences._volumeNotification);
        values.put(KEY_E_VOLUMES_MEDIA, eventPreferences._volumeMedia);
        values.put(KEY_E_VOLUMES_ALARM, eventPreferences._volumeAlarm);
        values.put(KEY_E_VOLUMES_SYSTEM, eventPreferences._volumeSystem);
        values.put(KEY_E_VOLUMES_VOICE, eventPreferences._volumeVoice);
        values.put(KEY_E_VOLUMES_BLUETOOTHSCO, eventPreferences._volumeBluetoothSCO);
        values.put(KEY_E_VOLUMES_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    private void updateEventPreferencesActivatedProfile(Event event, SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        EventPreferencesActivatedProfile eventPreferences = event._eventPreferencesActivatedProfile;

        values.put(KEY_E_ACTIVATED_PROFILE_ENABLED, (eventPreferences._enabled) ? 1 : 0);
        values.put(KEY_E_ACTIVATED_PROFILE_START_PROFILE, eventPreferences._startProfile);
        values.put(KEY_E_ACTIVATED_PROFILE_END_PROFILE, eventPreferences._endProfile);
        values.put(KEY_E_ACTIVATED_PROFILE_RUNNING, eventPreferences._running);
        values.put(KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED, eventPreferences.getSensorPassed());

        // updating row
        db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                new String[] { String.valueOf(event._id) });
    }

    // this is called only from addEvent and updateEvent.
    // for this is not needed to calling importExportLock.lock();
    private void updateEventPreferences(Event event, SQLiteDatabase db) {
        updateEventPreferencesTime(event, db);
        updateEventPreferencesBattery(event, db);
        updateEventPreferencesCall(event, db);
        updateEventPreferencesAccessory(event, db);
        updateEventPreferencesCalendar(event, db);
        updateEventPreferencesWifi(event, db);
        updateEventPreferencesScreen(event, db);
        updateEventPreferencesBluetooth(event, db);
        updateEventPreferencesSMS(event, db);
        updateEventPreferencesNotification(event, db);
        updateEventPreferencesApplication(event, db);
        updateEventPreferencesLocation(event, db);
        updateEventPreferencesOrientation(event, db);
        updateEventPreferencesMobileCells(event, db);
        updateEventPreferencesNFC(event, db);
        updateEventPreferencesRadioSwitch(event, db);
        updateEventPreferencesAlarmClock(event, db);
        updateEventPreferencesDeviceBoot(event, db);
        updateEventPreferencesSoundProfile(event, db);
        updateEventPreferencesPeriodic(event, db);
        updateEventPreferencesVolumes(event, db);
        updateEventPreferencesActivatedProfile(event, db);
    }


    int getEventStatus(Event event)
    {
        importExportLock.lock();
        try {
            int eventStatus = 0;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_STATUS
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        eventStatus = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_STATUS));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return eventStatus;
        } finally {
            stopRunningCommand();
        }
    }

    void updateEventStatus(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                int status = event.getStatus();
                ContentValues values = new ContentValues();
                values.put(KEY_E_STATUS, status);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateEventStatus", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateEventBlocked(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_BLOCKED, event._blocked ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateEventBlocked", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void unblockAllEvents()
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_BLOCKED, 0);

                db.beginTransaction();

                try {
                    // updating rows
                    db.update(TABLE_EVENTS, values, null, null);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.unblockAllEvents", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateAllEventsStatus(@SuppressWarnings("SameParameterValue") int fromStatus,
                               @SuppressWarnings("SameParameterValue") int toStatus)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_STATUS, toStatus);

                db.beginTransaction();

                try {
                    // updating rows
                    db.update(TABLE_EVENTS, values, KEY_E_STATUS + " = ?",
                            new String[]{String.valueOf(fromStatus)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateAllEventsStatus", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    long getEventIdByName(String name)
    {
        importExportLock.lock();
        try {
            long id = 0;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{KEY_E_ID},
                        "trim(" + KEY_E_NAME + ")=?",
                        new String[]{name}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    int rc = cursor.getCount();

                    if (rc == 1) {
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ID));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return id;
        } finally {
            stopRunningCommand();
        }
    }

    int getEventSensorPassed(EventPreferences eventPreferences, int eventType)
    {
        if (eventPreferences._event != null) {
            importExportLock.lock();
            try {
                int sensorPassed = EventPreferences.SENSOR_PASSED_NOT_PASSED;
                try {
                    startRunningCommand();

                    //SQLiteDatabase db = this.getReadableDatabase();
                    SQLiteDatabase db = getMyWritableDatabase();

                    String sensorPassedField = "";
                    switch (eventType) {
                        case ETYPE_BLUETOOTH:
                            sensorPassedField = KEY_E_BLUETOOTH_SENSOR_PASSED;
                            break;
                        case ETYPE_LOCATION:
                            sensorPassedField = KEY_E_LOCATION_SENSOR_PASSED;
                            break;
                        case ETYPE_MOBILE_CELLS:
                            sensorPassedField = KEY_E_MOBILE_CELLS_SENSOR_PASSED;
                            break;
                        case ETYPE_ORIENTATION:
                            sensorPassedField = KEY_E_ORIENTATION_SENSOR_PASSED;
                            break;
                        case ETYPE_WIFI:
                            sensorPassedField = KEY_E_WIFI_SENSOR_PASSED;
                            break;
                        case ETYPE_TIME:
                            sensorPassedField = KEY_E_TIME_SENSOR_PASSED;
                            break;
                        case ETYPE_BATTERY:
                        case ETYPE_BATTERY_WITH_LEVEL:
                            sensorPassedField = KEY_E_BATTERY_SENSOR_PASSED;
                            break;
                        case ETYPE_CALL:
                            sensorPassedField = KEY_E_CALL_SENSOR_PASSED;
                            break;
                        case ETYPE_ACCESSORY:
                            sensorPassedField = KEY_E_ACCESSORY_SENSOR_PASSED;
                            break;
                        case ETYPE_CALENDAR:
                            sensorPassedField = KEY_E_CALENDAR_SENSOR_PASSED;
                            break;
                        case ETYPE_SCREEN:
                            sensorPassedField = KEY_E_SCREEN_SENSOR_PASSED;
                            break;
                        case ETYPE_SMS:
                            sensorPassedField = KEY_E_SMS_SENSOR_PASSED;
                            break;
                        case ETYPE_NOTIFICATION:
                            sensorPassedField = KEY_E_NOTIFICATION_SENSOR_PASSED;
                            break;
                        case ETYPE_APPLICATION:
                            sensorPassedField = KEY_E_APPLICATION_SENSOR_PASSED;
                            break;
                        case ETYPE_NFC:
                            sensorPassedField = KEY_E_NFC_SENSOR_PASSED;
                            break;
                        case ETYPE_RADIO_SWITCH:
                            sensorPassedField = KEY_E_RADIO_SWITCH_SENSOR_PASSED;
                            break;
                        case ETYPE_ALARM_CLOCK:
                            sensorPassedField = KEY_E_ALARM_CLOCK_SENSOR_PASSED;
                            break;
                        case ETYPE_DEVICE_BOOT:
                            sensorPassedField = KEY_E_DEVICE_BOOT_SENSOR_PASSED;
                            break;
                        case ETYPE_SOUND_PROFILE:
                            sensorPassedField = KEY_E_SOUND_PROFILE_SENSOR_PASSED;
                            break;
                        case ETYPE_PERIODIC:
                            sensorPassedField = KEY_E_PERIODIC_SENSOR_PASSED;
                            break;
                        case ETYPE_VOLUMES:
                            sensorPassedField = KEY_E_VOLUMES_SENSOR_PASSED;
                            break;
                        case ETYPE_ACTIVATED_PROFILE:
                            sensorPassedField = KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED;
                            break;
                    }

                    Cursor cursor = db.query(TABLE_EVENTS,
                            new String[]{
                                    sensorPassedField
                            },
                            KEY_E_ID + "=?",
                            new String[]{String.valueOf(eventPreferences._event._id)}, null, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();

                        if (cursor.getCount() > 0) {
                            sensorPassed = cursor.getInt(cursor.getColumnIndexOrThrow(sensorPassedField));
                        }

                        cursor.close();
                    }

                    //db.close();

                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
                return sensorPassed;
            } finally {
                stopRunningCommand();
            }
        }
        else
            return EventPreferences.SENSOR_PASSED_NOT_PASSED;
    }

    void updateEventSensorPassed(Event event, int eventType)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                int sensorPassed = EventPreferences.SENSOR_PASSED_NOT_PASSED;
                String sensorPassedField = "";
                switch (eventType) {
                    case ETYPE_BLUETOOTH:
                        sensorPassed = event._eventPreferencesBluetooth.getSensorPassed();
                        sensorPassedField = KEY_E_BLUETOOTH_SENSOR_PASSED;
                        break;
                    case ETYPE_LOCATION:
                        sensorPassed = event._eventPreferencesLocation.getSensorPassed();
                        sensorPassedField = KEY_E_LOCATION_SENSOR_PASSED;
                        break;
                    case ETYPE_MOBILE_CELLS:
                        sensorPassed = event._eventPreferencesMobileCells.getSensorPassed();
                        sensorPassedField = KEY_E_MOBILE_CELLS_SENSOR_PASSED;
                        break;
                    case ETYPE_ORIENTATION:
                        sensorPassed = event._eventPreferencesOrientation.getSensorPassed();
                        sensorPassedField = KEY_E_ORIENTATION_SENSOR_PASSED;
                        break;
                    case ETYPE_WIFI:
                        sensorPassed = event._eventPreferencesWifi.getSensorPassed();
                        sensorPassedField = KEY_E_WIFI_SENSOR_PASSED;
                        break;
                    case ETYPE_TIME:
                        sensorPassed = event._eventPreferencesTime.getSensorPassed();
                        sensorPassedField = KEY_E_TIME_SENSOR_PASSED;
                        break;
                    case ETYPE_BATTERY:
                    case ETYPE_BATTERY_WITH_LEVEL:
                        sensorPassed = event._eventPreferencesBattery.getSensorPassed();
                        sensorPassedField = KEY_E_BATTERY_SENSOR_PASSED;
                        break;
                    case ETYPE_CALL:
                        sensorPassed = event._eventPreferencesCall.getSensorPassed();
                        sensorPassedField = KEY_E_CALL_SENSOR_PASSED;
                        break;
                    case ETYPE_ACCESSORY:
                        sensorPassed = event._eventPreferencesAccessories.getSensorPassed();
                        sensorPassedField = KEY_E_ACCESSORY_SENSOR_PASSED;
                        break;
                    case ETYPE_CALENDAR:
                        sensorPassed = event._eventPreferencesCalendar.getSensorPassed();
                        sensorPassedField = KEY_E_CALENDAR_SENSOR_PASSED;
                        break;
                    case ETYPE_SCREEN:
                        sensorPassed = event._eventPreferencesScreen.getSensorPassed();
                        sensorPassedField = KEY_E_SCREEN_SENSOR_PASSED;
                        break;
                    case ETYPE_SMS:
                        sensorPassed = event._eventPreferencesSMS.getSensorPassed();
                        sensorPassedField = KEY_E_SMS_SENSOR_PASSED;
                        break;
                    case ETYPE_NOTIFICATION:
                        sensorPassed = event._eventPreferencesNotification.getSensorPassed();
                        sensorPassedField = KEY_E_NOTIFICATION_SENSOR_PASSED;
                        break;
                    case ETYPE_APPLICATION:
                        sensorPassed = event._eventPreferencesApplication.getSensorPassed();
                        sensorPassedField = KEY_E_APPLICATION_SENSOR_PASSED;
                        break;
                    case ETYPE_NFC:
                        sensorPassed = event._eventPreferencesNFC.getSensorPassed();
                        sensorPassedField = KEY_E_NFC_SENSOR_PASSED;
                        break;
                    case ETYPE_RADIO_SWITCH:
                        sensorPassed = event._eventPreferencesRadioSwitch.getSensorPassed();
                        sensorPassedField = KEY_E_RADIO_SWITCH_SENSOR_PASSED;
                        break;
                    case ETYPE_ALARM_CLOCK:
                        sensorPassed = event._eventPreferencesAlarmClock.getSensorPassed();
                        sensorPassedField = KEY_E_ALARM_CLOCK_SENSOR_PASSED;
                        break;
                    case ETYPE_DEVICE_BOOT:
                        sensorPassed = event._eventPreferencesDeviceBoot.getSensorPassed();
                        sensorPassedField = KEY_E_DEVICE_BOOT_SENSOR_PASSED;
                        break;
                    case ETYPE_SOUND_PROFILE:
                        sensorPassed = event._eventPreferencesSoundProfile.getSensorPassed();
                        sensorPassedField = KEY_E_SOUND_PROFILE_SENSOR_PASSED;
                        break;
                    case ETYPE_PERIODIC:
                        sensorPassed = event._eventPreferencesPeriodic.getSensorPassed();
                        sensorPassedField = KEY_E_PERIODIC_SENSOR_PASSED;
                        break;
                    case ETYPE_VOLUMES:
                        sensorPassed = event._eventPreferencesVolumes.getSensorPassed();
                        sensorPassedField = KEY_E_VOLUMES_SENSOR_PASSED;
                        break;
                    case ETYPE_ACTIVATED_PROFILE:
                        sensorPassed = event._eventPreferencesActivatedProfile.getSensorPassed();
                        sensorPassedField = KEY_E_ACTIVATED_PROFILE_SENSOR_PASSED;
                        break;
                }
                ContentValues values = new ContentValues();
                values.put(sensorPassedField, sensorPassed);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateEventSensorPassed", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateAllEventSensorsPassed(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_BLUETOOTH_SENSOR_PASSED, event._eventPreferencesBluetooth.getSensorPassed());
                values.put(KEY_E_LOCATION_SENSOR_PASSED, event._eventPreferencesLocation.getSensorPassed());
                values.put(KEY_E_MOBILE_CELLS_SENSOR_PASSED, event._eventPreferencesMobileCells.getSensorPassed());
                values.put(KEY_E_ORIENTATION_SENSOR_PASSED, event._eventPreferencesOrientation.getSensorPassed());
                values.put(KEY_E_WIFI_SENSOR_PASSED, event._eventPreferencesWifi.getSensorPassed());
                values.put(KEY_E_APPLICATION_SENSOR_PASSED, event._eventPreferencesApplication.getSensorPassed());
                values.put(KEY_E_BATTERY_SENSOR_PASSED, event._eventPreferencesBattery.getSensorPassed());
                values.put(KEY_E_CALENDAR_SENSOR_PASSED, event._eventPreferencesCalendar.getSensorPassed());
                values.put(KEY_E_CALL_SENSOR_PASSED, event._eventPreferencesCall.getSensorPassed());
                values.put(KEY_E_NFC_SENSOR_PASSED, event._eventPreferencesNFC.getSensorPassed());
                values.put(KEY_E_NOTIFICATION_SENSOR_PASSED, event._eventPreferencesNotification.getSensorPassed());
                values.put(KEY_E_ACCESSORY_SENSOR_PASSED, event._eventPreferencesAccessories.getSensorPassed());
                values.put(KEY_E_RADIO_SWITCH_SENSOR_PASSED, event._eventPreferencesRadioSwitch.getSensorPassed());
                values.put(KEY_E_SCREEN_SENSOR_PASSED, event._eventPreferencesScreen.getSensorPassed());
                values.put(KEY_E_SMS_SENSOR_PASSED, event._eventPreferencesSMS.getSensorPassed());
                values.put(KEY_E_TIME_SENSOR_PASSED, event._eventPreferencesTime.getSensorPassed());
                values.put(KEY_E_ALARM_CLOCK_SENSOR_PASSED, event._eventPreferencesAlarmClock.getSensorPassed());
                values.put(KEY_E_DEVICE_BOOT_SENSOR_PASSED, event._eventPreferencesDeviceBoot.getSensorPassed());
                values.put(KEY_E_SOUND_PROFILE_SENSOR_PASSED, event._eventPreferencesSoundProfile.getSensorPassed());
                values.put(KEY_E_PERIODIC_SENSOR_PASSED, event._eventPreferencesPeriodic.getSensorPassed());
                values.put(KEY_E_VOLUMES_SENSOR_PASSED, event._eventPreferencesVolumes.getSensorPassed());

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateEventSensorPassed", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateAllEventsSensorsPassed(int sensorPassed)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_BLUETOOTH_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_LOCATION_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_MOBILE_CELLS_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_ORIENTATION_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_WIFI_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_APPLICATION_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_BATTERY_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_CALENDAR_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_CALL_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_NFC_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_NOTIFICATION_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_ACCESSORY_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_RADIO_SWITCH_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_SCREEN_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_SMS_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_TIME_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_ALARM_CLOCK_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_DEVICE_BOOT_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_SOUND_PROFILE_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_PERIODIC_SENSOR_PASSED, sensorPassed);
                values.put(KEY_E_VOLUMES_SENSOR_PASSED, sensorPassed);

                db.beginTransaction();

                try {
                    // updating rows
                    db.update(TABLE_EVENTS, values, null, null);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.clearAllEventsSensorPassed", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    int getTypeEventsCount(int eventType/*, boolean onlyRunning*/)
    {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                final String countQuery;
                String eventTypeChecked;
                //if (onlyRunning)
                //    eventTypeChecked = KEY_E_STATUS + "=2";  //  only running events
                //else
                    eventTypeChecked = KEY_E_STATUS + "!=0";  //  only not stopped events
                if (eventType != ETYPE_ALL) {
                    eventTypeChecked = eventTypeChecked  + " AND ";
                    if (eventType == ETYPE_TIME)
                        eventTypeChecked = eventTypeChecked + KEY_E_TIME_ENABLED + "=1";
                    else if (eventType == ETYPE_BATTERY)
                        eventTypeChecked = eventTypeChecked + KEY_E_BATTERY_ENABLED + "=1";
                    else if (eventType == ETYPE_BATTERY_WITH_LEVEL)
                        eventTypeChecked = eventTypeChecked + KEY_E_BATTERY_ENABLED + "=1" + " AND " +
                                "((" + KEY_E_BATTERY_LEVEL_LOW + " > 0) OR (" + KEY_E_BATTERY_LEVEL_HIGHT + " < 100))";
                    else if (eventType == ETYPE_CALL)
                        eventTypeChecked = eventTypeChecked + KEY_E_CALL_ENABLED + "=1";
                    else if (eventType == ETYPE_ACCESSORY)
                        eventTypeChecked = eventTypeChecked + KEY_E_ACCESSORY_ENABLED + "=1";
                    else if (eventType == ETYPE_CALENDAR)
                        eventTypeChecked = eventTypeChecked + KEY_E_CALENDAR_ENABLED + "=1";
                    else if (eventType == ETYPE_WIFI_CONNECTED)
                        eventTypeChecked = eventTypeChecked + KEY_E_WIFI_ENABLED + "=1" + " AND " +
                                "(" + KEY_E_WIFI_CONNECTION_TYPE + "=0 OR " + KEY_E_WIFI_CONNECTION_TYPE + "=2)";
                    else if (eventType == ETYPE_WIFI_NEARBY)
                        eventTypeChecked = eventTypeChecked + KEY_E_WIFI_ENABLED + "=1" + " AND " +
                                "(" + KEY_E_WIFI_CONNECTION_TYPE + "=1 OR " + KEY_E_WIFI_CONNECTION_TYPE + "=3)";
                    else if (eventType == ETYPE_SCREEN)
                        eventTypeChecked = eventTypeChecked + KEY_E_SCREEN_ENABLED + "=1";
                    else if (eventType == ETYPE_BLUETOOTH_CONNECTED)
                        eventTypeChecked = eventTypeChecked + KEY_E_BLUETOOTH_ENABLED + "=1" + " AND " +
                                "(" + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=0 OR " + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=2)";
                    else if (eventType == ETYPE_BLUETOOTH_NEARBY)
                        eventTypeChecked = eventTypeChecked + KEY_E_BLUETOOTH_ENABLED + "=1" + " AND " +
                                "(" + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=1 OR " + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=3)";
                    else if (eventType == ETYPE_SMS)
                        eventTypeChecked = eventTypeChecked + KEY_E_SMS_ENABLED + "=1";
                    else if (eventType == ETYPE_NOTIFICATION)
                        eventTypeChecked = eventTypeChecked + KEY_E_NOTIFICATION_ENABLED + "=1";
                    else if (eventType == ETYPE_APPLICATION)
                        eventTypeChecked = eventTypeChecked + KEY_E_APPLICATION_ENABLED + "=1";
                    else if (eventType == ETYPE_LOCATION)
                        eventTypeChecked = eventTypeChecked + KEY_E_LOCATION_ENABLED + "=1";
                    else if (eventType == ETYPE_ORIENTATION)
                        eventTypeChecked = eventTypeChecked + KEY_E_ORIENTATION_ENABLED + "=1";
                    else if (eventType == ETYPE_MOBILE_CELLS)
                        eventTypeChecked = eventTypeChecked + KEY_E_MOBILE_CELLS_ENABLED + "=1";
                    else if (eventType == ETYPE_NFC)
                        eventTypeChecked = eventTypeChecked + KEY_E_NFC_ENABLED + "=1";
                    else if (eventType == ETYPE_RADIO_SWITCH)
                        eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1";
                    else if (eventType == ETYPE_RADIO_SWITCH_WIFI)
                        eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                KEY_E_RADIO_SWITCH_WIFI + "!=0";
                    else if (eventType == ETYPE_RADIO_SWITCH_BLUETOOTH)
                        eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                KEY_E_RADIO_SWITCH_BLUETOOTH + "!=0";
                    else if (eventType == ETYPE_RADIO_SWITCH_MOBILE_DATA)
                        eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                KEY_E_RADIO_SWITCH_MOBILE_DATA + "!=0";
                    else if (eventType == ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS)
                        eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS + "!=0";
                    else if (eventType == ETYPE_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS)
                        eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS + "!=0";
                    else if (eventType == ETYPE_RADIO_SWITCH_GPS)
                        eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                KEY_E_RADIO_SWITCH_GPS + "!=0";
                    else if (eventType == ETYPE_RADIO_SWITCH_NFC)
                        eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                KEY_E_RADIO_SWITCH_NFC + "!=0";
                    else if (eventType == ETYPE_RADIO_SWITCH_AIRPLANE_MODE)
                        eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                KEY_E_RADIO_SWITCH_AIRPLANE_MODE + "!=0";
                    else if (eventType == ETYPE_RADIO_SWITCH_SIM_ON_OFF)
                        eventTypeChecked = eventTypeChecked + KEY_E_RADIO_SWITCH_ENABLED + "=1" + " AND " +
                                KEY_E_RADIO_SWITCH_SIM_ON_OFF + "!=0";
                    else if (eventType == ETYPE_ALARM_CLOCK)
                        eventTypeChecked = eventTypeChecked + KEY_E_ALARM_CLOCK_ENABLED + "=1";
                    else if (eventType == ETYPE_TIME_TWILIGHT)
                        eventTypeChecked = eventTypeChecked + KEY_E_TIME_ENABLED + "=1" + " AND " +
                                KEY_E_TIME_TYPE + "!=0";
                    else if (eventType == ETYPE_DEVICE_BOOT)
                        eventTypeChecked = eventTypeChecked + KEY_E_DEVICE_BOOT_ENABLED + "=1";
                    else if (eventType == ETYPE_SOUND_PROFILE)
                        eventTypeChecked = eventTypeChecked + KEY_E_SOUND_PROFILE_ENABLED + "=1";
                    else if (eventType == ETYPE_PERIODIC)
                        eventTypeChecked = eventTypeChecked + KEY_E_PERIODIC_ENABLED + "=1";
                    else if (eventType == ETYPE_VOLUMES)
                        eventTypeChecked = eventTypeChecked + KEY_E_VOLUMES_ENABLED + "=1";
                    else if (eventType == ETYPE_ACTIVATED_PROFILE)
                        eventTypeChecked = eventTypeChecked + KEY_E_ACTIVATED_PROFILE_ENABLED + "=1";
                }

                countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS +
                        " WHERE " + eventTypeChecked;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    int getNotStoppedEventsCount() {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                final String countQuery;
                countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS +
                                " WHERE " + KEY_E_STATUS + "!=0";

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    void updateEventCalendarTimes(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_CALENDAR_EVENT_START_TIME, event._eventPreferencesCalendar._startTime);
                values.put(KEY_E_CALENDAR_EVENT_END_TIME, event._eventPreferencesCalendar._endTime);
                values.put(KEY_E_CALENDAR_EVENT_FOUND, event._eventPreferencesCalendar._eventFound ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateEventCalendarTimes", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void setEventCalendarTimes(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_CALENDAR_EVENT_START_TIME,
                                KEY_E_CALENDAR_EVENT_END_TIME,
                                KEY_E_CALENDAR_EVENT_FOUND
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesCalendar._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_EVENT_START_TIME));
                        event._eventPreferencesCalendar._endTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_EVENT_END_TIME));
                        event._eventPreferencesCalendar._eventFound = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALENDAR_EVENT_FOUND)) == 1);
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateEventCalendarTodayExists(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_CALENDAR_EVENT_TODAY_EXISTS, event._eventPreferencesCalendar._eventTodayExists ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateEventCalendarTimes", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    boolean getEventInDelayStart(Event event)
    {
        importExportLock.lock();
        try {
            int eventInDelay = 0;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_IS_IN_DELAY_START
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        eventInDelay = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_IS_IN_DELAY_START));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return (eventInDelay == 1);
        } finally {
            stopRunningCommand();
        }
    }

    void updateEventInDelayStart(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_IS_IN_DELAY_START, event._isInDelayStart ? 1 : 0);
                values.put(KEY_E_START_STATUS_TIME, event._startStatusTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateEventInDelayStart", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void resetAllEventsInDelayStart()
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_IS_IN_DELAY_START, 0);

                db.beginTransaction();

                try {
                    // updating rows
                    db.update(TABLE_EVENTS, values, null, null);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.resetAllEventsInDelayStart", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    boolean getEventInDelayEnd(Event event)
    {
        importExportLock.lock();
        try {
            int eventInDelay = 0;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_IS_IN_DELAY_END
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        eventInDelay = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_IS_IN_DELAY_END));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return (eventInDelay == 1);
        } finally {
            stopRunningCommand();
        }
    }

    void updateEventInDelayEnd(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_IS_IN_DELAY_END, event._isInDelayEnd ? 1 : 0);
                values.put(KEY_E_PAUSE_STATUS_TIME, event._pauseStatusTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateEventInDelayEnd", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateSMSStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_SMS_START_TIME, event._eventPreferencesSMS._startTime);
                values.put(KEY_E_SMS_FROM_SIM_SLOT, event._eventPreferencesSMS._fromSIMSlot);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateSMSStartTimes", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getSMSStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_SMS_START_TIME,
                                KEY_E_SMS_FROM_SIM_SLOT
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesSMS._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_SMS_START_TIME));
                        event._eventPreferencesSMS._fromSIMSlot = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_SMS_FROM_SIM_SLOT));
                        //if ((event != null) && (event._name != null) && (event._name.equals("SMS event")))
                        //    PPApplication.logE("[SMS sensor] DatabaseHandler.getSMSStartTime", "startTime="+event._eventPreferencesSMS._startTime);
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    /*
    void updateNotificationStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_NOTIFICATION_START_TIME, event._eventPreferencesNotification._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    Log.e("DatabaseHandler.updateNotificationStartTimes", Log.getStackTraceString(e));
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getNotificationStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_NOTIFICATION_START_TIME
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesNotification._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_START_TIME));
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }
    */

    /*
    int getBluetoothDevicesTypeCount(int devicesType, int forceScan)
    {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                if (forceScan != WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                    final String countQuery;
                    String devicesTypeChecked;
                    devicesTypeChecked = KEY_E_STATUS + "!=0" + " AND ";  //  only not stopped events
                    devicesTypeChecked = devicesTypeChecked + KEY_E_BLUETOOTH_ENABLED + "=1" + " AND ";
                    devicesTypeChecked = devicesTypeChecked + "(" + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=1 OR " + KEY_E_BLUETOOTH_CONNECTION_TYPE + "=3) AND ";
                    if (devicesType == EventPreferencesBluetooth.DTYPE_CLASSIC)
                        devicesTypeChecked = devicesTypeChecked + KEY_E_BLUETOOTH_DEVICES_TYPE + "=0";
                    else if (devicesType == EventPreferencesBluetooth.DTYPE_LE)
                        devicesTypeChecked = devicesTypeChecked + KEY_E_BLUETOOTH_DEVICES_TYPE + "=1";

                    countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS +
                            " WHERE " + devicesTypeChecked;

                    //SQLiteDatabase db = this.getReadableDatabase();
                    SQLiteDatabase db = getMyWritableDatabase();

                    Cursor cursor = db.rawQuery(countQuery, null);

                    if (cursor != null) {
                        cursor.moveToFirst();
                        r = cursor.getInt(0);
                        cursor.close();
                    }

                    //db.close();

                } else
                    r = 999;
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }
    */

    void updateNFCStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_NFC_START_TIME, event._eventPreferencesNFC._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateNFCStartTimes", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getNFCStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_NFC_START_TIME
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesNFC._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_NFC_START_TIME));
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    /*
    int getBatteryEventWithLevelCount()
    {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                final String countQuery;
                String eventChecked = KEY_E_STATUS + "!=0" + " AND ";  //  only not stopped events
                eventChecked = eventChecked + KEY_E_BATTERY_ENABLED + "=1" + " AND ";
                eventChecked = eventChecked + "(" + KEY_E_BATTERY_LEVEL_LOW + ">0" + " OR ";
                eventChecked = eventChecked + KEY_E_BATTERY_LEVEL_HIGHT + "<100" + ")";

                countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS +
                        " WHERE " + eventChecked;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }
    */

    void updateCallStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_CALL_START_TIME, event._eventPreferencesCall._startTime);
                values.put(KEY_E_CALL_FROM_SIM_SLOT, event._eventPreferencesCall._fromSIMSlot);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateCallStartTimes", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getCallStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_CALL_START_TIME,
                                KEY_E_CALL_FROM_SIM_SLOT
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesCall._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_CALL_START_TIME));
                        event._eventPreferencesCall._fromSIMSlot = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_CALL_FROM_SIM_SLOT));
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateAlarmClockStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_ALARM_CLOCK_START_TIME, event._eventPreferencesAlarmClock._startTime);
                values.put(KEY_E_ALARM_CLOCK_PACKAGE_NAME, event._eventPreferencesAlarmClock._alarmPackageName);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateAlarmClockStartTime", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getAlarmClockStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_ALARM_CLOCK_START_TIME,
                                KEY_E_ALARM_CLOCK_PACKAGE_NAME
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesAlarmClock._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ALARM_CLOCK_START_TIME));
                        event._eventPreferencesAlarmClock._alarmPackageName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ALARM_CLOCK_PACKAGE_NAME));
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateDeviceBootStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_DEVICE_BOOT_START_TIME, event._eventPreferencesDeviceBoot._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateDeviceBootStartTime", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getDeviceBootStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_DEVICE_BOOT_START_TIME
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        event._eventPreferencesDeviceBoot._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_DEVICE_BOOT_START_TIME));
                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updatePeriodicCounter(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_PERIODIC_COUNTER, event._eventPreferencesPeriodic._counter);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updatePeriodicStartTime", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updatePeriodicStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_PERIODIC_START_TIME, event._eventPreferencesPeriodic._startTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updatePeriodicStartTime", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void getPeriodicStartTime(Event event)
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{
                                KEY_E_PERIODIC_COUNTER,
                                KEY_E_PERIODIC_MULTIPLY_INTERVAL,
                                KEY_E_PERIODIC_START_TIME
                        },
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(event._id)}, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        int multiplyInterval = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_PERIODIC_MULTIPLY_INTERVAL));

                        event._eventPreferencesPeriodic._counter = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_PERIODIC_COUNTER));
                        if (event._eventPreferencesPeriodic._counter >=
                                ApplicationPreferences.applicationEventPeriodicScanningScanInterval * multiplyInterval)
                            event._eventPreferencesPeriodic._startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_PERIODIC_START_TIME));
                        else
                            event._eventPreferencesPeriodic._startTime = 0;

                    }

                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateEventForceRun(Event event) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();
                try {
                    ContentValues values = new ContentValues();
                    values.put(KEY_E_FORCE_RUN, event._ignoreManualActivation);
                    if (event._ignoreManualActivation) {
                        values.put(KEY_E_NO_PAUSE_BY_MANUAL_ACTIVATION, event._noPauseByManualActivation);
                    }

                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    int getOrientationWithLightSensorEventsCount()
    {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                final String countQuery;
                String eventTypeChecked;
                eventTypeChecked = KEY_E_STATUS + "!=0 AND ";  //  only not stopped events
                eventTypeChecked = eventTypeChecked + KEY_E_ORIENTATION_ENABLED + "=1 AND " +
                                            KEY_E_ORIENTATION_CHECK_LIGHT + "=1";

                countQuery = "SELECT  count(*) FROM " + TABLE_EVENTS +
                        " WHERE " + eventTypeChecked;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    void updateActivatedProfileSensorRunningParameter(Event event) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();
                try {
                    ContentValues values = new ContentValues();
                    values.put(KEY_E_ACTIVATED_PROFILE_RUNNING, event._eventPreferencesActivatedProfile._running);

                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[]{String.valueOf(event._id)});

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

// EVENT TIMELINE ------------------------------------------------------------------

    // Adding time line
    void addEventTimeline(EventTimeline eventTimeline) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_ET_FK_EVENT, eventTimeline._fkEvent); // Event id
                //values.put(KEY_ET_FK_PROFILE_RETURN, eventTimeline._fkProfileEndActivated); // Profile id returned on pause/stop event
                values.put(KEY_ET_EORDER, getMaxEOrderET() + 1); // event running order

                db.beginTransaction();

                try {
                    // Inserting Row
                    eventTimeline._id = db.insert(TABLE_EVENT_TIMELINE, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting max(eorder)
    private int getMaxEOrderET() {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                String countQuery = "SELECT MAX(" + KEY_ET_EORDER + ") FROM " + TABLE_EVENT_TIMELINE;
                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        r = cursor.getInt(0);
                    }
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    // Getting all event timeline
    List<EventTimeline> getAllEventTimelines() {
        importExportLock.lock();
        try {
            List<EventTimeline> eventTimelineList = new ArrayList<>();
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_ET_ID + "," +
                        KEY_ET_FK_EVENT + "," +
                        KEY_ET_FK_PROFILE_RETURN + "," +
                        KEY_ET_EORDER +
                        " FROM " + TABLE_EVENT_TIMELINE +
                        " ORDER BY " + KEY_ET_EORDER;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        EventTimeline eventTimeline = new EventTimeline();

                        eventTimeline._id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ET_ID));
                        eventTimeline._fkEvent = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ET_FK_EVENT));
                        //eventTimeline._fkProfileEndActivated = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ET_FK_PROFILE_RETURN));
                        eventTimeline._eorder = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ET_EORDER));

                        // Adding event timeline to list
                        eventTimelineList.add(eventTimeline);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return eventTimelineList;
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting event timeline
    void deleteEventTimeline(EventTimeline eventTimeline) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();
                db.delete(TABLE_EVENT_TIMELINE, KEY_ET_ID + " = ?",
                        new String[]{String.valueOf(eventTimeline._id)});
                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting all events from timeline
    void deleteAllEventTimelines(/*boolean updateEventStatus*/) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_E_STATUS, Event.ESTATUS_PAUSE);

                db.beginTransaction();

                try {

                    db.delete(TABLE_EVENT_TIMELINE, null, null);

                    //if (updateEventStatus) {
                    db.update(TABLE_EVENTS, values, KEY_E_STATUS + " = ?",
                            new String[]{String.valueOf(Event.ESTATUS_RUNNING)});
                    //}

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.deleteAllEventTimelines", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    /*
    // Getting max(eorder)
    int getCountEventsInTimeline() {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                String countQuery = "SELECT COUNT(" + KEY_ET_ID + ") FROM " + TABLE_EVENT_TIMELINE;
                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor.getCount() > 0) {
                    if (cursor.moveToFirst()) {
                        r = cursor.getInt(0);
                    }
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }
    */

    String getLastStartedEventName() {
        importExportLock.lock();
        try {
            String eventName = "?";
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                String query =
                        "SELECT "+KEY_ET_FK_EVENT+" FROM "+TABLE_EVENT_TIMELINE+" ORDER BY "+KEY_ET_EORDER+" DESC LIMIT 1";
                Cursor cursor1 = db.rawQuery(query, null);

                long lastEvent = 0;

                if (cursor1.getCount() > 0) {
                    if (cursor1.moveToFirst()) {
                        lastEvent = cursor1.getLong(0);
                    }

                    if (lastEvent > 0) {
                        query = "SELECT "+KEY_E_NAME+","+KEY_E_FORCE_RUN+
                                " FROM "+TABLE_EVENTS+
                                " WHERE "+KEY_E_ID+"="+lastEvent;
                        Cursor cursor2 = db.rawQuery(query, null);

                        if (cursor2.getCount() > 0) {
                            if (cursor2.moveToFirst()) {
                                String _eventName = cursor2.getString(0);
                                boolean _forceRun = cursor2.getInt(1) == 1;
                                //if ((!ApplicationPreferences.prefEventsBlocked) || _forceRun)
                                //    eventName = _eventName;
                                if ((!Event.getEventsBlocked(context)) || _forceRun)
                                    eventName = _eventName;
                            }
                        }
                        cursor2.close();
                    }
                }
                cursor1.close();
                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return eventName;
        } finally {
            stopRunningCommand();
        }
    }

// ACTIVITY LOG -------------------------------------------------------------------

    // Adding activity log
    void addActivityLog(int deleteOldActivityLogs,
                        int logType, String eventName, String profileName, String profileEventsCount) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_AL_LOG_TYPE, logType);
                values.put(KEY_AL_EVENT_NAME, eventName);
                values.put(KEY_AL_PROFILE_NAME, profileName);
                values.put(KEY_AL_PROFILE_EVENT_COUNT, profileEventsCount);

                db.beginTransaction();

                try {
                    if (deleteOldActivityLogs > 0) {
                        // delete older than 7 days old records
                        db.delete(TABLE_ACTIVITY_LOG, KEY_AL_LOG_DATE_TIME +
                                " < date('now','-" + deleteOldActivityLogs + " days')", null);
                    }

                    // Inserting Row
                    db.insert(TABLE_ACTIVITY_LOG, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void clearActivityLog() {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                //db.beginTransaction();

                try {
                    db.delete(TABLE_ACTIVITY_LOG, null, null);

                    // db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                    //} finally {
                    //db.endTransaction();
                    PPApplication.recordException(e);
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    Cursor getActivityLogCursor() {
        importExportLock.lock();
        try {
            Cursor cursor = null;
            try {
                startRunningCommand();

                final String selectQuery = "SELECT " + KEY_AL_ID + "," +
                        KEY_AL_LOG_DATE_TIME + "," +
                        KEY_AL_LOG_TYPE + "," +
                        KEY_AL_EVENT_NAME + "," +
                        KEY_AL_PROFILE_NAME + "," +
                        //KEY_AL_PROFILE_ICON + "," +
                        //KEY_AL_DURATION_DELAY + "," +
                        KEY_AL_PROFILE_EVENT_COUNT +
                        " FROM " + TABLE_ACTIVITY_LOG +
                        " ORDER BY " + KEY_AL_ID + " DESC";

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                cursor = db.rawQuery(selectQuery, null);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return cursor;
        } finally {
            stopRunningCommand();
        }
    }

// GEOFENCES ----------------------------------------------------------------------

    // Adding new geofence
    void addGeofence(Geofence geofence) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_G_NAME, geofence._name); // geofence Name
                values.put(KEY_G_LATITUDE, geofence._latitude);
                values.put(KEY_G_LONGITUDE, geofence._longitude);
                values.put(KEY_G_RADIUS, geofence._radius);
                values.put(KEY_G_CHECKED, 0);
                values.put(KEY_G_TRANSITION, 0);

                db.beginTransaction();

                try {
                    // Inserting Row
                    geofence._id = db.insert(TABLE_GEOFENCES, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting single geofence
    Geofence getGeofence(long geofenceId) {
        importExportLock.lock();
        try {
            Geofence geofence = null;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_GEOFENCES,
                        new String[]{KEY_G_ID,
                                KEY_G_NAME,
                                KEY_G_LATITUDE,
                                KEY_G_LONGITUDE,
                                KEY_G_RADIUS
                        },
                        KEY_G_ID + "=?",
                        new String[]{String.valueOf(geofenceId)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        geofence = new Geofence();
                        geofence._id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_G_ID));
                        geofence._name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_G_NAME));
                        geofence._latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_G_LATITUDE));
                        geofence._longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_G_LONGITUDE));
                        geofence._radius = cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_G_RADIUS));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return geofence;
        } finally {
            stopRunningCommand();
        }
    }

    // Getting All geofences
    List<Geofence> getAllGeofences() {
        importExportLock.lock();
        try {
            List<Geofence> geofenceList = new ArrayList<>();
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_G_ID + "," +
                        KEY_G_NAME + "," +
                        KEY_G_LATITUDE + "," +
                        KEY_G_LONGITUDE + "," +
                        KEY_G_RADIUS + "," +
                        KEY_G_TRANSITION +
                        " FROM " + TABLE_GEOFENCES +
                        " ORDER BY " + KEY_G_ID;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Geofence geofence = new Geofence();
                        geofence._id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_G_ID));
                        geofence._name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_G_NAME));
                        geofence._latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_G_LATITUDE));
                        geofence._longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_G_LONGITUDE));
                        geofence._radius = cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_G_RADIUS));
                        geofence._transition = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_G_TRANSITION));
                        geofenceList.add(geofence);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return geofenceList;
        } finally {
            stopRunningCommand();
        }
    }

    // Updating single geofence
    void updateGeofence(Geofence geofence) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_G_NAME, geofence._name);
                values.put(KEY_G_LATITUDE, geofence._latitude);
                values.put(KEY_G_LONGITUDE, geofence._longitude);
                values.put(KEY_G_RADIUS, geofence._radius);
                values.put(KEY_G_CHECKED, 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_GEOFENCES, values, KEY_G_ID + " = ?",
                            new String[]{String.valueOf(geofence._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateEvent", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateGeofenceTransition(long geofenceId, int geofenceTransition) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                //db.beginTransaction();

                try {
                    ContentValues values = new ContentValues();
                    values.put(KEY_G_TRANSITION, geofenceTransition);
                    db.update(TABLE_GEOFENCES, values, KEY_G_ID + " = ?", new String[]{String.valueOf(geofenceId)});

                    //db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateGeofenceTransition", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                    //} finally {
                    //db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    /*
    void updateAllGeofenceTransitions(List<Geofence> geofences) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {

                    for (Geofence geofence : geofences) {
                        ContentValues values = new ContentValues();
                        values.put(KEY_G_TRANSITION, geofence._transition);
                        db.update(TABLE_GEOFENCES, values, KEY_G_ID + " = ?", new String[]{String.valueOf(geofence._id)});
                    }

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateGeofenceTransition", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }
    */

    void clearAllGeofenceTransitions() {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                //db.beginTransaction();

                try {
                    ContentValues values = new ContentValues();
                    values.put(KEY_G_TRANSITION, 0);
                    db.update(TABLE_GEOFENCES, values, null, null);

                    //db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.clearAllGeofenceTransitions", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                    //} finally {
                    //db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting single geofence
    void deleteGeofence(long geofenceId) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                final String selectQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_LOCATION_GEOFENCES +
                        " FROM " + TABLE_EVENTS;

                Cursor cursor = db.rawQuery(selectQuery, null);

                //noinspection TryFinallyCanBeTryWithResources
                try {

                    // delete geofence
                    db.delete(TABLE_GEOFENCES, KEY_G_ID + " = ?",
                            new String[]{String.valueOf(geofenceId)});

                    // looping through all rows and adding to list
                    if (cursor.moveToFirst()) {
                        do {
                            String geofences = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_LOCATION_GEOFENCES));
                            String[] splits = geofences.split("\\|");
                            boolean found = false;
                            geofences = "";
                            for (String geofence : splits) {
                                if (!geofence.isEmpty()) {
                                    if (!geofence.equals(Long.toString(geofenceId))) {
                                        if (!geofences.isEmpty())
                                            //noinspection StringConcatenationInLoop
                                            geofences = geofences + "|";
                                        //noinspection StringConcatenationInLoop
                                        geofences = geofences + geofence;
                                    } else
                                        found = true;
                                }
                            }
                            if (found) {
                                // unlink geofence from events
                                ContentValues values = new ContentValues();
                                values.put(KEY_E_LOCATION_GEOFENCES, geofences);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?", new String[]{String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_ID)))});
                            }
                        } while (cursor.moveToNext());
                    }

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.deleteGeofence", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void checkGeofence(String geofences, int check) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();

                db.beginTransaction();

                try {
                    if (!geofences.isEmpty()) {
                        // check geofences
                        String[] splits = geofences.split("\\|");
                        for (String geofence : splits) {
                            if (!geofence.isEmpty()) {
                                int _check = check;
                                if (check == 2) {
                                    final String selectQuery = "SELECT " + KEY_G_CHECKED +
                                            " FROM " + TABLE_GEOFENCES +
                                            " WHERE " + KEY_G_ID + "=" + geofence;
                                    Cursor cursor = db.rawQuery(selectQuery, null);
                                    if (cursor != null) {
                                        if (cursor.moveToFirst())
                                            _check = (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_G_CHECKED)) == 0) ? 1 : 0;
                                        cursor.close();
                                    }
                                }
                                if (_check != 2) {
                                    values.clear();
                                    values.put(KEY_G_CHECKED, _check);
                                    db.update(TABLE_GEOFENCES, values, KEY_G_ID + " = ?", new String[]{geofence});
                                }
                            }
                        }
                    } else {
                        // uncheck geofences
                        values.clear();
                        values.put(KEY_G_CHECKED, 0);
                        db.update(TABLE_GEOFENCES, values, null, null);
                    }

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.checkGeofence", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    Cursor getGeofencesCursor() {
        importExportLock.lock();
        try {
            Cursor cursor = null;
            try {
                startRunningCommand();

                final String selectQuery = "SELECT " + KEY_G_ID + "," +
                        KEY_G_LATITUDE + "," +
                        KEY_G_LONGITUDE + "," +
                        KEY_G_RADIUS + "," +
                        KEY_G_NAME + "," +
                        KEY_G_CHECKED +
                        " FROM " + TABLE_GEOFENCES +
                        " ORDER BY " + /*KEY_G_CHECKED + " DESC," +*/ KEY_G_NAME + " ASC";

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                cursor = db.rawQuery(selectQuery, null);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return cursor;
        } finally {
            stopRunningCommand();
        }
    }

    String getGeofenceName(long geofenceId) {
        importExportLock.lock();
        try {
            String r = "";
            try {
                startRunningCommand();

                final String countQuery = "SELECT " + KEY_G_NAME +
                        " FROM " + TABLE_GEOFENCES +
                        " WHERE " + KEY_G_ID + "=" + geofenceId;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    if (cursor.moveToFirst())
                        r = cursor.getString(cursor.getColumnIndexOrThrow(KEY_G_NAME));
                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    String getCheckedGeofences() {
        importExportLock.lock();
        try {
            String value = "";
            try {
                startRunningCommand();

                final String countQuery = "SELECT " + KEY_G_ID + ","
                        + KEY_G_CHECKED +
                        " FROM " + TABLE_GEOFENCES;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            if (cursor.getInt(cursor.getColumnIndexOrThrow(KEY_G_CHECKED)) == 1) {
                                if (!value.isEmpty())
                                    //noinspection StringConcatenationInLoop
                                    value = value + "|";
                                //noinspection StringConcatenationInLoop
                                value = value + cursor.getLong(cursor.getColumnIndexOrThrow(KEY_G_ID));
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return value;
        } finally {
            stopRunningCommand();
        }
    }

    int getGeofenceCount() {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                String countQuery = "SELECT  count(*) FROM " + TABLE_GEOFENCES;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    boolean isGeofenceUsed(long geofenceId/*, boolean onlyEnabledEvents*/) {
        importExportLock.lock();
        try {
            boolean found = false;
            try {
                startRunningCommand();

                String selectQuery = "SELECT " + KEY_E_LOCATION_GEOFENCES +
                        " FROM " + TABLE_EVENTS +
                        " WHERE " + KEY_E_LOCATION_ENABLED + "=1";

                /*
                if (onlyEnabledEvents)
                    selectQuery = selectQuery + " AND " + KEY_E_STATUS + " IN (" +
                            String.valueOf(Event.ESTATUS_PAUSE) + "," +
                            String.valueOf(Event.ESTATUS_RUNNING) + ")";
                */

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        String geofences = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_LOCATION_GEOFENCES));
                        String[] splits = geofences.split("\\|");
                        for (String geofence : splits) {
                            if (!geofence.isEmpty()) {
                                if (geofence.equals(Long.toString(geofenceId))) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (found)
                            break;
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return found;
        } finally {
            stopRunningCommand();
        }
    }

    int getGeofenceTransition(long geofenceId) {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                final String countQuery = "SELECT " + KEY_G_TRANSITION +
                        " FROM " + TABLE_GEOFENCES +
                        " WHERE " + KEY_G_ID + "=" + geofenceId;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    if (cursor.moveToFirst())
                        r = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_G_TRANSITION));
                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

// SHORTCUTS ----------------------------------------------------------------------

    // Adding new shortcut
    void addShortcut(Shortcut shortcut) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_S_INTENT, shortcut._intent);
                values.put(KEY_S_NAME, shortcut._name);

                db.beginTransaction();

                try {
                    // Inserting Row
                    shortcut._id = db.insert(TABLE_SHORTCUTS, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting single shortcut
    Shortcut getShortcut(long shortcutId) {
        importExportLock.lock();
        try {
            Shortcut shortcut = null;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_SHORTCUTS,
                        new String[]{KEY_S_ID,
                                KEY_S_INTENT,
                                KEY_S_NAME
                        },
                        KEY_S_ID + "=?",
                        new String[]{String.valueOf(shortcutId)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        shortcut = new Shortcut();
                        shortcut._id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_S_ID));
                        shortcut._intent = cursor.getString(cursor.getColumnIndexOrThrow(KEY_S_INTENT));
                        shortcut._name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_S_NAME));
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return shortcut;
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting single shortcut
    void deleteShortcut(long shortcutId) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {

                    // delete geofence
                    db.delete(TABLE_SHORTCUTS, KEY_S_ID + " = ?",
                            new String[]{String.valueOf(shortcutId)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.deleteShortcut", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

// MOBILE_CELLS ----------------------------------------------------------------------

    // Adding new mobile cell
    private void addMobileCell(MobileCell mobileCell) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_MC_CELL_ID, mobileCell._cellId);
                values.put(KEY_MC_NAME, mobileCell._name);
                values.put(KEY_MC_NEW, mobileCell._new ? 1 : 0);
                values.put(KEY_MC_LAST_CONNECTED_TIME, mobileCell._lastConnectedTime);
                values.put(KEY_MC_LAST_RUNNING_EVENTS, mobileCell._lastRunningEvents);
                values.put(KEY_MC_LAST_PAUSED_EVENTS, mobileCell._lastPausedEvents);
                values.put(KEY_MC_DO_NOT_DETECT, mobileCell._doNotDetect ? 1 : 0);

                db.beginTransaction();

                try {
                    // Inserting Row
                    mobileCell._id = db.insert(TABLE_MOBILE_CELLS, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Updating single mobile cell
    private void updateMobileCell(MobileCell mobileCell) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_MC_CELL_ID, mobileCell._cellId);
                values.put(KEY_MC_NAME, mobileCell._name);
                values.put(KEY_MC_NEW, mobileCell._new ? 1 : 0);
                values.put(KEY_MC_LAST_CONNECTED_TIME, mobileCell._lastConnectedTime);
                values.put(KEY_MC_LAST_RUNNING_EVENTS, mobileCell._lastRunningEvents);
                values.put(KEY_MC_LAST_PAUSED_EVENTS, mobileCell._lastPausedEvents);
                values.put(KEY_MC_DO_NOT_DETECT, mobileCell._doNotDetect ? 1 : 0);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_MOBILE_CELLS, values, KEY_MC_ID + " = ?",
                            new String[]{String.valueOf(mobileCell._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateMobileCell", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // add mobile cells to list
    void addMobileCellsToList(List<MobileCellsData> cellsList, int onlyCellId) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                // Select All Query
                String selectQuery = "SELECT " + KEY_MC_CELL_ID + "," +
                        KEY_MC_NAME + "," +
                        KEY_MC_NEW + "," +
                        KEY_MC_LAST_CONNECTED_TIME + "," +
                        KEY_MC_LAST_RUNNING_EVENTS + "," +
                        KEY_MC_LAST_PAUSED_EVENTS + "," +
                        KEY_MC_DO_NOT_DETECT +
                        " FROM " + TABLE_MOBILE_CELLS;

                if (onlyCellId != 0) {
                    selectQuery = selectQuery +
                            " WHERE " + KEY_MC_CELL_ID + "=" + onlyCellId;
                }

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        int cellId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MC_CELL_ID));
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MC_NAME));
                        boolean _new = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MC_NEW)) == 1;
                        long lastConnectedTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_MC_LAST_CONNECTED_TIME));
                        String lastRunningEvents = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MC_LAST_RUNNING_EVENTS));
                        String lastPausedEvents = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MC_LAST_PAUSED_EVENTS));
                        boolean doNotDetect = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MC_DO_NOT_DETECT)) == 1;
                        //Log.d("DatabaseHandler.addMobileCellsToList", "cellId="+cellId + " new="+_new);
                        boolean found = false;
                        for (MobileCellsData cell : cellsList) {
                            if (cell.cellId == cellId) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            MobileCellsData cell = new MobileCellsData(cellId, name, false, _new, lastConnectedTime,
                                    lastRunningEvents, lastPausedEvents, doNotDetect);
                            cellsList.add(cell);
                        }
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void saveMobileCellsList(List<MobileCellsData> cellsList, boolean _new, boolean renameExistingCell) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_MC_ID + "," +
                        KEY_MC_CELL_ID + "," +
                        KEY_MC_NAME + "," +
                        KEY_MC_LAST_CONNECTED_TIME + "," +
                        KEY_MC_LAST_RUNNING_EVENTS + "," +
                        KEY_MC_LAST_PAUSED_EVENTS + "," +
                        KEY_MC_DO_NOT_DETECT +
                        " FROM " + TABLE_MOBILE_CELLS;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                for (MobileCellsData cell : cellsList) {
                    boolean found = false;
                    long foundedDbId = 0;
                    String foundedCellName = "";
                    long foundedLastConnectedTime = 0;
                    //String foundedLastRunningEvents = "";
                    //String foundedLastPausedEvents = "";
                    //boolean doNotDetect = false;
                    if (cursor.moveToFirst()) {
                        do {
                            String dbCellId = Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MC_CELL_ID)));
                            if (dbCellId.equals(Integer.toString(cell.cellId))) {
                                foundedDbId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_MC_ID));
                                foundedCellName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MC_NAME));
                                foundedLastConnectedTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_MC_LAST_CONNECTED_TIME));
                                //foundedLastRunningEvents = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MC_LAST_RUNNING_EVENTS));
                                //foundedLastPausedEvents = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MC_LAST_PAUSED_EVENTS));
                                //doNotDetect = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MC_DO_NOT_DETECT)) == 1;
                                found = true;
                                break;
                            }
                        } while (cursor.moveToNext());
                    }
                    MobileCell mobileCell = new MobileCell();
                    if (!found) {
                        //Log.d("DatabaseHandler.saveMobileCellsList", "!found");
                        mobileCell._cellId = cell.cellId;
                        mobileCell._name = cell.name;
                        mobileCell._new = true;
                        mobileCell._lastConnectedTime = cell.lastConnectedTime;
                        mobileCell._lastRunningEvents = cell.lastRunningEvents;
                        mobileCell._lastPausedEvents = cell.lastPausedEvents;
                        mobileCell._doNotDetect = cell.doNotDetect;
                        addMobileCell(mobileCell);
                    } else {
                        //Log.d("DatabaseHandler.saveMobileCellsList", "found="+foundedDbId+" cell.new="+cell._new+" new="+_new);
                        mobileCell._id = foundedDbId;
                        mobileCell._cellId = cell.cellId;
                        mobileCell._name = cell.name;
                        if (!renameExistingCell && !foundedCellName.isEmpty())
                            mobileCell._name = foundedCellName;
                        mobileCell._new = _new && cell._new;
                        if (cell.connected)
                            mobileCell._lastConnectedTime = cell.lastConnectedTime;
                        else
                            mobileCell._lastConnectedTime = foundedLastConnectedTime;
                        mobileCell._lastRunningEvents = cell.lastRunningEvents;
                        mobileCell._lastPausedEvents = cell.lastPausedEvents;
                        mobileCell._doNotDetect = cell.doNotDetect;
                        updateMobileCell(mobileCell);
                    }
                }

                cursor.close();
                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void renameMobileCellsList(List<MobileCellsData> cellsList, String name, boolean _new, String value) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_MC_ID + "," +
                        KEY_MC_CELL_ID +
                        " FROM " + TABLE_MOBILE_CELLS;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                for (MobileCellsData cell : cellsList) {
                    boolean found = false;
                    long foundedDbId = 0;
                    if (cursor.moveToFirst()) {
                        do {
                            String dbCellId = Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MC_CELL_ID)));
                            if (dbCellId.equals(Integer.toString(cell.cellId))) {
                                foundedDbId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_MC_ID));
                                found = true;
                                break;
                            }
                        } while (cursor.moveToNext());
                    }
                    if (found) {
                        if (_new) {
                            // change news
                            if (cell._new) {
                                cell.name = name;
                                MobileCell mobileCell = new MobileCell();
                                mobileCell._id = foundedDbId;
                                mobileCell._cellId = cell.cellId;
                                mobileCell._name = cell.name;
                                mobileCell._new = true;
                                mobileCell._lastConnectedTime = cell.lastConnectedTime;
                                mobileCell._lastRunningEvents = cell.lastRunningEvents;
                                mobileCell._lastPausedEvents = cell.lastPausedEvents;
                                mobileCell._doNotDetect = cell.doNotDetect;
                                updateMobileCell(mobileCell);
                            }
                        } else {
                            if (value != null) {
                                // change selected
                                String[] splits = value.split("\\|");
                                for (String valueCell : splits) {
                                    if (valueCell.equals(Integer.toString(cell.cellId))) {
                                        cell.name = name;
                                        MobileCell mobileCell = new MobileCell();
                                        mobileCell._id = foundedDbId;
                                        mobileCell._cellId = cell.cellId;
                                        mobileCell._name = cell.name;
                                        mobileCell._new = cell._new;
                                        mobileCell._lastConnectedTime = cell.lastConnectedTime;
                                        mobileCell._lastRunningEvents = cell.lastRunningEvents;
                                        mobileCell._lastPausedEvents = cell.lastPausedEvents;
                                        mobileCell._doNotDetect = cell.doNotDetect;
                                        updateMobileCell(mobileCell);
                                    }
                                }
                            }
                            else {
                                // change all
                                cell.name = name;
                                MobileCell mobileCell = new MobileCell();
                                mobileCell._id = foundedDbId;
                                mobileCell._cellId = cell.cellId;
                                mobileCell._name = cell.name;
                                mobileCell._new = cell._new;
                                mobileCell._lastConnectedTime = cell.lastConnectedTime;
                                mobileCell._lastRunningEvents = cell.lastRunningEvents;
                                mobileCell._lastPausedEvents = cell.lastPausedEvents;
                                mobileCell._doNotDetect = cell.doNotDetect;
                                updateMobileCell(mobileCell);
                            }
                        }
                    }
                }

                cursor.close();
                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void deleteMobileCell(int mobileCell) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {
                    // delete geofence
                    db.delete(TABLE_MOBILE_CELLS, KEY_MC_CELL_ID + " = ?",
                            new String[]{String.valueOf(mobileCell)});

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.deleteMobileCell", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void updateMobileCellLastConnectedTime(int mobileCell, long lastConnectedTime) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_MC_LAST_CONNECTED_TIME, lastConnectedTime);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_MOBILE_CELLS, values, KEY_MC_CELL_ID + " = ?",
                            new String[]{String.valueOf(mobileCell)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateMobileCellLastConnectedTime", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    void addMobileCellNamesToList(List<String> cellNamesList) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_MC_NAME +
                        " FROM " + TABLE_MOBILE_CELLS +
                        " WHERE " + KEY_MC_NAME + " IS NOT NULL" +
                        " AND " + KEY_MC_NAME + " <> ''" +
                        " GROUP BY " + KEY_MC_NAME +
                        " ORDER BY " + KEY_MC_NAME;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MC_NAME));
                        cellNamesList.add(name);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    int getNewMobileCellsCount() {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT COUNT(*) " +
                        " FROM " + TABLE_MOBILE_CELLS +
                        " WHERE " + KEY_MC_NEW + "=1";

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r;
        } finally {
            stopRunningCommand();
        }
    }

    // Updating single event
    void updateMobileCellsCells(long eventId, String cells) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();

                //EventPreferencesMobileCells eventPreferences = event._eventPreferencesMobileCells;
                //values.put(KEY_E_MOBILE_CELLS_CELLS, eventPreferences._cells);
                values.put(KEY_E_MOBILE_CELLS_CELLS, cells);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                            new String[] { String.valueOf(eventId) });

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateMobileCellsCells", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isMobileCellSaved(int mobileCell) {
        importExportLock.lock();
        try {
            int r = 0;
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT COUNT(*) " +
                        " FROM " + TABLE_MOBILE_CELLS +
                        " WHERE " + KEY_MC_CELL_ID + "=" + mobileCell;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    r = cursor.getInt(0);
                    cursor.close();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return r > 0;
        } finally {
            stopRunningCommand();
        }
    }

    void loadMobileCellsSensorRunningPausedEvents(List<NotUsedMobileCells> eventList/*, boolean outsideParameter*/) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                eventList.clear();

                final String countQuery;
                String eventTypeChecked;
                eventTypeChecked = KEY_E_STATUS + "=" + Event.ESTATUS_PAUSE + " AND ";  //  only paused events
                eventTypeChecked = eventTypeChecked + KEY_E_MOBILE_CELLS_ENABLED + "=1";
                /*if (outsideParameter) {
                    eventTypeChecked = KEY_E_STATUS + "=" + Event.ESTATUS_PAUSE + " AND ";  //  only paused events
                    eventTypeChecked = eventTypeChecked + KEY_E_MOBILE_CELLS_ENABLED + "=1 AND ";
                    //eventTypeChecked = eventTypeChecked + KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + "=1";
                }
                else {
                    eventTypeChecked = KEY_E_STATUS + "=" + Event.ESTATUS_RUNNING + " AND ";  //  only running events
                    eventTypeChecked = eventTypeChecked + KEY_E_MOBILE_CELLS_ENABLED + "=1 AND ";
                    //eventTypeChecked = eventTypeChecked + KEY_E_MOBILE_CELLS_WHEN_OUTSIDE + "=0";
                }*/

                countQuery = "SELECT " + KEY_E_ID + "," + KEY_E_MOBILE_CELLS_CELLS + "," + KEY_E_MOBILE_CELLS_WHEN_OUTSIDE +
                        " FROM " + TABLE_EVENTS + " WHERE " + eventTypeChecked;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(countQuery, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            NotUsedMobileCells notUsedMobileCells = new NotUsedMobileCells();
                            notUsedMobileCells.eventId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_E_ID));
                            notUsedMobileCells.cells = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_MOBILE_CELLS_CELLS));
                            notUsedMobileCells.whenOutside = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_E_MOBILE_CELLS_WHEN_OUTSIDE)) == 1;
                            eventList.add(notUsedMobileCells);
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    String getEventMobileCellsCells(long eventId) {
        importExportLock.lock();
        try {
            String cells = "";
            try {
                startRunningCommand();

                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_EVENTS,
                        new String[]{KEY_E_MOBILE_CELLS_CELLS},
                        KEY_E_ID + "=?",
                        new String[]{String.valueOf(eventId)}, null, null, null, null);
                if (cursor != null)
                {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0)
                    {
                        cells = cursor.getString(cursor.getColumnIndexOrThrow(KEY_E_MOBILE_CELLS_CELLS));
                    }
                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }

            return cells;
        } finally {
            stopRunningCommand();
        }
    }


// NFC_TAGS ----------------------------------------------------------------------

    // Adding new nfc tag
    void addNFCTag(NFCTag tag) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_NT_UID, tag._uid);
                values.put(KEY_NT_NAME, tag._name);

                db.beginTransaction();

                try {
                    // Inserting Row
                    db.insert(TABLE_NFC_TAGS, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting All nfc tags
    List<NFCTag> getAllNFCTags() {
        importExportLock.lock();
        try {
            List<NFCTag> nfcTagList = new ArrayList<>();
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_NT_ID + "," +
                        KEY_NT_UID + ", " +
                        KEY_NT_NAME +
                        " FROM " + TABLE_NFC_TAGS +
                        " ORDER BY " + KEY_NT_NAME;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        NFCTag nfcTag = new NFCTag(
                            cursor.getLong(cursor.getColumnIndexOrThrow(KEY_NT_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_NT_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(KEY_NT_UID)));
                        nfcTagList.add(nfcTag);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return nfcTagList;
        } finally {
            stopRunningCommand();
        }
    }

    // Updating single nfc tag
    void updateNFCTag(NFCTag tag) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_NT_UID, tag._uid);
                values.put(KEY_NT_NAME, tag._name);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_NFC_TAGS, values, KEY_NT_ID + " = ?",
                            new String[]{String.valueOf(tag._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateNFCTag", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting single nfc tag
    void deleteNFCTag(NFCTag tag) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {
                    // delete geofence
                    db.delete(TABLE_NFC_TAGS, KEY_NT_ID + " = ?",
                            new String[]{String.valueOf(tag._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.deleteNFCTag", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    /*
    String getNFCTagNameByUid(String uid){
        importExportLock.lock();
        try {
            String tagName = "";
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_NT_NAME +
                        " FROM " + TABLE_NFC_TAGS +
                        " WHERE " + KEY_NT_UID + "=" + uid;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    tagName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NT_NAME));
                }

                cursor.close();

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return tagName;
        } finally {
            stopRunningCommand();
        }
    }
    */

// INTENTS ----------------------------------------------------------------------

    // Adding new intent
    void addIntent(PPIntent intent) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_IN_NAME, intent._name);
                values.put(KEY_IN_PACKAGE_NAME, intent._packageName);
                values.put(KEY_IN_CLASS_NAME, intent._className);
                values.put(KEY_IN_ACTION, intent._action);
                values.put(KEY_IN_DATA, intent._data);
                values.put(KEY_IN_MIME_TYPE, intent._mimeType);
                values.put(KEY_IN_EXTRA_KEY_1, intent._extraKey1);
                values.put(KEY_IN_EXTRA_VALUE_1, intent._extraValue1);
                values.put(KEY_IN_EXTRA_TYPE_1, intent._extraType1);
                values.put(KEY_IN_EXTRA_KEY_2, intent._extraKey2);
                values.put(KEY_IN_EXTRA_VALUE_2, intent._extraValue2);
                values.put(KEY_IN_EXTRA_TYPE_2, intent._extraType2);
                values.put(KEY_IN_EXTRA_KEY_3, intent._extraKey3);
                values.put(KEY_IN_EXTRA_VALUE_3, intent._extraValue3);
                values.put(KEY_IN_EXTRA_TYPE_3, intent._extraType3);
                values.put(KEY_IN_EXTRA_KEY_4, intent._extraKey4);
                values.put(KEY_IN_EXTRA_VALUE_4, intent._extraValue4);
                values.put(KEY_IN_EXTRA_TYPE_4, intent._extraType4);
                values.put(KEY_IN_EXTRA_KEY_5, intent._extraKey5);
                values.put(KEY_IN_EXTRA_VALUE_5, intent._extraValue5);
                values.put(KEY_IN_EXTRA_TYPE_5, intent._extraType5);
                values.put(KEY_IN_EXTRA_KEY_6, intent._extraKey6);
                values.put(KEY_IN_EXTRA_VALUE_6, intent._extraValue6);
                values.put(KEY_IN_EXTRA_TYPE_6, intent._extraType6);
                values.put(KEY_IN_EXTRA_KEY_7, intent._extraKey7);
                values.put(KEY_IN_EXTRA_VALUE_7, intent._extraValue7);
                values.put(KEY_IN_EXTRA_TYPE_7, intent._extraType7);
                values.put(KEY_IN_EXTRA_KEY_8, intent._extraKey8);
                values.put(KEY_IN_EXTRA_VALUE_8, intent._extraValue8);
                values.put(KEY_IN_EXTRA_TYPE_8, intent._extraType8);
                values.put(KEY_IN_EXTRA_KEY_9, intent._extraKey9);
                values.put(KEY_IN_EXTRA_VALUE_9, intent._extraValue9);
                values.put(KEY_IN_EXTRA_TYPE_9, intent._extraType9);
                values.put(KEY_IN_EXTRA_KEY_10, intent._extraKey10);
                values.put(KEY_IN_EXTRA_VALUE_10, intent._extraValue10);
                values.put(KEY_IN_EXTRA_TYPE_10, intent._extraType10);
                values.put(KEY_IN_CATEGORIES, intent._categories);
                values.put(KEY_IN_FLAGS, intent._flags);
                values.put(KEY_IN_INTENT_TYPE, intent._intentType);

                //values.put(KEY_IN_USED_COUNT, intent._usedCount);
                values.put(KEY_IN_DO_NOT_DELETE, intent._doNotDelete);

                db.beginTransaction();

                try {
                    // Inserting Row
                    intent._id = db.insert(TABLE_INTENTS, null, values);

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close(); // Closing database connection
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting All intents
    List<PPIntent> getAllIntents() {
        importExportLock.lock();
        try {
            List<PPIntent> intentList = new ArrayList<>();
            try {
                startRunningCommand();

                // Select All Query
                final String selectQuery = "SELECT " + KEY_IN_ID + "," +
                        KEY_IN_NAME + ", " +
                        KEY_IN_PACKAGE_NAME + ", " +
                        KEY_IN_CLASS_NAME + ", " +
                        KEY_IN_ACTION + ", " +
                        KEY_IN_DATA + ", " +
                        KEY_IN_MIME_TYPE + ", " +
                        KEY_IN_EXTRA_KEY_1 + ", " +
                        KEY_IN_EXTRA_VALUE_1 + ", " +
                        KEY_IN_EXTRA_TYPE_1 + ", " +
                        KEY_IN_EXTRA_KEY_2 + ", " +
                        KEY_IN_EXTRA_VALUE_2 + ", " +
                        KEY_IN_EXTRA_TYPE_2 + ", " +
                        KEY_IN_EXTRA_KEY_3 + ", " +
                        KEY_IN_EXTRA_VALUE_3 + ", " +
                        KEY_IN_EXTRA_TYPE_3 + ", " +
                        KEY_IN_EXTRA_KEY_4 + ", " +
                        KEY_IN_EXTRA_VALUE_4 + ", " +
                        KEY_IN_EXTRA_TYPE_4 + ", " +
                        KEY_IN_EXTRA_KEY_5 + ", " +
                        KEY_IN_EXTRA_VALUE_5 + ", " +
                        KEY_IN_EXTRA_TYPE_5 + ", " +
                        KEY_IN_EXTRA_KEY_6 + ", " +
                        KEY_IN_EXTRA_VALUE_6 + ", " +
                        KEY_IN_EXTRA_TYPE_6 + ", " +
                        KEY_IN_EXTRA_KEY_7 + ", " +
                        KEY_IN_EXTRA_VALUE_7 + ", " +
                        KEY_IN_EXTRA_TYPE_7 + ", " +
                        KEY_IN_EXTRA_KEY_8 + ", " +
                        KEY_IN_EXTRA_VALUE_8 + ", " +
                        KEY_IN_EXTRA_TYPE_8 + ", " +
                        KEY_IN_EXTRA_KEY_9 + ", " +
                        KEY_IN_EXTRA_VALUE_9 + ", " +
                        KEY_IN_EXTRA_TYPE_9 + ", " +
                        KEY_IN_EXTRA_KEY_10 + ", " +
                        KEY_IN_EXTRA_VALUE_10 + ", " +
                        KEY_IN_EXTRA_TYPE_10 + ", " +
                        KEY_IN_CATEGORIES + ", " +
                        KEY_IN_FLAGS + ", " +
                        KEY_IN_INTENT_TYPE + ", " +

                        //KEY_IN_USED_COUNT + ", " +
                        KEY_IN_DO_NOT_DELETE +

                        " FROM " + TABLE_INTENTS +
                        " ORDER BY " + KEY_IN_NAME;

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        PPIntent ppIntent = new PPIntent(
                                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_IN_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_PACKAGE_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_CLASS_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_ACTION)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_DATA)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_MIME_TYPE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_1)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_1)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_1)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_2)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_3)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_3)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_3)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_4)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_4)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_4)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_5)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_5)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_5)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_6)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_6)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_6)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_7)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_7)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_7)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_8)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_8)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_8)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_9)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_9)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_9)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_10)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_10)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_10)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_CATEGORIES)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_FLAGS)),
                                //cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_USED_COUNT)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_INTENT_TYPE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_DO_NOT_DELETE))  == 1
                        );
                        intentList.add(ppIntent);
                    } while (cursor.moveToNext());
                }

                cursor.close();
                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return intentList;
        } finally {
            stopRunningCommand();
        }
    }

    // Updating single intent
    void updateIntent(PPIntent intent) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_IN_NAME, intent._name);
                values.put(KEY_IN_PACKAGE_NAME, intent._packageName);
                values.put(KEY_IN_CLASS_NAME, intent._className);
                values.put(KEY_IN_ACTION, intent._action);
                values.put(KEY_IN_DATA, intent._data);
                values.put(KEY_IN_MIME_TYPE, intent._mimeType);
                values.put(KEY_IN_EXTRA_KEY_1, intent._extraKey1);
                values.put(KEY_IN_EXTRA_VALUE_1, intent._extraValue1);
                values.put(KEY_IN_EXTRA_TYPE_1, intent._extraType1);
                values.put(KEY_IN_EXTRA_KEY_2, intent._extraKey2);
                values.put(KEY_IN_EXTRA_VALUE_2, intent._extraValue2);
                values.put(KEY_IN_EXTRA_TYPE_2, intent._extraType2);
                values.put(KEY_IN_EXTRA_KEY_3, intent._extraKey3);
                values.put(KEY_IN_EXTRA_VALUE_3, intent._extraValue3);
                values.put(KEY_IN_EXTRA_TYPE_3, intent._extraType3);
                values.put(KEY_IN_EXTRA_KEY_4, intent._extraKey4);
                values.put(KEY_IN_EXTRA_VALUE_4, intent._extraValue4);
                values.put(KEY_IN_EXTRA_TYPE_4, intent._extraType4);
                values.put(KEY_IN_EXTRA_KEY_5, intent._extraKey5);
                values.put(KEY_IN_EXTRA_VALUE_5, intent._extraValue5);
                values.put(KEY_IN_EXTRA_TYPE_5, intent._extraType5);
                values.put(KEY_IN_EXTRA_KEY_6, intent._extraKey6);
                values.put(KEY_IN_EXTRA_VALUE_6, intent._extraValue6);
                values.put(KEY_IN_EXTRA_TYPE_6, intent._extraType6);
                values.put(KEY_IN_EXTRA_KEY_7, intent._extraKey7);
                values.put(KEY_IN_EXTRA_VALUE_7, intent._extraValue7);
                values.put(KEY_IN_EXTRA_TYPE_7, intent._extraType7);
                values.put(KEY_IN_EXTRA_KEY_8, intent._extraKey8);
                values.put(KEY_IN_EXTRA_VALUE_8, intent._extraValue8);
                values.put(KEY_IN_EXTRA_TYPE_8, intent._extraType8);
                values.put(KEY_IN_EXTRA_KEY_9, intent._extraKey9);
                values.put(KEY_IN_EXTRA_VALUE_9, intent._extraValue9);
                values.put(KEY_IN_EXTRA_TYPE_9, intent._extraType9);
                values.put(KEY_IN_EXTRA_KEY_10, intent._extraKey10);
                values.put(KEY_IN_EXTRA_VALUE_10, intent._extraValue10);
                values.put(KEY_IN_EXTRA_TYPE_10, intent._extraType10);
                values.put(KEY_IN_CATEGORIES, intent._categories);
                values.put(KEY_IN_FLAGS, intent._flags);
                values.put(KEY_IN_INTENT_TYPE, intent._intentType);

                //values.put(KEY_IN_USED_COUNT, intent._usedCount);
                values.put(KEY_IN_DO_NOT_DELETE, intent._doNotDelete);

                db.beginTransaction();

                try {
                    // updating row
                    db.update(TABLE_INTENTS, values, KEY_IN_ID + " = ?",
                            new String[]{String.valueOf(intent._id)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updateIntent", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    // Getting single intent
    PPIntent getIntent(long intentId) {
        importExportLock.lock();
        try {
            PPIntent intent = null;
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getReadableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursor = db.query(TABLE_INTENTS,
                        new String[]{KEY_IN_ID,
                                KEY_IN_NAME,
                                KEY_IN_PACKAGE_NAME,
                                KEY_IN_CLASS_NAME,
                                KEY_IN_ACTION,
                                KEY_IN_DATA,
                                KEY_IN_MIME_TYPE,
                                KEY_IN_EXTRA_KEY_1,
                                KEY_IN_EXTRA_VALUE_1,
                                KEY_IN_EXTRA_TYPE_1,
                                KEY_IN_EXTRA_KEY_2,
                                KEY_IN_EXTRA_VALUE_2,
                                KEY_IN_EXTRA_TYPE_2,
                                KEY_IN_EXTRA_KEY_3,
                                KEY_IN_EXTRA_VALUE_3,
                                KEY_IN_EXTRA_TYPE_3,
                                KEY_IN_EXTRA_KEY_4,
                                KEY_IN_EXTRA_VALUE_4,
                                KEY_IN_EXTRA_TYPE_4,
                                KEY_IN_EXTRA_KEY_5,
                                KEY_IN_EXTRA_VALUE_5,
                                KEY_IN_EXTRA_TYPE_5,
                                KEY_IN_EXTRA_KEY_6,
                                KEY_IN_EXTRA_VALUE_6,
                                KEY_IN_EXTRA_TYPE_6,
                                KEY_IN_EXTRA_KEY_7,
                                KEY_IN_EXTRA_VALUE_7,
                                KEY_IN_EXTRA_TYPE_7,
                                KEY_IN_EXTRA_KEY_8,
                                KEY_IN_EXTRA_VALUE_8,
                                KEY_IN_EXTRA_TYPE_8,
                                KEY_IN_EXTRA_KEY_9,
                                KEY_IN_EXTRA_VALUE_9,
                                KEY_IN_EXTRA_TYPE_9,
                                KEY_IN_EXTRA_KEY_10,
                                KEY_IN_EXTRA_VALUE_10,
                                KEY_IN_EXTRA_TYPE_10,
                                KEY_IN_CATEGORIES,
                                KEY_IN_FLAGS,
                                KEY_IN_INTENT_TYPE,

                                //KEY_IN_USED_COUNT,
                                KEY_IN_DO_NOT_DELETE
                        },
                        KEY_IN_ID + "=?",
                        new String[]{String.valueOf(intentId)}, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();

                    if (cursor.getCount() > 0) {
                        intent = new PPIntent(
                                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_IN_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_PACKAGE_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_CLASS_NAME)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_ACTION)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_DATA)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_MIME_TYPE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_1)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_1)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_1)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_2)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_2)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_3)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_3)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_3)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_4)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_4)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_4)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_5)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_5)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_5)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_6)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_6)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_6)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_7)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_7)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_7)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_8)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_8)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_8)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_9)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_9)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_9)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_KEY_10)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_VALUE_10)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_EXTRA_TYPE_10)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_CATEGORIES)),
                                cursor.getString(cursor.getColumnIndexOrThrow(KEY_IN_FLAGS)),
                                //cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_USED_COUNT)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_INTENT_TYPE)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_DO_NOT_DELETE)) == 1
                        );
                    }

                    cursor.close();
                }

                //db.close();

            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return intent;
        } finally {
            stopRunningCommand();
        }
    }

    // Deleting single intent
    void deleteIntent(long intentId) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {
                    // delete geofence
                    db.delete(TABLE_INTENTS, KEY_IN_ID + " = ?",
                            new String[]{String.valueOf(intentId)});

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.deleteIntent", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    /*
    void updatePPIntentUsageCount(final List<Application> oldApplicationsList,
                                   final List<Application> applicationsList) {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                db.beginTransaction();

                try {

                    for (Application application : oldApplicationsList) {
                        if ((application.type == Application.TYPE_INTENT) && (application.intentId > 0)) {

                            Cursor cursor = db.query(TABLE_INTENTS,
                                    new String[]{ KEY_IN_USED_COUNT },
                                    KEY_IN_ID + "=?",
                                    new String[]{String.valueOf(application.intentId)}, null, null, null, null);

                            if (cursor != null) {
                                cursor.moveToFirst();

                                if (cursor.getCount() > 0) {
                                    int usedCount = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_USED_COUNT));
                                    if (usedCount > 0) {
                                        --usedCount;

                                        Log.e("DatabaseHandler.updatePPIntentUsageCount", "usedCount (old)="+usedCount);
                                        ContentValues values = new ContentValues();
                                        values.put(KEY_IN_USED_COUNT, usedCount);
                                        db.update(TABLE_INTENTS, values, KEY_IN_ID + " = ?",
                                                new String[]{String.valueOf(application.intentId)});

                                    }
                                }

                                cursor.close();
                            }
                        }
                    }

                    for (Application application : applicationsList) {
                        if ((application.type == Application.TYPE_INTENT) && (application.intentId > 0)) {

                            Cursor cursor = db.query(TABLE_INTENTS,
                                    new String[]{ KEY_IN_USED_COUNT },
                                    KEY_IN_ID + "=?",
                                    new String[]{String.valueOf(application.intentId)}, null, null, null, null);

                            if (cursor != null) {
                                cursor.moveToFirst();

                                if (cursor.getCount() > 0) {
                                    int usedCount = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IN_USED_COUNT));
                                    ++usedCount;

                                    Log.e("DatabaseHandler.updatePPIntentUsageCount", "usedCount (new)="+usedCount);
                                    ContentValues values = new ContentValues();
                                    values.put(KEY_IN_USED_COUNT, usedCount);
                                    db.update(TABLE_INTENTS, values, KEY_IN_ID + " = ?",
                                            new String[]{String.valueOf(application.intentId)});
                                }

                                cursor.close();
                            }
                        }
                    }

                    db.setTransactionSuccessful();

                } catch (Exception e) {
                    //Error in between database transaction
                    //Log.e("DatabaseHandler.updatePPIntentUsageCount", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                }

                //db.close();
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }
    */

// OTHERS -------------------------------------------------------------------------

    void disableNotAllowedPreferences()
    {
        importExportLock.lock();
        try {
            try {
                startRunningCommand();

                final String selectProfilesQuery = "SELECT " + KEY_ID + "," +
                        KEY_DEVICE_AIRPLANE_MODE + "," +
                        KEY_DEVICE_WIFI + "," +
                        KEY_DEVICE_BLUETOOTH + "," +
                        KEY_DEVICE_MOBILE_DATA + "," +
                        KEY_DEVICE_MOBILE_DATA_PREFS + "," +
                        KEY_DEVICE_GPS + "," +
                        KEY_DEVICE_LOCATION_SERVICE_PREFS + "," +
                        KEY_DEVICE_NFC + "," +
                        KEY_VOLUME_RINGER_MODE + "," +
                        KEY_DEVICE_WIFI_AP + "," +
                        KEY_DEVICE_POWER_SAVE_MODE + "," +
                        KEY_VOLUME_ZEN_MODE + "," +
                        KEY_DEVICE_NETWORK_TYPE + "," +
                        KEY_DEVICE_NETWORK_TYPE_PREFS + "," +
                        KEY_NOTIFICATION_LED + "," +
                        KEY_VIBRATE_WHEN_RINGING + "," +
                        KEY_VIBRATE_NOTIFICATIONS + "," +
                        KEY_DEVICE_CONNECT_TO_SSID + "," +
                        KEY_APPLICATION_DISABLE_WIFI_SCANNING + "," +
                        KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING + "," +
                        KEY_DEVICE_WIFI_AP_PREFS + "," +
                        KEY_HEADS_UP_NOTIFICATIONS + "," +
                        KEY_ALWAYS_ON_DISPLAY + "," +
                        KEY_DEVICE_LOCATION_MODE + "," +
                        KEY_CAMERA_FLASH + "," +
                        KEY_DEVICE_NETWORK_TYPE_SIM1 + "," +
                        KEY_DEVICE_NETWORK_TYPE_SIM2 + "," +
                        KEY_DEVICE_MOBILE_DATA_SIM1 + "," +
                        KEY_DEVICE_MOBILE_DATA_SIM2 + "," +
                        KEY_DEVICE_DEFAULT_SIM_CARDS + "," +
                        KEY_DEVICE_ONOFF_SIM1 + "," +
                        KEY_DEVICE_ONOFF_SIM2 + "," +
                        KEY_SOUND_RINGTONE_CHANGE_SIM1 + "," +
                        KEY_SOUND_RINGTONE_CHANGE_SIM2 + "," +
                        KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "," +
                        KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "," +
                        KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS +
                        " FROM " + TABLE_PROFILES;
                final String selectEventsQuery = "SELECT " + KEY_E_ID + "," +
                        KEY_E_WIFI_ENABLED + "," +
                        KEY_E_BLUETOOTH_ENABLED + "," +
                        KEY_E_NOTIFICATION_ENABLED + "," +
                        KEY_E_ORIENTATION_ENABLED + "," +
                        KEY_E_MOBILE_CELLS_ENABLED + "," +
                        KEY_E_NFC_ENABLED + "," +
                        KEY_E_RADIO_SWITCH_ENABLED + "," +
                        KEY_E_SOUND_PROFILE_ENABLED + "," +
                        KEY_E_VOLUMES_ENABLED + "," +
                        KEY_E_VOLUMES_RINGTONE + "," +
                        KEY_E_VOLUMES_NOTIFICATION + "," +
                        KEY_E_VOLUMES_MEDIA + "," +
                        KEY_E_VOLUMES_ALARM + "," +
                        KEY_E_VOLUMES_SYSTEM + "," +
                        KEY_E_VOLUMES_VOICE + "," +
                        KEY_E_VOLUMES_BLUETOOTHSCO + "," +
                        KEY_E_VOLUMES_ACCESSIBILITY +
                        " FROM " + TABLE_EVENTS;

                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                ContentValues values = new ContentValues();

                Cursor profilesCursor = db.rawQuery(selectProfilesQuery, null);
                Cursor eventsCursor = db.rawQuery(selectEventsQuery, null);

                db.beginTransaction();
                //noinspection TryFinallyCanBeTryWithResources
                try {
                    SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("temp_disableNotAllowedPreferences", Context.MODE_PRIVATE);

                    if (profilesCursor.moveToFirst()) {
                        do {
                            Profile profile = getProfile(profilesCursor.getLong(profilesCursor.getColumnIndexOrThrow(KEY_ID)), false);
                            profile.saveProfileToSharedPreferences(sharedPreferences);

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_AIRPLANE_MODE)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SET_AS_ASSISTANT) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_AIRPLANE_MODE, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_WIFI)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_WIFI, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_BLUETOOTH)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_BLUETOOTH, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_MOBILE_DATA)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    //Log.e("*********** DatabaseHandler.disableNotAllowedPreferences", "KEY_DEVICE_MOBILE_DATA");
                                    //Log.e("*********** DatabaseHandler.disableNotAllowedPreferences", "preferenceAllowed.notAllowedReason="+preferenceAllowed.notAllowedReason);
                                    values.clear();
                                    values.put(KEY_DEVICE_MOBILE_DATA, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_MOBILE_DATA_SIM1)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_MOBILE_DATA_SIM1, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_MOBILE_DATA_SIM2)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_MOBILE_DATA_SIM2, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_MOBILE_DATA_PREFS)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, null, sharedPreferences, true, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_MOBILE_DATA_PREFS, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_GPS)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_GPS, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_GPS, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_LOCATION_SERVICE_PREFS)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_NFC)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NFC, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_NFC, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_WIFI_AP)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_WIFI_AP, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_VOLUME_RINGER_MODE)) == 5) {
                                boolean notRemove = ActivateProfileHelper.canChangeZenMode(context);
                                if (!notRemove) {
                                    int zenMode = profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_VOLUME_ZEN_MODE));
                                    int ringerMode = 0;
                                    switch (zenMode) {
                                        case 1:
                                            ringerMode = 1;
                                            break;
                                        case 2:
                                        case 3:
                                        case 6:
                                            ringerMode = 4;
                                            break;
                                        case 4:
                                        case 5:
                                            ringerMode = 3;
                                            break;
                                    }
                                    values.clear();
                                    values.put(KEY_VOLUME_RINGER_MODE, ringerMode);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                                if (!((vibrator != null) && vibrator.hasVibrator())) {
                                    int ringerMode = profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_VOLUME_RINGER_MODE));
                                    if (ringerMode == 3) {
                                        ringerMode = 1;

                                        values.clear();
                                        values.put(KEY_VOLUME_RINGER_MODE, ringerMode);
                                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                                new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                    }
                                    else
                                    if (ringerMode == 5) {
                                        int zenMode = profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_VOLUME_ZEN_MODE));
                                        if (zenMode == 4)
                                            zenMode = 1;
                                        else
                                        if (zenMode == 5)
                                            zenMode = 2;

                                        values.clear();
                                        values.put(KEY_VOLUME_ZEN_MODE, zenMode);
                                        db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                                new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                    }
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_POWER_SAVE_MODE)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_POWER_SAVE_MODE, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_NETWORK_TYPE)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_NETWORK_TYPE, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_NETWORK_TYPE_SIM1)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_NETWORK_TYPE_SIM1, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_NETWORK_TYPE_SIM2)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_NETWORK_TYPE_SIM2, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_NOTIFICATION_LED)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_NOTIFICATION_LED, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_VIBRATE_WHEN_RINGING)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_VIBRATE_WHEN_RINGING, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_VIBRATE_NOTIFICATIONS)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_VIBRATE_NOTIFICATIONS, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            PreferenceAllowed _preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, null, sharedPreferences, false, context);
                            if ((_preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                    (_preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                    (_preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                    (_preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                    (_preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                values.clear();
                                values.put(KEY_DEVICE_CONNECT_TO_SSID, Profile.CONNECTTOSSID_JUSTANY);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_APPLICATION_DISABLE_WIFI_SCANNING)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_APPLICATION_DISABLE_WIFI_SCANNING, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_APPLICATION_DISABLE_BLUETOOTH_SCANNING, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_WIFI_AP_PREFS)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_WIFI_AP_PREFS, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_HEADS_UP_NOTIFICATIONS)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_HEADS_UP_NOTIFICATIONS, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_NETWORK_TYPE_PREFS)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_NETWORK_TYPE_PREFS, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ALWAYS_ON_DISPLAY)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_ALWAYS_ON_DISPLAY, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_LOCATION_MODE)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_LOCATION_MODE, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_CAMERA_FLASH)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_CAMERA_FLASH, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_CAMERA_FLASH, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_DEFAULT_SIM_CARDS)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_DEFAULT_SIM_CARDS, "0|0|0");
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_ONOFF_SIM1)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_ONOFF_SIM1, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_DEVICE_ONOFF_SIM2)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_DEVICE_ONOFF_SIM2, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_SOUND_RINGTONE_CHANGE_SIM1)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_SOUND_RINGTONE_CHANGE_SIM1, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_SOUND_RINGTONE_CHANGE_SIM2)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_SOUND_RINGTONE_CHANGE_SIM2, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_SOUND_NOTIFICATION_CHANGE_SIM1)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_SOUND_NOTIFICATION_CHANGE_SIM1, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_SOUND_NOTIFICATION_CHANGE_SIM2)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_SOUND_NOTIFICATION_CHANGE_SIM2, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }
                            if (profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS)) != 0) {
                                PreferenceAllowed preferenceAllowed = Profile.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, null, sharedPreferences, false, context);
                                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_SIM_CARD)) {
                                    values.clear();
                                    values.put(KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, 0);
                                    db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                            new String[]{String.valueOf(profilesCursor.getInt(profilesCursor.getColumnIndexOrThrow(KEY_ID)))});
                                }
                            }

                        } while (profilesCursor.moveToNext());
                    }

                    //-----------------------

                    if (eventsCursor.moveToFirst()) {
                        do {
                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_WIFI_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_WIFI_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                            }
                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_BLUETOOTH_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_BLUETOOTH_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                            }
                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_NOTIFICATION_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_NOTIFICATION_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                            }
                            if (eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ORIENTATION_ENABLED)) != 0) {
                                boolean hasAccelerometer = PPApplication.accelerometerSensor != null;
                                boolean hasMagneticField = PPApplication.magneticFieldSensor != null;
                                boolean hasProximity = PPApplication.proximitySensor != null;
                                boolean hasLight = PPApplication.lightSensor != null;

                                boolean enabled = hasAccelerometer && hasMagneticField;
                                if (!enabled) {
                                    values.clear();
                                    values.put(KEY_E_ORIENTATION_DISPLAY, "");
                                    values.put(KEY_E_ORIENTATION_SIDES, "");
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                                }
                                enabled = hasAccelerometer;
                                if (!enabled) {
                                    values.clear();
                                    values.put(KEY_E_ORIENTATION_SIDES, "");
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                                }
                                enabled = hasProximity;
                                if (!enabled) {
                                    values.clear();
                                    values.put(KEY_E_ORIENTATION_DISTANCE, 0);
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                                }
                                enabled = hasLight;
                                if (!enabled) {
                                    values.clear();
                                    values.put(KEY_E_ORIENTATION_CHECK_LIGHT, 0);
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                                }
                            }
                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_MOBILE_CELLS_ENABLED)) != 0) &&
                                    //(Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                    (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED_NO_CHECK_SIM, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_MOBILE_CELLS_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                            }

                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_NFC_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_NFC_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                            }

                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_RADIO_SWITCH_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_RADIO_SWITCH_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                            }

                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_SOUND_PROFILE_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesSoundProfile.PREF_EVENT_SOUND_PROFILE_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_SOUND_PROFILE_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                            }

                            if ((eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_VOLUMES_ENABLED)) != 0) &&
                                    (Event.isEventPreferenceAllowed(EventPreferencesVolumes.PREF_EVENT_VOLUMES_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)) {
                                values.clear();
                                values.put(KEY_E_VOLUMES_ENABLED, 0);
                                db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                        new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                            }

                            String value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(KEY_E_VOLUMES_RINGTONE));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(KEY_E_VOLUMES_RINGTONE, splits[0] + "|0|0");
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(KEY_E_VOLUMES_NOTIFICATION));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(KEY_E_VOLUMES_NOTIFICATION, splits[0] + "|0|0");
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(KEY_E_VOLUMES_MEDIA));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(KEY_E_VOLUMES_MEDIA, splits[0] + "|0|0");
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(KEY_E_VOLUMES_ALARM));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(KEY_E_VOLUMES_ALARM, splits[0] + "|0|0");
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(KEY_E_VOLUMES_SYSTEM));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(KEY_E_VOLUMES_SYSTEM, splits[0] + "|0|0");
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(KEY_E_VOLUMES_VOICE));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(KEY_E_VOLUMES_VOICE, splits[0] + "|0|0");
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(KEY_E_VOLUMES_BLUETOOTHSCO));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(KEY_E_VOLUMES_BLUETOOTHSCO, splits[0] + "|0|0");
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                                }
                            }

                            value = eventsCursor.getString(eventsCursor.getColumnIndexOrThrow(KEY_E_VOLUMES_ACCESSIBILITY));
                            if (value != null) {
                                int operator = 0;
                                String[] splits = value.split("\\|");
                                if (splits.length > 1) {
                                    try {
                                        operator = Integer.parseInt(splits[1]);
                                    } catch (Exception ignored) {
                                    }
                                }
                                if (operator > 6) {
                                    values.clear();
                                    values.put(KEY_E_VOLUMES_ACCESSIBILITY, splits[0] + "|0|0");
                                    db.update(TABLE_EVENTS, values, KEY_E_ID + " = ?",
                                            new String[]{String.valueOf(eventsCursor.getInt(eventsCursor.getColumnIndexOrThrow(KEY_E_ID)))});
                                }
                            }

                        } while (eventsCursor.moveToNext());
                    }

                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    //Error in between database transaction
//                    Log.e("DatabaseHandler.disableNotAllowedPreferences", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    db.endTransaction();
                    profilesCursor.close();
                    eventsCursor.close();
                }

                //db.close();
            } catch (Exception e) {
//                Log.e("DatabaseHandler.disableNotAllowedPreferences", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        } finally {
            stopRunningCommand();
        }
    }

    private boolean tableExists(String tableName, SQLiteDatabase db)
    {
        //boolean tableExists = false;

        /* get cursor on it */
        try
        {
            String query = "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'";
            try (Cursor cursor = db.rawQuery(query, null)) {
                if(cursor!=null) {
                    if (cursor.getCount()>0) {
                        cursor.close();
                        return true;
                    }
                    cursor.close();
                }
                return false;
            }
            /*
            Cursor c = db.query(tableName, null,
                null, null, null, null, null);
            tableExists = true;
            c.close();*/
        }
        catch (Exception e) {
            /* not exists ? */
            PPApplication.recordException(e);
        }

        return false;
    }

    private void importProfiles(SQLiteDatabase db, SQLiteDatabase exportedDBObj,
                       List<Long> exportedDBEventProfileIds, List<Long> importDBEventProfileIds) {

        long profileId;

        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + TABLE_PROFILES);

            // cursor for profiles exportedDB
            cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_PROFILES, null);
            columnNamesExportedDB = cursorExportedDB.getColumnNames();

            // cursor for profiles of destination db
            cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_PROFILES, null);

            if (cursorExportedDB.moveToFirst()) {
                do {
                    values.clear();
                    for (int i = 0; i < columnNamesExportedDB.length; i++) {
                        // put only when columnNamesExportedDB[i] exists in cursorImportDB
                        if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                            String value = cursorExportedDB.getString(i);
                            values.put(columnNamesExportedDB[i], value);
                        }
                    }

                    // Inserting Row do db z SQLiteOpenHelper
                    profileId = db.insert(TABLE_PROFILES, null, values);
                    // save profile ids
                    exportedDBEventProfileIds.add(cursorExportedDB.getLong(cursorExportedDB.getColumnIndexOrThrow(KEY_ID)));
                    importDBEventProfileIds.add(profileId);

                } while (cursorExportedDB.moveToNext());
            }

            cursorExportedDB.close();
            cursorImportDB.close();
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    private void afterImportDb(SQLiteDatabase db) {
        Cursor cursorImportDB = null;

        // update volumes by device max value
        try {
            cursorImportDB = db.rawQuery("SELECT " +
                            KEY_ID + ","+
                            KEY_VOLUME_RINGTONE + ","+
                            KEY_VOLUME_NOTIFICATION + ","+
                            KEY_VOLUME_MEDIA + ","+
                            KEY_VOLUME_ALARM + ","+
                            KEY_VOLUME_SYSTEM + ","+
                            KEY_VOLUME_VOICE + ","+
                            KEY_VOLUME_DTMF + ","+
                            KEY_VOLUME_ACCESSIBILITY + ","+
                            KEY_VOLUME_BLUETOOTH_SCO +
                    " FROM " + TABLE_PROFILES, null);

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                // these values are saved during export of PPP data
                SharedPreferences sharedPreferences = ApplicationPreferences.getSharedPreferences(context);
                int maximumVolumeRing = sharedPreferences.getInt("maximumVolume_ring", 0);
                int maximumVolumeNotification = sharedPreferences.getInt("maximumVolume_notification", 0);
                int maximumVolumeMusic = sharedPreferences.getInt("maximumVolume_music", 0);
                int maximumVolumeAlarm = sharedPreferences.getInt("maximumVolume_alarm", 0);
                int maximumVolumeSystem = sharedPreferences.getInt("maximumVolume_system", 0);
                int maximumVolumeVoiceCall = sharedPreferences.getInt("maximumVolume_voiceCall", 0);
                int maximumVolumeDTFM = sharedPreferences.getInt("maximumVolume_dtmf", 0);
                int maximumVolumeAccessibility = sharedPreferences.getInt("maximumVolume_accessibility", 0);
                int maximumVolumeBluetoothSCO = sharedPreferences.getInt("maximumVolume_bluetoothSCO", 0);

                if (cursorImportDB.moveToFirst()) {
                    do {

                        long profileId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(KEY_ID));

                        ContentValues values = new ContentValues();

                        if (maximumVolumeRing > 0) {
                            String value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_VOLUME_RINGTONE));
                            try {
                                String[] splits = value.split("\\|");
                                int volume = Integer.parseInt(splits[0]);
                                //Log.e("DatabaseHandler.importDB", "old max ringtone volume="+maximumVolumeRing);
                                //Log.e("DatabaseHandler.importDB", "old ringtone volume="+volume);
                                float fVolume = volume;
                                float percentage = fVolume / maximumVolumeRing * 100f;
                                fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) / 100f * percentage;
                                volume = Math.round(fVolume);
                                //Log.e("DatabaseHandler.importDB", "new max ringtone volume="+audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
                                //Log.e("DatabaseHandler.importDB", "new ringtone volume="+volume);
                                if (splits.length == 3)
                                    values.put(KEY_VOLUME_RINGTONE, volume + "|" + splits[1] + "|" + splits[2]);
                                else
                                    values.put(KEY_VOLUME_RINGTONE, volume + "|" + splits[1]);
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Bad stream type X
                                //PPApplication.recordException(e);
                            } catch (Exception e) {
                                //Log.e("DatabaseHandler.importDB", Log.getStackTraceString(e));
                                PPApplication.recordException(e);
                            }
                        }
                        if (maximumVolumeNotification > 0) {
                            String value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_VOLUME_NOTIFICATION));
                            try {
                                String[] splits = value.split("\\|");
                                int volume = Integer.parseInt(splits[0]);
                                float fVolume = volume;
                                float percentage = fVolume / maximumVolumeNotification * 100f;
                                fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION) / 100f * percentage;
                                volume = Math.round(fVolume);
                                if (splits.length == 3)
                                    values.put(KEY_VOLUME_NOTIFICATION, volume + "|" + splits[1] + "|" + splits[2]);
                                else
                                    values.put(KEY_VOLUME_NOTIFICATION, volume + "|" + splits[1]);
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Bad stream type X
                                //PPApplication.recordException(e);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                        if (maximumVolumeMusic > 0) {
                            String value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_VOLUME_MEDIA));
                            try {
                                String[] splits = value.split("\\|");
                                int volume = Integer.parseInt(splits[0]);
                                float fVolume = volume;
                                float percentage = fVolume / maximumVolumeMusic * 100f;
                                fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100f * percentage;
                                volume = Math.round(fVolume);
                                if (splits.length == 3)
                                    values.put(KEY_VOLUME_MEDIA, volume + "|" + splits[1] + "|" + splits[2]);
                                else
                                    values.put(KEY_VOLUME_MEDIA, volume + "|" + splits[1]);
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Bad stream type X
                                //PPApplication.recordException(e);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                        if (maximumVolumeAlarm > 0) {
                            String value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_VOLUME_ALARM));
                            try {
                                String[] splits = value.split("\\|");
                                int volume = Integer.parseInt(splits[0]);
                                float fVolume = volume;
                                float percentage = fVolume / maximumVolumeAlarm * 100f;
                                fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) / 100f * percentage;
                                volume = Math.round(fVolume);
                                if (splits.length == 3)
                                    values.put(KEY_VOLUME_ALARM, volume + "|" + splits[1] + "|" + splits[2]);
                                else
                                    values.put(KEY_VOLUME_ALARM, volume + "|" + splits[1]);
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Bad stream type X
                                //PPApplication.recordException(e);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                        if (maximumVolumeSystem > 0) {
                            String value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_VOLUME_SYSTEM));
                            try {
                                String[] splits = value.split("\\|");
                                int volume = Integer.parseInt(splits[0]);
                                float fVolume = volume;
                                float percentage = fVolume / maximumVolumeSystem * 100f;
                                fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM) / 100f * percentage;
                                volume = Math.round(fVolume);
                                if (splits.length == 3)
                                    values.put(KEY_VOLUME_SYSTEM, volume + "|" + splits[1] + "|" + splits[2]);
                                else
                                    values.put(KEY_VOLUME_SYSTEM, volume + "|" + splits[1]);
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Bad stream type X
                                //PPApplication.recordException(e);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                        if (maximumVolumeVoiceCall > 0) {
                            String value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_VOLUME_VOICE));
                            try {
                                String[] splits = value.split("\\|");
                                int volume = Integer.parseInt(splits[0]);
                                float fVolume = volume;
                                float percentage = fVolume / maximumVolumeVoiceCall * 100f;
                                fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) / 100f * percentage;
                                volume = Math.round(fVolume);
                                if (splits.length == 3)
                                    values.put(KEY_VOLUME_VOICE, volume + "|" + splits[1] + "|" + splits[2]);
                                else
                                    values.put(KEY_VOLUME_VOICE, volume + "|" + splits[1]);
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Bad stream type X
                                //PPApplication.recordException(e);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                        if (maximumVolumeDTFM > 0) {
                            String value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_VOLUME_DTMF));
                            try {
                                String[] splits = value.split("\\|");
                                int volume = Integer.parseInt(splits[0]);
                                float fVolume = volume;
                                float percentage = fVolume / maximumVolumeDTFM * 100f;
                                fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF) / 100f * percentage;
                                volume = Math.round(fVolume);
                                if (splits.length == 3)
                                    values.put(KEY_VOLUME_DTMF, volume + "|" + splits[1] + "|" + splits[2]);
                                else
                                    values.put(KEY_VOLUME_DTMF, volume + "|" + splits[1]);
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Bad stream type X
                                //PPApplication.recordException(e);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                        if (Build.VERSION.SDK_INT >= 26) {
                            if (maximumVolumeAccessibility > 0) {
                                String value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_VOLUME_ACCESSIBILITY));
                                try {
                                    String[] splits = value.split("\\|");
                                    int volume = Integer.parseInt(splits[0]);
                                    float fVolume = volume;
                                    float percentage = fVolume / maximumVolumeAccessibility * 100f;
                                    fVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY) / 100f * percentage;
                                    volume = Math.round(fVolume);
                                    if (splits.length == 3)
                                        values.put(KEY_VOLUME_ACCESSIBILITY, volume + "|" + splits[1] + "|" + splits[2]);
                                    else
                                        values.put(KEY_VOLUME_ACCESSIBILITY, volume + "|" + splits[1]);
                                } catch (IllegalArgumentException e) {
                                    // java.lang.IllegalArgumentException: Bad stream type 10 - Android 6
                                    //PPApplication.recordException(e);
                                } catch (Exception e) {
                                    PPApplication.recordException(e);
                                }
                            }
                        }
                        if (maximumVolumeBluetoothSCO > 0) {
                            String value = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_VOLUME_BLUETOOTH_SCO));
                            try {
                                String[] splits = value.split("\\|");
                                int volume = Integer.parseInt(splits[0]);
                                float fVolume = volume;
                                float percentage = fVolume / maximumVolumeBluetoothSCO * 100f;
                                fVolume = audioManager.getStreamMaxVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO) / 100f * percentage;
                                volume = Math.round(fVolume);
                                if (splits.length == 3)
                                    values.put(KEY_VOLUME_BLUETOOTH_SCO, volume + "|" + splits[1] + "|" + splits[2]);
                                else
                                    values.put(KEY_VOLUME_BLUETOOTH_SCO, volume + "|" + splits[1]);
                            } catch (IllegalArgumentException e) {
                                // java.lang.IllegalArgumentException: Bad stream type X
                                //PPApplication.recordException(e);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }

                        // updating row
                        if (values.size() > 0)
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});
                    } while (cursorImportDB.moveToNext());
                }
            }
            cursorImportDB.close();
        } finally {
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }

        // clear dual sim parameters for device without dual sim support
        int phoneCount = 1;
        if (Build.VERSION.SDK_INT >= 26) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                phoneCount = telephonyManager.getPhoneCount();
            }
        }
        if (phoneCount < 2) {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NETWORK_TYPE_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NETWORK_TYPE_SIM2 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_NETWORK_TYPE_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_NETWORK_TYPE_SIM2 + "=0");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_MOBILE_DATA_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_MOBILE_DATA_SIM2 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_MOBILE_DATA_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_MOBILE_DATA_SIM2 + "=0");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_DEFAULT_SIM_CARDS + "=\"0|0|0\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_DEFAULT_SIM_CARDS + "=\"0|0|0\"");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_ONOFF_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_ONOFF_SIM2 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_ONOFF_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_DEVICE_ONOFF_SIM2 + "=0");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_RINGTONE_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_RINGTONE_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_RINGTONE_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_RINGTONE_SIM2 + "=\"\"");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_NOTIFICATION_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_NOTIFICATION_SIM2 + "=\"\"");

            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_RINGTONE_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_RINGTONE_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_NOTIFICATION_CHANGE_SIM1 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_NOTIFICATION_CHANGE_SIM2 + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_RINGTONE_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_RINGTONE_SIM2 + "=\"\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_NOTIFICATION_SIM1 + "=\"\"");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_NOTIFICATION_SIM2 + "=\"\"");

            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + "=0");
            db.execSQL("UPDATE " + TABLE_MERGED_PROFILE + " SET " + KEY_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS + "=0");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_FROM_SIM_SLOT + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_CALL_FOR_SIM_CARD + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_FROM_SIM_SLOT + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_SMS_FOR_SIM_CARD + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_MOBILE_CELLS_FOR_SIM_CARD + "=0");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_CALLS + "=0");
            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_DEFAULT_SIM_FOR_SMS + "=0");

            db.execSQL("UPDATE " + TABLE_EVENTS + " SET " + KEY_E_RADIO_SWITCH_SIM_ON_OFF + "=0");
        }

        // set profile parameters to "Not used" for non-granted Uri premissions
        try {
            cursorImportDB = db.rawQuery("SELECT " +
                            KEY_ID + ","+
                            KEY_ICON + "," +
                            KEY_SOUND_RINGTONE + "," +
                            KEY_SOUND_RINGTONE_SIM1 + "," +
                            KEY_SOUND_RINGTONE_SIM2 + "," +
                            KEY_SOUND_NOTIFICATION + "," +
                            KEY_SOUND_NOTIFICATION_SIM1 + "," +
                            KEY_SOUND_NOTIFICATION_SIM2 + "," +
                            KEY_SOUND_ALARM + "," +
                            KEY_DEVICE_WALLPAPER_CHANGE + "," +
                            KEY_DEVICE_WALLPAPER + "," +
                            KEY_DEVICE_WALLPAPER_FOLDER + "," +
                            KEY_DURATION_NOTIFICATION_SOUND +
                    " FROM " + TABLE_PROFILES, null);

            ContentResolver contentResolver = context.getContentResolver();

            if (cursorImportDB.moveToFirst()) {
                do {
                    long profileId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(KEY_ID));

                    ContentValues values = new ContentValues();

                    String icon = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_ICON));
                    if (!Profile.getIsIconResourceID(icon)) {
                        String iconIdentifier = Profile.getIconIdentifier(icon);
                        boolean isGranted = false;
                        Uri uri = Uri.parse(iconIdentifier);
                        if (uri != null) {
                            try {
                                context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                isGranted = true;
                            } catch (Exception e) {
                                //isGranted = false;
                            }
//                            Log.e("*********** DatabaseHandler.afterImportDb", "KEY_ICON -isGranted=" + isGranted);
                        }
                        if (!isGranted) {
                            values.clear();
                            values.put(KEY_ICON, Profile.defaultValuesString.get(Profile.PREF_PROFILE_ICON));
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});
                        }
                    }

                    String tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_SOUND_RINGTONE));
                    String[] splits = tone.split("\\|");
                    String ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(context, ringtone, RingtoneManager.TYPE_RINGTONE);
                            if (uri != null) {
                                try {
                                    context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
//                                Log.e("*********** DatabaseHandler.afterImportDb", "KEY_SOUND_RINGTONE isGranted=" + isGranted);
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(KEY_SOUND_RINGTONE_CHANGE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE));
                                values.put(KEY_SOUND_RINGTONE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE));
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_SOUND_RINGTONE_SIM1));
                    splits = tone.split("\\|");
                    ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(context, ringtone, RingtoneManager.TYPE_RINGTONE);
                            if (uri != null) {
                                try {
                                    context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
//                                Log.e("*********** DatabaseHandler.afterImportDb", "KEY_SOUND_RINGTONE_SIM1 isGranted=" + isGranted);
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(KEY_SOUND_RINGTONE_CHANGE_SIM1, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1));
                                values.put(KEY_SOUND_RINGTONE_SIM1, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1));
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_SOUND_RINGTONE_SIM2));
                    splits = tone.split("\\|");
                    ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(context, ringtone, RingtoneManager.TYPE_RINGTONE);
                            if (uri != null) {
                                try {
                                    context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
//                                Log.e("*********** DatabaseHandler.afterImportDb", "KEY_SOUND_RINGTONE_SIM2 isGranted=" + isGranted);
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(KEY_SOUND_RINGTONE_CHANGE_SIM2, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2));
                                values.put(KEY_SOUND_RINGTONE_SIM2, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2));
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_SOUND_NOTIFICATION));
                    splits = tone.split("\\|");
                    ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(context, ringtone, RingtoneManager.TYPE_NOTIFICATION);
                            if (uri != null) {
                                try {
                                    context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
//                                Log.e("*********** DatabaseHandler.afterImportDb", "KEY_SOUND_NOTIFICATION isGranted=" + isGranted);
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(KEY_SOUND_NOTIFICATION_CHANGE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE));
                                values.put(KEY_SOUND_NOTIFICATION, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION));
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_SOUND_NOTIFICATION_SIM1));
                    splits = tone.split("\\|");
                    ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(context, ringtone, RingtoneManager.TYPE_NOTIFICATION);
                            if (uri != null) {
                                try {
                                    context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
//                                Log.e("*********** DatabaseHandler.afterImportDb", "KEY_SOUND_NOTIFICATION_SIM1 isGranted=" + isGranted);
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(KEY_SOUND_NOTIFICATION_CHANGE_SIM1, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1));
                                values.put(KEY_SOUND_NOTIFICATION_SIM1, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1));
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_SOUND_NOTIFICATION_SIM2));
                    splits = tone.split("\\|");
                    ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(context, ringtone, RingtoneManager.TYPE_NOTIFICATION);
                            if (uri != null) {
                                try {
                                    context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
//                                Log.e("*********** DatabaseHandler.afterImportDb", "KEY_SOUND_NOTIFICATION_SIM2 isGranted=" + isGranted);
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(KEY_SOUND_NOTIFICATION_CHANGE_SIM2, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2));
                                values.put(KEY_SOUND_NOTIFICATION_SIM2, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2));
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_SOUND_ALARM));
                    splits = tone.split("\\|");
                    ringtone = splits[0];
                    if (!ringtone.isEmpty()) {
                        if (ringtone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = ActivateProfileHelper.getUriOfSavedTone(context, ringtone, RingtoneManager.TYPE_ALARM);
                            if (uri != null) {
                                try {
                                    context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
//                                Log.e("*********** DatabaseHandler.afterImportDb", "KEY_SOUND_ALARM isGranted=" + isGranted);
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(KEY_SOUND_ALARM_CHANGE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE));
                                values.put(KEY_SOUND_ALARM, Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ALARM));
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }

                    String wallpaper = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_DEVICE_WALLPAPER));
                    if (!wallpaper.isEmpty() && !wallpaper.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER))) {
                        boolean isGranted = false;
                        Uri uri = Uri.parse(wallpaper);
                        if (uri != null) {
                            try {
                                context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                isGranted = true;
                            } catch (Exception e) {
                                //isGranted = false;
                            }
//                            Log.e("*********** DatabaseHandler.afterImportDb", "KEY_DEVICE_WALLPAPER isGranted=" + isGranted);
                        }
                        if (!isGranted) {
                            values.clear();
                            String wallpaperChange = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_DEVICE_WALLPAPER_CHANGE));
                            if (wallpaperChange.equals("1"))
                                values.put(KEY_DEVICE_WALLPAPER_CHANGE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE));
                            values.put(KEY_DEVICE_WALLPAPER, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER));
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});
                        }
                    }
                    String wallpaperFolder = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_DEVICE_WALLPAPER_FOLDER));
                    if (!wallpaperFolder.isEmpty() && !wallpaperFolder.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER))) {
                        boolean isGranted = false;
                        Uri uri = Uri.parse(wallpaperFolder);
                        if (uri != null) {
                            try {
                                context.grantUriPermission(PPApplication.PACKAGE_NAME, uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION /* | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION*/);
                                // persistent permissions
                                final int takeFlags = //data.getFlags() &
                                        (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
                                isGranted = true;
                            } catch (Exception e) {
                                //isGranted = false;
                            }
//                            Log.e("*********** DatabaseHandler.afterImportDb", "KEY_DEVICE_WALLPAPER_FOLDER isGranted=" + isGranted);
                        }
                        if (!isGranted) {
                            values.clear();
                            String wallpaperChange = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_DEVICE_WALLPAPER_CHANGE));
                            if (wallpaperChange.equals("3"))
                                values.put(KEY_DEVICE_WALLPAPER_CHANGE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE));
                            values.put(KEY_DEVICE_WALLPAPER_FOLDER, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER));
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                    new String[]{String.valueOf(profileId)});
                        }
                    }

                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_DURATION_NOTIFICATION_SOUND));
                    if (!tone.isEmpty()) {
                        if (tone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = Uri.parse(tone);
                            if (uri != null) {
                                try {
                                    context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
//                                Log.e("*********** DatabaseHandler.afterImportDb", "KEY_DURATION_NOTIFICATION_SOUND isGranted=" + isGranted);
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(KEY_DURATION_NOTIFICATION_SOUND, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND));
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(profileId)});
                            }
                        }
                    }

                } while (cursorImportDB.moveToNext());
            }
            cursorImportDB.close();
        } finally {
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }

        // set event parameters to "Not used" for non-granted Uri premissions
        try {
            cursorImportDB = db.rawQuery("SELECT " +
                    KEY_E_ID + ","+
                    KEY_E_NOTIFICATION_SOUND_START + "," +
                    KEY_E_NOTIFICATION_SOUND_END +
                    " FROM " + TABLE_EVENTS, null);

            ContentResolver contentResolver = context.getContentResolver();

            if (cursorImportDB.moveToFirst()) {
                do {
                    long eventId = cursorImportDB.getLong(cursorImportDB.getColumnIndexOrThrow(KEY_E_ID));

                    ContentValues values = new ContentValues();

                    String tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_START));
                    if (!tone.isEmpty()) {
                        if (tone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = Uri.parse(tone);
                            if (uri != null) {
                                try {
                                    context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
//                                Log.e("*********** DatabaseHandler.afterImportDb", "KEY_E_NOTIFICATION_SOUND_START isGranted=" + isGranted);
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(KEY_E_NOTIFICATION_SOUND_START, "");
                                db.update(TABLE_EVENTS, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(eventId)});
                            }
                        }
                    }
                    tone = cursorImportDB.getString(cursorImportDB.getColumnIndexOrThrow(KEY_E_NOTIFICATION_SOUND_END));
                    if (!tone.isEmpty()) {
                        if (tone.contains("content://media/external")) {
                            boolean isGranted = false;
                            Uri uri = Uri.parse(tone);
                            if (uri != null) {
                                try {
                                    context.grantUriPermission(PPApplication.PACKAGE_NAME, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    isGranted = true;
                                } catch (Exception e) {
                                    //isGranted = false;
                                }
//                                Log.e("*********** DatabaseHandler.afterImportDb", "KEY_E_NOTIFICATION_SOUND_END isGranted=" + isGranted);
                            }
                            if (!isGranted) {
                                values.clear();
                                values.put(KEY_E_NOTIFICATION_SOUND_END, "");
                                db.update(TABLE_EVENTS, values, KEY_ID + " = ?",
                                        new String[]{String.valueOf(eventId)});
                            }
                        }
                    }

                } while (cursorImportDB.moveToNext());
            }
            cursorImportDB.close();
        } finally {
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }

//        Log.e("*********** DatabaseHandler.afterImportDb", "*** END ***");

    }

    private void importEvents(SQLiteDatabase db, SQLiteDatabase exportedDBObj,
                              List<Long> exportedDBEventProfileIds, List<Long> importDBEventProfileIds) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + TABLE_EVENTS);

            if (tableExists(TABLE_EVENTS, exportedDBObj)) {
                // cursor for events exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_EVENTS, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor for profiles of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_EVENTS, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                if (columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_START) ||
                                        columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_END) ||
                                        columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED) ||
                                        columnNamesExportedDB[i].equals(KEY_E_START_WHEN_ACTIVATED_PROFILE)) {
                                    // imported profile has new id
                                    // map old profile id to new imported id
                                    if (columnNamesExportedDB[i].equals(KEY_E_START_WHEN_ACTIVATED_PROFILE)) {
                                        String fkProfiles = cursorExportedDB.getString(i);
                                        if (!fkProfiles.isEmpty()) {
                                            String[] splits = fkProfiles.split("\\|");
                                            StringBuilder newFkProfiles = new StringBuilder();
                                            for (String split : splits) {
                                                long fkProfile = Long.parseLong(split);
                                                int profileIdx = exportedDBEventProfileIds.indexOf(fkProfile);
                                                if (profileIdx != -1) {
                                                    if (newFkProfiles.length() > 0)
                                                        newFkProfiles.append("|");
                                                    newFkProfiles.append(importDBEventProfileIds.get(profileIdx));
                                                }
                                            }
                                            values.put(columnNamesExportedDB[i], newFkProfiles.toString());
                                        } else
                                            values.put(columnNamesExportedDB[i], "");
                                    } else {
                                        int profileIdx = exportedDBEventProfileIds.indexOf(cursorExportedDB.getLong(i));
                                        if (profileIdx != -1)
                                            values.put(columnNamesExportedDB[i], importDBEventProfileIds.get(profileIdx));
                                        else {
                                            if (columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_END) &&
                                                    (cursorExportedDB.getLong(i) == Profile.PROFILE_NO_ACTIVATE))
                                                values.put(columnNamesExportedDB[i], Profile.PROFILE_NO_ACTIVATE);
                                            else if (columnNamesExportedDB[i].equals(KEY_E_FK_PROFILE_START_WHEN_ACTIVATED) &&
                                                    (cursorExportedDB.getLong(i) == Profile.PROFILE_NO_ACTIVATE))
                                                values.put(columnNamesExportedDB[i], Profile.PROFILE_NO_ACTIVATE);
                                            else
                                                values.put(columnNamesExportedDB[i], 0);
                                        }
                                    }
                                } else
                                    values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(TABLE_EVENTS, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();

            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    private void importActivityLog(/*String applicationDataPath, */SQLiteDatabase db, SQLiteDatabase exportedDBObj/*,
                                   List<Long> exportedDBEventProfileIds, List<Long> importDBEventProfileIds*/) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + TABLE_ACTIVITY_LOG);

            if (tableExists(TABLE_ACTIVITY_LOG, exportedDBObj)) {
                // cursor for events exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_ACTIVITY_LOG, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor for profiles of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_ACTIVITY_LOG, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // for non existent fields set default value
                        if (exportedDBObj.getVersion() < 2409) {
                            values.put(KEY_AL_PROFILE_EVENT_COUNT, "1 [0]");
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(TABLE_ACTIVITY_LOG, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();
            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    private void importGeofences(SQLiteDatabase db, SQLiteDatabase exportedDBObj) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + TABLE_GEOFENCES);

            if (tableExists(TABLE_GEOFENCES, exportedDBObj)) {
                // cursor for events exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_GEOFENCES, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor for profiles of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_GEOFENCES, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(TABLE_GEOFENCES, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();
            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    private void importShortcuts(SQLiteDatabase db, SQLiteDatabase exportedDBObj) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + TABLE_SHORTCUTS);

            if (tableExists(TABLE_SHORTCUTS, exportedDBObj)) {
                // cursor for events exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_SHORTCUTS, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor for profiles of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_SHORTCUTS, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(TABLE_SHORTCUTS, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();
            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    private void importMobileCells(SQLiteDatabase db, SQLiteDatabase exportedDBObj) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + TABLE_MOBILE_CELLS);

            if (tableExists(TABLE_MOBILE_CELLS, exportedDBObj)) {
                // cursor for exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_MOBILE_CELLS, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_MOBILE_CELLS, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(TABLE_MOBILE_CELLS, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();
            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    private void importNFCTags(SQLiteDatabase db, SQLiteDatabase exportedDBObj) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + TABLE_NFC_TAGS);

            if (tableExists(TABLE_NFC_TAGS, exportedDBObj)) {
                // cursor for events exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_NFC_TAGS, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor for profiles of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_NFC_TAGS, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(TABLE_NFC_TAGS, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();
            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    private void importIntents(SQLiteDatabase db, SQLiteDatabase exportedDBObj) {
        Cursor cursorExportedDB = null;
        String[] columnNamesExportedDB;
        Cursor cursorImportDB = null;
        ContentValues values = new ContentValues();

        try {
            db.execSQL("DELETE FROM " + TABLE_INTENTS);

            if (tableExists(TABLE_INTENTS, exportedDBObj)) {
                // cursor for events exportedDB
                cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_INTENTS, null);
                columnNamesExportedDB = cursorExportedDB.getColumnNames();

                // cursor for profiles of destination db
                cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_INTENTS, null);

                if (cursorExportedDB.moveToFirst()) {
                    do {
                        values.clear();
                        for (int i = 0; i < columnNamesExportedDB.length; i++) {
                            // put only when columnNamesExportedDB[i] exists in cursorImportDB
                            if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                            }
                        }

                        // Inserting Row do db z SQLiteOpenHelper
                        db.insert(TABLE_INTENTS, null, values);

                    } while (cursorExportedDB.moveToNext());
                }

                cursorExportedDB.close();
                cursorImportDB.close();
            }
        } finally {
            if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                cursorExportedDB.close();
            if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                cursorImportDB.close();
        }
    }

    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    int importDB(/*String applicationDataPath*/) {
        importExportLock.lock();
        try {
            int ret = IMPORT_ERROR_BUG;
            try {
                startRunningImportExport();

                List<Long> exportedDBEventProfileIds = new ArrayList<>();
                List<Long> importDBEventProfileIds = new ArrayList<>();

                // Close SQLiteOpenHelper so it will commit the created empty
                // database to internal storage
                //close();

                try {
                    //File sd = Environment.getExternalStorageDirectory();
                    File sd = context.getExternalFilesDir(null);

                    //File exportedDB = new File(sd, applicationDataPath + "/" + EXPORT_DBFILENAME);
                    File exportedDB = new File(sd, EXPORT_DBFILENAME);

                    if (exportedDB.exists()) {
                        //PPApplication.logE("DatabaseHandler.importDB", "exportedDB.getAbsolutePath()="+exportedDB.getAbsolutePath());

                        try {
                            //noinspection ResultOfMethodCallIgnored
                            exportedDB.setReadable(true, false);
                        } catch (Exception ee) {
                            PPApplication.recordException(ee);
                        }
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            exportedDB.setWritable(true, false);
                        } catch (Exception ee) {
                            PPApplication.recordException(ee);
                        }

                        SQLiteDatabase exportedDBObj;
                        //if (Build.VERSION.SDK_INT < 27)
                            exportedDBObj = SQLiteDatabase.openDatabase(exportedDB.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
                        /*else {
                            SQLiteDatabase.OpenParams openParams = new SQLiteDatabase.OpenParams.Builder()
                                    .setOpenFlags(SQLiteDatabase.OPEN_READONLY)
                                    .build();
                            exportedDBObj = SQLiteDatabase.openDatabase(exportedDB, openParams);
                        }*/
                        int version;
                        //try {
                            // this will crash when PPP directory is not created by PPP :-(
                            version = exportedDBObj.getVersion();
                        //} catch (Exception ignored) {}

                        if (version <= DATABASE_VERSION) {
                            SQLiteDatabase db = getMyWritableDatabase();

                            try {
                                db.beginTransaction();

                                importProfiles(db, exportedDBObj, exportedDBEventProfileIds, importDBEventProfileIds);
                                importEvents(db, exportedDBObj, exportedDBEventProfileIds, importDBEventProfileIds);
                                importActivityLog(db, exportedDBObj);
                                importGeofences(db, exportedDBObj);
                                importShortcuts(db, exportedDBObj);
                                importMobileCells(db, exportedDBObj);
                                importNFCTags(db, exportedDBObj);
                                importIntents(db, exportedDBObj);

                                updateDb(db, version);

                                afterUpdateDb(db);
                                afterImportDb(db);

                                db.setTransactionSuccessful();

                                ret = IMPORT_OK;
                            } finally {
                                db.endTransaction();
                                //db.close();
                            }
                        } else {
                            //    exportedDBObj.close();
                            ret = IMPORT_ERROR_NEVER_VERSION;
                        }
                    }
                } catch (Exception e1) {
                    //Log.e("DatabaseHandler.importDB", Log.getStackTraceString(e1));
                    //getVersion(): android.database.sqlite.SQLiteCantOpenDatabaseException: unable to open database file (Sqlite code 14), (OS error - 2:No such file or directory)
                    PPApplication.recordException(e1);
                    ret = IMPORT_ERROR_BUG;
                }

            } catch (Exception e2) {
                //Log.e("DatabaseHandler.importDB", Log.getStackTraceString(e2));
                //getVersion(): android.database.sqlite.SQLiteCantOpenDatabaseException: unable to open database file (Sqlite code 14), (OS error - 2:No such file or directory)
                PPApplication.recordException(e2);
                //PPApplication.logE("DatabaseHandler.importDB", Log.getStackTraceString(e2));
            }
            return ret;
        } finally {
            stopRunningImportExport();
        }
    }

    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    int exportDB()
    {
        importExportLock.lock();
        try {
            int ret = 0;
            try {
                startRunningImportExport();

                FileInputStream src = null;
                FileOutputStream dst = null;
                try {
                    try {
                        //File sd = Environment.getExternalStorageDirectory();
                        //File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                        File sd = context.getExternalFilesDir(null);

                        File data = Environment.getDataDirectory();

                        File dataDB = new File(data, PPApplication.DB_FILEPATH + "/" + DATABASE_NAME);
                        //File exportedDB = new File(sd, PPApplication.EXPORT_PATH + "/" + EXPORT_DBFILENAME);
                        File exportedDB = new File(sd, EXPORT_DBFILENAME);

                        if (dataDB.exists()) {
                            // close db
                            close();

                            src = new FileInputStream(dataDB);
                            dst = new FileOutputStream(exportedDB);

                            FileChannel srcCh = new FileInputStream(dataDB).getChannel();
                            FileChannel dstCh = new FileOutputStream(exportedDB).getChannel();

                            srcCh.force(true);
                            dstCh.force(true);

                            boolean ok = false;
                            long transferredSize = dstCh.transferFrom(srcCh, 0, srcCh.size());
                            if (transferredSize == dataDB.length())
                                ok = true;

                            srcCh.close();
                            dstCh.close();

                            dst.flush();

                            src.close();
                            dst.close();

                            try {
                                //noinspection ResultOfMethodCallIgnored
                                exportedDB.setReadable(true, false);
                            } catch (Exception ee) {
                                PPApplication.recordException(ee);
                            }
                            try {
                                //noinspection ResultOfMethodCallIgnored
                                exportedDB.setWritable(true, false);
                            } catch (Exception ee) {
                                PPApplication.recordException(ee);
                            }

                            if (ok)
                                ret = 1;
                        }
                    } catch (Exception e) {
                        //Log.e("DatabaseHandler.exportDB", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    }
                } finally {
                    if (src != null)
                        src.close();
                    if (dst != null)
                        dst.close();
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            return ret;
        } finally {
            stopRunningImportExport();
        }
    }

}
