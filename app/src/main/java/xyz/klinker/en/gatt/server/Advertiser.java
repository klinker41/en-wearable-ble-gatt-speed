package xyz.klinker.en.gatt.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;

import androidx.annotation.Nullable;

import xyz.klinker.en.gatt.util.GattQueue;

import static android.bluetooth.BluetoothGatt.GATT_FAILURE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_BALANCED;
import static android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM;
import static xyz.klinker.en.gatt.util.Constants.READ_SCANS_UUID;
import static xyz.klinker.en.gatt.util.Constants.SERVICE_UUID;
import static xyz.klinker.en.gatt.util.Constants.WRITE_ADVERTISEMENTS_UUID;

final class Advertiser {

    private static final int ERROR_NO_ADAPTER = -1;

    private final Context context;
    private final GattQueue gattQueue;
    @Nullable private AdvertiseCallback platformCallback;
    @Nullable private AdvertiserCallback callback;
    @Nullable private BluetoothGattServer server;

    Advertiser(Context context, GattQueue gattQueue) {
        this.context = context;
        this.gattQueue = gattQueue;
    }

    void beginAdvertising(AdvertiserCallback callback) {
        this.callback = callback;
        AdvertiseSettings settings =
                new AdvertiseSettings.Builder()
                        .setAdvertiseMode(ADVERTISE_MODE_BALANCED)
                        .setTxPowerLevel(ADVERTISE_TX_POWER_MEDIUM)
                        .setConnectable(true)
                        .build();
        AdvertiseData data =
                new AdvertiseData.Builder()
                        .setIncludeDeviceName(false)
                        .setIncludeTxPowerLevel(false)
                        .addServiceUuid(SERVICE_UUID)
                        .build();
        platformCallback =
                new AdvertiseCallback() {
                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        super.onStartSuccess(settingsInEffect);
                        callback.onAdvertisingStarted();
                        createGattServer();
                    }

                    @Override
                    public void onStartFailure(int errorCode) {
                        super.onStartFailure(errorCode);
                        callback.onAdvertisingFailed(errorCode);
                    }
                };
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            callback.onAdvertisingFailed(ERROR_NO_ADAPTER);
            return;
        }
        BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();
        if (advertiser == null) {
            callback.onAdvertisingFailed(ERROR_NO_ADAPTER);
            return;
        }
        advertiser.startAdvertising(settings, data, platformCallback);
    }

    void stopAdvertising() {
        if (server != null) {
            for (BluetoothDevice device : server.getConnectedDevices()) {
                server.cancelConnection(device);
            }
            server.close();
            server = null;
        }
        if (platformCallback != null) {
            BluetoothAdapter.getDefaultAdapter()
                    .getBluetoothLeAdvertiser()
                    .stopAdvertising(platformCallback);
            platformCallback = null;
        }
        if (callback != null) {
            callback.onAdvertisingStopped();
        }
    }

    private void createGattServer() {
        BluetoothManager manager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        AdvertiserGattServerCallback gattCallback =
                new AdvertiserGattServerCallback(callback, gattQueue);
        BluetoothGattServer server = manager.openGattServer(context, gattCallback);
        BluetoothGattService service =
                new BluetoothGattService(
                        SERVICE_UUID.getUuid(), BluetoothGattService.SERVICE_TYPE_PRIMARY);
        service.addCharacteristic(
                new BluetoothGattCharacteristic(
                        WRITE_ADVERTISEMENTS_UUID,
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE));
        service.addCharacteristic(
                new BluetoothGattCharacteristic(
                        READ_SCANS_UUID,
                        BluetoothGattCharacteristic.PROPERTY_READ,
                        BluetoothGattCharacteristic.PERMISSION_READ));
        server.addService(service);
        gattCallback.setBluetoothGattServer(server);
        callback.onGattCreated();
    }

    private static class AdvertiserGattServerCallback extends BluetoothGattServerCallback {

        private final AdvertiserCallback callback;
        private final GattQueue gattQueue;
        private  BluetoothGattServer server;

        private AdvertiserGattServerCallback(AdvertiserCallback callback, GattQueue gattQueue) {
            this.callback = callback;
            this.gattQueue = gattQueue;
        }

        private void setBluetoothGattServer(BluetoothGattServer server) {
            this.server = server;
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            callback.onGattOperation(
                    device,
                    "connectionStateChange",
                    "status: " + status + ", newState " + newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTING:
                    callback.onGattConnecting(device);
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    callback.onGattConnected(device);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    callback.onGattDisconnected(device);
                    break;
            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            callback.onGattOperation(null, "serviceAdded", service.getUuid().toString());
        }

        @Override
        public void onCharacteristicReadRequest(
                BluetoothDevice device,
                int requestId,
                int offset,
                BluetoothGattCharacteristic characteristic) {
            callback.onGattOperation(
                    device, "characteristicReadRequest", characteristic.getUuid().toString());
            byte[] value = gattQueue.readIfNeeded();
            if (value != null) {
                server.sendResponse(device, requestId, GATT_SUCCESS, 0, value);
                callback.onGattOperation(device, "sendingScan", "sent");
            } else {
                server.sendResponse(device, requestId, GATT_FAILURE, 0, null);
                callback.onGattOperation(device, "finishedReadingScans", "done");
            }
        }

        @Override
        public void onCharacteristicWriteRequest(
                BluetoothDevice device,
                int requestId,
                BluetoothGattCharacteristic characteristic,
                boolean preparedWrite,
                boolean responseNeeded,
                int offset,
                byte[] value) {
            callback.onGattOperation(
                    device,
                    "characteristicWriteRequest",
                    characteristic.getUuid().toString());
            if (responseNeeded) {
                server.sendResponse(device, requestId, GATT_SUCCESS, 0, null);
                callback.onGattOperation(device, "characteristicWriteResponse", "sent");
            }
        }

        @Override
        public void onDescriptorReadRequest(
                BluetoothDevice device,
                int requestId,
                int offset,
                BluetoothGattDescriptor descriptor) {
            callback.onGattOperation(
                    device, "descriptorReadRequest", descriptor.getUuid().toString());
        }

        @Override
        public void onDescriptorWriteRequest(
                BluetoothDevice device,
                int requestId,
                BluetoothGattDescriptor descriptor,
                boolean preparedWrite,
                boolean responseNeeded,
                int offset,
                byte[] value) {
            callback.onGattOperation(
                    device, "descriptorWrite", descriptor.getUuid().toString());
            if (responseNeeded) {
                server.sendResponse(device, requestId, GATT_SUCCESS, 0, null);
                callback.onGattOperation(device, "descriptorWriteResponse", "sent");
            }
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            callback.onGattOperation(device, "executeWrite", Boolean.toString(execute));
            server.sendResponse(device, requestId, GATT_SUCCESS, 0, null);
            callback.onGattOperation(device, "executeWriteResponse", "sent");
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            callback.onGattOperation(device, "notificationSent", Integer.toString(status));
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            callback.onGattMtuChanged(device, mtu);
        }

        @Override
        public void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status) {
            callback.onGattOperation(
                    device, "phyUpdate", txPhy + ", " + rxPhy + ", " + status);
        }

        @Override
        public void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {
            callback.onGattOperation(
                    device, "phyRead", txPhy + ", " + rxPhy + ", " + status);
        }
    }

    public interface AdvertiserCallback {
        void onAdvertisingStarted();
        void onAdvertisingFailed(int errorCode);
        void onAdvertisingStopped();
        void onGattCreated();
        void onGattConnecting(BluetoothDevice device);
        void onGattConnected(BluetoothDevice device);
        void onGattMtuChanged(BluetoothDevice device, int mtu);
        void onGattOperation(BluetoothDevice device, String operation, String value);
        void onGattDisconnected(BluetoothDevice device);
    }
}
