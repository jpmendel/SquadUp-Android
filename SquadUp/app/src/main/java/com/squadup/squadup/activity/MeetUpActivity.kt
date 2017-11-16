package com.squadup.squadup.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.content.res.ResourcesCompat
import android.widget.Button
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.squadup.squadup.R
import com.squadup.squadup.manager.PermissionManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.squadup.squadup.data.Group
import com.squadup.squadup.data.User
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptor




class MeetUpActivity : BaseActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var map: GoogleMap

    private lateinit var meetNowButton: Button

    private lateinit var notifyGroupButton: Button

    private lateinit var broadcastManager: LocalBroadcastManager

    private lateinit var locationManager: LocationManager

    private lateinit var user: User

    private lateinit var group: Group

    private var myLocation: Location? = null

    private var locations: MutableMap<String, LatLng> = mutableMapOf()

    private var locationMarkers: MutableMap<String, Marker> = mutableMapOf()

    private var lastMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up)
        if (!PermissionManager.checkLocationPermission(this)) {
            PermissionManager.requestLocationPermission(this)
        }
        initializeViews()
        resetValues()
        setupButtons()
        loadGoogleMap()
    }

    override fun onDestroy() {
        super.onDestroy()
        app.backend.stopListening(group.id)
        broadcastManager.unregisterReceiver(broadcastReceiver)
    }

    private fun initializeViews() {
        meetNowButton = findViewById(R.id.meet_now_button)
        notifyGroupButton = findViewById(R.id.notify_button)
    }

    private fun initializeBroadcastReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(LOGIN_MESSAGE)
        intentFilter.addAction(LOCATION_MESSAGE)
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter)
        app.backend.startListening(group.id)
        sendLoginMessage()
    }

    private fun resetValues() {
        user = User("Jacob Mendelowitz", "Jacob Mendelowitz")
        group = Group("squad-up", "SquadUp")
        locations = mutableMapOf()
        locationMarkers = mutableMapOf()
    }

    private fun setupButtons() {
        meetNowButton.setOnClickListener {
            if (locations.containsKey("Jason Corriveau")) {
                addLocation("Stephen Haberle", LatLng(40.957906, -76.884733))
            } else {
                addLocation("Jason Corriveau", LatLng(40.95231, -76.880407))
            }
            zoomToFit()
        }

        notifyGroupButton.setOnClickListener {
            onNotifyGroupButtonClick()
        }
    }

    private fun loadGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        initializeMap()
        initializeLocationManager()
        setInitialRegion()
        initializeBroadcastReceiver()
    }

    // Sets up the Google Map on the layout.
    private fun initializeMap() {
        map.isBuildingsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.isScrollGesturesEnabled = false
        map.uiSettings.isZoomGesturesEnabled = false
        map.uiSettings.isRotateGesturesEnabled = false
        map.uiSettings.isTiltGesturesEnabled = false
        map.setOnMarkerClickListener(OnMarkerClickListener {
            marker: Marker ->
            if (lastMarker != null) {
                lastMarker!!.hideInfoWindow()
                if (lastMarker!! == marker) {
                    lastMarker = null
                    return@OnMarkerClickListener true
                }
            }
            marker.showInfoWindow()
            lastMarker = marker
            true
        })
    }

    // Sets up the location manager that will keep track of the user's location.
    private fun initializeLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (PermissionManager.checkLocationPermission(this)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
            myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (myLocation != null) {
                addLocation(user.id, LatLng(myLocation!!.latitude, myLocation!!.longitude))
            }
        }
    }

    // Sets the initial region on the map.
    private fun setInitialRegion() {
        if (myLocation != null) {
            updateZoom(myLocation!!, 18f)
        }
    }

    private fun updateZoom(location: Location, zoom: Float) {
        val userLocation = LatLng(location.latitude, location.longitude)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoom))
    }

    private fun zoomToFit() {
        var minLat = 0.0
        var maxLat = 0.0
        var minLong = 0.0
        var maxLong = 0.0
        for (location in locations.values) {
            if (minLat == 0.0) {
                minLat = location.latitude
                maxLat = location.latitude
            }
            if (minLong == 0.0) {
                minLong = location.longitude
                maxLong = location.longitude
            }
            if (location.latitude < minLat) {
                minLat = location.latitude
            } else if (location.latitude > maxLat) {
                maxLat = location.latitude
            }
            if (location.longitude < minLong) {
                minLong = location.longitude
            } else if (location.longitude > maxLong) {
                maxLong = location.longitude
            }
        }
        val latPadding = (maxLat - minLat) * 0.25
        val longPadding = (maxLong - minLong) * 0.25
        val northeast = LatLng(maxLat + latPadding, maxLong + longPadding)
        val southwest = LatLng(minLat - latPadding, minLong - longPadding)
        val bounds = LatLngBounds(southwest, northeast)
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0))
    }

    private fun addLocation(user: String, location: LatLng) {
        if (!locations.containsKey(user)) {
            locations[user] = location
            val icon = BitmapFactory.decodeResource(resources,
                        android.R.drawable.ic_menu_myplaces).copy(Bitmap.Config.ARGB_8888, true)
            val paint = Paint()
            val filter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.color_accent), PorterDuff.Mode.SRC_IN);
            paint.colorFilter = filter
            val canvas = Canvas(icon)
            canvas.drawBitmap(icon, 0f, 0f, paint)
            val marker = map.addMarker(MarkerOptions()
                    .position(location)
                    .title(user)
                    .icon(BitmapDescriptorFactory.fromBitmap(icon)))
            locationMarkers[user] = marker
        }
    }

    private fun sendLoginMessage() {
        if (myLocation != null) {
            app.backend.sendLoginMessage(group.id, user.id, myLocation!!.latitude, myLocation!!.longitude)
        }
    }

    private fun sendMyLocation() {
        if (myLocation != null) {
            app.backend.sendLocationMessage(group.id, user.id, myLocation!!.latitude, myLocation!!.longitude)
        }
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null && !locations.containsKey(user.id)) {
            locations[user.id] = LatLng(location.latitude, location.longitude)
            myLocation = location
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == LOGIN_MESSAGE) {
                val sender = intent.getStringExtra("sender")
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                if (!locations.containsKey(sender)) {
                    addLocation(sender, LatLng(latitude, longitude))
                    zoomToFit()
                    Toast.makeText(baseContext, String.format("%s has joined", sender), Toast.LENGTH_SHORT).show()
                }
                sendMyLocation()
            } else if (intent.action == LOCATION_MESSAGE) {
                val sender = intent.getStringExtra("sender")
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                if (!locations.containsKey(sender)) {
                    addLocation(sender, LatLng(latitude, longitude))
                    zoomToFit()
                }
            }
        }
    }

    private fun onNotifyGroupButtonClick() {
        app.backend.sendNotification(group.id, "${user.id} (SquadUp)", "Hey, let's meet up!")
    }

    override fun onStatusChanged(status: String?, code: Int, bundle: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {

    }
}
