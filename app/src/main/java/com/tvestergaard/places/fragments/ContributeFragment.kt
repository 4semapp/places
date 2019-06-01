package com.tvestergaard.places.fragments

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v4.content.ContextCompat.getSystemService
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import com.tvestergaard.places.MainActivity
import com.tvestergaard.places.R
import com.tvestergaard.places.SelectPictureActivity
import khttp.async
import khttp.post
import kotlinx.android.synthetic.main.fragment_contribute.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.db.DEFAULT
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.toast
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import java.io.File

val gson = GsonBuilder().create()

class ContributeFragment : Fragment(), AnkoLogger, android.location.LocationListener {

    private lateinit var parent: MainActivity
    private var numberOfUpdates = 0
    private var currentLocation: Location? = null
    private lateinit var locationManager: LocationManager
    private val permissionRequestCode = 0
    private var hasPermissions = false
    private var images = arrayOf<Image>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contribute, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = parent.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 10.0f, this)
    }

    override fun onStart() {
        super.onStart()
        btnSelectPictures.setOnClickListener {
            var intent = Intent(parent, SelectPictureActivity::class.java)
            startActivityForResult(intent, selectPictureRequestCode)
        }
        if (images.size > 0) {
            btnStorePictures.isEnabled = true
        } else {
            btnStorePictures.isEnabled = false
        }
        btnStorePictures.setOnClickListener {
            toast("You have clicked a buttoN!")

            info(images)
            images[0].file
            val base64Images = images.map { img ->
                val thumb = img.file
                val full = File(thumb.parent, thumb.name.removePrefix("thumb_"))

                val thumbBase64 = Base64.encodeToString(thumb.inputStream().readBytes(), Base64.DEFAULT)
                val fullBase64 = Base64.encodeToString(full.inputStream().readBytes(), Base64.DEFAULT)
                InPicture(fullData = fullBase64, thumbData = thumbBase64)

            }
            val inPlace = InPlace(
                editTitle.text.toString(),
                editDescription.text.toString(),
                lat = editLat.text.toString().toFloat(),
                lon = editLon.text.toString().toFloat(),
                pictures = base64Images.toTypedArray()
            )

            doAsync {
                val response = post("http://cb811550.ngrok.io/places", data = gson.toJson(inPlace), headers = mapOf("Content-Type" to "application/json"))
                runOnUiThread {
                    if (response.statusCode < 200 || response.statusCode > 299) {
                        toast("You could not be authenticated.")
                    }
                }
            }

        }
    }


    data class InPicture(
        var fullData: String,
        var thumbData: String
    )

    data class InPlace(
        var title: String,
        var description: String,
        var lat: Float,
        var lon: Float,
        var pictures: Array<InPicture>
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            parent = context
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            currentLocation = location
            numberOfUpdates++
            updateDisplay(location)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            selectPictureRequestCode -> {
                if (data != null) {
                    images = data!!.extras["selected"] as Array<Image>
                }
            }
        }
        updateLocation()
        if (images.size > 0) {
            //call endpoint here
            btnSelectPictures.isEnabled = true
        } else {
            btnStorePictures.isEnabled = false
        }
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
     * Checks whether or not we have permissions to access the location data.
     *
     * Updates the hasPermissions field. Requests permission from the user.
     */
    private fun checkPermissions() {
        if (checkSelfPermission(parent, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), permissionRequestCode)
        } else {
            hasPermissions = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            permissionRequestCode -> { // ACCESS_FINE_LOCATION
                this.hasPermissions = grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED
                if (this.hasPermissions)
                    updateLocation()
                else
                    toast("You must grant location permissions for the application to work.")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocation() {

        if (!this.hasPermissions) {
            checkPermissions()
            return
        }

        val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location == null) {
            toast("Could not retrieve the last known location.")
        } else {

            editTitle.setText("Test1")
            editDescription.setText("desc1")
            editLon.setText(location.longitude.toString())
            editLat.setText(location.latitude.toString())
        }
    }

    //not used
    private fun updateDisplay(location: Location?) {
        if (location != null) {
            editTitle.setText("Test1")
            editDescription.setText("desc1")
            editLon.setText(location.longitude.toString())
            editLat.setText(location.latitude.toString())
        }
    }


}
