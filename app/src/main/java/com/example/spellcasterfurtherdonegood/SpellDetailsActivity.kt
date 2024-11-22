package com.example.spellcasterfurtherdonegood

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SpellDetailsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private var spellName =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spell_details)
        auth = Firebase.auth

        // Retrieve data from the Intent
        spellName = intent.getStringExtra("spellName").toString()
        val spellCategory = intent.getStringExtra("spellCategory")
        val spellDescription = intent.getStringExtra("spellDescription")
        val spellIncantation = intent.getStringExtra("spellIncantation")
        val spellSomatic = intent.getBooleanExtra("spellSomatic", false)
        val spellPrice = intent.getFloatExtra("spellPrice", 0.0f)
        val spellMaterial = intent.getStringExtra("spellMaterial")
        val spellDamage = intent.getStringExtra("spellDamage")
        val fromInventory = intent.getBooleanExtra("fromInventory", false)

        // Update UI elements with the retrieved data
        val spellNameTextView: TextView = findViewById(R.id.spell_name)
        val spellCategoryTextView: TextView = findViewById(R.id.spell_category)
        val description: TextView = findViewById(R.id.spell_description)
        val incantation: TextView = findViewById(R.id.spell_vocal)
        val somatic: TextView = findViewById(R.id.spell_sommatic)
        val price: TextView = findViewById(R.id.spell_price)
        val material: TextView = findViewById(R.id.spell_materials)
        val damage: TextView = findViewById(R.id.spell_damage)
        val addToInventoryButton: View = findViewById(R.id.add_to_inventory)
        if(fromInventory) {
            addToInventoryButton.visibility = View.GONE
        }


        val toolbar : Toolbar = findViewById(R.id.toolbar)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener {
            finish()
        }

        spellNameTextView.text = spellName
        spellCategoryTextView.text = spellCategory
        description.text = spellDescription
        incantation.text = spellIncantation
        somatic.text = if (spellSomatic) "Yes" else "No"
        price.text = spellPrice.toString()
        material.text = spellMaterial
        damage.text = spellDamage

    }

    fun addToInventory(view: View) {
        // Add the spell to the user's inventory - add to the firestore
        Log.d("SpellDetailsActivity", "Adding $spellName to inventory")
        val data = hashMapOf(
            "spells" to FieldValue.arrayUnion(spellName)
        )

        db.collection("users").document(auth.currentUser?.displayName.toString())
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("inventory", true)
                }
                startActivity(intent)
                finish()
            }
    }


}