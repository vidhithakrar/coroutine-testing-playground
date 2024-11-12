package com.coroutine.testing.playground

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

interface Repository {
    val dataFlow: Flow<String>
    val sessionFlow: Flow<SessionType>

    suspend fun login()
    suspend fun logout()
}

enum class SessionType {
    LOGGED_IN,
    LOGGED_OUT
}

interface DataSource {
    suspend fun fetchData(): String
}

class RepositoryImpl(dataSource: DataSource) : Repository{
    override val dataFlow: Flow<String> = flow {
        while (currentCoroutineContext().isActive) {
            emit(dataSource.fetchData())
        }
    }

    private val _session = MutableSharedFlow<SessionType>()
    override val sessionFlow: Flow<SessionType> = _session.asSharedFlow()

    override suspend fun login() {
        _session.emit(SessionType.LOGGED_IN)
    }

    override suspend fun logout() {
        _session.emit(SessionType.LOGGED_OUT)
    }
}