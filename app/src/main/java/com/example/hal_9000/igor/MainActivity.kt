package com.example.hal_9000.igor

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.View
import com.example.hal_9000.igor.model.Categoria
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar_layout.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupMenu()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        val listaDeCategorias: ArrayList<Categoria> = montarListaDeOpcoes(this)

//        expandableListView.setAdapter()
        setupFragment(savedInstanceState)
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

    fun montarListaDeOpcoes(context: Context) : ArrayList<Categoria> {
        val listCategoria = ArrayList<Categoria>()
        listCategoria.add(Categoria(
                "Aventuras",
                ContextCompat.getDrawable(context, R.drawable.aventuras_icone),
                null))

        listCategoria.add(Categoria(
                "Livros",
                ContextCompat.getDrawable(context, R.drawable.livros_icone),
                null))

        listCategoria.add(Categoria(
                "Conta",
                ContextCompat.getDrawable(context, R.drawable.conta_icone),
                null))

        listCategoria.add(Categoria(
                "Notificações",
                ContextCompat.getDrawable(context, R.drawable.notificacoes_icone),
                null))

        listCategoria.add(Categoria(
                "Configurações",
                ContextCompat.getDrawable(context, R.drawable.configuracoes_icone),
                null))

        return listCategoria
    }
}
