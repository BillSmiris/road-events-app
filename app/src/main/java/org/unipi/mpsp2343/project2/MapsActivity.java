package org.unipi.mpsp2343.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.unipi.mpsp2343.project2.databinding.ActivityMapsBinding;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final @EventType int DEFAULT_EVENT_TYPE = EventType.SUDDEN_BRAKING;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    FirebaseDatabase database;
    DatabaseReference eventsReference;
    List<RoadEvent> eventList;
    Spinner eventTypesSpinner;
    ArrayAdapter<CharSequence> adapter;

    @EventType int selectedType = DEFAULT_EVENT_TYPE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        eventsReference = database.getReference("event");
        eventTypesSpinner = findViewById(R.id.eventTypesSpinner);
        adapter=ArrayAdapter.createFromResource(this, R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        eventTypesSpinner.setAdapter(adapter);

        eventTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        fetchEventsByCategory(DEFAULT_EVENT_TYPE);
    }

    private void fetchEventsByCategory(@EventType int category) {
        eventsReference.orderByChild("type").equalTo(category).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventList = new ArrayList<>();
                if (!dataSnapshot.exists()) {
                    Toast.makeText(MapsActivity.this, getResources().getString(R.string.no_events_found), Toast.LENGTH_LONG).show();
                }
                else {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        RoadEvent event = snapshot.getValue(RoadEvent.class);
                        eventList.add(event);
                    }
                    Toast.makeText(MapsActivity.this, getResources().getString(R.string.fetch_events_success), Toast.LENGTH_LONG).show();
                }
                drawMapMarkers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showMessage(getResources().getString(R.string.error), getResources().getString(R.string.fetch_events_error));
            }
        });
    }

    private void drawMapMarkers(){
        mMap.clear();
        LatLng latLng = new LatLng(0,0);
        boolean rendered = false;
        for(RoadEvent event : eventList) {
            latLng = new LatLng(event.getLat(), event.getLon());
            mMap.addMarker(new MarkerOptions().position(latLng).title(event.getTimestampFormatted()));
            rendered = true;
        }
        if(rendered){
            RoadEvent lastEvent = eventList.get(eventList.size() - 1);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastEvent.getLat(), lastEvent.getLon())));
        }
    }

    private void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }

    public void showMarkers(View view) {
        fetchEventsByCategory(selectedType);
    }
}