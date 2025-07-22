package com.example.mysms;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import com.example.mysms.SmsReceiver;

import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_SMS_PERMISSION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForPermissions();
    }

    private void askForPermissions() {
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS,
                            Manifest.permission.READ_PHONE_STATE
                    },
                    REQUEST_SMS_PERMISSION);
        } else {
            Toast.makeText(this, "All permissions already granted", Toast.LENGTH_SHORT).show();

            Button testButton = findViewById(R.id.test_button);
            testButton.setOnClickListener(v -> {
                SmsReceiver.sendToServer(this, "+1234567890", "Test message from MainActivity");
            });
        }
    }
}
