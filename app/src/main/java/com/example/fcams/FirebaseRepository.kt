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

    fun bookRoom(roomID: String, userId: String, timeDate: String, onResult: (Boolean, String?) -> Unit) {
        val bookingId = db.collection("room_bookings").document().id
        val booking = RoomBooking(
            bookingID = bookingId,
            roomID = roomID,
            userId = userId,
            timeDate = timeDate,
            status = "Pending"
        )

        db.collection("room_bookings").document(bookingId)
            .set(booking)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
            }
    }
}