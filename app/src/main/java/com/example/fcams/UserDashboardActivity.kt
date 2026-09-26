package com.example.fcams

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

class UserDashboardActivity: ComponentActivity() {
    /*
    The unified user profile and dashboard page.
    */
    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                UserDashboardScreen()
            }
        }
    }

    @Composable
    fun UserDashboardScreen() {
        val context = LocalContext.current
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "User"

        // State variables for data tracking
        var roomList by remember { mutableStateOf<List<Room>>(emptyList()) }
        var userRole by remember { mutableStateOf<String>("student") } // Default to student, fetch later if needed

        // Fetch room data and user role when the screen loads
        LaunchedEffect(Unit) {
            firebaseRepo.getRooms { rooms ->
                roomList = rooms
            }
            firebaseRepo.fetchUserRole(currentUserEmail) { role ->
                if (role != null) userRole = role
            }
        }

        // Main layout container following your wireframe structure
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            
            // --- 1. TOP HEADER: Profile Picture & Settings ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Profile Picture Placeholder
                Button(onClick = { /* TODO: Open profile settings */ }) {
                    Text("Profile")
                }
                // Settings Button
                Button(onClick = { /* TODO: Open app settings */ }) {
                    Text("⚙")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. QUICK ACTION BUTTONS (Book Room, Booked Rooms, Swap Requests) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBy(8.dp)
            ) {
                Button(onClick = { /* TODO: Navigate to booking */ }, modifier = Modifier.weight(1f)) {
                    Text("Book Room", fontSize = 12.sp)
                }
                Button(onClick = { /* TODO: View booked rooms */ }, modifier = Modifier.weight(1f)) {
                    Text("Booked Rooms", fontSize = 12.sp)
                }
                Button(onClick = { /* TODO: View swap requests */ }, modifier = Modifier.weight(1f)) {
                    Text("Swap Requests", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. SCROLLABLE CONTENT SECTIONS ---
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                
                // Notifications Section
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Notifications", fontWeight = FontWeight.Bold)
                            Text("No new notifications.", fontSize = 14.sp)
                        }
                    }
                }

                // Booked Rooms Section Summary
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Your Booked Rooms", fontWeight = FontWeight.Bold)
                            Text("Check your active reservations here.", fontSize = 14.sp)
                        }
                    }
                }

                // Swap Requests Section
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Swap Requests (Incoming / Outgoing)", fontWeight = FontWeight.Bold)
                            Text("No pending swaps.", fontSize = 14.sp)
                        }
                    }
                }

                // --- 4. LECTURERS & STAFF ONLY SECTION ---
                if (userRole == "lecturer" || userRole == "staff") {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(thickness = 2.dp)
                        Text(
                            text = "Lecturers and other staff only.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Room Reports", fontWeight = FontWeight.Bold)
                                Text("Inspect overall room analytics and statistics.", fontSize = 14.sp)
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Room Logs", fontWeight = FontWeight.Bold)
                                Text("View historical system audit logs.", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}