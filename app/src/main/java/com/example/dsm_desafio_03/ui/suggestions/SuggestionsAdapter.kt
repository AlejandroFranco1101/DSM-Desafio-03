package com.example.dsm_desafio_03.ui.suggestions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dsm_desafio_03.databinding.ItemBookSuggestionBinding
import com.example.dsm_desafio_03.model.BookSuggestion

class SuggestionsAdapter(
    private val suggestions: List<BookSuggestion>,
    private val onAddBookClicked: (BookSuggestion) -> Unit
) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemBookSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(suggestions[position])
    }

    override fun getItemCount(): Int = suggestions.size

    inner class SuggestionViewHolder(
        private val binding: ItemBookSuggestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookSuggestion) {
            binding.ivBookCover.setImageResource(item.coverResId)
            binding.tvSuggestionGenre.text = item.genre
            binding.tvSuggestionTag.text = item.publishYear
            binding.tvSuggestionTitle.text = item.title
            binding.tvSuggestionAuthor.text = "Por ${item.author}"
            binding.tvSuggestionPages.text = "📖 ${item.totalPages} páginas"
            binding.tvSuggestionReview.text = item.review

            binding.btnAddSuggestedBook.setOnClickListener {
                onAddBookClicked(item)
            }
        }
    }
}
