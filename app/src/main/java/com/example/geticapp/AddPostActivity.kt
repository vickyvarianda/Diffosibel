package com.example.geticapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_setting.*
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri : Uri? = null
    private var storageAddPostProfilePicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storageAddPostProfilePicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")

        save_add_btn.setOnClickListener { uploadImage() }

        CropImage.activity()
            .setAspectRatio(2,1)
            .start(this@AddPostActivity)

        close_add_btn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            image_post_add.setImageURI(imageUri)
        }
        else
        {
            Toast.makeText(this, "cancelled", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadImage() {
        when{
            imageUri == null-> {
                Toast.makeText(this, "Please Select Image First.", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(deskripsi_add.text.toString()) -> {
                Toast.makeText(this, "Please Write description First.", Toast.LENGTH_LONG).show()
            }

            else ->{
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Please Wait, we are Updating. . .")
                progressDialog.show()

                val fileRef = storageAddPostProfilePicRef!!.child(System.currentTimeMillis().toString() + ".jpg")

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
                })
                    .addOnCompleteListener (OnCompleteListener<Uri> { task ->
                        if (task.isSuccessful)
                        {
                            val downloadUrl = task.result
                            myUrl = downloadUrl.toString()

                            val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                            val postId = ref.push().key

                            val postMap = HashMap<String, Any>()
                            postMap["postid"] = postId!!
                            postMap["deskripsi"] = deskripsi_add.text.toString().toLowerCase()
                            postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                            postMap["postimage"] = myUrl

                            ref.child(postId).updateChildren(postMap)

                            Toast.makeText(this, "Upload Successfully", Toast.LENGTH_LONG).show()

                            val intent = Intent(this, MainActivity::class.java)
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