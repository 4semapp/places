package com.tvestergaard.places.fragments

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_camera.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.support.v4.toast
import java.io.File
import android.support.v4.app.ActivityCompat
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.Environment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.FileProvider
import com.tvestergaard.places.R.*
import android.graphics.Bitmap
import android.support.media.ExifInterface
import com.tvestergaard.places.*
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import java.io.ByteArrayOutputStream


// https://developer.android.com/training/camera/photobasics
// https://developer.android.com/training/data-storage/files
// https://stackoverflow.com/questions/41144898/android-camera-intent-fileuriexposedexception-for-sdk-24
// https://inthecheesefactory.com/blog/how-to-share-access-to-file-with-fileprovider-on-android-nougat/en
// https://developer.android.com/training/secure-file-sharing/setup-sharing

class CameraFragment : Fragment(), AnkoLogger {

    private var permissions = arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE)
    private var takenPicture: File? = null
    private var mediaStorageDir = File(
        Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ), subdirectoryName
    )

    private var gallery = GalleryFragment.create(mediaStorageDir)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.fragment_camera, container, false)
    }

    override fun onStart() {
        super.onStart()

        if (!hasPermissions(permissions)) {
            newPictureButton.isEnabled = false
            ActivityCompat.requestPermissions(activity, permissions, requestPermissionsCode)
        } else {
            newPictureButton.isEnabled = true
            loadStoredImages()
        }

        newPictureButton.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    /**
     * Loads images and enables the 'take picture' button, when permissions have been granted.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == requestPermissionsCode) {
            if (isGranted(grantResults)) {
                newPictureButton.isEnabled = true
                loadStoredImages()
            }
        }
    }

    private fun loadStoredImages() {
        activity
            .supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.galleryFragmentContainer, gallery)
            .commitAllowingStateLoss()
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

        val timeStamp = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())

        takenPicture = File(
            mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg"
        )

        return takenPicture
    }

    /**
     * Returns the write-safe uri for the provided file.
     */
    private fun toUri(file: File) =
        FileProvider.getUriForFile(activity, fileProviderName, file)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == requestImageCaptureCode && resultCode == RESULT_OK) {
            toast(string.pictureTakenSuccess)
            if (takenPicture != null) {
                correctRotation(takenPicture!!)
                thumbnail(takenPicture!!)
                takenPicture = null
            }
        }
    }

    private fun correctRotation(picture: File) {

        val ei = ExifInterface(picture.absolutePath)
        val orientation = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        val inputBitmap = BitmapFactory.decodeFile(picture.absolutePath)
        var rotatedBitmap: Bitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(inputBitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(inputBitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(inputBitmap, 270)
            else -> inputBitmap
        }

        val bos = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        picture.writeBytes(bos.toByteArray())
    }

    private fun thumbnail(file: File) {
        val inputBitmap = BitmapFactory.decodeFile(file.absolutePath)
        val outputBitmap = ThumbnailUtils.extractThumbnail(inputBitmap, 500, 500)
        val outputFile = File(file.parentFile.absolutePath + "/thumb_" + file.name)
        FileOutputStream(outputFile).use { out ->
            outputBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            gallery.addImage(outputFile)
        }
    }

    companion object {

        const val requestPermissionsCode = 0
        const val requestImageCaptureCode = 1
        const val subdirectoryName = "places"
        const val fileProviderName = "${BuildConfig.APPLICATION_ID}.provider"

        @JvmStatic
        fun newInstance(prevState: Bundle? = Bundle()) =
            CameraFragment().apply {
                arguments = prevState
            }
    }
}
