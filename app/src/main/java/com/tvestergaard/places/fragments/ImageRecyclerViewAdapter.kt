package com.tvestergaard.places.fragments

import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.tvestergaard.places.R


import com.tvestergaard.places.fragments.GalleryFragment.Listener

import kotlinx.android.synthetic.main.fragment_image.view.*

class ImageRecyclerViewAdapter(
    private val mValues: List<GalleryFragment.Image>,
    private val mListener: Listener?
) : RecyclerView.Adapter<ImageRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as GalleryFragment.Image
            mListener?.onClick(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        val data = BitmapFactory.decodeStream(item.file.inputStream())
        holder.imageView.setImageBitmap(data)

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val imageView: ImageView = mView.image
    }
}
