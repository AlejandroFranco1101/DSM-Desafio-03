package com.example.dsm_desafio_03

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dsm_desafio_03.data.ReadingRepository
import com.example.dsm_desafio_03.databinding.ActivityMainBinding
import com.example.dsm_desafio_03.databinding.DialogEditGoalBinding
import com.example.dsm_desafio_03.model.Book
import com.example.dsm_desafio_03.ui.auth.LoginActivity
import com.example.dsm_desafio_03.ui.book.BookFormActivity
import com.example.dsm_desafio_03.ui.library.LibraryActivity
import com.example.dsm_desafio_03.ui.main.CurrentlyReadingAdapter
import com.example.dsm_desafio_03.ui.stats.StatisticsActivity
import com.example.dsm_desafio_03.utils.ValidationUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var repository: ReadingRepository
    private lateinit var readingAdapter: CurrentlyReadingAdapter

    private var goalListener: ValueEventListener? = null
    private var booksListener: ValueEventListener? = null

    private var currentAnnualGoal: Int = 0
    private var allUserBooks: List<Book> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        repository = ReadingRepository()

        ensureInitialBooksInFirebase()
        setupUserHeader()
        setupRecyclerView()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }
        startRealtimeObservers()
    }

    override fun onStop() {
        super.onStop()
        val year = repository.getCurrentYear()
        repository.removeGoalListener(year, goalListener)
        repository.removeBooksListener(booksListener)
    }

    private fun ensureInitialBooksInFirebase() {
        val uid = auth.currentUser?.uid ?: return
        val prefs = getSharedPreferences("bibliolara_prefs", MODE_PRIVATE)
        val key = "initial_books_inserted_$uid"
        if (!prefs.getBoolean(key, false)) {
            repository.seedRealBooksToFirebaseIfNeeded {
                prefs.edit().putBoolean(key, true).apply()
            }
        }
    }

    private fun setupUserHeader() {
        val user = auth.currentUser ?: return
        val email = user.email.orEmpty()
        val displayName = user.displayName?.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

        binding.tvGreeting.text = "¡Hola, $displayName!"
        binding.tvUserEmail.text = email

        val currentYear = repository.getCurrentYear()
        binding.tvGoalTitle.text = "Meta Anual $currentYear"
    }

    private fun setupRecyclerView() {
        readingAdapter = CurrentlyReadingAdapter(
            onBookClick = { book ->
                onBookSelected(book)
            }
        )
        binding.rvCurrentlyReading.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = readingAdapter
        }
    }

    private fun setupListeners() {
        binding.btnEditGoal.setOnClickListener {
            showEditGoalDialog()
        }

        binding.btnAddBook.setOnClickListener {
            openAddOrEditBook(null)
        }

        binding.btnOpenLibrary.setOnClickListener {
            openLibraryScreen()
        }

        binding.btnOpenStats.setOnClickListener {
            openStatsScreen()
        }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseas cerrar tu sesión actual en BiblioLara?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Cerrar sesión") { _, _ ->
                    auth.signOut()
                    navigateToLogin()
                }
                .show()
        }
    }

    private fun startRealtimeObservers() {
        val currentYear = repository.getCurrentYear()
        goalListener = repository.observeAnnualGoal(currentYear) { goal ->
            currentAnnualGoal = goal
            updateDashboardUI()
        }
        booksListener = repository.observeBooks { books ->
            allUserBooks = books
            updateDashboardUI()
        }
    }

    private fun updateDashboardUI() {
        val currentYear = repository.getCurrentYear()

        // Libros terminados en el año actual
        val finishedThisYear = allUserBooks.count { book ->
            book.status == Book.STATUS_FINISHED &&
                (book.finishedYear == currentYear || book.finishedYear == 0)
        }

        binding.tvGoalFinishedValue.text = finishedThisYear.toString()

        if (currentAnnualGoal > 0) {
            val remaining = (currentAnnualGoal - finishedThisYear).coerceAtLeast(0)
            val progressPercent = ((finishedThisYear.toDouble() / currentAnnualGoal.toDouble()) * 100.0)
                .roundToInt()
                .coerceIn(0, 100)

            binding.tvGoalTargetValue.text = currentAnnualGoal.toString()
            binding.tvGoalRemainingValue.text = remaining.toString()
            binding.tvGoalRemainingValue.setTextColor(
                getColor(if (remaining == 0) R.color.status_success else R.color.steel_blue)
            )
            binding.btnEditGoal.text = "Editar meta"
            binding.tvGoalSubtitle.text = "Avance de lectura en $currentYear"
            binding.tvGoalPercent.text = "$progressPercent%"
            binding.progressAnnualGoal.progress = progressPercent

            binding.tvGoalStatusMessage.text = when {
                remaining == 0 -> "¡Felicidades! Has cumplido tu meta anual de $currentAnnualGoal libros"
                remaining == 1 -> "¡Estás a solo 1 libro de alcanzar tu meta anual!"
                else -> "Te faltan $remaining libros para alcanzar tu meta de $currentYear"
            }
        } else {
            binding.tvGoalTargetValue.text = "Sin definir"
            binding.tvGoalTargetValue.textSize = 15f
            binding.tvGoalRemainingValue.text = "-"
            binding.btnEditGoal.text = "Definir meta"
            binding.tvGoalSubtitle.text = "Define tu objetivo de lectura para $currentYear"
            binding.tvGoalPercent.text = "0%"
            binding.progressAnnualGoal.progress = 0
            binding.tvGoalStatusMessage.text = "Toca \"Definir meta\" para establecer tu objetivo anual"
        }

        if (currentAnnualGoal > 0) {
            binding.tvGoalTargetValue.textSize = 22f
        }

        // Libros en estado "Leyendo" actualmente
        val readingBooks = allUserBooks.filter { it.status == Book.STATUS_READING }
        binding.tvReadingCountBadge.text =
            if (readingBooks.size == 1) "1 activo" else "${readingBooks.size} activos"

        if (readingBooks.isEmpty()) {
            binding.rvCurrentlyReading.visibility = View.GONE
            binding.cardEmptyReading.visibility = View.VISIBLE
        } else {
            binding.cardEmptyReading.visibility = View.GONE
            binding.rvCurrentlyReading.visibility = View.VISIBLE
            readingAdapter.submitList(readingBooks)
        }
    }

    private fun showEditGoalDialog() {
        val dialogBinding = DialogEditGoalBinding.inflate(layoutInflater)
        val currentYear = repository.getCurrentYear()

        if (currentAnnualGoal > 0) {
            dialogBinding.etGoalInput.setText(currentAnnualGoal.toString())
            dialogBinding.etGoalInput.setSelection(dialogBinding.etGoalInput.text?.length ?: 0)
        }

        dialogBinding.etGoalInput.doAfterTextChanged {
            dialogBinding.tilGoalInput.error = null
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(
                if (currentAnnualGoal > 0) "Modificar Meta Anual ($currentYear)"
                else "Definir Meta Anual ($currentYear)"
            )
            .setView(dialogBinding.root)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar Meta", null)
            .create()

        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveBtn.setOnClickListener {
                val rawValue = dialogBinding.etGoalInput.text?.toString().orEmpty()
                val validationError = ValidationUtils.validateAnnualGoal(rawValue)
                if (validationError != null) {
                    dialogBinding.tilGoalInput.error = validationError
                    return@setOnClickListener
                }

                val newGoal = rawValue.trim().toInt()
                positiveBtn.isEnabled = false
                repository.saveAnnualGoal(newGoal, currentYear) { success, errorMsg ->
                    positiveBtn.isEnabled = true
                    if (success) {
                        dialog.dismiss()
                        showMessage("Meta anual de $newGoal libros guardada correctamente")
                    } else {
                        showMessage(errorMsg ?: "No se pudo guardar la meta", isError = true)
                    }
                }
            }
        }

        dialog.show()
    }

    private fun onBookSelected(book: Book) {
        openAddOrEditBook(book)
    }

    private fun openAddOrEditBook(book: Book?) {
        startActivity(BookFormActivity.createIntent(this, book))
    }

    private fun openLibraryScreen() {
        startActivity(Intent(this, LibraryActivity::class.java))
    }

    private fun openStatsScreen() {
        startActivity(Intent(this, StatisticsActivity::class.java))
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showMessage(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(
            getColor(if (isError) R.color.status_error else R.color.deep_teal)
        )
        snackbar.setTextColor(getColor(R.color.white))
        snackbar.show()
    }
}
