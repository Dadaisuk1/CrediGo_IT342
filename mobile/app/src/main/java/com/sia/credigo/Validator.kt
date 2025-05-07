package com.sia.credigo

import android.content.Context
import android.widget.Toast
import com.sia.credigo.app.CredigoApp
import com.sia.credigo.model.User
import com.sia.credigo.viewmodel.UserViewModel





fun toastShort(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun validateEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}


//--- Function to validate password with Toast messages

//    validate passworddddd
fun isValidPasswordFormat(context: Context, password: String): Boolean {

    //1: Minimum 8 characters
    if (password.length < 8) {
        toastShort(context, "Password must be at least 8 characters")
        return false
    }

    //2: At least 1 uppercase letter
    if (!password.any { it.isUpperCase() }) {
        toastShort(context, "Password must contain at least 1 uppercase letter")
        return false
    }

    //3: At least 1 lowercase letter
    if (!password.any { it.isLowerCase() }) {
        toastShort(context, "Password must contain at least 1 lowercase letter")
        return false
    }

    // 4: At least 1 number
    if (!password.any { it.isDigit() }) {
        toastShort(context, "Password must contain at least 1 number")
        return false
    }
    // 5: At least 1 special character
    if (!password.any { "!@#$%^&*()_+[]{}|;:,.<>?".contains(it) }) {
        toastShort(context, "Password must contain at least 1 special character")
        return false
    }
    // 6: No spaces
    if (password.contains(" ")) {
        toastShort(context, "Password must not contain spaces")
        return false
    }
    // 7: No common patterns (e.g., "password", "12345678")
    val commonPatterns = listOf("password", "12345678", "qwerty", "abc123", "123123")
    if (commonPatterns.any { password.contains(it, ignoreCase = true) }) {
        toastShort(context, "Password is too common")
        return false
    }
    //all is good
    return true;
}


// Function to validate if the password and confirm password match
fun validatePasswordsMatch(password: String, confirmPassword: String): Boolean {
    return password == confirmPassword
}


//-- Function to validate email
//check if email is valid
fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

// check if email is already registered
suspend fun isEmailRegistered(context: Context, email: String): Boolean {
    val app = context.applicationContext as CredigoApp
    val userViewModel = UserViewModel(app)
    val user = userViewModel.getUserByEmail(email)
    return user != null
}

// check if username is already taken
suspend fun isUsernameTaken(context: Context, username: String): Boolean {
    val app = context.applicationContext as CredigoApp
    val userViewModel = UserViewModel(app)
    val user = userViewModel.getUserByUsername(username)
    return user != null
}
