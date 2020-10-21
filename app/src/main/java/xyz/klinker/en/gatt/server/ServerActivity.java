package xyz.klinker.en.gatt.server;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import xyz.klinker.en.gatt.R;
import xyz.klinker.en.gatt.util.Logger;

public class ServerActivity  extends AppCompatActivity {

    private static final String TAG = "ServerActivity";

    private Logger logger;
    private TextView numberOfScanRecordsLabel;
    private Slider numberOfScanRecordsSlider;
    private TextView sizeOfScanRecordsLabel;
    private Slider sizeOfScanRecordsSlider;
    private TextView connectionStatusLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        logger = new Logger(TAG, findViewById(R.id.log));

        numberOfScanRecordsLabel = findViewById(R.id.number_scan_records);
        numberOfScanRecordsSlider = findViewById(R.id.number_scan_records_slider);
        sizeOfScanRecordsLabel = findViewById(R.id.scan_record_size);
        sizeOfScanRecordsSlider = findViewById(R.id.scan_record_size_slider);
        connectionStatusLabel = findViewById(R.id.connection_status);
        initializeSliders();
    }

    public void initiateScanRecordTransfer(View view) {
        logger.i("Initializing scan record transfer");
    }

    private void initializeSliders() {
        numberOfScanRecordsSlider
                .addOnChangeListener(((slider, value, fromUser) ->
                        setLabelValue(numberOfScanRecordsLabel, slider.getValue())));
        sizeOfScanRecordsSlider
                .addOnChangeListener(((slider, value, fromUser) ->
                        setLabelValue(sizeOfScanRecordsLabel, slider.getValue())));
        setLabelValue(numberOfScanRecordsLabel, numberOfScanRecordsSlider.getValue());
        setLabelValue(sizeOfScanRecordsLabel, sizeOfScanRecordsSlider.getValue());
    }

    private void setLabelValue(TextView label, float value) {
        label.setText(Integer.toString((int) value));
    }
}
