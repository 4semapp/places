package com.tvestergaard.places

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.tvestergaard.places.fragments.CameraFragment
import com.tvestergaard.places.fragments.ContributeFragment
import com.tvestergaard.places.fragments.SearchFragment
import com.tvestergaard.places.pages.AuthenticationActivity
import com.tvestergaard.places.pages.AuthenticationActivity.Companion.authenticationRequestCode
import com.tvestergaard.places.pages.HomeFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.toast
import java.lang.RuntimeException
import android.R.attr.data



class MainActivity : AppCompatActivity(), AnkoLogger {

    private var currentNavigationFragment = -1
    private var account: GoogleSignInAccount? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(createNavigationListener())

        account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null)
            show(0, HomeFragment())
        else
            promptAuthentication()
    }

    /**
     * Prompts the user to authenticate using google services.
     */
    private fun promptAuthentication() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivityForResult(intent, authenticationRequestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * Called when the user successfully authenticates.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }

        when (requestCode) {
            authenticationRequestCode -> {
                if (data != null) {
                    account = data.extras["account"] as GoogleSignInAccount
                    toast("Welcome ${account!!.givenName}")
                    show(0, HomeFragment())
                }
            }
        }
    }


    private fun createNavigationListener(): BottomNavigationView.OnNavigationItemSelectedListener {
        return BottomNavigationView.OnNavigationItemSelectedListener { item ->

            val fragment: Fragment = when (item.itemId) {
                R.id.navigation_home -> HomeFragment.newInstance()
                R.id.navigation_search -> SearchFragment.newInstance()
                R.id.navigation_contribute -> ContributeFragment.newInstance()
                R.id.navigation_camera -> CameraFragment.newInstance()
                else -> throw RuntimeException("unhandled fragment type ${item.itemId}.")
            }

            show(item.itemId, fragment)
            true
        }
    }

    private fun getPositionalItem(item: Int): Int {
        return when (item) {
            R.id.navigation_home -> 1
            R.id.navigation_camera -> 2
            R.id.navigation_search -> 3
            R.id.navigation_contribute -> 4
            else -> -1
        }
    }

    private fun show(id: Int, fragment: Fragment) {

        val position = getPositionalItem(id)

        val transaction = supportFragmentManager.beginTransaction()

        if (currentNavigationFragment != -1 && currentNavigationFragment < position)
            transaction.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_left
            )
        if (currentNavigationFragment != -1 && currentNavigationFragment > position)
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )

        transaction
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.fragmentContainer, fragment)
            .commitAllowingStateLoss()

        currentNavigationFragment = position
    }
}
