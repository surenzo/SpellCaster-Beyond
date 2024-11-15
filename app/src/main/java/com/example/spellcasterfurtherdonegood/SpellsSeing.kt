package com.example.spellcasterfurtherdonegood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels

class SpellsSeing : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spellsseing)
    }
}