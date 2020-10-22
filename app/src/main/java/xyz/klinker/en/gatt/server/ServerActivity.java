package xyz.klinker.en.gatt.server;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import xyz.klinker.en.gatt.R;
import xyz.klinker.en.gatt.util.Logger;

public class ServerActivity  extends AppCompatActivity implements Advertiser.AdvertiserCallback {

    private static final String TAG = "ServerActivity";

    private Logger logger;
    private Advertiser advertiser;

    private TextView numberOfScanRecordsLabel;
    private Slider numberOfScanRecordsSlider;
    private TextView sizeOfScanRecordsLabel;
    private Slider sizeOfScanRecordsSlider;
    private TextView connectionStatusLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        logger = new Logger(this, TAG, findViewById(R.id.log));
        advertiser = new Advertiser(this);

        numberOfScanRecordsLabel = findViewById(R.id.number_scan_records);
        numberOfScanRecordsSlider = findViewById(R.id.number_scan_records_slider);
        sizeOfScanRecordsLabel = findViewById(R.id.scan_record_size);
        sizeOfScanRecordsSlider = findViewById(R.id.scan_record_size_slider);
        connectionStatusLabel = findViewById(R.id.connection_status);
        initializeSliders();
    }

    @Override
    public void onStart() {
        super.onStart();
        advertiser.beginAdvertising(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        advertiser.stopAdvertising();
    }

    @Override
    public void onAdvertisingStarted() {
        logger.i("Successfully started advertising");
    }

    @Override
    public void onAdvertisingFailed(int errorCode) {
        logger.e("Failed to start advertising: " + errorCode);
    }

    @Override
    public void onAdvertisingStopped() {
        logger.i("Successfully stopped advertising");
    }

    @Override
    public void onGattCreated() {
        logger.i("GATT server created");
    }

    @Override
    public void onGattConnecting(BluetoothDevice device) {
        logger.i("Received GATT connection request: " + device);
    }

    @Override
    public void onGattConnected(BluetoothDevice device) {
        logger.i("GATT connected: " + device);
        runOnUiThread(() ->
                connectionStatusLabel.setText(R.string.connection_status_connected));
    }

    @Override
    public void onGattMtuChanged(BluetoothDevice device, int mtu) {
        logger.i("GATT MTU changed: " + mtu);
    }

    @Override
    public void onGattOperation(BluetoothDevice device, String operation, String value) {
        logger.v("GATT: " + device + ", " + operation + ", " + value);
    }

    @Override
    public void onGattDisconnected(BluetoothDevice device) {
        logger.i("GATT disconnected: " + device);
        runOnUiThread(() ->
                connectionStatusLabel.setText(R.string.connection_status_disconnected));
    }

    private void initializeSliders() {
        attachSliderListener(numberOfScanRecordsSlider, numberOfScanRecordsLabel);
        attachSliderListener(sizeOfScanRecordsSlider, sizeOfScanRecordsLabel);
    }

    private void attachSliderListener(Slider slider, TextView label) {
        slider.addOnChangeListener(
                ((s, value, fromUser) -> setLabelValue(label, slider.getValue())));
        setLabelValue(label, slider.getValue());
    }

    private void setLabelValue(TextView label, float value) {
        label.setText(Integer.toString((int) value));
    }
}
