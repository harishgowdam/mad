package com.example.exp_8sdcard_notificationmanager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String CHANNEL_ID = "sd_card_channel";
    private static final int NOTIFICATION_ID = 1;

    private EditText editTextInput;
    private Button btnWriteSDCard;
    private Button btnShowNotification;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextInput = findViewById(R.id.editTextInput);
        btnWriteSDCard = findViewById(R.id.btnWriteSDCard);
        btnShowNotification = findViewById(R.id.btnShowNotification);
        tvStatus = findViewById(R.id.tvStatus);

        createNotificationChannel();

        btnWriteSDCard.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                writeDataToSDCard();
            } else {
                requestStoragePermission();
            }
        });

        btnShowNotification.setOnClickListener(v -> showNotification());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for SD card operations");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Toast.makeText(this, "Please grant Manage External Storage permission in Settings",
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writeDataToSDCard();
            } else {
                Toast.makeText(this, "Permission denied. Cannot write to SD card.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void writeDataToSDCard() {
        String data = editTextInput.getText().toString().trim();
        if (data.isEmpty()) {
            Toast.makeText(this, "Please enter some data first", Toast.LENGTH_SHORT).show();
            return;
        }

        File externalDir = getExternalFilesDir(null);
        if (externalDir == null) {
            tvStatus.setText("Status: External storage not available");
            Toast.makeText(this, "External storage not available", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(externalDir, "sd_card_data.txt");

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.getBytes(StandardCharsets.UTF_8));
            fos.close();

            String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
                    Locale.getDefault()).format(new Date());
            String statusMsg = "Status: Data written successfully at " + timestamp
                    + "\nFile: " + file.getAbsolutePath();
            tvStatus.setText(statusMsg);
            Toast.makeText(this, "Data written to SD card successfully!",
                    Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            tvStatus.setText("Status: Error - " + e.getMessage());
            Toast.makeText(this, "Error writing to SD card: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showNotification() {
        String data = editTextInput.getText().toString().trim();
        String contentText = data.isEmpty() ? "No data entered yet" : "Written: " + data;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("SD Card Operation")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            managerCompat.notify(NOTIFICATION_ID, builder.build());
            Toast.makeText(this, "Notification displayed!", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 200);
        }
    }
}
