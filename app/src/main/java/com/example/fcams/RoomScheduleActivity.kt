package com.example.fcams

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RoomScheduleActivity : AppCompatActivity() {

    private val firebaseRepo = FirebaseRepository()
    private val calendarFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // In-memory data store holding all month bookings to filter seamlessly on the fly
    private var masterMonthBookings: List<RoomBooking> = emptyList()
    private var activeTimeFilter: String = "ALL" // "ALL", "MORNING", "AFTERNOON"
    private var currentRoomID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_schedule)

        currentRoomID = intent.getStringExtra("ROOM_ID") ?: ""
        val roomName = intent.getStringExtra("ROOM_NAME") ?: "Room"
        
        findViewById<TextView>(R.id.tvScheduleRoomTitle).text = "Schedule for $roomName"

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val lvBookedSlots = findViewById<ListView>(R.id.lvBookedSlots)

        // Filter button binding references
        val btnAll = findViewById<Button>(R.id.btnFilterAll)
        val btnMorning = findViewById<Button>(R.id.btnFilterMorning)
        val btnAfternoon = findViewById<Button>(R.id.btnFilterAfternoon)

        // Initial launch downloads all room records to map out the entire month immediately
        fetchMasterMonthSchedule(currentRoomID, lvBookedSlots)

        // Calendar selection focuses filters instantly on a specific selected day
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            val selectedDateStr = calendarFormat.format(calendar.time)
            
            // Highlight focused selection context
            findViewById<TextView>(R.id.tvStreamHeader).text = "Schedule Stream: $selectedDateStr"
            renderFilteredList(masterMonthBookings.filter { it.timeDate.startsWith(selectedDateStr) }, lvBookedSlots)
        }

        // --- FILTER CHIP ROW CONTROLLERS ---
        btnAll.setOnClickListener {
            activeTimeFilter = "ALL"
            updateButtonColors(btnAll, btnMorning, btnAfternoon)
            renderFilteredList(masterMonthBookings, lvBookedSlots)
            findViewById<TextView>(R.id.tvStreamHeader).text = "30-Day Master Schedule Stream"
        }

        btnMorning.setOnClickListener {
            activeTimeFilter = "MORNING"
            updateButtonColors(btnMorning, btnAll, btnAfternoon)
            // Filter slots containing early hours (e.g., matching AM blocks or specific hour digits)
            val morningList = masterMonthBookings.filter { extractHour(it.timeDate) < 12 }
            renderFilteredList(morningList, lvBookedSlots)
        }

        btnAfternoon.setOnClickListener {
            activeTimeFilter = "AFTERNOON"
            updateButtonColors(btnAfternoon, btnAll, btnMorning)
            val afternoonList = masterMonthBookings.filter { extractHour(it.timeDate) >= 12 }
            renderFilteredList(afternoonList, lvBookedSlots)
        }
    }

    private fun fetchMasterMonthSchedule(roomID: String, listView: ListView) {
        firebaseRepo.getBookingsForRoom(roomID) { allBookings ->
            // Sort bookings chronologically so the month reads sequentially
            masterMonthBookings = allBookings.sortedBy { it.timeDate }
            runOnUiThread {
                renderFilteredList(masterMonthBookings, listView)
            }
        }
    }

    private fun renderFilteredList(bookings: List<RoomBooking>, listView: ListView) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.email ?: ""
        
        // Map data arrays cleanly into scannable row elements
        val displayList = bookings.map { 
            "📅 Slot: ${it.timeDate}\n👤 User: ${it.userId}  |  Status: ${it.status.uppercase()}"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        listView.adapter = adapter

        // Re-bind click interaction handlers safely for swaps
        listView.setOnItemClickListener { _, _, position, _ ->
            val targetBooking = bookings[position]

            if (targetBooking.userId == currentUserId) {
                Toast.makeText(this, "This is already your booking!", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }

            firebaseRepo.getUserBookings { myBookings ->
                if (myBookings.isEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this, "You have no active bookings to swap with!", Toast.LENGTH_SHORT).show()
                    }
                    return@getUserBookings
                }

                val myBookingStrings = myBookings.map { "Room: ${it.roomID} at ${it.timeDate}" }.toTypedArray()
                
                runOnUiThread {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Initiate Swap Trade Offer")
                        .setMessage("Target: Room ${targetBooking.roomID} (${targetBooking.timeDate})\n\nSelect your booking to trade:")
                        .setItems(myBookingStrings) { _, whichIndex ->
                            val chosenMyBooking = myBookings[whichIndex]
                            firebaseRepo.requestSwap(chosenMyBooking.bookingID, targetBooking.bookingID) { success, message ->
                                Toast.makeText(this@RoomScheduleActivity, message, Toast.LENGTH_LONG).show()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
    }

    private fun updateButtonColors(active: Button, inactive1: Button, inactive2: Button) {
        active.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D32F2F")))
        active.setTextColor(android.graphics.Color.WHITE)
        
        inactive1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E0E0E2")))
        inactive1.setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
        
        inactive2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E0E0E2")))
        inactive2.setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
    }

    private fun extractHour(dateTimeStr: String): Int {
        return try {
            // Evaluates formats like "2026-09-27 14:00" safely
            val timePart = dateTimeStr.split(" ")[1]
            timePart.split(":")[0].toInt()
        } catch (e: Exception) {
            12 // Fallback default baseline
        }
    }
}
