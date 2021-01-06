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
                "mhatremr68@gmail.com", "madhuramhatre1968@gmail.com" -> MADHURA_MHATRE
                "nilimaraut8007@gmail.com", "nilimaraut1973@gmail.com" -> NILIMA_RAUT
                else -> ADITYA_MHATRE
            }
        }

        fun isAuthorized(email: String): Boolean {
            return when (email) {
                "aditya.r.mhatre@gmail.com", "my.shazam.songs@gmail.com",
                "rajeshmhatre1965@gmail.com",
                "mhatremr68@gmail.com", "madhuramhatre1968@gmail.com",
                "nilimaraut8007@gmail.com", "nilimaraut1973@gmail.com" -> true
                else -> false
            }
        }
    }
}
