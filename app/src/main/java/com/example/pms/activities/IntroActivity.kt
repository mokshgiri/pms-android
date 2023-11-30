package com.example.pms.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import com.example.pms.R

class IntroActivity : BaseActivity() {

    private lateinit var signUpBtn : Button
    private lateinit var signInBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        initializeViews()

        signUpBtn.setOnClickListener {
            openSignUpActivity()
        }

        signInBtn.setOnClickListener {
            openSignInActivity()
        }
    }

    private fun openSignUpActivity() {
        val intent = Intent(this@IntroActivity, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun openSignInActivity() {
        val intent = Intent(this@IntroActivity, SignInActivity::class.java)
        startActivity(intent)

    }

    private fun initializeViews() {
        signUpBtn = findViewById(R.id.btn_sign_up_intro)
        signInBtn = findViewById(R.id.btn_sign_in_intro)
    }
}