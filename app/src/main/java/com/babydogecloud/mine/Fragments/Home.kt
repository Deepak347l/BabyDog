package com.babydogecloud.mine.Fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.concurrent.TimeUnit
import android.os.Handler
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.babydogecloud.mine.Advance_Opction.Scratch_Card
import com.babydogecloud.mine.R
import com.babydogecloud.mine.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*


class Home : Fragment(), MaxAdListener {
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    val BASE_URL = "https://countdowntimer-cryptoemining.onrender.com"
    //varables for futional activitys
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var countdownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 0
    private var timerRunning: Boolean = false
    private var curentTiming: Long = 0

    private lateinit var interstitialAd: MaxInterstitialAd
    private var retryAttempt = 0.0
    var adCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        //ads setup
        interstitialAd = MaxInterstitialAd( "76ae1b36f08e8245", context as Activity?)
        interstitialAd.setListener( this )

        // Load the first ad
        interstitialAd.loadAd()
        //shared pref data retiveral
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        // Restore timer state
        timerRunning = sharedPreferences.getBoolean("timerRunning", false)
        timeLeftInMillis = sharedPreferences.getLong("timeLeftInMillis", 3600000)
        //timer runing then do this
        if (timerRunning) {
            curentTiming = sharedPreferences.getLong("endTime", 3600000)//86400000
            timeLeftInMillis = curentTiming - System.currentTimeMillis()
            if (timeLeftInMillis < 0) {
                timeLeftInMillis = 3600000 //86400000
                //here handle for when user update there balance
                FirebaseDatabase.getInstance().getReference("user")
                    .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            try {
                                val income = snapshot.child("income").value.toString().toInt()
                                val finalIncome = income + 15000
                                val hashMap = HashMap<String, Any>()
                                hashMap.put("income", finalIncome.toString())
                                FirebaseDatabase.getInstance()
                                    .getReference("user")
                                    .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                    .updateChildren(hashMap)
                                //custom message
                                try {
                                    val dialog = Dialog(context!!)
                                    dialog.setContentView(R.layout.custom_dialog)
                                    dialog.window!!.setLayout(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    dialog.setCancelable(false)
                                    dialog.window!!.attributes.windowAnimations = R.style.animation
                                    val okay_text = dialog.findViewById<TextView>(R.id.okay_text)
                                    val des_text = dialog.findViewById<TextView>(R.id.textview)
                                    des_text.text = "Congratulations!! Your token added successfully in your wallet"
                                    okay_text.text = "CLOSE"
                                    okay_text.setOnClickListener {
                                        //balance add   code
                                        dialog.dismiss()
                                    }
                                    dialog.show()
                                } catch (e: Exception) {
                                    Log.e("error", e.message.toString())
                                }
                            } catch (e: Exception) {
                                Log.e("error", e.message.toString())
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("error", error.message.toString())
                        }
                    })
                // is this extra code for add balance when timer comlete and our app closed
                timerRunning = false
            }else {
                startTimer()
            }
        }
            //btn clicks
            binding.bn1.setOnClickListener {
                if ( interstitialAd.isReady() )
                {
                    adCount = adCount + 2
                    interstitialAd.showAd();
                }
                else
                {
                    val scratchActivity = Intent(context, Scratch_Card::class.java)
                    startActivity(scratchActivity)
                }
            }
            binding.bn4.setOnClickListener {
             //continue...
             Toast.makeText(context, "Comming Soon!", Toast.LENGTH_SHORT).show()
            }
            binding.timer.setOnClickListener {
                if (!timerRunning) {
                    if ( interstitialAd.isReady() )
                    {
                        adCount = adCount + 1
                        interstitialAd.showAd();
                    }
                    else
                    {
                        startTimer()
                    }
                }
                binding.timer.isEnabled = false
            }
            //now we call api for countsown timer
            //check the user fiest tieme or last user
            //balance details retrive for top
            FirebaseDatabase.getInstance().getReference("user")
                .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            val income = snapshot.child("income").value.toString().toInt()
                            binding.avlToken.text = income.toString() + " Tokens"
                        } catch (e: Exception) {
                            Log.e("error", e.message.toString())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("error", error.message.toString())
                    }
                })
            //here we call api and set for timer
        return binding.root
    }
    private fun startTimer() {
        curentTiming = System.currentTimeMillis() + timeLeftInMillis
        countdownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerUI()
                //we run handeler which update every secound balance
            }

            override fun onFinish() {
                timerRunning = false
                timeLeftInMillis = 3600000     //86400000
                binding.timer.isEnabled = true
                binding.timer.text = "START"
                //here we update our text box
                updateBalance()
            }
        }.start()
        timerRunning = true
    }

    private fun updateBalance() {
        //we add balane here
        FirebaseDatabase.getInstance().getReference("user")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val income =
                            snapshot.child("income").value.toString()
                                .toInt()
                        val finalIncome = income + 15000
                        val hashMap = HashMap<String, Any>()
                        hashMap.put("income", finalIncome.toString())
                        FirebaseDatabase.getInstance()
                            .getReference("user")
                            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                            .updateChildren(hashMap)
                        //custom message
                        try {
                            val dialog = Dialog(context!!)
                            dialog.setContentView(R.layout.custom_dialog)
                            dialog.window!!.setLayout(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            dialog.setCancelable(false)
                            dialog.window!!.attributes.windowAnimations = R.style.animation
                            val okay_text = dialog.findViewById<TextView>(R.id.okay_text)
                            val des_text = dialog.findViewById<TextView>(R.id.textview)
                            des_text.text =
                                "Congratulations!! Your token added successfully in your wallet"
                            okay_text.text = "CLOSE"
                            okay_text.setOnClickListener {
                                //balance add   code
                                dialog.dismiss()
                            }
                            dialog.show()
                        } catch (e: Exception) {
                            Log.e("error", e.message.toString())
                        }
                    } catch (e: Exception) {
                        Log.e("error", e.message.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("error", error.message.toString())
                }
            })
    }

    private fun updateTimerUI() {
        try {
            val hours = (timeLeftInMillis / 1000) / 3600
            val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
            val seconds = (timeLeftInMillis / 1000) % 60
            val timeLeftFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            binding.timer.text = timeLeftFormatted
        }catch (e: Exception){
        }
    }

    //ads callback methods


    override fun onPause() {
        super.onPause()
        // Save timer state
        val editor = sharedPreferences.edit()
        editor.putBoolean("timerRunning", timerRunning)
        editor.putLong("timeLeftInMillis", timeLeftInMillis)
        editor.putLong("endTime", curentTiming)
        editor.apply()
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onAdLoaded(ad: MaxAd?) {
        Log.d("ads","adLOaded")
        retryAttempt = 0.0
    }

    override fun onAdDisplayed(ad: MaxAd?) {
        Log.d("ads","adDisplayed")
    }

    override fun onAdHidden(ad: MaxAd?) {
        if (adCount == 1) {
            startTimer()
        }else{
            val scratchActivity = Intent(context, Scratch_Card::class.java)
            startActivity(scratchActivity)
        }
    }

    override fun onAdClicked(ad: MaxAd?) {
        Log.d("ads","adClicked")
    }

    override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
        // Interstitial ad failed to load
        // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)
        retryAttempt++
        val delayMillis = TimeUnit.SECONDS.toMillis( Math.pow( 2.0, Math.min( 6.0, retryAttempt ) ).toLong() )

        Handler().postDelayed( { interstitialAd.loadAd() }, delayMillis )
    }

    override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
        interstitialAd.loadAd()
    }
}