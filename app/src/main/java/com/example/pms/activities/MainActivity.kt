package com.example.pms.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pms.CurrentAssignedMemberId
import com.example.pms.R
import com.example.pms.adapters.BoardItemsAdapter
import com.example.pms.firebase.FirestoreClass
import com.example.pms.models.Board
import com.example.pms.models.User
import com.example.pms.utils.Constants
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE = 11
        const val CREATE_BOARD_REQUEST_CODE = 12
    }

    private lateinit var toolbar : Toolbar
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var userImg : CircleImageView
    private lateinit var userName : TextView
    private lateinit var fab_create_board : FloatingActionButton
    private lateinit var rv_boards_list : RecyclerView
    private lateinit var tv_no_boards_available : TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setUpActionbar()
        floatingBtnAction()

        navView.setNavigationItemSelectedListener(this)

        sharedPreferences = this.getSharedPreferences(Constants.PMS_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = sharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this, true)
        }
        else{
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        // Use the FCM token
                        updateFcmToken(token)
                        Log.d("FCM Token", token ?: "Token is null")
                    } else {
                        Log.e("FCM Token", "Error getting FCM token: ${task.exception}")
                    }
                })

        }

        FirestoreClass().loadUserData(this@MainActivity, true)

        // Access memberId from any activity
//        val memberId = CurrentAssignedMemberId().memberId
//
//        Log.d("MainActivity", "memberId: $memberId, currentUserID: ${FirestoreClass().getCurrentUserId()}")
//
//
//        if (memberId != null){
//            Log.d("MainActivity", "memberId is not null")
//            if (memberId == FirestoreClass().getCurrentUserId()){
//                Log.d("MainActivity", "memberId matches current user ID")
//                FirestoreClass().loadUserData(this@MainActivity, true)
//            }
//        }

    }

    private fun floatingBtnAction() {
        fab_create_board.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, userName.text.toString())
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)

            Log.d("btnClickedintent", "successful")
        }
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar_main_activity)
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        userImg = navView.getHeaderView(0).findViewById(R.id.iv_user_image)
        userName =  navView.getHeaderView(0).findViewById(R.id.tv_username)
        rv_boards_list = findViewById(R.id.rv_boards_list)
        tv_no_boards_available = findViewById(R.id.tv_no_boards_available)
        fab_create_board =  findViewById(R.id.fab_create_board)
    }

    fun setUpActionbar(){
        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_menu_icon)

        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {


        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else {

            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_my_profile -> {
                val intent = Intent(this@MainActivity, MyProfileActivity::class.java)
                startActivityForResult(intent, MY_PROFILE_REQUEST_CODE)

                Toast.makeText(this@MainActivity, "My Profile", Toast.LENGTH_SHORT).show()
            }

            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                sharedPreferences.edit().clear().apply()

                val intent = Intent(this@MainActivity, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intent)
                finish()

                Toast.makeText(this@MainActivity, "Sign Out", Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }
        else if (resultCode == RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getBoardsList(this)
        }
        else{
            Log.e("cancelled", "cancelled")
        }
    }
    fun updateNavigationUserDetails(user: User, readBoardsList : Boolean) {

        hideProgressDialog()

        Glide.with(this@MainActivity).load(user.image).centerCrop().placeholder(R.drawable.ic_user_place_holder).into(userImg)
        userName.text = user.name

        if (readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {

        hideProgressDialog()

        if (boardsList.size > 0) {

            rv_boards_list.visibility = View.VISIBLE
            tv_no_boards_available.visibility = View.GONE

            rv_boards_list.layoutManager = LinearLayoutManager(this@MainActivity)
            rv_boards_list.setHasFixedSize(true)

            // Create an instance of BoardItemsAdapter and pass the boardList to it.
            val adapter = BoardItemsAdapter(this@MainActivity, boardsList)
            rv_boards_list.adapter = adapter // Attach the adapter to the recyclerView.


            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }

            })

            adapter.notifyDataSetChanged()
        } else {
            rv_boards_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()

        val editor = sharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().loadUserData(this, true)
    }

    private  fun updateFcmToken(token: String){
        val userHashMap = HashMap< String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

}