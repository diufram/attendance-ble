package com.example.attendance.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.attendance.controller.LoginController
import com.example.attendance.view.theme.AppPrimaryButton
import com.example.attendance.view.theme.AppSecondaryButton
import com.example.attendance.view.theme.AppTextField
import com.example.attendance.view.theme.AttendanceThemeTokens

@Composable
fun LoginView(
    controller: LoginController
) {
    val metrics = AttendanceThemeTokens.metrics
    val sizes = AttendanceThemeTokens.textSizes

    var carnet by remember { mutableStateOf("") }
    var esDocente by remember { mutableStateOf(true) }
    val error by controller.errorMensaje.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(metrics.thinBorder, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Attendance",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Inicia sesion para continuar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (esDocente) {
                            AppPrimaryButton(
                                text = "Docente",
                                onClick = { esDocente = true; controller.limpiarError() },
                                modifier = Modifier.weight(1f)
                            )
                            AppSecondaryButton(
                                text = "Estudiante",
                                onClick = { esDocente = false; controller.limpiarError() },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            AppSecondaryButton(
                                text = "Docente",
                                onClick = { esDocente = true; controller.limpiarError() },
                                modifier = Modifier.weight(1f)
                            )
                            AppPrimaryButton(
                                text = "Estudiante",
                                onClick = { esDocente = false; controller.limpiarError() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    AppTextField(
                        value = carnet,
                        onValueChange = {
                            carnet = it
                            controller.limpiarError()
                        },
                        label = "Carnet de Identidad",
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedVisibility(visible = !error.isNullOrEmpty()) {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = sizes.helperText),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    AppPrimaryButton(
                        text = "Ingresar",
                        onClick = { controller.ingresar(carnet, esDocente) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (esDocente)
                    "Se creara tu perfil docente si no existe"
                else
                    "Se creara tu perfil estudiante si no existe",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = sizes.helperText),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
