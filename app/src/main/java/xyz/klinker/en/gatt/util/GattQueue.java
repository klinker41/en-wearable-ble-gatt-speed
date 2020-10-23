package xyz.klinker.en.gatt.util;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.Queue;

import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
import static xyz.klinker.en.gatt.util.Constants.SERVICE_UUID;
import static xyz.klinker.en.gatt.util.Constants.WRITE_ADVERTISEMENTS_UUID;

public class GattQueue {

    private final Logger logger;
    private final Queue<byte[]> requests = new LinkedList<>();

    private BluetoothGatt gatt;
    private long startTime;
    private int totalRequests;
    @Nullable private GattFinishedCallback callback;

    public GattQueue(Logger logger) {
        this.logger = logger;
    }

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public synchronized void add(byte[] request) {
        requests.add(request);
    }

    public synchronized void clear() {
        requests.clear();
    }

    public synchronized void start(@Nullable GattFinishedCallback callback) {
        this.callback = callback;
        totalRequests = requests.size();
        startTime = System.currentTimeMillis();
        writeIfNeeded();
    }

    public synchronized void releaseNext() {
        writeIfNeeded();
    }

    private synchronized void writeIfNeeded() {
        if (!requests.isEmpty()) {
            byte[] request = requests.poll();
            BluetoothGattCharacteristic characteristic =
                    gatt
                            .getService(SERVICE_UUID.getUuid())
                            .getCharacteristic(WRITE_ADVERTISEMENTS_UUID);
            characteristic.setValue(request);
            characteristic.setWriteType(WRITE_TYPE_DEFAULT);
            // Note that this write doesn't require encryption. Encryption would most likely
            // mean longer writing time.
            boolean result = gatt.writeCharacteristic(characteristic);
            logger.v("Wrote packet with result " + result + ", " + requests.size() + " remaining");
            if (callback != null) {
                callback.onUpdate(totalRequests - requests.size(), totalRequests);
            }
        } else {
            logger.i(
                    "Finished writing packets in "
                            + (System.currentTimeMillis() - startTime)
                            + " ms");
            if (callback != null) {
                callback.onFinished();
            }
        }
    }

    @Nullable
    public byte[] readIfNeeded() {
        if (!requests.isEmpty()) {
            return requests.poll();
        }
        return null;
    }

    public interface GattFinishedCallback {
        void onUpdate(int current, int total);
        void onFinished();
    }
}
