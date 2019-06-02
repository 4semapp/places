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
import android.widget.BaseAdapter
import android.widget.TextView
import com.tvestergaard.places.transport.InSearchResult
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.toast
import pyxis.uzuki.live.richutilskt.utils.inflate


class SearchFragment : Fragment(), AnkoLogger {

    private var parent: MainActivity? = null
    private var backendCommunicator: BackendCommunicator = BackendCommunicator()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        /*doAsync {
            val resultIns = communicator.search("")
            runOnUiThread {
                searchResults.adapter = SearchResultAdapter(parent!!, resultIns.toTypedArray())
            }
        }*/

    }

    override fun onStart() {
        super.onStart()
        val communicator = BackendCommunicator()

        btnSearch.setOnClickListener {
            val searchTitle = searchBar.text.toString()
            var inSearchResults: List<InSearchResult>? = null //list of places
            doAsync {
                inSearchResults = backendCommunicator.search(searchTitle)
                if (inSearchResults != null) {
                    inSearchResults?.forEach {
                        info("---")
                        info(it)
                    }
                }
            }
            toast("SKAL DU HA TOOOOAST")

        }
    }


    class SearchResultAdapter(private val context: Context, val resultIns: Array<InSearchResult>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = context.inflate(R.layout.search_item, null)
            view.findViewById<TextView>(R.id.searchResultTitle).text = resultIns[position].title
            return view
        }

        override fun getItem(position: Int): Any {
            return resultIns[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong();
        }

        override fun getCount(): Int {
            return resultIns.size;
        }
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
