package com.andres.wikitboiandres.db

import kotlin.Long
import kotlin.String

public data class Consumibles(
  public val uid: Long,
  public val id: Long,
  public val nombre: String,
  public val descripcion: String,
  public val tipo: String,
)
