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
import com.example.hal_9000.igor.LoginActivity
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Atributo
import com.example.hal_9000.igor.model.Aventura
import com.example.hal_9000.igor.model.Personagem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_new_character.*
import kotlinx.android.synthetic.main.fragment_new_character.view.*


class NewCharacterFragment : Fragment() {

    private val TAG = "NewCharacterFragment"
    private var aventura: Aventura? = null
    private lateinit var personagemOld: Personagem
    private var isNPC: Boolean = false

    private lateinit var etNome: EditText
    private lateinit var etClasse: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etHealth: EditText
    private lateinit var tlAtributos: TableLayout

    private lateinit var db: FirebaseFirestore
    private lateinit var aventuraId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_character, container, false)

        aventura = NewCharacterFragmentArgs.fromBundle(arguments).aventura
        personagemOld = NewCharacterFragmentArgs.fromBundle(arguments).personagem
        isNPC = NewCharacterFragmentArgs.fromBundle(arguments).isNpc

        etNome = view.findViewById(R.id.et_nome)
        etClasse = view.findViewById(R.id.et_classe)
        etDescricao = view.findViewById(R.id.et_descricao)
        etHealth = view.findViewById(R.id.et_hp)
        tlAtributos = view.findViewById(R.id.tl_atributos)

        db = FirebaseFirestore.getInstance()

        if (isNPC)
            etNome.hint = "Nome"

        if (personagemOld.id.isNotEmpty())
            completeFields()

        aventuraId = "${aventura?.creator}_${aventura?.title}"

        view.btn_adicionar_atributo.setOnClickListener {

            if (et_novo_atributo_titulo.text.isEmpty() || et_novo_atributo_valor.text.isEmpty()) {
                Toast.makeText(context, "Insira ambos os dados do atributo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addTableRow(et_novo_atributo_titulo.text.toString(), et_novo_atributo_valor.text.toString())

            et_novo_atributo_titulo.text.clear()
            et_novo_atributo_valor.text.clear()
        }

        view.btn_concluir.setOnClickListener { concluirCriacao() }

        return view
    }

    private fun completeFields() {
        etNome.setText(personagemOld.nome)
        etClasse.setText(personagemOld.classe)
        etDescricao.setText(personagemOld.descricao)

        if (personagemOld.healthMax != -1)
            etHealth.setText(personagemOld.healthMax.toString(), TextView.BufferType.EDITABLE)

        for (atributo in personagemOld.atributos) {
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

        val personagem = Personagem()
        personagem.nome = etNome.text.toString()
        personagem.classe = etClasse.text.toString()
        personagem.descricao = etDescricao.text.toString()

        if (etHealth.text.isNotEmpty())
            personagem.healthMax = Integer.valueOf(etHealth.text.toString())

        personagem.creator = LoginActivity.username
        personagem.isNpc = isNPC
        personagem.isMaster = false

        Log.d(TAG, "${personagem.nome}, ${personagem.classe}, ${personagem.descricao}, ${personagem.healthMax}")

        for (i in 0 until tl_atributos.childCount) {
            val row = tl_atributos.getChildAt(i) as TableRow
            val nome = (row.getVirtualChildAt(0) as TextView).text.toString()
            val valor = (row.getVirtualChildAt(1) as TextView).text.toString()
            personagem.atributos.add(Atributo(nome, valor))
            Log.d(TAG, "$nome : $valor")
        }

        personagem.aventuraId = aventuraId

        if (personagemOld.id.isEmpty()) {
            personagem.created = System.currentTimeMillis() / 1000
            personagem.health = personagem.healthMax
            createCharacter(personagem)

        } else {
            personagem.id = personagemOld.id
            personagem.created = personagemOld.created
            personagem.health = personagemOld.health
            editCharacter(personagem)
        }
    }

    private fun createCharacter(personagem: Personagem) {
        db.collection("characters")
                .add(personagem)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Document ${documentReference.id} created successfully")
                    Toast.makeText(context, "Personagem criado com sucesso!", Toast.LENGTH_SHORT).show()
                    db.collection("characters").document(documentReference.id).update("id", documentReference.id)
                    db.collection("adventures").document(aventuraId).update("players." + personagem.nome, true)
                    NavHostFragment.findNavController(this).popBackStack()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error creating document", e)
                    Toast.makeText(context, "Erro ao criar personagem", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                }
    }

    private fun editCharacter(personagem: Personagem) {
        db.collection("characters")
                .document(personagem.id)
                .set(personagem)
                .addOnSuccessListener {
                    Log.d(TAG, "Document updated successfully")
                    Toast.makeText(context, "Personagem atualizado com sucesso!", Toast.LENGTH_SHORT).show()

                    if (!isNPC && personagemOld.nome != personagem.nome) {
                        if (personagem.nome.isNotEmpty())
                            db.collection("adventures").document(aventuraId).update("players." + personagem.nome, true)
                        if (personagemOld.nome.isNotEmpty())
                            db.collection("adventures").document(aventuraId).update("players." + personagemOld.nome, FieldValue.delete())
                    }

                    NavHostFragment.findNavController(this).popBackStack()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating document", e)
                    Toast.makeText(context, "Erro ao atualizar personagem", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                }
    }
}
