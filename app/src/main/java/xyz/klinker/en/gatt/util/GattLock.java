package xyz.klinker.en.gatt.util;

/** The most naive thread locking class ever. */
public class GattLock {

    private boolean hold = false;

    public void await() {
        while (hold) {}
    }

    public void release() {
        hold = false;
    }

    public void reset() {
        hold = true;
    }
}
