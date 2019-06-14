package com.tvestergaard.places

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.tvestergaard.places.fragments.*
import com.tvestergaard.places.transport.BackendCommunicator
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.toast
import java.lang.RuntimeException


class MainActivity : AppCompatActivity(), AnkoLogger {

    private var currentFragmentId = DEFAULT_FRAGMENT
    private var currentFragment: Fragment? = null
    var account: AuthenticatedUser? = null
    lateinit var googleAuthClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleAuthClient = GoogleSignIn.getClient(
            this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("361668683148-casfe6p1qcgpf8s5aa2cg2tr6qvstdg0.apps.googleusercontent.com")
                .requestEmail()
                .build()
        )
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(createNavigationListener())

        if (savedInstanceState != null) {
            account = savedInstanceState.getSerializable("account") as AuthenticatedUser?
        }

        if (account == null) {
            switchFragment(AUTHENTICATION_FRAGMENT, createFragmentFromId(AUTHENTICATION_FRAGMENT))
            return
        }

        val continueKey = continueFragment(savedInstanceState)
        switchFragment(continueKey, createFragmentFromId(continueKey))
    }

    private fun continueFragment(savedInstanceState: Bundle?): Int {
        /*if (savedInstanceState != null) {
            val key = savedInstanceState.getInt(CURRENT_NAVIGATION_BUNDLE_KEY, DEFAULT_FRAGMENT)
            val content = supportFragmentManager.getFragment(savedInstanceState, "fragment_$key");
            switchFragment(key, content ?: createFragmentFromId(key))
            return true
        }*/

        if (savedInstanceState == null) {
            return DEFAULT_FRAGMENT
        }

        return savedInstanceState.getInt(CURRENT_NAVIGATION_BUNDLE_KEY, DEFAULT_FRAGMENT)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putSerializable(CURRENT_NAVIGATION_BUNDLE_KEY, currentFragmentId)
        outState.putSerializable("account", account)
        supportFragmentManager.putFragment(outState, "fragment_$currentFragmentId", currentFragment);
    }

    fun onAuthenticationSuccess(user: AuthenticatedUser) {
        this.account = user
        BackendCommunicator.authenticatedUser = user
        toast("Welcome back ${user.name}")
        switchFragment(DEFAULT_FRAGMENT, createFragmentFromId(DEFAULT_FRAGMENT))
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
    }

    private fun createFragmentFromId(id: Int) = when (id) {
        0 -> AuthenticationFragment.newInstance()
        1 -> HomeFragment.newInstance()
        2 -> CameraFragment.newInstance()
        3 -> SearchFragment.newInstance()
        4 -> ContributeFragment.newInstance()
        5 -> ProfileFragment.newInstance()
        else -> throw RuntimeException("unhandled fragment type $id.")
    }

    private fun createNavigationListener(): BottomNavigationView.OnNavigationItemSelectedListener {
        return BottomNavigationView.OnNavigationItemSelectedListener { item ->
            val navigationId = getPositionalItem(item.itemId)
            switchFragment(navigationId, createFragmentFromId(navigationId))
            true
        }
    }

    private fun getPositionalItem(item: Int): Int {
        return when (item) {
            R.id.navigation_home -> 1
            R.id.navigation_camera -> 2
            R.id.navigation_search -> 3
            R.id.navigation_contribute -> 4
            R.id.navigation_profile -> 5
            else -> DEFAULT_FRAGMENT
        }
    }

    private fun switchFragment(id: Int, fragment: Fragment) {

        if (id == AUTHENTICATION_FRAGMENT)
            navigation.visibility = View.GONE
        else
            navigation.visibility = View.VISIBLE
        val transaction = supportFragmentManager.beginTransaction()

        if (currentFragmentId < id)
            transaction.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_left
            )
        if (currentFragmentId > id)
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_right
            )


        currentFragment = fragment

        transaction
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.fragmentContainer, fragment)
            .commitAllowingStateLoss()

        currentFragmentId = id
    }

    fun signOut() {
        googleAuthClient.signOut()
        finish()
    }

    companion object {
        const val AUTHENTICATION_FRAGMENT = 0
        const val DEFAULT_FRAGMENT = 1
        const val CURRENT_NAVIGATION_BUNDLE_KEY = "currentFragmentId"
    }
}
