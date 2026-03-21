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
          |CREATE TABLE IsaacItem (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    name TEXT NOT NULL,
          |    description TEXT NOT NULL
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
