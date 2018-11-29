package com.example.hal_9000.igor.fragment

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment() {
    private val TAG = "SignUpFragment"

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etBirthday: EditText
    private lateinit var etGender: EditText
    private lateinit var btnCreateAccount: Button
    private lateinit var mProgressBar: ProgressBar

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var username: String
    private lateinit var birthday: String
    private lateinit var gender: String

    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth

    private var model: MainViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        etEmail = view.findViewById(R.id.et_email)
        etPassword = view.findViewById(R.id.et_password)
        etUsername = view.findViewById(R.id.et_username)
        etBirthday = view.findViewById(R.id.et_birthday)
        etGender = view.findViewById(R.id.et_gender)
        btnCreateAccount = view.findViewById(R.id.btn_signUp)
        mProgressBar = view.findViewById(R.id.progressBar)

        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        model = activity?.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        btnCreateAccount.setOnClickListener { signUp() }

        return view
    }

    private fun signUp() {

        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)

        email = etEmail.text.toString()
        password = etPassword.text.toString()
        username = etUsername.text.toString()
        birthday = etBirthday.text.toString()
        gender = etGender.text.toString()

        val errorMessage = checkData(email, password, username, birthday, gender)
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        mProgressBar.visibility = View.VISIBLE

        mAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    mProgressBar.visibility = View.INVISIBLE

                    if (task.isSuccessful) {
                        Log.d(TAG, "signUp: success")
                        Toast.makeText(context, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        storeUserData()
                        model?.setUsername(username)
                        NavHostFragment.findNavController(this).popBackStack()
                    } else {
                        Log.w(TAG, "signUp: fail", task.exception)
                        Toast.makeText(context, "Falha ao cadastrar", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun storeUserData() {
        val user = Usuario()
        user.username = username
        user.birthday = birthday
        user.gender = gender
        user.email = email
        user.uid = mAuth.currentUser!!.uid

        db.collection("users")
                .document(username)
                .set(user)
                .addOnSuccessListener {
                    Log.d(TAG, "Document added successfully")
                    val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(user.username)
                            .build()
                    mAuth.currentUser?.updateProfile(profileUpdates)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }

    private fun checkData(email: String, password: String, username: String, birthday: String, gender: String): String {

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(username) || TextUtils.isEmpty(birthday)
                || TextUtils.isEmpty(gender))
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
