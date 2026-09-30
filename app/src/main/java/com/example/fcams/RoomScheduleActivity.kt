package com.example.fcams

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class RoomScheduleActivity : AppCompatActivity() {
    private val firebaseRepo = FirebaseRepository()
    private val calendarFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_schedule)

        // Add activity transition
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        val roomID = intent.getStringExtra("ROOM_ID") ?: ""
        val roomName = intent.getStringExtra("ROOM_NAME") ?: "Room"

        findViewById<TextView>(R.id.tvScheduleRoomTitle).text = "Schedule • $roomName"

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val lvBookedSlots = findViewById<ListView>(R.id.lvBookedSlots)

        var selectedDateStr = calendarFormat.format(Calendar.getInstance().time)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            selectedDateStr = calendarFormat.format(calendar.time)
            fetchAndDisplayBookings(roomID, selectedDateStr, lvBookedSlots)
        }

        fetchAndDisplayBookings(roomID, selectedDateStr, lvBookedSlots)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun fetchAndDisplayBookings(roomID: String, date: String, listView: ListView) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.email ?: ""

        firebaseRepo.getBookingsForRoom(roomID) { allBookings ->
            val occupiedSlots = allBookings.filter { it.timeDate.startsWith(date) }

            runOnUiThread {
                if (occupiedSlots.isEmpty()) {
                    listView.visibility = View.GONE
                    // Could show empty state here
                } else {
                    listView.visibility = View.VISIBLE
                }

                listView.adapter = BookingAdapter(this, occupiedSlots)

                listView.setOnItemClickListener { _, _, position, _ ->
                    val targetBooking = occupiedSlots[position]

                    if (targetBooking.userId == currentUserId) {
                        Toast.makeText(this, "This is already your booking!", Toast.LENGTH_SHORT).show()
                        return@setOnItemClickListener
                    }

                    firebaseRepo.getUserBookings { myBookings ->
                        if (myBookings.isEmpty()) {
                            runOnUiThread {
                                Toast.makeText(this, "You have no bookings to swap", Toast.LENGTH_LONG).show()
                            }
                            return@getUserBookings
                        }

                        val myBookingStrings = myBookings.map {
                            "Room ${it.roomID} • ${it.timeDate}"
                        }.toTypedArray()

                        runOnUiThread {
                            AlertDialog.Builder(this)
                                .setTitle("🔄 Request Room Swap")
                                .setMessage("Target: ${targetBooking.timeDate}\n\nSelect a booking to offer:")
                                .setItems(myBookingStrings) { _, which ->
                                    val chosen = myBookings[which]
                                    firebaseRepo.requestSwap(chosen.bookingID, targetBooking.bookingID) { success, msg ->
                                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
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

    // Custom Adapter for beautiful list items
    inner class BookingAdapter(
        private val context: Context,
        private val bookings: List<RoomBooking>
    ) : BaseAdapter() {

        override fun getCount() = bookings.size
        override fun getItem(position: Int) = bookings[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_booking, parent, false)

            val booking = bookings[position]

            view.findViewById<TextView>(R.id.tvBookingTime).text = booking.timeDate
            view.findViewById<TextView>(R.id.tvBookingOwner).text = booking.userId

            val statusBadge = view.findViewById<TextView>(R.id.tvBookingStatus)
            statusBadge.text = booking.status

            // Color code the status badge
            val bgColor = when (booking.status.lowercase()) {
                "confirmed" -> R.color.success
                "rejected" -> R.color.error
                else -> R.color.warning
            }
            val drawable = context.getDrawable(android.R.drawable.dialog_holo_light_frame)
            statusBadge.setBackgroundResource(R.drawable.status_badge)
            statusBadge.background.setTint(context.getColor(bgColor))

            return view
        }
    }
}