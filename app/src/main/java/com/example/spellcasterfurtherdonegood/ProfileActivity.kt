package com.example.spellcasterfurtherdonegood

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore


class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        auth = Firebase.auth


        // Redirect to profile page
        setupToolBar()
        // Set up the profile page
        setupProfilePage()
    }


    private fun setupToolBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun setupProfilePage() {
        //get the profile picture, the pseudo champs and change them to the user's
        val picture = findViewById<ImageView>(R.id.profile_picture)
        val pseudo = findViewById<TextView>(R.id.pseudonym)
        val email = findViewById<TextView>(R.id.email)

        auth.currentUser?.let {
            pseudo.text = it.displayName
            email.text = it.email
            // if it has a photo, set it
            if(it.photoUrl != "".toUri()){
                Glide.with(this)
                    .load(it.photoUrl)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                    .into(picture)
            }
            else
                picture.setImageResource(R.drawable.avatar)
        }
    }

    fun signOut(view: View) {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()}

    fun deleteAccount(view: View) {
        auth.currentUser?.delete()!!
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(auth.currentUser?.displayName.toString()).delete()
                    Log.d("Connection", "User account deleted.")
                }
            }
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    fun updateAccount(view: View) {
        updateInformationDialog()
    }
    fun toSpecial(view: View){
        val intent = Intent(this, SpecialActivity::class.java)
        startActivity(intent)
    }

    private fun updateInformationDialog() {
        val myDialog = object : Dialog(this) {
            @Deprecated("Deprecated in Java", ReplaceWith("dismiss()"))
            override fun onBackPressed() {
                dismiss()
            }
        }
        myDialog.setContentView(R.layout.dialog_update_info)
        myDialog.setCancelable(false)
        myDialog.setCanceledOnTouchOutside(true)

        myDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        myDialog.show()

        val emailAddress: EditText = myDialog.findViewById(R.id.et_email)
        val password: EditText = myDialog.findViewById(R.id.et_password)
        val username: EditText = myDialog.findViewById(R.id.et_username)
        val linkPicture: EditText = myDialog.findViewById(R.id.et_image)

        (myDialog.findViewById<Button>(R.id.updateButton)!!).setOnClickListener {
            val profileUpdates = userProfileChangeRequest {
                //update the username and the photo if the user put something in the fields
                if (username.text.toString() != ""){

                    //change the user database to the new username
                    val db = FirebaseFirestore.getInstance()
                    val currentUserDocRef = db.collection("users").document(auth.currentUser?.displayName.toString())
                    currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
                        if (!documentSnapshot.exists()) {
                            return@addOnSuccessListener
                        }

                        val userData = documentSnapshot.data ?: return@addOnSuccessListener

                        db.collection("users").document(username.text.toString()).set(userData)
                            .addOnSuccessListener {
                                Log.d("Connection", "User data copied to new username.")
                                currentUserDocRef.delete()
                                    .addOnSuccessListener {
                                        Log.d("Connection", "User data deleted.")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("Connection", "Error deleting user data", e)
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.w("Connection", "Error copying user data", e)
                            }
                    }
                    displayName = username.text.toString()
                }

                //if the user put a link to a picture, set it
                if (linkPicture.text.toString() != "")
                    photoUri = linkPicture.text.toString().toUri()
            }
            auth.currentUser?.updateProfile(profileUpdates)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Connection", "User profile updated.")
                    }
                }
            //update the email and the password if the user put something in the fields
            if (emailAddress.text.toString() != "")
                auth.currentUser?.updateEmail(emailAddress.text.toString())
            if (password.text.toString() != "")
                auth.currentUser?.updatePassword(password.text.toString())
            myDialog.dismiss()
            //recharge the activity to see the changes
            finish()
        }
    }
}