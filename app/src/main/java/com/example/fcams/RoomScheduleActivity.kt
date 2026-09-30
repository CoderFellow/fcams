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
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

/**
 * Activity responsible for displaying a room's schedule, managing calendar-based date selections,
 * and facilitating room swap requests between users.
 */
class RoomScheduleActivity : AppCompatActivity() {
    private val firebaseRepo = FirebaseRepository()
    private val calendarFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Initializes the activity, sets up views, reads intent data, and configures calendar date listeners.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state, if any.
     */
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

    /**
     * Fetches and displays booked slots for a specific room and date, handling item click actions
     * to trigger room swap requests.
     *
     * @param roomID The ID of the target room.
     * @param date The date string to filter bookings by.
     * @param listView The ListView used to render the booked slots.
     */
    private fun fetchAndDisplayBookings(roomID: String, date: String, listView: ListView) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.email ?: ""

        // Pull bookings for this room through the repository
        firebaseRepo.getBookingsForRoom(roomID) { allBookings ->
            val occupiedSlots = allBookings.filter { it.timeDate.startsWith(date) }

            val displayList = occupiedSlots.map { 
                "Time: ${it.timeDate} | Owner: ${it.userId} | Status: ${it.status}" 
            }

            runOnUiThread {
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
                listView.adapter = adapter
            }

            listView.setOnItemClickListener { _, _, position, _ ->
                val targetBooking = occupiedSlots[position]

                if (targetBooking.userId == currentUserId) {
                    Toast.makeText(this, "This is already your booking!", Toast.LENGTH_SHORT).show()
                    return@setOnItemClickListener
                }

                // Call the repository function cleanly
                firebaseRepo.getUserBookings { myBookings ->
                    if (myBookings.isEmpty()) {
                        runOnUiThread {
                            // Let's print out what email it's checking so we can verify
                            val currentEmail = FirebaseAuth.getInstance().currentUser?.email ?: "no_email"
                            Toast.makeText(this, "No bookings found for user: $currentEmail", Toast.LENGTH_LONG).show()
                        }
                        return@getUserBookings
                    }

                    val myBookingStrings = myBookings.map { "Room: ${it.roomID} at ${it.timeDate}" }.toTypedArray()
                    
                    runOnUiThread {
                        androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Request Room Swap\nTarget Slot: ${targetBooking.timeDate}\n\nSelect one of your bookings to offer:")
                            .setItems(myBookingStrings) { _, whichIndex ->
                                val chosenMyBooking = myBookings[whichIndex]
    
                                firebaseRepo.requestSwap(chosenMyBooking.bookingID, targetBooking.bookingID) { success, message ->
                                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            }
        }
    }
}