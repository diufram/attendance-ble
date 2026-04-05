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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendance.model.Estudiante

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InscritosView(
    materiaNombre: String,
    inscritos: List<Estudiante>,
    onBack: () -> Unit,
    onAgregarEstudiante: (String, String, String) -> Boolean,
    onImportarCsv: (String) -> Unit
) {
    var mostrarDialogoEstudiante by remember { mutableStateOf(false) }
    var mostrarDialogoCsv by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Inscritos - $materiaNombre") },
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
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { mostrarDialogoEstudiante = true }, modifier = Modifier.weight(1f)) {
                        Text("Agregar")
                    }
                    Button(onClick = { mostrarDialogoCsv = true }, modifier = Modifier.weight(1f)) {
                        Text("Importar CSV")
                    }
                }
            }

            item {
                Text("Estudiantes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (inscritos.isEmpty()) {
                item {
                    Text("No hay estudiantes inscritos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(inscritos) { estudiante ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${estudiante.nombre} ${estudiante.apellido}")
                            Text("CI: ${estudiante.carnetIdentidad}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoEstudiante) {
        var carnet by remember { mutableStateOf("") }
        var nombre by remember { mutableStateOf("") }
        var apellido by remember { mutableStateOf("") }

        ModalBottomSheet(onDismissRequest = { mostrarDialogoEstudiante = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Agregar estudiante", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(value = carnet, onValueChange = { carnet = it }, label = { Text("Carnet") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = apellido, onValueChange = { apellido = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { mostrarDialogoEstudiante = false }, modifier = Modifier.weight(1f)) {
                        Text("Cancelar")
                    }
                    Button(onClick = {
                        val ok = onAgregarEstudiante(carnet, nombre, apellido)
                        if (ok) mostrarDialogoEstudiante = false
                    }, modifier = Modifier.weight(1f)) {
                        Text("Guardar")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    if (mostrarDialogoCsv) {
        var contenido by remember { mutableStateOf("") }

        ModalBottomSheet(onDismissRequest = { mostrarDialogoCsv = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Importar CSV", style = MaterialTheme.typography.titleLarge)
                Text("Formato: carnet,nombre,apellido", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text("Contenido CSV") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { mostrarDialogoCsv = false }, modifier = Modifier.weight(1f)) {
                        Text("Cancelar")
                    }
                    Button(onClick = {
                        onImportarCsv(contenido)
                        mostrarDialogoCsv = false
                    }, modifier = Modifier.weight(1f)) {
                        Text("Importar")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
