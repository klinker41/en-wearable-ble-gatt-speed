package xyz.klinker.en.gatt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import xyz.klinker.en.gatt.client.ClientActivity;
import xyz.klinker.en.gatt.server.ServerActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launchServer(View view) {
        startActivity(new Intent(this, ServerActivity.class));
    }

    public void launchClient(View view) {
        startActivity(new Intent(this, ClientActivity.class));
    }
}
