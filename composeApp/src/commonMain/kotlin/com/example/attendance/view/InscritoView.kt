package com.example.attendance.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.attendance.model.EstudianteModel
import com.example.attendance.model.InscritoModel
import com.example.attendance.view.theme.AppPrimaryButton
import com.example.attendance.view.theme.AppSecondaryButton
import com.example.attendance.view.theme.AppTextField
import com.example.attendance.view.theme.AttendanceThemeTokens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface IInscritoView {
    val inscritos: StateFlow<List<EstudianteModel>>
    val mostrarModal: StateFlow<Boolean>
    val mostrarEliminarModal: StateFlow<Boolean>
    val estudianteSeleccionado: StateFlow<EstudianteModel?>
    val carnet: StateFlow<String>
    val nombre: StateFlow<String>
    val apellido: StateFlow<String>

    fun setInscritos(inscritos: StateFlow<List<EstudianteModel>>)
    fun onMostrarModal(valor: Boolean)
    fun onMostrarEliminarModal(valor: Boolean)
    fun onEstudianteSeleccionado(estudiante: EstudianteModel?)
    fun onCarnetChange(valor: String)
    fun onNombreChange(valor: String)
    fun onApellidoChange(valor: String)
    fun limpiarFormulario()
}

class InscritoViewData : IInscritoView {
    private var _inscritos: StateFlow<List<EstudianteModel>> = MutableStateFlow(emptyList())
    override val inscritos: StateFlow<List<EstudianteModel>> get() = _inscritos

    private val _mostrarModal = MutableStateFlow(false)
    override val mostrarModal: StateFlow<Boolean> = _mostrarModal.asStateFlow()

    private val _mostrarEliminarModal = MutableStateFlow(false)
    override val mostrarEliminarModal: StateFlow<Boolean> = _mostrarEliminarModal.asStateFlow()

    private val _estudianteSeleccionado = MutableStateFlow<EstudianteModel?>(null)
    override val estudianteSeleccionado: StateFlow<EstudianteModel?> = _estudianteSeleccionado.asStateFlow()

    private val _carnet = MutableStateFlow("")
    override val carnet: StateFlow<String> = _carnet.asStateFlow()

    private val _nombre = MutableStateFlow("")
    override val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _apellido = MutableStateFlow("")
    override val apellido: StateFlow<String> = _apellido.asStateFlow()

    override fun setInscritos(inscritos: StateFlow<List<EstudianteModel>>) {
        _inscritos = inscritos
    }

    override fun onMostrarModal(valor: Boolean) {
        _mostrarModal.value = valor
    }

    override fun onMostrarEliminarModal(valor: Boolean) {
        _mostrarEliminarModal.value = valor
    }

    override fun onEstudianteSeleccionado(estudiante: EstudianteModel?) {
        _estudianteSeleccionado.value = estudiante
    }

    override fun onCarnetChange(valor: String) {
        _carnet.value = valor.filter(Char::isDigit)
    }

    override fun onNombreChange(valor: String) {
        _nombre.value = valor
    }

    override fun onApellidoChange(valor: String) {
        _apellido.value = valor
    }

    override fun limpiarFormulario() {
        _carnet.value = ""
        _nombre.value = ""
        _apellido.value = ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InscritoView(
    materiaNombre: String,
    inscritos: StateFlow<List<EstudianteModel>>,
    mostrarModal: StateFlow<Boolean>,
    mostrarEliminarModal: StateFlow<Boolean>,
    estudianteSeleccionado: StateFlow<EstudianteModel?>,
    carnet: StateFlow<String>,
    nombre: StateFlow<String>,
    apellido: StateFlow<String>,
    onVolver: () -> Unit,
    onAgregar: () -> Boolean,
    onEliminar: () -> Boolean,
    onMostrarModal: (Boolean) -> Unit,
    onMostrarEliminarModal: (Boolean) -> Unit,
    onEstudianteSeleccionado: (EstudianteModel?) -> Unit,
    onCarnetChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onApellidoChange: (String) -> Unit,
    limpiarFormulario: () -> Unit
) {
    val metrics = AttendanceThemeTokens.metrics
    val sizes = AttendanceThemeTokens.textSizes
    val inscritosValue by inscritos.collectAsState()
    val mostrarModalValue by mostrarModal.collectAsState()
    val mostrarEliminarModalValue by mostrarEliminarModal.collectAsState()
    val estudianteSeleccionadoValue by estudianteSeleccionado.collectAsState()
    val carnetValue by carnet.collectAsState()
    val nombreValue by nombre.collectAsState()
    val apellidoValue by apellido.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.33f),
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-88).dp, y = (-66).dp)
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        )

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Inscritos - $materiaNombre", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "Gestion de estudiantes",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onVolver) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atras"
                            )
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
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                val contentModifier = Modifier.fillMaxWidth().widthIn(max = 760.dp)

                Column(
                    modifier = contentModifier
                        .fillMaxSize()
                        .padding(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        AppSecondaryButton(text = "Agregar", onClick = { onMostrarModal(true) }, modifier = Modifier.weight(1f))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                        ),
                        border = BorderStroke(metrics.thinBorder, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "${inscritosValue.size} estudiantes inscritos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Puedes agregar estudiantes manualmente",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Text(
                        text = "Estudiantes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (inscritosValue.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                            border = BorderStroke(metrics.thinBorder, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f))
                        ) {
                            Text(
                                text = "No hay estudiantes inscritos",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(20.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(inscritosValue) { estudiante ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onEstudianteSeleccionado(estudiante)
                                            onMostrarEliminarModal(true)
                                        },
                                    shape = RoundedCornerShape(metrics.cardRadius),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                                    border = BorderStroke(metrics.thinBorder, MaterialTheme.colorScheme.outline.copy(alpha = 0.36f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Person,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(7.dp)
                                                )
                                            }
                                            Text(
                                                "${estudiante.nombre} ${estudiante.apellido}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontSize = sizes.cardSubtitle),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    onEstudianteSeleccionado(estudiante)
                                                    onMostrarEliminarModal(true)
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Eliminar estudiante",
                                                    tint = MaterialTheme.colorScheme.error,
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))

                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    "CI ${estudiante.carnetIdentidad}",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = sizes.helperText)
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

    if (mostrarModalValue) {
        ModalBottomSheet(
            onDismissRequest = { onMostrarModal(false) },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = metrics.modalHorizontalPadding, vertical = metrics.modalVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Agregar estudiante", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Registra estudiante individualmente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AppTextField(
                    value = carnetValue,
                    onValueChange = onCarnetChange,
                    label = "Carnet",
                    leadingIcon = Icons.Filled.Badge,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextField(
                    value = nombreValue,
                    onValueChange = onNombreChange,
                    label = "Nombre",
                    leadingIcon = Icons.Filled.Person,
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextField(
                    value = apellidoValue,
                    onValueChange = onApellidoChange,
                    label = "Apellido",
                    leadingIcon = Icons.Filled.Person,
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppSecondaryButton(
                        text = "Cancelar",
                        onClick = { onMostrarModal(false) },
                        modifier = Modifier.weight(1f)
                    )
                    AppPrimaryButton(
                        text = "Guardar",
                        onClick = {
                            val agregado = onAgregar()
                            if (agregado) {
                                limpiarFormulario()
                                onMostrarModal(false)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Estudiante agregado correctamente")
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("No se pudo agregar el estudiante")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    if (mostrarEliminarModalValue && estudianteSeleccionadoValue != null) {
        ModalBottomSheet(
            onDismissRequest = {
                onMostrarEliminarModal(false)
                onEstudianteSeleccionado(null)
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = metrics.modalHorizontalPadding, vertical = metrics.modalVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Eliminar inscripción", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "¿Realmente quieres eliminar a ${estudianteSeleccionadoValue?.nombre} ${estudianteSeleccionadoValue?.apellido}?",
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
                            onMostrarEliminarModal(false)
                            onEstudianteSeleccionado(null)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    AppPrimaryButton(
                        text = "Eliminar",
                        onClick = {
                            val eliminado = onEliminar()
                            if (eliminado) {
                                onMostrarEliminarModal(false)
                                onEstudianteSeleccionado(null)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Inscripción eliminada")
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("No se pudo eliminar")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
