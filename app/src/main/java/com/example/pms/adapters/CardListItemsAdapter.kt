package com.example.pms.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pms.R
import com.example.pms.activities.TaskListActivity
import com.example.pms.models.Card
import com.example.pms.models.SelectedMembers


open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            if (model.labelColor.isNotEmpty()) {
                holder.view_label_color.visibility = View.VISIBLE
                holder.view_label_color.setBackgroundColor(Color.parseColor(model.labelColor))
            } else {
                holder.view_label_color.visibility = View.GONE
            }

            holder.tv_card_name.text = model.name

            if ((context as TaskListActivity).assignedMembersDetailsList.size > 0){
                val selectedMembersList : ArrayList<SelectedMembers> = ArrayList()

                for (i in context.assignedMembersDetailsList.indices){
                    for (j in model.assignedTo){
                        if (context.assignedMembersDetailsList[i].id == j){
                            val selectedMembers = SelectedMembers(context.assignedMembersDetailsList[i].id,
                                context.assignedMembersDetailsList[i].image)

                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }
                if(selectedMembersList.size > 0){
                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                        holder.rv_card_selected_members.visibility = View.GONE
                    }
                    else{
                        holder.rv_card_selected_members.visibility = View.VISIBLE
                        holder.rv_card_selected_members.layoutManager = GridLayoutManager(context, 4)

                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                        holder.rv_card_selected_members.adapter = adapter

                        adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                            override fun onClick() {
                                if (onClickListener != null){
                                    onClickListener!!.onClick(position)
                                }
                            }

                        })
                    }
                }

                else{
                    holder.rv_card_selected_members.visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    onClickListener!!.onClick(position)
                }
            }

//            // TODO (Step 3: Now with use of public list of Assigned members detail List populate the recyclerView for Assigned Members.)
//            // START
//            if ((context as TaskListActivity).mAssignedMembersDetailList.size > 0) {
//                // A instance of selected members list.
//                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
//
//                // Here we got the detail list of members and add it to the selected members list as required.
//                for (i in context.mAssignedMembersDetailList.indices) {
//                    for (j in model.assignedTo) {
//                        if (context.mAssignedMembersDetailList[i].id == j) {
//                            val selectedMember = SelectedMembers(
//                                context.mAssignedMembersDetailList[i].id,
//                                context.mAssignedMembersDetailList[i].image
//                            )
//
//                            selectedMembersList.add(selectedMember)
//                        }
//                    }
//                }
//
//                if (selectedMembersList.size > 0) {
//
//                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
//                        holder.itemView.rv_card_selected_members_list.visibility = View.GONE
//                    } else {
//                        holder.itemView.rv_card_selected_members_list.visibility = View.VISIBLE
//
//                        holder.itemView.rv_card_selected_members_list.layoutManager =
//                            GridLayoutManager(context, 4)
//                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
//                        holder.itemView.rv_card_selected_members_list.adapter = adapter
//                        adapter.setOnClickListener(object :
//                            CardMemberListItemsAdapter.OnClickListener {
//                            override fun onClick() {
//                                if (onClickListener != null) {
//                                    onClickListener!!.onClick(position)
//                                }
//                            }
//                        })
//                    }
//                } else {
//                    holder.itemView.rv_card_selected_members_list.visibility = View.GONE
//                }
//            }
//            // END
//
//            holder.itemView.setOnClickListener {
//                if (onClickListener != null) {
//                    onClickListener!!.onClick(position)
//                }
//            }
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A function for OnClickListener where the Interface is the expected parameter..
     */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * An interface for onclick items.
     */
    interface OnClickListener {
        fun onClick(cardPosition: Int)
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val view_label_color : View = itemView.findViewById(R.id.view_label_color)
        val tv_card_name : TextView = itemView.findViewById(R.id.tv_card_name)
        val rv_card_selected_members : RecyclerView = itemView.findViewById(R.id.rv_card_selected_members)
    }
}