package xyz.klinker.en.gatt.client;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.Slider;

import xyz.klinker.en.gatt.R;
import xyz.klinker.en.gatt.util.GattQueue;
import xyz.klinker.en.gatt.util.Logger;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ClientActivity  extends AppCompatActivity implements Scanner.ScannerCallback {

    private static final String TAG = "ServerActivity";
    private static final int PERMISSION_REQUEST_CODE = 1234;

    private Logger logger;
    private GattQueue gattQueue;
    private Scanner scanner;
    private AdvertisementGenerator generator;
    private AdvertisementSyncer syncer;

    private TextView numberOfDaysLabel;
    private Slider numberOfDaysSlider;
    private TextView numberOfAdvertisementsLabel;
    private Slider numberOfAdvertisementsSlider;
    private TextView sizeOfAdvertisementLabel;
    private Slider sizeOfAdvertisementSlider;
    private TextView mtuSizeLabel;
    private Slider mtuSizeSlider;
    private TextView connectionStatusLabel;
    private Button transferAdvertisements;
    private Button transferScans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        logger = new Logger(this, TAG, findViewById(R.id.log));
        gattQueue = new GattQueue(logger);
        scanner = new Scanner(this, gattQueue);
        generator = new AdvertisementGenerator();
        syncer = new AdvertisementSyncer(this, gattQueue);

        numberOfDaysLabel = findViewById(R.id.number_of_days);
        numberOfDaysSlider = findViewById(R.id.number_of_days_slider);
        numberOfAdvertisementsLabel = findViewById(R.id.number_of_advertisements);
        numberOfAdvertisementsSlider = findViewById(R.id.number_of_advertisements_slider);
        sizeOfAdvertisementLabel = findViewById(R.id.size_of_advertisements);
        sizeOfAdvertisementSlider = findViewById(R.id.size_of_advertisements_slider);
        mtuSizeLabel = findViewById(R.id.mtu_size);
        mtuSizeSlider = findViewById(R.id.mtu_size_slider);
        connectionStatusLabel = findViewById(R.id.connection_status);
        transferAdvertisements = findViewById(R.id.transfer_advertisements);
        transferScans = findViewById(R.id.transfer_scans);
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
        new Thread(() ->
            syncer.sendRpis(
                    generator.generateAdvertisements(
                            (int) numberOfDaysSlider.getValue(),
                            (int) numberOfAdvertisementsSlider.getValue(),
                            (int) sizeOfAdvertisementSlider.getValue()),
                    logger))
                .start();
    }

    public void initiateScanRecordTransfer(View view) {
        logger.i("Requesting scan record transfer");
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
    public void onGattConnecting(BluetoothDevice device) {
        logger.i("Starting GATT connection: " + device);
    }

    @Override
    public void onGattConnected(BluetoothDevice device, BluetoothGatt gatt) {
        logger.i("GATT connected: " + device);
        gatt.requestMtu((int) mtuSizeSlider.getValue());
    }

    @Override
    public void onGattMtuChanged(BluetoothDevice device, int mtu) {
        logger.i("GATT MTU changed: " + mtu);
    }

    @Override
    public void onGattServicesDiscovered(BluetoothDevice device, BluetoothGatt gatt) {
        logger.i("GATT services discovered");
        syncer.setGatt(gatt, (int) mtuSizeSlider.getValue());
        runOnUiThread(() -> {
            connectionStatusLabel.setText(R.string.connection_status_connected);
            transferAdvertisements.setEnabled(true);
            transferScans.setEnabled(true);
            mtuSizeSlider.setEnabled(false);
        });
    }

    @Override
    public void onGattOperation(BluetoothDevice device, String operation, String value) {
        logger.v("GATT: " + device + ", " + operation + ", " + value);
    }

    @Override
    public void onGattDisconnected(BluetoothDevice device) {
        logger.i("GATT disconnected: " + device);
        runOnUiThread(() -> {
            connectionStatusLabel.setText(R.string.connection_status_disconnected);
            transferAdvertisements.setEnabled(false);
            transferScans.setEnabled(false);
            mtuSizeSlider.setEnabled(true);
        });
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
