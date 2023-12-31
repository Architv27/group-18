package com.google.mediapipe.examples.fluenthands

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileActivity: ComponentActivity() {
    private lateinit var profilePhoto: ImageView
    private lateinit var changePhotoButton: Button
    private lateinit var name: EditText
    //    private lateinit var email: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profilePhoto = findViewById(R.id.profilePhoto)
        changePhotoButton = findViewById(R.id.buttonChangePhoto)
        name = findViewById(R.id.editName)
//        email = findViewById(R.id.editEmail)
        saveButton = findViewById(R.id.buttonSave)
        cancelButton = findViewById(R.id.buttonCancel)

        changePhotoButton.setOnClickListener {
            changePhoto()
        }
        saveButton.setOnClickListener() {
            saveInputs()
        }
        cancelButton.setOnClickListener() {
            finish()
        }
        firebaseAuth = FirebaseAuth.getInstance()

        val user: FirebaseUser? = firebaseAuth.currentUser
        if (user != null) {
            var getName = user.displayName
            user.reload().addOnCompleteListener{reloadTask ->
                if (reloadTask.isSuccessful) {
                    name.text = Editable.Factory.getInstance().newEditable(getName)
                }

            }
        }

    }

    override fun onResume() {
        super.onResume()
        getPhoto()
    }
    private fun getPhoto() {
        val user: FirebaseUser? = firebaseAuth.currentUser
        var key: String =  user?.uid.toString()
        val sharedPreferences = getSharedPreferences(key, MODE_PRIVATE)
        profilePhoto.setImageURI(Uri.parse(sharedPreferences.getString("imgUri", "")))
    }

    private fun changePhoto() {
        val cameraIntent = Intent(this, CameraActivity::class.java)
        startActivity(cameraIntent)
    }

    private fun saveInputs() {
        val updatedName = name.text.toString()
//        val updatedEmail = email.text.toString()

        // Get the current user from FirebaseAuth
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // Update the display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(updatedName)
                .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { profileUpdateTask ->
                    Toast.makeText(
                        this,
                        "Profile updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(
                this,
                "Failed to update profile",
                Toast.LENGTH_SHORT
            ).show()
        }
        finish()
    }
}
