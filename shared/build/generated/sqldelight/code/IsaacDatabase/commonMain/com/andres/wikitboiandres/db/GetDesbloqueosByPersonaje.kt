package com.andres.wikitboiandres.db

import kotlin.Long
import kotlin.String

public data class GetDesbloqueosByPersonaje(
  public val personajeNombre: String,
  public val marcaId: Long,
  public val marcaNombre: String,
  public val logroNombre: String?,
  public val logroId: Long?,
  public val objetoId: Long?,
  public val consumibleId: Long?,
  public val pId: Long?,
  public val consumibleTipo: String?,
)
