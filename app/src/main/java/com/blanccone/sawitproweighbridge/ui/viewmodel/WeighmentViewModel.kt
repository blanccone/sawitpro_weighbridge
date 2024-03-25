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
            _isLoading.postValue(it is Resource.Loading)
            when(it) {
                is Resource.Success -> _insertTicketSuccessful.postValue(ticket.id)
                is Resource.Error -> {
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
                is Resource.Success -> _insertImageSuccessful.postValue(true)
                is Resource.Error -> _error.postValue(it.message ?: unknownMsg())
                else -> Unit
            }
        }
    }

    private val _images = LiveEvent<List<WeightImage>>()
    val images: LiveData<List<WeightImage>> = _images
    fun getimages(ticketId: String) = viewModelScope.launch {
        persistenceRepository.getImages(ticketId).collectLatest {
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

    private val _updateTicketSuccessful = LiveEvent<Ticket?>()
    val updateTicketSuccessful: LiveData<Ticket?> = _updateTicketSuccessful
    fun updateTicket(ticket: Ticket) = viewModelScope.launch {
        persistenceRepository.updateTicket(ticket).collectLatest {
            _isLoading.postValue(it is Resource.Loading)
            when(it) {
                is Resource.Success -> _updateTicketSuccessful.postValue(ticket)
                is Resource.Error -> {
                    _updateTicketSuccessful.postValue(null)
                    _error.postValue(it.message ?: unknownMsg())
                }
                else -> Unit
            }
        }
    }

    private val _updateImageSuccessful = LiveEvent<WeightImage?>()
    val updateImageSuccessful: LiveData<WeightImage?> = _updateImageSuccessful
    fun updateImage(image: WeightImage) = viewModelScope.launch {
        persistenceRepository.updateImage(image).collectLatest {
            _isLoading.postValue(it is Resource.Loading)
            when(it) {
                is Resource.Success -> _updateImageSuccessful.postValue(image)
                is Resource.Error -> {
                    _updateImageSuccessful.postValue(null)
                    _error.postValue(it.message ?: unknownMsg())
                }
                else -> Unit
            }
        }
    }

    private val _tickets = LiveEvent<List<Ticket>>()
    val tickets: LiveData<List<Ticket>> = _tickets
    fun gettickets() = viewModelScope.launch {
        persistenceRepository.getTickets().collectLatest {
            _isLoading.postValue(it is Resource.Loading)
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
            _isLoading.postValue(it is Resource.Loading)
            when(it) {
                is Resource.Success -> _insertTicketsSuccessful.postValue(true)
                is Resource.Error -> _error.postValue(it.message ?: unknownMsg())
                else -> Unit
            }
        }
    }
}