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

class RoomScheduleActivity : AppCompatActivity() {
/*
An activity that displays a calendar view and a list of booked slots for a specific room, 
allowing users to inspect or initiate room swaps
*/

    // Connects to Firebase to fetch room bookings and handle swaps
    private val firebaseRepo = FirebaseRepository()

    // Defines the date string format 
    private val calendarFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
    /*
    Initializes the layout, extracts intent extras (ROOM_ID, ROOM_NAME), sets up the calendar 
    listener, and loads initial schedules.
    */


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_schedule)

        // Retrieve room data passed via Intent from the dashboard
        val roomID = intent.getStringExtra("ROOM_ID") ?: ""
        val roomName = intent.getStringExtra("ROOM_NAME") ?: "Room"
        
        val tvTitle = findViewById<TextView>(R.id.tvScheduleRoomTitle)
        tvTitle.text = "Schedule for $roomName"

        // Default selected date string to today's current date
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
    /*
    Queries bookings for the room, filters them by date, updates the list view, 
    and manages click events to handle room swapping.
    */

        val currentUserId = FirebaseAuth.getInstance().currentUser?.email ?: ""

        // Pull bookings for this room through the repository.
        firebaseRepo.getBookingsForRoom(roomID) { allBookings ->

            // Filter bookings that match the currently selected date string.
            val occupiedSlots = allBookings.filter { it.timeDate.startsWith(date) }

            // Display date string.
            val displayList = occupiedSlots.map { 
                "Time: ${it.timeDate} | Owner: ${it.userId} | Status: ${it.status}" 
            }
            
            // Update the UI ListView adapter on the main UI thread
            runOnUiThread {
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
                listView.adapter = adapter
            }

            // Set a click listener on items to handle potential room swaps
            listView.setOnItemClickListener { _, _, position, _ ->
                val targetBooking = occupiedSlots[position]

                // Prevent users from swapping with their own existing booking
                if (targetBooking.userId == currentUserId) {
                    Toast.makeText(this, "This is already your booking!", Toast.LENGTH_SHORT).show()
                    return@setOnItemClickListener
                }

                // Fetch the current user's bookings to choose one for a trade offer
                firebaseRepo.getUserBookings { myBookings ->
                    if (myBookings.isEmpty()) {
                        runOnUiThread {

                            // Print out what email it's checking so we can verify.
                            val currentEmail = FirebaseAuth.getInstance().currentUser?.email ?: "no_email"
                            Toast.makeText(this, "No bookings found for user: $currentEmail", Toast.LENGTH_LONG).show()
                        }
                        return@getUserBookings
                    }

                    val myBookingStrings = myBookings.map { "Room: ${it.roomID} at ${it.timeDate}" }.toTypedArray()
                    
                    // Display a selection dialog to let the user pick which of their bookings to offer.
                    runOnUiThread {
                        androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Request Room Swap\nTarget Slot: ${targetBooking.timeDate}\n\nSelect one of your bookings to offer:")
                            .setItems(myBookingStrings) { _, whichIndex ->
                                val chosenMyBooking = myBookings[whichIndex]

                                // Send the swap request via repository
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