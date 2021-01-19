package com.example.geticapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignInActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        sign_up_btn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finishAffinity()
        }

        back_signin.setOnClickListener {
            startActivity(Intent(this, WellcomeActivity::class.java))
            finishAffinity()
        }

        login_btn.setOnClickListener{
            loginUser();
        }
    }

    private fun loginUser()
    {
        val Email = email_add.text.toString()
        val Password = pass_add.text.toString()

        when{
            TextUtils.isEmpty(Email) -> Toast.makeText(this, "Email is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(Password) -> Toast.makeText(this, "Password is required", Toast.LENGTH_LONG).show()

            else ->
            {
                val progressDialog = ProgressDialog(this@SignInActivity)
                progressDialog.setTitle("Login")
                progressDialog.setMessage("please wait, this may take a while")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener{task ->
                    if (task.isSuccessful){
                        progressDialog.dismiss()
                        Toast.makeText(this, "Login Successfully", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
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
    }
}
