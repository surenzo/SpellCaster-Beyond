package com.example.spellcasterfurtherdonegood

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.SpellCasterFurtherDoneGood.graphql.GetSpellsQuery
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern

class SpecialActivity : AppCompatActivity() {

    val apolloClient = ApolloClient.Builder()
        .serverUrl("https://www.dnd5eapi.co/graphql")
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_special)
    }

    fun toMain(view: View){
        finish()
    }
    fun fetch(view: View){
        fetchSpellDetails()
    }
    fun fetch2(view: View){
        main()
    }

    fun fetchSpellDetails() {
        runBlocking {
            Log.d("SpecialActivity", "Fetching spell details")
            val response = apolloClient.query(GetSpellsQuery()).execute()

            if (response.data != null) {
                val spells = response.data?.spells
                for (spell in spells!!)
                    Log.d("SpecialActivity", "Spell: $spell")
            } else if (response.errors != null) {
                Log.e("SpecialActivity", "Error fetching spell details: ${response.errors}")
            }
            else {
                Log.e("SpecialActivity", "response.data is null")
            }
        }
    }


    fun main() {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val apiToken = "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"
            val url = "https://api-inference.huggingface.co/models/mistralai/Mixtral-8x7B-Instruct-v0.1/v1/chat/completions"

            val json = """
        {
            "model": "mistralai/Mixtral-8x7B-Instruct-v0.1",
            "messages": [
                {
                    "role": "user",
                    "content": "Tu es un magicien dans un monde de magie et de sorcellerie. Les sorts sont des formules magiques qui permettent de manipuler la réalité. Donne moi l'incantation en latin du sort boule de feu que tu es en train de lancer ! Tu ne dois dire que l'incantation !\n L'incantation doit être longue de vingt mots \nFormat désiré : 'Incantation : \"[incantation en 20 mots minimum]\"'"
                }
            ],
            "max_tokens": 500,
            "stream": false
        }
        """
            val requestBody = RequestBody.create("application/json".toMediaType(), json)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiToken")
                .addHeader("Content-Type", "application/json")
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val content = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    val pattern = Pattern.compile("Incantation : \"([^\"]+)\"")
                    val matcher = pattern.matcher(content)
                    if (matcher.find()) {
                        val incantation = matcher.group(1)
                        withContext(Dispatchers.Main) {
                            Log.d("SpecialActivity", incantation)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.d("SpecialActivity", "Incantation not found")
                        }
                    }
                } else {
                    throw IOException("Erreur : $response")
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("SpecialActivity", "Network request failed", e)
                }
            }
        }
    }
}