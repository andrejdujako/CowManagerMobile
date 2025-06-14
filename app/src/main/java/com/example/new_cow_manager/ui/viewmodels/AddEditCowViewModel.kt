package com.example.new_cow_manager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_cow_manager.data.model.Cow
import com.example.new_cow_manager.data.repository.CowRepository
import com.example.new_cow_manager.notifications.NotificationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

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
//    private val notificationService: NotificationService
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
                            inseminationDate = cow.inseminationDate,
                            birthDate = cow.birthDate,
                            appliedHormones = cow.appliedHormones,
                            corpusLuteum = cow.corpusLuteum,
                            corpusRubrum = cow.corpusRubrum,
                            cysts = cow.cysts,
                            follicles = cow.follicles,
                            diagnosis = cow.diagnosis,
                            comment = cow.comment,
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
                    pregnancyDuration = if (currentState.pregnant) currentState.pregnancyDuration else 0,
                    inseminationDate = currentState.inseminationDate,
                    birthDate = currentState.birthDate,
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
        _uiState.update {
            it.copy(
                pregnancyMonths = months,
                // Convert months to days for consistency
                pregnancyDuration = months * 30,
                // Update doNotMilk status based on pregnancy duration
                doNotMilk = months * 30 > 200
            )
        }
    }

    fun updatePregnancyDuration(days: Int) {
        _uiState.update {
            it.copy(
                pregnancyDuration = days,
                // Convert days to months for consistency
                pregnancyMonths = days / 30,
                // Update doNotMilk status
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

//    fun calculateAndSetGgpgDates() {
//        _uiState.value.ggpgFirstG?.let { firstG ->
//            val dates = Cow("").calculateGgpgDates(firstG)
//            _uiState.update {
//                it.copy(
//                    ggpgSecondG = dates.secondG,
//                    ggpgP = dates.p,
//                    ggpgFinalG = dates.finalG
//                )
//            }
//            // Schedule notifications for each date
////            scheduleGgpgNotifications(dates)
//        }
//    }
//
//    fun updateGgpgFirstG(date: LocalDate) {
//        _uiState.update { it.copy(ggpgFirstG = date) }
//        calculateAndSetGgpgDates()
//    }
//
//    fun updateGgpgSecondG(date: LocalDate) {
//        _uiState.update { it.copy(ggpgSecondG = date) }
//    }
//
//    fun updateGgpgP(date: LocalDate) {
//        _uiState.update { it.copy(ggpgP = date) }
//    }
//
//    fun updateGgpgFinalG(date: LocalDate) {
//        _uiState.update { it.copy(ggpgFinalG = date) }
//    }

//    private fun scheduleGgpgNotifications(dates: GgpgDates) {
//        notificationService.scheduleGgpgNotification(
//            dates.firstG,
//            "GGPG Protocol - First G",
//            "It's time for the first G treatment",
//            NotificationService.NOTIFICATION_ID_FIRST_G
//        )
//
//        notificationService.scheduleGgpgNotification(
//            dates.secondG,
//            "GGPG Protocol - Second G",
//            "Time for second G treatment (7 days after first G)",
//            NotificationService.NOTIFICATION_ID_SECOND_G
//        )
//
//        notificationService.scheduleGgpgNotification(
//            dates.p,
//            "GGPG Protocol - P Treatment",
//            "Time for P treatment (56 hours after second G)",
//            NotificationService.NOTIFICATION_ID_P
//        )
//
//        notificationService.scheduleGgpgNotification(
//            dates.finalG,
//            "GGPG Protocol - Final G",
//            "Time for final G treatment (2 days after P)",
//            NotificationService.NOTIFICATION_ID_FINAL_G
//        )
//    }
}
