package com.example.hal_9000.igor

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import com.example.hal_9000.igor.R.color.drawer_item
import com.example.hal_9000.igor.R.id.*
import com.example.hal_9000.igor.fragment.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar_layout.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupMenu()

        setupFragment(savedInstanceState)
    }

    fun setupMenu() {

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        // toolbar cast como View
        val drawerToggle: ActionBarDrawerToggle = object :
                ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer) {
            // TODO: Adicionar algo aqui se quisermos algo mais elaborado no drawer
            // (Provavelmente a gente não quer)
        }

        drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            item_aventuras -> {
                trocarDeFragment(AventuraFragment())
            }
            item_livros -> {
                trocarDeFragment(LivrosFragment())
            }
            item_conta -> {
                trocarDeFragment(ContaFragment())
                val img = findViewById<ImageView>(R.id.conta_img)
                img.setImageResource(R.drawable.aventuras_icone)
            }
            item_notificacoes -> {
                trocarDeFragment(NotificacoesFragment())
            }
            item_configuracoes -> {
                trocarDeFragment(ConfiguracoesFragment())
            }
            item_logout -> {
                val mAuth = FirebaseAuth.getInstance()
                if (mAuth!!.currentUser != null) {
                    mAuth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
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

    fun setupFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            this.supportFragmentManager.beginTransaction().replace(R.id.content_frame, HomeFragment())
                    .addToBackStack(null).commit()
        }
    }

    fun trocarDeFragment(supportFragment: Fragment) {
        this.supportFragmentManager.beginTransaction().replace(R.id.content_frame, supportFragment)
                .addToBackStack(null).commit()
    }
}
