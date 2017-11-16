package com.squadup.squadup.activity

import android.content.*
import android.graphics.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.squadup.squadup.R
import com.squadup.squadup.data.Group
import com.squadup.squadup.data.User
import com.squadup.squadup.manager.PermissionManager

class MeetUpActivity : BaseActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var map: GoogleMap

    private lateinit var statusText: TextView

    private lateinit var testButton: Button

    private lateinit var meetNowButton: Button

    private lateinit var notifyGroupButton: Button

    private lateinit var broadcastManager: LocalBroadcastManager

    private lateinit var locationManager: LocationManager

    private lateinit var user: User

    private lateinit var group: Group

    private var myLocation: Location? = null

    private var locations: MutableMap<String, LatLng> = mutableMapOf()

    private var locationMarkers: MutableMap<String, Marker> = mutableMapOf()

    private var meetingLocation: LatLng? = null

    private var readyResponses: MutableList<Boolean> = mutableListOf()

    private var requestedReady: Boolean = false

    private var lastMarker: Marker? = null

    private val animationHandler: Handler = Handler()

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
        stopAnimatingText()
        app.backend.stopListening(group.id)
        broadcastManager.unregisterReceiver(broadcastReceiver)
    }

    override fun initializeViews() {
        super.initializeViews()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        statusText = findViewById(R.id.status_text)
        testButton = findViewById(R.id.test_button)
        meetNowButton = findViewById(R.id.meet_now_button)
        notifyGroupButton = findViewById(R.id.notify_button)
        startAnimatingText()
    }

    private fun initializeBroadcastReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(LOGIN_MESSAGE)
        intentFilter.addAction(LOCATION_MESSAGE)
        intentFilter.addAction(READY_REQUEST_MESSAGE)
        intentFilter.addAction(READY_RESPONSE_MESSAGE)
        intentFilter.addAction(READY_DECISION_MESSAGE)
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter)
        app.backend.startListening(group.id)
    }

    private fun resetValues() {
        user = User("jacob", "Jacob Mendelowitz")
        group = Group("squad-up", "SquadUp")
        group.memberIDs = mutableListOf("jacob", "jason", "stephen", "eric")
        group.members = mutableListOf(
                User("jacob", "Jacob Mendelowitz"),
                User("jason", "Jason Corriveau"),
                User("stephen", "Stephen Haberle"),
                User("eric", "Eric Marshall")
        )
        locations = mutableMapOf()
        locationMarkers = mutableMapOf()
        readyResponses = mutableListOf()
    }

    private fun setupButtons() {
        testButton.setOnClickListener {
            if (!locations.containsKey("jason")) {
                addLocation("jason", "Jason Corriveau", LatLng(40.95231, -76.880407))
            } else if (!locations.containsKey("stephen")) {
                addLocation("stephen", "Stephen Haberle", LatLng(40.957906, -76.884733))
            } else if (!locations.containsKey("eric")) {
                addLocation("eric", "Eric Marshall", LatLng(40.956436, -76.884541))
            }
            zoomToFit()
            updateMembersRemainingText()
            if (checkAllMembersPresent()) {
                findMeetingLocation()
            }
        }
        meetNowButton.setOnClickListener {
            onMeetNowButtonClick()
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
        sendLoginMessage()
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
                addLocation(user.id, user.name, LatLng(myLocation!!.latitude, myLocation!!.longitude))
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

    private fun addLocation(id: String, name: String, location: LatLng) {
        if (!locations.containsKey(id)) {
            locations[id] = location
            val icon = BitmapFactory.decodeResource(resources,
                        android.R.drawable.ic_menu_myplaces).copy(Bitmap.Config.ARGB_8888, true)
            val paint = Paint()
            val filter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.color_accent), PorterDuff.Mode.SRC_IN)
            paint.colorFilter = filter
            val canvas = Canvas(icon)
            canvas.drawBitmap(icon, 0f, 0f, paint)
            val marker = map.addMarker(MarkerOptions()
                    .position(location)
                    .title(name)
                    .icon(BitmapDescriptorFactory.fromBitmap(icon)))
            locationMarkers[id] = marker
        }
    }

    private fun addLineToMap(start: LatLng, end: LatLng) {
        map.addPolyline(PolylineOptions()
                .add(start)
                .add(end)
                .width(5f))
    }

    private fun addMeetingLocationToMap(location: LatLng) {
        map.addMarker(MarkerOptions()
                .position(location)
                .title("Meeting Spot")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)))
    }

    private fun sendLoginMessage() {
        if (myLocation != null) {
            app.backend.sendLoginMessage(group.id, user.id, user.name, myLocation!!.latitude, myLocation!!.longitude)
        }
    }

    private fun sendMyLocation() {
        if (myLocation != null) {
            app.backend.sendLocationMessage(group.id, user.id, user.name, myLocation!!.latitude, myLocation!!.longitude)
        }
    }

    private fun checkAllMembersPresent(): Boolean {
        for (member in group.memberIDs) {
            if (!locations.containsKey(member)) {
                return false
            }
        }
        return true
    }

    private fun updateMembersRemainingText() {
        val membersRemaining = group.memberIDs.count() - locations.keys.count()
        if (membersRemaining == 1) {
            for (member in group.members) {
                if (!locations.containsKey(member.id)) {
                    statusText.text = "Waiting on ${member.name}..."
                    break
                }
            }
        } else {
            statusText.text = "Waiting on $membersRemaining members..."
        }

    }

    private fun calculateCenterPoint(): LatLng {
        var latSum = 0.0
        var longSum = 0.0
        for (location in locations.values) {
            latSum += location.latitude
            longSum += location.longitude
        }
        val latAvg = latSum / locations.values.count()
        val longAvg = longSum / locations.values.count()
        return LatLng(latAvg, longAvg)
    }

    private fun findMeetingLocation() {
        app.backend.stopListening(group.id)
        stopAnimatingText()
        statusText.text = "Calculating..."
        meetingLocation = calculateCenterPoint()
        var delay = 1000L
        for (location in locations.values) {
            Handler().postDelayed({
                addLineToMap(location, meetingLocation!!)
            }, delay)
            delay += 500L
        }
        Handler().postDelayed({
            addMeetingLocationToMap(meetingLocation!!)
            statusText.text = "Squad Up!"
        }, 1000L + locations.values.count() * 500L)
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
                onLoginMessageReceived(intent)
            } else if (intent.action == LOCATION_MESSAGE) {
                onLocationMessageReceived(intent)
            } else if (intent.action == READY_REQUEST_MESSAGE) {
                onReadyRequestMessageReceived(intent)
            } else if (intent.action == READY_RESPONSE_MESSAGE) {
                onReadyResponseMessageReceived(intent)
            } else if (intent.action == READY_DECISION_MESSAGE) {
                onReadyDecisionMessageReceived(intent)
            }
        }
    }

    private fun onLoginMessageReceived(intent: Intent) {
        val senderID = intent.getStringExtra("senderID")
        val senderName = intent.getStringExtra("senderName")
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        if (!locations.containsKey(senderID)) {
            addLocation(senderID, senderName, LatLng(latitude, longitude))
            zoomToFit()
            updateMembersRemainingText()
            Toast.makeText(baseContext, String.format("%s Has Joined!", senderName), Toast.LENGTH_SHORT).show()
        }
        sendMyLocation()
        if (checkAllMembersPresent()) {
            findMeetingLocation()
        }
    }

    private fun onLocationMessageReceived(intent: Intent) {
        val senderID = intent.getStringExtra("senderID")
        val senderName = intent.getStringExtra("senderName")
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        if (!locations.containsKey(senderID)) {
            addLocation(senderID, senderName, LatLng(latitude, longitude))
            zoomToFit()
        }
    }

    private fun onReadyRequestMessageReceived(intent: Intent) {
        val senderID = intent.getStringExtra("senderID")
        val senderName = intent.getStringExtra("senderName")
        if (senderID != user.id) {
            requestedReady = true
            AlertDialog.Builder(baseContext)
                    .setTitle(senderName)
                    .setMessage("Hey! Let's go!")
                    .setPositiveButton("Yes") {
                        dialog, id ->
                        app.backend.sendReadyResponseMessage(group.id, user.id, user.name, senderID,true)
                    }
                    .setNegativeButton("No") {
                        dialog, id ->
                        app.backend.sendReadyResponseMessage(group.id, user.id, user.name, senderID,false)
                    }
                    .setCancelable(false)
                    .show()
        }
    }

    private fun onReadyResponseMessageReceived(intent: Intent) {
        val senderName = intent.getStringExtra("senderName")
        val receiverID = intent.getStringExtra("receiverID")
        val response = intent.getBooleanExtra("response", false)
        if (receiverID == user.id) {
            readyResponses.add(response)
            val yesNo = if (response) "Yes" else "No"
            Toast.makeText(baseContext, String.format("%s Responded %s", senderName, yesNo), Toast.LENGTH_SHORT).show()
            if (readyResponses.count() == locations.count() - 1) {
                val respondedYes = readyResponses.count { it }
                val decision = respondedYes == locations.count() - 1
                app.backend.sendReadyDecisionMessage(group.id, user.id, user.name, decision)
            }
        }
    }

    private fun onReadyDecisionMessageReceived(intent: Intent) {
        val decision = intent.getBooleanExtra("decision", false)
        readyResponses.clear()
        requestedReady = false
        if (decision) {
            findMeetingLocation()
        } else {
            Toast.makeText(baseContext, String.format("Not everyone is ready."), Toast.LENGTH_SHORT).show()
        }
    }


    private fun onMeetNowButtonClick() {
        if (!requestedReady && locations.count() > 1) {
            readyResponses = mutableListOf()
            requestedReady = true
            app.backend.sendReadyRequestMessage(group.id, user.id, user.name)
        }
    }

    private fun onNotifyGroupButtonClick() {
        app.backend.sendNotification(group.id, "${user.name} (SquadUp)", "Hey, let's meet up!")
    }

    private val animateText = object : Runnable {
        override fun run() {
            statusText.animate().alpha(0f).duration = 1000
            Handler().postDelayed({
                statusText.animate().alpha(1f).duration = 1000
            }, 1000)
            animationHandler.postDelayed(this, 2000)
        }
    }

    private fun startAnimatingText() {
        animationHandler.post(animateText)
        statusText.alpha = 1f
    }

    private fun stopAnimatingText() {
        animationHandler.removeCallbacks(animateText)
        statusText.alpha = 1f
    }

    override fun onStatusChanged(status: String?, code: Int, bundle: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {

    }
}
