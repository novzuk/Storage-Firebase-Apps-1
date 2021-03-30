package com.goggxi.storagefirebaseapps

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.RoundedCornersTransformation
import com.goggxi.storagefirebaseapps.databinding.ActivityMainBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception


private const val REQUEST_CODE_PICK_IMAGE = 72
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var nameAnimal : String

    private var imageUri : Uri? = null

    private val imageStorageReference = FirebaseStorage.getInstance().getReference("uploads")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAction()
        setImageViewAnimal()
    }

    private fun initAction() {

        binding.buttonAddPhoto.setOnClickListener {
            pickImage()
        }

        binding.buttonUploadImage.setOnClickListener {
            nameAnimal = binding.editTextNameAnimal.text.toString().trim()
            if (imageUri != null) {
                if (nameAnimal.isBlank() || nameAnimal.isEmpty()){
                    binding.inputTextNameAnimal.error = "Required*"
                } else {
                    uploadImage(nameAnimal)
                }
            } else {
                Toast.makeText(this, "Select Image!", Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonDownloadImage.setOnClickListener {
            nameAnimal = binding.editTextNameAnimal.text.toString().trim()
            if (nameAnimal.isBlank() || nameAnimal.isEmpty()){
                binding.inputTextNameAnimal.error = "Required*"
            } else {
                downloadImage(nameAnimal)
            }
        }

        binding.buttonDeleteImage.setOnClickListener {
            nameAnimal = binding.editTextNameAnimal.text.toString().trim()
            if (nameAnimal.isBlank() || nameAnimal.isEmpty()){
                binding.inputTextNameAnimal.error = "Required*"
            } else {
                deleteImage(nameAnimal)
            }
        }

        binding.buttonShowAllImage.setOnClickListener {

        }
    }

    private fun setImageViewAnimal() {
        binding.imageViewAnimal.load(ContextCompat.getDrawable(this, R.drawable.shape)){
            crossfade(true)
            crossfade(500)
            transformations(RoundedCornersTransformation(10F))
        }
    }

    private fun pickImage() {
        Intent(Intent.ACTION_GET_CONTENT).also {
            it.type = "image/*"
            startActivityForResult(it, REQUEST_CODE_PICK_IMAGE)
        }
    }

    private fun resetApplication() {
        setImageViewAnimal()
        imageUri = null
        binding.inputTextNameAnimal.error = null
        binding.editTextNameAnimal.text?.clear()
        binding.progressBarLoadingIndicator.visibility = View.GONE
        binding.textViewIndicatorLoading.visibility = View.GONE
        binding.imageViewAnimal.isFocusableInTouchMode = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE) {
            data?.data?.let {
                imageUri = it
                binding.imageViewAnimal.load(imageUri){
                    crossfade(true)
                    crossfade(500)
                    transformations(RoundedCornersTransformation(10F))
                }
            }
        }
    }

    private fun uploadImage(animalName : String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            imageUri?.let { uri ->
                imageStorageReference.child(animalName).putFile(uri)
                    .addOnProgressListener {
                        binding.progressBarLoadingIndicator.visibility = View.VISIBLE
                        binding.textViewIndicatorLoading.visibility = View.VISIBLE

                        val progress: Int = ((100 * it.bytesTransferred) / it.totalByteCount).toInt()
                        binding.progressBarLoadingIndicator.progress = progress
                        val indicator = "Loading... $progress%"
                        binding.textViewIndicatorLoading.text = indicator
                    }.await()

                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity , "Success Uploaded", Toast.LENGTH_LONG).show()
                    resetApplication()
                }
            }
        } catch (e : Exception) {
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity , e.message, Toast.LENGTH_LONG).show()
                resetApplication()
            }
        }
    }

    private fun downloadImage(animalName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val maxDownloadSize = 5L * 1024 * 1024
            val bytes = imageStorageReference.child(animalName).getBytes(maxDownloadSize).await()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//            binding.progressBarLoadingIndicator.visibility = View.VISIBLE
//            binding.progressBarLoadingIndicator.isIndeterminate = true

            withContext(Dispatchers.Main) {
                binding.progressBarLoadingIndicator.visibility = View.GONE
                binding.imageViewAnimal.load(bmp){
                    crossfade(true)
                    crossfade(500)
                    transformations(RoundedCornersTransformation(10F))
                }
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                binding.progressBarLoadingIndicator.visibility = View.GONE
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteImage(animalName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            imageStorageReference.child(animalName).delete().await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Successfully deleted image.",
                    Toast.LENGTH_LONG).show()
                resetApplication()
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                resetApplication()
            }
        }
    }

}