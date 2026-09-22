package com.rustech.technician

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val loginError = findViewById<TextView>(R.id.loginError)
        val loginProgress = findViewById<ProgressBar>(R.id.loginProgress)

        btnLogin.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                loginError.text = "Punuin ang email at password."
                loginError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            loginError.visibility = View.GONE
            loginProgress.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    loginProgress.visibility = View.GONE
                    btnLogin.isEnabled = true

                    if (task.isSuccessful) {
                        goToWorkOrders()
                    } else {
                        val msg = when (task.exception) {
                            is FirebaseAuthInvalidCredentialsException,
                            is FirebaseAuthInvalidUserException -> "Mali ang email o password."
                            else -> "Login failed: " + (task.exception?.message ?: "unknown error")
                        }
                        loginError.text = msg
                        loginError.visibility = View.VISIBLE
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        // Already logged in from a previous session? Skip straight to the list.
        if (auth.currentUser != null) {
            goToWorkOrders()
        }
    }

    private fun goToWorkOrders() {
        startActivity(Intent(this, WorkOrdersActivity::class.java))
        finish()
    }
}
