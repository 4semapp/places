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
import com.tvestergaard.places.fragments.AuthenticationFragment.Companion.authenticationRequestCode
import com.tvestergaard.places.transport.BackendCommunicator
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.toast
import java.lang.RuntimeException


class MainActivity : AppCompatActivity(), AnkoLogger {

    private var currentNavigationFragment = DEFAULT_FRAGMENT
    private var currentFragment: Fragment? = null
    public var account: AuthenticatedUser? = null
    public lateinit var googleAuthClient: GoogleSignInClient

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

        currentNavigationFragment = getStartingFragment(savedInstanceState)
        show(currentNavigationFragment)
    }

    private fun getStartingFragment(savedInstanceState: Bundle?): Int {

        if (account == null)
            return AUTHENTICATION_FRAGMENT

        if (savedInstanceState != null)
            return savedInstanceState.getInt(CURRENT_NAVIGATION_BUNDLE_KEY, DEFAULT_FRAGMENT)

        return DEFAULT_FRAGMENT
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putSerializable(CURRENT_NAVIGATION_BUNDLE_KEY, currentNavigationFragment)
        outState.putSerializable("account", account)
        if (currentFragment != null) {
            supportFragmentManager.putFragment(outState, "currentFragment", currentFragment!!)
        }
    }

    fun onAuthenticationSuccess(user: AuthenticatedUser) {
        this.account = user
        BackendCommunicator.authenticatedUser = user
        toast("Welcome back ${user.name}")
        show(DEFAULT_FRAGMENT)
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

    private fun getFragmentFromId(id: Int) = when (id) {
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
            R.id.navigation_profile -> 5
            else -> DEFAULT_FRAGMENT
        }
    }

    private fun show(id: Int) {

        if (id == AUTHENTICATION_FRAGMENT)
            navigation.visibility = View.GONE
        else
            navigation.visibility = View.VISIBLE

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

    fun signOut() {
        googleAuthClient.signOut()
        finish()
    }

    companion object {
        const val AUTHENTICATION_FRAGMENT = 0
        const val DEFAULT_FRAGMENT = 1
        const val CURRENT_NAVIGATION_BUNDLE_KEY = "currentNavigationFragment"
    }
}
