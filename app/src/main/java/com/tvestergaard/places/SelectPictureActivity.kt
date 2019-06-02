package com.tvestergaard.places

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.FragmentTransaction
import com.tvestergaard.places.fragments.CameraFragment
import com.tvestergaard.places.fragments.GalleryFragment
import kotlinx.android.synthetic.main.activity_select_picture.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.toast
import java.io.File

class SelectPictureActivity : AppCompatActivity(), AnkoLogger {
    private var mediaStorageDir = File(
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ), CameraFragment.subdirectoryName
    )

    private var gallery = GalleryFragment.create(mediaStorageDir)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_picture)
    }

    override fun onStart() {
        super.onStart()
        loadStoredImages()
        completeSelectionButton.setOnClickListener {
            if (gallery.selected.size > 0) {
                val intent = Intent()
                intent.putExtra("selected", gallery.selected.toTypedArray())
                setResult(2, intent)
                finish()
            } else {
                toast("No Pictures selected")
            }
        }
    }

    private fun loadStoredImages() {
        this
            .supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.galleryFragmentContainer, gallery)
            .commitAllowingStateLoss()
    }


}
