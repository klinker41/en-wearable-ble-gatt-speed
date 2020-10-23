package xyz.klinker.en.gatt.client;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.text.format.Formatter;

import androidx.annotation.Nullable;

import java.util.List;

import xyz.klinker.en.gatt.util.GattQueue;
import xyz.klinker.en.gatt.util.Logger;

final class AdvertisementSyncer {

    private final Context context;
    private final GattQueue gattQueue;
    @Nullable private BluetoothGatt gatt;
    private int mtu = 0;

    AdvertisementSyncer(Context context, GattQueue gattQueue) {
        this.context = context;
        this.gattQueue = gattQueue;
    }

    void setGatt(BluetoothGatt gatt, int mtu) {
        this.gatt = gatt;
        this.mtu = mtu;
    }

    void sendRpis(List<byte[]> rpis, Logger logger, GattQueue.GattFinishedCallback callback) {
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
        logger.i(
                "Preparing to send "
                        + rpis.size()
                        + " RPIs with total size "
                        + Formatter.formatShortFileSize(
                                context, rpis.size() * rpis.get(0).length)
                        + " over MTU "
                        + mtu);
        gattQueue.setGatt(gatt);
        byte[] writeRequest = new byte[mtu];
        int currentLength = 0;
        for (int i = 0; i < rpis.size(); i++) {
            byte[] rpi = rpis.get(i);
            if (currentLength + rpi.length > mtu) {
                enqueueCurrentWriteRequest(writeRequest, currentLength);
                writeRequest = new byte[mtu];
                currentLength = 0;
            }
            System.arraycopy(rpi, 0, writeRequest, currentLength, rpi.length);
            currentLength += rpi.length;
        }
        enqueueCurrentWriteRequest(writeRequest, currentLength);
        gattQueue.start(callback);
    }

    private void enqueueCurrentWriteRequest(byte[] writeRequest, int currentLength) {
        byte[] packet = new byte[currentLength];
        System.arraycopy(writeRequest, 0, packet, 0, currentLength);
        gattQueue.add(packet);
    }
}
