package com.adityamhatre.bookingscheduler.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.customViews.MonthView
import java.util.*

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView(view)
    }

    private fun setupView(view: View) {
        view.findViewById<ScrollView>(R.id.scrollLayout).postDelayed({
            view.findViewById<ScrollView>(R.id.scrollLayout)
                .smoothScrollTo(
                    0,
                    view.findViewById<LinearLayout>(R.id.yearList)[Calendar.getInstance()
                        .get(Calendar.MONTH)].top
                )
        }, 500)


        view.findViewById<LinearLayout>(R.id.yearList).children.forEachIndexed { i, it ->
            val monthView = it as MonthView
            monthView.dateClickedListener =
                MonthView.DateClickedListener { date, month -> viewBookings(date, month) }
            monthView.setOnClickListener { viewBookings(month = i + 1) }
        }
    }

    private fun viewBookings(date: Int = -1, month: Int) {
        if (activity == null) return
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.container, ListOfBookingsFragment.newInstance(date, month))
            .addToBackStack(null)
            .commit()
    }

}