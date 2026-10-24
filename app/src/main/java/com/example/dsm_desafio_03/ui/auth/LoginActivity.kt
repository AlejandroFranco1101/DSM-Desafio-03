package com.example.dsm_desafio_03.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.dsm_desafio_03.MainActivity
import com.example.dsm_desafio_03.R
import com.example.dsm_desafio_03.databinding.ActivityLoginBinding
import com.example.dsm_desafio_03.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupInputListeners()
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        // Persistencia de sesión: si ya hay un usuario autenticado, ir directo a MainActivity
        if (auth.currentUser != null) {
            navigateToMain()
        }
    }

    private fun setupInputListeners() {
        binding.etEmail.doAfterTextChanged {
            binding.tilEmail.error = null
        }
        binding.etPassword.doAfterTextChanged {
            binding.tilPassword.error = null
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text?.toString().orEmpty().trim()
        val password = binding.etPassword.text?.toString().orEmpty()

        val validation = ValidationUtils.validateLogin(email, password)
        binding.tilEmail.error = validation.emailError
        binding.tilPassword.error = validation.passwordError

        if (!validation.isValid) {
            showMessage("Por favor corrige los campos marcados antes de continuar", isError = true)
            return
        }

        setLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    val errorMsg = mapFirebaseError(task.exception)
                    showMessage(errorMsg, isError = true)
                }
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.btnGoToRegister.isEnabled = !loading
        binding.btnLogin.text = if (loading) "Verificando..." else "Iniciar Sesión"
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
            is FirebaseAuthInvalidUserException ->
                "No existe ninguna cuenta asociada a este correo electrónico."
            is FirebaseAuthInvalidCredentialsException ->
                "Correo o contraseña incorrectos. Verifica tus datos."
            is FirebaseNetworkException ->
                "Sin conexión a Internet. Verifica tu red e intenta de nuevo."
            else -> exception?.localizedMessage ?: "Error al iniciar sesión. Intenta nuevamente."
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
