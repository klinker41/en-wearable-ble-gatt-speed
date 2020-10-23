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
        gattQueue.startRead(new GattQueue.GattFinishedCallback() {
            @Override
            public void onUpdate(int current, int total) {
                readCharacteristic(logger, gatt);
            }

            @Override
            public void onFinished() {
                callback.onFinished();
            }
        });
        readCharacteristic(logger, gatt);
    }

    private void readCharacteristic(Logger logger, BluetoothGatt gatt) {
        boolean result =
                gatt.readCharacteristic(
                        gatt.getService(SERVICE_UUID.getUuid()).getCharacteristic(READ_SCANS_UUID));
        logger.v("Read characteristic with result " + result);
    }
}
