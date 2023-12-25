package com.babydogecloud.mine.Fragments

import android.app.Activity
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.babydogecloud.mine.R
import com.babydogecloud.mine.databinding.FragmentSpinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


class Spin : Fragment(), Animation.AnimationListener, MaxAdListener {
    private var mRequestQueue: RequestQueue? = null
    private var jsonObjectRequest: JsonObjectRequest? = null
    private var _binding : FragmentSpinBinding? = null
    private val binding get() = _binding!!
    var count = 0
    var flag = false
    lateinit var wonamt:String

    private lateinit var interstitialAd: MaxInterstitialAd
    private var retryAttempt = 0.0
    var adCount = 0
    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSpinBinding.inflate(inflater, container, false)
        //ads setup
        interstitialAd = MaxInterstitialAd( "98e88bc92fed8dab", context as Activity?)
        interstitialAd.setListener( this )

        // Load the first ad
        interstitialAd.loadAd()

        binding.spinBtn.setOnTouchListener(PowerTouchListener())
        intspiner()
        //availble spin
        FirebaseDatabase.getInstance().getReference("user").child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val income = snapshot.child("freeSpin").value.toString().toInt()
                        binding.spinCount.text = income.toString()
                    }catch(e:Exception){
                        Log.e("error", e.message.toString())
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("error", error.message.toString())
                }
            })
        return binding.root
    }
    val prizes = intArrayOf(10000, 20000, 30000, 40000, 50000)
    var mSpinDuration:Long = 0
    var mSpinRevolution = 0f
    private fun intspiner() {}
    fun startSpiner(){
        mSpinRevolution = 3600f
        mSpinDuration = 5000
        if (count >= 30){
            mSpinDuration = 1000
            mSpinRevolution = (3600 * 2).toFloat()
        }
        if (count >= 60){
            mSpinDuration = 15000
            mSpinRevolution = (3600 * 3).toFloat()
        }
        val end = Math.floor(Math.random() * 3600).toInt()
        val numberOfPrizes = prizes.size
        val degreesPerPrize = 360/numberOfPrizes
        val shift = 0
        val prizeIndex = (shift + end) % numberOfPrizes

        wonamt = prizes[prizeIndex].toString()
        val rotateAnimation = RotateAnimation(0f,mSpinRevolution + end,
            Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f)
        rotateAnimation.interpolator = DecelerateInterpolator()
        rotateAnimation.repeatCount = 0
        rotateAnimation.duration = mSpinDuration
        rotateAnimation.setAnimationListener(this)
        rotateAnimation.fillAfter = true
        binding.spinImage.startAnimation(rotateAnimation)
    }

    override fun onAnimationStart(animation: Animation?) {}

    override fun onAnimationEnd(animation: Animation?) {
       //write our code to add prize there
        FirebaseDatabase.getInstance().getReference("user")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString()).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val income = snapshot.child("income").value.toString().toInt()
                        val finalIncome = income + wonamt.toInt()
                        val hashMap = HashMap<String,Any>()
                        hashMap.put("income",finalIncome.toString())
                        FirebaseDatabase.getInstance().getReference("user")
                            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                            .updateChildren(hashMap)
                        //shoow a congratches dialog
                        try {
                             dialog = Dialog(context!!)
                            dialog.setContentView(R.layout.custom_dialog)
                            dialog.window!!.setLayout(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            dialog.setCancelable(false)
                            dialog.window!!.attributes.windowAnimations = R.style.animation
                            val okay_text = dialog.findViewById<TextView>(R.id.okay_text)
                            val des_text = dialog.findViewById<TextView>(R.id.textview)
                            des_text.text = "Congratulations!! Your "+wonamt.toString()+"token added successfully in your wallet"
                            okay_text.text = "SPIN AGAIN"
                            okay_text.setOnClickListener{

                                if ( interstitialAd.isReady() )
                                {
                                    adCount = adCount + 3  // ADcount = 2
                                    interstitialAd.showAd();
                                }
                                else{
                                    dialog.dismiss()
                                }

                            }
                            dialog.show()
                        } catch (e: Exception) {
                            Log.e("error", e.message.toString())
                        }
                    }catch(e:Exception){
                        Log.e("error", e.message.toString())
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("error", error.message.toString())
                }
            })
        Toast.makeText(context,wonamt,Toast.LENGTH_SHORT).show()
    }

    override fun onAnimationRepeat(animation: Animation?) {}
    private inner class PowerTouchListener:View.OnTouchListener{
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {

            when(event!!.action){
                MotionEvent.ACTION_DOWN -> {
                    flag = true
                    count = 0
                    Thread{
                        while (flag){
                            count++
                            if (count == 100 ){
                                try{
                                    Thread.sleep(100)
                                }catch (e:InterruptedException){
                                    Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()
                                }
                                count = 0
                            }
                            try{
                                Thread.sleep(10)
                            }catch (e:InterruptedException){
                                Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.start()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    flag = false
                    FirebaseDatabase.getInstance().getReference("user")
                        .child(FirebaseAuth.getInstance().currentUser?.uid.toString()).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val income =  snapshot.child("freeSpin").value.toString().toInt()
                                if(income > 0){
                                    val fincome =  income - 1
                                    val hashMap = HashMap<String, Any>()
                                    hashMap.put("freeSpin", fincome.toString())
                                    FirebaseDatabase.getInstance().getReference("user")
                                        .child(FirebaseAuth.getInstance().currentUser?.uid.toString()).updateChildren(hashMap)
                                    if ( interstitialAd.isReady() )
                                    {
                                        adCount = adCount + 1  // ADcount = 1
                                        interstitialAd.showAd();
                                    }
                                    else{
                                        startSpiner()
                                    }
                                }
                                else{
                                    //shoow a congratches dialog
                                    try {
                                        dialog = Dialog(context!!)
                                        dialog.setContentView(R.layout.custom_dialog)
                                        dialog.window!!.setLayout(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                        dialog.setCancelable(false)
                                        dialog.window!!.attributes.windowAnimations = R.style.animation
                                        val okay_text = dialog.findViewById<TextView>(R.id.okay_text)
                                        val des_text = dialog.findViewById<TextView>(R.id.textview)
                                        des_text.text = "Ohh!! Your daily spin limit is over"
                                        okay_text.text = "CLOSE"
                                        okay_text.setOnClickListener{
                                            if ( interstitialAd.isReady() )
                                            {
                                                adCount = adCount + 2  // ADcount = 2
                                                interstitialAd.showAd();
                                            }
                                            else{
                                                dialog.dismiss()
                                            }
                                        }
                                        dialog.show()
                                    } catch (e: Exception) {
                                        Log.e("error", e.message.toString())
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.e("mainActivityError", error.message)
                            }
                        })
                    return false
                }
            }
            return false
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
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
        if (adCount == 1){
            startSpiner()
        }else if(adCount == 2){
            dialog.dismiss()
        }else{
            dialog.dismiss()
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