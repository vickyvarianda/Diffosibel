package com.example.geticapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
    when (item.itemId) {
        R.id.nav_home -> {
            moveToFragment(HomeFragment())
            return@OnNavigationItemSelectedListener true
        }
        R.id.nav_search -> {
            moveToFragment(SearchFragment())
            return@OnNavigationItemSelectedListener true
        }
        R.id.nav_add_post -> {
            item.isChecked = false
            startActivity(Intent(this@MainActivity, AddPostActivity::class.java))
            return@OnNavigationItemSelectedListener true
        }
        R.id.nav_notifications -> {
            moveToFragment(NotificationFragment())
            return@OnNavigationItemSelectedListener true
        }
        R.id.nav_profile -> {
            moveToFragment(ProfileFragment())
            return@OnNavigationItemSelectedListener true
        }
    }

    false
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val navView: BottomNavigationView = findViewById(R.id.nav_view)

    navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)


    moveToFragment(HomeFragment())
}


private fun moveToFragment(fragment: Fragment)
{
    val fragmentTrans = supportFragmentManager.beginTransaction()
    fragmentTrans.replace(R.id.fragment_container, fragment)
    fragmentTrans.commit()
}

    @Override
    override fun onBackPressed(){
        if (doubleBackToExitPressedOnce){
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

}

