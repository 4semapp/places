package com.tvestergaard.places.fragments

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tvestergaard.places.*
import com.tvestergaard.places.R.string.contribute_choose_pictures_button_size
import com.tvestergaard.places.transport.BackendCommunicator
import com.tvestergaard.places.transport.OutPicture
import com.tvestergaard.places.transport.OutPlace
import kotlinx.android.synthetic.main.fragment_contribute.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.toast
import java.io.File

val onResumeOperations = mutableListOf<(ContributeFragment) -> Unit>()

class ContributeFragment : Fragment(), AnkoLogger, android.location.LocationListener {

    private var locationManager: LocationManager? = null
    private var images = arrayListOf<DiskImage>()
    private var requiredPermissions = arrayOf(READ_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION)

    // used to lock the manualLocation property
    // when true the manualLocation value will not be set to true, when the user edits the longitude or latitude
    // used to differentiate between the user editing inputs and the input values being set programmatically
    private var manualLocationLock = false

    // when false, the user is provided their current location
    // within the longitude and latitude inputs
    // set to true when the user edits the longitude or latitude inputs
    private var manualLocation = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contribute, container, false)
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()

        choosePicturesButton.setOnClickListener {
            var intent = Intent(activity, SelectPictureActivity::class.java)
            startActivityForResult(intent, SELECT_PICTURE_REQUEST_CODE)
        }

        updateButtons()
        submitPlaceButton.setOnClickListener { submitPlace() }
        latitudeInput.addTextChangedListener(Watcher())
        longitudeInput.addTextChangedListener(Watcher())

        if (!arguments.isEmpty) {
            titleInput.setText(arguments.getString(TITLE_BUNDLE_KEY))
            images.clear()
            images.addAll(arguments.get(IMAGES_BUNDLE_KEY) as ArrayList<DiskImage>)
            updateButtons()
        }

        if (onResumeOperations.isNotEmpty()) {
            onResumeOperations.forEach { it.invoke(this) }
            onResumeOperations.clear()
            updateButtons()
        }
    }

    override fun onResume() {
        super.onResume()
        if (onResumeOperations.isNotEmpty()) {
            onResumeOperations.forEach { it.invoke(this) }
            onResumeOperations.clear()
            updateButtons()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TITLE_BUNDLE_KEY, titleInput?.text?.toString())
        outState.putSerializable(IMAGES_BUNDLE_KEY, this.images)
    }

    private fun updateButtons() {
        choosePicturesButton.text = getString(contribute_choose_pictures_button_size, images.size)
        enableButton(choosePicturesButton)
        if (images.isNotEmpty())
            enableButton(submitPlaceButton)
        else
            disableButton(submitPlaceButton)
    }

    private inner class Watcher : TextWatcher, AnkoLogger {

        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (!manualLocationLock)
                this@ContributeFragment.manualLocation = true
        }
    }

    private fun submitPlace() {

        disableButton(submitPlaceButton)
        disableButton(choosePicturesButton)

        val base64Images = images.map { img ->
            val thumbnailFile = img.file
            val fullFile = getFullFile(thumbnailFile)
            OutPicture(
                fullData = readBytesToBase64(fullFile),
                thumbData = readBytesToBase64(thumbnailFile)
            )
        }

        val toCreate = OutPlace(
            title = titleInput.text.toString(),
            description = descriptionInput.text.toString(),
            latitude = latitudeInput.text.toString().toFloat(),
            longitude = longitudeInput.text.toString().toFloat(),
            pictures = base64Images.toTypedArray()
        )

        doAsync {
            val response = BackendCommunicator().postPlace(toCreate)
            runOnUiThread {
                if (response == null)
                    toast(getString(R.string.contribute_place_created_error))
                else {
                    toast(getString(R.string.contribute_place_created_success))
                    this@ContributeFragment.reset()
                }
            }
        }
    }

    private fun getFullFile(thumbnailFile: File) =
        File(thumbnailFile.parent, thumbnailFile.name.removePrefix("thumb_"))

    private fun readBytesToBase64(imageFile: File) =
        Base64.encodeToString(imageFile.inputStream().readBytes(), Base64.DEFAULT)

    @SuppressLint("MissingPermission")
    private fun startLocationListener() {
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 10.0f, this)
        val lastKnownLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastKnownLocation != null)
            updateLocation(lastKnownLocation)
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null && !manualLocation) {
            updateLocation(location)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String?) {}
    override fun onProviderDisabled(provider: String?) {}

    private fun updateLocation(location: Location) {
        manualLocationLock = true
        longitudeInput.setText(location.longitude.round(4).toString())
        latitudeInput.setText(location.latitude.round(4).toString())
        manualLocationLock = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SELECT_PICTURE_REQUEST_CODE ->
                if (resultCode == RESULT_OK && data != null) {
                    this.manualLocation = true
                    onResumeOperations.add { self ->
                        val selected = data.extras[SelectPictureActivity.INTENT_SELECTED_KEY] as ArrayList<DiskImage>
                        self.images.clear()
                        self.images.addAll(selected)
                    }
                }
        }
    }

    /**
     * Checks whether or not we have requiredPermissions to access the location data.
     *
     * Updates the hasPermissions field. Requests permission from the user.
     */
    private fun checkPermissions() {
        if (!hasPermissions(requiredPermissions))
            requestPermissions(requiredPermissions, PERMISSION_REQUEST_CODE)
        else
            startLocationListener()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (isGranted(grantResults))
                    startLocationListener()
            }
        }
    }

    private fun reset() {
        titleInput.setText("")
        images.clear()
        updateButtons()
    }

    override fun onDestroy() {
        locationManager?.removeUpdates(this)
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance(prevState: Bundle? = Bundle()) =
            ContributeFragment().apply {
                arguments = prevState
            }

        const val SELECT_PICTURE_REQUEST_CODE = 2
        const val PERMISSION_REQUEST_CODE = 4
        const val TITLE_BUNDLE_KEY = "title"
        const val IMAGES_BUNDLE_KEY = "images"
    }
}
