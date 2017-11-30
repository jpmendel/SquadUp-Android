package com.squadup.squadup.activity

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.squadup.squadup.R
import com.squadup.squadup.data.User
import com.squadup.squadup.manager.BackendManager
import java.util.*
import android.support.v4.content.res.ResourcesCompat
import android.graphics.drawable.Drawable



class LoginActivity : BaseActivity() {

    private val RC_SIGN_IN = 9001 //Value taken from Google: https://github.com/googlesamples/google-services/blob/master/android/signin/app/src/main/java/com/google/samples/quickstart/signin/SignInActivity.java

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
        Log.i("Login", "Sign in Function")
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
            app.updateCurrentUserRegistration()
        }
    }

    private var animateImages = object : Runnable {
        override fun run() {
            val imageView = ImageView(baseContext)
            val icon = ResourcesCompat.getDrawable(
                    resources, android.R.drawable.ic_menu_myplaces, null)
            icon!!.setColorFilter(ResourcesCompat.getColor(
                    resources, R.color.medium_orange, null), PorterDuff.Mode.SRC_ATOP)
            imageView.setImageDrawable(icon)
            imageView.visibility = View.VISIBLE
            imageView.alpha = 0.0f
            val params = LinearLayout.LayoutParams(80, 80)
            imageView.layoutParams = params
            val lowerX = campusMapImage.x + 40
            val upperX = campusMapImage.x + campusMapImage.width - 40
            imageView.x = random.nextInt((upperX - lowerX).toInt()) + lowerX
            val lowerY = campusMapImage.y + 40
            val upperY = campusMapImage.y + campusMapImage.height - 40
            imageView.y = random.nextInt((upperY - lowerY).toInt()) + lowerY
            images.add(imageView)
            mainLayout.addView(imageView)
            imageView.animate()
                    .alpha(1.0f)
                    .duration = 1000
            if (images.count() > 5) {
                val removedImage = images.removeAt(0)
                removedImage.animate()
                        .alpha(0.0f)
                        .setDuration(1000)
                        .withEndAction {
                            mainLayout.removeView(removedImage)
                        }
            }
            Log.i("LoginActivity", images.count().toString())
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
