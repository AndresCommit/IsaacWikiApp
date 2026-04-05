package com.andres.wikitboiandres.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Double
import kotlin.Long
import kotlin.String

public class IsaacDatabaseQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAllPersonajes(mapper: (
    id: Long,
    nombre: String,
    es_tainted: Long?,
    metodo_desbloqueo: String?,
  ) -> T): Query<T> = Query(525_042_678, arrayOf("Personajes"), driver, "IsaacDatabase.sq",
      "selectAllPersonajes",
      "SELECT Personajes.id, Personajes.nombre, Personajes.es_tainted, Personajes.metodo_desbloqueo FROM Personajes") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2),
      cursor.getString(3)
    )
  }

  public fun selectAllPersonajes(): Query<Personajes> = selectAllPersonajes { id, nombre,
      es_tainted, metodo_desbloqueo ->
    Personajes(
      id,
      nombre,
      es_tainted,
      metodo_desbloqueo
    )
  }

  public fun <T : Any> selectAllObjetos(mapper: (
    id: Long,
    nombre: String,
    descripcion: String,
    tipo: String?,
  ) -> T): Query<T> = Query(967_571_456, arrayOf("Objetos"), driver, "IsaacDatabase.sq",
      "selectAllObjetos",
      "SELECT Objetos.id, Objetos.nombre, Objetos.descripcion, Objetos.tipo FROM Objetos") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)
    )
  }

  public fun selectAllObjetos(): Query<Objetos> = selectAllObjetos { id, nombre, descripcion,
      tipo ->
    Objetos(
      id,
      nombre,
      descripcion,
      tipo
    )
  }

  public fun <T : Any> selectAllConsumibles(mapper: (
    id: Long,
    nombre: String,
    descripcion: String,
    tipo: String,
  ) -> T): Query<T> = Query(-1_334_684_882, arrayOf("Consumibles"), driver, "IsaacDatabase.sq",
      "selectAllConsumibles",
      "SELECT Consumibles.id, Consumibles.nombre, Consumibles.descripcion, Consumibles.tipo FROM Consumibles") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!
    )
  }

  public fun selectAllConsumibles(): Query<Consumibles> = selectAllConsumibles { id, nombre,
      descripcion, tipo ->
    Consumibles(
      id,
      nombre,
      descripcion,
      tipo
    )
  }

  public fun <T : Any> selectAllMarcas(mapper: (id: Long, nombre: String) -> T): Query<T> =
      Query(250_360_641, arrayOf("Marcas"), driver, "IsaacDatabase.sq", "selectAllMarcas",
      "SELECT Marcas.id, Marcas.nombre FROM Marcas") { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!
    )
  }

  public fun selectAllMarcas(): Query<Marcas> = selectAllMarcas { id, nombre ->
    Marcas(
      id,
      nombre
    )
  }

  public fun <T : Any> getDesbloqueosByPersonaje(personaje_id: Long, mapper: (
    personajeNombre: String,
    marcaNombre: String,
    objetoNombre: String?,
    consumibleNombre: String?,
  ) -> T): Query<T> = GetDesbloqueosByPersonajeQuery(personaje_id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2),
      cursor.getString(3)
    )
  }

  public fun getDesbloqueosByPersonaje(personaje_id: Long): Query<GetDesbloqueosByPersonaje> =
      getDesbloqueosByPersonaje(personaje_id) { personajeNombre, marcaNombre, objetoNombre,
      consumibleNombre ->
    GetDesbloqueosByPersonaje(
      personajeNombre,
      marcaNombre,
      objetoNombre,
      consumibleNombre
    )
  }

  public fun <T : Any> getEstadisticasByPersonaje(personaje_id: Long, mapper: (
    personaje_id: Long,
    salud: String,
    velocidad: Double,
    lagrimas: Double,
    dano: Double,
    rango: Double,
    velocidad_disparo: Double,
    suerte: Double,
    objeto_inicial_id: Long?,
    consumible_inicial_id: Long?,
  ) -> T): Query<T> = GetEstadisticasByPersonajeQuery(personaje_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getDouble(2)!!,
      cursor.getDouble(3)!!,
      cursor.getDouble(4)!!,
      cursor.getDouble(5)!!,
      cursor.getDouble(6)!!,
      cursor.getDouble(7)!!,
      cursor.getLong(8),
      cursor.getLong(9)
    )
  }

  public fun getEstadisticasByPersonaje(personaje_id: Long): Query<Estadisticas_Personaje> =
      getEstadisticasByPersonaje(personaje_id) { personaje_id_, salud, velocidad, lagrimas, dano,
      rango, velocidad_disparo, suerte, objeto_inicial_id, consumible_inicial_id ->
    Estadisticas_Personaje(
      personaje_id_,
      salud,
      velocidad,
      lagrimas,
      dano,
      rango,
      velocidad_disparo,
      suerte,
      objeto_inicial_id,
      consumible_inicial_id
    )
  }

  public fun insertPersonaje(
    id: Long?,
    nombre: String,
    es_tainted: Long?,
    metodo_desbloqueo: String?,
  ) {
    driver.execute(335_351_123, """
        |INSERT OR REPLACE INTO Personajes(id, nombre, es_tainted, metodo_desbloqueo)
        |VALUES (?, ?, ?, ?)
        """.trimMargin(), 4) {
          bindLong(0, id)
          bindString(1, nombre)
          bindLong(2, es_tainted)
          bindString(3, metodo_desbloqueo)
        }
    notifyQueries(335_351_123) { emit ->
      emit("Personajes")
    }
  }

  public fun insertObjeto(
    id: Long?,
    nombre: String,
    descripcion: String,
    tipo: String?,
  ) {
    driver.execute(-1_266_167_459, """
        |INSERT OR REPLACE INTO Objetos(id, nombre, descripcion, tipo)
        |VALUES (?, ?, ?, ?)
        """.trimMargin(), 4) {
          bindLong(0, id)
          bindString(1, nombre)
          bindString(2, descripcion)
          bindString(3, tipo)
        }
    notifyQueries(-1_266_167_459) { emit ->
      emit("Objetos")
    }
  }

  public fun insertConsumible(
    id: Long,
    nombre: String,
    descripcion: String,
    tipo: String,
  ) {
    driver.execute(960_758_639, """
        |INSERT OR REPLACE INTO Consumibles(id, nombre, descripcion, tipo)
        |VALUES (?, ?, ?, ?)
        """.trimMargin(), 4) {
          bindLong(0, id)
          bindString(1, nombre)
          bindString(2, descripcion)
          bindString(3, tipo)
        }
    notifyQueries(960_758_639) { emit ->
      emit("Consumibles")
    }
  }

  public fun insertMarca(id: Long?, nombre: String) {
    driver.execute(1_204_212_648, """
        |INSERT OR REPLACE INTO Marcas(id, nombre)
        |VALUES (?, ?)
        """.trimMargin(), 2) {
          bindLong(0, id)
          bindString(1, nombre)
        }
    notifyQueries(1_204_212_648) { emit ->
      emit("Marcas")
    }
  }

  public fun insertDesbloqueo(
    personaje_id: Long,
    marca_id: Long,
    objeto_id: Long?,
    consumible_id: Long?,
  ) {
    driver.execute(-675_181_963, """
        |INSERT OR REPLACE INTO Desbloqueos(personaje_id, marca_id, objeto_id, consumible_id)
        |VALUES (?, ?, ?, ?)
        """.trimMargin(), 4) {
          bindLong(0, personaje_id)
          bindLong(1, marca_id)
          bindLong(2, objeto_id)
          bindLong(3, consumible_id)
        }
    notifyQueries(-675_181_963) { emit ->
      emit("Desbloqueos")
    }
  }

  public fun insertEstadisticas(
    personaje_id: Long?,
    salud: String,
    velocidad: Double,
    lagrimas: Double,
    dano: Double,
    rango: Double,
    velocidad_disparo: Double,
    suerte: Double,
    objeto_inicial_id: Long?,
    consumible_inicial_id: Long?,
  ) {
    driver.execute(11_513_633, """
        |INSERT OR REPLACE INTO Estadisticas_Personaje(personaje_id, salud, velocidad, lagrimas, dano, rango, velocidad_disparo, suerte, objeto_inicial_id, consumible_inicial_id)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 10) {
          bindLong(0, personaje_id)
          bindString(1, salud)
          bindDouble(2, velocidad)
          bindDouble(3, lagrimas)
          bindDouble(4, dano)
          bindDouble(5, rango)
          bindDouble(6, velocidad_disparo)
          bindDouble(7, suerte)
          bindLong(8, objeto_inicial_id)
          bindLong(9, consumible_inicial_id)
        }
    notifyQueries(11_513_633) { emit ->
      emit("Estadisticas_Personaje")
    }
  }

  private inner class GetDesbloqueosByPersonajeQuery<out T : Any>(
    public val personaje_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Personajes", "Marcas", "Objetos", "Consumibles", "Desbloqueos", listener =
          listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Personajes", "Marcas", "Objetos", "Consumibles", "Desbloqueos",
          listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_016_237_415, """
    |SELECT
    |    Personajes.nombre AS personajeNombre,
    |    Marcas.nombre AS marcaNombre,
    |    Objetos.nombre AS objetoNombre,
    |    Consumibles.nombre AS consumibleNombre
    |FROM Desbloqueos
    |JOIN Personajes ON Desbloqueos.personaje_id = Personajes.id
    |JOIN Marcas ON Desbloqueos.marca_id = Marcas.id
    |LEFT JOIN Objetos ON Desbloqueos.objeto_id = Objetos.id
    |LEFT JOIN Consumibles ON Desbloqueos.consumible_id = Consumibles.id
    |WHERE Desbloqueos.personaje_id = ?
    """.trimMargin(), mapper, 1) {
      bindLong(0, personaje_id)
    }

    override fun toString(): String = "IsaacDatabase.sq:getDesbloqueosByPersonaje"
  }

  private inner class GetEstadisticasByPersonajeQuery<out T : Any>(
    public val personaje_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Estadisticas_Personaje", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Estadisticas_Personaje", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(891_622_280,
        """SELECT Estadisticas_Personaje.personaje_id, Estadisticas_Personaje.salud, Estadisticas_Personaje.velocidad, Estadisticas_Personaje.lagrimas, Estadisticas_Personaje.dano, Estadisticas_Personaje.rango, Estadisticas_Personaje.velocidad_disparo, Estadisticas_Personaje.suerte, Estadisticas_Personaje.objeto_inicial_id, Estadisticas_Personaje.consumible_inicial_id FROM Estadisticas_Personaje WHERE personaje_id = ?""",
        mapper, 1) {
      bindLong(0, personaje_id)
    }

    override fun toString(): String = "IsaacDatabase.sq:getEstadisticasByPersonaje"
  }
}
