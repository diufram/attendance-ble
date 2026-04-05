package com.example.attendance.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendance.model.DetalleAsistencia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaDetalleView(
    detalles: List<DetalleAsistencia>,
    onBack: () -> Unit,
    onToggleEstado: (Int, String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Control de asistencia") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atras") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (detalles.isEmpty()) {
                item {
                    Text("No hay registros", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(detalles) { detalle ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "${detalle.nombreEstudiante} ${detalle.apellidoEstudiante}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("CI: ${detalle.estudianteId}")
                                val estadoEsPresente = detalle.estado == "PRESENTE"
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (estadoEsPresente) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = detalle.estado,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Button(onClick = { onToggleEstado(detalle.estudianteId, detalle.estado) }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (detalle.estado == "PRESENTE") "Marcar falta" else "Marcar presente")
                            }
                        }
                    }
                }
            }
        }
    }
}
