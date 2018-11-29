package com.example.hal_9000.igor

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.navigation.NavOptions
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.hal_9000.igor.adapters.ListDrawerAdapter
import com.example.hal_9000.igor.model.Categoria
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar_layout.*
import android.arch.lifecycle.ViewModelProviders
import com.example.hal_9000.igor.viewmodel.MainViewModel


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var mAuth: FirebaseAuth

    private val listItems = arrayListOf<Categoria>()

    private lateinit var model: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .setPersistenceEnabled(true)
                .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        model = this.run {
            ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser == null) {
            nav_host.findNavController().navigate(
                    R.id.loginFragment,
                    null,
                    NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build())
        } else {
            model.setUsername(FirebaseAuth.getInstance().currentUser?.displayName.toString())
        }

        setupMenu()

        NavHostFragment.findNavController(nav_host).addOnNavigatedListener { _, destination ->
            when (destination.id) {
                R.id.loginFragment -> {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    toolbar.visibility = View.GONE
                }
                R.id.signUpFragment -> {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    toolbar.visibility = View.GONE
                }
                else -> {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    toolbar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupMenu() {

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val drawerToggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer)

        drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        listItems.add(Categoria("Aventuras", true, false))
        listItems.add(Categoria("Livros", false, false))
        listItems.add(Categoria("Conta", false, false))
        listItems.add(Categoria("Notificações", false, false))
        listItems.add(Categoria("Configurações", false, false))
        listItems.add(Categoria("Log Out", false, false))

        val adapter = ListDrawerAdapter(this, listItems)
        val listView = findViewById<ListView>(R.id.lst_menu_items)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->

            val navController = findNavController(this, R.id.nav_host)
            val navBuilder = NavOptions.Builder()
            val navOptions = navBuilder.setPopUpTo(R.id.homeFragment, false).build()

            when (position) {
                0 -> {
                    navController.navigate(R.id.homeFragment, null, navOptions)
                }
                1 -> {
                    navController.navigate(R.id.booksFragment, null, navOptions)
                }
                2 -> {
                    navController.navigate(R.id.accountFragment, null, navOptions)
                }
                3 -> {
                    navController.navigate(R.id.notificacoesFragment, null, navOptions)
                }
                4 -> {
                    navController.navigate(R.id.configuracoesFragment, null, navOptions)
                }
                5 -> {
                    if (mAuth.currentUser != null) {
                        mAuth.signOut()
                        nav_host.findNavController().navigate(
                                R.id.loginFragment,
                                null,
                                NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build())
                    }
                }
            }

            listItems[0] = Categoria(listItems[0].nome, false, listItems[0].notification)
            listItems[1] = Categoria(listItems[1].nome, false, listItems[1].notification)
            listItems[2] = Categoria(listItems[2].nome, false, listItems[2].notification)
            listItems[3] = Categoria(listItems[3].nome, false, listItems[3].notification)
            listItems[4] = Categoria(listItems[4].nome, false, listItems[4].notification)
            listItems[5] = Categoria(listItems[5].nome, false, listItems[5].notification)

            listItems[position] = Categoria(listItems[position].nome, true, false)
            adapter.notifyDataSetChanged()

            if (!listItems[0].notification!! && !listItems[1].notification!! && !listItems[2].notification!! && !listItems[3].notification!! && !listItems[4].notification!!)
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

            drawer.closeDrawer(GravityCompat.START)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_editar -> {
                Toast.makeText(this, "Editar", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_ordenar -> {
                Toast.makeText(this, "Ordenar", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setNotificationIndicator(categoria: String) {

        val adapter = ListDrawerAdapter(this, listItems)

        when (categoria) {
            "Aventuras" -> {
                listItems[0] = Categoria(listItems[0].nome, listItems[0].selected, true)
            }
            "Livros" -> {
                listItems[1] = Categoria(listItems[1].nome, listItems[1].selected, true)
            }
            "Conta" -> {
                listItems[2] = Categoria(listItems[2].nome, listItems[2].selected, true)
            }
            "Notificações" -> {
                listItems[3] = Categoria(listItems[3].nome, listItems[3].selected, true)
            }
            "Configurações" -> {
                listItems[4] = Categoria(listItems[4].nome, listItems[4].selected, true)
            }
            else -> return
        }
        adapter.notifyDataSetChanged()
        supportActionBar?.setHomeAsUpIndicator(R.drawable.nav_menu)
    }
}
