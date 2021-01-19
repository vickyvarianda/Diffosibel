package com.example.geticapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplashActivity : AppCompatActivity() {

    lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        super.onStart()

        if (FirebaseAuth.getInstance().currentUser != null)
        {
            handler = Handler()
            handler.postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }, 2000)
        }
        else
        {
            handler = Handler()
            handler.postDelayed({

                val inten = Intent(this, WellcomeActivity::class.java)
                startActivity(inten)
                finish()

            }, 2000)
        }

    }

}
