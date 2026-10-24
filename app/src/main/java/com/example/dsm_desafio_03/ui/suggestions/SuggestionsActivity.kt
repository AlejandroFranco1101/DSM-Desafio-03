package com.example.dsm_desafio_03.ui.suggestions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dsm_desafio_03.R
import com.example.dsm_desafio_03.data.ReadingRepository
import com.example.dsm_desafio_03.databinding.ActivitySuggestionsBinding
import com.example.dsm_desafio_03.model.Book
import com.example.dsm_desafio_03.model.BookSuggestion
import com.google.android.material.snackbar.Snackbar

class SuggestionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuggestionsBinding
    private lateinit var repository: ReadingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuggestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ReadingRepository()

        binding.btnBack.setOnClickListener {
            finish()
        }

        val adapter = SuggestionsAdapter(BookSuggestion.CURATED_SUGGESTIONS) { suggestion ->
            addSuggestionToUserLibrary(suggestion)
        }

        binding.rvSuggestions.apply {
            layoutManager = LinearLayoutManager(this@SuggestionsActivity)
            this.adapter = adapter
        }
    }

    private fun addSuggestionToUserLibrary(suggestion: BookSuggestion) {
        val newBook = Book(
            title = suggestion.title,
            author = suggestion.author,
            genre = suggestion.genre,
            totalPages = suggestion.totalPages,
            readPages = 0,
            status = Book.STATUS_TO_READ,
            rating = 0,
            createdAt = System.currentTimeMillis()
        )

        repository.saveBook(newBook) { success, errorMsg ->
            val msg = if (success) {
                "\"${suggestion.title}\" se agregó a tu Biblioteca como \"Por leer\""
            } else {
                errorMsg ?: "No se pudo agregar el libro"
            }
            val snackbar = Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            snackbar.setBackgroundTint(
                getColor(if (success) R.color.deep_teal else R.color.status_error)
            )
            snackbar.setTextColor(getColor(R.color.white))
            snackbar.show()
        }
    }
}
