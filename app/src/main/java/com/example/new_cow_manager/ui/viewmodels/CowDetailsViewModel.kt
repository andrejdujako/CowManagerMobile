package com.example.new_cow_manager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_cow_manager.data.model.Cow
import com.example.new_cow_manager.data.repository.CowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CowDetailsViewModel(
    private val cowId: String,
    private val repository: CowRepository = CowRepository()
) : ViewModel() {

    private val _cow = MutableStateFlow<Cow?>(null)
    val cow = _cow.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val examinations = repository.getCowExaminations(cowId)
        .catch { e ->
            _error.value = "Failed to load examinations: ${e.message}"
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    init {
        loadCowDetails()
    }

    private fun loadCowDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _cow.value = repository.getCowById(cowId)
                if (_cow.value == null) {
                    _error.value = "Could not find cow with ID: $cowId"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load cow details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retryLoading() {
        _error.value = null
        loadCowDetails()
    }
}
