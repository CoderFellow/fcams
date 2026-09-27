package com.example.fcams

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /* Authenticates users using their email address and password, returning success or error messages via a callback.*/
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

    /* Queries the Firestore users collection by email to retrieve a user's role (e.g., admin, student, staff).*/
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

    /* Retrieves a list of all available rooms from the rooms collection.*/
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

    /* Checks for scheduling conflicts first, and if clear, generates a unique booking 
    ID and saves a new booking object with a "Pending" status to the roomBookings collection.*/
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

    /*Queries active bookings in the roomBookings collection for a specific room and time slot to prevent double-booking.*/
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

    /*Fetches all bookings tied to a specific room ID or retrieves bookings belonging to the currently signed-in user.*/
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

    // Request a Swap with time verification
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

    // Fetch pending swaps (Clean single copy)
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

    // Accept or Reject Swap with completed batch updates
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
    
    /*Fetches all bookings belonging to the currently signed-in user.*/
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
}
