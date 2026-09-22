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
    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
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
        val context = LocalContext.current
        var roomList by remember { mutableStateOf<List<Room>>(emptyList()) }

        LaunchedEffect(Unit) {
            firebaseRepo.getRooms { rooms ->
                roomList = rooms
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Lecturer Portal - Room Control", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Manage rooms and inspect advanced metadata.", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(roomList) { room ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = room.roomName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            // Displaying meta details unique to lecturers
                            Text(text = "Room ID: ${room.roomID} | Capacity: ${room.capacity}")
                            Text(text = "Status: Active / Monitored", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            // Lecturers can view the schedule calendar directly
                            Button(
                                onClick = {
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