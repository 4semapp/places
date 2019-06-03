package com.tvestergaard.places.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.tvestergaard.places.MainActivity
import com.tvestergaard.places.R
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.AnkoLogger

class ProfileFragment : Fragment(), AnkoLogger {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onStart() {
        super.onStart()

        val main = activity as MainActivity
        with(main.account!!) {
            Glide.with(activity).load(picture).into(profilePicture)
            nameText.text = name
            localeText.text = locale
            numberOfPostsText.text = 5.toString()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ProfileFragment().apply {
                arguments = Bundle()
            }
    }
}