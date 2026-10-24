package com.example.dsm_desafio_03.model

import com.google.firebase.database.IgnoreExtraProperties
import kotlin.math.roundToInt

@IgnoreExtraProperties
data class Book(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val genre: String = GENRE_NOVEL,
    val totalPages: Int = 0,
    val readPages: Int = 0,
    val status: String = STATUS_TO_READ,
    val rating: Int = 0,
    val finishedAt: Long? = null,
    val finishedDateFormatted: String = "",
    val finishedYear: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun progressPercentage(): Int {
        if (totalPages <= 0) return 0
        if (status == STATUS_FINISHED) return 100
        return ((readPages.toDouble() / totalPages.toDouble()) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
    }

    companion object {
        const val GENRE_NOVEL = "Novela"
        const val GENRE_SCIFI = "Ciencia ficción"
        const val GENRE_HISTORY = "Historia"
        const val GENRE_PERSONAL_DEV = "Desarrollo personal"

        val MANDATORY_GENRES = listOf(
            GENRE_NOVEL,
            GENRE_SCIFI,
            GENRE_HISTORY,
            GENRE_PERSONAL_DEV
        )

        const val STATUS_TO_READ = "Por leer"
        const val STATUS_READING = "Leyendo"
        const val STATUS_FINISHED = "Terminado"

        val BOOK_STATUSES = listOf(
            STATUS_TO_READ,
            STATUS_READING,
            STATUS_FINISHED
        )
    }
}
