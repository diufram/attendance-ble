package com.example.attendance.view

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.attendance.ble.rememberRequestBlePermissions
import com.example.attendance.controller.MateriaEstudianteController
import com.example.attendance.model.MateriaModel
import com.example.attendance.view.theme.AppPrimaryButton
import com.example.attendance.view.theme.AppSecondaryButton
import com.example.attendance.view.theme.AttendanceThemeTokens
import kotlinx.coroutines.launch

@Composable
expect fun QrScannerView(
    modifier: Modifier = Modifier,
    onQrScanned: (String) -> Unit,
    onError: (String) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaEstudianteView(
    model: MateriaModel,
    bleEstado: String,
    bleActivoMateriaId: Long?,
    bleConfirmacion: MateriaEstudianteController.BleConfirmacionUi?,
    onCerrarSesion: () -> Unit,
    onRegistrarMateriaDesderQr: (String) -> String?,
    onMarcarAsistencia: (MateriaModel) -> String?,
    onDetenerMarcadoAsistencia: () -> Unit,
    onCerrarConfirmacionAsistencia: () -> Unit,
) {
    val scannerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val materiaSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var mostrarEscaner by remember { mutableStateOf(false) }
    var mostrarMateriaSheet by remember { mutableStateOf(false) }
    var materiaSeleccionada by remember { mutableStateOf<MateriaModel?>(null) }
    var materiaPendienteBle by remember { mutableStateOf<MateriaModel?>(null) }


    val metrics = AttendanceThemeTokens.metrics
    val sizes = AttendanceThemeTokens.textSizes
    val materias by model.materiasEstudiante.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val solicitarPermisosBle = rememberRequestBlePermissions(
        onGranted = {
            val materia = materiaPendienteBle ?: return@rememberRequestBlePermissions
            val error = onMarcarAsistencia(materia)
            if (error != null) {
                coroutineScope.launch { snackbarHostState.showSnackbar(error) }
            }
        },
        onDenied = { mensaje ->
            coroutineScope.launch { snackbarHostState.showSnackbar(mensaje) }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f),
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-88).dp, y = (-68).dp)
                .size(196.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 78.dp, y = 90.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
        )

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { mostrarEscaner = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = "Escanear QR"
                    )
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Mis materias", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "Panel estudiante",
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

                if (materias.isEmpty()) {
                    Card(
                        modifier = contentModifier.padding(top = 20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
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
                                text = "No estas inscrito en materias",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Escanea el QR o solicita inscripcion a tu docente.",
                                style = MaterialTheme.typography.bodyMedium,
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
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                            ),
                            border = BorderStroke(metrics.thinBorder, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Estas inscrito en ${materias.size} materias",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Pulsa el boton QR para registrar nuevas materias",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.QrCodeScanner,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        if (bleActivoMateriaId != null || bleConfirmacion != null) {
                            val materiaActiva = materias.firstOrNull { it.id == bleActivoMateriaId }
                            val materiaConfirmada = bleConfirmacion
                            val confirmado = bleConfirmacion != null
                            val confirmCardColor = Color(0xFFDFF6E3)
                            val confirmBorderColor = Color(0xFF2E7D32)
                            val confirmContentColor = Color(0xFF1B5E20)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (confirmado) {
                                        confirmCardColor
                                    } else {
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                                    }
                                ),
                                border = BorderStroke(
                                    metrics.thinBorder,
                                    if (confirmado) {
                                        confirmBorderColor.copy(alpha = 0.45f)
                                    } else {
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = if (confirmado) "Asistencia Confirmada" else "Emitiendo asistencia",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (confirmado) {
                                                    confirmContentColor
                                                } else {
                                                    MaterialTheme.colorScheme.onTertiaryContainer
                                                }
                                            )
                                            if (confirmado && materiaConfirmada != null) {
                                                Text(
                                                    text = "de ${materiaConfirmada.nombreMateria} ${materiaConfirmada.sigla} ${materiaConfirmada.grupo}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = confirmContentColor.copy(alpha = 0.9f)
                                                )
                                            } else if (materiaActiva != null) {
                                                Text(
                                                    text = "${materiaActiva.sigla} - ${materiaActiva.grupo}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                                                )
                                            }
                                            Text(
                                                text = if (confirmado) "Confirmacion recibida y guardada" else bleEstado,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (confirmado) {
                                                    confirmContentColor.copy(alpha = 0.78f)
                                                } else {
                                                    MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                                }
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (confirmado) Icons.Filled.TaskAlt else Icons.Filled.BluetoothSearching,
                                                contentDescription = null,
                                                tint = if (confirmado) {
                                                    confirmContentColor
                                                } else {
                                                    MaterialTheme.colorScheme.onTertiaryContainer
                                                },
                                                modifier = Modifier.size(32.dp)
                                            )
                                            IconButton(
                                                onClick = {
                                                    if (confirmado) {
                                                        onCerrarConfirmacionAsistencia()
                                                    } else {
                                                        materiaPendienteBle = null
                                                        onDetenerMarcadoAsistencia()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (confirmado) Icons.Filled.Close else Icons.Filled.Stop,
                                                    contentDescription = if (confirmado) "Cerrar confirmacion" else "Detener emision",
                                                    tint = if (confirmado) {
                                                        confirmContentColor
                                                    } else {
                                                        MaterialTheme.colorScheme.error
                                                    },
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(materias) { materia ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            materiaSeleccionada = materia
                                            mostrarMateriaSheet = true
                                        },
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
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))

                                        Surface(
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
                                                    text = "Periodo ${materia.periodo}",
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

    if (mostrarMateriaSheet) {
        val materiaActiva = materiaSeleccionada
        ModalBottomSheet(
            onDismissRequest = { mostrarMateriaSheet = false },
            sheetState = materiaSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp).size(34.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Marcar asistencia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (materiaActiva != null) {
                    Text(
                        text = materiaActiva.nombre,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${materiaActiva.sigla} - ${materiaActiva.grupo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Al presionar 'Marcar' se iniciara la emision de tu asistencia via Bluetooth. Espera la confirmacion del docente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppSecondaryButton(
                        text = "Cerrar",
                        onClick = {
                            mostrarMateriaSheet = false
                            onDetenerMarcadoAsistencia()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AppPrimaryButton(
                        text = "Marcar",
                        onClick = {
                            if (materiaActiva == null) return@AppPrimaryButton
                            materiaPendienteBle = materiaActiva
                            solicitarPermisosBle()
                            mostrarMateriaSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (mostrarEscaner) {
        ModalBottomSheet(
            onDismissRequest = { mostrarEscaner = false },
            sheetState = scannerSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Escanear QR de materia",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Enfoca el codigo dentro del recuadro",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black, RoundedCornerShape(16.dp))
                    ) {
                        QrScannerView(
                            modifier = Modifier.fillMaxSize(),
                            onQrScanned = { payload ->
                                val error = onRegistrarMateriaDesderQr(payload)
                                if (error == null) {
                                    mostrarEscaner = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Materia vinculada correctamente")
                                    }
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(error)
                                    }
                                }
                            },
                            onError = { error ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(error)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
