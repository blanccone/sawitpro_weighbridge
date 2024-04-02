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
class WeighmentViewModel @Inject constructor(
    private val persistenceRepository: PersistenceRepository
): ViewModel() {

    private val _isLoading = LiveEvent<Boolean?>()
    val isLoading: LiveData<Boolean?> = _isLoading

    private val _error = LiveEvent<String?>()
    val error: LiveData<String?> = _error

    private val _insertTicketSuccessful = LiveEvent<String?>()
    val insertTicketSuccessful: LiveData<String?> = _insertTicketSuccessful
    fun insertTicket(ticket: Ticket) = viewModelScope.launch {
        persistenceRepository.insertTicket(ticket).collectLatest {
            when(it) {
                is Resource.Success -> {
                    _isLoading.postValue(false)
                    _insertTicketSuccessful.postValue(ticket.id)
                }
                is Resource.Error -> {
                    _isLoading.postValue(false)
                    _insertTicketSuccessful.postValue(null)
                    _error.postValue(it.message ?: unknownMsg())
                }
                else -> Unit
            }
        }
    }

    private val _insertImageSuccessful = LiveEvent<Boolean>()
    val insertImageSuccessful: LiveData<Boolean> = _insertImageSuccessful
    fun insertImage(image: WeightImage) = viewModelScope.launch {
        persistenceRepository.insertImage(image).collectLatest {
            _isLoading.postValue(it is Resource.Loading)
            when(it) {
                is Resource.Success -> {
                    _isLoading.postValue(false)
                    _insertImageSuccessful.postValue(true)
                }
                is Resource.Error -> {
                    _isLoading.postValue(false)
                    _insertImageSuccessful.postValue(false)
                    _error.postValue(it.message ?: unknownMsg())
                }
                else -> Unit
            }
        }
    }

    private val _images = LiveEvent<List<WeightImage>>()
    val images: LiveData<List<WeightImage>> = _images
    fun getimages(ticketId: String) = viewModelScope.launch {
        persistenceRepository.getImages(ticketId).collectLatest {
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

    private val _updateTicketSuccessful = LiveEvent<Boolean?>()
    val updateTicketSuccessful: LiveData<Boolean?> = _updateTicketSuccessful
    fun updateTicket(ticket: Ticket) = viewModelScope.launch {
        persistenceRepository.updateTicket(ticket).collectLatest {
            when(it) {
                is Resource.Success -> {
                    _isLoading.postValue(false)
                    _updateTicketSuccessful.postValue(true)
                }
                is Resource.Error -> {
                    _isLoading.postValue(false)
                    _updateTicketSuccessful.postValue(false)
                    _error.postValue(it.message ?: unknownMsg())
                }
                else -> Unit
            }
        }
    }

    private val _updateImageSuccessful = LiveEvent<Boolean?>()
    val updateImageSuccessful: LiveData<Boolean?> = _updateImageSuccessful
    fun updateImage(image: WeightImage) = viewModelScope.launch {
        persistenceRepository.updateImage(image).collectLatest {
            when(it) {
                is Resource.Success -> {
                    _isLoading.postValue(false)
                    _updateImageSuccessful.postValue(true)
                }
                is Resource.Error -> {
                    _isLoading.postValue(false)
                    _updateImageSuccessful.postValue(false)
                    _error.postValue(it.message ?: unknownMsg())
                }
                else -> Unit
            }
        }
    }

    private val _tickets = LiveEvent<List<Ticket>>()
    val tickets: LiveData<List<Ticket>> = _tickets
    fun getTickets() = viewModelScope.launch {
        persistenceRepository.getTickets().collectLatest {
            when(it) {
                is Resource.Success -> _tickets.postValue(it.data ?: emptyList())
                is Resource.Error -> {
                    _tickets.postValue(emptyList())
                    _error.postValue(it.message ?: unknownMsg())
                }
                else -> Unit
            }
        }
    }

    private val _insertTicketsSuccessful = LiveEvent<Boolean>()
    val insertTicketsSuccessful: LiveData<Boolean> = _insertTicketsSuccessful
    fun insertTickets(tickets: List<Ticket>) = viewModelScope.launch {
        persistenceRepository.insertTickets(tickets).collectLatest {
            when(it) {
                is Resource.Success -> _insertTicketsSuccessful.postValue(true)
                is Resource.Error -> _error.postValue(it.message ?: unknownMsg())
                else -> Unit
            }
        }
    }
}