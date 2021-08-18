package com.app.geofenceapp

import android.app.*
import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class GeofenceService : IntentService("GeofenceService") {

    var TAG = "GeoIntentService";


    override fun onHandleIntent(intent: Intent?) {
        var geofencingEvent = GeofencingEvent.fromIntent(intent) as GeofencingEvent
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "GeofencingEvent error " + geofencingEvent.getErrorCode())
        } else {
            var transaction = geofencingEvent.getGeofenceTransition()
            var geofences = geofencingEvent.getTriggeringGeofences()
            var geofence = geofences.get(0)
            if (transaction == Geofence.GEOFENCE_TRANSITION_ENTER && geofence.getRequestId().equals(Constants.GEOFENCE_ID)) {
                Log.d(TAG, "You are inside of desired area");
            } else {
                Log.d(TAG, "You are outside of desired area");
            }
            var geofenceTransitionDetails = getGeofenceTrasitionDetails(transaction, geofences) as String

            sendNotification( geofenceTransitionDetails );
        }
    }

    private fun sendNotification(geofenceTransitionDetails: String) {
        Log.i(TAG, "sendNotification: " + geofenceTransitionDetails )

        // Intent to start the main Activity
        val intent = Intent(this, MapsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* Request code */,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val CHANNEL_ID = "CHANNEL_ID"

        if (Build.VERSION.SDK_INT >= 26) {  // Build.VERSION_CODES.O
            val mChannel =
                NotificationChannel(CHANNEL_ID, "CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(mChannel)
        }

        notificationManager.notify(
            0,
            createNotification(geofenceTransitionDetails, pendingIntent));
    }

    private fun getGeofenceTrasitionDetails(geoFenceTransition: Int, geofences: List<Geofence>): Any {
        // get the ID of each geofence triggered
        var triggeringGeofencesList =  ArrayList<String>();
        for (i in 0..geofences.size-1) {
            triggeringGeofencesList.add( geofences.get(i).requestId)
        }

        var status: String? = null
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
            status = "Entering ";
        else if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
            status = "Exiting ";
        return status + TextUtils.join( ", ", triggeringGeofencesList);
    }

    // Create a notification

    fun createNotification(msg:String, notificationPendingIntent: PendingIntent): Notification {
        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var notificationBuilder =  NotificationCompat.Builder(this)
        notificationBuilder
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setColor(Color.RED)
            .setContentTitle(msg)
            .setContentText("Geofence Notification!")
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(false)
            .setSound(defaultSoundUri)
            .setDefaults(Notification.DEFAULT_ALL)
        return notificationBuilder.build()
    }
}
