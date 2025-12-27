package pt.cravodeabril.movies.utils

import androidx.room.TypeConverter
import kotlin.time.Instant

class InstantConverter {
    @TypeConverter
    fun fromInstant(value: Instant?): String? = value?.toString()

    @TypeConverter
    fun toInstant(value: String?): Instant? = value?.let { Instant.parse(it) }
}
