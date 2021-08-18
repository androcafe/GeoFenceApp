package com.app.geofenceapp

import com.google.android.gms.maps.model.LatLng


object Constants {
    //Location
    const val GEOFENCE_ID = "GEO_ID"
    const val GEOFENCE_RADIUS_IN_METERS = 100.00

    /**
     * Map for storing information
     */
    val AREA_LANDMARKS = HashMap<String, LatLng>()

    init {
        // Tacme
        AREA_LANDMARKS[GEOFENCE_ID] = LatLng(18.4589975 ,73.8594186)
    }
}