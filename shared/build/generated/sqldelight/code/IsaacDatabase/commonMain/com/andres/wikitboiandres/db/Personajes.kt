package com.andres.wikitboiandres.db

import kotlin.Long
import kotlin.String

public data class Personajes(
  public val id: Long,
  public val nombre: String,
  public val descripcion: String?,
  public val es_tainted: Long?,
  public val metodo_desbloqueo: String?,
)
