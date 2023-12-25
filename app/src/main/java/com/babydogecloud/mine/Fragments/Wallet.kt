package com.babydogecloud.mine.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.babydogecloud.mine.databinding.FragmentWalletBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class Wallet : Fragment() {
    private var _binding : FragmentWalletBinding? = null
    private val binding get() = _binding!!
    private var mRequestQueue: RequestQueue? = null
    private var jsonObjectRequest: JsonObjectRequest? = null
    private val TELEGRAM_API = "https://api.telegram.org/bot6914913021:AAG2fuTSXDjOfT1k_KRquX8CwtkFHyOjaGQ/sendMessage?chat_id=@telsjsjjs&text="
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        //balance details retrive for top
        FirebaseDatabase.getInstance().getReference("user").child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val income = snapshot.child("income").value.toString().toInt()
                        binding.availToken2.text = income.toString() + " Tokens"
                    }catch(e:Exception){
                        Log.e("error", e.message.toString())
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("error", error.message.toString())
                }
            })
        binding.withdrawlbtn2.setOnClickListener {
            validdata()
        }
        return binding.root
    }

    private fun validdata() {
        val PaytmNumber = binding.usernameWWUU2.text.toString()
        val Ammount = binding.usernameWWUU12.text.toString()
        if (PaytmNumber.isEmpty()){
            binding.usernameWWUU2.setError("required")
            binding.usernameWWUU2.requestFocus()
        }
        else if(Ammount.isEmpty()){
            binding.usernameWWUU12.setError("required")
            binding.usernameWWUU12.requestFocus()
        }
        else{
            wthdraw()
        }
    }

    private fun wthdraw() {
        FirebaseDatabase.getInstance().getReference("user")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString()).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val mAmmount = binding.usernameWWUU12.text.toString().toInt()
                        val income =  snapshot.child("income").value.toString().toInt()
                        val iddata = snapshot.child("id").value.toString()
                        if (income < 2500000.toInt()){
                            Toast.makeText(context,"Minimum Token Required 2,500,000",Toast.LENGTH_SHORT).show()
                        }else if(mAmmount >= 2500000.toInt()){
                            val Ammount = binding.usernameWWUU12.text.toString().toInt()
                            if (Ammount <= income){
                                val sdf = SimpleDateFormat("dd/MM/yyyy")
                                val curDate = sdf.format(Date())
                                val updateAmounta = income-Ammount
                                val hashMap = HashMap<String,Any>()
                                hashMap.put("paytm",binding.usernameWWUU2.getText().toString())
                                hashMap.put("uid",FirebaseAuth.getInstance().currentUser?.uid.toString())
                                hashMap.put("id",iddata.toString())
                                hashMap.put("status",false)
                                hashMap.put("Amount",Ammount.toString())
                                hashMap.put("date", curDate.toString())
                                Toast.makeText(context,"Credited within 48 hours",Toast.LENGTH_SHORT).show()
                                FirebaseDatabase.getInstance().getReference("Withdrawl_Requests")
                                    .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                    .push()
                                    .setValue(hashMap)
                                val hashMap1 = HashMap<String, Any>()
                                hashMap1.put("income", updateAmounta.toString())
                                FirebaseDatabase.getInstance().getReference("user")
                                    .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                    .updateChildren(hashMap1)

                                //set allrat for telegram bot
                                allrattelegramBot()
                            }else{
                                Toast.makeText(context,"Insuficent Balance",Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(context,"Please Enter Minimum 2,500,000",Toast.LENGTH_SHORT).show()
                        }
                    }catch(e:Exception){
                        Log.e("error", e.message.toString())
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context,error.message.toString(), Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun allrattelegramBot() {
       //using telegram bot api we do this excution
        val email = FirebaseAuth.getInstance().currentUser?.email.toString()
        val uid = binding.usernameWWUU2.getText().toString()
        val Ammount = binding.usernameWWUU12.text.toString()

        //call newtwork api
        mRequestQueue    = Volley.newRequestQueue(context)
        jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            TELEGRAM_API+"Email%20=%20["+email+"]"+"\nAmmount%20=%20["+Ammount+"]"+"\nUid%20=%20["+uid+"]",
            null,
            Response.Listener
            { response ->
                Log.d("sucsess", response.toString())
            }, Response.ErrorListener {
                Log.d("error", it.message.toString())
            })
        mRequestQueue!!.add(jsonObjectRequest)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
  }
