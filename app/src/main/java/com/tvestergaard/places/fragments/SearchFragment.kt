package com.tvestergaard.places.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tvestergaard.places.MainActivity
import com.tvestergaard.places.R
import com.tvestergaard.places.transport.BackendCommunicator
import kotlinx.android.synthetic.main.fragment_search.*
import com.bumptech.glide.Glide
import com.tvestergaard.places.SearchDetailActivity
import com.tvestergaard.places.transport.InSearchResult
import kotlinx.android.synthetic.main.fragment_search_result_master_item.view.*
import org.jetbrains.anko.*
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread


class SearchFragment : Fragment(), AnkoLogger {

    private var parent: MainActivity? = null
    private var backendCommunicator: BackendCommunicator = BackendCommunicator()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        val communicator = BackendCommunicator()

        btnSearch.setOnClickListener {
            val searchTitle = searchBar.text.toString()
            var inSearchResults: List<InSearchResult>? = null
            doAsync {
                inSearchResults = backendCommunicator.search(searchTitle)
                if (inSearchResults != null) {

                    runOnUiThread {
                        thumbNailList.layoutManager = LinearLayoutManager(parent)
                        thumbNailList.layoutManager = GridLayoutManager(parent, 2)
                        thumbNailList.adapter = ThumbnailAdapter(inSearchResults!!, activity)

                        inSearchResults?.forEach {
                        }
                    }

                }
            }
        }
    }

    class ThumbnailAdapter(val items: List<InSearchResult>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.fragment_search_result_master_item,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            holder?.thumbDetail?.text = items.get(position).description

            Glide.with(context)
                .load("${BackendCommunicator.IMG_ROOT}/${items.get(position).pictures.get(position).thumbName}")
                .into(holder?.thumbPic)

            holder?.personName?.text = items.get(position).user.name

            holder?.container?.setOnClickListener {
                val intent = Intent(context, SearchDetailActivity::class.java)
                intent.putExtra("place", items.get(position))
                context.startActivity(intent)
            }
        }
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val thumbDetail = view.thumbDetail
        val thumbPic = view.thumbPic
        val personName = view.personName

        val container = view
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
        @JvmStatic
        fun newInstance() =
            SearchFragment().apply {
                arguments = Bundle()
            }
    }
}
