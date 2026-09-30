package com.example.fcams

/**
 * Data class representing a room swap request between two users.
 *
 * @property swapID Unique identifier for the swap request.
 * @property requesterUserId Identifier of the user requesting the swap.
 * @property requesterBookingID Booking identifier belonging to the requester.
 * @property targetUserId Identifier of the target user whose booking is requested.
 * @property targetBookingID Booking identifier belonging to the target user.
 * @property timeDate Date and time string associated with the swap.
 * @property status Current status of the swap request (e.g., "Pending", "Accepted", "Rejected").
 */
data class RoomSwapRequest(
    val swapID: String = "",
    val requesterUserId: String = "",
    val requesterBookingID: String = "",
    val targetUserId: String = "",
    val targetBookingID: String = "",
    val timeDate: String = "",
    val status: String = "Pending" // "Pending", "Accepted", "Rejected"
)