package com.medreminder.presentation.ui.screens.medication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medreminder.presentation.viewmodel.MedReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicationScreen(
    medicationId: Long?,
    onBack: () -> Unit,
    viewModel: MedReminderViewModel = hiltViewModel()
) {
    val editing by viewModel.editingMedication.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var times by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isVaccine by remember { mutableStateOf(false) }

    LaunchedEffect(medicationId) {
        if (medicationId != null && medicationId > 0) viewModel.loadMedication(medicationId)
        else viewModel.startNewMedication()
    }

    LaunchedEffect(editing) {
        editing?.let {
            name = it.name
            dose = it.dose
            times = it.scheduleTimes.joinToString(", ")
            notes = it.notes
            isVaccine = it.isVaccine
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (medicationId != null && medicationId > 0) "Edit Medication" else "Add Medication") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = dose, onValueChange = { dose = it }, label = { Text("Dose") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = times, onValueChange = { times = it },
                label = { Text("Schedule (e.g. 08:00, 20:00)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isVaccine, onCheckedChange = { isVaccine = it })
                Text("Vaccine")
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.saveMedication(name, dose, times, notes, isVaccine)
                    onBack()
                },
                enabled = name.isNotBlank() && dose.isNotBlank() && times.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
        }
    }
}
