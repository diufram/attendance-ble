package com.example.attendance.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCsvPicker(
    onCsvContent: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    return remember(onError) {
        {
            onError("Seleccion de archivos CSV no disponible en iOS por ahora")
        }
    }
}
