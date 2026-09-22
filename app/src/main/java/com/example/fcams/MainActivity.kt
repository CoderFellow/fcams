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
    private val firebaseRepo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseRepo.loginUser(email, pass) { success, error ->
                    if (success) {
                        firebaseRepo.fetchUserRole(email) { role ->
                            when (role) {
                                "student" -> {
                                    Toast.makeText(this, "Welcome Student!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, StudentDashboardActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                "lecturer" -> {
                                    Toast.makeText(this, "Welcome Lecturer!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, LecturerDashboardActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                else -> {
                                    Toast.makeText(this, "Role not found for this user", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Fill in email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}