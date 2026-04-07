package com.andres.wikitboiandres.db

import kotlin.Boolean
import kotlin.Double
import kotlin.Long

public data class Estadisticas_Personaje(
  public val personaje_id: Long,
  public val corazones_rojos: Long,
  public val corazones_alma: Long,
  public val corazones_negros: Long,
  public val corazones_hueso: Long,
  public val corazones_moneda: Long,
  public val manto_sagrado: Boolean?,
  public val salud_aleatoria: Boolean?,
  public val velocidad: Double,
  public val lagrimas: Double,
  public val dano: Double,
  public val rango: Double,
  public val velocidad_disparo: Double,
  public val suerte: Double,
  public val objeto_inicial_id: Long?,
  public val consumible_inicial_id: Long?,
)
