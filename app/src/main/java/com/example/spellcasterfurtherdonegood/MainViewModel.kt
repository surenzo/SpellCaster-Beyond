package com.example.spellcasterfurtherdonegood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _selectedCategory = MutableLiveData<String>()
    val selectedCategory: LiveData<String> get() = _selectedCategory

    private val spells = mapOf(
        "Abjuration" to mutableListOf<Spell>(),
        "Conjuration" to mutableListOf<Spell>(),
        // Add other categories
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun getSpellsForCategory(category: String): List<Spell> {
        return spells[category] ?: emptyList()
    }
}