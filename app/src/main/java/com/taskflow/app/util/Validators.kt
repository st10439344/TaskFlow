package com.taskflow.app.util

/** Client-side validation so bad input is caught before it reaches the API. */
object Validators {
    private val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")
    private val TIME = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    fun isValidEmail(value: String) = EMAIL.matches(value.trim())

    /** 8-72 characters with at least one letter and one number (same rule as the API). */
    fun isValidPassword(value: String) =
        value.length in 8..72 && value.any { it.isLetter() } && value.any { it.isDigit() }

    fun passwordsMatch(a: String, b: String) = a == b

    fun isValidName(value: String) = value.trim().length >= 2

    fun isValidTitle(value: String) = value.trim().isNotEmpty() && value.length <= 200

    fun isValidTime(value: String) = TIME.matches(value)
}
