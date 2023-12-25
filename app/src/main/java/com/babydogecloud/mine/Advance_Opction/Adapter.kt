package com.babydogecloud.mine.Advance_Opction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.view.View
import android.widget.TextView
import com.babydogecloud.mine.R


class Adapter(coursesArrayList: ArrayList<Model>, context: Context) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {
    // creating variables for our ArrayList and context
    private val coursesArrayList: ArrayList<Model>
    private val context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // passing our layout file for displaying our card item
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.withdral_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // setting data to our text views from our modal class.
        val courses = coursesArrayList[position]
        holder.courseDescTV.setText("Amount : "+courses.Amount)
        if (courses.status == false){
            holder.courseNameTV.setText("Status : "+"Panding")
        }
        else{
            holder.courseNameTV.setText("Status : "+"Successful")
        }
        holder.courseDescTVnew.setText("Lbank : "+ courses.paytm)
        holder.courseDurationTV.text = "Date : "+courses.date
    }

    override fun getItemCount(): Int {
        // returning the size of our array list.
        return coursesArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // creating variables for our text views.
        val courseNameTV: TextView
        val courseDurationTV: TextView
        val courseDescTV: TextView
        val courseDescTVnew: TextView

        init {
            // initializing our text views.
            courseNameTV = itemView.findViewById(R.id.head1)
            courseDurationTV = itemView.findViewById(R.id.desc1)
            courseDescTV = itemView.findViewById(R.id.desc12)
            courseDescTVnew = itemView.findViewById(R.id.desc123)
        }
    }

    // creating constructor for our adapter class
    init {
        this.coursesArrayList = coursesArrayList
        this.context = context
    }
}