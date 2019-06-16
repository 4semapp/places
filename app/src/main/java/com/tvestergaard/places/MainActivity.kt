package com.tvestergaard.places

import android.annotation.SuppressLint
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


class MainActivity : AppCompatActivity(), AnkoLogger {

    private var currentFragmentId = DEFAULT_FRAGMENT
    private var currentFragment: Fragment? = null
    private var fragmentBundles = HashMap<Int, Bundle?>()
    var account: AuthenticatedUser? = null
    lateinit var googleAuthClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleAuthClient = GoogleSignIn.getClient(
            this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_authentication_app_id))
                .requestEmail()
                .build()
        )
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(createNavigationListener())

        if (savedInstanceState != null) {
            account = savedInstanceState.getSerializable(ACCOUNT_BUNDLE_KEY) as AuthenticatedUser?
            fragmentBundles = savedInstanceState.getSerializable(FRAGMENT_STATE_BUNDLE_KEY) as HashMap<Int, Bundle?>
            currentFragmentId = savedInstanceState.getInt(CURRENT_NAVIGATION_BUNDLE_KEY, DEFAULT_FRAGMENT)
            navigation.selectedItemId = getNavigationItemFromId(currentFragmentId)
        }

        if (account == null) {
            switchFragment(AUTHENTICATION_FRAGMENT, createFragmentFromId(AUTHENTICATION_FRAGMENT))
            return
        }

        switchFragment(currentFragmentId, createFragmentFromId(currentFragmentId))
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {

        outState.putSerializable(ACCOUNT_BUNDLE_KEY, account)
        outState.putInt(CURRENT_NAVIGATION_BUNDLE_KEY, currentFragmentId)

        if (!isFinishing) {
            val fragmentBundle = Bundle()
            currentFragment?.onSaveInstanceState(fragmentBundle)
            fragmentBundles[currentFragmentId] = fragmentBundle
            outState.putSerializable(FRAGMENT_STATE_BUNDLE_KEY, fragmentBundles)
        }
    }

    fun onAuthenticationSuccess(user: AuthenticatedUser) {
        this.account = user
        BackendCommunicator.authenticatedUser = user
        toast(getString(R.string.welcome_back_name, user.name))
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

    private fun createFragmentFromId(id: Int): Fragment {

        val bundle = fragmentBundles.getOrDefault(id, Bundle())

        return when (id) {
            0 -> AuthenticationFragment.newInstance(bundle)
            1 -> HomeFragment.newInstance(bundle)
            2 -> CameraFragment.newInstance(bundle)
            3 -> SearchFragment.newInstance(bundle)
            4 -> ContributeFragment.newInstance(bundle)
            5 -> ProfileFragment.newInstance(bundle)
            else -> HomeFragment.newInstance(bundle)
        }
    }

    private fun createNavigationListener(): BottomNavigationView.OnNavigationItemSelectedListener {
        return BottomNavigationView.OnNavigationItemSelectedListener { item ->
            val navigationId = getNavigationIdFromItem(item.itemId)
            switchFragment(navigationId, createFragmentFromId(navigationId))
            true
        }
    }

    private fun getNavigationIdFromItem(item: Int): Int {
        return when (item) {
            R.id.navigation_home -> 1
            R.id.navigation_camera -> 2
            R.id.navigation_search -> 3
            R.id.navigation_contribute -> 4
            R.id.navigation_profile -> 5
            else -> DEFAULT_FRAGMENT
        }
    }

    private fun getNavigationItemFromId(item: Int): Int {
        return when (item) {
            1 -> R.id.navigation_home
            2 -> R.id.navigation_camera
            3 -> R.id.navigation_search
            4 -> R.id.navigation_contribute
            5 -> R.id.navigation_profile
            else -> R.id.navigation_home // default is home
        }
    }

    private fun switchFragment(id: Int, fragment: Fragment) {

        // hide the navigation bar, when we are on authentication page
        navigation.visibility = if (id == AUTHENTICATION_FRAGMENT) View.GONE else View.VISIBLE

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

        transaction
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.fragmentContainer, fragment)
            .commitAllowingStateLoss()

        // Save instance state when navigating
        if (currentFragment != null) {
            val bundle = Bundle()
            currentFragment!!.onSaveInstanceState(bundle)
            fragmentBundles[currentFragmentId] = bundle
        }

        currentFragment = fragment
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
        const val ACCOUNT_BUNDLE_KEY = "account"
        const val FRAGMENT_STATE_BUNDLE_KEY = "fragmentBundles"
    }
}
