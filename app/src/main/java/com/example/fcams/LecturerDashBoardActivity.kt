package com.example.fcams

import android.content.Intent
import android.os.Bundle
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

class LecturerDashboardActivity : ComponentActivity() {
/*
The dashboard screen presented to logged-in lecturers, focusing on room control and schedule inspection.
*/
    // Initialize the repository to communicate with the Firebase backend
    // An instance of the repository to fetch room data from Firebase.
    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
    /*
    Sets the Compose content view when the activity initializes.
    */

        // Set up the Jetpack Compose UI layout for this activity
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                LecturerDashboardScreen()
            }
        }
    }

    @Composable
    fun LecturerDashboardScreen() {
    /*
    The main UI layout function that fetches rooms from Firebase and renders them in a scrollable list.
    */

        val context = LocalContext.current

        // Create a reactive state list to store fetched rooms and automatically update UI
        var roomList by remember { mutableStateOf<List<Room>>(emptyList()) }

        // Fetch room data from Firebase when the screen first loads
        LaunchedEffect(Unit) {
            firebaseRepo.getRooms { rooms ->
                roomList = rooms
            }
        }

        // Main layout container holding headers and the room list
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Lecturer Portal - Room Control", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Manage rooms and inspect advanced metadata.", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(16.dp))

            // Efficient vertical scrolling list for displaying rooms
            LazyColumn {
                items(roomList) { room ->
                    
                    // Card container for each individual room item
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // Display room name, ID, capacity, and status metadata unique to lecturers
                            Text(text = room.roomName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Room ID: ${room.roomID} | Capacity: ${room.capacity}")
                            Text(text = "Status: Active / Monitored", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            // Button to view the room schedule calendar directly
                            Button(
                                onClick = {

                                    // Bundle Room ID and Name into an Intent and launch RoomScheduleActivity
                                    val intent = Intent(context, RoomScheduleActivity::class.java).apply {
                                        putExtra("ROOM_ID", room.roomID)
                                        putExtra("ROOM_NAME", room.roomName)
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View Room Schedule & Bookings")
                            }
                        }
                    }
                }
            }
        }
    }
}