package com.example.pms.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.example.pms.R
import com.example.pms.firebase.FirestoreClass
import com.example.pms.models.User
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var etEmail: AppCompatEditText
    private lateinit var btnSignIn: Button
    private lateinit var etPassword: AppCompatEditText
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        initializeViews()
        initializeFirebase()
        setUpActionbar()

        btnSignIn.setOnClickListener {
            signInRegisteredUser()
        }
    }

    private fun initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar_sign_in_activity)
        btnSignIn = findViewById(R.id.btn_sign_in)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
    }


    private fun setUpActionbar() {
        setSupportActionBar(toolbar)

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun signInRegisteredUser() {
        val email = etEmail.text.toString().trim() { it <= ' ' }
        val pass = etPassword.text.toString().trim() { it <= ' ' }

        if (validateForm(email,pass)){
            showProgressDialog(resources.getString(R.string.please_wait))
            firebaseAuth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful){

                        FirestoreClass().loadUserData(this@SignInActivity)

                    }
                    else{
                        hideProgressDialog()
                        Toast.makeText(this@SignInActivity, task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter your email")
                false
            }

            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter your password")
                false
            }
            else -> {
                true
            }
        }
    }

    fun signInSuccess(user : User) {
        hideProgressDialog()

        val intent = Intent(this@SignInActivity, MainActivity::class.java)
        startActivity(intent)

        finish()
    }
}
