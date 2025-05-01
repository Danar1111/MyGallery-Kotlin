package com.example.mygallery

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mygallery.adapter.ImageAdapter
import com.example.mygallery.model.ImageItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private val PICK_IMAGE_CODE = 100
    private val CAMERA_REQUEST_CODE = 101

    private lateinit var recyclerView: RecyclerView
    private var dynamicImageList = mutableListOf<ImageItem>()

    private lateinit var sharedPrefs: SharedPreferences
    private val gson = Gson()
    private val PREF_KEY = "gallery_images"
    private var isFabOpen = false
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefs = getSharedPreferences("gallery", MODE_PRIVATE)

        loadSavedImages()

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = ImageAdapter(this, dynamicImageList)
        recyclerView.adapter = adapter

        val fabMain: View = findViewById(R.id.fab_main)
        val fabUpload: View = findViewById(R.id.fab_upload)
        val fabCamera: View = findViewById(R.id.fab_camera)
        val fabDelete: View = findViewById(R.id.fab_delete)
        fabDelete.visibility = View.GONE

        adapter.setOnSelectionChangeListener { selectedCount ->
            fabDelete.visibility = if (selectedCount > 0) View.VISIBLE else View.GONE
        }

        fabDelete.setOnClickListener {
            adapter.deleteSelectedItems()
            saveImages()
        }

        fabMain.setOnClickListener {
            isFabOpen = !isFabOpen
            if (isFabOpen) {
                animateFab(fabUpload, true, -120f)
                animateFab(fabCamera, true, -220f)
            } else {
                animateFab(fabUpload, false, 0f)
                animateFab(fabCamera, false, 0f)
            }
        }

        fabUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_CODE)
        }

        fabCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }
    }

    private fun animateFab(fab: View, show: Boolean, offsetY: Float) {
        fab.animate()
            .translationY(if (show) offsetY else 0f)
            .alpha(if (show) 1f else 0f)
            .scaleX(if (show) 1f else 0f)
            .scaleY(if (show) 1f else 0f)
            .setDuration(200)
            .start()
        fab.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_CODE -> {
                    val uri = data?.data
                    uri?.let {
                        dynamicImageList.add(ImageItem(it.toString()))
                        adapter.notifyDataSetChanged()
                        saveImages()
                    }
                }

                CAMERA_REQUEST_CODE -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        val path = MediaStore.Images.Media.insertImage(contentResolver, it, "CameraImage", null)
                        path?.let { imagePath ->
                            dynamicImageList.add(ImageItem(imagePath))
                            adapter.notifyDataSetChanged()
                            saveImages()
                        }
                    }
                }
            }
        }
    }

    private fun saveImages() {
        val json = gson.toJson(dynamicImageList)
        sharedPrefs.edit().putString(PREF_KEY, json).apply()
    }

    private fun loadSavedImages() {
        val json = sharedPrefs.getString(PREF_KEY, null)
        if (json != null) {
            val type = object : TypeToken<List<ImageItem>>() {}.type
            val savedList: List<ImageItem> = gson.fromJson(json, type)
            dynamicImageList.addAll(savedList)
        }
    }
}
