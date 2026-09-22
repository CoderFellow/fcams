package com.example.fcams

data class RoomSwapRequest(
    val swapID: String = "",
    val requesterUserId: String = "",
    val requesterBookingID: String = "",
    val targetUserId: String = "",
    val targetBookingID: String = "",
    val timeDate: String = "",
    val status: String = "Pending" // "Pending", "Accepted", "Rejected"
)