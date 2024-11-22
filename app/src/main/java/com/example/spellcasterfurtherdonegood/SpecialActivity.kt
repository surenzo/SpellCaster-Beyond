package com.example.spellcasterfurtherdonegood

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.SpellCasterFurtherDoneGood.graphql.GetSpellsQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.BooleanExpression
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val db = FirebaseFirestore.getInstance()

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
    }

    fun fetchSpellDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("SpecialActivity", "Fetching spell details")
            val response = apolloClient.query(GetSpellsQuery()).execute()

            withContext(Dispatchers.Main) {
                if (response.data != null) {
                    val spells = response.data?.spells
                    spells?.forEach { spell ->
                        Log.d("SpecialActivity", "Updating Spell: ${spell.name}")
                        updateDatabaseWithIA(spell)
                    }
                } else if (response.errors != null) {
                    Log.e("SpecialActivity", "Error fetching spell details: ${response.errors}")
                } else {
                    Log.e("SpecialActivity", "response.data is null")
                }
            }
        }
    }

    suspend fun updateDatabaseWithIA(spell: GetSpellsQuery.Spell) {
        //check if the spell has a vocal component and use IA if so
        spell.components?.let { components ->
            if (components.any { it?.rawValue == "V" }) {
                useIAWithBackoff(spell) { incant, pr->
                    Log.d("SpecialActivity", "Incantation: $incant")
                    nextUpdate(spell, incant, pr)
                }
            }
            else{
                nextUpdate(spell, "", 0.0f)
            }
        }
    }

    fun nextUpdate(spell: GetSpellsQuery.Spell, incantation: String, price: Float) {
        val sommatic = true
        // do the same but with sommatic
        //TODO: update the database with the incantation and sommatic
        val sanitizedSpellName = spell.name.replace("/", "-") // Remplace les slashs par un tiret
        val damage = spell.damage?.damage_at_character_level?.get(0)?.damage
        val data = hashMapOf(
            "name" to sanitizedSpellName,
            "category" to spell.school.name,
            "description" to spell.desc.joinToString(separator = " \n"),
            "damage" to "2d6",
            "material" to spell.material,
            "incantation" to incantation,
            "sommatic" to sommatic,
            "price" to price
        )
        Log.d("SpecialActivity", "Updating database with spell: ${sanitizedSpellName}")
        db.collection("spells").document(sanitizedSpellName)
            .set(data, SetOptions.merge())
    }
   /* fun updateSpellsInBatch(spells: List<GetSpellsQuery.Spell>) {
        val batch = db.batch()

        spells.forEach { spell ->
            val spellRef = db.collection("spells").document(spell.name)
            val data = hashMapOf(
                "name" to spell.name,
                "category" to spell.school.name,
                "description" to spell.desc.joinToString(separator = " \n"),
                "damage" to "2d6",
                "material" to spell.material,
                "incantation" to "Unknown", // Remplis ou génère cette donnée
                "sommatic" to true,
                "price" to 0.0f // Remplis ou génère cette donnée
            )
            batch.set(spellRef, data, SetOptions.merge())
        }

        batch.commit()
            .addOnSuccessListener { Log.d("SpecialActivity", "Batch update successful!") }
            .addOnFailureListener { e -> Log.e("SpecialActivity", "Batch update failed", e) }
    }*/
   suspend fun useIAWithBackoff(spell: GetSpellsQuery.Spell, callback: (String, Float) -> Unit) {
       withContext(Dispatchers.IO) { // Force l'exécution dans un thread IO
           val client = OkHttpClient()
           val apiToken = "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"
           val url = "https://api-inference.huggingface.co/models/mistralai/Mixtral-8x7B-Instruct-v0.1/v1/chat/completions"
           val json = """
        {
            "model": "mistralai/Mixtral-8x7B-Instruct-v0.1",
            "messages": [
                {
                    "role": "user",
                    "content": "Tu es un magicien dans un monde de magie et de sorcellerie. Les sorts sont des formules magiques qui permettent de manipuler la réalité. Donne moi l'incantation en latin du sort ${spell.name} que tu es en train de lancer et son prix ! \n L'incantation doit être longue de vingt mots \nFormat désiré : 'Incantation : \"[incantation en 20 mots minimum]\"'\n'Prix : \"[Prix]\"'"
                }
            ],
            "max_tokens": 500,
            "stream": false
        }
        """
           val requestBody = RequestBody.create("application/json".toMediaType(), json)

           var attempts = 0
           val maxAttempts = 5
           var delayTime = 1000L // 1 second initial delay

           while (attempts < maxAttempts) {
               try {
                   val request = Request.Builder()
                       .url(url)
                       .post(requestBody)
                       .addHeader("Authorization", "Bearer $apiToken")
                       .addHeader("Content-Type", "application/json")
                       .build()

                   val response = client.newCall(request).execute()
                   val responseBody = response.body?.string()

                   if (!response.isSuccessful || responseBody == null) {
                       throw IOException("Erreur dans la réponse de l'API : $responseBody")
                   }

                   val jsonResponse = JSONObject(responseBody)
                   val content = jsonResponse.getJSONArray("choices")
                       .getJSONObject(0)
                       .getJSONObject("message")
                       .getString("content")

                   val patternIncant = Pattern.compile("Incantation : \"([^\"]+)\"")
                   val matcherIncant = patternIncant.matcher(content)

                   if (matcherIncant.find()) {
                       val incantation = matcherIncant.group(1)
                       withContext(Dispatchers.Main) { // Retourne au thread principal pour le callback
                           callback(incantation, 0.0f)
                       }
                       return@withContext
                   }

               } catch (e: IOException) {
                   Log.e("SpecialActivity", "Tentative $attempts échouée : ${e.message}")
               }

               attempts++
               if (attempts < maxAttempts) {
                   Log.d("SpecialActivity", "Nouvelle tentative dans ${delayTime}ms")
                   delay(delayTime)
                   delayTime *= 2 // Double le délai à chaque tentative
               }
           }

           withContext(Dispatchers.Main) {
               Log.e("SpecialActivity", "Échec après $maxAttempts tentatives")
               callback("", 0.0f)
           }
       }
   }

}