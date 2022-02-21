package com.mamasodikov.interested

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mamasodikov.interested.utilities.Constants
import com.mamasodikov.interested.utilities.PreferenceManager

open class BaseFragment: Fragment() {

    lateinit var docRef: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = FirebaseFirestore.getInstance()
        val preferenceManager = context?.let { PreferenceManager(it) }

        if (preferenceManager != null) {
            docRef = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID)!!)
        }

    }

    override fun onPause() {
        super.onPause()

        docRef.update(Constants.KEY_AVAILABILITY, 0)
    }

    override fun onResume() {
        super.onResume()

        docRef.update(Constants.KEY_AVAILABILITY, 1)
    }
}