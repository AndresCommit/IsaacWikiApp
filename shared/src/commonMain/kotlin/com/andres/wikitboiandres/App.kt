package com.andres.wikitboiandres

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import com.andres.wikitboiandres.db.IsaacDatabase
import kotlinx.coroutines.launch

@Composable
fun App(database: IsaacDatabase) {
    val repository = remember { IsaacRepository(database) }
    var status by remember { mutableStateOf("Iniciando...") }
    var itemCount by remember { mutableStateOf(0) }

    MaterialTheme {
        LaunchedEffect(Unit) {
            status = "Descargando items de GitHub..."
            try {
                repository.fetchAndSaveItems()
                status = "Items descargados. Consultando base de datos..."
                val items = repository.getAllItems()
                itemCount = items.size
                status = "Éxito: $itemCount items en la base de datos."
            } catch (e: Exception) {
                status = "Error: ${e.message}"
                e.printStackTrace()
            }
        }

        Column {
            Text(status)
            if (itemCount > 0) {
                Text("Total guardado: $itemCount")
            }
        }
    }
}
