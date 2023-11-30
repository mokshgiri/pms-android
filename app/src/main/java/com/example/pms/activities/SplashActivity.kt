package com.example.pms.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.pms.R
import com.example.pms.firebase.FirestoreClass

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler().postDelayed({

            val currentUserUid = FirestoreClass().getCurrentUserId()

            if (currentUserUid.isNotEmpty()){
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
            }
            else{
                val intent = Intent(this@SplashActivity, IntroActivity::class.java)
                startActivity(intent)
            }
            finish()}, 2000)

    }

}