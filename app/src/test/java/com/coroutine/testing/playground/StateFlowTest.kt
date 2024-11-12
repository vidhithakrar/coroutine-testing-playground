package com.coroutine.testing.playground

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class StateFlowTest {

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testStateFlow() = runTest {
        val repository = mock<Repository>()
        val dataFlow = MutableSharedFlow<String>()
        whenever(repository.dataFlow).thenReturn(dataFlow)

        val viewModel = MainViewModel(repository)

        val uiStates = mutableListOf<String>()
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect(uiStates::add)
        }

        dataFlow.emit("Data1")
        dataFlow.emit("Data2")

        assertThat(uiStates).isEqualTo(listOf(viewModel.initialUiState, "Data1-UI", "Data2-UI"))
        job.cancel()
    }

    @Test
    fun testSubsequentEmissionOnStateFlow() = runTest {
        val repository = mock<Repository>()
        whenever(repository.login()).thenReturn(Unit)

        val viewModel = MainViewModel(repository)

        val loginState = mutableListOf<String>()
        // This fails...
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.loginState.collect(loginState::add)
        }

        viewModel.login()

        assertThat(loginState).isEqualTo(listOf("Start", "Loading", "End"))
        job.cancel()
    }

    @Test
    fun testSubsequentEmissionOnStateFlowWithTurbine() = runTest {
        val repository = mock<Repository>()
        whenever(repository.login()).thenReturn(Unit)

        val viewModel = MainViewModel(repository)

        // This also fails...
        viewModel.loginState.test {
            assertEquals("Start", awaitItem())
            viewModel.login()
            assertEquals("Loading", awaitItem())
            assertEquals("End", awaitItem())
        }
    }

    @Test
    fun testSubsequentEmissionOnStateFlowWithImmediateTestDispatcher() = runTest {
        val repository = mock<Repository>()
        whenever(repository.login()).thenReturn(Unit)

        val viewModel = MainViewModel(repository)

        val loginState = mutableListOf<String>()
        val job = launch(ImmediateTestDispatcher()) {
            viewModel.loginState.collect(loginState::add)
        }

        viewModel.login()

        assertThat(loginState).isEqualTo(listOf("Start", "Loading", "End"))
        job.cancel()
    }

    @Test
    fun testSubsequentEmissionOnStateFlowWithDelay() = runTest {
        val repository = mock<Repository>()
        whenever(repository.login()).doSuspendableAnswer {
            delay(10)
        }

        val viewModel = MainViewModel(repository)

        assertThat(viewModel.loginState.value).isEqualTo("Start")
        viewModel.login()
        assertThat(viewModel.loginState.value).isEqualTo("Loading")
        advanceUntilIdle()
        assertThat(viewModel.loginState.value).isEqualTo("End")
    }

    @Test
    fun testSubsequentEmissionOnStateFlowWithDeferredResult() = runTest {
        val repository = mock<Repository>()
        val deferredResult = CompletableDeferred<Unit>()
        whenever(repository.login()).doSuspendableAnswer {
            deferredResult.await()
        }

        val viewModel = MainViewModel(repository)

        assertThat(viewModel.loginState.value).isEqualTo("Start")
        viewModel.login()
        assertThat(viewModel.loginState.value).isEqualTo("Loading")
        deferredResult.complete(Unit)
        assertThat(viewModel.loginState.value).isEqualTo("End")
    }
}