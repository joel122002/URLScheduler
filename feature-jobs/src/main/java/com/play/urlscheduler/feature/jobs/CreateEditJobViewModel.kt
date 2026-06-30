package com.play.urlscheduler.feature.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.play.urlscheduler.domain.model.LaunchMode
import com.play.urlscheduler.domain.model.RotatorJob
import com.play.urlscheduler.domain.model.UrlEntry
import com.play.urlscheduler.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle

data class UrlInput(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "",
    val error: String? = null
)

data class CreateJobUiState(
    val isEditing: Boolean = false,
    val name: String = "",
    val intervalSeconds: String = "10",
    val launchMode: LaunchMode = LaunchMode.CUSTOM_TAB,
    val urls: List<UrlInput> = listOf(UrlInput()),
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false
)

@HiltViewModel
class CreateEditJobViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val jobId: String? = savedStateHandle.get<String>("jobId")
    private var originalCreatedAt: Long = System.currentTimeMillis()

    private val _uiState = MutableStateFlow(CreateJobUiState(isEditing = jobId != null))
    val uiState: StateFlow<CreateJobUiState> = _uiState.asStateFlow()

    init {
        jobId?.let { id ->
            viewModelScope.launch {
                val job = jobRepository.getJobById(id)
                val urls = jobRepository.getUrlsListForJob(id)
                
                if (job != null) {
                    originalCreatedAt = job.createdAt
                    val mappedUrls = if (urls.isNotEmpty()) {
                        urls.map { UrlInput(id = it.id, url = it.url, error = null) }
                    } else {
                        listOf(UrlInput())
                    }
                    
                    _uiState.update { state ->
                        state.copy(
                            name = job.name,
                            intervalSeconds = job.intervalSeconds.toString(),
                            launchMode = job.launchMode,
                            urls = mappedUrls
                        )
                    }
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateInterval(interval: String) {
        // Only allow digits
        if (interval.all { char -> char.isDigit() }) {
            _uiState.update { it.copy(intervalSeconds = interval) }
        }
    }

    fun updateLaunchMode(mode: LaunchMode) {
        _uiState.update { it.copy(launchMode = mode) }
    }

    fun addUrl() {
        _uiState.update { it.copy(urls = it.urls + UrlInput()) }
    }

    fun updateUrl(id: String, newUrl: String) {
        _uiState.update { state ->
            val updatedUrls = state.urls.map {
                if (it.id == id) {
                    it.copy(url = newUrl, error = validateUrl(newUrl))
                } else it
            }
            state.copy(urls = updatedUrls)
        }
    }

    fun removeUrl(id: String) {
        _uiState.update { state ->
            state.copy(urls = state.urls.filter { it.id != id })
        }
    }

    private fun validateUrl(url: String): String? {
        if (url.isBlank()) return "URL cannot be empty"
        
        val normalized = normalizeUrl(url)
        
        if (normalized.startsWith("ftp://")) return "FTP is not supported"
        if (normalized.startsWith("javascript:")) return "JavaScript URLs are not supported"
        if (normalized.startsWith("file://")) return "Local files are not supported"
        if (normalized.startsWith("content://")) return "Content URIs are not supported"
        
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            return "Invalid URL format"
        }
        
        return null
    }

    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return trimmed
        
        // Auto-prepend https:// if missing typical scheme protocols but looks like a domain
        if (!trimmed.contains("://") && !trimmed.startsWith("javascript:") && trimmed.contains(".")) {
            return "https://\$trimmed"
        }
        return trimmed
    }

    fun saveJob() {
        val currentState = _uiState.value
        
        // Final validation pass
        val validatedUrls = currentState.urls.map { 
            val normalized = normalizeUrl(it.url)
            it.copy(url = normalized, error = validateUrl(normalized)) 
        }
        
        val hasErrors = validatedUrls.any { it.error != null } || 
                        currentState.name.isBlank() || 
                        currentState.intervalSeconds.toLongOrNull() == null ||
                        validatedUrls.isEmpty()

        if (hasErrors) {
            _uiState.update { it.copy(urls = validatedUrls) }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val finalJobId = jobId ?: UUID.randomUUID().toString()
            
            val job = RotatorJob(
                id = finalJobId,
                name = currentState.name.ifBlank { "Unnamed Job" },
                enabled = true,
                intervalSeconds = currentState.intervalSeconds.toLongOrNull() ?: 10L,
                launchMode = currentState.launchMode,
                currentIndex = 0, // Reset to 0 when editing
                createdAt = originalCreatedAt,
                updatedAt = System.currentTimeMillis()
            )

            val urlEntries = validatedUrls.mapIndexed { index, urlInput ->
                UrlEntry(
                    id = UUID.randomUUID().toString(), // Generate new URL IDs since we replace them
                    jobId = finalJobId,
                    url = urlInput.url,
                    orderIndex = index
                )
            }

            // If editing, clear old URLs first
            if (jobId != null) {
                jobRepository.deleteUrlsForJob(finalJobId)
            }
            
            jobRepository.saveJob(job)
            jobRepository.saveUrls(urlEntries)

            _uiState.update { it.copy(isSaving = false, saveCompleted = true) }
        }
    }
}
