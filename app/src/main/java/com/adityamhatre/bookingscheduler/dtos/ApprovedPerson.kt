package com.adityamhatre.bookingscheduler.dtos

import java.io.Serializable

enum class ApprovedPerson(val readableName: String) : Serializable {
    RAJESH_MHATRE("Rajesh Mhatre"),
    MADHURA_MHATRE("Madhura Mhatre"),
    NILIMA_RAUT("Nilima Raut"),
    ADITYA_MHATRE("Aditya Mhatre");

    companion object {
        fun findByEmail(email: String): ApprovedPerson {
            return when (email) {
                "aditya.r.mhatre@gmail.com", "my.shazam.songs@gmail.com" -> ADITYA_MHATRE
                "rajeshmhatre1965@gmail.com" -> RAJESH_MHATRE
                else -> ADITYA_MHATRE
            }
        }
    }
}
