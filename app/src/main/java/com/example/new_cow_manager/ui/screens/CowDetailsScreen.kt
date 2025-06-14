package com.example.new_cow_manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_cow_manager.data.model.Cow
import com.example.new_cow_manager.data.model.CowExamination
import com.example.new_cow_manager.ui.viewmodels.CowDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CowDetailsScreen(
    cowId: String,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: CowDetailsViewModel = viewModel { CowDetailsViewModel(cowId) }
) {
    val cow by viewModel.cow.collectAsState()
    val examinations by viewModel.examinations.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cow Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(error ?: "Unknown error")
                        Button(onClick = { viewModel.retryLoading() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            cow != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Basic Information
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Cow #${cow?.cowNumber}",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(if (cow?.pregnant == true) "Pregnant" else "Not Pregnant")
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        labelColor = if (cow?.pregnant == true)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                if (cow?.pregnant == true) {
                                    Text("Pregnancy Duration: ${cow?.pregnancyDuration} days")
                                }
                            }
                        }
                    }

                    // Dates
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Important Dates", style = MaterialTheme.typography.titleMedium)
                                cow?.inseminationDate?.let {
                                    Text("Insemination Date: $it")
                                }
                                cow?.birthDate?.let {
                                    Text("Birth Date: $it")
                                }
                            }
                        }
                    }

                    // Applied Hormones
                    if (cow?.appliedHormones?.isNotEmpty() == true) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Applied Hormones", style = MaterialTheme.typography.titleMedium)
                                    cow?.appliedHormones?.forEach { (hormone, date) ->
                                        Text("$hormone - $date")
                                    }
                                }
                            }
                        }
                    }

                    // Medical Observations
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Medical Observations", style = MaterialTheme.typography.titleMedium)

                                if (cow?.corpusLuteum?.isNotEmpty() == true) {
                                    ObservationSection("Corpus Luteum", cow?.corpusLuteum ?: emptyMap())
                                }

                                if (cow?.corpusRubrum?.isNotEmpty() == true) {
                                    ObservationSection("Corpus Rubrum", cow?.corpusRubrum ?: emptyMap())
                                }

                                if (cow?.cysts?.isNotEmpty() == true) {
                                    ObservationSection("Cysts", cow?.cysts ?: emptyMap())
                                }

                                if (cow?.follicles?.isNotEmpty() == true) {
                                    ObservationSection("Follicles", cow?.follicles ?: emptyMap())
                                }
                            }
                        }
                    }

                    // Notes
                    if (cow?.diagnosis?.isNotBlank() == true || cow?.comment?.isNotBlank() == true) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Notes", style = MaterialTheme.typography.titleMedium)

                                    if (cow?.diagnosis?.isNotBlank() == true) {
                                        Column {
                                            Text(
                                                "Diagnosis",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(cow?.diagnosis ?: "")
                                        }
                                    }

                                    if (cow?.comment?.isNotBlank() == true) {
                                        Column {
                                            Text(
                                                "Comment",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(cow?.comment ?: "")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Examination History
                    if (examinations.isNotEmpty()) {
                        item {
                            Text(
                                "Examination History",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(examinations) { examination ->
                            ExaminationCard(examination)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ObservationSection(
    title: String,
    observations: Map<String, String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        observations.forEach { (side, info) ->
            Text("$side: ${if (info.isNotBlank()) info else "No additional info"}")
        }
    }
}

@Composable
private fun ExaminationCard(examination: CowExamination) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date(examination.date)),
                style = MaterialTheme.typography.titleMedium
            )

            examination.previousState?.let { prevState ->
                if (prevState.pregnant != examination.newState?.pregnant) {
                    Text(
                        "Pregnancy Status Changed: ${if (prevState.pregnant) "Yes" else "No"} → " +
                        "${if (examination.newState?.pregnant == true) "Yes" else "No"}"
                    )
                }

                if (prevState.diagnosis != examination.newState?.diagnosis) {
                    Text("Diagnosis Updated")
                }

                if (prevState.comment != examination.newState?.comment) {
                    Text("Comment Updated")
                }
            }
        }
    }
}
