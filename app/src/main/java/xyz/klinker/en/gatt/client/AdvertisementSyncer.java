package xyz.klinker.en.gatt.client;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.text.format.Formatter;

import androidx.annotation.Nullable;

import java.util.List;

import xyz.klinker.en.gatt.util.GattLock;
import xyz.klinker.en.gatt.util.Logger;

import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
import static xyz.klinker.en.gatt.util.Constants.SERVICE_UUID;
import static xyz.klinker.en.gatt.util.Constants.WRITE_ADVERTISEMENTS_UUID;

final class AdvertisementSyncer {

    private final Context context;
    private final GattLock gattLock;
    @Nullable private BluetoothGatt gatt;
    private int mtu = 0;

    AdvertisementSyncer(Context context, GattLock gattLock) {
        this.context = context;
        this.gattLock = gattLock;
    }

    void setGatt(BluetoothGatt gatt, int mtu) {
        this.gatt = gatt;
        this.mtu = mtu;
    }

    void sendRpis(List<byte[]> rpis, Logger logger) {
        if (gatt == null) {
            logger.e("Unable to send RPIs, no GATT connection");
            return;
        }
        if (rpis.size() == 0) {
            logger.e("Unable to send RPIs, none available");
            return;
        }
        if (mtu < rpis.get(0).length) {
            logger.e("Unable to send RPIs, MTU less than packet size");
            return;
        }
        long startTime = System.currentTimeMillis();
        logger.i(
                "Preparing to send "
                        + rpis.size()
                        + " RPIs with total size "
                        + Formatter.formatShortFileSize(
                                context, rpis.size() * rpis.get(0).length)
                        + " over MTU "
                        + mtu);
        byte[] writeRequest = new byte[mtu];
        int currentLength = 0;
        for (int i = 0; i < rpis.size(); i++) {
            byte[] rpi = rpis.get(i);
            if (currentLength + rpi.length > mtu) {
                logger.v("Starting write of " + currentLength + " bytes");
                boolean result = sendCurrentWriteRequest(writeRequest, currentLength);
                if (result) {
                    logger.v(
                            "Finished writing "
                                    + currentLength
                                    + " bytes, progress: "
                                    + ((float) i / rpis.size()));
                } else {
                    logger.e("Failed to write characteristic");
                }
                writeRequest = new byte[mtu];
                currentLength = 0;
            }
            System.arraycopy(rpi, 0, writeRequest, currentLength, rpi.length);
            currentLength += rpi.length;
        }
        sendCurrentWriteRequest(writeRequest, currentLength);
        logger.i("Finished sending RPIs in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    private boolean sendCurrentWriteRequest(byte[] writeRequest, int currentLength) {
        // Note that this write doesn't require encryption. Encryption would most likely
        // mean longer writing time.
        byte[] packet = new byte[currentLength];
        System.arraycopy(writeRequest, 0, packet, 0, currentLength);
        BluetoothGattCharacteristic characteristic =
                gatt
                        .getService(SERVICE_UUID.getUuid())
                        .getCharacteristic(WRITE_ADVERTISEMENTS_UUID);
        characteristic.setValue(packet);
        characteristic.setWriteType(WRITE_TYPE_DEFAULT);
        // TODO(jklinker): Figure out the right timing here, this isn't working reliably.
        gattLock.await();
        boolean result = gatt.writeCharacteristic(characteristic);
        gattLock.reset();
        return result;
    }
}
