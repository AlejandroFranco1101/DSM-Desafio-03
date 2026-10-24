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
}
