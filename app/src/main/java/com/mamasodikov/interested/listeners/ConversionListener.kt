package com.mamasodikov.interested.listeners

import com.mamasodikov.interested.models.User

interface ConversionListener {
    fun onConversionClicked(user:User)
}