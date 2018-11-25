package com.example.hal_9000.igor.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.R
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private val TAG = "LoginFragment"

    private lateinit var mProgressBar: ProgressBar
    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    private lateinit var mAuth: FirebaseAuth

    companion object {
        var username: String = ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.login_fragment, container, false)

        mProgressBar = view.findViewById(R.id.progressBar)
        btnLogin = view.findViewById(R.id.btn_login)
        btnSignUp = view.findViewById(R.id.btn_signUp)
        etEmail = view.findViewById(R.id.et_email)
        etPassword = view.findViewById(R.id.et_password)

        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser != null)
            exitFragment()

        btnLogin.setOnClickListener {

            mProgressBar.visibility = View.VISIBLE

            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (emailCorrect(email) && passwordCorrect(password)) {
                signIn(view, email, password)
            } else {
                Toast.makeText(context, "Informações inválidas", Toast.LENGTH_SHORT).show()
                mProgressBar.visibility = View.INVISIBLE
            }
        }

        btnSignUp.setOnClickListener {
            val action = LoginFragmentDirections.ActionLoginFragmentToSignupFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }

        return view
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
        Toast.makeText(context, "Autenticando...", Toast.LENGTH_SHORT).show()

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Login Successful")
                Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                exitFragment()
            } else {
                Log.d(TAG, "Login Error")
                Toast.makeText(context, "Erro ao realizar login", Toast.LENGTH_SHORT).show()
            }
            mProgressBar.visibility = View.INVISIBLE
        }
    }

    private fun exitFragment() {
        NavHostFragment.findNavController(this).navigate(
                R.id.homeFragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build())
    }
}
