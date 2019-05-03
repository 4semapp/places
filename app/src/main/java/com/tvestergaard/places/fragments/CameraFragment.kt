package com.tvestergaard.places.fragments

import android.Manifest
import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
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
import android.net.Uri
import com.tvestergaard.places.MainActivity
import java.io.File
import java.lang.Exception
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Environment
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v4.content.FileProvider
import com.tvestergaard.places.BuildConfig
import com.tvestergaard.places.R.*
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import java.text.SimpleDateFormat
import java.util.*


// https://developer.android.com/training/camera/photobasics
// https://developer.android.com/training/data-storage/files
// https://stackoverflow.com/questions/41144898/android-camera-intent-fileuriexposedexception-for-sdk-24
// https://inthecheesefactory.com/blog/how-to-share-access-to-file-with-fileprovider-on-android-nougat/en
// https://developer.android.com/training/secure-file-sharing/setup-sharing

class CameraFragment : Fragment(), AnkoLogger {

    private var parent: MainActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.fragment_camera, container, false)
    }

    override fun onStart() {
        super.onStart()
        newPictureButton.onClick {
            dispatchTakePictureIntent()
        }

        val checkCameraPermission = checkSelfPermission(parent!!, CAMERA) != PERMISSION_GRANTED
        val checkExternalStoragePermission = checkSelfPermission(parent!!, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED

        if (checkCameraPermission && checkExternalStoragePermission) {
            newPictureButton.isEnabled = false
            ActivityCompat.requestPermissions(
                parent!!,
                arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE),
                requestPermissionsCode
            )
        } else {
            newPictureButton.isEnabled = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == requestPermissionsCode) {
            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PERMISSION_GRANTED &&
                grantResults[1] == PERMISSION_GRANTED
            ) {
                newPictureButton.isEnabled = true
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val file = getOutputMediaFile() ?: return
            val uri = toUri(file)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(intent, requestImageCaptureCode)
        } catch (e: Exception) {
            info("Failed", e)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getOutputMediaFile(): File? {
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ), directoryName
        )

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                error("Could not create directories for picture")
                return null
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return File(
            mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg"
        )
    }

    private fun toUri(file: File): Uri {
        return FileProvider.getUriForFile(
            parent!!,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == requestImageCaptureCode && resultCode == RESULT_OK) {
            toast(string.pictureTakenSucess)
        } else {
            toast("Could not take picture.")
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

        const val requestPermissionsCode = 0
        const val requestImageCaptureCode = 1
        const val directoryName = "places"

        @JvmStatic
        fun newInstance() =
            CameraFragment().apply {
                arguments = Bundle()
            }
    }
}
