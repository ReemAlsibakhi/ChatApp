package com.reemsib.chatapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.reemsib.chatapp.R
import com.reemsib.chatapp.messages.ChatLogActivity
import com.reemsib.chatapp.model.AppConstants
import com.reemsib.chatapp.model.UserItem
import com.reemsib.chatapp.model.Users
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_msg.*

class NewMsgActivity : AppCompatActivity() {
    var db: FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_msg)

        db = Firebase.firestore

//        val adapter = GroupAdapter<ViewHolder>()
//        rv_newMsg.layoutManager= LinearLayoutManager(this)
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem(Users("3","Reem","")))   adapter.add(UserItem(Users("0","Reem","")))
//        adapter.add(UserItem(Users("1","Reem","")))
//        adapter.add(UserItem(Users("2","Reem","")))
//        adapter.add(UserItem(Users("3","Reem","")))
    //    rv_newMsg.adapter = adapter

        FetchUsers()
    }


    private fun FetchUsers() {
        // startAnim()
        //val data = ArrayList<UserItem>()
        db!!.collection(AppConstants.USER_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                //       stopAnim()
                val adapter = GroupAdapter<ViewHolder>()

                for (document in result) {
                    Toast.makeText(applicationContext, "sucess get", Toast.LENGTH_LONG).show()

                    Log.d(TAG, "${document.id} => ${document.data}")
                    val user = document.toObject(Users::class.java)
                    user.uid = document.id
                    adapter.add(UserItem(user))
                }
              adapter.setOnItemClickListener { item, view ->
                  val user=item as UserItem
              val intent=Intent(this,ChatLogActivity::class.java)
                intent.putExtra(USER_KEY,user.users)
                  startActivity(intent)
                  finish()
              }
                rv_newMsg.layoutManager= LinearLayoutManager(this)
                rv_newMsg.adapter = adapter
            }
            .addOnFailureListener { exception ->
                //  stopAnim()
                Toast.makeText(applicationContext, exception.message.toString(), Toast.LENGTH_LONG)
                    .show()
                Log.w("", exception.message.toString())

            }
      //  return data
    }

    companion object {
        private const val TAG = "NewMsgActivity"
        private const val RC_SIGN_IN = 9001
         const val USER_KEY = "USER_KEY"

    }

}