package xyz.klinker.en.gatt.util;

import android.os.ParcelUuid;

import java.util.UUID;

public class Constants {

    public static final ParcelUuid SERVICE_UUID = new ParcelUuid(to128BitUuid((short) 0x1234));
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
