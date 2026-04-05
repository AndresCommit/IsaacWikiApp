package com.andres.wikitboiandres.db

import kotlin.String

public data class GetDesbloqueosByPersonaje(
  public val personajeNombre: String,
  public val marcaNombre: String,
  public val objetoNombre: String?,
  public val consumibleNombre: String?,
)
