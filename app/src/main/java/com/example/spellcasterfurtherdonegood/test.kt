import android.util.Log
import com.SpellCasterFurtherDoneGood.graphql.GetSpellsQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern

fun useIA(spell: GetSpellsQuery.Spell, callback: (String, Float) -> Unit) {
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
v                }
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
            if (!response.isSuccessful || responseBody == null) {
                throw IOException("Erreur : $response")
            }
            val jsonResponse = JSONObject(responseBody)
            val content = jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            val patternIncant = Pattern.compile("Incantation : \"([^\"]+)\"")
            val matcherIncant = patternIncant.matcher(content)
            val patternPrice = Pattern.compile("Prix : \"([^\"]+)\"")
            val matcherPrice = patternPrice.matcher(content)
            if (matcherIncant.find()) {
                val incantation = matcherIncant.group(1)
                //val price = matcherPrice.group(1)?.toFloat()
                withContext(Dispatchers.Main) {
                    Log.d("SpecialActivity", incantation)
                    callback(incantation, 0.0f)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Log.d("SpecialActivity", "Incantation not found")
                }
            }
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                Log.e("SpecialActivity", "Network request failed", e)
            }
        }
    }
}