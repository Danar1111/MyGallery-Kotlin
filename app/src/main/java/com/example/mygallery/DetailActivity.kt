package com.example.mygallery

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyGallery)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val imageView: ImageView = findViewById(R.id.detail_image)
        val imageUri = intent.getStringExtra("imageResId")

        Glide.with(this)
            .load(Uri.parse(imageUri))
            .into(imageView)
    }
}
