package com.example.geticapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*
import kotlin.collections.HashMap


class SignUpActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sign_in_btn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finishAffinity()
        }
        back_signup.setOnClickListener {
            startActivity(Intent(this, WellcomeActivity::class.java))
            finishAffinity()
        }

        signup_btn.setOnClickListener{
            CreateAccount()
        }
    }

    private fun CreateAccount(){
        val fullName = fullname_signup.text.toString()
        val userName = username_signup.text.toString()
        val Email = email_signup.text.toString()
        val Password = pass_signup.text.toString()

        when{
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "Fullname is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "username is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(Email) -> Toast.makeText(this, "Email is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(Password) -> Toast.makeText(this, "Password is required", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("please wait, this may take a while")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful)
                        {
                            saveUserInfo(fullName,userName, Email, progressDialog)
                        }
                        else
                        {
                            val message = task.exception!!.toString()
                            Toast.makeText(this, "Error : $message", Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }

                    }
            }
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, Email: String, progressDialog: ProgressDialog)
    {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName.toLowerCase()
        userMap["email"] = Email
        userMap["bio"] = "Hey im using Getic, See my Ideas."
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/getic-app.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=69abad62-2e5e-4bcf-a746-89abc5fafc3d"

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful)
                {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account Has Been Created Successfully", Toast.LENGTH_LONG).show()

                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID)
                        .setValue(true)

                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Error : $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }

            }
    }
}


