package com.andres.wikitboiandres

import com.andres.wikitboiandres.db.IsaacDatabase
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RemoteObjeto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val tipo: String
)

@Serializable
data class RemoteConsumible(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val tipo: String
)

@Serializable
data class RemoteMarca(
    val id: Int,
    val nombre: String
)

@Serializable
data class RemoteDesbloqueo(
    val personaje_id: Int,
    val marca_id: Int,
    val objeto_id: Int? = null,
    val consumible_id: Int? = null
)

@Serializable
data class RemotePersonaje(
    val id: Int,
    val nombre: String,
    val es_tainted: Boolean,
    val metodo_desbloqueo: String? = null
)

@Serializable
data class RemoteEstadisticas(
    val personaje_id: Int,
    val salud: String,
    val velocidad: Double,
    val lagrimas: Double,
    val dano: Double,
    val rango: Double,
    val velocidad_disparo: Double,
    val suerte: Double,
    val objeto_inicial_id: Int? = null,
    val consumible_inicial_id: Int? = null
)

class IsaacRepository(private val database: IsaacDatabase) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                },
                contentType = ContentType.Any 
            )
        }
    }

    suspend fun fetchAndSaveObjetos() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/ab_objetos.json"
            val objetos: List<RemoteObjeto> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                objetos.forEach { obj ->
                    database.isaacDatabaseQueries.insertObjeto(
                        id = obj.id.toLong(),
                        nombre = obj.nombre,
                        descripcion = obj.descripcion,
                        tipo = obj.tipo
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchAndSaveConsumibles() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/ab_consumibles.json"
            val consumibles: List<RemoteConsumible> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                consumibles.forEach { cons ->
                    database.isaacDatabaseQueries.insertConsumible(
                        id = cons.id.toLong(),
                        nombre = cons.nombre,
                        descripcion = cons.descripcion,
                        tipo = cons.tipo
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchAndSavePersonajes() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/personajes_completos.json"
            val personajes: List<RemotePersonaje> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                personajes.forEach { per ->
                    database.isaacDatabaseQueries.insertPersonaje(
                        id = per.id.toLong(),
                        nombre = per.nombre,
                        es_tainted = if (per.es_tainted) 1L else 0L,
                        metodo_desbloqueo = per.metodo_desbloqueo
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchAndSaveMarcas() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/marcas.json"
            val marcas: List<RemoteMarca> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                marcas.forEach { mar ->
                    database.isaacDatabaseQueries.insertMarca(
                        id = mar.id.toLong(),
                        nombre = mar.nombre
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchAndSaveDesbloqueos() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/desbloqueos.json"
            val desbloqueos: List<RemoteDesbloqueo> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                desbloqueos.forEach { des ->
                    database.isaacDatabaseQueries.insertDesbloqueo(
                        personaje_id = des.personaje_id.toLong(),
                        marca_id = des.marca_id.toLong(),
                        objeto_id = des.objeto_id?.toLong(),
                        consumible_id = des.consumible_id?.toLong()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchAndSaveEstadisticas() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/refs/heads/main/estadisticas_personajes.json"
            val stats: List<RemoteEstadisticas> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                stats.forEach { s ->
                    database.isaacDatabaseQueries.insertEstadisticas(
                        personaje_id = s.personaje_id.toLong(),
                        salud = s.salud,
                        velocidad = s.velocidad,
                        lagrimas = s.lagrimas,
                        dano = s.dano,
                        rango = s.rango,
                        velocidad_disparo = s.velocidad_disparo,
                        suerte = s.suerte,
                        objeto_inicial_id = s.objeto_inicial_id?.toLong(),
                        consumible_inicial_id = s.consumible_inicial_id?.toLong()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun getAllObjetos(): List<RemoteObjeto> {
        return database.isaacDatabaseQueries.selectAllObjetos().executeAsList().map {
            RemoteObjeto(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion,
                tipo = it.tipo ?: ""
            )
        }.sortedBy { it.id }
    }

    fun getAllPersonajes(): List<RemotePersonaje> {
        return database.isaacDatabaseQueries.selectAllPersonajes().executeAsList().map {
            RemotePersonaje(
                id = it.id.toInt(),
                nombre = it.nombre,
                es_tainted = it.es_tainted == 1L,
                metodo_desbloqueo = it.metodo_desbloqueo
            )
        }.sortedBy { it.id }
    }

    fun getAllConsumibles(): List<RemoteConsumible> {
        return database.isaacDatabaseQueries.selectAllConsumibles().executeAsList().map {
            RemoteConsumible(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion,
                tipo = it.tipo
            )
        }.sortedBy { it.id }
    }

    fun getDesbloqueosByPersonaje(personajeId: Int): List<Pair<String, String>> {
        return database.isaacDatabaseQueries.getDesbloqueosByPersonaje(personajeId.toLong())
            .executeAsList()
            .map {
                val premio = it.objetoNombre ?: it.consumibleNombre ?: "Premio desconocido"
                it.marcaNombre to premio
            }
    }

    fun getEstadisticasByPersonaje(personajeId: Int): RemoteEstadisticas? {
        return database.isaacDatabaseQueries.getEstadisticasByPersonaje(personajeId.toLong())
            .executeAsOneOrNull()?.let {
                RemoteEstadisticas(
                    personaje_id = it.personaje_id.toInt(),
                    salud = it.salud,
                    velocidad = it.velocidad,
                    lagrimas = it.lagrimas,
                    dano = it.dano,
                    rango = it.rango,
                    velocidad_disparo = it.velocidad_disparo,
                    suerte = it.suerte,
                    objeto_inicial_id = it.objeto_inicial_id?.toInt(),
                    consumible_inicial_id = it.consumible_inicial_id?.toInt()
                )
            }
    }

    fun getAllObjetosCount(): Long {
        return database.isaacDatabaseQueries.selectAllObjetos().executeAsList().size.toLong()
    }

    fun getAllConsumiblesCount(): Long {
        return database.isaacDatabaseQueries.selectAllConsumibles().executeAsList().size.toLong()
    }

    fun getAllPersonajesCount(): Long {
        return database.isaacDatabaseQueries.selectAllPersonajes().executeAsList().size.toLong()
    }
}
