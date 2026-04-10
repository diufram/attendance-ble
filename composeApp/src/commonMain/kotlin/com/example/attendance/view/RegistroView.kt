package com.example.attendance.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.attendance.view.theme.AppPrimaryButton
import com.example.attendance.view.theme.AppTextField
import com.example.attendance.view.theme.AttendanceThemeTokens

@Composable
fun RegistroView(
    onRegistrar: (carnet: String, nombre: String, apellido: String, esDocente: Boolean) -> String?,
    onVolver: () -> Unit
) {
    val metrics = AttendanceThemeTokens.metrics
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var carnet by remember { mutableStateOf("") }
    var esDocente by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                border = BorderStroke(
                    metrics.thinBorder,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(
                            metrics.thinBorder,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Text(
                        text = "Registrarse",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Ingresa tus datos para registrarte",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LoginRolePill(
                            label = "Estudiante",
                            selected = !esDocente,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (submitting) return@LoginRolePill
                                esDocente = false
                                error = ""
                            }
                        )
                        LoginRolePill(
                            label = "Docente",
                            selected = esDocente,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (submitting) return@LoginRolePill
                                esDocente = true
                                error = ""
                            }
                        )
                    }

                    AppTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            error = ""
                        },
                        label = "Nombre",
                        leadingIcon = Icons.Filled.Person,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppTextField(
                        value = apellido,
                        onValueChange = {
                            apellido = it
                            error = ""
                        },
                        label = "Apellido",
                        leadingIcon = Icons.Filled.Person,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppTextField(
                        value = carnet,
                        onValueChange = {
                            carnet = it.filter(Char::isDigit)
                            error = ""
                        },
                        label = "Carnet de Identidad",
                        leadingIcon = Icons.Filled.Badge,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error.isNotEmpty()) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    AppPrimaryButton(
                        text = "Registrarse",
                        onClick = {
                            submitting = true
                            val result = onRegistrar(carnet, nombre, apellido, esDocente)
                            if (result != null) {
                                error = result
                                submitting = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !submitting
                    )

                    TextButton(onClick = onVolver) {
                        Text("¿Ya tienes sesión? Iniciar sesión")
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginRolePill(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val metrics = AttendanceThemeTokens.metrics

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        },
        border = BorderStroke(
            metrics.thinBorder,
            if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            }
        )
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge
        )
    }
}