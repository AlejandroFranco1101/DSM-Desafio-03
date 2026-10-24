package com.example.dsm_desafio_03.model

import androidx.annotation.DrawableRes
import com.example.dsm_desafio_03.R

data class BookSuggestion(
    val title: String,
    val author: String,
    val genre: String,
    val totalPages: Int,
    val publishYear: String,
    val review: String,
    @DrawableRes val coverResId: Int
) {
    companion object {
        val CURATED_SUGGESTIONS = listOf(
            BookSuggestion(
                title = "1984",
                author = "George Orwell",
                genre = Book.GENRE_SCIFI,
                totalPages = 328,
                publishYear = "Clásico Distópico",
                review = "En un mundo vigilado por el Gran Hermano donde hasta el pensamiento está controlado, Winston Smith intenta recuperar su libertad interior y descubrir la verdad detrás del régimen totalitario.",
                coverResId = R.drawable.cover_book_1
            ),
            BookSuggestion(
                title = "El hombre en busca de sentido",
                author = "Viktor E. Frankl",
                genre = Book.GENRE_PERSONAL_DEV,
                totalPages = 184,
                publishYear = "Psicología y Vida",
                review = "Un conmovedor relato donde el psiquiatra Viktor Frankl comparte cómo encontrar un propósito vital nos permite superar incluso las circunstancias más extremas de la existencia humana.",
                coverResId = R.drawable.cover_book_2
            ),
            BookSuggestion(
                title = "El nombre del viento",
                author = "Patrick Rothfuss",
                genre = Book.GENRE_NOVEL,
                totalPages = 662,
                publishYear = "Fantasía Épica",
                review = "La historia de Kvothe, un joven músico, mendigo y estudiante de magia en la Universidad que llegó a convertirse en la leyenda más célebre y misteriosa de su mundo.",
                coverResId = R.drawable.cover_book_3
            ),
            BookSuggestion(
                title = "Los pilares de la Tierra",
                author = "Ken Follett",
                genre = Book.GENRE_HISTORY,
                totalPages = 1040,
                publishYear = "Edad Media • Siglo XII",
                review = "Una magistral epopeya histórica en la Inglaterra medieval que entrelaza ambición, intrigas reales y la construcción de la primera gran catedral gótica en Kingsbridge.",
                coverResId = R.drawable.cover_book_4
            ),
            BookSuggestion(
                title = "Enfócate (Deep Work)",
                author = "Cal Newport",
                genre = Book.GENRE_PERSONAL_DEV,
                totalPages = 296,
                publishYear = "Productividad",
                review = "Una guía práctica y científica para entrenar la concentración profunda en una era llena de distracciones digitales, permitiéndote dominar habilidades complejas en menos tiempo.",
                coverResId = R.drawable.cover_book_5
            )
        )
    }
}
