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
    The unified user profile and dashboard page mapped to real Firebase data.
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
        var userRole by remember { mutableStateOf("student") }
        var userBookings by remember { mutableStateOf<List<RoomBooking>>(emptyList()) }
        var loadingBookings by remember { mutableStateOf(true) }

        // Fetch data when the screen loads
        LaunchedEffect(Unit) {
            firebaseRepo.fetchUserRole(currentUserEmail) { role ->
                if (role != null) userRole = role
            }
            
            firebaseRepo.getUserBookings { bookings ->
                userBookings = bookings
                loadingBookings = false
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            
            // --- 1. TOP HEADER: Profile Picture & Settings ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { Toast.makeText(context, "Profile clicked", Toast.LENGTH_SHORT).show() }) {
                    Text("Profile")
                }
                Button(onClick = { Toast.makeText(context, "Settings clicked", Toast.LENGTH_SHORT).show() }) {
                    Text("⚙")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. QUICK ACTION BUTTONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Book Room hooks into StudentDashboard to leverage existing booking flows
                Button(
                    onClick = { 
                        val intent = Intent(context, StudentDashboardActivity::class.java)
                        context.startActivity(intent)
                    }, 
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Book Room", fontSize = 12.sp)
                }
                
                Button(
                    onClick = { Toast.makeText(context, "Scrolling to Bookings Below", Toast.LENGTH_SHORT).show() }, 
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Booked Rooms", fontSize = 12.sp)
                }
                
                Button(
                    onClick = { Toast.makeText(context, "Scrolling to Swaps Below", Toast.LENGTH_SHORT).show() }, 
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Swap Requests", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. SCROLLABLE CONTENT SECTIONS ---
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                
                // Booked Rooms Section Title
                item {
                    Text("Your Active Reservations", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                // Render active user bookings dynamically from database
                if (loadingBookings) {
                    item { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
                } else if (userBookings.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text("No active room reservations found.", modifier = Modifier.padding(16.dp), fontSize = 14.sp)
                        }
                    }
                } else {
                    items(userBookings) { booking ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Room ID: ${booking.roomID}", fontWeight = FontWeight.SemiBold)
                                Text("Scheduled: ${booking.timeDate}", fontSize = 14.sp)
                                Text("Status: ${booking.status}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Swap Requests Section Title
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Swap Requests (Incoming / Outgoing)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                // Swap Requests Section Container Placeholder
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("To manage swaps, select a room via the 'Book Room' menu to open its master schedule, then tap an occupied slot to trade.", fontSize = 14.sp)
                        }
                    }
                }

                // --- 4. LECTURERS & STAFF ONLY SECTION ---
                if (userRole == "lecturer" || userRole == "staff") {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 2.dp)
                        Text(
                            text = "Lecturers and Staff Administration Tools",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Room Reports", fontWeight = FontWeight.Bold)
                                Text("Inspect overall room analytics and statistics.", fontSize = 14.sp)
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
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
