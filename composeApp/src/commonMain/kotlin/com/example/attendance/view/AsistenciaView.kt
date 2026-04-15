package com.example.attendance.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.attendance.model.AsistenciaModel
import com.example.attendance.view.theme.AppPrimaryButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.example.attendance.view.theme.AppSecondaryButton
import com.example.attendance.view.theme.AttendanceThemeTokens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import qrcode.QRCode

interface IAsistenciaView {
    val asistencias: StateFlow<List<AsistenciaModel>>
    val mostrarQr: StateFlow<Boolean>
    val qrMatriz: StateFlow<List<List<Boolean>>>
    val mostrarEliminarModal: StateFlow<Boolean>
    val asistenciaAEliminar: StateFlow<AsistenciaModel?>

    fun setAsistencias(asistencias: List<AsistenciaModel>)
    fun onMostrarQr(valor: Boolean)
    fun onQrMatriz(matriz: List<List<Boolean>>)
    fun onMostrarEliminarModal(valor: Boolean)
    fun onAsistenciaAEliminar(asistencia: AsistenciaModel?)
}

class AsistenciaViewData : IAsistenciaView {
    private val _asistencias = MutableStateFlow<List<AsistenciaModel>>(emptyList())
    override val asistencias: StateFlow<List<AsistenciaModel>> = _asistencias.asStateFlow()

    private val _mostrarQr = MutableStateFlow(false)
    override val mostrarQr: StateFlow<Boolean> = _mostrarQr.asStateFlow()

    private val _qrMatriz = MutableStateFlow<List<List<Boolean>>>(emptyList())
    override val qrMatriz: StateFlow<List<List<Boolean>>> = _qrMatriz.asStateFlow()

    private val _mostrarEliminarModal = MutableStateFlow(false)
    override val mostrarEliminarModal: StateFlow<Boolean> = _mostrarEliminarModal.asStateFlow()

    private val _asistenciaAEliminar = MutableStateFlow<AsistenciaModel?>(null)
    override val asistenciaAEliminar: StateFlow<AsistenciaModel?> = _asistenciaAEliminar.asStateFlow()

    override fun setAsistencias(asistencias: List<AsistenciaModel>) {
        _asistencias.value = asistencias
    }

    override fun onMostrarQr(valor: Boolean) {
        _mostrarQr.value = valor
    }

    override fun onQrMatriz(matriz: List<List<Boolean>>) {
        _qrMatriz.value = matriz
    }

    override fun onMostrarEliminarModal(valor: Boolean) {
        _mostrarEliminarModal.value = valor
    }

    override fun onAsistenciaAEliminar(asistencia: AsistenciaModel?) {
        _asistenciaAEliminar.value = asistencia
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaView(
    view: IAsistenciaView,
    materiaId: Long,
    materiaNombre: String,
    materiaQrDetalle: String,
    onVolver: () -> Unit,
    onIrInscritos: () -> Unit,
    onIrCrearAsistencia: () -> Unit,
    onAbrirDetalle: (Long) -> Unit,
    onGenerarQr: () -> String?,
    onEliminar: () -> Boolean,
) {
    val metrics = AttendanceThemeTokens.metrics
    val sizes = AttendanceThemeTokens.textSizes
    val asistencias by view.asistencias.collectAsState()
    val mostrarQr by view.mostrarQr.collectAsState()
    val qrMatriz by view.qrMatriz.collectAsState()
    val mostrarEliminarModal by view.mostrarEliminarModal.collectAsState()
    val asistenciaAEliminar by view.asistenciaAEliminar.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-88).dp, y = (-70).dp)
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 92.dp)
                .size(228.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
        )

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(materiaNombre, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "Sesion de asistencia",
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
                    actions = {
                        IconButton(onClick = {
                            val payload = onGenerarQr() ?: return@IconButton
                            view.onQrMatriz(generarQrMatriz(payload))
                            view.onMostrarQr(true)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.QrCode2,
                                contentDescription = "Generar QR"
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        AppSecondaryButton(text = "Inscritos", onClick = onIrInscritos, modifier = Modifier.weight(1f))
                        AppPrimaryButton(text = "Crear asistencia", onClick = onIrCrearAsistencia, modifier = Modifier.weight(1f))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.74f)
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
                                    text = "${asistencias.size} sesiones registradas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Genera QR para una sesion en vivo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.TaskAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Text(
                        text = "Asistencias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (asistencias.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                            border = BorderStroke(metrics.thinBorder, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TaskAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Sin asistencias registradas",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(asistencias) { asistencia ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAbrirDetalle(asistencia.id) },
                                    shape = RoundedCornerShape(metrics.cardRadius),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                                    border = BorderStroke(metrics.thinBorder, MaterialTheme.colorScheme.outline.copy(alpha = 0.36f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                                    Icons.AutoMirrored.Filled.EventNote,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Asistencia #$materiaId-${asistencia.id}",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = sizes.cardTitle),
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = asistencia.fecha,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = sizes.cardSubtitle),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    view.onAsistenciaAEliminar(asistencia)
                                                    view.onMostrarEliminarModal(true)
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Eliminar asistencia",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = "Tap para ver detalle",
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
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

    if (mostrarQr) {
        ModalBottomSheet(
            onDismissRequest = { view.onMostrarQr(false) },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Codigo QR de la materia",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                if (qrMatriz.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(modifier = Modifier.padding(10.dp)) {
                                Canvas(modifier = Modifier.size(250.dp)) {
                                    val sizePx = size.minDimension
                                    val count = qrMatriz.size
                                    if (count > 0) {
                                        val cell = sizePx / count.toFloat()
                                        qrMatriz.forEachIndexed { r, row ->
                                            row.forEachIndexed { c, dark ->
                                                if (dark) {
                                                    drawRect(
                                                        color = Color.Black,
                                                        topLeft = Offset(c * cell, r * cell),
                                                        size = Size(cell, cell)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text("No se pudo generar el QR", textAlign = TextAlign.Center)
                }

                Text(
                    text = materiaQrDetalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (mostrarEliminarModal && asistenciaAEliminar != null) {
        ModalBottomSheet(
            onDismissRequest = {
                view.onMostrarEliminarModal(false)
                view.onAsistenciaAEliminar(null)
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Eliminar asistencia",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "¿Realmente quieres eliminar la asistencia #$materiaId-${asistenciaAEliminar?.id}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppSecondaryButton(
                        text = "Cancelar",
                        onClick = {
                            view.onMostrarEliminarModal(false)
                            view.onAsistenciaAEliminar(null)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AppPrimaryButton(
                        text = "Eliminar",
                        onClick = {
                            val eliminado = onEliminar()
                            if (eliminado) {
                                view.onMostrarEliminarModal(false)
                                view.onAsistenciaAEliminar(null)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Asistencia eliminada")
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("No se pudo eliminar")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun generarQrMatriz(payload: String): List<List<Boolean>> {
    return runCatching {
        QRCode(payload).rawData.map { row -> row.map { it.dark } }
    }.getOrElse { emptyList() }
}
