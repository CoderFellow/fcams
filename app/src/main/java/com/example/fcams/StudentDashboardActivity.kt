package com.example.fcams

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class StudentDashboardActivity : ComponentActivity() {
    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF8F9FA)
            ) {
                StudentDashboardScreen()
            }
        }
    }

    @Composable
    fun StudentDashboardScreen() {
        val context = LocalContext.current
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "student@fcams.edu"
        
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()
        var roomList by remember { mutableStateOf<List<Room>>(emptyList()) }

        LaunchedEffect(Unit) {
            firebaseRepo.getRooms { rooms ->
                roomList = rooms
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color.White,
                    modifier = Modifier.width(300.dp).fillMaxHeight()
                ) {
                    // --- DRAWER HEADER BRANDING ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A1A1A))
                            .padding(24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_lab4o_logo),
                            contentDescription = "Menu Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("LAB 4O WORKSPACE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        Text(currentUserEmail, fontSize = 14.sp, color = Color.LightGray, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- NAVIGATION LINKS ---
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Workspace Hub", color = Color(0xFF1A1A1A)) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                val intent = Intent(context, UserDashboardActivity::class.java)
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFFD32F2F)) },
                        label = { Text("Book Available Space", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A)) },
                        selected = true,
                        onClick = { coroutineScope.launch { drawerState.close() } },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = Color(0xFFF8F9FA))
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    HorizontalDivider(color = Color(0xFFE0E0E2), thickness = 1.dp)

                    // --- SYSTEM FOOTER LINKS ---
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("App Settings", color = Color(0xFF1A1A1A)) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                Toast.makeText(context, "Opening Settings Portal...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFD32F2F)) },
                        label = { Text("Log Out Session", color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                FirebaseAuth.getInstance().signOut()
                                val intent = Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
                
                // --- TOP ACTION HEADER BAR ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = Color(0xFF1A1A1A), modifier = Modifier.size(28.dp))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LAB 4O WORKSPACE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        Text("Available Rooms", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_lab4o_logo),
                            contentDescription = "Branding",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // --- ROOM LISTING GRID ---
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(roomList) { room ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = room.roomName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                Text(text = "Room ID: ${room.roomID} | Capacity: ${room.capacity}", fontSize = 13.sp, color = Color.Gray)
                                
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(context, SetBookingActivity::class.java).apply {
                                                putExtra("ROOM_ID", room.roomID)
                                            }
                                            context.startActivity(intent)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Text("Book", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(context, RoomScheduleActivity::class.java).apply {
                                                putExtra("ROOM_ID", room.roomID)
                                                putExtra("ROOM_NAME", room.roomName)
                                            }
                                            context.startActivity(intent)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A1A1A)),
                                        modifier = Modifier.weight(1f).height(38.dp).border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(6.dp))
                                    ) {
                                        Text("Schedule", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}