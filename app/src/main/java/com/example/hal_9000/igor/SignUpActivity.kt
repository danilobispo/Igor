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
import com.example.hal_9000.igor.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore


class SignUpActivity : AppCompatActivity() {

    private val TAG = "SignUpActivity"

    private var db: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etBirthday: EditText
    private lateinit var etGenre: EditText
    private lateinit var btnCreateAccount: Button
    private lateinit var mProgressBar: ProgressBar

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var username: String
    private lateinit var birthday: String
    private lateinit var genre: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        initialize()
    }

    private fun initialize() {
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etUsername = findViewById(R.id.et_username)
        etBirthday = findViewById(R.id.et_birthday)
        etGenre = findViewById(R.id.et_genre)
        btnCreateAccount = findViewById(R.id.btn_signUp)
        mProgressBar = findViewById(R.id.progressBar)

        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        btnCreateAccount.setOnClickListener { signUp() }
    }

    private fun signUp() {

        val view = this.findViewById<View>(android.R.id.content)
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        email = etEmail.text.toString()
        password = etPassword.text.toString()
        username = etUsername.text.toString()
        birthday = etBirthday.text.toString()
        genre = etGenre.text.toString()

        val errorMessage = checkData(email, password, username, birthday, genre)
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        mProgressBar.visibility = View.VISIBLE

        mAuth!!
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    mProgressBar.visibility = View.INVISIBLE

                    if (task.isSuccessful) {
                        Log.d(TAG, "signUp: success")
                        Toast.makeText(this@SignUpActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        storeUserData()
                        LoginActivity.username = username
                        finish()
                    } else {
                        Log.w(TAG, "signUp: fail", task.exception)
                        Toast.makeText(this@SignUpActivity, "Falha ao cadastrar", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun storeUserData() {
        val user = Usuario()
        user.username = username
        user.birthday = birthday
        user.genre = genre
        user.email = email
        user.uid = mAuth!!.currentUser!!.uid

        db!!.collection("users")
                .document(username)
                .set(user)
                .addOnSuccessListener {
                    Log.d(TAG, "Document added successfully")
                    val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(user.username)
                            .build()
                    mAuth?.currentUser?.updateProfile(profileUpdates)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }

    private fun checkData(email: String, password: String, username: String, birthday: String, genre: String): String {

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(username) || TextUtils.isEmpty(birthday)
                || TextUtils.isEmpty(genre))
            return "Preencha todas as informações"
        if (!email.contains('@'))
            return "E-mail inválido"
        if (password.length < 6)
            return "A senha deve conter ao menos 6 caracteres"
        if (username.toLowerCase() == "system" || username.toLowerCase() == "admin")
            return "Nome de usuário inválido"

        return ""
    }
}
