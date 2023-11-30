package com.example.pms.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.pms.activities.CardDetailsActivity
import com.example.pms.activities.CreateBoardActivity
import com.example.pms.activities.MainActivity
import com.example.pms.activities.MembersActivity
import com.example.pms.activities.MyProfileActivity
import com.example.pms.activities.SignInActivity
import com.example.pms.activities.SignUpActivity
import com.example.pms.activities.TaskListActivity
import com.example.pms.models.Board
import com.example.pms.models.User
import com.example.pms.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject

class FirestoreClass {

    private val fireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity : SignUpActivity, userInfo : User){
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
    }

    fun loadUserData(activity : Activity, readBoardsList : Boolean = false){
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)

                when(activity){
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser!!)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser!!, readBoardsList)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUi(loggedInUser!!)
                    }
                    }
                }.addOnFailureListener {
                when(activity){
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
            }

            }
    }

    fun updateUserProfileData(activity: Activity, userHashMap : HashMap<String, Any>){
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()

                if (activity is MyProfileActivity) {
                    activity.profileUpdateSuccess()
                }
                else if (activity is MainActivity){
                    activity.tokenUpdateSuccess()
                }
            }.addOnFailureListener {
                e ->
                if (activity is MyProfileActivity) {
                    activity.hideProgressDialog()
                }
                else if (activity is MainActivity){
                    activity.hideProgressDialog()
                }
                Toast.makeText(activity, "Some error while updating the profile", Toast.LENGTH_SHORT).show()

            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board){
        fireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,"Board created successfully")
                Toast.makeText(activity
                    ,"Board created successfully", Toast.LENGTH_SHORT).show()

                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                exception ->
                Log.e(activity.javaClass.simpleName, "Error while creating a board", exception)
            }
    }

    fun getBoardsList(activity: MainActivity) {

        // The collection name for BOARDS
        fireStore.collection(Constants.BOARDS)
            // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->
                // Here we get the list of boards in the form of documents.
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                // Here we have created a new instance for Boards ArrayList.
                val boardsList: ArrayList<Board> = ArrayList()

                // A for loop as per the list of documents to convert them into Boards ArrayList.
                for (i in document.documents) {

                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id

                    boardsList.add(board)
                }

                // Here pass the result to the base activity.
                activity.populateBoardsListToUI(boardsList)
            }
            .addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    fun getCurrentUserId() : String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }

        return currentUserId
    }

    fun getBoardDetails(activity: TaskListActivity, boardDocumentId: String) {
        // The collection name for BOARDS
        fireStore.collection(Constants.BOARDS)
            // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
            .document(boardDocumentId)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->
                // Here we get the list of boards in the form of documents.
                Log.e(activity.javaClass.simpleName, document.toString())

                val board = document.toObject(Board::class.java)
                board?.documentId = document.id
                activity.boardDetails(board!!)

            }
            .addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        fireStore.collection(Constants.BOARDS).document(board.documentId.toString())
            .update(taskListHashMap).addOnSuccessListener {

                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                }
                else if (activity is CardDetailsActivity){
                    activity.addUpdateTaskListSuccess()
                }
            }.addOnFailureListener {
                e ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                else if (activity is CardDetailsActivity){
                    activity.hideProgressDialog()
                }
            }
    }

    fun getAssignedMembersListDetails(
        activity: Activity, assignedTo : ArrayList<String>
    ){
        fireStore.collection(Constants.USERS).whereIn(Constants.ID, assignedTo).get()
            .addOnSuccessListener {
                document ->

                val usersList : ArrayList<User> = ArrayList()

                for(i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                if (activity is MembersActivity){
                    activity.setUpMembersList(usersList)
                }
                else if (activity is TaskListActivity){
                    activity.boardMembersDetailsList(usersList)
                }
            }.addOnFailureListener{
                if (activity is MembersActivity) {
                    activity.hideProgressDialog()
                }
                else if (activity is TaskListActivity){
                    activity.hideProgressDialog()
                }
            }
    }

    fun getMemberDetails(activity: MembersActivity, email : String) {
        fireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }
    }

    fun assignMembersToBoard(activity: MembersActivity, board: Board, user: User){
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        fireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignedSuccess(user)
            }.addOnFailureListener{
                activity.hideProgressDialog()
            }
    }
}