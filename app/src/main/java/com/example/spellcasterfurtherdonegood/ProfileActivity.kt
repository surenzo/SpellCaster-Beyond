package com.example.spellcasterfurtherdonegood

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import androidx.appcompat.app.AppCompatActivity


class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        auth = Firebase.auth


        // Redirect to profile page
        setupToolBar()
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

        auth.currentUser?.let {
            pseudo.text = it.displayName
            picture.setImageURI(it.photoUrl)
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
                    Log.d("Connection", "User account deleted.")
                }
            }
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    fun updateAccount(view: View) {
        // TODO: Implement update account
    }
    fun toSpecial(view: View){
        val intent = Intent(this, SpecialActivity::class.java)
        startActivity(intent)
    }
}