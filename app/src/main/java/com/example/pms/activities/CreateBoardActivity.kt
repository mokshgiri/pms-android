package com.example.pms.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.pms.R
import com.example.pms.firebase.FirestoreClass
import com.example.pms.models.Board
import com.example.pms.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    // TODO (Step 3: Add a global variable for URI of a selected image from phone storage.)
    // START
    // Add a global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri: Uri? = null
    private lateinit var iv_board_image: CircleImageView
    private lateinit var toolbar_create_board_activity : androidx.appcompat.widget.Toolbar
    private lateinit var userName : String
    private var boardImageUrl : String ?= null
    private lateinit var etBoardName : AppCompatEditText
    private lateinit var btn_create : Button
    // END

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        initializeView()
        setupActionBar()

        if (intent.hasExtra(Constants.NAME)){
            userName = intent.getStringExtra(Constants.NAME).toString()
        }

        // TODO (Step 4: Add click event for iv_board_image.)
        // START
        iv_board_image.setOnClickListener { view ->

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this@CreateBoardActivity)
            } else {
                /*Requests permissions to be granted to this application. These permissions
                 must be requested in your manifest, they should not be granted to your app,
                 and they should have protection level*/
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btn_create.setOnClickListener {

            // Here if the image is not selected then update the other details of user.
            if (mSelectedImageFileUri != null) {

                uploadBoardImage()
            } else {

                showProgressDialog(resources.getString(R.string.please_wait))

                // Call a function to update create a board.
                createBoard()
            }
        }
        // END
    }

    private fun initializeView() {
        toolbar_create_board_activity = findViewById(R.id.toolbar_create_board_activity)
        iv_board_image = findViewById(R.id.iv_board_image)
        btn_create = findViewById(R.id.btn_create)

        etBoardName = findViewById(R.id.et_board_name)
    }

    // TODO (Step 5: Here the read storage permission result will be handled. And further execution will be done.)
    // START
    /**
     * This function will notify the user after tapping on allow or deny
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@CreateBoardActivity)
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
    // END

    // TODO (Step 6: Get the result of the image selection based on the constant code.)
    // START
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
                && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
                && data!!.data != null
        ) {
            mSelectedImageFileUri = data.data

            try {
                // Load the board image in the ImageView.
                Glide
                        .with(this@CreateBoardActivity)
                        .load(Uri.parse(mSelectedImageFileUri.toString())) // URI of the image
                        .centerCrop() // Scale type of the image.
                        .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                        .into(iv_board_image) // the view in which the image will be loaded.
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    // END

    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbar_create_board_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_create_board_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun createBoard(){
        val assignedUsersArrayList : ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserId())

        var board = Board(etBoardName.text.toString(),
            boardImageUrl.toString(), userName, assignedUsersArrayList)
        FirestoreClass().createBoard(this, board)

//        boardCreatedSuccessfully()

    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE" + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(this@CreateBoardActivity, mSelectedImageFileUri)
        )

        //adding the file to reference
        sRef.putFile(mSelectedImageFileUri!!)
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
                        boardImageUrl = uri.toString()

                        // Call a function to create the board.
                        createBoard()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@CreateBoardActivity,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()

                hideProgressDialog()
            }
    }
    // END
}