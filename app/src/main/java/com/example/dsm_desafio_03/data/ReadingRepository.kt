package com.example.dsm_desafio_03.data

import com.example.dsm_desafio_03.ReadingTrackerApp
import com.example.dsm_desafio_03.model.Book
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class ReadingRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance(ReadingTrackerApp.DATABASE_URL)

    private fun userRootRef(): DatabaseReference? {
        val uid = auth.currentUser?.uid ?: return null
        val ref = database.reference.child("users").child(uid)
        ref.keepSynced(true)
        return ref
    }

    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun observeAnnualGoal(
        year: Int = getCurrentYear(),
        onGoalChanged: (Int) -> Unit
    ): ValueEventListener? {
        val goalRef = userRootRef()?.child("goals")?.child(year.toString()) ?: return null
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val goal = snapshot.getValue(Int::class.java) ?: 0
                onGoalChanged(goal)
            }

            override fun onCancelled(error: DatabaseError) {
                // Keep UI state
            }
        }
        goalRef.addValueEventListener(listener)
        return listener
    }

    fun removeGoalListener(year: Int = getCurrentYear(), listener: ValueEventListener?) {
        if (listener == null) return
        userRootRef()?.child("goals")?.child(year.toString())?.removeEventListener(listener)
    }

    fun saveAnnualGoal(
        targetBooks: Int,
        year: Int = getCurrentYear(),
        onComplete: (Boolean, String?) -> Unit
    ) {
        val goalRef = userRootRef()?.child("goals")?.child(year.toString())
        if (goalRef == null) {
            onComplete(false, "Sesión no válida")
            return
        }
        goalRef.setValue(targetBooks)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.localizedMessage ?: "Error al guardar la meta")
                }
            }
    }

    fun observeBooks(onBooksChanged: (List<Book>) -> Unit): ValueEventListener? {
        val booksRef = userRootRef()?.child("books") ?: return null
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Book>()
                for (child in snapshot.children) {
                    val book = child.getValue(Book::class.java)
                    if (book != null) {
                        list.add(book)
                    }
                }
                // Ordenar del más reciente al más antiguo por defecto
                list.sortByDescending { it.createdAt }
                onBooksChanged(list)
            }

            override fun onCancelled(error: DatabaseError) {
                // Keep UI state
            }
        }
        booksRef.addValueEventListener(listener)
        return listener
    }

    fun removeBooksListener(listener: ValueEventListener?) {
        if (listener == null) return
        userRootRef()?.child("books")?.removeEventListener(listener)
    }

    fun saveBook(book: Book, onComplete: (Boolean, String?) -> Unit) {
        val booksRef = userRootRef()?.child("books")
        if (booksRef == null) {
            onComplete(false, "Sesión no válida")
            return
        }
        val bookId = book.id.ifBlank { booksRef.push().key ?: System.currentTimeMillis().toString() }
        val bookToSave = book.copy(id = bookId)

        booksRef.child(bookId).setValue(bookToSave)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.localizedMessage ?: "Error al guardar el libro")
                }
            }
    }

    fun deleteBook(bookId: String, onComplete: (Boolean, String?) -> Unit) {
        val bookRef = userRootRef()?.child("books")?.child(bookId)
        if (bookRef == null) {
            onComplete(false, "Sesión no válida")
            return
        }
        bookRef.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.localizedMessage ?: "Error al eliminar el libro")
                }
            }
    }

    fun seedRealBooksToFirebaseIfNeeded(onSeeded: () -> Unit = {}) {
        val booksRef = userRootRef()?.child("books") ?: return
        val currentYear = getCurrentYear()

        booksRef.get().addOnSuccessListener { snapshot ->
            val existingTitles = snapshot.children.mapNotNull {
                it.getValue(Book::class.java)?.title?.lowercase()?.trim()
            }.toSet()

            val calFirst = Calendar.getInstance().apply {
                set(currentYear, Calendar.MARCH, 12, 10, 30, 0)
            }
            val calSecond = Calendar.getInstance().apply {
                set(currentYear, Calendar.JUNE, 18, 16, 45, 0)
            }

            val sampleBooks = listOf(
                Book(
                    title = "Cien años de soledad",
                    author = "Gabriel García Márquez",
                    genre = Book.GENRE_NOVEL,
                    totalPages = 496,
                    readPages = 496,
                    status = Book.STATUS_FINISHED,
                    rating = 5,
                    finishedAt = calFirst.timeInMillis,
                    finishedDateFormatted = "12/03/$currentYear",
                    finishedYear = currentYear,
                    createdAt = calFirst.timeInMillis - 86_400_000L
                ),
                Book(
                    title = "Proyecto Hail Mary",
                    author = "Andy Weir",
                    genre = Book.GENRE_SCIFI,
                    totalPages = 480,
                    readPages = 480,
                    status = Book.STATUS_FINISHED,
                    rating = 5,
                    finishedAt = calSecond.timeInMillis,
                    finishedDateFormatted = "18/06/$currentYear",
                    finishedYear = currentYear,
                    createdAt = calSecond.timeInMillis - 86_400_000L
                ),
                Book(
                    title = "Hábitos Atómicos",
                    author = "James Clear",
                    genre = Book.GENRE_PERSONAL_DEV,
                    totalPages = 320,
                    readPages = 208,
                    status = Book.STATUS_READING,
                    rating = 0,
                    finishedAt = null,
                    finishedDateFormatted = "",
                    finishedYear = 0,
                    createdAt = System.currentTimeMillis() - 7_200_000L
                ),
                Book(
                    title = "Sapiens: De animales a dioses",
                    author = "Yuval Noah Harari",
                    genre = Book.GENRE_HISTORY,
                    totalPages = 512,
                    readPages = 0,
                    status = Book.STATUS_TO_READ,
                    rating = 0,
                    finishedAt = null,
                    finishedDateFormatted = "",
                    finishedYear = 0,
                    createdAt = System.currentTimeMillis() - 3_600_000L
                )
            )

            var addedAny = false
            for (book in sampleBooks) {
                if (book.title.lowercase().trim() !in existingTitles) {
                    val newKey = booksRef.push().key ?: continue
                    booksRef.child(newKey).setValue(book.copy(id = newKey))
                    addedAny = true
                }
            }

            if (addedAny) {
                onSeeded()
            }
        }
    }
}
