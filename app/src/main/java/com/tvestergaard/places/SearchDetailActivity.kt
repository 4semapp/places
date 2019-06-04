package com.tvestergaard.places

import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tvestergaard.places.transport.InPlace

import kotlinx.android.synthetic.main.activity_search_detail.*
import kotlinx.android.synthetic.main.activity_search_detail_picture.view.*
import org.jetbrains.anko.AnkoLogger

class SearchDetailActivity : AppCompatActivity(), OnMapReadyCallback, AnkoLogger {

    private lateinit var place: InPlace

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search_detail)
        place = intent.extras.getSerializable("place") as InPlace


        recyclerViewPlace.layoutManager = LinearLayoutManager(this)
        recyclerViewPlace.adapter = FullPictureAdapter(place)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        titleTextView.text = place.title
        descriptionTextView.text = place.description
    }

    override fun onMapReady(map: GoogleMap) {
        val position = LatLng(place.latitude.toDouble(), place.longitude.toDouble())
        map.addMarker(MarkerOptions().position(position).title(place.title))
        map.moveCamera(CameraUpdateFactory.newLatLng(position))
    }

    private inner class FullPictureAdapter(private val item: InPlace) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(this@SearchDetailActivity).inflate(
                    R.layout.activity_search_detail_picture,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return item.pictures.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.picture.glide(item.pictures[position].fullName)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val picture: ImageView = view.fullPicture
    }
}
