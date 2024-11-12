package com.coroutine.testing.playground

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SessionManager(
    private val coroutineScope: CoroutineScope,
    private val repository: Repository,
    private val navigation: Navigation
) {
    fun manage() {
        coroutineScope.launch {
            repository.sessionFlow.collect { sessionType ->
                when (sessionType) {
                    SessionType.LOGGED_IN -> navigation.goToHome()
                    SessionType.LOGGED_OUT -> navigation.goBackToLogin()
                }
            }
        }
    }
}

interface Navigation {
    fun goToHome()
    fun goBackToLogin()
}