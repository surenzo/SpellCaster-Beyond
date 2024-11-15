package com.example.spellcasterfurtherdonegood

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val categorySpinner: Spinner = findViewById(R.id.category_spinner)
        val spellGrid: GridView = findViewById(R.id.spell_grid)
        val addSpellButton: Button = findViewById(R.id.add_spell_button)
        val signInButton: Button = findViewById(R.id.sign_in_button)


        val categories = listOf("Abjuration", "Conjuration", "Divination", "Enchantment", "Evocation", "Illusion", "Necromancy", "Transmutation")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categorySpinner.adapter = categoryAdapter

        val spellAdapter = SpellAdapter(this, mutableListOf())
        spellGrid.adapter = spellAdapter

        viewModel.selectedCategory.observe(this, Observer { category ->
            val spells = viewModel.getSpellsForCategory(category)
            spellAdapter.updateSpells(spells)
        })


        addSpellButton.setOnClickListener {
            // Handle add spell logic
        }
        signInButton.setOnClickListener {
            val intent = Intent(this, ActivityResultLauncher::class.java)
            startActivity(intent)
        }
    }
}