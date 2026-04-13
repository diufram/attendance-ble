package com.example.attendance.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.attendance.model.MateriaModel
import com.example.attendance.view.theme.AppPrimaryButton
import com.example.attendance.view.theme.AppSecondaryButton
import com.example.attendance.view.theme.AppTextField
import com.example.attendance.view.theme.AttendanceThemeTokens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface IMateriaDocenteView {
    val materias: StateFlow<List<MateriaModel>>
    val sigla: StateFlow<String>
    val nombre: StateFlow<String>
    val grupo: StateFlow<String>
    val periodo: StateFlow<String>
    val mostrarModalMateria: StateFlow<Boolean>
    val mostrarModalEliminarMateria: StateFlow<Boolean>
    val materiaSeleccionadaAccion: StateFlow<MateriaModel?>
    val errorMensaje: StateFlow<String?>

    fun setMaterias(materias: List<MateriaModel>)
    fun onSiglaChange(valor: String)
    fun onNombreChange(valor: String)
    fun onGrupoChange(valor: String)
    fun onPeriodoChange(valor: String)
    fun onAbrirModalCrear()
    fun onCerrarModalCrear()
    fun onAbrirModalEditar(materia: MateriaModel)
    fun onCerrarModalEditar()
    fun onAbrirModalEliminar(materia: MateriaModel)
    fun onCerrarModalEliminar()
    fun setErrorMensaje(mensaje: String?)
    fun onCerrarError()
    fun limpiarFormulario()
}

class MateriaDocenteViewData : IMateriaDocenteView {
    private val _materias = MutableStateFlow<List<MateriaModel>>(emptyList())
    override val materias: StateFlow<List<MateriaModel>> = _materias.asStateFlow()

    private val _sigla = MutableStateFlow("")
    override val sigla: StateFlow<String> = _sigla.asStateFlow()

    private val _nombre = MutableStateFlow("")
    override val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _grupo = MutableStateFlow("")
    override val grupo: StateFlow<String> = _grupo.asStateFlow()

    private val _periodo = MutableStateFlow("")
    override val periodo: StateFlow<String> = _periodo.asStateFlow()

    private val _mostrarModalMateria = MutableStateFlow(false)
    override val mostrarModalMateria: StateFlow<Boolean> = _mostrarModalMateria.asStateFlow()

    private val _mostrarModalEliminarMateria = MutableStateFlow(false)
    override val mostrarModalEliminarMateria: StateFlow<Boolean> = _mostrarModalEliminarMateria.asStateFlow()

    private val _materiaSeleccionadaAccion = MutableStateFlow<MateriaModel?>(null)
    override val materiaSeleccionadaAccion: StateFlow<MateriaModel?> = _materiaSeleccionadaAccion.asStateFlow()

    private val _errorMensaje = MutableStateFlow<String?>(null)
    override val errorMensaje: StateFlow<String?> = _errorMensaje.asStateFlow()

    override fun setMaterias(materias: List<MateriaModel>) {
        _materias.value = materias
    }

    override fun onSiglaChange(valor: String) {
        _sigla.value = valor
    }

    override fun onNombreChange(valor: String) {
        _nombre.value = valor
    }

    override fun onGrupoChange(valor: String) {
        _grupo.value = valor
    }

    override fun onPeriodoChange(valor: String) {
        _periodo.value = valor
    }

    override fun onAbrirModalCrear() {
        _errorMensaje.value = null
        limpiarFormulario()
        _materiaSeleccionadaAccion.value = null
        _mostrarModalMateria.value = true
    }

    override fun onCerrarModalCrear() {
        _mostrarModalMateria.value = false
        limpiarFormulario()
    }

    override fun onAbrirModalEditar(materia: MateriaModel) {
        _errorMensaje.value = null
        _materiaSeleccionadaAccion.value = materia
        _sigla.value = materia.sigla
        _nombre.value = materia.nombre
        _grupo.value = materia.grupo
        _periodo.value = materia.periodo
        _mostrarModalMateria.value = true
    }

    override fun onCerrarModalEditar() {
        _mostrarModalMateria.value = false
        _materiaSeleccionadaAccion.value = null
        limpiarFormulario()
    }

    override fun onAbrirModalEliminar(materia: MateriaModel) {
        _errorMensaje.value = null
        _materiaSeleccionadaAccion.value = materia
        _mostrarModalEliminarMateria.value = true
    }

    override fun onCerrarModalEliminar() {
        _mostrarModalEliminarMateria.value = false
        _materiaSeleccionadaAccion.value = null
    }

    override fun setErrorMensaje(mensaje: String?) {
        _errorMensaje.value = mensaje
    }

    override fun onCerrarError() {
        _errorMensaje.value = null
    }

    override fun limpiarFormulario() {
        _sigla.value = ""
        _nombre.value = ""
        _grupo.value = ""
        _periodo.value = ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaDocenteView(
    view: IMateriaDocenteView,
    onCerrarSesion: () -> Unit,
    irAsistenciaView: (Long) -> Unit,
    onCrear: () -> Unit,
    onGuardar: () -> Unit,
    onEliminar: () -> Unit,
) {
    val sigla by view.sigla.collectAsState()
    val nombre by view.nombre.collectAsState()
    val grupo by view.grupo.collectAsState()
    val periodo by view.periodo.collectAsState()
    val mostrarModalMateria by view.mostrarModalMateria.collectAsState()
    val mostrarModalEliminarMateria by view.mostrarModalEliminarMateria.collectAsState()
    val materiaSeleccionadaAccion by view.materiaSeleccionadaAccion.collectAsState()
    val errorMensaje by view.errorMensaje.collectAsState()
    val metrics = AttendanceThemeTokens.metrics
    val sizes = AttendanceThemeTokens.textSizes
    val materias by view.materias.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-94).dp, y = (-70).dp)
                .size(210.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.11f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 82.dp, y = 96.dp)
                .size(230.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f))
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Materias", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "Panel docente",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = onCerrarSesion) {
                            Icon(Icons.Filled.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salir")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },
            containerColor = Color.Transparent,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = view::onAbrirModalCrear,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nueva materia") },
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 5.dp)
                )
            }

        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                val contentModifier = Modifier.fillMaxWidth().widthIn(max = 760.dp)

                if (materias.isEmpty()) {
                    Card(
                        modifier = contentModifier.padding(top = 22.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        border = BorderStroke(metrics.thinBorder, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    modifier = Modifier.padding(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                "No tienes materias aun",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = sizes.cardTitle),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Pulsa Nueva materia para crear tu primera clase.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = sizes.cardSubtitle),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = contentModifier
                            .fillMaxSize()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                            ),
                            border = BorderStroke(
                                metrics.thinBorder,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Tienes ${materias.size} materias activas",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Selecciona una para ver sus asistencias",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(materias) { materia ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { irAsistenciaView(materia.id) },
                                    shape = RoundedCornerShape(metrics.cardRadius),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                    ),
                                    border = BorderStroke(metrics.thinBorder, MaterialTheme.colorScheme.outline.copy(alpha = 0.36f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.MenuBook,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${materia.sigla} - ${materia.grupo}",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = sizes.cardTitle),
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Text(
                                                    text = materia.nombre,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = sizes.cardSubtitle),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                IconButton(
                                                    onClick = {
                                                        view.onAbrirModalEditar(materia)
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Edit,
                                                        contentDescription = "Editar materia",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        view.onAbrirModalEliminar(materia)
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = "Eliminar materia",
                                                        tint = MaterialTheme.colorScheme.error,
                                                    )
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                modifier = Modifier.weight(1f),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Filled.CalendarMonth,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = materia.periodo,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = sizes.helperText),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Surface(
                                                modifier = Modifier.weight(1f),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Person,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "Docente",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = sizes.helperText),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            }
        }
    }

    val esModalCrear = mostrarModalMateria && materiaSeleccionadaAccion == null
    val esModalEditar = mostrarModalMateria && materiaSeleccionadaAccion != null
    if (esModalCrear || esModalEditar) {
        ModalBottomSheet(
            onDismissRequest = {
                if (esModalEditar) view.onCerrarModalEditar() else view.onCerrarModalCrear()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = metrics.modalHorizontalPadding, vertical = metrics.modalVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(if (esModalEditar) "Editar materia" else "Nueva Materia", style = MaterialTheme.typography.titleLarge)
                if (esModalCrear) {
                    Text(
                        text = "Completa los datos para habilitar registro de asistencia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AppTextField(
                    value = sigla,
                    onValueChange = view::onSiglaChange,
                    label = if (esModalEditar) "Sigla" else "Sigla (ej: INF-301)",
                    leadingIcon = Icons.AutoMirrored.Filled.MenuBook,
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextField(
                    value = nombre,
                    onValueChange = view::onNombreChange,
                    label = "Nombre",
                    leadingIcon = Icons.Filled.School,
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextField(
                    value = grupo,
                    onValueChange = view::onGrupoChange,
                    label = if (esModalEditar) "Grupo" else "Grupo (ej: A)",
                    leadingIcon = Icons.Filled.Groups,
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextField(
                    value = periodo,
                    onValueChange = view::onPeriodoChange,
                    label = if (esModalEditar) "Periodo" else "Periodo (ej: 1-2026)",
                    leadingIcon = Icons.Filled.CalendarMonth,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppSecondaryButton(
                        text = "Cancelar",
                        onClick = {
                            if (esModalEditar) view.onCerrarModalEditar() else view.onCerrarModalCrear()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AppPrimaryButton(
                        text = if (esModalEditar) "Guardar" else "Crear",
                        onClick = {
                            if (esModalEditar) onGuardar() else onCrear()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    if (mostrarModalEliminarMateria && materiaSeleccionadaAccion != null) {
        ModalBottomSheet(
            onDismissRequest = {
                view.onCerrarModalEliminar()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = metrics.modalHorizontalPadding, vertical = metrics.modalVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Eliminar materia", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "¿Realmente quieres eliminar ${materiaSeleccionadaAccion?.sigla} - ${materiaSeleccionadaAccion?.grupo}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppSecondaryButton(
                        text = "Cancelar",
                        onClick = {
                            view.onCerrarModalEliminar()
                        },
                        modifier = Modifier.weight(1f),
                    )
                    AppPrimaryButton(
                        text = "Eliminar",
                        onClick = {
                            onEliminar()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    val errorActual = errorMensaje
    if (errorActual != null) {
        AlertDialog(
            onDismissRequest = view::onCerrarError,
            confirmButton = {
                TextButton(onClick = view::onCerrarError) {
                    Text("Aceptar")
                }
            },
            title = { Text("Atención") },
            text = { Text(errorActual) },
        )
    }
}
