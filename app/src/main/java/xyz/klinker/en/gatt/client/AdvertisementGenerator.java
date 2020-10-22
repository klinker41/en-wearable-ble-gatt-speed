package xyz.klinker.en.gatt.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class AdvertisementGenerator {

    private static final Random RANDOM = new Random();

    List<byte[]> generateAdvertisements(
            int numberOfDays, int numberOfAdvertisementsPerDay, int sizeOfAdvertisement) {
        List<byte[]> advertisements = new ArrayList<>();
        for (int i = 0; i < numberOfDays; i++) {
            for (int j = 0; j < numberOfAdvertisementsPerDay; j++) {
                advertisements.add(appendMetadata(generateAdvertisement(sizeOfAdvertisement)));
            }
        }
        return advertisements;
    }

    private byte[] generateAdvertisement(int sizeOfAdvertisement) {
        byte[] advertisement = new byte[sizeOfAdvertisement];
        RANDOM.nextBytes(advertisement);
        return advertisement;
    }

    private byte[] appendMetadata(byte[] advertisement) {
        // According to spec, the first 4 bytes are the timestamp where this advertisement should
        // be used, bytes 5-6 are the timestamp and the remaining at the advertisement. Since we're
        // just mocking out the spec here, we'll just use 0 for both values to make our lives easier
        // since the other side doesn't need to parse this data.
        byte[] packet = new byte[advertisement.length + 6];
        System.arraycopy(advertisement, 0, packet, 6, advertisement.length);
        return packet;
    }
}
