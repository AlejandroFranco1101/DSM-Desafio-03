package com.example.dsm_desafio_03.ui.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dsm_desafio_03.R
import com.example.dsm_desafio_03.databinding.ItemLibraryBookBinding
import com.example.dsm_desafio_03.model.Book

class LibraryAdapter(
    private var books: List<Book> = emptyList(),
    private val onBookClick: (Book) -> Unit = {}
) : RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder>() {

    fun submitList(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val binding = ItemLibraryBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LibraryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    inner class LibraryViewHolder(
        private val binding: ItemLibraryBookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            val context = binding.root.context
            val percentage = book.progressPercentage()

            binding.tvBookTitle.text = book.title
            binding.tvBookAuthor.text = "Por ${book.author}"
            binding.tvGenreBadge.text = book.genre
            binding.tvStatusBadge.text = book.status
            binding.tvPagesDetail.text = "${book.readPages} de ${book.totalPages} páginas"
            binding.tvProgressPercentage.text = "$percentage% de avance"
            binding.progressBook.progress = percentage

            when (book.status) {
                Book.STATUS_FINISHED -> {
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_finished)
                    binding.tvStatusBadge.setTextColor(context.getColor(R.color.status_success))
                }
                Book.STATUS_READING -> {
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_reading)
                    binding.tvStatusBadge.setTextColor(context.getColor(R.color.deep_teal))
                }
                else -> {
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_toread)
                    binding.tvStatusBadge.setTextColor(context.getColor(R.color.dark_night))
                }
            }

            // Mostrar la calificación únicamente si el libro está terminado
            if (book.status == Book.STATUS_FINISHED && book.rating in 1..5) {
                binding.layoutFinishedRating.visibility = View.VISIBLE
                binding.ratingBarSmall.rating = book.rating.toFloat()
                binding.tvRatingText.text =
                    "${book.rating}/5 ${if (book.rating == 1) "estrella" else "estrellas"}"
                binding.tvFinishedDate.text = if (book.finishedDateFormatted.isNotBlank()) {
                    "Terminado: ${book.finishedDateFormatted}"
                } else {
                    "Terminado"
                }
            } else {
                binding.layoutFinishedRating.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onBookClick(book)
            }
        }
    }
}
