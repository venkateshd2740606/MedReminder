package com.medreminder.presentation.ui.screens.history

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medreminder.presentation.viewmodel.MedReminderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MedReminderViewModel = hiltViewModel()) {
    val logs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

    Scaffold(topBar = { TopAppBar(title = { Text("History") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            items(logs, key = { it.id }) { log ->
                Card(Modifier.fillMaxSize()) {
                    androidx.compose.foundation.layout.Column(Modifier.padding(12.dp)) {
                        Text(log.medicationName, fontWeight = FontWeight.Bold)
                        Text("${log.date} ${log.scheduledTime} — ${log.status.name}")
                        Text(fmt.format(Date(log.timestamp)), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
