package com.reemsib.chatapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.reemsib.chatapp.R
import kotlinx.android.synthetic.main.activity_latestmsgs.*

class LatestMsgsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latestmsgs)
        verifyUserLogin()

        img_newMsg.setOnClickListener {
            val i=Intent(this,NewMsgActivity::class.java)
            startActivity(i)
        }
        img_logout.setOnClickListener {
            Firebase.auth.signOut()
            val i = Intent(this,LoginActivity::class.java)
            i.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        }

    }
    private fun verifyUserLogin() {
        val uid= FirebaseAuth.getInstance().uid
        Log.e("main_uid",uid.toString())
        if (uid==null){
            val i = Intent(this,StartActivity::class.java)
            i.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        }
    }
}