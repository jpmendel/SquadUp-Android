package com.squadup.squadup.activity

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.res.ResourcesCompat
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.squadup.squadup.R
import com.squadup.squadup.data.User
import java.util.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory

class LoginActivity : BaseActivity() {

    private val RC_SIGN_IN = 9001 //Value taken from Google: https://github.com/googlesamples/google-services/blob/master/android/signin/app/src/main/java/com/google/samples/quickstart/signin/SignInActivity.java

    private val MAX_IMAGES_ON_MAP = 12

    private lateinit var mGoogleSignInClient : GoogleSignInClient

    private lateinit var signInButton: SignInButton

    private lateinit var mainLayout: RelativeLayout

    private lateinit var campusMapImage: ImageView

    private val animationHandler: Handler = Handler()

    private var images: MutableList<ImageView> = mutableListOf()

    private var random: Random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initializeViews()
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        signInButton.setOnClickListener {
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)
    }

    override fun onResume() {
        super.onResume()
        startAnimatingImages()
    }

    override fun onPause() {
        super.onPause()
        stopAnimatingImages()
    }

    override fun initializeViews() {
        super.initializeViews()
        hideSignOut()
        signInButton = findViewById(R.id.sign_in_button)
        mainLayout = findViewById(R.id.main_layout)
        campusMapImage = findViewById(R.id.campus_map_image)
        val bitmapIcon = BitmapFactory.decodeResource(resources, R.drawable.campus_map)
        val roundedMap = RoundedBitmapDrawableFactory.create(resources, bitmapIcon)
        roundedMap.cornerRadius = 800f
        campusMapImage.setImageDrawable(roundedMap)
    }

    private fun updateUI(account: GoogleSignInAccount?){
        if (account != null) {
            Log.i("Login", "User Name " + account.displayName)
            Log.i("Login", "Email " + account.email)
            Log.i("Login", "Account ID " + account.id)
            setUserGlobal(account)
            showScreen(MainActivity::class.java)
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            updateUI(account)
        }
    }

    private fun setUserGlobal(account: GoogleSignInAccount?) {
        val userID = account!!.email
        app.backend.getUserRecord(userID!!) {
            user: User? ->
                if (user == null) {
                    //create the user object, send it to the backend and application manager
                    val newUser = User(account.email!!, account.displayName!!)
                    app.backend.createUserRecord(newUser)
                    app.backend.addUserToUserList(newUser.id)
                    app.user = newUser
                } else {
                    //send the user to the application manager
                    app.user = user
                }
            app.user = app.backend.retrieveUserGroupAndFriendInfo(app.user!!)
            app.updateCurrentUserRegistration()
        }
    }

    private var animateImages = object : Runnable {
        override fun run() {
            val imageView = ImageView(baseContext)
            val icon = ResourcesCompat.getDrawable(
                    resources, android.R.drawable.ic_menu_myplaces, null)
            icon!!.setColorFilter(ResourcesCompat.getColor(
                    resources, R.color.medium_orange, null), PorterDuff.Mode.MULTIPLY)
            imageView.setImageDrawable(icon)
            imageView.visibility = View.VISIBLE
            imageView.alpha = 0f
            val params = LinearLayout.LayoutParams(80, 80)
            imageView.layoutParams = params
            val lowerX = campusMapImage.x + 40
            val upperX = campusMapImage.x + campusMapImage.width - 120
            imageView.x = random.nextInt((upperX - lowerX).toInt()) + lowerX
            val lowerY = campusMapImage.y + 40
            val upperY = campusMapImage.y + campusMapImage.height - 120
            imageView.y = random.nextInt((upperY - lowerY).toInt()) + lowerY
            images.add(imageView)
            mainLayout.addView(imageView)
            imageView.animate()
                    .alpha(1f)
                    .duration = 1000
            if (images.count() > MAX_IMAGES_ON_MAP) {
                val removedImage = images.removeAt(0)
                Handler().postDelayed({
                    removedImage.animate()
                            .alpha(0f)
                            .setDuration(500)
                            .withEndAction {
                                mainLayout.removeView(removedImage)
                            }
                }, 500)
            }
            animationHandler.postDelayed(this, 2000)
        }
    }

    private fun startAnimatingImages() {
        animationHandler.postDelayed(animateImages, 1000)
    }

    private fun stopAnimatingImages() {
        animationHandler.removeCallbacks(animateImages)
    }

}
