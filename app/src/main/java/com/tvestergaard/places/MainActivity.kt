package com.tvestergaard.places

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.tvestergaard.places.fragments.CameraFragment
import com.tvestergaard.places.fragments.ContributeFragment
import com.tvestergaard.places.fragments.SearchFragment
import com.tvestergaard.places.pages.AuthenticatedUser
import com.tvestergaard.places.pages.AuthenticationActivity
import com.tvestergaard.places.pages.AuthenticationActivity.Companion.authenticationRequestCode
import com.tvestergaard.places.pages.HomeFragment
import com.tvestergaard.places.transport.BackendCommunicator
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.toast
import pyxis.uzuki.live.richutilskt.utils.put
import java.lang.RuntimeException


class MainActivity : AppCompatActivity(), AnkoLogger {

    private var currentNavigationFragment = DEFAULT_FRAGMENT
    private var account: AuthenticatedUser? = null
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(createNavigationListener())

        currentNavigationFragment = if (savedInstanceState != null)
            savedInstanceState.getInt(CURRENT_NAVIGATION_BUNDLE_KEY, DEFAULT_FRAGMENT)
        else
            DEFAULT_FRAGMENT

        val lastSignIn = GoogleSignIn.getLastSignedInAccount(this)
        if (lastSignIn != null)
            account = BackendCommunicator().authenticateWithBackend(lastSignIn.idToken)

        if (account != null) {
            show(currentNavigationFragment)
        } else
            promptAuthentication()

        if (savedInstanceState != null) {
            var fragContent = supportFragmentManager.getFragment(savedInstanceState, "currentFragment")
            info("-----")
            info(fragContent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.put(CURRENT_NAVIGATION_BUNDLE_KEY, currentNavigationFragment)

        supportFragmentManager.putFragment(outState, "currentFragment", currentFragment!!)

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
                    account = data.extras["account"] as AuthenticatedUser
                    if (account != null) {
                        toast("Welcome back ${account!!.name}")
                        show(DEFAULT_FRAGMENT)
                    } else {
                        toast("You could not be authenticated.")
                    }
                }
            }
        }
    }

    private fun getFragmentFromId(id: Int) = when (id) {
        1 -> HomeFragment.newInstance()
        2 -> CameraFragment.newInstance()
        3 -> SearchFragment.newInstance()
        4 -> ContributeFragment.newInstance()
        else -> throw RuntimeException("unhandled fragment type $id.")
    }

    private fun createNavigationListener(): BottomNavigationView.OnNavigationItemSelectedListener {
        return BottomNavigationView.OnNavigationItemSelectedListener { item ->
            val navigationId = getPositionalItem(item.itemId)
            show(navigationId)
            true
        }
    }

    private fun getPositionalItem(item: Int): Int {
        return when (item) {
            R.id.navigation_home -> 1
            R.id.navigation_camera -> 2
            R.id.navigation_search -> 3
            R.id.navigation_contribute -> 4
            else -> DEFAULT_FRAGMENT
        }
    }

    private fun show(id: Int) {

        val fragment = getFragmentFromId(id)
        val transaction = supportFragmentManager.beginTransaction()

        if (currentNavigationFragment < id)
            transaction.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_left
            )
        if (currentNavigationFragment > id)
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )


        currentFragment = fragment

        transaction
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.fragmentContainer, fragment)
            .commitAllowingStateLoss()

        currentNavigationFragment = id
    }

    companion object {
        const val DEFAULT_FRAGMENT = 1
        const val CURRENT_NAVIGATION_BUNDLE_KEY = "currentNavigationFragment"
    }
}
