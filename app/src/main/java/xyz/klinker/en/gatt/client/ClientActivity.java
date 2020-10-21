package xyz.klinker.en.gatt.client;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import xyz.klinker.en.gatt.R;
import xyz.klinker.en.gatt.util.Logger;

public class ClientActivity  extends AppCompatActivity {

    private static final String TAG = "ServerActivity";

    private Logger logger;

    private TextView numberOfDaysLabel;
    private Slider numberOfDaysSlider;
    private TextView numberOfAdvertisementsLabel;
    private Slider numberOfAdvertisementsSlider;
    private TextView sizeOfAdvertisementLabel;
    private Slider sizeOfAdvertisementSlider;
    private TextView mtuSizeLabel;
    private Slider mtuSizeSlider;
    private TextView connectionStatusLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        logger = new Logger(TAG, findViewById(R.id.log));

        numberOfDaysLabel = findViewById(R.id.number_of_days);
        numberOfDaysSlider = findViewById(R.id.number_of_days_slider);
        numberOfAdvertisementsLabel = findViewById(R.id.number_of_advertisements);
        numberOfAdvertisementsSlider = findViewById(R.id.number_of_advertisements_slider);
        sizeOfAdvertisementLabel = findViewById(R.id.size_of_advertisements);
        sizeOfAdvertisementSlider = findViewById(R.id.size_of_advertisements_slider);
        mtuSizeLabel = findViewById(R.id.mtu_size);
        mtuSizeSlider = findViewById(R.id.mtu_size_slider);
        connectionStatusLabel = findViewById(R.id.connection_status);
        initializeSliders();
    }

    public void initiateAdvertisementTransfer(View view) {
        logger.i("Initializing advertisement transfer");
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
}
