package com.example.fcams

/**
 * Data class representing a room booking record within the system.
 *
 * @property bookingID Unique identifier for the booking record.
 * @property roomID The ID of the room being booked.
 * @property userId The ID or email of the user who made the booking.
 * @property timeDate The scheduled date and time for the booking.
 * @property status The current status of the booking (e.g., "Pending", "Confirmed", "Rejected").
 */
data class RoomBooking(
    val bookingID: String = "",
    val roomID: String = "",
    val userId: String = "",
    val timeDate: String = "",
    val status: String = "Pending" // Pending, Confirmed, Rejected
)