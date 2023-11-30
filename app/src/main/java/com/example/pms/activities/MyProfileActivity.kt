package com.example.pms.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.pms.R
import com.example.pms.firebase.FirestoreClass
import com.example.pms.models.User
import com.example.pms.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    companion object {
        //A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
        // END
    }

    private var selectedImageFileUri: Uri? = null
    private var profileImageURL: String? = ""
    private var mProfileImageURL: String = ""

    private lateinit var toolbar : Toolbar
    private lateinit var iv_profile_user_image : CircleImageView
    private lateinit var et_name : EditText
    private lateinit var et_email : EditText
    private lateinit var et_mobile : EditText
    private lateinit var btn_update : Button
    private lateinit var userDetails : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        initializeViews()
        setUpActionbar()

        FirestoreClass().loadUserData(this)

        iv_profile_user_image.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                showImageChooser()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                   READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btn_update.setOnClickListener {
            if (selectedImageFileUri != null){
                uploadUserImage()
            }
            else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == MyProfileActivity.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            // The uri of selection image from phone storage.
            selectedImageFileUri = data.data

            try {
                // Load the user image in the ImageView.
                Glide
                    .with(this@MyProfileActivity)
                    .load(Uri.parse(selectedImageFileUri.toString())) // URI of the image
                    .centerCrop() // Scale type of the image.
                    .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                    .into(iv_profile_user_image) // the view in which the image will be loaded.
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO (Step 9: Call the image chooser function.)
                // START
                showImageChooser()
                // END
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this,
                    "Oops, you just denied the permission for storage. You can also allow it from settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showImageChooser() {
        // An intent for launching the image selection of phone storage.
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // Launches the image selection of phone storage using the constant code.
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    private fun uploadUserImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        if (selectedImageFileUri != null) {

            //getting the storage reference
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(
                    selectedImageFileUri
                )
            )

            //adding the file to reference
            sRef.putFile(selectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success
                    Log.e(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    // Get the downloadable url from the task snapshot
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.e("Downloadable Image URL", uri.toString())

                            // assign the image url to the variable.
                            profileImageURL = uri.toString()

                            // assign the image url to the variable.
                            mProfileImageURL = uri.toString()

                            updateUserProfileData()

                            // Call a function to update user details in the database.
//                            updateUserProfileData()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@MyProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }
    }

    private fun getFileExtension(uri: Uri?): String? {

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != userDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (et_name.text.toString() != userDetails.name) {
            userHashMap[Constants.NAME] = et_name.text.toString()
        }

        if (et_mobile.text.toString().isNotEmpty() && et_mobile.text.toString() != userDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()
        }

        // Update the data in the database.
        FirestoreClass().updateUserProfileData(this@MyProfileActivity, userHashMap)
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar_my_profile_activity)
        iv_profile_user_image = findViewById(R.id.iv_profile_user_image)
        et_name = findViewById(R.id.et_name)
        et_email = findViewById(R.id.et_email)
        et_mobile = findViewById(R.id.et_mobile)
        btn_update = findViewById(R.id.btn_update)
    }

    private fun setUpActionbar() {
        setSupportActionBar(toolbar)

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        actionbar?.title = resources.getString(R.string.my_profile)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUserDataInUi(user: User) {

        userDetails = user

        Glide.with(this@MyProfileActivity).load(user.image).into(iv_profile_user_image)
        et_name.setText(user.name)
        et_email.setText(user.email)

        if (user.mobile != 0L) {
            et_mobile.setText(user.mobile.toString())
        }
    }

}