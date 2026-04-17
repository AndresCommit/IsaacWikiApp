package com.andres.wikitboiandres.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.Long
import kotlin.String

public class IsaacDatabaseQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAllPersonajes(mapper: (
    id: Long,
    nombre: String,
    descripcion: String?,
    es_tainted: Long?,
    metodo_desbloqueo: String?,
  ) -> T): Query<T> = Query(525_042_678, arrayOf("Personajes"), driver, "IsaacDatabase.sq",
      "selectAllPersonajes",
      "SELECT Personajes.id, Personajes.nombre, Personajes.descripcion, Personajes.es_tainted, Personajes.metodo_desbloqueo FROM Personajes") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2),
      cursor.getLong(3),
      cursor.getString(4)
    )
  }

  public fun selectAllPersonajes(): Query<Personajes> = selectAllPersonajes { id, nombre,
      descripcion, es_tainted, metodo_desbloqueo ->
    Personajes(
      id,
      nombre,
      descripcion,
      es_tainted,
      metodo_desbloqueo
    )
  }

  public fun <T : Any> getPersonajeById(id: Long, mapper: (
    id: Long,
    nombre: String,
    descripcion: String?,
    es_tainted: Long?,
    metodo_desbloqueo: String?,
  ) -> T): Query<T> = GetPersonajeByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2),
      cursor.getLong(3),
      cursor.getString(4)
    )
  }

  public fun getPersonajeById(id: Long): Query<Personajes> = getPersonajeById(id) { id_, nombre,
      descripcion, es_tainted, metodo_desbloqueo ->
    Personajes(
      id_,
      nombre,
      descripcion,
      es_tainted,
      metodo_desbloqueo
    )
  }

  public fun <T : Any> selectAllObjetos(mapper: (
    id: Long,
    nombre: String,
    descripcion: String,
    tipo: String?,
    calidad: Long?,
  ) -> T): Query<T> = Query(967_571_456, arrayOf("Objetos"), driver, "IsaacDatabase.sq",
      "selectAllObjetos",
      "SELECT Objetos.id, Objetos.nombre, Objetos.descripcion, Objetos.tipo, Objetos.calidad FROM Objetos") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getLong(4)
    )
  }

  public fun selectAllObjetos(): Query<Objetos> = selectAllObjetos { id, nombre, descripcion, tipo,
      calidad ->
    Objetos(
      id,
      nombre,
      descripcion,
      tipo,
      calidad
    )
  }

  public fun <T : Any> getObjetoById(id: Long, mapper: (
    id: Long,
    nombre: String,
    descripcion: String,
    tipo: String?,
    calidad: Long?,
  ) -> T): Query<T> = GetObjetoByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getLong(4)
    )
  }

  public fun getObjetoById(id: Long): Query<Objetos> = getObjetoById(id) { id_, nombre, descripcion,
      tipo, calidad ->
    Objetos(
      id_,
      nombre,
      descripcion,
      tipo,
      calidad
    )
  }

  public fun <T : Any> selectAllConsumibles(mapper: (
    uid: Long,
    id: Long,
    nombre: String,
    descripcion: String,
    tipo: String,
  ) -> T): Query<T> = Query(-1_334_684_882, arrayOf("Consumibles"), driver, "IsaacDatabase.sq",
      "selectAllConsumibles",
      "SELECT Consumibles.uid, Consumibles.id, Consumibles.nombre, Consumibles.descripcion, Consumibles.tipo FROM Consumibles") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!
    )
  }

  public fun selectAllConsumibles(): Query<Consumibles> = selectAllConsumibles { uid, id, nombre,
      descripcion, tipo ->
    Consumibles(
      uid,
      id,
      nombre,
      descripcion,
      tipo
    )
  }

  public fun <T : Any> getConsumibleById(id: Long, mapper: (
    uid: Long,
    id: Long,
    nombre: String,
    descripcion: String,
    tipo: String,
  ) -> T): Query<T> = GetConsumibleByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!
    )
  }

  public fun getConsumibleById(id: Long): Query<Consumibles> = getConsumibleById(id) { uid, id_,
      nombre, descripcion, tipo ->
    Consumibles(
      uid,
      id_,
      nombre,
      descripcion,
      tipo
    )
  }

  public fun <T : Any> getConsumibleByUid(uid: Long, mapper: (
    uid: Long,
    id: Long,
    nombre: String,
    descripcion: String,
    tipo: String,
  ) -> T): Query<T> = GetConsumibleByUidQuery(uid) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!
    )
  }

  public fun getConsumibleByUid(uid: Long): Query<Consumibles> = getConsumibleByUid(uid) { uid_, id,
      nombre, descripcion, tipo ->
    Consumibles(
      uid_,
      id,
      nombre,
      descripcion,
      tipo
    )
  }

  public fun <T : Any> getConsumibleByIdAndType(
    id: Long,
    tipo: String,
    mapper: (
      uid: Long,
      id: Long,
      nombre: String,
      descripcion: String,
      tipo: String,
    ) -> T,
  ): Query<T> = GetConsumibleByIdAndTypeQuery(id, tipo) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!
    )
  }

  public fun getConsumibleByIdAndType(id: Long, tipo: String): Query<Consumibles> =
      getConsumibleByIdAndType(id, tipo) { uid, id_, nombre, descripcion, tipo_ ->
    Consumibles(
      uid,
      id_,
      nombre,
      descripcion,
      tipo_
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

  public fun <T : Any> selectAllLogros(mapper: (
    id: Long,
    nombre: String,
    descripcion: String,
    desbloqueado: Boolean?,
    desbloquea_personaje_id: Long?,
    desbloquea_objeto_id: Long?,
    desbloquea_consumible_id: Long?,
  ) -> T): Query<T> = Query(234_347_932, arrayOf("Logros"), driver, "IsaacDatabase.sq",
      "selectAllLogros",
      "SELECT Logros.id, Logros.nombre, Logros.descripcion, Logros.desbloqueado, Logros.desbloquea_personaje_id, Logros.desbloquea_objeto_id, Logros.desbloquea_consumible_id FROM Logros") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getBoolean(3),
      cursor.getLong(4),
      cursor.getLong(5),
      cursor.getLong(6)
    )
  }

  public fun selectAllLogros(): Query<Logros> = selectAllLogros { id, nombre, descripcion,
      desbloqueado, desbloquea_personaje_id, desbloquea_objeto_id, desbloquea_consumible_id ->
    Logros(
      id,
      nombre,
      descripcion,
      desbloqueado,
      desbloquea_personaje_id,
      desbloquea_objeto_id,
      desbloquea_consumible_id
    )
  }

  public fun <T : Any> getLogroById(id: Long, mapper: (
    id: Long,
    nombre: String,
    descripcion: String,
    desbloqueado: Boolean?,
    desbloquea_personaje_id: Long?,
    desbloquea_objeto_id: Long?,
    desbloquea_consumible_id: Long?,
  ) -> T): Query<T> = GetLogroByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getBoolean(3),
      cursor.getLong(4),
      cursor.getLong(5),
      cursor.getLong(6)
    )
  }

  public fun getLogroById(id: Long): Query<Logros> = getLogroById(id) { id_, nombre, descripcion,
      desbloqueado, desbloquea_personaje_id, desbloquea_objeto_id, desbloquea_consumible_id ->
    Logros(
      id_,
      nombre,
      descripcion,
      desbloqueado,
      desbloquea_personaje_id,
      desbloquea_objeto_id,
      desbloquea_consumible_id
    )
  }

  public fun <T : Any> getLogrosByRewardObjeto(desbloquea_objeto_id: Long?, mapper: (
    id: Long,
    nombre: String,
    descripcion: String,
    desbloqueado: Boolean?,
    desbloquea_personaje_id: Long?,
    desbloquea_objeto_id: Long?,
    desbloquea_consumible_id: Long?,
  ) -> T): Query<T> = GetLogrosByRewardObjetoQuery(desbloquea_objeto_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getBoolean(3),
      cursor.getLong(4),
      cursor.getLong(5),
      cursor.getLong(6)
    )
  }

  public fun getLogrosByRewardObjeto(desbloquea_objeto_id: Long?): Query<Logros> =
      getLogrosByRewardObjeto(desbloquea_objeto_id) { id, nombre, descripcion, desbloqueado,
      desbloquea_personaje_id, desbloquea_objeto_id_, desbloquea_consumible_id ->
    Logros(
      id,
      nombre,
      descripcion,
      desbloqueado,
      desbloquea_personaje_id,
      desbloquea_objeto_id_,
      desbloquea_consumible_id
    )
  }

  public fun <T : Any> getDesbloqueosByPersonaje(personaje_id: Long, mapper: (
    personajeNombre: String,
    marcaId: Long,
    marcaNombre: String,
    logroNombre: String?,
    logroId: Long?,
    logroDescripcion: String?,
    desbloqueado: Boolean?,
    objetoId: Long?,
    consumibleId: Long?,
    pId: Long?,
    consumibleTipo: String?,
  ) -> T): Query<T> = GetDesbloqueosByPersonajeQuery(personaje_id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getLong(4),
      cursor.getString(5),
      cursor.getBoolean(6),
      cursor.getLong(7),
      cursor.getLong(8),
      cursor.getLong(9),
      cursor.getString(10)
    )
  }

  public fun getDesbloqueosByPersonaje(personaje_id: Long): Query<GetDesbloqueosByPersonaje> =
      getDesbloqueosByPersonaje(personaje_id) { personajeNombre, marcaId, marcaNombre, logroNombre,
      logroId, logroDescripcion, desbloqueado, objetoId, consumibleId, pId, consumibleTipo ->
    GetDesbloqueosByPersonaje(
      personajeNombre,
      marcaId,
      marcaNombre,
      logroNombre,
      logroId,
      logroDescripcion,
      desbloqueado,
      objetoId,
      consumibleId,
      pId,
      consumibleTipo
    )
  }

  public fun <T : Any> getEstadisticasByPersonaje(personaje_id: Long, mapper: (
    personaje_id: Long,
    corazones_rojos: Long,
    corazones_alma: Long,
    corazones_negros: Long,
    corazones_hueso: Long,
    corazones_moneda: Long,
    manto_sagrado: Boolean?,
    salud_aleatoria: Boolean?,
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
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getLong(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!,
      cursor.getBoolean(6),
      cursor.getBoolean(7),
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getDouble(10)!!,
      cursor.getDouble(11)!!,
      cursor.getDouble(12)!!,
      cursor.getDouble(13)!!,
      cursor.getLong(14),
      cursor.getLong(15)
    )
  }

  public fun getEstadisticasByPersonaje(personaje_id: Long): Query<Estadisticas_Personaje> =
      getEstadisticasByPersonaje(personaje_id) { personaje_id_, corazones_rojos, corazones_alma,
      corazones_negros, corazones_hueso, corazones_moneda, manto_sagrado, salud_aleatoria,
      velocidad, lagrimas, dano, rango, velocidad_disparo, suerte, objeto_inicial_id,
      consumible_inicial_id ->
    Estadisticas_Personaje(
      personaje_id_,
      corazones_rojos,
      corazones_alma,
      corazones_negros,
      corazones_hueso,
      corazones_moneda,
      manto_sagrado,
      salud_aleatoria,
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

  public fun <T : Any> selectAllTransformaciones(mapper: (
    id: Long,
    nombre: String,
    descripcion: String,
  ) -> T): Query<T> = Query(-95_815_694, arrayOf("Transformaciones"), driver, "IsaacDatabase.sq",
      "selectAllTransformaciones",
      "SELECT Transformaciones.id, Transformaciones.nombre, Transformaciones.descripcion FROM Transformaciones") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!
    )
  }

  public fun selectAllTransformaciones(): Query<Transformaciones> = selectAllTransformaciones { id,
      nombre, descripcion ->
    Transformaciones(
      id,
      nombre,
      descripcion
    )
  }

  public fun <T : Any> getObjetosByTransformacion(transformacion_id: Long, mapper: (
    id: Long,
    nombre: String,
    descripcion: String,
    tipo: String?,
    calidad: Long?,
  ) -> T): Query<T> = GetObjetosByTransformacionQuery(transformacion_id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getLong(4)
    )
  }

  public fun getObjetosByTransformacion(transformacion_id: Long): Query<Objetos> =
      getObjetosByTransformacion(transformacion_id) { id, nombre, descripcion, tipo, calidad ->
    Objetos(
      id,
      nombre,
      descripcion,
      tipo,
      calidad
    )
  }

  public fun <T : Any> selectAllMaldiciones(mapper: (
    id: Long,
    nombre: String,
    descripcion: String,
    notas: String?,
  ) -> T): Query<T> = Query(1_523_405_574, arrayOf("Maldiciones"), driver, "IsaacDatabase.sq",
      "selectAllMaldiciones",
      "SELECT Maldiciones.id, Maldiciones.nombre, Maldiciones.descripcion, Maldiciones.notas FROM Maldiciones") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)
    )
  }

  public fun selectAllMaldiciones(): Query<Maldiciones> = selectAllMaldiciones { id, nombre,
      descripcion, notas ->
    Maldiciones(
      id,
      nombre,
      descripcion,
      notas
    )
  }

  public fun insertPersonaje(
    id: Long?,
    nombre: String,
    descripcion: String?,
    es_tainted: Long?,
    metodo_desbloqueo: String?,
  ) {
    driver.execute(335_351_123, """
        |INSERT OR REPLACE INTO Personajes(id, nombre, descripcion, es_tainted, metodo_desbloqueo)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          bindLong(0, id)
          bindString(1, nombre)
          bindString(2, descripcion)
          bindLong(3, es_tainted)
          bindString(4, metodo_desbloqueo)
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
    calidad: Long?,
  ) {
    driver.execute(-1_266_167_459, """
        |INSERT OR REPLACE INTO Objetos(id, nombre, descripcion, tipo, calidad)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          bindLong(0, id)
          bindString(1, nombre)
          bindString(2, descripcion)
          bindString(3, tipo)
          bindLong(4, calidad)
        }
    notifyQueries(-1_266_167_459) { emit ->
      emit("Objetos")
    }
  }

  public fun insertConsumible(
    uid: Long?,
    id: Long,
    nombre: String,
    descripcion: String,
    tipo: String,
  ) {
    driver.execute(960_758_639, """
        |INSERT OR REPLACE INTO Consumibles(uid, id, nombre, descripcion, tipo)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          bindLong(0, uid)
          bindLong(1, id)
          bindString(2, nombre)
          bindString(3, descripcion)
          bindString(4, tipo)
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

  public fun insertLogro(
    id: Long?,
    nombre: String,
    descripcion: String,
    desbloqueado: Boolean?,
    desbloquea_personaje_id: Long?,
    desbloquea_objeto_id: Long?,
    desbloquea_consumible_id: Long?,
  ) {
    driver.execute(1_203_696_109, """
        |INSERT OR REPLACE INTO Logros(id, nombre, descripcion, desbloqueado, desbloquea_personaje_id, desbloquea_objeto_id, desbloquea_consumible_id)
        |VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 7) {
          bindLong(0, id)
          bindString(1, nombre)
          bindString(2, descripcion)
          bindBoolean(3, desbloqueado)
          bindLong(4, desbloquea_personaje_id)
          bindLong(5, desbloquea_objeto_id)
          bindLong(6, desbloquea_consumible_id)
        }
    notifyQueries(1_203_696_109) { emit ->
      emit("Logros")
    }
  }

  public fun updateLogroStatus(desbloqueado: Boolean?, id: Long) {
    driver.execute(1_975_707_695, """UPDATE Logros SET desbloqueado = ? WHERE id = ?""", 2) {
          bindBoolean(0, desbloqueado)
          bindLong(1, id)
        }
    notifyQueries(1_975_707_695) { emit ->
      emit("Logros")
    }
  }

  public fun insertDesbloqueo(
    personaje_id: Long,
    marca_id: Long,
    logro_id: Long?,
  ) {
    driver.execute(-675_181_963, """
        |INSERT OR REPLACE INTO Desbloqueos(personaje_id, marca_id, logro_id)
        |VALUES (?, ?, ?)
        """.trimMargin(), 3) {
          bindLong(0, personaje_id)
          bindLong(1, marca_id)
          bindLong(2, logro_id)
        }
    notifyQueries(-675_181_963) { emit ->
      emit("Desbloqueos")
    }
  }

  public fun insertEstadisticas(
    personaje_id: Long?,
    corazones_rojos: Long,
    corazones_alma: Long,
    corazones_negros: Long,
    corazones_hueso: Long,
    corazones_moneda: Long,
    manto_sagrado: Boolean?,
    salud_aleatoria: Boolean?,
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
        |INSERT OR REPLACE INTO Estadisticas_Personaje(
        |    personaje_id, corazones_rojos, corazones_alma, corazones_negros, corazones_hueso, corazones_moneda,
        |    manto_sagrado, salud_aleatoria, velocidad, lagrimas, dano, rango, velocidad_disparo, suerte,
        |    objeto_inicial_id, consumible_inicial_id
        |)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 16) {
          bindLong(0, personaje_id)
          bindLong(1, corazones_rojos)
          bindLong(2, corazones_alma)
          bindLong(3, corazones_negros)
          bindLong(4, corazones_hueso)
          bindLong(5, corazones_moneda)
          bindBoolean(6, manto_sagrado)
          bindBoolean(7, salud_aleatoria)
          bindDouble(8, velocidad)
          bindDouble(9, lagrimas)
          bindDouble(10, dano)
          bindDouble(11, rango)
          bindDouble(12, velocidad_disparo)
          bindDouble(13, suerte)
          bindLong(14, objeto_inicial_id)
          bindLong(15, consumible_inicial_id)
        }
    notifyQueries(11_513_633) { emit ->
      emit("Estadisticas_Personaje")
    }
  }

  public fun insertTransformacion(
    id: Long?,
    nombre: String,
    descripcion: String,
  ) {
    driver.execute(1_867_591_886, """
        |INSERT OR REPLACE INTO Transformaciones(id, nombre, descripcion)
        |VALUES (?, ?, ?)
        """.trimMargin(), 3) {
          bindLong(0, id)
          bindString(1, nombre)
          bindString(2, descripcion)
        }
    notifyQueries(1_867_591_886) { emit ->
      emit("Transformaciones")
    }
  }

  public fun insertTransformacionObjeto(transformacion_id: Long, objeto_id: Long) {
    driver.execute(928_411_575, """
        |INSERT OR REPLACE INTO Transformacion_Objeto(transformacion_id, objeto_id)
        |VALUES (?, ?)
        """.trimMargin(), 2) {
          bindLong(0, transformacion_id)
          bindLong(1, objeto_id)
        }
    notifyQueries(928_411_575) { emit ->
      emit("Transformacion_Objeto")
    }
  }

  public fun insertMaldicion(
    id: Long?,
    nombre: String,
    descripcion: String,
    notas: String?,
  ) {
    driver.execute(1_616_087_438, """
        |INSERT OR REPLACE INTO Maldiciones(id, nombre, descripcion, notas)
        |VALUES (?, ?, ?, ?)
        """.trimMargin(), 4) {
          bindLong(0, id)
          bindString(1, nombre)
          bindString(2, descripcion)
          bindString(3, notas)
        }
    notifyQueries(1_616_087_438) { emit ->
      emit("Maldiciones")
    }
  }

  private inner class GetPersonajeByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Personajes", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Personajes", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(319_199_518,
        """SELECT Personajes.id, Personajes.nombre, Personajes.descripcion, Personajes.es_tainted, Personajes.metodo_desbloqueo FROM Personajes WHERE id = ?""",
        mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "IsaacDatabase.sq:getPersonajeById"
  }

  private inner class GetObjetoByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Objetos", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Objetos", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_469_664_234,
        """SELECT Objetos.id, Objetos.nombre, Objetos.descripcion, Objetos.tipo, Objetos.calidad FROM Objetos WHERE id = ?""",
        mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "IsaacDatabase.sq:getObjetoById"
  }

  private inner class GetConsumibleByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Consumibles", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Consumibles", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-755_789_400,
        """SELECT Consumibles.uid, Consumibles.id, Consumibles.nombre, Consumibles.descripcion, Consumibles.tipo FROM Consumibles WHERE id = ? LIMIT 1""",
        mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "IsaacDatabase.sq:getConsumibleById"
  }

  private inner class GetConsumibleByUidQuery<out T : Any>(
    public val uid: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Consumibles", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Consumibles", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_954_623_133,
        """SELECT Consumibles.uid, Consumibles.id, Consumibles.nombre, Consumibles.descripcion, Consumibles.tipo FROM Consumibles WHERE uid = ?""",
        mapper, 1) {
      bindLong(0, uid)
    }

    override fun toString(): String = "IsaacDatabase.sq:getConsumibleByUid"
  }

  private inner class GetConsumibleByIdAndTypeQuery<out T : Any>(
    public val id: Long,
    public val tipo: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Consumibles", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Consumibles", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_806_701_289,
        """SELECT Consumibles.uid, Consumibles.id, Consumibles.nombre, Consumibles.descripcion, Consumibles.tipo FROM Consumibles WHERE id = ? AND tipo = ?""",
        mapper, 2) {
      bindLong(0, id)
      bindString(1, tipo)
    }

    override fun toString(): String = "IsaacDatabase.sq:getConsumibleByIdAndType"
  }

  private inner class GetLogroByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Logros", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Logros", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(935_277_880,
        """SELECT Logros.id, Logros.nombre, Logros.descripcion, Logros.desbloqueado, Logros.desbloquea_personaje_id, Logros.desbloquea_objeto_id, Logros.desbloquea_consumible_id FROM Logros WHERE id = ?""",
        mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "IsaacDatabase.sq:getLogroById"
  }

  private inner class GetLogrosByRewardObjetoQuery<out T : Any>(
    public val desbloquea_objeto_id: Long?,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Logros", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Logros", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(null,
        """SELECT Logros.id, Logros.nombre, Logros.descripcion, Logros.desbloqueado, Logros.desbloquea_personaje_id, Logros.desbloquea_objeto_id, Logros.desbloquea_consumible_id FROM Logros WHERE desbloquea_objeto_id ${ if (desbloquea_objeto_id == null) "IS" else "=" } ?""",
        mapper, 1) {
      bindLong(0, desbloquea_objeto_id)
    }

    override fun toString(): String = "IsaacDatabase.sq:getLogrosByRewardObjeto"
  }

  private inner class GetDesbloqueosByPersonajeQuery<out T : Any>(
    public val personaje_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Personajes", "Marcas", "Logros", "Consumibles", "Desbloqueos", listener =
          listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Personajes", "Marcas", "Logros", "Consumibles", "Desbloqueos", listener
          = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_016_237_415, """
    |SELECT
    |    Personajes.nombre AS personajeNombre,
    |    Marcas.id AS marcaId,
    |    Marcas.nombre AS marcaNombre,
    |    Logros.nombre AS logroNombre,
    |    Logros.id AS logroId,
    |    Logros.descripcion AS logroDescripcion,
    |    Logros.desbloqueado AS desbloqueado,
    |    Logros.desbloquea_objeto_id AS objetoId,
    |    Logros.desbloquea_consumible_id AS consumibleId,
    |    Logros.desbloquea_personaje_id AS pId,
    |    Consumibles.tipo AS consumibleTipo
    |FROM Desbloqueos
    |JOIN Personajes ON Desbloqueos.personaje_id = Personajes.id
    |JOIN Marcas ON Desbloqueos.marca_id = Marcas.id
    |LEFT JOIN Logros ON Desbloqueos.logro_id = Logros.id
    |LEFT JOIN Consumibles ON Logros.desbloquea_consumible_id = Consumibles.uid
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
        """SELECT Estadisticas_Personaje.personaje_id, Estadisticas_Personaje.corazones_rojos, Estadisticas_Personaje.corazones_alma, Estadisticas_Personaje.corazones_negros, Estadisticas_Personaje.corazones_hueso, Estadisticas_Personaje.corazones_moneda, Estadisticas_Personaje.manto_sagrado, Estadisticas_Personaje.salud_aleatoria, Estadisticas_Personaje.velocidad, Estadisticas_Personaje.lagrimas, Estadisticas_Personaje.dano, Estadisticas_Personaje.rango, Estadisticas_Personaje.velocidad_disparo, Estadisticas_Personaje.suerte, Estadisticas_Personaje.objeto_inicial_id, Estadisticas_Personaje.consumible_inicial_id FROM Estadisticas_Personaje WHERE personaje_id = ?""",
        mapper, 1) {
      bindLong(0, personaje_id)
    }

    override fun toString(): String = "IsaacDatabase.sq:getEstadisticasByPersonaje"
  }

  private inner class GetObjetosByTransformacionQuery<out T : Any>(
    public val transformacion_id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Objetos", "Transformacion_Objeto", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Objetos", "Transformacion_Objeto", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(25_117_152, """
    |SELECT Objetos.id, Objetos.nombre, Objetos.descripcion, Objetos.tipo, Objetos.calidad FROM Objetos
    |JOIN Transformacion_Objeto ON Objetos.id = Transformacion_Objeto.objeto_id
    |WHERE Transformacion_Objeto.transformacion_id = ?
    """.trimMargin(), mapper, 1) {
      bindLong(0, transformacion_id)
    }

    override fun toString(): String = "IsaacDatabase.sq:getObjetosByTransformacion"
  }
}
