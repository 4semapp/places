package com.tvestergaard.places.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.tvestergaard.places.MainActivity
import com.tvestergaard.places.R
import com.tvestergaard.places.runOnUiThread
import com.tvestergaard.places.transport.BackendCommunicator
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync

class ProfileFragment : Fragment(), AnkoLogger {

    private val backend = BackendCommunicator()

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
            numberOfPostsText.text = "-"
            profilePicture.contentDescription = "$name' profile picture"

            doAsync {
                val count = backend.countPosts(main.account!!)
                runOnUiThread {
                    numberOfPostsText.text = count.toString()
                }
            }
        }

        signOutButton.setOnClickListener {
            main.signOut()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(prevState: Bundle? = Bundle()) =
            ProfileFragment().apply {
                arguments = prevState
            }
    }
}