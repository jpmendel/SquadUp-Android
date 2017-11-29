package com.squadup.squadup.activity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import com.squadup.squadup.R

/**
 * Created by StephenHaberle on 11/27/17.
 */

class MainActivity : BaseActivity() {
    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var container: ViewPager ?= null
    private var tabs: TabLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
    }

    override fun initializeViews() {
        super.initializeViews()

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container = findViewById(R.id.container)
        tabs = findViewById(R.id.tabs)

        // Set up the ViewPager with the sections adapter.
        container!!.adapter = mSectionsPagerAdapter

        container!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs!!.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        //create those red varibales as members and find them in R here

    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            when (position) {
                0 -> return GroupsFragment.newInstance()
                1 -> return FriendsFragment.newInstance()
            }
            throw IllegalStateException("getItem error -- invalid position")
        }

        override fun getCount(): Int {
            return 2
        }
    }

}

