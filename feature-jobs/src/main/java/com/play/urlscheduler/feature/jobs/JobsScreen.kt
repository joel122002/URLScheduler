package com.play.urlscheduler.feature.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.play.urlscheduler.core.ui.components.RotatorTopAppBar
import com.play.urlscheduler.domain.model.RotatorJob

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsScreen(
    onNavigateToCreateJob: () -> Unit,
    onNavigateToEditJob: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JobsViewModel = hiltViewModel()
) {
    val activeJob by viewModel.activeJob.collectAsState()

    Scaffold(
        topBar = {
            RotatorTopAppBar(title = "Jobs")
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateJob) {
                Icon(Icons.Filled.Add, contentDescription = "Create Job")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (activeJob == null) {
                Text(
                    text = "No jobs created. Click + to create a new job.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                activeJob?.let { job ->
                    JobCard(
                        job = job, 
                        onStop = { viewModel.stopJob(job.id) },
                        onEdit = { onNavigateToEditJob(job.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun JobCard(job: RotatorJob, onStop: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(job.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Interval: ${job.intervalSeconds}s")
            Text("Launch Mode: ${job.launchMode.name}")
            Text("Current Index: ${job.currentIndex}")
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit Job")
                }
                
                Button(
                    onClick = onStop, 
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Job")
                }
            }
        }
    }
}
