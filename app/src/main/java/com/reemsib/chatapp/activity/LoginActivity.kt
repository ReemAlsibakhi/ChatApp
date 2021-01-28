package com.reemsib.chatapp.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cc.cloudist.acplibrary.ACProgressConstant
import cc.cloudist.acplibrary.ACProgressFlower
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.reemsib.chatapp.R
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() ,View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    var dialog: ACProgressFlower?=null
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    val  PASSWORD_PATTERN = Pattern.compile("^" + "(?=.*[0-9])" + "(?=.*[a-z])" + "(?=.*[A-Z])" + "(?=.*[a-zA-Z])" + "(?=.*[@#$%^&+=])" + "(?=\\S+$)" + ".{4,}" + "$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth
        progressDialog()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()

        btn_login.setOnClickListener(this)
        tv_signup.setOnClickListener (this)
        btn_google.setOnClickListener (this)
        btn_facebook.setOnClickListener (this)
    }

    private fun progressDialog() {
        dialog = ACProgressFlower.Builder(this)
            .direction(ACProgressConstant.DIRECT_CLOCKWISE)
            .themeColor(Color.WHITE)
            // .text("Title is here")
            .fadeColor(Color.DKGRAY).build()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btn_login ->{
                Login(et_email.text.toString(), et_password.text.toString())
            }
            R.id.tv_signup ->{
                val i=Intent(this, RegisterActivity::class.java)
                startActivity(i)
            }
            R.id.btn_google ->{
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
            R.id.btn_facebook ->{

                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
                    override fun onSuccess(loginResult: LoginResult?) {
                        Log.d(TAG, "facebook:onSuccess:$loginResult")
                        handleFacebookAccessToken(loginResult!!.accessToken)
                    }
                    override fun onCancel() {
                        Log.d(TAG, "facebook:onCancel")
                        Toast.makeText(applicationContext, "Login Cancel", Toast.LENGTH_LONG).show()
                    }

                    override fun onError(exception: FacebookException) {
                        Log.d(TAG, "facebook:onError", exception)
                        Toast.makeText(applicationContext, exception.message, Toast.LENGTH_LONG).show()
                    }
                })

            }
        }
    }
    private fun validateForm():Boolean {
        var valid = true
        val email = et_email.text.toString().trim()
        val password = et_password.text.toString().trim()
        when {
            email.isEmpty() -> {
                Toast.makeText(applicationContext, "Please enter an email", Toast.LENGTH_LONG).show()
                valid = false
            }
            password.isEmpty() -> {
                Toast.makeText(applicationContext, "Please enter password", Toast.LENGTH_LONG).show()
                valid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(applicationContext, "Please enter a valid email", Toast.LENGTH_LONG).show()
                valid = false
            }
            !PASSWORD_PATTERN.matcher(password).matches() -> {
                Toast.makeText(applicationContext, "Password too weak must contains at least capital letter, numbers, ", Toast.LENGTH_LONG).show()
                valid = false
            }
        }
        return valid
    }

    private fun Login(email: String, password: String) {
       if(!validateForm()){
           return
       }
        dialog!!.show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                dialog!!.dismiss()
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    Toast.makeText(applicationContext, "Login Success.", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                     updateUI(user)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(applicationContext," The password is invalid or the email already exists", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
       if(user !=null){
           val intent = Intent(this,LatestMsgsActivity::class.java)
           intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK or(Intent.FLAG_ACTIVITY_NEW_TASK)
           startActivity(intent)
       }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //facebook
        callbackManager.onActivityResult(requestCode, resultCode, data)
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
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
       dialog!!.dismiss()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    Snackbar.make(ConstraintLayout, "Authentication Succeed.", Snackbar.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(ConstraintLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                }

             dialog!!.dismiss()
            }
    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        dialog!!.show()
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                   updateUI(user)

                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(applicationContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
                dialog!!.dismiss()
            }
    }
    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }


}