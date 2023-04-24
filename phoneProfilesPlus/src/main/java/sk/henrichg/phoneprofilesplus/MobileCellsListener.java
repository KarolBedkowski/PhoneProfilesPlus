package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class MobileCellsListener extends PhoneStateListener {

    private final Context context;
    private final TelephonyManager telephonyManager;
    //private final SubscriptionInfo subscriptionInfo;

    //private int simSlot = 0;
    int registeredCell = Integer.MAX_VALUE;
    long lastConnectedTime = 0;

    final MobileCellsScanner scanner;

    MobileCellsListener(@SuppressWarnings("unused") SubscriptionInfo subscriptionInfo,
                        Context context,
                        MobileCellsScanner scanner, TelephonyManager telephonyManager) {

//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsListener - constructor", "******** ### *******");

        //this.subscriptionInfo = subscriptionInfo;
//        if (subscriptionInfo != null)
//            simSlot = subscriptionInfo.getSimSlotIndex()+1;
        this.context = context;
        this.scanner = scanner;
        this.telephonyManager = telephonyManager;
    }

    private void getAllCellInfo(List<CellInfo> cellInfo) {
        // only for registered cells is returned identify
        // SlimKat in Galaxy Nexus - returns null :-/
        // Honor 7 - returns empty list (not null), Dual SIM?

//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsListener.getAllCellInfo", "******** ### ******* (2)");

        if (cellInfo!=null) {

            if (Permissions.checkLocation(context.getApplicationContext())) {

                boolean anyRegistered = false;

                for (CellInfo _cellInfo : cellInfo) {

                    boolean isRegistered = false;

                    if (_cellInfo instanceof CellInfoGsm) {
                        CellIdentityGsm identityGsm = ((CellInfoGsm) _cellInfo).getCellIdentity();
                        if (MobileCellsScanner.isValidCellId(identityGsm.getCid())) {
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityGsm.getCid();
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                isRegistered = true;
                            }
                        }
                    } else if (_cellInfo instanceof CellInfoLte) {
                        CellIdentityLte identityLte = ((CellInfoLte) _cellInfo).getCellIdentity();
                        if (MobileCellsScanner.isValidCellId(identityLte.getCi())) {
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityLte.getCi();
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                isRegistered = true;
                            }
                        }
                    } else if (_cellInfo instanceof CellInfoWcdma) {
                        CellIdentityWcdma identityWcdma = ((CellInfoWcdma) _cellInfo).getCellIdentity();
                        if (MobileCellsScanner.isValidCellId(identityWcdma.getCid())) {
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityWcdma.getCid();
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                isRegistered = true;
                            }
                        }
                    } else if (_cellInfo instanceof CellInfoCdma) {
                        CellIdentityCdma identityCdma = ((CellInfoCdma) _cellInfo).getCellIdentity();
                        if (MobileCellsScanner.isValidCellId(identityCdma.getBasestationId())) {
                            if (_cellInfo.isRegistered()) {
                                registeredCell = identityCdma.getBasestationId();
                                lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                                isRegistered = true;
                            }
                        }
                    }

                    if (isRegistered) {
                        anyRegistered = true;
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
                        doAutoRegistration(registeredCell);
                    }
                }

                if (!anyRegistered) {
                    registeredCell = Integer.MAX_VALUE;
                    doAutoRegistration(registeredCell);
                }

            }

        }
    }

    @SuppressLint("MissingPermission")
    private List<CellInfo> getAllCellInfo() {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsListener.getAllCellInfo", "******** ### ******* (1)");

        if (telephonyManager != null) {
            List<CellInfo> cellInfo = null;
            if (Permissions.checkLocation(context.getApplicationContext())) {
                cellInfo = telephonyManager.getAllCellInfo();
                if (cellInfo == null)
                    return null;
            }
            if (cellInfo != null)
                getAllCellInfo(cellInfo);
            return cellInfo;
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCellInfoChanged(final List<CellInfo> cellInfo)
    {
        super.onCellInfoChanged(cellInfo);

//        PPApplicationStatic.logE("[IN_LISTENER] MobileCellsListener.onCellInfoChanged", "cellInfo="+cellInfo);
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsListener.onCellInfoChanged", "******** ### *******");

        if (cellInfo == null)
            return;


        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadBroadcast(/*"MobileCellsScanner.onCellInfoChanged"*/);
        //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(context.getApplicationContext(), telephonyManager) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=MobileCellsListener.onCellInfoChanged");

            //Context appContext= appContextWeakRef.get();
            //TelephonyManager telephonyManager = telephonyManagerWeakRef.get();

            //if ((appContext != null) && (telephonyManager != null)) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileCellsListener_onCellInfoChanged");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    //List<CellInfo> _cellInfo = cellInfo;
                    //if (_cellInfo == null)
                    //    _cellInfo = getAllCellInfo();
                    //else
                    //    getAllCellInfo(_cellInfo);
                    getAllCellInfo(cellInfo);

                    //if (_cellInfo != null) {
                    handleEvents(appContext);
                    //}

                } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
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
        PPApplicationStatic.createEventsHandlerExecutor();
        PPApplication.eventsHandlerExecutor.submit(runnable);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onServiceStateChanged (ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);

//        PPApplicationStatic.logE("[IN_LISTENER] MobileCellsListener.onServiceStateChanged", "xxx");
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsListener.onServiceStateChanged", "******** ### *******");

        if (serviceState == null)
            return;

        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadBroadcast(/*"MobileCellsScanner.onServiceStateChanged"*/);
        //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(context.getApplicationContext(), telephonyManager) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=MobileCellsListener.onServiceStateChanged");

            //Context appContext= appContextWeakRef.get();
            //TelephonyManager telephonyManager = telephonyManagerWeakRef.get();

            //if ((appContext != null) && (telephonyManager != null)) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileCellsListener_onServiceStateChanged");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    registerCell();

                    handleEvents(appContext);

                } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
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
        PPApplicationStatic.createEventsHandlerExecutor();
        PPApplication.eventsHandlerExecutor.submit(runnable);
    }

    private void getCellLocation(CellLocation location) {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsListener.getCellLocation", "******** ### ******* (2)");
        if (location != null) {

            if (Permissions.checkLocation(context.getApplicationContext())) {

                boolean isRegistered = false;

                if (location instanceof GsmCellLocation) {
                    GsmCellLocation gcLoc = (GsmCellLocation) location;
                    if (MobileCellsScanner.isValidCellId(gcLoc.getCid())) {
                        registeredCell = gcLoc.getCid();
                        lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
                        doAutoRegistration(registeredCell);
                        isRegistered = true;
                    }
                } else if (location instanceof CdmaCellLocation) {
                    CdmaCellLocation ccLoc = (CdmaCellLocation) location;
                    if (MobileCellsScanner.isValidCellId(ccLoc.getBaseStationId())) {
                        registeredCell = ccLoc.getBaseStationId();
                        lastConnectedTime = Calendar.getInstance().getTimeInMillis();
                        DatabaseHandler db = DatabaseHandler.getInstance(context);
                        db.updateMobileCellLastConnectedTime(registeredCell, lastConnectedTime);
                        doAutoRegistration(registeredCell);
                        isRegistered = true;
                    }
                }

                if (!isRegistered) {
                    registeredCell = Integer.MAX_VALUE;
                    doAutoRegistration(registeredCell);
                }

            }
        }

    }


    @SuppressWarnings("UnusedReturnValue")
    @SuppressLint("MissingPermission")
    private CellLocation getCellLocation() {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsListener.getCellLocation", "******** ### ******* (1)");

        if (telephonyManager != null) {
            CellLocation location = null;
            if (Permissions.checkLocation(context.getApplicationContext())) {
                //noinspection deprecation
                location = telephonyManager.getCellLocation();
                if (location == null)
                    return null;
            }
            if (location != null)
                getCellLocation(location);
            return location;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("MissingPermission")
    @Override
    public void onCellLocationChanged (final CellLocation location) {
        super.onCellLocationChanged(location);

//        PPApplicationStatic.logE("[IN_LISTENER] MobileCellsListener.onCellLocationChanged", "location="+location);
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsListener.onCellLocationChanged", "******** ### *******");

        if (location == null)
            return;

        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadBroadcast(/*"MobileCellsScanner.onCellLocationChanged"*/);
        //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(context.getApplicationContext(), telephonyManager) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=MobileCellsListener.onCellLocationChanged");

            //Context appContext= appContextWeakRef.get();
            //TelephonyManager telephonyManager = telephonyManagerWeakRef.get();

            //if ((appContext != null) && (telephonyManager != null)) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileCellsListener_onCellLocationChanged");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    //CellLocation _location = location;
                    //if (_location == null)
                    //    _location = getCellLocation();
                    //else
                    //    getCellLocation(_location);
                    getCellLocation(location);

                    handleEvents(appContext);
                    //}

                } catch (Exception e) {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
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
        PPApplicationStatic.createEventsHandlerExecutor();
        PPApplication.eventsHandlerExecutor.submit(runnable);
    }

    void registerCell() {
//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsListener.registerCell", "******** ### *******");
        if (getAllCellInfo() == null)
            getCellLocation();
    }

    void rescanMobileCells() {
        //if (ApplicationPreferences.applicationEventMobileCellEnableScanning || MobileCellsScanner.forceStart) {
        if (ApplicationPreferences.applicationEventMobileCellEnableScanning ||
                MobileCellsPreference.forceStart || MobileCellsRegistrationService.forceStart) {

//            PPApplicationStatic.logE("[TEST BATTERY] MobileCellsListener.rescanMobileCells", "******** ### *******");

            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadBroadcast(/*"MobileCellsScanner.rescanMobileCells"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(context.getApplicationContext(), telephonyManager) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=MobileCellsListener.rescanMobileCells");

                //Context appContext= appContextWeakRef.get();
                //TelephonyManager telephonyManager = telephonyManagerWeakRef.get();

                //if ((appContext != null) && (telephonyManager != null)) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileCellsListener_rescanMobileCells");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        registerCell();

                        handleEvents(appContext);

                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
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
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
        }
    }

    void handleEvents(final Context appContext) {
        if (EventStatic.getGlobalEventsRunning(appContext))
        {
//            PPApplicationStatic.logE("[TEST BATTERY] MobileCellsListener.handleEvents", "******** ### *******");

            /*
            //if (DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_MOBILE_CELLS, false) > 0) {
                // start events handler
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_MOBILE_CELLS);
            //}*/

            PPExecutors.handleEvents(appContext, EventsHandler.SENSOR_TYPE_MOBILE_CELLS, "SENSOR_TYPE_MOBILE_CELLS", 5);
            /*
            Data workData = new Data.Builder()
                    .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_MOBILE_CELLS)
                    .build();

            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG)
                            .setInputData(workData)
                            .setInitialDelay(5, TimeUnit.SECONDS)
                            //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                            .build();
            try {
                if (PPApplicationStatic.getApplicationStarted(true)) {
                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                    if (workManager != null) {

//                        //if (PPApplicationStatic.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                        PPApplicationStatic.logE("[WORKER_CALL] MobileCellsListener.handleEvents."+simSlot, "xxx");
                        //workManager.enqueue(worker);
                        workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_MOBILE_CELLS_SCANNER_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                    }
                }
            } catch (Exception e) {
                PPApplicationStatic.recordException(e);
            }
            */
        }

        /*
        // broadcast for cells editor
        Intent intent = new Intent(PPApplication.PACKAGE_NAME + ".PhoneStateChangedBroadcastReceiver_preference");
        //intent.putExtra("state", mode);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        */
    }

    private void doAutoRegistration(final int _registeredCell) {
        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

//        PPApplicationStatic.logE("[TEST BATTERY] MobileCellsListener.doAutoRegistration", "******** ### *******");

        synchronized (PPApplication.mobileCellsScannerMutex) {
            /*PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileCellsScanner_doAutoRegistration");
                    wakeLock.acquire(10 * 60 * 1000);
            }*/

            DatabaseHandler db = DatabaseHandler.getInstance(context);

            boolean notUsedMobileCellsNotificationEnabled = scanner.isNotUsedCellsNotificationEnabled();

            //MobileCellsScanner.lastRunningEventsNotOutside = "";
            MobileCellsScanner.lastPausedEvents = "";
            //List<NotUsedMobileCells> runningEventList = new ArrayList<>();
            //List<NotUsedMobileCells> pausedEventList = new ArrayList<>();
            List<NotUsedMobileCells> mobileCellsEventList = new ArrayList<>();
            if (notUsedMobileCellsNotificationEnabled) {
                // get running events with enabled Mobile cells sensor

                // get all paused events with enabled mobile cells sensor
                db.loadMobileCellsSensorPausedEvents(mobileCellsEventList);

                // update of lastPausedEventsOutside, lastRunningEventsNotOutside by from database laoded
                // events and its cells
                // this will be used in NotUsedMobileCellsDetectedActivity
                for (NotUsedMobileCells _event : mobileCellsEventList) {
                    if (!MobileCellsScanner.lastPausedEvents.isEmpty())
                        MobileCellsScanner.lastPausedEvents = MobileCellsScanner.lastPausedEvents + "|";
                    MobileCellsScanner.lastPausedEvents = MobileCellsScanner.lastPausedEvents + _event.eventId;

                    /*if (_event.whenOutside) {
                        if (!MobileCellsScanner.lastPausedEvents.isEmpty())
                            MobileCellsScanner.lastPausedEvents = MobileCellsScanner.lastPausedEvents + "|";
                        MobileCellsScanner.lastPausedEvents = MobileCellsScanner.lastPausedEvents + _event.eventId;
                    } else {
                        if (!MobileCellsScanner.lastRunningEventsNotOutside.isEmpty())
                            MobileCellsScanner.lastRunningEventsNotOutside = MobileCellsScanner.lastRunningEventsNotOutside + "|";
                        MobileCellsScanner.lastRunningEventsNotOutside = MobileCellsScanner.lastRunningEventsNotOutside + _event.eventId;
                    }*/
                }
                /*
                db.loadMobileCellsSensorRunningPausedEvents(runningEventList, false);
                for (NotUsedMobileCells runningEvent : runningEventList) {
                    if (!lastRunningEventsNotOutside.isEmpty())
                        lastRunningEventsNotOutside = lastRunningEventsNotOutside + "|";
                    lastRunningEventsNotOutside = lastRunningEventsNotOutside + runningEvent.eventId;
                }

                db.loadMobileCellsSensorRunningPausedEvents(pausedEventList, true);
                for (NotUsedMobileCells pausedEvent : pausedEventList) {
                    if (!lastPausedEventsOutside.isEmpty())
                        lastPausedEventsOutside = lastPausedEventsOutside + "|";
                    lastPausedEventsOutside = lastPausedEventsOutside + pausedEvent.eventId;
                }
                */
            }

            if (MobileCellsScanner.enabledAutoRegistration) {

                if (MobileCellsScanner.isValidCellId(_registeredCell)) {

                    if (!db.isMobileCellSaved(_registeredCell)) {

                        if (!MobileCellsScanner.cellsNameForAutoRegistration.isEmpty()) {
                            List<MobileCellsData> localCellsList = new ArrayList<>();
                            localCellsList.add(new MobileCellsData(_registeredCell,
                                    MobileCellsScanner.cellsNameForAutoRegistration, true, false,
                                    Calendar.getInstance().getTimeInMillis()//,
                                    //MobileCellsScanner.lastRunningEventsNotOutside,
                                    //MobileCellsScanner.lastPausedEventsOutside,
                                    //false
                            ));
                            db.saveMobileCellsList(localCellsList, true, true);

                            synchronized (MobileCellsScanner.autoRegistrationEventList) {
                                for (Long event_id : MobileCellsScanner.autoRegistrationEventList) {
                                    String currentCells = db.getEventMobileCellsCells(event_id);
                                    if (!currentCells.isEmpty()) {
                                        String newCells = MobileCellsScanner.addCellId(currentCells, _registeredCell);
                                        db.updateMobileCellsCells(event_id, newCells);

                                        // broadcast new cell to
                                        Intent intent = new Intent(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_NEW_CELL);
                                        intent.putExtra(PPApplication.EXTRA_EVENT_ID, event_id);
                                        intent.putExtra(MobileCellsRegistrationService.EXTRA_NEW_CELL_VALUE, _registeredCell);
                                        intent.setPackage(PPApplication.PACKAGE_NAME);
                                        context.sendBroadcast(intent);

//                                    PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] PhoneProfilesService.doAutoRegistration", "(1)");
                                        Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
                                        refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, event_id);
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (notUsedMobileCellsNotificationEnabled) {

                //boolean showRunningNotification = false;
                //boolean showPausedNotification = false;
                boolean showNotification = false;

                if (/*(!MobileCellsScanner.lastRunningEventsNotOutside.isEmpty()) ||*/ (!MobileCellsScanner.lastPausedEvents.isEmpty())) {
                    // some event has configured moble cells sensor with configured cells

                    if (MobileCellsScanner.isValidCellId(_registeredCell)) {

                        if (!db.isMobileCellSaved(_registeredCell)) {

                            showNotification = true;
                        }

                        if (!showNotification) {

                            // it is not new cell
                            // test if registered cell is configured in events

                            // load _registered cell from db
                            List<MobileCellsData> _cellsList = new ArrayList<>();
                            db.addMobileCellsToList(_cellsList, _registeredCell);

                            if ((!_cellsList.isEmpty()) /*&& (!_cellsList.get(0).doNotDetect)*/) {
                                boolean found = false;
                                for (NotUsedMobileCells notUsedMobileCells : mobileCellsEventList) {
                                    //String configuredCells = db.getEventMobileCellsCells(eventId);
                                    String configuredCells = notUsedMobileCells.cells;
                                    if (!configuredCells.isEmpty()) {
                                        if (configuredCells.contains("|" + _registeredCell + "|")) {
                                            // cell is between others
                                            found = true;
                                            break;
                                        }
                                        if (configuredCells.startsWith(_registeredCell + "|")) {
                                            // cell is at start of others
                                            found = true;
                                            break;
                                        }
                                        if (configuredCells.endsWith("|" + _registeredCell)) {
                                            // cell is at end of others
                                            found = true;
                                            break;
                                        }
                                        if (configuredCells.equals(String.valueOf(_registeredCell))) {
                                            // only this cell is configured
                                            found = true;
                                            break;
                                        }
                                    } else
                                        found = true;
                                }
                                // found == false = cell is not in events
                                showNotification = !found;
                            }
                        }

                    }
                }

                //if (showRunningNotification || showPausedNotification) {
                if (showNotification) {

                    // show notification about new cell non-configured in events

                    PPApplicationStatic.createMobileCellsNewCellNotificationChannel(context);

                    boolean isShown = false;
                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (mNotificationManager != null) {
                        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
                        for (StatusBarNotification notification : notifications) {
                            String tag = notification.getTag();
                            if ((tag != null) && tag.contains(PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_TAG + "_")) {
                                if (notification.getId() == _registeredCell + PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_ID) {
                                    isShown = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!isShown) {
                        NotificationCompat.Builder mBuilder;

                        Intent intent = new Intent(context, NotUsedMobileCellsDetectedActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        String nText = context.getString(R.string.notification_not_used_mobile_cell_text1);
                        nText = nText + " " + _registeredCell + ". ";
                        nText = nText + context.getString(R.string.notification_not_used_mobile_cell_text2);

                        mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), PPApplication.NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL)
                                .setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.notification_color))
                                .setSmallIcon(R.drawable.ic_information_notify)
                                .setContentTitle(context.getString(R.string.notification_not_used_mobile_cell_title))
                                .setContentText(nText)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(nText))
                                .setAutoCancel(true); // clear notification after click

                        // Android 12 - this do not starts activity - OK
                        Intent deleteIntent = new Intent(MobileCellsScanner.NEW_MOBILE_CELLS_NOTIFICATION_DELETED_ACTION);
                        deleteIntent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_CELL_ID, _registeredCell);
                        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, _registeredCell, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setDeleteIntent(deletePendingIntent);

                        // add action button to disable not used cells detection
                        // Android 12 - this do not starts activity - OK
                        Intent disableDetectionIntent = new Intent(MobileCellsScanner.NEW_MOBILE_CELLS_NOTIFICATION_DISABLE_ACTION);
                        disableDetectionIntent.putExtra("notificationId", _registeredCell + PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_ID);
                        PendingIntent pDisableDetectionIntent = PendingIntent.getBroadcast(context, 0, disableDetectionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
                                //R.drawable.ic_action_exit_app,
                                R.drawable.ic_empty,
                                context.getString(R.string.notification_not_used_mobile_cell_disable),
                                pDisableDetectionIntent);
                        mBuilder.addAction(actionBuilder.build());

                        intent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_CELL_ID, _registeredCell);
                        intent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_LAST_CONNECTED_TIME, lastConnectedTime);
                        //intent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_LAST_RUNNING_EVENTS, MobileCellsScanner.lastRunningEventsNotOutside);
                        intent.putExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_LAST_PAUSED_EVENTS, MobileCellsScanner.lastPausedEvents);

                        PendingIntent pi = PendingIntent.getActivity(context, _registeredCell, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(pi);
                        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
                        mBuilder.setWhen(0);
                        //mBuilder.setOnlyAlertOnce(true);
                        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
                        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                        mBuilder.setGroup(PPApplication.MOBILE_CELLS_REGISTRATION_RESULT_NOTIFICATION_GROUP);

                        NotificationManagerCompat _mNotificationManager = NotificationManagerCompat.from(context);
                        try {
                            //_mNotificationManager.cancel(_registeredCell + NEW_MOBILE_CELLS_NOTIFICATION_ID);
                            _mNotificationManager.notify(
                                    PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_TAG + "_" + registeredCell,
                                    PPApplication.NEW_MOBILE_CELLS_NOTIFICATION_ID + _registeredCell, mBuilder.build());
                        } catch (SecurityException en) {
                            Log.e("MobileCellsListener.doAutoRegistration", Log.getStackTraceString(en));
                        } catch (Exception e) {
                            //Log.e("MobileCellsListener.doAutoRegistration", Log.getStackTraceString(e));
                            PPApplicationStatic.recordException(e);
                        }
                    }
                }
            }

            /*} finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {}
                }
            }*/

            //if (forceStart) {
            if (MobileCellsPreference.forceStart || MobileCellsRegistrationService.forceStart) {
                if (MobileCellsScanner.isValidCellId(_registeredCell)) {
//                PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] PhoneProfilesService.doAutoRegistration", "(2)");
                    // broadcast for event preferences
                    Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".MobileCellsPreference_refreshListView");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
                }
            }

        }

    }

/*    private static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<TelephonyManager> telephonyManagerWeakRef;

        PPHandlerThreadRunnable(Context appContext,
                                       TelephonyManager telephonyManager) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.telephonyManagerWeakRef = new WeakReference<>(telephonyManager);
        }

    }*/

}
