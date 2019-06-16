package com.tvestergaard.places

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.FragmentTransaction
import com.tvestergaard.places.R.*
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

    private var gallery = GalleryFragment.create(mediaStorageDir, true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_select_picture)
    }

    override fun onStart() {
        super.onStart()
        loadStoredImages()
        completeSelectionButton.setOnClickListener {
            if (gallery.selected.size > 0) {
                val intent = Intent()
                intent.putExtra(INTENT_SELECTED_KEY, gallery.selected)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                toast(getString(string.no_pictures_selected_error))
            }
        }
    }

    private fun loadStoredImages() {
        this
            .supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(id.galleryFragmentContainer, gallery)
            .commitAllowingStateLoss()
    }

    companion object {
        const val INTENT_SELECTED_KEY = "selected"
    }
}
