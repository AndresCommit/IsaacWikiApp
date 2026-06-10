package com.andres.wikitboiandres

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AndroidSyncManager : SyncManager {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("users")

    override suspend fun uploadAchievements(userId: String, achievementIds: List<Int>) {
        if (userId.isBlank()) return
        try {
            Log.d("SyncManager", "Subiendo ${achievementIds.size} logros a Firestore...")
            val data = mapOf("completedAchievements" to achievementIds)
            collection.document(userId)
                .set(data, SetOptions.merge())
                .await()
            Log.d("SyncManager", "Logros subidos con éxito")
        } catch (e: Exception) {
            Log.e("SyncManager", "Error al subir logros: ${e.message}")
            throw e
        }
    }

    override suspend fun downloadAchievements(userId: String): List<Int> {
        if (userId.isBlank()) return emptyList()
        try {
            Log.d("SyncManager", "Descargando logros de Firestore...")
            val document = collection.document(userId).get().await()
            return if (document.exists()) {
                val rawList = document.get("completedAchievements") as? List<*>
                val achievements = rawList?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
                Log.d("SyncManager", "Descargados ${achievements.size} logros")
                achievements
            } else {
                Log.d("SyncManager", "No hay datos previos en la nube")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error al descargar logros: ${e.message}")
            throw e
        }
    }
}
