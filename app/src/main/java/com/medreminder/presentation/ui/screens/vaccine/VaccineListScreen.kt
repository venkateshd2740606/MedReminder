package com.medreminder.presentation.ui.screens.vaccine

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medreminder.presentation.viewmodel.MedReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineListScreen(viewModel: MedReminderViewModel = hiltViewModel()) {
    Scaffold(topBar = { TopAppBar(title = { Text("Child Vaccines") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.vaccines) { vaccine ->
                Card(Modifier.fillMaxSize()) {
                    androidx.compose.foundation.layout.Column(Modifier.padding(16.dp)) {
                        Text(vaccine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Age: ${vaccine.recommendedAge}", style = MaterialTheme.typography.bodyMedium)
                        Text(vaccine.notes, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
