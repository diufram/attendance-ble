package com.example.attendance.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberCsvPicker(
    onCsvContent: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val onCsvContentState = rememberUpdatedState(onCsvContent)
    val onErrorState = rememberUpdatedState(onError)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
                reader?.readText().orEmpty()
            }
        }.onSuccess { content ->
            if (content.isBlank()) {
                onErrorState.value("El archivo CSV esta vacio")
            } else {
                onCsvContentState.value(content)
            }
        }.onFailure {
            onErrorState.value("No se pudo leer el archivo CSV")
        }
    }

    return remember(launcher) {
        { launcher.launch("text/*") }
    }
}
