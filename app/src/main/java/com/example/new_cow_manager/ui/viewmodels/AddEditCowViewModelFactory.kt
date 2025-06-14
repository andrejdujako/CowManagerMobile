package com.example.new_cow_manager.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.new_cow_manager.utils.NotificationService

class AddEditCowViewModelFactory(
    private val cowId: String?,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditCowViewModel::class.java)) {
            return AddEditCowViewModel(
                cowId = cowId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//return AddEditCowViewModel(
//cowId = cowId,
//notificationService = NotificationService(context)
//) as T