package com.adityamhatre.bookingscheduler.googleapi

import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant

class InstantTypeConverter : JsonSerializer<Instant?>, JsonDeserializer<Instant?> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): Instant {
        return Instant.ofEpochMilli(json.asLong)
    }

    override fun serialize(
        src: Instant?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src?.toEpochMilli())
    }

}