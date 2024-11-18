package com.example.spellcasterfurtherdonegood

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData


class SpeechRecognitionHelper(private val context: Context) {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private var isListening: Boolean = false

    companion object {
        val recognizedText: MutableLiveData<String> = MutableLiveData("")
        var recognizedConfidence: Float = 0.0f
        val isSpeechEnded: MutableLiveData<Boolean> = MutableLiveData(false)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        val requestAudioPermissionLauncher = (context as ComponentActivity).registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Permission microphone requise", Toast.LENGTH_SHORT).show()
            }
        }
        requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        val pm = context.packageManager
        val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        val activities = pm.queryIntentActivities(recognitionIntent, 0)

        if (activities.isEmpty()) {
            Toast.makeText(context, "Reconnaissance vocale non disponible", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "${activities.size} activités disponibles pour la reconnaissance vocale", Toast.LENGTH_SHORT).show()
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SpeechRecognizer", "Prêt à écouter")
            }

            override fun onBeginningOfSpeech() {
                isListening = true
                Log.d("SpeechRecognizer", "Début de la parole")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                Log.d("SpeechRecognizer", "Fin de la parole")
                isSpeechEnded.postValue(true)
            }

            override fun onError(error: Int) {
                Log.e("SpeechRecognizer", "Erreur : $error")
                Toast.makeText(context, "Erreur de reconnaissance", Toast.LENGTH_SHORT).show()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidence = partialResults?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                if (matches != null) {
                    recognizedText.postValue(matches.joinToString("\n"))
                    Log.d("SpeechRecognizer", "Partial Results: ${recognizedText.value} (confiance : ${"%.2f".format(confidence?.getOrNull(0) ?: 0.0f)})")
                }
            }

            override fun onSegmentResults(segmentResults: Bundle) {
                val matches = segmentResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidences = segmentResults.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

                if (matches != null && confidences != null) {
                    val segmentText = matches.mapIndexed { index, word ->
                        val confidence = confidences.getOrNull(index) ?: 0.0f
                        "$word (confiance : ${"%.2f".format(confidence)})"
                    }.joinToString("\n")

                    recognizedText.postValue(segmentText)
                    Log.d("SpeechRecognizer", "Segment Results: ${recognizedText.value}")
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                /*recognizedText.postValue(matches?.mapIndexed { index, word ->
                    val confidence = confidences?.getOrNull(index) ?: 0.0f
                    "$word (confiance : ${"%.2f".format(confidence)})"
                }?.joinToString("\n") ?: "")*/
                //get avg confidence
                recognizedConfidence = confidences?.average()?.toFloat() ?: 0.0f
                Log.d("SpeechRecognizer", "Results: ${recognizedText.value}")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening() {
        isSpeechEnded.postValue(false)
        speechRecognizer.startListening(speechIntent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }

    fun isListening(): Boolean {
        return isListening
    }

    fun destroy() {
        speechRecognizer.destroy()
    }
}