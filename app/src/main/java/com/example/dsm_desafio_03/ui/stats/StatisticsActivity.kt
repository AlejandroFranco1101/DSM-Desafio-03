package com.example.dsm_desafio_03.ui.stats

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.dsm_desafio_03.data.ReadingRepository
import com.example.dsm_desafio_03.databinding.ActivityStatisticsBinding
import com.example.dsm_desafio_03.model.Book
import com.google.firebase.database.ValueEventListener
import kotlin.math.roundToInt

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var repository: ReadingRepository
    private var booksListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ReadingRepository()

        val currentYear = repository.getCurrentYear()
        binding.tvStatsTitle.text = "Estadísticas $currentYear"

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        booksListener = repository.observeBooks { books ->
            computeAndRenderStatistics(books)
        }
    }

    override fun onStop() {
        super.onStop()
        repository.removeBooksListener(booksListener)
    }

    private fun computeAndRenderStatistics(allBooks: List<Book>) {
        val currentYear = repository.getCurrentYear()

        // 1. Libros terminados en el año actual
        val finishedThisYear = allBooks.filter { book ->
            book.status == Book.STATUS_FINISHED &&
                (book.finishedYear == currentYear || book.finishedYear == 0)
        }

        val totalFinishedCount = finishedThisYear.size
        binding.tvTotalFinishedYearBadge.text =
            if (totalFinishedCount == 1) "1 terminado" else "$totalFinishedCount terminados"

        // Conteo por cada uno de los 4 géneros obligatorios
        val countNovela = finishedThisYear.count { it.genre == Book.GENRE_NOVEL }
        val countSciFi = finishedThisYear.count { it.genre == Book.GENRE_SCIFI }
        val countHistoria = finishedThisYear.count { it.genre == Book.GENRE_HISTORY }
        val countPersonalDev = finishedThisYear.count { it.genre == Book.GENRE_PERSONAL_DEV }

        binding.tvCountNovela.text = formatBookCount(countNovela)
        binding.tvCountSciFi.text = formatBookCount(countSciFi)
        binding.tvCountHistoria.text = formatBookCount(countHistoria)
        binding.tvCountPersonalDev.text = formatBookCount(countPersonalDev)

        binding.progressNovela.progress = calcPercent(countNovela, totalFinishedCount)
        binding.progressSciFi.progress = calcPercent(countSciFi, totalFinishedCount)
        binding.progressHistoria.progress = calcPercent(countHistoria, totalFinishedCount)
        binding.progressPersonalDev.progress = calcPercent(countPersonalDev, totalFinishedCount)

        // 2. Género más leído en el año
        val genreCounts = mapOf(
            Book.GENRE_NOVEL to countNovela,
            Book.GENRE_SCIFI to countSciFi,
            Book.GENRE_HISTORY to countHistoria,
            Book.GENRE_PERSONAL_DEV to countPersonalDev
        )

        val maxGenreCount = genreCounts.values.maxOrNull() ?: 0
        if (maxGenreCount <= 0) {
            binding.tvTopGenreValue.text = "Sin datos"
            binding.tvTopGenreSub.text = "Ningún libro terminado aún"
        } else {
            val topGenres = genreCounts.filterValues { it == maxGenreCount }.keys.toList()
            if (topGenres.size == 1) {
                binding.tvTopGenreValue.text = topGenres.first()
                binding.tvTopGenreSub.text = "${formatBookCount(maxGenreCount)} en $currentYear"
            } else {
                // Si hay empate en libros terminados por género, mostramos los empatados
                binding.tvTopGenreValue.text = topGenres.joinToString(" / ")
                binding.tvTopGenreSub.text = "Empate (${formatBookCount(maxGenreCount)} c/u)"
            }
        }

        // 3. Total de páginas leídas en el año
        val pagesFromFinished = finishedThisYear.sumOf {
            if (it.readPages > 0) it.readPages else it.totalPages
        }
        val pagesFromCurrentlyReading = allBooks
            .filter { it.status == Book.STATUS_READING }
            .sumOf { it.readPages.coerceAtLeast(0) }

        val totalPagesReadYear = pagesFromFinished + pagesFromCurrentlyReading
        binding.tvTotalPagesReadValue.text = totalPagesReadYear.toString()
        binding.tvTotalPagesReadSub.text = if (pagesFromCurrentlyReading > 0) {
            "$pagesFromFinished en terminados + $pagesFromCurrentlyReading leyendo"
        } else {
            "páginas leídas en $currentYear"
        }

        // 4. Libro mejor calificado del año (en caso de empate, el que se terminó primero)
        val bestBook = finishedThisYear
            .filter { it.rating in 1..5 }
            .sortedWith(
                compareByDescending<Book> { it.rating }
                    .thenBy { it.finishedAt ?: Long.MAX_VALUE }
                    .thenBy { it.createdAt }
            )
            .firstOrNull()

        if (bestBook == null) {
            binding.tvBestBookTitle.text = "Sin libros calificados aún"
            binding.tvBestBookAuthorAndGenre.text =
                "Marca un libro como Terminado y asígnale de 1 a 5 estrellas"
            binding.layoutBestBookDetails.visibility = View.GONE
        } else {
            val tiedCount = finishedThisYear.count { it.rating == bestBook.rating }

            binding.tvBestBookTitle.text = bestBook.title
            binding.tvBestBookAuthorAndGenre.text = "${bestBook.author} • ${bestBook.genre}"
            binding.layoutBestBookDetails.visibility = View.VISIBLE
            binding.ratingBarBestBook.rating = bestBook.rating.toFloat()
            binding.tvBestBookStarsText.text =
                "${bestBook.rating} / 5 ${if (bestBook.rating == 1) "estrella" else "estrellas"}"
            binding.tvBestBookPages.text = "${bestBook.totalPages} págs."

            val dateLabel = bestBook.finishedDateFormatted.ifBlank { "este año" }
            binding.tvBestBookFinishedInfo.text = if (tiedCount > 1) {
                "Terminado el $dateLabel (elegido por haberse terminado primero entre $tiedCount con ${bestBook.rating}★)"
            } else {
                "Terminado el $dateLabel"
            }
        }
    }

    private fun formatBookCount(count: Int): String {
        return if (count == 1) "1 libro" else "$count libros"
    }

    private fun calcPercent(part: Int, total: Int): Int {
        if (total <= 0) return 0
        return ((part.toDouble() / total.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
    }
}
