package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.CheckResult;

class ApplicationPreferences {

    static int prefRingerVolume;
    static int prefNotificationVolume;
    static int prefRingerMode;
    static int prefZenMode;
    static boolean prefLockScreenDisabled;
    static int prefActivatedProfileScreenTimeoutWhenScreenOff;
    static boolean prefMergedRingNotificationVolumes;
    //static long prefActivatedProfileForDuration;
    static long prefActivatedProfileEndDurationTime;
    //static boolean prefShowIgnoreBatteryOptimizationNotificationOnStart;
    //static boolean prefEventsBlocked;
    //static boolean prefForceRunEventRunning;
    static String prefApplicationInForeground;
    static int prefEventCallEventType;
    static long prefEventCallEventTime;
    static String prefEventCallPhoneNumber;
    static int prefEventCallFromSIMSlot;
    static boolean prefWiredHeadsetConnected;
    static boolean prefWiredHeadsetMicrophone;
    static boolean prefBluetoothHeadsetConnected;
    static boolean prefBluetoothHeadsetMicrophone;
    static int prefForceOneWifiScan;
    static int prefForceOneBluetoothScan;
    static int prefForceOneBluetoothLEScan;
    static boolean prefEventBluetoothScanRequest;
    static boolean prefEventBluetoothLEScanRequest;
    static boolean prefEventBluetoothWaitForResult;
    static boolean prefEventBluetoothLEWaitForResult;
    static boolean prefEventBluetoothScanKilled;
    static boolean prefEventBluetoothEnabledForScan;
    static boolean prefEventWifiScanRequest;
    static boolean prefEventWifiWaitForResult;
    static boolean prefEventWifiEnabledForScan;
    //static boolean prefShowCriticalGitHubReleasesNotificationNotification;
    static int prefShowCriticalGitHubReleasesCodeNotification;
    static long prefEventAlarmClockTime;
    static String prefEventAlarmClockPackageName;
    static boolean keepScreenOnPermanent;

    static boolean applicationEventNeverAskForEnableRun;
    static boolean applicationNeverAskForGrantRoot;
    static boolean applicationNeverAskForGrantG1Permission;
    static int editorOrderSelectedItem;
    static int editorSelectedView;
    static int editorProfilesViewSelectedItem;
    static int editorEventsViewSelectedItem;
    static boolean applicationStartOnBoot;
    static boolean applicationActivate;
    static boolean applicationStartEvents;
    static boolean applicationActivateWithAlert;
    static boolean applicationClose;
    static boolean applicationLongClickActivation;
    //static String  applicationLanguage;
    static String applicationTheme;
    //static boolean applicationActivatorPrefIndicator;
    static boolean applicationEditorPrefIndicator;
    //static boolean applicationActivatorHeader;
    //static boolean applicationEditorHeader;
    static boolean notificationsToast = true;
    //static boolean notificationStatusBar;
    //static boolean notificationStatusBarPermanent;
    //static String notificationStatusBarCancel;
    static String notificationStatusBarStyle;
    static boolean notificationShowInStatusBar;
    static String notificationTextColor;
    static boolean notificationHideInLockScreen;
    //static String notificationTheme;
    static boolean applicationWidgetListPrefIndicator;
    static String applicationWidgetListPrefIndicatorLightness;
    static boolean applicationWidgetListHeader;
    static String applicationWidgetListBackground;
    static String applicationWidgetListLightnessB;
    static String applicationWidgetListLightnessT;
    static String applicationWidgetIconColor;
    static String applicationWidgetIconLightness;
    static String applicationWidgetListIconColor;
    static String applicationWidgetListIconLightness;
    //static boolean applicationEditorAutoCloseDrawer;
    //static boolean applicationEditorSaveEditorState;
    static boolean notificationPrefIndicator;
    static String notificationPrefIndicatorLightness;
    static String applicationHomeLauncher;
    static String applicationWidgetLauncher;
    static String applicationNotificationLauncher;
    static int applicationEventWifiScanInterval;
    static long applicationDefaultProfile;
    static String applicationDefaultProfileNotificationSound;
    static boolean applicationDefaultProfileNotificationVibrate;
    //static boolean applicationDefaultProfileUsage;
    static boolean applicationActivatorGridLayout;
    static boolean applicationWidgetListGridLayout;
    static int applicationEventBluetoothScanInterval;
    //static String applicationEventWifiRescan;
    //static String applicationEventBluetoothRescan;
    static boolean applicationWidgetIconHideProfileName;
    static boolean applicationShortcutEmblem;
    static String applicationEventWifiScanInPowerSaveMode;
    static String applicationEventBluetoothScanInPowerSaveMode;
    //static String applicationPowerSaveModeInternal;
    static int applicationEventBluetoothLEScanDuration;
    static int applicationEventLocationUpdateInterval;
    static String applicationEventLocationUpdateInPowerSaveMode;
    static boolean  applicationEventLocationUseGPS;
    //static String  applicationEventLocationRescan;
    static int applicationEventOrientationScanInterval;
    static String applicationEventOrientationScanInPowerSaveMode;
    static String applicationEventMobileCellsScanInPowerSaveMode;
    //static String applicationEventMobileCellsRescan;
    static int applicationDeleteOldActivityLogs;
    static String applicationWidgetIconBackground;
    static String applicationWidgetIconLightnessB;
    static String applicationWidgetIconLightnessT;
    static boolean applicationEventUsePriority;
    static boolean applicationUnlinkRingerNotificationVolumes;
    static int applicationForceSetMergeRingNotificationVolumes;
    //static boolean applicationSamsungEdgePrefIndicator;
    static boolean applicationSamsungEdgeHeader;
    static String applicationSamsungEdgeBackground;
    static String applicationSamsungEdgeLightnessB;
    static String applicationSamsungEdgeLightnessT;
    static String applicationSamsungEdgeIconColor;
    static String applicationSamsungEdgeIconLightness;
    //static boolean applicationSamsungEdgeGridLayout;
    static boolean applicationEventLocationScanOnlyWhenScreenIsOn;
    static boolean applicationEventWifiScanOnlyWhenScreenIsOn;
    static boolean applicationEventBluetoothScanOnlyWhenScreenIsOn;
    static boolean applicationEventMobileCellScanOnlyWhenScreenIsOn;
    static boolean  applicationEventOrientationScanOnlyWhenScreenIsOn;
    static boolean applicationRestartEventsWithAlert;
    static boolean applicationWidgetListRoundedCorners;
    static boolean applicationWidgetIconRoundedCorners;
    static boolean applicationWidgetListBackgroundType;
    static String applicationWidgetListBackgroundColor;
    static boolean applicationWidgetIconBackgroundType;
    static String applicationWidgetIconBackgroundColor;
    static boolean applicationSamsungEdgeBackgroundType;
    static String applicationSamsungEdgeBackgroundColor;
    //static boolean applicationEventWifiEnableWifi;
    //static boolean applicationEventBluetoothEnableBluetooth;
    static boolean applicationEventWifiScanIfWifiOff;
    static boolean applicationEventBluetoothScanIfBluetoothOff;
    static boolean applicationEventWifiEnableScanning;
    static boolean applicationEventBluetoothEnableScanning;
    static boolean applicationEventLocationEnableScanning;
    static boolean applicationEventMobileCellEnableScanning;
    static boolean applicationEventOrientationEnableScanning;
    static boolean applicationEventWifiDisabledScannigByProfile;
    static boolean applicationEventBluetoothDisabledScannigByProfile;
    static boolean applicationEventLocationDisabledScannigByProfile;
    static boolean applicationEventMobileCellDisabledScannigByProfile;
    static boolean applicationEventOrientationDisabledScannigByProfile;
    static boolean applicationEventNotificationDisabledScannigByProfile;
    static boolean applicationUseAlarmClock;
    static boolean notificationShowButtonExit;
    static boolean applicationWidgetOneRowPrefIndicator;
    static String applicationWidgetOneRowPrefIndicatorLightness;
    static String applicationWidgetOneRowBackground;
    static String applicationWidgetOneRowLightnessB;
    static String applicationWidgetOneRowLightnessT;
    static String applicationWidgetOneRowIconColor;
    static String applicationWidgetOneRowIconLightness;
    static boolean applicationWidgetOneRowRoundedCorners;
    static boolean applicationWidgetOneRowBackgroundType;
    static String  applicationWidgetOneRowBackgroundColor;
    static String applicationWidgetListLightnessBorder;
    static String applicationWidgetOneRowLightnessBorder;
    static String applicationWidgetIconLightnessBorder;
    static boolean applicationWidgetListShowBorder;
    static boolean applicationWidgetOneRowShowBorder;
    static boolean applicationWidgetIconShowBorder;
    static boolean  applicationWidgetListCustomIconLightness;
    static boolean applicationWidgetOneRowCustomIconLightness;
    static boolean applicationWidgetIconCustomIconLightness;
    static boolean applicationSamsungEdgeCustomIconLightness;
    //static boolean notificationDarkBackground;
    static boolean notificationUseDecoration;
    static String notificationLayoutType;
    static String notificationBackgroundColor;
    //static String applicationNightModeOffTheme;
    static boolean applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled;
    static String applicationSamsungEdgeVerticalPosition;
    static int notificationBackgroundCustomColor;
    //static boolean notificationNightMode;
    static boolean applicationEditorHideHeaderOrBottomBar;
    static boolean applicationWidgetIconShowProfileDuration;
    static String notificationNotificationStyle;
    static boolean notificationShowProfileIcon;
    static boolean applicationEventPeriodicScanningEnableScanning;
    static int applicationEventPeriodicScanningScanInterval;
    static String applicationEventPeriodicScanningScanInPowerSaveMode;
    static boolean applicationEventPeriodicScanningScanOnlyWhenScreenIsOn;
    static boolean applicationEventWifiScanIgnoreHotspot;
    static boolean applicationEventNotificationEnableScanning;
    static String applicationEventNotificationScanInPowerSaveMode;
    static boolean applicationEventNotificationScanOnlyWhenScreenIsOn;
    static int applicationWidgetOneRowRoundedCornersRadius;
    static int applicationWidgetListRoundedCornersRadius;
    static int applicationWidgetIconRoundedCornersRadius;
    static String applicationActivatorNumColums;
    static String applicationApplicationInterfaceNotificationSound;
    static boolean applicationApplicationInterfaceNotificationVibrate;
    static boolean applicationActivatorAddRestartEventsIntoProfileList;
    static boolean applicationActivatorIncreaseBrightness;
    //static boolean applicationWidgetOneRowHigherLayout;
    static String applicationWidgetOneRowLayoutHeight;
    static boolean applicationWidgetIconChangeColorsByNightMode;
    static boolean applicationWidgetOneRowChangeColorsByNightMode;
    static boolean applicationWidgetListChangeColorsByNightMode;
    static boolean applicationSamsungEdgeChangeColorsByNightMode;
    static boolean applicationForceSetBrightnessAtScreenOn;
    static boolean notificationShowRestartEventsAsButton;
    static String notificationProfileIconColor;
    static String notificationProfileIconLightness;
    static boolean notificationCustomProfileIconLightness;
    static String applicationShortcutIconColor;
    static String applicationShortcutIconLightness;
    static boolean applicationShortcutCustomIconLightness;
    static boolean applicationEventPeriodicScanningDisabledScannigByProfile;
    static boolean applicationWidgetIconUseDynamicColors;
    static boolean applicationWidgetOneRowUseDynamicColors;
    static boolean applicationWidgetListUseDynamicColors;

    static String applicationEventPeriodicScanningScanInTimeMultiply;
    static int applicationEventPeriodicScanningScanInTimeMultiplyFrom;
    static int applicationEventPeriodicScanningScanInTimeMultiplyTo;
    static String applicationEventBluetoothScanInTimeMultiply;
    static int applicationEventBluetoothScanInTimeMultiplyFrom;
    static int applicationEventBluetoothScanInTimeMultiplyTo;
    static String applicationEventLocationScanInTimeMultiply;
    static int applicationEventLocationScanInTimeMultiplyFrom;
    static int applicationEventLocationScanInTimeMultiplyTo;
    static String applicationEventMobileCellScanInTimeMultiply;
    static int applicationEventMobileCellScanInTimeMultiplyFrom;
    static int applicationEventMobileCellScanInTimeMultiplyTo;
    static String applicationEventNotificationScanInTimeMultiply;
    static int applicationEventNotificationScanInTimeMultiplyFrom;
    static int applicationEventNotificationScanInTimeMultiplyTo;
    static String applicationEventOrientationScanInTimeMultiply;
    static int applicationEventOrientationScanInTimeMultiplyFrom;
    static int applicationEventOrientationScanInTimeMultiplyTo;
    static String applicationEventWifiScanInTimeMultiply;
    static int applicationEventWifiScanInTimeMultiplyFrom;
    static int applicationEventWifiScanInTimeMultiplyTo;

    static boolean prefActivatorActivityStartTargetHelps;
    static boolean prefActivatorActivityStartTargetHelpsFinished;
    static boolean prefActivatorFragmentStartTargetHelps;
    static boolean prefActivatorFragmentStartTargetHelpsFinished;
    static boolean prefActivatorAdapterStartTargetHelps;
    @SuppressWarnings("unused")
    static boolean prefActivatorAdapterStartTargetHelpsFinished;

    static boolean prefEditorActivityStartTargetHelps;
    static boolean prefEditorActivityStartTargetHelpsRunStopIndicator;
    static boolean prefEditorActivityStartTargetHelpsBottomNavigation;
    static boolean prefEditorActivityStartTargetHelpsFinished;

    static boolean prefEditorFragmentStartTargetHelpsDefaultProfile;
    static boolean prefEditorProfilesFragmentStartTargetHelps;
    static boolean prefEditorProfilesFragmentStartTargetHelpsFilterSpinner;
    static boolean prefEditorProfilesFragmentStartTargetHelpsFinished;
    static boolean prefEditorProfilesAdapterStartTargetHelps;
    static boolean prefEditorProfilesAdapterStartTargetHelpsOrder;
    static boolean prefEditorProfilesAdapterStartTargetHelpsShowInActivator;
    @SuppressWarnings("unused")
    static boolean prefEditorProfilesAdapterStartTargetHelpsFinished;

    static boolean prefEditorEventsFragmentStartTargetHelps;
    static boolean prefEditorEventsFragmentStartTargetHelpsFilterSpinner;
    static boolean prefEditorEventsFragmentStartTargetHelpsOrderSpinner;
    static boolean prefEditorEventsFragmentStartTargetHelpsFinished;
    static boolean prefEditorEventsAdapterStartTargetHelps;
    static boolean prefEditorEventsAdapterStartTargetHelpsOrder;
    static boolean prefEditorEventsAdapterStartTargetHelpsStatus;
    @SuppressWarnings("unused")
    static boolean prefEditorEventsAdapterStartTargetHelpsFinished;

    static boolean prefProfilePrefsActivityStartTargetHelps;
    //static boolean prefProfilePrefsActivityStartTargetHelpsSave;
    @SuppressWarnings("unused")
    static boolean prefProfilePrefsActivityStartTargetHelpsFinished;

    static boolean prefEventPrefsActivityStartTargetHelps;
    @SuppressWarnings("unused")
    static boolean prefEventPrefsActivityStartTargetHelpsFinished;

    private static SharedPreferences preferences = null;

    //static final String PREF_APPLICATION_PACKAGE_REPLACED = "applicationPackageReplaced";
    static final String PREF_APPLICATION_FIRST_START = "applicationFirstStart";
    static final String PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN = "applicationEventNeverAskForEnableRun";
    static final String PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT = "applicationNeverAskForGrantRoot";
    static final String PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION = "applicationNeverAskForGrantG1Permission";

    static final String PREF_EDITOR_PROFILES_FIRST_START = "editorProfilesFirstStart";
    static final String PREF_EDITOR_EVENTS_FIRST_START = "editorEventsFirstStart";

    static final String EDITOR_ORDER_SELECTED_ITEM = "editor_order_selected_item";
    static final String EDITOR_SELECTED_VIEW = "editor_selected_view";
    static final String EDITOR_PROFILES_VIEW_SELECTED_ITEM = "editor_profiles_view_selected_item";
    static final String EDITOR_EVENTS_VIEW_SELECTED_ITEM = "editor_events_view_selected_item";

    static final String PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI = "applicationEventWifiEnableWifi";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH = "applicationEventBluetoothEnableBluetooth";
    static final String PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE = "applicationEventWifiDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE = "applicationEventBluetoothDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE = "applicationEventLocationDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE = "applicationEventMobileCellDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE = "applicationEventOrientationDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_NOTIFICATION_DISABLED_SCANNING_BY_PROFILE = "applicationEventNotificationDisabledScannigByProfile";
    static final String PREF_APPLICATION_EVENT_PERIODIC_SCANNING_DISABLED_SCANNING_BY_PROFILE = "applicationEventPeriodicScanningDisabledScannigByProfile";

    static final String PREF_APPLICATION_START_ON_BOOT = "applicationStartOnBoot";
    static final String PREF_APPLICATION_ACTIVATE = "applicationActivate";
    static final String PREF_APPLICATION_START_EVENTS = "applicationStartEvents";
    static final String PREF_APPLICATION_ALERT = "applicationAlert";
    static final String PREF_APPLICATION_CLOSE = "applicationClose";
    static final String PREF_APPLICATION_LONG_PRESS_ACTIVATION = "applicationLongClickActivation";
    //static final String PREF_APPLICATION_LANGUAGE = "applicationLanguage";
    static final String PREF_APPLICATION_THEME = "applicationTheme";
    //static final String PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR = "applicationActivatorPrefIndicator";
    static final String PREF_APPLICATION_EDITOR_PREF_INDICATOR = "applicationEditorPrefIndicator";
    //static final String PREF_APPLICATION_ACTIVATOR_HEADER = "applicationActivatorHeader";
    //static final String PREF_APPLICATION_EDITOR_HEADER = "applicationEditorHeader";
    static final String PREF_NOTIFICATION_TOAST = "notificationsToast";
    static final String PREF_NOTIFICATION_STATUS_BAR = "notificationStatusBar";
    static final String PREF_NOTIFICATION_STATUS_BAR_STYLE = "notificationStatusBarStyle";
    static final String PREF_NOTIFICATION_STATUS_BAR_PERMANENT = "notificationStatusBarPermanent";
    //static final String PREF_NOTIFICATION_STATUS_BAR_CANCEL  = "notificationStatusBarCancel";
    static final String PREF_NOTIFICATION_SHOW_IN_STATUS_BAR = "notificationShowInStatusBar";
    static final String PREF_NOTIFICATION_TEXT_COLOR = "notificationTextColor";
    static final String PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR = "applicationWidgetListPrefIndicator";
    static final String PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS = "applicationWidgetListPrefIndicatorLightness";
    static final String PREF_APPLICATION_WIDGET_LIST_HEADER = "applicationWidgetListHeader";
    static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND = "applicationWidgetListBackground";
    static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B = "applicationWidgetListLightnessB";
    static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T = "applicationWidgetListLightnessT";
    static final String PREF_APPLICATION_WIDGET_ICON_COLOR = "applicationWidgetIconColor";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS = "applicationWidgetIconLightness";
    static final String PREF_APPLICATION_WIDGET_LIST_ICON_COLOR = "applicationWidgetListIconColor";
    static final String PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS = "applicationWidgetListIconLightness";
    //static final String PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER = "applicationEditorAutoCloseDrawer";
    //static final String PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE = "applicationEditorSaveEditorState";
    static final String PREF_NOTIFICATION_PREF_INDICATOR = "notificationPrefIndicator";
    static final String PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS = "notificationPrefIndicatorLightness";
    static final String PREF_APPLICATION_HOME_LAUNCHER = "applicationHomeLauncher";
    static final String PREF_APPLICATION_WIDGET_LAUNCHER = "applicationWidgetLauncher";
    static final String PREF_APPLICATION_NOTIFICATION_LAUNCHER = "applicationNotificationLauncher";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL = "applicationEventWifiScanInterval";
    static final String PREF_APPLICATION_DEFAULT_PROFILE = "applicationBackgroundProfile";
    static final String PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND = "applicationBackgroundProfileNotificationSound";
    static final String PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE = "applicationBackgroundProfileNotificationVibrate";
    static final String PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT = "applicationActivatorGridLayout";
    static final String PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT = "applicationWidgetListGridLayout";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL = "applicationEventBluetoothScanInterval";
    //static final String PREF_APPLICATION_EVENT_WIFI_RESCAN = "applicationEventWifiRescan";
    //static final String PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN = "applicationEventBluetoothRescan";
    static final String PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME = "applicationWidgetIconHideProfileName";
    static final String PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES = "applicationUnlinkRingerNotificationVolumes";
    //static final String PREF_APPLICATION_RINGER_NOTIFICATION_VOLUMES_UNLINKED_INFO = "applicationRingerNotificationVolumesUnlinkedInfo";
    static final String PREF_APPLICATION_SHORTCUT_EMBLEM = "applicationShortcutEmblem";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE = "applicationEventWifiScanInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE = "applicationEventBluetoothScanInPowerSaveMode";
    //static final String PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION = "applicationEventBluetoothLEScanDuration";
    static final String PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL = "applicationEventLocationUpdateInterval";
    static final String PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE = "applicationEventLocationUpdateInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_LOCATION_USE_GPS = "applicationEventLocationUseGPS";
    //static final String PREF_APPLICATION_EVENT_LOCATION_RESCAN = "applicationEventLocationRescan";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL = "applicationEventOrientationScanInterval";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE = "applicationEventOrientationScanInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE = "applicationEventMobileCellScanInPowerSaveMode";
    //static final String PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN = "applicationEventMobileCellsRescan";
    static final String PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN = "notificationHideInLockscreen";
    static final String PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS = "applicationDeleteOldActivityLogs";
    static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND = "applicationWidgetIconBackground";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B = "applicationWidgetIconLightnessB";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T = "applicationWidgetIconLightnessT";
    static final String PREF_APPLICATION_EVENT_USE_PRIORITY = "applicationEventUsePriority";
    //static final String PREF_NOTIFICATION_THEME = "notificationTheme";
    static final String PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES = "applicationForceSetMergeRingNotificationVolumes";
    //private static final String PREF_APPLICATION_SAMSUNG_EDGE_PREF_INDICATOR = "applicationSamsungEdgePrefIndicator";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_HEADER = "applicationSamsungEdgeHeader";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND = "applicationSamsungEdgeBackground";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B = "applicationSamsungEdgeLightnessB";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T = "applicationSamsungEdgeLightnessT";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR = "applicationSamsungEdgeIconColor";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS = "applicationSamsungEdgeIconLightness";
    //private static final String PREF_APPLICATION_SAMSUNG_EDGE_GRID_LAYOUT= "applicationSamsungEdgeGridLayout";
    static final String PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventLocationScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventWifiScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventBluetoothScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventMobileCellScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventOrientationScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_RESTART_EVENTS_ALERT = "applicationRestartEventsAlert";
    static final String PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS = "applicationWidgetListRoundedCorners";
    static final String PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS = "applicationWidgetIconRoundedCorners";
    static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE = "applicationWidgetListBackgroundType";
    static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR = "applicationWidgetListBackgroundColor";
    static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE = "applicationWidgetIconBackgroundType";
    static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR = "applicationWidgetIconBackgroundColor";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE = "applicationSamsungEdgeBackgroundType";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR = "applicationSamsungEdgeBackgroundColor";
    static final String PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING = "applicationEventWifiEnableScannig";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF = "applicationEventWifiScanIfWifiOff";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING = "applicationEventBluetoothEnableScannig";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF = "applicationEventBluetoothScanIfBluetoothOff";
    static final String PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING = "applicationEventLocationEnableScannig";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING = "applicationEventMobileCellEnableScannig";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING = "applicationEventOrientationEnableScannig";
    static final String PREF_APPLICATION_USE_ALARM_CLOCK = "applicationUseAlarmClock";
    static final String PREF_NOTIFICATION_SHOW_BUTTON_EXIT = "notificationShowButtonExit";
    //static final String PREF_APPLICATION_DEFAULT_PROFILE_USAGE = "applicationBackgroundProfileUsage";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR = "applicationWidgetOneRowPrefIndicator";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS = "applicationWidgetOneRowPrefIndicatorLightness";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND = "applicationWidgetOneRowBackground";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B = "applicationWidgetOneRowLightnessB";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T = "applicationWidgetOneRowLightnessT";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR = "applicationWidgetOneRowIconColor";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS = "applicationWidgetOneRowIconLightness";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS = "applicationWidgetOneRowRoundedCorners";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE = "applicationWidgetOneRowBackgroundType";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR = "applicationWidgetOneRowBackgroundColor";
    static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER = "applicationWidgetListLightnessBorder";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER = "applicationWidgetOneRowLightnessBorder";
    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER = "applicationWidgetIconLightnessBorder";
    static final String PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER = "applicationWidgetListShowBorder";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER = "applicationWidgetOneRowShowBorder";
    static final String PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER = "applicationWidgetIconShowBorder";
    //static final String PREF_APPLICATION_NOT_DARK_THEME = "applicationNotDarkTheme";
    static final String PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS = "applicationWidgetListCustomIconLightness";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS = "applicationWidgetOneRowCustomIconLightness";
    static final String PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS = "applicationWidgetIconCustomIconLightness";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS = "applicationSamsungEdgeCustomIconLightness";
    //static final String PREF_NOTIFICATION_DARK_BACKGROUND = "notificationDarkBackground";
    static final String PREF_NOTIFICATION_USE_DECORATION = "notificationUseDecoration";
    static final String PREF_NOTIFICATION_LAYOUT_TYPE = "notificationLayoutType";
    static final String PREF_NOTIFICATION_BACKGROUND_COLOR = "notificationBackgroundColor";
    //static final String PREF_APPLICATION_NIGHT_MODE_OFF_THEME = "applicationNightModeOffTheme";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED = "applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION = "applicationSamsungEdgeVerticalPosition";
    static final String PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR = "notificationBackgroundCustomColor";
    //static final String PREF_NOTIFICATION_NIGHT_MODE = "notificationNightMode";
    static final String PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR = "applicationEditorHideHeaderOrBottomBar";
    static final String PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION = "applicationWidgetIconShowProfileDuration";
    static final String PREF_NOTIFICATION_NOTIFICATION_STYLE = "notificationNotificationStyle";
    static final String PREF_NOTIFICATION_SHOW_PROFILE_ICON = "notificationShowProfileIcon";
    static final String PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING = "applicationEventPeriodicScanningEnableScannig";
    static final String PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL = "applicationEventPeriodicScanningScanInterval";
    static final String PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_POWER_SAVE_MODE = "applicationEventPeriodicScanningScanInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventPeriodicScanningScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT = "applicationEventWifiScanIgnoreHotspot";
    static final String PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING = "applicationEventNotificationEnableScannig";
    static final String PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_POWER_SAVE_MODE = "applicationEventNotificationScanInPowerSaveMode";
    static final String PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_ONLY_WHEN_SCREEN_IS_ON = "applicationEventNotificationScanOnlyWhenScreenIsOn";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS = "applicationWidgetOneRowRoundedCornersRadius";
    static final String PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS = "applicationWidgetListRoundedCornersRadius";
    static final String PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS = "applicationWidgetIconRoundedCornersRadius";
    static final String PREF_APPLICATION_ACTIVATOR_NUM_COLUMNS = "applicationActivatorNumColums";
    static final String PREF_APPLICATION_APPLICATION_INTERFACE_NOTIFICATION_SOUND = "applicationApplicationInterfaceNotificationSound";
    static final String PREF_APPLICATION_APPLICATION_INTERFACE_NOTIFICATION_VIBRATE = "applicationApplicationInterfaceNotificationVibrate";
    static final String PREF_APPLICATION_ACTIVATOR_ADD_RESTART_EVENTS_INTO_PROFILE_LIST = "applicationActivatorAddRestartEventsIntoProfileList";
    static final String PREF_APPLICATION_ACTIVATOR_INCREASE_BRIGHTNESS = "applicationActivatorIncreaseBrightness";
    static final String PREF_APPLICATION_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON = "applicationForceSetBrightnessAtScreenOn";
    static final String PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON = "notificationShowRestartEventsAsButton";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LAYOUT_HEIGHT = "applicationWidgetOneRowLayoutHeight";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_HIGHER_LAYOUT = "applicationWidgetOneRowHigherLayout";
    static final String PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE = "applicationWidgetIconChangeColorsByNightMode";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE = "applicationWidgetOneRowChangeColorsByNightMode";
    static final String PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE = "applicationWidgetListChangeColorsByNightMode";
    static final String PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE = "applicationSamsungEdgeChangeColorsByNightMode";
    static final String PREF_NOTIFICATION_PROFILE_ICON_COLOR = "notificationProfileIconColor";
    static final String PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS = "notificationProfileIconLightness";
    static final String PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS = "notificationCustomProfileIconLightness";
    static final String PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS = "applicationWidgetIconUseDynamicColors";
    static final String PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS = "applicationWidgetOneRowUseDynamicColors";
    static final String PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS = "applicationWidgetListUseDynamicColors";

    static final String PREF_APPLICATION_SHORTCUT_ICON_COLOR = "applicationShortcutIconColor";
    static final String PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS = "applicationShortcutIconLightness";
    static final String PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS = "applicationShortcutCustomIconLightness";

    static final String PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY = "applicationEventPeriodicScanningScanInTimeMultiply";
    static final String PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_FROM = "applicationEventPeriodicScanningScanInTimeMultiplyFrom";
    static final String PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_TO = "applicationEventPeriodicScanningScanInTimeMultiplyTo";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY = "applicationEventBluetoothScanInTimeMultiply";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_FROM = "applicationEventBluetoothScanInTimeMultiplyFrom";
    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_TO = "applicationEventBluetoothScanInTimeMultiplyTo";
    static final String PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY = "applicationEventLocationScanInTimeMultiply";
    static final String PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_FROM = "applicationEventLocationScanInTimeMultiplyFrom";
    static final String PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_TO = "applicationEventLocationScanInTimeMultiplyTo";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY = "applicationEventMobileCellScanInTimeMultiply";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_FROM = "applicationEventMobileCellScanInTimeMultiplyFrom";
    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_TO = "applicationEventMobileCellScanInTimeMultiplyTo";
    static final String PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY = "applicationEventNotificationScanInTimeMultiply";
    static final String PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_FROM = "applicationEventNotificationScanInTimeMultiplyFrom";
    static final String PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_TO = "applicationEventNotificationScanInTimeMultiplyTo";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY = "applicationEventOrientationScanInTimeMultiply";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_FROM = "applicationEventOrientationScanInTimeMultiplyFrom";
    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_TO = "applicationEventOrientationScanInTimeMultiplyTo";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY = "applicationEventWifiScanInTimeMultiply";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_FROM = "applicationEventWifiScanInTimeMultiplyFrom";
    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_TO = "applicationEventWifiScanInTimeMultiplyTo";

    static final String PREF_QUICK_TILE_PROFILE_ID = "quickTileProfileId";

    @CheckResult
    static SharedPreferences getSharedPreferences(Context context) {
        if (preferences == null)
            preferences = context.getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences;
    }

    @CheckResult
    static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    /*
    static boolean applicationPackageReplaced(Context context) {
        return ApplicationPreferences.getSharedPreferences(context).getBoolean(ApplicationPreferences.PREF_APPLICATION_PACKAGE_REPLACED, false);
    }
    */
    /*
    static boolean applicationFirstStart(Context context) {
        return ApplicationPreferences.getSharedPreferences(context).getBoolean(ApplicationPreferences.PREF_APPLICATION_FIRST_START, true);
    }
    */

    static String applicationTheme(Context context, boolean useNightMode) {
        synchronized (PPApplication.applicationPreferencesMutex) {
            if (applicationTheme == null)
                applicationTheme(context);
            String _applicationTheme = applicationTheme;
            if (_applicationTheme.equals("light") ||
                    _applicationTheme.equals("material") ||
                    _applicationTheme.equals("color") ||
                    _applicationTheme.equals("dlight")) {
                String defaultValue = "white";
                if (Build.VERSION.SDK_INT >= 28)
                    defaultValue = "night_mode";
                _applicationTheme = defaultValue;
                SharedPreferences.Editor editor = ApplicationPreferences.getSharedPreferences(context).edit();
                editor.putString(ApplicationPreferences.PREF_APPLICATION_THEME, _applicationTheme);
                editor.apply();
                applicationTheme = _applicationTheme;
            }
            if (_applicationTheme.equals("night_mode") && useNightMode) {
                int nightModeFlags =
                        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        _applicationTheme = "dark";
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                        _applicationTheme = "white";
                        break;
                }
            }
            return _applicationTheme;
        }
    }

    static final boolean PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN_DEFAULT_VALUE = false;
    static void applicationEventNeverAskForEnableRun(Context context) {
        applicationEventNeverAskForEnableRun = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN, PREF_APPLICATION_EVENT_NEVER_ASK_FOR_ENABLE_RUN_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT_DEFAULT_VALUE = false;
    static void applicationNeverAskForGrantRoot(Context context) {
        applicationNeverAskForGrantRoot = getSharedPreferences(context).getBoolean(PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT, PREF_APPLICATION_NEVER_ASK_FOR_GRANT_ROOT_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION_DEFAULT_VALUE = false;
    static void applicationNeverAskForGrantG1Permission(Context context) {
        applicationNeverAskForGrantG1Permission = getSharedPreferences(context).getBoolean(PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION, PREF_APPLICATION_NEVER_ASK_FOR_GRANT_G1_PERMISSION_DEFAULT_VALUE);
    }

    static final int EDITOR_ORDER_SELECTED_ITEM_DEFAULT_VALUE = 0;
    static void editorOrderSelectedItem(Context context) {
        editorOrderSelectedItem = getSharedPreferences(context).getInt(EDITOR_ORDER_SELECTED_ITEM, EDITOR_ORDER_SELECTED_ITEM_DEFAULT_VALUE);
    }

    static final int EDITOR_SELECTED_VIEW_DEFAULT_VALUE = 0;
    static void editorSelectedView(Context context) {
        editorSelectedView = getSharedPreferences(context).getInt(EDITOR_SELECTED_VIEW, EDITOR_SELECTED_VIEW_DEFAULT_VALUE);
    }

    static final int EDITOR_PROFILES_VIEW_SELECTED_ITEM_DEFAULT_VALUE = 0;
    static void editorProfilesViewSelectedItem(Context context) {
        editorProfilesViewSelectedItem = getSharedPreferences(context).getInt(EDITOR_PROFILES_VIEW_SELECTED_ITEM, EDITOR_PROFILES_VIEW_SELECTED_ITEM_DEFAULT_VALUE);
    }

    static final int EDITOR_EVENTS_VIEW_SELECTED_ITEM_DEFAULT_VALUE = 0;
    static void editorEventsViewSelectedItem(Context context) {
        editorEventsViewSelectedItem = getSharedPreferences(context).getInt(EDITOR_EVENTS_VIEW_SELECTED_ITEM, EDITOR_EVENTS_VIEW_SELECTED_ITEM_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_START_ON_BOOT_DEFAULT_VALUE = true;
    static void applicationStartOnBoot(Context context) {
        applicationStartOnBoot = getSharedPreferences(context).getBoolean(PREF_APPLICATION_START_ON_BOOT, PREF_APPLICATION_START_ON_BOOT_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_ACTIVATE_DEFAULT_VALUE = true;
    static void applicationActivate(Context context) {
        applicationActivate = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATE, PREF_APPLICATION_ACTIVATE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_START_EVENTS_DEFAULT_VALUE = true;
    static void applicationStartEvents(Context context) {
        applicationStartEvents = getSharedPreferences(context).getBoolean(PREF_APPLICATION_START_EVENTS, PREF_APPLICATION_START_EVENTS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_ALERT_DEFAULT_VALUE = true;
    static void applicationActivateWithAlert(Context context) {
        applicationActivateWithAlert = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ALERT, PREF_APPLICATION_ALERT_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_CLOSE_DEFAULT_VALUE = true;
    static void applicationClose(Context context) {
        applicationClose = getSharedPreferences(context).getBoolean(PREF_APPLICATION_CLOSE, PREF_APPLICATION_CLOSE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_LONG_PRESS_ACTIVATION_DEFAULT_VALUE = false;
    static void applicationLongClickActivation(Context context) {
        applicationLongClickActivation = getSharedPreferences(context).getBoolean(PREF_APPLICATION_LONG_PRESS_ACTIVATION, PREF_APPLICATION_LONG_PRESS_ACTIVATION_DEFAULT_VALUE);
    }

    /*
    static String applicationLanguage(Context context) {
         applicationLanguage = getSharedPreferences(context).getString(PREF_APPLICATION_LANGUAGE, "system");
         return  applicationLanguage;
    }
    */

    static private final String PREF_APPLICATION_THEME_DEFAULT_VALUE_WHITE = "white";
    static private final String PREF_APPLICATION_THEME_DEFAULT_VALUE_NIGHT_MODE = "night_mode";
    static String applicationThemeDefaultValue() {
        String defaultValue = PREF_APPLICATION_THEME_DEFAULT_VALUE_WHITE;
        if (Build.VERSION.SDK_INT >= 28)
            defaultValue = PREF_APPLICATION_THEME_DEFAULT_VALUE_NIGHT_MODE;
        return defaultValue;
    }
    static void applicationTheme(Context context) {
        applicationTheme = getSharedPreferences(context).getString(PREF_APPLICATION_THEME, applicationThemeDefaultValue());
    }

    /*
    static boolean applicationActivatorPrefIndicator(Context context) {
        applicationActivatorPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR, true);
        return applicationActivatorPrefIndicator;
    }
    */

    static final boolean PREF_APPLICATION_EDITOR_PREF_INDICATOR_DEFAULT_VALUE = true;
    static void applicationEditorPrefIndicator(Context context) {
        applicationEditorPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_PREF_INDICATOR, PREF_APPLICATION_EDITOR_PREF_INDICATOR_DEFAULT_VALUE);
    }

    /*
    static boolean applicationActivatorHeader(Context context) {
        applicationActivatorHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_HEADER, true);
        return applicationActivatorHeader;
    }

    static boolean applicationEditorHeader(Context context) {
        applicationEditorHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_HEADER, true);
        return applicationEditorHeader;
    }
    */

    static final boolean PREF_NOTIFICATION_TOAST_DEFAULT_VALUE = true;
    static void notificationsToast(Context context) {
        notificationsToast = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_TOAST, PREF_NOTIFICATION_TOAST_DEFAULT_VALUE);
    }

    /*
    static boolean notificationStatusBar(Context context) {
        notificationStatusBar = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_STATUS_BAR, true);
        return notificationStatusBar;
    }

    static boolean notificationStatusBarPermanent(Context context) {
        notificationStatusBarPermanent = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_STATUS_BAR_PERMANENT, true);
        return notificationStatusBarPermanent;
    }

    static String notificationStatusBarCancel(Context context) {
        notificationStatusBarCancel = getSharedPreferences(context).getString(PREF_NOTIFICATION_STATUS_BAR_CANCEL, "10");
        return notificationStatusBarCancel;
    }
    */

    static private final String PREF_NOTIFICATION_STATUS_BAR_STYLE_DEFAULT_VALUE_OTHERS = "1"; // android
    static private final String PREF_NOTIFICATION_STATUS_BAR_STYLE_DEFAULT_VALUE_PIXEL = "1";  // android
    static String notificationStatusBarStyleDefaultValue() {
        String defaultValue;
        if (PPApplication.deviceIsPixel && (Build.VERSION.SDK_INT >= 31))
            defaultValue = PREF_NOTIFICATION_STATUS_BAR_STYLE_DEFAULT_VALUE_PIXEL;
        else
            defaultValue = PREF_NOTIFICATION_STATUS_BAR_STYLE_DEFAULT_VALUE_OTHERS;
        return defaultValue;
    }
    static void notificationStatusBarStyle(Context context) {
        notificationStatusBarStyle = getSharedPreferences(context).getString(PREF_NOTIFICATION_STATUS_BAR_STYLE, notificationStatusBarStyleDefaultValue());
        // Native (1) is OK, becuse in Pixel 5 with Android 12, Colorful (0) not working, icon is not displayed.
        // But by me, it is bug in Pixel 5, because in my Pixel 3a working also Colorful.
        if (PPApplication.deviceIsPixel && (Build.VERSION.SDK_INT >= 31) &&
                notificationStatusBarStyle.equals("0")) {
            SharedPreferences prefs = getSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_NOTIFICATION_STATUS_BAR_STYLE, PREF_NOTIFICATION_STATUS_BAR_STYLE_DEFAULT_VALUE_PIXEL);
            editor.apply();
            notificationStatusBarStyle = PREF_NOTIFICATION_STATUS_BAR_STYLE_DEFAULT_VALUE_PIXEL;
        }
    }

    static final boolean PREF_NOTIFICATION_SHOW_IN_STATUS_BAR_DEFAULT_VALUE = true;
    static void notificationShowInStatusBar(Context context) {
        notificationShowInStatusBar = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_IN_STATUS_BAR, PREF_NOTIFICATION_SHOW_IN_STATUS_BAR_DEFAULT_VALUE);
    }

    static final String PREF_NOTIFICATION_TEXT_COLOR_DEFAULT_VALUE = "0";
    static void notificationTextColor(Context context) {
        // default value for Pixel (Android 12+) -> 0 (native)
        notificationTextColor = getSharedPreferences(context).getString(PREF_NOTIFICATION_TEXT_COLOR, PREF_NOTIFICATION_TEXT_COLOR_DEFAULT_VALUE);
    }

    static final boolean PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN_DEFAULT_VALUE = false;
    static void notificationHideInLockScreen(Context context) {
        notificationHideInLockScreen = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN, PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN_DEFAULT_VALUE);
    }

    /*
    static String notificationTheme(Context context) {
        notificationTheme = getSharedPreferences(context).getString(PREF_NOTIFICATION_THEME, "0");
        return notificationTheme;
    }
    */

    static final boolean PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_DEFAULT_VALUE = true;
    static void applicationWidgetListPrefIndicator(Context context) {
        applicationWidgetListPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS_DEFAULT_VALUE = "50";
    static void applicationWidgetListPrefIndicatorLightness(Context context) {
        applicationWidgetListPrefIndicatorLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS, PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_LIST_HEADER_DEFAULT_VALUE = true;
    static void applicationWidgetListHeader(Context context) {
        applicationWidgetListHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_HEADER, PREF_APPLICATION_WIDGET_LIST_HEADER_DEFAULT_VALUE);
    }

    static private final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND_DEFAULT_VALUE_PIXEL = "100";
    static private final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND_DEFAULT_VALUE_OTHERS = "25";
    static String applicationWidgetListBackgroundDefaultValue(Context context) {
        String defaultValue;
        if (PPApplication.isPixelLauncherDefault(context) ||
                PPApplication.isOneUILauncherDefault(context))
            defaultValue = PREF_APPLICATION_WIDGET_LIST_BACKGROUND_DEFAULT_VALUE_PIXEL;
        else
            defaultValue = PREF_APPLICATION_WIDGET_LIST_BACKGROUND_DEFAULT_VALUE_OTHERS;
        return defaultValue;
    }
    static void applicationWidgetListBackground(Context context) {
       applicationWidgetListBackground = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND, applicationWidgetListBackgroundDefaultValue(context));
    }

    static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B_DEFAULT_VALUE = "0";
    static void applicationWidgetListLightnessB(Context context) {
        applicationWidgetListLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B, PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T_DEFAULT_VALUE = "100";
    static void applicationWidgetListLightnessT(Context context) {
        applicationWidgetListLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T, PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_ICON_COLOR_DEFAULT_VALUE = "0";
    static void applicationWidgetIconColor(Context context) {
        applicationWidgetIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_COLOR, PREF_APPLICATION_WIDGET_ICON_COLOR_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_DEFAULT_VALUE = "100";
    static void applicationWidgetIconLightness(Context context) {
        applicationWidgetIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS, PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_LIST_ICON_COLOR_DEFAULT_VALUE = "0";
    static void applicationWidgetListIconColor(Context context) {
        applicationWidgetListIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, PREF_APPLICATION_WIDGET_LIST_ICON_COLOR_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS_DEFAULT_VALUE = "100";
    static void applicationWidgetListIconLightness(Context context) {
        applicationWidgetListIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS, PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS_DEFAULT_VALUE);
    }

    /*
    static boolean applicationEditorAutoCloseDrawer(Context context) {
        applicationEditorAutoCloseDrawer = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_AUTO_CLOSE_DRAWER, true);
        return applicationEditorAutoCloseDrawer;
    }
    */
    /*
    static boolean applicationEditorSaveEditorState(Context context) {
        applicationEditorSaveEditorState = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE, true);
        return applicationEditorSaveEditorState;
    }
    */

    static private final boolean PREF_NOTIFICATION_PREF_INDICATOR_DEFAULT_VALUE_SAMSUNG_31P = true; //false;
    static private final boolean PREF_NOTIFICATION_PREF_INDICATOR_DEFAULT_VALUE_OTHERS = true;
    static boolean notificationPrefIndicatorDefaultValue() {
        boolean defaultValue;
        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && (Build.VERSION.SDK_INT >= 31)) {
            defaultValue = PREF_NOTIFICATION_PREF_INDICATOR_DEFAULT_VALUE_SAMSUNG_31P;
        }
        else
            defaultValue = PREF_NOTIFICATION_PREF_INDICATOR_DEFAULT_VALUE_OTHERS;
        return defaultValue;
    }
    static void notificationPrefIndicator(Context context) {
        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && (Build.VERSION.SDK_INT >= 31)) {
            // default value for One UI 4 is better 1 (native)
            if (!getSharedPreferences(context).contains(PREF_NOTIFICATION_PREF_INDICATOR)) {
                // not contains this preference set to false
                SharedPreferences prefs = getSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(PREF_NOTIFICATION_PREF_INDICATOR, PREF_NOTIFICATION_PREF_INDICATOR_DEFAULT_VALUE_SAMSUNG_31P);
                editor.apply();
                notificationPrefIndicator = PREF_NOTIFICATION_PREF_INDICATOR_DEFAULT_VALUE_SAMSUNG_31P;
            }
        }
        notificationPrefIndicator = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_PREF_INDICATOR, notificationPrefIndicatorDefaultValue());
    }

    static final String PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS_DEFAULT_VALUE = "50";
    static void notificationPrefIndicatorLightness(Context context) {
        notificationPrefIndicatorLightness = getSharedPreferences(context).getString(PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS, PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_HOME_LAUNCHER_DEFAULT_VALUE = "activator";
    static void applicationHomeLauncher(Context context) {
        applicationHomeLauncher = getSharedPreferences(context).getString(PREF_APPLICATION_HOME_LAUNCHER, PREF_APPLICATION_HOME_LAUNCHER_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_LAUNCHER_DEFAULT_VALUE = "activator";
    static void applicationWidgetLauncher(Context context) {
        applicationWidgetLauncher = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LAUNCHER, PREF_APPLICATION_WIDGET_LAUNCHER_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_NOTIFICATION_LAUNCHER_DEFAULT_VALUE = "activator";
    static void applicationNotificationLauncher(Context context) {
        applicationNotificationLauncher = getSharedPreferences(context).getString(PREF_APPLICATION_NOTIFICATION_LAUNCHER, PREF_APPLICATION_NOTIFICATION_LAUNCHER_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL_DEFAULT_VALUE = "15";
    static void applicationEventWifiScanInterval(Context context) {
        applicationEventWifiScanInterval = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL, PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL_DEFAULT_VALUE));
    }

    static final String PREF_APPLICATION_DEFAULT_PROFILE_DEFAULT_VALUE = "-999";
    static void applicationDefaultProfile(Context context) {
        applicationDefaultProfile = Long.parseLong(getSharedPreferences(context).getString(PREF_APPLICATION_DEFAULT_PROFILE, PREF_APPLICATION_DEFAULT_PROFILE_DEFAULT_VALUE));
    }
    static long getApplicationDefaultProfileOnBoot() {
        if (PPApplication.applicationFullyStarted)
            return ApplicationPreferences.applicationDefaultProfile;
        else
            return Profile.PROFILE_NO_ACTIVATE;
    }

    static final String PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND_DEFAULT_VALUE = "";
    static void applicationDefaultProfileNotificationSound(Context context) {
        applicationDefaultProfileNotificationSound = getSharedPreferences(context).getString(PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND, PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE_DEFAULT_VALUE = false;
    static void applicationDefaultProfileNotificationVibrate(Context context) {
        applicationDefaultProfileNotificationVibrate = getSharedPreferences(context).getBoolean(PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE, PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE_DEFAULT_VALUE);
    }

    /*
    static void applicationDefaultProfileUsage(Context context) {
        applicationDefaultProfileUsage = getSharedPreferences(context).getBoolean(PREF_APPLICATION_DEFAULT_PROFILE_USAGE, false);
    }
    */

    static final boolean PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT_DEFAULT_VALUE = true;
    static void applicationActivatorGridLayout(Context context) {
        applicationActivatorGridLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT, PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT_DEFAULT_VALUE = true;
    static void applicationWidgetListGridLayout(Context context) {
        applicationWidgetListGridLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT, PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL_DEFAULT_VALUE = "15";
    static void applicationEventBluetoothScanInterval(Context context) {
        applicationEventBluetoothScanInterval = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL, PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL_DEFAULT_VALUE));
    }

    /*static void applicationEventWifiRescan(Context context) {
        applicationEventWifiRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_RESCAN, "1");
    }*/

    /*static void applicationEventBluetoothRescan(Context context) {
        applicationEventBluetoothRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN, "1");
    }*/

    static final boolean PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME_DEFAULT_VALUE = false;
    static void applicationWidgetIconHideProfileName(Context context) {
        applicationWidgetIconHideProfileName = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_SHORTCUT_EMBLEM_DEFAULT_VALUE = true;
    static void applicationShortcutEmblem(Context context) {
        applicationShortcutEmblem = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SHORTCUT_EMBLEM, PREF_APPLICATION_SHORTCUT_EMBLEM_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE = "1";
    static void applicationEventWifiScanInPowerSaveMode(Context context) {
        applicationEventWifiScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE, PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE = "1";
    static void applicationEventBluetoothScanInPowerSaveMode(Context context) {
        applicationEventBluetoothScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE, PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE);
    }

    /*
    static void applicationPowerSaveModeInternal(Context context) {
        applicationPowerSaveModeInternal = getSharedPreferences(context).getString(PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL, "3");
    }
    */

    static final String PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION_DEFAULT_VALUE = "10";
    static void applicationEventBluetoothLEScanDuration(Context context) {
        applicationEventBluetoothLEScanDuration = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION, PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION_DEFAULT_VALUE));
    }

    static final String PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL_DEFAULT_VALUE = "15";
    static void applicationEventLocationUpdateInterval(Context context) {
        applicationEventLocationUpdateInterval = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL, PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL_DEFAULT_VALUE));
    }

    static final String PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE_DEFAULT_VALUE = "1";
    static void applicationEventLocationUpdateInPowerSaveMode(Context context) {
        applicationEventLocationUpdateInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE, PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_LOCATION_USE_GPS_DEFAULT_VALUE = false;
    static void applicationEventLocationUseGPS(Context context) {
        applicationEventLocationUseGPS = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_USE_GPS, PREF_APPLICATION_EVENT_LOCATION_USE_GPS_DEFAULT_VALUE);
    }

    /*static void applicationEventLocationRescan(Context context) {
        applicationEventLocationRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_RESCAN, "1");
    }*/

    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL_DEFAULT_VALUE = "10";
    static void applicationEventOrientationScanInterval(Context context) {
        applicationEventOrientationScanInterval = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL, PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL_DEFAULT_VALUE));
    }

    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE = "1";
    static void applicationEventOrientationScanInPowerSaveMode(Context context) {
        applicationEventOrientationScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE, PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE = "1";
    static void applicationEventMobileCellsScanInPowerSaveMode(Context context) {
        applicationEventMobileCellsScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE, PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE);
    }

    /*static void applicationEventMobileCellsRescan(Context context) {
        applicationEventMobileCellsRescan = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN, "1");
    }*/

    static final String PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS_DEFAULT_VALUE = "7";
    static void applicationDeleteOldActivityLogs(Context context) {
        applicationDeleteOldActivityLogs = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS_DEFAULT_VALUE));
    }

    static private final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND_DEFAULT_VALUE_PIXEL = "100";
    static private final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND_DEFAULT_VALUE_OTHERS = "25";
    static String applicationWidgetIconBackgroundDefaultValue(Context context) {
        String defaultValue;
        if (PPApplication.isPixelLauncherDefault(context) ||
                PPApplication.isOneUILauncherDefault(context))
            defaultValue = PREF_APPLICATION_WIDGET_ICON_BACKGROUND_DEFAULT_VALUE_PIXEL;
        else
            defaultValue = PREF_APPLICATION_WIDGET_ICON_BACKGROUND_DEFAULT_VALUE_OTHERS;
        return defaultValue;
    }
    static void applicationWidgetIconBackground(Context context) {
        applicationWidgetIconBackground = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND, applicationWidgetIconBackgroundDefaultValue(context));
    }

    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B_DEFAULT_VALUE = "0";
    static void applicationWidgetIconLightnessB(Context context) {
        applicationWidgetIconLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B, PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T_DEFAULT_VALUE = "100";
    static void applicationWidgetIconLightnessT(Context context) {
        applicationWidgetIconLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T, PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_USE_PRIORITY_DEFAULT_VALUE = false;
    static void applicationEventUsePriority(Context context) {
        applicationEventUsePriority = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_USE_PRIORITY, PREF_APPLICATION_EVENT_USE_PRIORITY_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES_DEFAULT_VALUE = false;
    static void applicationUnlinkRingerNotificationVolumes(Context context) {
        applicationUnlinkRingerNotificationVolumes = getSharedPreferences(context).getBoolean(PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES, PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES_DEFAULT_VALUE = "0";
    static void applicationForceSetMergeRingNotificationVolumes(Context context) {
        applicationForceSetMergeRingNotificationVolumes = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES, PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES_DEFAULT_VALUE));
    }

    /*
    static boolean applicationSamsungEdgePrefIndicator(Context context) {
        applicationSamsungEdgePrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_PREF_INDICATOR, false);
        return applicationSamsungEdgePrefIndicator;
    }
    */

    static final boolean PREF_APPLICATION_SAMSUNG_EDGE_HEADER_DEFAULT_VALUE = true;
    static void applicationSamsungEdgeHeader(Context context) {
        applicationSamsungEdgeHeader = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_HEADER, PREF_APPLICATION_SAMSUNG_EDGE_HEADER_DEFAULT_VALUE);
    }

    static private final String PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_DEFAULT_VALUE_30P = "100";
    static private final String PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_DEFAULT_VALUE_30M = "50";
    static String applicationSamsungEdgeBackgroundDefaultValue() {
        String defaultValue;
        if (Build.VERSION.SDK_INT >= 30)
            // change by night mode is by default enabled, and for this reason set also opaqueness of background to 100
            defaultValue = PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_DEFAULT_VALUE_30P;
        else
            defaultValue = PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_DEFAULT_VALUE_30M;
        return defaultValue;
    }
    static void applicationSamsungEdgeBackground(Context context) {
        applicationSamsungEdgeBackground = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND, applicationSamsungEdgeBackgroundDefaultValue());
    }

    static final String PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B_DEFAULT_VALUE = "0";
    static void applicationSamsungEdgeLightnessB(Context context) {
        applicationSamsungEdgeLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B, PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T_DEFAULT_VALUE = "100";
    static void applicationSamsungEdgeLightnessT(Context context) {
        applicationSamsungEdgeLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T, PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR_DEFAULT_VALUE = "0";
    static void applicationSamsungEdgeIconColor(Context context) {
        applicationSamsungEdgeIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR_DEFAULT_VALUE);
        //return applicationSamsungEdgeIconColor;
    }

    static final String PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS_DEFAULT_VALUE = "100";
    static void applicationSamsungEdgeIconLightness(Context context) {
        applicationSamsungEdgeIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS, PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS_DEFAULT_VALUE);
    }

    /*
    static boolean applicationSamsungEdgeGridLayout(Context context) {
        applicationSamsungEdgeGridLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_GRID_LAYOUT, true);
        return applicationSamsungEdgeGridLayout;
    }
    */

    static final boolean PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE = false;
    static void applicationEventLocationScanOnlyWhenScreenIsOn(Context context) {
        applicationEventLocationScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE = false;
    static void applicationEventWifiScanOnlyWhenScreenIsOn(Context context) {
        applicationEventWifiScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON, PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE = false;
    static void applicationEventBluetoothScanOnlyWhenScreenIsOn(Context context) {
        applicationEventBluetoothScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON, PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE = false;
    static void applicationEventMobileCellScanOnlyWhenScreenIsOn(Context context) {
        applicationEventMobileCellScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON, PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE = true;
    static void applicationEventOrientationScanOnlyWhenScreenIsOn(Context context) {
        applicationEventOrientationScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE);    }

    static final boolean PREF_APPLICATION_RESTART_EVENTS_ALERT_DEFAULT_VALUE = true;
    static void applicationRestartEventsWithAlert(Context context) {
        applicationRestartEventsWithAlert = getSharedPreferences(context).getBoolean(PREF_APPLICATION_RESTART_EVENTS_ALERT, PREF_APPLICATION_RESTART_EVENTS_ALERT_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_DEFAULT_VALUE = true;
    static void applicationWidgetListRoundedCorners(Context context) {
        applicationWidgetListRoundedCorners = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_DEFAULT_VALUE = true;
    static void applicationWidgetIconRoundedCorners(Context context) {
        applicationWidgetIconRoundedCorners = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE_DEFAULT_VALUE = false;
    static void applicationWidgetListBackgroundType(Context context) {
        applicationWidgetListBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_DEFAULT_VALUE = "-1"; // white color
    static void applicationWidgetListBackgroundColor(Context context) {
        applicationWidgetListBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR, PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE_DEFAULT_VALUE = false;
    static void applicationWidgetIconBackgroundType(Context context) {
        applicationWidgetIconBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_DEFAULT_VALUE = "-1"; // white color
    static void applicationWidgetIconBackgroundColor(Context context) {
        applicationWidgetIconBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR, PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE_DEFAULT_VALUE = false;
    static void applicationSamsungEdgeBackgroundType(Context context) {
        applicationSamsungEdgeBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_DEFAULT_VALUE = "-1"; // white color
    static void applicationSamsungEdgeBackgroundColor(Context context) {
        applicationSamsungEdgeBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR, PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR_DEFAULT_VALUE);
    }

    /*
    static boolean applicationEventWifiEnableWifi(Context context) {
        applicationEventWifiEnableWifi = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, true);
        return applicationEventWifiEnableWifi;
    }

    static boolean applicationEventBluetoothEnableBluetooth(Context context) {
        applicationEventBluetoothEnableBluetooth = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, true);
        return applicationEventBluetoothEnableBluetooth;
    }
    */

    static final boolean PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF_DEFAULT_VALUE = true;
    static void applicationEventWifiScanIfWifiOff(Context context) {
        applicationEventWifiScanIfWifiOff = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF, PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF_DEFAULT_VALUE = true;
    static void applicationEventBluetoothScanIfBluetoothOff(Context context) {
        applicationEventBluetoothScanIfBluetoothOff = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF, PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING_DEFAULT_VALUE = false;
    static void applicationEventWifiEnableScanning(Context context) {
        applicationEventWifiEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING_DEFAULT_VALUE = false;
    static void applicationEventBluetoothEnableScanning(Context context) {
        applicationEventBluetoothEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING_DEFAULT_VALUE = false;
    static void applicationEventLocationEnableScanning(Context context) {
        applicationEventLocationEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING_DEFAULT_VALUE = false;
    static void applicationEventMobileCellEnableScanning(Context context) {
        applicationEventMobileCellEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING_DEFAULT_VALUE = false;
    static void applicationEventOrientationEnableScanning(Context context) {
        applicationEventOrientationEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE = false;
    static void applicationEventWifiDisabledScannigByProfile(Context context) {
        applicationEventWifiDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE = false;
    static void applicationEventBluetoothDisabledScannigByProfile(Context context) {
        applicationEventBluetoothDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE = false;
    static void applicationEventLocationDisabledScannigByProfile(Context context) {
        applicationEventLocationDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE = false;
    static void applicationEventMobileCellDisabledScannigByProfile(Context context) {
        applicationEventMobileCellDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE =  false;
    static void applicationEventOrientationDisabledScannigByProfile(Context context) {
        applicationEventOrientationDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_NOTIFICATION_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE = false;
    static void applicationEventNotificationDisabledScannigByProfile(Context context) {
        applicationEventNotificationDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_NOTIFICATION_DISABLED_SCANNING_BY_PROFILE, PREF_APPLICATION_EVENT_NOTIFICATION_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_USE_ALARM_CLOCK_DEFAULT_VALUE = false;
    static void applicationUseAlarmClock(Context context) {
        applicationUseAlarmClock = getSharedPreferences(context).getBoolean(PREF_APPLICATION_USE_ALARM_CLOCK, PREF_APPLICATION_USE_ALARM_CLOCK_DEFAULT_VALUE);
    }

    static final boolean PREF_NOTIFICATION_SHOW_BUTTON_EXIT_DEFAULT_VALUE = false;
    static void notificationShowButtonExit(Context context) {
        notificationShowButtonExit = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_BUTTON_EXIT, PREF_NOTIFICATION_SHOW_BUTTON_EXIT_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_DEFAULT_VALUE = true;
    static void applicationWidgetOneRowPrefIndicator(Context context) {
        applicationWidgetOneRowPrefIndicator = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS_DEFAULT_VALUE = "50";
    static void applicationWidgetOneRowPrefIndicatorLightness(Context context) {
        applicationWidgetOneRowPrefIndicatorLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS, PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS_DEFAULT_VALUE);
    }

    static private final String PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_DEFAULT_VALUE_PIXEL = "100";
    static private final String PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_DEFAULT_VALUE_OTHERS = "25";
    static String applicationWidgetOneRowBackgroundDefaultValue(Context context) {
        String defaultValue;
        if (PPApplication.isPixelLauncherDefault(context) ||
                PPApplication.isOneUILauncherDefault(context))
            defaultValue = PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_DEFAULT_VALUE_PIXEL;
        else
            defaultValue = PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_DEFAULT_VALUE_OTHERS;
        return defaultValue;
    }
    static void applicationWidgetOneRowBackground(Context context) {
        applicationWidgetOneRowBackground = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND, applicationWidgetOneRowBackgroundDefaultValue(context));
    }

    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B_DEFAULT_VALUE = "0";
    static void applicationWidgetOneRowLightnessB(Context context) {
        applicationWidgetOneRowLightnessB = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B, PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T_DEFAULT_VALUE = "100";
    static void applicationWidgetOneRowLightnessT(Context context) {
        applicationWidgetOneRowLightnessT = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T, PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR_DEFAULT_VALUE = "0";
    static void applicationWidgetOneRowIconColor(Context context) {
        applicationWidgetOneRowIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS_DEFAULT_VALUE = "100";
    static void applicationWidgetOneRowIconLightness(Context context) {
        applicationWidgetOneRowIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS, PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_DEFAULT_VALUE = true;
    static void applicationWidgetOneRowRoundedCorners(Context context) {
        applicationWidgetOneRowRoundedCorners = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE_DEFAULT_VALUE = false;
    static void applicationWidgetOneRowBackgroundType(Context context) {
        applicationWidgetOneRowBackgroundType = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_DEFAULT_VALUE = "-1"; // white color
    static void applicationWidgetOneRowBackgroundColor(Context context) {
        applicationWidgetOneRowBackgroundColor = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR, PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER_DEFAULT_VALUE = "100";
    static void applicationWidgetListLightnessBorder(Context context) {
        applicationWidgetListLightnessBorder = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER, PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER_DEFAULT_VALUE = "100";
    static void applicationWidgetOneRowLightnessBorder(Context context) {
        applicationWidgetOneRowLightnessBorder = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER, PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER_DEFAULT_VALUE = "100";
    static void applicationWidgetIconLightnessBorder(Context context) {
        applicationWidgetIconLightnessBorder = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER, PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER_DEFAULT_VALUE = false;
    static void applicationWidgetListShowBorder(Context context) {
        applicationWidgetListShowBorder = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER_DEFAULT_VALUE = false;
    static void applicationWidgetOneRowShowBorder(Context context) {
        applicationWidgetOneRowShowBorder = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER_DEFAULT_VALUE = false;
    static void applicationWidgetIconShowBorder(Context context) {
        applicationWidgetIconShowBorder = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE = false;
    static void applicationWidgetListCustomIconLightness(Context context) {
        applicationWidgetListCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS, PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE = false;
    static void applicationWidgetOneRowCustomIconLightness(Context context) {
        applicationWidgetOneRowCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS, PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE = false;
    static void applicationWidgetIconCustomIconLightness(Context context) {
        applicationWidgetIconCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS, PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE = false;
    static void applicationSamsungEdgeCustomIconLightness(Context context) {
        applicationSamsungEdgeCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS, PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE);
    }

    /*
    static boolean notificationDarkBackground(Context context) {
        notificationDarkBackground = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_DARK_BACKGROUND, false);
        return notificationDarkBackground;
    }
    */

    static final boolean PREF_NOTIFICATION_USE_DECORATION_DEFAULT_VALUE = true;
    static void notificationUseDecoration(Context context) {
        // default value for Pixel (Android 12+) -> true
        notificationUseDecoration = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_USE_DECORATION, PREF_NOTIFICATION_USE_DECORATION_DEFAULT_VALUE);
    }

    static final String PREF_NOTIFICATION_LAYOUT_TYPE_DEFAULT_VALUE = "0";
    static void notificationLayoutType(Context context) {
        // default value for Pixel (Android 12+) -> 0 (expandable)
        notificationLayoutType = getSharedPreferences(context).getString(PREF_NOTIFICATION_LAYOUT_TYPE, PREF_NOTIFICATION_LAYOUT_TYPE_DEFAULT_VALUE);
    }

    static final String PREF_NOTIFICATION_BACKGROUND_COLOR_DEFAULT_VALUE = "0";
    static void notificationBackgroundColor(Context context) {
        // default value for Pixel (Android 12+) -> 0 (native)
        notificationBackgroundColor = getSharedPreferences(context).getString(PREF_NOTIFICATION_BACKGROUND_COLOR, PREF_NOTIFICATION_BACKGROUND_COLOR_DEFAULT_VALUE);
    }

    /*
    static String applicationNightModeOffTheme(Context context) {
        applicationNightModeOffTheme = getSharedPreferences(context).getString(PREF_APPLICATION_NIGHT_MODE_OFF_THEME, "white");
        return applicationNightModeOffTheme;
    }
    */

    static final boolean PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED_DEFAULT_VALUE = true;
    static void applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled(Context context) {
        applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED, PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION_DEFAULT_VALUE = "0";
    static void applicationSamsungEdgeVerticalPosition(Context context) {
        applicationSamsungEdgeVerticalPosition = getSharedPreferences(context).getString(PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION, PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION_DEFAULT_VALUE);
    }

    static final int PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR_DEFAULT_VALUE = 0xFFFFFFFF;
    static void notificationBackgroundCustomColor(Context context) {
        notificationBackgroundCustomColor = getSharedPreferences(context).getInt(PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR, PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR_DEFAULT_VALUE);
    }

//    static void notificationNightMode(Context context) {
//        notificationNightMode = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_NIGHT_MODE, false);
//    }

    static final boolean PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR_DEFAULT_VALUE = true;
    static void applicationEditorHideHeaderOrBottomBar(Context context) {
        applicationEditorHideHeaderOrBottomBar = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR, PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION_DEFAULT_VALUE = true;
    static void applicationWidgetIconShowProfileDuration(Context context) {
        applicationWidgetIconShowProfileDuration = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION, PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION_DEFAULT_VALUE);
    }

    static private final String PREF_NOTIFICATION_NOTIFICATION_STYLE_DEFAULT_VALUE_OTHERS = "0"; // custom
    static private final String PREF_NOTIFICATION_NOTIFICATION_STYLE_DEFAULT_VALUE_SAMSUNG_31P = "1"; // native
    static String notificationNotificationStyleDefaultValue() {
        // change: for all devices with Android 12 set "native"
        String defaultValue;
        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && (Build.VERSION.SDK_INT >= 31)) {
            // default value for One UI 4 is better 1 (native)
            defaultValue = PREF_NOTIFICATION_NOTIFICATION_STYLE_DEFAULT_VALUE_SAMSUNG_31P;
        }
        else
            defaultValue = PREF_NOTIFICATION_NOTIFICATION_STYLE_DEFAULT_VALUE_OTHERS;
        return defaultValue;
    }
    static void notificationNotificationStyle(Context context) {
        // change: for all devices with Android 12 set "native"
        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && (Build.VERSION.SDK_INT >= 31)) {
            // default value for One UI 4 is better 1 (native)
            if (!getSharedPreferences(context).contains(PREF_NOTIFICATION_NOTIFICATION_STYLE)) {
                // not contains this preference set to 1
                SharedPreferences prefs = getSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PREF_NOTIFICATION_NOTIFICATION_STYLE, PREF_NOTIFICATION_NOTIFICATION_STYLE_DEFAULT_VALUE_SAMSUNG_31P);
                editor.apply();
                notificationNotificationStyle = PREF_NOTIFICATION_NOTIFICATION_STYLE_DEFAULT_VALUE_SAMSUNG_31P;
            }
        }
        notificationNotificationStyle = getSharedPreferences(context).getString(PREF_NOTIFICATION_NOTIFICATION_STYLE, notificationNotificationStyleDefaultValue());
    }

    static private final boolean PREF_NOTIFICATION_SHOW_PROFILE_ICON_DEFAULT_VALUE_SAMSUNG_31P = false;
    static private final boolean PREF_NOTIFICATION_SHOW_PROFILE_ICON_DEFAULT_VALUE_OTHERS = true;
    static boolean notificationShowProfileIconDefaultValue() {
        boolean defaultValue;
        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && (Build.VERSION.SDK_INT >= 31)) {
            defaultValue = PREF_NOTIFICATION_SHOW_PROFILE_ICON_DEFAULT_VALUE_SAMSUNG_31P;
        }
        else
            defaultValue = PREF_NOTIFICATION_SHOW_PROFILE_ICON_DEFAULT_VALUE_OTHERS;
        return defaultValue;
    }
    static void notificationShowProfileIcon(Context context) {
        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy && (Build.VERSION.SDK_INT >= 31)) {
            // default value for One UI 4 is better 1 (native)
            if (!getSharedPreferences(context).contains(PREF_NOTIFICATION_SHOW_PROFILE_ICON)) {
                // not contains this preference set to false
                SharedPreferences prefs = getSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(PREF_NOTIFICATION_SHOW_PROFILE_ICON, PREF_NOTIFICATION_SHOW_PROFILE_ICON_DEFAULT_VALUE_SAMSUNG_31P);
                editor.apply();
                notificationShowProfileIcon = PREF_NOTIFICATION_SHOW_PROFILE_ICON_DEFAULT_VALUE_SAMSUNG_31P;
            }
        }
        notificationShowProfileIcon = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_PROFILE_ICON, notificationShowProfileIconDefaultValue());
    }

    static final boolean PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING_DEFAULT_VALUE = false;
    static void applicationEventPeriodicScanningEnableScanning(Context context) {
        applicationEventPeriodicScanningEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING, PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL_DEFAULT_VALUE = "15";
    static void applicationEventPeriodicScanningScanInterval(Context context) {
        applicationEventPeriodicScanningScanInterval = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL, PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL_DEFAULT_VALUE));
    }

    static final String PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE = "1";
    static void applicationEventPeriodicScanningScanInPowerSaveMode(Context context) {
        applicationEventPeriodicScanningScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_POWER_SAVE_MODE, PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE = false;
    static void applicationEventPeriodicScanningScanOnlyWhenScreenIsOn(Context context) {
        applicationEventPeriodicScanningScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_ONLY_WHEN_SCREEN_IS_ON, PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT_DEFAULT_VALUE = false;
    static void applicationEventWifiScanIgnoreHotspot(Context context) {
        applicationEventWifiScanIgnoreHotspot = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT, PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING_DEFAULT_VALUE = false;
    static void applicationEventNotificationEnableScanning(Context context) {
        applicationEventNotificationEnableScanning = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE = "1";
    static void applicationEventNotificationScanInPowerSaveMode(Context context) {
        applicationEventNotificationScanInPowerSaveMode = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_POWER_SAVE_MODE, PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_POWER_SAVE_MODE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE = false;
    static void applicationEventNotificationScanOnlyWhenScreenIsOn(Context context) {
        applicationEventNotificationScanOnlyWhenScreenIsOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_ONLY_WHEN_SCREEN_IS_ON, PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_ONLY_WHEN_SCREEN_IS_ON_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE = "5";
    static void applicationWidgetOneRowRoundedCornersRadius(Context context) {
        applicationWidgetOneRowRoundedCornersRadius = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS, PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE));
    }

    static final String PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE = "5";
    static void applicationWidgetListRoundedCornersRadius(Context context) {
        applicationWidgetListRoundedCornersRadius = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS, PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE));
    }

    static final String PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE = "5";
    static void applicationWidgetIconRoundedCornersRadius(Context context) {
        applicationWidgetIconRoundedCornersRadius = Integer.parseInt(getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS, PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS_DEFAULT_VALUE));
    }

    static final String PREF_APPLICATION_ACTIVATOR_NUM_COLUMNS_DEFAULT_VALUE = "3";
    static void applicationActivatorNumColums(Context context) {
        applicationActivatorNumColums = getSharedPreferences(context).getString(PREF_APPLICATION_ACTIVATOR_NUM_COLUMNS, PREF_APPLICATION_ACTIVATOR_NUM_COLUMNS_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_APPLICATION_INTERFACE_NOTIFICATION_SOUND_DEFAULT_VALUE = "";
    static void applicationApplicationInterfaceNotificationSound(Context context) {
        applicationApplicationInterfaceNotificationSound = getSharedPreferences(context).getString(PREF_APPLICATION_APPLICATION_INTERFACE_NOTIFICATION_SOUND, PREF_APPLICATION_APPLICATION_INTERFACE_NOTIFICATION_SOUND_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_APPLICATION_INTERFACE_NOTIFICATION_VIBRATE_DEFAULT_VALUE = false;
    static void applicationApplicationInterfaceNotificationVibrate(Context context) {
        applicationApplicationInterfaceNotificationVibrate = getSharedPreferences(context).getBoolean(PREF_APPLICATION_APPLICATION_INTERFACE_NOTIFICATION_VIBRATE, PREF_APPLICATION_APPLICATION_INTERFACE_NOTIFICATION_VIBRATE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_ACTIVATOR_ADD_RESTART_EVENTS_INTO_PROFILE_LIST_DEFAULT_VALUE = false;
    static void applicationActivatorAddRestartEventsIntoProfileList(Context context) {
        applicationActivatorAddRestartEventsIntoProfileList = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_ADD_RESTART_EVENTS_INTO_PROFILE_LIST, PREF_APPLICATION_ACTIVATOR_ADD_RESTART_EVENTS_INTO_PROFILE_LIST_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_ACTIVATOR_INCREASE_BRIGHTNESS_DEFAULT_VALUE = false;
    static void applicationActivatorIncreaseBrightness(Context context) {
        applicationActivatorIncreaseBrightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_ACTIVATOR_INCREASE_BRIGHTNESS, PREF_APPLICATION_ACTIVATOR_INCREASE_BRIGHTNESS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON_DEFAULT_VALUE = false;
    static void applicationForceSetBrightnessAtScreenOn(Context context) {
        applicationForceSetBrightnessAtScreenOn = getSharedPreferences(context).getBoolean(PREF_APPLICATION_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON, PREF_APPLICATION_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE = "0";
    static void applicationEventPeriodicScanningScanInTimeMultiply(Context context) {
        applicationEventPeriodicScanningScanInTimeMultiply = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY, PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE = 0;
    static void applicationEventPeriodicScanningScanInTimeMultiplyFrom(Context context) {
        applicationEventPeriodicScanningScanInTimeMultiplyFrom = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_FROM, PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE = 0;
    static void applicationEventPeriodicScanningScanInTimeMultiplyTo(Context context) {
        applicationEventPeriodicScanningScanInTimeMultiplyTo = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_TO, PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE = "0";
    static void applicationEventBluetoothScanInTimeMultiply(Context context) {
        applicationEventBluetoothScanInTimeMultiply = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY, PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE = 0;
    static void applicationEventBluetoothScanInTimeMultiplyFrom(Context context) {
        applicationEventBluetoothScanInTimeMultiplyFrom = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_FROM, PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE = 0;
    static void applicationEventBluetoothScanInTimeMultiplyTo(Context context) {
        applicationEventBluetoothScanInTimeMultiplyTo = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_TO, PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE = "0";
    static void applicationEventLocationScanInTimeMultiply(Context context) {
        applicationEventLocationScanInTimeMultiply = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY, PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE = 0;
    static void applicationEventLocationScanInTimeMultiplyFrom(Context context) {
        applicationEventLocationScanInTimeMultiplyFrom = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_FROM, PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE = 0;
    static void applicationEventLocationScanInTimeMultiplyTo(Context context) {
        applicationEventLocationScanInTimeMultiplyTo = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_TO, PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE = "1";
    static void applicationEventMobileCellScanInTimeMultiply(Context context) {
        applicationEventMobileCellScanInTimeMultiply = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY, PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE = 0;
    static void applicationEventMobileCellScanInTimeMultiplyFrom(Context context) {
        applicationEventMobileCellScanInTimeMultiplyFrom = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_FROM, PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE = 0;
    static void applicationEventMobileCellScanInTimeMultiplyTo(Context context) {
        applicationEventMobileCellScanInTimeMultiplyTo = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_TO, PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE = "1";
    static void applicationEventNotificationScanInTimeMultiply(Context context) {
        applicationEventNotificationScanInTimeMultiply = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY, PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE = 0;
    static void applicationEventNotificationScanInTimeMultiplyFrom(Context context) {
        applicationEventNotificationScanInTimeMultiplyFrom = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_FROM, PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE = 0;
    static void applicationEventNotificationScanInTimeMultiplyTo(Context context) {
        applicationEventNotificationScanInTimeMultiplyTo = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_TO, PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE = "0";
    static void applicationEventOrientationScanInTimeMultiply(Context context) {
        applicationEventOrientationScanInTimeMultiply = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY, PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE = 0;
    static void applicationEventOrientationScanInTimeMultiplyFrom(Context context) {
        applicationEventOrientationScanInTimeMultiplyFrom = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_FROM, PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE = 0;
    static void applicationEventOrientationScanInTimeMultiplyTo(Context context) {
        applicationEventOrientationScanInTimeMultiplyTo = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_TO, PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE = "0";
    static void applicationEventWifiScanInTimeMultiply(Context context) {
        applicationEventWifiScanInTimeMultiply = getSharedPreferences(context).getString(PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY, PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE = 0;
    static void applicationEventWifiScanInTimeMultiplyFrom(Context context) {
        applicationEventWifiScanInTimeMultiplyFrom = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_FROM, PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_FROM_DEFAULT_VALUE);
    }

    static final int PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE = 0;
    static void applicationEventWifiScanInTimeMultiplyTo(Context context) {
        applicationEventWifiScanInTimeMultiplyTo = getSharedPreferences(context).getInt(PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_TO, PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_TO_DEFAULT_VALUE);
    }

    //static private final boolean PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON_DEFAULT_VALUE_31P = true;
    //static private final boolean PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON_DEFAULT_VALUE_30L = false;
    @SuppressWarnings("SameReturnValue")
    static boolean notificationShowRestartEventsAsButtonDefaultValue() {
        /*boolean defaultValue;
        if (Build.VERSION.SDK_INT >= 31)
            defaultValue = PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON_DEFAULT_VALUE_31P;
        else
            defaultValue = PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON_DEFAULT_VALUE_30L;*/
        return false; //defaultValue;
    }
    static void notificationShowRestartEventsAsButton(Context context) {
        notificationShowRestartEventsAsButton = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON, notificationShowRestartEventsAsButtonDefaultValue());
    }

    static final String PREF_APPLICATION_WIDGET_ONE_ROW_LAYOUT_HEIGHT_DEFAULT_VALUE = "0";
    static void applicationWidgetOneRowLayoutHeight(Context context) {
        applicationWidgetOneRowLayoutHeight = getSharedPreferences(context).getString(PREF_APPLICATION_WIDGET_ONE_ROW_LAYOUT_HEIGHT, PREF_APPLICATION_WIDGET_ONE_ROW_LAYOUT_HEIGHT_DEFAULT_VALUE);
    }
    /*static final boolean PREF_APPLICATION_WIDGET_ONE_ROW_HIGHER_LAYOUT_DEFAULT_VALUE = false;
    static void applicationWidgetOneRowHigherLayout(Context context) {
        applicationWidgetOneRowHigherLayout = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_HIGHER_LAYOUT, PREF_APPLICATION_WIDGET_ONE_ROW_HIGHER_LAYOUT_DEFAULT_VALUE);
    }*/

    static private final boolean PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_PIXEL = true;
    static private final boolean PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_OTHERS = false;
    static boolean pplicationWidgetIconChangeColorsByNightModeDefaultValue(Context context) {
        boolean defaultValue;
        if (PPApplication.isPixelLauncherDefault(context) ||
                PPApplication.isOneUILauncherDefault(context))
            defaultValue = PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_PIXEL;
        else
            defaultValue = PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_OTHERS;
        return defaultValue;
    }
    static void applicationWidgetIconChangeColorsByNightMode(Context context) {
        // copy bad preference into ok preference
        SharedPreferences mySPrefs = getSharedPreferences(context);
        if (mySPrefs.contains("applicationWidgetChangeColorsByNightMode")) {
            SharedPreferences.Editor editor = mySPrefs.edit();
            editor.putBoolean(PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE,
                    mySPrefs.getBoolean("applicationWidgetChangeColorsByNightMode", PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_OTHERS));
            editor.apply();
        }

        /*if (DebugVersion.enabled) {
            SharedPreferences mySPrefs = getSharedPreferences(context);
            SharedPreferences.Editor editor = mySPrefs.edit();
            editor.remove(PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE);
            editor.apply();
        }*/

        applicationWidgetIconChangeColorsByNightMode = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE, pplicationWidgetIconChangeColorsByNightModeDefaultValue(context));
    }

    static private final boolean PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_PIXEL = true;
    static private final boolean PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_OTHERS = false;
    static boolean applicationWidgetOneRowChangeColorsByNightModeDefaultValue(Context context) {
        boolean defaultValue;
        if (PPApplication.isPixelLauncherDefault(context) ||
                PPApplication.isOneUILauncherDefault(context))
            defaultValue = PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_PIXEL;
        else
            defaultValue = PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_OTHERS;
        return defaultValue;
    }
    static void applicationWidgetOneRowChangeColorsByNightMode(Context context) {
        // copy bad preference into ok preference
        SharedPreferences mySPrefs = getSharedPreferences(context);
        if (mySPrefs.contains("applicationWidgetChangeColorsByNightMode")) {
            SharedPreferences.Editor editor = mySPrefs.edit();
            editor.putBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE,
                    mySPrefs.getBoolean("applicationWidgetChangeColorsByNightMode", PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_OTHERS));
            editor.apply();
        }

        /*if (DebugVersion.enabled) {
            SharedPreferences mySPrefs = getSharedPreferences(context);
            SharedPreferences.Editor editor = mySPrefs.edit();
            editor.remove(PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE);
            editor.apply();
        }*/

        applicationWidgetOneRowChangeColorsByNightMode = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE, applicationWidgetOneRowChangeColorsByNightModeDefaultValue(context));
    }

    static private final boolean PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_PIXEL = true;
    static private final boolean PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_OTHERS = false;
    static boolean applicationWidgetListChangeColorsByNightModeDefaultValue(Context context) {
        boolean defaultValue;
        if (PPApplication.isPixelLauncherDefault(context) ||
                PPApplication.isOneUILauncherDefault(context))
            defaultValue = PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_PIXEL;
        else
            defaultValue = PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_OTHERS;
        return defaultValue;
    }
    static void applicationWidgetListChangeColorsByNightMode(Context context) {
        // copy bad preference into ok preference
        SharedPreferences mySPrefs = getSharedPreferences(context);
        if (mySPrefs.contains("applicationWidgetChangeColorsByNightMode")) {
            SharedPreferences.Editor editor = mySPrefs.edit();
            editor.putBoolean(PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE,
                    mySPrefs.getBoolean("applicationWidgetChangeColorsByNightMode", PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_OTHERS));
            editor.apply();
        }

        /*if (DebugVersion.enabled) {
            SharedPreferences mySPrefs = getSharedPreferences(context);
            SharedPreferences.Editor editor = mySPrefs.edit();
            editor.remove(PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE);
            editor.apply();
        }*/

        applicationWidgetListChangeColorsByNightMode = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE, applicationWidgetListChangeColorsByNightModeDefaultValue(context));
    }

    static private final boolean PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_30P = true;
    static private final boolean PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_29L = false;
    static boolean applicationSamsungEdgeChangeColorsByNightModeDefaultValue() {
        boolean defaultValue;
        if (Build.VERSION.SDK_INT >= 30)
            defaultValue = PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_30P;
        else
            defaultValue = PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE_DEFAULT_VALUE_29L;
        return defaultValue;
    }
    static void applicationSamsungEdgeChangeColorsByNightMode(Context context) {
        /*if (DebugVersion.enabled) {
            SharedPreferences mySPrefs = getSharedPreferences(context);
            SharedPreferences.Editor editor = mySPrefs.edit();
            editor.remove(PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE);
            editor.apply();
        }*/

        applicationSamsungEdgeChangeColorsByNightMode = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE, applicationSamsungEdgeChangeColorsByNightModeDefaultValue());
    }

    static final String PREF_NOTIFICATION_PROFILE_ICON_COLOR_DEFAULT_VALUE = "0";
    static void notificationProfileIconColor(Context context) {
        notificationProfileIconColor = getSharedPreferences(context).getString(PREF_NOTIFICATION_PROFILE_ICON_COLOR, PREF_NOTIFICATION_PROFILE_ICON_COLOR_DEFAULT_VALUE);
    }

    static final String PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS_DEFAULT_VALUE = "100";
    static void notificationProfileIconLightness(Context context) {
        notificationProfileIconLightness = getSharedPreferences(context).getString(PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS, PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS_DEFAULT_VALUE);
    }

    static final boolean PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS_DEFAULT_VALUE = false;
    static void notificationCustomProfileIconLightness(Context context) {
        notificationCustomProfileIconLightness = getSharedPreferences(context).getBoolean(PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS, PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_SHORTCUT_ICON_COLOR_DEFAULT_VALUE = "0";
    static void applicationShortcutIconColor(Context context) {
        applicationShortcutIconColor = getSharedPreferences(context).getString(PREF_APPLICATION_SHORTCUT_ICON_COLOR, PREF_APPLICATION_SHORTCUT_ICON_COLOR_DEFAULT_VALUE);
    }

    static final String PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS_DEFAULT_VALUE = "100";
    static void applicationShortcutIconLightness(Context context) {
        applicationShortcutIconLightness = getSharedPreferences(context).getString(PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS, PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE = false;
    static void applicationShortcutCustomIconLightness(Context context) {
        applicationShortcutCustomIconLightness = getSharedPreferences(context).getBoolean(PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS, PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_EVENT_PERIODIC_SCANNING_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE = false;
    static void applicationEventPeriodicScanningDisabledScannigByProfile(Context context) {
        applicationEventPeriodicScanningDisabledScannigByProfile = getSharedPreferences(context).getBoolean(PREF_APPLICATION_EVENT_PERIODIC_SCANNING_DISABLED_SCANNING_BY_PROFILE, PREF_APPLICATION_EVENT_PERIODIC_SCANNING_DISABLED_SCANNING_BY_PROFILE_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS_DEFAULT_VALUE = true;
    static void applicationWidgetIconUseDynamicColors(Context context) {
        applicationWidgetIconUseDynamicColors = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS, PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS_DEFAULT_VALUE = true;
    static void applicationWidgetOneRowUseDynamicColors(Context context) {
        applicationWidgetOneRowUseDynamicColors = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS, PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS_DEFAULT_VALUE);
    }

    static final boolean PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS_DEFAULT_VALUE = true;
    static void applicationWidgetListUseDynamicColors(Context context) {
        applicationWidgetListUseDynamicColors = getSharedPreferences(context).getBoolean(PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS, PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS_DEFAULT_VALUE);
    }

    static void deleteBadPreferences(Context context) {
        SharedPreferences mySPrefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = mySPrefs.edit();
        editor.remove("applicationWidgetChangeColorsByNightMode");
        editor.apply();
    }

    static void loadStartTargetHelps(Context context) {
        SharedPreferences _preferences = getSharedPreferences(context);
        prefActivatorActivityStartTargetHelps = _preferences.getBoolean(ActivatorActivity.PREF_START_TARGET_HELPS, false);
        prefActivatorActivityStartTargetHelpsFinished = _preferences.getBoolean(ActivatorActivity.PREF_START_TARGET_HELPS_FINISHED, false);
        prefActivatorFragmentStartTargetHelps = _preferences.getBoolean(ActivatorListFragment.PREF_START_TARGET_HELPS, false);
        prefActivatorFragmentStartTargetHelpsFinished = _preferences.getBoolean(ActivatorListFragment.PREF_START_TARGET_HELPS_FINISHED, false);
        prefActivatorAdapterStartTargetHelps = _preferences.getBoolean(ActivatorListAdapter.PREF_START_TARGET_HELPS, false);
        prefActivatorAdapterStartTargetHelpsFinished = _preferences.getBoolean(ActivatorListAdapter.PREF_START_TARGET_HELPS_FINISHED, false);

        prefEditorActivityStartTargetHelps = _preferences.getBoolean(EditorActivity.PREF_START_TARGET_HELPS, false);
        prefEditorActivityStartTargetHelpsRunStopIndicator = _preferences.getBoolean(EditorActivity.PREF_START_TARGET_HELPS_RUN_STOP_INDICATOR, false);
        prefEditorActivityStartTargetHelpsBottomNavigation = _preferences.getBoolean(EditorActivity.PREF_START_TARGET_HELPS_BOTTOM_NAVIGATION, false);
        prefEditorActivityStartTargetHelpsFinished = _preferences.getBoolean(EditorActivity.PREF_START_TARGET_HELPS_FINISHED, false);

        prefEditorProfilesFragmentStartTargetHelps = _preferences.getBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, false);
        prefEditorProfilesFragmentStartTargetHelpsFilterSpinner = _preferences.getBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS_FILTER_SPINNER, false);
        prefEditorFragmentStartTargetHelpsDefaultProfile = _preferences.getBoolean(EditorActivity.PREF_START_TARGET_HELPS_DEFAULT_PROFILE, false);
        prefEditorProfilesFragmentStartTargetHelpsFinished = _preferences.getBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS_FINISHED, false);
        prefEditorProfilesAdapterStartTargetHelps = _preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, false);
        prefEditorProfilesAdapterStartTargetHelpsOrder = _preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
        prefEditorProfilesAdapterStartTargetHelpsShowInActivator = _preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, false);
        prefEditorProfilesAdapterStartTargetHelpsFinished = _preferences.getBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_FINISHED, false);

        prefEditorEventsFragmentStartTargetHelps = _preferences.getBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, false);
        prefEditorEventsFragmentStartTargetHelpsFilterSpinner = _preferences.getBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_FILTER_SPINNER, false);
        prefEditorEventsFragmentStartTargetHelpsOrderSpinner = _preferences.getBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_ORDER_SPINNER, false);
        prefEditorEventsFragmentStartTargetHelpsFinished = _preferences.getBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_FINISHED, false);
        prefEditorEventsAdapterStartTargetHelps = _preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, false);
        prefEditorEventsAdapterStartTargetHelpsOrder = _preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, false);
        prefEditorEventsAdapterStartTargetHelpsStatus = _preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_STATUS, false);
        prefEditorEventsAdapterStartTargetHelpsFinished = _preferences.getBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_FINISHED, false);

        prefProfilePrefsActivityStartTargetHelps = _preferences.getBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, false);
        //prefProfilePrefsActivityStartTargetHelpsSave = _preferences.getBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS_SAVE, false);
        prefProfilePrefsActivityStartTargetHelpsFinished = _preferences.getBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS_FINISHED, false);

        prefEventPrefsActivityStartTargetHelps = _preferences.getBoolean(EventsPrefsActivity.PREF_START_TARGET_HELPS, false);
        prefEventPrefsActivityStartTargetHelpsFinished = _preferences.getBoolean(EventsPrefsActivity.PREF_START_TARGET_HELPS_FINISHED, false);
    }

    static void startStopTargetHelps(Context context, boolean start) {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);

        editor.putBoolean(ActivatorActivity.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefActivatorActivityStartTargetHelps = start;
        editor.putBoolean(ActivatorActivity.PREF_START_TARGET_HELPS_FINISHED, !start);
        ApplicationPreferences.prefActivatorActivityStartTargetHelpsFinished = !start;
        editor.putBoolean(ActivatorListFragment.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefActivatorFragmentStartTargetHelps = start;
        editor.putBoolean(ActivatorListFragment.PREF_START_TARGET_HELPS_FINISHED, !start);
        ApplicationPreferences.prefActivatorFragmentStartTargetHelpsFinished = !start;
        editor.putBoolean(ActivatorListAdapter.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefActivatorAdapterStartTargetHelps = start;
        editor.putBoolean(ActivatorListAdapter.PREF_START_TARGET_HELPS_FINISHED, !start);
        ApplicationPreferences.prefActivatorAdapterStartTargetHelpsFinished = !start;

        editor.putBoolean(EditorActivity.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefEditorActivityStartTargetHelps = start;
        editor.putBoolean(EditorActivity.PREF_START_TARGET_HELPS_RUN_STOP_INDICATOR, start);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsRunStopIndicator = start;
        editor.putBoolean(EditorActivity.PREF_START_TARGET_HELPS_BOTTOM_NAVIGATION, start);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsBottomNavigation = start;
        editor.putBoolean(EditorActivity.PREF_START_TARGET_HELPS_FINISHED, !start);
        ApplicationPreferences.prefEditorActivityStartTargetHelpsFinished = !start;

        editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelps = start;
        editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS_FILTER_SPINNER, start);
        ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelpsFilterSpinner = start;
        editor.putBoolean(EditorActivity.PREF_START_TARGET_HELPS_DEFAULT_PROFILE, start);
        ApplicationPreferences.prefEditorFragmentStartTargetHelpsDefaultProfile = start;
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS, start);
        editor.putBoolean(EditorProfileListFragment.PREF_START_TARGET_HELPS_FINISHED, !start);
        ApplicationPreferences.prefEditorProfilesFragmentStartTargetHelpsFinished = !start;
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelps = start;
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_ORDER, start);
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsOrder = start;
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_SHOW_IN_ACTIVATOR, start);
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsShowInActivator = start;
        editor.putBoolean(EditorProfileListAdapter.PREF_START_TARGET_HELPS_FINISHED, !start);
        ApplicationPreferences.prefEditorProfilesAdapterStartTargetHelpsFinished = !start;

        editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefEditorEventsFragmentStartTargetHelps = start;
        editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_FILTER_SPINNER, start);
        ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFilterSpinner = start;
        editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_ORDER_SPINNER, start);
        ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsOrderSpinner = start;
        editor.putBoolean(EditorEventListFragment.PREF_START_TARGET_HELPS_FINISHED, !start);
        ApplicationPreferences.prefEditorEventsFragmentStartTargetHelpsFinished = !start;
        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelps = start;
        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_ORDER, start);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsOrder = start;
        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_STATUS, start);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsStatus = start;
        editor.putBoolean(EditorEventListAdapter.PREF_START_TARGET_HELPS_FINISHED, !start);
        ApplicationPreferences.prefEditorEventsAdapterStartTargetHelpsFinished = !start;

        editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefProfilePrefsActivityStartTargetHelps = start;
        //editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS_SAVE, start);
        //ApplicationPreferences.prefProfilePrefsActivityStartTargetHelpsSave = start;
        editor.putBoolean(ProfilesPrefsActivity.PREF_START_TARGET_HELPS_FINISHED, !start);
        ApplicationPreferences.prefProfilePrefsActivityStartTargetHelpsFinished = !start;

        editor.putBoolean(EventsPrefsActivity.PREF_START_TARGET_HELPS, start);
        ApplicationPreferences.prefEventPrefsActivityStartTargetHelps = start;
        editor.putBoolean(EventsPrefsActivity.PREF_START_TARGET_HELPS_FINISHED, !start);
        ApplicationPreferences.prefEventPrefsActivityStartTargetHelpsFinished = !start;

        editor.apply();
    }

    static long getQuickTileProfileId(Context context, int tile) {
        SharedPreferences _preferences = getSharedPreferences(context);
        return _preferences.getLong(PREF_QUICK_TILE_PROFILE_ID + "_" + tile, 0);
    }

    static void setQuickTileProfileId(Context context, int tile, long profileId) {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        editor.putLong(PREF_QUICK_TILE_PROFILE_ID + "_" + tile, profileId);
        editor.apply();
    }

}
