package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.SystemClock;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class BluetoothConnectionBroadcastReceiver extends BroadcastReceiver {

    private static volatile List<BluetoothDeviceData> connectedDevices = null;

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] BluetoothConnectionBroadcastReceiver.onReceive", "xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (intent == null)
            return;

        final String action = intent.getAction();
        if (action == null)
            return;

        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED) ||
                action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) ||
                action.equals(BluetoothDevice.ACTION_NAME_CHANGED) ||
                action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {
            // BluetoothConnectionBroadcastReceiver

            PPApplicationStatic.logE("[IN_BROADCAST] BluetoothConnectionBroadcastReceiver.onReceive", "action="+action);
//            Log.e("BluetoothConnectionBroadcastReceiver.onReceive", "[2] action="+action);

            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            PPApplicationStatic.logE("[IN_BROADCAST] BluetoothConnectionBroadcastReceiver.onReceive", "device="+device);

            //if (device == null)
            //    return;

            //final boolean connected = action.equals(BluetoothDevice.ACTION_ACL_CONNECTED);
            final String newName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            PPApplicationStatic.logE("[IN_BROADCAST] BluetoothConnectionBroadcastReceiver.onReceive", "newName="+newName);

            // this is important, because ACTION_NAME_CHANGED is called very often
            if (action.equals(BluetoothDevice.ACTION_NAME_CHANGED)) {
                if (newName == null) {
                    return;
                }
                try {
                    if ((device != null) && newName.equals(device.getName())) {
                        return;
                    }
                } catch (SecurityException e) {
                    return;
                }
            }

//            if (device != null) {
////            PPApplicationStatic.logE("[IN_BROADCAST] BluetoothConnectionBroadcastReceiver.onReceive", "device="+device.getName());
//                Log.e("BluetoothConnectionBroadcastReceiver.onReceive", "[2] device.name=" + device.getName());
//                Log.e("BluetoothConnectionBroadcastReceiver.onReceive", "[2] device.address=" + device.getAddress());
//            }

            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadBroadcast(/*"BluetoothConnectionBroadcastReceiver.onReceive"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPHandlerThreadRunnable(context.getApplicationContext(), device) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BluetoothConnectionBroadcastReceiver.onReceive");
//                Log.e("BluetoothConnectionBroadcastReceiver.onReceive", "[2] start of executor");

                //Context appContext= appContextWeakRef.get();
                //BluetoothDevice device = deviceWeakRef.get();

                //if ((appContext != null) && (device != null)) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothConnectionBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        //TODO
                        /*
                        getConnectedDevices(appContext);

                        if (device != null) {
                            try {
                                switch (action) {
                                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                                        addConnectedDevice(device);
                                        break;
                                    case BluetoothDevice.ACTION_NAME_CHANGED:
                                        //noinspection ConstantConditions
                                        if (newName != null) {
                                            changeDeviceName(device, newName);
                                        }
                                        break;
                                    default:
                                        removeConnectedDevice(device);
                                        break;
                                }
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                            }
                        }

                        saveConnectedDevices(appContext);
                        */

                        if (EventStatic.getGlobalEventsRunning(appContext)) {

                            //if (lastState != currState)
                            //{

                            /*
                            if (!(ApplicationPreferences.prefEventBluetoothScanRequest ||
                                    ApplicationPreferences.prefEventBluetoothLEScanRequest ||
                                    ApplicationPreferences.prefEventBluetoothWaitForResult ||
                                    ApplicationPreferences.prefEventBluetoothLEWaitForResult ||
                                    ApplicationPreferences.prefEventBluetoothEnabledForScan)) {
                             */

//                                Log.e("BluetoothConnectionBroadcastReceiver.onReceive", "**** START of getConnectedDevices");
                                BluetoothConnectedDevices.getConnectedDevices(appContext, true);

                            //}
                        }
                        //}

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

    private static final String CONNECTED_DEVICES_COUNT_PREF = "count";
    private static final String CONNECTED_DEVICES_DEVICE_PREF = "device";

    @SuppressLint("MissingPermission")
    static void getConnectedDevices(Context context)
    {
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            if (connectedDevices == null)
                connectedDevices = new ArrayList<>();

            connectedDevices.clear();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME, Context.MODE_PRIVATE);

            int count = preferences.getInt(CONNECTED_DEVICES_COUNT_PREF, 0);

            Gson gson = new Gson();

            int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
            for (int i = 0; i < count; i++) {
                String json = preferences.getString(CONNECTED_DEVICES_DEVICE_PREF + i, "");
                if (!json.isEmpty()) {
                    BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);

                    //long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                    Calendar calendar = Calendar.getInstance();
                    long bootTime = calendar.getTimeInMillis() - SystemClock.elapsedRealtime() - gmtOffset;

                    if (device.timestamp >= bootTime) {
                        connectedDevices.add(device);
                    }
                }
            }
        }
    }

    static void saveConnectedDevices(Context context)
    {
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {

            if (connectedDevices == null)
                connectedDevices = new ArrayList<>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            editor.putInt(CONNECTED_DEVICES_COUNT_PREF, connectedDevices.size());

            Gson gson = new Gson();

            for (int i = 0; i < connectedDevices.size(); i++) {
                String json = gson.toJson(connectedDevices.get(i));
                editor.putString(CONNECTED_DEVICES_DEVICE_PREF + i, json);
            }

            editor.apply();
        }
    }

/*
    @SuppressLint("MissingPermission")
    private static void addConnectedDevice(BluetoothDevice device)
    {
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            boolean found = false;
            for (BluetoothDeviceData _device : connectedDevices) {
                if (_device.address.equals(device.getAddress())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (BluetoothDeviceData _device : connectedDevices) {
                    if (_device.getName().equalsIgnoreCase(device.getName())) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                Calendar now = Calendar.getInstance();
                long timestamp = now.getTimeInMillis() - gmtOffset;
                connectedDevices.add(new BluetoothDeviceData(device.getName(), device.getAddress(),
                        BluetoothScanWorker.getBluetoothType(device), false, timestamp, false, false));
            }
        }
    }

    @SuppressLint("MissingPermission")
    private static void removeConnectedDevice(BluetoothDevice device)
    {
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            //int index = 0;
            BluetoothDeviceData deviceToRemove = null;
            boolean found = false;
            for (BluetoothDeviceData _device : connectedDevices) {
                if (_device.address.equals(device.getAddress())) {
                    found = true;
                    deviceToRemove = _device;
                    break;
                }
                //++index;
            }
            if (!found) {
                //index = 0;
                for (BluetoothDeviceData _device : connectedDevices) {
                    if (_device.getName().equalsIgnoreCase(device.getName())) {
                        found = true;
                        deviceToRemove = _device;
                        break;
                    }
                    //++index;
                }
            }
            if (found)
                //connectedDevices.remove(index);
                connectedDevices.remove(deviceToRemove);
        }
    }
*/
    static void clearConnectedDevices(Context context, boolean onlyOld)
    {
        if (onlyOld) {
            getConnectedDevices(context);
        }

        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            if (connectedDevices != null) {
                if (onlyOld) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    for (Iterator<BluetoothDeviceData> it = connectedDevices.iterator(); it.hasNext(); ) {
                        BluetoothDeviceData device = it.next();
                        //long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                        Calendar calendar = Calendar.getInstance();
                        long bootTime = calendar.getTimeInMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                        if (device.timestamp < bootTime)
                            //connectedDevices.remove(device);
                            it.remove();
                    }
                }
                else
                    connectedDevices.clear();
            }
        }
    }

/*
    @SuppressLint("MissingPermission")
    private static void changeDeviceName(BluetoothDevice device, String deviceName)
    {
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            boolean found = false;
            for (BluetoothDeviceData _device : connectedDevices) {
                if (_device.address.equals(device.getAddress()) && !deviceName.isEmpty()) {
                    _device.setName(deviceName);
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (BluetoothDeviceData _device : connectedDevices) {
                    if (_device.getName().equalsIgnoreCase(device.getName()) && !deviceName.isEmpty()) {
                        _device.setName(deviceName);
                        break;
                    }
                }
            }
        }
    }
*/

    static void addConnectedDeviceData(List<BluetoothDeviceData> detectedDevices)
    {
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            for (BluetoothDeviceData device : detectedDevices) {
                boolean found = false;
                for (BluetoothDeviceData _device : connectedDevices) {
                    if (_device.getAddress().equals(device.getAddress())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    for (BluetoothDeviceData _device : connectedDevices) {
                        if (_device.getName().equalsIgnoreCase(device.getName())) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    connectedDevices.add(device);
                }
            }
        }
    }

    static boolean isBluetoothConnected(BluetoothDeviceData deviceData, String sensorDeviceName)
    {
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            if ((deviceData == null) && sensorDeviceName.isEmpty()) {
               // is device connected to any external bluetooth device ???

                return (connectedDevices != null) && (connectedDevices.size() > 0);
            }
            else {
                if (connectedDevices != null) {
                    if (deviceData != null) {
                        // is device connected to deviceData ???

                        boolean found = false;
                        for (BluetoothDeviceData _device : connectedDevices) {
                            if (_device.address.equals(deviceData.getAddress())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            for (BluetoothDeviceData _device : connectedDevices) {
                                String _deviceName = _device.getName().trim();
                                String deviceDataName = deviceData.getName().trim();
                                if (_deviceName.equalsIgnoreCase(deviceDataName)) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        return found;
                    }
                    else {
                        // is device connected to sensorDeviceName ???

                        for (BluetoothDeviceData _device : connectedDevices) {
                            String device = _device.getName().trim().toUpperCase();
                            String _adapterName = sensorDeviceName.trim().toUpperCase();
                            if (Wildcard.match(device, _adapterName, '_', '%', true)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        }
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
