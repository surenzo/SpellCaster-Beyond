package com.example.spellcasterfurtherdonegood

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class DrawActivity : AppCompatActivity() {

    var sommaticPercentage = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)

        val spellName = intent.getStringExtra("spellName")
        val spellSomatic = intent.getBooleanExtra("spellSomatic", false)
        val spellMaterial = intent.getStringExtra("spellMaterial")

        val spellNameTextView: TextView = findViewById(R.id.spell_name)
        val nextButton: Button = findViewById(R.id.button_next)
        val retryButton: Button = findViewById(R.id.button_retry)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val distanceTextView: TextView = findViewById(R.id.distance)

        val toolbar : Toolbar = findViewById(R.id.toolbar)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener {
            finish()
        }

        //nextButton.visibility = View.GONE

        spellNameTextView.text = spellName
        progressBar.progress = 0
        progressBar.progressTintList = getColorStateList(R.color.colorProgressFirst)
        retryButton.text = "Begin"
    }

    fun nextButton(view: View) {
        // Create an AlertDialog to show the damage and confirm the action
        val nani = intent.getStringExtra("spellDamage")
        // in nani we have "2d6" we take the maximum value of the dice and multiply it by the number of dice to calculate the damage
        val dice = nani?.split("d")
        val maxDiceValue = dice?.get(1)?.toInt()
        val diceNumber = dice?.get(0)?.toInt()
        val damage = maxDiceValue?.times(diceNumber!!)
        val spellDamage = (intent.getIntExtra("incantationPercentage", 0) + sommaticPercentage ) * damage!! / 200
        AlertDialog.Builder(this)
            .setTitle("Damage Report")
            .setMessage("You did $spellDamage damage. Do you want to go back to the main activity?")
            .setPositiveButton("OK") { dialog, which ->
                finish()
            }
            .show()
    }

    fun retryButton(view: View) {
        val retryButton: Button = findViewById(R.id.button_retry)
        retryButton.text = "Retry"
    }
}