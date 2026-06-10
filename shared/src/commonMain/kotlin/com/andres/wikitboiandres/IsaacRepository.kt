package com.andres.wikitboiandres

import com.andres.wikitboiandres.db.IsaacDatabase
import com.andres.wikitboiandres.models.SteamAchievementResponse
import com.andres.wikitboiandres.network.SteamApiService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RemoteObjeto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val tipo: String,
    val calidad: Int = 0
)

@Serializable
data class RemoteConsumible(
    val uid: Int,
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
    val logro_id: Int? = null
)

@Serializable
data class RemotePersonaje(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val es_tainted: Boolean,
    val metodo_desbloqueo: String? = null
)

@Serializable
data class RemoteLogro(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val desbloqueado: Boolean = false,
    val desbloquea_personaje_id: Int? = null,
    val desbloquea_objeto_id: Int? = null,
    val desbloquea_consumible_id: Int? = null
)

@Serializable
data class RemoteEstadisticas(
    val personaje_id: Int,
    val corazones_rojos: Int = 0,
    val corazones_alma: Int = 0,
    val corazones_negros: Int = 0,
    val corazones_hueso: Int = 0,
    val corazones_moneda: Int = 0,
    val manto_sagrado: Boolean = false,
    val salud_aleatoria: Boolean = false,
    val velocidad: Double,
    val lagrimas: Double,
    val dano: Double,
    val rango: Double,
    val velocidad_disparo: Double,
    val suerte: Double,
    val objeto_inicial_id: Int? = null,
    val consumible_inicial_id: Int? = null
)

@Serializable
data class RemoteTransformacion(
    val id: Int,
    val nombre: String,
    val descripcion: String
)

@Serializable
data class RemoteTransformacionObjeto(
    val transformacion_id: Int,
    val objeto_id: Int
)

@Serializable
data class RemoteMaldicion(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val notas: String? = null
)

@Serializable
data class RemoteSinergia(
    val obj_a_id: Int,
    val obj_b_id: Int,
    val descripcion_sinergia: String
)

@Serializable
data class RemoteSala(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val objetos_ids: List<String> = emptyList()
)

data class SinergiaInfo(
    val objetoRelacionadoId: Int,
    val descripcion: String
)

data class DesbloqueoInfo(
    val marcaNombre: String,
    val premioNombre: String,
    val premioId: Int,
    val esObjeto: Boolean,
    val consumibleTipo: String? = null,
    val pId: Int? = null,
    val logroId: Int? = null,
    val logroDescripcion: String? = null,
    val desbloqueado: Boolean = false
)

class IsaacRepository(
    private val database: IsaacDatabase,
    private val steamApiService: SteamApiService
) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            val jsonConfig = Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            }
            json(jsonConfig, ContentType.Application.Json)
            json(jsonConfig, ContentType.Text.Plain)
        }
    }

    suspend fun fetchAndSaveObjetos() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/objetos.json"
            val objetos: List<RemoteObjeto> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                objetos.forEach { obj ->
                    database.isaacDatabaseQueries.insertObjeto(
                        obj.id.toLong(),
                        obj.nombre,
                        obj.descripcion,
                        obj.tipo,
                        obj.calidad.toLong()
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
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/consumibles.json"
            val consumibles: List<RemoteConsumible> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                consumibles.forEach { cons ->
                    database.isaacDatabaseQueries.insertConsumible(
                        cons.uid.toLong(),
                        cons.id.toLong(),
                        cons.nombre,
                        cons.descripcion,
                        cons.tipo
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
                        per.id.toLong(),
                        per.nombre,
                        per.descripcion,
                        if (per.es_tainted) 1L else 0L,
                        per.metodo_desbloqueo
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
                        mar.id.toLong(),
                        mar.nombre
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchAndSaveLogros() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/logros.json"
            val logros: List<RemoteLogro> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                logros.forEach { logro ->
                    database.isaacDatabaseQueries.insertLogro(
                        logro.id.toLong(),
                        logro.nombre,
                        logro.descripcion,
                        logro.desbloqueado,
                        logro.desbloquea_personaje_id?.toLong(),
                        logro.desbloquea_objeto_id?.toLong(),
                        logro.desbloquea_consumible_id?.toLong()
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
                        des.personaje_id.toLong(),
                        des.marca_id.toLong(),
                        des.logro_id?.toLong()
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
                        s.personaje_id.toLong(),
                        s.corazones_rojos.toLong(),
                        s.corazones_alma.toLong(),
                        s.corazones_negros.toLong(),
                        s.corazones_hueso.toLong(),
                        s.corazones_moneda.toLong(),
                        s.manto_sagrado,
                        s.salud_aleatoria,
                        s.velocidad,
                        s.lagrimas,
                        s.dano,
                        s.rango,
                        s.velocidad_disparo,
                        s.suerte,
                        s.objeto_inicial_id?.toLong(),
                        s.consumible_inicial_id?.toLong()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchAndSaveTransformaciones() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/transformaciones.json"
            val trans: List<RemoteTransformacion> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                trans.forEach { t ->
                    database.isaacDatabaseQueries.insertTransformacion(
                        t.id.toLong(),
                        t.nombre,
                        t.descripcion
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchAndSaveTransformacionObjeto() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/transformacion_objeto.json"
            val links: List<RemoteTransformacionObjeto> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                links.forEach { l ->
                    database.isaacDatabaseQueries.insertTransformacionObjeto(
                        l.transformacion_id.toLong(),
                        l.objeto_id.toLong()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchAndSaveMaldiciones() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/maldiciones.json"
            val curses: List<RemoteMaldicion> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                curses.forEach { c ->
                    database.isaacDatabaseQueries.insertMaldicion(
                        c.id.toLong(),
                        c.nombre,
                        c.descripcion,
                        c.notas
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchAndSaveSinergias() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/sinergias.json"
            val sinergias: List<RemoteSinergia> = client.get(url).body()

            database.isaacDatabaseQueries.transaction {
                sinergias.forEach { sin ->
                    database.isaacDatabaseQueries.insertSinergia(
                        sin.obj_a_id.toLong(),
                        sin.obj_b_id.toLong(),
                        sin.descripcion_sinergia
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchAndSaveSalas() {
        try {
            val url = "https://raw.githubusercontent.com/AndresCommit/isaac-resources/main/salas-items"
            val response: String = client.get(url).bodyAsText()
            val cleanJson = if (response.trim().startsWith("[")) response else "[$response]"
            val json = Json { ignoreUnknownKeys = true }
            val salas: List<RemoteSala> = json.decodeFromString(cleanJson)

            database.isaacDatabaseQueries.transaction {
                salas.forEach { sala ->
                    database.isaacDatabaseQueries.insertSala(
                        sala.id.toLong(),
                        sala.nombre,
                        sala.descripcion
                    )
                    sala.objetos_ids.forEach { objIdStr ->
                        objIdStr.toLongOrNull()?.let { objId ->
                            database.isaacDatabaseQueries.insertSalaObjeto(sala.id.toLong(), objId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun updateLogroStatus(id: Int, unlocked: Boolean) {
        database.isaacDatabaseQueries.updateLogroStatus(unlocked, id.toLong())
    }

    fun getAllObjetos(): List<RemoteObjeto> {
        return database.isaacDatabaseQueries.selectAllObjetos().executeAsList().map {
            RemoteObjeto(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion,
                tipo = it.tipo ?: "",
                calidad = it.calidad?.toInt() ?: 0
            )
        }.sortedBy { it.id }
    }

    fun getAllPersonajes(): List<RemotePersonaje> {
        return database.isaacDatabaseQueries.selectAllPersonajes().executeAsList().map {
            RemotePersonaje(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion,
                es_tainted = it.es_tainted == 1L,
                metodo_desbloqueo = it.metodo_desbloqueo
            )
        }.sortedBy { it.id }
    }

    fun getAllConsumibles(): List<RemoteConsumible> {
        return database.isaacDatabaseQueries.selectAllConsumibles().executeAsList().map {
            RemoteConsumible(
                uid = it.uid.toInt(),
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion,
                tipo = it.tipo
            )
        }.sortedBy { it.uid }
    }

    fun getAllTransformaciones(): List<RemoteTransformacion> {
        return database.isaacDatabaseQueries.selectAllTransformaciones().executeAsList().map {
            RemoteTransformacion(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion
            )
        }.sortedBy { it.nombre }
    }

    fun getAllLogros(): List<RemoteLogro> {
        return database.isaacDatabaseQueries.selectAllLogros().executeAsList().map {
            RemoteLogro(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion,
                desbloqueado = it.desbloqueado ?: false,
                desbloquea_personaje_id = it.desbloquea_personaje_id?.toInt(),
                desbloquea_objeto_id = it.desbloquea_objeto_id?.toInt(),
                desbloquea_consumible_id = it.desbloquea_consumible_id?.toInt()
            )
        }.sortedBy { it.id }
    }

    fun getAllMaldiciones(): List<RemoteMaldicion> {
        return database.isaacDatabaseQueries.selectAllMaldiciones().executeAsList().map {
            RemoteMaldicion(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion,
                notas = it.notas
            )
        }.sortedBy { it.id }
    }

    fun getAllSalas(): List<RemoteSala> {
        return database.isaacDatabaseQueries.selectAllSalas().executeAsList().map {
            RemoteSala(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion
            )
        }.sortedBy { it.nombre }
    }

    fun getObjetosByTransformacion(transformacionId: Int): List<RemoteObjeto> {
        return database.isaacDatabaseQueries.getObjetosByTransformacion(transformacionId.toLong()).executeAsList().map {
            RemoteObjeto(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion,
                tipo = it.tipo ?: "",
                calidad = it.calidad?.toInt() ?: 0
            )
        }
    }

    fun getDesbloqueosByPersonaje(personajeId: Int): List<DesbloqueoInfo> {
        return database.isaacDatabaseQueries.getDesbloqueosByPersonaje(personajeId.toLong())
            .executeAsList()
            .map {
                DesbloqueoInfo(
                    marcaNombre = it.marcaNombre,
                    premioNombre = it.logroNombre ?: "Desbloqueo desconocido",
                    premioId = (it.objetoId ?: it.consumibleId ?: it.pId ?: 0).toInt(),
                    esObjeto = it.objetoId != null,
                    consumibleTipo = it.consumibleTipo,
                    pId = it.pId?.toInt(),
                    logroId = it.logroId?.toInt(),
                    logroDescripcion = it.logroDescripcion,
                    desbloqueado = it.desbloqueado ?: false
                )
            }
    }

    fun getEstadisticasByPersonaje(personajeId: Int): RemoteEstadisticas? {
        return database.isaacDatabaseQueries.getEstadisticasByPersonaje(personajeId.toLong())
            .executeAsOneOrNull()?.let {
                RemoteEstadisticas(
                    personaje_id = it.personaje_id.toInt(),
                    corazones_rojos = it.corazones_rojos.toInt(),
                    corazones_alma = it.corazones_alma.toInt(),
                    corazones_negros = it.corazones_negros.toInt(),
                    corazones_hueso = it.corazones_hueso.toInt(),
                    corazones_moneda = it.corazones_moneda.toInt(),
                    manto_sagrado = it.manto_sagrado ?: false,
                    salud_aleatoria = it.salud_aleatoria ?: false,
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

    fun getObjetoById(id: Int): RemoteObjeto? {
        return database.isaacDatabaseQueries.getObjetoById(id.toLong()).executeAsOneOrNull()?.let {
            RemoteObjeto(it.id.toInt(), it.nombre, it.descripcion, it.tipo ?: "", it.calidad?.toInt() ?: 0)
        }
    }

    fun getPersonajeById(id: Int): RemotePersonaje? {
        return database.isaacDatabaseQueries.getPersonajeById(id.toLong()).executeAsOneOrNull()?.let {
            RemotePersonaje(it.id.toInt(), it.nombre, it.descripcion, it.es_tainted == 1L, it.metodo_desbloqueo)
        }
    }

    fun getConsumibleById(id: Int): RemoteConsumible? {
        return database.isaacDatabaseQueries.getConsumibleById(id.toLong()).executeAsOneOrNull()?.let {
            RemoteConsumible(it.uid.toInt(), it.id.toInt(), it.nombre, it.descripcion, it.tipo)
        }
    }

    fun getConsumibleByUid(uid: Int): RemoteConsumible? {
        return database.isaacDatabaseQueries.getConsumibleByUid(uid.toLong()).executeAsOneOrNull()?.let {
            RemoteConsumible(it.uid.toInt(), it.id.toInt(), it.nombre, it.descripcion, it.tipo)
        }
    }

    fun getConsumibleByIdAndType(id: Int, tipo: String): RemoteConsumible? {
        return database.isaacDatabaseQueries.getConsumibleByIdAndType(id.toLong(), tipo).executeAsOneOrNull()?.let {
            RemoteConsumible(it.uid.toInt(), it.id.toInt(), it.nombre, it.descripcion, it.tipo)
        }
    }

    fun getLogroById(id: Int): RemoteLogro? {
        return database.isaacDatabaseQueries.getLogroById(id.toLong()).executeAsOneOrNull()?.let {
            RemoteLogro(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion,
                desbloqueado = it.desbloqueado ?: false,
                desbloquea_personaje_id = it.desbloquea_personaje_id?.toInt(),
                desbloquea_objeto_id = it.desbloquea_objeto_id?.toInt(),
                desbloquea_consumible_id = it.desbloquea_consumible_id?.toInt()
            )
        }
    }

    fun getLogrosByRewardObjeto(objetoId: Int): List<RemoteLogro> {
        return database.isaacDatabaseQueries.getLogrosByRewardObjeto(objetoId.toLong()).executeAsList().map {
            RemoteLogro(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion,
                desbloqueado = it.desbloqueado ?: false,
                desbloquea_personaje_id = it.desbloquea_personaje_id?.toInt(),
                desbloquea_objeto_id = it.desbloquea_objeto_id?.toInt(),
                desbloquea_consumible_id = it.desbloquea_consumible_id?.toInt()
            )
        }
    }

    fun getSinergiasByObjeto(objetoId: Int): List<SinergiaInfo> {
        return database.isaacDatabaseQueries.getSinergiasByObjeto(objetoId.toLong()).executeAsList().map {
            SinergiaInfo(
                objetoRelacionadoId = it.objetoRelacionadoId.toInt(),
                descripcion = it.descripcion
            )
        }
    }

    fun getSalasByObjeto(objetoId: Int): List<RemoteSala> {
        return database.isaacDatabaseQueries.getSalasByObjeto(objetoId.toLong()).executeAsList().map {
            RemoteSala(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion
            )
        }
    }

    fun getObjetosBySala(salaId: Int): List<RemoteObjeto> {
        return database.isaacDatabaseQueries.getObjetosBySala(salaId.toLong()).executeAsList().map {
            RemoteObjeto(
                id = it.id.toInt(),
                nombre = it.nombre,
                descripcion = it.descripcion,
                tipo = it.tipo ?: "",
                calidad = it.calidad?.toInt() ?: 0
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

    fun getAllTransformacionesCount(): Long {
        return database.isaacDatabaseQueries.selectAllTransformaciones().executeAsList().size.toLong()
    }

    fun getAllLogrosCount(): Long {
        return database.isaacDatabaseQueries.selectAllLogros().executeAsList().size.toLong()
    }

    fun getAllMaldicionesCount(): Long {
        return database.isaacDatabaseQueries.selectAllMaldiciones().executeAsList().size.toLong()
    }

    fun getAllSalasCount(): Long {
        return database.isaacDatabaseQueries.selectAllSalas().executeAsList().size.toLong()
    }

    suspend fun syncAchievementsWithSteam(apiKey: String, steamId: String): Result<Unit> {
        return try {
            val steamResponse = steamApiService.getPlayerAchievements(apiKey, steamId)
            val playerStats = steamResponse.playerstats
            
            if (playerStats != null && playerStats.success) {
                val achievements = playerStats.achievements ?: emptyList()
                
                database.isaacDatabaseQueries.transaction {
                    achievements.forEach { steamAch ->
                        if (steamAch.achieved == 1) {
                            steamAch.apiname.toIntOrNull()?.let { numericId ->
                                database.isaacDatabaseQueries.updateLogroStatus(true, numericId.toLong())
                            }
                        }
                    }
                }
                Result.success(Unit)
            } else {
                val errorMsg = playerStats?.error ?: "Perfil privado o error en Steam."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
