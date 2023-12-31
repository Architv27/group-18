package com.google.mediapipe.examples.fluenthands

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.mediapipe.examples.fluenthands.databinding.ActivityCameraBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity: AppCompatActivity() {

    companion object ImageData{
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    private lateinit var viewBinding: ActivityCameraBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var firebaseAuth: FirebaseAuth
    private var lensFacing = CameraSelector.LENS_FACING_BACK



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        //Calling functions to get user permissions to use camera
        if (allPermissionsGranted()) {
            startCamera()
        }
        else {
            requestPermissions()
        }
//      Listener on take photo button to capture a photo
        viewBinding.cameraCaptureButton.setOnClickListener {takePhoto()}
        viewBinding.flipCameraButton.setOnClickListener { flipCamera() }

        cameraExecutor = Executors.newSingleThreadExecutor()
        firebaseAuth = FirebaseAuth.getInstance()

    }

    private fun requestPermissions() {
        requestPermissionsActivity.launch(REQUIRED_PERMISSIONS)
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.CANADA)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has been taken
        Log.d("LiveData", "Calling Take Picture")
        // Capture the image only if the camera use case is properly bound
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("LiveData", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val user: FirebaseUser? = firebaseAuth.currentUser
                    val imageUri = output.savedUri
                    var key: String =  user?.uid.toString()
                    val sharedPreferences = getSharedPreferences(key, MODE_PRIVATE)
                    val write = sharedPreferences.edit()
                    write.putString("imgUri", imageUri.toString())
                    write.apply()

                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    println("Here are the content values: $contentValues")
                    Log.d("LiveData", msg)
                    Log.d("LiveData", "Output Uri (output.savedUri): ${output.savedUri}")
                    finish()

                }
            }
        )


    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("LiveData", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun flipCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera() // Restart the camera with the new configuration
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private val requestPermissionsActivity = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions())
    { permissions ->
//        Initially set permissions granted to true, but loop through and if just 1 is not granted, then set boolean to false.
        var permissionGranted = true
        permissions.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value) {
                permissionGranted = false
            }
        }
        if (!permissionGranted) {
            Toast.makeText(baseContext, "Permissions Denied", Toast.LENGTH_SHORT).show()
        }
        else {
            //Have to call start camera because it won't get called since permissions were originally not granted
            startCamera()
        }

    }


}