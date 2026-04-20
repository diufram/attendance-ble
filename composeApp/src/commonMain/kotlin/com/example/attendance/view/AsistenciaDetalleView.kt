package com.example.attendance.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.attendance.ble.rememberRequestBlePermissions
import com.example.attendance.model.AsistenciaDetalleModel
import com.example.attendance.view.theme.AttendanceThemeTokens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface AsistenciaDetalleView {
    val detalles: StateFlow<List<AsistenciaDetalleModel>>
    val bleActivo: StateFlow<Boolean>
    val bleEstado: StateFlow<String>

    fun setDetalles(detalles: StateFlow<List<AsistenciaDetalleModel>>)
    fun onBleActivo(valor: Boolean)
    fun onBleEstado(valor: String)
}

class AsistenciaDetalleViewData : AsistenciaDetalleView {
    private val _detalles = MutableStateFlow<List<AsistenciaDetalleModel>>(emptyList())
    override val detalles: StateFlow<List<AsistenciaDetalleModel>> = _detalles.asStateFlow()

    private val _bleActivo = MutableStateFlow(false)
    override val bleActivo: StateFlow<Boolean> = _bleActivo.asStateFlow()

    private val _bleEstado = MutableStateFlow("BLE inactivo")
    override val bleEstado: StateFlow<String> = _bleEstado.asStateFlow()

    override fun setDetalles(detalles: StateFlow<List<AsistenciaDetalleModel>>) {
        _detalles.value = detalles.value
    }

    override fun onBleActivo(valor: Boolean) {
        _bleActivo.value = valor
    }

    override fun onBleEstado(valor: String) {
        _bleEstado.value = valor
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaDetalleViewi(
    materiaSigla: String,
    materiaGrupo: String,
    detalles: StateFlow<List<AsistenciaDetalleModel>>,
    bleActivo: StateFlow<Boolean>,
    bleEstado: StateFlow<String>,
    onVolver: () -> Unit,
    onIniciarEscaneo: () -> Unit,
    onDetenerEscaneo: () -> Unit,
    onAlternarEstado: (AsistenciaDetalleModel) -> Unit,
    onGuardar: () -> Unit,
) {
    val detallesValue by detalles.collectAsState()
    val bleActivoValue by bleActivo.collectAsState()
    val bleEstadoValue by bleEstado.collectAsState()

    val metrics = AttendanceThemeTokens.metrics
    val sizes = AttendanceThemeTokens.textSizes
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val solicitarPermisosBle = rememberRequestBlePermissions(
        onGranted = onIniciarEscaneo,
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
                .size(188.dp)
                .offset(x = (-82).dp, y = (-62).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        )

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Control de asistencia", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "Lista de estudiantes",
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
                        IconButton(onClick = onGuardar) {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = "Guardar asistencia",
                                tint = MaterialTheme.colorScheme.primary
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
                    val presentes = detallesValue.count { it.estado == "PRESENTE" }
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
                                    text = "$presentes presentes de ${detallesValue.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Puedes alternar estado por estudiante",
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

                    val bleActiveContainer = Color(0xFFDFF6E3)
                    val bleActiveBorder = Color(0xFF2E7D32)
                    val bleActiveContent = Color(0xFF1B5E20)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (bleActivoValue) {
                                    onDetenerEscaneo()
                                } else {
                                    solicitarPermisosBle()
                                }
                            },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (bleActivoValue) {
                                bleActiveContainer
                            } else {
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                            }
                        ),
                        border = BorderStroke(
                            metrics.thinBorder,
                            if (bleActivoValue) bleActiveBorder.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
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
                                        text = if (bleActivoValue) "Escuchando asistencias" else "Encender Radar",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (bleActivoValue) bleActiveContent else MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "$materiaSigla - $materiaGrupo",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (bleActivoValue) bleActiveContent.copy(alpha = 0.86f)
                                        else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = bleEstadoValue,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (bleActivoValue) bleActiveContent.copy(alpha = 0.76f)
                                        else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = if (bleActivoValue) "Toca para detener" else "Toca para iniciar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (bleActivoValue) bleActiveContent.copy(alpha = 0.8f)
                                        else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.Radar,
                                    contentDescription = null,
                                    tint = if (bleActivoValue) bleActiveContent else MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }

                    if (detallesValue.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                            border = BorderStroke(metrics.thinBorder, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f))
                        ) {
                            Text(
                                text = "No hay registros",
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
                            items(detallesValue) { detalle ->
                                val estadoEsPresente = detalle.estado == "PRESENTE"
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(metrics.cardRadius),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                                    border = BorderStroke(metrics.thinBorder, MaterialTheme.colorScheme.outline.copy(alpha = 0.36f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${detalle.nombreEstudiante} ${detalle.apellidoEstudiante}",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = sizes.cardSubtitle),
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(30.dp)
                                                        .clickable {
                                                            if (!estadoEsPresente) {
                                                                onAlternarEstado(detalle)
                                                            }
                                                        },
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (estadoEsPresente) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surface
                                                ) {
                                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                        Text(
                                                            "Presente",
                                                            style = MaterialTheme.typography.labelLarge,
                                                            color = if (estadoEsPresente) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(30.dp)
                                                        .clickable {
                                                            if (estadoEsPresente) {
                                                                onAlternarEstado(detalle)
                                                            }
                                                        },
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (estadoEsPresente) MaterialTheme.colorScheme.surface else Color(0xFFC62828)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                        Text(
                                                            "Falta",
                                                            style = MaterialTheme.typography.labelLarge,
                                                            color = if (estadoEsPresente) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
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
    }
}
