package com.tvestergaard.places.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tvestergaard.places.R
import java.io.File


class GalleryFragment : Fragment() {


    var listener: Listener? = null
    var imageDirectory: File? = null
    private var columnCount = 1
    private lateinit var adapter: ImageRecyclerViewAdapter
    private val images: MutableList<Image> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_image_list, container, false)
        if (view is RecyclerView) {
            this.adapter = ImageRecyclerViewAdapter(images, listener)
            view.adapter = this.adapter
            view.layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
        }

        if (imageDirectory != null) {
            addImages(readImages(imageDirectory!!))
        }

        return view
    }

    private fun readImages(directory: File): Array<out File> {
        return directory.listFiles()
    }

    private fun addImages(images: Array<out File>) {
        this.images.addAll(images.map { Image(it) })
        this.adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        listener = null
        super.onDestroy()
    }

    interface Listener {
        fun onClick(item: Image?)
    }

    data class Image(val file: File) {
        override fun toString(): String = file.absolutePath
    }
}
