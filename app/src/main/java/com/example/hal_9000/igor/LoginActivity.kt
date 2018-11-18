package com.example.hal_9000.igor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"

    private var mProgressBar: ProgressBar? = null

    private var mAuth: FirebaseAuth? = null

    companion object {
        var username: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .setPersistenceEnabled(true)
                .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        mAuth = FirebaseAuth.getInstance()
        mProgressBar = findViewById<View>(R.id.progressBar) as ProgressBar

        if (mAuth!!.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val btnLogin = findViewById<Button>(R.id.btn_login)
        btnLogin.setOnClickListener {

            mProgressBar!!.visibility = View.VISIBLE

            val view = this.findViewById<View>(android.R.id.content)
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            val email = et_email.text.toString()
            val password = et_password.text.toString()

            if (emailCorrect(email) && passwordCorrect(password)) {
                signIn(view, email, password)
            } else {
                Toast.makeText(this, "Informações inválidas", Toast.LENGTH_SHORT).show()
                mProgressBar!!.visibility = View.INVISIBLE
            }
        }

        val btnSignUp = findViewById<TextView>(R.id.btn_signUp)
        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun emailCorrect(email: String): Boolean {
        if (email.isNotEmpty() && email.contains("@")) {
            return true
        }
        Log.d(TAG, ": Email invalid")
        return false
    }

    private fun passwordCorrect(password: String): Boolean {
        if (password.isNotEmpty() && password.length >= 6) {
            return true
        }
        Log.d(TAG, ": Password invalid")
        return false
    }

    private fun signIn(view: View, email: String, password: String) {
        showMessage(view, "Autenticando...")

        mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, ": Login Successful")
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("id", mAuth!!.currentUser?.uid)
                startActivity(intent)
                finish()
            } else {
                Log.d(TAG, ": Login Error")
                showMessage(view, "Error: ${task.exception?.message}")
            }
            mProgressBar!!.visibility = View.INVISIBLE
        }
    }

    private fun showMessage(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show()
    }

    override fun onResume() {
        super.onResume()
        if (mAuth!!.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
