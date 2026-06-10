package com.andres.wikitboiandres

interface SyncManager {
    suspend fun uploadAchievements(userId: String, achievementIds: List<Int>)
    suspend fun downloadAchievements(userId: String): List<Int>
}


