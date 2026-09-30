package com.example.fcams

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Activity responsible for letting users input a date and time to reserve a specific room.
 */
class SetBookingActivity : AppCompatActivity() {

    // Connects to Firebase to handle conflict checks and save the booking.
    private val firebaseRepo = FirebaseRepository()

    /**
     * Initializes the layout, extracts the room ID from the intent, 
     * grabs the user's email, and sets up the confirmation button click listener.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_booking)

        // Retrieve the Room ID passed via Intent from the dashboard.
        val roomID = intent.getStringExtra("ROOM_ID") ?: ""
        
        // Grab the current user's email to associate ownership with the booking
        val userId = FirebaseAuth.getInstance().currentUser?.email ?: "unknown_user"

        // Link UI elements from the layout XML.
        val etDateTimeInput = findViewById<EditText>(R.id.etDateTimeInput)
        val btnConfirmBooking = findViewById<Button>(R.id.btnConfirmBooking)

        // Set click listener to handle booking confirmation attempts.
        btnConfirmBooking.setOnClickListener {

            // Extract and clean the date/time input text.
            val dateTime = etDateTimeInput.text.toString().trim()

            if (dateTime.isNotEmpty() && roomID.isNotEmpty()) {
                
                // Call your repository function that checks for conflicts and saves to Firestore
                firebaseRepo.bookRoom(roomID, userId, dateTime) { success, message ->
                    
                    // Ensure UI updates and Toast messages run safely on the main thread.
                    runOnUiThread {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                        if (success) {
                            
                            // Close the booking screen and return to the dashboard on success.
                            finish()
                        }
                    }
                }
            } 
            // Prompt user if the date/time field is left blank
            else {
                Toast.makeText(this, "Please enter a valid date and time", Toast.LENGTH_SHORT).show()
            }
        }
    }
}