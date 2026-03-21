package com.andres.wikitboiandres.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.String

public class IsaacDatabaseQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAllItems(mapper: (
    id: String,
    name: String,
    description: String,
  ) -> T): Query<T> = Query(4_935_926, arrayOf("IsaacItem"), driver, "IsaacDatabase.sq",
      "selectAllItems",
      "SELECT IsaacItem.id, IsaacItem.name, IsaacItem.description FROM IsaacItem") { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!
    )
  }

  public fun selectAllItems(): Query<IsaacItem> = selectAllItems { id, name, description ->
    IsaacItem(
      id,
      name,
      description
    )
  }

  public fun insertItem(
    id: String,
    name: String,
    description: String,
  ) {
    driver.execute(-1_208_181_721, """
        |INSERT OR REPLACE INTO IsaacItem(id, name, description)
        |VALUES (?, ?, ?)
        """.trimMargin(), 3) {
          bindString(0, id)
          bindString(1, name)
          bindString(2, description)
        }
    notifyQueries(-1_208_181_721) { emit ->
      emit("IsaacItem")
    }
  }
}
