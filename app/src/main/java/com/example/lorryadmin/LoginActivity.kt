package com.example.lorryadmin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : ComponentActivity() {
    public lateinit var auth: FirebaseAuth
    public lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContent {
            LorryAdminTheme {
                // Surface with a background color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {


                    val apiAvailability = GoogleApiAvailability.getInstance()
                    val status = apiAvailability.isGooglePlayServicesAvailable(this)
                    if (status != ConnectionResult.SUCCESS) {
                        if (apiAvailability.isUserResolvableError(status)) {
                            apiAvailability.getErrorDialog(this, status, 2404)!!?.show()
                        } else {
                            Toast.makeText(this, "Google Play Services is not available", Toast.LENGTH_SHORT).show()
                        }
                    }

                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id)) // This should match the Web Client ID in Firebase Console
                        .requestEmail()
                        .build()

                    googleSignInClient = GoogleSignIn.getClient(this, gso)
                    val context = LocalContext.current

                    val email = remember { mutableStateOf("") }
                    val password = remember { mutableStateOf("") }
                    var passwordVisible by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()), // Make the layout scrollable
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Welcome Back!", style = MaterialTheme.typography.headlineLarge)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Email input field with consistent border and rounded corners
                        Text("Email")
                        InputField(
                            value = email.value,
                            onValueChange = { email.value = it },
                            imeAction = ImeAction.Next
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password input field with consistent border and rounded corners
                        Text("Password")
                        InputField(
                            value = password.value,
                            onValueChange = { password.value = it },
                            imeAction = ImeAction.Done,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Login Button
                        Button(
                            onClick = {
                                val emailInput = email.value
                                val passwordInput = password.value

                                // Validate email format
                                if (emailInput.isEmpty() || passwordInput.isEmpty()) {
                                    Toast.makeText(this@LoginActivity, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                                    // Validate email format using Patterns.EMAIL_ADDRESS regex
                                    Toast.makeText(this@LoginActivity, "Invalid email format", Toast.LENGTH_SHORT).show()
                                } else if (passwordInput.length < 6) {
                                    // Password validation (minimum length check, for example)
                                    Toast.makeText(this@LoginActivity, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    Log.d(emailInput,passwordInput)
                                    // If email and password are valid, proceed with login
                                    handleLogin(emailInput, passwordInput)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Login")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = {
                                val emailInput = email.value // Get the email input from the user

                                if (emailInput.isEmpty()) {
                                    // Show a message if the email field is empty
                                    Toast.makeText(this@LoginActivity, "Please enter your email address", Toast.LENGTH_SHORT).show()
                                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                                    // Validate the email format
                                    Toast.makeText(this@LoginActivity, "Invalid email format", Toast.LENGTH_SHORT).show()
                                } else {
                                    // If email is valid, send the password reset email
                                    FirebaseAuth.getInstance().sendPasswordResetEmail(emailInput)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // Successfully sent password reset email
                                                Toast.makeText(this@LoginActivity, "Password reset mail sent", Toast.LENGTH_SHORT).show()
                                            } else {
                                                // Handle failure (e.g., user not found)
                                                Toast.makeText(this@LoginActivity, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Forgot Password?", color = Color.Blue)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Google Login button with icon
                        IconButton(
                            onClick = {
                                val signInIntent = googleSignInClient.signInIntent
                                googleSignInLauncher.launch(signInIntent)




                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.google), // Placeholder icon from resources
                                contentDescription = "Google Login",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Unspecified
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp)) // Add more space below Google Login button

                        // New User! Register button
                        TextButton(
                            onClick = {
                                // Handle the register button click action
                                // You can navigate to a Registration screen or show a dialog
//                                Toast.makeText(this@LoginActivity, "Redirecting to Registration", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context, RegisterActivity::class.java)
                                startActivity(intent)
                            }
                        ) {
                            Text("New User! Register", color = Color.Blue)

                        }
                    }
                }
            }
        }
    }

    // Handle login button click
    fun handleLogin(email: String, password: String) {
        Log.d(email,password)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                Log.d(email,task.toString())
                if (task.isSuccessful) {
                    // Login successful, navigate to home or next screen
//                    Toast.makeText(this, "LoMgin  email,", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    // Navigate to home activity or next screen
                }
                else {
                    // Login failed, get the exception for more details
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        when (exception.errorCode) {
                            "ERROR_INVALID_EMAIL" -> {
                                // Invalid email format
                                Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT).show()
                            }
                            "ERROR_WRONG_PASSWORD" -> {
                                // Wrong password
                                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
                            }
                            "ERROR_USER_NOT_FOUND" -> {
                                // User not found
                                Toast.makeText(this, "No account found with this email", Toast.LENGTH_SHORT).show()
                            }
                            "ERROR_USER_DISABLED" -> {
                                // User account is disabled
                                Toast.makeText(this, "This account has been disabled", Toast.LENGTH_SHORT).show()
                            }
                            "ERROR_TOO_MANY_REQUESTS" -> {
                                // Too many requests, rate-limiting
                                Toast.makeText(this, "Too many login attempts. Please try again later.", Toast.LENGTH_SHORT).show()
                            }
                            "ERROR_NETWORK_REQUEST_FAILED" -> {
                                // Network failure
                                Toast.makeText(this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                // General authentication error
                                Log.d("Auth Error Login${exception}","err")
                                Toast.makeText(this, "Authentication Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // Generic error (for non-Firebase related issues)
                        Toast.makeText(this, "Authentication Failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                    }

                }
            }
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                Log.d("g sign", "googleSignInLauncher")
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    Log.d("firebaseAuthWithGoogle", "calling")
                    firebaseAuthWithGoogle(it)
                }
            } catch (e: ApiException) {
                Log.e("GoogleSignInError", "Google sign-in failed", e)
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {


            Toast.makeText(this, "Google sign-in was canceled", Toast.LENGTH_SHORT).show()
        }
    }

    // Authenticate with Firebase using Google Account
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        Log.d("firebaseAuthWithGoogle","called")
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful.", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    // Navigate to home or next screen
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
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
            .height(56.dp) // Set a fixed height for the input fields
            .border(BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(8.dp)) // Border and rounded corners
            .padding(8.dp)
    ) {
        BasicTextField(
            value = value, // Using the mutable state
            onValueChange = onValueChange, // Update the state on value change
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart), // Align the text to the start, and the padding will take care of centering
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Black,
//                textAlign = TextAlign.Center // Center the text inside the field
            ), // Text style consistency
            cursorBrush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Black, Color.Black)) // Ensure cursor is visible
        )

        // Password visibility toggle button for password fields
        if (isPassword) {
            IconButton(
                onClick = { onPasswordVisibilityChange?.invoke() },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility), // Replace with your icon resource
                    contentDescription = "Toggle Password Visibility",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

