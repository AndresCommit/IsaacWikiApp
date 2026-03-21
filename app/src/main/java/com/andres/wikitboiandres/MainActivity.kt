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
        val database = IsaacDatabase(driver)
        
        setContent {
            App(database)
        }
    }
}
