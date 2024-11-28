package com.example.spellcasterfurtherdonegood

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class Library : Fragment() {

    private lateinit var spellAdapter: SpellAdapter
    private lateinit var recyclerView: RecyclerView
    private val spellList = mutableListOf<Spell>()

    //initiate firestore
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        spellAdapter = SpellAdapter(spellList, "library")
        recyclerView.adapter = spellAdapter

        fetchSpellsFromFirestore()

        return view
    }

    private fun fetchSpellsFromFirestore() {
        db.collection("spells")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    //Log.e("Spell", "${document.id} => ${document.data}")
                    val spell = document.toObject(Spell::class.java)
                    spellList.add(spell)
                }
                spellAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

}