package com.andres.wikitboiandres

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import wikitboiandres.shared.generated.resources.Res
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.andres.wikitboiandres.db.IsaacDatabase
import com.andres.wikitboiandres.network.SteamApiService
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import androidx.compose.foundation.lazy.grid.GridItemSpan
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

enum class Screen {
    Loading, Menu, Objetos, Personajes, SubMenuConsumibles, ConsumiblesPorTipo, DetalleObjeto, DetallePersonaje, DetalleConsumible, GlobalSearch, Transformaciones, DetalleTransformacion, Logros, DetalleLogro, Maldiciones, DetalleMaldicion, Salas, DetalleSala, Jefes, DetalleJefe, Pisos, DetallePiso, Perfil
}

data class SearchItem(val id: Int, val nombre: String, val tipo: String, val original: Any)

@Composable
fun App(database: IsaacDatabase, authManager: AuthManager, syncManager: SyncManager) {
    val client = remember {
        HttpClient {
            install(ContentNegotiation) {
                val jsonConfig = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                }
                json(jsonConfig, ContentType.Application.Json)
                json(jsonConfig, ContentType.Text.Plain)
            }
        }
    }
    val steamApiService = remember { SteamApiService(client) }
    val repository = remember { IsaacRepository(database, steamApiService) }
    
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    
    var currentScreen by remember { mutableStateOf(Screen.Loading) }
    var isDataLoaded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Cargando datos...") }
    val navigationStack = remember { mutableStateListOf<Screen>() }

    val currentUser by authManager.currentUser.collectAsState()
    var showSteamDialog by remember { mutableStateOf(false) }

    var objetos by remember { mutableStateOf(emptyList<RemoteObjeto>()) }
    var personajes by remember { mutableStateOf(emptyList<RemotePersonaje>()) }
    var consumibles by remember { mutableStateOf(emptyList<RemoteConsumible>()) }
    var transformaciones by remember { mutableStateOf(emptyList<RemoteTransformacion>()) }
    var logros by remember { mutableStateOf(emptyList<RemoteLogro>()) }
    var maldiciones by remember { mutableStateOf(emptyList<RemoteMaldicion>()) }
    var salas by remember { mutableStateOf(emptyList<RemoteSala>()) }
    var jefes by remember { mutableStateOf(emptyList<RemoteJefe>()) }
    var pisos by remember { mutableStateOf(emptyList<RemotePiso>()) }

    var searchQueryObjetos by remember { mutableStateOf("") }
    var searchQueryConsumibles by remember { mutableStateOf("") }
    var searchQueryLogros by remember { mutableStateOf("") }
    var searchQueryMaldiciones by remember { mutableStateOf("") }
    var searchQueryJefes by remember { mutableStateOf("") }
    var searchQueryGlobal by remember { mutableStateOf("") }

    var isObjectsGridView by remember { mutableStateOf(false) }
    var isConsumiblesGridView by remember { mutableStateOf(false) }
    var isLogrosGridView by remember { mutableStateOf(false) }
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

    val allSearchItems = remember(objetos, personajes, consumibles, logros, maldiciones, jefes, pisos) {
        val list = mutableListOf<SearchItem>()
        objetos.forEach { list.add(SearchItem(it.id, it.nombre, "Objeto", it)) }
        personajes.forEach { list.add(SearchItem(it.id, it.nombre, "Personaje", it)) }
        consumibles.forEach { list.add(SearchItem(it.id, it.nombre, it.tipo, it)) }
        logros.forEach { list.add(SearchItem(it.id, it.nombre, "Logro", it)) }
        maldiciones.forEach { list.add(SearchItem(it.id, it.nombre, "Maldicion", it)) }
        jefes.forEach { list.add(SearchItem(it.id, it.nombre, "Jefe", it)) }
        pisos.forEach { list.add(SearchItem(it.id, it.nombre, "Piso", it)) }
        list
    }

    var selectedObjeto by remember { mutableStateOf<RemoteObjeto?>(null) }
    var selectedPersonaje by remember { mutableStateOf<RemotePersonaje?>(null) }
    var selectedConsumible by remember { mutableStateOf<RemoteConsumible?>(null) }
    var selectedTransformacion by remember { mutableStateOf<RemoteTransformacion?>(null) }
    var selectedLogro by remember { mutableStateOf<RemoteLogro?>(null) }
    var selectedMaldicion by remember { mutableStateOf<RemoteMaldicion?>(null) }
    var selectedSala by remember { mutableStateOf<RemoteSala?>(null) }
    var selectedJefe by remember { mutableStateOf<RemoteJefe?>(null) }
    var selectedPiso by remember { mutableStateOf<RemotePiso?>(null) }

    val syncAchievements: (isManualAction: Boolean) -> Unit = { isManualAction ->
        val user = currentUser
        if (user != null) {
            scope.launch {
                try {
                    val localLogros = repository.getAllLogros()
                    val localUnlockedIds = localLogros.filter { it.desbloqueado }.map { it.id }

                    if (isManualAction) {
                        println("Sync: Acción manual. Sobrescribiendo la nube con progreso local.")
                        syncManager.uploadAchievements(user.uid, localUnlockedIds)
                    } else {
                        val remoteIds = syncManager.downloadAchievements(user.uid) ?: return@launch println("Sync: Error de conexión, abortando sincronización")

                        val mergedIds = (localUnlockedIds + remoteIds).toSet()

                        if (mergedIds.size > localUnlockedIds.size) {
                            println("Sync: Actualizando DB local con logros de la nube")
                            mergedIds.forEach { id ->
                                repository.updateLogroStatus(id, true)
                            }
                            logros = repository.getAllLogros()
                            selectedLogro?.let { current ->
                                selectedLogro = repository.getLogroById(current.id)
                            }
                        }

                        if (mergedIds.size > remoteIds.size) {
                            println("Sync: Subiendo progreso local fusionado a la nube")
                            syncManager.uploadAchievements(user.uid, mergedIds.toList())
                        }
                    }
                } catch (e: Exception) {
                    println("Sync: Error inesperado: ${e.message}")
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        try {
            if (repository.getAllObjetosCount() == 0L) {
                status = "Cargando..."
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
                repository.fetchAndSaveSinergias()
                repository.fetchAndSaveSalas()
                repository.fetchAndSavePisos()
                repository.fetchAndSaveJefes()
            }
            objetos = repository.getAllObjetos()
            personajes = repository.getAllPersonajes()
            consumibles = repository.getAllConsumibles()
            transformaciones = repository.getAllTransformaciones()
            logros = repository.getAllLogros()
            maldiciones = repository.getAllMaldiciones()
            salas = repository.getAllSalas()
            pisos = repository.getAllPisos()
            jefes = repository.getAllJefes()
            
            isDataLoaded = true
            currentScreen = Screen.Menu
        } catch (e: Exception) {
            status = "Error: ${e.message}"
        }
    }

    LaunchedEffect(currentUser, isDataLoaded) {
        if (currentUser != null && isDataLoaded) {
            syncAchievements(false)
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
        } else {
            currentScreen = Screen.Menu
        }
    }

    val grayScaleFilter = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }

    MaterialTheme(colors = darkColors) {
        Scaffold(
            modifier = Modifier.systemBarsPadding(),
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DynamicImage("dice-logo", Modifier.size(32.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Isaac Wiki", fontWeight = FontWeight.Bold)
                        }
                    },
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
                DrawerContent(
                    user = currentUser,
                    onSignIn = { scope.launch { authManager.signInWithGoogle() } },
                    onSignOut = { scope.launch { authManager.signOut() } },
                    onSyncSteam = { 
                        showSteamDialog = true 
                    },
                    onNavigate = { screen ->
                        navigationStack.clear()
                        currentScreen = screen
                        scope.launch { scaffoldState.drawerState.close() }
                    }
                )
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
                    Screen.Perfil -> {
                        PerfilScreen(
                            user = currentUser,
                            logrosTotales = logros.size,
                            logrosDesbloqueados = logros.count { it.desbloqueado },
                            objetosTotales = objetos.size,
                            personajes = personajes,
                            getDesbloqueos = { perId -> repository.getDesbloqueosByPersonaje(perId) },
                            onSyncSteamClick = { showSteamDialog = true }
                        )
                    }
                    Screen.ConsumiblesPorTipo -> Column {
                        val validTypes = listOf("Trinket", "Carta", "Píldora", "Pildora", "Consumible", "Pickup")

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(end = 8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (selectedTipoConsumible in validTypes) {
                                    SearchBar(searchQueryConsumibles) { searchQueryConsumibles = it }
                                }
                            }
                            if (selectedTipoConsumible in validTypes) {
                                IconButton(onClick = { isConsumiblesGridView = !isConsumiblesGridView }) {
                                    Icon(
                                        imageVector = if (isConsumiblesGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                                        contentDescription = "Cambiar vista",
                                        tint = orangeColor
                                    )
                                }
                            }
                        }

                        if (isConsumiblesGridView && selectedTipoConsumible in validTypes) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(80.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(filteredConsumibles) { cons ->
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .aspectRatio(1f)
                                            .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedConsumible = cons
                                                navigateTo(Screen.DetalleConsumible)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        DynamicImage(getImagePath(cons.id, cons.tipo, cons.uid) ?: "", Modifier.size(48.dp))
                                    }
                                }
                            }
                        } else {
                            ListScreen(filteredConsumibles, { it.nombre }, { it.tipo }, { getImagePath(it.id, it.tipo, it.uid) }, 48) {
                                selectedConsumible = it
                                navigateTo(Screen.DetalleConsumible)
                            }
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Progreso: $unlockedLogros / $totalLogros (${(progress * 100).toInt()}%)",
                                    color = Color.White,
                                    style = MaterialTheme.typography.subtitle1,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { isLogrosGridView = !isLogrosGridView }) {
                                    Icon(
                                        imageVector = if (isLogrosGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                                        contentDescription = "Cambiar vista",
                                        tint = orangeColor
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = orangeColor,
                                backgroundColor = Color.DarkGray
                            )
                        }

                        SearchBar(searchQueryLogros) { searchQueryLogros = it }
                        
                        if (isLogrosGridView) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(80.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(filteredLogros) { logro ->
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .aspectRatio(1f)
                                            .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                                            .clickable { 
                                                selectedLogro = logro
                                                navigateTo(Screen.DetalleLogro)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        DynamicImage(
                                            getImagePath(logro.id, "Logro") ?: "", 
                                            Modifier.size(56.dp),
                                            colorFilter = if (!logro.desbloqueado) grayScaleFilter else null
                                        )
                                        Checkbox(
                                            checked = logro.desbloqueado,
                                            onCheckedChange = { newVal ->
                                                repository.updateLogroStatus(logro.id, newVal)
                                                logros = repository.getAllLogros()
                                                syncAchievements(true)
                                            },
                                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp),
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = orangeColor,
                                                uncheckedColor = Color.Gray.copy(alpha = 0.5f),
                                                checkmarkColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
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
                                            logros = repository.getAllLogros()
                                            syncAchievements(true)
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
                    }
                    Screen.Maldiciones -> Column {
                        SearchBar(searchQueryMaldiciones) { searchQueryMaldiciones = it }
                        ListScreen(filteredMaldiciones, { it.nombre }, { it.descripcion.take(50) + "..." }, { getImagePath(it.id, "Maldicion") }, 56) {
                            selectedMaldicion = it
                            navigateTo(Screen.DetalleMaldicion)
                        }
                    }
                    Screen.Salas -> {
                        ListScreen(salas, { it.nombre }, { "" }, { getImagePath(it.id, "Sala") }, 48) {
                            selectedSala = it
                            navigateTo(Screen.DetalleSala)

                        }
                    }
                    Screen.Jefes -> {
                        val filteredJefes = jefes.filter { it.nombre.contains(searchQueryJefes, ignoreCase = true) }
                        Column {
                            SearchBar(searchQueryJefes) { searchQueryJefes = it }
                            ListScreen(filteredJefes, { it.nombre }, { "Vida Base: ${it.vida_base}" }, { getImagePath(it.id, "Jefe") }, 56) {
                                selectedJefe = it
                                navigateTo(Screen.DetalleJefe)
                            }
                        }
                    }
                    Screen.DetalleJefe -> selectedJefe?.let { jefe ->
                        val pisosDelJefe = remember(jefe.id) { repository.getPisosByJefe(jefe.id) }

                        // Calcular jefe anterior y siguiente
                        val currentIndex = jefes.indexOfFirst { it.id == jefe.id }
                        val prevJefe = if (currentIndex > 0) jefes[currentIndex - 1] else null
                        val nextJefe = if (currentIndex != -1 && currentIndex < jefes.size - 1) jefes[currentIndex + 1] else null

                        DetailJefeScreen(
                            jefe = jefe,
                            pisos = pisosDelJefe,
                            prevJefe = prevJefe,
                            nextJefe = nextJefe,
                            onPisoClick = {
                                selectedPiso = it
                                navigateTo(Screen.DetallePiso)
                            },
                            onNavigate = {
                                selectedJefe = it
                            }
                        )
                    }
                    Screen.Pisos -> ListScreen(
                        itemList = pisos,
                        getTitle = { it.nombre },
                        getSubtitle = { it.descripcion.take(60) + "..." },
                        getImageName = { getImagePath(it.id, "Chapter") },
                        iconSize = 64
                    ) { piso ->
                        selectedPiso = piso
                        navigateTo(Screen.DetallePiso)
                    }

                    Screen.DetallePiso -> selectedPiso?.let { piso ->
                        // Consultamos la tabla intermedia usando el ID del piso seleccionado
                        val jefesDelPiso = remember(piso.id) { repository.getJefesByPiso(piso.id) }

                        DetailPisoScreen(
                            piso = piso,
                            jefes = jefesDelPiso,
                            onJefeClick = { jefe ->
                                selectedJefe = jefe
                                navigateTo(Screen.DetalleJefe)
                            }
                        )
                    }
                    Screen.DetalleObjeto -> selectedObjeto?.let { obj ->
                        val relatedLogros = remember(obj.id) { repository.getLogrosByRewardObjeto(obj.id) }
                        val sinergias = remember(obj.id) { repository.getSinergiasByObjeto(obj.id) }
                        val itemSalas = remember(obj.id) { repository.getSalasByObjeto(obj.id) }
                        val currentIndex = objetos.indexOfFirst { it.id == obj.id }
                        val prevObjeto = if (currentIndex > 0) objetos[currentIndex - 1] else null
                        val nextObjeto = if (currentIndex != -1 && currentIndex < objetos.size - 1) objetos[currentIndex + 1] else null

                        DetailObjetoScreen(
                            objeto = obj,
                            relatedLogros = relatedLogros,
                            sinergias = sinergias,
                            salas = itemSalas,
                            repository = repository,
                            onLogroClick = { logro ->
                                selectedLogro = logro
                                navigateTo(Screen.DetalleLogro)
                            },
                            onObjetoClick = { newObj ->
                                selectedObjeto = newObj
                            },
                            onSalaClick = { sala ->
                                selectedSala = sala
                                navigateTo(Screen.DetalleSala)
                            },
                            prevObjeto = prevObjeto,
                            nextObjeto = nextObjeto
                        )
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
                            rewardConsumibleUid = rewardConsumible?.uid,
                            onStatusChange = { newVal ->
                                repository.updateLogroStatus(logro.id, newVal)
                                logros = repository.getAllLogros()
                                selectedLogro = repository.getLogroById(logro.id)
                                scope.launch { syncAchievements(true) }
                            }
                        ) { type, id ->
                            when(type) {
                                "Objeto" -> { selectedObjeto = repository.getObjetoById(id); navigateTo(Screen.DetalleObjeto) }
                                "Personaje" -> { selectedPersonaje = repository.getPersonajeById(id); navigateTo(Screen.DetallePersonaje) }
                                "Consumible", "Trinket", "Carta", "Píldora", "Pildora" -> { 
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
                    Screen.DetalleSala -> selectedSala?.let { sala ->
                        val objs = remember(sala.id) { repository.getObjetosBySala(sala.id) }
                        DetailSalaScreen(sala, objs) {
                            selectedObjeto = it
                            navigateTo(Screen.DetalleObjeto)
                        }
                    }
                    Screen.DetallePersonaje -> selectedPersonaje?.let { per ->
                        val desbloqueos = remember(per.id) { repository.getDesbloqueosByPersonaje(per.id) }
                        val stats = remember(per.id) { repository.getEstadisticasByPersonaje(per.id) }
                        val unlockLogros = remember(per.id, logros) { logros.filter { it.desbloquea_personaje_id == per.id } }
                        val currentIndex = personajes.indexOfFirst { it.id == per.id }
                        val prevPersonaje = if (currentIndex > 0) personajes[currentIndex - 1] else null
                        val nextPersonaje = if (currentIndex != -1 && currentIndex < personajes.size - 1) personajes[currentIndex + 1] else null

                        DetailPersonajeScreen(
                            per = per,
                            stats = stats,
                            desbloqueos = desbloqueos,
                            unlockLogros = unlockLogros,
                            imageName = getImagePath(per.id, "Personaje"),
                            prevPersonaje = prevPersonaje,
                            nextPersonaje = nextPersonaje,
                            onLogroStatusChange = { logroId, newVal ->
                                repository.updateLogroStatus(logroId, newVal)
                                logros = repository.getAllLogros()
                                syncAchievements(true)
                            },
                            onLogroClick = { 
                                selectedLogro = it
                                navigateTo(Screen.DetalleLogro)
                            },
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
                    Screen.DetalleConsumible -> selectedConsumible?.let { cons ->
                        val currentIndex = filteredConsumibles.indexOfFirst { it.uid == cons.uid }
                        val prevConsumible = if (currentIndex > 0) filteredConsumibles[currentIndex - 1] else null
                        val nextConsumible = if (currentIndex != -1 && currentIndex < filteredConsumibles.size - 1) filteredConsumibles[currentIndex + 1] else null

                        DetailConsumibleScreen(
                            consumible = cons,
                            prevConsumible = prevConsumible,
                            nextConsumible = nextConsumible,
                            onNavigate = { selectedConsumible = it }
                        )
                    }
                    Screen.GlobalSearch -> GlobalSearchScreen(
                        allItems = allSearchItems,
                        query = searchQueryGlobal,
                        onQueryChange = { searchQueryGlobal = it },
                        onItemClick = { item ->
                            when (item.original) {
                                is RemoteObjeto -> { selectedObjeto = item.original; navigateTo(Screen.DetalleObjeto) }
                                is RemotePersonaje -> { selectedPersonaje = item.original; navigateTo(Screen.DetallePersonaje) }
                                is RemoteConsumible -> { selectedConsumible = item.original; navigateTo(Screen.DetalleConsumible) }
                                is RemoteLogro -> { selectedLogro = item.original; navigateTo(Screen.DetalleLogro) }
                                is RemoteMaldicion -> { selectedMaldicion = item.original; navigateTo(Screen.DetalleMaldicion) }
                                is RemoteJefe -> { selectedJefe = item.original; navigateTo(Screen.DetalleJefe) }
                                is RemotePiso -> { selectedPiso = item.original; navigateTo(Screen.DetallePiso) }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showSteamDialog) {
        var isSyncing by remember { mutableStateOf(false) }
        
        SteamSyncDialog(
            isSyncing = isSyncing,
            onDismiss = { if (!isSyncing) showSteamDialog = false },
            onSync = { apiKey, steamId ->
                isSyncing = true
                scope.launch {
                    try {
                        val result = repository.syncAchievementsWithSteam(apiKey, steamId)
                        if (result.isSuccess) {
                            logros = repository.getAllLogros()
                            scaffoldState.snackbarHostState.showSnackbar("¡Sincronización con Steam completada!")
                            showSteamDialog = false
                        } else {
                            val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                            scaffoldState.snackbarHostState.showSnackbar("Error: $error")
                        }
                    } catch (e: Exception) {
                        scaffoldState.snackbarHostState.showSnackbar("Error de red: ${e.message}")
                    } finally {
                        isSyncing = false
                    }
                }
            }
        )
    }
}

@Composable
fun SteamSyncDialog(isSyncing: Boolean, onDismiss: () -> Unit, onSync: (String, String) -> Unit) {
    var apiKey by remember { mutableStateOf("") }
    var steamId by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), backgroundColor = MaterialTheme.colors.surface, elevation = 8.dp) {
            Column(Modifier.padding(24.dp)) {
                Text("Sincronizar con Steam", style = MaterialTheme.typography.h6, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text("Tu perfil de Steam debe ser Público", style = MaterialTheme.typography.caption, color = Color.LightGray)
                Spacer(Modifier.height(16.dp))
                
                TextField(
                    value = apiKey, onValueChange = { apiKey = it },
                    label = { Text("Steam API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSyncing
                )
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = steamId, onValueChange = { steamId = it },
                    label = { Text("Steam ID64 (17 dígitos)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSyncing
                )
                
                Spacer(Modifier.height(24.dp))
                
                if (isSyncing) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFF4500))
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancelar") }
                        Button(
                            onClick = { if (apiKey.isNotBlank() && steamId.isNotBlank()) onSync(apiKey.trim(), steamId.trim()) },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF4500))
                        ) {
                            Text("Sincronizar", color = Color.White)
                        }
                    }
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
        
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                DynamicImage("dice-logo", Modifier.size(240.dp).alpha(0.1f))
            }

            if (query.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Escribe para buscar objetos, personajes, logros, maldiciones...",
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
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
                                .background(MaterialTheme.colors.surface.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                                .clickable { onItemClick(item) },
                            contentAlignment = Alignment.Center
                        ) {
                            val imageName = if (item.original is RemoteConsumible) {
                                getImagePath(item.id, item.tipo, item.original.uid)
                            } else {
                                getImagePath(item.id, item.tipo)
                            }
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
}

fun getImagePath(id: Int, tipo: String, uid: Int? = null): String? {
    return when (tipo) {
        "Objeto" -> "collectibles_${id.toString().padStart(3, '0')}"
        "Trinket" -> "collectibles_${2000 + id}"
        "Personaje" -> "Character_${id}_icon"
        "Carta" -> "Pickup_$id"
        "Transformacion" -> "Transformation_$id"
        "Logro" -> "Logro_$id"
        "Maldicion" -> "maldicion_$id"
        "Sala" -> "sala_$id"
        "Jefe" -> "jefe_$id"
        "Piso" -> "piso_$id"
        "Chapter" -> "chapter_$id"
        "Píldora", "Pildora" -> {
            val pillNumber = (id % 14) + 1
            "pill_$pillNumber"
        }
        "Consumible", "Pickup" -> if (uid != null) "consumible_$uid" else null
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
fun DetailObjetoScreen(
    objeto: RemoteObjeto, 
    relatedLogros: List<RemoteLogro>, 
    sinergias: List<SinergiaInfo>,
    salas: List<RemoteSala>,
    repository: IsaacRepository,
    onLogroClick: (RemoteLogro) -> Unit,
    onObjetoClick: (RemoteObjeto) -> Unit,
    onSalaClick: (RemoteSala) -> Unit,
    prevObjeto: RemoteObjeto? = null,
    nextObjeto: RemoteObjeto? = null
) {
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
        DescriptionText(text = objeto.descripcion, color = Color.White, style = MaterialTheme.typography.body1)

        if (salas.isNotEmpty()) {
            Spacer(Modifier.height(32.dp))
            Text(text = "Puede aparecer en:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                salas.forEach { sala ->
                    Row(
                        Modifier.padding(end = 12.dp)
                            .clickable { onSalaClick(sala) }
                            .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DynamicImage("sala_${sala.id}", Modifier.size(32.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(sala.nombre, color = Color.White, style = MaterialTheme.typography.body2)
                    }
                }
            }
        }

        if (sinergias.isNotEmpty()) {
            Spacer(Modifier.height(32.dp))
            Text(text = "Sinergias Especiales:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))
            sinergias.forEach { sin ->
                val otherObj = remember(sin.objetoRelacionadoId) { repository.getObjetoById(sin.objetoRelacionadoId) }
                Row(
                    Modifier.fillMaxWidth().clickable { otherObj?.let { onObjetoClick(it) } }.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DynamicImage(getImagePath(sin.objetoRelacionadoId, "Objeto") ?: "", Modifier.size(48.dp).padding(end = 16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(otherObj?.nombre ?: "Objeto Desconocido", color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
                        DescriptionText(sin.descripcion, color = Color.White, style = MaterialTheme.typography.body2)
                    }
                }
                Divider(color = Color.DarkGray.copy(alpha = 0.5f))
            }
        }

        if (relatedLogros.isNotEmpty()) {
            Spacer(Modifier.height(32.dp))
            Text(text = "Desbloqueado con:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))
            relatedLogros.forEach { logro ->
                Row(Modifier.fillMaxWidth().clickable { onLogroClick(logro) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    DynamicImage(getImagePath(logro.id, "Logro") ?: "", Modifier.size(40.dp).padding(end = 12.dp))
                    Text(logro.nombre, color = Color.White, style = MaterialTheme.typography.body1)
                }
                Divider(color = Color.DarkGray.copy(alpha = 0.5f))
            }
        }
        
        Spacer(Modifier.height(32.dp))
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (prevObjeto != null) {
                Row(Modifier.clickable { onObjetoClick(prevObjeto) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colors.secondary)
                    Spacer(Modifier.width(8.dp))
                    DynamicImage(getImagePath(prevObjeto.id, "Objeto") ?: "", Modifier.size(40.dp))
                }
            } else Spacer(Modifier.width(1.dp))

            Text(text = "ID: ${objeto.id}", color = Color.Gray.copy(alpha = 0.5f), style = MaterialTheme.typography.caption)

            if (nextObjeto != null) {
                Row(Modifier.clickable { onObjetoClick(nextObjeto) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    DynamicImage(getImagePath(nextObjeto.id, "Objeto") ?: "", Modifier.size(40.dp))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colors.secondary)
                }
            } else Spacer(Modifier.width(1.dp))
        }
    }
}

@Composable
fun DetailLogroScreen(logro: RemoteLogro, rewardConsumibleTipo: String?, rewardConsumibleUid: Int?, onStatusChange: (Boolean) -> Unit, onPremioClick: (String, Int) -> Unit) {
    val grayScaleFilter = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
    Column(Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colors.background).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 16.dp)) {
                DynamicImage(name = getImagePath(logro.id, "Logro") ?: "", modifier = Modifier.size(80.dp), colorFilter = if (!logro.desbloqueado) grayScaleFilter else null)
                Text(text = "Secreto: ${logro.id}", color = Color.Gray, style = MaterialTheme.typography.caption, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text(logro.nombre, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
            }
            Checkbox(checked = logro.desbloqueado, onCheckedChange = onStatusChange, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.secondary))
        }
        Spacer(Modifier.height(24.dp))
        Text(text = "Requisito:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        DescriptionText(text = logro.descripcion, color = Color.White, style = MaterialTheme.typography.body1)
        Spacer(Modifier.height(24.dp))
        Text(text = "Recompensa:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        val premioId = logro.desbloquea_objeto_id ?: logro.desbloquea_personaje_id ?: logro.desbloquea_consumible_id
        val premioTipo = when {
            logro.desbloquea_objeto_id != null -> "Objeto"; logro.desbloquea_personaje_id != null -> "Personaje"; logro.desbloquea_consumible_id != null -> rewardConsumibleTipo ?: "Consumible"; else -> null
        }
        if (premioId != null && premioTipo != null) {
            val label = if (premioTipo == "Trinket" || premioTipo == "Carta" || premioTipo == "Consumible" || premioTipo == "Píldora" || premioTipo == "Pildora") "Consumible" else premioTipo
            Row(Modifier.fillMaxWidth().clickable { onPremioClick(premioTipo, premioId) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                DynamicImage(getImagePath(premioId, premioTipo, rewardConsumibleUid) ?: "", Modifier.size(48.dp).padding(end = 16.dp))
                Text("Ver $label desbloqueado", color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Medium)
            }
        } else {
            Text("Este logro no desbloquea nada específico.", color = Color.LightGray, style = MaterialTheme.typography.body1)
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
        DescriptionText(text = trans.descripcion, color = Color.White, style = MaterialTheme.typography.body1)
        Spacer(Modifier.height(24.dp))
        Text(text = "Objetos que contribuyen:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.h6)
        Spacer(Modifier.height(8.dp))
        objetos.forEach { obj ->
            Row(Modifier.fillMaxWidth().clickable { onObjetoClick(obj) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                DynamicImage(getImagePath(obj.id, "Objeto") ?: "", Modifier.size(40.dp).padding(end = 12.dp))
                Text(obj.nombre, color = Color.White, style = MaterialTheme.typography.body1)
            }
            Divider(color = Color.DarkGray.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun DetailSalaScreen(sala: RemoteSala, objetos: List<RemoteObjeto>, onObjetoClick: (RemoteObjeto) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(64.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DynamicImage(getImagePath(sala.id, "Sala") ?: "", Modifier.size(80.dp).padding(end = 16.dp))
                    Text(sala.nombre, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
                Text(text = "Descripción:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
                DescriptionText(text = sala.descripcion, color = Color.White, style = MaterialTheme.typography.body1)
                Spacer(Modifier.height(24.dp))
                Text(text = "Objetos que pueden aparecer:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.h6)
            }
        }
        items(objetos) { obj ->
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .aspectRatio(1f)
                    .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                    .clickable { onObjetoClick(obj) },
                contentAlignment = Alignment.Center
            ) {
                DynamicImage(getImagePath(obj.id, "Objeto") ?: "", Modifier.size(40.dp))
            }
        }
    }
}
@Composable
fun DetailPersonajeScreen(per: RemotePersonaje, stats: RemoteEstadisticas?, desbloqueos: List<DesbloqueoInfo>, unlockLogros: List<RemoteLogro>, imageName: String?, prevPersonaje: RemotePersonaje?, nextPersonaje: RemotePersonaje?, onLogroStatusChange: (Int, Boolean) -> Unit, onLogroClick: (RemoteLogro) -> Unit, onPremioClick: (DesbloqueoInfo) -> Unit, onNavigate: (RemotePersonaje) -> Unit) {
    val grayScaleFilter = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (imageName != null) DynamicImage(imageName, Modifier.size(100.dp).padding(end = 16.dp))
            Text(per.nombre, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
        Text(text = "Estado:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        Text(if (per.es_tainted) "Tainted" else "Normal", color = Color.White, style = MaterialTheme.typography.body1)
        if (per.descripcion != null) {
            Spacer(Modifier.height(16.dp))
            Text(text = "Descripción:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
            DescriptionText(per.descripcion, color = Color.White, style = MaterialTheme.typography.body1)
        }
        Spacer(Modifier.height(16.dp))
        Text(text = "Método de Desbloqueo:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        DescriptionText(per.metodo_desbloqueo ?: "Desbloqueado por defecto", color = Color.White, style = MaterialTheme.typography.body1)
        
        if (unlockLogros.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            unlockLogros.forEach { logro ->
                Row(Modifier.fillMaxWidth().clickable { onLogroClick(logro) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    DynamicImage(name = getImagePath(logro.id, "Logro") ?: "", modifier = Modifier.size(56.dp).padding(end = 16.dp), colorFilter = if (!logro.desbloqueado) grayScaleFilter else null)
                    Column(Modifier.weight(1f)) {
                        Text(logro.nombre, color = Color.White, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                        DescriptionText(logro.descripcion, color = Color.Gray, style = MaterialTheme.typography.body2)
                    }
                    Checkbox(
                        checked = logro.desbloqueado, 
                        onCheckedChange = { onLogroStatusChange(logro.id, it) }, 
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.secondary)
                    )
                }
                Divider(color = Color.DarkGray.copy(alpha = 0.5f))
            }
        }

        stats?.let { s ->
            Spacer(Modifier.height(24.dp))
            Text("Estadísticas Iniciales:", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
            HealthDisplay(s)
            StatRow("Velocidad", s.velocidad.toString()); StatRow("Lágrimas", s.lagrimas.toString()); StatRow("Daño", s.dano.toString()); StatRow("Alcance", s.rango.toString()); StatRow("Vel. Disparo", s.velocidad_disparo.toString()); StatRow("Suerte", s.suerte.toString())
        }
        Spacer(Modifier.height(24.dp))
        Text("Desbloqueos (Post-it):", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        desbloqueos.forEach { info ->
            Row(Modifier.fillMaxWidth().clickable { if(info.logroId != null || info.premioId > 0) onPremioClick(info) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (info.logroId != null) DynamicImage(name = getImagePath(info.logroId, "Logro") ?: "", modifier = Modifier.size(56.dp).padding(end = 16.dp), colorFilter = if (!info.desbloqueado) grayScaleFilter else null) else Box(Modifier.size(56.dp).padding(end = 16.dp))
                Column(Modifier.weight(1f)) {
                    Text(info.premioNombre, color = Color.White, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                    if (info.logroDescripcion != null) DescriptionText(info.logroDescripcion, color = Color.Gray, style = MaterialTheme.typography.body2)
                }
                if(info.logroId != null || info.premioId > 0) Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colors.secondary.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }
            Divider(color = Color.DarkGray.copy(alpha = 0.5f))
        }
        Spacer(Modifier.height(32.dp))
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (prevPersonaje != null) {
                Row(Modifier.clickable { onNavigate(prevPersonaje) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colors.secondary)
                    Spacer(Modifier.width(8.dp))
                    DynamicImage(getImagePath(prevPersonaje.id, "Personaje") ?: "", Modifier.size(40.dp))
                }
            } else Spacer(Modifier.width(1.dp))

            Text(text = "ID: ${per.id}", color = Color.Gray.copy(alpha = 0.5f), style = MaterialTheme.typography.caption)

            if (nextPersonaje != null) {
                Row(Modifier.clickable { onNavigate(nextPersonaje) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    DynamicImage(getImagePath(nextPersonaje.id, "Personaje") ?: "", Modifier.size(40.dp))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colors.secondary)
                }
            } else Spacer(Modifier.width(1.dp))
        }
    }
}

@Composable
fun HealthDisplay(stats: RemoteEstadisticas) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Salud: ", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.body2)
        if (stats.salud_aleatoria) Text("Aleatoria", color = Color.White, style = MaterialTheme.typography.body2)
        else Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            repeat(stats.corazones_rojos) { DynamicImage("HUD_heart_red_full", Modifier.size(20.dp)) }
            repeat(stats.corazones_alma) { DynamicImage("HUD_heart_soul_full", Modifier.size(20.dp)) }
            repeat(stats.corazones_negros) { DynamicImage("HUD_heart_black_full", Modifier.size(20.dp)) }
            repeat(stats.corazones_hueso) { DynamicImage("HUD_heart_bone_full", Modifier.size(20.dp)) }
            repeat(stats.corazones_moneda) { DynamicImage("HUD_heart_coin_full", Modifier.size(20.dp)) }
            if (stats.manto_sagrado) { Spacer(Modifier.width(4.dp)); DynamicImage("HUD_holy_mantle", Modifier.size(20.dp)) }
        }
    }
}
@Composable
fun PerfilScreen(
    user: AuthUser?,
    logrosTotales: Int,
    logrosDesbloqueados: Int,
    objetosTotales: Int,
    personajes: List<RemotePersonaje>,
    getDesbloqueos: (Int) -> List<DesbloqueoInfo>,
    onSyncSteamClick: () -> Unit
) {
    val progress = if (logrosTotales > 0) logrosDesbloqueados.toFloat() / logrosTotales else 0f
    val orangeColor = Color(0xFFFF4500)
    var tabIndex by remember { mutableStateOf(0) } // 0 = Normal, 1 = Tainted

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar",
            tint = orangeColor,
            modifier = Modifier.size(100.dp).padding(bottom = 16.dp)
        )
        Text(user?.name ?: "Usuario Invitado", style = MaterialTheme.typography.h4, color = Color.White, fontWeight = FontWeight.Bold)
        Text(user?.email ?: "Inicia sesión para guardar tu progreso", color = Color.Gray, style = MaterialTheme.typography.body1)

        Spacer(Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            item {
                Card(backgroundColor = MaterialTheme.colors.surface, shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(8.dp).fillMaxSize()) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("Logros", color = orangeColor, fontWeight = FontWeight.Bold)
                        Text("$logrosDesbloqueados / $logrosTotales", color = Color.White, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                        Text("${(progress * 100).toInt()}%", color = Color.Gray, style = MaterialTheme.typography.caption)
                    }
                }
            }
            item {
                Card(backgroundColor = MaterialTheme.colors.surface, shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(8.dp).fillMaxSize()) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("Objetos", color = orangeColor, fontWeight = FontWeight.Bold)
                        Text("$objetosTotales", color = Color.White, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                        Text("En Base de Datos", color = Color.Gray, style = MaterialTheme.typography.caption)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text("Marcas del Post-It", style = MaterialTheme.typography.h6, color = Color.White, modifier = Modifier.align(Alignment.Start).padding(horizontal = 16.dp))
        Spacer(Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = tabIndex,
            backgroundColor = Color.Transparent,
            contentColor = orangeColor,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Normales", fontWeight = FontWeight.Bold) })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Oscuros", fontWeight = FontWeight.Bold) })
        }

        Spacer(Modifier.height(16.dp))

        val filteredPersonajes = personajes.filter { if (tabIndex == 0) !it.es_tainted else it.es_tainted }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredPersonajes.size) { index ->
                val per = filteredPersonajes[index]

                val marcas = getDesbloqueos(per.id)
                val marcasDesbloqueadas = marcas.count { it.desbloqueado }

                Card(
                    backgroundColor = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.width(280.dp).wrapContentHeight()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DynamicImage(getImagePath(per.id, "Personaje") ?: "", Modifier.size(40.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(per.nombre, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle1)
                                Text("$marcasDesbloqueadas / ${marcas.size} completado", color = Color.Gray, style = MaterialTheme.typography.caption)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        val grayScale = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        Column(modifier = Modifier.fillMaxWidth()) {
                            marcas.chunked(4).forEach { filaMarcas ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    filaMarcas.forEach { marca ->
                                        val iconName = "marca_${marca.marcaId}"

                                        // Contenedor que hace de "ranura" para la marca
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    color = if (marca.desbloqueado) Color.Transparent else Color.Black.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            DynamicImage(
                                                name = iconName,
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .alpha(if (marca.desbloqueado) 1f else 0.4f),
                                                colorFilter = if (marca.desbloqueado) null else grayScale
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))

        // 4. Panel de Acciones Inferior
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("Gestión de Cuenta", style = MaterialTheme.typography.h6, color = Color.White, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onSyncSteamClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                border = BorderStroke(1.dp, orangeColor),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent)
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, tint = orangeColor)
                Spacer(Modifier.width(12.dp))
                Text("Sincronizar progreso con Steam", color = Color.White, fontWeight = FontWeight.Bold)
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
fun DrawerContent(user: AuthUser?, onSignIn: () -> Unit, onSignOut: () -> Unit, onSyncSteam: () -> Unit, onNavigate: (Screen) -> Unit) {
    val orangeColor = Color(0xFFFF4500)
    Column(Modifier.fillMaxSize().background(MaterialTheme.colors.surface)) {
        Box(Modifier.fillMaxWidth().background(orangeColor).padding(24.dp)) {
            if (user != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(64.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp), tint = orangeColor) }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(user.name ?: "Usuario", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(user.email ?: "", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text("Cerrar Sesión", color = Color.White, modifier = Modifier.clickable { onSignOut() }.padding(top = 4.dp), style = MaterialTheme.typography.caption, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(Modifier.clickable { onSignIn() }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.width(16.dp))
                    Column { Text("Iniciar Sesión", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("Sincroniza tus logros", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp) }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Sincronización", style = MaterialTheme.typography.caption, color = Color.Gray, fontWeight = FontWeight.Bold)
            DrawerItem("Sincronizar Steam", imageName = "steam-logo") { onSyncSteam() }
            Spacer(Modifier.height(16.dp))
            Text("Navegación", style = MaterialTheme.typography.caption, color = Color.Gray, fontWeight = FontWeight.Bold)
            DrawerItem("Inicio", imageName = "dice-logo") { onNavigate(Screen.Menu) }
            DrawerItem("Mi Perfil", Icons.Default.Person) { onNavigate(Screen.Perfil) }
            Divider(color = Color.Gray.copy(alpha = 0.2f))
            DrawerItem("Buscador Global", imageName = "questionmark") { onNavigate(Screen.GlobalSearch) }
            DrawerItem("Objetos", imageName = "collectibles_001") { onNavigate(Screen.Objetos) }
            DrawerItem("Jefes", imageName = "jefe_1") { onNavigate(Screen.Jefes) }
            DrawerItem("Pisos", imageName = "piso_1") { onNavigate(Screen.Pisos) }
            DrawerItem("Salas de Items", imageName = "chapter_1") { onNavigate(Screen.Salas) }
            DrawerItem("Personajes", imageName = "Character_1_icon") { onNavigate(Screen.Personajes) }
            DrawerItem("Consumibles", imageName = "collectibles_2001") { onNavigate(Screen.SubMenuConsumibles) }
            DrawerItem("Transformaciones", imageName = "Transformation_1") { onNavigate(Screen.Transformaciones) }
            DrawerItem("Logros", imageName = "trophy") { onNavigate(Screen.Logros) }
            DrawerItem("Maldiciones", imageName = "maldicion_1") { onNavigate(Screen.Maldiciones) }
        }
    }
}

@Composable
fun DrawerItem(text: String, icon: ImageVector? = null, imageName: String? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (imageName != null) {
            DynamicImage(imageName, Modifier.size(24.dp))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colors.secondary)
        }
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

@Composable
fun LoadingScreen(status: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(color = MaterialTheme.colors.secondary); Spacer(Modifier.height(16.dp)); Text(status, color = Color.White) }
    }
}

@Composable
fun MenuScreen(onNavigate: (Screen) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        DynamicImage("dice-logo", Modifier.size(160.dp).padding(bottom = 24.dp))
        MenuButton("Mi Perfil", Icons.Default.Person) { onNavigate(Screen.Perfil) }
        MenuButton("Buscador Global", imageName = "questionmark") { onNavigate(Screen.GlobalSearch) }
        MenuButton("Objetos", imageName = "collectibles_001") { onNavigate(Screen.Objetos) }
        MenuButton("Jefes", imageName = "jefe_1") { onNavigate(Screen.Jefes) }
        MenuButton("Pisos", imageName = "piso_1") { onNavigate(Screen.Pisos) }
        MenuButton("Salas de Items", imageName = "chapter_1") { onNavigate(Screen.Salas) }
        MenuButton("Personajes", imageName = "Character_1_icon") { onNavigate(Screen.Personajes) }
        MenuButton("Consumibles", imageName = "collectibles_2001") { onNavigate(Screen.SubMenuConsumibles) }
        MenuButton("Transformaciones", imageName = "Transformation_1") { onNavigate(Screen.Transformaciones) }
        MenuButton("Logros", imageName = "trophy") { onNavigate(Screen.Logros) }
        MenuButton("Maldiciones", imageName = "maldicion_1") { onNavigate(Screen.Maldiciones) }
    }
}
@Composable
fun BentoCategoryButton(text: String, imageName: String, onClick: () -> Unit) {
    val orangeColor = Color(0xFFFF4500)
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, orangeColor),
        backgroundColor = Color.Transparent,
        elevation = 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            DynamicImage(imageName, Modifier.size(80.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
fun SubMenuConsumiblesScreen(tipos: List<String>, onTipoClick: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Consumibles",
            style = MaterialTheme.typography.h4,
            color = MaterialTheme.colors.secondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tipos) { tipo ->
                val representativeImage = when (tipo) {
                    "Trinket" -> "collectibles_2026"
                    "Carta" -> "Pickup_80"
                    "Píldora", "Pildora" -> "pill_12"
                    "Pickup", "Consumible" -> "consumible_369"
                    else -> "dice-logo"
                }

                BentoCategoryButton(
                    text = tipo,
                    imageName = representativeImage,
                    onClick = { onTipoClick(tipo) }
                )
            }
        }
    }
}

@Composable
fun MenuButton(text: String, icon: ImageVector? = null, imageName: String? = null, onClick: () -> Unit) {
    val orangeColor = Color(0xFFFF4500)
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        border = BorderStroke(2.dp, orangeColor),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent)
    ) {
        if (imageName != null) {
            DynamicImage(imageName, Modifier.size(24.dp))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, tint = orangeColor)
        }
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun <T> ListScreen(itemList: List<T>, getTitle: (T) -> String, getSubtitle: (T) -> String, getImageName: (T) -> String?, iconSize: Int, getImageFilter: (T) -> ColorFilter? = { null }, trailingContent: @Composable ((T) -> Unit)? = null, onItemClick: (T) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(itemList) { item ->
            Row(Modifier.fillMaxWidth().clickable { onItemClick(item) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                val imageName = getImageName(item)
                if (imageName != null) DynamicImage(name = imageName, modifier = Modifier.size(iconSize.dp).padding(end = 16.dp), colorFilter = getImageFilter(item))
                Column(Modifier.weight(1f)) {
                    Text(getTitle(item), style = MaterialTheme.typography.h6, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DescriptionText(getSubtitle(item), style = MaterialTheme.typography.body2, color = Color.Gray)
                        if (item is RemoteObjeto) { Spacer(Modifier.width(8.dp)); QualityStars(item.calidad, 14) }
                    }
                }
                if (trailingContent != null) trailingContent(item)
            }
            Divider(color = Color.DarkGray)
        }
    }
}

@Composable
fun DetailScreen(title: String, label1: String, value1: String, label2: String, value2: String, imageName: String?) {
    Column(Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colors.background).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (imageName != null) DynamicImage(imageName, Modifier.size(80.dp).padding(end = 16.dp))
            Text(title, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
        Text(text = "$label1:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1); DescriptionText(text = value1, color = Color.White, style = MaterialTheme.typography.body1)
        Spacer(Modifier.height(16.dp))
        Text(text = "$label2:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1); DescriptionText(text = value2, color = Color.White, style = MaterialTheme.typography.body1)
    }
}

@Composable
fun DetailConsumibleScreen(
    consumible: RemoteConsumible,
    prevConsumible: RemoteConsumible? = null,
    nextConsumible: RemoteConsumible? = null,
    onNavigate: (RemoteConsumible) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colors.background).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val imageName = getImagePath(consumible.id, consumible.tipo, consumible.uid)
            if (imageName != null) {
                DynamicImage(imageName, Modifier.size(80.dp).padding(end = 16.dp))
            }
            Text(consumible.nombre, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(24.dp))
        Text(text = "Tipo:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        Text(text = consumible.tipo, color = Color.White, style = MaterialTheme.typography.body1)
        Spacer(Modifier.height(16.dp))
        Text(text = "Descripción:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        DescriptionText(text = consumible.descripcion, color = Color.White, style = MaterialTheme.typography.body1)

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (prevConsumible != null) {
                Row(Modifier.clickable { onNavigate(prevConsumible) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colors.secondary)
                    Spacer(Modifier.width(8.dp))
                    DynamicImage(getImagePath(prevConsumible.id, prevConsumible.tipo, prevConsumible.uid) ?: "", Modifier.size(40.dp))
                }
            } else Spacer(Modifier.width(1.dp))

            // Se muestra el ID o UID tenue como en el resto
            Text(text = "UID: ${consumible.uid}", color = Color.Gray.copy(alpha = 0.5f), style = MaterialTheme.typography.caption)

            if (nextConsumible != null) {
                Row(Modifier.clickable { onNavigate(nextConsumible) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    DynamicImage(getImagePath(nextConsumible.id, nextConsumible.tipo, nextConsumible.uid) ?: "", Modifier.size(40.dp))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colors.secondary)
                }
            } else Spacer(Modifier.width(1.dp))
        }
    }
}
@Composable
fun DetailJefeScreen(
    jefe: RemoteJefe,
    pisos: List<RemotePiso>,
    prevJefe: RemoteJefe?,
    nextJefe: RemoteJefe?,
    onPisoClick: (RemotePiso) -> Unit,
    onNavigate: (RemoteJefe) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colors.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            DynamicImage(getImagePath(jefe.id, "Jefe") ?: "", Modifier.size(180.dp))
        }

        Spacer(Modifier.height(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(jefe.nombre, style = MaterialTheme.typography.h3, color = MaterialTheme.colors.secondary, fontWeight = FontWeight.Bold)
            Text("Vida Base: ${jefe.vida_base}", color = Color.Gray, style = MaterialTheme.typography.subtitle1)
        }

        Spacer(Modifier.height(32.dp))
        Text("Descripción:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        DescriptionText(jefe.descripcion)

        Spacer(Modifier.height(16.dp))
        Text("Comportamiento:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        DescriptionText(jefe.comportamiento)

        if (pisos.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Aparece en los subpisos:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                pisos.forEach { piso ->
                    Row(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { onPisoClick(piso) }
                            .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DynamicImage(getImagePath(piso.id, "Chapter") ?: "", Modifier.size(32.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(piso.nombre, color = Color.White, style = MaterialTheme.typography.body2)
                    }
                }
            }
        }

        jefe.notas?.let {
            Spacer(Modifier.height(24.dp))
            Text("Notas:", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
            DescriptionText(it)
        }

        Spacer(Modifier.height(40.dp))
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (prevJefe != null) {
                Row(Modifier.clickable { onNavigate(prevJefe) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colors.secondary)
                    Spacer(Modifier.width(8.dp))
                    DynamicImage(getImagePath(prevJefe.id, "Jefe") ?: "", Modifier.size(48.dp))
                }
            } else Spacer(Modifier.width(1.dp))

            Text(text = "ID: ${jefe.id}", color = Color.Gray.copy(alpha = 0.5f), style = MaterialTheme.typography.caption)

            if (nextJefe != null) {
                Row(Modifier.clickable { onNavigate(nextJefe) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    DynamicImage(getImagePath(nextJefe.id, "Jefe") ?: "", Modifier.size(48.dp))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colors.secondary)
                }
            } else Spacer(Modifier.width(1.dp))
        }
    }
}

@Composable
fun DetailPisoScreen(piso: RemotePiso, jefes: List<RemoteJefe>, onJefeClick: (RemoteJefe) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .verticalScroll(rememberScrollState())
    ) {
        DynamicImage(
            name = getImagePath(piso.id, "Piso") ?: "",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                DynamicImage(
                    name = getImagePath(piso.id, "Chapter") ?: "",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    text = piso.nombre,
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Descripción:",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(Modifier.height(8.dp))
            DescriptionText(piso.descripcion)

            if (jefes.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "Jefes de este Piso:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.h6
                )
                Spacer(Modifier.height(12.dp))
                jefes.forEach { jefe ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onJefeClick(jefe) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DynamicImage(
                            name = getImagePath(jefe.id, "Jefe") ?: "",
                            modifier = Modifier
                                .size(48.dp)
                                .padding(end = 12.dp)
                        )
                        Text(text = jefe.nombre, color = Color.White, style = MaterialTheme.typography.body1)
                    }
                    Divider(color = Color.DarkGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun DynamicImage(name: String, modifier: Modifier, colorFilter: ColorFilter? = null) {
    val imageBitmap = produceState<ImageBitmap?>(null, name) {
        try {
            val bytes = Res.readBytes("drawable/$name.png")
            value = bytes.decodeToImageBitmap()
        } catch (e: Exception) { value = null }
    }.value
    if (imageBitmap != null) Image(bitmap = imageBitmap, contentDescription = null, modifier = modifier, colorFilter = colorFilter)
    else Box(modifier = modifier.background(Color.DarkGray, shape = RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Gray) }
}

@Composable
fun DescriptionText(text: String, style: TextStyle = MaterialTheme.typography.body1, color: Color = Color.White) {
    val iconMapping = mapOf(
        "Tears" to "lagrimas_icono",
        "Damage" to "dano_icono",
        "Shotspeed" to "velocidad-disparo_icono",
        "Range" to "rango_icono",
        "Speed" to "velocidad_icono",
        "Luck" to "suerte_icono",
        "Heart" to "vida_icono"
    )

    val regex = Regex("\\{\\{([^}]*)\\}\\}")
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        regex.findAll(text).forEach { matchResult ->
            append(text.substring(lastIndex, matchResult.range.first))
            val key = matchResult.groupValues[1]
            if (iconMapping.containsKey(key)) {
                appendInlineContent(key, "[icon]")
            }
            lastIndex = matchResult.range.last + 1
        }
        append(text.substring(lastIndex))
    }

    val inlineContent = iconMapping.mapValues { entry ->
        InlineTextContent(
            Placeholder(
                width = 18.sp,
                height = 18.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            DynamicImage(entry.value, Modifier.fillMaxSize())
        }
    }

    Text(
        text = annotatedString,
        inlineContent = inlineContent,
        style = style,
        color = color
    )
}
