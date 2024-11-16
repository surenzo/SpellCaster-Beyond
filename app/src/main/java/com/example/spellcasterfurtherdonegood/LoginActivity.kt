package com.example.spellcasterfurtherdonegood

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth


class LoginActivity : ComponentActivity(){

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG_GOOGLE = "GoogleActivity"
        private const val TAG = "EmailPassword"
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //GOOGLE
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize Firebase Auth
        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //GOOGLE
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG_GOOGLE, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG_GOOGLE, "Google sign in failed", e)
            }
        }

    }

    //GOOGLE
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG_GOOGLE, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if(auth.currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //GOOGLE
    fun signInGoogle(view: View) {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    //MAIL
    private fun callLoginDialog() {
        val myDialog = object : Dialog(this) {
            @Deprecated("Deprecated in Java", ReplaceWith("dismiss()"))
            override fun onBackPressed() {
                dismiss()
            }
        }
        myDialog.setContentView(R.layout.dialog_login_mail)
        myDialog.setCancelable(false)
        myDialog.setCanceledOnTouchOutside(true)

        myDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val emailAddress: EditText = myDialog.findViewById(R.id.et_username)
        val password: EditText = myDialog.findViewById(R.id.et_password)
        myDialog.show()

        (myDialog.findViewById<Button>(R.id.loginButton)!!).setOnClickListener {
            if(emailAddress.text.toString().isEmpty() || password.text.toString().isEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
            else{
            signIn(emailAddress.text.toString(), password.text.toString())
            myDialog.dismiss()
            }
        }
        (myDialog.findViewById<Button>(R.id.lbl_create)!!).setOnClickListener {
            callCreateDialog()
            myDialog.dismiss()
        }
        (myDialog.findViewById<Button>(R.id.lbl_forgot)!!).setOnClickListener {
            callForgotDialog()
            myDialog.dismiss()
        }
    }

    //MAIL
    private fun callCreateDialog() {
        val myDialog = object : Dialog(this) {
            @Deprecated("Deprecated in Java")
            override fun onBackPressed() {
                dismiss()
                callLoginDialog()
            }
        }
        myDialog.setContentView(R.layout.dialog_create_mail)
        myDialog.setCancelable(false)
        myDialog.setCanceledOnTouchOutside(true)

        myDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val emailAddress: EditText = myDialog.findViewById(R.id.et_username)
        val password: EditText = myDialog.findViewById(R.id.et_password)
        myDialog.show()

        (myDialog.findViewById<Button>(R.id.createButton)!!).setOnClickListener {
            //check if the password contains at least 8 characters, uses a number, a lower case letter, an upper case letter, and a special character
            val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$".toRegex()
            if(!password.text.toString().matches(passwordPattern)){
                Toast.makeText(this, "Password must contain at least 8 characters, a number, a lower case letter, an upper case letter, and a special character", Toast.LENGTH_SHORT).show()
            }
            else
                if(emailAddress.text.toString().isEmpty() || password.text.toString().isEmpty()){
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
                else{
                    createAccount(emailAddress.text.toString(), password.text.toString())
                    myDialog.dismiss()
                }
        }
    }

    //MAIL
    private fun callForgotDialog() {
        val myDialog = object : Dialog(this) {
            @Deprecated("Deprecated in Java")
            override fun onBackPressed() {
                dismiss()
                callLoginDialog()
            }
        }
        myDialog.setContentView(R.layout.dialog_forgot_password)
        myDialog.setCancelable(false)
        myDialog.setCanceledOnTouchOutside(true)

        myDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val emailAddress: EditText = myDialog.findViewById(R.id.et_username)
        myDialog.show()

        (myDialog.findViewById<Button>(R.id.createButton)!!).setOnClickListener {
            if(emailAddress.text.toString().isEmpty() ){
                Toast.makeText(this, "Please fill in field", Toast.LENGTH_SHORT).show()
            }
            else{
                sendPasswordReset(emailAddress.text.toString())
                myDialog.dismiss()
            }
        }
    }

    //MAIL
    private fun createAccount(email: String, password: String) {
        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    sendEmailVerification()
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }

    //MAIL
    private fun signIn(email: String, password: String) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    callLoginDialog()
                    Toast.makeText(this, "Username and password don't match", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
        // [END sign_in_with_email]
    }

    private fun sendPasswordReset(emailAddress: String) {
        Firebase.auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                }
            }
    }
    private fun sendEmailVerification() {
        // [START send_email_verification]
        val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                // Email Verification sent
            }
        // [END send_email_verification]
    }

    fun signInMail(view: View) {
        callLoginDialog()
    }

    fun createAccount(view: View) {

    }
    fun forgotPassword(view: View) {
        callLoginDialog()
    }
}