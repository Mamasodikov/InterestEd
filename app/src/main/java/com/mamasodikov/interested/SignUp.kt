package com.mamasodikov.interested

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.mamasodikov.interested.databinding.FragmentSignUpBinding
import java.io.ByteArrayOutputStream
import java.util.*
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.mamasodikov.interested.utilities.Constants
import com.mamasodikov.interested.utilities.PreferenceManager
import java.io.FileNotFoundException
import java.io.InputStream


class SignUp : Fragment() {


    lateinit var bnd: FragmentSignUpBinding
    var encodedImage: String? = null
    lateinit var preferenceManager:PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        bnd = FragmentSignUpBinding.inflate(inflater, container, false)
        setListener()
        preferenceManager = PreferenceManager(requireContext())
        return bnd.root
    }


    fun encodeImage(bitmap:Bitmap):String {
        val previewWidth: Int = 150
        val previewHeight: Int = bitmap.height * previewWidth / bitmap.width
        val previewBitmap: Bitmap =
            Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes: ByteArray = byteArrayOutputStream.toByteArray()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(bytes)
        } else {
            android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        }
    }

    fun showToast (message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    val pickImage:ActivityResultLauncher <Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null) {

                val imageUri: Uri? = result.data!!.data

                try {

                    val inputStream:InputStream = context?.contentResolver?.openInputStream(imageUri!!)!!
                    val bitmap:Bitmap = BitmapFactory.decodeStream(inputStream)
                    bnd.imageProfile.setImageBitmap(bitmap)
                    bnd.imageText.visibility = View.GONE
                    encodedImage = encodeImage(bitmap)
                }
                catch (e:FileNotFoundException){
                    e.printStackTrace()
                }
            }
        }
    }

    fun signUp (){

        loading(true)
        addDataToFirestore()

    }

    fun loading (isLoading:Boolean){
        if(isLoading)
        {
            bnd.btnSignUp.visibility = View.INVISIBLE
            bnd.progressBar.visibility = View.VISIBLE
        }

        else{
            bnd.btnSignUp.visibility = View.VISIBLE
            bnd.progressBar.visibility = View.INVISIBLE
        }
    }

    fun isValidSignUpDetails():Boolean {

        if(encodedImage==null)
        {  showToast("Profil rasmini tanlang")
            return false}
        else if (bnd.inputName.text.toString().trim().isEmpty())
        {
            showToast("Ismni kiriting")
            return false
        }
        else if (bnd.inputEmail.text.toString().trim().isEmpty())
        {
            showToast("Emailni kiriting")
            return false

        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(bnd.inputEmail.text.toString()).matches())
        {
            showToast("Emailni tog'ri kiriting")
            return false

        }
        else if (bnd.inputPassword.text.toString().trim().isEmpty())
        {
            showToast("Parolni kiriting")
            return false

        }
        else if (bnd.inputConfirmPassword.text.toString().trim().isEmpty())
        {
            showToast("Parolni tasdiqlang")
            return false

        }
        else if (!bnd.inputPassword.text.toString().equals(bnd.inputConfirmPassword.text.toString()))
        {
            showToast("Parollar mosligini tekshiring")
            return false

        }

        else return true


    }

    fun addDataToFirestore() {

        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val data = hashMapOf<String, String>()

        data.put(Constants.KEY_NAME, bnd.inputName.text.toString())
        data.put(Constants.KEY_EMAIL, bnd.inputEmail.text.toString())
        data.put(Constants.KEY_PASSWORD, bnd.inputPassword.text.toString())
        data.put(Constants.KEY_IMAGE, encodedImage!!)

        database.collection(Constants.KEY_COLLECTION_USERS)
            .add(data)
            .addOnSuccessListener { documentReference ->
                loading(false)
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, documentReference.id)
                preferenceManager.putString(Constants.KEY_NAME, bnd.inputName.text.toString())
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage!!)
                findNavController().navigate(R.id.action_signUp_to_homeActivity)

                Log.w("FCM", "DocumentSnapshot added with ID:${documentReference.id}")
            }
            .addOnFailureListener{
                loading(false)
                Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun setListener() {
        bnd.txtSignIn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                findNavController().navigate(R.id.action_signUp_to_signIn)
            }

        })

        bnd.btnSignUp.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                if(isValidSignUpDetails()) {
                    signUp()
                }
            }
        })

        bnd.layoutImage.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {

                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                pickImage.launch(intent)
            }
        })
    }

}