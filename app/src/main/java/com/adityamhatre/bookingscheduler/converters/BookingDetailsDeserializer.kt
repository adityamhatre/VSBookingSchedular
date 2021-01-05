package com.adityamhatre.bookingscheduler.converters

import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant
import java.util.*

class BookingDetailsDeserializer : JsonDeserializer<BookingDetails> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): BookingDetails {
        val deserialized = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantTypeConverter())
            .create().fromJson(json, BookingDetails::class.java)

        val fields = deserialized.javaClass.declaredFields

        val mapOfNewFieldsToIsValueSet =
            mutableMapOf("phoneNumber" to false, "bookingIdOnGoogle" to false)
        mapOfNewFieldsToIsValueSet.keys.forEach { newField ->
            val field = fields.first {
                it.isAccessible = true
                it.name == newField
            } ?: return@forEach
            mapOfNewFieldsToIsValueSet[newField] = field.get(deserialized) != null
        }

        return BookingDetails(
            accommodations = deserialized.accommodations,
            checkIn = deserialized.checkIn,
            checkOut = deserialized.checkOut,
            bookingMainPerson = deserialized.bookingMainPerson,
            totalNumberOfPeople = deserialized.totalNumberOfPeople,
            bookedBy = deserialized.bookedBy,
            advancePaymentInfo = deserialized.advancePaymentInfo,
            phoneNumber = if (mapOfNewFieldsToIsValueSet["phoneNumber"] == true) deserialized.phoneNumber else "",
            bookingIdOnGoogle = if (mapOfNewFieldsToIsValueSet["bookingIdOnGoogle"] == true) deserialized.bookingIdOnGoogle else UUID.randomUUID()
                .toString()
        )
    }

}
