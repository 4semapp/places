package com.tvestergaard.places.fragments

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import com.tvestergaard.places.transport.BackendCommunicator
import kotlinx.android.synthetic.main.fragment_search.*
import com.tvestergaard.places.SearchDetailActivity
import com.tvestergaard.places.glide
import com.tvestergaard.places.runOnUiThread
import com.tvestergaard.places.transport.InPlace
import kotlinx.android.synthetic.main.fragment_search_item.view.*
import org.jetbrains.anko.*
import com.tvestergaard.places.R.*
import com.tvestergaard.places.reverseGeocode


class SearchFragment : Fragment(), AnkoLogger {

    private val backendCommunicator = BackendCommunicator()
    private val results = mutableListOf<InPlace>()
    private lateinit var adapter: SearchResultsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(layout.fragment_search, container, false)

    override fun onStart() {
        super.onStart()

        adapter = SearchResultsAdapter(results, activity)
        searchResults.layoutManager = LinearLayoutManager(activity)
        searchResults.adapter = adapter

        searchInput.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(search: String?): Boolean {
                this@SearchFragment.searchFor(search ?: "")
                return true
            }

            override fun onQueryTextChange(p0: String?) = true
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            this.searchInput.setQuery(savedInstanceState.get("query") as String, true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("query", this.searchInput.query.toString())
    }

    private fun searchFor(search: String) {
        doAsync {
            results.clear()
            results.addAll(backendCommunicator.search(search))
            runOnUiThread {
                adapter.notifyDataSetChanged()

                // collapse the SearchView keyboard
                searchInput.clearFocus()
                searchInput.onActionViewCollapsed()
                screen.requestFocus()
            }
        }
    }

    private class SearchResultsAdapter(val items: List<InPlace>, val context: Context) : Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(context).inflate(
                    layout.fragment_search_item,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            if (holder != null) {
                with(holder) {
                    val place = items[position]
                    title.text = place.title
                    location.text = context.reverseGeocode(place.latitude.toDouble(), place.longitude.toDouble())
                    poster.text = place.user.name
                    thumbnail.glide(place.pictures[0].thumbName)
                    container.setOnClickListener { this@SearchResultsAdapter.showDetail(place) }
                }
            }
        }

        private fun showDetail(place: InPlace) {
            val intent = Intent(context, SearchDetailActivity::class.java)
            intent.putExtra("place", place)
            context.startActivity(intent)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title
        val thumbnail: ImageView = view.thumbnail
        val poster: TextView = view.poster
        val location: TextView = view.location
        val container = view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SearchFragment().apply {
                arguments = Bundle()
            }
    }
}
