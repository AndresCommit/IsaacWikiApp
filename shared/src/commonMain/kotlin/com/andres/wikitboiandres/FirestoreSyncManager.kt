package com.andres.wikitboiandres

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.serialization.Serializable

// Clase que permite a Kotlin serializar los datos para Firebase correctamente
@Serializable
data class UserAchievements(val completedAchievements: List<Int> = emptyList())

class FirestoreSyncManager : SyncManager {
    private val firestore = Firebase.firestore
    private val collection = firestore.collection("users")

    override suspend fun uploadAchievements(userId: String, achievementIds: List<Int>) {
        if (userId.isBlank()) return
        try {
            val document = collection.document(userId)
            // Guardamos usando la clase serializable
            document.set(UserAchievements(achievementIds), merge = true)
            println("Sync: Subida exitosa a la nube")
        } catch (e: Exception) {
            println("Sync: Error uploadAchievements: ${e.message}")
        }
    }

    override suspend fun downloadAchievements(userId: String): List<Int>? {
        if (userId.isBlank()) return null
        return try {
            val snapshot = collection.document(userId).get()
            if (!snapshot.exists) {
                println("Sync: Usuario nuevo, sin logros en la nube")
                return emptyList()
            }

            // Extraemos los datos usando la clase fuertemente tipada (Adiós al error Any?)
            val userDoc = snapshot.data<UserAchievements>()
            val result = userDoc.completedAchievements

            println("Sync: Descargados ${result.size} logros correctamente")
            result
        } catch (e: Exception) {
            println("Sync: Error crítico en descarga: ${e.message}")
            null
        }
    }
}