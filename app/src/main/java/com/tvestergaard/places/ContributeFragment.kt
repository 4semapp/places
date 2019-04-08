package com.tvestergaard.places

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ContributeFragment : Fragment() {

    private var parent: Parent? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contribute, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Parent) {
            parent = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        parent = null
    }

    interface Parent {

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ContributeFragment().apply {
                arguments = Bundle()
            }
    }
}
