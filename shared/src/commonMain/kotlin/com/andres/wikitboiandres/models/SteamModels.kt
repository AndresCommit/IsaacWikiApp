package com.andres.wikitboiandres.models

import kotlinx.serialization.Serializable

@Serializable
data class SteamAchievementResponse(
    val playerstats: PlayerStats? = null
)

@Serializable
data class PlayerStats(
    val steamID: String? = null,
    val gameName: String? = null,
    val achievements: List<SteamAchievement>? = null,
    val error: String? = null,
    val success: Boolean = true
)

@Serializable
data class SteamAchievement(
    val apiname: String,
    val achieved: Int,
    val unlocktime: Long
)
