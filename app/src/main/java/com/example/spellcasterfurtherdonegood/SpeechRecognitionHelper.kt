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
import kotlinx.coroutines.delay


class SpeechRecognitionHelper(private val context: Context) {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private var isListening: Boolean = false

    companion object {
        val recognizedText: MutableLiveData<String> = MutableLiveData("")
        val isSpeechEnded: MutableLiveData<Boolean> = MutableLiveData(false)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun initializeSpeechRecognizer() {
        recognizedText.postValue("")
        isSpeechEnded.postValue(false)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "la")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
        }

        val requestAudioPermissionLauncher = (context as ComponentActivity).registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Permission refusée", Toast.LENGTH_SHORT).show()
            }
        }
        requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
            }

            override fun onBeginningOfSpeech() {
                isListening = true
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                Log.e("SpeechRecognitionHelper", "Error : $error")
                Toast.makeText(context, "Erreur de reconnaissance", Toast.LENGTH_SHORT).show()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null) {
                    recognizedText.postValue(matches.joinToString("\n"))
                }
            }

            override fun onSegmentResults(segmentResults: Bundle) {
                val matches = segmentResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null) {
                    recognizedText.postValue(matches.joinToString("\n"))
                }
            }

            override fun onResults(results: Bundle?) {
                //toast("Résultat")
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null) {
                    recognizedText.postValue(matches.joinToString("\n"))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("SpeechRecognitionHelper", "Event : $eventType")
            }
        })
    }

    fun startListening() {
        if (isListening){
            stopListening()
            cancelListening()
            Toast.makeText(context, "Reclick", Toast.LENGTH_SHORT).show()
        }
        else{
            speechRecognizer.startListening(speechIntent)
            isListening = true
        }

    }

    fun cancelListening() {
        speechRecognizer.cancel()
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }

    fun destroy() {
        speechRecognizer.destroy()
    }
}