package com.app.geofenceapp

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.app.geofenceapp.model.SelectedLoc
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.collections.ArrayList


public class MapsActivity : BaseActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener{
    @BindView(R.id.bottom_sheet)
    lateinit var layoutBottomSheet: LinearLayout

    @BindView(R.id.edtLat)
    lateinit var edtLat: EditText

    @BindView(R.id.edtLong)
    lateinit var edtLong: EditText

    @BindView(R.id.edtRadius)
    lateinit var edtRadius: EditText

    private lateinit var mMap: GoogleMap
    //**************** UI COMPONENTS **********************

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null

    lateinit var geofencingClient: GeofencingClient
    private var mLocationRequest: LocationRequest? = null

    var mylatitude = 0.0
    var myLongitude = 0.0

    var markers: ArrayList<Marker> = ArrayList()

    //for rotation
    var prevLoc: Location? = null
    var newLoc: Location? = null
    var marker1: Marker? = null

    //ARRAYLIST
    var geofenceList : ArrayList<SelectedLoc> = ArrayList()
    var sheetBehavior: BottomSheetBehavior<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        ButterKnife.bind(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initViews()
    }

    private fun initViews() {

        getSupportActionBar()!!.hide()

        //****************************** Check loc reuest ******************************
        checkLocationPermission()

        //******************************  Initialize google client ***********************
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            //.addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        mGoogleApiClient!!.connect()

        //**************************** Initialize GEOFENCING CLIENT *****************
        geofencingClient = LocationServices.getGeofencingClient(this)

        setBottomNavigation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.getUiSettings().setZoomControlsEnabled(true)
        if(checkValidPermission())
        {
            mMap.isMyLocationEnabled = true
        }
    }

    //========================== CONNECTION LISTENER ==================================
    override fun onConnected(p0: Bundle?) {
        if(checkValidPermission())
        {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient!!)
            startLocationUpdates()
            if (mLocation != null) {
                mylatitude = mLocation!!.getLatitude()
                myLongitude = mLocation!!.getLongitude()

                Log.i("Route", "Lat >> $mylatitude $myLongitude")

                marker1 = mMap.addMarker(
                    MarkerOptions().position(LatLng(mylatitude, myLongitude)).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                )
                markers.add(marker1!!)

                val cameraPosition =
                    CameraPosition.builder().target(LatLng(mylatitude, myLongitude)).zoom(17f).build()
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                Log.i("Route", "Route Connected")

            } else {
                Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {

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

    protected fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000)
            .setFastestInterval(10000)

        // Request location updates
       if(checkValidPermission())
        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClient!!,
            mLocationRequest, this
        )
    }


    private fun setBottomNavigation() {
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        sheetBehavior!!.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(@NonNull bottomSheet: View, slideOffset : Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN ->{}
                    BottomSheetBehavior.STATE_EXPANDED ->{}
                    BottomSheetBehavior.STATE_COLLAPSED ->{}
                    BottomSheetBehavior.STATE_DRAGGING ->{}
                    BottomSheetBehavior.STATE_SETTLING ->{}

                }
            }
        })
    }


    @OnClick(R.id.btnAdd)
    protected fun onClickAdd()
    {

        if(TextUtils.isEmpty(edtLat!!.text.toString()))
            edtLat!!.setError(resources.getString(R.string.enter_laitude))
        else if(TextUtils.isEmpty(edtLong!!.text.toString()))
            edtLong!!.setError(resources.getString(R.string.enter_longitude))
        else if(TextUtils.isEmpty(edtRadius!!.text.toString()))
            edtRadius!!.setError(resources.getString(R.string.enter_radius))
        else{
            GeoFenceCreator.createGeofences(this,edtLat!!.text.toString().toDouble(),edtLong!!.text.toString().toDouble(),edtRadius!!.text.toString().toFloat(),mGoogleApiClient!!)

            var selectedLoc = SelectedLoc(edtLat!!.text.toString().toDouble(),edtLong!!.text.toString().toDouble(),edtRadius!!.text.toString().toFloat())
            geofenceList.add(selectedLoc)

            addGeoFenceToMap(edtLat!!.text.toString().toDouble(),edtLong!!.text.toString().toDouble(),edtRadius!!.text.toString().toDouble())

            edtLat!!.setText("")
            edtLong!!.setText("")
            edtRadius!!.setText("")
        }
    }

    private fun addGeoFenceToMap(lat: Double, lng: Double, radius: Double) {
        val newGeoFence = LatLng(lat, lng)

        val circle: Circle = mMap.addCircle(
            CircleOptions()
                .center(LatLng(lat, lng))
                .radius(100.0)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
        )

        mMap.addMarker(
            MarkerOptions()
                .position(newGeoFence)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("lat: "+lat+" ,lng: "+lng)
        )

    }

}