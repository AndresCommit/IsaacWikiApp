package com.andres.wikitboiandres.db

import kotlin.Boolean
import kotlin.Long
import kotlin.String

public data class Logros(
  public val id: Long,
  public val nombre: String,
  public val descripcion: String,
  public val desbloqueado: Boolean?,
  public val desbloquea_personaje_id: Long?,
  public val desbloquea_objeto_id: Long?,
  public val desbloquea_consumible_id: Long?,
)
