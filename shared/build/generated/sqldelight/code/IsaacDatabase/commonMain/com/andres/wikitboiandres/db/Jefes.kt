package com.andres.wikitboiandres.db

import kotlin.Long
import kotlin.String

public data class Jefes(
  public val id: Long,
  public val nombre: String,
  public val vida_base: Long,
  public val descripcion: String,
  public val comportamiento: String,
  public val notas: String,
)
