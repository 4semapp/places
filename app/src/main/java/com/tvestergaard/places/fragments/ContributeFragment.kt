package com.tvestergaard.places.fragments

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
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
import com.google.gson.GsonBuilder
import com.tvestergaard.places.*
import com.tvestergaard.places.transport.BackendCommunicator
import com.tvestergaard.places.transport.OutPicture
import com.tvestergaard.places.transport.OutPlace
import kotlinx.android.synthetic.main.fragment_contribute.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.toast
import java.io.File

val gson = GsonBuilder().create()

class ContributeFragment : Fragment(), AnkoLogger, android.location.LocationListener {

    private lateinit var locationManager: LocationManager
    private var images = arrayOf<Image>()
    private val permissionRequestCode = 0
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

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()

        checkPermissions()
        choosePicturesButton.setOnClickListener {
            var intent = Intent(activity, SelectPictureActivity::class.java)
            startActivityForResult(intent, selectPictureRequestCode)
        }

        submitPlaceButton.isEnabled = images.isNotEmpty()
        submitPlaceButton.setOnClickListener { submitPlace() }
        latitudeInput.addTextChangedListener(Watcher())
        longitudeInput.addTextChangedListener(Watcher())
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

        val base64Images = images.map { img ->
            val thumbnailFile = img.file
            val fullFile = getFullFile(thumbnailFile)
            OutPicture(
                fullData = readBase64(fullFile),
                thumbData = readBase64(thumbnailFile)
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
                    toast("The place could not be created.")
                else
                    toast("The place was successfully created.")
            }
        }
    }

    private fun getFullFile(thumbnailFile: File) =
        File(thumbnailFile.parent, thumbnailFile.name.removePrefix("thumb_"))

    private fun readBase64(imageFile: File) =
        Base64.encodeToString(imageFile.inputStream().readBytes(), Base64.DEFAULT)

    @SuppressLint("MissingPermission")
    private fun startLocationListener() {
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 10.0f, this)
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
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
        longitudeInput.setText(location.longitude.toString())
        latitudeInput.setText(location.latitude.toString())
        manualLocationLock = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            selectPictureRequestCode ->
                if (data != null)
                    images = data.extras["selected"] as Array<Image>
        }

        submitPlaceButton.isEnabled = images.isNotEmpty()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ContributeFragment().apply {
                arguments = Bundle()
            }

        const val selectPictureRequestCode = 2
    }

    /**
     * Checks whether or not we have requiredPermissions to access the location data.
     *
     * Updates the hasPermissions field. Requests permission from the user.
     */
    private fun checkPermissions() {
        if (!hasPermissions(requiredPermissions))
            requestPermissions(requiredPermissions, permissionRequestCode)
        else
            startLocationListener()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            permissionRequestCode -> {
                if (isGranted(grantResults))
                    startLocationListener()
                else
                    toast("You must grant location requiredPermissions for the automatic location finder.")
            }
        }
    }

    override fun onDestroy() {
        locationManager.removeUpdates(this)
        super.onDestroy()
    }
}
