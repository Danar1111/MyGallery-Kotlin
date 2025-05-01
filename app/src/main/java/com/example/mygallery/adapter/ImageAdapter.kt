package com.example.mygallery.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mygallery.DetailActivity
import com.example.mygallery.R
import com.example.mygallery.model.ImageItem

class ImageAdapter(
    private val context: Context,
    private val imageList: MutableList<ImageItem>
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private val selectedItems = mutableSetOf<Int>()
    private var selectionChangeListener: ((Int) -> Unit)? = null

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
        val overlay: View = view.findViewById(R.id.image_overlay)
        val label: TextView = view.findViewById(R.id.image_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageItem = imageList[position]

        holder.label.text = "Photo ${position + 1}"
        holder.label.visibility = View.VISIBLE

        Glide.with(holder.imageView.context)
            .load(imageItem.imageUri)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imageView)

        holder.overlay.visibility = if (selectedItems.contains(position)) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            if (selectedItems.isNotEmpty()) {
                toggleSelection(position)
            } else {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("imageResId", imageItem.imageUri)
                context.startActivity(intent)
            }
        }

        holder.itemView.setOnLongClickListener {
            toggleSelection(position)
            true
        }
    }

    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }

        notifyItemChanged(position)

        selectionChangeListener?.invoke(selectedItems.size)
    }

    fun deleteSelectedItems() {
        val newList = imageList.filterIndexed { index, _ -> !selectedItems.contains(index) }
        imageList.clear()
        imageList.addAll(newList)

        selectedItems.clear()

        notifyDataSetChanged()
        selectionChangeListener?.invoke(0)
    }

    fun setOnSelectionChangeListener(listener: (Int) -> Unit) {
        this.selectionChangeListener = listener
    }

    override fun getItemCount(): Int = imageList.size
}