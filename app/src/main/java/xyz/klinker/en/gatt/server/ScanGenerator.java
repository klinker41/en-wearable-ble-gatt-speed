package xyz.klinker.en.gatt.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class ScanGenerator {

    private static final Random RANDOM = new Random();

    List<byte[]> generateScans(int numberOfRecords, int recordSize) {
        List<byte[]> scans = new ArrayList<>();
        for (int i = 0; i < numberOfRecords; i++) {
            scans.add(appendMetadata(generateScan(recordSize)));
        }
        return scans;
    }

    private byte[] generateScan(int scanSize) {
        // Just generates random scan data instead of something valid.
        byte[] scan = new byte[scanSize];
        RANDOM.nextBytes(scan);
        return scan;
    }

    private byte[] appendMetadata(byte[] scan) {
        // According to the spec, each value should have the following data:
        // 3 bytes sequence number
        // 4 bytes timestamp
        // 2 bytes length
        // X bytes LTV (which contains the scanned service data and RSSI as 2 of the fields).
        //   1 byte length
        //   1 byte type
        //   X bytes value (scan.length for service data, 1 for RSSI)
        // For simplicity, all values are just set to 0 except the scan data.
        int length = 3 + 4 + 2 + 1 + 1 + scan.length + 1 + 1 + 1;
        byte[] packet = new byte[length];
        System.arraycopy(scan, 0, packet, 11, scan.length);
        return packet;
    }
}
