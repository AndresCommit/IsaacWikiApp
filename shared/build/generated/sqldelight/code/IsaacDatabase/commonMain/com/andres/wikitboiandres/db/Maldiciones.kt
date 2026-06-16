package com.andres.wikitboiandres.db

import kotlin.Long
import kotlin.String

public data class Maldiciones(
  public val id: Long,
  public val nombre: String,
  public val descripcion: String,
  public val notas: String?,
)
