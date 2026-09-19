package com.example.fcams

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
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

class StudentDashboardActivity : ComponentActivity() {
    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                StudentDashboardScreen()
            }
        }
    }

    @Composable
    fun StudentDashboardScreen() {
        val context = LocalContext.current
        var roomList by remember { mutableStateOf<List<Room>>(emptyList()) }
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

        LaunchedEffect(Unit) {
            firebaseRepo.getRooms { rooms ->
                roomList = rooms
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Available Rooms", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(roomList) { room ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                val targetTime = "2026-09-20 10:00 AM"
                                firebaseRepo.bookRoom(room.roomID, currentUserEmail, targetTime) { success, error ->
                                    if (success) {
                                        Toast.makeText(context, "Booked ${room.roomName}!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = room.roomName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Room ID: ${room.roomID} | Capacity: ${room.capacity}")
                        }
                    }
                }
            }
        }
    }
}