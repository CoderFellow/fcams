package com.example.fcams

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

class UserDashboardActivity : ComponentActivity() {
    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply professional light color background token[cite: 1]
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF8F9FA)
            ) {
                UserDashboardScreen()
            }
        }
    }

    @Composable
    fun UserDashboardScreen() {
        val context = LocalContext.current
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "student@fcams.edu"

        // State variables explicitly using 'by remember' to maintain scope throughout sub-blocks[cite: 1]
        var userRole by remember { mutableStateOf("student") } 
        var userBookings by remember { mutableStateOf<List<RoomBooking>>(emptyList()) }
        var swapList by remember { mutableStateOf<List<RoomSwapRequest>>(emptyList()) }
        
        var loadingBookings by remember { mutableStateOf(true) }
        var loadingSwaps by remember { mutableStateOf(true) }

        // Fetch data when screen loads[cite: 1]
        LaunchedEffect(Unit) {
            firebaseRepo.fetchUserRole(currentUserEmail) { role ->
                if (role != null) userRole = role
            }
            firebaseRepo.getUserBookings { bookings ->
                userBookings = bookings
                loadingBookings = false
            }
            firebaseRepo.getSwapRequests(currentUserEmail) { swaps ->
                swapList = swaps
                loadingSwaps = false
            }
        }

        // Core Brand Layout Wrapper Stack[cite: 1]
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Functional Content Overlay[cite: 1]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                
                // --- 1. PREMIUM BRAND HEADLINE BAR ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LAB 4O workspace",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F) // Crimson signature[cite: 1]
                        )
                        Text(
                            text = "User Dashboard",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                    // Minimalistic Profile Node Indicator[cite: 1]
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("U", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // --- 2. HIGH EFFICIENCY QUICK ACTION ACTIONS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { 
                            val intent = Intent(context, StudentDashboardActivity::class.java)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Book Room", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Showing active reservations below", Toast.LENGTH_SHORT).show() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A1A1A)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(8.dp))
                    ) {
                        Text("My Schedule", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // --- 3. SCROLLABLE CONTENT SECTIONS ---
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    
                    // Reservations Section Header
                    item {
                        Text(
                            "Your Active Reservations",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }

                    if (loadingBookings) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFD32F2F))
                            }
                        }
                    } else if (userBookings.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    "No active room reservations found.",
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(userBookings) { booking ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Room ID: ${booking.roomID}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF1A1A1A)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFE3F2FD), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                booking.status.uppercase(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E88E5)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Scheduled: ${booking.timeDate}",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // Swap Section Header
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Active Swap Requests",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }

                    if (loadingSwaps) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFD32F2F))
                            }
                        }
                    } else if (swapList.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    "No pending swap requests.",
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(swapList) { swap ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    if (swap.requesterUserId == currentUserEmail) {
                                        Text(
                                            "📤 Outgoing Swap Request",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFFD32F2F)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Offered trade to user: ${swap.targetUserId}",
                                            fontSize = 13.sp,
                                            color = Color(0xFF1A1A1A)
                                        )
                                        Text(
                                            "Time: ${swap.timeDate}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            "Status: Pending",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E88E5)
                                        )
                                    } else {
                                        Text(
                                            "📥 Incoming Swap Request",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1E88E5)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "User ${swap.requesterUserId} wants your slot.",
                                            fontSize = 13.sp,
                                            color = Color(0xFF1A1A1A)
                                        )
                                        Text(
                                            "Time: ${swap.timeDate}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    firebaseRepo.respondToSwap(swap.swapID, true) { _, message ->
                                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                                modifier = Modifier.weight(1f).height(38.dp)
                                            ) {
                                                Text("Accept", fontSize = 12.sp)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    firebaseRepo.respondToSwap(swap.swapID, false) { _, message ->
                                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A1A1A)),
                                                modifier = Modifier.weight(1f).height(38.dp).border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(8.dp))
                                            ) {
                                                Text("Reject", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- 4. ADMINISTRATIVE STACK ---
                    if (userRole == "lecturer" || userRole == "staff") {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E2))
                            Text(
                                text = "Lecturers and Staff Administration Tools",
                                color = Color(0xFFD32F2F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Room Reports", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Inspect overall room analytics and statistics.", fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Room Logs", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("View historical system audit logs.", fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}