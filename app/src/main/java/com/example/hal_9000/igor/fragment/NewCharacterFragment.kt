package com.example.hal_9000.igor.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.util.TypedValue
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
    private lateinit var personagem: Personagem

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
        personagem = NewCharacterFragmentArgs.fromBundle(arguments).personagem

        etNome = view.findViewById(R.id.et_nome)
        etClasse = view.findViewById(R.id.et_classe)
        etDescricao = view.findViewById(R.id.et_descricao)
        etHealth = view.findViewById(R.id.et_hp)
        tlAtributos = view.findViewById(R.id.tl_atributos)

        if (personagem.id.isNotEmpty())
            completeFields(personagem)

        view.btn_adicionar_atributo.setOnClickListener {

            if (et_novo_atributo_titulo.text.isEmpty() || et_novo_atributo_valor.text.isEmpty()) {
                Toast.makeText(context, "Insira ambos os dados do atributo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addTableRow(et_novo_atributo_titulo.text.toString(), et_novo_atributo_valor.text.toString())
        }

        view.btn_concluir.setOnClickListener { concluirCriacao() }

        return view
    }

    private fun completeFields(personagem: Personagem) {
        etNome.setText(personagem.nome)
        etClasse.setText(personagem.classe)
        etDescricao.setText(personagem.descricao)

        if (personagem.health != -1)
            etHealth.setText(personagem.health.toString(), TextView.BufferType.EDITABLE)

        for (atributo in personagem.atributos) {
            addTableRow(atributo.nome, atributo.valor)
        }
    }

    private fun addTableRow(name: String, value: String) {
        val tr = TableRow(context)
        tr.layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)

        val tvAtributoNome = TextView(context)
        tvAtributoNome.text = name
        tvAtributoNome.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
        tvAtributoNome.setPadding(5, 5, 5, 5)
        tr.addView(tvAtributoNome)

        val tvAtributoValor = TextView(context)
        tvAtributoValor.text = value
        tvAtributoValor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
        tvAtributoValor.setPadding(5, 5, 5, 5)
        tr.addView(tvAtributoValor)

        tlAtributos.addView(tr)
    }

    private fun concluirCriacao() {
        progressBar.visibility = View.VISIBLE
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        personagem.nome = etNome.text.toString()
        personagem.classe = etClasse.text.toString()
        personagem.descricao = etDescricao.text.toString()

        personagem.health = Integer.valueOf(etHealth.text?.toString())

        Log.d(TAG, "${personagem.nome}, ${personagem.classe}, ${personagem.descricao}, ${personagem.health}")


        personagem.atributos.clear()
        for (i in 0 until tl_atributos.childCount) {
            val row = tl_atributos.getChildAt(i) as TableRow
            val nome = (row.getVirtualChildAt(0) as TextView).text.toString()
            val valor = (row.getVirtualChildAt(1) as TextView).text.toString()
            Log.d(TAG, "$nome : $valor")

            personagem.atributos.add(Atributo(nome, valor))
        }

        if (personagem.id.isNotEmpty()) {
            db.collection("characters")
                    .document(personagem.id)
                    .set(personagem)
                    .addOnSuccessListener {
                        Log.d(TAG, "Document updated successfully")
                        Toast.makeText(context, "Personagem atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        NavHostFragment.findNavController(this).popBackStack(R.id.adventureFragment, false)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error updating document", e)
                        Toast.makeText(context, "Erro ao atualizar personagem", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.INVISIBLE
                    }
        } else {

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

                                    db.collection("characters").document(documentReference.id).update("id", documentReference.id)

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
    }
}
