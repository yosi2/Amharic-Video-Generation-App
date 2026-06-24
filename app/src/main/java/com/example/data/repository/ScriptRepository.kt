package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ScriptDao
import com.example.data.model.Scene
import com.example.data.model.ScriptEntity
import com.example.data.model.VideoScript
import com.example.data.remote.Content
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.GenerationConfig
import com.example.data.remote.Part
import com.example.data.remote.RetrofitClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ScriptRepository(private val scriptDao: ScriptDao) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val videoScriptAdapter = moshi.adapter(VideoScript::class.java)

    val allScripts: Flow<List<ScriptEntity>> = scriptDao.getAllScripts()

    suspend fun getScriptById(id: Long): ScriptEntity? = withContext(Dispatchers.IO) {
        scriptDao.getScriptById(id)
    }

    suspend fun deleteScriptById(id: Long) = withContext(Dispatchers.IO) {
        scriptDao.deleteScriptById(id)
    }

    suspend fun generateAndSaveScript(
        topic: String,
        platform: String,
        tone: String
    ): VideoScript = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Gemini API Key is missing! Please configure it in the Secrets panel.")
        }

        val systemInstructionText = """
            Role: You are the core AI Engine for an Amharic automated video creation app. Your goal is to generate highly engaging, structured, and culturally resonant short-form video content in Amharic (አማርኛ) for TikTok, YouTube Shorts, and Instagram Reels.

            Guidelines:
            1. Always output the final script inside a valid JSON object so the mobile app can easily parse it.
            2. The language must be natural, modern, and viral-friendly Amharic.
            3. Every video must include a strong hook (first 3 seconds), 3 quick body points, and a clear Call to Action (CTA).
            4. Provide precise descriptions for background video footage (B-roll) and text-overlay keywords that match the audio.
        """.trimIndent()

        val userPrompt = """
            Task: Create a viral short-form video script in Amharic based on the user's input. The output must be strictly in JSON format.

            Input Details:
            - Topic: $topic
            - Platform: $platform
            - Tone: $tone

            Output Format:
            {
              "videoTitle": "Amharic title here",
              "scenes": [
                {
                  "sceneNumber": 1,
                  "section": "Hook",
                  "visualDescription": "Detailed prompt for stock video/image in English",
                  "textOverlay": "Short Amharic text to show on screen",
                  "voiceoverText": "The exact Amharic words to be spoken by AI voice"
                },
                {
                  "sceneNumber": 2,
                  "section": "Body_Point_1",
                  "visualDescription": "Detailed prompt for stock video/image in English",
                  "textOverlay": "Short Amharic text to show on screen",
                  "voiceoverText": "The exact Amharic words to be spoken by AI voice"
                },
                {
                  "sceneNumber": 3,
                  "section": "Body_Point_2",
                  "visualDescription": "Detailed prompt for stock video/image in English",
                  "textOverlay": "Short Amharic text to show on screen",
                  "voiceoverText": "The exact Amharic words to be spoken by AI voice"
                },
                {
                  "sceneNumber": 4,
                  "section": "Body_Point_3",
                  "visualDescription": "Detailed prompt for stock video/image in English",
                  "textOverlay": "Short Amharic text to show on screen",
                  "voiceoverText": "The exact Amharic words to be spoken by AI voice"
                },
                {
                  "sceneNumber": 5,
                  "section": "CTA",
                  "visualDescription": "Detailed prompt for stock video/image in English",
                  "textOverlay": "Short Amharic text to show on screen",
                  "voiceoverText": "The exact Amharic words to be spoken by AI voice"
                }
              ]
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            )
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val rawJsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("No response returned from Gemini")

        Log.d("ScriptRepository", "Raw Response: $rawJsonText")

        val cleanJsonText = cleanJson(rawJsonText)
        val videoScript = videoScriptAdapter.fromJson(cleanJsonText)
            ?: throw Exception("Failed to parse response JSON: $cleanJsonText")

        // Save to database
        val entity = ScriptEntity(
            topic = topic,
            platform = platform,
            tone = tone,
            videoTitle = videoScript.videoTitle,
            scriptJson = cleanJsonText
        )
        scriptDao.insertScript(entity)

        videoScript
    }

    private fun cleanJson(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```")) {
            val firstLineEnd = text.indexOf('\n')
            text = if (firstLineEnd != -1) {
                text.substring(firstLineEnd + 1)
            } else {
                text.removePrefix("```")
            }
            if (text.endsWith("```")) {
                text = text.removeSuffix("```").trim()
            }
        }
        return text.trim()
    }
}
