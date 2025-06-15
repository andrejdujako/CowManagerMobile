package com.example.new_cow_manager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_cow_manager.data.model.Cow
import com.example.new_cow_manager.data.repository.CowRepository
import com.example.new_cow_manager.utils.NotificationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

sealed class AddEditCowUiEvent {
    data class ShowError(val message: String) : AddEditCowUiEvent()
    object SaveSuccess : AddEditCowUiEvent()
}

data class AddEditCowUiState(
    val cowNumber: String = "",
    val pregnant: Boolean = false, // Changed from isPregnant to match database field
    val pregnancyDuration: Int = 0,
    val pregnancyMonths: Int = 0,
    val inseminationDate: LocalDate? = null,
    val birthDate: LocalDate? = null,
    val appliedHormones: Map<String, LocalDate> = emptyMap(),
    val corpusLuteum: Map<String, String> = emptyMap(),
    val corpusRubrum: Map<String, String> = emptyMap(),
    val cysts: Map<String, String> = emptyMap(),
    val follicles: Map<String, String> = emptyMap(),
    val diagnosis: String = "",
    val comment: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val doNotMilk: Boolean = false,
    val ggpgFirstG: LocalDate? = null,
    val ggpgSecondG: LocalDate? = null,
    val ggpgP: LocalDate? = null,
    val ggpgFinalG: LocalDate? = null
)

class AddEditCowViewModel(
    private val cowId: String?,
    private val repository: CowRepository = CowRepository(),
    private val notificationService: NotificationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditCowUiState())
    val uiState: StateFlow<AddEditCowUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AddEditCowUiEvent>()
    val uiEvent: SharedFlow<AddEditCowUiEvent> = _uiEvent.asSharedFlow()

    init {
        if (cowId != null) {
            loadCow()
        }
    }

    private fun loadCow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val cow = repository.getCowById(cowId!!)
                if (cow != null) {
                    _uiState.update {
                        it.copy(
                            cowNumber = cow.cowNumber,
                            pregnant = cow.pregnant,
                            pregnancyDuration = cow.pregnancyDuration,
                            pregnancyMonths = cow.pregnancyMonths,
                            inseminationDate = cow.inseminationDate,
                            birthDate = cow.birthDate,
                            appliedHormones = cow.appliedHormones,
                            corpusLuteum = cow.corpusLuteum,
                            corpusRubrum = cow.corpusRubrum,
                            cysts = cow.cysts,
                            follicles = cow.follicles,
                            diagnosis = cow.diagnosis,
                            comment = cow.comment,
                            ggpgFirstG = cow.ggpgFirstG,
                            ggpgSecondG = cow.ggpgSecondG,
                            ggpgP = cow.ggpgP,
                            ggpgFinalG = cow.ggpgFinalG,
                            isLoading = false
                        )
                    }
                } else {
                    _uiEvent.emit(AddEditCowUiEvent.ShowError("Cow not found"))
                }
            } catch (e: Exception) {
                _uiEvent.emit(AddEditCowUiEvent.ShowError("Failed to load cow: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveCow() {
        val currentState = _uiState.value

        if (currentState.cowNumber.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(AddEditCowUiEvent.ShowError("Cow number is required"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val cow = Cow(
                    id = cowId ?: "",
                    cowNumber = currentState.cowNumber,
                    pregnant = currentState.pregnant,
                    pregnancyDuration = currentState.pregnancyDuration,
                    pregnancyMonths = currentState.pregnancyMonths,
                    doNotMilk = currentState.pregnant && currentState.pregnancyDuration > 200,
                    inseminationDate = currentState.inseminationDate,
                    birthDate = currentState.birthDate,
                    ggpgFirstG = currentState.ggpgFirstG,
                    ggpgSecondG = currentState.ggpgSecondG,
                    ggpgP = currentState.ggpgP,
                    ggpgFinalG = currentState.ggpgFinalG,
                    appliedHormones = currentState.appliedHormones,
                    corpusLuteum = currentState.corpusLuteum,
                    corpusRubrum = currentState.corpusRubrum,
                    cysts = currentState.cysts,
                    follicles = currentState.follicles,
                    diagnosis = currentState.diagnosis,
                    comment = currentState.comment
                )

                val result = if (cowId == null) {
                    repository.addCow(cow)
                } else {
                    repository.updateCow(cow)
                }

                result.fold(
                    onSuccess = {
                        _uiEvent.emit(AddEditCowUiEvent.SaveSuccess)
                    },
                    onFailure = { error ->
                        _uiEvent.emit(AddEditCowUiEvent.ShowError("Failed to save cow: ${error.message}"))
                    }
                )
            } catch (e: Exception) {
                _uiEvent.emit(AddEditCowUiEvent.ShowError("Failed to save cow: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // State update functions
    fun updateCowNumber(number: String) {
        _uiState.update { it.copy(cowNumber = number) }
    }

    fun updatePregnant(pregnant: Boolean) {
        _uiState.update {
            it.copy(
                pregnant = pregnant,
                // Reset pregnancy duration to 0 when setting to not pregnant
                pregnancyDuration = if (!pregnant) 0 else it.pregnancyDuration
            )
        }
    }

    fun updatePregnancyMonths(months: Int) {
        _uiState.update { currentState ->
            val days = months * 30 // Approximate conversion
            currentState.copy(
                pregnancyMonths = months,
                pregnancyDuration = days,
                doNotMilk = days > 200
            )
        }
    }

    fun updatePregnancyDuration(days: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                pregnancyDuration = days,
                doNotMilk = days > 200
            )
        }
    }

    fun updateInseminationDate(date: LocalDate) {
        _uiState.update { it.copy(inseminationDate = date) }
    }

    fun updateBirthDate(date: LocalDate) {
        _uiState.update { it.copy(birthDate = date) }
    }

    fun addHormone(name: String, date: LocalDate) {
        _uiState.update {
            val updatedHormones = it.appliedHormones.toMutableMap()
            updatedHormones[name] = date
            it.copy(appliedHormones = updatedHormones)
        }
    }

    fun removeHormone(name: String) {
        _uiState.update {
            val updatedHormones = it.appliedHormones.toMutableMap()
            updatedHormones.remove(name)
            it.copy(appliedHormones = updatedHormones)
        }
    }

    fun addCorpusLuteum(side: String, info: String) {
        _uiState.update {
            if (it.corpusLuteum.size >= 2) return@update it
            val updated = it.corpusLuteum.toMutableMap()
            updated[side] = info
            it.copy(corpusLuteum = updated)
        }
    }

    fun removeCorpusLuteum(side: String) {
        _uiState.update {
            val updated = it.corpusLuteum.toMutableMap()
            updated.remove(side)
            it.copy(corpusLuteum = updated)
        }
    }

    fun addCorpusRubrum(side: String, info: String) {
        _uiState.update {
            if (it.corpusRubrum.size >= 2) return@update it
            val updated = it.corpusRubrum.toMutableMap()
            updated[side] = info
            it.copy(corpusRubrum = updated)
        }
    }

    fun removeCorpusRubrum(side: String) {
        _uiState.update {
            val updated = it.corpusRubrum.toMutableMap()
            updated.remove(side)
            it.copy(corpusRubrum = updated)
        }
    }

    fun addCyst(side: String, info: String) {
        _uiState.update {
            if (it.cysts.size >= 2) return@update it
            val updated = it.cysts.toMutableMap()
            updated[side] = info
            it.copy(cysts = updated)
        }
    }

    fun removeCyst(side: String) {
        _uiState.update {
            val updated = it.cysts.toMutableMap()
            updated.remove(side)
            it.copy(cysts = updated)
        }
    }

    fun addFollicle(side: String, info: String) {
        _uiState.update {
            if (it.follicles.size >= 2) return@update it
            val updated = it.follicles.toMutableMap()
            updated[side] = info
            it.copy(follicles = updated)
        }
    }

    fun removeFollicle(side: String) {
        _uiState.update {
            val updated = it.follicles.toMutableMap()
            updated.remove(side)
            it.copy(follicles = updated)
        }
    }

    fun updateDiagnosis(diagnosis: String) {
        _uiState.update { it.copy(diagnosis = diagnosis) }
    }

    fun updateComment(comment: String) {
        _uiState.update { it.copy(comment = comment) }
    }

    fun setGGPGFirstG(date: LocalDate) {
        _uiState.update { currentState ->
            // Use kotlinx.datetime's proper methods
            val secondG = date.plus(DatePeriod(days = 7))
            val p = secondG.plus(DatePeriod(days = 2))
            val finalG = p.plus(DatePeriod(days = 2))

            // Schedule notifications for each date
            viewModelScope.launch {
                notificationService.scheduleGgpgNotification(
                    date,
                    "GGPG Protocol - First G",
                    "Time for the first G treatment",
                    NotificationService.GGPG_NOTIFICATION_ID_FIRST_G
                )
                notificationService.scheduleGgpgNotification(
                    secondG,
                    "GGPG Protocol - Second G",
                    "Time for the second G treatment",
                    NotificationService.GGPG_NOTIFICATION_ID_SECOND_G
                )
                notificationService.scheduleGgpgNotification(
                    p,
                    "GGPG Protocol - P Treatment (56 hours after Second G)",
                    "Time for P treatment",
                    NotificationService.GGPG_NOTIFICATION_ID_P
                )
                notificationService.scheduleGgpgNotification(
                    finalG,
                    "GGPG Protocol - Final G",
                    "Time for the final G treatment",
                    NotificationService.GGPG_NOTIFICATION_ID_FINAL_G
                )
            }

            currentState.copy(
                ggpgFirstG = date,
                ggpgSecondG = secondG,
                ggpgP = p,
                ggpgFinalG = finalG
            )
        }
    }
}
