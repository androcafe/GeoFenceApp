package com.app.geofenceapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class MyGeofenceReceiver : BroadcastReceiver() {

    var TAG = "BroadcastReceiver"
    val GEOFENCE_ID = "GEO_ID"
    lateinit var context: Context

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context

        System.out.println("Yes")

        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        var geofencingEvent = GeofencingEvent.fromIntent(intent) as GeofencingEvent
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "GeofencingEvent error " + geofencingEvent.getErrorCode())
        } else {
            var transaction = geofencingEvent.getGeofenceTransition()
            var geofences = geofencingEvent.getTriggeringGeofences()
            var geofence = geofences.get(0)
            if (transaction == Geofence.GEOFENCE_TRANSITION_ENTER && geofence.getRequestId().equals(GEOFENCE_ID)) {
                Log.d(TAG, "You are inside of desired area");
            } else {
                Log.d(TAG, "You are outside of desired area");
            }
            var geofenceTransitionDetails = getGeofenceTrasitionDetails(transaction, geofences) as String
            System.out.println("Yess")
            sendNotification( geofenceTransitionDetails );
        }
    }

    private fun getGeofenceTrasitionDetails(geoFenceTransition: Int, geofences: List<Geofence>): String {
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

    private fun sendNotification(geofenceTransitionDetails: String) {
        Log.i(TAG, "sendNotification: " + geofenceTransitionDetails )

        // Intent to start the main Activity
        val intent = Intent(context, MapsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0 /* Request code */,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val CHANNEL_ID = "CHANNEL_ID"

        if (Build.VERSION.SDK_INT >= 26) {  // Build.VERSION_CODES.O
            val mChannel =
                NotificationChannel(CHANNEL_ID, "CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(mChannel)
        }

        val notification = NotificationCompat.Builder(context,
            "CHANNEL_ID")
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("Geofence Notification! \n"+geofenceTransitionDetails)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setChannelId("CHANNEL_ID")
            .setContentIntent(pendingIntent)
            .build()
        notificationManager?.notify(100, notification)
    }

}
