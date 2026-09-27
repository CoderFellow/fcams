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

class UserDashboardActivity : ComponentActivity() {
    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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

        // State variables explicitly using 'by remember' to maintain scope throughout sub-blocks
        var userRole by remember { mutableStateOf("student") } 
        var userBookings by remember { mutableStateOf<List<RoomBooking>>(emptyList()) }
        var swapList by remember { mutableStateOf<List<RoomSwapRequest>>(emptyList()) }
        
        var loadingBookings by remember { mutableStateOf(true) }
        var loadingSwaps by remember { mutableStateOf(true) }

        // Fetch data when screen loads
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

        // Core Brand Layout Wrapper Stack
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Functional Content Overlay
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
                            color = Color(0xFFD32F2F) // Crimson signature
                        )
                        Text(
                            text = "User Dashboard",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                    
                    // 🌟 BRANDED REPLACEMENT: Swapped out the letter "U" for your real logo image
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_lab4o_logo),
                            contentDescription = "Profile Branding",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
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
                                    Text("Room ID: ${booking.roomID}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A1A))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Scheduled: ${booking.timeDate}", fontSize = 13.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Status: ${booking.status}", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Swap Management Section Header
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Active Swap Requests", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
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
                                Text("No pending swap requests.", modifier = Modifier.padding(16.dp), fontSize = 14.sp, color = Color.Gray)
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
                                        Text("📤 Outgoing Swap Offer", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                                        Text("Offered trade to user: ${swap.targetUserId}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                                        Text("Slot Time: ${swap.timeDate}", fontSize = 13.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Status: Awaiting Response", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    } else {
                                        Text("📥 Incoming Swap Offer", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 12.sp)
                                        Text("User ${swap.requesterUserId} wants your slot.", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                                        Text("Slot Time: ${swap.timeDate}", fontSize = 13.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    firebaseRepo.respondToSwap(swap.swapID, true) { _, message ->
                                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Accept")
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    firebaseRepo.respondToSwap(swap.swapID, false) { _, message ->
                                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                                                modifier = Modifier.weight(1f).border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(6.dp))
                                            ) {
                                                Text("Reject")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- 4. STAFF ADMINISTRATIVE ACTIONS STACK ---
                    if (userRole == "lecturer" || userRole == "staff") {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
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
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Room Analytics Reports", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                    Text("Inspect overall campus space allocations and booking usage statistics.", fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E2), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("System Security Logs", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                    Text("View real-time historical network transaction tracking logs.", fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}