package com.mamasodikov.interested

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.mamasodikov.interested.adapters.UserAdapter
import com.mamasodikov.interested.databinding.FragmentUserActivityBinding
import com.mamasodikov.interested.listeners.UserListener
import com.mamasodikov.interested.models.User
import com.mamasodikov.interested.utilities.Constants
import com.mamasodikov.interested.utilities.PreferenceManager

class UserActivity : BaseFragment(), UserListener {


    lateinit var bnd:FragmentUserActivityBinding
    lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        bnd = FragmentUserActivityBinding.inflate(inflater, container, false)
        preferenceManager = PreferenceManager(requireContext())
        setListeners()
        getUsers()

    return bnd.root
    }

    private fun setListeners() {
        bnd.imageBack.setOnClickListener{useless->
            requireActivity().onBackPressed()
        }
    }

    fun getUsers(){
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener{task->
                loading(false)
                val currentUserId =preferenceManager.getString(Constants.KEY_USER_ID)

                if(task.isSuccessful && task.result != null && task.result!!.documents.size > 0 )
                {
                    val users:ArrayList<User> = ArrayList()
                    for (i in task.result!!)
                    {
                     if(currentUserId.equals(i.id)) {continue}
                        val user:User = User()
                        user.name = i.getString(Constants.KEY_NAME)!!
                        user.email = i.getString(Constants.KEY_EMAIL)!!
                        user.image = i.getString(Constants.KEY_IMAGE)!!

                        if(i.getString(Constants.KEY_FCM_TOKEN) != null)
                        {user.token = i.getString(Constants.KEY_FCM_TOKEN)!!}

                        user.id = i.id
                        users.add(user)
                    }
                    if (users.size>0){
                        val adapter = UserAdapter(users, this)
                        bnd.usersRecView.adapter = adapter
                        bnd.usersRecView.visibility = View.VISIBLE
                    }
                    else showErrorMessage()
                }

                else
                {
                    showErrorMessage()
                }

            }
    }

    fun loading (isLoading: Boolean){
        if(isLoading)
            bnd.progressBar.visibility = View.VISIBLE
        else
            bnd.progressBar.visibility = View.GONE

    }
    
    fun showErrorMessage(){
        bnd.textErrorMessage.setText(String.format("%s", "Foydalanuvchilar topilmadi"))
        bnd.textErrorMessage.visibility = View.VISIBLE
    }

    override fun onUserClick(user: User) {

        val data:Bundle = Bundle()
        data.putSerializable(Constants.KEY_USER, user)
        findNavController().navigate(R.id.action_userActivity_to_chatActivity, data)
    }

}