package com.example.dsm_desafio_03.ui.book

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.dsm_desafio_03.R
import com.example.dsm_desafio_03.data.ReadingRepository
import com.example.dsm_desafio_03.databinding.ActivityBookFormBinding
import com.example.dsm_desafio_03.model.Book
import com.example.dsm_desafio_03.utils.ValidationUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class BookFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookFormBinding
    private lateinit var repository: ReadingRepository

    private var editingBookId: String = ""
    private var existingCreatedAt: Long = 0L
    private var existingFinishedAt: Long? = null
    private var existingFinishedDateFormatted: String = ""
    private var existingFinishedYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ReadingRepository()

        setupGenreDropdown()
        loadIntentDataIfEditing()
        setupListeners()
        updateStatusDependentUI()
        updateProgressPreview()
    }

    private fun setupGenreDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            Book.MANDATORY_GENRES
        )
        binding.actvGenre.setAdapter(adapter)
        if (binding.actvGenre.text.isNullOrBlank()) {
            binding.actvGenre.setText(Book.GENRE_NOVEL, false)
        }
    }

    private fun loadIntentDataIfEditing() {
        editingBookId = intent.getStringExtra(EXTRA_BOOK_ID).orEmpty()
        if (editingBookId.isBlank()) {
            binding.tvHeaderBadge.text = "NUEVO REGISTRO"
            binding.tvScreenTitle.text = "Registrar Libro"
            binding.btnSaveBook.text = "Guardar Libro"
            binding.btnDeleteBook.visibility = View.GONE
            return
        }

        // Modo Edición
        binding.tvHeaderBadge.text = "EDITAR LIBRO"
        binding.tvScreenTitle.text = "Editar Libro"
        binding.btnSaveBook.text = "Actualizar Cambios"
        binding.btnDeleteBook.visibility = View.VISIBLE

        val title = intent.getStringExtra(EXTRA_BOOK_TITLE).orEmpty()
        val author = intent.getStringExtra(EXTRA_BOOK_AUTHOR).orEmpty()
        val genre = intent.getStringExtra(EXTRA_BOOK_GENRE) ?: Book.GENRE_NOVEL
        val totalPages = intent.getIntExtra(EXTRA_BOOK_TOTAL_PAGES, 0)
        val readPages = intent.getIntExtra(EXTRA_BOOK_READ_PAGES, 0)
        val status = intent.getStringExtra(EXTRA_BOOK_STATUS) ?: Book.STATUS_TO_READ
        val rating = intent.getIntExtra(EXTRA_BOOK_RATING, 0)

        existingCreatedAt = intent.getLongExtra(EXTRA_BOOK_CREATED_AT, System.currentTimeMillis())
        val finishedMillis = intent.getLongExtra(EXTRA_BOOK_FINISHED_AT, -1L)
        existingFinishedAt = if (finishedMillis > 0L) finishedMillis else null
        existingFinishedDateFormatted = intent.getStringExtra(EXTRA_BOOK_FINISHED_DATE).orEmpty()
        existingFinishedYear = intent.getIntExtra(EXTRA_BOOK_FINISHED_YEAR, 0)

        binding.etTitle.setText(title)
        binding.etAuthor.setText(author)
        binding.actvGenre.setText(genre, false)
        if (totalPages > 0) binding.etTotalPages.setText(totalPages.toString())
        binding.etReadPages.setText(readPages.toString())

        when (status) {
            Book.STATUS_READING -> binding.chipReading.isChecked = true
            Book.STATUS_FINISHED -> binding.chipFinished.isChecked = true
            else -> binding.chipToRead.isChecked = true
        }

        if (status == Book.STATUS_FINISHED && rating in 1..5) {
            binding.ratingBarBook.rating = rating.toFloat()
        } else {
            binding.ratingBarBook.rating = 0f
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.etTitle.doAfterTextChanged {
            binding.tilTitle.error = null
        }

        binding.etAuthor.doAfterTextChanged {
            binding.tilAuthor.error = null
        }

        binding.actvGenre.doAfterTextChanged {
            binding.tilGenre.error = null
        }

        binding.etTotalPages.doAfterTextChanged {
            binding.tilTotalPages.error = null
            binding.tilReadPages.error = null
            updateProgressPreview()
        }

        binding.etReadPages.doAfterTextChanged {
            binding.tilReadPages.error = null
            updateProgressPreview()
        }

        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, _ ->
            binding.tvRatingError.visibility = View.GONE
            // Si marca como Terminado y totalPages es válido, sincronizar páginas leídas si estaba en 0
            if (getSelectedStatus() == Book.STATUS_FINISHED) {
                val total = binding.etTotalPages.text?.toString()?.trim()?.toIntOrNull()
                val read = binding.etReadPages.text?.toString()?.trim()?.toIntOrNull() ?: 0
                if (total != null && total > 0 && read == 0) {
                    binding.etReadPages.setText(total.toString())
                }
            } else if (getSelectedStatus() == Book.STATUS_TO_READ) {
                val read = binding.etReadPages.text?.toString()?.trim()?.toIntOrNull() ?: 0
                if (read > 0 && editingBookId.isBlank()) {
                    binding.etReadPages.setText("0")
                }
            }
            updateStatusDependentUI()
            updateProgressPreview()
        }

        binding.ratingBarBook.setOnRatingBarChangeListener { _, rating, _ ->
            binding.tvRatingError.visibility = View.GONE
            val stars = rating.roundToInt()
            if (getSelectedStatus() == Book.STATUS_FINISHED) {
                binding.tvRatingValueLabel.text = if (stars > 0) {
                    "Calificación seleccionada: $stars ${if (stars == 1) "estrella" else "estrellas"}"
                } else {
                    "Toca las estrellas para calificar (obligatorio)"
                }
            }
        }

        binding.btnSaveBook.setOnClickListener {
            attemptSaveBook()
        }

        binding.btnDeleteBook.setOnClickListener {
            confirmDeleteBook()
        }
    }

    private fun getSelectedStatus(): String {
        return when (binding.chipGroupStatus.checkedChipId) {
            R.id.chipReading -> Book.STATUS_READING
            R.id.chipFinished -> Book.STATUS_FINISHED
            else -> Book.STATUS_TO_READ
        }
    }

    private fun updateStatusDependentUI() {
        val status = getSelectedStatus()
        if (status == Book.STATUS_FINISHED) {
            binding.ratingBarBook.setIsIndicator(false)
            binding.ratingBarBook.alpha = 1.0f
            binding.cardRatingSection.setStrokeColor(getColor(R.color.accent_turquoise))
            val stars = binding.ratingBarBook.rating.roundToInt()
            binding.tvRatingValueLabel.text = if (stars in 1..5) {
                "Calificación seleccionada: $stars ${if (stars == 1) "estrella" else "estrellas"}"
            } else {
                "Selecciona de 1 a 5 estrellas (obligatorio)"
            }
            binding.layoutFinishedDateInfo.visibility = View.VISIBLE
            val dateToShow = existingFinishedDateFormatted.ifBlank {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            }
            binding.tvFinishedDateText.text = "Fecha de finalización: $dateToShow"
        } else {
            // Bloquear y limpiar calificación si no está Terminado
            binding.ratingBarBook.rating = 0f
            binding.ratingBarBook.setIsIndicator(true)
            binding.ratingBarBook.alpha = 0.4f
            binding.cardRatingSection.setStrokeColor(getColor(R.color.divider_soft))
            binding.tvRatingValueLabel.text =
                "Sin calificación (solo se habilita en estado \"Terminado\")"
            binding.layoutFinishedDateInfo.visibility = View.GONE
        }
    }

    private fun updateProgressPreview() {
        val total = binding.etTotalPages.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val read = binding.etReadPages.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val status = getSelectedStatus()

        val percent = when {
            total <= 0 -> 0
            status == Book.STATUS_FINISHED -> 100
            else -> ((read.toDouble() / total.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
        }

        binding.tvPreviewPercent.text = "$percent%"
        binding.progressPreview.progress = percent
    }

    private fun attemptSaveBook() {
        val title = binding.etTitle.text?.toString().orEmpty()
        val author = binding.etAuthor.text?.toString().orEmpty()
        val genre = binding.actvGenre.text?.toString().orEmpty()
        val totalPagesStr = binding.etTotalPages.text?.toString().orEmpty()
        val readPagesStr = binding.etReadPages.text?.toString().orEmpty()
        val status = getSelectedStatus()
        val rating = if (status == Book.STATUS_FINISHED) {
            binding.ratingBarBook.rating.roundToInt()
        } else {
            0
        }

        val validation = ValidationUtils.validateBookForm(
            title = title,
            author = author,
            genre = genre,
            totalPagesStr = totalPagesStr,
            readPagesStr = readPagesStr,
            status = status,
            rating = rating
        )

        binding.tilTitle.error = validation.titleError
        binding.tilAuthor.error = validation.authorError
        binding.tilGenre.error = validation.genreError
        binding.tilTotalPages.error = validation.totalPagesError
        binding.tilReadPages.error = validation.readPagesError

        if (validation.ratingError != null) {
            binding.tvRatingError.text = validation.ratingError
            binding.tvRatingError.visibility = View.VISIBLE
        } else {
            binding.tvRatingError.visibility = View.GONE
        }

        if (!validation.isValid) {
            showMessage(
                validation.detailedMessage ?: "Verifica los campos inválidos antes de guardar",
                isError = true
            )
            return
        }

        val totalPages = totalPagesStr.trim().toInt()
        val readPages = readPagesStr.trim().toInt()

        // Calcular fecha de finalización si el estado es Terminado
        val now = System.currentTimeMillis()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val todayFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(now))

        val finishedAtMillis: Long?
        val finishedDateStr: String
        val finishedYearInt: Int

        if (status == Book.STATUS_FINISHED) {
            finishedAtMillis = existingFinishedAt ?: now
            finishedDateStr = existingFinishedDateFormatted.ifBlank { todayFormatted }
            finishedYearInt = if (existingFinishedYear > 0) existingFinishedYear else currentYear
        } else {
            finishedAtMillis = null
            finishedDateStr = ""
            finishedYearInt = 0
        }

        val book = Book(
            id = editingBookId,
            title = title.trim(),
            author = author.trim(),
            genre = genre.trim(),
            totalPages = totalPages,
            readPages = readPages,
            status = status,
            rating = rating,
            finishedAt = finishedAtMillis,
            finishedDateFormatted = finishedDateStr,
            finishedYear = finishedYearInt,
            createdAt = if (existingCreatedAt > 0L) existingCreatedAt else now
        )

        setLoading(true)
        repository.saveBook(book) { success, errorMsg ->
            setLoading(false)
            if (success) {
                finish()
            } else {
                showMessage(errorMsg ?: "Error al guardar el libro", isError = true)
            }
        }
    }

    private fun confirmDeleteBook() {
        if (editingBookId.isBlank()) return
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar libro")
            .setMessage("¿Estás seguro de que deseas eliminar este libro de tu cuenta?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                setLoading(true)
                repository.deleteBook(editingBookId) { success, errorMsg ->
                    setLoading(false)
                    if (success) {
                        finish()
                    } else {
                        showMessage(errorMsg ?: "No se pudo eliminar el libro", isError = true)
                    }
                }
            }
            .show()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBarSave.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSaveBook.isEnabled = !loading
        binding.btnDeleteBook.isEnabled = !loading
    }

    private fun showMessage(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(
            getColor(if (isError) R.color.status_error else R.color.deep_teal)
        )
        snackbar.setTextColor(getColor(R.color.white))
        snackbar.show()
    }

    companion object {
        private const val EXTRA_BOOK_ID = "extra_book_id"
        private const val EXTRA_BOOK_TITLE = "extra_book_title"
        private const val EXTRA_BOOK_AUTHOR = "extra_book_author"
        private const val EXTRA_BOOK_GENRE = "extra_book_genre"
        private const val EXTRA_BOOK_TOTAL_PAGES = "extra_book_total_pages"
        private const val EXTRA_BOOK_READ_PAGES = "extra_book_read_pages"
        private const val EXTRA_BOOK_STATUS = "extra_book_status"
        private const val EXTRA_BOOK_RATING = "extra_book_rating"
        private const val EXTRA_BOOK_FINISHED_AT = "extra_book_finished_at"
        private const val EXTRA_BOOK_FINISHED_DATE = "extra_book_finished_date"
        private const val EXTRA_BOOK_FINISHED_YEAR = "extra_book_finished_year"
        private const val EXTRA_BOOK_CREATED_AT = "extra_book_created_at"

        fun createIntent(context: Context, book: Book? = null): Intent {
            return Intent(context, BookFormActivity::class.java).apply {
                if (book != null) {
                    putExtra(EXTRA_BOOK_ID, book.id)
                    putExtra(EXTRA_BOOK_TITLE, book.title)
                    putExtra(EXTRA_BOOK_AUTHOR, book.author)
                    putExtra(EXTRA_BOOK_GENRE, book.genre)
                    putExtra(EXTRA_BOOK_TOTAL_PAGES, book.totalPages)
                    putExtra(EXTRA_BOOK_READ_PAGES, book.readPages)
                    putExtra(EXTRA_BOOK_STATUS, book.status)
                    putExtra(EXTRA_BOOK_RATING, book.rating)
                    putExtra(EXTRA_BOOK_FINISHED_AT, book.finishedAt ?: -1L)
                    putExtra(EXTRA_BOOK_FINISHED_DATE, book.finishedDateFormatted)
                    putExtra(EXTRA_BOOK_FINISHED_YEAR, book.finishedYear)
                    putExtra(EXTRA_BOOK_CREATED_AT, book.createdAt)
                }
            }
        }
    }
}
