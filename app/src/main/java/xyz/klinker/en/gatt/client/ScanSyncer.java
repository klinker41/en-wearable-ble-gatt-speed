package xyz.klinker.en.gatt.client;

import android.bluetooth.BluetoothGatt;
import android.content.Context;

import xyz.klinker.en.gatt.util.GattQueue;
import xyz.klinker.en.gatt.util.Logger;

import static xyz.klinker.en.gatt.util.Constants.READ_SCANS_UUID;
import static xyz.klinker.en.gatt.util.Constants.SERVICE_UUID;

final class ScanSyncer {

    private final Context context;
    private final GattQueue gattQueue;

    ScanSyncer(Context context, GattQueue gattQueue) {
        this.context = context;
        this.gattQueue = gattQueue;
    }

    void readScans(BluetoothGatt gatt, Logger logger, GattQueue.GattFinishedCallback callback) {
        // TODO(jklinker): Read characteristic multiple times until finished.
        boolean result =
                gatt.readCharacteristic(
                        gatt.getService(SERVICE_UUID.getUuid()).getCharacteristic(READ_SCANS_UUID));
        logger.i("Read characteristic with result " + result);
        callback.onFinished();
    }
}
