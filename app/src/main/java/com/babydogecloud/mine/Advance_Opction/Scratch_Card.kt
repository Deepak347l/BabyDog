package com.babydogecloud.mine.Advance_Opction

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anupkumarpanwar.scratchview.ScratchView
import com.babydogecloud.mine.databinding.ActivityScratchCardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.HashMap

class Scratch_Card : AppCompatActivity() {
    private lateinit var binding:ActivityScratchCardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScratchCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //availble spin
        FirebaseDatabase.getInstance().getReference("user").child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val income = snapshot.child("freeScratch").value.toString().toInt()
                        binding.spinCountsnew.text = income.toString()
                    }catch(e:Exception){
                        Log.e("error", e.message.toString())
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("error", error.message.toString())
                }
            })
        binding.scratchViewwin.text =  "15,000"+" Tokens"
        binding.scratchView.setRevealListener(object : ScratchView.IRevealListener {
            override fun onRevealed(scratchView: ScratchView) {
                //create custom dialog
                    FirebaseDatabase.getInstance().getReference("user")
                        .child(FirebaseAuth.getInstance().currentUser?.uid.toString()).addListenerForSingleValueEvent(object:
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                try {
                                    val incomex =
                                        snapshot.child("freeScratch").value.toString().toInt()
                                    if (incomex > 0) {
                                        income(incomex, snapshot)
                                    } else {
                                        Toast.makeText(
                                            this@Scratch_Card,
                                            "Limit Over",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("error", e.message.toString())
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.e("error", error.message.toString())
                            }
                        })
                    // method to close our dialog.
                    finish()
            }
            override fun onRevealPercentChangedListener(scratchView: ScratchView, percent: Float) {}
        })
    }

    private fun income(incomex: Int, snapshot: DataSnapshot) {
        val fincome = incomex - 1
        val hashMap1 = HashMap<String, Any>()
        hashMap1.put("freeScratch", fincome.toString())
        FirebaseDatabase.getInstance().getReference("user")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .updateChildren(hashMap1)
        val income =
            snapshot.child("income").value.toString().toInt()
        val finalIncome = income + 15000
        val hashMap = HashMap<String, Any>()
        hashMap.put("income", finalIncome.toString())
        FirebaseDatabase.getInstance().getReference("user")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .updateChildren(hashMap)
    }
}