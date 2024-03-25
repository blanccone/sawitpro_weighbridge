package com.blanccone.sawitproweighbridge.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.model.local.WeightImage
import com.blanccone.core.util.ViewUtils.unknownMsg
import com.blanccone.persistence.service.repository.PersistenceRepository
import com.hadilq.liveevent.LiveEvent
import com.technicaltest.core.service.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EFormWeighmentViewModel @Inject constructor(
    private val persistenceRepository: PersistenceRepository
): ViewModel() {

    private val _isLoading = LiveEvent<Boolean?>()
    val isLoading: LiveData<Boolean?> = _isLoading

    private val _error = LiveEvent<String?>()
    val error: LiveData<String?> = _error

    private val _insertSuccessful = LiveEvent<Boolean>()
    val insertSuccessful: LiveData<Boolean> = _insertSuccessful
    fun insertTicket(ticket: Ticket) = viewModelScope.launch {
        persistenceRepository.insertTicket(ticket).collectLatest {
            _isLoading.postValue(it is Resource.Loading)
            when(it) {
                is Resource.Success -> _insertSuccessful.postValue(true)
                is Resource.Error -> _error.postValue(it.message ?: unknownMsg())
                else -> Unit
            }
        }
    }

    private val _insertImageSuccessful = LiveEvent<Boolean>()
    val insertImageSuccessful: LiveData<Boolean> = _insertImageSuccessful
    fun insertImage(ticket: WeightImage) = viewModelScope.launch {
        persistenceRepository.insertImage(ticket).collectLatest {
            _isLoading.postValue(it is Resource.Loading)
            when(it) {
                is Resource.Success -> _insertImageSuccessful.postValue(true)
                is Resource.Error -> _error.postValue(it.message ?: unknownMsg())
                else -> Unit
            }
        }
    }

    private val _images = LiveEvent<List<WeightImage>>()
    val images: LiveData<List<WeightImage>> = _images
    fun getimages() = viewModelScope.launch {
        persistenceRepository.getImages().collectLatest {
            _isLoading.postValue(it is Resource.Loading)
            when(it) {
                is Resource.Success -> _images.postValue(it.data ?: emptyList())
                is Resource.Error -> {
                    _images.postValue(emptyList())
                    _error.postValue(it.message ?: unknownMsg())
                }
                else -> Unit
            }
        }
    }

    private val _updateSuccessful = LiveEvent<Ticket?>()
    val updateSuccessful: LiveData<Ticket?> = _updateSuccessful
    fun updateTicket(ticket: Ticket) = viewModelScope.launch {
        persistenceRepository.updateTicket(ticket).collectLatest {
            _isLoading.postValue(it is Resource.Loading)
            when(it) {
                is Resource.Success -> _updateSuccessful.postValue(ticket)
                is Resource.Error -> {
                    _updateSuccessful.postValue(null)
                    _error.postValue(it.message ?: unknownMsg())
                }
                else -> Unit
            }
        }
    }
}