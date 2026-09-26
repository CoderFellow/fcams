package com.example.fcams

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    /* 
    The main entry-point screen of the app.
    */

    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
    /* 
    The primary lifecycle method called when the activity is first created, 
    responsible for setting up the UI and user interaction logic.
    */

        // 
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // The activity starts, configures itself to look modern with edge-to-edge
         window framing, and loads the user interface*/
        setContentView(R.layout.activity_main)

        // ensures the root layout adds safe padding around system status and navigation bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // From the main_activity layout file using their IDs.
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Set a click listener to handle login events when the button is tapped
        btnLogin.setOnClickListener {

            Extract and clean input text from the text fields
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            // Validate that fields are not left blank
            if (email.isNotEmpty() && pass.isNotEmpty()) {

                // Call Firebase repository to authenticate user credentials
                firebaseRepo.loginUser(email, pass) { success, error ->
                    if (success) {

                        // If login succeeds, fetch the user's specific role from Firestore
                        firebaseRepo.fetchUserRole(email) { role ->
                            when (role) {

                                // Route student users to the student dashboard
                                "student" -> {
                                    Toast.makeText(this, "Welcome Student!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, StudentDashboardActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                                // Route lecturer users to the lecturer dashboard
                                "lecturer" -> {
                                    Toast.makeText(this, "Welcome Lecturer!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, LecturerDashboardActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                                // Handle cases where the role field is missing or undefined
                                else -> {
                                    Toast.makeText(this, "Role not found for this user", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } 

                    // Show error message if Firebase authentication fails
                    else {
                        Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                }
            } 
            
            // Prompt user if input boxes are empty
            else {
                Toast.makeText(this, "Fill in email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}