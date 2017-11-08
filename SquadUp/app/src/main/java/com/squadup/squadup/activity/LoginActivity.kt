package com.squadup.squadup.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.squadup.squadup.R
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException
import android.widget.Toast


class LoginActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient : GoogleSignInClient
    private val RC_SIGN_IN = 9001 //Value taken from Google: https://github.com/googlesamples/google-services/blob/master/android/signin/app/src/main/java/com/google/samples/quickstart/signin/SignInActivity.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        setContentView(R.layout.activity_login)
        findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener(View.OnClickListener {
            Log.i("Login", "Sign in Function")
            val signInIntent = mGoogleSignInClient.getSignInIntent()
            startActivityForResult(signInIntent, RC_SIGN_IN)
        })
    }

    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)

    }

    fun updateUI(account: GoogleSignInAccount?){
        //TODO: If User account exists: Then move to next activity. Otherwise, display Google Login Button.
        if (account != null) {
            findViewById<SignInButton>(R.id.sign_in_button).setVisibility(View.GONE);
            Toast.makeText(this,
                    "User has logged in!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this,
                    "User is not logged in!", Toast.LENGTH_SHORT).show()
        }
    }

    fun signIn(view: View){
        Log.i("Login", "Sign in Function")
        val signInIntent = mGoogleSignInClient.getSignInIntent()
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


}
