package com.example.hal_9000.igor

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.hal_9000.igor.adapters.DrawerListAdapter
import com.example.hal_9000.igor.model.DrawerItem
import com.example.hal_9000.igor.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar_layout.*


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var lvDrawer: ListView
    private lateinit var navController: NavController
    private lateinit var mAuth: FirebaseAuth

    private val drawerItemList = arrayListOf<DrawerItem>()
    private var lastItemSelected = 0

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

        lvDrawer = findViewById(R.id.lst_menu_items)

        navController = findNavController(this, R.id.nav_host)

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
            onNavigatedListener(destination)
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

        drawerItemList.add(DrawerItem(getString(R.string.str_aventuras), true, false))
        drawerItemList.add(DrawerItem(getString(R.string.str_livros), false, false))
        drawerItemList.add(DrawerItem(getString(R.string.str_conta), false, false))
        drawerItemList.add(DrawerItem(getString(R.string.str_configuracoes), false, false))
        drawerItemList.add(DrawerItem(getString(R.string.str_notificacoes), false, false))
        drawerItemList.add(DrawerItem(getString(R.string.str_logout), false, false))

        val adapter = DrawerListAdapter(this, drawerItemList)
        lvDrawer.adapter = adapter

        lvDrawer.setOnItemClickListener { _, _, position, _ ->
            val navBuilder = NavOptions.Builder()
            val navOptions = navBuilder.setPopUpTo(R.id.homeFragment, false).build()

            when (position) {
                0 -> navController.navigate(R.id.homeFragment, null, navOptions)
                1 -> navController.navigate(R.id.booksFragment, null, navOptions)
                2 -> navController.navigate(R.id.accountFragment, null, navOptions)
                3 -> navController.navigate(R.id.notificacoesFragment, null, navOptions)
                4 -> navController.navigate(R.id.configuracoesFragment, null, navOptions)
                5 -> {
                    if (mAuth.currentUser != null) {
                        mAuth.signOut()
                        model.clearUser()
                        model.clearUsername()
                        nav_host.findNavController().navigate(
                                R.id.loginFragment,
                                null,
                                NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build())
                    }
                }
            }

            for (item in drawerItemList)
                item.selected = false
            drawerItemList[position].selected = true
            drawerItemList[position].notification = false
            adapter.notifyDataSetChanged()

            if (!drawerItemList[0].notification && !drawerItemList[1].notification && !drawerItemList[2].notification && !drawerItemList[3].notification && !drawerItemList[4].notification)
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

            drawer.closeDrawer(GravityCompat.START)
        }
    }

    private fun onNavigatedListener(destination: NavDestination) {
        Log.d(TAG, "onNavigatedListener: ${destination.label}")
        when (destination.id) {
            R.id.loginFragment -> {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                toolbar.visibility = View.GONE
                lastItemSelected = -1
                return
            }
            R.id.signUpFragment -> {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                toolbar.visibility = View.GONE
                lastItemSelected = -1
                return
            }
            R.id.homeFragment -> {
                model.clearAdventure()
                model.clearSessionId()
                model.clearIsMaster()
            }
        }

        val idx = when (destination.id) {
            R.id.booksFragment -> 1
            R.id.accountFragment -> 2
            R.id.notificacoesFragment -> 3
            R.id.configuracoesFragment -> 4
            R.id.characterProfileFragment -> lastItemSelected
            R.id.newCharacterFragment -> lastItemSelected
            R.id.itemProfileFragment -> lastItemSelected
            R.id.newItemFragment -> lastItemSelected
            else -> 0
        }

        if (idx == lastItemSelected) return

        if (lastItemSelected == -1) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            toolbar.visibility = View.VISIBLE
        }

        for (item in drawerItemList)
            item.selected = false
        drawerItemList[idx].selected = true
        drawerItemList[idx].notification = false
        (lvDrawer.adapter as DrawerListAdapter).notifyDataSetChanged()

        lastItemSelected = idx
    }

    fun setNotificationIndicator(drawerItemName: String) {
        when (drawerItemName) {
            "adventures" -> drawerItemList[0] =
                    DrawerItem(drawerItemList[0].name, drawerItemList[0].selected, true)
            "books" -> drawerItemList[1] =
                    DrawerItem(drawerItemList[1].name, drawerItemList[1].selected, true)
            "account" -> drawerItemList[2] =
                    DrawerItem(drawerItemList[2].name, drawerItemList[2].selected, true)
            "notifications" -> drawerItemList[3] =
                    DrawerItem(drawerItemList[3].name, drawerItemList[3].selected, true)
            "settings" -> drawerItemList[4] =
                    DrawerItem(drawerItemList[4].name, drawerItemList[4].selected, true)
            else -> return
        }
        (lvDrawer.adapter as DrawerListAdapter).notifyDataSetChanged()
        supportActionBar?.setHomeAsUpIndicator(R.drawable.nav_menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_editar ->
                Toast.makeText(this, "Editar", Toast.LENGTH_SHORT).show()
            R.id.menu_ordenar ->
                Toast.makeText(this, "Ordenar", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }
}
