package com.andres.wikitboiandres

import com.andres.wikitboiandres.db.IsaacDatabase
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class IsaacItem(
    val id: String,
    val name: String,
    val description: String
)

class IsaacRepository(private val database: IsaacDatabase) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun fetchAndSaveItems() {
        try {
            val url = "https://raw.githubusercontent.com/Zamiell/isaac-save-viewer/main/src/data/items.json"
            val items: List<IsaacItem> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                items.forEach { item ->
                    database.isaacDatabaseQueries.insertItem(
                        id = item.id,
                        name = item.name,
                        description = item.description
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun getAllItems(): List<IsaacItem> {
        return database.isaacDatabaseQueries.selectAllItems().executeAsList().map {
            IsaacItem(it.id, it.name, it.description)
        }
    }
}
