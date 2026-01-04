package com.cryptosignal.assistant.presentation.screens.signals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptosignal.assistant.domain.models.Direction
import com.cryptosignal.assistant.domain.models.Signal
import com.cryptosignal.assistant.domain.models.SignalStatus
import com.cryptosignal.assistant.domain.repository.SignalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SignalsViewModel @Inject constructor(
    private val signalRepository: SignalRepository
) : ViewModel() {
    
    private val _selectedFilter = MutableStateFlow(SignalFilter.ALL)
    val selectedFilter: StateFlow<SignalFilter> = _selectedFilter.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    val signals: StateFlow<List<Signal>> = combine(
        signalRepository.getSignals(),
        _selectedFilter
    ) { signals, filter ->
        applyFilter(signals, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        loadSignals()
    }
    
    fun setFilter(filter: SignalFilter) {
        _selectedFilter.value = filter
    }
    
    fun refreshSignals() {
        loadSignals()
    }
    
    private fun loadSignals() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Repository already provides flow, so we just ensure it's collected
                Timber.d("Loading signals...")
            } catch (e: Exception) {
                Timber.e(e, "Error loading signals")
                _error.value = "Ошибка загрузки сигналов: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun applyFilter(signals: List<Signal>, filter: SignalFilter): List<Signal> {
        return when (filter) {
            SignalFilter.ALL -> signals
            SignalFilter.ACTIVE -> signals.filter { it.isActive() }
            SignalFilter.LONG -> signals.filter { it.isLong() }
            SignalFilter.SHORT -> signals.filter { it.isShort() }
        }
    }
    
    fun updateSignalStatus(signalId: String, newStatus: SignalStatus) {
        viewModelScope.launch {
            try {
                signalRepository.updateSignalStatus(signalId, newStatus)
                Timber.d("Updated signal $signalId status to $newStatus")
            } catch (e: Exception) {
                Timber.e(e, "Error updating signal status")
                _error.value = "Ошибка обновления сигнала: ${e.message}"
            }
        }
    }
    
    fun deleteSignal(signalId: String) {
        viewModelScope.launch {
            try {
                signalRepository.deleteSignal(signalId)
                Timber.d("Deleted signal $signalId")
            } catch (e: Exception) {
                Timber.e(e, "Error deleting signal")
                _error.value = "Ошибка удаления сигнала: ${e.message}"
            }
        }
    }
}

enum class SignalFilter {
    ALL, ACTIVE, LONG, SHORT
}