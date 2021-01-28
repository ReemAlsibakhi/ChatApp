package com.reemsib.chatapp.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns.EMAIL_ADDRESS
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
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.orhanobut.hawk.Hawk
import com.reemsib.chatapp.R
import com.reemsib.chatapp.model.AppConstants
import com.reemsib.chatapp.model.Users
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.btn_google
import kotlinx.android.synthetic.main.activity_register.et_email
import kotlinx.android.synthetic.main.activity_register.et_password
import java.util.*
import java.util.regex.Pattern


class RegisterActivity : AppCompatActivity() , View.OnClickListener{
    private  var auth: FirebaseAuth= Firebase.auth
    var dialog:ACProgressFlower ?=null
    var ImageURI: Uri?= null
    val storage = Firebase.storage
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    var db: FirebaseFirestore = Firebase.firestore
    val  PASSWORD_PATTERN = Pattern.compile("^" + "(?=.*[0-9])" + "(?=.*[a-z])" + "(?=.*[A-Z])" + "(?=.*[a-zA-Z])" + "(?=.*[@#$%^&+=])" + "(?=\\S+$)" + ".{4,}" + "$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        Hawk.init(applicationContext).build();
        progressDialog()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()

        btn_signup.setOnClickListener(this)
        btn_google.setOnClickListener(this)
        btn_facebook.setOnClickListener(this)
        btn_pickImg.setOnClickListener (this)
    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.btn_signup ->{
                Register(et_name.text.toString(),et_email.text.toString(), et_password.text.toString())
            }
            R.id.tv_signin ->{
                val i=Intent(this, LoginActivity::class.java)
                startActivity(i)
            }
            R.id.btn_google ->{
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent,RC_SIGN_IN)
            }
            R.id.btn_facebook ->{

                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(callbackManager, object :
                    FacebookCallback<LoginResult?> {
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
            R.id.btn_pickImg ->{
                ImagePicker.with(this).crop().compress(1024).maxResultSize(1080, 1080).start()
            }
        }
    }

    private fun progressDialog() {
        dialog = ACProgressFlower.Builder(this).
        direction(ACProgressConstant.DIRECT_CLOCKWISE).
        themeColor(Color.WHITE).fadeColor(Color.DKGRAY).build()

    }

    private fun Register(username:String, email: String, password: String) {
       if(!validateForm()){
           return
       }
        dialog!!.show()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("uid1", "Successfully created user with ${task.result!!.user!!.uid} .")
                    Toast.makeText(applicationContext, "Successfully created user.", Toast.LENGTH_SHORT).show()
                    Hawk.put(AppConstants.CURRENT_USER_KEY,  auth.currentUser)
                    if (ImageURI==null){
                        saveUser(username,"")
                    }else{
                        uploadProfileImg()
                    }

                } else {
                    dialog!!.dismiss()
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(applicationContext, "Failed,  ${task.exception} Email already exists.", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun saveUser(username:String,imgUri:String) {
     val uid=FirebaseAuth.getInstance().uid
        val user =Users(uid!!,username,imgUri)
        Log.d("uid2",uid)
        Log.d("img2",imgUri)

        db.collection(AppConstants.USER_COLLECTION)
            .add(user)
            .addOnSuccessListener { documentReference ->
                dialog!!.dismiss()
                Toast.makeText(this, "add Success user", Toast.LENGTH_SHORT).show()
                val user1 = auth.currentUser
                updateUI(user1)
                Log.d(TAG, "DocumentSnapshot added with ID:  ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                dialog!!.dismiss()
                Toast.makeText(this, "Error adding document", Toast.LENGTH_SHORT).show()
                Log.w("save user failed", e.message.toString())
            }
    }

    private fun uploadProfileImg() {
     //   showDialog()
        val storage = Firebase.storage
        val storageRef = storage.reference
        val imagesRef = storageRef.child("images")
        val image = imagesRef.child("image" + System.currentTimeMillis() + ".png")
        val uploadTask = image.putFile(ImageURI!!)

        uploadTask.addOnFailureListener { exception ->
            Log.e(TAG, exception.message.toString())
            // Handle unsuccessful uploads
        }.addOnSuccessListener { uploadTask ->
            image.downloadUrl.addOnSuccessListener { uri ->
                Log.e(TAG+"img1", uri.toString())
                ImageURI = uri
                saveUser(et_name.text.toString(),uri.toString())
            }.addOnFailureListener { exception ->
                dialog!!.dismiss()
                Log.e(TAG, exception.message.toString())
            }

            Log.e(TAG, "Image uploaded Successfully")
        }.addOnFailureListener { exception ->
            Log.e("fail_image", exception.message.toString())
            Toast.makeText(this, "Failed upload image", Toast.LENGTH_LONG).show(); }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
           ImageURI = data?.data
            btn_pickImg.setImageURI(ImageURI)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    fun validateForm():Boolean{
        var valid = true
        val email = et_email.text.toString().trim()
        val password = et_password.text.toString().trim()
        val username=et_name.text.toString().trim()
        when {
            username.isEmpty() -> {
                Toast.makeText(applicationContext, "Please enter username", Toast.LENGTH_LONG).show()
                et_name.error = "Please Enter Username.."
                valid = false
            }
            email.isEmpty() -> {
                Toast.makeText(applicationContext, "Please enter an email", Toast.LENGTH_LONG).show()
                et_email.error="Please enter an email"
                valid = false
            }
            password.isEmpty() -> {
                Toast.makeText(applicationContext, "Please enter password", Toast.LENGTH_LONG).show()
                et_password.error="Please enter password"
                valid = false

            }
            !EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(applicationContext, "Please enter a valid email", Toast.LENGTH_LONG).show()
                et_email.error="Please enter a valid email"
                valid = false

            }
            !PASSWORD_PATTERN.matcher(password).matches() -> {
                Toast.makeText(applicationContext, "Password too weak", Toast.LENGTH_LONG).show()
                et_password.error="password too weak, must contains at least capital letter, number, and $ #"
                valid = false
            }
        }
    return valid
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

    private fun updateUI(user: FirebaseUser?) {
        if(user !=null){
            val intent = Intent(this,LatestMsgsActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

    }

    companion object {
        private const val TAG = "RegisterActivity"
        private const val RC_SIGN_IN = 9001
    }

}