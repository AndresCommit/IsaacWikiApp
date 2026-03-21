package com.andres.wikitboiandres.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.andres.wikitboiandres.db.shared.newInstance
import com.andres.wikitboiandres.db.shared.schema
import kotlin.Unit

public interface IsaacDatabase : Transacter {
  public val isaacDatabaseQueries: IsaacDatabaseQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = IsaacDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): IsaacDatabase =
        IsaacDatabase::class.newInstance(driver)
  }
}
