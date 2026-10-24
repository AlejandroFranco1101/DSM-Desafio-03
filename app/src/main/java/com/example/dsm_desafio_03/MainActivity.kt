package com.example.dsm_desafio_03

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dsm_desafio_03.databinding.ActivityMainBinding
import com.example.dsm_desafio_03.ui.auth.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            navigateToLogin()
            return
        }

        setupUserHeader()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            navigateToLogin()
        }
    }

    private fun setupUserHeader() {
        val user = auth.currentUser ?: return
        val email = user.email.orEmpty()
        val displayName = user.displayName?.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

        binding.tvGreeting.text = "¡Hola, $displayName!"
        binding.tvUserEmail.text = email
        binding.tvSessionDetails.text =
            "Conectado como $email\nTu sesión de Firebase se mantiene activa aunque cierres la aplicación."
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseas cerrar tu sesión actual en BiblioMeta?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Cerrar sesión") { _, _ ->
                    auth.signOut()
                    navigateToLogin()
                }
                .show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
