package com.sample.imageloadersample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.sample.imagery.ImageLoader




class RecyclerAdapter(private val imageList: ArrayList<String>, context: Context):
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var imageLoader: ImageLoader = ImageLoader(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_images, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = imageList[position]
        imageLoader.displayImage(imageUrl, holder.imageView, R.drawable.ic_launcher_background)
    }

    override fun getItemCount(): Int {
       return imageList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.img_sample)
    }

}