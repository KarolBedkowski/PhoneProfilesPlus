package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.PowerManager;

import java.util.List;

@SuppressLint("MissingPermission")
class BluetoothLEScanCallback21 extends ScanCallback {

    private final Context context;

    BluetoothLEScanCallback21(Context _context) {
        context = _context;
    }

    public void onScanResult(int callbackType, ScanResult result) {
//        PPApplication.logE("[IN_LISTENER] BluetoothLEScanCallback21.onScanResult", "xxx");

        final BluetoothDevice device = result.getDevice();

        if (device == null)
            return;

        if (!PPApplication.getApplicationStarted(true, true))
            // application is not started
            return;
        if (ApplicationPreferences.prefForceOneBluetoothScan != BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
            if (!ApplicationPreferences.applicationEventBluetoothEnableScanning)
                // scanning is disabled
                return;
        }

        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadPPScanners();
        //final Handler __handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
        //__handler.post(new PPHandlerThreadRunnable(context.getApplicationContext(), device) {
        //__handler.post(() -> {
        Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BluetoothLEScanCallback21.onScanResult");

            //Context appContext= appContextWeakRef.get();
            //BluetoothDevice device = deviceWeakRef.get();

            //if ((appContext != null) && (device != null)) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothLEScanCallback21_onScanResult");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    String btName = device.getName();

                    BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress(),
                            BluetoothScanWorker.getBluetoothType(device), false, 0, false, true);

                    BluetoothScanWorker.addLEScanResult(deviceData);
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
                }
            //}
        }; //);
        PPApplication.createScannersExecutor();
        PPApplication.scannersExecutor.submit(runnable);
    }

    public void onBatchScanResults(List<ScanResult> results) {
//        PPApplication.logE("[IN_LISTENER] BluetoothLEScanCallback21.onBatchScanResults", "xxx");

        if ((results == null) || (results.size() == 0))
            return;

        if (!PPApplication.getApplicationStarted(true, true))
            // application is not started
            return;

        if (ApplicationPreferences.prefForceOneBluetoothLEScan != BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
            if (!ApplicationPreferences.applicationEventBluetoothEnableScanning)
                // scanning is disabled
                return;
        }

        for (ScanResult result : results) {
            final BluetoothDevice device = result.getDevice();

            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadPPScanners();
            //final Handler __handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(context.getApplicationContext(), device) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThreadBluetoothLECallback", "START run - from=BluetoothLEScanCallback21.onBatchScanResults");

                //Context appContext= appContextWeakRef.get();
                //BluetoothDevice device = deviceWeakRef.get();

                //if ((appContext != null) && (device != null)) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothLEScanCallback21_onBatchScanResults");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        //String btAddress = device.getAddress();
                        String btName = device.getName();

                        BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress(),
                                BluetoothScanWorker.getBluetoothType(device), false, 0, false, true);

                        BluetoothScanWorker.addLEScanResult(deviceData);

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
                    }
                //}
            }; //);
            PPApplication.createScannersExecutor();
            PPApplication.scannersExecutor.submit(runnable);

        }

    }

    public void onScanFailed(int errorCode) {
//        PPApplication.logE("[IN_LISTENER] BluetoothLEScanCallback21.onScanFailed", "xxx");

        PPApplication.logToACRA("E/BluetoothLEScanCallback21.onScanFailed: errorCode=" + errorCode);
    }

/*    private static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<BluetoothDevice> deviceWeakRef;

        PPHandlerThreadRunnable(Context appContext,
                                    BluetoothDevice device) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.deviceWeakRef = new WeakReference<>(device);
        }

    }*/

}
