package com.example.attendance.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
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

interface ILoginView {
    val carnet: StateFlow<String>
    val error: StateFlow<String>
    val submitting: StateFlow<Boolean>

    fun onCarnetChange(valor: String)
    fun setError(valor: String)
    fun setSubmitting(valor: Boolean)
}

class LoginViewData : ILoginView {
    private val _carnet = MutableStateFlow("")
    override val carnet: StateFlow<String> = _carnet.asStateFlow()

    private val _error = MutableStateFlow("")
    override val error: StateFlow<String> = _error.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    override val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    override fun onCarnetChange(valor: String) {
        _carnet.value = valor.filter(Char::isDigit)
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
fun LoginView(
    view: ILoginView,
    onLogin: () -> Unit,
    onIrRegistro: () -> Unit
) {
    val carnet by view.carnet.collectAsState()
    val error by view.error.collectAsState()
    val submitting by view.submitting.collectAsState()

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
                            imageVector = Icons.Filled.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Text(
                        text = "Attendance Control",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Ingresa tu carnet para iniciar sesión",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Center
                    )

                    AppTextField(
                        value = carnet,
                        onValueChange = view::onCarnetChange,
                        label = "Carnet de Identidad",
                        leadingIcon = Icons.Filled.Badge,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error.isNotEmpty()) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    AppPrimaryButton(
                        text = "Ingresar",
                        onClick = {
                            onLogin()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !submitting
                    )

                    TextButton(
                        onClick = onIrRegistro,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Regístrate", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
