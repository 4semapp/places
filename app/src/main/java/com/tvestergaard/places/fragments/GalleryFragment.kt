package com.tvestergaard.places.fragments

import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.tvestergaard.places.R
import kotlinx.android.synthetic.main.fragment_gallery_image.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import java.io.File
import java.io.IOException


class GalleryFragment : Fragment(), GalleryAdapterListener {


    private var imageDirectory: File? = null
    private lateinit var adapter: GalleryAdapter
    private val diskImages: MutableList<DiskImage> = ArrayList()
    private var selectMode: Boolean = false
    val selected = mutableListOf<DiskImage>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_gallery, container, false)
        if (view is RecyclerView) {
            this.adapter = GalleryAdapter(diskImages, this)
            view.adapter = this.adapter
            val orientation = resources.configuration.orientation
            view.layoutManager = GridLayoutManager(
                context, when (orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> 4
                    else -> 2
                }
            )
        }

        if (imageDirectory != null)
            readThumbnails(imageDirectory!!)
                .toTypedArray()
                .forEach(this::addImage)

        return view
    }

    /**
     * Returns the thumbnails within the provided directory.
     */
    private fun readThumbnails(directory: File): List<File> {
        if (!directory.exists() || !directory.isDirectory)
            return ArrayList(0)

        return directory.listFiles().filter { it.name.contains("thumb_") }
    }

    /**
     * Adds a new image to the gallery on screen.
     */
    fun addImage(image: File) {
        this.diskImages.add(DiskImage(image, false))
        this.adapter.notifyDataSetChanged()
    }

    override fun onClick(item: DiskImage) {
        if (selectMode) {
            if (item.selected)
                selected.remove(item)
            else
                selected.add(item)
        }
    }

    override fun onLongClick(item: DiskImage) {
        alert("Are you sure you want to delete this image?", "Delete DiskImage") {
            yesButton {
                try {
                    item.file.delete()
                    File(item.file.parent, item.file.name.removePrefix("thumb_")).delete()
                    diskImages.remove(item)
                    adapter.notifyDataSetChanged()
                    toast("The image war successfully been deleted.")
                } catch (e: IOException) {
                    toast("The image could not be deleted.")
                }
            }
            noButton {}
        }.show()
    }

    companion object {
        fun create(imageDirectory: File? = null, selectMode: Boolean = false): GalleryFragment {
            val fragment = GalleryFragment()
            fragment.imageDirectory = imageDirectory
            fragment.selectMode = selectMode
            return fragment
        }
    }

    private inner class GalleryAdapter(
        private val diskImages: List<DiskImage>,
        private val listener: GalleryAdapterListener?
    ) :
        RecyclerView.Adapter<GalleryAdapter.ViewHolder>(), AnkoLogger {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_gallery_image, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val data = BitmapFactory.decodeStream(diskImages[position].file.inputStream())
            holder.imageView.setImageBitmap(data)

            with(holder.view) {

                tag = diskImages[position]

                setOnClickListener { v ->
                    val item = v.tag as DiskImage
                    listener?.onClick(item)
                    if (this@GalleryFragment.selectMode) {
                        if (item.selected) {
                            item.selected = false
                            holder.imageView.alpha = 1.0f
                        } else {
                            item.selected = true
                            //holder.imageView.background = resources.getDrawable(R.drawable.back, null)
                            //ResourcesCompat.getDrawable(resources, R.drawable.back, null)
                            // ImageViewCompat.setImageTintList(this.image, ColorStateList.valueOf(Color.RED))
                            holder.imageView.alpha = 0.5f
                        }
                    }
                }

                setOnLongClickListener { v ->
                    val item = v.tag as DiskImage
                    listener?.onLongClick(item)
                    true
                }


            }
        }

        override fun getItemCount(): Int = diskImages.size

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.image
        }
    }
}

interface GalleryAdapterListener {
    fun onClick(item: DiskImage)
    fun onLongClick(item: DiskImage)
}