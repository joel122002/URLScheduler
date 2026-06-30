package com.play.urlscheduler.feature.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.play.urlscheduler.core.ui.components.RotatorTopAppBar
import com.play.urlscheduler.domain.model.LaunchMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditJobScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateEditJobViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            RotatorTopAppBar(
                title = if (uiState.isEditing) "Edit Job" else "Create Job", 
                canNavigateBack = true,
                navigateUp = onNavigateBack
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.saveJob() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isSaving) "Saving..." else "Save Job")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Job Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Job Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.intervalSeconds,
                    onValueChange = viewModel::updateInterval,
                    label = { Text("Interval (seconds)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            item {
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.launchMode.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Launch Mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        LaunchMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name) },
                                onClick = {
                                    viewModel.updateLaunchMode(mode)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("URLs to Rotate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Button(onClick = viewModel::addUrl) {
                        Icon(Icons.Filled.Add, contentDescription = "Add URL")
                        Text("Add")
                    }
                }
            }

            items(uiState.urls, key = { it.id }) { urlInput ->
                UrlInputRow(
                    urlInput = urlInput,
                    onUrlChange = { viewModel.updateUrl(urlInput.id, it) },
                    onRemove = { viewModel.removeUrl(urlInput.id) },
                    canRemove = uiState.urls.size > 1
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // padding for bottom bar
            }
        }
    }
}

@Composable
fun UrlInputRow(
    urlInput: UrlInput,
    onUrlChange: (String) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = urlInput.url,
                    onValueChange = onUrlChange,
                    label = { Text("URL") },
                    modifier = Modifier.weight(1f),
                    isError = urlInput.error != null,
                    singleLine = true
                )
                if (canRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Filled.Delete, 
                            contentDescription = "Remove URL",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (urlInput.error != null) {
                Text(
                    text = urlInput.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }
    }
}
