package xyz.klinker.en.gatt.client;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.Slider;

import xyz.klinker.en.gatt.R;
import xyz.klinker.en.gatt.util.Logger;
import xyz.klinker.en.gatt.util.Scanner;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ClientActivity  extends AppCompatActivity implements Scanner.ScannerCallback {

    private static final String TAG = "ServerActivity";
    private static final int PERMISSION_REQUEST_CODE = 1234;

    private Logger logger;
    private Scanner scanner;

    private TextView numberOfDaysLabel;
    private Slider numberOfDaysSlider;
    private TextView numberOfAdvertisementsLabel;
    private Slider numberOfAdvertisementsSlider;
    private TextView sizeOfAdvertisementLabel;
    private Slider sizeOfAdvertisementSlider;
    private TextView mtuSizeLabel;
    private Slider mtuSizeSlider;
    private TextView connectionStatusLabel;
    private Button initiateTransferButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        logger = new Logger(TAG, findViewById(R.id.log));
        scanner = new Scanner(this);

        numberOfDaysLabel = findViewById(R.id.number_of_days);
        numberOfDaysSlider = findViewById(R.id.number_of_days_slider);
        numberOfAdvertisementsLabel = findViewById(R.id.number_of_advertisements);
        numberOfAdvertisementsSlider = findViewById(R.id.number_of_advertisements_slider);
        sizeOfAdvertisementLabel = findViewById(R.id.size_of_advertisements);
        sizeOfAdvertisementSlider = findViewById(R.id.size_of_advertisements_slider);
        mtuSizeLabel = findViewById(R.id.mtu_size);
        mtuSizeSlider = findViewById(R.id.mtu_size_slider);
        connectionStatusLabel = findViewById(R.id.connection_status);
        initiateTransferButton = findViewById(R.id.initiate_transfer);
        initializeSliders();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        requestPermissionOrStartScanning();
    }

    @Override
    public void onStart() {
        super.onStart();
        scanner.beginScanning(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        scanner.stopScanning();
    }

    public void initiateAdvertisementTransfer(View view) {
        logger.i("Initializing advertisement transfer");
    }

    @Override
    public void onScanningStarted() {
        logger.i("Successfully started scanning");
    }

    @Override
    public void onScanningFailed(int errorCode) {
        logger.e("Failed to start scanning: " + errorCode);
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        logger.i("Scanned compatible device: " + device);
    }

    @Override
    public void onScanningStopped() {
        logger.i("Successfully stopped scanning");
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
        attachSliderListener(numberOfDaysSlider, numberOfDaysLabel);
        attachSliderListener(numberOfAdvertisementsSlider, numberOfAdvertisementsLabel);
        attachSliderListener(sizeOfAdvertisementSlider, sizeOfAdvertisementLabel);
        attachSliderListener(mtuSizeSlider, mtuSizeLabel);
    }

    private void attachSliderListener(Slider slider, TextView label) {
        slider.addOnChangeListener(
                ((s, value, fromUser) -> setLabelValue(label, slider.getValue())));
        setLabelValue(label, slider.getValue());
    }

    private void setLabelValue(TextView label, float value) {
        label.setText(Integer.toString((int) value));
    }

    private void requestPermissionOrStartScanning() {
        if (hasLocationPermission()) {
            scanner.beginScanning(this);
        } else {
            requestLocationPermission();
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
    }
}
