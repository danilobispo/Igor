package com.example.hal_9000.igor.fragment


import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.hal_9000.igor.NavGraphDirections
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.adapters.AccountCharactersListAdapter
import com.example.hal_9000.igor.model.Personagem
import com.example.hal_9000.igor.model.Usuario
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore

class AccountFragment : Fragment() {
    private val TAG = "AccountFragment"

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvBirthday: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: AccountCharactersListAdapter
    private lateinit var db: FirebaseFirestore

    private lateinit var model: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        tvName = view.findViewById(R.id.tv_name)
        tvEmail = view.findViewById(R.id.tv_email)
        tvGender = view.findViewById(R.id.tv_gender)
        tvBirthday = view.findViewById(R.id.tv_birthday)
        progressBar = view.findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.rv_characters)

        db = FirebaseFirestore.getInstance()

        model = activity!!.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        if (model.getUser() == null) {
            progressBar.visibility = View.VISIBLE

            db.collection("users")
                    .whereEqualTo("username", model.getUsername())
                    .limit(1)
                    .get()
                    .addOnSuccessListener {
                        Log.d(TAG, "Document queried successfully")
                        progressBar.visibility = View.GONE
                        val user = it.documents[0].toObject(Usuario::class.java)!!
                        model.setUser(user)
                        setData()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error querying documents", e)
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Erro ao buscar usuário", Toast.LENGTH_SHORT).show()
                    }
        } else {
            setData()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        db = FirebaseFirestore.getInstance()

        val query = db.collection("characters")
                .whereEqualTo("nome", model.getUsername()!!)

        val options = FirestoreRecyclerOptions.Builder<Personagem>()
                .setQuery(query, Personagem::class.java)
                .build()

        adapter = AccountCharactersListAdapter(options) { character: Personagem -> characterItemClicked(character) }
        recyclerView.adapter = adapter

        return view
    }

    private fun setData() {
        val user = model.getUser()!!
        tvName.text = user.username
        tvEmail.text = user.email
        tvGender.text = user.gender
        tvBirthday.text = user.birthday
    }

    private fun characterItemClicked(character: Personagem) {
        val action = NavGraphDirections.actionGlobalCharacterProfileFragment(character)
        action.setReadOnly(true)
        Navigation.findNavController(activity!!, R.id.nav_host).navigate(action)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
