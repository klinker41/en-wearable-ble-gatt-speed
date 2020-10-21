package xyz.klinker.en.gatt.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;

import androidx.annotation.Nullable;

import static android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_BALANCED;
import static android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM;
import static xyz.klinker.en.gatt.util.Constants.SERVICE_UUID;

public class Advertiser {

    private static final int ERROR_NO_ADAPTER = -1;

    private final Context context;
    @Nullable private AdvertiseCallback platformCallback;
    @Nullable private AdvertiserCallback callback;

    public Advertiser(Context context) {
        this.context = context;
    }

    public void beginAdvertising(AdvertiserCallback callback) {
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
                        // TODO(jklinker): Start GATT server.
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

    public void stopAdvertising() {
        if (platformCallback == null) {
            return;
        }
        BluetoothAdapter.getDefaultAdapter()
                .getBluetoothLeAdvertiser()
                .stopAdvertising(platformCallback);
        platformCallback = null;
        if (callback == null) {
            return;
        }
        callback.onAdvertisingStopped();
        callback = null;
    }

    public interface AdvertiserCallback {
        void onAdvertisingStarted();
        void onAdvertisingFailed(int errorCode);
        void onAdvertisingStopped();
        void onGattConnected(BluetoothDevice device);
        void onGattDisconnected(BluetoothDevice device);
    }
}
