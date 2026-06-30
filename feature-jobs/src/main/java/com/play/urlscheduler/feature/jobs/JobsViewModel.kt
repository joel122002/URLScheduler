package com.play.urlscheduler.feature.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.play.urlscheduler.domain.model.LaunchMode
import com.play.urlscheduler.domain.model.RotatorJob
import com.play.urlscheduler.domain.model.UrlEntry
import com.play.urlscheduler.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {

    val activeJob: StateFlow<RotatorJob?> = jobRepository.getActiveJob()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun stopJob(jobId: String) {
        viewModelScope.launch {
            jobRepository.deleteJob(jobId) // simplified for now
        }
    }
}
