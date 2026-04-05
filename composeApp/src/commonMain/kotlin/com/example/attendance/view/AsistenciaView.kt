package com.example.attendance.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendance.model.Asistencia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaView(
    materiaNombre: String,
    asistencias: List<Asistencia>,
    onBack: () -> Unit,
    onInscritosClick: () -> Unit,
    onIniciarAsistencia: () -> Unit,
    onAsistenciaClick: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(materiaNombre) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atras") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onInscritosClick, modifier = Modifier.weight(1f)) {
                        Text("Inscritos")
                    }
                    Button(onClick = onIniciarAsistencia, modifier = Modifier.weight(1f)) {
                        Text("Iniciar asistencia")
                    }
                }
            }

            item {
                Text("Asistencias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (asistencias.isEmpty()) {
                item {
                    Text("Sin asistencias registradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(asistencias) { asistencia ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onAsistenciaClick(asistencia.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "Asistencia ${asistencia.id} - ${asistencia.fecha}",
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}
