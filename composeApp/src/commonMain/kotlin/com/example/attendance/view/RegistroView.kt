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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.attendance.view.theme.AppPrimaryButton
import com.example.attendance.view.theme.AppTextField
import com.example.attendance.view.theme.AttendanceThemeTokens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface RegistroView {
    val nombre: StateFlow<String>
    val apellido: StateFlow<String>
    val carnet: StateFlow<String>
    val esDocente: StateFlow<Boolean>
    val error: StateFlow<String>
    val submitting: StateFlow<Boolean>

    fun onNombreChange(valor: String)
    fun onApellidoChange(valor: String)
    fun onCarnetChange(valor: String)
    fun onEsDocenteChange(valor: Boolean)
    fun setError(valor: String)
    fun setSubmitting(valor: Boolean)
}

class RegistroViewData : RegistroView {
    private val _nombre = MutableStateFlow("")
    override val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _apellido = MutableStateFlow("")
    override val apellido: StateFlow<String> = _apellido.asStateFlow()

    private val _carnet = MutableStateFlow("")
    override val carnet: StateFlow<String> = _carnet.asStateFlow()

    private val _esDocente = MutableStateFlow(false)
    override val esDocente: StateFlow<Boolean> = _esDocente.asStateFlow()

    private val _error = MutableStateFlow("")
    override val error: StateFlow<String> = _error.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    override val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    override fun onNombreChange(valor: String) {
        _nombre.value = valor
        _error.value = ""
    }

    override fun onApellidoChange(valor: String) {
        _apellido.value = valor
        _error.value = ""
    }

    override fun onCarnetChange(valor: String) {
        _carnet.value = valor.filter(Char::isDigit)
        _error.value = ""
    }

    override fun onEsDocenteChange(valor: Boolean) {
        _esDocente.value = valor
        _error.value = ""
    }

    override fun setError(valor: String) {
        _error.value = valor
    }

    override fun setSubmitting(valor: Boolean) {
        _submitting.value = valor
    }
}

@Composable
fun RegistroViewi(
    nombre: StateFlow<String>,
    apellido: StateFlow<String>,
    carnet: StateFlow<String>,
    esDocente: StateFlow<Boolean>,
    error: StateFlow<String>,
    submitting: StateFlow<Boolean>,
    onRegistrar: () -> Unit,
    onVolver: () -> Unit,
    onNombreChange: (String) -> Unit,
    onApellidoChange: (String) -> Unit,
    onCarnetChange: (String) -> Unit,
    onEsDocenteChange: (Boolean) -> Unit,
    setError: (String) -> Unit,
    setSubmitting: (Boolean) -> Unit,

) {
    val metrics = AttendanceThemeTokens.metrics
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardContainer = if (isDark) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    }
    val secondaryTextColor = if (isDark) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val nombreValue by nombre.collectAsState()
    val apellidoValue by apellido.collectAsState()
    val carnetValue by carnet.collectAsState()
    val esDocenteValue by esDocente.collectAsState()
    val errorValue by error.collectAsState()
    val submittingValue by submitting.collectAsState()

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
                    containerColor = cardContainer
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
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Ingresa tus datos para registrarte",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LoginRolePill(
                            label = "Estudiante",
                            selected = !esDocenteValue,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (submittingValue) return@LoginRolePill
                                onEsDocenteChange(false)
                            }
                        )
                        LoginRolePill(
                            label = "Docente",
                            selected = esDocenteValue,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (submittingValue) return@LoginRolePill
                                onEsDocenteChange(true)
                            }
                        )
                    }

                    AppTextField(
                        value = nombreValue,
                        onValueChange = onNombreChange,
                        label = "Nombre",
                        leadingIcon = Icons.Filled.Person,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppTextField(
                        value = apellidoValue,
                        onValueChange = onApellidoChange,
                        label = "Apellido",
                        leadingIcon = Icons.Filled.Person,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppTextField(
                        value = carnetValue,
                        onValueChange = onCarnetChange,
                        label = "Carnet de Identidad",
                        leadingIcon = Icons.Filled.Badge,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorValue.isNotEmpty()) {
                        Text(
                            text = errorValue,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    AppPrimaryButton(
                        text = "Registrarse",
                        onClick = {
                            onRegistrar()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !submittingValue
                    )

                    TextButton(
                        onClick = onVolver,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("¿Ya tienes sesión? Iniciar sesión", fontWeight = FontWeight.SemiBold)
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

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
                if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                else MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
