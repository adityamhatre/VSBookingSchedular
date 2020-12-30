package com.adityamhatre.bookingscheduler.enums

enum class Accommodation(val readableName: String) {
    BUNGALOW_5_1("Bungalow (5+1)"),
    BUNGALOW_3_1("Bungalow (3+1)"),
    SPECIAL_ROOM_1("Special Room 1"),
    SPECIAL_ROOM_2("Special Room 2"),
    ROOM_1_VIHAR("Room 1 (Vihar)"),
    ROOM_2_VISHAVA("Room 2 (Vishava)"),
    ROOM_3_VISHRAM("Room 3 (Vishram)"),
    ROOM_4_VISHRANT("Room 4 (Vishrant)"),
    NIVANT("Nivant"),
    DORMITORY_SOBAT("Dormitory (Sobat)"),
    DORMITORY_SANGAT("Dormitory (Sangat)");

    companion object {
        fun all() = values().map { it }
    }

    fun asList() = listOf(this)

}
