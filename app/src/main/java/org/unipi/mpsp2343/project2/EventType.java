package org.unipi.mpsp2343.project2;

import androidx.annotation.IntDef;

@IntDef({EventType.SUDDEN_BRAKING, EventType.SUDDEN_ACCELERATION, EventType.SPEED_LIMIT_VIOLATION, EventType.SEVERE_ROAD_BUMP})
public @interface EventType {
    int SUDDEN_BRAKING = 0;
    int SUDDEN_ACCELERATION = 1;
    int SPEED_LIMIT_VIOLATION = 2;
    int SEVERE_ROAD_BUMP = 3;
}