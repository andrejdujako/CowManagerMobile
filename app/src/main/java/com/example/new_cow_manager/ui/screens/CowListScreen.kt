package com.example.new_cow_manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_cow_manager.data.model.Cow
import com.example.new_cow_manager.ui.viewmodels.CowListViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CowListScreen(
    onCowClick: (String) -> Unit,
    onAddCowClick: () -> Unit,
    viewModel: CowListViewModel = viewModel()
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cows by viewModel.cows.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cow Manager") },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCowClick,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Cow")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search by Cow Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = cows,
                    key = { it.id }
                ) { cow ->
                    CowCard(
                        cow = cow,
                        onClick = { onCowClick(cow.id) }
                    )
                }
            }
        }

        if (showFilterDialog) {
            FilterDialog(
                onDismiss = { showFilterDialog = false },
                onApplyFilter = { days ->
                    viewModel.filterByPregnancyDuration(days)
                    showFilterDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CowCard(
    cow: Cow,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
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
                    text = "Cow #${cow.cowNumber}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (cow.doNotMilk) {
                    Text(
                        text = "DO NOT MILK",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Show diagnosis if available
            if (cow.diagnosis.isNotBlank()) {
                Text(
                    text = "Diagnosis: ${cow.diagnosis}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // GGPG Protocol Status
            cow.ggpgFirstG?.let { firstG ->
                val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val (nextStepDate, nextStepType) = when {
                    currentDate < firstG -> firstG to "First G"
                    currentDate < cow.ggpgSecondG!! -> cow.ggpgSecondG to "Second G"
                    currentDate < cow.ggpgP!! -> cow.ggpgP to "P Treatment"
                    currentDate < cow.ggpgFinalG!! -> cow.ggpgFinalG to "Final G"
                    else -> null to null
                }

                if (nextStepDate != null && nextStepType != null) {
                    Text(
                        text = "Next GGPG step: $nextStepType on $nextStepDate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "GGPG Protocol completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Show pregnancy status and duration
            if (cow.pregnant) {
                Text(
                    text = "Pregnant (${cow.pregnancyDuration} days)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApplyFilter: (Int) -> Unit
) {
    var daysInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Pregnancy Duration") },
        text = {
            OutlinedTextField(
                value = daysInput,
                onValueChange = { daysInput = it },
                label = { Text("Minimum Days") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    daysInput.toIntOrNull()?.let { onApplyFilter(it) }
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
