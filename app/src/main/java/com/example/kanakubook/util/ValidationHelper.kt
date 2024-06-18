package com.example.kanakubook.util


class FieldValidator {

    private val nameRegex = "^[a-zA-Z ]+$".toRegex()
    private val phoneRegex =
        """^(?:\+?\d{10,15}|(?:\d{3}[-._]?){2}\d{4}|(?:\(\d{3}\)[-_]?)?\d{10}|(?:\d{3}_?){3}\d{3})$""".toRegex()
    private val passwordRegex =
        "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!/%*#?&]{8,20}\$".toRegex()

    fun validateName(name: String): String? {
        val maxLength = 160
        val minLength = 3


        return when {
            name.isBlank() -> {
                "Name field cannot be left blank."
            }

            name.length < minLength -> {
                "Name must be at least $minLength characters long."
            }

            name.length > maxLength -> {
                "Name exceeds maximum length of $maxLength characters."
            }

            !name.matches(nameRegex) -> {
                "Invalid name format. Please use letters, numbers, and spaces only."
            }

            containsProfanity(name) -> {
                "Name contains inappropriate language."
            }

            else -> null
        }
    }

    fun validatePhoneNumber(phoneNumber: String, type: Boolean = true): String? {
        if (type) {
            val maxLength = 15
            val minLength = 10

            return when {
                phoneNumber.isBlank() -> {
                    "Phone number field cannot be left blank."
                }

                phoneNumber.length < minLength -> {
                    "Phone number must be at least $minLength digits long."
                }

                phoneNumber.length > maxLength -> {
                    "Phone number exceeds maximum length of $maxLength digits."
                }

                !phoneNumber.matches(phoneRegex) -> {
                    "Invalid phone number format. Please use only digits and optional '+' symbol in before number."
                }

                else -> null
            }
        } else {
            val minLength = 10
            return when {
                phoneNumber.isBlank() -> {
                    "Phone number field cannot be left blank."
                }

                phoneNumber.length < minLength -> {
                    "Phone number must be at least $minLength digits long."
                }


                !phoneNumber.matches(Regex("""^\d+$""")) -> {
                    "Invalid phone number format.give without country code (+91)"
                }

                else -> null
            }
        }
    }


    enum class PasswordStrength(val indicator: Int, val text: String) {
        VERY_WEAK(20, "very weak"),
        WEAK(40, "weak"),
        REASONABLE(60, "reasonable"),
        MEDIUM(80, "medium"),
        STRONG(100, "strong")
    }


    data class PasswordValidationResult(
        val errorMessage: String?,
        val strength: PasswordStrength
    )

    fun validatePassword(password: String): PasswordValidationResult {
        val minLength = 8
        val maxLength = 20
        val passwordRegex = Regex("^[a-zA-Z0-9!@#\$%^&*()-_=+`~<>?,./;:'\"\\[\\]{}|\\\\]*\$")

        var errorMessage: String? = null
        var strength = PasswordStrength.VERY_WEAK

        when {
            password.isBlank() -> {
                errorMessage = "Password field cannot be left blank."
            }

            password.length < minLength -> {
                errorMessage = "Password must be at least $minLength characters long."
            }

            password.length > maxLength -> {
                errorMessage = "Password exceeds maximum length of $maxLength characters."
            }

            !password.matches(passwordRegex) -> {
                errorMessage =
                    "Password contains invalid characters. Please use only letters, numbers, and special characters like !@#\$%^&*()-_=+`~<>?,./;:'\"[]{}|\\."
            }

            !password.any { it.isDigit() } -> {
                errorMessage = "Password must contain at least one digit (0-9)."
            }

            !password.any { it.isLowerCase() } -> {
                errorMessage = "Password must contain at least one lowercase letter (a-z)."
            }

            !password.any { it.isUpperCase() } -> {
                errorMessage = "Password must contain at least one uppercase letter (A-Z)."
            }

            !password.any { it in "!@#\$%^&*()-_=+`~<>?,./;:'\"[]{}|\\" } -> {
                errorMessage = "Password must contain at least one special character."
            }

            else -> {
                strength = when {
                    password.length >= 16 && password.contains(Regex("[a-zA-Z]")) &&
                            password.contains(Regex("[0-9]")) &&
                            password.contains(Regex("[!@#\$%^&*()-_=+`~<>?,./;:'\"\\[\\]{}|\\\\]")) -> {
                        PasswordStrength.STRONG
                    }

                    password.length >= 12 && password.contains(Regex("[a-zA-Z]")) &&
                            (password.contains(Regex("[0-9]")) ||
                                    password.contains(Regex("[!@#\$%^&*()-_=+`~<>?,./;:'\"\\[\\]{}|\\\\]"))) -> {
                        PasswordStrength.MEDIUM
                    }

                    password.length >= 10 && password.contains(Regex("[a-zA-Z]")) &&
                            password.contains(Regex("[0-9]")) -> {
                        PasswordStrength.REASONABLE
                    }

                    password.length >= 8 && password.contains(Regex("[a-zA-Z]")) -> {
                        PasswordStrength.WEAK
                    }

                    else -> PasswordStrength.VERY_WEAK
                }
            }
        }

        return PasswordValidationResult(errorMessage, strength)
    }


    private fun containsProfanity(text: String): Boolean {
        val profanePatterns = listOf(
            "\\u0066\\u0075\\u0063\\u006b",
            "\\u0062\\u0069\\u0074\\u0063\\u0068",
            "\\u0062\\u0061\\u0073\\u0074\\u0061\\u0072\\u0064",
            "\\u0064\\u0069\\u0063\\u006b\\u0068\\u0065\\u0061\\u0064",
            "\\u0063\\u006f\\u0063\\u006b\\u0073\\u0075\\u0063\\u006b\\u0065\\u0072",
            "\\u006d\\u006f\\u0074\\u0068\\u0065\\u0072\\u0066\\u0075\\u0063\\u006b\\u0065\\u0072",
            "\\u0063\\u0075\\u006e\\u0074",
            "\\u0074\\u0077\\u0061\\u0074",
            "\\u0064\\u0069\\u0063\\u006b",
            "\\u0063\\u006f\\u0063\\u006b",
            "\\u0077\\u0061\\u006e\\u006b\\u0065\\u0072",
            "\\u0070\\u0072\\u0069\\u0063\\u006b",
            "\\u0061\\u0073\\u0073\\u0068\\u006f\\u006c\\u0065",
            "\\u0064\\u006f\\u0075\\u0063\\u0068\\u0065\\u0062\\u0061\\u0067",
            "\\u0063\\u0075\\u006e\\u0074\\u0066\\u0061\\u0063\\u0065",
            "\\u0073\\u0068\\u0069\\u0074\\u0068\\u0065\\u0061\\u0064",
            "\\u0062\\u0075\\u006c\\u006c\\u0063\\u0072\\u0061\\u0070",
            "\\u0062\\u006f\\u006c\\u006c\\u006f\\u0063\\u006b\\u0073",
            "\\u0062\\u0075\\u0067\\u0067\\u0065\\u0072",
            "\\u006b\\u006e\\u006f\\u0062\\u0068\\u0065\\u0061\\u0064",
            "\\u0064\\u006f\\u0075\\u0063\\u0068\\u0065\\u0063\\u0061\\u006e\\u006f\\u0065",
            "\\u0061\\u0072\\u0073\\u0065\\u0068\\u006f\\u006c\\u0065",
            "\\u0064\\u0069\\u0063\\u006b\\u0077\\u0061\\u0064",
            "\\u0064\\u0069\\u0063\\u006b\\u0077\\u0065\\u0065\\u0064",
            "\\u006a\\u0061\\u0063\\u006b\\u0061\\u0073\\u0073",
            "\\u0064\\u0069\\u0070\\u0073\\u0068\\u0069\\u0074",
            "\\u0064\\u0069\\u006e\\u0067\\u006c\\u0065\\u0062\\u0065\\u0072\\u0072\\u0079",
            "\\u0061\\u0073\\u0073\\u0077\\u0069\\u0070\\u0065",
            "\\u0061\\u0073\\u0073\\u0063\\u006c\\u006f\\u0077\\u006e",
            "\\u006e\\u0075\\u006d\\u0062\\u006e\\u0075\\u0074\\u0073",
            "\\u0070\\u0065\\u0063\\u006b\\u0065\\u0072\\u0068\\u0065\\u0061\\u0064",
            "\\u0073\\u0063\\u0068\\u006d\\u0075\\u0063\\u006b",
            "\\u0064\\u0075\\u006d\\u0062\\u0061\\u0073\\u0073",
            "\\u0066\\u0075\\u0063\\u006b\\u0077\\u0069\\u0074",
            "\\u0073\\u0068\\u0069\\u0074\\u0062\\u0061\\u0067",
            "\\u0073\\u006c\\u0075\\u0074",
            "\\u0077\\u0068\\u006f\\u0072\\u0065",
            "\\u0070\\u0072\\u0069\\u0063\\u006b\\u0074\\u0065\\u0061\\u0073\\u0065",
            "\\u0074\\u0077\\u0061\\u0074\\u0077\\u0061\\u0066\\u0066\\u006c\\u0065",
            "\\u0073\\u0068\\u0069\\u0074\\u002d\\u0066\\u006f\\u0072\\u002d\\u0062\\u0072\\u0061\\u0069\\u006e\\u0073",
            "\\u0074\\u0068\\u0075\\u006e\\u0064\\u0065\\u0072\\u0063\\u0075\\u006e\\u0074",
            "\\u0061\\u0073\\u0073\\u002d\\u0068\\u0061\\u0074",
            "\\u0064\\u0069\\u0063\\u006b\\u0063\\u0068\\u0065\\u0065\\u0073\\u0065",
            "\\u006b\\u006e\\u006f\\u0062\\u006a\\u006f\\u0063\\u006b\\u0065\\u0079",
            "\\u0064\\u0069\\u0063\\u006b\\u0066\\u0061\\u0063\\u0065",
            "\\u0066\\u0061\\u0072\\u0074\\u006b\\u006e\\u006f\\u0063\\u006b\\u0065\\u0072",
            "\\u0062\\u0075\\u0074\\u0074\\u006d\\u0075\\u006e\\u0063\\u0068\\u0065\\u0072",
            "\\u0064\\u0069\\u006c\\u006c\\u0068\\u006f\\u006c\\u0065",
            "\\u0061\\u0073\\u0073\\u006e\\u0075\\u0067\\u0067\\u0065\\u0074",
            "\\u0074\\u0077\\u0061\\u0074\\u0066\\u0061\\u0063\\u0065",
            "\\u006b\\u006e\\u006f\\u0062\\u0067\\u006f\\u0062\\u0062\\u006c\\u0065\\u0072",
            "\\u0064\\u0069\\u006c\\u006c\\u0068\\u006f\\u006c\\u0065",
            "\\u0061\\u0073\\u0073\\u006e\\u0075\\u0067\\u0067\\u0065\\u0072"
        )


        val combinedPattern = profanePatterns.joinToString(separator = "|")

        val regex = Regex(combinedPattern)

        return regex.containsMatchIn(text)
    }
}