package org.unipi.mpsp2343.project2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    //<editor-fold desc="Constants">
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final float DECELERATION_THRESHOLD = -10.0f;
    private static final float ACCELERATION_THRESHOLD = 20.0f;
    private static final float SPEED_LIMIT = 100.0f;
    private static final float BUMP_THRESHOLD = 30.0f;
    private static final int VIBRATION_PERIOD = 200;
    //</editor-fold>

    //<editor-fold desc="Service variables">
    FirebaseAuth auth;
    LocationManager locationManager;
    LocationListener locationListener;
    Vibrator vibrator;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;
    FirebaseDatabase database;
    DatabaseReference eventsReference;
    //</editor-fold>

    //<editor-fold desc="UI Element Variables">
    TextView accelerationText;
    TextView speedText;
    Button toggleButton;
    //</editor-fold

    //<editor-fold desc="State variables">
    boolean metricsEnabled = false;
    boolean isInsideBrakingEvent = false;
    boolean isInsideAccelerationEvent = false;
    boolean isInsideSpeedLimitEvent = false;
    boolean isInsideBumpEvent = false;
    float acceleration = 0f;
    Long currentMils = 0L;
    float currentSpeed = 0f;
    Long previousMils = 0L;
    float previousSpeed = 0f;
    Location lastReceivedLocation;
    //</editor-fold>

    //<editor-fold desc="Lifecycle functions">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //<editor-fold desc="App specific code">a
        auth = FirebaseAuth.getInstance();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        locationListener = this::getGPSMetrics;

        accelerationText = findViewById(R.id.accelerationText);
        accelerationText.setText(getString(R.string.acceleration_value, acceleration));
        speedText = findViewById(R.id.speedText);
        speedText.setText(getString(R.string.speed_value, currentSpeed));
        toggleButton = findViewById((R.id.toggleButton));
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                detectBump(sensorEvent);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        database = FirebaseDatabase.getInstance();
        eventsReference = database.getReference("event");
        eventsReference.push().setValue(new RoadEvent(204.3, 39.78, 0, EventType.SUDDEN_BRAKING)).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, getResources().getString(R.string.save_event_error), Toast.LENGTH_LONG).show();
            }
        });
        //</editor-fold>
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getCurrentUser() == null){
            goToMainActivity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(metricsEnabled){
            gpsOn();
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(metricsEnabled) {
            gpsOff();
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(sensorEventListener);
    }
    //</editor-fold>

    //<editor-fold desc="View flow management">
    public void signOut(View view) {
        auth.signOut();
        Toast.makeText(this, getResources().getString(R.string.signout_ok), Toast.LENGTH_LONG).show();
        goToMainActivity();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void openMap(View view) {
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }
    //</editor-fold>

    //<editor-fold desc="Sensor management">
    public void toggleMetrics(View view){
        if(metricsEnabled){
            gpsOff();
            toggleButton.setText(getString(R.string.enable));
            accelerationText.setText(getString(R.string.acceleration_value, acceleration));
            speedText.setText(getString(R.string.speed_value, currentSpeed));
            metricsEnabled = false;
            sensorManager.unregisterListener(sensorEventListener);
            return;
        }
        if(gpsOn()) {
            sensorManager.registerListener(sensorEventListener,sensor, SensorManager.SENSOR_DELAY_UI);
            toggleButton.setText(getString(R.string.disable));
            metricsEnabled = true;
        }
    }

    private boolean gpsOn() {
        if(!metricsEnabled) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
                return false;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);
            return true;
        }
        return false;
    }

    private void gpsOff(){
        if(metricsEnabled){
            locationManager.removeUpdates(locationListener);
            currentSpeed = 0.0f;
            previousSpeed = 0.0f;
            acceleration = 0.0f;
            accelerationText.setText(getString(R.string.acceleration_value, acceleration));
            speedText.setText(getString(R.string.speed_value, currentSpeed));
        }
    }
    //</editor-fold>

    //<editor-fold desc="Event detection">
    public void getGPSMetrics(Location location) {
        lastReceivedLocation = location;
        currentMils = location.getTime();
        currentSpeed = location.getSpeed();
        acceleration = calculateAcceleration();
        accelerationText.setText(getString(R.string.acceleration_value, acceleration));
        speedText.setText(getString(R.string.speed_value, currentSpeed));
        detectBrakingEvent(location);
        detectSuddenAccelerationEvent(location);
        detectSpeedLimitViolationEvent(location);
        previousMils = currentMils;
        previousSpeed = currentSpeed;
    }

    private float calculateAcceleration() {
        float deltaTimeMils = currentMils - previousMils;
        float deltaTimeSeconds = deltaTimeMils / 1000.0f;
        float deltaVelocity = currentSpeed - previousSpeed;

        return deltaVelocity - deltaTimeSeconds;
    }

    private void detectBrakingEvent(Location location) {
        if(!isInsideBrakingEvent) {
            if(acceleration <= DECELERATION_THRESHOLD) {
                accelerationText.setTextColor(ContextCompat.getColor(this, R.color.red));
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_PERIOD, VibrationEffect.DEFAULT_AMPLITUDE));
                Toast.makeText(this, getResources().getString(R.string.braking_detection_toast_message), Toast.LENGTH_LONG).show();
                saveEvent(location, EventType.SUDDEN_BRAKING);
                isInsideBrakingEvent = true;
            }
        }
        else {
            if(acceleration > DECELERATION_THRESHOLD) {
                accelerationText.setTextColor(ContextCompat.getColor(this, R.color.white));
                isInsideBrakingEvent = false;
            }
        }
    }

    private void detectSuddenAccelerationEvent(Location location) {
        if(!isInsideAccelerationEvent) {
            if(acceleration >= ACCELERATION_THRESHOLD) {
                accelerationText.setTextColor(ContextCompat.getColor(this, R.color.red));
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_PERIOD, VibrationEffect.DEFAULT_AMPLITUDE));
                Toast.makeText(this, getResources().getString(R.string.acceleration_detection_toast_message), Toast.LENGTH_LONG).show();
                saveEvent(location, EventType.SUDDEN_ACCELERATION);
                isInsideAccelerationEvent = true;
            }
        }
        else {
            if(acceleration < ACCELERATION_THRESHOLD) {
                accelerationText.setTextColor(ContextCompat.getColor(this, R.color.white));
                isInsideAccelerationEvent = false;
            }
        }
    }

    private void detectSpeedLimitViolationEvent(Location location) {
        if(!isInsideSpeedLimitEvent) {
            if(currentSpeed > SPEED_LIMIT) {
                speedText.setTextColor(ContextCompat.getColor(this, R.color.red));
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_PERIOD, VibrationEffect.DEFAULT_AMPLITUDE));
                Toast.makeText(this, getResources().getString(R.string.speed_limit_violation_detection_toast_message), Toast.LENGTH_LONG).show();
                saveEvent(location, EventType.SPEED_LIMIT_VIOLATION);
                isInsideSpeedLimitEvent = true;
            }
        }
        else {
            if(acceleration <= SPEED_LIMIT) {
                speedText.setTextColor(ContextCompat.getColor(this, R.color.white));
                isInsideSpeedLimitEvent = false;
            }
        }
    }

    private void detectBump(SensorEvent sensorEvent) {
        if (lastReceivedLocation == null)
            return;
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        double magnitude = Math.sqrt(x * x + y * y + z * z);

        if(!isInsideBumpEvent) {
            if(magnitude > BUMP_THRESHOLD) {
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_PERIOD, VibrationEffect.DEFAULT_AMPLITUDE));
                Toast.makeText(this, getResources().getString(R.string.road_bump_detection_toast_message), Toast.LENGTH_LONG).show();
                saveEvent(lastReceivedLocation, EventType.SEVERE_ROAD_BUMP);
                isInsideBumpEvent = true;
            }
        }
        else {
            if(magnitude <= BUMP_THRESHOLD) {
                isInsideBumpEvent = false;
            }
        }
    }
    //</editor-fold>

    private void saveEvent(Location location, @EventType int type){
        double lon = location.getLongitude();
        double lat = location.getLatitude();
        long timestamp = location.getTime();

        RoadEvent newEvent = new RoadEvent(lon, lat, timestamp, type);
        eventsReference.push().setValue(newEvent).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, getResources().getString(R.string.save_event_error), Toast.LENGTH_LONG).show();
            }
        });
    }
}