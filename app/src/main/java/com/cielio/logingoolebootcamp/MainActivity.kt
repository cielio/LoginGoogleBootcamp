package com.cielio.logingoolebootcamp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.cielio.logingoolebootcamp.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = Firebase.auth

        binding.singInButton.setOnClickListener {
            signIn()
        }

        binding.singOutButton.setOnClickListener {
            signOut()
        }
    }
    
    private fun signIn() = startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {task ->
                if (task.isSuccessful) {
                    Log.d(TAG,"firebaseAuthWithGoogle: success")
                    setUpUI()
                } else {
                    Log.d(TAG,"firebaseAuthWithGoogle: failure", task.exception)
                }
            }
    }

    private fun setUpUI() {
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            binding.singInButton.visibility = View.GONE
            binding.singOutButton.visibility = View.VISIBLE
            binding.googleUserCredential.visibility = View.VISIBLE

            val personName = acct.displayName
            val personGivenName = acct.givenName
            val personFamily = acct.familyName
            val personEmail = acct.email
            val personId = acct.id
            val personPhoto = acct.photoUrl

            val userData = """ 
                | $personName
                | $personGivenName
                | $personFamily
                | $personEmail
                | $personId
                """.trimMargin()
            binding.googleUserCredential.text = userData
        }
    }

    private fun signOut() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener(this, OnCompleteListener {
           binding.singInButton.visibility = View.VISIBLE
           binding.singOutButton.visibility = View.GONE
           binding.googleUserCredential.visibility = View.GONE
        })
    }
}
