package com.mamasodikov.interested

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.mamasodikov.interested.databinding.FragmentSignInBinding
import com.mamasodikov.interested.utilities.Constants
import com.mamasodikov.interested.utilities.PreferenceManager


class SignIn : Fragment() {

    lateinit var bnd:FragmentSignInBinding
    lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bnd = FragmentSignInBinding.inflate(inflater, container, false)

        setListener()
        preferenceManager = PreferenceManager(requireContext())

        return  bnd.root
    }

    private fun setListener() {
        bnd.txtCreateAccount.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {

                findNavController().navigate(R.id.action_signIn_to_signUp)
            }
        })
        bnd.btnSignIn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                if(isValidSignInDetails())
                    signIn()
            }
        })

    }

    private fun signIn() {

        loading (true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, bnd.inputEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, bnd.inputPassword.text.toString())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null
                    && task.result!!.documents.size > 0)
                {
                    val documentSnapshot:DocumentSnapshot = task.result!!.documents.get(0)
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                    preferenceManager.putString(Constants.KEY_NAME,
                        documentSnapshot.getString(Constants.KEY_NAME)!!
                    )
                    preferenceManager.putString(Constants.KEY_IMAGE,
                        documentSnapshot.getString(Constants.KEY_IMAGE)!!)
                        findNavController().navigate(R.id.action_signIn_to_homeActivity)

                }

                else{
                    loading(false)
                    showToast("Kirish amalga oshmadi")

                }
            }
            .addOnFailureListener { error->
                showToast(error.message.toString())
            }



    }


    fun showToast(message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun isValidSignInDetails():Boolean{

        if (bnd.inputEmail.text.toString().trim().isEmpty()){
            return false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(bnd.inputEmail.text.toString()).matches())
        {showToast("To'g'ri Email kiriting")
            return false}
        else if (bnd.inputPassword.text.toString().trim().isEmpty())
        {
            showToast("Parolni kirting")
            return false
        }
        else return true

    }

    fun loading (isLoading:Boolean)
    {
        if (isLoading){
            bnd.btnSignIn.visibility = View.INVISIBLE
            bnd.progressBar.visibility = View.VISIBLE
        }
        else{
            bnd.progressBar.visibility = View.INVISIBLE
            bnd.btnSignIn.visibility = View.VISIBLE
        }
    }

}