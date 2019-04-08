package com.tvestergaard.places

import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.lang.RuntimeException

class MainActivity :
    AppCompatActivity(),
    CameraFragment.Parent,
    ContributeFragment.Parent,
    HomeFragment.Parent,
    SearchFragment.Parent {

    private var currentNavigationFragment = -1
    private val navigationListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

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

    private fun show(id: Int, fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

//        if (currentNavigationFragment != -1 && currentNavigationFragment < id)
//            transaction.setCustomAnimations(R.anim.slide_out_right, R.anim.slide_in_right)
//        if (currentNavigationFragment != -1 && currentNavigationFragment > id)
//            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)

        transaction
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.fragmentContainer, fragment)
            .commitAllowingStateLoss();

        currentNavigationFragment = id
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(navigationListener)
        show(0, HomeFragment())
    }
}
