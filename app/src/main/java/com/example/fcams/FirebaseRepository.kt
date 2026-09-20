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
}