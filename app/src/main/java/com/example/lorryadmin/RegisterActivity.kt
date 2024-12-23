package com.example.lorryadmin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lorryadmin.ui.theme.LorryAdminTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class RegisterActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            LorryAdminTheme  {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {


                    RegisterScreen(LocalContext.current)
                }
            }
        }
    }

    @Composable
    fun RegisterScreen(current: Context) {
        val email = remember { mutableStateOf("") }
        val password = remember { mutableStateOf("") }
        val confirmPassword = remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var confirmPasswordVisible by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Make the layout scrollable
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create New Account", style = MaterialTheme.typography.headlineLarge)

            Spacer(modifier = Modifier.height(24.dp))

            // Email Input
            Text("Email")
            InputField(
                value = email.value,
                onValueChange = { email.value = it },
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            Text("Password")
            InputField(
                value = password.value,
                onValueChange = { password.value = it },
                imeAction = ImeAction.Next,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Input
            Text("Confirm Password")
            InputField(
                value = confirmPassword.value,
                onValueChange = { confirmPassword.value = it },
                imeAction = ImeAction.Done,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Register Button
            Button(
                onClick = {
                    val emailInput = email.value
                    val passwordInput = password.value
                    val confirmPasswordInput = confirmPassword.value

                    // Validate fields
                    if (emailInput.isEmpty() || passwordInput.isEmpty() || confirmPasswordInput.isEmpty()) {
                        Toast.makeText(this@RegisterActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    } else if (passwordInput != confirmPasswordInput) {
                        // Check if password and confirm password match
                        Toast.makeText(this@RegisterActivity, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    } else {
                        // Register user with Firebase
                        registerUser(emailInput, passwordInput)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Already have an account? Login Button
            TextButton(
                onClick = {
                    // Handle navigation to the Login screen
                    val intent = Intent(current, LoginActivity::class.java)
                    startActivity(intent)
                    finish()

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account? Login", color = Color.Blue)
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                    // Navigate to the login screen or home screen as needed
                } else {
                    // Registration failed, show specific error message
                    val exception = task.exception
                    when (exception) {
                        is FirebaseAuthUserCollisionException -> {
                            // Email already in use
                            Toast.makeText(this, "This email is already registered. Please use a different email.", Toast.LENGTH_LONG).show()
                        }
                        is FirebaseAuthWeakPasswordException -> {
                            // Weak password
                            Toast.makeText(this, "Password is too weak. Please choose a stronger password.", Toast.LENGTH_LONG).show()
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            // Invalid email format
                            Toast.makeText(this, "Invalid email format. Please enter a valid email address.", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            // General error or unknown issue
                            val errorMessage = exception?.localizedMessage ?: "Registration failed due to an unknown error."
                            Toast.makeText(this, "Registration Failed: $errorMessage", Toast.LENGTH_LONG).show()
                            Log.e("RegisterUserError", "Registration failed", exception)
                        }
                    }
                }
            }
    }

    @Composable
    fun InputField(
        value: String,
        onValueChange: (String) -> Unit,
        imeAction: ImeAction,
        isPassword: Boolean = false,
        passwordVisible: Boolean = false,
        onPasswordVisibilityChange: (() -> Unit)? = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction),
                visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                cursorBrush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Black, Color.Black))
            )

            // Toggle password visibility for password fields
            if (isPassword) {
                IconButton(
                    onClick = { onPasswordVisibilityChange?.invoke() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        painter = painterResource(id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                        contentDescription = "Toggle Password Visibility",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
