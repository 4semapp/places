package com.tvestergaard.places.fragments

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
import com.tvestergaard.places.MainActivity
import java.io.File
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Environment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v4.content.FileProvider
import com.tvestergaard.places.BuildConfig
import com.tvestergaard.places.R
import com.tvestergaard.places.R.*
import java.text.SimpleDateFormat
import java.util.*


// https://developer.android.com/training/camera/photobasics
// https://developer.android.com/training/data-storage/files
// https://stackoverflow.com/questions/41144898/android-camera-intent-fileuriexposedexception-for-sdk-24
// https://inthecheesefactory.com/blog/how-to-share-access-to-file-with-fileprovider-on-android-nougat/en
// https://developer.android.com/training/secure-file-sharing/setup-sharing

class CameraFragment : Fragment(), AnkoLogger {

    private var parent: MainActivity? = null
    private var mediaStorageDir = File(
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ), subdirectoryName
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.fragment_camera, container, false)
    }

    override fun onStart() {
        super.onStart()
        newPictureButton.onClick {
            dispatchTakePictureIntent()
        }

        val checkCameraPermission = isGranted(checkSelfPermission(parent!!, CAMERA))
        val checkExternalStoragePermission = isGranted(checkSelfPermission(parent!!, WRITE_EXTERNAL_STORAGE))

        if (checkCameraPermission && checkExternalStoragePermission) {
            newPictureButton.isEnabled = false
            ActivityCompat.requestPermissions(parent!!, arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE), requestPermissionsCode)
        }


        val galleryFragment = GalleryFragment()
        galleryFragment.imageDirectory = mediaStorageDir

        val transaction = parent!!.supportFragmentManager.beginTransaction()
        transaction
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.galleryFragmentContainer, galleryFragment)
            .commitAllowingStateLoss()

        // TODO: set height of to match viewheight - button
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == requestPermissionsCode) {
            if (isGranted(grantResults)) {
                newPictureButton.isEnabled = true
            }
        }
    }

    private fun isGranted(code: Int): Boolean {
        return code == PERMISSION_GRANTED
    }

    private fun isGranted(codes: IntArray): Boolean {
        for (code in codes)
            if (code == PERMISSION_DENIED)
                return false

        return true
    }

    private fun dispatchTakePictureIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = getOutputMediaFile() ?: return
        val uri = toUri(file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, requestImageCaptureCode)
    }

    private fun getOutputMediaFile(): File? {

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return File(
            mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg"
        )
    }

    /**
     * Returns the write-safe uri for the provided file.
     */
    private fun toUri(file: File) =
        FileProvider.getUriForFile(parent!!, fileProviderName, file)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == requestImageCaptureCode && resultCode == RESULT_OK) {
            toast(string.pictureTakenSucess)
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
        const val subdirectoryName = "places"
        const val fileProviderName = "${BuildConfig.APPLICATION_ID}.provider"

        @JvmStatic
        fun newInstance() =
            CameraFragment().apply {
                arguments = Bundle()
            }
    }
}
