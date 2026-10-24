package com.example.dsm_desafio_03.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dsm_desafio_03.databinding.ItemCurrentlyReadingBinding
import com.example.dsm_desafio_03.model.Book

class CurrentlyReadingAdapter(
    private var books: List<Book> = emptyList(),
    private val onBookClick: (Book) -> Unit = {}
) : RecyclerView.Adapter<CurrentlyReadingAdapter.ReadingViewHolder>() {

    fun submitList(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingViewHolder {
        val binding = ItemCurrentlyReadingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReadingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReadingViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    inner class ReadingViewHolder(
        private val binding: ItemCurrentlyReadingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            val percentage = book.progressPercentage()
            binding.tvBookTitle.text = book.title
            binding.tvBookAuthor.text = book.author
            binding.tvBookGenre.text = book.genre
            binding.tvPagesProgress.text = "${book.readPages} de ${book.totalPages} páginas leídas"
            binding.tvProgressPercent.text = "$percentage%"
            binding.progressBook.progress = percentage

            binding.root.setOnClickListener {
                onBookClick(book)
            }
        }
    }
}
