package com.goggxi.storagefirebaseapps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.goggxi.storagefirebaseapps.databinding.ListAnimalBinding

class AnimalAdapter(private val urlImage: List<String>) : RecyclerView.Adapter<AnimalAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ListAnimalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String) {
            with(binding) {
                imageViewListAnimal.load(url){
                    crossfade(true)
                    crossfade(500)
                    transformations(RoundedCornersTransformation(10F))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding  = ListAnimalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        urlImage[position].let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return urlImage.size
    }

}