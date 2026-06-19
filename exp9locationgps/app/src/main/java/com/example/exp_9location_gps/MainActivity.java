package com.example.exp_9location_gps;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final int ACTIVITY_PERMISSION_REQUEST = 1002;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST = 1003;
    private static final String GEOFENCE_ID = "HOME_GEOFENCE";
    private static final float GEOFENCE_RADIUS = 500f;

    private TextView tvLatitude, tvLongitude, tvLocation, tvGeofenceStatus, tvActivity;
    private Button btnStartTracking, btnStopTracking;
    private Button btnAddGeofence, btnRemoveGeofence;
    private Button btnStartActivity, btnStopActivity;

    private FusedLocationProviderClient fusedLocationClient;
    private GeofencingClient geofencingClient;
    private ActivityRecognitionClient activityRecognitionClient;
    private LocationCallback locationCallback;

    private PendingIntent geofencePendingIntent;
    private PendingIntent activityPendingIntent;

    private boolean isTracking = false;
    private boolean isActivityDetecting = false;

    private BroadcastReceiver geofenceUpdateReceiver;
    private BroadcastReceiver activityUpdateReceiver;

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

        initViews();
        initClients();
        setupCallbacks();
        registerGeofenceReceiver();
        registerActivityReceiver();
    }

    private void initViews() {
        tvLatitude = findViewById(R.id.tv_latitude);
        tvLongitude = findViewById(R.id.tv_longitude);
        tvLocation = findViewById(R.id.tv_location);
        tvGeofenceStatus = findViewById(R.id.tv_geofence_status);
        tvActivity = findViewById(R.id.tv_activity);

        btnStartTracking = findViewById(R.id.btn_start_tracking);
        btnStopTracking = findViewById(R.id.btn_stop_tracking);
        btnAddGeofence = findViewById(R.id.btn_add_geofence);
        btnRemoveGeofence = findViewById(R.id.btn_remove_geofence);
        btnStartActivity = findViewById(R.id.btn_start_activity);
        btnStopActivity = findViewById(R.id.btn_stop_activity);

        btnStartTracking.setOnClickListener(v -> startGpsTracking());
        btnStopTracking.setOnClickListener(v -> stopGpsTracking());
        btnAddGeofence.setOnClickListener(v -> addGeofence());
        btnRemoveGeofence.setOnClickListener(v -> removeGeofence());
        btnStartActivity.setOnClickListener(v -> startActivityDetection());
        btnStopActivity.setOnClickListener(v -> stopActivityDetection());
    }

    private void initClients() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        activityRecognitionClient = ActivityRecognition.getClient(this);
    }

    private void setupCallbacks() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    updateLocationUI(location);
                }
            }
        };
    }

    private void registerGeofenceReceiver() {
        geofenceUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("message");
                if (message != null) {
                    tvGeofenceStatus.setText(getString(R.string.geofence_status) + " " + message);
                }
            }
        };
        IntentFilter filter = new IntentFilter("GEOFENCE_UPDATE");
        registerReceiver(geofenceUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    private void registerActivityReceiver() {
        activityUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String activity = intent.getStringExtra("activity");
                int confidence = intent.getIntExtra("confidence", 0);
                if (activity != null) {
                    tvActivity.setText(getString(R.string.current_activity) + " " +
                            activity + " (" + confidence + "%)");
                }
            }
        };
        IntentFilter filter = new IntentFilter("ACTIVITY_UPDATE");
        registerReceiver(activityUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    // ==================== GPS TRACKING ====================

    private void startGpsTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateDistanceMeters(1)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, getMainLooper());

        isTracking = true;
        btnStartTracking.setEnabled(false);
        btnStopTracking.setEnabled(true);
        Toast.makeText(this, "GPS tracking started", Toast.LENGTH_SHORT).show();
    }

    private void stopGpsTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        isTracking = false;
        btnStartTracking.setEnabled(true);
        btnStopTracking.setEnabled(false);
        Toast.makeText(this, "GPS tracking stopped", Toast.LENGTH_SHORT).show();
    }

    private void updateLocationUI(Location location) {
        tvLatitude.setText(getString(R.string.latitude) + " " + String.format("%.6f", location.getLatitude()));
        tvLongitude.setText(getString(R.string.longitude) + " " + String.format("%.6f", location.getLongitude()));
        tvLocation.setText(getString(R.string.current_location) + " Acc: " + String.format("%.1fm", location.getAccuracy()));
    }

    // ==================== GEOFENCING ====================

    private void addGeofence() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    BACKGROUND_LOCATION_PERMISSION_REQUEST);
            return;
        }

        // Use last known location or default
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location == null) {
                Toast.makeText(this, "Cannot get current location. Start GPS tracking first.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            createGeofence(location.getLatitude(), location.getLongitude());
        });
    }

    private void createGeofence(double latitude, double longitude) {
        Geofence geofence = new Geofence.Builder()
                .setRequestId(GEOFENCE_ID)
                .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT |
                        Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(10000)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        geofencePendingIntent = getGeofencePendingIntent();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener(this, unused -> {
                    tvGeofenceStatus.setText(getString(R.string.geofence_status) + " " +
                            getString(R.string.geofence_added));
                    btnAddGeofence.setEnabled(false);
                    btnRemoveGeofence.setEnabled(true);
                    Toast.makeText(this, R.string.geofence_added, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, "Failed to add geofence: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void removeGeofence() {
        List<String> geofenceIds = new ArrayList<>();
        geofenceIds.add(GEOFENCE_ID);

        geofencingClient.removeGeofences(geofenceIds)
                .addOnSuccessListener(this, unused -> {
                    tvGeofenceStatus.setText(getString(R.string.geofence_status) + " None");
                    btnAddGeofence.setEnabled(true);
                    btnRemoveGeofence.setEnabled(false);
                    Toast.makeText(this, R.string.geofence_removed, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, "Failed to remove geofence: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        return geofencePendingIntent;
    }

    // ==================== ACTIVITY RECOGNITION ====================

    private void startActivityDetection() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    ACTIVITY_PERMISSION_REQUEST);
            return;
        }

        activityPendingIntent = getActivityPendingIntent();

        Task<Void> task = activityRecognitionClient.requestActivityUpdates(
                5000, activityPendingIntent);

        task.addOnSuccessListener(this, unused -> {
            isActivityDetecting = true;
            btnStartActivity.setEnabled(false);
            btnStopActivity.setEnabled(true);
            Toast.makeText(this, "Activity detection started", Toast.LENGTH_SHORT).show();
        });

        task.addOnFailureListener(this, e -> {
            Toast.makeText(this, "Failed to start activity detection: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void stopActivityDetection() {
        activityPendingIntent = getActivityPendingIntent();

        Task<Void> task = activityRecognitionClient.removeActivityUpdates(activityPendingIntent);

        task.addOnSuccessListener(this, unused -> {
            isActivityDetecting = false;
            btnStartActivity.setEnabled(true);
            btnStopActivity.setEnabled(false);
            tvActivity.setText(getString(R.string.current_activity) + " Unknown");
            Toast.makeText(this, "Activity detection stopped", Toast.LENGTH_SHORT).show();
        });

        task.addOnFailureListener(this, e -> {
            Toast.makeText(this, "Failed to stop activity detection: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private PendingIntent getActivityPendingIntent() {
        if (activityPendingIntent != null) {
            return activityPendingIntent;
        }
        Intent intent = new Intent(this, ActivityRecognitionReceiver.class);
        activityPendingIntent = PendingIntent.getBroadcast(this, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        return activityPendingIntent;
    }

    // ==================== PERMISSION HANDLING ====================

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGpsTracking();
                } else {
                    showPermissionDialog(R.string.location_permission_message);
                }
                break;

            case BACKGROUND_LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addGeofence();
                } else {
                    showPermissionDialog(R.string.location_permission_message);
                }
                break;

            case ACTIVITY_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityDetection();
                } else {
                    showPermissionDialog(R.string.activity_permission_message);
                }
                break;
        }
    }

    private void showPermissionDialog(int messageId) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(messageId)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTracking) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (geofenceUpdateReceiver != null) {
            unregisterReceiver(geofenceUpdateReceiver);
        }
        if (activityUpdateReceiver != null) {
            unregisterReceiver(activityUpdateReceiver);
        }
    }
}
