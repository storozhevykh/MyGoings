package com.storozhevykh.mygoings.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.storozhevykh.mygoings.R
import com.storozhevykh.mygoings.model.Going

class GoingsListAdapter(goingsList: List<Going>): RecyclerView.Adapter<GoingsListAdapter.ViewHolder>() {

    private val goings = goingsList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.going_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, goings.get(position))
    }

    override fun getItemCount(): Int {
        return goings.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)

        fun bind(position: Int, going: Going) {
            val text: TextView = itemView.findViewById(R.id.cardText)
            text.text = going.title
        }
    }

}