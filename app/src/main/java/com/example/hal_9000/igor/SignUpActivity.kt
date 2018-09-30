package com.example.hal_9000.igor

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SignUpActivity : AppCompatActivity() {

    private val TAG = "SignUpActivity"

    private var db: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null

    private var etUsername: EditText? = null
    private var etEmail: EditText? = null
    private var etPassword: EditText? = null
    private var etBirthday: EditText? = null
    private var etGenre: EditText? = null
    private var btnCreateAccount: Button? = null
    private var mProgressBar: ProgressBar? = null

    private var email: String? = null
    private var password: String? = null
    private var username: String? = null
    private var birthday: String? = null
    private var genre: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        initialize()
    }

    private fun initialize() {
        etEmail = findViewById<View>(R.id.et_email) as EditText
        etPassword = findViewById<View>(R.id.et_password) as EditText
        etUsername = findViewById<View>(R.id.et_username) as EditText
        btnCreateAccount = findViewById<View>(R.id.btn_signUp) as Button
        mProgressBar = findViewById<View>(R.id.progressBar) as ProgressBar

        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        btnCreateAccount!!.setOnClickListener { signUp() }
    }

    private fun signUp() {

        val view = this.findViewById<View>(android.R.id.content)
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        email = etEmail?.text.toString()
        password = etPassword?.text.toString()
        username = etUsername?.text.toString()
        birthday = etBirthday?.text.toString()
        genre = etGenre?.text.toString()

        if (!checkData(email!!, password!!, username!!, birthday!!, genre!!)) {
            Toast.makeText(this, "Digite todos as informações corretamente", Toast.LENGTH_SHORT).show()
            return
        }

        mProgressBar!!.visibility = View.VISIBLE

        mAuth!!
                .createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    mProgressBar!!.visibility = View.INVISIBLE

                    if (task.isSuccessful) {
                        Log.d(TAG, "signUp: success")
                        Toast.makeText(this@SignUpActivity, "Cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                        storeUserData()
                        finish()
                    } else {
                        Log.w(TAG, "signUp: fail", task.exception)
                        Toast.makeText(this@SignUpActivity, "Falha ao cadastrar", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun storeUserData() {
        val user = HashMap<String, Any>()
        user["username"] = username!!
        user["birthday"] = birthday!!
        user["genre"] = genre!!
        user["email"] = email!!

        db!!.collection("users")
                .document(mAuth!!.currentUser!!.uid)
                .set(user)
                .addOnSuccessListener { Log.d(TAG, "Document added successfully") }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }

    private fun checkData(email: String, password: String, username: String, birthday: String, genre: String): Boolean {

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(username) || TextUtils.isEmpty(birthday)
                || TextUtils.isEmpty(genre))
            return false
        if (!email.contains('@'))
            return false
        if (password.length < 6)
            return false

        return true
    }
}
