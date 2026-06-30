package com.medreminder.presentation.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medreminder.ads.AdManager
import com.medreminder.domain.model.ScheduledDose
import com.medreminder.presentation.ui.components.AdBanner
import com.medreminder.presentation.viewmodel.MedReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddMedication: () -> Unit,
    onEditMedication: (Long) -> Unit,
    adManager: AdManager,
    adsEnabled: Boolean,
    viewModel: MedReminderViewModel = hiltViewModel()
) {
    val schedule by viewModel.todaySchedule.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Today's Medicines") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedication) {
                Icon(Icons.Default.Add, contentDescription = "Add medication")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AdBanner(adManager = adManager, adsEnabled = adsEnabled)
            if (schedule.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No medicines scheduled for today", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onAddMedication) { Text("Add Medication") }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(schedule, key = { "${it.medication.id}_${it.scheduledTime}" }) { dose ->
                        DoseCard(dose = dose, onTaken = { viewModel.markTaken(dose) }, onSkipped = { viewModel.markSkipped(dose) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DoseCard(dose: ScheduledDose, onTaken: () -> Unit, onSkipped: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(dose.medication.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${dose.medication.dose} at ${dose.scheduledTime}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            when {
                dose.isTaken -> Text("Taken", color = MaterialTheme.colorScheme.primary)
                dose.isSkipped -> Text("Skipped", color = MaterialTheme.colorScheme.error)
                else -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onTaken) { Text("Mark Taken") }
                    TextButton(onClick = onSkipped) { Text("Skip") }
                }
            }
        }
    }
}
