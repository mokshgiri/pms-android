package com.example.pms.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.pms.R
import java.util.*

// TODO (Step 4: Create an adapter class for selection of card label color using the "item_label_color".)
// START
class LabelColorListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_label_color,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]

        if (holder is MyViewHolder) {

            holder.view_main.setBackgroundColor(Color.parseColor(item))

            if (item == mSelectedColor) {
                holder.iv_selected_color.visibility = View.VISIBLE
            } else {
                holder.iv_selected_color.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {

                if (onItemClickListener != null) {
                    onItemClickListener!!.onClick(position, item)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val view_main : View = itemView.findViewById(R.id.view_main)
        val iv_selected_color : ImageView = itemView.findViewById(R.id.iv_selected_color)
    }

    interface OnItemClickListener {

        fun onClick(position: Int, color: String)
    }
}
// END