package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Scene(
    val sceneNumber: Int,
    val section: String,
    val visualDescription: String,
    val textOverlay: String,
    val voiceoverText: String
)

@JsonClass(generateAdapter = true)
data class VideoScript(
    val videoTitle: String,
    val scenes: List<Scene>
)
