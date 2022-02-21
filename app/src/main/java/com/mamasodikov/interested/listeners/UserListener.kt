package com.mamasodikov.interested.listeners

import com.mamasodikov.interested.models.User

interface UserListener {

    fun onUserClick(user: User)
}