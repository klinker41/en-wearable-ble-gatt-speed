package xyz.klinker.en.gatt.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.bluetooth.le.ScanSettings.SCAN_MODE_BALANCED;
import static xyz.klinker.en.gatt.util.Constants.SERVICE_UUID;

public class Scanner {

    private static final int ERROR_NO_ADAPTER = -1;

    private final Context context;
    private final Set<BluetoothDevice> discoveredDevices = new HashSet<>();

    @Nullable private ScanCallback platformCallback;
    @Nullable private ScannerCallback callback;

    public Scanner(Context context) {
        this.context = context;
    }

    public void beginScanning(ScannerCallback callback) {
        this.callback = callback;
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(SERVICE_UUID).build());
        ScanSettings settings =
                new ScanSettings.Builder().setScanMode(SCAN_MODE_BALANCED).build();
        platformCallback =
                new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        boolean added = discoveredDevices.add(result.getDevice());
                        if (added) {
                            callback.onDeviceFound(result.getDevice());
                            // TODO(jklinker): Start connecting GATT to the device.
                        }
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        callback.onScanningFailed(errorCode);
                    }
                };
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            callback.onScanningFailed(ERROR_NO_ADAPTER);
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner == null) {
            callback.onScanningFailed(ERROR_NO_ADAPTER);
            return;
        }
        scanner.startScan(filters, settings, platformCallback);
        callback.onScanningStarted();
    }

    public void stopScanning() {
        if (platformCallback == null) {
            return;
        }
        BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().stopScan(platformCallback);
        platformCallback = null;
        if (callback == null) {
            return;
        }
        callback.onScanningStopped();
        callback = null;
    }

    public interface ScannerCallback {
        void onScanningStarted();
        void onScanningFailed(int errorCode);
        void onDeviceFound(BluetoothDevice device);
        void onScanningStopped();
        void onGattConnected(BluetoothDevice device);
        void onGattDisconnected(BluetoothDevice device);
    }
}
