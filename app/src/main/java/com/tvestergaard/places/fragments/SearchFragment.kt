package com.tvestergaard.places.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.tvestergaard.places.R
import com.tvestergaard.places.transport.BackendCommunicator
import kotlinx.android.synthetic.main.fragment_search.*
import com.tvestergaard.places.SearchDetailActivity
import com.tvestergaard.places.glide
import com.tvestergaard.places.runOnUiThread
import com.tvestergaard.places.transport.InSearchResult
import kotlinx.android.synthetic.main.fragment_search_master_item.view.*
import org.jetbrains.anko.*


class SearchFragment : Fragment(), AnkoLogger {

    private val backendCommunicator = BackendCommunicator()
    private val results = mutableListOf<InSearchResult>()
    private lateinit var adapter: SearchResultsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_search, container, false)

    override fun onStart() {
        super.onStart()

        adapter = SearchResultsAdapter(results, activity)
        searchResults.layoutManager = LinearLayoutManager(activity)
        searchResults.adapter = adapter

        searchButton.setOnClickListener {
            val search = searchInput.text.toString()
            doAsync {
                results.clear()
                results.addAll(backendCommunicator.search(search))
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private class SearchResultsAdapter(val items: List<InSearchResult>, val context: Context) : Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.fragment_search_master_item,
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
                    poster.text = place.user.name
                    thumbnail.glide(place.pictures[0].thumbName)
                    container.setOnClickListener { this@SearchResultsAdapter.showDetail(place) }
                }
            }
        }

        private fun showDetail(place: InSearchResult) {
            val intent = Intent(context, SearchDetailActivity::class.java)
            intent.putExtra("place", place)
            context.startActivity(intent)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title
        val thumbnail: ImageView = view.thumbnail
        val poster: TextView = view.poster
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
