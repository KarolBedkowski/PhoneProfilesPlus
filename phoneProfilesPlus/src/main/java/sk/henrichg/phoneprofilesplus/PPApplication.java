package sk.henrichg.phoneprofilesplus;

import static android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.Process;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.look.Slook;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.config.NotificationConfigurationBuilder;
import org.acra.data.StringFormat;
import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import dev.doubledot.doki.views.DokiContentView;
import me.drakeet.support.toast.ToastCompat;

public class PPApplication extends Application
                                        //implements Configuration.Provider
                                        //implements Application.ActivityLifecycleCallbacks
{
    // this version code must by <= version code in dependencies.gradle
    static final int PPP_VERSION_CODE_FOR_IMPORTANT_INFO_NEWS = 6752;
    static final boolean SHOW_IMPORTANT_INFO_NEWS = false;
    static final boolean SHOW_IMPORTANT_INFO_NOTIFICATION_NEWS = false;

    //static final int VERSION_CODE_EXTENDER_3_0 = 200;
    //static final int VERSION_CODE_EXTENDER_4_0 = 400;
    //static final int VERSION_CODE_EXTENDER_5_1_3_1 = 540;
    //static final int VERSION_CODE_EXTENDER_5_1_4_1 = 600;
    //static final int VERSION_CODE_EXTENDER_6_0 = 620;
    //static final int VERSION_CODE_EXTENDER_6_2 = 670;
    //static final int VERSION_CODE_EXTENDER_7_0 = 700;
    //static final int VERSION_CODE_EXTENDER_8_0 = 800;
    static final int VERSION_CODE_EXTENDER_LATEST = 850;
    static final String VERSION_NAME_EXTENDER_LATEST = "8.0.4.1";

    static final int VERSION_CODE_PPPPS_LATEST = 35;
    static final String VERSION_NAME_PPPPS_LATEST = "1.0.2";

    static final int pid = Process.myPid();
    static final int uid = Process.myUid();

    // import/export
    static final String DB_FILEPATH = "/data/" + PPApplication.PACKAGE_NAME + "/databases";
    //static final String REMOTE_EXPORT_PATH = "/PhoneProfiles";
    static final String EXPORT_APP_PREF_FILENAME = "ApplicationPreferences.backup";
    //static final String EXPORT_DEF_PROFILE_PREF_FILENAME = "DefaultProfilePreferences.backup";
    static final String SHARED_EXPORT_FILENAME = "phoneProfilesPlus_backup";
    static final String SHARED_EXPORT_FILEEXTENSION = ".zip";

    static boolean exportIsRunning = false;

    private static volatile PPApplication instance;
    private static volatile WorkManager workManagerInstance;

    static volatile boolean applicationFullyStarted = false;
    static volatile boolean normalServiceStart = false;
    static volatile boolean showToastForProfileActivation = false;

    // this for display of alert dialog when works not started at start of app
    //static long startTimeOfApplicationStart = 0;

    static final long APPLICATION_START_DELAY = 2 * 60 * 1000;
    static final int WORK_PRUNE_DELAY_DAYS = 1;
    static final int WORK_PRUNE_DELAY_MINUTES = 60;

    // urls
    static final String CROWDIN_URL = "https://crowdin.com/project/phoneprofilesplus";

    // This is file: https://github.com/henrichg/PhoneProfilesPlus/blob/master/docs/privacy_policy.md
    // Used is GitHub Pages, needed is use of html type, because this file is displayed in html browser
    static final String PRIVACY_POLICY_URL = "https://henrichg.github.io/PhoneProfilesPlus/privacy_policy.html";

    static final String GITHUB_PPP_RELEASES_URL = "https://github.com/henrichg/PhoneProfilesPlus/releases";
    static final String GITHUB_PPP_DOWNLOAD_URL = "https://github.com/henrichg/PhoneProfilesPlus/releases/latest/download/PhoneProfilesPlus.apk";

    static final String GITHUB_PPPE_RELEASES_URL = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases";
    static final String GITHUB_PPPE_DOWNLOAD_URL = "https://github.com/henrichg/PhoneProfilesPlusExtender/releases/latest/download/PhoneProfilesPlusExtender.apk";

    static final String GITHUB_PPP_URL = "https://github.com/henrichg/PhoneProfilesPlus";
    static final String GITHUB_PPPE_URL = "https://github.com/henrichg/PhoneProfilesPlusExtender";
    static final String GITHUB_PPPPS_URL = "https://github.com/henrichg/PPPPutSettings";
    static final String XDA_DEVELOPERS_PPP_URL = "https://forum.xda-developers.com/t/phoneprofilesplus.3799429/";

    static final String PAYPAL_DONATION_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=AF5QK49DMAL2U";

    // This is file: https://github.com/henrichg/PhoneProfilesPlus/blob/master/docs/releases_debug.md
    // Used is GitHub Pages, not neded to use html type, this file is directly downloaded
    static final String PPP_RELEASES_DEBUG_URL = "https://henrichg.github.io/PhoneProfilesPlus/releases-debug.md";
    // This is file: https://github.com/henrichg/PhoneProfilesPlus/blob/master/docs/releases.md
    // Used is GitHub Pages, not neded to use html type, this file is directly downloaded
    static final String PPP_RELEASES_URL = "https://henrichg.github.io/PhoneProfilesPlus/releases.md";

    static final String FDROID_PPP_RELEASES_URL = "https://apt.izzysoft.de/fdroid/index/apk/sk.henrichg.phoneprofilesplus";
    static final String FDROID_APPLICATION_URL = "https://www.f-droid.org/";
    static final String FDROID_REPOSITORY_URL = "https://apt.izzysoft.de/fdroid/index/info";

    //static final String AMAZON_APPSTORE_PPP_RELEASES_URL = "https://www.amazon.com/Henrich-Gron-PhoneProfilesPlus/dp/B01N3SM44J/ref=sr_1_1?keywords=phoneprofilesplus&qid=1637084235&qsid=134-9049988-7816540&s=mobile-apps&sr=1-1&sres=B01N3SM44J%2CB078K93HFD%2CB01LXZDPDR%2CB00LBK7OSY%2CB07RX5L3CP%2CB07XM7WVS8%2CB07XWGWPH5%2CB08KXB3R7S%2CB0919N2P7J%2CB08NWD7K8H%2CB01A7MACL2%2CB07XY8YFQQ%2CB07XM8GDWC%2CB07QVYLDRL%2CB09295KQ9Q%2CB01LVZ3JBI%2CB08723759H%2CB09728VTDK%2CB08R7D4KZJ%2CB01BUIGF9K";
    //static final String AMAZON_APPSTORE_APPLICATION_URL = "https://www.amazon.com/gp/mas/get/amazonapp";

    static final String APKPURE_PPP_RELEASES_URL = "https://m.apkpure.com/p/sk.henrichg.phoneprofilesplus";
    static final String APKPURE_APPLICATION_URL = "https://apkpure.com/apkpure/com.apkpure.aegon";

    static final String HUAWEI_APPGALLERY_PPP_RELEASES_URL = "https://appgallery.cloud.huawei.com/ag/n/app/C104501059?channelId=PhoneProfilesPlus+application&id=957ced9f0ca648df8f253a3d1460051e&s=79376612D7DD2C824692C162FB2F957A7AEE81EE1471CDC58034CD5106DAB009&detailType=0&v=&callType=AGDLINK&installType=0000";
    static final String HUAWEI_APPGALLERY_APPLICATION_URL = "https://consumer.huawei.com/en/mobileservices/appgallery/";

    //This file: https://github.com/henrichg/PhoneProfilesPlus/blob/master/docs/grant_g1_permission.md
    static final String HELP_HOW_TO_GRANT_G1_URL = "https://henrichg.github.io/PhoneProfilesPlus/grant_g1_permission.html";
    static final String HELP_HOW_TO_GRANT_G1_URL_DEVEL = "https://github.com/henrichg/PhoneProfilesPlus/blob/devel/docs/grant_g1_permission.md";

    //This file: https://github.com/henrichg/PhoneProfilesPlus/blob/master/docs/wifi_scan_throttling.md
    static final String HELP_WIFI_SCAN_THROTTLING = "https://henrichg.github.io/PhoneProfilesPlus/wifi_scan_throttling.html";
    static final String HELP_WIFI_SCAN_THROTTLING_DEVEL = "https://github.com/henrichg/PhoneProfilesPlus/blob/devel/docs/wifi_scan_throttling.md";

    //This file: https://github.com/henrichg/PhoneProfilesPlus/blob/master/docs/airplane_mode_radios_config.md
    static final String HELP_AIRPLANE_MODE_RADIOS_CONFIG = "https://henrichg.github.io/PhoneProfilesPlus/airplane_mode_radios_config.html";
    static final String HELP_AIRPLANE_MODE_RADIOS_CONFIG_DEVEL = "https://github.com/henrichg/PhoneProfilesPlus/blob/devel/docs/airplane_mode_radios_config.md";

    static final String DROIDIFY_PPP_RELEASES_URL = "https://apt.izzysoft.de/fdroid/index/apk/sk.henrichg.phoneprofilesplus";
    static final String DROIDIFY_APPLICATION_URL = "https://apt.izzysoft.de/fdroid/index/apk/com.looker.droidify";

    static final String GITHUB_PPPPS_RELEASES_URL = "https://github.com/henrichg/PPPPutSettings/releases";
    static final String GITHUB_PPPPS_DOWNLOAD_URL = "https://github.com/henrichg/PPPPutSettings/releases/latest/download/PPPPutSettings.apk";

    static final String GALAXY_STORE_PPP_RELEASES_URL = "https://galaxystore.samsung.com/detail/sk.henrichg.phoneprofilesplus";

    //static final boolean gitHubRelease = true;
    //static boolean googlePlayInstaller = false;

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean logIntoLogCat = true && DebugVersion.enabled;
    //TODO change it back to not log crash for releases
    static final boolean logIntoFile = false;
    @SuppressWarnings("PointlessBooleanExpression")
    static final boolean crashIntoFile = false && DebugVersion.enabled;
    static final boolean rootToolsDebug = false;
    private static final String logFilterTags = "##### PPApplication.onCreate"
                                                //+"|PPApplication.isXiaomi"
                                                //+"|PPApplication.isHuawei"
                                                //+"|PPApplication.isSamsung"
                                                //+"|PPApplication.isLG"
                                                //+"|PPApplication.getEmuiRomName"
                                                //+"|PPApplication.isEMUIROM"
                                                //+"|PPApplication.isMIUIROM"
                                                //+"|PPApplication.attachBaseContext"
                                                //+"|PPApplication.startPPServiceWhenNotStarted"
                                                +"|PPApplication.exitApp"
                                                +"|PPApplication._exitApp"
                                                //+"|PPApplication.createPPPAppNotificationChannel"
                                                //+"|AvoidRescheduleReceiverWorker"
                                                +"|PhoneProfilesService.onCreate"
                                                +"|PhoneProfilesService.onStartCommand"
                                                +"|PhoneProfilesService.doForFirstStart"
                                                +"|PhoneProfilesService.doForPackageReplaced"
                                                +"|MainWorker.doAfterFirstStart"
                                                //+"|GlobalUtils.getServiceInfo"
                                                //+"|PhoneProfilesService.isServiceRunning"
                                                +"|PackageReplacedReceiver.onReceive"
                                                //+"|PhoneProfilesService.doCommand"
                                                //+"|PPPAppNotification.showNotification"
                                                //+"|PPPAppNotification._showNotification"
                                                //+"|[CUST] PPPAppNotification._showNotification"
                                                //+"|PhoneProfilesService.onConfigurationChanged"
                                                //+"|PhoneProfilesService.stopReceiver"
                                                //+"|PhoneProfilesService.onTaskRemoved"
                                                +"|PhoneProfilesService.onDestroy"
                                                //+"|PhoneProfilesService.cancelWork"
                                                +"|DataWrapper.firstStartEvents"
                                                //+"|DataWrapper.setProfileActive"
                                                //+"|DataWrapper.activateProfileOnBoot"
                                                +"|BootUpReceiver"
                                                //+"|PhoneProfilesBackupAgent"
                                                +"|ShutdownBroadcastReceiver"
                                                +"|DatabaseHandler.onUpgrade"
                                                //+"|IgnoreBatteryOptimizationNotification"
                                                //+"|LauncherActivity.startPPServiceWhenNotStarted"
                                                //+"|PPApplication.updateGUI"
                                                //+"|DatabaseHandler.onCreate"
                                                //+"|DatabaseHandler.createTableColumsWhenNotExists"
                                                //+"|TopExceptionHandler.uncaughtException"
                                                //+"|ImportantInfoNotification"
                                                //+"|ImportantInfoHelpFragment"

//                                                +"|[IN_WORKER]"
//                                                +"|[WORKER_CALL]"
//                                                +"|[IN_EXECUTOR]"
//                                                +"|[EXECUTOR_CALL]"
//                                                +"|[IN_THREAD_HANDLER]"
//                                                +"|[IN_BROADCAST]"
//                                                +"|[IN_BROADCAST_ALARM]"
//                                                +"|[LOCAL_BROADCAST_CALL]"
//                                                +"|[IN_OBSERVER]"
//                                                +"|[IN_LISTENER]"
//                                                +"|[IN_EVENTS_HANDLER]"
//                                                +"|[EVENTS_HANDLER_CALL]"
//                                                +"|[TEST BATTERY]"
//                                                +"|[APP_START]"
//                                                +"|[HANDLER]"
                                                //+"|[SHEDULE_WORK]"
                                                //+"|[SHEDULE_SCANNER]"
                                                //+"|[TEST MEDIA VOLUME]"
                                                //+"|[TEST_BLOCK_PROFILE_EVENTS_ACTIONS]"
                                                //+"|[FIFO_TEST]"
                                                //+"|[BLOCK_ACTIONS]"
                                                //+"|[ACTIVATOR]"
                                                //+"|[G1_TEST]"
                                                //+"|[BACKGROUND_ACTIVITY]"
                                                //+"|[START_PP_SERVICE]"
                                                //+"|[BRS]"
                                                //+"|[CONNECTIVITY_TEST]"
                                                //+"|[BRIGHTNESS]"
                                                //+"|[BRSD]"
                                                //+"|[ROOT]"
                                                //+"|[DB_LOCK]"
                                                //+"|[WIFI]"
                                                //+"|[VOLUMES]"
                                                //+"|[PPP_NOTIFICATION]"
                                                //+"|[DUAL_SIM]"
                                                //+"|[APPLICATION_FULLY_STARTED]"

                                                //+"|EventPreferencesOrientation"
                                                //+"|LocationScanner.updateTransitionsByLastKnownLocation"
                                                ;

    static final int ACTIVATED_PROFILES_FIFO_SIZE = 20;

    // activity log types
    static final int ALTYPE_UNDEFINED = 0;

    static final int ALTYPE_PROFILE_ACTIVATION = 1;
    static final int ALTYPE_APPLICATION_EXIT = 10;
    static final int ALTYPE_DATA_IMPORT = 11;
    static final int ALTYPE_PAUSED_LOGGING = 12;
    static final int ALTYPE_STARTED_LOGGING = 13;
    static final int ALTYPE_EVENT_END_DELAY = 14;
    static final int ALTYPE_EVENT_STOP = 15;
    static final int ALTYPE_APPLICATION_START_ON_BOOT = 16;
    static final int ALTYPE_EVENT_PREFERENCES_CHANGED = 17;
    static final int ALTYPE_EVENT_DELETED = 18;
    static final int ALTYPE_PROFILE_DELETED = 19;

    static final int ALTYPE_MERGED_PROFILE_ACTIVATION = 2;
    static final int ALTYPE_MANUAL_RESTART_EVENTS = 20;
    static final int ALTYPE_AFTER_DURATION_UNDO_PROFILE = 21;
    static final int ALTYPE_AFTER_DURATION_DEFAULT_PROFILE = 22;
    static final int ALTYPE_AFTER_DURATION_RESTART_EVENTS = 23;

    static final int ALTYPE_EVENT_START = 3;
    static final int ALTYPE_PROFILE_PREFERENCES_CHANGED = 30;
    static final int ALTYPE_SHARED_PROFILE_PREFERENCES_CHANGED = 31;
    static final int ALTYPE_ALL_EVENTS_DELETED = 32;
    static final int ALTYPE_ALL_PROFILES_DELETED = 33;
    static final int ALTYPE_APPLICATION_UPGRADE = 34;
    static final int ALTYPE_AFTER_DURATION_SPECIFIC_PROFILE = 35;

    static final int ALTYPE_EVENT_START_DELAY = 4;

    static final int ALTYPE_EVENT_END_NONE = 51;
    static final int ALTYPE_EVENT_END_ACTIVATE_PROFILE = 52;
    static final int ALTYPE_EVENT_END_UNDO_PROFILE = 53;
    static final int ALTYPE_EVENT_END_ACTIVATE_PROFILE_UNDO_PROFILE = 54;
    static final int ALTYPE_EVENT_END_RESTART_EVENTS = 55;
    static final int ALTYPE_EVENT_END_ACTIVATE_PROFILE_RESTART_EVENTS = 56;
    static final int ALTYPE_AFTER_END_OF_ACTIVATION_UNDO_PROFILE = 57;
    static final int ALTYPE_AFTER_END_OF_ACTIVATION_DEFAULT_PROFILE = 58;
    static final int ALTYPE_AFTER_END_OF_ACTIVATION_RESTART_EVENTS = 59;
    static final int ALTYPE_AFTER_END_OF_ACTIVATION_SPECIFIC_PROFILE = 60;

    static final int ALTYPE_RESTART_EVENTS = 6;
    static final int ALTYPE_RUN_EVENTS_DISABLE = 7;
    static final int ALTYPE_RUN_EVENTS_ENABLE = 8;
    static final int ALTYPE_APPLICATION_START = 9;

    static final int ALTYPE_PROFILE_ERROR_RUN_APPLICATION_APPLICATION = 1000;
    static final int ALTYPE_PROFILE_ERROR_RUN_APPLICATION_SHORTCUT = 1001;
    static final int ALTYPE_PROFILE_ERROR_RUN_APPLICATION_INTENT = 1002;
    static final int ALTYPE_PROFILE_ERROR_SET_TONE_RINGTONE = 1003;
    static final int ALTYPE_PROFILE_ERROR_SET_TONE_NOTIFICATION = 1004;
    static final int ALTYPE_PROFILE_ERROR_SET_TONE_ALARM = 1005;
    static final int ALTYPE_PROFILE_ERROR_SET_WALLPAPER = 1006;
    static final int ALTYPE_PROFILE_ERROR_SET_VPN = 1007;
    static final int ALTYPE_PROFILE_ERROR_CAMERA_FLASH = 1008;

    static final int ALTYPE_DATA_IMPORT_FROM_PP = 100;
    static final int ALTYPE_DATA_EXPORT = 101;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_PROFILE_ACTIVATION = 102;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_RESTART_EVENTS = 103;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_ENABLE_RUN_FOR_EVENT = 104;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_PAUSE_EVENT = 105;
    static final int ALTYPE_ACTION_FROM_EXTERNAL_APP_STOP_EVENT = 106;
    static final int ALTYPE_APPLICATION_SYSTEM_RESTART = 107;
    static final int ALTYPE_PROFILE_ADDED = 108;
    static final int ALTYPE_EVENT_ADDED = 109;

    //static volatile boolean doNotShowPPPAppNotification = false;
    private volatile static boolean applicationStarted = false;
    static volatile boolean globalEventsRunStop = true;
    //static volatile boolean applicationPackageReplaced = false;
    static volatile boolean deviceBoot = false;

    //static final boolean restoreFinished = true;

    static volatile Collator collator = null;

    static volatile boolean lockRefresh = false;
    //static volatile long lastRefreshOfGUI = 0;
    //static volatile long lastRefreshOfPPPAppNotification = 0;

    //static final int DURATION_FOR_GUI_REFRESH = 500;
    //static final String EXTRA_REFRESH_ALSO_EDITOR = "refresh_also_editor";
    //static final String EXTRA_REFRESH = "refresh";

    static final List<String> elapsedAlarmsProfileDurationWork = new ArrayList<>();
    static final List<String> elapsedAlarmsRunApplicationWithDelayWork = new ArrayList<>();
    static final List<String> elapsedAlarmsEventDelayStartWork = new ArrayList<>();
    static final List<String> elapsedAlarmsEventDelayEndWork = new ArrayList<>();
    static final List<String> elapsedAlarmsStartEventNotificationWork = new ArrayList<>();

    static final ApplicationPreferencesMutex applicationPreferencesMutex = new ApplicationPreferencesMutex();
    static final ApplicationGlobalPreferencesMutex applicationGlobalPreferencesMutex = new ApplicationGlobalPreferencesMutex();
    static final ApplicationStartedMutex applicationStartedMutex = new ApplicationStartedMutex();
    static final ProfileActivationMutex profileActivationMutex = new ProfileActivationMutex();
    static final GlobalEventsRunStopMutex globalEventsRunStopMutex = new GlobalEventsRunStopMutex();
    static final EventsRunMutex eventsRunMutex = new EventsRunMutex();
    static final EventCallSensorMutex eventCallSensorMutex = new EventCallSensorMutex();
    static final EventAccessoriesSensorMutex eventAccessoriesSensorMutex = new EventAccessoriesSensorMutex();
    static final EventWifiSensorMutex eventWifiSensorMutex = new EventWifiSensorMutex();
    static final EventBluetoothSensorMutex eventBluetoothSensorMutex = new EventBluetoothSensorMutex();
    static final ContactsCacheMutex contactsCacheMutex = new ContactsCacheMutex();
    static final PhoneProfilesServiceMutex phoneProfilesServiceMutex = new PhoneProfilesServiceMutex();
    static final RootMutex rootMutex = new RootMutex();
    static final ServiceListMutex serviceListMutex = new ServiceListMutex();
    //static final RadioChangeStateMutex radioChangeStateMutex = new RadioChangeStateMutex();
    static final ShowPPPNotificationMutex showPPPNotificationMutex = new ShowPPPNotificationMutex();
    static final LocationScannerLastLocationMutex locationScannerLastLocationMutex = new LocationScannerLastLocationMutex();
    static final LocationScannerMutex locationScannerMutex = new LocationScannerMutex();
    static final WifiScannerMutex wifiScannerMutex = new WifiScannerMutex();
    static final WifiScanResultsMutex wifiScanResultsMutex = new WifiScanResultsMutex();
    static final BluetoothConnectionChangeStateMutex bluetoothConnectionChangeStateMutex = new BluetoothConnectionChangeStateMutex();
    static final BluetoothScannerMutex bluetoothScannerMutex = new BluetoothScannerMutex();
    static final BluetoothScanResultsMutex bluetoothScanResultsMutex = new BluetoothScanResultsMutex();
    static final BluetoothCLScanMutex bluetoothCLScanMutex = new BluetoothCLScanMutex();
    static final BluetoothLEScanMutex bluetoothLEScanMutex = new BluetoothLEScanMutex();
    static final EventsHandlerMutex eventsHandlerMutex = new EventsHandlerMutex();
    static final MobileCellsScannerMutex mobileCellsScannerMutex = new MobileCellsScannerMutex();
    static final OrientationScannerMutex orientationScannerMutex = new OrientationScannerMutex();
    static final TwilightScannerMutex twilightScannerMutex = new TwilightScannerMutex();
    static final NotUnlinkVolumesMutex notUnlinkVolumesMutex = new NotUnlinkVolumesMutex();
    static final EventRoamingSensorMutex eventRoamingSensorMutex = new EventRoamingSensorMutex();
    static final ApplicationCacheMutex applicationCacheMutex = new ApplicationCacheMutex();
    static final ProfileListWidgetDatasetChangedMutex profileListWidgetDatasetChangedMutex = new ProfileListWidgetDatasetChangedMutex();
    static final SamsungEdgeDatasetChangedMutex samsungEdgeDatasetChangedMutex = new SamsungEdgeDatasetChangedMutex();

    //static PowerManager.WakeLock keepScreenOnWakeLock;

    //static final String romManufacturer = getROMManufacturer();
    static final boolean deviceIsXiaomi = isXiaomi();
    static final boolean deviceIsHuawei = isHuawei();
    static final boolean deviceIsSamsung = isSamsung();
    static final boolean deviceIsLG = isLG();
    static final boolean deviceIsOnePlus = isOnePlus();
    static final boolean deviceIsOppo = isOppo();
    static final boolean deviceIsRealme = isRealme();
    static final boolean deviceIsLenovo = isLenovo();
    static final boolean deviceIsPixel = isPixel();
    static final boolean deviceIsSony = isSony();
    static final boolean deviceIsDoogee = isDoogee();
    static final boolean romIsMIUI = isMIUIROM();
    static final boolean romIsEMUI = isEMUIROM();
    static final boolean romIsGalaxy = isGalaxyROM();

    static volatile boolean HAS_FEATURE_BLUETOOTH_LE = false;
    static volatile boolean HAS_FEATURE_WIFI = false;
    static volatile boolean HAS_FEATURE_BLUETOOTH = false;
    static volatile boolean HAS_FEATURE_TELEPHONY = false;
    static volatile boolean HAS_FEATURE_NFC = false;
    static volatile boolean HAS_FEATURE_LOCATION = false;
    static volatile boolean HAS_FEATURE_LOCATION_GPS = false;
    static volatile boolean HAS_FEATURE_CAMERA_FLASH = false;

    static final String PACKAGE_NAME = "sk.henrichg.phoneprofilesplus";
    static final String PACKAGE_NAME_EXTENDER = "sk.henrichg.phoneprofilesplusextender";
    static final String PACKAGE_NAME_PP = "sk.henrichg.phoneprofiles";
    static final String PACKAGE_NAME_PPPPS = "sk.henrichg.pppputsettings";

    public static final String EXPORT_PATH = "/PhoneProfilesPlus";
    static final String LOG_FILENAME = "log.txt";

    static final String EXTRA_PROFILE_ID = "profile_id";
    static final String EXTRA_EVENT_ID = "event_id";
    static final String EXTRA_STARTUP_SOURCE = "startup_source";
    static final String EXTRA_EVENT_STATUS = "event_status";
    static final String EXTRA_APPLICATION_START = "application_start";
    static final String EXTRA_DEVICE_BOOT = "device_boot";

    static final int STARTUP_SOURCE_NOTIFICATION = 1;
    static final int STARTUP_SOURCE_WIDGET = 2;
    static final int STARTUP_SOURCE_SHORTCUT = 3;
    static final int STARTUP_SOURCE_FOR_FIRST_START = 4;
    static final int STARTUP_SOURCE_ACTIVATOR = 5;
    static final int STARTUP_SOURCE_EVENT = 6;
    static final int STARTUP_SOURCE_EDITOR = 8;
    static final int STARTUP_SOURCE_ACTIVATOR_START = 9;
    //static final int STARTUP_SOURCE_LAUNCHER_START = 10;
    static final int STARTUP_SOURCE_LAUNCHER = 11;
    static final int STARTUP_SOURCE_EVENT_MANUAL = 12;
    static final int STARTUP_SOURCE_EXTERNAL_APP = 13;
    static final int STARTUP_SOURCE_QUICK_TILE = 14;
    static final int STARTUP_SOURCE_EDITOR_SHOW_IN_ACTIVATOR_FILTER = 15;
    static final int STARTUP_SOURCE_EDITOR_SHOW_IN_EDITOR_FILTER = 16;
    static final int STARTUP_SOURCE_EDITOR_WIDGET_HEADER = 17;

    //static final int PREFERENCES_STARTUP_SOURCE_ACTIVITY = 1;
    //static final int PREFERENCES_STARTUP_SOURCE_FRAGMENT = 2;
    //static final int PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE = 3;

    static final String PROFILE_NOTIFICATION_CHANNEL = "phoneProfilesPlus_activated_profile";
    static final String MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_mobile_cells_registration";
    static final String INFORMATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_information";
    static final String EXCLAMATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_exclamation";
    static final String GRANT_PERMISSION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_grant_permission";
    static final String NOTIFY_EVENT_START_NOTIFICATION_CHANNEL = "phoneProfilesPlus_repeat_notify_event_start";
    static final String NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL = "phoneProfilesPlus_new_mobile_cell";
    static final String DONATION_NOTIFICATION_CHANNEL = "phoneProfilesPlus_donation";
    static final String NEW_RELEASE_NOTIFICATION_CHANNEL = "phoneProfilesPlus_newRelease";
    //static final String CRASH_REPORT_NOTIFICATION_CHANNEL = "phoneProfilesPlus_crash_report";
    static final String GENERATED_BY_PROFILE_NOTIFICATION_CHANNEL = "phoneProfilesPlus_generatedByProfile";
    static final String KEEP_SCREEN_ON_NOTIFICATION_CHANNEL = "phoneProfilesPlus_keepScreenOn";
    static final String PROFILE_LIST_NOTIFICATION_CHANNEL = "phoneProfilesPlus_profileList";

    static final int PROFILE_NOTIFICATION_ID = 100;
    static final int PROFILE_NOTIFICATION_NATIVE_ID = 500;
    static final String PROFILE_NOTIFICATION_GROUP = PACKAGE_NAME+"_ACTIVATED_PROFILE_NOTIFICATION_GROUP";

    static final int IMPORTANT_INFO_NOTIFICATION_ID = 101;
    static final String IMPORTANT_INFO_NOTIFICATION_TAG = PACKAGE_NAME+"_IMPORTANT_INFO_NOTIFICATION";
    static final String IMPORTANT_INFO_NOTIFICATION_EXTENDER_TAG = PACKAGE_NAME+"_IMPORTANT_INFO_NOTIFICATION_EXTENDER";
    static final String IMPORTANT_INFO_NOTIFICATION_PPPPS_TAG = PACKAGE_NAME+"_IMPORTANT_INFO_NOTIFICATION_PPPPS";
    static final int GRANT_PROFILE_PERMISSIONS_NOTIFICATION_ID = 102;
    static final String GRANT_PROFILE_PERMISSIONS_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_PROFILE_PERMISSIONS_NOTIFICATION";
    static final int GRANT_EVENT_PERMISSIONS_NOTIFICATION_ID = 104;
    static final String GRANT_EVENT_PERMISSIONS_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_EVENT_PERMISSIONS_NOTIFICATION";
    static final int GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_ID = 108;
    static final String GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION_TAG = PACKAGE_NAME+"_GRANT_PLAY_RINGTONE_NOTIFICATION_PERMISSIONS_NOTIFICATION";
    static final String GRANT_PERMISSIONS_NOTIFICATION_GROUP = PACKAGE_NAME+"_GRANT_PERMISSIONS_NOTIFICATION_GROUP";

    static final int MOBILE_CELLS_REGISTRATION_SERVICE_NOTIFICATION_ID = 109;
    static final int MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_ID = 117;
    static final String MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_TAG = PACKAGE_NAME+"_MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION";
    static final String MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_GROUP = PACKAGE_NAME+"_MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_GROUP";

    static final int ABOUT_APPLICATION_DONATE_NOTIFICATION_ID = 110;
    static final String ABOUT_APPLICATION_DONATE_NOTIFICATION_TAG = PACKAGE_NAME+"_ABOUT_APPLICATION_DONATE_NOTIFICATION";

    static final int ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID = 111;
    static final String ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_TAG = PACKAGE_NAME+"_ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION";
    static final String ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_GROUP = PACKAGE_NAME+"_ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_GROUP";

    static final int PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_ID = 113;
    static final String PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_MOBILE_DATA_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_ID = 114;
    static final String PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_LOCATION_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_ID = 115;
    static final String PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_WIFI_AP_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_ID = 116;
    static final String PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_NETWORK_TYPE_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_LIVE_WALLPAPER_NOTIFICATION_ID = 140;
    static final String PROFILE_ACTIVATION_LIVE_WALLPAPER_NOTIFICATION_TAG = PACKAGE_NAME+"PROFILE_ACTIVATION_LIVE_WALLPAPER_NOTIFICATION";
    static final int PROFILE_ACTIVATION_VPN_SETTINGS_PREFS_NOTIFICATION_ID = 141;
    static final String PROFILE_ACTIVATION_VPN_SETTINGS_PREFS_NOTIFICATION_TAG = PACKAGE_NAME+"PROFILE_ACTIVATION_VPN_SETTINGS_PREFS_NOTIFICATION";
    static final int PROFILE_ACTIVATION_WALLPAPER_WITH_NOTIFICATION_ID = 143;
    static final String PROFILE_ACTIVATION_WALLPAPER_WITH_NOTIFICATION_TAG = PACKAGE_NAME+"PROFILE_ACTIVATION_WALLPAPER_WITH_NOTIFICATION";
    static final String PROFILE_ACTIVATION_PREFS_NOTIFICATION_GROUP = PACKAGE_NAME+"_PROFILE_ACTIVATION_PREFS_NOTIFICATION_GROUP";

    static final int IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID = 120;
    static final String IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_TAG = PACKAGE_NAME+"_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION";
    static final int DRAW_OVER_APPS_NOTIFICATION_ID = 121;
    static final String DRAW_OVER_APPS_NOTIFICATION_TAG = PACKAGE_NAME+"_DRAW_OVER_APPS_NOTIFICATION";
    static final int EXTENDER_ACCESSIBILITY_SERVICE_NOT_ENABLED_NOTIFICATION_ID = 134;
    static final String EXTENDER_ACCESSIBILITY_SERVICE_NOT_ENABLED_NOTIFICATION_TAG = PACKAGE_NAME+"EXTENDER_ACCESSIBILITY_SERVICE_NOT_ENABLED_NOTIFICATION";
    static final int AUTOSTART_PERMISSION_NOTIFICATION_ID = 150;
    static final String AUTOSTART_PERMISSION_NOTIFICATION_TAG = PACKAGE_NAME+"_AUTOSTART_PERMISSION_NOTIFICATION";
    static final int LOCATION_NOT_WORKING_NOTIFICATION_ID = 151;
    static final String LOCATION_NOT_WORKING_NOTIFICATION_TAG = PACKAGE_NAME+"_LOCATION_NOT_WORKING_NOTIFICATION_NOTIFICATION";
    static final String SYTEM_CONFIGURATION_ERRORS_NOTIFICATION_GROUP = PACKAGE_NAME+"_SYTEM_CONFIGURATION_ERRORS_NOTIFICATION_GROUP";

    static final int CHECK_GITHUB_RELEASES_NOTIFICATION_ID = 122;
    static final String CHECK_GITHUB_RELEASES_NOTIFICATION_TAG = PACKAGE_NAME+"_CHECK_GITHUB_RELEASES_NOTIFICATION_TAG";
    static final int CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_ID = 124;
    static final String CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_TAG = PACKAGE_NAME+"_CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_TAG";
    static final int CHECK_REQUIRED_EXTENDER_RELEASES_NOTIFICATION_ID = 125;
    static final String CHECK_REQUIRED_EXTENDER_RELEASES_NOTIFICATION_TAG = PACKAGE_NAME+"_CHECK_REQUIRED_EXTENDER_RELEASES_NOTIFICATION_TAG";
    static final int CHECK_LATEST_PPPPS_RELEASES_NOTIFICATION_ID = 126;
    static final String CHECK_LATEST_PPPPS_RELEASES_NOTIFICATION_TAG = PACKAGE_NAME+"_CHECK_LATEST_PPPPS_RELEASES_NOTIFICATION_TAG";
    static final String CHECK_RELEASES_GROUP = PACKAGE_NAME+"_CHECK_RELEASES_GROUP";

    static final int PROFILE_ACTIVATION_ERROR_NOTIFICATION_ID = 130;
    static final String PROFILE_ACTIVATION_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION_ID = 131;
    static final String PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_WIFI_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION_ID = 132;
    static final String PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_WIFI_AP_ERROR_NOTIFICATION";
    static final int PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION_ID = 133;
    static final String PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_ACTIVATION_CLOSE_ALL_APPLICATIONS_ERROR_NOTIFICATION";
    static final String PROFILE_ACTIVATION_ERRORS_NOTIFICATION_GROUP = PACKAGE_NAME+"_PROFILE_ACTIVATION_ERRORS_NOTIFICATION_GROUP";

    static final int KEEP_SCREEN_ON_NOTIFICATION_ID = 142;
    static final String KEEP_SCREEN_ON_NOTIFICATION_TAG = PACKAGE_NAME+"_KEEP_SCREEN_ON_NOTIFICATION";
    static final String KEEP_SCREEN_ON_NOTIFICATION_GROUP = PACKAGE_NAME+"_KEEP_SCREEN_ON_NOTIFICATION_GROUP";

    static final int PROFILE_LIST_NOTIFICATION_ID = 550;
    static final String PROFILE_LIST_NOTIFICATION_TAG = PACKAGE_NAME+"_PROFILE_LIST_NOTIFICATION";
    static final String PROFILE_LIST_NOTIFICATION_GROUP = PACKAGE_NAME+"_PROFILE_LIST_NOTIFICATION_GROUP";

    //last notification id = 151

    // notifications have also tag, in it is tag name + profile/event/mobile cells id
    static final int PROFILE_ID_NOTIFICATION_ID = 1000;
    static final int EVENT_ID_NOTIFICATION_ID = 1000;

    static final int NOTIFY_EVENT_START_NOTIFICATION_ID = 1000;
    static final String NOTIFY_EVENT_START_NOTIFICATION_GROUP = PACKAGE_NAME+"_NOTIFY_EVENT_START_NOTIFICATION_GROUP";

    static final int NEW_MOBILE_CELLS_NOTIFICATION_ID = 1000;

    static final int GENERATED_BY_PROFILE_NOTIFICATION_ID = 10000;

    static final String DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG = PPApplication.PACKAGE_NAME+"_DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION";
    static final String DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION_TAG = PPApplication.PACKAGE_NAME+"_DISPLAY_PREFERENCES_EVENT_ERROR_NOTIFICATION";
    static final String NOTIFY_EVENT_START_NOTIFICATION_TAG = PPApplication.PACKAGE_NAME+"_NOTIFY_EVENT_START_NOTIFICATION";
    static final String NEW_MOBILE_CELLS_NOTIFICATION_TAG = PPApplication.PACKAGE_NAME+"_NEW_MOBILE_CELLS_NOTIFICATION";
    static final String GENERATED_BY_PROFILE_NOTIFICATION_TAG = PACKAGE_NAME+"_GENERATED_BY_PROFILE_NOTIFICATION_TAG";

    // shared preferences names !!! Configure also in res/xml/phoneprofiles_backup_scheme.xml !!!
    //static final String ACRA_PREFS_NAME = "phone_profiles_plus_acra";
    static final String APPLICATION_PREFS_NAME = "phone_profile_preferences";
    //static final String SHARED_PROFILE_PREFS_NAME = "profile_preferences_default_profile";
    //static final String ACTIVATED_PROFILE_PREFS_NAME = "profile_preferences_activated_profile";
    static final String WIFI_CONFIGURATION_LIST_PREFS_NAME = "wifi_configuration_list";
    static final String WIFI_SCAN_RESULTS_PREFS_NAME = "wifi_scan_results";
    static final String BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME = "bluetooth_connected_devices";
    static final String BLUETOOTH_BOUNDED_DEVICES_LIST_PREFS_NAME = "bluetooth_bounded_devices_list";
    static final String BLUETOOTH_CL_SCAN_RESULTS_PREFS_NAME = "bluetooth_cl_scan_results";
    static final String BLUETOOTH_LE_SCAN_RESULTS_PREFS_NAME = "bluetooth_le_scan_results";
    //static final String PERMISSIONS_PREFS_NAME = "permissions_list";
    static final String PERMISSIONS_STATUS_PREFS_NAME = "permissions_status";
    //static final String POSTED_NOTIFICATIONS_PREFS_NAME = "posted_notifications";
    static final String ACTIVATED_PROFILES_FIFO_PREFS_NAME = "activated_profiles_fifo";

    //public static final String RESCAN_TYPE_SCREEN_ON = "1";
    //public static final String RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS = "3";

    // global internal preferences
    private static final String PREF_APPLICATION_STARTED = "applicationStarted";
    private static final String PREF_ACTIVITY_LOG_ENABLED = "activity_log_enabled";
    private static final String PREF_SAVED_VERSION_CODE = "saved_version_code";
    private static final String PREF_DAYS_AFTER_FIRST_START = "days_after_first_start";
    private static final String PREF_DONATION_NOTIFICATION_COUNT = "donation_notification_count";
    private static final String PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION = "days_for_next_donation_notification";
    private static final String PREF_DONATION_DONATED = "donation_donated";
    //private static final String PREF_NOTIFICATION_PROFILE_NAME = "notification_profile_name";
    //private static final String PREF_WIDGET_PROFILE_NAME = "widget_profile_name";
    //private static final String PREF_ACTIVITY_PROFILE_NAME = "activity_profile_name";
    private static final String PREF_LAST_ACTIVATED_PROFILE = "last_activated_profile";
    private static final String PREF_WALLPAPER_CHANGE_TIME = "wallpaper_change_time";

    //static final String BUNDLE_WIDGET_TYPE = PACKAGE_NAME +"_BUNDLE_WIDGET_TYPE";
    //static final int WIDGET_TYPE_ICON = 1;
    //static final int WIDGET_TYPE_ONE_ROW = 2;
    //static final int WIDGET_TYPE_LIST = 3;

    // WorkManager tags
    static final String AFTER_FIRST_START_WORK_TAG = "afterFirstStartWork";
    //static final String PACKAGE_REPLACED_WORK_TAG = "packageReplacedWork";
    static final String AVOID_RESCHEDULE_RECEIVER_WORK_TAG = "avoidRescheduleReceiverWorker";

    // scanner start/stop types
    //static final int SCANNER_START_LOCATION_SCANNER = 1;
    //static final int SCANNER_STOP_LOCATION_SCANNER = 2;
    static final int SCANNER_RESTART_LOCATION_SCANNER = 3;

    //static final int SCANNER_START_ORIENTATION_SCANNER = 4;
    //static final int SCANNER_STOP_ORIENTATION_SCANNER = 5;
    //static final int SCANNER_FORCE_START_ORIENTATION_SCANNER = 5;
    static final int SCANNER_RESTART_ORIENTATION_SCANNER = 6;

    //static final int SCANNER_START_MOBILE_CELLS_SCANNER = 7;
    //static final int SCANNER_STOP_MOBILE_CELLS_SCANNER = 8;
    static final int SCANNER_FORCE_START_MOBILE_CELLS_SCANNER = 9;
    static final int SCANNER_RESTART_MOBILE_CELLS_SCANNER = 10;

    static final int SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER = 11;
    static final int SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER = 12;
    static final int SCANNER_RESTART_WIFI_SCANNER = 13;

    static final int SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER = 14;
    static final int SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER = 15;
    static final int SCANNER_RESTART_BLUETOOTH_SCANNER = 16;

    //static final int SCANNER_START_TWILIGHT_SCANNER = 17;
    //static final int SCANNER_STOP_TWILIGHT_SCANNER = 18;
    static final int SCANNER_RESTART_TWILIGHT_SCANNER = 19;
    static final int SCANNER_RESTART_PERIODIC_SCANNING_SCANNER = 20;
    static final int SCANNER_RESTART_NOTIFICATION_SCANNER = 21;

    static final int SCANNER_RESTART_ALL_SCANNERS = 50;

    //static final String EXTENDER_ACCESSIBILITY_SERVICE_ID = "sk.henrichg.phoneprofilesplusextender/.PPPEAccessibilityService";
    static final String EXTENDER_ACCESSIBILITY_PACKAGE_NAME = "sk.henrichg.phoneprofilesplusextender";

    static final String ACTION_PPPEXTENDER_STARTED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_PPPEXTENDER_STARTED";
    //static final String ACTION_ACCESSIBILITY_SERVICE_IS_CONNECTED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_ACCESSIBILITY_SERVICE_IS_CONNECTED";
    static final String ACTION_ACCESSIBILITY_SERVICE_CONNECTED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_ACCESSIBILITY_SERVICE_CONNECTED";
    static final String ACTION_ACCESSIBILITY_SERVICE_UNBIND = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_ACCESSIBILITY_SERVICE_UNBIND";
    static final String ACTION_FOREGROUND_APPLICATION_CHANGED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_FOREGROUND_APPLICATION_CHANGED";
    static final String ACTION_REGISTER_PPPE_FUNCTION = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_REGISTER_PPPE_FUNCTION";
    static final String ACTION_FORCE_STOP_APPLICATIONS_START = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_FORCE_STOP_APPLICATIONS_START";
    static final String ACTION_FORCE_STOP_APPLICATIONS_END = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_FORCE_STOP_APPLICATIONS_END";
    static final String ACTION_SMS_MMS_RECEIVED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_SMS_MMS_RECEIVED";
    static final String ACTION_CALL_RECEIVED = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_CALL_RECEIVED";
    static final String ACTION_LOCK_DEVICE = PPApplication.PACKAGE_NAME_EXTENDER + ".ACTION_LOCK_DEVICE";
    static final String PPP_EXTENDER_PERMISSION = PPApplication.PACKAGE_NAME_EXTENDER + ".ACCESSIBILITY_SERVICE_PERMISSION";

    //static final String ACTION_SHOW_PROFILE_NOTIFICATION = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_SHOW_PROFILE_NOTIFICATION";
    //static final String ACTION_UPDATE_GUI = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_UPDATE_GUI";
    static final String ACTION_DONATION = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_DONATION";
    static final String ACTION_CHECK_GITHUB_RELEASES = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_CHECK_GITHUB_RELEASES";
    static final String ACTION_CHECK_CRITICAL_GITHUB_RELEASES = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_CHECK_CRITICAL_GITHUB_RELEASES";
    static final String ACTION_FINISH_ACTIVITY = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_FINISH_ACTIVITY";
    static final String ACTION_CHECK_REQUIRED_EXTENDER_RELEASES = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_CHECK_REQUIRED_EXTENDER_RELEASES";
    static final String ACTION_CHECK_LATEST_PPPPS_RELEASES = PPApplication.PACKAGE_NAME + ".PPApplication.ACTION_CHECK_LATEST_PPPPS_RELEASES";

    static final String EXTRA_WHAT_FINISH = "what_finish";

    static final String ACTION_EXPORT_PP_DATA_START_FROM_PPP = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_START_FROM_PPP";
    static final String ACTION_EXPORT_PP_DATA_STOP_FROM_PPP = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_STOP_FROM_PPP";
    static final String ACTION_EXPORT_PP_DATA_STOP_FROM_PP = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_STOP_FROM_PP";
    static final String ACTION_EXPORT_PP_DATA_STARTED = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_STARTED";
    static final String ACTION_EXPORT_PP_DATA_ENDED = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_ENDED";
    static final String ACTION_EXPORT_PP_DATA_APPLICATION_PREFERENCES = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_APPLICATION_PREFERENCES";
    //static final String ACTION_EXPORT_PP_DATA_PROFILES_COUNT = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_PROFILES_COUNT";
    static final String ACTION_EXPORT_PP_DATA_PROFILES = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_PROFILES";
    //static final String ACTION_EXPORT_PP_DATA_SHORTCUTS_COUNT = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_SHORTCUTS_COUNT";
    static final String ACTION_EXPORT_PP_DATA_SHORTCUTS = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_SHORTCUTS";
    //static final String ACTION_EXPORT_PP_DATA_INTENTS_COUNT = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_INTENTS_COUNT";
    static final String ACTION_EXPORT_PP_DATA_INTENTS = PPApplication.PACKAGE_NAME_PP + ".ACTION_EXPORT_PP_DATA_INTENTS";
    static final String EXTRA_PP_APPLICATION_DATA = "extra_pp_application_data";
    //static final String EXTRA_PP_PROFILES_COUNT = "extra_pp_profiles_count";
    static final String EXTRA_PP_PROFILE_DATA = "extra_pp_profile_data";
    //static final String EXTRA_PP_SHORTCUTS_COUNT = "extra_pp_shortcuts_count";
    static final String EXTRA_PP_SHORTCUT_DATA = "extra_pp_shortcut_data";
    //static final String EXTRA_PP_INTENTS_COUNT = "extra_pp_intents_count";
    static final String EXTRA_PP_INTENT_DATA = "extra_pp_intent_data";
    static final String EXPORT_PP_DATA_PERMISSION = PPApplication.PACKAGE_NAME_PP + ".EXPORT_PP_DATA_PERMISSION";


    static final String EXTRA_REGISTRATION_APP = "registration_app";
    static final String EXTRA_REGISTRATION_TYPE = "registration_type";
    static final int REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER = 1;
    static final int REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER = -1;
    static final int REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER = 2;
    static final int REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER = -2;
    static final int REGISTRATION_TYPE_SMS_REGISTER = 3;
    static final int REGISTRATION_TYPE_SMS_UNREGISTER = -3;
    static final int REGISTRATION_TYPE_CALL_REGISTER = 4;
    static final int REGISTRATION_TYPE_CALL_UNREGISTER = -4;
    static final int REGISTRATION_TYPE_LOCK_DEVICE_REGISTER = 5;
    static final int REGISTRATION_TYPE_LOCK_DEVICE_UNREGISTER = -5;

    static final String EXTRA_APPLICATIONS = "extra_applications";
    static final String EXTRA_BLOCK_PROFILE_EVENT_ACTION = "extra_block_profile_event_actions";

    static final String CRASHLYTICS_LOG_DEVICE_ROOTED = "DEVICE_ROOTED";
    static final String CRASHLYTICS_LOG_DEVICE_ROOTED_WITH = "ROOTED_WITH";
//    static final String CRASHLYTICS_LOG_GOOGLE_PLAY_SERVICES_VERSION = "GOOGLE_PLAY_SERVICES_VERSION";
    static final String CRASHLYTICS_LOG_RESTORE_BACKUP_OK = "RESTORE_BACKUP_OK";
    static final String CRASHLYTICS_LOG_IMPORT_FROM_PP_OK = "IMPORT_FROM_PP_OK";

    private static final String SYS_PROP_MOD_VERSION = "ro.modversion";

    //public static long lastUptimeTime;
    //public static long lastEpochTime;

    private static volatile ApplicationsCache applicationsCache;
    static private volatile ContactsCache contactsCache;
    static private volatile ContactGroupsCache contactGroupsCache;

    static volatile KeyguardManager keyguardManager = null;
    @SuppressWarnings("deprecation")
    static volatile KeyguardManager.KeyguardLock keyguardLock = null;

    //BrightnessView brightnessView = null;
    //BrightnessView screenTimeoutAlwaysOnView = null;

    // this is OK, ActivateProfileHelper.removeKeepScreenOnView()
    // set it to null
    @SuppressLint("StaticFieldLeak")
    static volatile BrightnessView keepScreenOnView = null;

    // this is OK, activity will be removed and lockDeviceActivity set to null after destroy of
    // LockDeviceActivity
    @SuppressLint("StaticFieldLeak")
    static volatile LockDeviceActivity lockDeviceActivity = null;

    static volatile int screenTimeoutWhenLockDeviceActivityIsDisplayed = 0;

//    static int brightnessBeforeScreenOff;
//    static float adaptiveBrightnessBeforeScreenOff;
//    static int brightnessModeBeforeScreenOff;

    // 0 = wait for answer from Extender;
    // 1 = Extender is connected,
    // 2 = Extender is disconnected
    static volatile int accessibilityServiceForPPPExtenderConnected = 2;

    //boolean willBeDoRestartEvents = false;

    static final StartLauncherFromNotificationReceiver startLauncherFromNotificationReceiver = new StartLauncherFromNotificationReceiver();
    //static final UpdateGUIBroadcastReceiver updateGUIBroadcastReceiver = new UpdateGUIBroadcastReceiver();
    //static final ShowPPPAppNotificationBroadcastReceiver showPPPAppNotificationBroadcastReceiver = new ShowPPPAppNotificationBroadcastReceiver();
    static final RefreshActivitiesBroadcastReceiver refreshActivitiesBroadcastReceiver = new RefreshActivitiesBroadcastReceiver();
    static final DashClockBroadcastReceiver dashClockBroadcastReceiver = new DashClockBroadcastReceiver();
    static final IconWidgetProvider iconWidgetBroadcastReceiver = new IconWidgetProvider();
    static final OneRowWidgetProvider oneRowWidgetBroadcastReceiver = new OneRowWidgetProvider();
    static final ProfileListWidgetProvider listWidgetBroadcastReceiver = new ProfileListWidgetProvider();
    static final SamsungEdgeProvider edgePanelBroadcastReceiver = new SamsungEdgeProvider();
    static final OneRowProfileListWidgetProvider oneRowProfileListWidgetBroadcastReceiver = new OneRowProfileListWidgetProvider();

    static volatile TimeChangedReceiver timeChangedReceiver = null;
    static volatile StartEventNotificationDeletedReceiver startEventNotificationDeletedReceiver = null;
    static volatile NotUsedMobileCellsNotificationDeletedReceiver notUsedMobileCellsNotificationDeletedReceiver = null;
    static volatile ShutdownBroadcastReceiver shutdownBroadcastReceiver = null;
    static volatile ScreenOnOffBroadcastReceiver screenOnOffReceiver = null;
    static volatile InterruptionFilterChangedBroadcastReceiver interruptionFilterChangedReceiver = null;

    static volatile PhoneCallsListener phoneCallsListenerSIM1 = null;
    static volatile PhoneCallsListener phoneCallsListenerSIM2 = null;
    static volatile PhoneCallsListener phoneCallsListenerDefaul = null;
    static volatile TelephonyManager telephonyManagerSIM1 = null;
    static volatile TelephonyManager telephonyManagerSIM2 = null;
    static volatile TelephonyManager telephonyManagerDefault = null;


    static volatile RingerModeChangeReceiver ringerModeChangeReceiver = null;
    static volatile WifiStateChangedBroadcastReceiver wifiStateChangedBroadcastReceiver = null;
    static volatile NotUsedMobileCellsNotificationDisableReceiver notUsedMobileCellsNotificationDisableReceiver = null;
    static volatile DonationBroadcastReceiver donationBroadcastReceiver = null;
    static volatile CheckPPPReleasesBroadcastReceiver checkPPPReleasesBroadcastReceiver = null;
    static volatile CheckCriticalPPPReleasesBroadcastReceiver checkCriticalPPPReleasesBroadcastReceiver = null;
    static volatile CheckOnlineStatusBroadcastReceiver checkOnlineStatusBroadcastReceiver = null;
    static volatile SimStateChangedBroadcastReceiver simStateChangedBroadcastReceiver = null;
    static volatile CheckRequiredExtenderReleasesBroadcastReceiver checkRequiredExtenderReleasesBroadcastReceiver = null;
    static volatile CheckLatestPPPPSReleasesBroadcastReceiver checkLatestPPPPSReleasesBroadcastReceiver = null;

    static volatile BatteryChargingChangedBroadcastReceiver batteryChargingChangedReceiver = null;
    static volatile BatteryLevelChangedBroadcastReceiver batteryLevelChangedReceiver = null;
    static volatile HeadsetConnectionBroadcastReceiver headsetPlugReceiver = null;
    static volatile NFCStateChangedBroadcastReceiver nfcStateChangedBroadcastReceiver = null;
    static volatile DockConnectionBroadcastReceiver dockConnectionBroadcastReceiver = null;
    //static volatile WifiConnectionBroadcastReceiver wifiConnectionBroadcastReceiver = null;
    static volatile WifiNetworkCallback wifiConnectionCallback = null;
    static volatile MobileDataNetworkCallback mobileDataConnectionCallback = null;
    static volatile BluetoothConnectionBroadcastReceiver bluetoothConnectionBroadcastReceiver = null;
    static volatile BluetoothStateChangedBroadcastReceiver bluetoothStateChangedBroadcastReceiver = null;
    static volatile WifiAPStateChangeBroadcastReceiver wifiAPStateChangeBroadcastReceiver = null;
    static volatile LocationModeChangedBroadcastReceiver locationModeChangedBroadcastReceiver = null;
    static volatile AirplaneModeStateChangedBroadcastReceiver airplaneModeStateChangedBroadcastReceiver = null;
    //static volatile SMSBroadcastReceiver smsBroadcastReceiver = null;
    //static volatile SMSBroadcastReceiver mmsBroadcastReceiver = null;
    static volatile CalendarProviderChangedBroadcastReceiver calendarProviderChangedBroadcastReceiver = null;
    static volatile WifiScanBroadcastReceiver wifiScanReceiver = null;
    static volatile BluetoothScanBroadcastReceiver bluetoothScanReceiver = null;
    static volatile BluetoothLEScanBroadcastReceiver bluetoothLEScanReceiver = null;
    static volatile PPPExtenderBroadcastReceiver pppExtenderBroadcastReceiver = null;
    static volatile PPPExtenderBroadcastReceiver pppExtenderForceStopApplicationBroadcastReceiver = null;
    static volatile PPPExtenderBroadcastReceiver pppExtenderForegroundApplicationBroadcastReceiver = null;
    static volatile PPPExtenderBroadcastReceiver pppExtenderSMSBroadcastReceiver = null;
    static volatile PPPExtenderBroadcastReceiver pppExtenderCallBroadcastReceiver = null;
    static volatile EventTimeBroadcastReceiver eventTimeBroadcastReceiver = null;
    static volatile EventCalendarBroadcastReceiver eventCalendarBroadcastReceiver = null;
    static volatile EventDelayStartBroadcastReceiver eventDelayStartBroadcastReceiver = null;
    static volatile EventDelayEndBroadcastReceiver eventDelayEndBroadcastReceiver = null;
    static volatile ProfileDurationAlarmBroadcastReceiver profileDurationAlarmBroadcastReceiver = null;
    static volatile SMSEventEndBroadcastReceiver smsEventEndBroadcastReceiver = null;
    static volatile NFCEventEndBroadcastReceiver nfcEventEndBroadcastReceiver = null;
    static volatile RunApplicationWithDelayBroadcastReceiver runApplicationWithDelayBroadcastReceiver = null;
    static volatile MissedCallEventEndBroadcastReceiver missedCallEventEndBroadcastReceiver = null;
    static volatile StartEventNotificationBroadcastReceiver startEventNotificationBroadcastReceiver = null;
    static volatile LocationScannerSwitchGPSBroadcastReceiver locationScannerSwitchGPSBroadcastReceiver = null;
    static volatile LockDeviceActivityFinishBroadcastReceiver lockDeviceActivityFinishBroadcastReceiver = null;
    static volatile AlarmClockBroadcastReceiver alarmClockBroadcastReceiver = null;
    static volatile AlarmClockEventEndBroadcastReceiver alarmClockEventEndBroadcastReceiver = null;
    static volatile NotificationEventEndBroadcastReceiver notificationEventEndBroadcastReceiver = null;
    static volatile LockDeviceAfterScreenOffBroadcastReceiver lockDeviceAfterScreenOffBroadcastReceiver = null;
    //static volatile OrientationEventBroadcastReceiver orientationEventBroadcastReceiver = null;
    static volatile PowerSaveModeBroadcastReceiver powerSaveModeReceiver = null;
    static volatile DeviceIdleModeBroadcastReceiver deviceIdleModeReceiver = null;
    static volatile DeviceBootEventEndBroadcastReceiver deviceBootEventEndBroadcastReceiver = null;
    static volatile CalendarEventExistsCheckBroadcastReceiver calendarEventExistsCheckBroadcastReceiver = null;
    static volatile PeriodicEventEndBroadcastReceiver periodicEventEndBroadcastReceiver = null;
    static volatile DefaultSIMChangedBroadcastReceiver defaultSIMChangedBroadcastReceiver = null;
    //static volatile RestartEventsWithDelayBroadcastReceiver restartEventsWithDelayBroadcastReceiver = null;
    static volatile ActivatedProfileEventBroadcastReceiver activatedProfileEventBroadcastReceiver = null;
    static volatile VPNNetworkCallback vpnConnectionCallback = null;

    static volatile SettingsContentObserver settingsContentObserver = null;

    // this is OK, mobileDataStateChangedContentObserver will set to null when
    // observer will be unregistered
    @SuppressLint("StaticFieldLeak")
    static volatile MobileDataStateChangedContentObserver mobileDataStateChangedContentObserver = null;

    static volatile ContactsContentObserver contactsContentObserver = null;

    static volatile SensorManager sensorManager = null;
    static volatile Sensor accelerometerSensor = null;
    static volatile Sensor magneticFieldSensor = null;
    static volatile Sensor lightSensor = null;
    static volatile Sensor proximitySensor = null;

    static volatile OrientationScanner orientationScanner = null;
    static volatile boolean mStartedOrientationSensors = false;

    // this is OK, locationScanner will be set to null, when location scanner will be stopped
    @SuppressLint("StaticFieldLeak")
    static volatile LocationScanner locationScanner = null;

    // this is OK, mobileCellsScanner will be set to null, when mobile cells scanner will be stopped
    @SuppressLint("StaticFieldLeak")
    static volatile MobileCellsScanner mobileCellsScanner = null;


    // this is OK, twilightScanner will be set to null, when twilight scanner will be stopped
    @SuppressLint("StaticFieldLeak")
    static volatile TwilightScanner twilightScanner = null;

    static volatile boolean notificationScannerRunning = false;

    static volatile boolean isCharging = false;
    static volatile int batteryPct = -100;
    static volatile int plugged = -1;

    public volatile static boolean isScreenOn;
    //public static boolean isPowerSaveMode;

    static volatile Location lastLocation = null;

    public volatile static ExecutorService basicExecutorPool = null;
    public volatile static ExecutorService profileActiationExecutorPool = null;
    public volatile static ExecutorService eventsHandlerExecutor = null;
    public volatile static ExecutorService scannersExecutor = null;
    public volatile static ExecutorService playToneExecutor = null;
    public volatile static ScheduledExecutorService disableInternalChangeExecutor = null;
    public volatile static ScheduledExecutorService delayedGuiExecutor = null;
    public volatile static ScheduledExecutorService delayedAppNotificationExecutor = null;
    public volatile static ScheduledExecutorService delayedEventsHandlerExecutor = null;
    public volatile static ScheduledExecutorService delayedProfileActivationExecutor = null;

    // required for callbacks, observers, ...
    public volatile static HandlerThread handlerThreadBroadcast = null;
    // required for sensor manager
    public volatile static OrientationScannerHandlerThread handlerThreadOrientationScanner = null;
    // rewuired for location manager
    public volatile static HandlerThread handlerThreadLocation = null;

    //public static HandlerThread handlerThread = null;
    //public static HandlerThread handlerThreadCancelWork = null;
    //public static HandlerThread handlerThreadWidget = null;
    //public static HandlerThread handlerThreadPlayTone = null;
    //public static HandlerThread handlerThreadPPScanners = null;
    //public static HandlerThread handlerThreadPPCommand = null;

    //public static HandlerThread handlerThreadVolumes = null;
    //public static HandlerThread handlerThreadRadios = null;
    //public static HandlerThread handlerThreadWallpaper = null;
    //public static HandlerThread handlerThreadRunApplication = null;

    //public static HandlerThread handlerThreadProfileActivation = null;

    public volatile static Handler toastHandler;
    //public static Handler brightnessHandler;
    public volatile static Handler screenTimeoutHandler;

    public static final PPNotificationListenerService ppNotificationListenerService = new PPNotificationListenerService();

    //public static boolean isPowerSaveMode = false;

    // !! this must be here
    public volatile static boolean blockProfileEventActions = false;

    // Samsung Look instance
    public volatile static Slook sLook = null;
    public volatile static boolean sLookCocktailPanelEnabled = false;
    //public static boolean sLookCocktailBarEnabled = false;

    //public static final Random requestCodeForAlarm = new Random();

    static final long[] quickTileProfileId = {0, 0, 0, 0, 0, 0};
    static final QuickTileChooseTileBroadcastReceiver[] quickTileChooseTileBroadcastReceiver =
            {null, null, null, null, null, null};

    @Override
    public void onCreate()
    {
        PPApplication.logE("################# PPApplication.onCreate", "onCreate() start");

        /* Hm this resets start, why?!
        if (DebugVersion.enabled) {
            if (!ACRA.isACRASenderServiceProcess()) {
                PPApplication.logE("##### PPApplication.onCreate", "strict mode");

                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectAll()
                        //.detectNetwork()   // or .detectAll() for all detectable problems
                        .penaltyLog()
                        .build());
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects()
                        .penaltyLog()
                        .penaltyDeath()
                        .build());
            }
        }*/

        super.onCreate();

        // This is required : https://www.acra.ch/docs/Troubleshooting-Guide#applicationoncreate
        if (ACRA.isACRASenderServiceProcess()) {
            Log.e("################# PPApplication.onCreate", "ACRA.isACRASenderServiceProcess()");
            return;
        }

        synchronized (PPApplication.applicationStartedMutex) {
            PPApplication.exportIsRunning = false;
        }
        applicationFullyStarted = false;
        normalServiceStart = false;
        showToastForProfileActivation = false;
        instance = this;

        //registerActivityLifecycleCallbacks(PPApplication.this);

        /*try {
            //if (!DebugVersion.enabled) {
            // Obtain the FirebaseAnalytics instance.
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
            //}
            //FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        } catch (Exception e) {
            Log.e("PPApplication.onCreate", Log.getStackTraceString(e));
        }*/

        if (checkAppReplacingState()) {
            PPApplication.logE("##### PPApplication.onCreate", "kill PPApplication - not good");
            return;
        }

        PPApplication.logE("##### PPApplication.onCreate", "continue onCreate()");

        createBasicExecutorPool();
        createProfileActiationExecutorPool();
        createEventsHandlerExecutor();
        createScannersExecutor();
        createPlayToneExecutor();
        createNonBlockedExecutor();
        createDelayedGuiExecutor();
        createDelayedShowNotificationExecutor();
        createDelayedEventsHandlerExecutor();
        createDelayedProfileActivationExecutor();

        // keep this: it is required to use handlerThreadBroadcast for cal listener
        startHandlerThreadBroadcast();

        startHandlerThreadOrientationScanner(); // for seconds interval
        //startHandlerThread(/*"PPApplication.onCreate"*/);
        //startHandlerThreadCancelWork();
        //startHandlerThreadPPScanners(); // for minutes interval
        //startHandlerThreadPPCommand();
        startHandlerThreadLocation();
        //startHandlerThreadWidget();
        //startHandlerThreadPlayTone();
        //startHandlerThreadVolumes();
        //startHandlerThreadRadios();
        //startHandlerThreadWallpaper();
        //startHandlerThreadRunApplication();
        //startHandlerThreadProfileActivation();

        toastHandler = new Handler(getMainLooper());
        //brightnessHandler = new Handler(getMainLooper());
        screenTimeoutHandler = new Handler(getMainLooper());

        PackageManager packageManager = getPackageManager();
        HAS_FEATURE_BLUETOOTH_LE = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_BLUETOOTH_LE);
        HAS_FEATURE_WIFI = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_WIFI);
        HAS_FEATURE_BLUETOOTH = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_BLUETOOTH);
        HAS_FEATURE_TELEPHONY = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_TELEPHONY);
        HAS_FEATURE_NFC = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_NFC);
        HAS_FEATURE_LOCATION = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_LOCATION);
        HAS_FEATURE_LOCATION_GPS = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_LOCATION_GPS);
        HAS_FEATURE_CAMERA_FLASH = PPApplication.hasSystemFeature(packageManager, PackageManager.FEATURE_CAMERA_FLASH);

        PPApplication.logE("##### PPApplication.onCreate", "end of get features");

        PPApplication.createNotificationChannels(getApplicationContext());

        loadGlobalApplicationData(getApplicationContext());
        loadApplicationPreferences(getApplicationContext());
        loadProfileActivationData(getApplicationContext());

        workManagerInstance = WorkManager.getInstance(getApplicationContext());
        PPApplication.logE("##### PPApplication.onCreate", "workManagerInstance="+workManagerInstance);

        /*
        workManagerInstance.pruneWork();
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            int size = jobScheduler.getAllPendingJobs().size();
            PPApplication.logE("##### PPApplication.onCreate", "jobScheduler.getAllPendingJobs().size()="+size);
            jobScheduler.cancelAll();
        }
        */

        // https://issuetracker.google.com/issues/115575872#comment16
        AvoidRescheduleReceiverWorker.enqueueWork();

//        init() moved to ActivateProfileHelpser.execute();
//        try {
//            NoobCameraManager.getInstance().init(this);
//        } catch (Exception e) {
//            PPApplication.recordException(e);
//        }

        if (keyguardManager == null)
            keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null)
            //noinspection deprecation
            keyguardLock = keyguardManager.newKeyguardLock("phoneProfilesPlus.keyguardLock");

        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = getAccelerometerSensor(getApplicationContext());
        magneticFieldSensor = getMagneticFieldSensor(getApplicationContext());
        proximitySensor = getProximitySensor(getApplicationContext());
        lightSensor = getLightSensor(getApplicationContext());

//        if (lastLocation == null) {
//            lastLocation = new Location("GL");
//        }

        if (logEnabled()) {
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsXiaomi=" + deviceIsXiaomi);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsHuawei=" + deviceIsHuawei);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsSamsung=" + deviceIsSamsung);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsLG=" + deviceIsLG);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsOnePlus=" + deviceIsOnePlus);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsOppo=" + deviceIsOppo);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsRealme=" + deviceIsRealme);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsLenovo=" + deviceIsLenovo);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsPixel=" + deviceIsPixel);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsSony=" + deviceIsSony);
            PPApplication.logE("##### PPApplication.onCreate", "deviceIsDoogee=" + deviceIsDoogee);

            PPApplication.logE("##### PPApplication.onCreate", "romIsMIUI=" + romIsMIUI);
            PPApplication.logE("##### PPApplication.onCreate", "romIsEMUI=" + romIsEMUI);
            //PPApplication.logE("##### PPApplication.onCreate", "-- romIsEMUI=" + isEMUIROM());
            //PPApplication.logE("##### PPApplication.onCreate", "-- romIsMIUI=" + isMIUIROM());
            PPApplication.logE("##### PPApplication.onCreate", "romIsGalaxy=" + romIsGalaxy);

            PPApplication.logE("##### PPApplication.onCreate", "manufacturer=" + Build.MANUFACTURER);
            PPApplication.logE("##### PPApplication.onCreate", "model=" + Build.MODEL);
            PPApplication.logE("##### PPApplication.onCreate", "display=" + Build.DISPLAY);
            PPApplication.logE("##### PPApplication.onCreate", "brand=" + Build.BRAND);
            PPApplication.logE("##### PPApplication.onCreate", "fingerprint=" + Build.FINGERPRINT);
            PPApplication.logE("##### PPApplication.onCreate", "type=" + Build.TYPE);

            PPApplication.logE("##### PPApplication.onCreate", "modVersion=" + getReadableModVersion());
            PPApplication.logE("##### PPApplication.onCreate", "osVersion=" + System.getProperty("os.version"));
            PPApplication.logE("##### PPApplication.onCreate", "api level=" + Build.VERSION.SDK_INT);

            if (Build.VERSION.SDK_INT >= 25)
                PPApplication.logE("##### PPApplication.onCreate", "deviceName="+ Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME));
            PPApplication.logE("##### PPApplication.onCreate", "release="+ Build.VERSION.RELEASE);

            PPApplication.logE("##### PPApplication.onCreate", "board="+ Build.BOARD);
            PPApplication.logE("##### PPApplication.onCreate", "product="+ Build.PRODUCT);
        }

        // Fix for FC: java.lang.IllegalArgumentException: register too many Broadcast Receivers
        //LoadedApkHuaWei.hookHuaWeiVerifier(this);

        /*
        if (logIntoFile || crashIntoFile)
            Permissions.grantLogToFilePermissions(getApplicationContext());
        */

        ////////////////////////////////////////////////////////////////////////////////////
        // Bypass Android's hidden API restrictions
        // !!! WARNING - this is required also for android.jar from android-hidden-api !!!
        // https://github.com/tiann/FreeReflection
        /*if (Build.VERSION.SDK_INT >= 28) {
            try {
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});

                if (getRuntime != null) {
                    Object vmRuntime = getRuntime.invoke(null);
                    if (setHiddenApiExemptions != null)
                        setHiddenApiExemptions.invoke(vmRuntime, new Object[]{new String[]{"L"}});
                }
            } catch (Exception e) {
                //Log.e("PPApplication.onCreate", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }*/
        //////////////////////////////////////////

        /*
        // set up ANR-WatchDog
        ANRWatchDog anrWatchDog = new ANRWatchDog();
        //anrWatchDog.setReportMainThreadOnly();
        anrWatchDog.setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                //Crashlytics.getInstance().core.logException(error);
                PPApplication.recordException(error);
            }
        });
        anrWatchDog.start();
        */

        PPApplication.setCustomKey("FROM_GOOGLE_PLAY", false);
        PPApplication.setCustomKey("DEBUG", DebugVersion.enabled);

        //lastUptimeTime = SystemClock.elapsedRealtime();
        //lastEpochTime = System.currentTimeMillis();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null)
            isScreenOn = pm.isInteractive();
        else
            isScreenOn = false;
        /*DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager == null)
            isScreenOn = false;
        else {
            Display[] displays = displayManager.getDisplays();
            if ((displays == null) || (displays.length == 0))
                isScreenOn = false;
            else {
                int state = displays[0].getState();
                if ((state == Display.STATE_ON) || (state == Display.STATE_ON_SUSPEND))
                    isScreenOn = true;
            }
        }*/
//        brightnessModeBeforeScreenOff = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, -1);
//        brightnessBeforeScreenOff = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
//        adaptiveBrightnessBeforeScreenOff = Settings.System.getFloat(getContentResolver(), Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, -1);


        //isPowerSaveMode = DataWrapper.isPowerSaveMode(getApplicationContext());

        //	Debug.startMethodTracing("phoneprofiles");

        //resetLog();

        //firstStartServiceStarted = false;

        /*
        JobConfig.setApiEnabled(JobApi.WORK_MANAGER, true);
        //JobConfig.setForceAllowApi14(true); // https://github.com/evernote/android-job/issues/197
        //JobConfig.setApiEnabled(JobApi.GCM, false); // is only important for Android 4.X

        JobManager.create(this).addJobCreator(new PPJobsCreator());
        */

        RootUtils.initRoot();

        /*
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            F field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
        */

        //Log.d("PPApplication.onCreate", "memory usage (after create activateProfileHelper)=" + Debug.getNativeHeapAllocatedSize());

        //Log.d("PPApplication.onCreate","xxx");

        // Samsung Look initialization
        sLook = new Slook();
        try {
            sLook.initialize(this);
            // true = The Device supports Edge Single Mode, Edge Single Plus Mode, and Edge Feeds Mode.
            sLookCocktailPanelEnabled = sLook.isFeatureEnabled(Slook.COCKTAIL_PANEL);
            Log.e("***** PPApplication.onCreate", "sLookCocktailPanelEnabled="+sLookCocktailPanelEnabled);
            // true = The Device supports Edge Immersive Mode feature.
            //sLookCocktailBarEnabled = sLook.isFeatureEnabled(Slook.COCKTAIL_BAR);
        } catch (SsdkUnsupportedException e) {
            sLook = null;
            switch (e.getType()) {
                case SsdkUnsupportedException.VENDOR_NOT_SUPPORTED:
                    Log.e("***** PPApplication.onCreate", "Look not supported: VENDOR_NOT_SUPPORTED");
                    break;
                case SsdkUnsupportedException.DEVICE_NOT_SUPPORTED:
                    Log.e("***** PPApplication.onCreate", "Look not supported: DEVICE_NOT_SUPPORTED");
                    break;
                case SsdkUnsupportedException.LIBRARY_NOT_INSTALLED:
                    Log.e("***** PPApplication.onCreate", "Look not supported: LIBRARY_NOT_INSTALLED");
                    break;
                case SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED:
                    Log.e("***** PPApplication.onCreate", "Look not supported: LIBRARY_UPDATE_IS_REQUIRED");
                    break;
                case SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED:
                    Log.e("***** PPApplication.onCreate", "Look not supported: LIBRARY_UPDATE_IS_RECOMMENDED");
                    break;
            }
        }

        startPPServiceWhenNotStarted(this);
    }

    static PPApplication getInstance() {
        //synchronized (PPApplication.phoneProfilesServiceMutex) {
        return instance;
        //}
    }

    @Override
    protected void attachBaseContext(Context base) {
        //super.attachBaseContext(base);
        super.attachBaseContext(LocaleHelper.onAttach(base));
        //Reflection.unseal(base);
        if (Build.VERSION.SDK_INT >= 28) {
            HiddenApiBypass.addHiddenApiExemptions("L");
        }

        collator = GlobalUtils.getCollator();
        //MultiDex.install(this);

        // This is required : https://www.acra.ch/docs/Troubleshooting-Guide#applicationoncreate
        if (ACRA.isACRASenderServiceProcess()) {
            Log.e("################# PPApplication.attachBaseContext", "ACRA.isACRASenderServiceProcess()");
            return;
        }

//        PPApplication.logE("##### PPApplication.attachBaseContext", "ACRA inittialization");

        String packageVersion = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            packageVersion = " - v" + pInfo.versionName + " (" + PPApplication.getVersionCode(pInfo) + ")";
        } catch (Exception ignored) {
        }

        String body;
        if (Build.VERSION.SDK_INT >= 25)
            body = getString(R.string.important_info_email_body_device) + " " +
                    Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME) +
                    " (" + Build.MODEL + ")" + " \n";
        else {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer))
                body = getString(R.string.important_info_email_body_device) + " " + model + " \n";
            else
                body = getString(R.string.important_info_email_body_device) + " " + manufacturer + " " + model + " \n";
        }
        body = body + getString(R.string.important_info_email_body_android_version) + " " + Build.VERSION.RELEASE + " \n\n";
        body = body + getString(R.string.acra_email_body_text);

/*
        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this)
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.KEY_VALUE_LIST);
        //builder.getPluginConfigurationBuilder(ToastConfigurationBuilder.class)
        //        .setResText(R.string.acra_toast_text)
        //        .setEnabled(true);
        builder.getPluginConfigurationBuilder(NotificationConfigurationBuilder.class)
                .withResChannelName(R.string.notification_channel_crash_report)
                .withResChannelImportance(NotificationManager.IMPORTANCE_HIGH)
                .withResIcon(R.drawable.ic_exclamation_notify)
                .withResTitle(R.string.acra_notification_title)
                .withResText(R.string.acra_notification_text)
                .withResSendButtonIcon(0)
                .withResDiscardButtonIcon(0)
                .withSendOnClick(true)
                .withEnabled(true);
        builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class)
                .withMailTo("henrich.gron@gmail.com")
                .withSubject("PhoneProfilesPlus" + packageVersion + " - " + getString(R.string.acra_email_subject_text))
                .withBody(body)
                .withReportAsFile(true)
                .withReportFileName("crash_report.txt")
                .withEnabled(true);
*/

        //noinspection ArraysAsListWithZeroOrOneArgument
        CoreConfigurationBuilder builder = new CoreConfigurationBuilder()
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.KEY_VALUE_LIST)
                //.withSharedPreferencesName(ACRA_PREFS_NAME)
                .withAdditionalSharedPreferences(Arrays.asList(APPLICATION_PREFS_NAME));

        builder.withPluginConfigurations(
                new NotificationConfigurationBuilder()
                        .withChannelName(getString(R.string.notification_channel_crash_report))
                        .withChannelImportance(NotificationManager.IMPORTANCE_HIGH)
                        .withResIcon(R.drawable.ic_exclamation_notify)
                        .withTitle(getString(R.string.acra_notification_title))
                        .withText(getString(R.string.acra_notification_text))
                        .withResSendButtonIcon(0)
                        .withResDiscardButtonIcon(0)
                        .withSendOnClick(true)
                        .withEnabled(true)
                        .build(),
                new MailSenderConfigurationBuilder()
                        .withMailTo("henrich.gron@gmail.com")
                        .withSubject("PhoneProfilesPlus" + packageVersion + " - " + getString(R.string.acra_email_subject_text))
                        .withBody(body)
                        .withReportAsFile(true)
                        .withReportFileName("crash_report.txt")
                        .withEnabled(true)
                        .build()
        );

        ACRA.DEV_LOGGING = true;

        ACRA.init(this, builder);

        int actualVersionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
            actualVersionCode = PPApplication.getVersionCode(pInfo);
        } catch (Exception ignored) {}

        // Look at TopExceptionHandler.uncaughtException() for ignored exceptions
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(base, actualVersionCode));
        //}

    }

    private void startPPServiceWhenNotStarted(final Context appContext) {
        // this is for list widget header

        //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = () -> {
//            long start = System.currentTimeMillis();
//            PPApplication.logE("[IN_EXECUTOR]  ***** PPApplication.startPPServiceWhenNotStarted", "--------------- START");

            //Context appContext= appContextWeakRef.get();
            //if (appContext != null) {
            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPApplication_startPPServiceWhenNotStarted");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                boolean serviceStarted = GlobalUtils.isServiceRunning(appContext, PhoneProfilesService.class, false);
                if (!serviceStarted) {
                    //if (!PPApplication.getApplicationStarted(false)) {
                    if (ApplicationPreferences.applicationStartOnBoot) {
                        //AutostartPermissionNotification.showNotification(appContext, true);

                        // start PhoneProfilesService
                        //PPApplication.firstStartServiceStarted = false;
                        PPApplication.setApplicationStarted(appContext, true);
                        Intent serviceIntent = new Intent(appContext, PhoneProfilesService.class);
                        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
                        //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, true);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, true);
                        serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                        serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
//                        PPApplication.logE("[START_PP_SERVICE] PPApplication.startPPServiceWhenNotStarted", "(1)");
                        PPApplication.startPPService(appContext, serviceIntent);
                    }
                    //}
                }

//                long finish = System.currentTimeMillis();
//                long timeElapsed = finish - start;
//                PPApplication.logE("[IN_EXECUTOR]  ***** PPApplication.startPPServiceWhenNotStarted", "--------------- END - timeElapsed="+timeElapsed);
            } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startPPServiceWhenNotStarted", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
                //worker.shutdown();
            }
            //}
        };
        PPApplication.createDelayedGuiExecutor();
        PPApplication.delayedGuiExecutor.schedule(runnable, 1, TimeUnit.SECONDS);
    }

//    @NonNull
//    public Configuration getWorkManagerConfiguration() {
//        Configuration.Builder builder = new Configuration.Builder()
//                .setMinimumLoggingLevel(Log.DEBUG);
//
//        return builder.build();
//    }

    static WorkManager getWorkManagerInstance() {
        if (instance != null) {
            // get WorkManager instance only when PPApplication is created
            //if (workManagerInstance == null)
            return workManagerInstance;
        }
        else
            return null;
    }

    static void _cancelWork(final String name, final boolean forceCancel) {
        WorkManager workManager = PPApplication.getWorkManagerInstance();
        if (workManager != null) {
            ListenableFuture<List<WorkInfo>> statuses;
            statuses = workManager.getWorkInfosForUniqueWork(name);
            //noinspection TryWithIdenticalCatches
            try {
                List<WorkInfo> workInfoList = statuses.get();
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name+" workInfoList.size()="+workInfoList.size());
                // cancel only enqueued works
                for (WorkInfo workInfo : workInfoList) {
                    WorkInfo.State state = workInfo.getState();
                    if (forceCancel || (state == WorkInfo.State.ENQUEUED)) {
                        // any work is enqueued, cancel it
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name+" forceCancel="+forceCancel);
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name+" state="+state);
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name+" cancel it");
                        workManager.cancelWorkById(workInfo.getId());
                    }
                }

                if (name.startsWith(MainWorker.EVENT_DELAY_START_TAG_WORK))
                    PPApplication.elapsedAlarmsEventDelayStartWork.remove(name);
                if (name.startsWith(MainWorker.EVENT_DELAY_END_TAG_WORK))
                    PPApplication.elapsedAlarmsEventDelayEndWork.remove(name);
                if (name.startsWith(MainWorker.PROFILE_DURATION_WORK_TAG))
                    PPApplication.elapsedAlarmsProfileDurationWork.remove(name);
                if (name.startsWith(MainWorker.RUN_APPLICATION_WITH_DELAY_WORK_TAG))
                    PPApplication.elapsedAlarmsRunApplicationWithDelayWork.remove(name);
                if (name.startsWith(MainWorker.START_EVENT_NOTIFICATION_WORK_TAG))
                    PPApplication.elapsedAlarmsStartEventNotificationWork.remove(name);

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void cancelWork(final String name, final boolean forceCancel) {
        // cancel only enqueued works
        //PPApplication.startHandlerThreadCancelWork();
        //final Handler __handler = new Handler(PPApplication.handlerThreadCancelWork.getLooper());
        //__handler.post(() -> {
        Runnable runnable = () -> {
//            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.cancelWork", "name="+name);
                _cancelWork(name, forceCancel);
        }; //);
        PPApplication.createBasicExecutorPool();
        PPApplication.basicExecutorPool.submit(runnable);
    }

    // is called from ThreadHandler
    static void cancelAllWorks(/*boolean atStart*/) {
        /*if (atStart) {
            cancelWork(ShowProfileNotificationWorker.WORK_TAG, false);
            cancelWork(UpdateGUIWorker.WORK_TAG, false);
        }*/
        //if (!atStart)
            _cancelWork(PPApplication.AVOID_RESCHEDULE_RECEIVER_WORK_TAG, false);
        for (String tag : PPApplication.elapsedAlarmsProfileDurationWork)
            _cancelWork(tag, false);
        PPApplication.elapsedAlarmsProfileDurationWork.clear();
        for (String tag : PPApplication.elapsedAlarmsRunApplicationWithDelayWork)
            _cancelWork(tag, false);
        PPApplication.elapsedAlarmsRunApplicationWithDelayWork.clear();
        for (String tag : PPApplication.elapsedAlarmsEventDelayStartWork)
            _cancelWork(tag, false);
        PPApplication.elapsedAlarmsEventDelayStartWork.clear();
        for (String tag : PPApplication.elapsedAlarmsEventDelayEndWork)
            _cancelWork(tag, false);
        PPApplication.elapsedAlarmsEventDelayEndWork.clear();
        for (String tag : PPApplication.elapsedAlarmsStartEventNotificationWork)
            _cancelWork(tag, false);
        PPApplication.elapsedAlarmsStartEventNotificationWork.clear();
        /*if (atStart) {
            cancelWork(DisableInternalChangeWorker.WORK_TAG, false);
            cancelWork(DisableVolumesInternalChangeWorker.WORK_TAG, false);
            cancelWork(DisableScreenTimeoutInternalChangeWorker.WORK_TAG, false);
        }*/
        _cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false);
        _cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false);
        _cancelWork(MainWorker.CLOSE_ALL_APPLICATIONS_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG, false);
        _cancelWork(BluetoothScanWorker.WORK_TAG, false);
        _cancelWork(BluetoothScanWorker.WORK_TAG_SHORT, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_CE_SCANNER_WORK_TAG, false);
        _cancelWork(RestartEventsWithDelayWorker.WORK_TAG_1, false);
        _cancelWork(RestartEventsWithDelayWorker.WORK_TAG_2, false);
        _cancelWork(GeofenceScanWorker.WORK_TAG, false);
        _cancelWork(GeofenceScanWorker.WORK_TAG_SHORT, false);
        _cancelWork(MainWorker.LOCATION_SCANNER_SWITCH_GPS_TAG_WORK, false);
        _cancelWork(LocationGeofenceEditorActivityOSM.FETCH_ADDRESS_WORK_TAG_OSM, false);
        //if (atStart)
        //    cancelWork(MainWorker.LOCK_DEVICE_FINISH_ACTIVITY_TAG_WORK, false);
        _cancelWork(MainWorker.LOCK_DEVICE_AFTER_SCREEN_OFF_TAG_WORK, false);
        /*if (atStart) {
            cancelWork(PACKAGE_REPLACED_WORK_TAG, false);
            cancelWork(AFTER_FIRST_START_WORK_TAG, false);
            cancelWork(DisableBlockProfileEventActionWorker.WORK_TAG, false);
        }*/
        _cancelWork(SearchCalendarEventsWorker.WORK_TAG, false);
        _cancelWork(SearchCalendarEventsWorker.WORK_TAG_SHORT, false);
        _cancelWork(WifiScanWorker.WORK_TAG, false);
        _cancelWork(WifiScanWorker.WORK_TAG_SHORT, false);
        _cancelWork(WifiScanWorker.WORK_TAG_START_SCAN, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_TWILIGHT_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.ORIENTATION_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_NOTIFICATION_POSTED_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_NOTIFICATION_REMOVED_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.SCHEDULE_AVOID_RESCHEDULE_RECEIVER_WORK_TAG, false);
        _cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_WIFI_WORK_TAG, false);
        _cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_BLUETOOTH_WORK_TAG, false);
        _cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG, false);
        _cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG, false);
        _cancelWork(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG, false);
        _cancelWork(LocationSensorWorker.LOCATION_SENSOR_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_NOTIFICATION_RESCAN_SCANNER_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_SOUND_PROFILE_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_PERIODIC_WORK_TAG, false);
        _cancelWork(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG, false);
        _cancelWork(DisableInternalChangeWorker.WORK_TAG, false);
        _cancelWork(DisableScreenTimeoutInternalChangeWorker.WORK_TAG, false);
        _cancelWork(DisableVolumesInternalChangeWorker.WORK_TAG, false);

    }

    /*
    static void setWorkManagerInstance(Context context) {
        workManagerInstance = WorkManager.getInstance(context);
    }
    */

    // workaround for: java.lang.NullPointerException: Attempt to invoke virtual method
    // 'android.content.res.AssetManager android.content.res.Resources.getAssets()' on a null object reference
    // https://issuetracker.google.com/issues/36972466
    private boolean checkAppReplacingState() {
        if (getResources() == null) {
            try {
                android.os.Process.killProcess(pid);
                PPApplication.logToACRA("E/PPApplication.checkAppReplacingState: app is replacing...kill");
            } catch (Exception e) {
                //Log.e("PPApplication.checkAppReplacingState", Log.getStackTraceString(e));
            }
            return true;
        }
        return false;
    }

    /*
    static boolean isNewVersion(Context appContext) {
        int oldVersionCode = PPApplication.getSavedVersionCode(appContext);
        int actualVersionCode;
        try {
            if (oldVersionCode == 0) {
                // save version code
                try {
                    PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.PPApplication.PACKAGE_NAME, 0);
                    actualVersionCode = PPApplication.getVersionCode(pInfo);
                    PPApplication.setSavedVersionCode(appContext, actualVersionCode);
                } catch (Exception ignored) {
                }
                return false;
            }

            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.PPApplication.PACKAGE_NAME, 0);
            actualVersionCode = PPApplication.getVersionCode(pInfo);

            return (oldVersionCode < actualVersionCode);
        } catch (Exception e) {
            return false;
        }
    }
    */

    static int getVersionCode(PackageInfo pInfo) {
        //return pInfo.versionCode;
        return (int) PackageInfoCompat.getLongVersionCode(pInfo);
    }

    static void setApplicationFullyStarted(Context context) {
        boolean oldApplicationFullyStarted = applicationFullyStarted;
        applicationFullyStarted = true; //started;

//        PPApplication.logE("[APPLICATION_FULLY_STARTED] PPApplication.setApplicationFullyStarted", "oldApplicationFullyStarted="+oldApplicationFullyStarted);

        final Context appContext = context.getApplicationContext();

        if (!oldApplicationFullyStarted) {
//            PPApplication.logE("[PPP_NOTIFICATION] PPApplication.setApplicationFullyStarted", "call of updateGUI");
            updateGUI(true, false, appContext);
        }

        if (!oldApplicationFullyStarted && normalServiceStart && showToastForProfileActivation) {
            // it is not restart of application by system
            String text = appContext.getString(R.string.ppp_app_name) + " " + context.getString(R.string.application_is_started_toast);
            showToast(appContext, text, Toast.LENGTH_SHORT);
        }

        normalServiceStart = true;
    }

    //--------------------------------------------------------------

    static void addActivityLog(Context context, final int logType, final String eventName,
                               final String profileName, final String profilesEventsCount) {
        if (PPApplication.prefActivityLogEnabled) {
            final Context appContext = context;
            //PPApplication.startHandlerThread(/*"AlarmClockBroadcastReceiver.onReceive"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPApplication.addActivityLog");

                //Context context= appContextWeakRef.get();
                if (appContext != null) {
                    //if (ApplicationPreferences.preferences == null)
                    //    ApplicationPreferences.preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                    //ApplicationPreferences.setApplicationDeleteOldActivityLogs(context, Integer.valueOf(preferences.getString(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, "7")));
                    DatabaseHandler.getInstance(appContext).addActivityLog(ApplicationPreferences.applicationDeleteOldActivityLogs,
                            logType, eventName, profileName, profilesEventsCount);
                }
            }; //);
            PPApplication.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);
        }
    }

    //--------------------------------------------------------------

    static private void resetLog()
    {
        /*File sd = Environment.getExternalStorageDirectory();
        File exportDir = new File(sd, PPApplication.EXPORT_PATH);
        if (!(exportDir.exists() && exportDir.isDirectory()))
            exportDir.mkdirs();

        File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
        */

        File path = instance.getApplicationContext().getExternalFilesDir(null);
        File logFile = new File(path, LOG_FILENAME);

        //noinspection ResultOfMethodCallIgnored
        logFile.delete();
    }

    static private void logIntoFile(String type, String tag, String text)
    {
        if (!logIntoFile)
            return;

        if (instance == null)
            return;

        try {
            /*File sd = Environment.getExternalStorageDirectory();
            File exportDir = new File(sd, PPApplication.EXPORT_PATH);
            if (!(exportDir.exists() && exportDir.isDirectory()))
                exportDir.mkdirs();

            File logFile = new File(sd, EXPORT_PATH + "/" + LOG_FILENAME);
            */

            File path = instance.getApplicationContext().getExternalFilesDir(null);
            File logFile = new File(path, LOG_FILENAME);

            if (logFile.length() > 1024 * 10000)
                resetLog();

            if (!logFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            String log = "";
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("d.MM.yy HH:mm:ss:S");
            String time = sdf.format(Calendar.getInstance().getTimeInMillis());
            log = log + time + " [ " + type + " ] [ " + tag + " ]: " + text;
            buf.append(log);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (Exception e) {
            Log.e("***** PPApplication.logIntoFile", Log.getStackTraceString(e));
            //PPApplication.recordException(e);
        }
    }

    private static boolean logContainsFilterTag(String tag)
    {
        boolean contains = false;
        String[] filterTags = logFilterTags.split("\\|");
        for (String filterTag : filterTags) {
            if (!filterTag.contains("!")) {
                if (tag.contains(filterTag)) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    static boolean logEnabled() {
        //noinspection ConstantConditions
        return (logIntoLogCat || logIntoFile);
    }

    @SuppressWarnings("unused")
    static void logI(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            //if (logIntoLogCat) Log.i(tag, text);
            if (logIntoLogCat) Log.i(tag, "[ "+tag+" ]" + ": " + text);
            logIntoFile("I", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static void logW(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            //if (logIntoLogCat) Log.w(tag, text);
            if (logIntoLogCat) Log.w(tag, "[ "+tag+" ]" + ": " + text);
            logIntoFile("W", tag, text);
        }
    }

    static void logE(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            //if (logIntoLogCat) Log.e(tag, text);
            if (logIntoLogCat) Log.e(tag, "[ "+tag+" ]" + ": " + text);
            logIntoFile("E", tag, text);
        }
    }

    @SuppressWarnings("unused")
    static void logD(String tag, String text)
    {
        if (!logEnabled())
            return;

        if (logContainsFilterTag(tag))
        {
            //if (logIntoLogCat) Log.d(tag, text);
            if (logIntoLogCat) Log.d(tag, "[ "+tag+" ]" + ": " + text);
            logIntoFile("D", tag, text);
        }
    }

    /*
    public static String intentToString(Intent intent) {
        if (intent == null) {
            return null;
        }

        return intent.toString() + " " + bundleToString(intent.getExtras());
    }
    */

    /*
    private static String bundleToString(Bundle bundle) {
        StringBuilder out = new StringBuilder("Bundle[");

        if (bundle == null) {
            out.append("null");
        } else {
            boolean first = true;
            for (String key : bundle.keySet()) {
                if (!first) {
                    out.append(", ");
                }

                out.append(key).append('=');

                Object value = bundle.get(key);

                if (value instanceof int[]) {
                    out.append(Arrays.toString((int[]) value));
                } else if (value instanceof byte[]) {
                    out.append(Arrays.toString((byte[]) value));
                } else if (value instanceof boolean[]) {
                    out.append(Arrays.toString((boolean[]) value));
                } else if (value instanceof short[]) {
                    out.append(Arrays.toString((short[]) value));
                } else if (value instanceof long[]) {
                    out.append(Arrays.toString((long[]) value));
                } else if (value instanceof float[]) {
                    out.append(Arrays.toString((float[]) value));
                } else if (value instanceof double[]) {
                    out.append(Arrays.toString((double[]) value));
                } else if (value instanceof String[]) {
                    out.append(Arrays.toString((String[]) value));
                } else if (value instanceof CharSequence[]) {
                    out.append(Arrays.toString((CharSequence[]) value));
                } else if (value instanceof Parcelable[]) {
                    out.append(Arrays.toString((Parcelable[]) value));
                } else if (value instanceof Bundle) {
                    out.append(bundleToString((Bundle) value));
                } else {
                    out.append(value);
                }

                first = false;
            }
        }

        out.append("]");
        return out.toString();
    }
    */

    //--------------------------------------------------------------

    static void startPPService(Context context, Intent serviceIntent) {
        //if (isPPService)
        //    PhoneProfilesService.startForegroundNotification = true;
        if (Build.VERSION.SDK_INT < 26)
            context.getApplicationContext().startService(serviceIntent);
        else {
            boolean notificationsEnbaled = true;
            if (Build.VERSION.SDK_INT >= 33) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationsEnbaled = notificationManager.areNotificationsEnabled();
            }
            if (notificationsEnbaled)
                context.getApplicationContext().startForegroundService(serviceIntent);
        }
    }

    static void runCommand(Context context, Intent intent) {
//        PPApplication.logE("[LOCAL_BROADCAST_CALL] PPApplication.runCommand", "xxx");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    //--------------------------------------------------------------

    static void forceUpdateGUI(Context context, boolean alsoEditor, boolean alsoNotification/*, boolean refresh*/) {
        // update gui even when app is not fully started
        //if (!PPApplication.applicationFullyStarted)
        //    return;

        // icon widget
        try {
            //IconWidgetProvider myWidget = new IconWidgetProvider();
            //myWidget.updateWidgets(context, refresh);
            IconWidgetProvider.updateWidgets(context/*, true*/);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        // one row widget
        try {
            OneRowWidgetProvider.updateWidgets(context/*, true*/);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        // list widget
        try {
            //ProfileListWidgetProvider myWidget = new ProfileListWidgetProvider();
            //myWidget.updateWidgets(context, refresh);
            ProfileListWidgetProvider.updateWidgets(context/*, true*/);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        // one row profile list widget
        try {
            OneRowProfileListWidgetProvider.updateWidgets(context/*, true*/);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        // Samsung edge panel
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            try {
                //SamsungEdgeProvider myWidget = new SamsungEdgeProvider();
                //myWidget.updateWidgets(context, refresh);
                SamsungEdgeProvider.updateWidgets(context/*, true*/);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }

        // dash clock extension
//        PPApplication.logE("[LOCAL_BROADCAST_CALL] PPApplication.forceUpdateGUI", "dash clock extension)");
        Intent intent3 = new Intent(PPApplication.PACKAGE_NAME + ".DashClockBroadcastReceiver");
        //intent3.putExtra(DashClockBroadcastReceiver.EXTRA_REFRESH, true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);

        // activities
//        PPApplication.logE("[LOCAL_BROADCAST_CALL] PPApplication.forceUpdateGUI", "activities");
        Intent intent5 = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
        //intent5.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH, true);
        intent5.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ALSO_EDITOR, alsoEditor);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent5);

        // dynamic shortcuts
        DataWrapperStatic.setDynamicLauncherShortcuts(context);

        // restart tile - this invoke onStartListening()
        // require in manifest file for TileService this meta data:
        //     <meta-data android:name="android.service.quicksettings.ACTIVE_TILE"
        //         android:value="true" />
        TileService.requestListeningState(context, new ComponentName(context, PPTileService1.class));
        TileService.requestListeningState(context, new ComponentName(context, PPTileService2.class));
        TileService.requestListeningState(context, new ComponentName(context, PPTileService3.class));
        TileService.requestListeningState(context, new ComponentName(context, PPTileService4.class));
        TileService.requestListeningState(context, new ComponentName(context, PPTileService5.class));

        ProfileListNotification.drawNotification(true, context);

        if (alsoNotification) {
//            PPApplication.logE("[PPP_NOTIFICATION] PPApplication.forceUpdateGUI", "call of PPPAppNotification.drawNotification");
            PPPAppNotification.drawNotification(true, context);
        }
    }

    static void updateGUI(final boolean drawImmediattely, final boolean longDelay, final Context context)
    {
        try {
            final Context appContext = context.getApplicationContext();
            LocaleHelper.setApplicationLocale(appContext);

            if (drawImmediattely) {
//                PPApplication.logE("[PPP_NOTIFICATION] PPApplication.updateGUI (1)", "call of forceUpdateGUI");
                PPApplication.forceUpdateGUI(appContext, true, true/*, true*/);
                return;
            }

            int delay = 1;
            if (longDelay)
                delay = 10;

//            PPApplication.logE("[EXECUTOR_CALL]  ***** PPApplication.updateGUI", "schedule");

            //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
            Runnable runnable = () -> {
//                long start = System.currentTimeMillis();
//                PPApplication.logE("[IN_EXECUTOR]  ***** PPApplication.updateGUI", "--------------- START");

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPApplication_updateGUI");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

//                    PPApplication.logE("[PPP_NOTIFICATION] PPApplication.updateGUI (2)", "call of forceUpdateGUI");
                    PPApplication.forceUpdateGUI(appContext, true, false);
                    if (longDelay) {
//                        PPApplication.logE("[PPP_NOTIFICATION] PPApplication.updateGUI (1)", "call of PPPAppNotification.forceDrawNotification");
                        PPPAppNotification.forceDrawNotification(appContext);
                        ProfileListNotification.forceDrawNotification(appContext);
                    }


//                    long finish = System.currentTimeMillis();
//                    long timeElapsed = finish - start;
//                    PPApplication.logE("[IN_EXECUTOR]  ***** PPApplication.updateGUI", "--------------- END - timeElapsed="+timeElapsed);
                } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplication.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                    //worker.shutdown();
                }
            };
            PPApplication.delayedGuiExecutor.schedule(runnable, delay, TimeUnit.SECONDS);

            /*
            PPApplication.startHandlerThread();
            final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
            //__handler.postDelayed(new PPApplication.PPHandlerThreadRunnable(
            //        context.getApplicationContext()) {
            __handler.postDelayed(() -> {
//            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPApplication.updateGUI");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPApplication_updateGUI");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        // for longDelay=true, redraw also notiification
                        // for longDelay=false, notification redraw is called after this postDelayed()
                        PPApplication.forceUpdateGUI(appContext, true, longDelay);

                    } catch (Exception e) {
    //                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            }, delay * 1000L);
            */

            if (!longDelay) {
//                PPApplication.logE("[PPP_NOTIFICATION] PPApplication.updateGUI (2)", "call of PPPAppNotification.drawNotification");
                ProfileListNotification.drawNotification(false, context);
                PPPAppNotification.drawNotification(false, context);
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    /*
    static void updateNotificationAndWidgets(boolean refresh, boolean forService, Context context)
    {
        PPPAppNotification.showNotification(refresh, forService);
        updateGUI(context, true, refresh);
    }
    */

    static void showToast(final Context context, final String text, final int length) {
        final Context appContext = context.getApplicationContext();
        Handler handler = new Handler(context.getApplicationContext().getMainLooper());
        handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPApplication.showToast");
            try {
                LocaleHelper.setApplicationLocale(appContext);

                //ToastCompat msg = ToastCompat.makeText(appContext, text, length);
                ToastCompat msg = ToastCompat.makeCustom(appContext,
                        R.layout.toast_layout, R.drawable.toast_background,
                        R.id.custom_toast_message, text,
                        length);

                /*
                Toast msg = new Toast(appContext);
                View view = LayoutInflater.from(appContext).inflate(R.layout.toast_layout, null);
                TextView txtMsg = view.findViewById(R.id.custom_toast_message);
                txtMsg.setText(text);
                view.setBackgroundResource(R.drawable.toast_background);
                msg.setView(view);
                msg.setDuration(length);
                */

                msg.show();
            } catch (Exception ignored) {
                //PPApplication.recordException(e);
            }
        });
    }

    //--------------------------------------------------------------

    static void loadGlobalApplicationData(Context context) {
        synchronized (applicationStartedMutex) {
            applicationStarted = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_APPLICATION_STARTED, false);
        }
        synchronized (globalEventsRunStopMutex) {
            globalEventsRunStop = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(Event.PREF_GLOBAL_EVENTS_RUN_STOP, true);
        }

        //IgnoreBatteryOptimizationNotification.getShowIgnoreBatteryOptimizationNotificationOnStart(context);
        CheckCriticalPPPReleasesBroadcastReceiver.getShowCriticalGitHubReleasesNotification(context);
        getActivityLogEnabled(context);
        //getNotificationProfileName(context);
        //getWidgetProfileName(context);
        //getActivityProfileName(context);
        getLastActivatedProfile(context);
        getWallpaperChangeTime(context);
        Event.getEventsBlocked(context);
        Event.getForceRunEventRunning(context);
        PPPExtenderBroadcastReceiver.getApplicationInForeground(context);
        EventPreferencesCall.getEventCallEventType(context);
        EventPreferencesCall.getEventCallEventTime(context);
        EventPreferencesCall.getEventCallPhoneNumber(context);
        EventPreferencesCall.getEventCallSIMSlot(context);
        HeadsetConnectionBroadcastReceiver.getEventHeadsetParameters(context);
        WifiScanner.getForceOneWifiScan(context);
        BluetoothScanner.getForceOneBluetoothScan(context);
        BluetoothScanner.getForceOneLEBluetoothScan(context);
        BluetoothScanWorker.getBluetoothEnabledForScan(context);
        BluetoothScanWorker.getScanRequest(context);
        BluetoothScanWorker.getLEScanRequest(context);
        BluetoothScanWorker.getWaitForResults(context);
        BluetoothScanWorker.getWaitForLEResults(context);
        BluetoothScanWorker.getScanKilled(context);
        WifiScanWorker.getWifiEnabledForScan(context);
        WifiScanWorker.getScanRequest(context);
        WifiScanWorker.getWaitForResults(context);
        EventPreferencesRoaming.getEventRoamingInSIMSlot(context, 0);
        EventPreferencesRoaming.getEventRoamingInSIMSlot(context, 1);
        EventPreferencesRoaming.getEventRoamingInSIMSlot(context, 2);

        ApplicationPreferences.loadStartTargetHelps(context);
    }

    static void loadApplicationPreferences(Context context) {
        synchronized (PPApplication.applicationPreferencesMutex) {
            ApplicationPreferences.editorOrderSelectedItem(context);
            ApplicationPreferences.editorSelectedView(context);
            ApplicationPreferences.editorProfilesViewSelectedItem(context);
            ApplicationPreferences.editorEventsViewSelectedItem(context);
            //ApplicationPreferences.applicationFirstStart(context);
            ApplicationPreferences.applicationStartOnBoot(context);
            ApplicationPreferences.applicationActivate(context);
            ApplicationPreferences.applicationStartEvents(context);
            ApplicationPreferences.applicationActivateWithAlert(context);
            ApplicationPreferences.applicationClose(context);
            ApplicationPreferences.applicationLongClickActivation(context);
            //ApplicationPreferences.applicationLanguage(context);
            ApplicationPreferences.applicationTheme(context);
            //ApplicationPreferences.applicationActivatorPrefIndicator(context);
            ApplicationPreferences.applicationEditorPrefIndicator(context);
            //ApplicationPreferences.applicationActivatorHeader(context);
            //ApplicationPreferences.applicationEditorHeader(context);
            ApplicationPreferences.notificationsToast(context);
            //ApplicationPreferences.notificationStatusBar(context);
            //ApplicationPreferences.notificationStatusBarPermanent(context);
            //ApplicationPreferences.notificationStatusBarCancel(context);
            ApplicationPreferences.notificationStatusBarStyle(context);
            ApplicationPreferences.notificationShowInStatusBar(context);
            ApplicationPreferences.notificationTextColor(context);
            ApplicationPreferences.notificationHideInLockScreen(context);
            //ApplicationPreferences.notificationTheme(context);
            ApplicationPreferences.applicationWidgetListPrefIndicator(context);
            ApplicationPreferences.applicationWidgetListPrefIndicatorLightness(context);
            ApplicationPreferences.applicationWidgetListHeader(context);
            ApplicationPreferences.applicationWidgetListBackground(context);
            ApplicationPreferences.applicationWidgetListLightnessB(context);
            ApplicationPreferences.applicationWidgetListLightnessT(context);
            ApplicationPreferences.applicationWidgetIconColor(context);
            ApplicationPreferences.applicationWidgetIconLightness(context);
            ApplicationPreferences.applicationWidgetListIconColor(context);
            ApplicationPreferences.applicationWidgetListIconLightness(context);
            //ApplicationPreferences.applicationEditorAutoCloseDrawer(context);
            //ApplicationPreferences.applicationEditorSaveEditorState(context);
            ApplicationPreferences.notificationPrefIndicator(context);
            ApplicationPreferences.notificationPrefIndicatorLightness(context);
            ApplicationPreferences.applicationHomeLauncher(context);
            ApplicationPreferences.applicationWidgetLauncher(context);
            ApplicationPreferences.applicationNotificationLauncher(context);
            ApplicationPreferences.applicationEventWifiScanInterval(context);
            ApplicationPreferences.applicationDefaultProfile(context);
            ApplicationPreferences.applicationDefaultProfileNotificationSound(context);
            ApplicationPreferences.applicationDefaultProfileNotificationVibrate(context);
            //ApplicationPreferences.applicationDefaultProfileUsage(context);
            ApplicationPreferences.applicationActivatorGridLayout(context);
            ApplicationPreferences.applicationWidgetListGridLayout(context);
            ApplicationPreferences.applicationWidgetListCompactGrid(context);
            ApplicationPreferences.applicationEventBluetoothScanInterval(context);
            //ApplicationPreferences.applicationEventWifiRescan(context);
            //ApplicationPreferences.applicationEventBluetoothRescan(context);
            ApplicationPreferences.applicationWidgetIconHideProfileName(context);
            ApplicationPreferences.applicationShortcutEmblem(context);
            ApplicationPreferences.applicationEventWifiScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode(context);
            //ApplicationPreferences.applicationPowerSaveModeInternal(context);
            ApplicationPreferences.applicationEventBluetoothLEScanDuration(context);
            ApplicationPreferences.applicationEventLocationUpdateInterval(context);
            ApplicationPreferences.applicationEventLocationUpdateInPowerSaveMode(context);
            ApplicationPreferences.applicationEventLocationUseGPS(context);
            //ApplicationPreferences.applicationEventLocationRescan(context);
            ApplicationPreferences.applicationEventOrientationScanInterval(context);
            ApplicationPreferences.applicationEventOrientationScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventMobileCellsScanInPowerSaveMode(context);
            //ApplicationPreferences.applicationEventMobileCellsRescan(context);
            ApplicationPreferences.applicationDeleteOldActivityLogs(context);
            ApplicationPreferences.applicationWidgetIconBackground(context);
            ApplicationPreferences.applicationWidgetIconLightnessB(context);
            ApplicationPreferences.applicationWidgetIconLightnessT(context);
            ApplicationPreferences.applicationEventUsePriority(context);
            ApplicationPreferences.applicationUnlinkRingerNotificationVolumes(context);
            ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes(context);
            //ApplicationPreferences.applicationSamsungEdgePrefIndicator(context);
            ApplicationPreferences.applicationSamsungEdgeHeader(context);
            ApplicationPreferences.applicationSamsungEdgeBackground(context);
            ApplicationPreferences.applicationSamsungEdgeLightnessB(context);
            ApplicationPreferences.applicationSamsungEdgeLightnessT(context);
            ApplicationPreferences.applicationSamsungEdgeIconColor(context);
            ApplicationPreferences.applicationSamsungEdgeIconLightness(context);
            //ApplicationPreferences.applicationSamsungEdgeGridLayout(context);
            ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationRestartEventsWithAlert(context);
            ApplicationPreferences.applicationWidgetListRoundedCorners(context);
            ApplicationPreferences.applicationWidgetIconRoundedCorners(context);
            ApplicationPreferences.applicationWidgetListBackgroundType(context);
            ApplicationPreferences.applicationWidgetListBackgroundColor(context);
            ApplicationPreferences.applicationWidgetIconBackgroundType(context);
            ApplicationPreferences.applicationWidgetIconBackgroundColor(context);
            ApplicationPreferences.applicationSamsungEdgeBackgroundType(context);
            ApplicationPreferences.applicationSamsungEdgeBackgroundColor(context);
            //ApplicationPreferences.applicationEventWifiEnableWifi(context);
            //ApplicationPreferences.applicationEventBluetoothEnableBluetooth(context);
            ApplicationPreferences.applicationEventWifiScanIfWifiOff(context);
            ApplicationPreferences.applicationEventBluetoothScanIfBluetoothOff(context);
            ApplicationPreferences.applicationEventWifiEnableScanning(context);
            ApplicationPreferences.applicationEventBluetoothEnableScanning(context);
            ApplicationPreferences.applicationEventLocationEnableScanning(context);
            ApplicationPreferences.applicationEventMobileCellEnableScanning(context);
            ApplicationPreferences.applicationEventOrientationEnableScanning(context);
            ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventNeverAskForEnableRun(context);
            ApplicationPreferences.applicationUseAlarmClock(context);
            ApplicationPreferences.applicationNeverAskForGrantRoot(context);
            ApplicationPreferences.applicationNeverAskForGrantG1Permission(context);
            ApplicationPreferences.notificationShowButtonExit(context);
            ApplicationPreferences.applicationWidgetOneRowPrefIndicator(context);
            ApplicationPreferences.applicationWidgetOneRowPrefIndicatorLightness(context);
            ApplicationPreferences.applicationWidgetOneRowBackground(context);
            ApplicationPreferences.applicationWidgetOneRowLightnessB(context);
            ApplicationPreferences.applicationWidgetOneRowLightnessT(context);
            ApplicationPreferences.applicationWidgetOneRowIconColor(context);
            ApplicationPreferences.applicationWidgetOneRowIconLightness(context);
            ApplicationPreferences.applicationWidgetOneRowRoundedCorners(context);
            ApplicationPreferences.applicationWidgetOneRowBackgroundType(context);
            ApplicationPreferences.applicationWidgetOneRowBackgroundColor(context);
            ApplicationPreferences.applicationWidgetListLightnessBorder(context);
            ApplicationPreferences.applicationWidgetOneRowLightnessBorder(context);
            ApplicationPreferences.applicationWidgetIconLightnessBorder(context);
            ApplicationPreferences.applicationWidgetListShowBorder(context);
            ApplicationPreferences.applicationWidgetOneRowShowBorder(context);
            ApplicationPreferences.applicationWidgetIconShowBorder(context);
            ApplicationPreferences.applicationWidgetListCustomIconLightness(context);
            ApplicationPreferences.applicationWidgetOneRowCustomIconLightness(context);
            ApplicationPreferences.applicationWidgetIconCustomIconLightness(context);
            ApplicationPreferences.applicationSamsungEdgeCustomIconLightness(context);
            //ApplicationPreferences.notificationDarkBackground(context);
            ApplicationPreferences.notificationUseDecoration(context);
            ApplicationPreferences.notificationLayoutType(context);
            ApplicationPreferences.notificationBackgroundColor(context);
            //ApplicationPreferences.applicationNightModeOffTheme(context);
            ApplicationPreferences.applicationEventMobileCellNotUsedCellsDetectionNotificationEnabled(context);
            ApplicationPreferences.applicationSamsungEdgeVerticalPosition(context);
            ApplicationPreferences.notificationBackgroundCustomColor(context);
            //ApplicationPreferences.notificationNightMode(context);
            ApplicationPreferences.applicationEditorHideHeaderOrBottomBar(context);
            ApplicationPreferences.applicationWidgetIconShowProfileDuration(context);
            ApplicationPreferences.notificationNotificationStyle(context);
            ApplicationPreferences.notificationShowProfileIcon(context);
            ApplicationPreferences.applicationEventPeriodicScanningEnableScanning(context);
            ApplicationPreferences.applicationEventPeriodicScanningScanInterval(context);
            ApplicationPreferences.applicationEventPeriodicScanningScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventPeriodicScanningScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationEventWifiScanIgnoreHotspot(context);
            ApplicationPreferences.applicationEventNotificationEnableScanning(context);
            ApplicationPreferences.applicationEventNotificationScanInPowerSaveMode(context);
            ApplicationPreferences.applicationEventNotificationScanOnlyWhenScreenIsOn(context);
            ApplicationPreferences.applicationWidgetOneRowRoundedCornersRadius(context);
            ApplicationPreferences.applicationWidgetListRoundedCornersRadius(context);
            ApplicationPreferences.applicationWidgetIconRoundedCornersRadius(context);
            ApplicationPreferences.applicationActivatorNumColums(context);
            ApplicationPreferences.applicationApplicationInterfaceNotificationSound(context);
            ApplicationPreferences.applicationApplicationInterfaceNotificationVibrate(context);
            ApplicationPreferences.applicationActivatorAddRestartEventsIntoProfileList(context);
            ApplicationPreferences.applicationActivatorIncreaseBrightness(context);
            ApplicationPreferences.applicationWidgetOneRowLayoutHeight(context);
            //ApplicationPreferences.applicationWidgetOneRowHigherLayout(context);
            ApplicationPreferences.applicationWidgetIconChangeColorsByNightMode(context);
            ApplicationPreferences.applicationWidgetOneRowChangeColorsByNightMode(context);
            ApplicationPreferences.applicationWidgetListChangeColorsByNightMode(context);
            ApplicationPreferences.applicationSamsungEdgeChangeColorsByNightMode(context);
            ApplicationPreferences.applicationForceSetBrightnessAtScreenOn(context);
            ApplicationPreferences.notificationProfileIconColor(context);
            ApplicationPreferences.notificationProfileIconLightness(context);
            ApplicationPreferences.notificationCustomProfileIconLightness(context);
            ApplicationPreferences.applicationShortcutIconColor(context);
            ApplicationPreferences.applicationShortcutIconLightness(context);
            ApplicationPreferences.applicationShortcutCustomIconLightness(context);
            ApplicationPreferences.notificationShowRestartEventsAsButton(context);
            ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile(context);
            ApplicationPreferences.applicationWidgetIconUseDynamicColors(context);
            ApplicationPreferences.applicationWidgetOneRowUseDynamicColors(context);
            ApplicationPreferences.applicationWidgetListUseDynamicColors(context);
            ApplicationPreferences.applicationRestartEventsIconColor(context);
            //ApplicationPreferences.applicationIncreaseBrightnessForProfileIcon(context);
            ApplicationPreferences.applicationWidgetIconBackgroundColorNightModeOff(context);
            ApplicationPreferences.applicationWidgetIconBackgroundColorNightModeOn(context);
            ApplicationPreferences.applicationWidgetOneRowBackgroundColorNightModeOff(context);
            ApplicationPreferences.applicationWidgetOneRowBackgroundColorNightModeOn(context);
            ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOff(context);
            ApplicationPreferences.applicationWidgetListBackgroundColorNightModeOn(context);
            ApplicationPreferences.applicationSamsungEdgeBackgroundColorNightModeOff(context);
            ApplicationPreferences.applicationSamsungEdgeBackgroundColorNightModeOn(context);
            ApplicationPreferences.applicationWidgetIconLayoutHeight(context);
            ApplicationPreferences.applicationWidgetIconFillBackground(context);
            ApplicationPreferences.applicationWidgetOneRowFillBackground(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListFillBackground(context);

            ApplicationPreferences.applicationWidgetOneRowProfileListBackground(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListLightnessB(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListIconColor(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListIconLightness(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListRoundedCorners(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundType(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundColor(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListLightnessBorder(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListShowBorder(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListCustomIconLightness(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListRoundedCornersRadius(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListLayoutHeight(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListChangeColorsByNightMode(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListUseDynamicColors(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundColorNightModeOff(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListBackgroundColorNightModeOn(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListArrowsMarkLightness(context);
            ApplicationPreferences.applicationWidgetOneRowProfileListNumberOfProfilesPerPage(context);

            ApplicationPreferences.notificationProfileListDisplayNotification(context);
            ApplicationPreferences.notificationProfileListShowInStatusBar(context);
            ApplicationPreferences.notificationProfileListHideInLockScreen(context);
            ApplicationPreferences.notificationProfileListStatusBarStyle(context);
            ApplicationPreferences.notificationProfileListBackgroundColor(context);
            ApplicationPreferences.notificationProfileListBackgroundCustomColor(context);
            ApplicationPreferences.notificationProfileListPrefArrowsMarkLightness(context);
            ApplicationPreferences.notificationProfileListNumberOfProfilesPerPage(context);
            ApplicationPreferences.notificationProfileListIconColor(context);
            ApplicationPreferences.notificationProfileListIconLightness(context);
            ApplicationPreferences.notificationProfileListCustomIconLightness(context);

            ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiply(context);
            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventBluetoothScanInTimeMultiply(context);
            ApplicationPreferences.applicationEventLocationScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventLocationScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventLocationScanInTimeMultiply(context);
            ApplicationPreferences.applicationEventMobileCellScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventMobileCellScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventMobileCellScanInTimeMultiply(context);
            ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventNotificationScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventNotificationScanInTimeMultiply(context);
            ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventOrientationScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventOrientationScanInTimeMultiply(context);
            ApplicationPreferences.applicationEventWifiScanInTimeMultiplyFrom(context);
            ApplicationPreferences.applicationEventWifiScanInTimeMultiplyTo(context);
            ApplicationPreferences.applicationEventWifiScanInTimeMultiply(context);

            ApplicationPreferences.deleteBadPreferences(context);
        }
    }

    static void loadProfileActivationData(Context context) {
        ActivateProfileHelper.getRingerVolume(context);
        ActivateProfileHelper.getNotificationVolume(context);
        ActivateProfileHelper.getRingerMode(context);
        ActivateProfileHelper.getZenMode(context);
        ActivateProfileHelper.getLockScreenDisabled(context);
        ActivateProfileHelper.getActivatedProfileScreenTimeoutWhenScreenOff(context);
        ActivateProfileHelper.getKeepScreenOnPermanent(context);
        ActivateProfileHelper.getMergedRingNotificationVolumes(context);
        //Profile.getActivatedProfileForDuration(context);
        ProfileStatic.getActivatedProfileEndDurationTime(context);
    }

    //--------------------------------------------------------------

    static boolean getApplicationStarted(boolean testService, boolean testExport)
    {
        synchronized (applicationStartedMutex) {
            if (testService) {
                try {
                    return applicationStarted &&
                            ((!testExport) || (!exportIsRunning)) &&
                            (PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().getServiceHasFirstStart();
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return applicationStarted &&
                        ((!testExport) || (!exportIsRunning));
        }
    }

    static void setApplicationStarted(Context context, boolean appStarted)
    {
        synchronized (applicationStartedMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_APPLICATION_STARTED, appStarted);
            editor.apply();
            applicationStarted = appStarted;
        }
    }

    static int getSavedVersionCode(Context context) {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_SAVED_VERSION_CODE, 0);
    }

    static void setSavedVersionCode(Context context, int version)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_SAVED_VERSION_CODE, version);
        editor.apply();
    }

    static boolean prefActivityLogEnabled;
    private static void getActivityLogEnabled(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            prefActivityLogEnabled = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_ACTIVITY_LOG_ENABLED, true);
            //return prefActivityLogEnabled;
        }
    }
    static void setActivityLogEnabled(Context context, boolean enabled)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_ACTIVITY_LOG_ENABLED, enabled);
            editor.apply();
            prefActivityLogEnabled = enabled;
        }
    }

    /*
    static String prefNotificationProfileName;
    private static void getNotificationProfileName(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            prefNotificationProfileName = ApplicationPreferences.
                    getSharedPreferences(context).getString(PREF_NOTIFICATION_PROFILE_NAME, "");
            //return prefNotificationProfileName;
        }
    }
    static public void setNotificationProfileName(Context context, String notificationProfileName)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_NOTIFICATION_PROFILE_NAME, notificationProfileName);
            editor.apply();
            prefNotificationProfileName = notificationProfileName;
        }
    }
     */

    /*
    static String prefWidgetProfileName1;
    static String prefWidgetProfileName2;
    static String prefWidgetProfileName3;
    static String prefWidgetProfileName4;
    static String prefWidgetProfileName5;
    private static void getWidgetProfileName(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
            prefWidgetProfileName1 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_1", "");
            prefWidgetProfileName2 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_2", "");
            prefWidgetProfileName3 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_3", "");
            prefWidgetProfileName4 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_4", "");
            prefWidgetProfileName5 = preferences.getString(PREF_WIDGET_PROFILE_NAME + "_5", "");
            //return prefNotificationProfileName;
        }
    }
    static void setWidgetProfileName(Context context, int widgetType, String widgetProfileName)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_WIDGET_PROFILE_NAME + "_" + widgetType, widgetProfileName);
            editor.apply();
            switch (widgetType) {
                case 1:
                    prefWidgetProfileName1 = widgetProfileName;
                    break;
                case 2:
                    prefWidgetProfileName2 = widgetProfileName;
                    break;
                case 3:
                    prefWidgetProfileName3 = widgetProfileName;
                    break;
                case 4:
                    prefWidgetProfileName4 = widgetProfileName;
                    break;
                case 5:
                    prefWidgetProfileName5 = widgetProfileName;
                    break;
            }
        }
    }

    static String prefActivityProfileName1;
    static String prefActivityProfileName2;
    static String prefActivityProfileName3;
    private static void getActivityProfileName(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            SharedPreferences preferences = ApplicationPreferences.getSharedPreferences(context);
            prefActivityProfileName1 = preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_1", "");
            prefActivityProfileName2 = preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_2", "");
            prefActivityProfileName3 = preferences.getString(PREF_ACTIVITY_PROFILE_NAME + "_3", "");
            //return prefActivityProfileName;
        }
    }
    static void setActivityProfileName(Context context, int activityType, String activityProfileName)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putString(PREF_ACTIVITY_PROFILE_NAME + "_" + activityType, activityProfileName);
            editor.apply();
            switch (activityType) {
                case 1:
                    prefActivityProfileName1 = activityProfileName;
                    break;
                case 2:
                    prefActivityProfileName2 = activityProfileName;
                    break;
                case 3:
                    prefActivityProfileName3 = activityProfileName;
                    break;
            }
        }
    }
    */

    static long prefLastActivatedProfile;
    private static void getLastActivatedProfile(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            prefLastActivatedProfile = ApplicationPreferences.
                    getSharedPreferences(context).getLong(PREF_LAST_ACTIVATED_PROFILE, 0);
            //return prefLastActivatedProfile;
        }
    }
    static void setLastActivatedProfile(Context context, long profileId)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putLong(PREF_LAST_ACTIVATED_PROFILE, profileId);
            editor.apply();
            prefLastActivatedProfile = profileId;
        }
    }

    static long wallpaperChangeTime;
    private static void getWallpaperChangeTime(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            wallpaperChangeTime = ApplicationPreferences.
                    getSharedPreferences(context).getLong(PREF_WALLPAPER_CHANGE_TIME, 0);
            //return prefLastActivatedProfile;
        }
    }
    static void setWallpaperChangeTime(Context context)
    {
        synchronized (applicationGlobalPreferencesMutex) {
            Calendar now = Calendar.getInstance();
            long _time = now.getTimeInMillis();
            Editor editor = ApplicationPreferences.getEditor(context);
            editor.putLong(PREF_WALLPAPER_CHANGE_TIME, _time);
            editor.apply();
            wallpaperChangeTime = _time;
        }
    }

    static int getDaysAfterFirstStart(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_DAYS_AFTER_FIRST_START, 0);
    }
    static void setDaysAfterFirstStart(Context context, int days)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_DAYS_AFTER_FIRST_START, days);
        editor.apply();
    }

    static int getDonationNotificationCount(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_DONATION_NOTIFICATION_COUNT, 0);
    }
    static void setDonationNotificationCount(Context context, int days)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_DONATION_NOTIFICATION_COUNT, days);
        editor.apply();
    }

    static int getDaysForNextDonationNotification(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getInt(PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION, 0);
    }
    static void setDaysForNextDonationNotification(Context context, int days)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putInt(PREF_DAYS_FOR_NEXT_DONATION_NOTIFICATION, days);
        editor.apply();
    }

    static boolean getDonationDonated(Context context)
    {
        return ApplicationPreferences.
                getSharedPreferences(context).getBoolean(PREF_DONATION_DONATED, false);
    }
    static void setDonationDonated(Context context)
    {
        Editor editor = ApplicationPreferences.getEditor(context);
        editor.putBoolean(PREF_DONATION_DONATED, true);
        editor.apply();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean isIgnoreBatteryOptimizationEnabled(Context appContext) {
        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
        try {
            if (pm != null) {
                return pm.isIgnoringBatteryOptimizations(PPApplication.PACKAGE_NAME);
            }
        } catch (Exception ignore) {
            return false;
        }
        return false;
    }

    // --------------------------------

    // notification channels -------------------------

    static void createPPPAppNotificationChannel(/*Profile profile, */Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(PROFILE_NOTIFICATION_CHANNEL) != null)
                    return;// true;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_activated_profile);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_activated_profile_description_ppp);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(PROFILE_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setSound(null, null);
                channel.setShowBadge(false);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
                NotificationChannel newChannel = notificationManager.getNotificationChannel(PROFILE_NOTIFICATION_CHANNEL);

                if (newChannel == null)
                    throw new RuntimeException("PPApplication.createPPPAppNotificationChannel - NOT CREATED - newChannel=null");
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
        //return true;
    }

    static void createMobileCellsRegistrationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.phone_profiles_pref_applicationEventMobileCellsRegistration_notification);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_mobile_cells_registration_description);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(MOBILE_CELLS_REGISTRATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(true);
                channel.enableVibration(true);
                //channel.setSound(null, null);
                channel.setShowBadge(true);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createInformationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(INFORMATION_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_information);
                // The user-visible description of the channel.
                String description = context.getString(R.string.empty_string);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(INFORMATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(true);
                channel.enableVibration(true);
                //channel.setSound(null, null);
                channel.setShowBadge(false);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createExclamationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(EXCLAMATION_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_exclamation);
                // The user-visible description of the channel.
                String description = context.getString(R.string.empty_string);

                NotificationChannel channel = new NotificationChannel(EXCLAMATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_HIGH);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(true);
                channel.enableVibration(true);
                //channel.setSound(null, null);
                //channel.setShowBadge(false);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createGrantPermissionNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(GRANT_PERMISSION_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_grant_permission);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_grant_permission_description);

                NotificationChannel channel = new NotificationChannel(GRANT_PERMISSION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_HIGH);

                // Configure the notification channel.
                //channel.setImportance(importance);
                channel.setDescription(description);
                channel.enableLights(true);
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                //channel.setLightColor(ContextCompat.getColor(context, R.color.altype_error));
                channel.enableVibration(true);
                //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createNotifyEventStartNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(NOTIFY_EVENT_START_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_notify_event_start);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_notify_event_start_description);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(NOTIFY_EVENT_START_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(false);
                channel.enableVibration(true);
                //channel.setSound(null, null);
                channel.setShowBadge(true);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createMobileCellsNewCellNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_not_used_mobile_cell);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_not_used_mobile_cell_description);

                NotificationChannel channel = new NotificationChannel(NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_HIGH);

                // Configure the notification channel.
                //channel.setImportance(importance);
                channel.setDescription(description);
                channel.enableLights(true);
                // Sets the notification light color for notifications posted to this
                // channel, if the device supports this feature.
                //channel.setLightColor(ContextCompat.getColor(context, R.color.altype_error));
                channel.enableVibration(true);
                //channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createDonationNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(DONATION_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_donation);
                // The user-visible description of the channel.
                String description = context.getString(R.string.empty_string);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(DONATION_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setSound(null, null);
                channel.setShowBadge(true);
                channel.setBypassDnd(false);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createNewReleaseNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(NEW_RELEASE_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_new_release);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_new_release_description);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(NEW_RELEASE_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(true);
                channel.enableVibration(true);
                //channel.setSound(null, null);
                channel.setShowBadge(true);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    /*
    static void createCrashReportNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(CRASH_REPORT_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_crash_report);
                // The user-visible description of the channel.
                String description = context.getString(R.string.empty_string);

                NotificationChannel channel = new NotificationChannel(CRASH_REPORT_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(true);
                channel.enableVibration(true);
                //channel.setSound(null, null);
                channel.setShowBadge(true);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }
    */

    static void createGeneratedByProfileNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(GENERATED_BY_PROFILE_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                CharSequence name = context.getString(R.string.notification_channel_generated_by_profile);
                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_generated_by_profile_description);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(GENERATED_BY_PROFILE_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(true);
                channel.enableVibration(true);
                //channel.setSound(null, null);
                channel.setShowBadge(true);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createKeepScreenOnNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(KEEP_SCREEN_ON_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                String name = context.getString(R.string.profile_preferences_deviceScreenOnPermanent);

                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_keep_screen_on_description) +
                        " \"" + context.getString(R.string.profile_preferences_deviceScreenOnPermanent) + "\".";

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(KEEP_SCREEN_ON_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(false);
                channel.enableVibration(false);
                //channel.setSound(null, null);
                channel.setShowBadge(false);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createProfileListNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
                if (notificationManager.getNotificationChannel(PROFILE_LIST_NOTIFICATION_CHANNEL) != null)
                    return;

                // The user-visible name of the channel.
                String name = context.getString(R.string.notification_channel_profile_list);

                // The user-visible description of the channel.
                String description = context.getString(R.string.notification_channel_profile_list_description);

                // !!! For OnePlus must be in IMPORTANCE_DEFAULT !!!
                // because in IMPORTANCE_LOW is not displayed icon in status bar. By me bug in OnePlus
                NotificationChannel channel = new NotificationChannel(PROFILE_LIST_NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_MIN);

                // Configure the notification channel.
                channel.setDescription(description);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setSound(null, null);
                channel.setShowBadge(false);
                channel.setBypassDnd(true);

                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
    }

    static void createNotificationChannels(Context appContext) {
        PPApplication.createDonationNotificationChannel(appContext);
        PPApplication.createExclamationNotificationChannel(appContext);
        PPApplication.createGeneratedByProfileNotificationChannel(appContext);
        PPApplication.createGrantPermissionNotificationChannel(appContext);
        PPApplication.createInformationNotificationChannel(appContext);
        PPApplication.createKeepScreenOnNotificationChannel(appContext);
        PPApplication.createMobileCellsNewCellNotificationChannel(appContext);
        PPApplication.createMobileCellsRegistrationNotificationChannel(appContext);
        PPApplication.createNewReleaseNotificationChannel(appContext);
        PPApplication.createNotifyEventStartNotificationChannel(appContext);
        PPApplication.createPPPAppNotificationChannel(appContext);
        PPApplication.createProfileListNotificationChannel(appContext);

        //PPApplication.createCrashReportNotificationChannel(appContext);
    }

    /*
    static void showProfileNotification() {
        try {
            if (PhoneProfilesService.getInstance() != null)
                PhoneProfilesService.getInstance().showProfileNotification(false);

        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }
    */

    // -----------------------------------------------

    // scanners ------------------------------------------

    static void registerContentObservers(Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_CONTENT_OBSERVERS, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void registerCallbacks(Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_CALLBACKS, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void registerPhoneCallsListener(boolean register, Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            if (register)
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_PHONE_CALLS_LISTENER, true);
            else
                commandIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_PHONE_CALLS_LISTENER, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void restartPeriodicScanningScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_PERIODIC_SCANNING_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_PERIODIC_SCANNING_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void forceRegisterReceiversForWifiScanner(Context context) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void reregisterReceiversForWifiScanner(Context context) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_WIFI_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void restartWifiScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_WIFI_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_WIFI_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void forceRegisterReceiversForBluetoothScanner(Context context) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void reregisterReceiversForBluetoothScanner(Context context) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_REGISTER_RECEIVERS_FOR_BLUETOOTH_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void restartBluetoothScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_BLUETOOTH_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_BLUETOOTH_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void restartLocationScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_LOCATION_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_LOCATION_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void restartOrientationScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ORIENTATION_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ORIENTATION_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    /*
    public static void forceStartOrientationScanner(Context context) {
        try {
            //Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_ORIENTATION_SCANNER);
            //serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            //PPApplication.startPPService(context, serviceIntent);

            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_ORIENTATION_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }
    */

    static void forceStartMobileCellsScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_MOBILE_CELLS_SCANNER);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_FORCE_START_MOBILE_CELLS_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void restartMobileCellsScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_MOBILE_CELLS_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_MOBILE_CELLS_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void restartTwilightScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_TWILIGHT_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_TWILIGHT_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void restartNotificationScanner(Context context/*, boolean forScreenOn*/) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_NOTIFICATION_SCANNER);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, true);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_NOTIFICATION_SCANNER);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void restartAllScanners(Context context, boolean fromBatteryChange) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ALL_SCANNERS);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ALL_SCANNERS);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_FROM_BATTERY_CHANGE, fromBatteryChange);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void rescanAllScanners(Context context) {
        try {
            /*Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_STOP_SCANNER_TYPE, SCANNER_RESTART_ALL_SCANNERS);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_FOR_SCREEN_ON, forScreenOn);
            PPApplication.startPPService(context, serviceIntent);*/
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_RESCAN_SCANNERS, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void registerPPPExtenderReceiverForSMSCall(boolean register, Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            if (register)
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_PPP_EXTENDER_FOR_SMS_CALL_RECEIVER, true);
            else
                commandIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_PPP_EXTENDER_FOR_SMS_CALL_RECEIVER, true);
            PPApplication.runCommand(context, commandIntent);
//            Log.e("PPApplication.registerPPPExtenderReceiverForSMSCall", "xxx");
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void registerReceiversForCallSensor(boolean register, Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            if (register)
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_RECEIVERS_FOR_CALL_SENSOR, true);
            else
                commandIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_FOR_CALL_SENSOR, true);
            PPApplication.runCommand(context, commandIntent);
//            Log.e("PPApplication.registerReceiversForCallSensor", "xxx");
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void registerReceiversForSMSSensor(boolean register, Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            if (register)
                commandIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_RECEIVERS_FOR_SMS_SENSOR, true);
            else
                commandIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_FOR_SMS_SENSOR, true);
            PPApplication.runCommand(context, commandIntent);
//            Log.e("PPApplication.registerReceiversForSMSSensor", "xxx");
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

/*
    public static void restartEvents(Context context, boolean unblockEventsRun, boolean reactivateProfile) {
        try {
//            Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
//            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
//            serviceIntent.putExtra(PhoneProfilesService.EXTRA_RESTART_EVENTS, true);
//            serviceIntent.putExtra(PostDelayedBroadcastReceiver.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun);
//            serviceIntent.putExtra(PostDelayedBroadcastReceiver.EXTRA_REACTIVATE_PROFILE, reactivateProfile);
//            PPApplication.startPPService(context, serviceIntent);
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_RESTART_EVENTS, true);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_UNBLOCK_EVENTS_RUN, unblockEventsRun);
            commandIntent.putExtra(PhoneProfilesService.EXTRA_REACTIVATE_PROFILE, reactivateProfile);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception ignored) {}
    }
*/

    /*
    public static void stopSimulatingRingingCall(boolean disableInternalChnage, Context context) {
        try {
            Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
            //commandIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            if (disableInternalChnage)
                commandIntent.putExtra(PhoneProfilesService.EXTRA_STOP_SIMULATING_RINGING_CALL, true);
            else
                commandIntent.putExtra(PhoneProfilesService.EXTRA_STOP_SIMULATING_RINGING_CALL_NO_DISABLE_INTERNAL_CHANGE, true);
            PPApplication.runCommand(context, commandIntent);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }
*/

    //---------------------------------------------------------------

    // others ------------------------------------------------------------------

    /*
    static boolean isScreenOn(PowerManager powerManager) {
        //if (Build.VERSION.SDK_INT >= 20)
            return powerManager.isInteractive();
        //else
        //    return powerManager.isScreenOn();
    }
    */

    /*
    private static String getROMManufacturer() {
        String line;
        BufferedReader input = null;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.product.brand");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (Exception ex) {
            Log.e("PPApplication.getROMManufacturer", "Unable to read sysprop ro.product.brand", ex);
            return null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (Exception e) {
                    Log.e("PPApplication.getROMManufacturer", "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }
    */

    private static boolean isXiaomi() {
        return Build.BRAND.equalsIgnoreCase("xiaomi") ||
               Build.MANUFACTURER.equalsIgnoreCase("xiaomi") ||
               Build.FINGERPRINT.toLowerCase().contains("xiaomi");
    }

    private static boolean isMIUIROM() {
        boolean miuiRom1 = false;
        boolean miuiRom2 = false;
        boolean miuiRom3 = false;

        String line;
        BufferedReader input;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.code");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            miuiRom1 = line.length() != 0;
            input.close();

            if (!miuiRom1) {
                p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                miuiRom2 = line.length() != 0;
                input.close();
            }

            if (!miuiRom1 && !miuiRom2) {
                p = Runtime.getRuntime().exec("getprop ro.miui.internal.storage");
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                miuiRom3 = line.length() != 0;
                input.close();
            }

        } catch (Exception ex) {
            //Log.e("PPApplication.isMIUIROM", Log.getStackTraceString(ex));
            PPApplication.recordException(ex);
        }

        return miuiRom1 || miuiRom2 || miuiRom3;
    }

    private static String getEmuiRomName() {
        String line;
        BufferedReader input;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop ro.build.version.emui");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
            return line;
        } catch (Exception ex) {
            //Log.e("PPApplication.getEmuiRomName", Log.getStackTraceString(ex));
            PPApplication.recordException(ex);
            return "";
        }
    }

    private static boolean isHuawei() {
        return Build.BRAND.equalsIgnoreCase("huawei") ||
                Build.MANUFACTURER.equalsIgnoreCase("huawei") ||
                Build.FINGERPRINT.toLowerCase().contains("huawei");
    }

    private static boolean isEMUIROM() {
        String emuiRomName = getEmuiRomName();

        return (emuiRomName.length() != 0) ||
                Build.DISPLAY.toLowerCase().contains("emui2.3");// || "EMUI 2.3".equalsIgnoreCase(emuiRomName);
    }

    private static boolean isSamsung() {
        return Build.BRAND.equalsIgnoreCase("samsung") ||
                Build.MANUFACTURER.equalsIgnoreCase("samsung") ||
                Build.FINGERPRINT.toLowerCase().contains("samsung");
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static String getOneUiVersion() throws Exception {
        //if (!isSemAvailable(getApplicationContext())) {
        //    return ""; // was "1.0" originally but probably just a dummy value for one UI devices
        //}
        Field semPlatformIntField = Build.VERSION.class.getDeclaredField("SEM_PLATFORM_INT");
        int version = semPlatformIntField.getInt(null) - 90000;
        if (version < 0) {
            // not one ui (could be previous Samsung OS)
            return "";
        }
        return (version / 10000) + "." + ((version % 10000) / 100);
    }

    /*
    private static boolean isSemAvailable(Context context) {
        return context != null &&
                (context.getPackageManager().hasSystemFeature("com.samsung.feature.samsung_experience_mobile") ||
                        context.getPackageManager().hasSystemFeature("com.samsung.feature.samsung_experience_mobile_lite"));
    }
    */

    private static boolean isGalaxyROM() {
        try {
            //noinspection unused
            String romName = getOneUiVersion();
            /*if (romName.isEmpty())
                return true; // old, non-OneUI ROM
            else
                return true; // OneUI ROM*/
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isLG() {
        return Build.BRAND.equalsIgnoreCase("lge") ||
                Build.MANUFACTURER.equalsIgnoreCase("lge") ||
                Build.FINGERPRINT.toLowerCase().contains("lge");
    }

    private static boolean isOnePlus() {
        return Build.BRAND.equalsIgnoreCase("oneplus") ||
                Build.MANUFACTURER.equalsIgnoreCase("oneplus") ||
                Build.FINGERPRINT.toLowerCase().contains("oneplus");
    }

    private static boolean isOppo() {
        return Build.BRAND.equalsIgnoreCase("oppo") ||
                Build.MANUFACTURER.equalsIgnoreCase("oppo") ||
                Build.FINGERPRINT.toLowerCase().contains("oppo");
    }

    private static boolean isRealme() {
        return Build.BRAND.equalsIgnoreCase("realme") ||
                Build.MANUFACTURER.equalsIgnoreCase("realme") ||
                Build.FINGERPRINT.toLowerCase().contains("realme");
    }

    private static boolean isLenovo() {
        return Build.BRAND.equalsIgnoreCase("lenovo") ||
                Build.MANUFACTURER.equalsIgnoreCase("lenovo") ||
                Build.FINGERPRINT.toLowerCase().contains("lenovo");
    }

    private static boolean isPixel() {
        return Build.BRAND.equalsIgnoreCase("google") ||
                Build.MANUFACTURER.equalsIgnoreCase("google") ||
                Build.FINGERPRINT.toLowerCase().contains("google");
    }

    private static boolean isSony() {
        return Build.BRAND.equalsIgnoreCase("sony") ||
                Build.MANUFACTURER.equalsIgnoreCase("sony") ||
                Build.FINGERPRINT.toLowerCase().contains("sony");
    }

    private static boolean isDoogee() {
        return Build.BRAND.equalsIgnoreCase("doogee") ||
                Build.MANUFACTURER.equalsIgnoreCase("doogee") ||
                Build.FINGERPRINT.toLowerCase().contains("doogee");
    }

    private static String getReadableModVersion() {
        String modVer = getSystemProperty(SYS_PROP_MOD_VERSION);
        return (modVer == null || modVer.length() == 0 ? "Unknown" : modVer);
    }

    private static String getSystemProperty(@SuppressWarnings("SameParameterValue") String propName)
    {
        String line;
        BufferedReader input = null;
        try
        {
            java.lang.Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        }
        catch (Exception ex)
        {
            PPApplication.recordException(ex);
            return null;
        }
        finally
        {
            if(input != null)
            {
                try
                {
                    input.close();
                }
                catch (Exception e)
                {
                    //Log.e("PPApplication.getSystemProperty", "Exception while closing InputStream", e);
                    PPApplication.recordException(e);
                }
            }
        }
        return line;
    }

    private static boolean hasSystemFeature(PackageManager packageManager, String feature) {
        try {
            return packageManager.hasSystemFeature(feature);
        } catch (Exception e) {
            return false;
        }
    }

    private static void _exitApp(final Context context, final DataWrapper dataWrapper, final Activity activity,
                               final boolean shutdown, final boolean removeNotifications) {
        try {
            PPApplication.logE("PPApplication._exitApp", "shutdown="+shutdown);

            if (!shutdown)
                PPApplication.cancelAllWorks(/*false*/);

            if (dataWrapper != null)
                dataWrapper.stopAllEvents(false, false, false, false);

            if (!shutdown) {

                // remove notifications
                ImportantInfoNotification.removeNotification(context);
                DrawOverAppsPermissionNotification.removeNotification(context);
                IgnoreBatteryOptimizationNotification.removeNotification(context);
                AutostartPermissionNotification.removeNotification(context);
                Permissions.removeNotifications(context);
                ProfileListNotification.clearNotification(context);

                if (removeNotifications) {
                    if (dataWrapper != null) {
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                        //noinspection ForLoopReplaceableByForEach
                        for (Iterator<Profile> it = dataWrapper.profileList.iterator(); it.hasNext(); ) {
                            Profile profile = it.next();
                            try {
                                notificationManager.cancel(
                                        PPApplication.DISPLAY_PREFERENCES_PROFILE_ERROR_NOTIFICATION_TAG + "_" + profile._id,
                                        PPApplication.PROFILE_ID_NOTIFICATION_ID + (int) profile._id);
                                notificationManager.cancel(
                                        PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_TAG,
                                        PPApplication.GENERATED_BY_PROFILE_NOTIFICATION_ID + (int) profile._id);
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                    }
                    ActivateProfileHelper.cancelNotificationsForInteractiveParameters(context);
                }

                addActivityLog(context, PPApplication.ALTYPE_APPLICATION_EXIT, null, null, "");

                //if (PPApplication.brightnessHandler != null) {
                //    PPApplication.brightnessHandler.post(new Runnable() {
                //        public void run() {
                //            ActivateProfileHelper.removeBrightnessView(context);
                //        }
                //    });
                //}
                //if (PPApplication.screenTimeoutHandler != null) {
                //    PPApplication.screenTimeoutHandler.post(new Runnable() {
                //        public void run() {
                            //ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(context);
                            //ActivateProfileHelper.removeBrightnessView(context);
                            ActivateProfileHelper.removeKeepScreenOnView(context);
                //        }
                //    });
                //}

                //PPApplication.initRoot();

                if (dataWrapper != null) {
                    synchronized (dataWrapper.profileList) {
                        if (!dataWrapper.profileListFilled)
                            dataWrapper.fillProfileList(false, false);
                        for (Profile profile : dataWrapper.profileList)
                            ProfileDurationAlarmBroadcastReceiver.removeAlarm(profile, context);
                    }

                    synchronized (dataWrapper.eventList) {
                        if (!dataWrapper.eventListFilled)
                            dataWrapper.fillEventList();
                        for (Event event : dataWrapper.eventList)
                            StartEventNotificationBroadcastReceiver.removeAlarm(event, context);
                    }
                }


                //Profile.setActivatedProfileForDuration(context, 0);
                if (dataWrapper != null) {
                    synchronized (PPApplication.profileActivationMutex) {
                        List<String> activateProfilesFIFO = new ArrayList<>();
                        dataWrapper.fifoSaveProfiles(activateProfilesFIFO);
                    }
                }
            }

            LocationScannerSwitchGPSBroadcastReceiver.removeAlarm(context);
            LockDeviceActivityFinishBroadcastReceiver.removeAlarm(context);

            PPApplication.logE("PPApplication._exitApp", "stop service");
            //PhoneProfilesService.getInstance().showProfileNotification(false);
            //context.stopService(new Intent(context, PhoneProfilesService.class));
            PhoneProfilesService.stop(/*context*/);
            //if (PhoneProfilesService.getInstance() != null)
            //    PhoneProfilesService.getInstance().setApplicationFullyStarted(false, false);

            Permissions.setAllShowRequestPermissions(context.getApplicationContext(), true);

            //WifiBluetoothScanner.setShowEnableLocationNotification(context.getApplicationContext(), true, WifiBluetoothScanner.SCANNER_TYPE_WIFI);
            //WifiBluetoothScanner.setShowEnableLocationNotification(context.getApplicationContext(), true, WifiBluetoothScanner.SCANNER_TYPE_BLUETOOTH);
            //MobileCellsScanner.setShowEnableLocationNotification(context.getApplicationContext(), true);
            //ActivateProfileHelper.setScreenUnlocked(context, true);

            if (!shutdown) {
                //ActivateProfileHelper.updateGUI(context, false, true);
//                PPApplication.logE("[PPP_NOTIFICATION] PPApplication._exitApp", "call of forceUpdateGUI");
                PPApplication.forceUpdateGUI(context.getApplicationContext(), false, false/*, true*/);

                Handler _handler = new Handler(context.getMainLooper());
                Runnable r = () -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPApplication._exitApp");
                    try {
                        if (activity != null)
                            activity.finish();
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                };
                _handler.post(r);
                /*if (killProcess) {
                    Handler _handler = new Handler(context.getMainLooper());
                    Runnable r = new Runnable() {
                        public void run() {
                            android.os.Process.killProcess(PPApplication.pid);
                        }
                    };
                    _handler.postDelayed(r, 1000);
                }*/
            }

            //workManagerInstance.pruneWork();

            PPApplication.logE("PPApplication._exitApp", "set application started = false");
            PPApplication.setApplicationStarted(context, false);

        } catch (Exception e) {
            //Log.e("PPApplication._exitApp", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void exitApp(final boolean useHandler, final Context context, final DataWrapper dataWrapper, final Activity activity,
                                 final boolean shutdown, boolean removeNotifications) {
        try {
            if (useHandler) {
                //PPApplication.startHandlerThread(/*"PPApplication.exitApp"*/);
                //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
                //__handler.post(new ExitAppRunnable(context.getApplicationContext(), dataWrapper, activity) {
                //__handler.post(() -> {
                Runnable runnable = () -> {
//                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPApplication.exitApp");

                    //Context appContext= appContextWeakRef.get();
                    //DataWrapper dataWrapper = dataWrapperWeakRef.get();
                    //Activity activity = activityWeakRef.get();

                    //if ((appContext != null) && (dataWrapper != null) && (activity != null)) {
                        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PPApplication_exitApp");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                            _exitApp(context, dataWrapper, activity, shutdown, removeNotifications);

                        } catch (Exception e) {
//                            Log.e("[IN_EXECUTOR] PPApplication.exitApp", Log.getStackTraceString(e));
                            PPApplication.recordException(e);
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    //}
                }; //);
                PPApplication.createBasicExecutorPool();
                PPApplication.basicExecutorPool.submit(runnable);
            }
            else
                _exitApp(context, dataWrapper, activity, shutdown, removeNotifications);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    static void showDoNotKillMyAppDialog(final Activity activity) {
/*
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    return ((JSONObject) new JSONTokener(
                            InputStreamUtil.read(new URL("https://dontkillmyapp.com/api/v2/"+Build.MANUFACTURER.toLowerCase().replaceAll(" ", "-")+".json").openStream())).nextValue()
                    ).getString("user_solution").replaceAll("\\[[Yy]our app\\]", fragment.getString(R.string.app_name));
                } catch (Exception e) {
                    // This vendor is not in the DontKillMyApp list
                    Log.e("PPApplication.showDoNotKillMyAppDialog", Log.getStackTraceString(e));
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    if (result != null) {
                        String head = "<head><style>img{max-width: 100%; width:auto; height: auto;}</style></head>";
                        String html = "<html>" + head + "<body>" + result + "</body></html>";

                        WebView wv = new WebView(fragment.getContext());
                        WebSettings settings = wv.getSettings();
                        WebSettings.LayoutAlgorithm layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING;
                        settings.setLayoutAlgorithm(layoutAlgorithm);
                        wv.loadData(html, "text/html; charset=utf-8", "UTF-8");
                        wv.setWebViewClient(new WebViewClient() {
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                view.loadUrl(url);
                                return true;
                            }
                        });

                        new AlertDialog.Builder(fragment.getContext())
                                .setTitle("How to make my app work")
                                .setView(wv).setPositiveButton(android.R.string.ok, null).show();

                    }
                    else {
                        String url = "https://dontkillmyapp.com/";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        try {
                            fragment.startActivity(Intent.createChooser(i, fragment.getString(R.string.web_browser_chooser)));
                        } catch (Exception ignored) {}
                    }
                } catch (Exception e) {
                    Log.e("PPApplication.showDoNotKillMyAppDialog", Log.getStackTraceString(e));
                    String url = "https://dontkillmyapp.com/";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        fragment.startActivity(Intent.createChooser(i, fragment.getString(R.string.web_browser_chooser)));
                    } catch (Exception ignored) {}
                }
            }
        }.execute();
*/

        if (activity != null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.phone_profiles_pref_applicationDoNotKillMyApp_dialogTitle);
            dialogBuilder.setPositiveButton(android.R.string.ok, null);

            LayoutInflater inflater = activity.getLayoutInflater();
            @SuppressLint("InflateParams")
            View layout = inflater.inflate(R.layout.dialog_do_not_kill_my_app, null);
            dialogBuilder.setView(layout);

            DokiContentView doki = layout.findViewById(R.id.do_not_kill_my_app_dialog_dokiContentView);
            if (doki != null) {
                doki.setButtonsVisibility(false);
                doki.loadContent(Build.MANUFACTURER.toLowerCase().replace(" ", "-"));
            }

            AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

            if (!activity.isFinishing())
                dialog.show();
        }

    }

    static void createBasicExecutorPool() {
        if (basicExecutorPool == null)
            basicExecutorPool = Executors.newCachedThreadPool();
    }
    static void createProfileActiationExecutorPool() {
        if (profileActiationExecutorPool == null)
            profileActiationExecutorPool = Executors.newCachedThreadPool();
    }
    static void createEventsHandlerExecutor() {
        if (eventsHandlerExecutor == null)
            eventsHandlerExecutor = Executors.newCachedThreadPool();
    }
    static void createScannersExecutor() {
        if (scannersExecutor == null)
            scannersExecutor = Executors.newCachedThreadPool();
    }
    static void createPlayToneExecutor() {
        if (playToneExecutor == null)
            playToneExecutor = Executors.newSingleThreadExecutor();
    }
    static void createNonBlockedExecutor() {
        if (disableInternalChangeExecutor == null)
            disableInternalChangeExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    static void createDelayedGuiExecutor() {
        if (delayedGuiExecutor == null)
            delayedGuiExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    static void createDelayedShowNotificationExecutor() {
        if (delayedAppNotificationExecutor == null)
            delayedAppNotificationExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    static void createDelayedEventsHandlerExecutor() {
        if (delayedEventsHandlerExecutor == null)
            delayedEventsHandlerExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    static void createDelayedProfileActivationExecutor() {
        if (delayedProfileActivationExecutor == null)
            delayedProfileActivationExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    /*
    static void startHandlerThread() {
        if (handlerThread == null) {
            handlerThread = new HandlerThread("PPHandlerThread", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThread.start();
        }
    }
    */

    /*
    static void startHandlerThreadCancelWork() {
        if (handlerThreadCancelWork == null) {
            handlerThreadCancelWork = new HandlerThread("PPHandlerThreadCancelWork", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadCancelWork.start();
        }
    }
    */

    static void startHandlerThreadBroadcast(/*String from*/) {
        if (handlerThreadBroadcast == null) {
            handlerThreadBroadcast = new HandlerThread("PPHandlerThreadBroadcast", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadBroadcast.start();
        }
    }

    /*
    static void startHandlerThreadPPScanners() {
        if (handlerThreadPPScanners == null) {
            handlerThreadPPScanners = new HandlerThread("PPHandlerThreadPPScanners", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadPPScanners.start();
        }
    }
    */

    static void startHandlerThreadOrientationScanner() {
        if (handlerThreadOrientationScanner == null) {
            handlerThreadOrientationScanner = new OrientationScannerHandlerThread("PPHandlerThreadOrientationScanner", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadOrientationScanner.start();
            if (PPApplication.proximitySensor != null)
                PPApplication.handlerThreadOrientationScanner.maxProximityDistance = PPApplication.proximitySensor.getMaximumRange();
            if (PPApplication.lightSensor != null)
                PPApplication.handlerThreadOrientationScanner.maxLightDistance = PPApplication.lightSensor.getMaximumRange();
        }
    }

    /*
    static void startHandlerThreadPPCommand() {
        if (handlerThreadPPCommand == null) {
            handlerThreadPPCommand = new HandlerThread("PPHandlerThreadPPCommand", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadPPCommand.start();
        }
    }
    */

    static void startHandlerThreadLocation() {
        if (handlerThreadLocation == null) {
            handlerThreadLocation = new HandlerThread("PPHandlerThreadLocation", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadLocation.start();
        }
    }

    /*
    static void startHandlerThreadWidget() {
        if (handlerThreadWidget == null) {
            handlerThreadWidget = new HandlerThread("PPHandlerThreadWidget", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadWidget.start();
        }
    }
    */

    /*
    static void startHandlerThreadPlayTone() {
        if (handlerThreadPlayTone == null) {
            handlerThreadPlayTone = new HandlerThread("PPHandlerThreadPlayTone", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadPlayTone.start();
        }
    }
    */

    /*
    static void startHandlerThreadVolumes() {
        if (handlerThreadVolumes == null) {
            handlerThreadVolumes = new HandlerThread("handlerThreadVolumes", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadVolumes.start();
        }
    }
    */

    /*
    static void startHandlerThreadRadios() {
        if (handlerThreadRadios == null) {
            handlerThreadRadios = new HandlerThread("handlerThreadRadios", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadRadios.start();
        }
    }
    */

    /*
    static void startHandlerThreadWallpaper() {
        if (handlerThreadWallpaper == null) {
            handlerThreadWallpaper = new HandlerThread("handlerThreadWallpaper", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadWallpaper.start();
        }
    }
    */

    /*
    static void startHandlerThreadRunApplication() {
        if (handlerThreadRunApplication == null) {
            handlerThreadRunApplication = new HandlerThread("handlerThreadRunApplication", THREAD_PRIORITY_MORE_FAVORABLE); //);
            handlerThreadRunApplication.start();
        }
    }
    */

    /*
    static void startHandlerThreadProfileActivation() {
        if (handlerThreadProfileActivation == null) {
            handlerThreadProfileActivation = new HandlerThread("handlerThreadProfileActivation", THREAD_PRIORITY_MORE_FAVORABLE); //);;
            handlerThreadProfileActivation.start();
        }
    }
    */

    static void setBlockProfileEventActions(boolean enable) {
        // if blockProfileEventActions = true, do not perform any actions, for example ActivateProfileHelper.lockDevice()
        PPApplication.blockProfileEventActions = enable;
        if (enable) {
            PPExecutors.scheduleDisableBlockProfileEventActionExecutor();
        }
        else {
            PPApplication.cancelWork(DisableBlockProfileEventActionWorker.WORK_TAG, false);
        }
    }

/*    static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;

        PPHandlerThreadRunnable(Context appContext) {
            this.appContextWeakRef = new WeakReference<>(appContext);
        }

    }*/

/*    private static abstract class ExitAppRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<DataWrapper> dataWrapperWeakRef;
        final WeakReference<Activity> activityWeakRef;

        ExitAppRunnable(Context appContext, DataWrapper dataWrapper, Activity activity) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.dataWrapperWeakRef = new WeakReference<>(dataWrapper);
            this.activityWeakRef = new WeakReference<>(activity);
        }

    }*/

    //--------------------

/*    //-----------------------------

    private static WeakReference<Activity> foregroundEditorActivity;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof EditorActivity)
            foregroundEditorActivity=new WeakReference<>(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof EditorActivity)
            foregroundEditorActivity=new WeakReference<>(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof EditorActivity)
            foregroundEditorActivity = null;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    static Activity getEditorActivity() {
        if (foregroundEditorActivity != null && foregroundEditorActivity.get() != null) {
            return foregroundEditorActivity.get();
        }
        return null;
    }
*/

    // Sensor manager ------------------------------------------------------------------------------

    static Sensor getAccelerometerSensor(Context context) {
        if (sensorManager == null)
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else
            return null;
    }

    static Sensor getMagneticFieldSensor(Context context) {
        if (sensorManager == null)
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        else
            return null;
    }

    static Sensor getProximitySensor(Context context) {
        if (sensorManager == null)
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        else
            return null;
    }

    /*
    private Sensor getOrientationSensor(Context context) {
        synchronized (PPApplication.orientationScannerMutex) {
            if (mOrientationSensorManager == null)
                mOrientationSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            return mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
    }*/

    static Sensor getLightSensor(Context context) {
        if (sensorManager == null)
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //Sensor sensor = mOrientationSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            //if (sensor != null) {
            //    if (sensor.getPower() > 0)
            //        return sensor;
            //    else
            //        return null;
            //}
            //return null;
            return sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        else
            return null;
    }

    // application cache -----------------

    static void createApplicationsCache(boolean clear)
    {
        if (clear) {
            if (applicationsCache != null) {
                applicationsCache.clearCache(true);
            }
            applicationsCache = null;
        }
        if (applicationsCache == null)
            applicationsCache =  new ApplicationsCache();
    }

    static ApplicationsCache getApplicationsCache()
    {
        return applicationsCache;
    }

    // contacts and contact groups cache -----------------

    static void createContactsCache(Context context, boolean clear)
    {
        if (clear) {
            if (contactsCache != null)
                contactsCache.clearCache();
        }
        if (contactsCache == null)
            contactsCache = new ContactsCache();
        contactsCache.getContactList(context);
    }

    static ContactsCache getContactsCache()
    {
        return contactsCache;
    }

    static void createContactGroupsCache(Context context, boolean clear)
    {
        if (clear) {
            if (contactGroupsCache != null)
                contactGroupsCache.clearCache();
        }
        if (contactGroupsCache == null)
            contactGroupsCache = new ContactGroupsCache();
        contactGroupsCache.getContactGroupListX(context);
    }

    static ContactGroupsCache getContactGroupsCache()
    {
        return contactGroupsCache;
    }

    // check if Pixel Launcher is default --------------------------------------------------

    static boolean isPixelLauncherDefault(Context context) {
        if (Build.VERSION.SDK_INT >= 31) {
            if (context != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    ResolveInfo defaultLauncher;
                    //if (Build.VERSION.SDK_INT < 33)
                    //noinspection deprecation
                    defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    //else
                    //    defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY));
                    return defaultLauncher.activityInfo.packageName.toLowerCase().contains(
                            "com.google.android.apps.nexuslauncher");
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        else
            return false;
    }

    // check if One UI 4 Samsung Launcher is default --------------------------------------------------

    static boolean isOneUILauncherDefault(Context context) {
        if (Build.VERSION.SDK_INT >= 31) {
            if (context != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);

                    //ResolveInfo defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

                    ResolveInfo defaultLauncher;
                    //if (Build.VERSION.SDK_INT < 33)
                    //noinspection deprecation
                    defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    //else
                    //    defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY));

                    return defaultLauncher.activityInfo.packageName.toLowerCase().contains(
                            "com.sec.android.app.launcher");
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        else
            return false;
    }

    // check if One UI 4 Samsung Launcher is default --------------------------------------------------

    static boolean isMIUILauncherDefault(Context context) {
        if (Build.VERSION.SDK_INT >= 31) {
            if (context != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);

                    //ResolveInfo defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

                    ResolveInfo defaultLauncher;
                    //if (Build.VERSION.SDK_INT < 33)
                    //noinspection deprecation
                    defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    //else
                    //    defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY));

                    //Log.e("PPApplication.isMIUILauncherDefault", "defaultLauncher="+defaultLauncher);
                    return defaultLauncher.activityInfo.packageName.toLowerCase().contains(
                            "com.miui.home");
                } catch (Exception e) {
                    return false;
                }
            }
            else
                return false;
        }
        else
            return false;
    }

    // get PPP version from relases.md ----------------------------------------------

    static class PPPReleaseData {
        String versionNameInReleases = "";
        int versionCodeInReleases = 0;
        boolean critical = true;
    }

    static PPPReleaseData getReleaseData(String contents, boolean forceDoData, Context appContext) {
        // this must be added when you tests debug branch
//        if (DebugVersion.enabled)
//            contents = "@@@ppp-release:5.1.1.1b:6651:normal***@@@";
//

        boolean doData = false;
        try {
            PPPReleaseData pppReleaseData = new PPPReleaseData();

            if (!contents.isEmpty()) {
                int startIndex = contents.indexOf("@@@ppp-release:");
                int endIndex = contents.indexOf("***@@@");
                if ((startIndex >= 0) && (endIndex > startIndex)) {
                    String version = contents.substring(startIndex, endIndex);
                    startIndex = version.indexOf(":");
                    if (startIndex != -1) {
                        version = version.substring(startIndex + 1);
                        String[] splits = version.split(":");
                        if (splits.length >= 2) {
                            int versionCode = 0;
                            try {
                                PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(PPApplication.PACKAGE_NAME, 0);
                                versionCode = PPApplication.getVersionCode(pInfo);
                            } catch (Exception ignored) {
                            }
                            pppReleaseData.versionNameInReleases = splits[0];
                            pppReleaseData.versionCodeInReleases = Integer.parseInt(splits[1]);
                            if (forceDoData)
                                doData = true;
                            else {
                                if (ApplicationPreferences.prefShowCriticalGitHubReleasesCodeNotification < pppReleaseData.versionCodeInReleases) {
                                    if ((versionCode > 0) && (versionCode < pppReleaseData.versionCodeInReleases))
                                        doData = true;
                                }
                            }
                        }
                        /*if (splits.length == 2) {
                            // old check, always critical update
                            //critical = true;
                        }*/
                        if (splits.length == 3) {
                            // new, better check
                            // last parameter:
                            //  "normal" - normal update
                            //  "critical" - critical update
                            pppReleaseData.critical = splits[2].equals("critical");
                        }
                    }
                }
            }

            if (doData)
                return pppReleaseData;
            else
                return null;
        } catch (Exception e) {
//            Log.e("PPApplication.getReleaseData", Log.getStackTraceString(e));
            return null;
        }
    }

    // ACRA -------------------------------------------------------------------------

    static void recordException(Throwable ex) {
        try {
            //FirebaseCrashlytics.getInstance().recordException(ex);
            ACRA.getErrorReporter().handleException(ex);
            //ACRA.getErrorReporter().putCustomData("NON-FATAL_EXCEPTION", Log.getStackTraceString(ex));
        } catch (Exception ignored) {}
    }

    static void logToACRA(String s) {
        try {
            //FirebaseCrashlytics.getInstance().log(s);
            ACRA.getErrorReporter().putCustomData("Log", s);
        } catch (Exception ignored) {}
    }

    static void setCustomKey(String key, int value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, String.valueOf(value));
        } catch (Exception ignored) {}
    }

    static void setCustomKey(String key, String value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, value);
        } catch (Exception ignored) {}
    }

    static void setCustomKey(String key, boolean value) {
        try {
            //FirebaseCrashlytics.getInstance().setCustomKey(key, value);
            ACRA.getErrorReporter().putCustomData(key, String.valueOf(value));
        } catch (Exception ignored) {}
    }

    /*
    static void logAnalyticsEvent(Context context, String itemId, String itemName, String contentType) {
        try {
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        } catch (Exception e) {
            //recordException(e);
        }
    }
    */

    //---------------------------------------------------------------------------------------------

}
