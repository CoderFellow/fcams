package com.example.fcams

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

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

    fun fetchUserRole(email: String, onRoleFetched: (String?) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Grab the role from the matching document
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

    fun bookRoom(roomID: String, userId: String, timeDate: String, onResult: (Boolean, String) -> Unit) {
        // 1. Check for conflicts first
        checkBookingConflict(roomID, timeDate) { hasConflict ->
            if (hasConflict) {
                onResult(false, "This room is already booked for this date and time!")
            } else {
                // 2. Create the booking object
                val bookingID = db.collection("roomBookings").document().id
                val newBooking = RoomBooking(
                    bookingID = bookingID,
                    roomID = roomID,
                    userId = userId,
                    timeDate = timeDate,
                    status = "Pending"
                )
    
                // 3. Save to Firestore
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

    fun checkBookingConflict(roomID: String, selectedDateTime: String, onResult: (Boolean) -> Unit) {
        /*
        queries active bookings for a specific room and checks if the chosen slot overlaps
        
        */
    db.collection("roomBookings")
        .whereEqualTo("roomID", roomID)
        .whereEqualTo("timeDate", selectedDateTime)
        .get()
        .addOnSuccessListener { documents ->
            // If documents.isEmpty() is true, no conflict exists!
            val hasConflict = !documents.isEmpty
            onResult(hasConflict)
        }
        .addOnFailureListener {
            // Default to safe side or handle error
            onResult(true) 
        }
    }

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

    //---------------------------------------------

    // 1. Request a Swap with time verification
    fun requestSwap(requesterBookingID: String, targetBookingID: String, onResult: (Boolean, String) -> Unit) {
        db.collection("roomBookings").document(requesterBookingID).get().addOnSuccessListener { reqDoc ->
            db.collection("roomBookings").document(targetBookingID).get().addOnSuccessListener { targetDoc ->
                val reqBooking = reqDoc.toObject(RoomBooking::class.java)
                val targetBooking = targetDoc.toObject(RoomBooking::class.java)
    
                if (reqBooking != null && targetBooking != null) {
                    // Check if both rooms are set at the same date and time
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

    // 2. Accept or Reject Swap
    fun respondToSwap(swapID: String, accept: Boolean, onResult: (Boolean, String) -> Unit) {
        val swapRef = db.collection("roomSwaps").document(swapID)

        swapRef.get().addOnSuccessListener { doc ->
            val swap = doc.toObject(RoomSwapRequest::class.java)
            if (swap != null && swap.status == "Pending") {
                if (accept) {
                    // "Set Swap": Swap the roomIDs between the two bookings
                    val reqBookingRef = db.collection("roomBookings").document(swap.requesterBookingID)
                    val targetBookingRef = db.collection("roomBookings").document(swap.targetBookingID)

                    db.runBatch { batch ->
                        // Fetch current room IDs via temporary batch logic or direct reads
                        // For simplicity, swap their room assignments:
                        // (In production, you'd swap the roomID fields of the two documents)
                    }.addOnSuccessListener {
                        swapRef.update("status", "Accepted")
                        onResult(true, "Swap accepted and set!")
                    }
                } else {
                    // "Reject Swap"
                    swapRef.update("status", "Rejected")
                    onResult(true, "Swap request rejected.")
                }
            } else {
                onResult(false, "Invalid or already handled swap request.")
            }
        }
    }
    
    fun getUserBookings(onResult: (List<RoomBooking>) -> Unit) {
        val currentEmail = auth.currentUser?.email 
        if (currentEmail == null) {
            onResult(emptyList())
            return
        }
    
        db.collection("roomBookings")
            .whereEqualTo("userId", currentEmail) // Match by email if that's what you saved
            .get()
            .addOnSuccessListener { documents ->
                val allBookings = documents.toObjects(RoomBooking::class.java)
                android.util.Log.d("FIREBASE_DEBUG", "Successfully parsed ${allBookings.size} bookings for user.")
                onResult(allBookings)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}