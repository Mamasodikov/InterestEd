package com.mamasodikov.interested.models

import java.io.Serializable
import java.util.*

data class Message(
    var senderId:String="",
    var receiverId:String="",
    var message:String="",
    var time:String="",
    var dateObject:Date=Date(),
    var conversationId:String="",
    var conversationName:String="",
    var conversationImage:String=""
):Serializable
