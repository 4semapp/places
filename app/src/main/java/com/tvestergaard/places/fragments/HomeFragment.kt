package com.tvestergaard.places.fragments

import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.tvestergaard.places.R
import com.tvestergaard.places.SearchDetailActivity
import com.tvestergaard.places.runOnUiThread
import com.tvestergaard.places.transport.BackendCommunicator
import com.tvestergaard.places.transport.InPlace
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync

class HomeFragment : Fragment(), AnkoLogger {

    private val backend = BackendCommunicator()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onStart() {
        super.onStart()

        val places = mutableListOf<InPlace>()
        homeRecyclerView.layoutManager = LinearLayoutManager(activity)
        homeRecyclerView.adapter = HomePlacesAdapter(places)

        doAsync {
            val retrieved = backend.getHomePlaces()
            runOnUiThread {
                places.addAll(retrieved)
                homeRecyclerView.adapter.notifyDataSetChanged()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle()
            }
    }

    private inner class HomePlacesAdapter(val places: List<InPlace>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(this@HomeFragment.activity).inflate(
                    R.layout.fragment_home_item,
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val place = places[position]

            with(holder!!) {
                title.text = place.title
                description.text = place.description
                location.text = reverseGeocode(place.latitude.toDouble(), place.longitude.toDouble())
                container.setOnClickListener { showDetail(place) }
            }
        }

        private fun showDetail(place: InPlace) {
            val intent = Intent(context, SearchDetailActivity::class.java)
            intent.putExtra("place", place)
            context.startActivity(intent)
        }

        private fun reverseGeocode(latitude: Double, longitude: Double): String {
            val geoCoder = Geocoder(this@HomeFragment.activity)
            val addresses = geoCoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.size > 0) {
                val address = addresses[0]
                val sb = StringBuilder()
                for (i in 0 until address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i)).append(", ")
                }
                sb.append(address.locality).append(", ")
                sb.append(address.postalCode).append(", ")
                sb.append(address.countryName)
                return sb.toString()
            }

            return "Could not locate."
        }

        override fun getItemCount() = places.count()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.titleTextView
        val description: TextView = view.descriptionTextView
        val location: TextView = view.locationTextView
        val container: View = view
    }
}
