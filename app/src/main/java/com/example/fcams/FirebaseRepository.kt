package com.example.fcams

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Repository class responsible for handling Firebase Authentication and Firestore database operations
 * for user management, room bookings, and room swap requests.
 */
class FirebaseRepository {

    /** Firebase Authentication instance. */
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /** Firebase Firestore database instance. */
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Authenticates users using their email address and password.
     *
     * @param email The user's email address.
     * @param pass The user's password.
     * @param onResult Callback returning a Boolean success flag and an optional error message on failure.
     */
    fun loginUser(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage)
                }
            }
    }

    /**
     * Queries the Firestore users collection by email to retrieve a user's role.
     *
     * @param email The email address to look up.
     * @param onRoleFetched Callback returning the user's role string (e.g., admin, student, staff), or null if not found.
     */
    fun fetchUserRole(email: String, onRoleFetched: (String?) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val role = documents.documents[0].getString("role")
                    onRoleFetched(role)
                } else {
                    onRoleFetched(null)
                }
            }
            .addOnFailureListener {
                onRoleFetched(null)
            }
    }

    /**
     * Retrieves a list of all available rooms from the rooms collection.
     *
     * @param onRoomsFetched Callback returning a list of [Room] objects.
     */
    fun getRooms(onRoomsFetched: (List<Room>) -> Unit) {
        db.collection("rooms")
            .get()
            .addOnSuccessListener { result ->
                val roomList = result.toObjects(Room::class.java)
                onRoomsFetched(roomList)
            }
            .addOnFailureListener {
                onRoomsFetched(emptyList())
            }
    }

    /**
     * Checks for scheduling conflicts first, and if clear, generates a unique booking ID 
     * and saves a new booking object with a "Pending" status to the roomBookings collection.
     *
     * @param roomID The ID of the room being booked.
     * @param userId The ID of the user making the booking.
     * @param timeDate The date and time string for the booking.
     * @param onResult Callback returning a Boolean success flag and a status message.
     */
    fun bookRoom(roomID: String, userId: String, timeDate: String, onResult: (Boolean, String) -> Unit) {
        checkBookingConflict(roomID, timeDate) { hasConflict ->
            if (hasConflict) {
                onResult(false, "This room is already booked for this date and time!")
            } else {
                val bookingID = db.collection("roomBookings").document().id
                val newBooking = RoomBooking(
                    bookingID = bookingID,
                    roomID = roomID,
                    userId = userId,
                    timeDate = timeDate,
                    status = "Pending"
                )
    
                db.collection("roomBookings")
                    .document(bookingID)
                    .set(newBooking)
                    .addOnSuccessListener {
                        onResult(true, "Room successfully booked!")
                    }
                    .addOnFailureListener { e ->
                        onResult(false, "Failed to book: ${e.localizedMessage}")
                    }
            }
        }
    }

    /**
     * Queries active bookings in the roomBookings collection for a specific room and time slot to prevent double-booking.
     *
     * @param roomID The ID of the room to check.
     * @param selectedDateTime The target date and time string.
     * @param onResult Callback returning true if a conflict exists, or false otherwise.
     */
    fun checkBookingConflict(roomID: String, selectedDateTime: String, onResult: (Boolean) -> Unit) {
        db.collection("roomBookings")
            .whereEqualTo("roomID", roomID)
            .whereEqualTo("timeDate", selectedDateTime)
            .get()
            .addOnSuccessListener { documents ->
                val hasConflict = !documents.isEmpty
                onResult(hasConflict)
            }
            .addOnFailureListener {
                onResult(true) 
            }
    }

    /**
     * Fetches all bookings tied to a specific room ID.
     *
     * @param roomID The target room ID.
     * @param onResult Callback returning a list of [RoomBooking] objects.
     */
    fun getBookingsForRoom(roomID: String, onResult: (List<RoomBooking>) -> Unit) {
        db.collection("roomBookings")
            .whereEqualTo("roomID", roomID)
            .get()
            .addOnSuccessListener { documents ->
                val bookings = documents.toObjects(RoomBooking::class.java)
                onResult(bookings)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * Requests a room swap with time verification.
     *
     * @param requesterBookingID The booking ID of the user requesting the swap.
     * @param targetBookingID The target booking ID to swap with.
     * @param onResult Callback returning a Boolean success flag and a status message.
     */
    fun requestSwap(requesterBookingID: String, targetBookingID: String, onResult: (Boolean, String) -> Unit) {
        db.collection("roomBookings").document(requesterBookingID).get().addOnSuccessListener { reqDoc ->
            db.collection("roomBookings").document(targetBookingID).get().addOnSuccessListener { targetDoc ->
                val reqBooking = reqDoc.toObject(RoomBooking::class.java)
                val targetBooking = targetDoc.toObject(RoomBooking::class.java)
    
                if (reqBooking != null && targetBooking != null) {
                    if (reqBooking.timeDate == targetBooking.timeDate) {
                        val swapID = db.collection("roomSwaps").document().id
                        val swapRequest = RoomSwapRequest(
                            swapID = swapID,
                            requesterUserId = reqBooking.userId,
                            requesterBookingID = requesterBookingID,
                            targetUserId = targetBooking.userId,
                            targetBookingID = targetBookingID,
                            timeDate = reqBooking.timeDate,
                            status = "Pending"
                        )
    
                        db.collection("roomSwaps").document(swapID).set(swapRequest)
                            .addOnSuccessListener { onResult(true, "Swap request sent successfully!") }
                            .addOnFailureListener { e -> onResult(false, "Failed to send request: ${e.localizedMessage}") }
                    } else {
                        onResult(false, "Cannot swap: Rooms are not set at the same date and time!")
                    }
                } else {
                    onResult(false, "Booking details not found.")
                }
            }
        }
    }

    /**
     * Fetches pending swap requests involving the user.
     *
     * @param userId The ID of the user.
     * @param onResult Callback returning a list of pending [RoomSwapRequest] objects.
     */
    fun getSwapRequests(userId: String, onResult: (List<RoomSwapRequest>) -> Unit) {
        db.collection("roomSwaps")
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener { documents ->
                val allSwaps = documents.toObjects(RoomSwapRequest::class.java)
                val filteredSwaps = allSwaps.filter { it.requesterUserId == userId || it.targetUserId == userId }
                onResult(filteredSwaps)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * Accepts or rejects a swap request with completed batch updates.
     *
     * @param swapID The ID of the swap request.
     * @param accept Flag indicating whether to accept (true) or reject (false) the swap.
     * @param onResult Callback returning a Boolean success flag and a status message.
     */
    fun respondToSwap(swapID: String, accept: Boolean, onResult: (Boolean, String) -> Unit) {
        val swapRef = db.collection("roomSwaps").document(swapID)

        swapRef.get().addOnSuccessListener { doc ->
            val swap = doc.toObject(RoomSwapRequest::class.java)
            if (swap != null && swap.status == "Pending") {
                if (accept) {
                    val reqBookingRef = db.collection("roomBookings").document(swap.requesterBookingID)
                    val targetBookingRef = db.collection("roomBookings").document(swap.targetBookingID)

                    reqBookingRef.get().addOnSuccessListener { reqDoc ->
                        targetBookingRef.get().addOnSuccessListener { targetDoc ->
                            val reqBooking = reqDoc.toObject(RoomBooking::class.java)
                            val targetBooking = targetDoc.toObject(RoomBooking::class.java)

                            if (reqBooking != null && targetBooking != null) {
                                db.runBatch { batch ->
                                    batch.update(reqBookingRef, "userId", targetBooking.userId)
                                    batch.update(targetBookingRef, "userId", reqBooking.userId)
                                    batch.update(swapRef, "status", "Accepted")
                                }.addOnSuccessListener {
                                    onResult(true, "Swap accepted! Room assignments successfully traded.")
                                }.addOnFailureListener { e ->
                                    onResult(false, "Transaction failed: ${e.localizedMessage}")
                                }
                            } else {
                                onResult(false, "Original booking documents data corrupted.")
                            }
                        }
                    }
                } else {
                    swapRef.update("status", "Rejected")
                        .addOnSuccessListener { onResult(true, "Swap request rejected.") }
                        .addOnFailureListener { e -> onResult(false, "Failed to reject: ${e.localizedMessage}") }
                }
            } else {
                onResult(false, "Invalid or already handled swap request.")
            }
        }
    }
    
    /**
     * Fetches all bookings belonging to the currently signed-in user.
     *
     * @param onResult Callback returning a list of [RoomBooking] objects.
     */
    fun getUserBookings(onResult: (List<RoomBooking>) -> Unit) {
        val currentEmail = auth.currentUser?.email 
        if (currentEmail == null) {
            onResult(emptyList())
            return
        }
    
        db.collection("roomBookings")
            .whereEqualTo("userId", currentEmail)
            .get()
            .addOnSuccessListener { documents ->
                val allBookings = documents.toObjects(RoomBooking::class.java)
                onResult(allBookings)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * Fetches ALL open swap market options posted by other users that are available to trade.
     *
     * @param currentUserId The ID of the current user to filter out their own requests.
     * @param onResult Callback returning a list of available [RoomSwapRequest] objects.
     */
    fun getAllAvailableSwaps(currentUserId: String, onResult: (List<RoomSwapRequest>) -> Unit) {
        db.collection("roomSwaps")
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener { documents ->
                val allSwaps = documents.toObjects(RoomSwapRequest::class.java)
                val publicMarketSwaps = allSwaps.filter { it.requesterUserId != currentUserId }
                onResult(publicMarketSwaps)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}