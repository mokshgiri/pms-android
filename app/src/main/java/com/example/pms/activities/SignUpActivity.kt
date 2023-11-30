package com.example.pms.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.example.pms.R
import com.example.pms.firebase.FirestoreClass
import com.example.pms.models.User
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : BaseActivity() {

    private lateinit var toolbar : Toolbar
    private lateinit var etName : AppCompatEditText
    private lateinit var etEmail : AppCompatEditText
    private lateinit var etPassword : AppCompatEditText
    private lateinit var btnSignUp : Button
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        initializeViews()
        initializeFirebase()
        setUpActionbar()

        btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar_sign_up_activity)
        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnSignUp = findViewById(R.id.btn_sign_up)
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

    private fun registerUser(){
        initializeViews()
        val name = etName.text.toString().trim() {it <= ' '}
        val pass = etPassword.text.toString().trim() {it <= ' '}
        val email = etEmail.text.toString().trim() {it <= ' '}

//        validateForm(name, email, pass)
        if (validateForm(name, email, pass)){
            showProgressDialog(resources.getString(R.string.please_wait))

            firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener{
                task ->

                if (task.isSuccessful) {
                    val firebaseUser = task.result.user
                    val registeredEmail = firebaseUser!!.email

                    val user = User(firebaseUser.uid, name, registeredEmail.toString())

                    FirestoreClass().registerUser(this@SignUpActivity, user)
                }
                else{
                    Toast.makeText(
                        this@SignUpActivity, task.exception!!.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun validateForm(name:String, email:String, password:String) : Boolean{
        return when{
            TextUtils.isEmpty(name) ->{
                showErrorSnackBar("Please enter your name")
                false
            }
            TextUtils.isEmpty(email) ->{
                showErrorSnackBar("Please enter your email")
                false
            }
            TextUtils.isEmpty(password) ->{
                showErrorSnackBar("Please enter your password")
                false
            }
            else -> {
                true
            }
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this@SignUpActivity,
            "You have successfully registered.",
            Toast.LENGTH_SHORT
        ).show()

        hideProgressDialog()

        firebaseAuth.signOut()
        finish()
    }
}