package com.babydogecloud.mine

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.babydogecloud.mine.databinding.ActivitySignupBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class Signup : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val RC_SIGN_IN: Int = 123
    private val TAG = "SignInActivity Tag"
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth
        //handele for click on signup device
        binding.signInButton.setOnClickListener {
            signin()
        }
        binding.signInButton2.setOnClickListener {
           auth.signInAnonymously().addOnCompleteListener(this){task ->
               if(task.isSuccessful){
                   val firebaseUser = auth.currentUser
                   updateUI(firebaseUser)
               }
               else{
                   updateUI(null)
               }
           }
        }
    }
    override fun onStart() {
        super.onStart()
        val currentuser = auth.currentUser
        if (currentuser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun signin() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account =
                completedTask.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode + e.message.toString())

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        binding.signInButton.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful){
                if (task.result.additionalUserInfo?.isNewUser == true){
                    val firebaseUser = auth.currentUser
                    updateUI(firebaseUser)
                }
                else {
                    val mainActivityIntent = Intent(this@Signup, MainActivity::class.java)
                    startActivity(mainActivityIntent)
                    finish()
                }
            }else {
                updateUI(null)
            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if(firebaseUser != null) {
            val curentdeviseid = android.provider.Settings.Secure.getString(this@Signup.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
            val AlphaNumericString = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    + "0123456789"
                    + "abcdefghijklmnopqrstuvxyz")
            val sb: StringBuilder = StringBuilder(10)
            for (i in 0 until 10) {
                // generate a random number between
                // 0 to AlphaNumericString variable length
                val index = (AlphaNumericString.length
                        * Math.random()).toInt()
                // add Character one by one in end of sb
                sb.append(AlphaNumericString[index])
            }
            val sdf = SimpleDateFormat("MM/dd/yyyy")
            val curDate = sdf.format(Date())
            val hashMap = HashMap<String, Any>()
            hashMap.put("uid", firebaseUser.uid)
            hashMap.put("name", firebaseUser.displayName.toString())
            hashMap.put("email", firebaseUser.email.toString())
            hashMap.put("referid", sb.toString())
            hashMap.put("divID",curentdeviseid.toString())
            hashMap.put("income", "0")
            hashMap.put("freeSpin", "1")
            hashMap.put("firstUserLogin",curDate)
            hashMap.put("banUser",false) //user if true then cant be login user
            hashMap.put("freeScratch", "1")
            hashMap.put("refered",false)
            hashMap.put("total_reffer","0")
            hashMap.put("id",sb.toString())
            hashMap.put("user_pic",firebaseUser.photoUrl.toString())
            FirebaseDatabase.getInstance().getReference("user").child(firebaseUser.uid).setValue(hashMap)
            try{
                val dialog = Dialog(this)
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
                    val mainActivityIntent = Intent(this@Signup, MainActivity::class.java)
                    startActivity(mainActivityIntent)
                    finish()
                    Toast.makeText(this@Signup, "registered", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                cancel_btn.setOnClickListener {
                    Toast.makeText(this,"Without Agreed You Not Processed Next Step!",Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    finish()
                    binding.signInButton.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    dialog.dismiss()
                }
                dialog.show()
            }catch(e:Exception){ }
        } else {
            binding.signInButton.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}