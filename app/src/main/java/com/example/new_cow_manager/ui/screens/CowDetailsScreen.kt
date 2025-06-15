package com.example.new_cow_manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_cow_manager.data.model.CowExamination
import com.example.new_cow_manager.ui.viewmodels.CowDetailsViewModel
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
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

    // Effect to refresh data when screen is focused
    LaunchedEffect(Unit) {
        viewModel.startObservingCow()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopObservingCow()
        }
    }

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
                    // Basic Information with Do not milk warning
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Cow #${cow?.cowNumber}",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    if (cow?.doNotMilk == true) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = "Warning",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            Text(
                                                "DO NOT MILK",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

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

                    // GGPG Protocol Section (if active)
                    cow?.ggpgFirstG?.let { firstG ->
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("GGPG Protocol", style = MaterialTheme.typography.titleMedium)

                                    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                                    val ggpgStatus = when {
                                        currentDate < firstG -> "Upcoming"
                                        currentDate > cow?.ggpgFinalG!! -> "Completed"
                                        else -> "In Progress"
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Status: $ggpgStatus",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = when(ggpgStatus) {
                                                "Upcoming" -> MaterialTheme.colorScheme.primary
                                                "In Progress" -> MaterialTheme.colorScheme.tertiary
                                                else -> MaterialTheme.colorScheme.secondary
                                            }
                                        )

                                        // Show next step more prominently if in progress
                                        if (ggpgStatus == "In Progress") {
                                            val nextStep = when {
                                                currentDate < firstG -> "First G"
                                                currentDate < cow?.ggpgSecondG!! -> "Second G"
                                                currentDate < cow?.ggpgP!! -> "P Treatment"
                                                else -> "Final G"
                                            }
                                            AssistChip(
                                                onClick = { },
                                                label = { Text("Next: $nextStep") },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    labelColor = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                                    Text("Treatment Schedule:", style = MaterialTheme.typography.titleSmall)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        GGPGDateItem("First G", firstG, currentDate)
                                        cow?.ggpgSecondG?.let { GGPGDateItem("Second G", it, currentDate) }
                                        cow?.ggpgP?.let { GGPGDateItem("P Treatment", it, currentDate) }
                                        cow?.ggpgFinalG?.let { GGPGDateItem("Final G", it, currentDate) }
                                    }
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
                                    Text("Given birth at: $it")
                                }
                                if (cow?.pregnant == true && cow!!.pregnancyDuration > 0) {
                                    val daysUntilCalving = 280 - cow!!.pregnancyDuration
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

                    // Examination history
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
                if (prevState.pregnant != examination.newState.pregnant) {
                    Text(
                        "Pregnancy Status Changed: ${if (prevState.pregnant) "Yes" else "No"} → " +
                        if (examination.newState.pregnant) "Yes" else "No"
                    )
                }

                if (prevState.diagnosis != examination.newState.diagnosis) {
                    Text("Diagnosis Updated")
                }

                if (prevState.comment != examination.newState.comment) {
                    Text("Comment Updated")
                }
            }
        }
    }
}

@Composable
private fun GGPGDateItem(label: String, date: LocalDate, currentDate: LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date.toString(),
                color = when {
                    currentDate == date -> MaterialTheme.colorScheme.primary
                    currentDate > date -> MaterialTheme.colorScheme.secondary
                    else -> LocalContentColor.current
                }
            )
            if (currentDate == date) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Today",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            } else if (currentDate > date) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
