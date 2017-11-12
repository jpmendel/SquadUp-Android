package com.squadup.squadup.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.squadup.squadup.R


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
            findViewById<SignInButton>(R.id.sign_in_button).setVisibility(View.GONE)
            findViewById<Button>(R.id.signOutButton).visibility = View.VISIBLE
            Toast.makeText(this,
                    "User has logged in!", Toast.LENGTH_SHORT).show()
            Log.i("Login", "User Name " + account.displayName)
            Log.i("Login", "Email " + account.email)
            Log.i("Login", "Account ID " + account.id)
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

    fun signOut(view: View){
        Log.i("Login", "Sign out Function")
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    findViewById<SignInButton>(R.id.sign_in_button).setVisibility(View.VISIBLE)
                    findViewById<Button>(R.id.signOutButton).setVisibility(View.GONE)
                }
        Toast.makeText(this,
                "Successfully logged out!!", Toast.LENGTH_SHORT).show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            //if (!GoogleSignIn.hasPermissions(account, )




            // Signed in successfully, show authenticated UI.
            updateUI(account)

        }
    }


}
