package com.adityamhatre.bookingscheduler.enums

import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.R

enum class Accommodation(val readableName: String, val calendarId: String) {
    BUNGALOW_3_1(
        "Bungalow (3 + 1)",
        Application.getApplicationContext().getString(R.string.bungalow_3_1_id)
    ),
    SPECIAL_ROOM_1(
        "Special Room 1",
        Application.getApplicationContext().getString(R.string.special_room_1_id)
    ),
    SPECIAL_ROOM_2(
        "Special Room 2",
        Application.getApplicationContext().getString(R.string.special_room_2_id)
    ),
    ROOM_1_VIHAR(
        "Room 1 (Vihar)",
        Application.getApplicationContext().getString(R.string.room_1_id)
    ),
    ROOM_2_VISHAVA(
        "Room 2 (Vishava)",
        Application.getApplicationContext().getString(R.string.room_2_id)
    ),
    ROOM_3_VISHRAM(
        "Room 3 (Vishram)",
        Application.getApplicationContext().getString(R.string.room_3_id)
    ),
    ROOM_4_VISHRANT(
        "Room 4 (Vishrant)",
        Application.getApplicationContext().getString(R.string.room_4_id)
    ),
    NIVANT("Nivant", Application.getApplicationContext().getString(R.string.nivant_id)),
    DORMITORY_SOBAT(
        "Dormitory (Sobat)",
        Application.getApplicationContext().getString(R.string.dormitory_1)
    ),
    DORMITORY_SANGAT(
        "Dormitory (Sangat)",
        Application.getApplicationContext().getString(R.string.dormitory_2)
    ),
    BUNGALOW_5_1(
        "Bungalow (5 + 1)",
        Application.getApplicationContext().getString(R.string.bungalow_3_1_id)
    );

    companion object {
        fun all() = values().filter { it != BUNGALOW_5_1 }.map { it }
        fun from(key: String) = all().first { key.startsWith(it.calendarId) }
        fun byReadableName(text: String) = all().first { it.readableName == text }
        fun bungalow51List(accommodations: Set<Accommodation>): Set<Accommodation> {
            val returnSet = mutableSetOf<Accommodation>()
            returnSet.addAll(accommodations)

            if (accommodations.contains(BUNGALOW_3_1) && accommodations.contains(SPECIAL_ROOM_1) && accommodations.contains(
                    SPECIAL_ROOM_2
                )
            ) {
                returnSet.remove(BUNGALOW_3_1)
                returnSet.remove(SPECIAL_ROOM_1)
                returnSet.remove(SPECIAL_ROOM_2)
                returnSet.add(BUNGALOW_5_1)
            }
            return returnSet.toSet()
        }
    }

}
