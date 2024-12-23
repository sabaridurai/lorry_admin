package com.example.lorryadmin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.lorryadmin.ui.theme.LorryAdminTheme
import com.example.lorryadmin.ui.theme.Orange
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


// Define screen routes
sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Profile : Screen("profile", "Product")
    object Settings : Screen("settings", "Settings")
}






@Composable
fun BottomNavigationBar(navController: NavController) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.surface, // Bottom bar background color
        contentColor = MaterialTheme.colors.onPrimary   // Icon and text color
    ) {
        val currentRoute = navController.currentDestination?.route
        val screens = listOf(Screen.Home, Screen.Profile, Screen.Settings)

        screens.forEach { screen ->
            BottomNavigationItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = { androidx.compose.material.Text(screen.title) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home, // Add custom icons for each screen
                        contentDescription = screen.title
                    )
                },
                selectedContentColor = MaterialTheme.colors.secondary, // Active item color
                unselectedContentColor = MaterialTheme.colors.onSurface // Inactive item color
            )
        }
    }
}

@Composable
fun HomeScreen(result1: MutableList<Product>) {
    // Add LazyColumn with padding
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Banner Section
        item {
            BannerSection()
        }

        // Spacer for spacing between sections
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // com.example.lorryhub.Product List
        item {
            ProductList(result1)
        }

        // Spacer for spacing between sections
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Booking Options Section
        item {
            BookingOptions()
        }
    }
}

@Composable
fun BannerSection() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var metrics by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var available by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Image Picker Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp),
            color = MaterialTheme.colors.primary,
            strokeWidth = 4.dp
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Grade Input
            OutlinedTextField(
                value = grade,
                onValueChange = { grade = it },
                label = { Text("Grade") },
                modifier = Modifier.fillMaxWidth()
            )

            // Price Input
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                )
            )

            // Availability Input
            OutlinedTextField(
                value = available,
                onValueChange = { available = it },
                label = { Text("Availability") },
                modifier = Modifier.fillMaxWidth()
            )

            // Metrics Input
            OutlinedTextField(
                value = metrics,
                onValueChange = { metrics = it },
                label = { Text("Metrics (e.g., Unit/Pic)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (Contact/Order)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            // Image Picker
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Pick Image")
            }

            // Display Selected Image
            imageUri?.let {
                Image(
                    painter = rememberImagePainter(data = it),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )
            }

            // Upload Button
            Button(
                onClick = {
                    if (metrics.isNotBlank() && imageUri != null && available.isNotBlank()
                        && title.isNotBlank() && description.isNotBlank() && name.isNotBlank() && price.isNotBlank()
                    ) {
                        isLoading = true
                        val storageReference = FirebaseStorage.getInstance().reference
                        val databaseReference = FirebaseDatabase.getInstance().getReference("banners")
                        val fileName = UUID.randomUUID().toString()
                        val imageRef = storageReference.child("banners/$fileName")
                        val userId = FirebaseAuth.getInstance().currentUser?.uid

                        imageUri?.let { uri ->
                            imageRef.putFile(uri)
                                .addOnSuccessListener {
                                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                        val availableBoolean = when (available.trim()) {
                                            "Yes", "yes", "YES", "1" -> true
                                            "No", "no", "NO" -> false
                                            else -> false // Default to false for any other input
                                        }

                                        val bannerData = mapOf(
                                            "Id" to fileName,
                                            "Name" to name,
                                            "Description" to description,
                                            "Image" to downloadUrl.toString(),
                                            "Available" to availableBoolean, // Boolean value
                                            "Button" to title,
                                            "Price" to price, // Ensure Price is Long
                                            "Grade" to grade,
                                            "Metrics" to metrics,
                                            "UserId" to userId
                                        )
                                        databaseReference.push().setValue(bannerData)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(context, "Banner uploaded successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                Toast.makeText(context, "Failed to upload: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    Toast.makeText(context, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }

                        }
                    } else {
                        Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Upload")
            }
        }
    }
}











@Composable
fun BookingOptions() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        androidx.compose.material.Text(
            "Quick Bookings",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow {
            items(3) { index -> // Sample 3 booking options
                BookingOptionItem()
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun BookingOptionItem() {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(150.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

//            booking_icon
            Icon(painter = painterResource(id = R.drawable.ic_visibility), contentDescription = "Booking Icon", modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material.Text(
                "Truck Rental",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.material.Text("Profile page")
    }
}

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.material.Text("Settings Screen")
    }
}
@Composable
fun ProductList(products: List<Product>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        products.forEach { product ->
            Log.d("---+",product.toString())
            ProductItem(product) // This will show all items one after the other
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ProductItem(product: Product) {
    Log.d("---", product.toString())

    Card(
        shape = RoundedCornerShape(8.dp), // Rounded corners for the card
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), // Add padding around the card
        elevation = 4.dp // Elevation for shadow effect
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp) // Padding inside the card
        ) {

            // Use the Image composable to load the product image
            Image(
                painter = rememberImagePainter(product.Image),
                contentDescription = product.Name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 8.dp), // Add space below the image
                contentScale = ContentScale.Crop // To crop the image to fit
            )
            androidx.compose.material.Text(
                text = product.Name,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp) // Add space below the title
            )
            androidx.compose.material.Text(
                text = product.Description,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(bottom = 8.dp) // Add space below the description
            )
            androidx.compose.material.Text(
                text = "Price: ₹${product.Price}",
                style = MaterialTheme.typography.body2,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Space between price and button
            ) {

                Button(onClick = {},colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Yellow, // Set the button background color to yellow
                    contentColor = Color.Black // Set the button text color to black
                )) { androidx.compose.material.Text(text = "Add Card") }

                Button(
                    onClick = {
                        // Handle button click here
                        Log.d("ButtonClicked", "${product.Name} button clicked")
                    },colors = ButtonDefaults.buttonColors(
                        backgroundColor = Orange, // Set the button background color to yellow
                        contentColor = Color.Black // Set the button text color to black
                    )
                ) {
                    Text(text = product.Button) // Button text from product
                }
            }
        }
    }
}


data class Product(

    val Name: String = "",
    val Description: String = "",
    val Price: Long = 0,
    val Image: String = "", // URL for the product image stored in Firebase Storage
    val Button:String=""
)

class MainActivity : ComponentActivity() {
    val result = mutableListOf<Product>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LorryAdminTheme() {
                FirebaseApp.initializeApp(this)
                val database = FirebaseDatabase.getInstance()
                val reference = database.getReference("Product")
                reference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        for (dataSnapshot in snapshot.children) {
                            val product = dataSnapshot.getValue(Product::class.java) // Parse as Product
                            if (product != null) {
                                result.add(product)
                            }
                        }

                        // Log each product
                        for (product in result) {
                            Log.d("+++===", product.toString())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("+++===", "Error: ${error.message}")
                    }
                })

                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    // Add the AppBar at the top
                    bottomBar = { BottomNavigationBar(navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) { HomeScreen(result) }
                        composable(Screen.Profile.route) { ProfileScreen() }
                        composable(Screen.Settings.route) { SettingsScreen() }
                    }
                }
            }
        }
    }
}
