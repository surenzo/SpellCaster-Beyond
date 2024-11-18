package com.example.spellcasterfurtherdonegood

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import kotlin.math.min

class CastActivity : AppCompatActivity() {

    private lateinit var speechRecognitionHelper: SpeechRecognitionHelper

    var spellName = ""
    var spellSomatic = false
    var spellMaterial = ""
    var spellDamage = ""

    var incantationPercentage = 0

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cast)

        spellName = intent.getStringExtra("spellName").toString()
        val spellIncantation = intent.getStringExtra("spellIncantation").toString()
        spellSomatic = intent.getBooleanExtra("spellSomatic", false)
        spellMaterial = intent.getStringExtra("spellMaterial").toString()
        spellDamage = intent.getStringExtra("spellDamage").toString()

        val spellNameTextView: TextView = findViewById(R.id.spell_name)
        val incantation: TextView = findViewById(R.id.spell_incantation)
        val nextButton: Button = findViewById(R.id.button_next)
        val retryButton: Button = findViewById(R.id.button_retry)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val spellRecitationTextView: TextView = findViewById(R.id.spell_recitation)
        val distanceTextView: TextView = findViewById(R.id.distance)


        speechRecognitionHelper = SpeechRecognitionHelper(this)
        speechRecognitionHelper.initializeSpeechRecognizer()


        val toolbar : Toolbar = findViewById(R.id.toolbar)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener {
            finish()
        }
        // Observe changes in recognizedText and update the TextView
        SpeechRecognitionHelper.recognizedText.observe(this, Observer { text ->
            spellRecitationTextView.text = text
            val distance = levenshtein(spellIncantation!!, text)
            distanceTextView.text = "Distance: $distance"
            val percentage:Float = (spellIncantation.length - distance).toFloat() / spellIncantation.length * 100
            if(percentage > 0){
                progressBar.progress = percentage.toInt()
                if (percentage > 0){
                    progressBar.progressTintList = getColorStateList(R.color.colorProgressFirst)
                }
                if(percentage > 25){
                    progressBar.progressTintList = getColorStateList(R.color.colorProgressSecond)
                }
                if(percentage > 50){
                    progressBar.progressTintList = getColorStateList(R.color.colorProgressThird)
                }
                if(percentage > 75){
                    progressBar.progressTintList = getColorStateList(R.color.colorProgressFourth)
                }
            }
            else
                progressBar.progress = 0

        })

        SpeechRecognitionHelper.isSpeechEnded.observe(this, Observer { isEnded ->
            if (isEnded) {
                nextButton.visibility = View.VISIBLE
                incantationPercentage = progressBar.progress
            }
            else {
                //nextButton.visibility = View.GONE
            }
        })

        //nextButton.visibility = View.GONE

        spellNameTextView.text = spellName
        incantation.text = spellIncantation
        progressBar.progress = 0
        progressBar.progressTintList = getColorStateList(R.color.colorProgressFirst)
        retryButton.text = "Begin"
    }

    fun nextButton(view: View) {
        Intent(this, DrawActivity::class.java).apply {
            putExtra("spellName", spellName)
            putExtra("incantationPercentage", incantationPercentage)
            putExtra("spellSomatic", spellSomatic)
            putExtra("spellMaterial", spellMaterial)
            putExtra("spellDamage", spellDamage)
            startActivity(this)
        }
        finish()
    }

    fun retryButton(view: View) {
        val retryButton: Button = findViewById(R.id.button_retry)
        retryButton.text = "Retry"
        speechRecognitionHelper.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognitionHelper.destroy()
    }

    private fun levenshtein(lhs : CharSequence, rhs : CharSequence) : Int {
        if(lhs == rhs) { return 0 }
        if(lhs.isEmpty()) { return rhs.length }
        if(rhs.isEmpty()) { return lhs.length }

        val lhsLength = lhs.length + 1
        val rhsLength = rhs.length + 1

        var cost = Array(lhsLength) { it }
        var newCost = Array(lhsLength) { 0 }

        for (i in 1..rhsLength-1) {
            newCost[0] = i

            for (j in 1..lhsLength-1) {
                val match = if(lhs[j - 1] == rhs[i - 1]) 0 else 1

                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength - 1]
    }
}