// SpellAdapter.kt
package com.example.spellcasterfurtherdonegood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Spell(val name: String = "", val description: String = "")


class SpellAdapter(private val spellList: List<Spell>) : RecyclerView.Adapter<SpellAdapter.SpellViewHolder>() {

    class SpellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val spellName: TextView = itemView.findViewById(R.id.spell_name)
        val spellDescription: TextView = itemView.findViewById(R.id.spell_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpellViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.spell_item, parent, false)
        return SpellViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SpellViewHolder, position: Int) {
        val currentSpell = spellList[position]
        holder.spellName.text = currentSpell.name
        holder.spellDescription.text = currentSpell.description
    }

    override fun getItemCount() = spellList.size
}