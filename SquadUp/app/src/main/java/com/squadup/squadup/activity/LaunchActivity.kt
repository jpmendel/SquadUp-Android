package com.squadup.squadup.activity

import android.os.Bundle
import android.os.Handler
import com.squadup.squadup.R

class LaunchActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        Handler().postDelayed({
            showScreen(MessagingTestActivity::class.java)
        }, 1500)
    }

}
