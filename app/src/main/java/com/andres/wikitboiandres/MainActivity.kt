package com.andres.wikitboiandres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.andres.wikitboiandres.db.DriverFactory
import com.andres.wikitboiandres.db.IsaacDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializamos el driver y la base de datos
        val driver = DriverFactory(this).createDriver()
        
        // Al haber simplificado el .sq, ya no necesitamos pasar adaptadores
        val database = IsaacDatabase(driver)
        
        setContent {
            // Llamamos a la función App del módulo compartido
            App(database)
        }
    }
}
