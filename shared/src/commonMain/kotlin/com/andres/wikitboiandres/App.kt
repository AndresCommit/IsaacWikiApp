package com.andres.wikitboiandres

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andres.wikitboiandres.db.IsaacDatabase
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import wikitboiandres.shared.generated.resources.Res

enum class Screen {
    Loading, Menu, Objetos, Personajes, SubMenuConsumibles, ConsumiblesPorTipo, DetalleObjeto, DetallePersonaje, DetalleConsumible
}

@Composable
fun App(database: IsaacDatabase) {
    val repository = remember { IsaacRepository(database) }
    var currentScreen by remember { mutableStateOf(Screen.Loading) }
    var status by remember { mutableStateOf("Cargando datos...") }
    
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    var objetos by remember { mutableStateOf(emptyList<RemoteObjeto>()) }
    var personajes by remember { mutableStateOf(emptyList<RemotePersonaje>()) }
    var consumibles by remember { mutableStateOf(emptyList<RemoteConsumible>()) }
    
    var selectedTipoConsumible by remember { mutableStateOf("") }
    val filteredConsumibles = remember(consumibles, selectedTipoConsumible) {
        if (selectedTipoConsumible.isEmpty()) consumibles else consumibles.filter { it.tipo == selectedTipoConsumible }
    }

    var selectedObjeto by remember { mutableStateOf<RemoteObjeto?>(null) }
    var selectedPersonaje by remember { mutableStateOf<RemotePersonaje?>(null) }
    var selectedConsumible by remember { mutableStateOf<RemoteConsumible?>(null) }

    LaunchedEffect(Unit) {
        try {
            if (repository.getAllObjetosCount() == 0L) {
                status = "Descargando datos..."
                repository.fetchAndSaveObjetos()
                repository.fetchAndSaveConsumibles()
                repository.fetchAndSavePersonajes()
                repository.fetchAndSaveMarcas()
                repository.fetchAndSaveDesbloqueos()
                repository.fetchAndSaveEstadisticas()
            }
            objetos = repository.getAllObjetos()
            personajes = repository.getAllPersonajes()
            consumibles = repository.getAllConsumibles()
            currentScreen = Screen.Menu
        } catch (e: Exception) {
            status = "Error: ${e.message}"
        }
    }

    val orangeColor = Color(0xFFFF4500)
    val darkColors = darkColors(
        primary = Color.White,
        secondary = orangeColor,
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onPrimary = Color.Black,
        onSecondary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White,
    )

    val onBackClick = {
        when (currentScreen) {
            Screen.Objetos, Screen.Personajes, Screen.SubMenuConsumibles -> currentScreen = Screen.Menu
            Screen.ConsumiblesPorTipo -> currentScreen = Screen.SubMenuConsumibles
            Screen.DetalleObjeto -> currentScreen = Screen.Objetos
            Screen.DetallePersonaje -> currentScreen = Screen.Personajes
            Screen.DetalleConsumible -> currentScreen = Screen.ConsumiblesPorTipo
            else -> {}
        }
    }

    MaterialTheme(colors = darkColors) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text("Wiki TBOI", fontWeight = FontWeight.Bold) },
                    backgroundColor = MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.primary,
                    navigationIcon = {
                        if (currentScreen == Screen.Menu) {
                            IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú", tint = orangeColor)
                            }
                        } else if (currentScreen != Screen.Loading) {
                            IconButton(onClick = { onBackClick() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = orangeColor)
                            }
                        }
                    }
                )
            },
            drawerContent = {
                DrawerContent(onNavigate = { screen ->
                    currentScreen = screen
                    scope.launch { scaffoldState.drawerState.close() }
                })
            },
            backgroundColor = MaterialTheme.colors.background
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colors.background)) {
                when (currentScreen) {
                    Screen.Loading -> LoadingScreen(status)
                    Screen.Menu -> MenuScreen(onNavigate = { currentScreen = it })
                    Screen.Objetos -> ListScreen(objetos, { it.nombre }, { "ID: ${it.id}" }, { getImagePath(it.id, "Objeto") }) {
                        selectedObjeto = it
                        currentScreen = Screen.DetalleObjeto
                    }
                    Screen.Personajes -> ListScreen(personajes, { it.nombre }, { if (it.es_tainted) "Tainted" else "Normal" }, { getImagePath(it.id, "Personaje") }) {
                        selectedPersonaje = it
                        currentScreen = Screen.DetallePersonaje
                    }
                    Screen.SubMenuConsumibles -> SubMenuConsumiblesScreen(
                        tipos = consumibles.map { it.tipo }.distinct().sorted(),
                        onTipoClick = { tipo ->
                            selectedTipoConsumible = tipo
                            currentScreen = Screen.ConsumiblesPorTipo
                        }
                    )
                    Screen.ConsumiblesPorTipo -> ListScreen(filteredConsumibles, { it.nombre }, { it.tipo }, { getImagePath(it.id, it.tipo) }) {
                        selectedConsumible = it
                        currentScreen = Screen.DetalleConsumible
                    }
                    Screen.DetalleObjeto -> selectedObjeto?.let {
                        DetailScreen(it.nombre, "Tipo", it.tipo, "Descripción", it.descripcion, getImagePath(it.id, "Objeto"))
                    }
                    Screen.DetallePersonaje -> selectedPersonaje?.let { per ->
                        val desbloqueos = remember(per.id) { repository.getDesbloqueosByPersonaje(per.id) }
                        val stats = remember(per.id) { repository.getEstadisticasByPersonaje(per.id) }
                        DetailPersonajeScreen(
                            nombre = per.nombre,
                            esTainted = per.es_tainted,
                            metodo = per.metodo_desbloqueo ?: "Desbloqueado por defecto",
                            stats = stats,
                            desbloqueos = desbloqueos,
                            imageName = getImagePath(per.id, "Personaje")
                        )
                    }
                    Screen.DetalleConsumible -> selectedConsumible?.let {
                        DetailScreen(it.nombre, "Tipo", it.tipo, "Descripción", it.descripcion, getImagePath(it.id, it.tipo))
                    }
                }
            }
        }
    }
}

fun getImagePath(id: Int, tipo: String): String? {
    return when (tipo) {
        "Objeto" -> "collectibles_${id.toString().padStart(3, '0')}"
        "Trinket" -> "collectibles_${2000 + id}"
        "Personaje" -> "Character_${id}_icon"
        else -> null
    }
}

@Composable
fun DetailPersonajeScreen(
    nombre: String, 
    esTainted: Boolean, 
    metodo: String, 
    stats: RemoteEstadisticas?,
    desbloqueos: List<Pair<String, String>>, 
    imageName: String?
) {
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (imageName != null) {
                DynamicImage(imageName, Modifier.size(80.dp).padding(end = 16.dp))
            }
            Text(nombre, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))
        Text(text = "Estado:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        Text(if (esTainted) "Tainted" else "Normal", color = Color.White, style = MaterialTheme.typography.body1)

        Spacer(Modifier.height(16.dp))
        Text(text = "Método de Desbloqueo:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        Text(metodo, color = Color.White, style = MaterialTheme.typography.body1)

        stats?.let { s ->
            Spacer(Modifier.height(24.dp))
            Text("Estadísticas Iniciales:", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            StatRow("Salud", s.salud)
            StatRow("Velocidad", s.velocidad.toString())
            StatRow("Lágrimas", s.lagrimas.toString())
            StatRow("Daño", s.dano.toString())
            StatRow("Alcance", s.rango.toString())
            StatRow("Vel. Disparo", s.velocidad_disparo.toString())
            StatRow("Suerte", s.suerte.toString())
        }

        Spacer(Modifier.height(24.dp))
        Text("Desbloqueos (Post-it):", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))
        desbloqueos.forEach { (marca, premio) ->
            Row(Modifier.padding(vertical = 4.dp)) {
                Text("• $marca: ", fontWeight = FontWeight.Bold, color = Color.White)
                Text(premio, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.body2)
        Text(value, color = Color.LightGray, style = MaterialTheme.typography.body2)
    }
}

@Composable
fun DrawerContent(onNavigate: (Screen) -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colors.surface).padding(16.dp)) {
        Text("Wiki Isaac", style = MaterialTheme.typography.h5, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        DrawerItem("Inicio", Icons.Default.Home) { onNavigate(Screen.Menu) }
        Divider(color = Color.Gray.copy(alpha = 0.5f))
        DrawerItem("Objetos", Icons.Default.Info) { onNavigate(Screen.Objetos) }
        DrawerItem("Personajes", Icons.Default.Person) { onNavigate(Screen.Personajes) }
        DrawerItem("Consumibles", Icons.AutoMirrored.Filled.List) { onNavigate(Screen.SubMenuConsumibles) }
    }
}

@Composable
fun DrawerItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colors.secondary)
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

@Composable
fun LoadingScreen(status: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colors.secondary)
            Spacer(Modifier.height(16.dp))
            Text(status, color = Color.White)
        }
    }
}

@Composable
fun MenuScreen(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MenuButton("Objetos", Icons.Default.Info) { onNavigate(Screen.Objetos) }
        MenuButton("Personajes", Icons.Default.Person) { onNavigate(Screen.Personajes) }
        MenuButton("Consumibles", Icons.AutoMirrored.Filled.List) { onNavigate(Screen.SubMenuConsumibles) }
    }
}

@Composable
fun SubMenuConsumiblesScreen(tipos: List<String>, onTipoClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Categorías", style = MaterialTheme.typography.h5, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        LazyColumn {
            items(tipos) { tipo ->
                Button(
                    onClick = { onTipoClick(tipo) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface)
                ) {
                    Text(tipo, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MenuButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun <T> ListScreen(
    itemList: List<T>,
    getTitle: (T) -> String,
    getSubtitle: (T) -> String,
    getImageName: (T) -> String?,
    onItemClick: (T) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(itemList) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageName = getImageName(item)
                if (imageName != null) {
                    DynamicImage(imageName, Modifier.size(48.dp).padding(end = 16.dp))
                }
                Column {
                    Text(getTitle(item), style = MaterialTheme.typography.h6, color = Color.White)
                    Text(getSubtitle(item), style = MaterialTheme.typography.body2, color = Color.Gray)
                }
            }
            Divider(color = Color.DarkGray)
        }
    }
}

@Composable
fun DetailScreen(title: String, label1: String, value1: String, label2: String, value2: String, imageName: String?) {
    Column(Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colors.background)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (imageName != null) {
                DynamicImage(imageName, Modifier.size(80.dp).padding(end = 16.dp))
            }
            Text(title, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text(text = "$label1:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        Text(text = value1, color = Color.White, style = MaterialTheme.typography.body1)
        
        Spacer(Modifier.height(16.dp))
        
        Text(text = "$label2:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        Text(text = value2, color = Color.White, style = MaterialTheme.typography.body1)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun DynamicImage(name: String, modifier: Modifier) {
    val imageBitmap = produceState<ImageBitmap?>(null, name) {
        try {
            val bytes = Res.readBytes("drawable/$name.png")
            value = bytes.decodeToImageBitmap()
        } catch (e: Exception) {
            value = null
        }
    }.value

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(Color.DarkGray, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Gray)
        }
    }
}
