package com.example.spellcasterfurtherdonegood

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class Inventory : Fragment() {

    private lateinit var spellAdapter: SpellAdapter
    private lateinit var recyclerView: RecyclerView
    private val spellList = mutableListOf<Spell>()

    private val auth = Firebase.auth
    //initiate firestore
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inventory, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        spellAdapter = SpellAdapter(spellList, "inventory")
        recyclerView.adapter = spellAdapter

        fetchSpellsFromFirestore()

        return view
    }

    private fun fetchSpellsFromFirestore() {
        var nameSpells = mutableListOf<String>()
        db.collection("users").document(auth.currentUser?.displayName.toString())
            .get()
            .addOnSuccessListener { document ->
                if(document != null) {
                    //Log.e("Spell", "${document.id} => ${document.data}")
                    val spells = document.data?.get("spells")
                    if (spells is MutableList<*>) {
                        nameSpells = spells.filterIsInstance<String>().toMutableList()
                        querySpells(nameSpells)

                    } else {
                        Log.e("Error", "Spells data is not a list of strings")
                    }
                } else {
                    Log.e("Error", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Error", "Error getting documents: ", exception)
            }
    }

    private fun querySpells(nameSpells: MutableList<String>) {
        val query = db.collection("spells").whereIn("name", nameSpells)
        query.get()
            .addOnSuccessListener { result ->
                //Log.d("Spell", "Success")
                for (document in result) {
                    //Log.e("Spell", "${document.id} => ${document.data}")
                    val spell = document.toObject(Spell::class.java)
                    spellList.add(spell)
                }
                spellAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Error", "Error getting documents: ", exception)
            }
    }

}