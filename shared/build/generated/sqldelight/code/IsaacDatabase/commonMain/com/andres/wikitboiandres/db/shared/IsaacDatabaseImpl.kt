package com.andres.wikitboiandres.db.shared

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.andres.wikitboiandres.db.IsaacDatabase
import com.andres.wikitboiandres.db.IsaacDatabaseQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<IsaacDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = IsaacDatabaseImpl.Schema

internal fun KClass<IsaacDatabase>.newInstance(driver: SqlDriver): IsaacDatabase =
    IsaacDatabaseImpl(driver)

private class IsaacDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), IsaacDatabase {
  override val isaacDatabaseQueries: IsaacDatabaseQueries = IsaacDatabaseQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE Personajes (
          |    id INTEGER PRIMARY KEY,
          |    nombre TEXT NOT NULL,
          |    es_tainted INTEGER DEFAULT 0,
          |    metodo_desbloqueo TEXT
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE Objetos (
          |    id INTEGER PRIMARY KEY,
          |    nombre TEXT NOT NULL,
          |    descripcion TEXT NOT NULL,
          |    tipo TEXT
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE Consumibles (
          |    id INTEGER NOT NULL,
          |    nombre TEXT NOT NULL,
          |    descripcion TEXT NOT NULL,
          |    tipo TEXT NOT NULL,
          |    PRIMARY KEY (id, tipo) -- Clave compuesta para evitar que se pisen Cartas/Pills/Trinkets
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE Estadisticas_Personaje (
          |    personaje_id INTEGER PRIMARY KEY,
          |    salud TEXT NOT NULL,
          |    velocidad REAL NOT NULL,
          |    lagrimas REAL NOT NULL,
          |    dano REAL NOT NULL,
          |    rango REAL NOT NULL,
          |    velocidad_disparo REAL NOT NULL,
          |    suerte REAL NOT NULL,
          |    objeto_inicial_id INTEGER,
          |    consumible_inicial_id INTEGER,
          |    FOREIGN KEY (personaje_id) REFERENCES Personajes(id) ON DELETE CASCADE,
          |    FOREIGN KEY (objeto_inicial_id) REFERENCES Objetos(id) ON DELETE SET NULL,
          |    FOREIGN KEY (consumible_inicial_id) REFERENCES Consumibles(id) ON DELETE SET NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE Sinergias (
          |    obj_a_id INTEGER NOT NULL,
          |    obj_b_id INTEGER NOT NULL,
          |    descripcion_sinergia TEXT NOT NULL,
          |    PRIMARY KEY (obj_a_id, obj_b_id),
          |    FOREIGN KEY (obj_a_id) REFERENCES Objetos(id) ON DELETE CASCADE,
          |    FOREIGN KEY (obj_b_id) REFERENCES Objetos(id) ON DELETE CASCADE
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE Transformaciones (
          |    id INTEGER PRIMARY KEY,
          |    nombre TEXT NOT NULL,
          |    desc_efecto TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE Transformacion_Objeto (
          |    objeto_id INTEGER NOT NULL,
          |    transformacion_id INTEGER NOT NULL,
          |    PRIMARY KEY (objeto_id, transformacion_id),
          |    FOREIGN KEY (objeto_id) REFERENCES Objetos(id) ON DELETE CASCADE,
          |    FOREIGN KEY (transformacion_id) REFERENCES Transformaciones(id) ON DELETE CASCADE
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE Marcas (
          |    id INTEGER PRIMARY KEY,
          |    nombre TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE Desbloqueos (
          |    personaje_id INTEGER NOT NULL,
          |    marca_id INTEGER NOT NULL,
          |    objeto_id INTEGER,
          |    consumible_id INTEGER,
          |    PRIMARY KEY (personaje_id, marca_id),
          |    FOREIGN KEY (personaje_id) REFERENCES Personajes(id) ON DELETE CASCADE,
          |    FOREIGN KEY (marca_id) REFERENCES Marcas(id) ON DELETE CASCADE,
          |    FOREIGN KEY (objeto_id) REFERENCES Objetos(id) ON DELETE SET NULL,
          |    FOREIGN KEY (consumible_id) REFERENCES Consumibles(id) ON DELETE SET NULL
          |)
          """.trimMargin(), 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
