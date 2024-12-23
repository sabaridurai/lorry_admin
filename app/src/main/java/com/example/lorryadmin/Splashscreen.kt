package com.example.lorryadmin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.lorryadmin.ui.theme.LorryAdminTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up Composable content for the splash screen
        setContent {
            LorryAdminTheme {
                SplashScreen()
            }
        }

        // Show splash screen for 2 seconds before navigating
        Handler().postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                 // If logged in, navigate to HomeActivity
                val database = FirebaseDatabase.getInstance()
                val adminRef = database.getReference("User")

                // Get the Firebase User ID
                val userId = auth.uid ?: "unknown_user"

                // Write the UserId to the Realtime Database
                adminRef.child(userId).setValue(mapOf("userId" to userId))
                    .addOnSuccessListener {
                        Log.d("RealtimeDatabase", "UserId successfully sent to Firebase Realtime Database")
                        navigateToHome() // Navigate to LoginActivity after sending data
                    }
                    .addOnFailureListener { e ->
                        Log.e("RealtimeDatabase", "Error sending UserId to Firebase Realtime Database", e)
                    }
            } else {
                navigateToLogin() // If not logged in, navigate to LoginActivity
            }
        }, 2000) // Splash screen will be shown for 2 seconds
    }

    // Navigate to HomeActivity
    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Navigate to LoginActivity
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Display the splash screen image, scaling it to fit the available space
        Image(
            painter = painterResource(id = R.drawable.google), // Replace with your splash image
            contentDescription = "Splash Image",
            modifier = Modifier.fillMaxSize(), // Make the image fill the full screen
            contentScale = ContentScale.Crop // Ensure the image fits within the bounds while maintaining aspect ratio
        )
    }
}

