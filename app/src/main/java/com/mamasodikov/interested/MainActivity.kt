package com.mamasodikov.interested

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.mamasodikov.interested.utilities.Constants
import com.mamasodikov.interested.utilities.PreferenceManager

class MainActivity : AppCompatActivity() {

    lateinit var preferenceManager: PreferenceManager
    private var destination:Int = 0
    private var isRestarted:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    preferenceManager = PreferenceManager(applicationContext)
    }

    override fun onStart() {
        super.onStart()

        if(isRestarted !=true) {
    destination = if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
        R.id.homeActivity
    else R.id.signIn

    preferenceManager = PreferenceManager(this)
    val navController = findNavController(R.id.fragmentContainerView)
    val navGraph = navController.navInflater.inflate(R.navigation.navigation)

    navGraph.startDestination = destination
    navController.graph = navGraph
}
    }

    override fun onRestart() {
        super.onRestart()

        isRestarted = true
    }
}