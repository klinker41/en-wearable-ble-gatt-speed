package xyz.klinker.en.gatt.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
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

import xyz.klinker.en.gatt.util.GattQueue;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.le.ScanSettings.SCAN_MODE_BALANCED;
import static xyz.klinker.en.gatt.util.Constants.SERVICE_UUID;

final class Scanner {

    private static final int ERROR_NO_ADAPTER = -1;

    private final Context context;
    private final GattQueue gattQueue;
    private final Set<BluetoothDevice> discoveredDevices = new HashSet<>();

    @Nullable private ScanCallback platformCallback;
    @Nullable private ScannerCallback callback;
    @Nullable private BluetoothGatt gatt;

    Scanner(Context context, GattQueue gattQueue) {
        this.context = context;
        this.gattQueue = gattQueue;
    }

    void beginScanning(ScannerCallback callback) {
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
                            gatt =
                                    result.getDevice().connectGatt(
                                            context,
                                            false,
                                            new ScannerGattCallback(callback, gattQueue));
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

    void stopScanning() {
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
            gatt = null;
        }
        if (platformCallback != null) {
            BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().stopScan(platformCallback);
            platformCallback = null;
        }
        if (callback != null) {
            callback.onScanningStopped();
            callback = null;
        }
    }

    private static class ScannerGattCallback extends BluetoothGattCallback {

        private final ScannerCallback callback;
        private final GattQueue gattQueue;

        private ScannerGattCallback(ScannerCallback callback, GattQueue gattQueue) {
            this.callback = callback;
            this.gattQueue = gattQueue;
        }

        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            callback.onGattOperation(
                    gatt.getDevice(), "phyUpdate", txPhy + ", " + rxPhy + ", " + status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            callback.onGattOperation(
                    gatt.getDevice(), "phyRead", txPhy + ", " + rxPhy + ", " + status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            callback.onGattOperation(
                    gatt.getDevice(),
                    "connectionStateChange",
                    "status: " + status + ", newState " + newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTING:
                    callback.onGattConnecting(gatt.getDevice());
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    callback.onGattConnected(gatt.getDevice(), gatt);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    callback.onGattDisconnected(gatt.getDevice());
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            callback.onGattServicesDiscovered(gatt.getDevice(), gatt);
        }

        @Override
        public void onCharacteristicRead(
                BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            callback.onGattOperation(
                    gatt.getDevice(),
                    "characteristicRead",
                    characteristic.getUuid().toString());
            if (status == GATT_SUCCESS) {
                gattQueue.releaseNextRead();
            } else {
                gattQueue.finishedReading();
            }
        }

        @Override
        public void onCharacteristicWrite(
                BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            callback.onGattOperation(
                    gatt.getDevice(),
                    "characteristicWrite",
                    characteristic.getUuid().toString() + ", " + characteristic.getValue().length);
            gattQueue.releaseNextWrite();
        }

        @Override
        public void onCharacteristicChanged(
                BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            callback.onGattOperation(
                    gatt.getDevice(),
                    "characteristicChanged",
                    characteristic.getStringValue(0));
        }

        @Override
        public void onDescriptorRead(
                BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            callback.onGattOperation(
                    gatt.getDevice(), "descriptorRead", descriptor.getUuid().toString());
        }

        @Override
        public void onDescriptorWrite(
                BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            callback.onGattOperation(
                    gatt.getDevice(), "descriptorWrite", descriptor.getUuid().toString());
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            callback.onGattOperation(
                    gatt.getDevice(),
                    "reliableWriteCompleted",
                    Integer.toString(status));
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            callback.onGattOperation(
                    gatt.getDevice(), "readRemoteRssi", Integer.toString(rssi));
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            callback.onGattMtuChanged(gatt.getDevice(), mtu);
            gatt.discoverServices();
        }
    }

    interface ScannerCallback {
        void onScanningStarted();
        void onScanningFailed(int errorCode);
        void onDeviceFound(BluetoothDevice device);
        void onScanningStopped();
        void onGattConnecting(BluetoothDevice device);
        void onGattConnected(BluetoothDevice device, BluetoothGatt gatt);
        void onGattMtuChanged(BluetoothDevice device, int mtu);
        void onGattServicesDiscovered(BluetoothDevice device, BluetoothGatt gatt);
        void onGattOperation(BluetoothDevice device, String operation, String value);
        void onGattDisconnected(BluetoothDevice device);
    }
}
