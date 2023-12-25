package com.babydogecloud.mine.Fragments


import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.babydogecloud.mine.Advance_Opction.withdralhistory
import com.babydogecloud.mine.R
import com.babydogecloud.mine.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Profile : Fragment() {
    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        FirebaseDatabase.getInstance().getReference("user").child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val income = snapshot.child("user_pic").value.toString()
                        val email = snapshot.child("email").value.toString()
                        Glide.with(context!!).load(income).circleCrop().into(binding.profieImagenew)
                        binding.emailHolder.text = email
                    }catch(e:Exception){
                        Log.e("error", e.message.toString())
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("error", error.message.toString())
                }
            })
        binding.btn1.setOnClickListener {
            Toast.makeText(context,"GO PLAYSTORE AND RATEUS",Toast.LENGTH_SHORT).show()
        }
        binding.btn2.setOnClickListener {
            val s = Wallet()
            setCurrentFragment(s)
        }
        binding.btn4.setOnClickListener {
            val intent = Intent(context, withdralhistory::class.java)
            startActivity(intent)
        }
        binding.btn5.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/babydogeminer"))
            startActivity(intent)
        }
        binding.btn6.setOnClickListener {
            val dialog = Dialog(context!!)
            dialog.setContentView(R.layout.activity_term__condition)
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.setCancelable(false)
            dialog.window!!.attributes.windowAnimations = R.style.animation
            val cancel_btn  = dialog.findViewById<TextView>(R.id.cancel)
            val sumbit_btn  = dialog.findViewById<TextView>(R.id.save)
            sumbit_btn.setOnClickListener {
                dialog.dismiss()
            }
            cancel_btn.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
        binding.btn7.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.freeprivacypolicy.com/live/210df4d9-5092-4698-bb1f-ef71492e968e"))
            startActivity(intent)
        }
        return binding.root
    }
    private fun setCurrentFragment(s: Wallet) {
        val transaction = fragmentManager?.beginTransaction()
        transaction?.setCustomAnimations(
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
        transaction?.replace(R.id.frameLayout, s)
        transaction?.commit()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}