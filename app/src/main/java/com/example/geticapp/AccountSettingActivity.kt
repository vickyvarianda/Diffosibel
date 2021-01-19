package com.example.geticapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.storage.StorageManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.geticapp.Model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageActivity
import kotlinx.android.synthetic.main.activity_account_setting.*
import kotlinx.android.synthetic.main.activity_account_setting.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.edit_account_settings_btn

class AccountSettingActivity : AppCompatActivity() {
    private lateinit var profileId : String
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri : Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setting)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures")

        close_profile_btn.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        logout_btn_profile.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Log Out Successfully", Toast.LENGTH_LONG).show()

            val intent = Intent(this, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        profile_image_view_edit.setOnClickListener {

            checker = "clicked"
            CropImage.activity()
                    .setAspectRatio(1,1)
                    .start(this@AccountSettingActivity)
        }

        save_info_profile_btn.setOnClickListener{
            if (checker == "clicked")
            {
                uploadImageAndUpdateInfo()
            }
            else
            {
                updateUserInfoOnly()
            }
        }
        userInfo()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            profile_image_view.setImageURI(imageUri)
        }
    }

    private fun updateUserInfoOnly() {
        when {
            TextUtils.isEmpty(full_change_name.text.toString()) -> {
                Toast.makeText(this, "Please Write Full name First.", Toast.LENGTH_LONG).show()
            }
            username_change_profile.text.toString() == "" -> {
                Toast.makeText(this, "Please Write User name First.", Toast.LENGTH_LONG).show()
            }
            bio_change_profile.text.toString() == "" -> {
                Toast.makeText(this, "Please Write Your Bio First.", Toast.LENGTH_LONG).show()
            }
            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = full_change_name.text.toString().toLowerCase()
                userMap["username"] = username_change_profile.text.toString().toLowerCase()
                userMap["bio"] = bio_change_profile.text.toString().toLowerCase()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this, "Update Successfully", Toast.LENGTH_LONG).show()

                val intent = Intent(this@AccountSettingActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun userInfo()
    {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                if(context != null)
//                {
//                    return
//                }

                if (snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_view)
                    username_change_profile.setText(user!!.getUsername())
                    full_change_name.setText(user!!.getFullname())
                    bio_change_profile.setText(user!!.getBio())
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun uploadImageAndUpdateInfo() {
        when
        {
            imageUri == null-> {
                Toast.makeText(this, "Please Select Image First.", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(full_change_name.text.toString()) -> {
                Toast.makeText(this, "Please Write Full name First.", Toast.LENGTH_LONG).show()
            }
            username_change_profile.text.toString() == "" -> {
                Toast.makeText(this, "Please Write User name First.", Toast.LENGTH_LONG).show()
            }
            bio_change_profile.text.toString() == "" -> {
                Toast.makeText(this, "Please Write Your Bio First.", Toast.LENGTH_LONG).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please Wait, we are Updating your Profile. . .")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser!!.uid + "jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if (!task.isSuccessful)
                    {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener (OnCompleteListener<Uri> {task ->
                    if (task.isSuccessful)
                    {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = full_change_name.text.toString().toLowerCase()
                        userMap["username"] = username_change_profile.text.toString().toLowerCase()
                        userMap["bio"] = bio_change_profile.text.toString().toLowerCase()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this, "Update Successfully", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@AccountSettingActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else
                    {
                        progressDialog.dismiss()
                    }
                } )
            }
        }
    }
}
