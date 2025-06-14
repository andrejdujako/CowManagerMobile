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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_cow_manager.data.model.Cow
import com.example.new_cow_manager.ui.viewmodels.CowListViewModel

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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Cow #${cow.cowNumber}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = {
                        Text(if (cow.pregnant) "Pregnant" else "Not Pregnant")
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = if (cow.pregnant)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                if (cow.pregnant) {
                    AssistChip(
                        onClick = { },
                        label = { Text("${cow.pregnancyDuration} days") }
                    )
                }
            }

            cow.inseminationDate?.let {
                Text(
                    text = "Insemination: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (cow.diagnosis.isNotBlank()) {
                Text(
                    text = "Diagnosis: ${cow.diagnosis}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
