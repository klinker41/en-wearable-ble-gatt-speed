package xyz.klinker.en.gatt.server;

import android.content.Context;
import android.text.format.Formatter;

import java.util.List;

import xyz.klinker.en.gatt.util.GattQueue;
import xyz.klinker.en.gatt.util.Logger;

final class ScanProvider {

    private final Context context;
    private final GattQueue gattQueue;

    ScanProvider(Context context, GattQueue gattQueue) {
        this.context = context;
        this.gattQueue = gattQueue;
    }

    void loadScans(List<byte[]> scans, int mtu, Logger logger) {
        byte[] readRequest = new byte[mtu];
        int currentLength = 0;
        for (int i = 0; i < scans.size(); i++) {
            byte[] scan = scans.get(i);
            if (currentLength + scan.length > mtu) {
                enqueueCurrentReadRequest(readRequest, currentLength);
                readRequest = new byte[mtu];
                currentLength = 0;
            }
            System.arraycopy(scan, 0, readRequest, currentLength, scan.length);
            currentLength += scan.length;
        }
        enqueueCurrentReadRequest(readRequest, currentLength);
        if (scans.size() > 0) {
            logger.i(
                    "Loaded "
                            + Formatter.formatShortFileSize(
                                    context,scans.size() * scans.get(0).length)
                            + " worth of scans");
        }
    }

    private void enqueueCurrentReadRequest(byte[] request, int currentLength) {
        byte[] packet = new byte[currentLength];
        System.arraycopy(request, 0, packet, 0, currentLength);
        gattQueue.add(packet);
    }

    void clearScans() {
        gattQueue.clear();
    }
}
