package com.andres.wikitboiandres.db

import kotlin.Long

public data class Desbloqueos(
  public val personaje_id: Long,
  public val marca_id: Long,
  public val objeto_id: Long?,
  public val consumible_id: Long?,
)
