package com.example.hal_9000.igor.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.NavHostFragment
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Atributo
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.model.Personagem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_new_character.*
import kotlinx.android.synthetic.main.fragment_new_character.view.*


class NewCharacterFragment : Fragment() {

    private val TAG = "NewCharacterFragment"
    private var aventura: Aventura? = null

    lateinit var etNome: EditText
    lateinit var etClasse: EditText
    lateinit var etDescricao: EditText
    lateinit var etHealth: EditText
    lateinit var tlAtributos: TableLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_character, container, false)

        aventura = NewCharacterFragmentArgs.fromBundle(arguments).aventura
        val personagem = NewCharacterFragmentArgs.fromBundle(arguments).personagem

        etNome = view.findViewById(R.id.et_nome)
        etClasse = view.findViewById(R.id.et_classe)
        etDescricao = view.findViewById(R.id.et_descricao)
        etHealth = view.findViewById(R.id.et_hp)
        tlAtributos = view.findViewById(R.id.tl_atributos)

        if (personagem.aventuraId.isNotEmpty())
            completeFields(personagem)

        view.btn_adicionar_atributo.setOnClickListener {

            if (et_novo_atributo_titulo.text.isEmpty() || et_novo_atributo_valor.text.isEmpty()) {
                Toast.makeText(context, "Insira ambos os dados do atributo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tr = TableRow(context)
            tr.layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)

            val tvAtributoNome = TextView(context)
            tvAtributoNome.text = et_novo_atributo_titulo.text.toString()
            tvAtributoNome.setPadding(5, 5, 5, 5)
            tr.addView(tvAtributoNome)

            val tvAtributoValor = TextView(context)
            tvAtributoValor.text = et_novo_atributo_valor.text.toString()
            tvAtributoValor.setPadding(5, 5, 5, 5)
            tr.addView(tvAtributoValor)

            tlAtributos.addView(tr)
        }

        view.btn_concluir.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()

            val personagem = Personagem()

            personagem.nome = etNome.text.toString()
            personagem.classe = etClasse.text.toString()
            personagem.descricao = etDescricao.text.toString()

            personagem.health = Integer.valueOf(etHealth.text?.toString())

            Log.d(TAG, "${personagem.nome}, ${personagem.classe}, ${personagem.descricao}, ${personagem.health}")

            for (i in 0 until tl_atributos.childCount) {
                val row = tl_atributos.getChildAt(i) as TableRow
                val nome = (row.getVirtualChildAt(0) as TextView).text.toString()
                val valor = (row.getVirtualChildAt(1) as TextView).text.toString()
                Log.d(TAG, "$nome : $valor")

                val atributo = Atributo(nome, valor)
                personagem.atributos.add(atributo)
            }

            //TODO: Atualizar documento se ele já existir

            db.collection("adventures")
                    .whereEqualTo("title", aventura?.title)
                    .whereEqualTo("creator", aventura?.creator)
                    .get()
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "Document ${documentReference.documents[0].id} created successfully")
                        personagem.aventuraId = documentReference.documents[0].id

                        db.collection("characters")
                                .add(personagem)
                                .addOnSuccessListener { documentReference ->
                                    Log.d(TAG, "Document ${documentReference.id} created successfully")
                                    Toast.makeText(context, "Personagem criado com sucesso!", Toast.LENGTH_SHORT).show()
                                    NavHostFragment.findNavController(this).popBackStack(R.id.adventureFragment, false)
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error creating document", e)
                                    Toast.makeText(context, "Erro ao criar personagem", Toast.LENGTH_SHORT).show()
                                    progressBar.visibility = View.INVISIBLE
                                }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error getting adventure ID", e)
                        Toast.makeText(context, "Erro ao reconhecer aventura", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.INVISIBLE
                    }
        }

        return view
    }

    private fun completeFields(personagem: Personagem) {
        etNome.setText(personagem.nome)
        etClasse.setText(personagem.classe)
        etDescricao.setText(personagem.descricao)

        if (personagem.health != -1)
            etHealth.setText(personagem.health.toString(), TextView.BufferType.EDITABLE)

        for (atributo in personagem.atributos) {
            val tr = TableRow(context)
            tr.layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)

            val tvAtributoNome = TextView(context)
            tvAtributoNome.text = atributo.nome
            tvAtributoNome.setPadding(5, 5, 5, 5)
            tr.addView(tvAtributoNome)

            val tvAtributoValor = TextView(context)
            tvAtributoValor.text = atributo.valor
            tvAtributoValor.setPadding(5, 5, 5, 5)
            tr.addView(tvAtributoValor)

            tlAtributos.addView(tr)
        }
    }
}
