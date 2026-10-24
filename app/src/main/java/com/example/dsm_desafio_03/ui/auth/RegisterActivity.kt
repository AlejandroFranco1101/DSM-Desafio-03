package com.example.dsm_desafio_03.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.dsm_desafio_03.MainActivity
import com.example.dsm_desafio_03.R
import com.example.dsm_desafio_03.ReadingTrackerApp
import com.example.dsm_desafio_03.databinding.ActivityRegisterBinding
import com.example.dsm_desafio_03.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupInputListeners()
        setupClickListeners()
    }

    private fun setupInputListeners() {
        binding.etEmail.doAfterTextChanged {
            binding.tilEmail.error = null
        }
        binding.etPassword.doAfterTextChanged {
            binding.tilPassword.error = null
        }
        binding.etConfirmPassword.doAfterTextChanged {
            binding.tilConfirmPassword.error = null
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            attemptRegister()
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun attemptRegister() {
        val name = binding.etName.text?.toString().orEmpty().trim()
        val email = binding.etEmail.text?.toString().orEmpty().trim()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()

        val validation = ValidationUtils.validateRegister(email, password, confirmPassword)
        binding.tilEmail.error = validation.emailError
        binding.tilPassword.error = validation.passwordError
        binding.tilConfirmPassword.error = validation.confirmPasswordError

        if (!validation.isValid) {
            showMessage("Revisa los campos marcados en rojo antes de registrarte", isError = true)
            return
        }

        setLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && name.isNotEmpty()) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user.updateProfile(profileUpdates).addOnCompleteListener {
                            saveUserProfileAndNavigate(user.uid, name, email)
                        }
                    } else if (user != null) {
                        saveUserProfileAndNavigate(user.uid, "", email)
                    } else {
                        setLoading(false)
                        navigateToMain()
                    }
                } else {
                    setLoading(false)
                    val errorMsg = mapFirebaseError(task.exception)
                    showMessage(errorMsg, isError = true)
                }
            }
    }

    private fun saveUserProfileAndNavigate(uid: String, name: String, email: String) {
        val userRef = FirebaseDatabase.getInstance(ReadingTrackerApp.DATABASE_URL)
            .reference
            .child("users")
            .child(uid)
            .child("profile")

        val profileData = mapOf(
            "name" to name,
            "email" to email
        )
        userRef.setValue(profileData)
        setLoading(false)
        navigateToMain()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
        binding.btnBackToLogin.isEnabled = !loading
        binding.btnRegister.text = if (loading) "Creando cuenta..." else "Registrarme Ahora"
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun mapFirebaseError(exception: Exception?): String {
        return when (exception) {
            is FirebaseAuthUserCollisionException ->
                "Este correo electrónico ya está registrado. Intenta iniciar sesión."
            is FirebaseAuthWeakPasswordException ->
                "La contraseña es muy débil. Debe tener al menos 6 caracteres."
            is FirebaseNetworkException ->
                "Sin conexión a Internet. Verifica tu red e intenta de nuevo."
            else -> exception?.localizedMessage ?: "No se pudo completar el registro."
        }
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
