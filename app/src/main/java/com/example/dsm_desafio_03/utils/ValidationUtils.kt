package com.example.dsm_desafio_03.utils

import android.util.Patterns

object ValidationUtils {

    data class AuthValidationResult(
        val isValid: Boolean,
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null
    )

    fun validateLogin(email: String, password: String): AuthValidationResult {
        val trimmedEmail = email.trim()
        val emailErr = when {
            trimmedEmail.isEmpty() -> "El correo electrónico es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() ->
                "Ingresa un formato de correo electrónico válido (ejemplo@dominio.com)"
            else -> null
        }

        val passwordErr = when {
            password.isEmpty() -> "La contraseña es obligatoria"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }

        return AuthValidationResult(
            isValid = emailErr == null && passwordErr == null,
            emailError = emailErr,
            passwordError = passwordErr
        )
    }

    fun validateRegister(
        email: String,
        password: String,
        confirmPassword: String
    ): AuthValidationResult {
        val baseResult = validateLogin(email, password)

        val confirmErr = when {
            confirmPassword.isEmpty() -> "Debes confirmar tu contraseña"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> null
        }

        return baseResult.copy(
            isValid = baseResult.isValid && confirmErr == null,
            confirmPasswordError = confirmErr
        )
    }
}
