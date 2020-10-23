package xyz.klinker.en.gatt.server;

import android.content.Context;

import java.util.List;

import xyz.klinker.en.gatt.util.GattQueue;

final class ScanProvider {

    private final Context context;
    private final GattQueue gattQueue;

    ScanProvider(Context context, GattQueue gattQueue) {
        this.context = context;
        this.gattQueue = gattQueue;
    }

    void loadScans(List<byte[]> scans) {
        for (byte[] scan : scans) {
            gattQueue.add(scan);
        }
    }

    void clearScans() {
        gattQueue.clear();
    }
}
