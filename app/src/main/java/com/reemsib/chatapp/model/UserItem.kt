package com.reemsib.chatapp.model

import com.reemsib.chatapp.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_row_new_message.view.*


class UserItem (val users: Users):Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
       viewHolder.itemView.tv_username.text=users.username
       Picasso.get().load(users.imgUrl).into(viewHolder.itemView.img_profile)
    }

    override fun getLayout(): Int {
    return R.layout.user_row_new_message
    }
}