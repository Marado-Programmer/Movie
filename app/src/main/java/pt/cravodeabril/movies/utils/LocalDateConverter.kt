package pt.cravodeabril.movies.utils

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

class LocalDateConverter {
    @TypeConverter
    fun fromInstant(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toInstant(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}
