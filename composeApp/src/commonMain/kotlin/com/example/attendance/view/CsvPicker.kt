package com.example.attendance.view

import androidx.compose.runtime.Composable

@Composable
expect fun rememberCsvPicker(
    onCsvContent: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit
