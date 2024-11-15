package com.example.spellcasterfurtherdonegood

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class SpellAdapter(private val context: Context, private var spells: List<Spell>) : BaseAdapter() {

    override fun getCount(): Int = spells.size

    override fun getItem(position: Int): Any = spells[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_spell, parent, false)
        val spell = spells[position]
        val spellNameTextView: TextView = view.findViewById(R.id.spell_name)
        spellNameTextView.text = spell.name
        return view
    }

    fun updateSpells(newSpells: List<Spell>) {
        spells = newSpells
        notifyDataSetChanged()
    }
}