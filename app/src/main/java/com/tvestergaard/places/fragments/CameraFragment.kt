package com.tvestergaard.places.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_camera.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.toast
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.net.Uri
import com.tvestergaard.places.MainActivity
import com.tvestergaard.places.R
import java.io.File
import java.lang.Exception


// https://developer.android.com/training/camera/photobasics
// https://developer.android.com/training/data-storage/files

class CameraFragment : Fragment(), AnkoLogger {

    private var parent: MainActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onStart() {
        super.onStart()
        newPictureButton.onClick {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            try {
                var dir = context!!.getExternalFilesDir(null)
                if (dir == null)
                    dir = context!!.filesDir
                val format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                val timeStamp = LocalDateTime.now().format(format)
                val output = File(dir, "JPEG_$timeStamp.jpg")
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output))
                takePictureIntent.resolveActivity(context!!.packageManager)?.also {
                    startActivityForResult(
                        takePictureIntent,
                        requestImageCaptureCode
                    )
                }
            } catch (e: Exception) {
                error(e)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == requestImageCaptureCode && resultCode == RESULT_OK) {
            toast(getString(R.string.pictureTakenSucess))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity)
            parent = context
    }

    override fun onDetach() {
        parent = null
        super.onDetach()
    }

    companion object {

        const val requestImageCaptureCode = 1

        @JvmStatic
        fun newInstance() =
            ContributeFragment().apply {
                arguments = Bundle()
            }
    }
}
