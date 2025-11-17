package com.example.ziakaa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.ziakaa.databinding.ActivitySignBinding
import com.example.ziakaa.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database




class SignActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var username: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
   private lateinit var googleSignInClint : GoogleSignInClient
    private lateinit var binding: ActivitySignBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val googleSignInOptions: GoogleSignInOptions = GoogleSignInOptions. Builder (GoogleSignInOptions. DEFAULT_SIGN_IN)
            .requestIdToken (getString(R.string.default_web_client_id)).requestEmail().build()
        // initialize Firebase auth
        auth =  Firebase.auth
        // initialize Firebase DataBase
        database = Firebase.database.reference
        // initialize Firebase DataBase
        googleSignInClint = GoogleSignIn.getClient(this, googleSignInOptions)

        binding.createAccountButton.setOnClickListener {
            username = binding.UserName.text.toString()
            email = binding.EmailAddress.text.toString().trim()
            password = binding.Password.text.toString().trim()

            if (email.isEmpty() || password.isBlank()||username.isBlank()) {
                Toast.makeText(this,"Please Fill all the details", Toast.LENGTH_SHORT).show()
            }else{
                createAccount (email, password)
            }
        }


        binding.havebutton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.GoogleButton.setOnClickListener {
            // Force account chooser EVERY TIME
            googleSignInClint.signOut().addOnCompleteListener {

                val signInIntent = googleSignInClint.signInIntent
                launcher.launch(signInIntent)
            }
        }
//        binding.createAccountButton.setOnClickListener {
//            val intent = Intent(this, ChooseLocationActivity::class.java)
//            startActivity(intent)
//        }
    }
    // launcher for google sign-in
    private val launcher = registerForActivityResult (ActivityResultContracts. StartActivityForResult()) { result ->
        if (result.resultCode == Activity. RESULT_OK) {
            val task =  GoogleSignIn.getSignedInAccountFromIntent (result.data)
            if (task.isSuccessful) {
                //Log.d("GOOGLE_LOGIN", "Google account received")
                val account: GoogleSignInAccount? = task.result
                val credential: AuthCredential = GoogleAuthProvider.getCredential (account?.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Sign in Successful 😊",Toast.LENGTH_SHORT).show()
                        startActivity(Intent( this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Sign in failed 😒",Toast.LENGTH_SHORT).show()
                        //Log.e("GOOGLE_LOGIN", "Google SignIn failed: ", task.exception)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Sign in failed 😒",Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAccount (email: String, password: String) {
        auth.createUserWithEmailAndPassword (email, password).addOnCompleteListener {
                task ->
            if (task.isSuccessful){
                Toast.makeText(this,  "Account Created successfully", Toast.LENGTH_SHORT).show()
                saveUserData()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }else{
                Toast.makeText(this, "Account Creation Failed", Toast.LENGTH_SHORT).show()
                Log.d("Account", "createAccount: Failure", task. exception)
            }
        }
    }

    private fun saveUserData() {
    // retrieve data from input filed
        username = binding.UserName.text.toString()
        email = binding. EmailAddress.text.toString().trim()
        password = binding.Password.text.toString().trim()
        val user = UserModel(username, email, password)
        val userId : String = FirebaseAuth.getInstance().currentUser!!.uid
    // save data to Firebase data base
        database.child("user").child (userId).setValue(user)
    }
}


