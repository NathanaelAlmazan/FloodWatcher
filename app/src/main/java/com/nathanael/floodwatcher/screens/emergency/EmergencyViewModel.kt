package com.nathanael.floodwatcher.screens.emergency

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nathanael.floodwatcher.model.EmergencyDirectory
import com.nathanael.floodwatcher.model.repos.EmergencyRepository
import com.nathanael.floodwatcher.model.repos.Result
import kotlinx.coroutines.launch

class EmergencyViewModel(private val repository: EmergencyRepository): ViewModel() {
    private var _barangayDirectories = mutableStateListOf<EmergencyDirectory>()
    private var _medicalDirectories = mutableStateListOf<EmergencyDirectory>()
    private var _municipalDirectories = mutableStateListOf<EmergencyDirectory>()
    private var _nationalDirectories = mutableStateListOf<EmergencyDirectory>()
    private var _errorMessage by mutableStateOf<String?>(null)

    val barangayDirectories: List<EmergencyDirectory> get() = _barangayDirectories
    val medicalDirectories: List<EmergencyDirectory> get() = _medicalDirectories
    val municipalDirectories: List<EmergencyDirectory> get() = _municipalDirectories
    val nationalDirectories: List<EmergencyDirectory> get() = _nationalDirectories

    init {
        val directories = listOf("barangay", "medical", "municipal", "ndrrmc")
        for (directory in directories) {
            fetchDirectories(directory)
        }
    }

    private fun fetchDirectories(type: String) {
        viewModelScope.launch {
            when (val result = repository.fetchDirectories(type)) {
                is Result.Success<List<EmergencyDirectory>> -> {
                    when (type) {
                        "barangay" -> {
                            _barangayDirectories.clear()
                            _barangayDirectories.addAll(result.data)
                        }
                        "medical" -> {
                            _medicalDirectories.clear()
                            _medicalDirectories.addAll(result.data)
                        }
                        "municipal" -> {
                            _municipalDirectories.clear()
                            _municipalDirectories.addAll(result.data)
                        }
                        else -> {
                            _nationalDirectories.clear()
                            _nationalDirectories.addAll(result.data)
                        }
                    }
                }
                is Result.Error -> _errorMessage = result.exception.message
            }
        }
    }

     companion object {
         val Factory: ViewModelProvider.Factory = viewModelFactory {
             initializer {
                 val database = Firebase.firestore
                 EmergencyViewModel(EmergencyRepository(database))
             }
         }
     }
}