package com.reemsib.chatapp.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.reemsib.chatapp.R
import com.reemsib.chatapp.activity.NewMsgActivity
import com.reemsib.chatapp.model.ChatFromItem
import com.reemsib.chatapp.model.ChatToItem
import com.reemsib.chatapp.model.Users
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

     val data= intent.getParcelableExtra<Users>(NewMsgActivity.USER_KEY)
      tv_username.text= data!!.username

        val adapter=GroupAdapter<ViewHolder>()
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())


        rv_chatLog.layoutManager= LinearLayoutManager(this)
        rv_chatLog.adapter=adapter
    }
}