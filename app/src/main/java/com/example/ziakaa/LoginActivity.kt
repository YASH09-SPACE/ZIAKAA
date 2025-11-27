 package com.example.ziakaa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.ziakaa.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database


 class LoginActivity : AppCompatActivity() {
     private lateinit var email: String
     private lateinit var password: String
     private lateinit var auth: FirebaseAuth
     private lateinit var database: DatabaseReference
     private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val googleSignInOptions = GoogleSignInOptions. Builder (GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken (getString(R.string.default_web_client_id)).requestEmail().build()
// Initialization of Firebase Auth
                auth = Firebase.auth
// Initialization of Firebase Database
                database = Firebase.database.reference
// Initialization of Google
                googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        binding.loginbutton.setOnClickListener {

            email = binding.EmailAddress.text.toString().trim()
            password = binding.Password.text.toString().trim()
            if (email.isBlank()||password.isBlank()) {
                Toast.makeText(this,"Please enter all the details 🥱",Toast.LENGTH_SHORT).show()
            }else{
                createUser()
            }

        }
        binding.donthavebutton.setOnClickListener {
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
        }
        binding.googleButton.setOnClickListener {
            // Force account chooser EVERY TIME
            googleSignInClient.signOut().addOnCompleteListener {

                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }
        }
        binding.forgotPassword.setOnClickListener {
            val emailInput = binding.EmailAddress.text.toString().trim()

            if (emailInput.isEmpty()) {
                Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show()
            } else {
                resetPassword(emailInput)
            }
        }

    }
     private fun resetPassword(email: String) {
         auth.sendPasswordResetEmail(email)
             .addOnCompleteListener { task ->
                 if (task.isSuccessful) {
                     Toast.makeText(
                         this,
                         "Reset link sent to your email 🔗📩",
                         Toast.LENGTH_LONG
                     ).show()
                 } else {
                     Toast.makeText(
                         this,
                         "Failed to send reset email 😕",
                         Toast.LENGTH_SHORT
                     ).show()
                 }
             }
     }

     private val launcher = registerForActivityResult (ActivityResultContracts. StartActivityForResult()) { result ->
         if (result.resultCode == Activity. RESULT_OK) {
             val task =  GoogleSignIn.getSignedInAccountFromIntent (result.data)
             if (task.isSuccessful) {
                 //Log.d("GOOGLE_LOGIN", "Google account received")
                 val account: GoogleSignInAccount? = task.result
                 val credential: AuthCredential = GoogleAuthProvider.getCredential (account?.idToken, null)
                 auth.signInWithCredential(credential).addOnCompleteListener { task ->
                     if (task.isSuccessful) {
                         Toast.makeText(this, "Log in Successful 😊",Toast.LENGTH_SHORT).show()
                         startActivity(Intent( this, MainActivity::class.java))
                         finish()
                     } else {
                         Toast.makeText(this, "Log in failed 😒",Toast.LENGTH_SHORT).show()
                         //Log.e("GOOGLE_LOGIN", "Google SignIn failed: ", task.exception)
                     }
                 }
             }
         } else {
             Toast.makeText(this, "Log in failed 😒",Toast.LENGTH_SHORT).show()
         }
     }

     private fun createUser() {
         auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task->
             if (task.isSuccessful){
                 Toast.makeText(this,"LogIn Successful 😁",Toast.LENGTH_SHORT).show()
                 val user: FirebaseUser? = auth.currentUser
                 updateUi(user)
             }else{
                 Toast.makeText(this,"Sign-in failed 😒",Toast.LENGTH_SHORT).show()
             }
         }
     }

     override fun onStart() {
         super.onStart()
         val currentUser: FirebaseUser? = auth.currentUser
         if (currentUser != null) {
             val intent = Intent(this, MainActivity :: class.java)
             startActivity(intent)
             finish()
         }
     }


     private fun updateUi(user: FirebaseUser?) {
         val intent = Intent(this, MainActivity::class.java)
         startActivity(intent)
         finish()
     }
 }