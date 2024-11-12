package com.coroutine.testing.playground

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class FlowTest {

    @Test
    fun testProducer() = runTest {
        val dataSource = mock<DataSource>()
        whenever(dataSource.fetchData()).thenReturn("Data1", "Data2", "Data3")
        val repository = RepositoryImpl(dataSource)

        val dataResult = mutableListOf<String>()
        launch(UnconfinedTestDispatcher(this.testScheduler)) {
            repository.dataFlow.take(3).collect {
                dataResult.add(it)
            }
        }
        assertThat(dataResult).isEqualTo(listOf("Data1", "Data2", "Data3"))
    }

    @Test
    fun testConsumer() = runTest {
        val repository = mock<Repository>()
        val navigation = mock<Navigation>()

        val sessionFlow = MutableSharedFlow<SessionType>()
        whenever(repository.sessionFlow).thenReturn(sessionFlow)

        val sessionManager = SessionManager(backgroundScope, repository, navigation)

        sessionManager.manage()
        runCurrent()
        verifyNoInteractions(navigation)

        sessionFlow.emit(SessionType.LOGGED_IN)
        verify(navigation).goToHome()

        sessionFlow.emit(SessionType.LOGGED_OUT)
        verify(navigation).goBackToLogin()
    }
}