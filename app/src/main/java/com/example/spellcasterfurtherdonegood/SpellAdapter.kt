// SpellAdapter.kt
package com.example.spellcasterfurtherdonegood

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android. app. Activity
import android.content.Context
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

data class Spell(val name: String = "", val category: String = "", val description: String = "", val incantation: String = "", val somatic:Boolean = false, val price: Float = 0.0f, val material: String =  ""  , val damage: String = "")


class SpellAdapter(private val spellList: List<Spell>, private val activity: String) : RecyclerView.Adapter<SpellAdapter.SpellViewHolder>() {

    class SpellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val spellName: TextView = itemView.findViewById(R.id.spell_name)
        val spellCategory: TextView = itemView.findViewById(R.id.spell_category)
        val spellCast: Button = itemView.findViewById(R.id.spell_cast)
        val spellImage: ImageView = itemView.findViewById(R.id.spell_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpellViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.spell_item, parent, false)
        return SpellViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SpellViewHolder, position: Int) {
        val currentSpell = spellList[position]
        val site = "https://www.dndbeyond.com/content/1-0-3005-0/skins/waterdeep/images/spell-schools/35/"
        Glide.with(holder.itemView.context)
            .load(site + currentSpell.category.lowercase() + ".png")
            .into(holder.spellImage)

        holder.spellName.text = currentSpell.name
        holder.spellCategory.text = currentSpell.category
        // check the activity, if it is the library does that
        if(activity == "library"){
            holder.spellCast.visibility = View.GONE
            holder.itemView.setOnClickListener(View.OnClickListener {
                // Start new activity with spell details
                Intent(holder.itemView.context, SpellDetailsActivity::class.java).apply {
                    putExtra("spellName", currentSpell.name)
                    putExtra("spellCategory", currentSpell.category)
                    putExtra("spellDescription", currentSpell.description)
                    putExtra("spellIncantation", currentSpell.incantation)
                    putExtra("spellSomatic", currentSpell.somatic)
                    putExtra("spellPrice", currentSpell.price)
                    putExtra("spellMaterial", currentSpell.material)
                    putExtra("spellDamage", currentSpell.damage)
                    holder.itemView.context.startActivity(this)
                }
            })
        }
        if(activity == "inventory")
        {
            holder.spellCast.setOnClickListener(View.OnClickListener {
                // Start new activity with spell details
                Intent(holder.itemView.context, CastActivity::class.java).apply {
                    putExtra("spellName", currentSpell.name)
                    putExtra("spellIncantation", currentSpell.incantation)
                    putExtra("spellSomatic", currentSpell.somatic)
                    putExtra("spellDamage", currentSpell.damage)
                    holder.itemView.context.startActivity(this)
                }
            })
            holder.itemView.setOnClickListener(View.OnClickListener {
                // Start new activity with spell details
                Intent(holder.itemView.context, SpellDetailsActivity::class.java).apply {
                    putExtra("spellName", currentSpell.name)
                    putExtra("spellCategory", currentSpell.category)
                    putExtra("spellDescription", currentSpell.description)
                    putExtra("spellIncantation", currentSpell.incantation)
                    putExtra("spellSomatic", currentSpell.somatic)
                    putExtra("spellPrice", currentSpell.price)
                    putExtra("spellMaterial", currentSpell.material)
                    putExtra("spellDamage", currentSpell.damage)
                    putExtra("fromInventory", true)
                    holder.itemView.context.startActivity(this)
                }
            })
        }
    }

    override fun getItemCount() = spellList.size
}