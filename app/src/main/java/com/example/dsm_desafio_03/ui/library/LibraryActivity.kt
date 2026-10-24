package com.example.dsm_desafio_03.ui.library

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dsm_desafio_03.R
import com.example.dsm_desafio_03.data.ReadingRepository
import com.example.dsm_desafio_03.databinding.ActivityLibraryBinding
import com.example.dsm_desafio_03.model.Book
import com.example.dsm_desafio_03.ui.book.BookFormActivity
import com.google.firebase.database.ValueEventListener

class LibraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLibraryBinding
    private lateinit var repository: ReadingRepository
    private lateinit var libraryAdapter: LibraryAdapter

    private var booksListener: ValueEventListener? = null
    private var allBooks: List<Book> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ReadingRepository()

        setupRecyclerView()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        booksListener = repository.observeBooks { books ->
            allBooks = books
            applyFilterAndRender()
        }
    }

    override fun onStop() {
        super.onStop()
        repository.removeBooksListener(booksListener)
    }

    private fun setupRecyclerView() {
        libraryAdapter = LibraryAdapter { selectedBook ->
            startActivity(BookFormActivity.createIntent(this, selectedBook))
        }
        binding.rvLibraryBooks.apply {
            layoutManager = LinearLayoutManager(this@LibraryActivity)
            adapter = libraryAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAddBookLibrary.setOnClickListener {
            startActivity(BookFormActivity.createIntent(this, null))
        }

        binding.btnEmptyAddBook.setOnClickListener {
            startActivity(BookFormActivity.createIntent(this, null))
        }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, _ ->
            applyFilterAndRender()
        }
    }

    private fun applyFilterAndRender() {
        val totalCount = allBooks.size
        binding.tvLibrarySubtitle.text =
            if (totalCount == 1) "1 libro en tu colección" else "$totalCount libros en tu colección"

        val selectedChipId = binding.chipGroupFilter.checkedChipId
        val filteredBooks = when (selectedChipId) {
            R.id.chipFilterReading -> allBooks.filter { it.status == Book.STATUS_READING }
            R.id.chipFilterFinished -> allBooks.filter { it.status == Book.STATUS_FINISHED }
            R.id.chipFilterToRead -> allBooks.filter { it.status == Book.STATUS_TO_READ }
            else -> allBooks
        }

        if (filteredBooks.isEmpty()) {
            binding.rvLibraryBooks.visibility = View.GONE
            binding.cardEmptyLibrary.visibility = View.VISIBLE

            if (allBooks.isEmpty()) {
                binding.tvEmptyLibraryTitle.text = "Tu biblioteca está vacía"
                binding.tvEmptyLibraryMessage.text =
                    "Aún no hay libros que mostrar en tu cuenta. Agrega tu primer libro para comenzar tu registro."
                binding.btnEmptyAddBook.visibility = View.VISIBLE
            } else {
                binding.tvEmptyLibraryTitle.text = "Sin libros en este estado"
                binding.tvEmptyLibraryMessage.text =
                    "No tienes libros registrados bajo el filtro seleccionado actualmente."
                binding.btnEmptyAddBook.visibility = View.GONE
            }
        } else {
            binding.cardEmptyLibrary.visibility = View.GONE
            binding.rvLibraryBooks.visibility = View.VISIBLE
            libraryAdapter.submitList(filteredBooks)
        }
    }
}
