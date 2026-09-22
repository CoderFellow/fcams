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
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = room.roomName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Room ID: ${room.roomID} | Capacity: ${room.capacity}")
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Book Button -> Opens SetBookingActivity
                                Button(
                                    onClick = {
                                        val intent = Intent(context, SetBookingActivity::class.java).apply {
                                            putExtra("ROOM_ID", room.roomID)
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Book")
                                }

                                // 2. Schedule Button -> Opens RoomScheduleActivity (Calendar)
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(context, RoomScheduleActivity::class.java).apply {
                                            putExtra("ROOM_ID", room.roomID)
                                            putExtra("ROOM_NAME", room.roomName)
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Schedule")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}