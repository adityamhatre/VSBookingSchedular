package com.adityamhatre.bookingscheduler.dtos

class AdapterContainer<T>() {
    private var adapter: T? = null

    fun setAdapter(adapter: T) {
        this.adapter = adapter
    }

    fun getAdapter() = adapter
}
