package com.app.geofenceapp

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.karumi.dexter.BuildConfig
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.util.*
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), OnMapReadyCallback , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{
    // Task :
    //1. Add or check permission for location
    //2. Check users current location
    //3. add fencing area

    private lateinit var mMap: GoogleMap

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null

    lateinit var geofencingClient: GeofencingClient
    private var geofencingRequest: GeofencingRequest? = null
    private var pendingIntent: PendingIntent? = null
    private var mLocationRequest: LocationRequest? = null

    var mylatitude = 0.0
    var myLongitude = 0.0

    var markers: ArrayList<Marker> = ArrayList()

    //for rotation
    var prevLoc: Location? = null
    var newLoc: Location? = null
    var marker1: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initViews()    }

    private fun initViews() {

        getSupportActionBar()!!.hide()

        //****************************** Check loc reuest ******************************
        checkLocationPermission()

        //******************************  Initialize google client ***********************
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        mGoogleApiClient!!.connect()

        //**************************** Initialize GEOFENCING CLIENT *****************
        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        System.out.println("Map Ready")
    }




    //========================== CHECK LOCATION PERMISSION ==================================
    open fun checkLocationPermission() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
            .withPermission(ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {}
                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        // open device settings when the permission is
                        // denied permanently
                        openSettings()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts(
            "package",
            BuildConfig.APPLICATION_ID, null
        )
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
    //========================== CHECK LOCATION PERMISSION ==================================

    //========================== CONNECTION LISTENER ==================================
    override fun onConnected(p0: Bundle?) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        if (mLocation == null) {
            startLocationUpdates()
        }
        if (mLocation != null) {
            mylatitude = mLocation!!.getLatitude()
            myLongitude = mLocation!!.getLongitude()

            Log.i("Route", "Lat >> $mylatitude $myLongitude")

            mMap.addMarker(
                MarkerOptions().position(LatLng(mylatitude, myLongitude)).icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            )
            val cameraPosition =
                CameraPosition.builder().target(LatLng(mylatitude, myLongitude)).zoom(17f).build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            Log.i("Route", "Route Connected")

        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    Log.i(TAG, "Connection Suspended");
    mGoogleApiClient.connect();

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onLocationChanged(location: Location) {
        if (location != null) {
            Log.e("Locations", location.toString())

            mylatitude = location!!.getLatitude()
            myLongitude = location!!.getLongitude()

            Log.i("Route", "Lat >> $mylatitude $myLongitude")

            if (prevLoc == null) {
                prevLoc = location
                newLoc = location
            } else {
                prevLoc = newLoc
                newLoc = location
            }
            updateMap()
        }
    }


    private fun updateMap() {
        val me = LatLng(mylatitude, myLongitude)

        var bearing = 0f
        if (prevLoc != null && newLoc != null) {
            bearing = prevLoc!!.bearingTo(newLoc)
        }

        //mMap.clear();
       // System.out.println("Markers Size " + markers!!.size())
        markers.clear()
        if (marker1 != null)
            marker1!!.remove()
        marker1 = mMap.addMarker(
            MarkerOptions()
                .position(me)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .flat(true)
                .anchor(0.5f, 0.5f)
                .rotation(bearing)
        )
        markers.add(marker1!!)

    }

    //========================== CONNECTION LISTENER ==================================

    //========================== Geofencing add to dest =================
    /**
     * Create a Geofence list by adding all fences you want to track
     */
    fun createGeofences(latitude: Double, longitude: Double) {
        val id: String = UUID.randomUUID().toString()
        val geoFence = Geofence.Builder()
            .setRequestId(id)
            .setNotificationResponsiveness(1000)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setCircularRegion(latitude, longitude, 200f) // Try changing your radius
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        pendingIntent = getGeofencePendingIntent()

        geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
            .addGeofence(geoFence)
            .build()

        System.out.println("Maps>>>>>>")
        if (!mGoogleApiClient!!.isConnected()) {
            Log.d("Map", "Google API client not connected");
        } else {
            try {
                LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    geofencingRequest,
                    pendingIntent
                ).setResultCallback(object : ResultCallback<Status> {
                    override fun onResult(status: Status) {
                        if (status.isSuccess()) {
                            Log.d("Map", "Successfully Geofencing Connected");
                        } else {
                            Log.d("Map", "Failed to add Geofencing " + status.getStatus());
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
        val intent = Intent(this, GeofenceService::class.java)
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    protected fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(4000)
            .setFastestInterval(4000)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClient,
            mLocationRequest, this
        )
        Log.d("reque", "--->>>>")
    }


}