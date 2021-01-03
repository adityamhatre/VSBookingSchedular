package com.adityamhatre.bookingscheduler.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainFragmentViewModel : ViewModel() {
    private val viewWasLoaded: MutableLiveData<Boolean> = MutableLiveData(false)

    fun wasViewLoaded(): LiveData<Boolean> = viewWasLoaded
    fun viewDidLoad() {
        viewWasLoaded.value = true
    }
}