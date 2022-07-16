package sk.henrichg.phoneprofilesplus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PeriodicEventsHandlerWorker extends Worker {

    final Context context;

    static final String WORK_TAG  = "periodicEventsHandlerWorker";
    static final String WORK_TAG_SHORT  = "periodicEventsHandlerWorkerShort";

    public PeriodicEventsHandlerWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
//            long start = System.currentTimeMillis();
//            PPApplication.logE("[IN_WORKER]  PeriodicEventsHandlerWorker.doWork", "--------------- START");

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return Result.success();

            if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {

                //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
                boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                if (isPowerSaveMode) {
                    if (ApplicationPreferences.applicationEventPeriodicScanningScanInPowerSaveMode.equals("2")) {
                        PPApplication.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false);
                        PPApplication.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false);
//                        if (PPApplication.logEnabled()) {
//                            PPApplication.logE("[IN_WORKER] PeriodicEventsHandlerWorker.doWork", "return - update in power save mode is not allowed");
//                            PPApplication.logE("[IN_WORKER] PeriodicEventsHandlerWorker.doWork", "---------------------------------------- END");
//                        }
                        return Result.success();
                    }
                }
                else {
                    if (ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiply.equals("2")) {
                        if (PhoneProfilesService.isNowTimeBetweenTimes(
                                ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyFrom,
                                ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyTo)) {
                            // not scan in configured time
//                            PPApplication.logE("PeriodicEventsHandlerWorker.doWork", "-- END - scan in time = 2 -------");
                            PPApplication.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG, false);
                            PPApplication.cancelWork(PeriodicEventsHandlerWorker.WORK_TAG_SHORT, false);
//                            if (PPApplication.logEnabled()) {
//                                PPApplication.logE("[IN_WORKER] PeriodicEventsHandlerWorker.doWork", "return - update in configured time is not allowed");
//                                PPApplication.logE("[IN_WORKER] PeriodicEventsHandlerWorker.doWork", "---------------------------------------- END");
//                            }
                            return Result.success();
                        }
                    }
                }

                if (Event.getGlobalEventsRunning()) {

                    boolean callEventsHandler = false;
                    Set<String> tags = getTags();
                    for (String tag : tags) {
//                        PPApplication.logE("######### PeriodicEventsHandlerWorker.doWork", "tag="+tag);

                        if (tag.equals(WORK_TAG)) {
                            callEventsHandler = true;
                            break;
                        }
                    }

//                    PPApplication.logE("######### PeriodicEventsHandlerWorker.doWork", "callEventsHandler="+callEventsHandler);

                    if (callEventsHandler) {
                        //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=PeriodicEventsHandlerWorker.doWork");

//                        PPApplication.logE("[EVENTS_HANDLER_CALL] PeriodicEventsHandlerWorker.doWork", "sensorType=SENSOR_TYPE_PERIODIC_EVENTS_HANDLER");
                        EventsHandler eventsHandler = new EventsHandler(getApplicationContext());
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_PERIODIC_EVENTS_HANDLER);

                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=PeriodicEventsHandlerWorker.doWork");
                    }
                }

                PPApplication.logE("[EXECUTOR_CALL]  ***** PeriodicEventsHandlerWorker.doWork", "schedule - SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG");
                final Context appContext = context;
                final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
                Runnable runnable = () -> {
                    long start1 = System.currentTimeMillis();
                    PPApplication.logE("[IN_EXECUTOR]  ***** PeriodicEventsHandlerWorker.doWork", "--------------- START - SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG");
                    PeriodicEventsHandlerWorker.enqueueWork(appContext);
                    long finish = System.currentTimeMillis();
                    long timeElapsed = finish - start1;
                    PPApplication.logE("[IN_EXECUTOR]  ***** PeriodicEventsHandlerWorker.doWork", "--------------- END - SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG - timeElapsed="+timeElapsed);
                    worker.shutdown();
                };
                worker.schedule(runnable, 5, TimeUnit.SECONDS);
                /*
                //enqueueWork();
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG)
                                .setInitialDelay(5000, TimeUnit.MILLISECONDS)
                                .build();
                try {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                                PPApplication.logE("[TEST BATTERY] PeriodicEventsHandlerWorker.doWork", "for=" + MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                        PPApplication.logE("[WORKER_CALL] PeriodicEventsHandlerWorker.doWork", "xxx");
                        workManager.enqueueUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_PERIODIC_EVENTS_HANDLER_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
                */
                /*
                PPApplication.startHandlerThreadPPScanners();
                final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PeriodicEventsHandlerWorker.enqueueWork(context);
                    }
                }, 1500);
                */
            }

//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//            PPApplication.logE("[IN_WORKER]  PeriodicEventsHandlerWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            //Log.e("PeriodicEventsHandlerWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            /*Handler _handler = new Handler(getApplicationContext().getMainLooper());
            Runnable r = new Runnable() {
                public void run() {
                    android.os.Process.killProcess(PPApplication.pid);
                }
            };
            _handler.postDelayed(r, 1000);*/
            return Result.failure();
        }
    }

    static void enqueueWork(Context appContext) {
        int interval = ApplicationPreferences.applicationEventPeriodicScanningScanInterval;
        //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
        boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(appContext);
        if (isPowerSaveMode) {
            if (ApplicationPreferences.applicationEventPeriodicScanningScanInPowerSaveMode.equals("1"))
                interval = 2 * interval;
        }
        else {
            if (ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiply.equals("1")) {
                if (PhoneProfilesService.isNowTimeBetweenTimes(
                        ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyFrom,
                        ApplicationPreferences.applicationEventPeriodicScanningScanInTimeMultiplyTo)) {
                    interval = 2 * interval;
//                    PPApplication.logE("PeriodicEventsHandlerWorker.enqueueWork", "scan in time - 2x interval");
                }
            }
        }

        /*int keepResultsDelay = (interval * 5);
        if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
            keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/
        OneTimeWorkRequest periodicEventsHandlerWorker =
                new OneTimeWorkRequest.Builder(PeriodicEventsHandlerWorker.class)
                        .addTag(PeriodicEventsHandlerWorker.WORK_TAG)
                        .setInitialDelay(interval, TimeUnit.MINUTES)
                        .build();
        try {
            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {

//                                //if (PPApplication.logEnabled()) {
//                                ListenableFuture<List<WorkInfo>> statuses;
//                                statuses = workManager.getWorkInfosForUniqueWork(PeriodicEventsHandlerWorker.WORK_TAG);
//                                try {
//                                    List<WorkInfo> workInfoList = statuses.get();
//                                    PPApplication.logE("[TEST BATTERY] PeriodicEventsHandlerWorker.enqueueWork", "for=" + PeriodicEventsHandlerWorker.WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                                } catch (Exception ignored) {
//                                }
//                                //}

//                PPApplication.logE("[WORKER_CALL] PeriodicEventsHandlerWorker.enqueueWork", "xxx");
                workManager.enqueueUniqueWork(PeriodicEventsHandlerWorker.WORK_TAG, ExistingWorkPolicy.REPLACE/*KEEP*/, periodicEventsHandlerWorker);
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

}
