package com.example.fcams

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SetBookingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_booking)

        val roomID = intent.getStringExtra("ROOM_ID") ?: ""
        val userId = "CURRENT_USER_ID" // Replace with your authenticated user's ID

        val etDateTimeInput = findViewById<EditText>(R.id.etDateTimeInput)
        val btnConfirmBooking = findViewById<Button>(R.id.btnConfirmBooking)

        btnConfirmBooking.setOnClickListener {
            val dateTime = etDateTimeInput.text.toString().trim()
            if (dateTime.isNotEmpty()) {
                // Call your booking conflict check and save logic here
            }
        }
    }
}