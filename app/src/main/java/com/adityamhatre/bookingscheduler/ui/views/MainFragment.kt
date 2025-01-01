package com.adityamhatre.bookingscheduler.ui.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.BuildConfig
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.customViews.MonthView
import com.adityamhatre.bookingscheduler.ui.viewmodels.MainFragmentViewModel
import java.time.ZoneId
import java.util.*

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireActivity().packageManager.canRequestPackageInstalls()) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Please click \"Allow from this source\" on the next screen to allow auto updates")
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                        )
                    )
                    Toast.makeText(
                        requireContext(),
                        "Please click \"Allow from this source\" to enable auto updates",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            val alert = builder.create()
            alert.show()
            return
        }
        Application.getInstance().checkForUpdates()
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
        loadMonthlyBookingsCount(view)
    }

    // adds 1 year from current year
    private fun addExtraMonths(monthsList: LinearLayout) {
        val (lastMonth, lastYear) = (monthsList[monthsList.childCount - 1] as MonthView).getMonthYear()
        var newMonth = lastMonth
        var newYear = lastYear
        for (i in 0..11) {
            newMonth += 1
            if (newMonth > 12) {
                newMonth = 1
                newYear += 1
            }
            printMonths(newMonth, newYear)
        }
    }

    private fun printMonths(newMonth: Int, newYear: Int) {
        val xml = """
             <com.adityamhatre.bookingscheduler.customViews.MonthView
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_marginTop="16dp"
                month_view:month="${MonthView.monthName(newMonth)}"
                month_view:year="$newYear" />
        """.trimIndent()
        println(xml)
    }


    private fun loadMonthlyBookingsCount(view: View) {
        val timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                Application.getInstance().getRenderService()
                    .getBookingSummary { bookingSummaryJsonObject ->
                        timer.cancel()
                        bookingSummaryJsonObject.keys().forEach {
                            val monthYear = it
                            val month = it.substring(0, 2).toInt()
                            val year = it.substring(2).toInt()
                            val count = bookingSummaryJsonObject[it].toString().toInt()

                            val index = 12 * (year - 2021) + month - 1
                            val monthView =
                                view.findViewById<LinearLayout>(R.id.yearList)[index] as MonthView

                            monthView.setBookingsCount(count)
                        }
                    }
            }
        }

        timer.scheduleAtFixedRate(timerTask, 0, 3000)
    }

    private fun setupView(view: View) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));
        val year = calendar.get(Calendar.YEAR);
        val month = calendar.get(Calendar.MONTH)
        val scrollToIndex = 12 * (year - 2021) + month

        viewModel.wasViewLoaded().observe(viewLifecycleOwner, {
            if (!it) {
                viewModel.viewDidLoad()
                view.findViewById<ScrollView>(R.id.scrollLayout).postDelayed({
                    view.findViewById<ScrollView>(R.id.scrollLayout)
                        .smoothScrollTo(
                            0,
                            view.findViewById<LinearLayout>(R.id.yearList)[scrollToIndex].top
                        )
                }, 500)
            }
        })

        addExtraMonths(view.findViewById<LinearLayout>(R.id.yearList))

        view.findViewById<LinearLayout>(R.id.yearList).children.forEachIndexed { i, it ->
            val monthView = it as MonthView
            monthView.dateClickedListener =
                MonthView.DateClickedListener { date, month, year ->
                    viewBookings(
                        date,
                        month,
                        year
                    )
                }

            monthView.monthClickedListener =
                MonthView.MonthClickedListener { month, year ->
                    viewBookings(
                        month = month,
                        year = year
                    )
                }
            monthView.setOnClickListener { }
            monthView.addBookingInfo()
        }
    }

    private fun viewBookings(date: Int = -1, month: Int, year: Int) {
        if (activity == null) return
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.container, ListOfBookingsFragment.newInstance(date, month, year))
            .addToBackStack(null)
            .commit()
    }

}