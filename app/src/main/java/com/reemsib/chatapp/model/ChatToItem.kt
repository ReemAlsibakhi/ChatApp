package com.reemsib.chatapp.model

import com.reemsib.chatapp.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder


class ChatToItem ():Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout(): Int {
    return R.layout.chat_to_row
    }
}