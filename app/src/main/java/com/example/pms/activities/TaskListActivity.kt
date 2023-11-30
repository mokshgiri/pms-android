package com.example.pms.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pms.R
import com.example.pms.adapters.TaskListItemsAdapter
import com.example.pms.firebase.FirestoreClass
import com.example.pms.models.Board
import com.example.pms.models.Card
import com.example.pms.models.Task
import com.example.pms.models.User
import com.example.pms.utils.Constants

class TaskListActivity : BaseActivity() {

    private lateinit var toolbar : Toolbar
    private lateinit var rv_task_list : RecyclerView
    private lateinit var boardDetails : Board
    private lateinit var boardDocumentId : String
    lateinit var assignedMembersDetailsList : ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        initializeViews()

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, boardDocumentId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)

        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardDetails(this, boardDocumentId)
        }
        else{
            Log.e("cancelled","cancelled" )
        }
    }

    fun boardDetails(board : Board){
        boardDetails = board

        hideProgressDialog()
        setUpActionbar()


        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this, boardDetails.assignedTo)
    }

    fun boardMembersDetailsList(list: ArrayList<User>){
        assignedMembersDetailsList = list
        hideProgressDialog()

        val addTaskList = Task(resources.getString(R.string.add_list))
        boardDetails.taskList.add(addTaskList)

        rv_task_list.layoutManager= LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_task_list.setHasFixedSize(true)

        val adapter =  TaskListItemsAdapter(this, boardDetails.taskList)
        rv_task_list.adapter = adapter
    }

    private fun setUpActionbar() {
        setSupportActionBar(toolbar)

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        actionbar?.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        actionbar?.title = boardDetails.name

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar_task_list_activity)
        rv_task_list = findViewById(R.id.rv_task_list)
    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, boardDetails.documentId.toString())
    }

    fun createTaskList(taskListName : String){
        val task = Task(taskListName, FirestoreClass().getCurrentUserId())

        boardDetails.taskList.add(0, task)
        boardDetails.taskList.removeAt(boardDetails.taskList.size  - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)

        boardDetails.taskList[position] = task
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }

    fun deleteTaskList(position: Int) {
        boardDetails.taskList.removeAt(position)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        val cardAssignedUserList : ArrayList<String> = ArrayList()
        cardAssignedUserList.add(FirestoreClass().getCurrentUserId())

        val card = Card(cardName, FirestoreClass().getCurrentUserId(), cardAssignedUserList)

        val cardList = boardDetails.taskList[position].cards
        cardList.add(card)

        val task = Task(boardDetails.taskList[position].title,
            boardDetails.taskList[position].createdBy,
            cardList
            )

        boardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }

    fun cardDetails(taskListPosition : Int, cardPosition : Int){
        val intent = Intent(this@TaskListActivity, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, assignedMembersDetailsList)

//        Toast.makeText(this, assignedMembersDetailsList.toString(), Toast.LENGTH_SHORT).show()
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards : ArrayList<Card>){
        boardDetails.taskList.removeAt(boardDetails.taskList.size-1)
        boardDetails.taskList[taskListPosition].cards = cards

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }


    companion object {
         const val MEMBERS_REQUEST_CODE : Int = 13
         const val CARD_DETAILS_REQUEST_CODE : Int = 14
    }

}