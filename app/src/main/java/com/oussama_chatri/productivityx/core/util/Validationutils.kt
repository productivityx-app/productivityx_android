package com.oussama_chatri.productivityx.core.util

object ValidationUtils {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9_]{3,30}$")
    private val PHONE_REGEX = Regex("^\\+?[1-9]\\d{7,14}$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun isValidUsername(username: String): Boolean = USERNAME_REGEX.matches(username)

    fun isValidPhone(phone: String): Boolean = PHONE_REGEX.matches(phone.trim())

    fun isValidPassword(password: String): Boolean = password.length >= 8 &&
            password.any { it.isUpperCase() } &&
            password.any { it.isLowerCase() } &&
            password.any { it.isDigit() } &&
            password.any { !it.isLetterOrDigit() }

    fun passwordStrength(password: String): PasswordStrength {
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return PasswordStrength.entries[score]
    }

    fun isValidName(name: String): Boolean = name.trim().length in 1..50

    fun isValidBio(bio: String): Boolean = bio.length <= 500

    enum class PasswordStrength { NONE, WEAK, FAIR, GOOD, STRONG }
}