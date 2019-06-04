package com.tvestergaard.places

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tvestergaard.places.transport.BackendCommunicator
import com.tvestergaard.places.transport.InSearchResult

import kotlinx.android.synthetic.main.activity_search_detail.*
import kotlinx.android.synthetic.main.activity_search_detail_picture.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class SearchDetailActivity : AppCompatActivity(), OnMapReadyCallback, AnkoLogger {

    private var inPlace: InSearchResult? = null //InSearchResult = InPlace
    private lateinit var mMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search_detail)
        inPlace = intent.extras.getSerializable("place") as InSearchResult
        recyclerViewPlace.layoutManager = LinearLayoutManager(this)
        recyclerViewPlace.adapter = FullPictureAdapter(inPlace as InSearchResult, this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        info("AYYYYYYYYYYYYYYYYYYY")
        info(inPlace?.latitude)
        info(inPlace?.longitude)
        //get info for marker
        val curPosition = LatLng(inPlace?.latitude!!.toDouble(), inPlace?.longitude!!.toDouble())
        mMap.addMarker(MarkerOptions().position(curPosition).title("Picture Taken here "))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(curPosition))
        /*
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        */
    }

    class FullPictureAdapter(val item: InSearchResult, val context: Context) : RecyclerView.Adapter<ViewHolder>(),
        AnkoLogger {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.activity_search_detail_picture,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return item.pictures.size
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            Glide.with(context) //use helper here
                .load("${BackendCommunicator.IMG_ROOT}/${item.pictures.get(position).fullName}")
                .into(holder?.picture)
        }
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val picture = view.fullPicture
    }


    override fun onStart() {
        super.onStart()
        titlePlace.text = inPlace?.title
        description.text = inPlace?.description


    }
}
