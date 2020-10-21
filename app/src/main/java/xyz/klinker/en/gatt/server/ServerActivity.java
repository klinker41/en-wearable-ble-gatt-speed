package xyz.klinker.en.gatt.server;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import xyz.klinker.en.gatt.R;
import xyz.klinker.en.gatt.util.Advertiser;
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
    private Button initiateTransferButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        logger = new Logger(TAG, findViewById(R.id.log));
        advertiser = new Advertiser(this);

        numberOfScanRecordsLabel = findViewById(R.id.number_scan_records);
        numberOfScanRecordsSlider = findViewById(R.id.number_scan_records_slider);
        sizeOfScanRecordsLabel = findViewById(R.id.scan_record_size);
        sizeOfScanRecordsSlider = findViewById(R.id.scan_record_size_slider);
        connectionStatusLabel = findViewById(R.id.connection_status);
        initiateTransferButton = findViewById(R.id.initiate_transfer);
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

    public void initiateScanRecordTransfer(View view) {
        logger.i("Initializing scan record transfer");
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
    public void onGattConnected(BluetoothDevice device) {
        logger.i("GATT connected: " + device);
        connectionStatusLabel.setText(R.string.connection_status_connected);
        initiateTransferButton.setEnabled(true);
    }

    @Override
    public void onGattDisconnected(BluetoothDevice device) {
        logger.i("GATT disconnected: " + device);
        connectionStatusLabel.setText(R.string.connection_status_disconnected);
        initiateTransferButton.setEnabled(false);
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
