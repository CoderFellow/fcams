package com.example.fcams

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RoomScheduleActivity : AppCompatActivity() {
    private val firebaseRepo = FirebaseRepository()
    private val calendarFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_schedule)

        val roomID = intent.getStringExtra("ROOM_ID") ?: ""
        val roomName = intent.getStringExtra("ROOM_NAME") ?: "Room"
        
        val tvTitle = findViewById<TextView>(R.id.tvScheduleRoomTitle)
        tvTitle.text = "Schedule for $roomName"

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val lvBookedSlots = findViewById<ListView>(R.id.lvBookedSlots)

        // Default to today's date string
        var selectedDateStr = calendarFormat.format(Calendar.getInstance().time)

        // Listen for user picking a date on the calendar
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            selectedDateStr = calendarFormat.format(calendar.time)
            fetchAndDisplayBookings(roomID, selectedDateStr, lvBookedSlots)
        }

        // Load initial schedule for today
        fetchAndDisplayBookings(roomID, selectedDateStr, lvBookedSlots)
    }

    private fun fetchAndDisplayBookings(roomID: String, date: String, listView: ListView) {
        firebaseRepo.getBookingsForRoom(roomID) { allBookings ->
            // Filter bookings that match the selected date string (assuming timeDate starts with yyyy-MM-dd)
            val occupiedSlots = allBookings
                .filter { it.timeDate.startsWith(date) }
                .map { "${it.timeDate} - Status: ${it.status} (User: ${it.userId})" }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, occupiedSlots)
            listView.adapter = adapter
        }
    }
}