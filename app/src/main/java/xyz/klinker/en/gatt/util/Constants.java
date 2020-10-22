package xyz.klinker.en.gatt.util;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.ParcelUuid;

import java.util.UUID;

public class Constants {

    public static final ParcelUuid SERVICE_UUID = new ParcelUuid(to128BitUuid((short) 0x1234));
    public static final UUID WRITE_ADVERTISEMENTS_UUID = to128BitUuid((short) 0x1111);
    public static final UUID READ_SCANS_UUID = to128BitUuid((short) 0x2222);
    public static final BluetoothGattCharacteristic WRITE_ADVERTISEMENTS =
            new BluetoothGattCharacteristic(
                    WRITE_ADVERTISEMENTS_UUID,
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);
    public static final BluetoothGattCharacteristic READ_SCANS =
            new BluetoothGattCharacteristic(
                    READ_SCANS_UUID,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);

    private static final int BIT_INDEX_OF_16_BIT_UUID = 32;

    public static UUID to128BitUuid(short shortUuid) {
        UUID baseUuid = UUID.fromString("00000000-0000-1000-8000-00805F9B34FB");
        return new UUID(
                ((shortUuid & 0xFFFFL) << BIT_INDEX_OF_16_BIT_UUID)
                        | baseUuid.getMostSignificantBits(),
                baseUuid.getLeastSignificantBits());
    }

    private Constants() {}
}
