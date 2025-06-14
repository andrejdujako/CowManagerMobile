package com.example.new_cow_manager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_cow_manager.data.model.Cow
import com.example.new_cow_manager.data.repository.CowRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CowListViewModel(
    private val repository: CowRepository = CowRepository()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterByPregnancyDays = MutableStateFlow<Int?>(null)

    val cows = combine(
        _searchQuery.debounce(300),
        _filterByPregnancyDays
    ) { query, pregnancyDays ->
        Pair(query, pregnancyDays)
    }.flatMapLatest { (query, pregnancyDays) ->
        when {
            pregnancyDays != null -> repository.getCowsByPregnancyDuration(pregnancyDays)
            query.isNotBlank() -> repository.searchCowsByNumber(query)
            else -> repository.getAllCows()
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _filterByPregnancyDays.value = null
    }

    fun filterByPregnancyDuration(days: Int) {
        _filterByPregnancyDays.value = days
        _searchQuery.value = ""
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _filterByPregnancyDays.value = null
    }
}
