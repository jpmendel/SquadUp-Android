package com.squadup.squadup.activity

import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.squadup.squadup.R
import com.squadup.squadup.data.Constants
import com.squadup.squadup.data.Group
import com.squadup.squadup.data.User
import com.squadup.squadup.manager.PermissionManager

/**
 * An activity to manage the map screen where members of a group can find a location to meet up.
 */
class MeetUpActivity : BaseActivity(), OnMapReadyCallback, LocationListener {

    // The Google Map used to display everyone's location.
    private lateinit var map: GoogleMap

    // The frame to hold the Google Map.
    private lateinit var mapFrame: FrameLayout

    // The text displaying the status at the top of the screen.
    private lateinit var statusText: TextView

    // The image displayed while the GPS is acquiring the user's location.
    private lateinit var loadingImage: ImageView

    // FOR TESTING PURPOSES ONLY.
    private lateinit var testButton: Button

    // The layout at the bottom of the screen containing two buttons.
    private lateinit var lowerButtonLayout: LinearLayout

    // The button used to find a meeting spot without all members present.
    private lateinit var meetNowButton: Button

    // The button used to send push notifications to group members.
    private lateinit var notifyGroupButton: Button

    // The layout holding the continue button below the screen.
    private lateinit var continueButtonFrame: FrameLayout

    // The button used to continue to the next screen.
    private lateinit var continueButton: Button

    // Manages receiving broadcast messages from the FirebaseMessageService.
    private lateinit var broadcastManager: LocalBroadcastManager

    // The current user of the app on this device.
    private lateinit var user: User

    // The group the user is trying to meet with.
    private lateinit var group: Group

    // Manages the requesting of location updates.
    private lateinit var locationManager: LocationManager

    // The location of the current user.
    private var myLocation: Location? = null

    // A list of locations of group members.
    private var locations: MutableMap<String, LatLng> = mutableMapOf()

    // A list of markers displayed on the map of group members.
    private var locationMarkers: MutableMap<String, Marker> = mutableMapOf()

    // The calculated meeting location for the group.
    private var meetingLocation: Map.Entry<String, LatLng>? = null

    // A list that keeps track of yes or no answers when requesting to meet early.
    private var readyResponses: MutableList<Boolean> = mutableListOf()

    // Whether or not someone in the group has requested to meet early.
    private var requestedReady: Boolean = false

    // Whether or not the app has started to find a meeting location.
    private var findingMeetingLocation: Boolean = false

    // Keeps track of the names displayed over group members' heads without centering on them.
    private var lastMarker: Marker? = null

    // Handles the animation of the status text.
    private val statusTextAnimationHandler: Handler = Handler()

    // Handles the animation of the loading image.
    private val loadingImageAnimationHandler: Handler = Handler()

    // Runs when the activity is loaded and created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet_up)
        initializeViews()
        resetValues()
        setupButtons()
        loadGoogleMap()
    }

    // Runs when the activity is closed and removed from memory.
    override fun onDestroy() {
        super.onDestroy()
        stopAnimatingText()
        app.backend.stopListening(group.id)
        broadcastManager.unregisterReceiver(broadcastReceiver)
    }

    // Sets up all of the views on the screen.
    override fun initializeViews() {
        super.initializeViews()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mapFrame = findViewById(R.id.map_frame)
        statusText = findViewById(R.id.status_text)
        loadingImage = findViewById(R.id.loading_image)
        testButton = findViewById(R.id.test_button)
        lowerButtonLayout = findViewById(R.id.lower_button_layout)
        meetNowButton = findViewById(R.id.meet_now_button)
        meetNowButton.setBackgroundResource(R.drawable.shape_round_button_gray)
        notifyGroupButton = findViewById(R.id.notify_button)
        continueButtonFrame = findViewById(R.id.continue_button_frame)
        continueButton = findViewById(R.id.continue_button)
        startAnimatingText()
    }

    // Sets up the receiver to get broadcast messages from the FirebaseMessageService.
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

    // Resets any values associated with the activity.
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

    // Sets up any buttons on the screen.
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
        continueButton.setOnClickListener {
            onContinueButtonClick()
        }
    }

    // Loads the Google Map into the map fragment.
    private fun loadGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this) // Will call onMapReady() when map is ready.
    }

    // Called when the Google Map loads.
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        initializeMap()
        if (PermissionManager.checkLocationPermission(this)) {
            initializeLocationManager()
        } else {
            PermissionManager.requestLocationPermission(this)
        }
    }

    // Sets up the Google Map on the screen.
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
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null)
            } else {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null)
            }
            startAnimatingLoadingImage()
            statusText.text = "Acquiring..."
        }
    }

    // Used to run code that may have been skipped if the user hasn't given permission yet, but accepted after.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PermissionManager.PERMISSION_REQUEST_CODE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeLocationManager()
            }
        }
    }

    // Runs when the device location changes.
    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            myLocation = location
            addLocation(user.id, user.name, LatLng(location.latitude, location.longitude))
            setInitialRegion()
            initializeBroadcastReceiver()
            sendLoginMessage()
            stopAnimatingLoadingImage()
            animateScreenIn()
            statusText.text = "Waiting For Others..."
            testButton.visibility = View.VISIBLE
        }
    }

    // Sets the initial region being viewed on the map.
    private fun setInitialRegion() {
        if (myLocation != null) {
            updateZoom(myLocation!!, 18f)
        }
    }

    // Updates the zoom of the map.
    private fun updateZoom(location: Location, zoom: Float) {
        val userLocation = LatLng(location.latitude, location.longitude)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoom))
    }

    // Zooms the map to fit all locations of group members.
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

    // Adds the location of a group member to the map and creates graphics.
    private fun addLocation(id: String, name: String, location: LatLng) {
        if (!locations.containsKey(id)) {
            locations[id] = location
            val icon = BitmapFactory.decodeResource(resources,
                        android.R.drawable.ic_menu_myplaces).copy(Bitmap.Config.ARGB_8888, true)
            val paint = Paint()
            val filter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.medium_orange), PorterDuff.Mode.SRC_IN)
            paint.colorFilter = filter
            val canvas = Canvas(icon)
            canvas.drawBitmap(icon, 0f, 0f, paint)
            val marker = map.addMarker(MarkerOptions()
                    .position(location)
                    .title(name)
                    .icon(BitmapDescriptorFactory.fromBitmap(icon)))
            locationMarkers[id] = marker
            if (locations.count() > 1) {
                meetNowButton.setBackgroundResource(R.drawable.shape_round_button_blue)
            }
        }
    }

    // Adds a line graphic to the map.
    private fun addLineToMap(start: LatLng, end: LatLng) {
        map.addPolyline(PolylineOptions()
                .add(start)
                .add(end)
                .width(5f))
    }

    // Adds a graphic for the meeting location to the map.
    private fun addMeetingLocationToMap(name: String, location: LatLng) {
        map.addMarker(MarkerOptions()
                .position(location)
                .title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)))
    }

    // Sends a message that the user has entered the map screen.
    private fun sendLoginMessage() {
        if (myLocation != null) {
            app.backend.sendLoginMessage(group.id, user.id, user.name, myLocation!!.latitude, myLocation!!.longitude)
        }
    }

    // Sends a message that contains the user's location.
    private fun sendMyLocation() {
        if (myLocation != null) {
            app.backend.sendLocationMessage(group.id, user.id, user.name, myLocation!!.latitude, myLocation!!.longitude)
        }
    }

    // Checks if all members in the group are present.
    private fun checkAllMembersPresent(): Boolean {
        for (member in group.memberIDs) {
            if (!locations.containsKey(member)) {
                return false
            }
        }
        return true
    }

    // Updates the text at the top of the screen for how many members are left to join.
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

    // Calculates the distance between two locations in meters.
    private fun distanceBetween(location1: LatLng, location2: LatLng): Double {
        val latDiffMeters = location1.latitude - location2.latitude
        val longDiffMeters = location1.longitude - location2.longitude
        return Math.sqrt(Math.pow(latDiffMeters, 2.0) + Math.pow(longDiffMeters, 2.0)) * Constants.METERS_PER_DEGREE
    }

    // Calculates the center point of all group members.
    private fun calculateCenterPoint(): LatLng {
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
        val latAvg = (maxLat + minLat) / 2.0
        val longAvg = (maxLong + minLong) / 2.0
        return LatLng(latAvg, longAvg)
    }

    // Find the closest building to the center point of all group members.
    private fun findClosestBuilding(location: LatLng): Map.Entry<String, LatLng> {
        var closest: Map.Entry<String, LatLng>? = null
        for (building in Constants.MEETING_LOCATIONS) {
            if (closest == null) {
                closest = building
            } else {
                if (distanceBetween(location, building.value) < distanceBetween(location, closest.value)) {
                    closest = building
                }
            }
        }
        return closest!!
    }

    // Finds a meeting location for all members of the group.
    private fun findMeetingLocation() {
        findingMeetingLocation = true
        app.backend.stopListening(group.id)
        stopAnimatingText()
        meetNowButton.setBackgroundResource(R.drawable.shape_round_button_gray)
        notifyGroupButton.setBackgroundResource(R.drawable.shape_round_button_gray)
        statusText.text = "Calculating..."
        val centerPoint = calculateCenterPoint()
        meetingLocation = findClosestBuilding(centerPoint)
        var delay = 1000L
        for (location in locations.values) {
            Handler().postDelayed({
                addLineToMap(location, centerPoint)
            }, delay)
            delay += 500L
        }
        Handler().postDelayed({
            addLineToMap(centerPoint, meetingLocation!!.value)
            addMeetingLocationToMap(meetingLocation!!.key, meetingLocation!!.value)
            statusText.text = "Squad Up!"
            animateSwitchButtons()
        }, 1000L + locations.values.count() * 500L)
    }

    // The receiver to handle any broadcasts from the FirebaseMessageService.
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

    // Runs when a login message is received.
    private fun onLoginMessageReceived(intent: Intent) {
        val senderID = intent.getStringExtra("senderID")
        if (user.id != senderID) {
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
    }

    // Runs when a location message is received.
    private fun onLocationMessageReceived(intent: Intent) {
        val senderID = intent.getStringExtra("senderID")
        if (user.id != senderID) {
            val senderName = intent.getStringExtra("senderName")
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)
            if (!locations.containsKey(senderID)) {
                addLocation(senderID, senderName, LatLng(latitude, longitude))
                zoomToFit()
                updateMembersRemainingText()
            }
            if (checkAllMembersPresent()) {
                findMeetingLocation()
            }
        }
    }

    // Runs when a ready request message is received.
    private fun onReadyRequestMessageReceived(intent: Intent) {
        val senderID = intent.getStringExtra("senderID")
        if (senderID != user.id) {
            val senderName = intent.getStringExtra("senderName")
            requestedReady = true
            meetNowButton.setBackgroundResource(R.drawable.shape_round_button_gray)
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

    // Runs when a ready response message is received.
    private fun onReadyResponseMessageReceived(intent: Intent) {
        val receiverID = intent.getStringExtra("receiverID")
        if (receiverID == user.id) {
            val senderName = intent.getStringExtra("senderName")
            val response = intent.getBooleanExtra("response", false)
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

    // Runs when a ready decision message is received.
    private fun onReadyDecisionMessageReceived(intent: Intent) {
        val decision = intent.getBooleanExtra("decision", false)
        readyResponses.clear()
        requestedReady = false
        meetNowButton.setBackgroundResource(R.drawable.shape_round_button_blue)
        if (decision) {
            findMeetingLocation()
        } else {
            Toast.makeText(baseContext, String.format("Not everyone is ready."), Toast.LENGTH_SHORT).show()
        }
    }

    // Runs when the meet now button is pressed.
    private fun onMeetNowButtonClick() {
        if (!requestedReady && !findingMeetingLocation && locations.count() > 1) {
            readyResponses = mutableListOf()
            app.backend.sendReadyRequestMessage(group.id, user.id, user.name)
            requestedReady = true
            meetNowButton.setBackgroundResource(R.drawable.shape_round_button_gray)
        }
    }

    // Runs when the notify group button is pressed.
    private fun onNotifyGroupButtonClick() {
        if (!findingMeetingLocation) {
            app.backend.sendNotification(group.id, "${user.name} (SquadUp)", "Hey, let's meet up!")
        }
    }

    private fun onContinueButtonClick() {
        showScreen(MeetingLocationViewActivity::class.java) {
            intent: Intent ->
            intent.putExtra("meetingLocation", meetingLocation!!.key)
        }
    }

    // Animates the text to fade in and fade out.
    private val animateText = object : Runnable {
        override fun run() {
            statusText.animate().alpha(0f).duration = 1000
            Handler().postDelayed({
                statusText.animate().alpha(1f).duration = 1000
            }, 1000)
            statusTextAnimationHandler.postDelayed(this, 2000)
        }
    }

    // Start animating the status text.
    private fun startAnimatingText() {
        statusTextAnimationHandler.post(animateText)
        statusText.alpha = 1f
    }

    // Stop animating the status text.
    private fun stopAnimatingText() {
        statusTextAnimationHandler.removeCallbacks(animateText)
        statusText.alpha = 1f
    }

    private val animateLoadingImage = object : Runnable {
        override fun run() {
            loadingImage.animate()
                    .scaleX(0.9f)
                    .scaleY(1.1f)
                    .duration = 500
            Handler().postDelayed({
                loadingImage.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .duration = 500
            }, 500)
            Handler().postDelayed({
                statusTextAnimationHandler.post(this)
            }, 1000)
        }
    }

    private fun startAnimatingLoadingImage() {
        loadingImageAnimationHandler.post(animateLoadingImage)
        loadingImage.alpha = 1f
    }

    private fun stopAnimatingLoadingImage() {
        loadingImageAnimationHandler.removeCallbacks(animateLoadingImage)
        loadingImage.alpha = 0f
    }

    // Animates the lower button layout onto the screen.
    private fun animateScreenIn() {
        mapFrame.animate()
                .setInterpolator(DecelerateInterpolator())
                .translationX(0f)
                .duration = 250
        lowerButtonLayout.animate()
                .setInterpolator(DecelerateInterpolator())
                .translationY(0f)
                .duration = 500
    }

    // Animate the buttons to swap the original two with the continue button.
    private fun animateSwitchButtons() {
        lowerButtonLayout.animate()
                .setInterpolator(DecelerateInterpolator())
                .translationY(300f)
                .duration = 500
        Handler().postDelayed({
            continueButtonFrame.animate()
                    .setInterpolator(AccelerateInterpolator())
                    .translationY(0f)
                    .duration = 500
        }, 500)
    }

    // Runs when the location manager status changes.
    override fun onStatusChanged(status: String?, code: Int, bundle: Bundle?) {

    }

    // Runs when the location provider is enabled.
    override fun onProviderEnabled(provider: String?) {

    }

    // Runs when the location provider is disabled.
    override fun onProviderDisabled(provider: String?) {

    }

}
