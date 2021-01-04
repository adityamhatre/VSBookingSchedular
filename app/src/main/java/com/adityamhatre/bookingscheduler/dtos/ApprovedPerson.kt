package com.adityamhatre.bookingscheduler.dtos

import java.io.Serializable

enum class ApprovedPerson(val readableName: String) : Serializable {
    RAJESH_MHATRE("Rajesh Mhatre"),
    MADHURA_MHATRE("Madhura Mhatre"),
    NILIMA_RAUT("Nilima Raut"),
    ADITYA_MHATRE("Aditya Mhatre")
}
