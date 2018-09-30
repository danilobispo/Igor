package com.example.hal_9000.igor

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.example.hal_9000.igor.R.id.*
import com.example.hal_9000.igor.fragment.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar_layout.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupMenu()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        nav_view.setNavigationItemSelectedListener(this)
        setupFragment(savedInstanceState)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            item_aventuras -> {
                trocarDeFragment(AventuraFragment())
                return true
            }
            item_livros -> {
                trocarDeFragment(LivrosFragment())
                return true
            }
            item_conta -> {
                trocarDeFragment(ContaFragment())
                return true
            }
            item_notificacoes -> {
                trocarDeFragment(NotificacoesFragment())
                return true
            }
            item_configuracoes -> {
                trocarDeFragment(ConfiguracoesFragment())
                return true
            }
            item_logout -> {
                val mAuth = FirebaseAuth.getInstance()
                if (mAuth!!.currentUser != null) {
                    mAuth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun setupMenu() {
        // toolbar cast como View
        val drawerToggle: ActionBarDrawerToggle = object :
                ActionBarDrawerToggle(this, drawer, toolbar as Toolbar, R.string.open_drawer, R.string.close_drawer) {
            // TODO: Adicionar algo aqui se quisermos algo mais elaborado no drawer
            // (Provavelmente a gente não quer)

        }

        drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    fun setupFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            this.supportFragmentManager.beginTransaction().replace(R.id.content_frame, HomeFragment())
                    .addToBackStack(null).commit()
        }
    }

    fun trocarDeFragment(supportFragment: Fragment){
        this.supportFragmentManager.beginTransaction().replace(R.id.content_frame, supportFragment)
                .addToBackStack(null).commit()
    }
}
