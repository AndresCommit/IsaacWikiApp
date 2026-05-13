package com.andres.wikitboiandres.network

import com.andres.wikitboiandres.models.SteamAchievementResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class SteamApiService(private val httpClient: HttpClient) {
    private val baseUrl = "https://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v0001/"
    private val appId = "250900" 

    suspend fun getPlayerAchievements(apiKey: String, steamId: String): SteamAchievementResponse {
        return try {
            val response = httpClient.get(baseUrl) {
                parameter("appid", appId)
                parameter("key", apiKey)
                parameter("steamid", steamId)
            }
            

            if (response.status == HttpStatusCode.OK) {
                val body: SteamAchievementResponse = response.body()
                body
            } else {
                val errorBody = response.bodyAsText()
                SteamAchievementResponse(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SteamAchievementResponse(null)
        }
    }
}
