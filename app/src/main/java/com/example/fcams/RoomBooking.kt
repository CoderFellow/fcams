package com.example.fcams

data class RoomBooking(
    val bookingID: String = "",
    val roomID: String = "",
    val userId: String = "",
    val timeDate: String = "",
    val status: String = "Pending" // Pending, Confirmed, Rejected
)