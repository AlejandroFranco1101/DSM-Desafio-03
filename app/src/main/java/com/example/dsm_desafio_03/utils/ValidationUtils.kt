package com.example.dsm_desafio_03.utils

import android.util.Patterns
import com.example.dsm_desafio_03.model.Book

object ValidationUtils {

    data class AuthValidationResult(
        val isValid: Boolean,
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null
    )

    data class BookValidationResult(
        val isValid: Boolean,
        val titleError: String? = null,
        val authorError: String? = null,
        val genreError: String? = null,
        val totalPagesError: String? = null,
        val readPagesError: String? = null,
        val statusError: String? = null,
        val ratingError: String? = null,
        val detailedMessage: String? = null
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

    fun validateAnnualGoal(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return "Debes ingresar una cantidad de libros para tu meta anual"
        }
        val number = trimmed.toIntOrNull()
            ?: return "La meta anual debe ser un número entero válido"
        if (number <= 0) {
            return "La meta anual debe ser un número entero mayor que cero"
        }
        return null
    }

    fun validateBookForm(
        title: String,
        author: String,
        genre: String,
        totalPagesStr: String,
        readPagesStr: String,
        status: String,
        rating: Int
    ): BookValidationResult {
        val cleanTitle = title.trim()
        val cleanAuthor = author.trim()
        val cleanGenre = genre.trim()
        val cleanStatus = status.trim()

        val titleErr = when {
            cleanTitle.isEmpty() ->
                "Campo 'Título': es obligatorio y no puede estar vacío."
            cleanTitle.length < 2 || cleanTitle.length > 60 ->
                "Campo 'Título': debe tener entre 2 y 60 caracteres (actual: ${cleanTitle.length})."
            else -> null
        }

        val authorErr = when {
            cleanAuthor.isEmpty() ->
                "Campo 'Autor': es obligatorio y no puede estar vacío."
            cleanAuthor.length < 2 || cleanAuthor.length > 60 ->
                "Campo 'Autor': debe tener entre 2 y 60 caracteres (actual: ${cleanAuthor.length})."
            else -> null
        }

        val genreErr = when {
            cleanGenre.isEmpty() || cleanGenre !in Book.MANDATORY_GENRES ->
                "Campo 'Género': debes seleccionar uno de los 4 géneros válidos."
            else -> null
        }

        val totalPages = totalPagesStr.trim().toIntOrNull()
        val totalPagesErr = when {
            totalPagesStr.trim().isEmpty() ->
                "Campo 'Total de páginas': es obligatorio."
            totalPages == null ->
                "Campo 'Total de páginas': debe ser un número entero válido."
            totalPages <= 0 ->
                "Campo 'Total de páginas': debe ser un número entero mayor que cero."
            else -> null
        }

        val readPages = readPagesStr.trim().toIntOrNull()
        val readPagesErr = when {
            readPagesStr.trim().isEmpty() ->
                "Campo 'Páginas leídas': es obligatorio (ingresa 0 si aún no inicias)."
            readPages == null ->
                "Campo 'Páginas leídas': debe ser un número entero válido."
            readPages < 0 ->
                "Campo 'Páginas leídas': no puede ser menor que 0."
            totalPages != null && totalPages > 0 && readPages > totalPages ->
                "Campo 'Páginas leídas': debe estar entre 0 y el total de páginas ($totalPages)."
            else -> null
        }

        val statusErr = when {
            cleanStatus !in Book.BOOK_STATUSES ->
                "Campo 'Estado': debes seleccionar Por leer, Leyendo o Terminado."
            else -> null
        }

        val ratingErr = when {
            cleanStatus == Book.STATUS_FINISHED && (rating < 1 || rating > 5) ->
                "Campo 'Calificación': es obligatoria (de 1 a 5 estrellas) cuando el estado es Terminado."
            cleanStatus != Book.STATUS_FINISHED && rating > 0 ->
                "Campo 'Calificación': solo puede asignarse cuando el estado del libro es Terminado."
            else -> null
        }

        val firstError = listOfNotNull(
            titleErr,
            authorErr,
            genreErr,
            totalPagesErr,
            readPagesErr,
            statusErr,
            ratingErr
        ).firstOrNull()

        return BookValidationResult(
            isValid = firstError == null,
            titleError = titleErr,
            authorError = authorErr,
            genreError = genreErr,
            totalPagesError = totalPagesErr,
            readPagesError = readPagesErr,
            statusError = statusErr,
            ratingError = ratingErr,
            detailedMessage = firstError
        )
    }
}
