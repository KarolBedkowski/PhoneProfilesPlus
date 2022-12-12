package sk.henrichg.phoneprofilesplus;

import static android.app.Notification.DEFAULT_VIBRATE;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;

public class CheckPPPReleasesBroadcastReceiver extends BroadcastReceiver {

    private static final String PREF_PPP_RELEASE_ALARM = "github_release_alarm";

    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] CheckGitHubReleasesBroadcastReceiver.onReceive", "xxx");
//        PPApplication.logE("[IN_BROADCAST_ALARM] CheckGitHubReleasesBroadcastReceiver.onReceive", "xxx");

        if (intent != null) {
            doWork(/*true,*/ context);
        }
    }

    static void setAlarm(Context context)
    {
        removeAlarm(context);

        Calendar alarm = Calendar.getInstance();

        long lastAlarm = ApplicationPreferences.
                getSharedPreferences(context).getLong(PREF_PPP_RELEASE_ALARM, 0);

        long alarmTime;

        // TODO remove for release
        /*if (DebugVersion.enabled) {
            alarm.add(Calendar.MINUTE, 1);

            alarmTime = alarm.getTimeInMillis();
        } else*/
        {
            if ((lastAlarm == 0) || (lastAlarm <= alarm.getTimeInMillis())) {
                // saved alarm is less then actual time

                // each month at 13:00
                alarm.set(Calendar.HOUR_OF_DAY, 13);
                alarm.set(Calendar.MINUTE, 0);
                alarm.add(Calendar.DAY_OF_MONTH, 30);
                alarm.set(Calendar.SECOND, 0);
                alarm.set(Calendar.MILLISECOND, 0);

                alarmTime = alarm.getTimeInMillis();

                setShowPPPReleasesNotification(context, alarmTime);
            }
            else {
                alarmTime = lastAlarm;
            }
        }

        //Intent intent = new Intent(_context, CheckGitHubReleasesBroadcastReceiver.class);
        Intent intent = new Intent();
        intent.setAction(PPApplication.ACTION_CHECK_GITHUB_RELEASES);
        //intent.setClass(context, CheckGitHubReleasesBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (ApplicationPreferences.applicationUseAlarmClock) {
                Intent editorIntent = new Intent(context, EditorActivity.class);
                editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                alarmManager.setAlarmClock(clockInfo, pendingIntent);
            }
            else {
                //if (android.os.Build.VERSION.SDK_INT >= 23)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                //    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                //else
                //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            }
        }
    }

    static private void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(_context, CheckGitHubReleasesBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PPApplication.ACTION_CHECK_GITHUB_RELEASES);
                //intent.setClass(context, CheckGitHubReleasesBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_DONATION_TAG_WORK);
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        if (!PPApplication.getApplicationStarted(true, true))
            // application is not started
            return;

        //if (useHandler) {
            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadBroadcast(/*"DonationBroadcastReceiver.onReceive"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        context.getApplicationContext()) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=CheckGitHubReleasesBroadcastReceiver.doWork");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":CheckGitHubReleasesBroadcastReceiver_doWork");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        try {
                            boolean getVersion;
                            if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy)
                                getVersion = false;
                            else
                            if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)
                                getVersion = false;
                            else {
                                PackageManager packageManager = appContext.getPackageManager();

                                //Intent intent = packageManager.getLaunchIntentForPackage("com.amazon.venezia");
                                //boolean amazonAppStoreInstalled = (intent != null);

                                Intent intent = packageManager.getLaunchIntentForPackage("com.huawei.appmarket");
                                boolean huaweiAppGalleryInstalled = (intent != null);

                                intent = packageManager.getLaunchIntentForPackage("org.fdroid.fdroid");
                                boolean fdroidInstalled = (intent != null);

                                intent = packageManager.getLaunchIntentForPackage("com.looker.droidify");
                                boolean droidifyInstalled = (intent != null);

                                getVersion = !(huaweiAppGalleryInstalled || fdroidInstalled || droidifyInstalled);
                            }
                            if (getVersion)
                                _doWorkGitHub(appContext);
                            else
                                _doWorkOthers(appContext);

                        } catch (Exception ignored) {
                        }

                        setAlarm(appContext);

                    } catch (Exception e) {
//                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        /*}
        else {
            _doWork(appContext);
            setAlarm(appContext);
        }*/
    }

    private static void showNotification(Context appContext,
                                         String versionNameInReleases,
                                         int versionCodeInReleases,
                                         boolean critical) {

        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                String tag = notification.getTag();
                if ((tag != null) && tag.contains(PPApplication.CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_TAG)) {
                    if (notification.getId() == PPApplication.CHECK_CRITICAL_GITHUB_RELEASES_NOTIFICATION_ID) {
                        // do not show notification when is displayed critical notification
                        return;
                    }
                }
            }
        }

        // show notification for check new release
        PPApplication.createNewReleaseNotificationChannel(appContext);

        NotificationCompat.Builder mBuilder;
        Intent _intent;
        _intent = new Intent(appContext, CheckPPPReleasesActivity.class);
        _intent.putExtra(CheckPPPReleasesActivity.EXTRA_CRITICAL_CHECK, false);
        _intent.putExtra(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_NAME, versionNameInReleases);
        _intent.putExtra(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_CODE, versionCodeInReleases);
        _intent.putExtra(CheckPPPReleasesActivity.EXTRA_NEW_VERSION_CRITICAL, critical);

        String nTitle = appContext.getString(R.string.menu_check_github_releases);
        String nText = appContext.getString(R.string.check_ppp_releases_notification);
        mBuilder = new NotificationCompat.Builder(appContext, PPApplication.NEW_RELEASE_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(appContext, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_information_notify) // notification icon
                .setContentTitle(nTitle) // title for notification
                .setContentText(nText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                .setAutoCancel(true); // clear notification after click

        PendingIntent pi = PendingIntent.getActivity(appContext, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //if (android.os.Build.VERSION.SDK_INT >= 21) {
        mBuilder.setCategory(NotificationCompat.CATEGORY_EVENT);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}

        mBuilder.setGroup(PPApplication.CHECK_RELEASES_GROUP);

        Notification notification = mBuilder.build();
        if (Build.VERSION.SDK_INT < 26) {
            notification.vibrate = null;
            notification.defaults &= ~DEFAULT_VIBRATE;
        }

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(appContext);
        try {
            mNotificationManager.notify(
                    PPApplication.CHECK_GITHUB_RELEASES_NOTIFICATION_TAG,
                    PPApplication.CHECK_GITHUB_RELEASES_NOTIFICATION_ID, notification);
        } catch (Exception e) {
            //Log.e("CheckPPPReleasesBroadcastReceiver._doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    private static void  _doWorkOthers(Context appContext) {
        showNotification(appContext, "", 0, false);
    }

    private static void _doWorkGitHub(Context appContext) {
        try {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(appContext);
            String url;
            if (DebugVersion.enabled)
                url = PPApplication.PPP_RELEASES_DEBUG_URL;
            else
                url = PPApplication.PPP_RELEASES_URL;
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET,
                    url,
                    response -> {
                        boolean showNotification;
                        boolean critical = true;
                        String versionNameInReleases = "";
                        int versionCodeInReleases = 0;

                        //noinspection UnnecessaryLocalVariable
                        String contents = response;

                        PPApplication.PPPReleaseData pppReleaseData =
                                PPApplication.getReleaseData(contents, true, appContext);

                        showNotification = pppReleaseData != null;
                        if (showNotification) {
                            critical = pppReleaseData.critical;
                            versionNameInReleases = pppReleaseData.versionNameInReleases;
                            versionCodeInReleases = pppReleaseData.versionCodeInReleases;
                        }

                        try {
                            if (showNotification) {
                                showNotification(appContext,
                                        versionNameInReleases,
                                        versionCodeInReleases,
                                        critical);
                            }

                        } catch (Exception e) {
//                            Log.e("CheckPPPReleasesBroadcastReceiver._doWork", Log.getStackTraceString(e));
                        }

                    },
                    error -> {
//                        Log.e("CheckPPPReleasesBroadcastReceiver._doWork", Log.getStackTraceString(error));
                    });
            queue.add(stringRequest);

        } catch (Exception e) {
//            Log.e("CheckPPPReleasesBroadcastReceiver._doWork", Log.getStackTraceString(e));
        }
    }

    static void setShowPPPReleasesNotification(Context context, long alarmTime)
    {
        SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
        editor.putLong(PREF_PPP_RELEASE_ALARM, alarmTime);
        editor.apply();
    }

}
