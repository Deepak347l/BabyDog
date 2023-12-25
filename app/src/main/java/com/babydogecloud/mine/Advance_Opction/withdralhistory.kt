package com.babydogecloud.mine.Advance_Opction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.babydogecloud.mine.databinding.ActivityWithdralhistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.annotations.Nullable

class withdralhistory : AppCompatActivity() {
    private lateinit var  withdralarray: ArrayList<Model>
    private lateinit var withdraladapter: Adapter
    private lateinit var binding:ActivityWithdralhistoryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdralhistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //set array for recylerview
        withdralarray = ArrayList()
        withdraladapter = Adapter(withdralarray, this)
        binding.rcv.layoutManager = LinearLayoutManager(this)
        binding.rcv.adapter = withdraladapter
        //now we call methode for get data
        getData()
    }
    private fun getData() {
        FirebaseDatabase.getInstance().getReference("Withdrawl_Requests")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addChildEventListener(object :
                ChildEventListener {
                override fun onChildAdded(
                    snapshot: DataSnapshot,
                    @Nullable previousChildName: String?
                ) {
                    // on below line we are hiding our progress bar.
                    binding.test.visibility = View.GONE
                    snapshot.getValue(Model::class.java)?.let { withdralarray.add(it) }
                    // notifying our adapter that data has changed.
                    withdraladapter.notifyDataSetChanged()
                }
                override fun onChildChanged(
                    snapshot: DataSnapshot,
                    @Nullable previousChildName: String?
                ) {
                    // this method is called when new child is added
                    // we are notifying our adapter and making progress bar
                    // visibility as gone.
                    binding.test.visibility = View.GONE
                    withdraladapter.notifyDataSetChanged()
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // notifying our adapter when child is removed.
                    withdraladapter.notifyDataSetChanged()
                    binding.test.visibility = View.GONE
                }

                override fun onChildMoved(
                    snapshot: DataSnapshot,
                    @Nullable previousChildName: String?
                ) {
                    // notifying our adapter when child is moved.
                    withdraladapter.notifyDataSetChanged()
                    binding.test.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@withdralhistory,
                        "Fail to get the data" + error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}