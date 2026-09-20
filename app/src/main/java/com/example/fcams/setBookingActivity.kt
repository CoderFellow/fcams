package com.example.fcams

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SetBookingActivity : AppCompatActivity() {
    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_booking)

        val roomID = intent.getStringExtra("ROOM_ID") ?: ""
        // Grab the current user's ID/email to associate with the booking
        val userId = FirebaseAuth.getInstance().currentUser?.email ?: "unknown_user"

        val etDateTimeInput = findViewById<EditText>(R.id.etDateTimeInput)
        val btnConfirmBooking = findViewById<Button>(R.id.btnConfirmBooking)

        btnConfirmBooking.setOnClickListener {
            val dateTime = etDateTimeInput.text.toString().trim()

            if (dateTime.isNotEmpty() && roomID.isNotEmpty()) {
                // Call your repository function that checks for conflicts and saves to Firestore
                firebaseRepo.bookRoom(roomID, userId, dateTime) { success, message ->
                    // Make sure UI updates/Toasts happen smoothly
                    runOnUiThread {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                        if (success) {
                            // Close the booking screen and return to the dashboard on success
                            finish()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a valid date and time", Toast.LENGTH_SHORT).show()
            }
        }
    }
}