package com.goggxi.storagefirebaseapps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.goggxi.storagefirebaseapps.databinding.ActivityShowListPhotoBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ShowListPhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowListPhotoBinding

    private val imageStorageReference = FirebaseStorage.getInstance().getReference("uploads")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowListPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "All Image"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listFiles()
    }

    private fun listFiles() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val images = imageStorageReference.listAll().await()
            val imageUrls = mutableListOf<String>()
            for(image in images.items) {
                val url = image.downloadUrl.await()
                imageUrls.add(url.toString())
            }
            withContext(Dispatchers.Main) {
                val imageAdapter = AnimalAdapter(imageUrls)
                binding.recyclerViewAnimal.apply {
                    adapter = imageAdapter
                    layoutManager = LinearLayoutManager(this@ShowListPhotoActivity)
                }
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ShowListPhotoActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}