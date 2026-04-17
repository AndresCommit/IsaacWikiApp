package com.andres.wikitboiandres

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
    Loading, Menu, Objetos, Personajes, SubMenuConsumibles, ConsumiblesPorTipo, DetalleObjeto, DetallePersonaje, DetalleConsumible, GlobalSearch, Transformaciones, DetalleTransformacion, Logros, DetalleLogro, Maldiciones, DetalleMaldicion
}

data class SearchItem(val id: Int, val nombre: String, val tipo: String, val original: Any)

@Composable
fun App(database: IsaacDatabase) {
    val repository = remember { IsaacRepository(database) }
    var currentScreen by remember { mutableStateOf(Screen.Loading) }
    var status by remember { mutableStateOf("Cargando datos...") }
    
    val navigationStack = remember { mutableStateListOf<Screen>() }

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    var objetos by remember { mutableStateOf(emptyList<RemoteObjeto>()) }
    var personajes by remember { mutableStateOf(emptyList<RemotePersonaje>()) }
    var consumibles by remember { mutableStateOf(emptyList<RemoteConsumible>()) }
    var transformaciones by remember { mutableStateOf(emptyList<RemoteTransformacion>()) }
    var logros by remember { mutableStateOf(emptyList<RemoteLogro>()) }
    var maldiciones by remember { mutableStateOf(emptyList<RemoteMaldicion>()) }
    
    var searchQueryObjetos by remember { mutableStateOf("") }
    var searchQueryConsumibles by remember { mutableStateOf("") }
    var searchQueryLogros by remember { mutableStateOf("") }
    var searchQueryMaldiciones by remember { mutableStateOf("") }
    var searchQueryGlobal by remember { mutableStateOf("") }

    var isObjectsGridView by remember { mutableStateOf(false) }

    var selectedTipoConsumible by remember { mutableStateOf("") }
    
    val filteredObjetos = remember(objetos, searchQueryObjetos) {
        if (searchQueryObjetos.isEmpty()) objetos else objetos.filter { it.nombre.contains(searchQueryObjetos, ignoreCase = true) }
    }

    val filteredConsumibles = remember(consumibles, selectedTipoConsumible, searchQueryConsumibles) {
        var list = if (selectedTipoConsumible.isEmpty()) consumibles else consumibles.filter { it.tipo == selectedTipoConsumible }
        if (searchQueryConsumibles.isNotEmpty()) {
            list = list.filter { it.nombre.contains(searchQueryConsumibles, ignoreCase = true) }
        }
        list
    }

    val filteredLogros = remember(logros, searchQueryLogros) {
        if (searchQueryLogros.isEmpty()) logros else logros.filter { it.nombre.contains(searchQueryLogros, ignoreCase = true) }
    }

    val filteredMaldiciones = remember(maldiciones, searchQueryMaldiciones) {
        if (searchQueryMaldiciones.isEmpty()) maldiciones else maldiciones.filter { it.nombre.contains(searchQueryMaldiciones, ignoreCase = true) }
    }

    val allSearchItems = remember(objetos, personajes, consumibles, logros, maldiciones) {
        val list = mutableListOf<SearchItem>()
        objetos.forEach { list.add(SearchItem(it.id, it.nombre, "Objeto", it)) }
        personajes.forEach { list.add(SearchItem(it.id, it.nombre, "Personaje", it)) }
        consumibles.forEach { list.add(SearchItem(it.id, it.nombre, it.tipo, it)) }
        logros.forEach { list.add(SearchItem(it.id, it.nombre, "Logro", it)) }
        maldiciones.forEach { list.add(SearchItem(it.id, it.nombre, "Maldicion", it)) }
        list
    }

    var selectedObjeto by remember { mutableStateOf<RemoteObjeto?>(null) }
    var selectedPersonaje by remember { mutableStateOf<RemotePersonaje?>(null) }
    var selectedConsumible by remember { mutableStateOf<RemoteConsumible?>(null) }
    var selectedTransformacion by remember { mutableStateOf<RemoteTransformacion?>(null) }
    var selectedLogro by remember { mutableStateOf<RemoteLogro?>(null) }
    var selectedMaldicion by remember { mutableStateOf<RemoteMaldicion?>(null) }

    LaunchedEffect(Unit) {
        try {
            if (repository.getAllObjetosCount() == 0L) {
                status = "Descargando datos..."
                repository.fetchAndSaveObjetos()
                repository.fetchAndSaveConsumibles()
                repository.fetchAndSavePersonajes()
                repository.fetchAndSaveMarcas()
                repository.fetchAndSaveLogros()
                repository.fetchAndSaveDesbloqueos()
                repository.fetchAndSaveEstadisticas()
                repository.fetchAndSaveTransformaciones()
                repository.fetchAndSaveTransformacionObjeto()
                repository.fetchAndSaveMaldiciones()
            } else if (repository.getAllMaldicionesCount() == 0L) {
                repository.fetchAndSaveMaldiciones()
            }
            objetos = repository.getAllObjetos()
            personajes = repository.getAllPersonajes()
            consumibles = repository.getAllConsumibles()
            transformaciones = repository.getAllTransformaciones()
            logros = repository.getAllLogros()
            maldiciones = repository.getAllMaldiciones()
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

    val navigateTo = { screen: Screen ->
        navigationStack.add(currentScreen)
        currentScreen = screen
    }

    val navigateBack = {
        if (navigationStack.isNotEmpty()) {
            currentScreen = navigationStack.removeAt(navigationStack.size - 1)
        }
    }

    val grayScaleFilter = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }

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
                            IconButton(onClick = { navigateBack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = orangeColor)
                            }
                        }
                    },
                    actions = {
                        if (currentScreen == Screen.Menu) {
                            IconButton(onClick = { navigateTo(Screen.GlobalSearch) }) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar", tint = orangeColor)
                            }
                        }
                    }
                )
            },
            drawerContent = {
                DrawerContent(onNavigate = { screen ->
                    navigationStack.clear()
                    currentScreen = screen
                    scope.launch { scaffoldState.drawerState.close() }
                })
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colors.background)) {
                when (currentScreen) {
                    Screen.Loading -> LoadingScreen(status)
                    Screen.Menu -> MenuScreen(onNavigate = { navigateTo(it) })
                    Screen.Objetos -> Column {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(end = 8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                SearchBar(searchQueryObjetos) { searchQueryObjetos = it }
                            }
                            IconButton(onClick = { isObjectsGridView = !isObjectsGridView }) {
                                Icon(
                                    imageVector = if (isObjectsGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                                    contentDescription = "Cambiar vista",
                                    tint = orangeColor
                                )
                            }
                        }
                        if (isObjectsGridView) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(80.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(filteredObjetos) { obj ->
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .aspectRatio(1f)
                                            .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                                            .clickable { 
                                                selectedObjeto = obj
                                                navigateTo(Screen.DetalleObjeto)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        DynamicImage(getImagePath(obj.id, "Objeto") ?: "", Modifier.size(48.dp))
                                    }
                                }
                            }
                        } else {
                            ListScreen(filteredObjetos, { it.nombre }, { it.tipo }, { getImagePath(it.id, "Objeto") }, 48) {
                                selectedObjeto = it
                                navigateTo(Screen.DetalleObjeto)
                            }
                        }
                    }
                    Screen.Personajes -> ListScreen(personajes, { it.nombre }, { if (it.es_tainted) "Tainted" else "Normal" }, { getImagePath(it.id, "Personaje") }, 56) {
                        selectedPersonaje = it
                        navigateTo(Screen.DetallePersonaje)
                    }
                    Screen.SubMenuConsumibles -> SubMenuConsumiblesScreen(
                        tipos = consumibles.map { it.tipo }.distinct().sorted(),
                        onTipoClick = { tipo ->
                            selectedTipoConsumible = tipo
                            searchQueryConsumibles = ""
                            navigateTo(Screen.ConsumiblesPorTipo)
                        }
                    )
                    Screen.ConsumiblesPorTipo -> Column {
                        if (selectedTipoConsumible == "Trinket" || selectedTipoConsumible == "Carta") {
                            SearchBar(searchQueryConsumibles) { searchQueryConsumibles = it }
                        }
                        ListScreen(filteredConsumibles, { it.nombre }, { it.tipo }, { getImagePath(it.id, it.tipo) }, 48) {
                            selectedConsumible = it
                            navigateTo(Screen.DetalleConsumible)
                        }
                    }
                    Screen.Transformaciones -> ListScreen(transformaciones, { it.nombre }, { it.descripcion.take(50) + "..." }, { getImagePath(it.id, "Transformacion") }, 48) {
                        selectedTransformacion = it
                        navigateTo(Screen.DetalleTransformacion)
                    }
                    Screen.Logros -> Column {
                        val totalLogros = logros.size
                        val unlockedLogros = logros.count { it.desbloqueado }
                        val progress = if (totalLogros > 0) unlockedLogros.toFloat() / totalLogros else 0f

                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Progreso: $unlockedLogros / $totalLogros (${(progress * 100).toInt()}%)",
                                color = Color.White,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = orangeColor,
                                backgroundColor = Color.DarkGray
                            )
                        }

                        SearchBar(searchQueryLogros) { searchQueryLogros = it }
                        ListScreen(
                            itemList = filteredLogros, 
                            getTitle = { it.nombre }, 
                            getSubtitle = { it.descripcion.take(50) + "..." }, 
                            getImageName = { getImagePath(it.id, "Logro") }, 
                            iconSize = 56,
                            getImageFilter = { if (!it.desbloqueado) grayScaleFilter else null },
                            trailingContent = { logro ->
                                Checkbox(
                                    checked = logro.desbloqueado,
                                    onCheckedChange = { newVal ->
                                        repository.updateLogroStatus(logro.id, newVal)
                                        logros = repository.getAllLogros() // Refresh
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = orangeColor,
                                        uncheckedColor = Color.Gray,
                                        checkmarkColor = Color.White
                                    )
                                )
                            }
                        ) {
                            selectedLogro = it
                            navigateTo(Screen.DetalleLogro)
                        }
                    }
                    Screen.Maldiciones -> Column {
                        SearchBar(searchQueryMaldiciones) { searchQueryMaldiciones = it }
                        ListScreen(filteredMaldiciones, { it.nombre }, { it.descripcion.take(50) + "..." }, { getImagePath(it.id, "Maldicion") }, 56) {
                            selectedMaldicion = it
                            navigateTo(Screen.DetalleMaldicion)
                        }
                    }
                    Screen.DetalleObjeto -> selectedObjeto?.let { obj ->
                        val relatedLogros = remember(obj.id) { repository.getLogrosByRewardObjeto(obj.id) }
                        DetailObjetoScreen(obj, relatedLogros) { logro ->
                            selectedLogro = logro
                            navigateTo(Screen.DetalleLogro)
                        }
                    }
                    Screen.DetalleTransformacion -> selectedTransformacion?.let { trans ->
                        val objs = remember(trans.id) { repository.getObjetosByTransformacion(trans.id) }
                        DetailTransformacionScreen(trans, objs) {
                            selectedObjeto = it
                            navigateTo(Screen.DetalleObjeto)
                        }
                    }
                    Screen.DetalleLogro -> selectedLogro?.let { logro ->
                        val rewardConsumible = remember(logro.desbloquea_consumible_id) {
                            logro.desbloquea_consumible_id?.let { repository.getConsumibleById(it) }
                        }
                        DetailLogroScreen(
                            logro = logro, 
                            rewardConsumibleTipo = rewardConsumible?.tipo,
                            onStatusChange = { newVal ->
                                repository.updateLogroStatus(logro.id, newVal)
                                logros = repository.getAllLogros() // Refresh list
                                selectedLogro = repository.getLogroById(logro.id) // Update current
                            }
                        ) { type, id ->
                            when(type) {
                                "Objeto" -> { selectedObjeto = repository.getObjetoById(id); navigateTo(Screen.DetalleObjeto) }
                                "Personaje" -> { selectedPersonaje = repository.getPersonajeById(id); navigateTo(Screen.DetallePersonaje) }
                                "Consumible", "Trinket", "Carta" -> { 
                                    selectedConsumible = if (rewardConsumible != null && id == rewardConsumible.id) rewardConsumible 
                                                         else repository.getConsumibleById(id)
                                    navigateTo(Screen.DetalleConsumible) 
                                }
                            }
                        }
                    }
                    Screen.DetalleMaldicion -> selectedMaldicion?.let { 
                        DetailScreen(it.nombre, "Descripción", it.descripcion, "Notas", it.notas ?: "Sin notas adicionales", getImagePath(it.id, "Maldicion"))
                    }
                    Screen.DetallePersonaje -> selectedPersonaje?.let { per ->
                        val desbloqueos = remember(per.id) { repository.getDesbloqueosByPersonaje(per.id) }
                        val stats = remember(per.id) { repository.getEstadisticasByPersonaje(per.id) }
                        
                        val currentIndex = personajes.indexOfFirst { it.id == per.id }
                        val prevPersonaje = if (currentIndex > 0) personajes[currentIndex - 1] else null
                        val nextPersonaje = if (currentIndex != -1 && currentIndex < personajes.size - 1) personajes[currentIndex + 1] else null

                        DetailPersonajeScreen(
                            nombre = per.nombre,
                            descripcion = per.descripcion,
                            esTainted = per.es_tainted,
                            metodo = per.metodo_desbloqueo ?: "Desbloqueado por defecto",
                            stats = stats,
                            desbloqueos = desbloqueos,
                            imageName = getImagePath(per.id, "Personaje"),
                            prevPersonaje = prevPersonaje,
                            nextPersonaje = nextPersonaje,
                            onPremioClick = { info ->
                                if (info.logroId != null) {
                                    selectedLogro = repository.getLogroById(info.logroId)
                                    navigateTo(Screen.DetalleLogro)
                                } else if (info.premioId > 0) {
                                    if (info.esObjeto) {
                                        selectedObjeto = repository.getObjetoById(info.premioId)
                                        navigateTo(Screen.DetalleObjeto)
                                    } else if (info.pId != null) {
                                        selectedPersonaje = repository.getPersonajeById(info.pId)
                                        navigateTo(Screen.DetallePersonaje)
                                    } else {
                                        selectedConsumible = if (info.consumibleTipo != null) {
                                            repository.getConsumibleByIdAndType(info.premioId, info.consumibleTipo)
                                        } else {
                                            repository.getConsumibleById(info.premioId)
                                        }
                                        navigateTo(Screen.DetalleConsumible)
                                    }
                                }
                            },
                            onNavigate = { selectedPersonaje = it }
                        )
                    }
                    Screen.DetalleConsumible -> selectedConsumible?.let {
                        DetailScreen(it.nombre, "Tipo", it.tipo, "Descripción", it.descripcion, getImagePath(it.id, it.tipo))
                    }
                    Screen.GlobalSearch -> GlobalSearchScreen(
                        allItems = allSearchItems,
                        query = searchQueryGlobal,
                        onQueryChange = { searchQueryGlobal = it },
                        onItemClick = { item ->
                            when (item.original) {
                                is RemoteObjeto -> {
                                    selectedObjeto = item.original
                                    navigateTo(Screen.DetalleObjeto)
                                }
                                is RemotePersonaje -> {
                                    selectedPersonaje = item.original
                                    navigateTo(Screen.DetallePersonaje)
                                }
                                is RemoteConsumible -> {
                                    selectedConsumible = item.original
                                    navigateTo(Screen.DetalleConsumible)
                                }
                                is RemoteLogro -> {
                                    selectedLogro = item.original
                                    navigateTo(Screen.DetalleLogro)
                                }
                                is RemoteMaldicion -> {
                                    selectedMaldicion = item.original
                                    navigateTo(Screen.DetalleMaldicion)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        placeholder = { Text("Buscar...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                }
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colors.surface,
            textColor = Color.White,
            focusedIndicatorColor = MaterialTheme.colors.secondary,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colors.secondary
        ),
        singleLine = true,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun GlobalSearchScreen(
    allItems: List<SearchItem>,
    query: String,
    onQueryChange: (String) -> Unit,
    onItemClick: (SearchItem) -> Unit
) {
    val filteredResults = remember(allItems, query) {
        if (query.isEmpty()) emptyList() 
        else allItems.filter { it.nombre.contains(query, ignoreCase = true) }
    }

    Column(Modifier.fillMaxSize()) {
        SearchBar(query, onQueryChange)
        
        if (query.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Escribe para buscar objetos, personajes, logros, maldiciones...", color = Color.Gray)
            }
        } else if (filteredResults.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontraron resultados", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(64.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(filteredResults) { item ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                            .clickable { onItemClick(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        val imageName = getImagePath(item.id, item.tipo)
                        if (imageName != null) {
                            DynamicImage(imageName, Modifier.size(48.dp))
                        } else {
                            Icon(Icons.Default.QuestionMark, contentDescription = null, tint = Color.Gray)
                        }
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
        "Carta" -> "Pickup_$id"
        "Transformacion" -> "Transformation_$id"
        "Logro" -> "Logro_$id"
        "Maldicion" -> "Curse_$id"
        else -> null
    }
}

@Composable
fun QualityStars(calidad: Int, size: Int = 20) {
    Row {
        repeat(4) { index ->
            val iconName = if (index < calidad) "Item_quality_1" else "Item_quality_0"
            DynamicImage(iconName, Modifier.size(size.dp).padding(end = 2.dp))
        }
    }
}

@Composable
fun DetailObjetoScreen(objeto: RemoteObjeto, relatedLogros: List<RemoteLogro>, onLogroClick: (RemoteLogro) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colors.background).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val imageName = getImagePath(objeto.id, "Objeto")
            if (imageName != null) {
                DynamicImage(imageName, Modifier.size(80.dp).padding(end = 16.dp))
            }
            Text(objeto.nombre, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text(text = "Tipo:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        Text(text = objeto.tipo, color = Color.White, style = MaterialTheme.typography.body1)
        
        Spacer(Modifier.height(16.dp))

        Text(text = "Calidad:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        QualityStars(objeto.calidad)

        Spacer(Modifier.height(16.dp))
        
        Text(text = "Descripción:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        Text(text = objeto.descripcion, color = Color.White, style = MaterialTheme.typography.body1)

        if (relatedLogros.isNotEmpty()) {
            Spacer(Modifier.height(32.dp))
            Text(text = "Desbloqueos Relacionados:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))
            relatedLogros.forEach { logro ->
                Row(
                    Modifier.fillMaxWidth().clickable { onLogroClick(logro) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DynamicImage(getImagePath(logro.id, "Logro") ?: "", Modifier.size(40.dp).padding(end = 12.dp))
                    Text(logro.nombre, color = Color.White, style = MaterialTheme.typography.body1)
                }
                Divider(color = Color.DarkGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun DetailLogroScreen(
    logro: RemoteLogro, 
    rewardConsumibleTipo: String?, 
    onStatusChange: (Boolean) -> Unit,
    onPremioClick: (String, Int) -> Unit
) {
    val grayScaleFilter = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
    
    Column(Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colors.background).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 16.dp)) {
                DynamicImage(
                    name = getImagePath(logro.id, "Logro") ?: "", 
                    modifier = Modifier.size(80.dp),
                    colorFilter = if (!logro.desbloqueado) grayScaleFilter else null
                )
                Text(
                    text = "Secreto: ${logro.id}", 
                    color = Color.Gray, 
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(Modifier.weight(1f)) {
                Text(logro.nombre, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
            }
            Checkbox(
                checked = logro.desbloqueado,
                onCheckedChange = onStatusChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.secondary)
            )
        }
        
        Spacer(Modifier.height(24.dp))
        Text(text = "Requisito:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        Text(text = logro.descripcion, color = Color.White, style = MaterialTheme.typography.body1)
        
        Spacer(Modifier.height(24.dp))
        Text(text = "Recompensa:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        
        val premioId = logro.desbloquea_objeto_id ?: logro.desbloquea_personaje_id ?: logro.desbloquea_consumible_id
        val premioTipo = when {
            logro.desbloquea_objeto_id != null -> "Objeto"
            logro.desbloquea_personaje_id != null -> "Personaje"
            logro.desbloquea_consumible_id != null -> rewardConsumibleTipo ?: "Consumible"
            else -> null
        }

        if (premioId != null && premioTipo != null) {
            val label = if (premioTipo == "Trinket" || premioTipo == "Carta" || premioTipo == "Consumible") "Consumible" else premioTipo
            Row(
                Modifier.fillMaxWidth().clickable { onPremioClick(premioTipo, premioId) }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DynamicImage(getImagePath(premioId, premioTipo) ?: "", Modifier.size(48.dp).padding(end = 16.dp))
                Text("Ver $label desbloqueado", color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Medium)
            }
        } else {
            Text("Este logro no desbloquea nada específico (o es una característica del juego).", color = Color.LightGray, style = MaterialTheme.typography.body1)
        }
    }
}

@Composable
fun DetailTransformacionScreen(trans: RemoteTransformacion, objetos: List<RemoteObjeto>, onObjetoClick: (RemoteObjeto) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val imgName = getImagePath(trans.id, "Transformacion") ?: ""
            DynamicImage(imgName, Modifier.size(80.dp).padding(end = 16.dp))
            Text(trans.nombre, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(24.dp))
        Text(text = "Efecto:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        Text(text = trans.descripcion, color = Color.White, style = MaterialTheme.typography.body1)
        
        Spacer(Modifier.height(24.dp))
        Text(text = "Objetos que contribuyen:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.h6)
        Spacer(Modifier.height(8.dp))
        
        objetos.forEach { obj ->
            Row(
                Modifier.fillMaxWidth().clickable { onObjetoClick(obj) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DynamicImage(getImagePath(obj.id, "Objeto") ?: "", Modifier.size(40.dp).padding(end = 12.dp))
                Text(obj.nombre, color = Color.White, style = MaterialTheme.typography.body1)
            }
            Divider(color = Color.DarkGray.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun DetailPersonajeScreen(
    nombre: String, 
    descripcion: String?,
    esTainted: Boolean, 
    metodo: String, 
    stats: RemoteEstadisticas?,
    desbloqueos: List<DesbloqueoInfo>, 
    imageName: String?,
    prevPersonaje: RemotePersonaje?,
    nextPersonaje: RemotePersonaje?,
    onPremioClick: (DesbloqueoInfo) -> Unit,
    onNavigate: (RemotePersonaje) -> Unit
) {
    val grayScaleFilter = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (imageName != null) {
                DynamicImage(imageName, Modifier.size(100.dp).padding(end = 16.dp)) 
            }
            Text(nombre, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))
        Text(text = "Estado:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        Text(if (esTainted) "Tainted" else "Normal", color = Color.White, style = MaterialTheme.typography.body1)

        if (descripcion != null) {
            Spacer(Modifier.height(16.dp))
            Text(text = "Descripción:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
            Text(descripcion, color = Color.White, style = MaterialTheme.typography.body1)
        }

        Spacer(Modifier.height(16.dp))
        Text(text = "Método de Desbloqueo:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        Text(metodo, color = Color.White, style = MaterialTheme.typography.body1)

        stats?.let { s ->
            Spacer(Modifier.height(24.dp))
            Text("Estadísticas Iniciales:", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
            HealthDisplay(s)
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
        desbloqueos.forEach { info ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { if(info.logroId != null || info.premioId > 0) onPremioClick(info) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (info.logroId != null) {
                    DynamicImage(
                        name = getImagePath(info.logroId, "Logro") ?: "", 
                        modifier = Modifier.size(56.dp).padding(end = 16.dp),
                        colorFilter = if (!info.desbloqueado) grayScaleFilter else null
                    )
                } else {
                    Box(Modifier.size(56.dp).padding(end = 16.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(info.premioNombre, color = Color.White, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                    if (info.logroDescripcion != null) {
                        Text(info.logroDescripcion, color = Color.Gray, style = MaterialTheme.typography.body2)
                    }
                }
                if(info.logroId != null || info.premioId > 0) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colors.secondary.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }
            Divider(color = Color.DarkGray.copy(alpha = 0.5f))
        }

        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (prevPersonaje != null) {
                Row(
                    modifier = Modifier
                        .clickable { onNavigate(prevPersonaje) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DynamicImage(getImagePath(prevPersonaje.id, "Personaje") ?: "", Modifier.size(40.dp))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Anterior", tint = MaterialTheme.colors.secondary)
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            if (nextPersonaje != null) {
                Row(
                    modifier = Modifier
                        .clickable { onNavigate(nextPersonaje) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Siguiente", tint = MaterialTheme.colors.secondary)
                    Spacer(Modifier.width(8.dp))
                    DynamicImage(getImagePath(nextPersonaje.id, "Personaje") ?: "", Modifier.size(40.dp))
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }
        }
    }
}

@Composable
fun HealthDisplay(stats: RemoteEstadisticas) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Salud: ", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.body2)
        if (stats.salud_aleatoria) {
            Text("Aleatoria", color = Color.White, style = MaterialTheme.typography.body2)
        } else {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                repeat(stats.corazones_rojos) { DynamicImage("HUD_heart_red_full", Modifier.size(20.dp)) }
                repeat(stats.corazones_alma) { DynamicImage("HUD_heart_soul_full", Modifier.size(20.dp)) }
                repeat(stats.corazones_negros) { DynamicImage("HUD_heart_black_full", Modifier.size(20.dp)) }
                repeat(stats.corazones_hueso) { DynamicImage("HUD_heart_bone_full", Modifier.size(20.dp)) }
                repeat(stats.corazones_moneda) { DynamicImage("HUD_heart_coin_full", Modifier.size(20.dp)) }
                if (stats.manto_sagrado) {
                    Spacer(Modifier.width(4.dp))
                    DynamicImage("HUD_holy_mantle", Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.body2)
        Text(value, color = Color.White, style = MaterialTheme.typography.body2)
    }
}

@Composable
fun DrawerContent(onNavigate: (Screen) -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colors.surface).padding(16.dp)) {
        Text("Wiki Isaac", style = MaterialTheme.typography.h5, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        DrawerItem("Inicio", Icons.Default.Home) { onNavigate(Screen.Menu) }
        Divider(color = Color.Gray.copy(alpha = 0.5f))
        DrawerItem("Buscador Global", Icons.Default.Search) { onNavigate(Screen.GlobalSearch) }
        DrawerItem("Objetos", Icons.Default.Info) { onNavigate(Screen.Objetos) }
        DrawerItem("Personajes", Icons.Default.Person) { onNavigate(Screen.Personajes) }
        DrawerItem("Consumibles", Icons.AutoMirrored.Filled.List) { onNavigate(Screen.SubMenuConsumibles) }
        DrawerItem("Transformaciones", Icons.Default.Star) { onNavigate(Screen.Transformaciones) }
        DrawerItem("Logros", Icons.Default.EmojiEvents) { onNavigate(Screen.Logros) }
        DrawerItem("Maldiciones", Icons.Default.Warning) { onNavigate(Screen.Maldiciones) }
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
        MenuButton("Buscador Global", Icons.Default.Search) { onNavigate(Screen.GlobalSearch) }
        MenuButton("Objetos", Icons.Default.Info) { onNavigate(Screen.Objetos) }
        MenuButton("Personajes", Icons.Default.Person) { onNavigate(Screen.Personajes) }
        MenuButton("Consumibles", Icons.AutoMirrored.Filled.List) { onNavigate(Screen.SubMenuConsumibles) }
        MenuButton("Transformaciones", Icons.Default.Star) { onNavigate(Screen.Transformaciones) }
        MenuButton("Logros", Icons.Default.EmojiEvents) { onNavigate(Screen.Logros) }
        MenuButton("Maldiciones", Icons.Default.Warning) { onNavigate(Screen.Maldiciones) }
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
    iconSize: Int,
    getImageFilter: (T) -> ColorFilter? = { null },
    trailingContent: @Composable ((T) -> Unit)? = null,
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
                    DynamicImage(
                        name = imageName, 
                        modifier = Modifier.size(iconSize.dp).padding(end = 16.dp),
                        colorFilter = getImageFilter(item)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(getTitle(item), style = MaterialTheme.typography.h6, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(getSubtitle(item), style = MaterialTheme.typography.body2, color = Color.Gray)
                        if (item is RemoteObjeto) {
                            Spacer(Modifier.width(8.dp))
                            QualityStars(item.calidad, 14)
                        }
                    }
                }
                if (trailingContent != null) {
                    trailingContent(item)
                }
            }
            Divider(color = Color.DarkGray)
        }
    }
}

@Composable
fun DetailScreen(title: String, label1: String, value1: String, label2: String, value2: String, imageName: String?) {
    Column(Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colors.background).verticalScroll(rememberScrollState())) {
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
fun DynamicImage(name: String, modifier: Modifier, colorFilter: ColorFilter? = null) {
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
            modifier = modifier,
            colorFilter = colorFilter
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
