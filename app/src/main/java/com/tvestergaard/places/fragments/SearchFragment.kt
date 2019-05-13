package com.tvestergaard.places.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tvestergaard.places.MainActivity
import com.tvestergaard.places.R
import com.tvestergaard.places.transport.BackendCommunicator
import kotlinx.android.synthetic.main.fragment_search.*
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import com.tvestergaard.places.transport.SearchResult
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import pyxis.uzuki.live.richutilskt.utils.inflate
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread


class SearchFragment : Fragment(), AnkoLogger {

    private var parent: MainActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val communicator = BackendCommunicator()

        doAsync {
            val results = communicator.search("")
            runOnUiThread {
                searchResults.adapter = SearchResultAdapter(parent!!, results.toTypedArray())
            }
        }
    }

    class SearchResultAdapter(private val context: Context, val results: Array<SearchResult>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = context.inflate(R.layout.search_item, null)
            view.findViewById<TextView>(R.id.searchResultTitle).text = results[position].title
            return view
        }

        override fun getItem(position: Int): Any {
            return results[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong();
        }

        override fun getCount(): Int {
            return results.size;
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
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
