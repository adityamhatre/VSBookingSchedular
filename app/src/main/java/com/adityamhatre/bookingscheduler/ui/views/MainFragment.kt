package com.adityamhatre.bookingscheduler.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.customViews.MonthView
import com.adityamhatre.bookingscheduler.ui.viewmodels.MainFragmentViewModel
import java.util.*

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainFragmentViewModel by viewModels()


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
        viewModel.wasViewLoaded().observe(viewLifecycleOwner, {
            if (!it) {
                viewModel.viewDidLoad()
                view.findViewById<ScrollView>(R.id.scrollLayout).postDelayed({
                    view.findViewById<ScrollView>(R.id.scrollLayout)
                        .smoothScrollTo(
                            0,
                            view.findViewById<LinearLayout>(R.id.yearList)[Calendar.getInstance()
                                .get(Calendar.MONTH)].top
                        )
                }, 500)
            }
        })


        view.findViewById<LinearLayout>(R.id.yearList).children.forEachIndexed { i, it ->
            val monthView = it as MonthView
            monthView.dateClickedListener =
                MonthView.DateClickedListener { date, month -> viewBookings(date, month) }
            monthView.setOnClickListener { viewBookings(month = i + 1) }
        }
    }

    private fun viewBookings(date: Int = -1, month: Int, year: Int = 2021) {
        if (activity == null) return
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.container, ListOfBookingsFragment.newInstance(date, month, year))
            .addToBackStack(null)
            .commit()
    }

}