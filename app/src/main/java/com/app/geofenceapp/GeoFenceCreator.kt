package com.app.geofenceapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import java.util.*

object GeoFenceCreator {
    private var geofencingRequest: GeofencingRequest? = null
    private var pendingIntent: PendingIntent? = null
    lateinit var context : Context

    /**
     * Create a Geofence list by adding all fences you want to track
     */
     fun createGeofences(
        context: Context,
        latitude: Double,
        longitude: Double,
        radius: Float,
        mGoogleApiClient: GoogleApiClient
    ) {
        this.context = context
        val id: String = UUID.randomUUID().toString()
        val geoFence = Geofence.Builder()
            .setRequestId(id)
            .setNotificationResponsiveness(1000)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setCircularRegion(latitude, longitude, radius) // Try changing your radius
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        pendingIntent = getGeofencePendingIntent()

        geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
            .addGeofence(geoFence)
            .build()

        if (!mGoogleApiClient!!.isConnected()) {
            Log.d("Map", "Google API client not connected");
        } else {
            try {
                LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    geofencingRequest!!,
                    pendingIntent!!
                ).setResultCallback(object : ResultCallback<Status> {
                    override fun onResult(status: Status) {
                        if (status.isSuccess()) {
                            Log.d("Map", "Successfully Geofencing Connected")
                            Toast.makeText(
                                context,
                                "Successfully Geofencing Connected",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Log.d("Map", "Failed to add Geofencing " + status.getStatus())
                            Toast.makeText(context, "Failed to add Geofencing ", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                });
            } catch (e: SecurityException) {
                Log.d("Map", e.message!!)
            }
        }
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        if (pendingIntent != null) {
            return pendingIntent!!
        }

        val intent = Intent(context, MyGeofenceReceiver::class.java)
        //intent.action = "com.app.geofenceapp.CUSTOM_INTENT"
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}