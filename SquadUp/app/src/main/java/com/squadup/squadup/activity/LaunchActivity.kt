package com.squadup.squadup.activity

import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.widget.ImageView
import com.squadup.squadup.R

/**
 * An activity for a splash screen. Shows for a few seconds then goes into the app.
 */
class LaunchActivity : BaseActivity() {

    private lateinit var splashImage: ImageView

    private var animationHandler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        initializeViews()
        animationHandler.post(animateSplashImage)
        Handler().postDelayed({
            animationHandler.removeCallbacks(animateSplashImage)
            presentScreen(LoginActivity::class.java)
        }, 2000)
    }

    override fun initializeViews() {
        splashImage = findViewById(R.id.splash_image)
    }

    private val animateSplashImage = object : Runnable {
        override fun run() {
            splashImage.animate()
                    .scaleX(0.9f)
                    .scaleY(1.1f)
                    .duration = 500
            Handler().postDelayed({
                splashImage.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .duration = 500
            }, 500)
            Handler().postDelayed({
                animationHandler.post(this)
            }, 1000)
        }
    }

}
