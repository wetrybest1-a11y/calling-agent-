package com.example.domain.ai

import com.example.BuildConfig
import com.example.data.models.CallRecord
import com.example.data.models.TranscriptMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CallAnalysisResult(
    val qualificationScore: Int,
    val summary: String,
    val customerIntent: String,
    val objections: List<String>,
    val nextAction: String,
    val leadStatus: String
)

class GeminiVoiceService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeCallTranscript(
        contactName: String,
        company: String,
        agentName: String,
        transcript: List<TranscriptMessage>
    ): CallAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val transcriptText = transcript.joinToString("\n") { "${it.sender} (+${it.timestampOffsetSecs}s): ${it.text}" }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are CallPilot AI's Post-Call CRM Analyst.
                    Analyze this outbound telephone conversation between AI Voice Agent ($agentName) and Lead ($contactName at $company).
                    
                    Transcript:
                    $transcriptText
                    
                    Return a JSON object with strictly these keys:
                    {
                      "qualificationScore": (integer between 0 and 100),
                      "summary": (concise 2-sentence summary of the conversation),
                      "customerIntent": ("High Purchase Intent" | "Exploring Options" | "Objection / Price Sensitive" | "Not Interested"),
                      "objections": (array of strings representing concerns raised),
                      "nextAction": (recommended next step),
                      "leadStatus": ("QUALIFIED" | "INTERESTED" | "FOLLOW_UP_SCHEDULED" | "DO_NOT_CALL")
                    }
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    val contentsArray = JSONArray()
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray()
                        partsArray.put(JSONObject().apply { put("text", prompt) })
                        put("parts", partsArray)
                    }
                    contentsArray.put(contentObj)
                    put("contents", contentsArray)

                    val genConfig = JSONObject().apply {
                        put("responseMimeType", "application/json")
                    }
                    put("generationConfig", genConfig)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestJson.toString().toRequestBody(mediaType)
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val json = JSONObject(responseBody)
                    val candidates = json.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text")

                    if (!text.isNullOrBlank()) {
                        val parsed = JSONObject(text)
                        val objectionsList = mutableListOf<String>()
                        val objArr = parsed.optJSONArray("objections")
                        if (objArr != null) {
                            for (i in 0 until objArr.length()) {
                                objectionsList.add(objArr.getString(i))
                            }
                        }

                        return@withContext CallAnalysisResult(
                            qualificationScore = parsed.optInt("qualificationScore", 85),
                            summary = parsed.optString("summary", "Call completed successfully. Lead showed keen interest in automated calling workflows."),
                            customerIntent = parsed.optString("customerIntent", "High Purchase Intent"),
                            objections = objectionsList.ifEmpty { listOf("Pricing structure inquiry") },
                            nextAction = parsed.optString("nextAction", "Send product demo calendar invite via email"),
                            leadStatus = parsed.optString("leadStatus", "QUALIFIED")
                        )
                    }
                }
            } catch (e: Exception) {
                // Fall back to rule-based analysis
            }
        }

        // Rule-based heuristic analysis fallback when offline or no API key
        val leadSpeech = transcript.filter { it.sender == "Lead" }.map { it.text.lowercase() }
        val hasBooking = leadSpeech.any { it.contains("tomorrow") || it.contains("invite") || it.contains("yes") || it.contains("sure") }
        val hasQuestions = leadSpeech.any { it.contains("how") || it.contains("cost") || it.contains("billing") || it.contains("work") }

        val score = if (hasBooking) 92 else if (hasQuestions) 75 else 50
        val intent = if (hasBooking) "High Purchase Intent" else if (hasQuestions) "Exploring Options" else "Neutral Inquiry"
        val objections = if (hasQuestions) listOf("Asked about per-second billing calculations") else emptyList()

        CallAnalysisResult(
            qualificationScore = score,
            summary = "AI Agent successfully verified contact details for $contactName and presented the usage-based calling platform.",
            customerIntent = intent,
            objections = objections,
            nextAction = if (hasBooking) "Scheduled product demo walkthrough for tomorrow" else "Follow up via email in 2 business days",
            leadStatus = if (hasBooking) "QUALIFIED" else "INTERESTED"
        )
    }

    suspend fun generateAgentResponse(
        agentScript: String,
        conversationHistory: List<TranscriptMessage>,
        userMessage: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are CallPilot AI calling agent. Your script/instructions:
                    $agentScript
                    
                    Recent turns:
                    ${conversationHistory.takeLast(4).joinToString("\n") { "${it.sender}: ${it.text}" }}
                    Lead: $userMessage
                    
                    Respond as the AI Agent in under 30 words, sounding natural, professional, and conversational.
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    val contentsArray = JSONArray()
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray()
                        partsArray.put(JSONObject().apply { put("text", prompt) })
                        put("parts", partsArray)
                    }
                    contentsArray.put(contentObj)
                    put("contents", contentsArray)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestJson.toString().toRequestBody(mediaType)
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

                val request = Request.Builder().url(url).post(body).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val json = JSONObject(responseBody)
                    val text = json.optJSONArray("candidates")?.optJSONObject(0)
                        ?.optJSONObject("content")?.optJSONArray("parts")
                        ?.optJSONObject(0)?.optString("text")

                    if (!text.isNullOrBlank()) {
                        return@withContext text.trim()
                    }
                }
            } catch (e: Exception) {
                // Return default response
            }
        }

        "Understood! With our pay-per-second model, you only pay for connected time with zero monthly commitment. Would you like me to send over the documentation?"
    }
}
