package com.example.new_cow_manager.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_cow_manager.ui.viewmodels.AddEditCowViewModel
import com.example.new_cow_manager.ui.viewmodels.AddEditCowViewModelFactory
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCowScreen(
    cowId: String?,
    onSaveComplete: () -> Unit,
    onBackClick: () -> Unit,
    context: Context = LocalContext.current,
    viewModel: AddEditCowViewModel = viewModel(
        factory = AddEditCowViewModelFactory(cowId, context)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf<DatePickerType?>(null) }
    var showError by remember { mutableStateOf(false) }
    var showHormoneDatePicker by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var newHormone by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cowId == null) "Add New Cow" else "Edit Cow") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (uiState.cowNumber.isBlank()) {
                                showError = true
                            } else {
                                viewModel.saveCow()
                                onSaveComplete()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(), // Add this to handle keyboard
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showError) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "Cow number is required",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            OutlinedTextField(
                value = uiState.cowNumber,
                onValueChange = {
                    viewModel.updateCowNumber(it)
                    showError = false
                },
                label = { Text("Cow Number *") },
                isError = showError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Pregnancy Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Pregnancy Information", style = MaterialTheme.typography.titleMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pregnant")
                        Switch(
                            checked = uiState.pregnant,
                            onCheckedChange = { viewModel.updatePregnant(it) }
                        )
                    }

                    if (uiState.pregnant) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = if (uiState.pregnancyDuration > 0) uiState.pregnancyDuration.toString() else "",
                                onValueChange = {
                                    val days = it.toIntOrNull() ?: 0
                                    viewModel.updatePregnancyDuration(days)
                                },
                                label = { Text("Pregnancy Duration (days)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = if (uiState.pregnancyMonths > 0) uiState.pregnancyMonths.toString() else "",
                                onValueChange = {
                                    val months = it.toIntOrNull() ?: 0
                                    viewModel.updatePregnancyMonths(months)
                                },
                                label = { Text("Months") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        if (uiState.doNotMilk) {
                            Text(
                                "Do not milk this cow",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // GGPG Protocol Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("GGPG Protocol", style = MaterialTheme.typography.titleMedium)

                    OutlinedButton(
                        onClick = { showDatePicker = DatePickerType.GGPG_FIRST_G },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.pregnant // Disable if cow is pregnant
                    ) {
                        Text(
                            if (uiState.ggpgFirstG != null)
                                "First G: ${uiState.ggpgFirstG}"
                            else
                                "Set First G Date"
                        )
                    }

                    if (uiState.pregnant) {
                        Text(
                            "GGPG protocol cannot be started for pregnant cows",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (uiState.ggpgFirstG != null) {
                        OutlinedButton(
                            onClick = { showDatePicker = DatePickerType.GGPG_FIRST_G },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        ) {
                            Text("Second G: ${uiState.ggpgSecondG ?: "Calculating..."}")
                        }

                        OutlinedButton(
                            onClick = { showDatePicker = DatePickerType.GGPG_FIRST_G },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        ) {
                            Text("P: ${uiState.ggpgP ?: "Calculating..."}")
                        }

                        OutlinedButton(
                            onClick = { showDatePicker = DatePickerType.GGPG_FIRST_G },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        ) {
                            Text("Final G: ${uiState.ggpgFinalG ?: "Calculating..."}")
                        }
                    }
                }
            }

            // Dates Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Important Dates", style = MaterialTheme.typography.titleMedium)

                    OutlinedButton(
                        onClick = { showDatePicker = DatePickerType.INSEMINATION },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (uiState.inseminationDate != null)
                                "Insemination Date: ${uiState.inseminationDate}"
                            else
                                "Set Insemination Date"
                        )
                    }

                    OutlinedButton(
                        onClick = { showDatePicker = DatePickerType.BIRTH },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (uiState.birthDate != null)
                                "Given birth at: ${uiState.birthDate}"
                            else
                                "Set Given birth date"
                        )
                    }

                    if (uiState.pregnant && uiState.pregnancyDuration > 0) {
                        val daysUntilCalving = 280 - uiState.pregnancyDuration
                        if (daysUntilCalving > 0) {
                            val currentDate = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                            val estimatedCalvingDate = currentDate.plus(DatePeriod(days = daysUntilCalving))
                            Text(
                                "Estimated calving date: $estimatedCalvingDate (in $daysUntilCalving days)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Medical Information
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Medical Information", style = MaterialTheme.typography.titleMedium)

                    // Hormones
                    Column {
                        Text("Applied Hormones", style = MaterialTheme.typography.titleSmall)

                        // Show existing hormones
                        uiState.appliedHormones.forEach { (hormone, date) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "$hormone - $date",
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.removeHormone(hormone) }) {
                                    Text("×")
                                }
                            }
                        }

                        // Add new hormone
                        var expanded by remember { mutableStateOf(false) }
                        var selectedHormone by remember { mutableStateOf<String?>(null) }
                        var otherHormone by remember { mutableStateOf("") }

                        val hormoneTypes = listOf("GnRH", "PGF2α", "Other")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = selectedHormone ?: "",
                                    onValueChange = { },
                                    label = { Text("Select Hormone") },
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { expanded = !expanded }) {
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = "Select hormone"
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    hormoneTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type) },
                                            onClick = {
                                                selectedHormone = type
                                                expanded = false
                                                if (type != "Other") {
                                                    showHormoneDatePicker = type to true
                                                    selectedHormone = null
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedHormone == "Other") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = otherHormone,
                                    onValueChange = { otherHormone = it },
                                    label = { Text("Hormone Name") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        if (otherHormone.isNotBlank()) {
                                            showHormoneDatePicker = otherHormone to true
                                            otherHormone = ""
                                            selectedHormone = null
                                        }
                                    }
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Medical Observations
                    MedicalObservationSection(
                        title = "Corpus Luteum",
                        observations = uiState.corpusLuteum,
                        onAdd = viewModel::addCorpusLuteum,
                        onRemove = viewModel::removeCorpusLuteum
                    )

                    MedicalObservationSection(
                        title = "Corpus Rubrum",
                        observations = uiState.corpusRubrum,
                        onAdd = viewModel::addCorpusRubrum,
                        onRemove = viewModel::removeCorpusRubrum
                    )

                    MedicalObservationSection(
                        title = "Cysts",
                        observations = uiState.cysts,
                        onAdd = viewModel::addCyst,
                        onRemove = viewModel::removeCyst
                    )

                    MedicalObservationSection(
                        title = "Follicles",
                        observations = uiState.follicles,
                        onAdd = viewModel::addFollicle,
                        onRemove = viewModel::removeFollicle
                    )
                }
            }

            // Notes Section with IME padding
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Notes", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = uiState.diagnosis,
                        onValueChange = { viewModel.updateDiagnosis(it) },
                        label = { Text("Diagnosis") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = uiState.comment,
                        onValueChange = { viewModel.updateComment(it) },
                        label = { Text("Comment") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            // Add bottom padding to ensure the last field is visible with keyboard
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Date Picker Dialogs
        showDatePicker?.let { type ->
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = when (type) {
                    DatePickerType.INSEMINATION -> uiState.inseminationDate?.toEpochDays()?.times(86400000L)
                    DatePickerType.BIRTH -> uiState.birthDate?.toEpochDays()?.times(86400000L)
                    DatePickerType.GGPG_FIRST_G -> uiState.ggpgFirstG?.toEpochDays()?.times(86400000L)
                    DatePickerType.GGPG_SECOND_G -> uiState.ggpgSecondG?.toEpochDays()?.times(86400000L)
                    DatePickerType.GGPG_P -> uiState.ggpgP?.toEpochDays()?.times(86400000L)
                    DatePickerType.GGPG_FINAL_G -> uiState.ggpgFinalG?.toEpochDays()?.times(86400000L)
                } ?: System.currentTimeMillis()
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val localDate = LocalDate.fromEpochDays((millis / 86400000).toInt())
                                when (type) {
                                    DatePickerType.INSEMINATION -> viewModel.updateInseminationDate(localDate)
                                    DatePickerType.BIRTH -> viewModel.updateBirthDate(localDate)
                                    DatePickerType.GGPG_FIRST_G -> viewModel.setGGPGFirstG(localDate)
                                    DatePickerType.GGPG_SECOND_G,
                                    DatePickerType.GGPG_P,
                                    DatePickerType.GGPG_FINAL_G -> { /* These are auto-calculated */ }
                                }
                            }
                            showDatePicker = null
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = null }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Hormone Date Picker
        showHormoneDatePicker?.let { (hormone, _) ->
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = System.currentTimeMillis()
            )

            DatePickerDialog(
                onDismissRequest = { showHormoneDatePicker = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val localDate = LocalDate.fromEpochDays((millis / 86400000).toInt())
                                viewModel.addHormone(hormone, localDate)
                            }
                            newHormone = ""
                            showHormoneDatePicker = null
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showHormoneDatePicker = null }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun MedicalObservationSection(
    title: String,
    observations: Map<String, String>,
    onAdd: (String, String) -> Unit,
    onRemove: (String) -> Unit
) {
    var info by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val sides = listOf("L", "R", "Both") // Using shorter text for better layout
    var selectedSide by remember { mutableStateOf(sides[0]) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (observations.size >= 2) {
                Text(
                    "Maximum 2 entries reached",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        observations.forEach { (side, obsInfo) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "${when(side) {
                        "L" -> "Left"
                        "R" -> "Right"
                        else -> side
                    }}: ${if (obsInfo.isNotBlank()) obsInfo else "No additional info"}",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onRemove(side) }) {
                    Text("×")
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(120.dp)) {
                OutlinedTextField(
                    value = selectedSide,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Side") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Select side"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sides.forEach { side ->
                        DropdownMenuItem(
                            text = {
                                Text(when(side) {
                                    "L" -> "Left"
                                    "R" -> "Right"
                                    else -> side
                                })
                            },
                            onClick = {
                                selectedSide = side
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = info,
                onValueChange = { info = it },
                label = { Text("Info (Optional)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Button(
                onClick = {
                    val sideValue = when(selectedSide) {
                        "L" -> "Left"
                        "R" -> "Right"
                        else -> selectedSide
                    }
                    if (observations.size < 2) {
                        onAdd(sideValue, info)
                        info = ""  // Reset the info field after adding
                    }
                },
            ) {
                Text("Add")
            }
        }
    }
}

enum class DatePickerType {
    INSEMINATION,
    BIRTH,
    GGPG_FIRST_G,
    GGPG_SECOND_G,
    GGPG_P,
    GGPG_FINAL_G
}
