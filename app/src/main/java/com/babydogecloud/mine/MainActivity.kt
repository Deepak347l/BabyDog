package com.babydogecloud.mine

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.babydogecloud.mine.Fragments.Home
import com.babydogecloud.mine.Fragments.Profile
import com.babydogecloud.mine.Fragments.Spin
import com.babydogecloud.mine.Fragments.Wallet
import com.babydogecloud.mine.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var isUserligal = false
    private var mRequestQueue: RequestQueue? = null
    private var jsonObjectRequest: JsonObjectRequest? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //ads sdk intilized
        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance( this@MainActivity ).setMediationProvider( "max" )
        AppLovinSdk.getInstance( this@MainActivity ).initializeSdk({ configuration: AppLovinSdkConfiguration ->
            // AppLovin SDK is initialized, start loading ads
        })
        //first we check user date and
        FirebaseDatabase.getInstance().getReference("user")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString()).addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    try
                    {
                      val curDate = snapshot.child("firstUserLogin").value.toString()
                      isUserligal = snapshot.child("banUser").value.toString().toBoolean()
                        val link = "https://www.timeapi.io/api/Time/current/coordinate?latitude=22.5726&longitude=88.3639"
                        mRequestQueue = Volley.newRequestQueue(this@MainActivity)
                        jsonObjectRequest = JsonObjectRequest(
                            Request.Method.GET,
                            link,
                            null,
                            Response.Listener
                            { response ->
                                try {
                                    //save
                                    val serverDate = response.getString("date") // dd/mm/yy
                                    if (curDate != serverDate){
                                        val hashMap = HashMap<String, Any>()
                                        hashMap.put("freeSpin", "1")
                                        hashMap.put("freeScratch", "1")
                                        hashMap.put("firstUserLogin",serverDate)
                                        FirebaseDatabase.getInstance().getReference("user")
                                            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                            .updateChildren(hashMap)
                                    }else{
                                        Log.d("tag","sucess")
                                    }
                                }catch (e:Exception){}
                            },
                            Response.ErrorListener {
                                Log.d("error", it.message.toString())
                            })
                        mRequestQueue!!.add(jsonObjectRequest)
                    }
                    catch (e:Exception)
                    {
                        Log.d("tag1",e.message.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                   Log.d("tag",error.message)
                }

            })
        //call out botom nev
          val home = Home()
          val wallet = Wallet()
          val spin = Spin()
          val profile = Profile()
        setCurrentFragment(home)
        binding.tabLayout.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.page_1-> setCurrentFragment(home)
                R.id.page_2-> setCurrentFragment(wallet)
                R.id.page_3-> setCurrentFragment(spin)
                R.id.page_4-> setCurrentFragment(profile)
            }
            true
        }
        //call our fragement and set in textview for timer
    }

    private fun setCurrentFragment(home: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
        transaction.replace(binding.frameLayout.id, home)
        transaction.commit()
    }

    override fun onStart() {
        super.onStart()
        if (isUserligal == true){
            finish()
        }else{
            Log.d("tag","sucess")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}