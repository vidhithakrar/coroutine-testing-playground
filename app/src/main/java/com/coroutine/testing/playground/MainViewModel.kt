package com.coroutine.testing.playground

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: Repository) : ViewModel() {

    val initialUiState = "Fetching..."

    val uiState = repository.dataFlow.map {
        "$it-UI"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialUiState)

    private val _loginState = MutableStateFlow<String>("Start")
    val loginState = _loginState.asStateFlow()

    fun login() {
        viewModelScope.launch {
            _loginState.emit("Loading")
            repository.login()
            _loginState.emit("End")
        }
    }
}