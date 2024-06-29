package org.unipi.mpsp2343.project2;

import android.icu.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

public class RoadEvent {
    double lon;
    double lat;
    long timestamp;
    @EventType int type;

    public RoadEvent() {
    }

    public RoadEvent(double lon, double lat, long timestamp,  @EventType int type) {
        this.lon = lon;
        this.lat = lat;
        this.timestamp = timestamp;
        this.type = type;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public @EventType int getType() {
        return type;
    }

    public void setType(@EventType int type) {
        this.type = type;
    }

    public String getTimestampFormatted() {
        Date date = new Date(this.timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }
}
